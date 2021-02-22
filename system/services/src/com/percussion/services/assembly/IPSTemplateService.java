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
package com.percussion.services.assembly;

import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.utils.guid.IPSGuid;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Manage templates and slots, allowing the creation, reading, updating and
 * deleting of template and slot instances. Although the methods use interfaces,
 * please be aware that objects to be passed to save or delete methods must be
 * created with the create methods or loaded from the load and finder methods.
 * 
 * @author dougrand
 */
public interface IPSTemplateService
{
   /**
    * Create a new template object, but does not persist it. You must call this
    * method to create a new template that can be persisted.
    * 
    * @return a new template object, never <code>null</code>, with no
    *         allocated guid.
    */
   PSAssemblyTemplate createTemplate();

   /**
    * Load the given template instance by id.
    * 
    * @param id the id of the template, never <code>null</code>
    * @param loadSlots pass <code>true</code> if the related slot objects
    *           should be loaded into memory, otherwise they may not be accessed
    *           outside of a session
    * @return the template, never <code>null</code>
    * @throws PSAssemblyException if the template is not found or there are
    *            other problems loading the template from the database
    */
   PSAssemblyTemplate loadTemplate(IPSGuid id, boolean loadSlots)
         throws PSAssemblyException;

   /**
    * Load the given template instance by string guid, calls
    * {@link #loadTemplate(IPSGuid, boolean)} internally. This is provided as a
    * convenience function.
    * 
    * @param guid the id of the template, never <code>null</code> or empty
    * @param loadSlots pass <code>true</code> if the related slot objects
    *           should be loaded into memory, otherwise they may not be accessed
    *           outside of a session
    * @return the template, never <code>null</code>
    * @throws PSAssemblyException if the template is not found or there are
    *            other problems loading the template from the database
    */
   PSAssemblyTemplate loadTemplate(String guid, boolean loadSlots)
         throws PSAssemblyException;

   /**
    * Find and load all available templates. This is a fast call, but will 
    * return shared instances that must not be modified. Each returned object 
    * is a complete tree that includes all aggregated objects, if any.
    * The returned objects cannot be saved by 
    * {@link #saveTemplate(IPSAssemblyTemplate)}.
    * 
    * @return a list of templates, probably not empty, never <code>null</code>.
    *    the returned objects are cached and it should not be modified then
    *    saved via {@link #saveTemplate(IPSAssemblyTemplate)}.
    *    
    * @throws PSAssemblyException if failed to load templates.
    */
   Set<IPSAssemblyTemplate> findAllTemplates() throws PSAssemblyException;

   /**
    * Find and load all templates that are global templates.
    * 
    * @return a list of zero or more matching templates.
    * Never <code>null</code>. Does not contain the 5.7 legacy global templates.
    * @throws PSAssemblyException
    * @see {@link #findAll57GlobalTemplates()}
    */
   Set<IPSAssemblyTemplate> findAllGlobalTemplates() throws PSAssemblyException;
   
   /**
    * Returns the names of all the 5.7 legacy global templates. 
    * @return all the names of the 5.7 legacy global templates.
    * Never <code>null</code>, can be empty. 
    * @throws PSAssemblyException on failure to retrieve the templates.
    */
   Set<String> findAll57GlobalTemplates() throws PSAssemblyException;

   /**
    * Find templates that are related to a given slot.
    * 
    * @param slot the slot, never <code>null</code>
    * @return the list of templates, never <code>null</code> but can be empty
    * @throws PSAssemblyException
    */
   List<IPSAssemblyTemplate> findTemplatesBySlot(IPSTemplateSlot slot)
         throws PSAssemblyException;

   /**
    * Find templates that share the same content assembler, if any.
    * 
    * @param pattern a string pattern to match in the assemblyurl like the
    *           following one: <code>../appName/%</code> and never
    *           <code>null</code>
    * @param loadSlot if <code>true</code> loads slots
    * @return the list of templates, never <code>null</code> but can be empty
    * @throws PSAssemblyException
    */
   List<IPSAssemblyTemplate> findTemplatesByAssemblyUrl(String pattern,
         boolean loadSlot) throws PSAssemblyException;

   /**
    * Find templates that are related to a given contenttype. Note that the 
    * returned templates are read-only instances
    * 
    * @param contenttype the contenttype, never <code>null</code>
    * @return the list of templates, never <code>null</code> but can be empty
    * @throws PSAssemblyException
    */
   List<IPSAssemblyTemplate> findTemplatesByContentType(IPSGuid contenttype)
         throws PSAssemblyException;

   /**
    * Save the given template to the database.
    * 
    * @param var the template, never <code>null</code>
    * @throws PSAssemblyException if an error occurs saving the template to the
    *            database
    */
   void saveTemplate(IPSAssemblyTemplate var) throws PSAssemblyException;

