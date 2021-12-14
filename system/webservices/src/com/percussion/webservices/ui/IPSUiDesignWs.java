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
package com.percussion.webservices.ui;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.ui.PSUiException;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSLockErrorException;
import com.percussion.webservices.aop.security.IPSWsMethod;
import com.percussion.webservices.aop.security.IPSWsParameter;
import com.percussion.webservices.ui.data.ActionType;

import java.util.List;

/**
 * This interface defines all ui design related webservices.
 */
public interface IPSUiDesignWs
{
   /**
    * Create new action definitions for the supplied parameters. The returned 
    * actions are not persisted to the repository.
    * 
    * @param names the names for the new menu actions, not <code>null</code> or 
    *    empty. The names must be unique across all defined actions in the 
    *    system, names are compared case-insensitive and cannot contain spaces.
    * @param types the action menu types, not <code>null</code> or empty.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>. 
    * 
    * @return the new created action menus for the supplied parameters. The 
    *    user must call {@link #saveActions(List, boolean, String, String)} 
    *    for the returned objects to persist it to the repository.
    * 
    * @throws PSLockErrorException if failed to create a lock for a new action.
    * @throws PSErrorException if failed to catalog existing actions.
    */
   public List<PSAction> createActions(List<String> names, 
         List<ActionType> types, String session, String user) 
         throws PSLockErrorException, PSErrorException;
   
   /**
    * Find all menu action summaries for the specified parameters.
    * 
    * @param name the action name for which to find all summaries, may be 
    *    <code>null</code> or empty, asterisk wildcards are accepted. All action 
    *    summaries are returned if not supplied or empty.
    * @param label the action label for which to find all summaries, may be 
    *    <code>null</code> or empty, asterisk wildcards are accepted. All action 
    *    summaries are returned if not supplied or empty.
    * @param types a list of requested types. It may be <code>null</code> or 
    *    empty if not filtered by type.
    *     
    * @return a list with all found object summaries of type 
    *    <code>PSAction</code> for the specified parameters, never 
    *    <code>null</code>, may be empty, ascending alpha ordered by name.
    *    
    * @throws PSErrorException if an error occurs during lookup operation.
    */
   public List<IPSCatalogSummary> findActions(String name, String label,
         List<ActionType> types) throws PSErrorException;
   
