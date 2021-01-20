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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSLocator;

import java.util.List;
import java.util.Set;

/**
 * The folder processor specifies methods used to execute actions specific to
 * folders. 
 * <p>
 * <em>Note:<em> This interface will be expanded to include all folder 
 * functionality in a future release. Until that time, it is not being 
 * published for implementer use.
 */
public interface IPSFolderProcessor
{
   /**
    * Get the fully qualified path(s) of the supplied object within the folder
    * trees. The <code>objectId</code> may be a folder or an item.
    * 
    * @param objectId Never <code>null</code>.
    * 
    * @return All fully qualified paths that contain the supplied object. Paths
    *         are of the form "//a/b/c", where 'c' is a parent of
    *         <code>objectId</code>. A folder will only have 1 path, while an
    *         item can have many paths. May be empty, never <code>null</code>.
    * @throws PSCmsException If path lookup fails for any reason.
    */
   //getParentPaths ??
   public String[] getFolderPaths(PSLocator objectId) throws PSCmsException;

    /**
     * Get the fully qualified path(s) of the supplied object within the folder
     * trees. The <code>objectId</code> may be a folder or an item.
     *
     * @param objectId Never <code>null</code>.
     *
     * @return All fully qualified paths that contain the supplied object. Paths
     *         are of the form "//a/b/c", where 'c' is a parent of
     *         <code>objectId</code>. A folder will only have 1 path, while an
     *         item can have many paths. May be empty, never <code>null</code>.
     * @throws PSCmsException If path lookup fails for any reason.
     */
    public String[] getFolderPaths(PSLocator objectId, String relationshipTypeName) throws PSCmsException;
   
   /**
    * Method to retrieve all descendant folders of an object. No descendant
    * items are included in the results.
    * 
     * @param folderId A valid key that references the folder for which you wish
     *            to obtain the folder children, recursively. A valid key is one
    *    that references an existing object in the database. Never
    *    <code>null</code>.
    * @return An array with 0 or more entries. Each entry is a valid PSLocator
    * for a folder.
    * @throws PSCmsException PSCmsException If any problems accessing the db.
    */
    public PSLocator[] getDescendentFolderLocators(PSLocator folderId) throws PSCmsException;
   
   /**
     * This is the same as {@link #getDescendentFolderLocators(PSLocator)},
     * except this does not apply folder community or folder security filter
     * during the process.
    *   
    * @param folderId the ID of the folder in question, not <code>null</code>.
    * 
     * @return all descendant folder locators, never <code>null</code>, but may
     *         be empty.
    * 
    * @throws PSCmsException if an error occurs.
    */
    public PSLocator[] getDescendentFolderLocatorsWithoutFilter(PSLocator folderId) throws PSCmsException;
   
   /**
    * This method can be used to catalog related parent objects. It is similar
    * with {@link #getChildSummaries(PSLocator) getChildSummaries}, except it
    * catalogs the parent objects for a given locator.
    * 
    * @param objectId A valid key referencing a folder or item for which you
    * want the parents. A valid key is one that references an
    *    existing object in the database. Never <code>null</code>.
    */
    public PSComponentSummary[] getParentSummaries(PSLocator objectId) throws PSCmsException;

   /**
     * Creates a new link between the supplied parent and each supplied child,
     * which may be a folder or item.
     * <p>
     * The process is transaction safe, meaning it either completes
    * successfully, or the database is unchanged.
    *
     * @param children Each entry must be a valid PSLocator, which is a key that
     *            references an existing object in the database. Never
     *            <code>null
    *    </code>. If empty, this method returns immediately without exception.
    *
     * @param targetFolderId A valid key referencing the folder that will own
     *            the newly created links. A valid key is one that references an
    *    existing object in the database. Never <code>null</code>.
    *
    * @throws PSCmsException If any problems accessing the db.
    */
    public void addChildren(List children, PSLocator targetFolderId) throws PSCmsException;
   
   /**
    * This method can be used to catalog a folder's content.
    *
     * @param folderId A valid key that references the folder for which you wish
     *            to obtain the child info. A valid key is one that references
     *            an existing folder in the database. Never <code>null</code>.
    *
    * @return An array with 0 or more entries. Each entry is a valid summary.
    *
    * @throws PSCmsException If any problems accessing the db.
    * 
    */
    public PSComponentSummary[] getChildSummaries(PSLocator folderId) throws PSCmsException;
   
