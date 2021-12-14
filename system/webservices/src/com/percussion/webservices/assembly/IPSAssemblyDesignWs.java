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
package com.percussion.webservices.assembly;

import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.aop.security.IPSWsMethod;
import com.percussion.webservices.aop.security.IPSWsParameter;
import com.percussion.webservices.assembly.data.PSAssemblyTemplateWs;

import java.util.List;
import java.util.Set;

/**
 * This interface defines all assembly design related webservices.
 */
public interface IPSAssemblyDesignWs
{
   /**
    * Creates new template slots for the supplied parameters. The returned 
    * template slots are not persisted to the repository until you call 
    * {@link #saveSlots(List, boolean, String, String)} for the returned objects.
    * 
    * @param names the names for the new template slots, not <code>null</code> 
    *    or empty. The names must be unique across all defined template slots 
    *    in the system, names are compared case-insensitive and cannot contain 
    *    spaces.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>. 
    * @return the new template slots initialized with the supplied parameters. 
    *    The user must call {@link #saveSlots(List, boolean, String, String)} 
    *    for the returned objects to persist the definitions.
    */
   public List<IPSTemplateSlot> createSlots(List<String> names, String session, 
      String user);
   
   /**
    * Finds all template slot summaries for the supplied name.
    * 
    * @param name the template slot name for which to find the summaries, 
    *    may be <code>null</code> or empty, asterisk wildcards are accepted. 
    *    If not supplied or empty, all summaries will be returned.
    * @param associatedTemplateId the template id for which to filter the 
    *    returned results, may be <code>null</code> to ignore this filter. 
    *    If supplied, only slots that have this template as an allowed 
    *    template will be returned.
    * @return a list with all objects summaries of type 
    *    <code>PSTemplateSlot</code> found for the supplied name, never 
    *    <code>null</code>, may be empty, alpha ordered by name.
    */
   public List<IPSCatalogSummary> findSlots(String name, 
      IPSGuid associatedTemplateId);
   