   /**
    * Loads all actions for the supplied ids in the requested mode.
    * 
    * @param ids a list of action ids to be loaded, not <code>null</code> or 
    *    empty, must be ids of existing actions.
    * @param lock <code>true</code> to lock the found results for edit, 
    *    <code>false</code> to return them read-only. Defaults to 
    *    <code>false</code> if not supplied. 
    * @param overrideLock <code>true</code> to allow the requesting user to
    *    override existing locks he owns in a different session, 
    *    <code>false</code> otherwise.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>. 
    * @return a list with all loaded actions in the requested mode in the 
    *    same order as requested, never <code>null</code> or empty.
    * @throws PSErrorResultsException if any of the requested objects could not
    *    be loaded or locked.
    */
   public List<PSAction> loadActions(List<IPSGuid> ids, 
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Save all supplied actions. New actions will be inserted, existing 
    * actions updated.
    * 
    * @param actions a list with all actions to be saved, not <code>null</code> 
    *    or empty.
    * @param release <code>true</code> to release all object locks after the 
    *    save, <code>false</code> to keep the locks. All locks will be
    *    released, no matter whether the operation was successful or not.
    * @param session the rhythmyx session for which to release the saved
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to release the saved objects, not 
    *    <code>null</code> or empty. 
    * @throws PSErrorsException if any of the supplied objects could not
    *    be saved.
    */
   public void saveActions(List<PSAction> actions, boolean release, 
      String session, String user) throws PSErrorsException;
   
   /**
    * Delete the actions for all supplied ids. Deletes cannot be reverted.
    * Only objects that are unlocked or locked by the requesting
    * user and session can be deleted, for all other cases an error will be 
    * returned.
    * <p>
    * All locks for successfully deleted objects will be released, locks which
    * exist for objects that failed to be deleted remain untouched.
    * 
    * @param ids a list of ids for each action to be deleted from the 
    *    repository, not <code>null</code> or empty. We ignore cases where 
    *    the object for a supplied id does not exist.
    * @param ignoreDependencies specifies whether or not the dependency check 
    *    prior to the delete of an object should be ignored, defaults to 
    *    <code>false</code> if not supplied. If dependency checks are enabled, 
    *    only objects without depenencies will be deleted, for all others an 
    *    error is returned so that the client can deal with it appropriately.
    * @param session the rhythmyx session, not <code>null</code> or empty. 
    * @param user the rhythmyx user, not <code>null</code> or empty. 
    * @throws PSErrorsException if any of the supplied objects could not
    *    be deleted.
    */
   public void deleteActions(List<IPSGuid> ids, boolean ignoreDependencies,
         String session, String user) throws PSErrorsException;

   /**
    * Create new display formats for the supplied parameters and default 
    * values. The returned objects are not persisted to the repository.
    * 
    * @param names the names for the new display formats, not <code>null</code> 
    *    or empty. The names must be unique across all defined display formats 
    *    in the system, names are compared case-insensitive and cannot contain 
    *    spaces.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>.
    *     
    * @return the new display formats for the supplied parameters, never 
    *    <code>null</code> or empty. The user must call 
    *    {@link #saveDisplayFormats(List, boolean, String, String)} to 
    *    persist the returned objects to the repository.
    * 
    * @throws PSLockErrorException if failed to create a lock for a new 
    *    display format.
    * @throws PSErrorException if failed to catalog existing display formats.
    */
   public List<PSDisplayFormat> createDisplayFormats(List<String> names,
         String session, String user) 
         throws PSLockErrorException, PSErrorException;
   
   /**
    * Find all display format summaries for the specified parameters.
    * 
    * @param name the display format name for which to find the summaries, may 
    *    be <code>null</code> or empty, asterisk wildcards are accepted. If not 
    *    supplied or empty all display format summaries will be returned.
    * @param label the display format label for which to find the summaries, may 
    *    be <code>null</code> or empty, asterisk wildcards are accepted. If not 
    *    supplied or empty all display format summaries will be returned.
    *    
    * @return a list with all object summaries of type 
    *    <code>PSDisplayFormat</code> found for the specified parameters, 
    *    never <code>null</code>, may be empty, ascending alpha ordered by name.
    *    
    * @throws PSErrorException if an error occurs during lookup operation.
    */
   public List<IPSCatalogSummary> findDisplayFormats(String name, String label)
         throws PSErrorException;
   
   /**
    * Finds the display format with the given ID. It returns a cached object, which
    * should not be modified in anyway by the caller.
    *    
    * @param id the ID of the display format in question, never <code>null</code>.
    * 
    * @return the display format. It may be <code>null</code> if the display format does not exist
    */
   public PSDisplayFormat findDisplayFormat(IPSGuid id);
   
   /**
    * Finds the display format with the given name. It returns a cached object, which
    * should not be modified in anyway by the caller.
    *    
    * @param name the name of the display format in question, never <code>null</code>.
    * 
    * @return the display format. It may be <code>null</code> if the display format does not exist
    */
   public PSDisplayFormat findDisplayFormat(String name);
   
   /**
    * Loads all display formats for the supplied ids in the requested mode.
    * 
    * @param ids a list of display format ids to be loaded, not 
    *    <code>null</code> or empty, must be ids of existing display formats.
    * @param lock <code>true</code> to lock the found results for edit, 
    *    <code>false</code> to return them read-only. Defaults to 
    *    <code>false</code> if not supplied. 
    * @param overrideLock <code>true</code> to allow the requesting user to
    *    override existing locks he owns in a different session, 
    *    <code>false</code> otherwise.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>. 
    * @return a list with all loaded display formats in the requested mode in 
    *    the same order as requested, never <code>null</code> or empty.
    * @throws PSErrorResultsException if any of the requested objects could not
    *    be loaded or locked.
    */
   public List<PSDisplayFormat> loadDisplayFormats(List<IPSGuid> ids, 
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Save all supplied display formats. New display formats will be inserted, 
    * existing display formats will be updated.
    * 
    * @param displayFormats a list of display formats to be saved, not 
    *    <code>null</code> or empty.
    * @param release <code>true</code> to release all object locks after the 
    *    save, <code>false</code> to keep the locks. All locks will be
    *    released, no matter whether the operation was successful or not.
    * @param session the rhythmyx session for which to release the saved
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to release the saved objects, not 
    *    <code>null</code> or empty. 
    * @throws PSErrorsException if any of the supplied objects could not
    *    be saved.
    */
   public void saveDisplayFormats(List<PSDisplayFormat> displayFormats, 
      boolean release, String session, String user) throws PSErrorsException;
   
   /**
    * Delete all display formats for the supplied ids. Deletes cannot be 
    * reverted. Only objects that are unlocked or locked by the requesting
    * user and session can be deleted, for all other cases an error will be 
    * returned.
    * <p>
    * All locks for successfully deleted objects will be released, locks which
    * exist for objects that failed to be deleted remain untouched.
    * 
    * @param ids a list of display format ids to be deleted from the repository, 
    *    not <code>null</code> or empty. We ignore cases where the object for 
    *    a supplied id does not exist.
    * @param ignoreDependencies specifies whether or not the dependency check 
    *    prior to the delete of an object should be ignored, defaults to 
    *    <code>false</code> if not supplied. If dependency checks are enabled, 
    *    only objects without depenencies will be deleted, for all others an 
    *    error is returned so that the client can deal with it appropriately.
    * @param session the rhythmyx session, not <code>null</code> or empty. 
    * @param user the rhythmyx user, not <code>null</code> or empty.
    * @throws PSErrorsException if any object failed to be deleted. 
    */
   public void deleteDisplayFormats(List<IPSGuid> ids,
         boolean ignoreDependencies, String session, String user)
         throws PSErrorsException;
   
   /**
    * Create new search definitions for the supplied parameters, defaults 
    * will be used for all other fields. The returned objects are not persisted 
    * to the repository.
    * 
    * @param names the names for the new searches, not <code>null</code> or 
    *    empty. The names must be unique across all defined searches in the 
    *    system, names are compared case-insensitive and cannot contain spaces.
    * @param types the search types, not <code>null</code> or empty.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>.
    *     
    * @return the new created search definitions, never <code>null</code>. The 
    *    user must call {@link #saveSearches(List, boolean, String, String)} 
    *    to persist the returned objects to the repository.
    * 
    * @throws PSLockErrorException if failed to create a lock for a new search.
    * @throws PSErrorException if failed to catalog existing search objects.
    */
   public List<PSSearch> createSearches(List<String> names, List<String> types,
         String session, String user)  
         throws PSLockErrorException, PSErrorException;
   
   /**
    * Find all search definition summaries for the specified parameters.
    * 
    * @param name the search name for which to find the summaries, may be 
    *    <code>null</code> or empty, asterisk wildcards are accepted. All search 
    *    summaries will be returned if not supplied or empty.
    * @param label the search label for which to find the summaries, may be 
    *    <code>null</code> or empty, asterisk wildcards are accepted. All search 
    *    summaries will be returned if not supplied or empty.
    *    
    * @return a list with all object summaries of type <code>PSSearch</code> 
    *    found for the specified parameters, never <code>null</code>, may be 
    *    empty, ascending alpha ordered by name.
    *    
    * @throws PSErrorException if an error occurs during lookup operation.
    */
   public List<IPSCatalogSummary> findSearches(String name, String label)
      throws PSErrorException;
   
   /**
    * Loads all searches for the supplied ids in the requested mode.
    * 
    * @param ids a list of search ids to be loaded, not <code>null</code> or 
    *    empty, must be ids of existing searches.
    * @param lock <code>true</code> to lock the found results for edit, 
    *    <code>false</code> to return them read-only. Defaults to 
    *    <code>false</code> if not supplied. 
    * @param overrideLock <code>true</code> to allow the requesting user to
    *    override existing locks he owns in a different session, 
    *    <code>false</code> otherwise.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>. 
    * @return a list with all loaded searche definitions in the requested mode 
    *    in the same order as requested, never <code>null</code>, may be empty.
    * @throws PSErrorResultsException if any of the requested objects could not
    *    be loaded or locked.
    */
   public List<PSSearch> loadSearches(List<IPSGuid> ids, 
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Save all supplied search definitions to the repository.
    * 
    * @param searches a list with all search definitions to be saved to the 
    *    server, not <code>null</code> or empty. New search definitions will 
    *    be inserted, existing definitions will be updated.
    * @param release <code>true</code> to release all object locks after the 
    *    save, <code>false</code> to keep the locks. All locks will be
    *    released, no matter whether the operation was successful or not.
    * @param session the rhythmyx session for which to release the saved
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to release the saved objects, not 
    *    <code>null</code> or empty. 
    * @throws PSErrorsException if any of the supplied objects could not
    *    be saved.
    */
   public void saveSearches(List<PSSearch> searches, boolean release, 
      String session, String user) throws PSErrorsException;
   
   /**
    * Delete all search definitions for the supplied ids. Deletes cannot be 
    * reverted. Only objects that are unlocked or locked by the requesting
    * user and session can be deleted, for all other cases an error will be 
    * returned.
    * <p>
    * All locks for successfully deleted objects will be released, locks which
    * exist for objects that failed to be deleted remain untouched.
    * 
    * @param ids a list of ids for each search definition to be deleted from 
    *    the repository, not <code>null</code> or empty. We ignore cases where 
    *    the object for a supplied id does not exist.
    * @param ignoreDependencies specifies whether or not the dependency check 
    *    prior to the delete of an object should be ignored, defaults to 
    *    <code>false</code> if not supplied. If dependency checks are enabled, 
    *    only objects without depenencies will be deleted, for all others an 
    *    error is returned so that the client can deal with it appropriately.
    * @param session the rhythmyx session, not <code>null</code> or empty. 
    * @param user the rhythmyx user, not <code>null</code> or empty. 
    * @throws PSErrorsException if any of the supplied objects could not
    *    be deleted.
    */
   public void deleteSearches(List<IPSGuid> ids, boolean ignoreDependencies,
         String session, String user) throws PSErrorsException;

   /**
    * Create new view definitions for the supplied parameters. The returend 
    * objects are not persisted to the repository.
    * 
    * @param names the names for the new views, not <code>null</code> or empty. 
    *    The names must be unique across all defined views in the system, 
    *    names are compared case-insensitive and cannot contain spaces.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>.
    *     
    * @return the new created view definitions. The user must call 
    * {@link #saveViews(List, boolean, String, String)} for the returned 
    * objects to persist it to the repository.
    * 
    * @throws PSLockErrorException if failed to create a lock for a new view.
    * @throws PSErrorException if failed to catalog existing view objects.
    */
   public List<PSSearch> createViews(List<String> names, String session, 
         String user) throws PSLockErrorException, PSErrorException;
   
   /**
    * Find all view definition summaries for the supplied parameters.
    * 
    * @param name the view name for which to find the summaries, may be 
    *    <code>null</code> or empty, asterisk wildcards are accepted. All 
    *    summaries will be returned if not supplied or empty.
    * @param label the view label for which to find the summaries, may be 
    *    <code>null</code> or empty, asterisk wildcards are accepted. All 
    *    summaries will be returned if not supplied or empty.
    * @return a list with all found object summaries of type 
    *    <code>PSViewDef</code> for the supplied name, never <code>null</code>, 
    *    may be empty, ascending alpha ordered by name.
    * @throws PSErrorException if error occurs while lookup the views.
    */
   public List<IPSCatalogSummary> findViews(String name, String label)
         throws PSErrorException;
   
   /**
    * Loads all view definitions for the supplied ids in the requested mode.
    * 
    * @param ids a list of view definition ids to be loaded, not 
    *    <code>null</code> or empty, must be ids of existing view definitions.
    * @param lock <code>true</code> to lock the found results for edit, 
    *    <code>false</code> to return them read-only. Defaults to 
    *    <code>false</code> if not supplied. 
    * @param overrideLock <code>true</code> to allow the requesting user to
    *    override existing locks he owns in a different session, 
    *    <code>false</code> otherwise.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>. 
    * @return a list with all loaded view definitions in the requested mode in 
    *    the same order as requested, never <code>null</code> or empty.
    * @throws PSErrorResultsException if any of the requested objects could not
    *    be loaded or locked.
    */
   public List<PSSearch> loadViews(List<IPSGuid> ids, 
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Save all supplied view definitions to the repository. New view 
    * definitions will be inserted, existing view definitions will be updated.
    * 
    * @param views a list with all view definitions to be saved to the 
    *    repository, not <code>null</code> or empty.
    * @param release <code>true</code> to release all object locks after the 
    *    save, <code>false</code> to keep the locks. All locks will be
    *    released, no matter whether the operation was successful or not.
    * @param session the rhythmyx session for which to release the saved
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to release the saved objects, not 
    *    <code>null</code> or empty. 
    * @throws PSErrorsException if any of the supplied objects could not
    *    be saved.
    */
   public void saveViews(List<PSSearch> views, boolean release, 
      String session, String user) throws PSErrorsException;
   
   /**
    * Delete all view desinitions for the supplied ids. Deletes cannot be 
    * reverted. Only objects that are unlocked or locked by the requesting
    * user and session can be deleted, for all other cases an error will be 
    * returned.
    * <p>
    * All locks for successfully deleted objects will be released, locks which
    * exist for objects that failed to be deleted remain untouched.
    * 
    * @param ids a list of ids for each view definition to be deleted from the 
    *    repository, not <code>null</code> or empty. We ignore cases where the 
    *    object for a supplied id does not exist.
    * @param ignoreDependencies specifies whether or not the dependency check 
    *    prior to the delete of an object should be ignored, defaults to 
    *    <code>false</code> if not supplied. If dependency checks are enabled, 
    *    only objects without depenencies will be deleted, for all others an 
    *    error is returned so that the client can deal with it appropriately.
    * @param session the rhythmyx session, not <code>null</code> or empty. 
    * @param user the rhythmyx user, not <code>null</code> or empty. 
    * @throws PSErrorsException if any of the supplied objects could not
    *    be deleted.
    */
   public void deleteViews(List<IPSGuid> ids, boolean ignoreDependencies,
         String session, String user) throws PSErrorsException;
   
   /**
    * Create new hierarchy nodes for the supplied parameters. The returned 
    * objects are not persisted to the repository.
    * 
    * @param names the names for the new hierarch nodes, not <code>null</code> 
    *    or empty. The names must be unique across all hierarchy nodes in the 
    *    same parent, names are compared case-insensitive.
    * @param parents the ids of the parent nodes to which the new nodes will be 
    *    attached. A new root node will be created if <code>null</code> is 
    *    provided.
    * @param types the types of the hierarchy nodes to create, not 
    *    <code>null</code> or empty.
    * @param session the rhythmyx session for which to lock the created
    *    object, not <code>null</code> or empty. 
    * @param user the user for which to lock the created object, not 
    *    <code>null</code> or empty. 
    * @return the new created hierarchy nodes. The user must call 
    *    {@link #saveHierarchyNodes(List, boolean, String, String)} for the 
    *    returned objects to persist it to the repository.
    * @throws PSUiException for any error creating the requested hierarchy 
    *    node.
    */
   public List<PSHierarchyNode> createHierarchyNodes(List<String> names, 
      List<IPSGuid> parents, List<PSHierarchyNode.NodeType> types, 
      String session, String user) throws PSUiException;
   
   /**
    * Finds all workbench hierarchy node summaries for the supplied search 
    * parameters.
    * 
    * @param path the path of the hierarchy node to find, may be 
    *    <code>null</code> or empty to get all node summaries, asterisk 
    *    wildcards are accepted. To get a single node summary you define 
    *    something like <code>//sites/us</code>. To get the summaries for an 
    *    entire tree you define something like <code>//sites/us/*</code>, 
    *    which will return the summaries for node <code>//sites/us</code> 
    *    including all children recursive.
    * @param type the node type by which to filter the returned results,
    *    may be <code>null</code> to ignore this filter.
    * @return a list with all object summaries of type 
    *    <code>PSHierarchyNode</code> found for the supplied search fields, 
    *    never <code>null</code>, may be empty.
    */
   @IPSWsMethod(ignore=true)
   public List<IPSCatalogSummary> findHierarchyNodes(String path, 
      PSHierarchyNode.NodeType type);
   
   /**
    * Loads all hierarchy nodes for the supplied ids in the requested mode.
    * 
    * @param ids a list of hierarchy node ids to be loaded, not 
    *    <code>null</code> or empty, must be ids of existing hierarchy nodes.
    * @param lock <code>true</code> to lock the found results for edit, 
    *    <code>false</code> to return them read-only. Defaults to 
    *    <code>false</code> if not supplied.
    * @param overrideLock <code>true</code> to allow the requesting user to
    *    override existing locks he owns in a different session, 
    *    <code>false</code> otherwise.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>. 
    * @return a list with all loaded hierarchy nodes in the requested mode in 
    *    the same order as requested, never <code>null</code> or empty.
    * @throws PSErrorResultsException if any of the requested objects could not
    *    be loaded or locked.
    */
   @IPSWsMethod(ignore=true)
   public List<PSHierarchyNode> loadHierachyNodes(List<IPSGuid> ids, 
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Save all supplied hierarchy nodes to the repository. New hierarchy 
    * nodes will be inserted, existing hierarchy nodes will be updated.
    * 
    * @param nodes a list with all hierarchy nodes to be saved to the 
    *    repository, not <code>null</code> or empty.
    * @param release <code>true</code> to release all object locks after the 
    *    save, <code>false</code> to keep the locks. All locks will be
    *    released, no matter whether the operation was successful or not.
    * @param session the rhythmyx session for which to release the saved
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to release the saved objects, not 
    *    <code>null</code> or empty. 
    * @throws PSErrorsException if any of the supplied objects could not
    *    be saved.
    */
   @IPSWsMethod(ignore=true)
   public void saveHierarchyNodes(List<PSHierarchyNode> nodes, boolean release, 
      String session, String user) throws PSErrorsException;
   
   /**
    * Delete all hierarchy nodes for the supplied ids. Deletes cannot be 
    * reverted. Only objects that are unlocked or locked by the requesting
    * user and session can be deleted, for all other cases an error will be 
    * returned.
    * <p>
    * All locks for successfully deleted objects will be released, locks which
    * exist for objects that failed to be deleted remain untouched.
    * 
    * @param ids a list of ids for each hierarchy node to be deleted from the 
    *    repository, not <code>null</code> or empty. We ignore cases where the 
    *    object for a supplied id does not exist.
    * @param ignoreDependencies specifies whether or not the dependency check 
    *    prior to the delete of an object should be ignored, defaults to 
    *    <code>false</code> if not supplied. If dependency checks are enabled, 
    *    only objects without depenencies will be deleted, for all others an 
    *    error is returned so that the client can deal with it appropriately.
    * @param session the rhythmyx session for which to delete the specified
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to release delete the specified objects, 
    *    not <code>null</code> or empty. 
    * @throws PSErrorsException if any of the supplied objects could not
    *    be deleted.
    */
   @IPSWsMethod(ignore=true)
   public void deleteHierarchyNodes(List<IPSGuid> ids, 
      boolean ignoreDependencies, String session, String user) 
      throws PSErrorsException;
   
   /**
    * Get all children for the supplied hierarchy node id.
    * 
    * @param id the hierarchy node id for which to get all children, may be 
    *    <code>null</code> to get all root nodes.
    * @return a list of of hierarchy node ids with all requested roots or 
    *    children, never <code>null</code>, may be empty.
    */
   public List<IPSGuid> getChildren(IPSGuid id);
   
   /**
    * Move all children from the specified source hierarchy node to the target 
    * hierarchy node.
    * 
    * @param source the hierarchy node source id from which to move the 
    *    specified children, not <code>null</code>.
    * @param target the hierarchy node target id to which to move the specified 
    *    children, not <code>null</code>.
    * @param ids a list with hierarchy node ids to be moved, not 
    *    <code>null</code> or empty.
    */
   public void moveChildren(IPSGuid source, IPSGuid target, List<IPSGuid> ids);
   
   /**
    * Remove all specified hierarchy nodes from the identified parent.
    * 
    * @param parent the hierarchy node parent id from which to remove the 
    *    specified children, not <code>null</code> or empty.
    * @param ids a list with hierarchy node ids which to remove from the 
    *    specified parent, not <code>null</code> or empty.
    */
   public void removeChildren(IPSGuid parent, List<IPSGuid> ids);
   
   /**
    * Get the ids for all supplied hierarchy node paths.
    * 
    * @param paths a list with fully qualified hierarchy node path for which 
    *    to get the ids, not <code>null</code> or empty. Must be paths to 
    *    existing hierarchy nodes.
    * @return a list of lists with all hierarchy node ids found for the 
    *    supplied paths in the same order as requested, never 
    *    <code>null</code> or empty.
    * @throws PSErrorException for any error converting the list of
    *    path into a list of list of ids.
    */
   public List<List<IPSGuid>> pathsToIds(List<String> paths)
      throws PSErrorException;
   
   /**
    * Get the paths for all supplied hierarchy node ids.
    * 
    * @param ids a list with hierarchy node ids for which to get the paths, 
    *    not <code>null</code> or empty. Must be ids of existing hierarchy 
    *    nodes.
    * @return a list with fully qualified hierarchy node paths found for the 
    *    supplied ids in the same order as requested, never <code>null</code> 
    *    or empty.
    * @throws PSErrorResultsException for any error converting the list of
    *    ids into a list of paths.
    */
   public List<String> idsToPaths(List<IPSGuid> ids) 
      throws PSErrorResultsException;
   
   /**
    * Finds all the searches and returns them, the searches are from cache and
    * should be used for readonly purposes. Use
    * {@link #loadSearches(List, boolean, boolean, String, String)} to get the
    * searches for read and write purposes.
    * <p>
    * Note, the returned objects is not designed to be filtered by the AOP
    * security (see {@link PSSecurityStrategy}), which may modify the filtered
    * objects. Added annotation to skip the AOP security process.
    * 
    * @return All the search objects from cache, never <code>null</code> may
    *         be empty.
    * @throws PSErrorResultsException
    */
   @IPSWsMethod(ignore=true)
   public List<PSSearch> findAllSearches() throws PSErrorResultsException,
         PSErrorException;
   
   
   /**
    * Get the path for supplied guid.
    * 
    * @param guid , not <code>null</code> or empty. Must be valid guid 
    *    of content types or templates.
    *    
    * @return a String which is the path of a content item or template
    *    not <code>null</code>.
    *    
    * @throws PSErrorResultsException for any error building path 
    *    for given guid.
    */
   public String objectIdToPath(IPSGuid guid) throws PSErrorsException;
   
   
}