   /**
    * Locate the object and get the component summary given fully qualified 
    * path. A full qualified path has following syntax:
    * <p>
    * <em>//folder_1/folder_2/folder_3/../../child_n</em>
    * <p>
    *   
     * @param path fully qualified path as explained above, must not be
     *            <code>null</code> or empty.
    * 
     * @return Component summary for the leaf object (referred by child_n above)
     *         in the path, can be<code>null</code> if no such path exists in
     *         the system. May be for an item or folder.
    *    
    * @throws PSCmsException If any problems retrieving the info from the db.
    */
    public PSComponentSummary getSummary(String path) throws PSCmsException;
   
   /**
     * Creates a new relationship of specified relationship type for each child
     * linking the child to the target parent. Whether the child is cloned is
     * dependent on the effects associated with the relationship type.
    * <p>
    * The process is not transaction safe. If the process fails, the caller
    * should requery the target parent to determine which relationships were
    * actually created.
    *
     * @param children Each entry must be a valid PSLocator, which is a key that
     *            references an existing object in the database. Never
     *            <code>null
    *    </code>. If empty, this method returns immediately without exception.
    *
     * @param targetFolderId A valid key referencing the folder that will own
     *            the supplied children. A valid key is one that references an
    *    existing object in the database. Never <code>null</code>.
    *
    * @throws PSCmsException If any problems occur while adding the children.
    */
    public void copyChildren(List children, PSLocator targetFolderId) throws PSCmsException;

   /**
    * Same as {@link #moveChildren(PSLocator, List, PSLocator, boolean)} with
    * the last parameter value of <code>false</code>.
    */
    public void removeChildren(PSLocator sourceFolderId, List children) throws PSCmsException;

   /**
    * Removes all items/folders specified in <code>children</code> from the
    * specified <code>sourceFolderId</code>. If a child is an item, it is
     * delinked from the parent, but not removed from the system. If the child
     * is a folder, it is purged from the system. Folders are processed
     * recursively.
    * <p>
    * The process is not transaction safe. If the process fails, the caller
    * should requery the source parent to determine which relationships were
    * actually removed.
    * 
    * @param sourceFolderId A valid key that references the current parent of
     *            all the supplied children. A valid key is one that references
     *            an existing object in the database. Never <code>null</code>.
    * 
     * @param children Each entry must be a non-<code>null</code> PSLocator. If
     *            the key does not reference a child of the supplied parent, it
     *            is skipped. Never <code>null</code>. If empty, this method
     *            returns immediately without exception.
    * 
    * @param force this is not used any more
    * 
    * @throws PSCmsException If the suppied relationship type (if supplied) is
     *             not found or cannot be instantiated, or any problems occur
     *             while removing the relationship(s).
    */
    public void removeChildren(PSLocator sourceFolderId, List children, boolean force) throws PSCmsException;

   /**
    * Same as {@link #moveChildren(PSLocator, List, PSLocator, boolean)} with
    * the last parameter value of <code>false</code>.
    */
    public void moveChildren(PSLocator sourceFolderId, List children, PSLocator targetFolderId) throws PSCmsException;

   /**
    * Changes the linkage of each child from the source parent to the target
    * parent.
    * <p>
     * The process is transaction safe, meaning it either completes
     * successfully, or the database is unchanged.
    * 
     * @param sourceFolderId A valid key that references the current owner of
     *            the links to all the supplied children. A valid key is one
     *            that references an existing object of the correct type in the
     *            database. Never <code>null</code>.
    * 
    * @param children Each entry must be a valid PSLocator, which is a key that
     *            references an existing object in the database. Never
     *            <code>null</code>. If empty, this method returns immediately
     *            without exception. If the type of the children are not
     *            supported by the target parent, an exception is thrown.
    * 
     * @param targetFolderId A valid key referencing the folder that will own
     *            the links after the move. A valid key is one that references
     *            an existing object of the correct type in the database. Never
     *            <code>null</code>.
    * 
    * @param force this is not used
    * 
    * @throws PSCmsException If the db can't be accessed for any reason.
    */
    public void moveChildren(PSLocator sourceFolderId, List children, PSLocator targetFolderId, boolean force)
      throws PSCmsException;

