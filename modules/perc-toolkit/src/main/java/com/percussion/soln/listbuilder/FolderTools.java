package com.percussion.soln.listbuilder;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSFolderProperty;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.services.assembly.impl.nav.PSNavHelper;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
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
public class FolderTools extends PSJexlUtilBase implements IPSJexlExpression 
{
   /**
    * Logger for this class
    */
   private static final Logger log = LogManager.getLogger(FolderTools.class);
   private IPSContentWs contentWs;
   private IPSGuidManager guidManager;
   

   /**
    * Extensions manager will use this constructor.
    */
   public FolderTools()
   {
      super();
   }
   
   
   /**
    * Preferred Constructor for programatic use outside jexl.
    * @param contentWs Content web service.
    * @param guidManager Guid Manager.
    */
   public FolderTools(IPSContentWs contentWs, IPSGuidManager guidManager) {
       super();
       init(contentWs, guidManager);
   }
   
   protected void init(IPSContentWs contentWs, IPSGuidManager guidManager) {
       this.contentWs = contentWs;
       this.guidManager = guidManager;
   }
   
   
   /**
    * Get the folder paths for an item. 
    * @param itemId the GUID for the item
    * @return the parent folder path. If there are multiple paths, the 
    * first one will be returned. Will be return empty list if the item is
    * not in any folders. 
    * @throws PSErrorException
    * @throws PSExtensionProcessingException
    */
   @IPSJexlMethod(description="get the folder path for this item", 
         params={@IPSJexlParam(name="itemId", description="the item GUID")})
   public List<String> getParentFolderPaths(IPSGuid itemId) 
   throws PSErrorException, PSExtensionProcessingException   
   {
      String errmsg; 
      if(itemId == null)
      {
         errmsg = "No path for null guid"; 
         log.error(errmsg); 
         throw new PSExtensionProcessingException(0, errmsg); 
      }
      String[] paths = null;
      try
      {
         paths = contentWs.findFolderPaths(itemId);
      } catch (Exception e)
      {
        log.error("Unexpected exception " + e.getMessage(), e );
        throw new PSExtensionProcessingException(this.getClass().getCanonicalName(), e); 
      } 
      if(paths == null)
      {
         errmsg = "cannot find folder path for " + itemId; 
         log.info(errmsg);
         return new ArrayList<String>(); 
         //throw new PSExtensionProcessingException(0, errmsg); 
      }
      
      return Arrays.asList(paths);
     
   }
   
   /**
    * Get the folder path for an item. 
    * @param itemId the GUID for the item
    * @return the parent folder path. If there are multiple paths, the 
    * first one will be returned. Will be <code>null</code> if the item is
    * not in any folders. 
    * @throws PSErrorException
    * @throws PSExtensionProcessingException
    */
   @IPSJexlMethod(description="get the folder path for this item", 
         params={@IPSJexlParam(name="itemId", description="the item GUID")})
   public String getParentFolderPath(IPSGuid itemId) 
   throws PSErrorException, PSExtensionProcessingException   
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

   /**
    * Gets the path of a folder containing this item. 
    * @param assemblyItem the assembly item whose parent folder will be fetched. 
    * @return the folder path of the containing folder. 
    * @throws PSErrorResultsException
    * @throws PSExtensionProcessingException
    * @throws PSErrorException
    */
   @IPSJexlMethod(description="get the folder path for this item", 
           params={@IPSJexlParam(name="assemblyItem", description="$sys.assemblyItem")},
           returns="the path of the folder that contains this item"
   )
   public String getParentFolderPath(IPSAssemblyItem assemblyItem)
            throws PSErrorResultsException, PSExtensionProcessingException, PSErrorException {
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
            IPSGuid folderGuid = guidManager.makeGuid(new PSLocator(id, -1));
            List<PSFolder> folders = contentWs.loadFolders(asList(folderGuid));
            if (folders.size() < 1) {
                path = null;
            } 
            else {
                path = folders.get(0).getFolderPath();
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
   
}
