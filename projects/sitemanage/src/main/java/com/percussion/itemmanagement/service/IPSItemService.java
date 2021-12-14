/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.itemmanagement.service;

import com.percussion.itemmanagement.data.PSAssetSiteImpact;
import com.percussion.itemmanagement.data.PSItemDates;
import com.percussion.itemmanagement.data.PSRevisionsSummary;
import com.percussion.itemmanagement.data.PSSoProMetadata;
import com.percussion.services.useritems.data.PSUserItem;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.webservices.PSErrorResultsException;

import java.util.List;
import java.util.Map;


/**
 * This interface handles all item operations, i.e., get revisions.
 * 
 * @author Jose Annunziato
 *
 */
public interface IPSItemService
{
    /**
     * Retrieves the revisions for a given page or asset.
     * 
     * @param id is the full id of the page or asset, must not be blank
     * 
     * @return a list of revisions, never null, may be empty
     * 
     * @throws PSItemServiceException if an error occurs.
     */
    public PSRevisionsSummary getRevisions(String id) throws PSItemServiceException;
    
    /**
     * Retrieves the start and end date for a given page or asset resource. 
     * Returned dates are in the following format: MM/dd/yyyy HH:mm
     * 
     * @param id is the full id of the page or asset, must not be blank
     * 
     * @return object with start date, end date, and item id, never null, may be empty
     * 
     * @throws PSItemServiceException if an error occurs.
     */
    public PSItemDates getItemDates(String id) throws PSItemServiceException;
    
    /**
     * Sets the start and end date for a given page or asset resource. 
     * Dates must be in the following format: MM/dd/yyyy HH:mm
     * 
     * @param req - PSItemdate object with itemId set, and start and end dates values to set/replace. 
     *  
     * @return PSNoContent with a successful save message.
     * 
     * @throws PSItemServiceException if an error occurs.
     */
    public PSNoContent setItemDates(PSItemDates req) throws Exception;
    
    /**
     * Retrieves the social promotion metadata for a given page.
     * 
     * @param id the full id of the page or asset, must not be blank
     * @return object with the item id and the metadata for that id
     * @throws PSItemServiceException
     */
    public PSSoProMetadata getSoProMetadata(String id) throws PSItemServiceException;
    
    /**
     * Sets the social promotion metadata for a given page.
     * 
     * @param req PSSoProMetadata object with itemId set, and the metadata value to set/replace. 
     * @return PSNoContent with a successful save message.
     * @throws PSItemServiceException
     */
    public PSNoContent setSoProMetadata(PSSoProMetadata req) throws PSItemServiceException;
    
    /**
     * Restores a prior revision of an item. 
     * If the item is checked out by the current user then checks the item in, if the item is in Live or Pending 
     * state then moves the item to quick edit state before restoring the older revision.
     * If the item has local content, restores the local content from that revision. After restoring adjusts the parent 
     * local content relationships.
	 * Validates whether the item can be restored from a prior revision or not, if not throws an exception.
	 * The item can't be restored in one of the following three cases. 
     * 1. User has read access to the folder.
     * 2. User has read or none access to the item.
     * 3. Item is checked out by someone else.
     * 
     * @param id The id of the item must not be blank. Expects the string format of the item guid. The revision part of 
     * the guid must be valid prior revision.
     * @return an object of PSNoContent.
     * @throws PSItemServiceException if the item is not valid for restoring from a prior revision.
     */
    public PSNoContent restoreRevision(String id) throws PSItemServiceException;
       
    /**
     * Copies all contents of the specified folder to a folder under the specified location.  Currently, only folders in
     * the Assets Library may be copied.
     * @param srcFolder the path of the source folder, may not be <code>null</code> or empty.
     * @param destFolder the path of the destination folder, may not be <code>null</code> or empty.
     * @param name of the folder to which the contents of the source folder will be copied, may not be <code>null</code>
     * or empty.
     *
     */
    public Map<String,String> copyFolder(String srcFolder, String destFolder, String name) throws PSItemServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException, PSErrorResultsException, PSDataServiceException;
    
