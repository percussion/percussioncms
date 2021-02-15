/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.share.dao;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.pathmanagement.data.PSFolderPermission;
import com.percussion.pathmanagement.data.PSFolderProperties;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;

import java.util.Date;
import java.util.List;

/**
 * A wrapper around {@link IPSContentWs} to add items to folders
 * and removing items from folders.
 * <p>
 * The methods mainly throw checked exceptions due to the underlying architecture.
 * So conversion to other exceptions will have to be done in another layer.
 * This is not ideal but for now works.
 * <p>
 * Behavior of this class is also not transactional so do not expect rollbacks.
 * This limitation will have to be looked at a later date.
 *  
 * @author adamgent
 *
 */
public interface IPSFolderHelper {
    
    /**
     * The path separator used in CM system paths.
     * This will almost always be '<code>/</code>'.
     * @return never <code>null</code> or empty.
     */
    public String pathSeparator();
    
    /**
     * Adds an item to a particular folder path, creating missing
     * folders if they do not exist in the path.
     * Thus a path of:
     * <pre>
     *  //a/b/c
     * </pre>
     * Would create folders //a, //a/b, and //a/b/c if they were not created yet.
     * @param path never <code>null</code> or empty.
     * @param id never <code>null</code> or empty.
     * @throws Exception if the item cannot be added to the path.
     */
    public void addItem(String path, String id) throws Exception;
    
    /**
     * Gets a folder path for legacy folder id or fails. 
     * @param id the legacy content id (sys_contentid) of a folder, never <code>null</code>.
     * @return never <code>null</code> or empty.
     * @throws Exception if no folder path can be found.
     */
    public String findPathFromLegacyFolderId(Number id) throws Exception;
    
    /**
     * The inverse of {@link #findPathFromLegacyFolderId(Number)}.
     * @param path a valid folder path, never <code>null</code> or empty.
     * @return legacy content id (sys_contentid), never <code>null</code>.
     * @throws Exception if the folder cannot be found or the item is not a folder.
     */
    public Number findLegacyFolderIdFromPath(String path) throws Exception;
    
    /**
     * Removes a specified item from the specified folder. 
     * @param path a valid folder path, never <code>null</code> or empty.
     * @param itemId never <code>null</code> or empty.  Must be a child of the specified folder.
     * @param purgeItem <code>true</code> to purge the item from the system, <code>false</code> to only remove the item
     * from the folder.
     * @throws Exception if the item cannot be removed from the folder.
     */
    public void removeItem(String path, String itemId, boolean purgeItem) throws Exception;
    
    /**
     * Finds all the paths associated with an item.
     * If the item is a folder a singleton list will be returned.
     * 
     * @param itemId never <code>null</code> or empty.
     * @return should not be empty but is not guaranteed to be, never <code>null</code>.
     * @throws Exception if the item does not exist.
     */
    public List<String> findPaths(String itemId) throws Exception;

    /**
     * Finds all the paths associated with an item.
     * If the item is a folder a singleton list will be returned.
     *
     * @param itemId never <code>null</code> or empty.
     * @return should not be empty but is not guaranteed to be, never <code>null</code>.
     * @throws Exception if the item does not exist.
     */
    public List<String> findPaths(String itemId, String relationshipTypeName) throws Exception;
    
    /**
     * Finds items that a children to a given folder path.
     * 
     * @param path must be a valid path to a <strong>folder</strong>, never <code>null</code>.
     * @return maybe empty, never <code>null</code>.
     * @throws Exception if the path is not a folder path.
     */
    public List<IPSItemSummary> findItems(String path) throws Exception;
    

