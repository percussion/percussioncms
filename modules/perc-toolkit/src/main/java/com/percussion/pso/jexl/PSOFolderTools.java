/*******************************************************************************
 * com.percussion.pso.jexl PSOFolderTools.java
 * 
 * COPYRIGHT (c) 1999 - 2013 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * @author AdamGent
 * @author DavidBenua
 *
 */
package com.percussion.pso.jexl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSFolderProperty;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.pso.utils.PSOItemFolderUtilities;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.services.assembly.impl.nav.PSNavHelper;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

/**
 * JEXL functions for folder manipulation. 
 *
 * @author DavidBenua
 * @author AdamGent
 *
 */
public class PSOFolderTools extends PSJexlUtilBase implements IPSJexlExpression 
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSOFolderTools.class);
   private IPSContentWs contentWs;
   private IPSGuidManager guidManager;
   

   /**
    * Extensions manager will use this constructor.
    */
   public PSOFolderTools()
   {
      super();
   }
   
   
   /**
    * Preferred Constructor for programatic use outside jexl.
    * @param contentWs Content web service.
    * @param guidManager Guid Manager.
    */
   public PSOFolderTools(IPSContentWs contentWs, IPSGuidManager guidManager) {
       super();
       init(contentWs, guidManager);
   }
   
   protected void init(IPSContentWs contentWs, IPSGuidManager guidManager) {
       this.contentWs = contentWs;
       this.guidManager = guidManager;
   }
   
   
   
   /**
    * Get the folder path for an item. 
    * @param itemId the GUID for the item
    * @return the parent folder path. If there are multiple paths, the 
    * first one will be returned. Will be <code>null</code> if the item is
    * not in any folders. 
    * @throws PSErrorException
    * @throws PSExtensionProcessingException
 * @throws PSCmsException 
    */
   @IPSJexlMethod(description="get the folder path for this item", 
         params={@IPSJexlParam(name="itemId", description="the item GUID")})
   public String getParentFolderPath(IPSGuid itemId) 
   throws PSErrorException, PSExtensionProcessingException, PSCmsException   
   {
	  String errmsg; 
	  
	  List<String> paths = getParentFolderPaths(itemId);
      if(paths.size() == 0)
      {
         errmsg = "no paths returned for " + itemId; 
         log.info(errmsg);
         return null;
         //throw new PSExtensionProcessingException(0, errmsg); 
      }
      if(paths.size() == 1)
      {
         log.debug("found path " + paths.get(0));
         return paths.get(0); 

      }
      log.warn("multiple paths found for item " + itemId);
      return paths.get(0);
   }
   
   
   public PSFolder getFolder(String path){
	   PSFolder folder = null;
	   try {
		   if(path == null)
			   throw new RuntimeException("Path parameter cannot be null");
		  
		   folder = getContentWs().loadFolders(new String[] { path })
                   .get(0);
        
           if(folder == null ){
        	   throw new RuntimeException();
           }
        	   
       } catch (PSErrorResultsException e) {
           log.error("Could not locate Folder for: " + path, e);
           throw new RuntimeException(e);
       }catch (Exception e){
    	   log.error("An unexpected exception occurred while retrieving Folder for:" + path,e);
       }
	return folder;
   }
   

   
   
   
   /**
    * Gets the path of a folder containing this item. 
    * @param assemblyItem the assembly item whose parent folder will be fetched. 
    * @return the folder path of the containing folder. 
    * @throws PSErrorResultsException
    * @throws PSExtensionProcessingException
    * @throws PSErrorException
 * @throws PSCmsException 
    */
   @IPSJexlMethod(description="get the folder path for this item", 
           params={@IPSJexlParam(name="assemblyItem", description="$sys.assemblyItem")},
           returns="the path of the folder that contains this item"
   )
   public String getParentFolderPath(IPSAssemblyItem assemblyItem)
            throws PSErrorResultsException, PSExtensionProcessingException, PSErrorException, PSCmsException {
        int id = assemblyItem.getFolderId();
        String path = null;
        /*
         * If there is no folder id associated with the assembly item
         * (ie sys_folderid was not passed as a parameter) then we are going
         * to have to lookup the folder using the same process that managed nav
         * does.
         * Unfortunately this process is tightly coupled to Nav so we have
         * to instantiate the PSNavHelper class instead of using a service.
         * TODO Dave should look over this.
         */
        if (id <= 0) {
            log.debug("Assembly Item does not have a folder id.");
            if (assemblyItem instanceof PSAssemblyWorkItem) {
                PSAssemblyWorkItem awi = (PSAssemblyWorkItem) assemblyItem;
                PSNavHelper helper = awi.getNavHelper();
                if (helper != null) {
                    log.debug("Using NavHelper to find folder id.");
                    String errMesg = "Tried to use NavHelper to get parent folder path but failed!";
                    try {
                        IPSNode navNode = (IPSNode) helper.findNavNode(assemblyItem);
                        if (navNode != null) {
                            path = getParentFolderPath(navNode.getGuid());
                        }
                        else {
                            log.warn("Tried to use NavHelper to getParentFolderPath " +
                                    "but no navon could be found.");
                            path = null;
                        }
                    } catch (PSCmsException e) {
                        log.error(errMesg, e);
                        throw new RuntimeException(e);
                    } catch (PSFilterException e) {
                        log.error(errMesg,e);
                        throw new RuntimeException(e);
                    } 
                    catch (RepositoryException e) {
                        log.warn("Could not find folder using NavHelper: ", e);
                        path = null;
                    }
                }
                else {
                    log.debug("Could not use NavHelper to find folderid because the" +
                            " provided assembly item did not have one. (getNavHelper() == null)");
                    path = null;
                }
            }
        } 
        else {
            log.debug("Using AssemblyItem's folderid = " + id);
            path = getFolderPath(id);
            if (path==null) {
            	log.debug("Could not get folder path for id "+id);
            }
        }
        return path;
    }
   
   @IPSJexlMethod(description="Gets the folder properties of a folder.", 
           params={@IPSJexlParam(name="path", description="folder path")},
           returns="The folder properties (Map)"
   )
   @SuppressWarnings("unchecked")
    public Map<String, String> getFolderProperties(String path) {
        try {
            PSFolder folder = getContentWs().loadFolders(new String[] { path })
                    .get(0);
            Map<String, String> props = new HashMap<String, String>();
            Iterator<PSFolderProperty> it = folder.getProperties();
            while (it.hasNext()) {
                PSFolderProperty prop = it.next();
                props.put(prop.getName(), prop.getValue());
            }
            return props;
        } catch (PSErrorResultsException e) {
            log.error("Could not get folder properties for: " + path, e);
            throw new RuntimeException(e);
        }
    }
   /**
    * Get the folder paths Given a FolderID. 
    * @param id the id of the folder
    * @return the folder path. 
    * @throws PSCmsException 
    */  
   @IPSJexlMethod(description="Gets the folder path given a Folder ID", 
           params={@IPSJexlParam(name="id", description="folder id")},
           returns="The folder path (String)"
   )
   public String getFolderPath(int id) throws PSCmsException {
	  return PSOItemFolderUtilities.getFolderPath(id);
   }
  
   /**
    * Get the folder paths for an item. 
    * @param guid the GUID for the item
    * @return the parent folder path. If there are multiple paths, the 
    * first one will be returned. Will be return empty list if the item is
    * not in any folders. 
    * @throws PSCmsException 
    */
   @IPSJexlMethod(description="get the folder path for this item", 
         params={@IPSJexlParam(name="itemId", description="the item GUID")})
   public List<String> getParentFolderPaths(IPSGuid guid) throws PSCmsException {
	   return PSOItemFolderUtilities.getFolderPathsForItem(guid);
   }
   
   /***
    * Returns a lightweight list of the child items and folders of this item.
    */
   @IPSJexlMethod(description="get the child folders & items for this item", 
	         params={@IPSJexlParam(name="folderId", description="the folderid")})
	   public List<PSItemSummary> getChildFolders(int folderId) throws PSCmsException, PSErrorException {
	   
	   	List<PSItemSummary> ret = new ArrayList<PSItemSummary>();
	   	
	   	try{
	   		ret = contentWs.findFolderChildren(guidManager.makeGuid(folderId, PSTypeEnum.LEGACY_CONTENT),false);
	   	}catch(PSErrorException psex){
	   		log.error(psex.getLocalizedMessage());
	   	}catch(Exception e){
	   		log.error(e.getLocalizedMessage());
	   	}
	   	return ret;
	   
	   }
   
   
   /**
    * Get the folder id for a folder path. 
    * @param path the path for the item
    * @return content item id for the folder
    * @throws PSCmsException 
    */
   @IPSJexlMethod(description="get the folder id for this folder path", 
         params={@IPSJexlParam(name="path", description="The path to get the id for")})
   public int getIdForPath(String path) throws PSCmsException {
	   PSRequest req = (PSRequest) PSRequestInfo
       .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
	   PSServerFolderProcessor folderproc = new PSServerFolderProcessor(req,null);
	   int id = folderproc.getIdByPath(path);
	   return id;
   }
   
   /**
    * Get the folder id for a item id 
    * @param itemId the id to find the parent folder id for
    * @return content item id for the folder
    * @throws PSCmsException 
    */
   @IPSJexlMethod(description="get the parent folder id for this item id", 
	         params={@IPSJexlParam(name="itemId", description="The item id to find the folder id for")})
	   public int getParentFolderId(int itemId) throws PSCmsException {
	   	return PSOItemFolderUtilities.getParentFolderId(itemId);
	   }
	      
   
   @Override
    public void init(IPSExtensionDef def, File codeRoot)
            throws PSExtensionException {
        super.init(def, codeRoot);
        IPSContentWs contentWs = PSContentWsLocator.getContentWebservice();
        IPSGuidManager guidManager = PSGuidManagerLocator.getGuidMgr();
        init(contentWs, guidManager);
    }
   
   

    public IPSContentWs getContentWs() {
        return contentWs;
    }

    public void setContentWs(IPSContentWs contentWs) {
        this.contentWs = contentWs;
    }

    public IPSGuidManager getGuidManager() {
        return guidManager;
    }

    public void setGuidManager(IPSGuidManager guidManager) {
        this.guidManager = guidManager;
    }


	@IPSJexlMethod(description="Add a folder tree for the fully qualified path. ", 
	           params={@IPSJexlParam(name="path", description="folder path")},
	           returns="The newly added folders"
	   )

	    public PSFolder addFolderTree(String path) 
	{
         PSFolder folder = null;
         String[] stringArr = new String[1];
         stringArr[0]= path;
         try {
			folder = contentWs.loadFolders(stringArr).get(0);
		} catch (PSErrorResultsException e1) {
			log.info("Folder does not exist, creating new FolderTree for path: " + path, e1);
			try {
				folder = contentWs.addFolderTree(path).get(0);
			} catch (PSErrorResultsException e) {
				log.error("Could not generate new folder at path: " + path, e);
	            throw new RuntimeException(e);
			} catch (PSErrorException e) {
				log.error("Could not generate new folder at path: " + path, e);
	            throw new RuntimeException(e);
			}
		}
         
		
		return folder;
	       
	    }
	@IPSJexlMethod(description="Add a folder below the specified Parent folder", 
			params={@IPSJexlParam(name="folderName", description="Name of the folder to create"),
			@IPSJexlParam(name="parent", description="folder path to create new folder in")},
			returns="The newly added folder"
	)
	
	public PSFolder addFolder(String folderName, String parent) 
	{
		PSFolder folder = null;

		try {
			folder = contentWs.addFolder(folderName, parent);
		}  catch (PSErrorException e) {
			log.error("Could not generate new folder '" + folderName + "' at path: " + parent, e);
			throw new RuntimeException(e);
		}
		return folder;
		
	}
}