    /**
     * Calculates the site impact of an asset and returns its result as a String representation of a JSONObject, the json object will have
     * three arrays.
     * {
     * "pages":[PSItemProperties],
     * "templates":[{"template":PSTemplateSummary, "site":sitename}],
     * }
     * pages is an array of PSItemProperties objects of all the pages that the asset is related to.
     * templates is an array of objects that consist of PSTemplateSummary object and site name of all the templates the asset is related to.
     * If the asset is related to another asset(case of inline images and file links) then it's parent page or template is returned as impacted item.
     * If the parent asset is not on any page or template then it is ignored.
     * If the asset is not used on any page or template then empty arrays are returned for pages and templates.
     * @param assetId The id of the asset in the string format of the guid, must not be blank and must be a valid asset id.
     * @return String representation of the JSONObject never <code>null</code>, see the description.
     */
    public String getAssetSiteImpact(String assetId);
    
    /**
     * Adds a page to logged in users my pages.
     * @param pageId guid of the page id, must not be blank.
     * @return PSNoContent with successful page added message
     */
    public PSNoContent addToMyPages(String pageId);
    
    /***
     * Adds a page to the psecified users my pages
     * @param userName The userName of the user
     * @param pageId The guid of the Page
     * @return PSNoContent with successful page added message
     */
    public PSNoContent addToMyPages(String userName, String pageId);
    
    /**
     * Removes a page from logged in users my pages.
     * @param pageId guid of the page id, must not be blank.
     * @return PSNoContent with successful page removed message
     */
    public PSNoContent removeFromMyPages(String pageId);
    
    /**
     * Checks whether the supplied pageid is a user page or not.
     * @param pageId guid of the page id, must not be blank.
     * @return true if the page is in currently logged in users pages, otherwise false.
     */
    public boolean isMyPage(String pageId);
    
    /**
     * Finds all user items associated with the supplied user name 
     * @param userName name of the user if blank or items doesn't exist returns empty list.
     * @return list of user items, may be empty, never <code>null</code>
     */
    public List<PSUserItem> getUserItems(String userName);
    
    /**
     * Finds all user items associated with the supplied item id
     * @param itemId assumed to be a valid contentid (raw)
     * @return list of user items, may be empty, never <code>null</code>
     */
    public List<PSUserItem> getUserItems(int itemId);
    
    /**
     * Adds a user item for the given user name and item id, if an entry with same details
     * already exists logs a warning ignores the request.
     * @param userName must not be blank.
     * @param itemId assumed to be a valid contentid (raw)
     * @param type user item type
     */
    public void addUserItem(String userName, int itemId, PSUserItemTypeEnum type) throws IPSGenericDao.SaveException;
    
    /**
     * Removes a user item corresponding to the supplied user name and item id, if no user item
     * exists with the supplied user name and item id logs the warning and ignores the request.
     * @param userName name of the user, must not be blank
     * @param itemId assumed to be a valid contentid (raw)
     */
    public void removeUserItem(String userName, int itemId);
    
    /**
     * Deletes all user item entries corresponding to the supplied itemId, if no entries exist
     * does nothing.
     * @param itemId assumed to be a valid contentid (raw)
     */
    public void deleteUserItems(int itemId);
    
    /**
     * Deletes all user item entries corresponding to the supplied user name, if no entries exist
     * does nothing.
     * @param userName must not be blank.
     */
    public void deleteUserItems(String userName);
    
    /**
    * Cleanup assets folder on error
    * 
    * @param itemMap  map from copyFolder contains all known id maps until error. 
    * @param folderName  Root destination folder
    * @throws PSItemServiceException
    */
    public void rollBackCopiedFolder(Map<String, String> itemMap, String folderName) throws PSItemServiceException;

    /**
     * Finds all user items associated with the logged in user 
     * @return list of user items, may be empty, never <code>null</code>
     */
    public List<PSItemProperties> getMyContent();
    
    /**
     * Thrown when an error is encountered in the item service.
     * 
     * @author Jose Annunziato
     *
     */
    public static class PSItemServiceException extends Exception
    {
        private static final long serialVersionUID = 1L;
        
        public PSItemServiceException()
        {
            super();
        }

        public PSItemServiceException(String message)
        {
            super(message);
        }
        
        public PSItemServiceException(String message, Throwable cause)
        {        
            super(message, cause);
        }
        
        public PSItemServiceException(Throwable cause)
        {
            super(cause);
        }     
    }
    
    /**
     * The item type enum used as the type for user items.
     */
    public enum PSUserItemTypeEnum{
        FAVORITE_PAGE
    }

    /***
     * 
     * @param assetIds  A list of asset id's to check site impact for
     * @return Returns a list of PSAssetSiteImpact items
     */
	public List<PSAssetSiteImpact> getAssetSiteImpact(List<String> assetIds);


}