   /**
    * Loads all template slots for the supplied ids in the requested mode.
    * 
    * @param ids a list of template slot ids to be loaded, not 
    *    <code>null</code> or empty, must be ids of existing template slots.
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
    * @return a list with all loaded template slots in the requested mode in 
    *    the same order as requested, never <code>null</code> or empty.
    * @throws PSErrorResultsException if any of the requested objects could not
    *    be loaded or locked.
    */
   public List<IPSTemplateSlot> loadSlots(List<IPSGuid> ids, 
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Save all supplied template slots to the repository. New template slots 
    * will be inserted, existing template slots updated.
    * 
    * @param slots a list with all template slots to be saved to the repository, 
    *    not <code>null</code> or empty. New template slots will be inserted, 
    *    existing template slots are updated.
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
   public void saveSlots(List<IPSTemplateSlot> slots, boolean release, 
      String session, String user) throws PSErrorsException;
   
   /**
    * Deletes the template slots for all supplied ids. Deletes cannot be 
    * reverted. Only objects that are unlocked or locked by the requesting
    * user and session can be deleted, for all other cases an error will be 
    * returned. Also deletes the template-slot associations, and in order to
    * do this must temporarily lock all templates that contain the supplied
    * slots.
    * <p>
    * All locks for successfully deleted objects will be released, locks which
    * exist for objects that failed to be deleted remain untouched.
    * 
    * @param ids a list with ids of all template slots to be deleted from the 
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
   public void deleteSlots(List<IPSGuid> ids, 
      boolean ignoreDependencies, String session, String user) 
      throws PSErrorsException;
   
   /**
    * Creates new assembly templates for the supplied parameters. The returned 
    * assembly templates are not persisted to the repository until you call 
    * {@link #saveAssemblyTemplates(List, boolean, String, String)} for the 
    * returned objects.
    * 
    * @param names the names for the new assembly templates, not 
    *    <code>null</code> or empty. The names must be unique across all 
    *    defined assembly templates in the system, names are compared 
    *    case-insensitive and cannot contain spaces.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>. 
    * @return the new assembly template initialized with the supplied 
    *    parameters and default values. The user must call 
    *    {@link #saveAssemblyTemplates(List, boolean, String, String)} for 
    *    the returned object to persist the definition.
    */
   public List<PSAssemblyTemplateWs> createAssemblyTemplates(List<String> names, 
      String session, String user);
   
   /**
    * Finds all assembly template summaries for the supplied parameters.
    * 
    * @param name the assembly template name for which to find the summaries, 
    *    may be <code>null</code> or empty, asterisk wildcards are accepted. All 
    *    summaries will be returned if not supplied or empty.
    * @param contentType the content type name for which to filter the results, 
    *    may be <code>null</code> or empty to ignore the filter, asterisk 
    *    wildcards are accepted.
    * @param outputFormats a set with all output formats for which to filter 
    *    the results, may be <code>null</code> or empty to ignore this filter.
    * @param type the template type for which to filter that results, may be
    *    <code>null</code> to ignore this filter.
    * @param globalFilter a flag to specify whether to filter all results 
    *    for global or non-global templates. If <code>true</code> only 
    *    global type templates are returned, if <code>false</code> only 
    *    non-global type templates are returned, if <code>null</code> is 
    *    supplied the filter is ignored.
    * @param legacyFilter a flag to specify whether to filter all results 
    *    for legacy or non-legacy templates. If <code>true</code> only legacy 
    *    type templates are returned, if <code>false</code> only non-legacy 
    *    type templates are returned, if <code>null</code> is supplied the 
    *    filter is ignored.
    * @param assembler the name of the assembler for which to filter the 
    *    results, may be <code>null</code> or empty to ignore this filter. 
    * @return a list with all object summaries of type 
    *    <code>PSAssemblyTemplate</code> found for the supplied parameters, 
    *    never <code>null</code>, may be empty, alpha ordered by name.
    */
   @IPSWsMethod(ignore=true)
   public List<IPSCatalogSummary> findAssemblyTemplates(String name, 
      String contentType, Set<IPSAssemblyTemplate.OutputFormat> outputFormats,
      IPSAssemblyTemplate.TemplateType type, Boolean globalFilter, 
      Boolean legacyFilter, String assembler);
   
   /**
    * Loads all assembly templates for the supplied ids in the requested mode.
    * 
    * @param ids a list of assembly template ids to be loaded, not 
    *    <code>null</code> or empty, must be ids of existing assembly templates.
    * @param lock <code>true</code> to lock the found results for edit, 
    *    <code>false</code> to return them read-only.
    * @param overrideLock <code>true</code> to allow the requesting user to
    *    override existing locks he owns in a different session, 
    *    <code>false</code> otherwise.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>. 
    * @return a list with all loaded assembly templates in the requested mode 
    *    in the same order as requested, never <code>null</code> or empty.
    * @throws PSErrorResultsException if any of the requested objects could not
    *    be loaded or locked.
    */
   public List<PSAssemblyTemplateWs> loadAssemblyTemplates(List<IPSGuid> ids, 
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Save all supplied assembly templates to the repository. New assembly 
    * templates will be inserted, existing assembly templates updated.
    * 
    * @param templates a list with all assembly templates to be saved to the 
    *    repository, not <code>null</code> or empty. New assembly templates 
    *    will be inserted, existing assembly templates are updated.
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
   public void saveAssemblyTemplates(List<PSAssemblyTemplateWs> templates, 
      boolean release, String session, String user) 
      throws PSErrorsException;
   
   /**
    * Deletes the assembly templates for all supplied ids. Deletes cannot be 
    * reverted. Only objects that are unlocked or locked by the requesting
    * user and session can be deleted, for all other cases an error will be 
    * returned. The caller must have write privileges on the object.
    * <p>
    * Content type associations are successfully deleted before the object is
    * deleted.
    * <p>
    * All locks for successfully deleted objects will be released, locks which
    * exist for objects that failed to be deleted remain untouched.
    * 
    * @param ids a list with ids of all assembly templates to be deleted from 
    *    the repository, not <code>null</code> or empty. We ignore cases where 
    *    the object for a supplied id does not exist.
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
   public void deleteAssemblyTemplates(List<IPSGuid> ids, 
      boolean ignoreDependencies, String session, String user) 
      throws PSErrorsException;
   
   /**
    * Gets the thumb size image paths for the specified templates.
    * 
    * @param names the names of the templates, never <code>null</code>, may be
    * empty.
    * @param site the site name, never <code>null</code> or empty. It may be
    * It may <code>AnySite</code>.
    * 
    * @return the image file paths which are relative to the root of the 
    * server installation. Never <code>null</code>, may be empty if the names 
    * list is empty. 
    */
   public List<String> getTemplateThumbImages(List<String> names, String site);
}

