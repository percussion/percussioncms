/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.validation;

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.data.PSConversionException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldValidator;
import com.percussion.extension.PSExtensionException;
import com.percussion.pso.utils.PSOExtensionParamsHelper;
import com.percussion.pso.utils.PSONodeCataloger;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;

/**
 * This is a field validator that checks whether or not a 
 * field is unique for all the folders that the item resides
 * (or to be created) in.
 * <p>
 * There are 3 parameters:
 * <ul>
 * <li>The field value - defaults to the current value of the field.</li>
 * <li>The field name - defaults to the name of the current field.</li>
 * <li>Exclude Promotable Versions flag -- specify <code>true</code> or <code>false</code></li>
 * </ul> 
 * 
 * See the <code>Extensions.xml</code> for more information.
 * @author adamgent
 *
 */
public class PSOUniqueFieldWithInFoldersValidator implements IPSFieldValidator {

    private IPSExtensionDef extensionDef = null;
    private IPSContentWs contentWs = null;
    private IPSGuidManager guidManager = null;
    private IPSContentMgr contentManager = null;
    private PSONodeCataloger nodeCataloger = null;
    private IPSSystemWs systemWs = null; 
    
    public Boolean processUdf(Object[] params, IPSRequestContext request)
            throws PSConversionException {
        String cmd = request.getParameter(IPSHtmlParameters.SYS_COMMAND);
        String actionType = request.getParameter("DBActionType");
        if(actionType == null || 
           !(actionType.equals("INSERT") || actionType.equals("UPDATE")))
           return true;
        
        PSOExtensionParamsHelper h = new PSOExtensionParamsHelper(getExtensionDef(), params, request, log);
        String fieldName = h.getRequiredParameter("fieldName");
        
        String fieldValue = request.getParameter(fieldName);
        if (fieldValue == null) {
            log.debug("Field value was null for field: " + fieldName);
            return true;
        }
        boolean xpv = h.getOptionalParameterAsBoolean("excludePromotableVersions", false);
        String checkPaths = h.getOptionalParameter("checkPaths", null);
        
        Number contentId = new Integer(0);
        try {
           if (actionType.equals("UPDATE")) {
               contentId = h.getRequiredParameterAsNumber("sys_contentid");
           }
           if(xpv)
           {
              log.debug("excluding promotable versions");
             
              //sys_command is modify if this is a user update, not a clone, new copy
              //or new version.
              if(StringUtils.isNotBlank(cmd) && !cmd.equalsIgnoreCase("modify"))
              {
                 log.debug("command is not modify - " + cmd); 
                 return true;
              }
                 
              if(isPromotable(contentId.intValue()))
              {
                 return true; 
              }
           }


           String typeList = this.makeTypeList(fieldName);
           boolean rvalue = true;
           if (actionType.equals("UPDATE")) {
              rvalue = isFieldValueUniqueInFolderForExistingItem(contentId.intValue(), fieldName, fieldValue, typeList, checkPaths);
           }
           else {
              Number folderId = getFolderId(request);
              if (folderId != null)
                 rvalue = isFieldValueUniqueInFolder(folderId.intValue(), fieldName, fieldValue, typeList, checkPaths);
              else
                 rvalue = false;
           }
           return rvalue;
        } catch (Exception e) {
           log.error(format("An error happend while checking if " +
                 "fieldName: {0} was unique for " +
                 "contentId: {1} with " +
                 "fieldValue: {2}",
                 fieldName, request.getParameter("sys_contentid"), fieldValue), e);
           return false;
        }
    }
    