   /**
    * Get the type of the supplied source folder.
    * 
    * @param source the locator of the folder for which to get the type, not
    *           <code>null</code>.
    * @param name the name of the folder for which to get the type, not
    *           <code>null</code> or empty.
    * @return the type of the folder, one of the
    *         <code>PSFolder.FOLDER_TYPE_xxx</code> values.
    * @throws PSCmsException for any error.
    */
//   public int getFolderType(PSLocator source, String name)
//         throws PSCmsException;

   /**
    * Get the set of communities found in the supplied source tree recursivly.
    * This includes the communities of both folders and items.
    * 
    * @param source the locator from where to start the search for all
    *           communities, not <code>null</code>.
    * @return a set of <code>Integer</code> objects with all community ids
    *         found in the supplied source tree recursivly, never
    *         <code>null</code>, may be empty.
    * @throws PSCmsException for any error.
    */
   public Set getFolderCommunities(PSLocator source) throws PSCmsException;

   /**
     * Clones the source folder and its children recursively and links it to the
     * supplied target for the given cloning options. Note that this method does
     * not use all options specified in the <code>options</code> parameter, it
     * only uses the options applying to the folders, items and nav.
    * 
     * @param sourceFolderId the site folder or site subfolder locator which
     *            needs to be cloned, not <code>null</code>.
     * @param targetFolderId the folder parent locator into which to clone the
     *            source, not <code>null</code>.
    * @param options the cloning options, not <code>null</code>.
    * @return the name of the log file if there were errors, <code>null</code>
    *         otherwise.
    * @throws PSCmsException for any error.
    * @todo ph: PSCloningOptions includes navigation options which don't belong
    * here.
    */
    public String copyFolder(PSLocator sourceFolderId, PSLocator targetFolderId, PSCloningOptions options)
            throws PSCmsException;
   
   /**
    * Copies the security (ACL) of the source folder to the target folder. 
    * 
     * @param sourceFolderId the folder from which the security will be copied,
     *            not <code>null</code>.
     * @param targetFolderId the folder to which the security will be copied,
     *            not <code>null</code>.
    * @throws PSCmsException for any error.
    */
    public void copyFolderSecurity(PSLocator sourceFolderId, PSLocator targetFolderId) throws PSCmsException;
   
   /**
    * Retrieves all ancestor locators for the specified folder.
    * 
    * @param folderId the locator of the specified folder, never 
    *    <code>null</code>.
    * 
    * @return A list of locators from the root folder to the specified folder, 
    *    never <code>null</code> or empty. The first element is the root 
     *         locator and the last element is the immediate parent of the
     *         specified folder.
    * 
    * @throws PSCmsException PSCmsException if the specified folder does not 
    *    exist.
    */
    public List<PSLocator> getAncestorLocators(PSLocator folderId) throws PSCmsException;

    /**
     * This is a fast Database method for purging items and folders from the
     * system If folders are selected subfolders will be recursively purged as
     * well as content unless the items are also linked to other folders not
     * being purged. No effects will be run on this action to prevent any side
     * effects, all relationships to and from the items will be removed.
     * 
     * @param items list if items to delete. Never <code>null</code>.
     * @throws PSCmsException
     */
    public void purgeFolderAndChildItems(List<PSLocator> items) throws PSCmsException;

    /**
     * This is a fast Database method for purging items and folders from the
     * system If folders are selected subfolders will be recursively purged as
     * well as content unless the items are also linked to other folders not
     * being purged. No effects will be run on this action to prevent any side
     * effects, all relationships to and from the items will be removed. If
     * sourceFolderId is
     * @param sourceFolder Locator of parent folder if null or locator id is -1 will
     * callpurgeFolderNavigation(PSLocator folder).
     * @param items list if items to delete. Never <code>null</code>.
     * @throws PSCmsException
     */
    public void purgeFolderAndChildItems(PSLocator sourceFolder, List<PSLocator> items) throws PSCmsException;

    /**
     * This is a fast Database method for purging Navon and Navtree items from a
     * folder and subfolders. This will prevent inconsistent navigation making
     * sure navon items are not left without a parent.
     * 
     * @param folder. Never <code>null</code>.
     * @throws PSCmsException
     */
    public void purgeFolderNavigation(PSLocator folder) throws PSCmsException;
}
