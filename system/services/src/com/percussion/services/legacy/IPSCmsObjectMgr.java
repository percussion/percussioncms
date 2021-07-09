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
package com.percussion.services.legacy;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cx.PSUiMode;
import com.percussion.design.objectstore.PSConfig;
import com.percussion.design.objectstore.PSRole;
import com.percussion.i18n.PSLocale;
import com.percussion.server.PSPersistentProperty;
import com.percussion.server.PSPersistentPropertyMeta;
import com.percussion.server.PSUserSession;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.data.IPSIdentifiableItem;
import com.percussion.services.menus.PSActionMenu;
import com.percussion.services.menus.PSUiContext;
import com.percussion.services.menus.PSUIMode;
import com.percussion.services.relationship.data.PSRelationshipConfigName;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.workflow.IPSStatesContext;
import com.percussion.workflow.IPSWorkflowAppsContext;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This manager allows access to the legacy schema via various object mappings.
 * This is meant to replace the sys_psxCms and xml dom for access to most system
 * objects.
 * 
 * @author dougrand
 */
public interface IPSCmsObjectMgr extends IPSCmsContentSummaries
{
   /**
    * Create a new concrete locale object.  The new object will have an assigned
    * guid, but will not yet be persisted.
    * 
    * @param languageString The language string that identifies this locale. May
    * not be <code>null</code> or empty.
    * @param displayName The display name of the locale, may not be 
    * <code>null</code> or empty.
    * 
    * @return The locale, never <code>null</code>.  The status will be initally
    * set to inactive.
    */
   PSLocale createLocale(String languageString, String displayName);
   /**
    * Find a locale object by the locale id. The resulting object can be saved
    * by calling the update routine.
    * 
    * @param id the id
    * @return <code>null</code> if the locale is not found
    */
   PSLocale loadLocale(int id);

   /**
    * Find a locale object by the locale name. The resulting object can be saved
    * by calling the update routine.
    * 
    * @param lang the language name to match, must never be <code>null</code>
    *           or empty
    * @return <code>null</code> if the locale is not found
    */
   PSLocale findLocaleByLanguageString(String lang);

   /**
    * Performs update operation to the given content id list. It sets the
    * {@link PSComponentSummary#getContentLastModifiedDate()} property to the 
    * current date & time.
    * 
    * @param ids the content IDs, never <code>null</code>, may be empty.
    */
   void touchItems(Collection<Integer> ids);
   
   /**
    * Performs update operation to the given content id list. It sets the
    * {@link PSComponentSummary#getContentPostDate()} property to the 
    * current date & time if it is not already set.
    * 
    * @param ids the content IDs, never <code>null</code>, may be empty.
    */
   void setPostDate(Collection<Integer> ids);
   /**
    * Find the first publishDate for teh given Item
    *
    * @param contentId the id of the object for which postdate needs to be found, must never be <code>null</code>
    *           or empty
    * @return <code>null</code> if the publish date is not found
    */
   Date getFirstPublishDate(Integer contentId);
   /**
    * Performs update operation to the given content id list.  It clears the
    * {@link PSComponentSummary#getContentStartDate()}.
    * 
    * @param ids the content IDs, never <code>null</code>, may be empty.
    */
   void clearStartDate(Collection<Integer> ids);
   
   /**
    * Performs update operation to the given content id list.  It clears the
    * {@link PSComponentSummary#getContentExpiryDate()}.
    * 
    * @param ids the content IDs, never <code>null</code>, may be empty.
    */
   void clearExpiryDate(Collection<Integer> ids);
   
   /**
    * Find a locale object by the locale status. The resulting object can be
    * saved by calling the update routine.
    * 
    * @param status a status value
    * @return a collection, which may be empty if no locales match the status,
    *    never <code>null</code>.
    */
   List<PSLocale> findLocaleByStatus(int status);
   
   /**
    * Find locales objects by the locale name and/or label.  
    * 
    * @param lang The name or language string, may be <code>null</code> or empty
    * to not filter by name.
    * @param label The label, may be <code>null</code> or empty to not filter
    * by label, sql wildcards are supported.
    * 
    * @return The resulting list, may be empty if no matches are found, never
    * <code>null</code>.
    */
   List<PSLocale> findLocales(String lang, String label);

   /**
    * Find all locale objects. The resulting object can be saved by calling the
    * update routine.
    * 
    * @return a collection, which may be empty if no locales match the status,
    *    never <code>null</code>.
    */
   List<PSLocale> findAllLocales();

   /**
    * If this is a new locale, then save it in the database, otherwise update
    * the existing instance.
    * 
    * @param locale the locale object, must never be <code>null</code>, new
    *           objects detected by an id of <code>0</code>
    * @throws PSORMException if the data persistence layer encounters a problem
    * @throws PSMissingBeanConfigurationException
    */
   void saveLocale(PSLocale locale) throws PSORMException,
         PSMissingBeanConfigurationException;