   /**
    * Finds the given template. The returned object is a shared and cached 
    * instance that must not be modified. It is a complete tree that includes 
    * all aggregated objects, if any. The returned object cannot be saved by 
    * {@link #saveTemplate(IPSAssemblyTemplate)}.
    * 
    * @param id the ID of the template, never <code>null</code>.
    * 
    * @return the template. It may be <code>null</code> if the template does 
    *    not exist.
    */
   IPSAssemblyTemplate findTemplate(IPSGuid id);

   /**
    * Finds the given template. The returned object is a shared and cached 
    * instance that must not be modified. It is a complete tree that includes 
    * all aggregated objects, if any. The returned object cannot be saved by 
    * {@link #saveTemplate(IPSAssemblyTemplate)}.
    * 
    * @param name the name of the template, never <code>null</code>
    * @return the template
    * @throws PSAssemblyException if the template is not found or there are
    *            other problems loading the template from the database including
    *            the given template name not being unique
    */
   PSAssemblyTemplate findTemplateByName(String name)
         throws PSAssemblyException;

   /**
    * Load a given template as specified by a content type and name.
    * 
    * @param name the template name, never <code>null</code> or empty
    * @param contenttype the guid that references the content type, never
    *           <code>null</code>
    * @return a template, never <code>null</code>
    * @throws PSAssemblyException if the template is not found or there are
    *            other problems loading the template(s) from the database.
    */
   IPSAssemblyTemplate findTemplateByNameAndType(String name,
         IPSGuid contenttype) throws PSAssemblyException;

   /**
    * Finds all templates filtered for the supplied name and content type.
    * 
    * @param name the name of the templates to find, may be <code>null</code>
    *           or empty in which case the name is excluded from the search
    *           criteria, sql type (%) wildcards are accepted.
    * @param contentType the content type of the templates to find, may be
    *           <code>null</code> or empty in which case the name is excluded
    *           from the search criteria, sql type (%) wildcards are accepted.
    * @param outputFormats a set with all output formats for which to filter the
    *           results, may be <code>null</code> or empty to ignore this
    *           filter.
    * @param type the template type for which to filter that results, may be
    *           <code>null</code> to ignore this filter.
    * @param globalFilter a flag to specify whether to filter all results for
    *           global or non-global templates. If <code>true</code> only
    *           global type templates are returned, if <code>false</code> only
    *           non-global type templates are returned, if <code>null</code>
    *           is supplied the filter is ignored.
    * @param legacyFilter a flag to specify whether to filter all results for
    *           legacy or non-legacy templates. If <code>true</code> only
    *           legacy type templates are returned, if <code>false</code> only
    *           non-legacy type templates are returned, if <code>null</code>
    *           is supplied the filter is ignored.
    * @param assembler the name of the assembler for which to filter the
    *           results, may be <code>null</code> or empty to ignore this
    *           filter.
    * @return a list with all found assembly templates for the specified name
    *         and content type, never <code>null</code>, may be empty.
    * 
    * @throws PSAssemblyException
    */
   List<IPSAssemblyTemplate> findTemplates(String name, String contentType,
         Set<IPSAssemblyTemplate.OutputFormat> outputFormats,
         IPSAssemblyTemplate.TemplateType type, Boolean globalFilter,
         Boolean legacyFilter, String assembler) throws PSAssemblyException;

   /**
    * Remove the specified template from the database.
    * 
    * @param id the GUID of the template to be removed, never <code>null</code>
    * @throws PSAssemblyException if an error occurs deleting the template from
    *            the database
    */
   void deleteTemplate(IPSGuid id) throws PSAssemblyException;

   /**
    * Create a new slot object, but does not persist it. You must call this
    * method to create a new slot object to be persisted.
    * 
    * @return a new slot object, never <code>null</code>, with no allocated
    *         GUID.
    */
   IPSTemplateSlot createSlot();

   /**
    * Load the given slot instance from the database by id. The returned
    * slot is a shared instance that must not be modified, and it is a complete
    * tree that includes all aggregated objects, if any.
    * 
    * @param id the id of the slot, never <code>null</code>
    * 
    * @return the slot, it may be <code>null</code> if cannot find the slot.
    */
   IPSTemplateSlot findSlot(IPSGuid id);
   
   /**
    * Load the given slot instance from the database by id. This is a fast
    * call, but will return a shared instance that must not be modified. The
    * returned object is a complete tree that includes all aggregated
    * objects, if any.
    * 
    * @param id the ID of the slot, never <code>null</code>.
    * 
    * @return the slot, never <code>null</code>
    *
    */
   IPSTemplateSlot loadSlot(IPSGuid id) throws PSAssemblyException;

   /**
    * Load the given slot instance from the database by id.
    * 
    * @param id the id of the slot, never <code>null</code>
    * @return the slot, never <code>null</code>
    */
   IPSTemplateSlot loadSlotModifiable(IPSGuid id) throws PSAssemblyException;

