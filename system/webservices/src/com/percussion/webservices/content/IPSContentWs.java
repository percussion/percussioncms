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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.webservices.content;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.i18n.PSLocale;
import com.percussion.search.objectstore.PSWSSearchRequest;
import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.content.data.PSRevisions;
import com.percussion.services.content.data.PSSearchSummary;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSInvalidStateException;
import com.percussion.webservices.PSUnknownChildException;
import com.percussion.webservices.PSUnknownContentTypeException;
import com.percussion.webservices.aop.security.IPSWsMethod;

import java.util.List;

/**
 * This interface defines all content related webservices.
 */
public interface IPSContentWs
{
   /**
    * Loads all keyword definitions for the supplied name in read-only mode.
    * 
    * @param name the name of the keyword to load, may be <code>null</code> 
    *    or empty, wildcards are accepted. All keywords will be loaded if the 
    *    supplied name is <code>null</code> or empty.
    * @return a list with all loaded keywords in read-only mode, never 
    *    <code>null</code>, may be empty, alpha ordered by name.
    */
   public List<PSKeyword> loadKeywords(String name);
   
   /**
    * Loads all locales for the supplied parameters in read-only mode.
    * 
    * @param code the locale code for which to load the locale, may be 
    *    <code>null</code> or empty. All locales will be loaded if 
    *    <code>null</code> or empty.
    * @param name the name of the locale for which to load the locale, may be 
    *    <code>null</code> or empty, wildcards are accepted. All locales will 
    *    be loaded if <code>null</code> or empty.
    * @return a list with all loaded locales in read-only mode, never 
    *    <code>null</code>, may be empty, alpha ordered by name.
    */
   public List<PSLocale> loadLocales(String code, String name);
   
   /**
    * Loads all translation settings defined on the server in read-only mode.
    * 
    * @return an list with all translation settings currently defined on the 
    *    server in read-only mode, never <code>null</code>, may be empty.
    */
   public List<PSAutoTranslation> loadTranslationSettings();

   /**
    * Load all content type summaries for the specified name in read-only mode.
    * 
    * @param name the content type name for which to load the content type 
    *    summaries, may be <code>null</code> or empty, wildcards are accepted. 
    *    All content type summaries will be loaded if <code>null</code> 
    *    or empty.
    * @return a list with all loaded content type summaries in read-only mode, 
    *    never <code>null</code>, may be empty, alphabetically ordered by name.
    */
   public List<PSContentTypeSummary> loadContentTypes(String name);
   
   /**
    * @deprecated Use {@link #createItems(String, int)}
    */
   @Deprecated
   public List<PSCoreItem> createItems(String contentType, int count, 
      String session, String user)
      throws PSUnknownContentTypeException, PSErrorException;
   
   /**
    * Use this method to create a list of new content items. The server will
    * construct the new items with default values and return them to the
    * requestor. The items are not persisted until the user calls the
    * {@link #saveItems(List, boolean, boolean, String, String)} method for the
    * returned objects.
    * 
    * @param contentType the name of the content type for which to create new
    * items, not <code>null</code>, must be an existing content type.
    * @param count the number of items the user wants to create, must be greater
    * than 0.
    * 
    * @return a list of newly constructed content items of the requested content
    * type, never <code>null</code> or empty. The returned items are not
    * persisted to the repository until the requestor calls the
    * {@link #saveItems(List, boolean, boolean, String, String)} method for
    * them.
    * @throws PSUnknownContentTypeException if the user requested a content type
    * unknown to the server.
    * @throws PSErrorException for any unexpected error.
    */
   public List<PSCoreItem> createItems(String contentType, int count)
      throws PSUnknownContentTypeException, PSErrorException;
   
   /**
    * @deprecated Use {@link #findItems(PSWSSearchRequest, boolean)}
    */
   @Deprecated
   public List<PSSearchSummary> findItems(PSWSSearchRequest search, 
      boolean loadOperations, String session, String user) 
      throws PSErrorException;
   
   /**
    * Finds all items for the specified search. The search object supplied 
    * can reference a saved search, which can be cataloged through the 
    * services specified in Searches, or individual search parameters.
    * 
    * @param search the search used to perform the requested lookup, not 
    *    <code>null</code>.
    * @param loadOperations <code>true</code> to load the allowed operations
    *    for each search summary, <code>false</code> otherwise.
    * @return an array of search summaries for the executed search, never 
    *    <code>null</code>, may be empty.
    * @throws PSErrorException for any unexpected error.
    */
   public List<PSSearchSummary> findItems(PSWSSearchRequest search, 
      boolean loadOperations) 
      throws PSErrorException;
   
   /**
    * Finds all items for the given ids.
    * 
    * @param ids The ids of the items to find. Never
    *    <code>null</code>.
    * @param loadOperations <code>true</code> to load the allowed operations
    *    for each item summary, <code>false</code> otherwise.
    * @return a list of item summaries never <code>null</code>, may be empty.
    * @throws PSErrorException for any unexpected error.
    */
   public List<PSItemSummary> findItems(List<IPSGuid> ids, 
         boolean loadOperations) 
      throws PSErrorException;
   
   /**
    * Finds the IDs of all items in the specified folder path, recursively processes child folders.  
    * 
    * @param folderPath The folder path to check, may not be <code>null<code/> or empty.
    * 
    * @return The list of item ids, may be empty, <code>null</code> if the supplied folder path is invalid.
    * @throws PSErrorException if there is an unexpected error.
    */
   public List<Integer> findItemIdsByFolder(String folderPath) throws PSErrorException;
   