   /**
    * Remove the given locale
    * 
    * @param locale the locale object, must never be <code>null</code>, must
    *           have a non-zero id
    * @throws PSORMException if the data persistence layer encounters a problem
    */
   void deleteLocale(PSLocale locale) throws PSORMException;

   /**
    * Find zero or more component summaries that are of the given content type
    * 
    * @param contentType the content type being searched for
    * @return zero or more matching component summaries, may be an empty list
    * 
    * @throws PSORMException if there is a problem in the data persistence layer
    */
   List<PSComponentSummary> findComponentSummariesByType(
         long contentType) throws PSORMException;

   /**
    * Find zero or more content ids for the given type
    * @param contentType the content type being searched for
    * @return zero or more matching content ids, may be an empty list
    * @throws PSORMException if there is a problem in the persistence layer
    */
   Collection<Integer> findContentIdsByType(long contentType) throws PSORMException;

   /**
    * Find zero or more content ids that are using the given workflow 
    * @param workflowid the workflow id being searched for
    * @return zero or more matching content ids, may be an empty list
    * @throws PSORMException if there is a problem in the persistence layer
    */
   Collection<Integer> findContentIdsByWorkflow(int workflowid) throws PSORMException;
   
   /**
    * Find zero or more content ids for the given workflow and state
    * @param workflowid the workflow id being searched for
    * @param stateid the state id being searched for
    * @return zero or more matching content ids, may be an empty list
    * @throws PSORMException if there is a problem in the persistence layer
    */
   Collection<Integer> findContentIdsByWorkflowStatus(int workflowid, int stateid) throws PSORMException;
   
   /**
    * Save the summaries
    * 
    * @param summaries one or more component summaries to be persisted to the
    *           database
    * @throws PSORMException if there is a problem in the data persistence layer
    */
   void saveComponentSummaries(List<PSComponentSummary> summaries)
         throws PSORMException;

   /**
    * Delete the summaries
    * 
    * @param summaries one or more component summaries to be delete from the
    *           database
    * @throws PSORMException if there is a problem in the data persistence layer
    */
   void deleteComponentSummaries(List<PSComponentSummary> summaries)
         throws PSORMException;

   /**
    * Evict the specific component summaries from the second-level cache
    * 
    * @param ids a list of one or more ids that reference component summary
    *           instances that should be removed from the second-level cache
    */
   void evictComponentSummaries(List<Integer> ids);

   /**
    * Load a single workflow app object
    * 
    * @param workflowAppId the workflow id to load
    * @return a workflow app instance, or <code>null</code> if the instance is
    *         not found
    */
   IPSWorkflowAppsContext loadWorkflowAppContext(int workflowAppId);

   /**
    * Load a single workflow state object, specified by a pairing of workflow
    * and state ids.
    * 
    * @param workflowappid the workflow app id to load
    * @param stateid the state id to load
    * @return a workflow state instance, or <code>null</code> if the instance
    *         is not found
    */
   IPSStatesContext loadWorkflowState(int workflowappid, int stateid);

   
   /**
    * Filter the given set of items by joining to the state information and
    * limiting by the states that have one of the passed flag values.
    * 
    * @param items a set of filter items that reference content items. Only the
    *           content id is used at this time. never <code>null</code>
    * @param flags a list of flags that should pass. Flags are single character
    *           values.
    * @return a list of passed items, may be empty but never <code>null</code>
    * @throws PSORMException if there is a problem in the data persistence layer
    */
   <T extends IPSIdentifiableItem> List<T> filterItemsByPublishableFlag(List<T> items,
         List<String> flags) throws PSORMException;

   /**
    * Evict the instance reference by id from the second level cache
    * 
    * @param clazz the class involved in persistence
    * @param id the serializable primary key that specifies the instance, may
    * be <code>null</code> to evict all instances of the class.
    * @throws PSORMException
    */
   void handleDataEviction(Class clazz, Serializable id) throws PSORMException;

   /**
    * Get all configurations.
    * 
    * @return a collection of configuration objects, which may be empty if there
    *         is no configurations in the repository.
    *         
    * @throws PSCmsException if error occurred during the lookup process.
    */
   Collection<PSConfig> findAllConfigs() throws PSCmsException;

   /**
    * Find the configuration by the specified name.
    * 
    * @param name the name of the configuration, never <code>null</code> or
    *           empty.
    * 
    * @return the loaded configuration. It may be <code>null</code> if cannot
    *         find the configuration by the specified name.
    *         
    * @throws PSCmsException if error occurred during the lookup process.
    */
   PSConfig findConfig(String name) throws PSCmsException;