    /**
     * Finds child items for a given path, optionally finding only folders.
     * 
     * @param path must be a valid path to a <strong>folder</strong>, never <code>null</code>.
     * @return maybe empty, never <code>null</code>.
     * @throws Exception if the path is not a folder path.
     */
    public List<IPSItemSummary> findItems(String path, boolean foldersOnly) throws Exception;
    
    
    /**
     * Finds all child item ids within the specified folder folder, recursing into child folders.  A very lightweight
     * and fast method that uses only cached data.
     * 
     * @param path must be a valid path to a <strong>folder</strong>, never <code>null</code>.
     * 
     * @return The list of item ids, excluding folders, never <code>null</code>, may be empty if none are found, or 
     * if the path is invalid.
     * 
     * @throws Exception if there are any errors.
     */
    public List<String> findItemIdsByPath(String path) throws Exception;
    
    /**
     * Finds an item by path.
     * @param path never <code>null</code>.
     * @return never <code>null</code>.
     * @throws Exception if the item is not found.
     */
    public IPSItemSummary findItem(String path) throws Exception;
    
    /**
     * Finds an item by ID. The usage of this method is to find 
     * all properties of the item, except its path. So the path
     * related properties may not be set in the returned object.
     * 
     * @param id the ID of the item, not blank.
     * 
     * @return the item, never <code>null</code>.
     */
    public PSPathItem findItemById(String id) throws IPSDataService.DataServiceLoadException, PSValidationException;

    public PSPathItem findItemById(String id, String relationshipTypeName) throws IPSDataService.DataServiceLoadException, PSValidationException;
    
    /**
     * Gets the parent folder ID for the specified item.
     * 
     * @param item ID in question, not <code>null</code>.
     * 
     * @return the folder ID, never <code>null</code>.
     */
    public IPSGuid getParentFolderId(IPSGuid itemId);

    /**
     * Gets the parent folder ID for the specified item.
     * 
     * @param item ID in question, not <code>null</code>.
     * @param isRequired if <code>true</code>, then the returned ID can never be <code>null</code>;
     * otherwise, the returned ID may be <code>null</code> if cannot find the parent folder.
     * 
     * @return the folder ID. It may be <code>null</code> if <code>isRequired</code> is <code>false</code> 
     * and cannot find the parent folder; otherwise it can never be <code>null</code> if 
     * <code>isRequired</code> is <code>true</code>.
     */
    public IPSGuid getParentFolderId(IPSGuid itemId, boolean isRequired);

    /**
     * Sets the access level to the specified item. The ACL is calculated according 
     * to current user. The ACL is the item itself for a "folder" or "site" item; 
     * otherwise it is the ACL of its parent folder.
     * 
     * @param item the item in question, assumed not <code>null</code>.
     * 
     * @return the item which contains the access level for the current user, 
     * never <code>null</code>.
     */
    public PSPathItem setFolderAccessLevel(PSPathItem item) throws PSValidationException;

    /**
     * Sets the access level for current user to the specified items (siblings).
     * The ACL is calculated the same way as described in {@link #setFolderAccessLevel(PSPathItem)}.
     * 
     * @param items the items in question, assumed not <code>null</code>.
     * Assumed all items are siblings or under the same parent folder.
     * 
     * @return the passed in items, never <code>null</code>.
     */
    public List<PSPathItem> setFolderAccessLevel(List<PSPathItem> items);

    /**
     * Determines what the path points to.
     * @param path never <code>null</code> or empty.
     * @return never <code>null</code>.
     */
    public PathTarget pathTarget(String path) throws IPSDataService.DataServiceNotFoundException;