   /**
    * Load the given slot instance from the database by id string. This is a 
    * fast call, but will return a shared instance that must not be modified. 
    * The returned object is a complete tree that includes all aggregated
    * objects, if any.
    * 
    * @param idstr the idstr of the slot, never <code>null</code> or empty and
    *        must be convertable to a GUID by
    *        {@link com.percussion.services.guidmgr.data.PSGuid#PSGuid(String)}.
    *        
    * @return the slot, never <code>null</code>
    *
    */
   IPSTemplateSlot loadSlot(String idstr) throws PSAssemblyException;

   /**
    * Load all template slots for the supplied IDs. 
    * The same as {@link #loadSlot(IPSGuid)}, the returned objects must not be 
    * modified and saved back to the repository.
    * 
    * @param ids the IDs for which to load the template slots, not
    *           <code>null</code> or empty.
    * @return a list with all loaded template slots in the same order as
    *         requested, never <code>null</code> or empty.
    */
   public List<IPSTemplateSlot> loadSlots(List<IPSGuid> ids) throws PSAssemblyException;

   /**
    * Save the given slot to the database.
    * 
    * @param var the slot, never <code>null</code>. It must not be a cached 
    *    object, e.g. returned by {@link #loadSlot(IPSGuid)}.
    *    
    * @throws PSAssemblyException if an error occurs saving the slot to the
    *            database
    */
   void saveSlot(IPSTemplateSlot var) throws PSAssemblyException;

   /**
    * Load the given slot from the database.
    * 
    * @param name the name of the slot, never <code>null</code> or empty
    * @return the slot, never <code>null</code>
    * @throws PSAssemblyException if the slot is missing from the database
    */
   IPSTemplateSlot findSlotByName(String name) throws PSAssemblyException;

   /**
    * Load all template slots for the supplied name.
    * 
    * @param name the name of the template slot, may be <code>null</code> or
    *           empty in which case all slots will be returned, sql type (%)
    *           wildcards are accepted.
    * @return a list with all found slot templates for the specified name, never
    *         <code>null</code>, may be empty, in ascending alpha order.
    */
   List<IPSTemplateSlot> findSlotsByName(String name);

   /**
    * Load all template slots for the supplied names.
    * 
    * @param names the names of the template slot, never <code>null</code> or
    *           empty. Names are exactly matched in an "in" clause
    * @return a list with all found slot templates for the specified name, never
    *         <code>null</code>, may be empty, in ascending alpha order.
    */
   List<IPSTemplateSlot> findSlotsByNames(List<String> names);

   /**
    * Remove the specified slot from the database.
    * 
    * @param id the slot to be removed, never <code>null</code>
    * @throws PSAssemblyException if an error occurs deleting the slot from the
    *            database
    */
   void deleteSlot(IPSGuid id) throws PSAssemblyException;

   /**
    * Load a template that will remain unmodified. There are some minor
    * modifications that may occur internally to transient data, but all
    * persisted data should remain unchanged to avoid concurrency issues.
    * 
    * @param tid the id of the template to load, never <code>null</code>
    * @return the template, which will be loaded from an in-memory cache
    *         if possible, never <code>null</code>
    * @throws PSAssemblyException if there's a problem loading the template from
    *            the database, see {@link #loadTemplate(IPSGuid, boolean)} for
    *            more information
    */
   public IPSAssemblyTemplate loadUnmodifiableTemplate(IPSGuid tid)
         throws PSAssemblyException;
   

   /**
    * Load a template that will remain unmodified. There are some minor
    * modifications that may occur internally to transient data, but all
    * persisted data should remain unchanged to avoid concurrency issues.
    * 
    * @param tid the id of the template to load, never <code>null</code> or
    *           empty
    * @return the template, which will be loaded from an in-memory cache
    *         if possible, never <code>null</code>
    * @throws PSAssemblyException if there's a problem loading the template from
    *            the database, see {@link #loadTemplate(IPSGuid, boolean)} for
    *            more information
    */
   public IPSAssemblyTemplate loadUnmodifiableTemplate(String tid)
         throws PSAssemblyException;
   
   /**
    * Creates a list of template binding objects from the given LinkedHashMap<String,
    * String>. Creates the template binding with variable as the first parameter
    * and expression as the second parameter, the linked hash maps order is used
    * as order by adding the starting order value. Throws assembly exception if
    * the variable parameter is null or empty. If the expression parameter is
    * null then it is set to empty string. The bindings are not persisted
    * bindings and should be used for in memory objects only.
    * 
    * @param bindings must not be <code>null</code>. may be empty.
    * @param startingOrder the starting order for the bindings. If it is less than 1 then uses
    *            1 as the starting order.
    * @return List of bindings never <code>null</code>, may be empty.
    * 
    * @throws IllegalArgumentException if the bindings is <code>null</code> or
    *             if the key of the bindings which is set as variable on the
    *             binding is blank.
    */
   public List<PSTemplateBinding> createBindings(
         LinkedHashMap<String, String> bindings, int startingOrder);
}