   /**
    * Save the supplied configuration.
    * 
    * @param config the to be saved configuration, never <code>null</code>.
    * 
    * @throws PSCmsException if failed to save the config.
    */
   void saveConfig(PSConfig config) throws PSCmsException;

   /**
    * Flush the second level hibernate cache. Please use with caution, only
    * appropriate when there are external modifications to the database that
    * must occur.
    */
   void flushSecondLevelCache();

   /**
    * Get all relationship names
    * 
    * @return a list of <code>PSRelationshipConfigName</code> objects. It may
    *         be empty, but never <code>null</code>.
    */
   Collection<PSRelationshipConfigName> findAllRelationshipConfigNames();
   
   /**
    * Find all {@link PSRelationshipConfigName} objects with the specified
    * name. 
    *  
    * @param name the name of the looked up object, it may contain wildcard,
    *   never <code>null</code> or empty.
    * 
    * @return the found object, never <code>null</code>, may be empty.
    */
   List<PSRelationshipConfigName> findRelationshipConfigNames(String name);
   
   /**
    * Find all rhythmyx backend roles by name.
    * 
    * @param name the name or the backendd role to find, may be 
    *    <code>null</code> or empty to find all roles. Asterisk wildcards are
    *    supported.
    * @return a list with all roles found for the suppliedd name, never
    *    <code>null</code>, may be empty.
    */
   List<PSRole> findRolesByName(String name);
   
   /**
    * For the given content ids, find the corresponding public or current guid 
    * @param ids the input content ids, never <code>null</code> or empty
    * @return a list of guids in the same order as the incoming ids
    */
   List<IPSGuid> findPublicOrCurrentGuids(List<Integer> ids);
   
   /**
    * Find a CMS object by a specified object type.
    * 
    * @param objectType the specified object type.
    * 
    * @return the CMS object with the specified object type, never 
    *   <code>null</code>, may be empty.
    */
   PSCmsObject loadCmsObject(int objectType);
   
   /**
    * Find all CMS objects.
    * 
    * @return all CMS objects, never <code>null</code> or empty.
    */
   List<PSCmsObject> findAllCmsObjects();
    
   /**
    * Find all persistent property's meta objects.
    * 
    * @return the meta objects, never <code>null</code>, but may be
    *    empty.
    */
   List<PSPersistentPropertyMeta> findAllPersistentMeta();

    /**
     * Save a list of PersistentPropertyMeta
     * @param list
     * @return
     */
   List<PSPersistentPropertyMeta> saveAllPersistentMeta(List<PSPersistentPropertyMeta> list);

    /***
     * Delete a list of PersistentProperty Meta
     * @param list
     */
   void deleteAllPersistentMeta(List<PSPersistentPropertyMeta> list);

    /***
     * Save one PersistentPropertyMeta
     *
     * @param meta
     * @return
     */
   PSPersistentPropertyMeta savePersistentPropertyMeta(PSPersistentPropertyMeta meta);
   /**
    * Find the persistent property's meta objects by a specified (user) name.
    * The query is case sensitive if the underline database is case sensitive; 
    * otherwise, the query is case insensitive.
    * 
    * @param name the specified (user) name as the query criteria.
    *    It may not be <code>null</code> or empty.
    * 
    * @return the meta objects with the specified (user) name, never 
    *    <code>null</code>, but may be empty.
    */
   List<PSPersistentPropertyMeta> findPersistentMetaByName(String name);

   /**
    * Find all persistent properties.
    * 
    * @return all persistent properties, never <code>null</code>, but may be
    *    empty.
    */
   List<PSPersistentProperty> findAllPersistentProperties();
   
   /**
    * Saves the specified persistent property to the backend repository.
    * 
    * @param prop the to be saved property, it may not be <code>null</code>.
    */
   void savePersistentProperty(PSPersistentProperty prop);

   /**
    * Deletes the specified persistent property from the backend repository.
    * 
    * @param prop the to be deleted property, it may not be <code>null</code>.
    */
   void deletePersistentProperty(PSPersistentProperty prop);

   /**
    * Updates the specified persistent property from the backend repository.
    * 
    * @param prop the to be updated property, it may not be <code>null</code>.
    */
   void updatePersistentProperty(PSPersistentProperty prop);

   /**
    * Find the persistent properties by a specified user name. The query is
    * case sensitive if the underline database is case sensitive; otherwise,
    * the query is case insensitive.
    * 
    * @param userName the specified user name as the query criteria.
    *    It may not be <code>null</code> or empty.
    * 
    * @return the properties with the specified user name, never 
    *    <code>null</code>, but may be empty.
    */
   List<PSPersistentProperty> findPersistentPropertiesByName(String userName);
   
