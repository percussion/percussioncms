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
package com.percussion.webservices.content;

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.i18n.PSLocale;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.contentmgr.data.PSContentTemplateDesc;
import com.percussion.services.contentmgr.data.PSContentTypeWorkflow;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSLockErrorException;
import com.percussion.webservices.aop.security.IPSWsMethod;
import com.percussion.webservices.aop.security.IPSWsParameter;

import java.util.List;

import javax.jcr.Node;

/**
 * This interface defines all content design related webservices.
 */
public interface IPSContentDesignWs
{
   /**
    * Gets the GUID from the specified item. If the revision of the id is
    * undefined, <code>-1</code>, then the return revision of the GUID is the
    * Edit Revision if the item is checked out by the current user; otherwise
    * the revision of the GUID is the Current Revision.
    * <p>
    * Do nothing and return the specified ID if the revision of it is not
    * <code>-1</code>.
    * 
    * @param id the ID of the item.
    * 
    * @return the GUID of the item, never <code>null</code>.
    */
   public IPSGuid getItemGuid(IPSGuid id);
   
   /**
    * Creates new keyword definitions for the supplied names with an empty key 
    * list. The returned keywords are not persisted to the repository until you 
    * call {@link #saveKeywords(List, boolean, String, String)} for the 
    * returned objects.
    * 
    * @param names the names for the new keyword definitions, not 
    *    <code>null</code> or empty. The names must be unique across all defined 
    *    keywords in the system, names are compared case-insensitive and 
    *    cannot contain spaces.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty. 
    * @return the new keyword definitions initialized with the supplied 
    *    parameters, never <code>null</code>. The user must call 
    *    {@link #saveKeywords(List, boolean, String, String)} for the 
    *    returned objects to persist the definitions to the repository.
    */
   public List<PSKeyword> createKeywords(List<String> names, String session, 
      String user);
   
   /**
    * Finds all keyword definition summaries for the supplied name.
    * 
    * @param name the name of the keyword to find, may be <code>null</code> or 
    *    empty, wildcards are accepted. All keyword summaries will be returned 
    *    if the supplied name is <code>null</code> or empty.
    * @return a list of object summaries of type <code>PSKeyword</code> 
    *    found for the supplied name, never <code>null</code>, may be empty, 
    *    alpha ordered by name.
    */
   public List<IPSCatalogSummary> findKeywords(String name);
   