    /**
     * {@link #pathTarget(String)}
     * @param path never <code>null</code> or empty.
     * @param shouldRecycle <code>true</code> if the item is being recycled. flag used to
     *                      indicated whether the
     *                      {@link com.percussion.design.objectstore.PSRelationshipConfig#TYPE_RECYCLED_CONTENT}
     *                      type is used or not.
     * @return never <code>null</code>.
     */
    public PathTarget pathTarget(String path, boolean shouldRecycle) throws IPSDataService.DataServiceNotFoundException;
    
    
    /**
     * Provides a unique folder name for the given folder path and given base name.  The name is also valid for a
     * folder, meaning any characters specified in {@link IPSConstants#INVALID_ITEM_NAME_CHARACTERS} will be removed.
     * If any folder/item exists with the same base name under the supplied parent folder, returns supplied baseName-n
     * with n starting at 2.  The first name 'baseName-n' will be used with the first available n value >= 2. 
     * @param parentPath the full internal folder path, must not be blank, throws exception if it is not a valid folder 
     * path.
     * @param baseName must not be blank and must contain at least one valid character.
     * @return String unique and valid name never blank.
     */
    public String getUniqueFolderName(String parentPath, String baseName);
    
       
    /**
     * Provides a unique name within a given folder path, given base name, and suffix.
     * If any item exists with the same base name and suffix under the supplied parent folder, returns supplied baseName-suffix-n
     * with n starting at 2.  The first name 'baseName-suffix-n' will be used with the first available n value >= 2.
     * @param parentPath the full internal folder path, must not be blank, throws exception if it is not a valid folder 
     * path.
     * @param baseName must not be blank.
     * @param suffix   can be blank.
     * @param startingIndex
     * @param skipFirstIndex whether the first index should be skipped
     * @return String  unique name never blank.
     */
    public String getUniqueNameInFolder(String parentPath, String baseName, String suffix, int startingIndex, boolean skipFirstIndex);

    /**
     * The target of a path.
     * Could be any of {@link PathTargetType}.
     * 
     * @author adamgent
     *
     */
    public static class PathTarget {
        private String path;
        private IPSItemSummary item;
        private PathTargetType targetType;
        private Exception exception;
        
        /**
         * Path target points to an item or folder.
         * @param path never <code>null</code>.
         * @param item never <code>null</code>.
         */
        public PathTarget(String path, IPSItemSummary item)
        {
            this(path);
            this.item = item;
            this.targetType = item.isFolder() ?  PathTargetType.FOLDER : PathTargetType.ITEM;
        }

        /**
         * Path target points to nothing.
         * @param path never <code>null</code>.
         */
        public PathTarget(String path)
        {
            this.path = path;
            this.targetType = PathTargetType.NOTHING;
        }
        
        public PathTarget(String path, Exception e) {
            this(path);
            this.exception = e;
            this.targetType = PathTargetType.INVALID;
        }
        
        public String getPath()
        {
            return path;
        }

        /**
         * The item that the path points to.
         * 
         * @return never <code>null</code>.
         * @throws Exception if item does not exist.
         */
        public IPSItemSummary getItem() throws PSDataServiceException {
            if (item == null)
                throw new PSDataServiceException("Item not found.");
            return item;
        }
        
        /**
         * Gets the item if its folder, fails otherwise.
         * 
         * @return never <code>null</code>.
         * @throws Exception if item is not a folder or does not exist.
         */
        public IPSItemSummary getFolder() throws Exception
        {
            IPSItemSummary item = getItem();
            if ( ! item.isFolder() )
                throw new RuntimeException("Item is not a folder.");
            return item;
        }

        public PathTargetType getTargetType()
        {
            return targetType;
        }

        /**
         * Is a path that points to a folder or nothing.
         * @return <code>true</code> or <code>false</code>
         */
        public boolean isToFolderOrNothing() {
            return targetType == PathTargetType.NOTHING 
            || targetType == PathTargetType.FOLDER;
        }
        
        /**
         * Does the path point to folder?
         * @return <code>true</code> if it does.
         */
        public boolean isToFolder() {
            return targetType == PathTargetType.FOLDER;
        }
        
        /**
         * Does the path point to nothing?
         * @return <code>true</code> if it does.
         */
        public boolean isToNothing() {
            return targetType == PathTargetType.NOTHING;
        }
        
        /**
         * Does the path point to something (either folder or item)?
         * 
         * @return <code>true</code> if it does.
         */
        public boolean isToSomething() {
            return ! isToNothing();
        }