   /**
    * Get the distinct set of content type ids represented by the passed 
    * content ids.
    * 
    * @param contentIds a collection of content ids, never <code>null</code>,
    * the ids must be either instances of <code>Number</code> or 
    * <code>String</code>. 
    * @return a collection of content type ids, never <code>null</code> but
    * will be empty if the id set is empty.
    */
   Set<Long> findContentTypesForIds(Collection<? extends Object> contentIds);
   
   /**
    * Finds a list of items.
    * 
    * @param contentIds the IDs of the items, not <code>null</code> or empty.
    * @param comparator the comparator used to sort the returned list of items.
    * It may be <code>null</code> if the return list does not need to be sorted.
    * 
    * @return a list of items with the specified sort order, never <code>null</code>.
    * It may be empty if cannot find the specified items or the cache is disabled.
    */
   List<IPSItemEntry> findItemEntries(List<Integer> contentIds, Comparator<IPSItemEntry> comparator);
   
   /**
    * Finds the specified (cached) item.
    * @param contentId the ID of the item.
    * @return the (cached) item. It may be <code>null</code> if cannot find the item or the cache is disabled.
    */
   IPSItemEntry findItemEntry(int contentId);
   
   /**
    * Ensures all items in the specified folders are in the workflow with the supplied target workflowId.  For any that
    * are not, they are changed to the workflow and if not in one of the state specified by the validStateNames, 
    * they are moved to the first state in that list.  Any items that are checked out are skipped. Items are not
    * transitioned, their content status entry is simply updated with the new workflow and state id, and an entry
    * is inserted into the content status history.
    * 
    * Note: Current user must be a member of the workflow's admin role.
    * 
    * @param folderIds The list of folders to check, may not be <code>null</code>.
    * @param workflowId The target workflow to use, must reference an existing workflow.
    * @param validStateNames The names of states that should be valid across every workflow, may not be <code>null</code>.  
    * Any content in a state with that name will be mapped to a state with the same name in the target workflow.  It is an 
    * error if the state does not exist in the target workflow.  Items in other non-valid states will be moved to the state
    * matching the first state in the valid state list.
    * 
    * @throws PSCmsException If there is an error locating folder children
    * @throws PSORMException If there is an error querying or updating the repository
    */
   void changeWorfklowForItems(List<Integer> folderIds, int workflowId, List<String> validStateNames) throws PSCmsException, PSORMException;
   
   /**
    * Ensures the workflow of the supplied item is in the specified target workflow.  If item is already in that workflow,
    * the method is a noop.  If the item is not, it is moved to the specified workflow.
    * 
    * @param itemId The item to move, must exist, may not be checked out, and cannot be a folder.
    * @param workflowId The target workflow, must exist.
    * @param validStateNames The names of states that should be valid across every workflow, may not be <code>null</code>.  
    * Any content in a state with that name will be mapped to a state with the same name in the target workflow.  It is an 
    * error if the state does not exist in the target workflow.  Items in other non-valid states will be moved to the state
    * matching the first state in the valid state list.
    * 
    * @throws PSORMException If there is an error querying or updating the repository
    */
   public void changeWorkflowForItem(int itemId, int workflowId, List<String> validStateNames) throws PSORMException;
   
   /**
    * Counts the number of Items of a certain contentType under the specified
    * root folder that match the given workflow states. Example: this can be
    * used to count the number of live or pending pages (content type page),
    * under a site's root folder.
    * 
    * @param rootFolderPath the path of the root folder from which to start
    *           searching, may not be <code>null</code>.
    * @param stateNames A list of workflow state names of the items that need to
    *           be counted. may not be <code>null</code>.
    * @param contentTypeName The name of the content type that needs to be
    *           counted (eg. Pages, templates). May not be <code>null</code>.
    * 
    * @throws PSCmsException If there is an error getting the childer of
    *            siteRootFolder
    * @throws PSInvalidContentTypeException If the contentTypeName supplied is
    *            invalid
    * 
    */
   public Integer getItemCount(String rootFolderPath,
         List<String> stateNames, String contentTypeName) throws PSCmsException, PSInvalidContentTypeException;

   /**
    * Force checks in content using hibernate and not Old XML Handler functionality
    * This bypasses any security checks or state checks.  This is to
    * be used to force content checkin on expired sessions.
    * 
    * @param usersToDelete
    * @throws PSORMException
    */
   public void forceCheckinUsers(HashMap<String, PSUserSession> usersToDelete);
   
   public void setRevisionLocks(List<Integer> ids);
   
   public void setPublishDate(List<Integer> ids, Date date);

   public List<PSActionMenu> findActionMenus();

   public List<PSUiContext> findUiContexts();

   public List<PSUIMode> findUiModes();
   void updateSummaryDateFieldBatch(String fieldName, Date dateToSet, List<Integer> idBatch, boolean updateExisting);

   
   public List<PSActionMenu> findActionMenusByType(String type);
   
}
