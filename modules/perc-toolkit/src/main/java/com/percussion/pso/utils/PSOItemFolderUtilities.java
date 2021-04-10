package com.percussion.pso.utils;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.utils.guid.IPSGuid;

import java.util.Arrays;
import java.util.List;


/***
 * Utility class to hold common and useful methods related to 
 * folders and items. 
 * 
 * @author natechadwick
 *
 */
public class PSOItemFolderUtilities {

	private PSOItemFolderUtilities(){
		//Force static access
	}
	
	/***
	 * Given an item's GUID will return all of the folder paths that
	 * the item belongs in. 
	 * 
	 * @param guid A valid Guid, must not be null
	 * @return An array of strings containing the folder paths. 
	 * @throws PSCmsException Exception if one occurred
	 */
	public static List<String> getFolderPathsForItem(IPSGuid guid) throws PSCmsException{
	
			   PSServerFolderProcessor folderproc =  PSServerFolderProcessor.getInstance();
			   String[] ret = folderproc.getFolderPaths(new PSLocator(guid.getUUID(),-1));
			   return  Arrays.asList(ret);
	}
	
	 public static String getFolderPath(int id) throws PSCmsException, PSNotFoundException {
		   PSServerFolderProcessor folderproc =  PSServerFolderProcessor.getInstance();
		   String[] ret = folderproc.getItemPaths(new PSLocator(id,-1));
		   return  (ret.length>0) ? ret[0] : null;
	   }

	public static int getParentFolderId(int itemId) throws PSCmsException, PSNotFoundException {
		   PSServerFolderProcessor folderproc =  PSServerFolderProcessor.getInstance();
		   return folderproc.getIdByPath(getFolderPath(itemId));
	}
	
	/***
	 * Returns the Path of the containing folder. 
	 * 
	 * @param id The Content ID of the item
	 * @return
	 * @throws PSCmsException
	 */
	 public static String getItemFolderPath(int id) throws PSCmsException, PSNotFoundException {
		   PSServerFolderProcessor folderproc = PSServerFolderProcessor.getInstance();
		   String[] ret = folderproc.getItemPaths(new PSLocator(id,-1));
		   
		   if(ret.length>0){
			   ret[0] = ret[0].substring(0, ret[0].lastIndexOf("/"));
		   }
		   
		   return  (ret.length>0) ? ret[0] : null;
	   }
	
	 public static int getItemParentFolderId(int itemId) throws PSCmsException, PSNotFoundException {
		   PSServerFolderProcessor folderproc = PSServerFolderProcessor.getInstance();
		   return folderproc.getIdByPath(getItemFolderPath(itemId));
	}
	
}

