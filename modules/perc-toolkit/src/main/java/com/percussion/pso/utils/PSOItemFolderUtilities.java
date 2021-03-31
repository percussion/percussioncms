package com.percussion.pso.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;


/***
 * Utility class to hold common and useful methods related to 
 * folders and items. 
 * 
 * @author natechadwick
 *
 */
public class PSOItemFolderUtilities {
	
	
	/***
	 * Given an item's GUID will return all of the folder paths that
	 * the item belongs in. 
	 * 
	 * @param guid A valid Guid, must not be null
	 * @return An array of strings containing the folder paths. 
	 * @throws PSCmsException
	 */
	public static List<String> getFolderPathsForItem(IPSGuid guid) throws PSCmsException{
	  PSRequest req = (PSRequest) PSRequestInfo
		       .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
	
			   PSServerFolderProcessor folderproc = new PSServerFolderProcessor(req,null);
			   String[] ret = folderproc.getFolderPaths(new PSLocator(guid.getUUID(),-1));
			   return  Arrays.asList(ret);
	}
	
	 public static String getFolderPath(int id) throws PSCmsException {
		   PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
		   PSServerFolderProcessor folderproc = new PSServerFolderProcessor(req,null);
		   String[] ret = folderproc.getItemPaths(new PSLocator(id,-1));
		   return  (ret.length>0) ? ret[0] : null;
	   }

	public static int getParentFolderId(int itemId) throws PSCmsException {
		   PSRequest req = (PSRequest) PSRequestInfo
	       .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
		   PSServerFolderProcessor folderproc = new PSServerFolderProcessor(req,null);
		   int id = folderproc.getIdByPath(getFolderPath(itemId));
		   return id;
	}
	
	/***
	 * Returns the Path of the containing folder. 
	 * 
	 * @param id The Content ID of the item
	 * @return
	 * @throws PSCmsException
	 */
	 public static String getItemFolderPath(int id) throws PSCmsException {
		   PSRequest req = (PSRequest) PSRequestInfo
	       .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
		   PSServerFolderProcessor folderproc = new PSServerFolderProcessor(req,null);
		   String[] ret = folderproc.getItemPaths(new PSLocator(id,-1));
		   
		   if(ret.length>0){
			   ret[0] = ret[0].substring(0, ret[0].lastIndexOf("/"));
		   }
		   
		   return  (ret.length>0) ? ret[0] : null;
	   }
	
	 public static int getItemParentFolderId(int itemId) throws PSCmsException {
		   PSRequest req = (PSRequest) PSRequestInfo
	       .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
		   PSServerFolderProcessor folderproc = new PSServerFolderProcessor(req,null);
		   int id = folderproc.getIdByPath(getItemFolderPath(itemId));
		   return id;
	}
	
}