    /**
     * See if a field value is unique in all the folders that the given existing item resides. 
     * @param contentId id of the item.
     * @param fieldName field name to check for uniqueness.
     * @param fieldValue the value of the field.
      * @return true if its unique
     * @throws PSErrorException
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    public boolean isFieldValueUniqueInFolderForExistingItem(int contentId, String fieldName, String fieldValue, String typeList)
    throws PSErrorException, InvalidQueryException, RepositoryException {
    	return this.isFieldValueUniqueInFolderForExistingItem(contentId, fieldName, fieldValue, typeList,null);
    }
   
    /**
     * See if a field value is unique in all the folders that the given existing item resides. 
     * @param contentId id of the item.
     * @param fieldName field name to check for uniqueness.
     * @param fieldValue the value of the field.
     * @param  path If set will check this path for uniqueness,  can use % to check subfolders
     * @return true if its unique
     * @throws PSErrorException
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    public boolean isFieldValueUniqueInFolderForExistingItem(int contentId, String fieldName, String fieldValue, String typeList, String path) 
        throws PSErrorException, InvalidQueryException, RepositoryException {
    	List<String> paths = new ArrayList<String>();
        boolean unique = true;
        IPSGuid guid = guidManager.makeGuid(new PSLocator(contentId, -1));
        List<String> itemPaths = Arrays.asList(contentWs.findFolderPaths(guid)); 
        if (path!=null) {
        	String[] pathSplit = path.split(",");
        	for(String paramPath : pathSplit) {
    			for(String itemPath : itemPaths) {
    				if (itemPath.startsWith(paramPath)) {
    					paths.add(paramPath+"/%");
    				}
    			}
    		}
        	
        } else {
        	paths = itemPaths; 
        }
        if (paths != null && paths.size() != 0) {
        	for (String pathItem : paths ) {
        		if (unique) {
		            String jcrQuery = getQueryForValueInFolders(contentId, fieldName, fieldValue, pathItem, typeList);
		            log.trace(jcrQuery);
		            Query q = contentManager.createQuery(jcrQuery, Query.SQL);
		            QueryResult results = contentManager.executeQuery(q, -1, null, null);
		            RowIterator rows = results.getRows();
		            long size = rows.getSize();
		            unique = size > 0 ? false : true;
        		}
        	}
        }
        else {
            log.debug("The item: " + contentId + " is not in any folders");
        }
        
        return unique;
    
    }
    
    /**
     *  See if a field value is unique in the given folder for a new item.
     * @param folderId id of the folder
     * @param fieldName name of the field
     * @param fieldValue the desired value of the field for the new item.
     * @return true if its unique.
     * @throws PSErrorException
     * @throws InvalidQueryException
     * @throws RepositoryException
     * @throws PSErrorResultsException
     */
    public boolean isFieldValueUniqueInFolder(int folderId, String fieldName, String fieldValue, String typeList)
    throws PSErrorException, InvalidQueryException, RepositoryException, PSErrorResultsException {
    	return this.isFieldValueUniqueInFolder(folderId, fieldName, fieldValue, typeList, null);
	}
    /**
     *  See if a field value is unique in the given folder for a new item.
     * @param folderId id of the folder
     * @param fieldName name of the field
     * @param fieldValue the desired value of the field for the new item.
     * @param  path If set will check this path for uniqueness,  can use % to check subfolders
     * @return true if its unique.
     * @throws PSErrorException
     * @throws InvalidQueryException
     * @throws RepositoryException
     * @throws PSErrorResultsException
     */
    public boolean isFieldValueUniqueInFolder(int folderId, String fieldName, String fieldValue, String typeList, String path)
    throws PSErrorException, InvalidQueryException, RepositoryException, PSErrorResultsException {
    	boolean unique = true;
    	List<String> paths = new ArrayList<String>();
    	IPSGuid guid = guidManager.makeGuid(new PSLocator(folderId, -1));
		List<PSFolder> folders = contentWs.loadFolders(asList(guid));
    	if (path == null) {
    		path = ! folders.isEmpty() ? folders.get(0).getFolderPath() : null;
    		paths.add(path);
    	} else {
    		String[] pathSplit = path.split(",");
    		for(String paramPath : pathSplit) {
    			for(PSFolder folder : folders) {
    				if (folder.getFolderPath().startsWith(paramPath)) {
    					paths.add(paramPath+"/%");
    				}
    			}
    		}
    	}
    	if (paths != null  && paths.size() != 0) {
    		for (String pathItem : paths ) {
    			if (unique ) {
    				String jcrQuery = getQueryForValueInFolder(fieldName, fieldValue, pathItem, typeList);
    				log.trace(jcrQuery);
    				Query q = contentManager.createQuery(jcrQuery, Query.SQL);
    				QueryResult results = contentManager.executeQuery(q, -1, null, null);
    				RowIterator rows = results.getRows();
    				long size = rows.getSize();
    				
    				unique = size > 0 ? false : true;
    			}
    		}
    	}
    	else {
    		log.error("The folder id: " + folderId + " did not have a path (BAD)");
    	}

    	return unique;
    }