        public boolean isInvalid() {
            return targetType == PathTargetType.INVALID;
        }
        /**
         * Will only be set if {@link #isInvalid()} is <code>true</code> but
         * even then it may not be set.
         * @return maybe <code>null</code>.
         */
        public Exception getException()
        {
            return exception;
        }
        
    }
    /**
     * 
     * Represents the differing objects a path points to.
     * 
     * @author adamgent
     *
     */
    public static enum PathTargetType {
        /**
         * The path is invalid.
         */
        INVALID,
        /**
         * The path points to nothing.
         */
        NOTHING,
        /**
         * The path points to a folder.
         */
        FOLDER,
        /**
         * The path points to an item (that is not a folder).
         */
        ITEM;
    }
    
    /**
     * Concatenates folder paths.
     * @param start never <code>null</code>.
     * @param end never <code>null</code>.
     * @return never <code>null</code>.
     * @see PSFolderPathUtils#concatPath(String, String...)
     */
    public String concatPath(String start, String ... end);
    
    /**
     * 
     * @param path never <code>null</code>.
     * @return never <code>null</code>.
     * @see PSFolderPathUtils#parentPath(String)
     */
    public String parentPath(String path);
    
    /**
     * 
     * @param path never <code>null</code>.
     * @return never <code>null</code>.
     * @see PSFolderPathUtils#getName(String)
     */
    public String name(String path);
    
    /**
     * Creates a particular folder path, creating missing
     * folders if they do not exist in the path.
     * Thus a path of:
     * <pre>
     *  //a/b/c
     * </pre>
     * Would create folders //a, //a/b, and //a/b/c if they were not created yet.
     * An error will <strong>NOT</strong> occur if the folder path already exists
     * or parts of the folder path exist so long as each component of the path is a
     * folder.
     * <p>
     * The permission of the created folder will have {@link PSFolderPermission.Access#ADMIN} ACL.
     *  
     * @param path never <code>null</code> or empty.
     * @throws Exception if the components of the path are not folders.
     */
    public void createFolder(String path) throws Exception;
    
    /**
     * In addition of creating a folder, just like {@link #createFolder(String)},
     * the created folder also has the specified folder permission.
     * 
     * @param path the path of the created folder, never <code>null</code> or empty.
     * @param acl the folder permission of the created folder, never <code>null</code>.
     */
    public void createFolder(String path, PSFolderPermission.Access acl) throws Exception;
    
    /**
     * Deletes a folder identified by the specified path.  This is a recursive delete, meaning all sub-folders and items
     * will also be deleted.  Items are not purged from the system.
     * @param path a valid folder path, never <code>null</code> or empty.
     * @throws Exception if the folder cannot be deleted.
     */
    public void deleteFolder(String path) throws Exception;

    /**
     * Deletes a folder identified by the specified path.  This is a recursive delete, meaning all sub-folders and items
     * will also be deleted.  Items are not purged from the system.
     * @param path a valid folder path, never <code>null</code> or empty.
     * @param recycleFolder <code>true</code> to recycle the folder, <code>false</code> to purge the folder and its contents.
     * @throws Exception if the folder cannot be deleted.
     */
    public void deleteFolder(String path, boolean recycleFolder) throws Exception;
    
    /**
     * Determines if the current user has "admin" access for the specified folder and all 
     * its descendant folders.
     * 
     * @param folderId the ID of the folder in question, not blank.
     * 
     * @return <code>true</code> if current user does have "admin" access to the folder
     * and its descendant folders; otherwise return <code>false</code>. 
     */
    public boolean validateFolderPermissionForDelete(String folderId);
    
    /**
     * Determines if the current user has the specified access level for the given folder.
     * 
     * @param folderId the ID of the folder in question, not blank.
     * @param acl the access level in question, not <code>null</code>.
     * 
     * @return <code>true</code> if current user does have the specified access level
     * to the specified folder; otherwise return <code>false</code>.
     */
    public boolean hasFolderPermission(String folderId, PSFolderPermission.Access acl);
    
