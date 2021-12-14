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
package com.percussion.webservices.system;

import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.services.security.data.PSUserAccessLevel;
import com.percussion.services.system.data.PSDependency;
import com.percussion.services.system.data.PSMimeContentAdapter;
import com.percussion.services.system.data.PSSharedProperty;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSLockErrorException;
import com.percussion.webservices.aop.security.IPSWsMethod;
import com.percussion.webservices.aop.security.IPSWsParameter;
import com.percussion.webservices.aop.security.IPSWsPermission;
import com.percussion.webservices.aop.security.IPSWsStrategy;
import com.percussion.webservices.aop.security.strategy.custom.PSLoadAclSecurityStrategy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * This interface defines all system design related webservices.
 */
public interface IPSSystemDesignWs
{
   /**
    * Load all shared properties for the supplied shared property name in the 
    * requested mode.
    * 
    * @param names the names of the properties to load, may be 
    *    <code>null</code> or empty, asterisk wildcards are accepted. All 
    *    properties are loaded if <code>null</code> or empty.
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
    * @return an array with all found shared properties for the specified 
    *    parameters, never <code>null</code>, may be empty.
    * @throws PSErrorResultsException if any of the requested objects could not
    *    be loaded or locked.
    */
   public List<PSSharedProperty> loadSharedProperties(String[] names, 
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Save the supplied shared properties to the repository.
    * 
    * @param properties a list with all shared properties to be persisted to 
    *    the repository, new shared properties will be inserted, existing 
    *    shared properties will be updated.
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
   public void saveSharedProperties(List<PSSharedProperty> properties, 
      boolean release, String session, String user) throws PSErrorsException;
   
   /**
    * Deletes the shared properties for the supplied shared property ids. 
    * Deletes cannot be reverted. Only objects that are unlocked or locked 
    * by the requesting user and session can be deleted, for all other cases 
    * an error will be returned.
    * <p>
    * All locks for successfully deleted objects will be released, locks which
    * exist for objects that failed to be deleted remain untouched.
    * 
    * @param properties a list of properties to delete, not 
    *    <code>null</code> or empty. Objects that do not exist anymore will be 
    *    ignored.
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
   public void deleteSharedProperties(List<PSSharedProperty> properties, 
      boolean ignoreDependencies, String session, String user) 
      throws PSErrorsException;
   
   /**
    * Extends the locks for each object referenced through the supplied 
    * ids. The object locks will be extendend to the requestor for 
    * 30 minutes or until they are released manually.
    * 
    * @param ids a list of object ids for each object the user wants to extend 
    *    an existing lock, not <code>null</code> or empty.
    * @param session the rhythmyx session for which to extend the locks, not 
    *    <code>null</code> or empty. 
    * @param user the user for which to extend the locks, not 
    *    <code>null</code> or empty. 
    *    
    * @throws PSErrorsException if there are any errors. 
    */
   public void extendLocks(List<IPSGuid> ids, String session, String user) 
      throws PSErrorsException;
   
   /**
    * Releases all object locks for all supplied ids. Objects which are 
    * not locked by the requestor will be ignored.
    * 
    * @param ids a list of object ids for each object the user wants to 
    *    release, not <code>null</code> or empty.
    * @param session the rhythmyx session for which to release the specified 
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to release the specified objects, not 
    *    <code>null</code> or empty. 
    */
   public void releaseLocks(List<IPSGuid> ids, String session, String user);
   
   /**
    * Looks up all objects locked for the user and session making the request 
    * and returns the object summaries for the found locks.
    * 
    * @param session the rhythmyx session for which to get the summaries of
    *    all objects he has locked, not <code>null</code> or empty. 
    * @param user the user for which to get the summaries of
    *    all objects he has locked, not <code>null</code> or empty. 
    * @return the object summaries for all objects for which the requesting 
    *    user owns the lock, never <code>null</code>, may be empty.
    * @throws PSErrorResultsException if any summary failed to load.
    */
   public List<PSObjectSummary> getLockedSummaries(String session, 
      String user) throws PSErrorResultsException;

   /**
    * Locks the objects for all supplied ids for the user making the request. 
    * The locks are valid for 30 minutes and may be extended using the 
    * <code>ExtendLocks</code> service or released using the 
    * <code>ReleaseLocks</code> service. New locks are created for all 
    * unlocked objects. Locks which the user already owns are extended. For 
    * objects that are locked by somebody else an error will be returned.
    * 
    * @param ids a list of object ids for each object the user wants to lock, 
    *    not <code>null</code> or empty.
    * @param overrideLock <code>true</code> to override an existing lock, 
    *    defaults to <code>false</code>. If the same user making the request 
    *    already has an object locked with a different session, this allows 
    *    him to override that lock.
    * @param session the rhythmyx session, not <code>null</code> or empty. 
    * @param user the user making the request, not <code>null</code> or empty. 
    * @throws PSErrorsException if any requested object could not be locked.
    */
   public void createLocks(List<IPSGuid> ids, boolean overrideLock, 
      String session, String user) throws PSErrorsException;
   /**
    * Tests if the objects for the supplied ids are locked by anybody or not.
    * 
    * @param ids the ids of all objects to test, not <code>null</code> or
    *    empty.
    * @param user the current user, not <code>null</code> or empty. 
    * @return the returned list contains the object summary for all locked
    *    objects or <code>null</code> if the object for that id is not locked,
    *    never <code>null</code> or empty. The returned summaries have the 
    *    same order as the ids supplied with the request.
    * @throws PSErrorResultsException if any summary failed to load.
    */
   public List<PSObjectSummary> isLocked(List<IPSGuid> ids, String user) 
      throws PSErrorResultsException;
   
   /**
    * Find all dependencies for objects associated with the supplied ids.
    * 
    * @param ids a list of object ids, one for each object to find all 
    *    dependencies for, not <code>null</code> or empty. The ids must 
    *    reference existing objects.
    * @return a list with all dependencies found for the supplied objects, 
    *    never <code>null</code> or empty, in the same order as requested.
    */
   @IPSWsMethod(ignore=true)
   public List<PSDependency> findDependencies(List<IPSGuid> ids);
   
   /**
    * Load the configuration for the supplied name.
    * 
    * @param name the configuration name to be loaded, not <code>null</code> 
    *    or empty, must be the name of a supported configuration.
    * @param lock <code>true</code> to lock the loaded configuration for edit, 
    *    <code>false</code> to return it read-only. Defaults to 
    *    <code>false</code> if not supplied. 
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>. 
    * @param overrideLock <code>true</code> to allow the requesting user to
    *    override existing locks he owns in a different session, 
    *    <code>false</code> otherwise.
    * @return the configuration in the requested mode, never <code>null</code>.
    * @throws FileNotFoundException If failed to load the configuration.
    * @throws PSLockErrorException If the request lock cannot be obtained.
    */
   public PSMimeContentAdapter loadConfiguration(String name, 
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws FileNotFoundException, PSLockErrorException;
   
   /**
    * Save the supplied configuration to the server.
    * 
    * @param configuration the configuration to be saved to the server, not 
    *    <code>null</code>.
    * @param release <code>true</code> to release all object locks after the 
    *    save, <code>false</code> to keep the locks. All locks will be
    *    released, no matter whether the operation was successful or not.
    * @param session the rhythmyx session for which to release the saved
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to release the saved objects, not 
    *    <code>null</code> or empty. 
    * @throws PSLockErrorException if the configuration is not locked.
    * @throws IOException if the configuration could not be saved.
    */
   public void saveConfiguration(PSMimeContentAdapter configuration, 
      boolean release, String session, String user) throws PSLockErrorException, 
      IOException;
   
   /**
    * Creates a new relationship type for the supplied parameters. The new 
    * relationship type is not persisted until you call 
    * {@link #saveRelationshipTypes(List, boolean, String, String)} for the 
    * returned object.
    * 
    * @param names the names for the new relationship types, not 
    *    <code>null</code> or empty. The name must be unique across all defined 
    *    relationship types in the system, names are compared case-insensitive 
    *    and cannot contain spaces.
    * @param categories the categories of the new relationship types, not 
    *    <code>null</code> or empty. Each category must be one of 
    *    <code>Active Assembly</code>, <code>New Copy</code>, 
    *    <code>Promotable Version</code> or <code>Translation</code>.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty. 
    *    
    * @return a list of new relationship types initialized with the supplied 
    *    parameters, never <code>null</code> or empty. The user must call 
    *    {@link #saveRelationshipTypes(List, boolean, String, String)} for the 
    *    returned object to persist the definition.
    * 
    * @throws PSLockErrorException if failed to create a lock for the 
    *    created relationship type.
    * @throws PSErrorException if failed to load existing relationships
    */
   public List<PSRelationshipConfig> createRelationshipTypes(List<String> names, 
      List<String> categories, String session, String user)  
      throws PSLockErrorException, PSErrorException;
   
   /**
    * Finds all relationship type summaries for the supplied search parameters.
    * 
    * @param name the relationship type name to find, may be <code>null</code> 
    *    or empty, asterisk wildcards are accepted. If not supplied or empty, 
    *    all relationship type summaries will be returned.
    * @param category the relationship category for which to find the 
    *    configurations, may be <code>null</code> but not empty. Must be an 
    *    existing category. All relationship configuration summaries will be 
    *    retunred if not supplied.
    *    
    * @return a list with all object summaries of type 
    *    <code>PSRelationshipConfig</code> found for the supplied parameters, 
    *    never <code>null</code>, may be empty, ascending alpha ordered by name.
    *    
    * @throws PSErrorException if an error occurred during the lookup process.
    */
   public List<IPSCatalogSummary> findRelationshipTypes(String name, 
      String category) throws PSErrorException;
   
   /**
    * Loads all relationship types for the supplied ids in the requested mode.
    * 
    * @param ids a list of relationship type ids to be loaded, not 
    *    <code>null</code> or empty, must be ids of existing relationship types.
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
    * @return a list with all loaded relationship types in the requested mode 
    *    in the same order as requested, never <code>null</code> or empty.
    * @throws PSErrorResultsException if any of the requested objects could not
    *    be loaded or locked.
    */
   public List<PSRelationshipConfig> loadRelationshipTypes(List<IPSGuid> ids, 
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Save the supplied relationship types to the repository.
    * 
    * @param configs a list with all relationship types to be persisted to 
    *    the repository, new relationship types will be inserted, existing 
    *    relationship types will be updated.
    * @param release <code>true</code> to release all object locks after the 
    *    save, <code>false</code> to keep the locks. All locks will be
    *    released, no matter whether the operation was successful or not.
    * @param session the rhythmyx session for which to release the saved
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to release the saved objects, not 
    *    <code>null</code> or empty. 
    *    
    * @throws PSErrorsException if any of the supplied objects could not
    *    be saved.
    * @throws PSErrorException if could not load current relationship types
    *    before the save operation.
    */
   public void saveRelationshipTypes(List<PSRelationshipConfig> configs, 
      boolean release, String session, String user) 
      throws PSErrorsException, PSErrorException;
   
   /**
    * Deletes the relationship types for the supplied relationship type ids. 
    * Deletes cannot be reverted. Only objects that are unlocked or locked 
    * by the requesting user and session can be deleted, for all other 
    * cases an error will be returned.
    * <p>
    * All locks for successfully deleted objects will be released, locks which
    * exist for objects that failed to be deleted remain untouched.
    * 
    * @param ids an array of ids for which to delete the relationship types, 
    *    not <code>null</code> or empty. We ignore cases where the object for 
    *    a supplied id does not exist.
    * @param ignoreDependencies specifies whether or not the dependency check 
    *    prior to the delete of an object should be ignored, defaults to 
    *    <code>false</code> if not supplied. If dependency checks are enabled, 
    *    only objects without depenencies will be deleted, for all others an 
    *    error is returned so that the client can deal with it appropriately.
    * @param session the rhythmyx session, not <code>null</code> or empty. 
    * @param user the rhythmyx user, not <code>null</code> or empty. 
    * @throws PSErrorException if failed to load existing relationship 
    *    configuration set.
    * @throws PSErrorsException if an error occurs while deleting the specified
    *    relationship configurations.
    */
   public void deleteRelationshipTypes(List<IPSGuid> ids, 
      boolean ignoreDependencies, String session, String user)
      throws PSErrorsException, PSErrorException;

   /**
    * Finds all workflow definition summaries for the supplied name.
    * 
    * @param name the workflow name for which to find the summaries, may be 
    *    <code>null</code> or empty, asterisk wildcards are accepted. All 
    *    workflow summaries will be returned if <code>null</code> or empty.
    * @return a list with all object summaries of type <code>PSWorkflow</code> 
    *    found for the supplied name, never <code>null</code>, may be empty, 
    *    ascending alpha ordered by name.
    */
   public List<IPSCatalogSummary> findWorkflows(String name);
   
   /**
    * Create a new ACl for the specified object with default permissions. 
    * The ACL is not saved to the repository until you call 
    * {@link #saveAcls(List, boolean, String, String)} for the returned object.
    * 
    * @param id the object id for which to create a new ACL, not 
    *    <code>null</code>. Must be an id of an existing object.
    * @param session the rhythmyx session for which to lock the created
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to use as the acl owner and to lock the 
    *    created objects, not <code>null</code> or empty. 
    *    
    * @return the new ACL created for the supplied object id with default 
    *    permissions, never <code>null</code>.
    *    
    * @throws PSLockErrorException If there is an error locking the newly 
    * created object. 
    */
   public PSAclImpl createAcl(IPSGuid id, String session, String user) 
      throws PSLockErrorException;
   
   /**
    * Loads the ACL’s for all objects identified through the supplied ids.
    * 
    * @param ids a list of design object ids for which to load the ACL’s, not
    * empty. If <code>null</code>, all acls are loaded.
    * @param lock <code>true</code> to lock the loaded results for edit,
    * <code>false</code> to return them read-only. Defaults to
    * <code>false</code> if not supplied.
    * @param overrideLock <code>true</code> to allow the requesting user to
    * override existing locks he owns in a different session, <code>false</code>
    * otherwise.
    * @param session the rhythmyx session for which to lock the returned
    * objects, not <code>null</code> or empty if lock is <code>true</code>.
    * @param user the user for which to lock the returned objects, not
    * <code>null</code> or empty <code>true</code>.
    * @return a list of ACL’s, one for each object id supplied with the request
    * in the same order, never <code>null</code> or empty. A <code>null</code>
    * list entry will be returned for objects that do not have an ACL object.
    * @throws PSErrorResultsException if any of the requested objects could not
    * be loaded or locked.
    */
   @IPSWsStrategy(value=PSLoadAclSecurityStrategy.class)
   public List<PSAclImpl> loadAcls(List<IPSGuid> ids, 
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Save the object ACL’s to the repository.
    * 
    * @param acls a list of object ACL’s to be saved to the repository, not 
    *    <code>null</code> or empty. New ACL’s will be inserted, existing ACL’s 
    *    will be updated.
    * @param release <code>true</code> to release all object locks after the 
    *    save, <code>false</code> to keep the locks. All locks will be
    *    released, no matter whether the operation was successful or not.
    * @param session the rhythmyx session for which to release the saved
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to release the saved objects, not 
    *    <code>null</code> or empty.
    * @return the permissions of the requestor to all saved objects in the same
    *    order as supplied, never <code>null</code> or empty.
    * @throws PSErrorResultsException if any of the supplied objects could not
    *    be saved.
    */
   @IPSWsPermission(PSPermissions.OWNER)
   public List<PSUserAccessLevel> saveAcls(List<PSAclImpl> acls, 
      boolean release, String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Deletes the ACL’s for the supplied ids. Deletes cannot be reverted.
    * Only objects that are unlocked or locked by the requesting
    * user and session can be deleted, for all other cases an error will be 
    * returned.
    * <p>
    * All locks for successfully deleted objects will be released, locks which
    * exist for objects that failed to be deleted remain untouched.
    * 
    * @param ids a list of ACL ids to delete, not <code>null</code> or empty. 
    *    We ignore cases where the ACL for a supplied id does not exist.
    * @param ignoreDependencies specifies whether or not the dependency check 
    *    prior to the delete of an object should be ignored, defaults to 
    *    <code>false</code> if not supplied. If dependency checks are enabled, 
    *    only objects without depenencies will be deleted, for all others an 
    *    error is returned so that the client can deal with it appropriately.
    * @param session the rhythmyx session, not <code>null</code> or empty. 
    * @param user the rhythmyx user, not <code>null</code> or empty. 
    * @throws PSErrorsException for any error while deleting the requested 
    *    objects.
    */
   @IPSWsPermission(PSPermissions.OWNER)
   public void deleteAcls(List<IPSGuid> ids, boolean ignoreDependencies, 
      String session, String user) throws PSErrorsException;
   
   /**
    * Create the specified number of guids for the requested type.
    * 
    * @param type the type of the object for which to create the new guids,
    *    not <code>null</code>.
    * @param count the number of guids to create, must be > 0.
    * @return the new created guids, never <code>null</code> or empty.
    */
   @IPSWsMethod(ignoreAuthorization=true)
   public List<IPSGuid> createGuids(PSTypeEnum type, int count);
   
   /**
    * Creates new item filters for the supplied names. The returned filters 
    * are not persisted to the repository until you call 
    * {@link #saveItemFilters(List, boolean, String, String)} for the returned 
    * objects.
    * 
    * @param names the names for the new item filters, not 
    *    <code>null</code> or empty. The names must be unique across all 
    *    defined filters in the system, names are compared case-insensitive.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty. 
    * @return the new item filters initialized with the supplied 
    *    names, never <code>null</code>. The user must call 
    *    {@link #saveItemFilters(List, boolean, String, String)} for the 
    *    returned objects to persist them to the repository.
    */
   public List<PSItemFilter> createItemFilters(List<String> names, 
      String session, String user);
   
   /**
    * Finds all item filter summaries for the supplied name.
    * 
    * @param name the name of the item ffilter to find, may be 
    *    <code>null</code> or empty, asterisk wildcards are accepted. All 
    *    filter summaries will be returned if the supplied name is 
    *    <code>null</code> or empty.
    * @return a list of object summaries of type <code>PSitemFilter</code> 
    *    found for the supplied name, never <code>null</code>, may be empty, 
    *    alpha ordered by name.
    */
   public List<IPSCatalogSummary> findItemFilters(String name);
   
   /**
    * Loads all item filters for the supplied ids in the requested mode.
    * 
    * @param ids a list of item filter ids to be loaded, not <code>null</code> 
    *    or empty, must be ids of existing item filters.
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
    * @return a list with all loaded item filters in the requested mode in the 
    *    same order as requested, never null or empty.
    * @throws PSErrorResultsException if any of the requested objects could not
    *    be loaded or locked.
    */
   public List<PSItemFilter> loadItemFilters(List<IPSGuid> ids, 
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Save all supplied item filters to the repository. New item filters will 
    * be inserted, existing item filters updated.
    * 
    * @param filters a list with all item filters to be saved to the repository, 
    *    not <code>null</code> or empty.
    * @param release <code>true</code> to release all object locks after the 
    *    save, <code>false</code> to keep the locks. All locks will be
    *    released, no matter whether the operation was successful or not.
    * @param session the rhythmyx session for which to release the saved
    *    objects, not <code>null</code> or empty if release is 
    *    <code>true</code>. 
    * @param user the user for which to release the saved objects, not 
    *    <code>null</code> or empty if release is <code>true</code>. 
    * @throws PSErrorsException for any error saving the supplied objects.
    */
   public void saveItemFilters(List<PSItemFilter> filters, 
      boolean release, String session, String user) throws PSErrorsException;
   
   /**
    * Deletes the item filters for all supplied ids. Deletes cannot be 
    * reverted. Only objects that are unlocked or locked by the requesting
    * user and session can be deleted, for all other cases an error will be 
    * returned.
    * <p>
    * All locks for successfully deleted objects will be released, locks which
    * exist for objects that failed to be deleted remain untouched.
    * 
    * @param ids a list with ids of all item filters to be deleted from the 
    *    repository, not <code>null</code> or empty. We ignore cases where the 
    *    object for a supplied id does not exist.
    * @param ignoreDependencies specifies whether or not the dependency check 
    *    prior to the delete of an object should be ignored, defaults to 
    *    <code>false</code> if not supplied. If dependency checks are enabled, 
    *    only objects without depenencies will be deleted, for all others an 
    *    error is returned so that the client can deal with it appropriately.
    * @param session the rhythmyx session for which to delete the supplied
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to delete the supplied objects, not 
    *    <code>null</code> or empty.
    * @throws PSErrorsException for any error while deleting the requested 
    *    objects.
    */
   public void deleteItemFilters(List<IPSGuid> ids, boolean ignoreDependencies, 
      String session, String user) throws PSErrorsException;
}