    public String getQueryForValueInFolders(
            int contentId, 
            String fieldName, 
            String fieldValue, 
            String path,
            String typeList) {
        String jcrQuery = format(
                "select rx:sys_contentid, rx:{0} " +
                "from {4} " +
                "where " +
                "rx:sys_contentid != {1} " +
                "and " +
                "rx:{0} = ''{2}'' " +
                "and " +
                "jcr:path like ''{3}''", 
                fieldName, ""+contentId, 
                fieldValue, path, typeList);
        return jcrQuery;
    }
    
    public String getQueryForValueInFolder(String fieldName, String fieldValue, String path, String typeList) {
        return format(
                "select rx:sys_contentid, rx:{0} " +
                "from {3} " +
                "where " +
                "rx:{0} = ''{1}'' " +
                "and " +
                "jcr:path like ''{2}''", 
                fieldName, fieldValue, path, typeList);
    }
    
    protected Integer getFolderId(IPSRequestContext request) {
        // get the target parent folder id from the redirect url
        String folderId = null;
        Integer rvalue = null;
        String psredirect = request.getParameter(
           IPSHtmlParameters.DYNAMIC_REDIRECT_URL);
        if (psredirect != null && psredirect.trim().length() > 0)
        {
           int index = psredirect.indexOf(IPSHtmlParameters.SYS_FOLDERID);
           if(index >= 0)
           {
              folderId = psredirect.substring(index +
                 IPSHtmlParameters.SYS_FOLDERID.length() + 1);
              index = folderId.indexOf('&');
              if(index > -1)
                 folderId = folderId.substring(0, index);
           }
        }
        if (StringUtils.isNumeric(folderId) && StringUtils.isNotBlank(folderId)) {
            rvalue = Integer.parseInt(folderId);
        
        }
        
        return rvalue;
    }

    protected String makeTypeList(String fieldname) throws RepositoryException
    {
       List<String> types = nodeCataloger.getContentTypeNamesWithField(fieldname);
       StringBuilder sb = new StringBuilder();
       boolean first = true;
       for(String t : types)
       {
          if(!first)
          {
             sb.append(", ");
          }
          sb.append(t);
          first = false;
       }
       return sb.toString();
    }
    
    /**
     * Is this item a promotable version.  Examines the relationships to determine if this item is a 
     * promotable version or not. 
     * @param contentid the content id for the item 
     * @return <code>true</code> if a PV relationship is found. 
     * @throws PSErrorException
     */
    protected boolean isPromotable(int contentid) throws PSErrorException
    {
       if(contentid == 0)
       {
          log.debug("no PV for content id 0");
          return false; 
       }
       PSLocator loc = new PSLocator(contentid);
       PSRelationshipFilter filter = new PSRelationshipFilter();
       filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_PROMOTABLE); 
       filter.setDependent(loc); 
      
       List<PSRelationship> rels = systemWs.loadRelationships(filter);
       log.debug("there are " + rels.size() + " PV relationships");
       
       return (rels.size() > 0) ? true : false; 
    }
    
    public void init(IPSExtensionDef extensionDef, File arg1)
            throws PSExtensionException {
        setExtensionDef(extensionDef);
        if (contentManager == null) setContentManager(PSContentMgrLocator.getContentMgr());
        if (contentWs == null) setContentWs(PSContentWsLocator.getContentWebservice());
        if (guidManager == null) setGuidManager(PSGuidManagerLocator.getGuidMgr());
        if (nodeCataloger == null) setNodeCataloger(new PSONodeCataloger());
        if (systemWs == null) setSystemWs(PSSystemWsLocator.getSystemWebservice()); 
    }

    public IPSExtensionDef getExtensionDef() {
        return extensionDef;
    }

    public void setExtensionDef(IPSExtensionDef extensionDef) {
        this.extensionDef = extensionDef;
    }

    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(PSOUniqueFieldWithInFoldersValidator.class);

    public void setContentWs(IPSContentWs contentWs) {
        this.contentWs = contentWs;
    }

    public void setGuidManager(IPSGuidManager guidManager) {
        this.guidManager = guidManager;
    }

    public void setContentManager(IPSContentMgr contentManager) {
        this.contentManager = contentManager;
    }

   /**
    * @param nodeCataloger the nodeCataloger to set
    */
   public void setNodeCataloger(PSONodeCataloger nodeCataloger)
   {
      this.nodeCataloger = nodeCataloger;
   }

   /**
    * @param systemWs the systemWs to set
    */
   public void setSystemWs(IPSSystemWs systemWs)
   {
      this.systemWs = systemWs;
   }
}