    /**
     * Changes the name of a folder identified by the specified path to the specified name. 
     * @param path a valid folder path, never <code>null</code> or empty.
     * @param name the new name of the folder, never blank.
     * @throws Exception if the folder cannot be renamed.
     */
    public void renameFolder(String path, String name) throws Exception;
    
    /**
     * 
     * Finds child paths of a given folder path.
     * 
     * @param path never <code>null</code> or empty.
     * @return never <code>null</code>, maybe empty.
     * @throws Exception if the path is not valid or is an item.
     * @see #findItems(String)
     */
    public List<String> findChildren(String path) throws Exception;
    
    /**
     * Gets the item properties.
     * 
     * @param path the path of the item, never blank.
     * 
     * @return never <code>null</code>.
     * @throws Exception If an item cannot be found for the given path.
     */
    public PSItemProperties findItemProperties(String path) throws Exception;

    /**
     * Gets the item properties.
     *
     * @param path the path of the item, never blank.
     *
     * @return never <code>null</code>.
     * @throws Exception If an item cannot be found for the given path.
     */
    public PSItemProperties findItemProperties(String path, String relationshipTypeName) throws Exception;
    
    /**
     * Gets the item properties by id.
     * 
     * @param id the id of the item, never blank.
     * 
     * @return never <code>null</code>.
     * @throws Exception If an item cannot be found for the given id.
     */
    public PSItemProperties findItemPropertiesById(String id) throws Exception;

    /**
     * Gets the item properties by id.
     *
     * @param id the id of the item, never blank.
     *
     * @return never <code>null</code>.
     * @throws Exception If an item cannot be found for the given id.
     */
    public PSItemProperties findItemPropertiesById(String id, String relationshipTypeName) throws Exception;
    
    /**
     * Gets the access level for a specified folder according to current user.
     * 
     * @param id the ID of the specified folder, not blank.
     * 
     * @return the access level, never <code>null</code>.
     */
    public PSFolderPermission.Access getFolderAccessLevel(String id) throws PSValidationException;
    
    /**
     * Gets the specified folder properties from its ID.
     * 
     * @param id the ID of the folder, never blank.
     * 
     * @return the folder properties, never <code>null</code>.
     * 
     * @throws PSErrorException If cannot be found the folder with the given path.
     */
    public PSFolderProperties findFolderProperties(String id) throws PSErrorException, PSValidationException;
    
    /**
     * Saves the specified folder properties.
     * <p>
     * There is a special consideration for the Workflow ID property: this property is
     * updated only if its value is greater than zero. If it's equals to
     * {@link Integer#MIN_VALUE} then the workflowId property is removed. If it's
     * anything else it's silently ignored.
     * 
     * @param folder the folder properties, never <code>null</code>.
     */
    public void saveFolderProperties(PSFolderProperties folder);
    
    /**
     * When trying to find a folder under Assets, the folders system path is
     * required, but this is not necessary when trying to find a folder under
     * Sites. It adds the system path if necessary, an also adds the slash
     * before the path, if necessary.
     * 
     * @param path the path to the folder
     * @return a String containing the id of the folder
     */
    String getFolderPath(String path);
    
   /**
    * Moves the specified item from its original folder to the specified folder.
    * 
    * Note, if the item is a page, then both folders must be under a site;
    * otherwise the item is an asset, then both folders must be under the
    * generic "Assets" folder.
    * 
    * @param targetFolderPath the path of the target folder, not blank.
    * @param itemPath the full path of the moved item, not blank.
    * @param isFolder determines if the moved item is a folder or non-folder item.
    * <code>true</code> if the item is a folder.
    * 
    * @throws PSErrorException if an error occurs.
    */
    public void moveItem(String targetFolderPath, String itemPath, boolean isFolder);