   /**
    * @deprecated Use 
    * {@link #loadItems(List, boolean, boolean, boolean, boolean)}
    */
   @Deprecated
   @IPSWsMethod(ignore=true)
   public List<PSCoreItem> loadItems(List<IPSGuid> contentIds, 
      boolean includeBinary, boolean includeChildren, boolean includeRelated,  
      boolean includeFolderPath, String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Loads all items for the specified ids and loading options.
    * 
    * @param contentIds a list with item ids to load, not <code>null</code> or
    * empty. It is an error if no item exists for any of the supplied ids. This
    * must be a content guid obtained from one of the IPS*Ws interfaces or from
    * the {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * @param includeBinary a boolean flag to specify whether or not to include
    * binary fields with the returned items. This flag is only used if no
    * <code>fieldNames</code> list was supplied and defaults to
    * <code>false</code>.
    * @param includeChildren a boolean flag to specify whether or not to include
    * children, defaults to <code>false</code> if not provided.
    * @param includeRelated a boolean flag to specify whether to include the
    * relationships of the related items, defaults to <code>false</code> if not
    * provided.
    * @param includeFolderPath a boolean flag to specify whether or not to
    * include the folder path information for the returned item, defaults to
    * <code>false</code> if not supplied.
    * 
    * @return a list of content items with the specified details in the same
    * order as requested, never <code>null</code> or empty. The returned items
    * are read-only, use the <code>prepareForEdit</code> service to make them
    * editable.
    * 
    * @throws PSErrorResultsException if any item fails to load.
    */   
   @IPSWsMethod(ignore=true)
   public List<PSCoreItem> loadItems(List<IPSGuid> contentIds, 
      boolean includeBinary, boolean includeChildren, boolean includeRelated,  
      boolean includeFolderPath) 
      throws PSErrorResultsException;

   @IPSWsMethod(ignore=true)
   public List<PSCoreItem> loadItems(List<IPSGuid> contentIds, 
      boolean includeBinary, boolean includeChildren, boolean includeRelated,  
      boolean includeFolderPath, boolean includeRelatedItem) 
      throws PSErrorResultsException;

    /**
     * The same as {@link #loadItems(List, boolean, boolean, boolean, boolean)},
     * in additional, this has an option to load both the relationships as well
     * as the related items.
     *
     * @param includeRelatedItem if both <code>includeRelated</code> and this
     * is <code>true</code>, then load both the relationships as well as the
     * related items; otherwise the related items will not be loaded.
     * The loaded items contain non-binary fields and child entries only,
     * but do not contain binary fields and the AA relationships with their
     * dependent items.
     */
   @IPSWsMethod(ignore=true)
   public List<PSCoreItem> loadItems(List<IPSGuid> contentIds,
                                     boolean includeBinary, boolean includeChildren, boolean includeRelated,
                                     boolean includeFolderPath, boolean includeRelatedItem, String relationshipTypeName)
           throws PSErrorResultsException;
   
   /**
    * @deprecated Use {@link #saveItems(List, boolean, boolean)}
    */
   @Deprecated
   public List<IPSGuid> saveItems(List<PSCoreItem> items, 
      boolean enableRevisions, boolean checkin, String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Inserts or updates all supplied items to the repository. All supplied 
    * items which do not define a valid item id will be inserted. All other 
    * items will be updated.
    * <p>
    * For inserts the following rules apply:
    * <p>
    * If supplied, all children and related content will be created as 
    * specified in each supplied item. Use the Content/Item Children services 
    * to manipulate individual child entries. Use Content/Related Items 
    * services to manipulate individual related items.
    * <p>
    * For updates the following rules apply:
    * <p>
    * If no children are provided with an item, children will remain 
    * untouched. If children are provided for an item, then new child entries 
    * will be inserted, existing child entries are updated and missing child 
    * entries will be deleted. To work on single child entries use the 
    * Content/Item Children services.
    * <p>
    * If no related content is provided for an item, then related content 
    * remains untouched. If related content is provided, then new related 
    * items will be added, existing related items will be updated and missing 
    * related items will be removed. Use the Content/Related Content 
    * services to work with individual related content items.
    * <p>
    * If no folder paths is provided for an item, then the folder paths 
    * remain untouched. If folder paths is provided, then new folder paths 
    * will be added, existing folder paths will be kept and missing folder 
    * paths will be removed.
    * 
    * @param items a list of items to be inserted or updated to the 
    *    repository, not <code>null</code> or empty.
    * @param enableRevisions a boolean flag to turn on revisions for new 
    *    items immediately. If this flag is <code>true</code>, any checkout 
    *    of the item will automatically create a new revision. This flag is 
    *    ignored for updates.
    * @param checkin a boolean flag to specify whether or not to checkin all 
    *    items after that save.
    * @return a list with all inserted or updated item ids in the same order 
    *    as supplied with the request, never <code>null</code> or empty.
    * @throws PSErrorResultsException if any item fails to be saved.
    */
   public List<IPSGuid> saveItems(List<PSCoreItem> items, 
      boolean enableRevisions, boolean checkin) 
      throws PSErrorResultsException;
   
   /**
    * The same as {@link #saveItems(List, boolean, boolean)}, but also
    * allows a folder id to be added to the request.
    * 
    * @param folderId the id of the folder to include in the request to save
    *    the items.  May be <code>null</code> if the folder id is not
    *    required.
    */
   public List<IPSGuid> saveItems(List<PSCoreItem> items,
      boolean enableRevisions, boolean checkin, IPSGuid folderId)
      throws PSErrorResultsException;

   public List<IPSGuid> saveItems(List<PSCoreItem> items,
                                  boolean enableRevisions, boolean checkin, IPSGuid folderId, String relationshipTypeName)
           throws PSErrorResultsException;
   
   /**
    * @deprecated Use {@link #deleteItems(List)}
    */
   @Deprecated
   public void deleteItems(List<IPSGuid> ids, String session, String user) 
      throws PSErrorsException, PSErrorException;
   
   /**
    * Deletes all items for the supplied ids. All items will be removed from the
    * repository. This operation cannot be reversed. All items must be in edit
    * mode for the requestor.
    * 
    * @param ids a list of ids for all items the user wants deleted, not
    * <code>null</code> or empty. This must be a content guid obtained from
    * one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}. We
    * ignore cases where an item of a supplied id does not exist.
    * 
    * @throws PSErrorsException if any item fails to be deleted.
    * @throws PSErrorException for any unexpected error.
    */
   public void deleteItems(List<IPSGuid> ids) 
      throws PSErrorsException, PSErrorException;
   
   /**
    * @deprecated Use 
    * {@link #viewItems(List, boolean, boolean, boolean, boolean)}
    */
   @Deprecated
   public List<PSCoreItem> viewItems(List<IPSGuid> ids, boolean includeBinary, 
      boolean includeChildren, boolean includeRelated, 
      boolean includeFolderPath, String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Returns all items for the specified ids and loading options. All items
    * will be returned read-only. The difference to the <code>loadItems</code>
    * service is that this allows one to get specific revisions of items.
    * 
    * @param ids a list with item ids to load, not <code>null</code> or empty.
    * It is an error if no item exists for any of the supplied ids. This must be
    * a content guid obtained from one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * All ids may include the revision to view a specific revision. If an id
    * does not include the revision, the current revision will be viewed.
    * 
    * @param includeBinary a boolean flag to specify whether or not to include
    * binary fields with the returned items. This flag is only used if no
    * <code>fieldNames</code> list was supplied and defaults to
    * <code>false</code>.
    * @param includeChildren a boolean flag to specify whether or not to include
    * children, defaults to <code>false</code> if not provided.
    * @param includeRelated a boolean flag to specify whether to include related
    * items, defaults to <code>false</code> if not provided.
    * @param includeFolderPath a boolean flag to specify whether or not to
    * include the folder path information for the returned item, defaults to
    * <code>false</code> if not supplied.
    * 
    * @return a list of content items with the specified details in the same
    * order as requested, never <code>null</code> or empty. The returned items
    * are read-only.
    * @throws PSErrorResultsException if any item fails to load.
    */
   public List<PSCoreItem> viewItems(List<IPSGuid> ids, boolean includeBinary, 
      boolean includeChildren, boolean includeRelated, 
      boolean includeFolderPath) 
      throws PSErrorResultsException;
   
   /**
    * @deprecated Use {@link #prepareForEdit(List)}
    */
   @Deprecated
   public List<PSItemStatus> prepareForEdit(List<IPSGuid> ids, String user)
   throws PSErrorResultsException;
   
   /**
    * Prepares all items for the supplied ids to be edited for the requesting
    * user. The user may call this multiple times for the same item but he is
    * responsible to keep the correct PSItemStatus that this service returned
    * with the first call.
    * 
    * @param ids a list of item ids to be prepared for edit, not
    * <code>null</code> or empty. It is an error if no item exists for any
    * supplied ids. This must be a content guid obtained from one of the IPS*Ws
    * interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @return a list of item status in the same order as requested. The item
    * status will hold the status of an item in which it was before we prepared
    * it for edit. Use this to call the <code>releaseFromEdit</code> service.
    * @throws PSErrorResultsException if any item fails to be prepared for edit.
    */
   public List<PSItemStatus> prepareForEdit(List<IPSGuid> ids)
      throws PSErrorResultsException;
   
   /**
    * Just like {@link #prepareForEdit(List)}, except this method prepares
    * one specified item.
    * 
    * @param itemId the id of the to be prepared item.
    * 
    * @return the result of the preparation, never <code>null</code>.
    * 
    * @throws PSErrorException if failed to prepare for editing.
    */
   public PSItemStatus prepareForEdit(IPSGuid itemId)
      throws PSErrorException;
   
   /**
    * Releases all items for the supplied status from edit mode. It is an error 
    * if an item is not in edit mode for the requesting user.
    * 
    * @param status a list of item status, one for each item that should be 
    *    released from edit mode, not <code>null</code> or empty.
    * @param checkinOnly a boolean flag to specify that the release from
    *    edit service should only checkin the items and skip the 
    *    functionality defined in <code>PSItemStatus</code>. 
    *    
    * @throws PSErrorsException if any item fails to be released from edit mode.
    */
   public void releaseFromEdit(List<PSItemStatus> status, boolean checkinOnly)
      throws PSErrorsException;
   
   /**
    * This is the same as {@link #releaseFromEdit(List, boolean)}, except it
    * is processing one item-status, not a list of them.
    * 
    * @param itemStatus the item status in question, assumed not <code>null</code>.
    * @param checkinOnly <code>true</code> if performs check-in only.
    * 
    * @throws PSErrorException if an error occurs.
    */
   public void releaseFromEdit(PSItemStatus itemStatus, boolean checkinOnly) 
   throws PSErrorException;
   
   /**
    * @deprecated Use {@link #newCopies(List, List, String, boolean)}
    */
   @Deprecated
   public List<PSCoreItem> newCopies(List<IPSGuid> ids, List<String> paths, 
      String relationshipType, boolean enableRevisions, String session, 
      String user) 
      throws PSErrorResultsException, PSErrorException;
   
   /**
    * Creates a new copy for each supplied item. New copies are persisted to the
    * repository and returned to the requestor in read-only mode.
    * 
    * @param ids a list of ids for all items to be copied, not <code>null</code>
    * or empty. This must be a content guid obtained from one of the IPS*Ws
    * interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}. It
    * is an error if no item exists for any of the supplied ids. If the same id
    * is supplied multiple times, multiple copies will be created.
    * 
    * @param paths a list of folder paths, one for each supplied item id in the
    * same order specifying to which folder the new copied item will be added,
    * not <code>null</code> or empty, must have one or the same size as the
    * supplied ids.
    * @param relationshipType the name of the relationship type to use, must be
    * of category <code>New Copy</code>, defaults to the
    * <code>System/New Copy</code> relationship type if not provided.
    * @param enableRevisions a boolean flag to turn on revisions for new items
    * immediately. If this flag is <code>true</code>, any checkout of the
    * item will automatically create a new revision. Defaults to
    * <code>false</code> if not supplied which means revisions must be turned
    * on later through the workflow.
    * @return a list with all new copies created, never <code>null</code> or
    * empty. The new copies are persisted to the repository. To change the
    * returned object you must call <code>prepareForEdit</code> for it and
    * then save it through <code>saveItems</code>.
    * @throws PSErrorResultsException if it fails to create a new copy for any
    * id.
    * @throws PSErrorException for any unexpected error.
    */
   public List<PSCoreItem> newCopies(List<IPSGuid> ids, List<String> paths, 
      String relationshipType, boolean enableRevisions) 
      throws PSErrorResultsException, PSErrorException;
    /**
     * Creates a new copy for each supplied item. New copies are persisted to the
     * repository and returned to the requestor in read-only mode.
     *
     * @param ids a list of ids for all items to be copied, not <code>null</code>
     * or empty. This must be a content guid obtained from one of the IPS*Ws
     * interfaces or from the
     * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}. It
     * is an error if no item exists for any of the supplied ids. If the same id
     * is supplied multiple times, multiple copies will be created.
     *
     * @param paths a list of folder paths, one for each supplied item id in the
     * same order specifying to which folder the new copied item will be added,
     * not <code>null</code> or empty, must have one or the same size as the
     * supplied ids.
     * @param relationshipType the name of the relationship type to use, must be
     * of category <code>New Copy</code>, defaults to the
     * <code>System/New Copy</code> relationship type if not provided.
     * @param enableRevisions a boolean flag to turn on revisions for new items
     * immediately. If this flag is <code>true</code>, any checkout of the
     * item will automatically create a new revision. Defaults to
     * <code>false</code> if not supplied which means revisions must be turned
     * on later through the workflow.
     * @param enableViewForceFully a boolean flag to turn on view for new items
     *vIf this flag is <code>true</code>, then it wil skip the validation of handleRevision method
     * in case of new asset creation. Defaults to
     * <code>false</code> if not supplied which means handleRevision may be invoked
     * on later through the workflow.
     * @return a list with all new copies created, never <code>null</code> or
     * empty. The new copies are persisted to the repository. To change the
     * returned object you must call <code>prepareForEdit</code> for it and
     * then save it through <code>saveItems</code>.
     * @throws PSErrorResultsException if it fails to create a new copy for any
     * id.
     * @throws PSErrorException for any unexpected error.
     */
    public List<PSCoreItem> newCopies(List<IPSGuid> ids, List<String> paths,
                                      String relationshipType, boolean enableRevisions, boolean enableViewForceFully)
            throws PSErrorResultsException, PSErrorException;

    /**
    * @deprecated Use 
    * {@link #newPromotableVersions(List, List, String, boolean)}
    */
   @Deprecated
   public List<PSCoreItem> newPromotableVersions(List<IPSGuid> ids, 
      List<String> paths, String relationshipType, boolean enableRevisions, 
      String session, String user) 
      throws PSErrorResultsException, PSErrorException;
   
   /**
    * Creates a new promotable version for each supplied item id. New promotable
    * versions are persisted to the repository and returned to the requestor in
    * read-only mode.
    * 
    * @param ids a list with all item ids for which to create a new promotable
    * version, not <code>null</code> or empty. This must be a content guid
    * obtained from one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}. It
    * is an error if no item exists for any of the supplied ids. If the same id
    * is supplied multiple times, multiple promotable versions will be created.
    * 
    * @param paths a list of folder paths, one for each supplied item id in the
    * same order specifying to which folder the new promotable versions will be
    * added, not <code>null</code> or empty, must have one or the same size as
    * the supplied ids.
    * @param relationshipType the name of the relationship type to use, must be
    * of category <code>Promotable Version</code>, defaults to the
    * <code>System/Promotable Version</code> relationship type if not
    * provided.
    * @param enableRevisions a boolean flag to turn on revisions for new items
    * immediately. If this flag is <code>true</code>, any checkout of the
    * item will automatically create a new revision. Defaults to
    * <code>false</code> if not supplied which means revisions must be turned
    * on later through the workflow.
    * 
    * @return a list with all new promotable versions created, never
    * <code>null</code> or empty. The returned items are persisted to the
    * repository, only changes to the returned objects must be saved to the
    * repository through the <code>saveItems</code> method.
    * @throws PSErrorResultsException if it fails to create a new promotable
    * versions for any id.
    * @throws PSErrorException for any unexpected error.
    */
   public List<PSCoreItem> newPromotableVersions(List<IPSGuid> ids, 
      List<String> paths, String relationshipType, boolean enableRevisions) 
      throws PSErrorResultsException, PSErrorException;
   
   /**
    * @deprecated Use {@link #newTranslations(List, List, String, boolean)}
    */
   @Deprecated
   public List<PSCoreItem> newTranslations(List<IPSGuid> ids, 
      List<PSAutoTranslation> translationsSettings, String relationshipType, 
      boolean enableRevisions, String session, String user) 
      throws PSErrorResultsException, PSErrorException;
   
   /**
    * Creates new translations for all supplied item ids for each supplied auto
    * translation definition. New translations are persisted to the repository
    * and returned to the requestor in read-only mode.
    * 
    * @param ids a list of ids for all items to be translated, not
    * <code>null</code> or empty. This must be a content guid obtained from
    * one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}. It
    * is an error if no item exists for any of the supplied ids. If the same id
    * is supplied multiple times, multiple translations are only created if also
    * multiple auto translations are supplied with different languages.
    * 
    * @param translationsSettings a list of auto translation definitions for
    * which to create the translations. If not provided all currently defined
    * system auto translations will be used.
    * @param relationshipType the name or the relationship type to use, must be
    * of category <code>Translation</code>, defaults to the
    * <code>System/Translation</code> relationship type if not provided.
    * @param enableRevisions a boolean flag to turn on revisions for new items
    * immediately. If this flag is <code>true</code>, any checkout of the
    * item will automatically create a new revision. Defaults to
    * <code>false</code> if not supplied which means revisions must be turned
    * on later through the workflow.
    * 
    * @return a list with all new translations created, never <code>null</code>
    * or empty. The returned items are persisted to the repository, only changes
    * to the returned objects must be saved to the repository through the
    * <code>saveItems</code> method.
    * @throws PSErrorResultsException if it fails to create a new translation
    * for any id.
    * @throws PSErrorException for any unexpected error.
    */
   public List<PSCoreItem> newTranslations(List<IPSGuid> ids, 
      List<PSAutoTranslation> translationsSettings, String relationshipType, 
      boolean enableRevisions) 
      throws PSErrorResultsException, PSErrorException;
   
   /**
    * @deprecated Use {@link #findRevisions(List)}
    */
   @Deprecated
   public List<PSRevisions> findRevisions(List<IPSGuid> ids, String session, 
      String user)
      throws PSErrorException;
   
   /**
    * Finds all revisions available for all supplied item ids.
    * 
    * @param ids a list of item ids for which to get the revisions, not
    * <code>null</code> or empty. It is an error if no item exists for any of
    * the supplied ids. This must be a content guid obtained from one of the
    * IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @return a list of item revisions in the same order as requested, never
    * <code>null</code> or empty. The ids returned with each result must
    * include the revision.
    * @throws PSErrorException if no item exists for any of the supplied ids.
    */
   public List<PSRevisions> findRevisions(List<IPSGuid> ids)
      throws PSErrorException;
   
   /**
    * @deprecated Use {@link #promoteRevisions(List)}
    */
   @Deprecated
   public void promoteRevisions(List<IPSGuid> ids, String session, String user)
   throws PSErrorsException, PSErrorException;
   
   /**
    * Promotes all items to the revision as it is included in the supplied ids.
    * Use the <code>findRevisions</code> service to get the revision specific
    * ids.
    * 
    * @param ids a list of item ids for which to promote the item, the revision
    * must be included in the ids, not <code>null</code> or empty. This must
    * be a content guid obtained from one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}. It
    * is an error if no item exists for any of the supplied ids. All items must
    * be checked in to promote a revision.
    * 
    * @throws PSErrorsException if we fail to promote the revision for any
    * supplied item.
    * @throws PSErrorException for any unexpected error.
    */
   public void promoteRevisions(List<IPSGuid> ids)
      throws PSErrorsException, PSErrorException;

   /**
    * @deprecated Use {@link #checkinItems(List, String)}
    */
   @Deprecated
   public void checkinItems(List<IPSGuid> ids, String comment, String user)
      throws PSErrorsException;
   
   /**
    * Checkin all items identified through the supplied ids. It is ok to checkin
    * items which are already checked in.
    * 
    * @param ids a list with ids for all items to be checked in, not
    * <code>null</code> or empty. This must be a content guid obtained from
    * one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}. It
    * is an error if no item exists for any of the supplied ids.
    * 
    * @param comment an optional comment used for this checkin, may be
    * <code>null</code> or empty.
    * 
    * @throws PSErrorsException if any item fails to be checked in.
    */
   public void checkinItems(List<IPSGuid> ids, String comment)
      throws PSErrorsException;
   
   /**
    * Checkin all items identified through the supplied ids. It is ok to checkin
    * items which are already checked in.
    * 
    * @param ids a list with ids for all items to be checked in, not
    * <code>null</code> or empty. This must be a content guid obtained from
    * one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}. It
    * is an error if no item exists for any of the supplied ids.
    * 
    * @param comment an optional comment used for this checkin, may be
    * <code>null</code> or empty.
    * 
    * @param ignoreRevisionCheck boolean to indicate whether to use handleRevision to check whether
    * user has the currect revision or not. Useful for force check-in cases.
    * 
    * 
    * @throws PSErrorsException if any item fails to be checked in.
    */
   public void checkinItems(List<IPSGuid> ids, String comment, boolean ignoreRevisionCheck)
      throws PSErrorsException;
   
   /**
    * @deprecated Call {@link #checkoutItems(List, String)}
    */
   @Deprecated
   public void checkoutItems(List<IPSGuid> ids, String comment, String user) 
      throws PSErrorsException;
   
   /**
    * Checkout all items identified through the supplied ids. It is ok to check
    * out items which are checked out already by the same user and session.
    * 
    * @param ids a list of ids for all items to be checked out, not
    * <code>null</code> or empty. This must be a content guid obtained from
    * one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}. It
    * is an error if no item exists for any of the supplied ids.
    * 
    * @param comment an optional comment used for this checkout, may be
    * <code>null</code> or empty.
    * 
    * @throws PSErrorsException if any item fails to be checked out.
    */
   public void checkoutItems(List<IPSGuid> ids, String comment) 
      throws PSErrorsException;
   
   /**
    * @deprecated Call 
    * {@link #getAssemblyUrls(List, String, int, String, String, String)}
    */
   @Deprecated
   public List<String> getAssemblyUrls(List<IPSGuid> ids, String template, 
      int context, String filter, String site, String folderPath, String user)
      throws PSErrorException;
   
   /**
    * Get the revision specific assembly urls for the supplied item ids and url
    * parameters.
    * 
    * @param ids The ids of all items for which to get the assembly urls, not
    * null or empty. All ids may include the revision to get the assembly urls
    * for a specific revision. If an id does not include the revision, the
    * revision of the assembly url will be selected according to the 'context'
    * of the request. If the 'context' of the request is <code>0</code>
    * (preview context), then uses edit revision if the item is checked out by
    * the requester; otherwise uses current revision. If the 'context' of the
    * request is not <code>0</code>, then uses public revision (if exists);
    * otherwise uses current revision (if public revision does not exist). This
    * must be a content guid obtained from one of the IPS*Ws interfaces or from
    * the {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param template the template name for which to get the assembly urls, not
    * <code>null</code> or empty.
    * @param context the context for which to get the assembly urls, not
    * <code>null</code>.
    * @param filter the item filter name for which to get the assembly urls, not
    * <code>null</code> or empty.
    * @param site the site name for which to get the assembly urls, may be
    * <code>null</code> or empty.
    * @param folderPath the folder path for which to get the assembly urls, may
    * be <code>null</code> or empty.
    * 
    * @return a list of assembly urls for the specified ids and url parameters
    * in the same order as requested, never <code>null</code> or empty.
    * 
    * @throws PSErrorException for any unexpected error.
    */
   public List<String> getAssemblyUrls(List<IPSGuid> ids, String template, 
      int context, String filter, String site, String folderPath)
      throws PSErrorException;
   
   /**
    * @deprecated Use {@link #createChildEntries(IPSGuid, String, int)}
    */
   @Deprecated
   public List<PSItemChildEntry> createChildEntries(IPSGuid id, String name, 
      int count, String session, String user) 
      throws PSUnknownChildException, PSInvalidStateException, PSErrorException;
   
   /**
    * Creates the specified number of child entries filled with default values
    * for the requested item and field set. The item must be prepared for edit
    * prior to this call. All returned child entries will not have been
    * persisted to the repository.
    * 
    * @param id the id of the item for which to create new child entries, not
    * <code>null</code> or empty, must be a valid id of an existing item. This
    * must be a content guid obtained from one of the IPS*Ws interfaces or from
    * the {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param name the field set name of the child for which to create the new
    * child entries, not <code>null</code> or empty, must be an existing field
    * set for the specified item.
    * @param count the number of how many new child entries to create, must be
    * greater then 0.
    * 
    * @return a list with all new child entries filled with default values,
    * never <code>null</code> or empty. All returned child entries have not
    * been persisted to the repository, and their action is set to
    * {@link PSItemChildEntry#CHILD_ACTION_UPDATE}.
    * @throws PSUnknownChildException if the requestor supplied a field set name
    * which is not defined for the specified item.
    * @throws PSInvalidStateException if the item is not prepared for edit.
    * @throws PSErrorException for any unexpected error.
    */
   public List<PSItemChildEntry> createChildEntries(IPSGuid id, String name, 
      int count) 
      throws PSUnknownChildException, PSInvalidStateException, PSErrorException;
   
   /**
    * @deprecated Use {@link #loadChildEntries(IPSGuid, String, boolean)}
    */
   @Deprecated
   @IPSWsMethod(ignore=true)
   public List<PSItemChildEntry> loadChildEntries(IPSGuid contentId, String name, 
      boolean includeBinary, String session, String user)
      throws PSUnknownChildException, PSErrorResultsException, PSErrorException;
   
   /**
    * Loads all child entries from the specified item and field set. The item
    * must be prepared for edit prior to this call if you want to edit the
    * loaded child entries.
    * 
    * @param contentId the id of the item from which to load the child entries,
    * not <code>null</code> or empty, must be a valid id of an existing item.
    * This must be a content guid obtained from one of the IPS*Ws interfaces or
    * from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param name the field set name of the child for which to load the child
    * entries, not <code>null</code> or empty, must be a valid field set.
    * @param includeBinary a boolean flag to specify whether or not to include
    * binary fields with the returned child entries. Defaults to
    * <code>false</code> if not supplied.
    * 
    * @return a list with all loaded child entries, never <code>null</code> or
    * empty, action is set to {@link PSItemChildEntry#CHILD_ACTION_UPDATE}
    * @throws PSUnknownChildException if the requestor supplied a field set name
    * which is not defined for the specified item.
    * @throws PSErrorResultsException if any item child entries fail to load.
    * @throws PSErrorException for any unexpected error.
    */
   @IPSWsMethod(ignore=true)
   public List<PSItemChildEntry> loadChildEntries(IPSGuid contentId, 
      String name, boolean includeBinary)
      throws PSUnknownChildException, PSErrorResultsException, PSErrorException;
   
   /**
    * @deprecated Use {@link #saveChildEntries(IPSGuid, String, List)}
    */
   @Deprecated
   public void saveChildEntries(IPSGuid contentId, String name, 
      List<PSItemChildEntry> entries, String session, String user)
      throws PSUnknownChildException, PSInvalidStateException, 
         PSErrorsException, PSErrorException;
   
   /**
    * Saves the provided child entries to the specified item and field set. It
    * is an error if the user calls this service for items that are not prepared
    * for edit.
    * 
    * @param contentId the id of the item for which to save the child entries,
    * not <code>null</code> or empty, must be a valid id of an existing item.
    * The item must be in edit mode. This must be a content guid obtained from
    * one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param name the field set name of the child for which to save the child
    * entries, not <code>null</code> or empty, must be an existing field set.
    * @param entries a list with all child entries to be saved, not
    * <code>null</code> or empty, must be valid child child entries for the
    * specified field set.
    * 
    * @throws PSUnknownChildException if the requestor supplied a field set name
    * which is not defined for the specified item.
    * @throws PSInvalidStateException if the item is not prepared for edit.
    * @throws PSErrorsException if any child entry fails to be saved.
    * @throws PSErrorException for any unexpected error.
    */
   public void saveChildEntries(IPSGuid contentId, String name, 
      List<PSItemChildEntry> entries)
      throws PSUnknownChildException, PSInvalidStateException, 
         PSErrorsException, PSErrorException;
   
   /**
    * @deprecated Use {@link #deleteChildEntries(IPSGuid, String, List)}
    */
   @Deprecated
   public void deleteChildEntries(IPSGuid id, String name, 
      List<IPSGuid> childIds, String session, String user)
      throws PSUnknownChildException, PSInvalidStateException, 
         PSErrorsException, PSErrorException;
   
   /**
    * Deletes the requested child entries from the specified item and field set.
    * It is an error if the user calls this service for items that are not
    * prepared for edit.
    * 
    * @param id the id of the item from which to delete the child entries, not
    * <code>null</code> or empty, must be a valid id of an existing item. The
    * item must be in edit mode. This must be a content guid obtained from one
    * of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param name the field set name of the child for which to delete the child
    * entries, not <code>null</code> or empty.
    * @param childIds a list with all child ids to be deleted, not
    * <code>null</code> or empty. We ignore cases where a child of a supplied
    * child id does not exist.
    * 
    * @throws PSUnknownChildException if the requestor supplied a field set name
    * which is not defined for the specified item.
    * @throws PSInvalidStateException if the item is not prepared for edit.
    * @throws PSErrorsException if any child entry fails to be deleted.
    * @throws PSErrorException for any unexpected error.
    */
   public void deleteChildEntries(IPSGuid id, String name, 
      List<IPSGuid> childIds)
      throws PSUnknownChildException, PSInvalidStateException, 
         PSErrorsException, PSErrorException;
   
   /**
    * @deprecated Use {@link #reorderChildEntries(IPSGuid, String, List)}
    */
   @Deprecated
   public void reorderChildEntries(IPSGuid id, String name, 
      List<IPSGuid> childIds, String session, String user)
      throws PSUnknownChildException, PSInvalidStateException, 
         PSErrorsException, PSErrorException;
   
   /**
    * Reorders all child entries in the order as supplied. The item must be
    * prepared for edit prior to this call.
    * 
    * @param id the id of the item for which to reorder the child entries, not
    * <code>null</code> or empty, must be a valid id of an existing item. The
    * item must be in edit mode. This must be a content guid obtained from one
    * of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param name the field set name of the child for which to reorder the child
    * entries, not <code>null</code> or empty. It is an error if the specified
    * child does not support sorting.
    * 
    * @param childIds a list with all child entry ids for the specified item,
    * not <code>null</code> or empty. This list specifies the new order of the
    * child entries. The specified child entries sortrank will start with 0 for
    * the first specified child id and be incremented by 1 for any additional
    * child id. All child entries that exist but are not specified with the
    * supplied ids will be appended to the end in the current order.
    * 
    * @throws PSUnknownChildException if the requestor supplied a field set name
    * which is not defined for the specified item.
    * @throws PSInvalidStateException if the item is not prepared for edit.
    * @throws PSErrorsException if any child entry fails to be reordered.
    * @throws PSErrorException for any unexpected error.
    */
   public void reorderChildEntries(IPSGuid id, String name, 
      List<IPSGuid> childIds)
      throws PSUnknownChildException, PSInvalidStateException, 
         PSErrorsException, PSErrorException;
   
   /**
    * @deprecated Use 
    * {@link #addContentRelations(IPSGuid, List, String, String, String, int)}
    */
   @Deprecated
   public List<PSAaRelationship> addContentRelations(IPSGuid id, 
      List<IPSGuid> relatedIds, String slot, String template, 
      int index,  String relationshipName, String user)
      throws PSErrorException;
   
   /**
    * Create the content relations for the specified item, slot, template and
    * index for the supplied related item ids. The type (or name) of the 
    * relationship created is determined through the slot definition found for 
    * the supplied slot name. However, the caller must specify the relationship
    * name if the relationship name is not defined in the specified slot.
    * 
    * @param id the item for which to create the new content relations, not
    * <code>null</code>. This must be a content guid obtained from one of the
    * IPS*WS interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param relatedIds the ids of all items to be related to the specified item
    * and slot, not <code>null</code> or empty. This must be a content guid
    * obtained from one of the IPS*WS interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param slot the name of the slot to which to add new content relations for
    * the specified related item ids, must be a valid slot for the specified
    * item, not <code>null</code> or empty. 
    * @param template the name of the template for which to create the new
    * content relations, not <code>null</code> or empty. Must be a valid
    * template id for the edited item and specified slot.
    * @param index the 0 based index at which position to insert the provided
    * related items in the order as supplied. If it is <code>-1</code> or
    * greater than the existing relationship size, then the new content
    * relations are appended to the existing ones in the order as the related
    * items were supplied.
    * @param relationshipName the name of the relationship type (or
    * configuration). It may be <code>null</code> or empty if not specified.
    * If it is specified, then its category must be
    * {@link com.percussion.design.objectstore.PSRelationshipConfig#CATEGORY_ACTIVE_ASSEMBLY}.
    * It will be used if the relationship name is not defined in the specified
    * slot. However, it must be the same as relationship name defined in the
    * slot if both are not <code>null</code> or empty. 
    * 
    * @return a list of created active assembly relationships in the order of
    * sort rank property, never <code>null</code> or empty. All content
    * relations are persisted in the repository.
    * 
    * @throws PSErrorException if an error occurs while loading or saving
    * relationships.
    * 
    * @see #loadContentRelations(PSRelationshipFilter, boolean)
    */
   public List<PSAaRelationship> addContentRelations(IPSGuid id, 
      List<IPSGuid> relatedIds, String slot, String template, 
      String relationshipName, int index)
      throws PSErrorException;

   /**
    * @deprecated Use {@link #addContentRelations(IPSGuid, List, String, String, int)}
    */
   @Deprecated
   public List<PSAaRelationship> addContentRelations(IPSGuid id, 
      List<IPSGuid> relatedIds, String slot, String template, 
      int index,  String user)
      throws PSErrorException;
   
   /**
    * Create the content relations for the specified item, slot, template and
    * index for the supplied related item ids. The type of the relationship
    * created is determined through the slot definition found for the supplied
    * slot name.
    * 
    * @param id the item for which to create the new content relations, not
    * <code>null</code>. This must be a content guid obtained from one of the
    * IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param relatedIds the ids of all items to be related to the specified item
    * and slot, not <code>null</code> or empty. This must be a content guid
    * obtained from one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param slot the name of the slot to which to add new content relations for
    * the specified related item ids, must be a valid slot for the specified
    * item, nut <code>null</code> or empty.
    * @param template the name of the template for which to create the new
    * content relations, not <code>null</code> or empty. Must be a valid
    * template id for the edited item and specified slot.
    * @param index the 0 based index at which position to insert the provided
    * related items in the order as supplied. If it is <code>-1</code> or
    * greater than the existing relationship size, then the new content
    * relations are appended to the existing ones in the order as the related
    * items were supplied.
    * @param user the requester. Must not be <code>null</code> or empty.
    * 
    * @return a list of created active assembly relationships in the order of
    * sort rank property, never <code>null</code> or empty. All content
    * relations are persisted in the repository.
    * 
    * @throws PSErrorException if an error occurs while loading or saving
    * relationships.
    * 
    * @see #loadContentRelations(PSRelationshipFilter, boolean)
    */
   public List<PSAaRelationship> addContentRelations(IPSGuid id, 
      List<IPSGuid> relatedIds, String slot, String template, 
      int index)
      throws PSErrorException;
   
   /**
    * @deprecated use {@link #addContentRelations(IPSGuid, List, IPSGuid, 
    * IPSGuid, IPSGuid, IPSGuid, int)}
    */
   @Deprecated
   public List<PSAaRelationship> addContentRelations(IPSGuid id, 
      List<IPSGuid> relatedIds, IPSGuid folderId, IPSGuid siteId,
      IPSGuid slotId, IPSGuid templateId, int index, String user)
      throws PSErrorException;

   /**
    * Create the content relations for the specified item, slot, template and
    * and index for the supplied related item ids (along with ids folder and
    * site ids). The type of the relationship created is determined through the 
    * slot definition found for the supplied slot name.
    * 
    * @param id the item for which to create the new content relations, not
    * <code>null</code>. This must be a content guid obtained from one of the
    * IPS*WS interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param relatedIds the ids of all items to be related to the specified item
    * and slot, not <code>null</code> or empty. This must be a content guid
    * obtained from one of the IPS*WS interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * @param folderId the id of the folder that the related items (specified by 
    * <code>relatedIds</code>) are located. It may be <code>null</code> if not 
    * specified. 
    * @param siteId the id of the site that the related items (specified by 
    * <code>relatedIds</code>) are located. It may be <code>null</code> if not 
    * specified.
    * 
    * @param slotId the id of the slot to which to add new content relations for
    * the specified related item ids, must be a valid slot for the specified
    * item, not <code>null</code> or empty.
    * @param templateId the id of the template for which to create the new
    * content relations, not <code>null</code> or empty. Must be a valid
    * template id for the edited item and specified slot.
    * @param index the 0 based index at which position to insert the provided
    * related items in the order as supplied. If it is <code>-1</code> or
    * greater than the existing relationship size, then the new content
    * relations are appended to the existing ones in the order as the related
    * items were supplied.
    * 
    * @return a list of created active assembly relationships in the order of
    * sort rank property, never <code>null</code> or empty. All content
    * relations are persisted in the repository.
    * 
    * @throws PSErrorException if an error occurs while loading or saving
    * relationships.
    * 
    * @see #loadContentRelations(PSRelationshipFilter, boolean)
    */
   public List<PSAaRelationship> addContentRelations(IPSGuid id, 
      List<IPSGuid> relatedIds, IPSGuid folderId, IPSGuid siteId,
      IPSGuid slotId, IPSGuid templateId, int index)
      throws PSErrorException;
   
   /**
    * Load all content relations for the specified filter. Note that a system
    * may have thousands of relationships defined and the perfomance may be poor
    * if the supplied filter is too general.
    * 
    * @param filter defines the parameters by which to filter the returned
    * relationships. If not supplied or no filter parameters are specified, all
    * relationships of category <code>Active Assembly</code> will be returned.
    * If the category of the filter is specified, then it must be
    * {@link PSRelationshipConfig#CATEGORY_ACTIVE_ASSEMBLY}. If the
    * relationship names of the filter are specified, then the category of the
    * relationship type for the relationship names must be
    * {@link PSRelationshipConfig#CATEGORY_ACTIVE_ASSEMBLY}.
    * @param isLoadReferenceInfo indicates whether the reference info is needed
    * in the returned objects. The reference info includes slot, template site
    * name, folder name and path. It is <code>true</code> if the reference
    * info is needed; otherwise the reference info will be <code>null</code>
    * or empty in the returned objects.
    * 
    * @return a list with all loaded active assembly relationships, never
    * <code>null</code>, may be empty. It is in the order of 
    * {@link IPSHtmlParameters#SYS_SORTRANK} property of the relationships. 
    * 
    * @throws PSErrorException for any unexpected error.
    * 
    * @see #saveContentRelations(List, String)
    * @see #addContentRelations(IPSGuid, List, IPSGuid, IPSGuid, IPSGuid, 
    * IPSGuid, int)
    */
   @IPSWsMethod(ignore=true)
   public List<PSAaRelationship> loadContentRelations(
      PSRelationshipFilter filter, boolean isLoadReferenceInfo) 
      throws PSErrorException;
   
   /**
    * Gets a list of Active Assembly relationships which have the specified
    * owner and slot.
    * 
    * @param ownerId the owner ID of the requested relationships, not 
    * <code>null</code>.
    * @param slotid the slot id of the specified slot, not <code>null</code>.
    *    
    * @return the requested relationships in the order of , it may be empty, 
    * but never <code>null</code>. It is in the order of 
    * {@link IPSHtmlParameters#SYS_SORTRANK} property of the relationships.
    *    
    * @throws PSErrorException if failed to load the relationships.
    */
   List<PSAaRelationship> loadSlotContentRelationships(IPSGuid ownerId,
      IPSGuid slotid) throws PSErrorException;

   /**
    * @deprecated Use {@link #saveContentRelations(List)}
    */
   @Deprecated
   public void saveContentRelations(
      List<PSAaRelationship> relationships, String user)
      throws PSErrorsException, PSErrorException;
   
   /**
    * Save all supplied content relations. The owners of all provided 
    * relationships must be prepared for edit prior to this call.
    * 
    * @param relationships a list with all relationships to be saved, not 
    *    <code>null</code> or empty.
    *    
    * @throws PSErrorsException if any relationship fails to be saved.
    * @throws PSErrorException if any other error occurs.
    * 
    * @see #loadContentRelations(PSRelationshipFilter, boolean)
    * @see #addContentRelations(IPSGuid, List, IPSGuid, IPSGuid, IPSGuid, 
    * IPSGuid, int)
    */
   public void saveContentRelations(
      List<PSAaRelationship> relationships)
      throws PSErrorsException, PSErrorException;
   
   /**
    * @deprecated Use {@link #deleteContentRelations(List)}
    */
   @Deprecated
   public void deleteContentRelations(List<IPSGuid> ids, String user)
      throws PSErrorsException, PSErrorException;
   
   /**
    * Delete all content relations for the specified relationship ids. The 
    * owners of all supplied relationship ids must be prepared for edit 
    * prior to this call.
    * 
    * @param ids a list of ids for all relationships to be deleted, not 
    *    <code>null</code> or empty. We ignore cases where no relationship 
    *    exists for any of the supplied ids.
    *    
    * @throws PSErrorsException if failed to remove any specified relationships.
    * @throws PSErrorException for any unexpected error.
    */
   public void deleteContentRelations(List<IPSGuid> ids)
      throws PSErrorsException, PSErrorException;
   
   /**
    * @deprecated Use {@link #reorderContentRelations(List, int)}
    */
   @Deprecated
   public void reorderContentRelations(List<IPSGuid> ids, int index, String user)
      throws PSErrorException;   
   
   /**
    * Reorders the related items specified via relationships and move to a
    * specified location. All the items from the relationship list will be
    * arranged together in the order they occur in the order as supplied and 
    * moved to the specified location. The first item (relationship) in the 
    * list will get the order specified by the new location. Specify 0 to move 
    * to top in the slot and -1 (or a value larger than the items in the slot) 
    * to move to bottom. All relationships in the list must be for the same 
    * slot and have the same owner. 
    * 
    * @param ids a list of relationship ids in the order that you want to 
    *    reorder them, not <code>null</code> or empty. All relationships 
    *    must have the same relationship owner and slot. The owner of all 
    *    supplied relationships must be prepared for edit prior to this call. 
    * @param index the new location of the first item in the relationship list
    *    to rearrange. It is <code>-1</code> (or bigger than the current 
    *    number of relationships/items in the slot) for bottom and any other 
    *    value specified becomes the sort index of the first item in the 
    *    relationship list to rearrange.
    *     
    * @throws PSErrorException if any error occurred during reordering.
    */
   public void reorderContentRelations(List<IPSGuid> ids, int index)
      throws PSErrorException;
   
   /**
    * @deprecated Use {@link #reArrangeContentRelations(List, int)}
    */
   @Deprecated
   public void reArrangeContentRelations(List<PSAaRelationship> rels, int index, 
      String user) throws PSErrorException;
   
   /**
    * This is the same as {@link #reorderContentRelations(List, int, String)},
    * except it provides a list of relationships instead of relationship ids.
    * 
    * Reorders the related items specified via relationships and move to a
    * specified location. All the items from the relationship list will be
    * arranged together in the order they occur in the order as supplied and 
    * moved to the specified location. The first item (relationship) in the 
    * list will get the order specified by the new location. Specify 0 to move 
    * to top in the slot and -1 (or a value larger than the items in the slot) 
    * to move to bottom. All relationships in the list must be for the same 
    * slot and have the same owner. 
    * 
    * @param rels a list of relationships in the order that you want to 
    *    reorder them, not <code>null</code> or empty. All relationships 
    *    must have the same relationship owner and slot. The owner of all 
    *    supplied relationships must be prepared for edit prior to this call. 
    * @param index the new location of the first item in the relationship list
    *    to rearrange. It is <code>-1</code> (or bigger than the current 
    *    number of relationships/items in the slot) for bottom and any other 
    *    value specified becomes the sort index of the first item in the 
    *    relationship list to rearrange.
    *     
    * @throws PSErrorException if any error occurred during rearranging.
    */
   public void reArrangeContentRelations(List<PSAaRelationship> rels, int index)
      throws PSErrorException;
   
   /**
    * @deprecated Use 
    * {@link #findDependents(IPSGuid, PSRelationshipFilter, boolean)}
    */   
   @Deprecated
   public List<PSItemSummary> findDependents(IPSGuid id, 
      PSRelationshipFilter filter, boolean isLoadOperations, String user)
      throws PSErrorException;
   
   /**
    * Find all related dependents items for the specified owner and filter.
    * 
    * @param id the owner id for which to find the dependents for the specified
    * filter, not <code>null</code> or empty. Must be the id of a valid item.
    * This must be a content guid obtained from one of the IPS*Ws interfaces or
    * from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param filter defines the parameters by which to filter the returned
    * children. If not supplied or no filter parameters are specified, all
    * children will be returned for the specified item. Note, the owner of the
    * filter will be ignored if specified. If the category of the filter is
    * specified, then it must be
    * {@link PSRelationshipConfig#CATEGORY_ACTIVE_ASSEMBLY}. If the
    * relationship names of the filter are specified, then the category of the
    * relationship type for the relationship names must be
    * {@link PSRelationshipConfig#CATEGORY_ACTIVE_ASSEMBLY}.
    * @param isLoadOperations indicates whether to load the allowed operations
    * for the returned item summaries or not, <code>true</code> the allowed
    * operations will be loaded; otherwise the allowed operations of the
    * returned item summaries will be empty.
    * 
    * @return a list of item summaries for all children found for the specified
    * item and filter, never <code>null</code>, may be empty.
    * 
    * @throws PSErrorException for any unexpected error.
    */
   public List<PSItemSummary> findDependents(IPSGuid id, 
      PSRelationshipFilter filter, boolean isLoadOperations)
      throws PSErrorException;
   
   /**
    * @deprecated Use 
    * {@link #findOwners(IPSGuid, PSRelationshipFilter, boolean)}
    */   
   @Deprecated
   public List<PSItemSummary> findOwners(IPSGuid id, 
      PSRelationshipFilter filter, boolean isLoadOperations, String user) 
      throws PSErrorException;
   
   /**
    * Find all owner items for the specified dependent and filter.
    * 
    * @param id the dependent id for which to find the specified owners, not
    * <code>null</code> or empty. This must be a content guid obtained from
    * one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param filter defines the parameters by which to filter the returned
    * parents. If not supplied or no filter parameters are specified, all
    * parents will be returned for the specified item. Note, the dependents of
    * the filter will be ignored if specified. If the category of the filter is
    * specified, then it must be
    * {@link PSRelationshipConfig#CATEGORY_ACTIVE_ASSEMBLY}. If the
    * relationship names of the filter are specified, then the category of the
    * relationship type for the relationship names must be
    * {@link PSRelationshipConfig#CATEGORY_ACTIVE_ASSEMBLY}.
    * @param isLoadOperations indicates whether to load the allowed operations
    * for the returned item summaries or not, <code>true</code> the allowed
    * operations will be loaded; otherwise the allowed operations of the
    * returned item summaries will be empty.
    * 
    * @return a list of item summaries for all parents found for the specified
    * item and filter, never <code>null</code>, may be empty.
    * 
    * @throws PSErrorException for any unexpected error.
    */
   public List<PSItemSummary> findOwners(IPSGuid id, 
      PSRelationshipFilter filter, boolean isLoadOperations) 
      throws PSErrorException;
   
   /**
    * Loads a complete folder object for the specified folder ID.
    * 
    * @param id the ID of the folder to load, not <code>null</code> or
    * empty. Must be ID of an existing folder. This must be a folder guid
    * obtained from one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @return the loaded folder for the supplied ID, never <code>null</code>.
    * 
    * @throws PSErrorException if folder fails to load.
    */
   @IPSWsMethod(ignore=true)
   public PSFolder loadFolder(IPSGuid id);
   
   /**
    * Loads the specified folder, the same as {@link #loadFolder(IPSGuid)},
    * except this has an option to load the transient data or not.
    * 
    * @param id the ID of the folder, not <code>null</code>.
    * @param loadTransientData <code>true</code> if load the transient data, 
    * such as community name, display name, ...etc.
    * 
    * @return the specified folder, never <code>null</code>.
    * 
    * @throws PSErrorException if failed to load the folder.
    */
   @IPSWsMethod(ignore=true)
   public PSFolder loadFolder(IPSGuid id, boolean loadTransientData)
         throws PSErrorException;

   /**
    * Loads the complete folder objects for all specified folder ids.
    * 
    * @param ids the ids of all folders to load, not <code>null</code> or
    * empty. Must be ids of existing folders. This must be a folder guid
    * obtained from one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @return a list with all folders loaded for the supplied ids in the same
    * order as requested, never <code>null</code>, may be empty.
    * @throws PSErrorResultsException if any folder fails to load.
    */
   @IPSWsMethod(ignore=true)
   public List<PSFolder> loadFolders(List<IPSGuid> ids) 
      throws PSErrorResultsException;
   
   /**
    * Loads the complete folder objects for all specified folder ids.
    * The same as {@link #loadFolders(List)}, except this has option to load
    * additional transient info.
    * 
    * @param ids the ids of all folders to load, not <code>null</code> or
    * empty. Must be ids of existing folders. This must be a folder guid
    * obtained from one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * @param loadTransientData <code>true</code> if load the transient data, 
    * such as community name, display name, ...etc.
    * 
    * @return a list with all folders loaded for the supplied ids in the same
    * order as requested, never <code>null</code>, may be empty.
    * @throws PSErrorResultsException if any folder fails to load.
    */
   @IPSWsMethod(ignore=true)
   public List<PSFolder> loadFolders(List<IPSGuid> ids, boolean loadTransientData) 
      throws PSErrorResultsException;
   
   /**
    * Finds all descendant folders that are sub-folders and grand sub-folders of the 
    * specified folder.
    * 
    * @param id the ID of the specified folder, not <code>null</code>.
    * 
    * @return the descendant folders, <code>null</code>, may be empty. The returned
    * folders (if there is any) do not contain any transient data, 
    * such as community name, display format name, ...etc.
    */
   @IPSWsMethod(ignore=true)
   public List<PSFolder> findDescendantFolders(IPSGuid id);
   
   /**
    * Loads the complete folder objects for all specified paths.
    * 
    * @param paths the fully qualified folder paths for which to load the 
    *    folders, not <code>null</code> or empty. An error will be returned 
    *    if any of the supplied path is invalid. Provide '/' to get the root 
    *    folders such as <code>Folders</code> and <code>Sites</code>.
    * @return a list with all folders loaded for the supplied paths in the 
    *    same order as requested, never <code>null</code>, may be empty.
    * @throws PSErrorResultsException if any folder fails to load.
    */
   @IPSWsMethod(ignore=true)
   public List<PSFolder> loadFolders(String[] paths) 
      throws PSErrorResultsException;
   
   /**
    * Save all supplied folder definitions. It can only existing folders, must
    * use {@link #addFolder(String, String, String)} or 
    * {@link #addFolderTree(String, String)} to create new folders.
    * 
    * @param folders a list of folders to be saved, not <code>null</code> or 
    *    empty. 
    * @return a list of folder ids for all saved folders in the same order 
    *    as requested, never <code>null</code> or empty.
    * @throws PSErrorResultsException if any folder fails to save.
    */
   public List<IPSGuid> saveFolders(List<PSFolder> folders) 
      throws PSErrorResultsException;
   
   /**
    * Saves the specified folder to the repository.
    * 
    * @param folder the to be saved folder, never <code>null</code>.
    *    It may or may not be a persisted folder.
    *    
    * @return the saved folder which contains all persisted keys and data.
    * 
    * @throws PSErrorException if failed to save the specified folder.
    */
   public PSFolder saveFolder(PSFolder folder) throws PSErrorException;
   
   /**
    * Delete all folders for the specified ids. The deletes are always recursive
    * meaning that the specified folders including all child folders and items
    * if requested are deleted. We ignore cases where no folder exists for any
    * of the specified ids.
    * 
    * @param ids a list of ids for all folders to be deleted, not
    * <code>null</code> or empty. This must be a folder guid obtained from one
    * of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * This deletes all folders for the supplied ids including all child folders
    * recursivly. Child items may be purged if so requested.
    * 
    * @param purgeItems a boolean flag to specify whether or not to purge child
    * items. Defaults to <code>false</code> if not supplied. The requesting
    * user must have admin previledges to purge items.
    * 
    * @throws PSErrorsException if any folder fails to be deleted.
    */
   public void deleteFolders(List<IPSGuid> ids, boolean purgeItems) 
      throws PSErrorsException;

   /**
    * Deletes a list of specified folders. This is the same as
    * {@link #deleteFolders(List, boolean)}, in addition, this has an option
    * to validate folder permissions during the delete operation.
    * 
    * @param ids a list of ids for all folders to be deleted, not
    * <code>null</code> or empty. This must be a folder guid obtained from one
    * of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * This deletes all folders for the supplied ids including all child folders
    * recursivly. Child items may be purged if so requested.
    * 
    * @param purgeItems a boolean flag to specify whether or not to purge child
    * items. Defaults to <code>false</code> if not supplied. The requesting
    * user must have admin previledges to purge items.
    * 
    * @param checkFolderPermission if <code>true</code>, then validate folder
    * permission before delete the folder; otherwise the folder permission is
    * ignored during the delete operation.
    * 
    * @throws PSErrorsException if any folder fails to be deleted.
    */
   public void deleteFolders(List<IPSGuid> ids, boolean purgeItems,
         boolean checkFolderPermission) throws PSErrorsException;

   /**
    * @deprecated Use {@link #addFolder(String, String)}
    */
   @Deprecated
   public PSFolder addFolder(String name, String path, String user) 
      throws PSErrorException;
   
   /**
    * Add a new folder for the specified label to the specified parent 
    * inheriting the ACL's from the parent folder.
    * 
    * @param name the name for the new folder, not <code>null</code> or 
    *    empty. Must be a unique label within the folder to which the 
    *    created folder will be added.
    * @param path the fully qualified path of the parent folder to which 
    *    the new folder will be added, not <code>null</code> or empty, 
    *    must be the path of an existing folder. If the requestor is not in 
    *    the user list of the parent this will add him automatically.
    *    
    * @return the newly added folder, never <code>null</code> or empty.
    * 
    * @throws PSErrorException if the requested folder could not be created 
    *    in the specified parent.
    */
   public PSFolder addFolder(String name, String path) 
      throws PSErrorException;

   /**
    * Add a new folder for the specified label to the specified parent 
    * inheriting the ACL's from the parent folder.
    * 
    * @param name the name for the new folder, not <code>null</code> or 
    *    empty. Must be a unique label within the folder to which the 
    *    created folder will be added.
    * @param path the fully qualified path of the parent folder to which 
    *    the new folder will be added, not <code>null</code> or empty, 
    *    must be the path of an existing folder. If the requestor is not in 
    *    the user list of the parent this will add him automatically.
    * @param loadTransientData <code>true</code> if load the transient data in
    *    the returned objects. The transient data includes community
    *    name, display name, ...etc.
    *    
    * @return the newly added folder, never <code>null</code> or empty.
    * 
    * @throws PSErrorException if the requested folder could not be created 
    *    in the specified parent.
    */
   public PSFolder addFolder(String name, String path, boolean loadTransientData) 
      throws PSErrorException;

   /**
    * The same as {@link #addFolder(String, String, boolean)}, except the folder is created
    * (or cloned) based on the specified source folder.
    * 
    * @param name the name for the new folder, not <code>null</code> or 
    *    empty. Must be a unique label within the folder to which the 
    *    created folder will be added.
    * @param parentPath the fully qualified path of the parent folder to which 
    *    the new folder will be added, not <code>null</code> or empty, 
    *    must be the path of an existing folder. If the requestor is not in 
    *    the user list of the parent this will add him automatically.
    * @param srcPath the fully qualified path of the source folder to which 
    *    the new folder will be cloned from, not <code>null</code> or empty, 
    *    must be the path of an existing folder. If the requestor is not in 
    *    the user list of the parent this will add him automatically.
    * @param loadTransientData <code>true</code> if load the transient data in
    *    the returned objects. The transient data includes community
    *    name, display name, ...etc.
    *    
    * @return the created folder, never <code>null</code> or empty.
    * 
    * @throws PSErrorException if the folder could not be cloned from the source folder.
    */
   public PSFolder addFolder(String name, String parentPath, String srcPath, boolean loadTransientData);
   
   /**
    * @deperecated Use {@link #addFolderTree(String)}
    */
   @Deprecated
   public List<PSFolder> addFolderTree(String path, String user) 
      throws PSErrorResultsException, PSErrorException;
   
   /**
    * Add a folder tree for the specified fully qualified path. All folders 
    * will be created starting with the first folder that does not exist in 
    * the supplied path. ACL's are inherited from the parents.
    * 
    * @param path the fully qualified path of the folder tree to create, 
    *    not <code>null</code> or empty, for new root folders you must 
    *    provide a '/' with some name. The name must be unique within the 
    *    parent folder
    *    
    * @return the newly added folders, never <code>null</code>, may be empty.
    * 
    * @throws PSErrorException if any of the path folders could not be created.
    * @throws PSErrorException if cannot find an existing folder from the
    *    specified path.
    */
   public List<PSFolder> addFolderTree(String path) 
      throws PSErrorResultsException, PSErrorException;

   /**
    * Add a folder tree for the specified fully qualified path. All folders will
    * be created starting with the first folder that does not exist in the
    * supplied path. ACL's are inherited from the parents.
    * 
    * @param path the fully qualified path of the folder tree to create, not
    *           <code>null</code> or empty, for new root folders you must
    *           provide a '/' with some name. The name must be unique within the
    *           parent folder
    * @param loadTransientData <code>true</code> if load the transient data in
    *           the returned objects. The transient data includes community
    *           name, display name, ...etc.
    * 
    * @return the newly added folders, never <code>null</code>, may be empty.
    * 
    * @throws PSErrorException if any of the path folders could not be created.
    * @throws PSErrorException if cannot find an existing folder from the
    *            specified path.
    */
   public List<PSFolder> addFolderTree(String path, boolean loadTransientData) 
      throws PSErrorResultsException, PSErrorException;

   /**
    * @deperecated Use {@link #findFolderChildren(IPSGuid, boolean)}
    */
   @Deprecated
   public List<PSItemSummary> findFolderChildren(IPSGuid id, 
      boolean isLoadOperations, String user) throws PSErrorException;
   
   /**
    * Find all direct children for the supplied folder id. This finds all
    * objects such as items and folders.
    * 
    * @param id the id of the source folder for which to find all children, not
    * <code>null</code>, must be an id of an existing folder. This must be a
    * folder guid obtained from one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param isLoadOperations indicates whether to load the allowed operations
    * for the returned item summaries or not, <code>true</code> the allowed
    * operations will be loaded; otherwise the allowed operations of the
    * returned item summaries will be empty.
    * 
    * @return a list of item and folder summaries for all child objects found
    * for the requested folder, never <code>null</code>, may be empty.
    * 
    * @throws PSErrorException for any unexpected error.
    */
   public List<PSItemSummary> findFolderChildren(IPSGuid id, 
      boolean isLoadOperations) throws PSErrorException;
   
   /**
    * Finds direct folder children of the supplied folder id.  Does not return items.
    * 
    * @param id The id of an existing parent folder, not <code>null</code>.
    * @return The child folder summaries, not <code>null</code>, may be empty.
    * 
    * @throws PSErrorException for any unexpected error.
    */
   List<PSItemSummary> findChildFolders(IPSGuid id) throws PSErrorException;
   
   /**
    * @deperecated Use {@link #findFolderChildren(String, boolean)}
    */
   @Deprecated
   public List<PSItemSummary> findFolderChildren(String path,
      boolean isLoadOperations, String user) throws PSErrorException;   
   
   /**
    * Find all direct folder parents for the supplied item or folder.
    * 
    * @param id the ID of the specified item or folder, never <code>null</code>.
    * @param isLoadOperations indicates whether to load the allowed operations 
    *    for the returned item summaries or not, <code>true</code> the 
    *    allowed operations will be loaded; otherwise the allowed operations of 
    *    the returned item summaries will be empty.
    *    
    * @return a list of folder summaries, which are the direct parent folders
    *   of the specified item or folder, never <code>null</code>, may be empty.
    */
   public List<PSItemSummary> findFolderParents(IPSGuid id,
         boolean isLoadOperation);
   
   /**
    * Find all direct children for the supplied folder path. This finds all 
    * objects such as items and folders.
    * 
    * @param path the fully qualified folder path for which to find all 
    *    children, not <code>null</code> or empty, must be the path of an 
    *    existing folder. Provide '/' to get all root folders such as 
    *    <code>Folders</code> and <code>Sites</code>.
    * @param isLoadOperations indicates whether to load the allowed operations 
    *    for the returned item summaries or not, <code>true</code> the 
    *    allowed operations will be loaded; otherwise the allowed operations of 
    *    the returned item summaries will be empty.
    *    
    * @return a list of item and folder summaries for all child objects 
    *    found for the requested folder, never <code>null</code>, may be empty.
    *    
    * @throws PSErrorException for any unexpected error.
    */
   public List<PSItemSummary> findFolderChildren(String path,
      boolean isLoadOperations) throws PSErrorException;
   
   /**
    * Add all supplied children to the specified folder.
    * 
    * @param parentId the id of the folder to which the suppplied children will
    * be added, not <code>null</code>, must be an id to an existing folder.
    * This must be a content guid obtained from one of the IPS*Ws interfaces or
    * from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param childIds a list of object ids which we want to add to the specified
    * parent folder, not <code>null</code> or empty, must be ids of existing
    * objects such as items and folders, and must be a list of
    * {@link PSLegacyGuid} instances. All child folder names added must be
    * unique withing the target folder.
    * 
    * @throws PSErrorException if the supplied children could not be added to
    * the specified parent.
    */
   public void addFolderChildren(IPSGuid parentId, List<IPSGuid> childIds) 
      throws PSErrorException;
   
   /**
    * Add all supplied children to the specified folder.
    * 
    * @param parentPath the fully qualified folder path to which to add all
    * supplied children, not <code>null</code> or empty, must be the path of
    * an existing folder.
    * 
    * @param childIds a list of object ids which we want to add to the specified
    * target folder, not <code>null</code> or empty, must be ids of existing
    * objects such as items and folders. This must be a content or folder guid
    * obtained from one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.All
    * child folder names added must be unique withing the target folder.
    * 
    * @throws PSErrorException if the supplied children could not be added to
    * the specified parent.
    */
   public void addFolderChildren(String parentPath, List<IPSGuid> childIds) 
      throws PSErrorException;
   
   /**
    * Removes the supplied folder children from the specified folder.
    * 
    * @param parentId the id of the folder from which we want the children
    * removed, not <code>null</code>, must be an id of an existing folder.
    * This must be a folder guid obtained from one of the IPS*Ws interfaces or
    * from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param childIds a list of object ids which we want removed from the
    * specified folder. All folders and items will be removed if not specified.
    * We ignore ids for which no child object is found.
    * @param purgeItems a boolean flag to specify whether we only want items
    * removed from the folder or purged from the system entirely. Defaults to
    * <code>false</code> if not supplied. The requesting user must have admin
    * previledges to purge items.
    * 
    * @throws PSErrorsException if any child folder fails to be removed from the
    * parent.
    * @throws PSErrorException if any other error occurs.
    */
   public void removeFolderChildren(IPSGuid parentId, 
      List<IPSGuid> childIds, boolean purgeItems) 
      throws PSErrorsException, PSErrorException;

   public void removeFolderChildren(IPSGuid parentId,
                                    List<IPSGuid> childIds, boolean purgeItems,
                                    String relationshipTypeName)
           throws PSErrorsException, PSErrorException;
   
   /**
    * Removes the supplied folder children from the specified folder.
    * 
    * @param parentPath the fully qualified folder path from which to remove all
    * supplied children, not <code>null</code> or empty, must be the path of
    * an existing folder.
    * 
    * @param childIds a list of object ids which we want removed from the
    * specified folder. All folders and items will be removed if not specified.
    * Each entry must be a content or folder guid obtained from one of the
    * IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param purgeItems a boolean flag to specify whether we only want items
    * removed from the folder or purged from the system entirely. Defaults to
    * <code>false</code> if not supplied. The requesting user must have admin
    * previledges to purge items.
    * 
    * @throws PSErrorsException if any child folder fails to be removed from the
    * parent.
    * @throws PSErrorException if any other error occurs.
    */
   public void removeFolderChildren(String parentPath, 
      List<IPSGuid> childIds, boolean purgeItems) 
      throws PSErrorsException, PSErrorException;
   
   /**
    * Moves the specified folder children from the source to the target folder.
    * 
    * @param sourceId the id of the source folder from which to move the
    * specified objects, not <code>null</code>. Must be the id of an existing
    * folder. This must be a folder guid obtained from one of the IPS*Ws
    * interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param targetId the id of the target folder to which to move the specified
    * objects, not <code>null</code>. Must be the id of an existing folder.
    * This must be a folder guid obtained from one of the IPS*Ws interfaces or
    * from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param childIds a list of object ids to be moved from the source to the
    * target folder. All source objects will be moved if not specified. Must be
    * ids of existing objects. All child folder names moved must be unique
    * withing the target folder. Each entry must be a content or folder guid
    * obtained from one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @throws PSErrorException if the specified source folder does not contain
    * any of the specified child ids. or an unexpected error occurs.
    */
   public void moveFolderChildren(IPSGuid sourceId, 
      IPSGuid targetId, List<IPSGuid> childIds) 
      throws PSErrorException;

   /**
    * Moves the specified folder children from the source to the target folder.
    * 
    * @param sourceId the id of the source folder from which to move the
    * specified objects, not <code>null</code>. Must be the id of an existing
    * folder. This must be a folder guid obtained from one of the IPS*Ws
    * interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param targetId the id of the target folder to which to move the specified
    * objects, not <code>null</code>. Must be the id of an existing folder.
    * This must be a folder guid obtained from one of the IPS*Ws interfaces or
    * from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @param childIds a list of object ids to be moved from the source to the
    * target folder. All source objects will be moved if not specified. Must be
    * ids of existing objects. All child folder names moved must be unique
    * withing the target folder. Each entry must be a content or folder guid
    * obtained from one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    *
    * @param checkFolderPermission <code>true</code> if enforce folder 
    * security/permission for the move child operation.
    * 
    * @throws PSErrorException if the specified source folder does not contain
    * any of the specified child ids. or an unexpected error occurs.
    */
   public void moveFolderChildren(IPSGuid sourceId, IPSGuid targetId,
         List<IPSGuid> childIds, boolean checkFolderPermission)
         throws PSErrorException;

   /**
    * Moves the specified folder children from the source to the target folder.
    * 
    * @param sourcePath the fully qualified source folder path from which to
    * move all supplied children, not <code>null</code> or empty, must be the
    * path of an existing folder.
    * @param targetPath the fully qualified target folder path to which to move
    * all supplied children, not <code>null</code> or empty, must be the path
    * of an existing folder.
    * 
    * @param childIds a list of object ids to be moved from the source to the
    * target folder. All source objects will be moved if not specified. Must be
    * ids of existing objects. All child folder names moved must be unique
    * withing the target folder. Each entry must be a content or folder guid
    * obtained from one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @throws PSErrorException if the specified source folder does not contain
    * any of the specified child ids; or one of the the specified folder paths
    * (source and target) is invalid, or an unexpected error occurs.
    */
   public void moveFolderChildren(String sourcePath, 
      String targetPath, List<IPSGuid> childIds) 
      throws PSErrorException;
   
   /**
    * Find the parent path for the specified object.
    * 
    * @param id the id of the object for which to find the parents, not
    * <code>null</code>. Must be the id of an existing object. This must be a
    * content or folder guid obtained from one of the IPS*Ws interfaces or from
    * the {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @return a list with complete folder paths such as
    * <code>//Sites/EnterpriseInvestments/Images</code> for the specified
    * object, never <code>null</code>, may be empty.
    * 
    * @throws PSErrorException for any unexpected error.
    */
   public String[] findFolderPaths(IPSGuid id) throws PSErrorException;

    public String[] findFolderPaths(IPSGuid id, String relationshipTypeName) throws PSErrorException;
   
   /**
    * Find the path for the specified item. This is similar with  
    * {@link #findFolderPaths(IPSGuid)}, except the returned path includes the 
    * item's name, but {@link #findFolderPaths(IPSGuid)} does not. 
    *
    * @param id the ID of the item (or folder) for which to find the paths,
    * <code>null</code>. Must be the id of an existing object. This must be a
    * content or folder guid obtained from one of the IPS*Ws interfaces or from
    * the {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @return an array of paths, may be empty.
    * 
    * @throws PSErrorException If path lookup fails for any reason.
    */
   public String[] findItemPaths(IPSGuid id) throws PSErrorException;
   
   /**
    * Find all ids for the specified path. For an input path of 
    * <code>//Sites/EnterpriseInvestments/InvestmentAdvice/Retirement</code> 
    * you would get back a list with 4 ids. The first id identifies the 
    * <code>Sites</code> folder, the next id the 
    * <code>EnterpriseInvestments</code> folder and so on.
    * 
    * @param path a fully qualified path for which to find the ids, not 
    *    <code>null</code> or empty. The path must identify an existing object.
    *    
    * @return a list with all path ids ordered top down, never 
    *    <code>null</code> or empty.
    * @throws PSErrorException for any unexpected error.
    */
   public List<IPSGuid> findPathIds(String path) 
      throws PSErrorException;

   /**
    * Gets the guid of item or folder for the specified path.
    * 
    * @param path a fully qualified path(case sensitive) for which to find the
    *           id, not <code>null</code> or empty.
    * 
    * @return IPSGuid of the item or folder corresponding to the supplied path
    *         or <code>null</code> if the path does not correspond to any item 
    *         or folder. The guid will have -1 for revision.
    * 
    * @throws PSErrorException for any unexpected error.
    */
   public IPSGuid getIdByPath(String path) 
      throws PSErrorException;

   /**
    * Gets the guid of item or folder for the specified path.
    *
    * @param path a fully qualified path(case sensitive) for which to find the
    *           id, not <code>null</code> or empty.
    *
    * @return IPSGuid of the item or folder corresponding to the supplied path
    *         or <code>null</code> if the path does not correspond to any item
    *         or folder. The guid will have -1 for revision.
    *
    * @throws PSErrorException for any unexpected error.
    */
   public IPSGuid getIdByPath(String path, String relationshipTypeName)
           throws PSErrorException;

   /**
    * Does a child item exist in a given folder.
    * 
    * @param folderPath the path of the folder in question, never blank.
    * @param name the name of the child item in question, never blank.
    * 
    * @return <code>true</code> if the there is a child with the specified name
    * exist in the specified folder; otherwise return <code>false</code>
    */
   public boolean isChildExistInFolder(String folderPath, String name);
   
   /**
    * Does a child item exist in a given folder.
    * 
    * @param folderId the ID of the folder in question, never blank.
    * @param name the name of the child item in question, never blank.
    * 
    * @return <code>true</code> if the there is a child with the specified name
    * exist in the specified folder; otherwise return <code>false</code>
    */
   public boolean isChildExistInFolder(IPSGuid folderId, String name);
 
   /**
    * Loads the specified active relationships.
    *  
    * @param dependent the dependent ID of the relationships, not <code>null</code>.
    * @param slotid the slot ID of the relations, not <code>null</code>.
    * 
    * @return the specified relationships, never <code>null</code>, but may be empty.
    */
   public List<PSAaRelationship> loadDependentSlotContentRelationships(PSLocator dependent, IPSGuid slotid);

   public List<PSFolder>  getFoldersByProperty(String property)  throws PSErrorException;
}