   /**
    * Loads all keyword definitions for the supplied ids in the requested mode.
    * 
    * @param ids a list of keyword ids to be loaded, not <code>null</code> or 
    *    empty, must be ids of existing keywords.
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
    * @return a list with all loaded keywords in the requested mode in the 
    *    same order as requested, never null or empty.
    * @throws PSErrorResultsException if any of the requested objects could not
    *    be loaded or locked.
    */
   public List<PSKeyword> loadKeywords(List<IPSGuid> ids, 
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Save all supplied keywords to the repository. New keywords will be 
    * inserted, existing keywords updated.
    * 
    * @param keywords a list with all keywords to be saved to the repository, 
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
   public void saveKeywords(List<PSKeyword> keywords, 
      boolean release, String session, String user) throws PSErrorsException;
   
   /**
    * Deletes the keyword definitions for all supplied ids. Deletes cannot be 
    * reverted. Only objects that are unlocked or locked by the requesting
    * user and session can be deleted, for all other cases an error will be 
    * returned.
    * <p>
    * All locks for successfully deleted objects will be released, locks which
    * exist for objects that failed to be deleted remain untouched.
    * 
    * @param ids a list with ids of all keywords to be deleted from the 
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
   public void deleteKeywords(List<IPSGuid> ids, boolean ignoreDependencies, 
      String session, String user) throws PSErrorsException;

   /**
    * Creates new locale definitions for the supplied parameters. The 
    * returned locales are not persisted to the repository until you call 
    * {@link #saveLocales(List, boolean, String, String)} for the returned 
    * objects.
    * 
    * @param codes the locale codes as specified with ISO 639-1, not 
    *    <code>null</code> or empty.
    * @param labels the display labels for the new locale, not <code>null</code> 
    *    or empty.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>.
    * @return the new locale definition initialized with the supplied 
    *    parameters, never <code>null</code>. The user must call 
    *    {@link #saveLocales(List, boolean, String, String)} for the returned 
    *    object to persist the definition.
    *    
    * @throws PSErrorException If there are any unexpected errors 
    */
   public List<PSLocale> createLocales(List<String> codes, List<String> labels,
      String session, String user) throws PSErrorException;
   
   /**
    * Finds all locale summaries for the supplied locale code and / or name.
    * 
    * @param code the locale code for which to find the locale summary, may be 
    *    <code>null</code> or empty. The locale summaries for all locales will 
    *    be returned if <code>null</code> or empty.
    * @param label the display label of the locale for which to find the 
    *    locale summaries, may be <code>null</code> or empty, wildcards are 
    *    accepted. All locale summaries will be returned if <code>null</code> 
    *    or empty.
    * @return a list with all summaries of type <code>PSLocale</code> found 
    *    for the supplied parameters, never <code>null</code>, may be empty, 
    *    alpha ordered by name.
    */
   public List<IPSCatalogSummary> findLocales(String code, String label);
   
   /**
    * Finds the specified item nodes.
    * 
    * @param ids the IDs of the items in question, never <code>null</code>.
    * @param isSummary <code>true</code> if load summary properties of the 
    * items, which does not include Clob or Blob type fields; otherwise load 
    * all properties of the items.
    * 
    * @return the item nodes, never <code>null</code>, but may be empty or
    * less than the length of the <code>ids</code>.
    */
   @IPSWsMethod(ignore=true)
   public List<Node> findNodesByIds(List<IPSGuid> ids, boolean isSummary);

   /**
    * Gets the edit URL for a given item. The returned URL can be used to 
    * retrieve the XML document for editing the content of the item.
    * <p> 
    * A typical usage of this method is to specify a list of hidden fields
    * via the "viewName" parameter, e.g., sys_hiddenFields:XXX,YYY,ZZZ, where
    * the "XXX,YYY,ZZZ" is a list of field names, which should be hidden 
    * from the retrieved XML document (with the returned edit URL).
    * <p>
    * For an existing item, it must be checked out by current user. The returned
    * URL contains edit revision of the specified item. 
    * 
    * @param itemId the ID of the item. It may be <code>null</code> if the 
    * returned URL will be used for creating an item.
    * @param ctName the name of the Content Type, never blank.
    * @param viewName the value of the "sys_view" for the specified 
    * Content Type, never blank.
    * 
    * @return the edit URL described above. It may be <code>null</code> if
    * the content type does not exist or it is not enabled.
    * 
    * @throws PSErrorException if existing item is not checked out by current 
    * user.
    */
   public String getItemEditUrl(IPSGuid itemId, String ctName, String viewName);
   
   /**
    * Gets the read only view URL for a given item. 
    * 
    * @param itemId the ID of the item. It may be <code>null</code> if the 
    * returned URL will be used for creating an item.
    * @param ctName the name of the Content Type, never blank.
    * @param viewName the value of the "sys_view" for the specified 
    * Content Type, never blank.
    * 
    * @return the read-only view URL described above. It may be <code>null</code> if
    * the content type does not exist or it is not enabled. 
    */
   public String getItemViewUrl(IPSGuid itemId, String ctName, String viewName);
   
   
   /**
    * Loads all locales for the supplied ids in the requested mode.
    * 
    * @param ids a list of locale ids to be loaded, not <code>null</code> or 
    *    empty, must be ids of existing locales.
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
    * @return a list with all loaded locales in the requested mode in the 
    *    same order as requested, never <code>null</code> or empty.
    * @throws PSErrorResultsException if any of the requested objects could not
    *    be loaded or locked.
    */
   public List<PSLocale> loadLocales(List<IPSGuid> ids, 
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Save all supplied locales to the repository. New locales will be inserted, 
    * existing locales updated.
    * 
    * @param locales a list with all locales to be saved to the repository. 
    *    New locales will be inserted, existing locales are updated.
    * @param release <code>true</code> to release all object locks after the 
    *    save, <code>false</code> to keep the locks. All locks will be
    *    released, no matter whether the operation was successful or not.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>.
    *    
    * @throws PSErrorsException If there are any errors. 
    */
   public void saveLocales(List<PSLocale> locales, boolean release,
      String session, String user) throws PSErrorsException;
   
   /**
    * Deletes the locale definitions for all supplied locale ids. Deletes 
    * cannot be reverted. Only objects that are unlocked or locked by the 
    * requesting user and session can be deleted, for all other cases an 
    * error will be returned.
    * <p>
    * All locks for successfully deleted objects will be released, locks which
    * exist for objects that failed to be deleted remain untouched.
    * 
    * @param ids a list with locale ids for all locales to be deleted from the 
    *    repository, not <code>null</code> or empty. We ignore cases where the 
    *    object for a supplied id does not exist.
    * @param ignoreDependencies specifies whether or not the dependency check 
    *    prior to the delete of an object should be ignored, defaults to 
    *    <code>false</code> if not supplied. If dependency checks are enabled, 
    *    only objects without depenencies will be deleted, for all others an 
    *    error is returned so that the client can deal with it appropriately.
    * @param session the rhythmyx session, not <code>null</code> or empty. 
    * @param user the rhythmyx user, not <code>null</code> or empty. 
    * @throws PSErrorsException if we failed to delete any of the requested
    *    objects.
    */
   public void deleteLocales(List<IPSGuid> ids, boolean ignoreDependencies,
      String session, String user) throws PSErrorsException;
   
   /**
    * Loads all template associations for the specified content type in the
    * requested mode.
    * 
    * @param contentTypeId the id of the content type for which to get all
    * associated templates, may be <code>null</code> to load associations for 
    * all content types, if not <code>null</code>, must specify an existing
    * content type.
    * @param lock <code>true</code> to lock the found results for edit,
    * <code>false</code> to return them read-only. Defaults to
    * <code>false</code> if not supplied.
    * @param overrideLock <code>true</code> to allow the requesting user to
    * override existing locks he owns in a different session, <code>false</code>
    * otherwise.
    * @param session the rhythmyx session for which to lock the returned
    * objects, not <code>null</code> or empty if lock is <code>true</code>.
    * @param user the user for which to lock the returned objects, not
    * <code>null</code> or empty if lock is <code>true</code>.
    * @return a list with all loaded content types in the requested mode in the
    * same order as requested, never <code>null</code> or empty.
    * @throws PSErrorResultsException if any of the requested objects could not
    * be loaded or locked.
    */
   public List<PSContentTemplateDesc> loadAssociatedTemplates(
      IPSGuid contentTypeId, @IPSWsParameter(isLockParameter=true) boolean lock, 
      boolean overrideLock, String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Loads all workflow associations for the specified content type in the
    * requested mode.
    * 
    * @param contentTypeId the id of the content type for which to get all
    * associated workflows, may be <code>null</code> to load associations for 
    * all content types, if not <code>null</code>, must specify an existing
    * content type.
    * @param lock <code>true</code> to lock the found results for edit,
    * <code>false</code> to return them read-only. Defaults to
    * <code>false</code> if not supplied.
    * @param overrideLock <code>true</code> to allow the requesting user to
    * override existing locks he owns in a different session, <code>false</code>
    * otherwise.
    * @return a list with all loaded content types in the requested mode in the
    * same order as requested, never <code>null</code> or empty.
    * @throws PSErrorResultsException if any of the requested objects could not
    * be loaded or locked.
    */
   public List<PSContentTypeWorkflow> loadAssociatedWorkflows(
      IPSGuid contentTypeId, @IPSWsParameter(isLockParameter=true) boolean lock, 
      boolean overrideLock) 
      throws PSErrorResultsException;
   
   /**
    * Loads all current translation settings in the requested mode.
    * 
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
    * @return a list with all currently defined auto translations in the 
    *    requested mode, never <code>null</code>, may be empty.
    * @throws PSLockErrorException if any of the requested objects could not
    *    be locked.
    */
   public List<PSAutoTranslation> loadTranslationSettings(
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSLockErrorException;
   
   /**
    * Save all supplied translation settings. New settings will be inserted, 
    * existing settings will be updated and missing settings will be removed 
    * on the server.
    * 
    * @param autoTranslations a list with all auto translations to save, not 
    *    <code>null</code>, may be empty. Settings which do not exist on the 
    *    server will be inserted, settings which exist will be updated and 
    *    settings which exist on the server but not in the supplied array 
    *    will be deleted.
    * @param release <code>true</code> to release all object locks after the 
    *    save, <code>false</code> to keep the locks. All locks will be
    *    released, no matter whether the operation was successful or not.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>.
    *    
    * @throws PSLockErrorException if the translations are not locked. 
    */
   public void saveTranslationSettings(List<PSAutoTranslation> autoTranslations, 
      boolean release, 
      String session, String user) throws PSLockErrorException;
   
   /**
    * Save the specified content type template associates.  New associations 
    * will be inserted, missing associations will be removed.
    * 
    * @param contentTypeId The content type to which the templates will be
    * associated, may not be <code>null</code> and must be an existing content
    * type.
    * @param templateIds The list of templates to associate, may be 
    * <code>null</code> or empty to remove all associations.
    * @param release <code>true</code> to release all object locks after the 
    *    save, <code>false</code> to keep the locks. All locks will be
    *    released, no matter whether the operation was successful or not.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>.    
    *     
    * @throws PSErrorsException If there are any errors.
    */
   public void saveAssociatedTemplates(IPSGuid contentTypeId, 
      List<IPSGuid> templateIds, boolean release, String session, 
      String user) throws PSErrorsException;

   /**
    * Save the specified content type workflow associates.  New associations 
    * will be inserted, missing associations will be removed.
    * 
    * @param contentTypeId The content type to which the workflows will be
    * associated, may not be <code>null</code> and must be an existing content
    * type.
    * @param workflowIds The list of workflows to associate, may be 
    * <code>null</code> or empty to remove all associations.
    * @param release <code>true</code> to release all object locks after the 
    *    save, <code>false</code> to keep the locks. All locks will be
    *    released, no matter whether the operation was successful or not.
    *     
    * @throws PSErrorsException If there are any errors.
    */
   public void saveAssociatedWorkflows(IPSGuid contentTypeId, 
      List<IPSGuid> workflowIds, boolean release) throws PSErrorsException;

   /**
    * Creates a new content type for the supplied parameters. The new content 
    * type is not persisted until you call 
    * {@link #saveContentTypes(List, boolean, String, String)} for the returned 
    * object.
    * 
    * @param names the names of the new content types, not <code>null</code> or 
    *    empty. The names must be unique across all defined content types in 
    *    the system, names are compared case-insensitive and cannot contain 
    *    spaces.
    * @return the new content types initialized with the supplied parameters, 
    *    never <code>null</code> or empty. The user must call 
    *    {@link #saveContentTypes(List, boolean, String, String)} for the 
    *    returned object to persist the definition.
    *    
    * @throws PSErrorException If there are any unexpected errors. 
    */
   public List<PSItemDefinition> createContentTypes(List<String> names, 
      String session, String user) throws PSErrorException;
   
   /**
    * Find all content types for the supplied name.
    * 
    * @param name the content type name for which to find the summaries, may 
    *    be <code>null</code> or empty, wildcards are accepted. All object 
    *    summaries will be returned if <code>null</code> or empty.
    * @return a list with all found object summaries of type 
    *    <code>PSItemDefinition</code>, never <code>null</code>, may be empty, 
    *    alpha ordered by name.
    */
   public List<IPSCatalogSummary> findContentTypes(String name);
   
   /**
    * Loads all content types for the supplied ids in the requested mode.
    * 
    * @param ids a list of content type ids to be loaded, not <code>null</code> 
    *    or empty, must be ids of existing content types.
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
    * @return a list with all loaded content types in the requested mode in 
    *    the same order as requested, never <code>null</code> or empty.
    * @throws PSErrorResultsException if any of the requested objects could not
    *    be loaded or locked.
    */
   public List<PSItemDefinition> loadContentTypes(List<IPSGuid> ids, 
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Save the supplied content types to the repository.
    * 
    * @param contentTypes a list with all content types to be persisted to the
    * repository, new content types will be inserted, existing content types
    * will be updated.
    * @param release <code>true</code> to release all object locks after the 
    *    save, <code>false</code> to keep the locks. All locks will be
    *    released, no matter whether the operation was successful or not.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>.    
    *     
    * @throws PSErrorsException If there are any errors.
    */
   public void saveContentTypes(List<PSItemDefinition> contentTypes, 
      boolean release, String session, String user) 
      throws PSErrorsException;
   
   /**
    * Deletes the content types for the supplied content type ids. Deletes
    * cannot be reverted. Only objects that are unlocked or locked by the
    * requesting user and session can be deleted, for all other cases an error
    * will be returned. The caller must have write privileges on the object.
    * <p>
    * Template associations are successfully deleted before the object is
    * deleted.
    * <p>
    * All locks for successfully deleted objects will be released, locks which
    * exist for objects that failed to be deleted remain untouched.
    * 
    * @param ids a list of ids for which to delete the content types, not
    * <code>null</code> or empty. We ignore cases where the object for a
    * supplied id does not exist.
    * @param ignoreDependencies specifies whether or not the dependency check
    * prior to the delete of an object should be ignored, defaults to
    * <code>false</code> if not supplied. If dependency checks are enabled,
    * only objects without depenencies will be deleted, for all others an error
    * is returned so that the client can deal with it appropriately.
    * @param session the rhythmyx session, not <code>null</code> or empty. 
    * @param user the rhythmyx user, not <code>null</code> or empty. 
    * @throws PSErrorsException if we failed to delete any of the requested
    * objects.
    */
   public void deleteContentTypes(List<IPSGuid> ids, 
      boolean ignoreDependencies, String session, String user)
         throws PSErrorsException;
   
   /**
    * Loads the content editor system definition and returns it for the 
    * requested mode. There is only one content editor system definition on 
    * each server.
    * 
    * @param lock <code>true</code> to lock the system definition for edit, 
    *    <code>false</code> to return it read-only. Defaults to 
    *    <code>false</code> if not supplied. 
    * @param overrideLock <code>true</code> to allow the requesting user to
    *    override existing locks he owns in a different session, 
    *    <code>false</code> otherwise.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>. 
    * @return the content editor system definition in the requested mode, 
    *    never <code>null</code>.
    * @throws PSLockErrorException if the requested object could not
    *    be locked.
    * @throws PSErrorException if there are any unexpected errors.
    */
   public PSContentEditorSystemDef loadContentEditorSystemDef(
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSLockErrorException, PSErrorException;
   
   /**
    * Save the supplied content editor system definition to the server.
    * 
    * @param def the content editor system definition to be saved to the 
    *    server, not <code>null</code>.
    * @param release <code>true</code> to release all object locks after the 
    *    save, <code>false</code> to keep the locks. All locks will be
    *    released, no matter whether the operation was successful or not.
    * @param session the rhythmyx session for which to release the saved
    *    object, not <code>null</code> or empty if release is 
    *    <code>true</code>. 
    * @param user the user for which to release the saved object, not 
    *    <code>null</code> or empty if release is <code>true</code>. 
    * @throws PSLockErrorException if the supplied object could not
    *    be released.
    * @throws PSErrorException if there are any unexpected errors.
    */
   public void saveContentEditorSystemDef(PSContentEditorSystemDef def, 
      boolean release, String session, String user) 
      throws PSLockErrorException, PSErrorException;
   
   /**
    * Loads the content editor shared definition.
    * 
    * @param lock <code>true</code> to lock it for edit, <code>false</code> to 
    *    return it read-only. Defaults to <code>false</code> if not supplied. 
    * @param overrideLock <code>true</code> to allow the requesting user to
    *    override existing locks he owns in a different session, 
    *    <code>false</code> otherwise.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>. 
    * @return the content editior shared definition, never <code>null</code>.
    * @throws PSLockErrorException if the requested object could not
    *    be locked.
    * @throws PSErrorException if there are any unexpected errors.
    */
   public PSContentEditorSharedDef loadContentEditorSharedDef(
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSLockErrorException, PSErrorException;
   
   /**
    * Save the supplied content editor shared definition to the server.
    * 
    * @param def the content editor shared definition to be saved to the server, 
    *    not <code>null</code>.
    * @param release <code>true</code> to release all object locks after the 
    *    save, <code>false</code> to keep the locks. All locks will be
    *    released, no matter whether the operation was successful or not.
    * @param session the rhythmyx session for which to release the saved
    *    object, not <code>null</code> or empty if release is 
    *    <code>true</code>. 
    * @param user the user for which to release the saved object, not 
    *    <code>null</code> or empty if release is <code>true</code>. 
    * @throws PSLockErrorException if the supplied object could not
    *    be released.
    * @throws PSErrorException if there are any unexpected errors.
    */
   public void saveContentEditorSharedDef(PSContentEditorSharedDef def, 
      boolean release, String session, String user) 
      throws PSLockErrorException, PSErrorException;
}