    /**
     * Finds the folder for the given path.
     * 
     * @param path a valid path to a folder.
     * @return the folders item summer, never <code>null</code>.
     * @throws Exception if the path is to nothing or an item that is not a folder.
     */
    public IPSItemSummary findFolder(String path) throws Exception;
    
    /**
     * Find and return all the sites the item exists in. The sites are ordered by
     * the name of the sites it exists in. 
     * This is a thin wrapper, calls {@link IPSSiteManager#getItemSites(IPSGuid)} to get the
     * sites.
     * 
     * @param contentId the string representation of the content GUID, never <code>null</code>
     * @return the sites the item exists in never <code>null</code> may be
     *         empty, if the item does not exist in any site.
     */
    public List<IPSSite> getItemSites(String contentId);    
    
    /**
     * Validates if the given name is contained in a list of reserved folder names not available
     * 
     * @param name the string representation of the folder name
     * @return <code>true</code> if the name is not a reserved folder name
     */
    public boolean validateFolderReservedName(String name);      
    
    /**
     * Returns a valid workflow id for the given {@link PSFolderProperties} object.
     * It tries to get the ID of the workflow currently assigned to the folder. If
     * it's not a valid workflow, then the default one is returned.
     * 
     * @param folder Never <code>null</code>.
     * @return The workflow id associated to the folder, or the default workflow id
     * if the former is invalid.
     */
    public int getValidWorkflowId(PSFolderProperties folder);
    
    /**
     * Get the id of the default workflow
     * 
     * @return The id.
     */
    public int getDefaultWorkflowId();
    
    /**
     * Gets the root level folder (immediate child for assets root) for a given shared asset.
     * Eg: /Assets/RootLevelFolder/anotherFolder/anotherInnerFolder/RichTextAsset.
     * Given the RichTextAsset id, the method will return the PSFolder object of "RootLevelFolder" 
     * 
     * @author federicoromanelli
     * @param assetId - the id of the asset. Never <code>null</code>.
     * @return the PSFolder object of the root level folder the shared asset is descendant of
     */
    public PSFolder getRootFolderForAsset(String assetId);
    
    /**
     * Gets the allowed sites properties of the root level folder for a given shared asset.
     * The allowed sites property represent the list of sites that the asset can be published to
     * @see com.percussion.share.dao.IPSFolderHelper#getRootFolderForAsset(String)
     * 
     * @author federicoromanelli
     * @param assetId - the id of the asset. Never <code>null</code>.
     * @return the value of the allowed sites property.
     * <code> Null </code> if the property is not set for the specific folder.
     */
    public String getRootLevelFolderAllowedSitesPropertyValue(String assetId);

    /**
     * Checks to see if the target path exists.  Checks the ID of the target and source and
     * verifies if they are the same.  If they are, it is the same folder.  If they aren't, it's obviously
     * a different folder.  Developed for checking if folders/items being recycled have a destination path
     * under the recycle bin that matches the recycled item's folder id. Always checks the target path
     * as the {@link com.percussion.design.objectstore.PSRelationshipConfig#TYPE_RECYCLED_CONTENT}
     * with the originiation type as {@link com.percussion.design.objectstore.PSRelationshipConfig#TYPE_FOLDER_CONTENT}.
     *
     * @param targetPath the target path to check.
     * @param originalPath the original path to check against.
     * @return <code>true</code> if the item exists in destination path.
     */
    boolean isFolderValidForRecycleOrRestore(String targetPath, String originalPath, String targetRelType, String origRelType);

    /**
     * Gets the user name and date when the specified item was last check in or out.
     * 
     * @param summary the summary of the specified item, not <code>null</code>.
     * @param isPublishable <code>true</code> if item is in a publishable state, <code>false</code> if not.
     * 
     * @return the user name (1st element) and modified date (2nd element,
     * never <code>null</code>.
     */
    PSPair<String, String> fixupLastModified(IPSGuid id, String userName, Date lastModified, boolean isPublishable);


}
