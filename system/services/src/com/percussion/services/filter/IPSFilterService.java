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
package com.percussion.services.filter;

import com.percussion.services.catalog.IPSCataloger;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;
import java.util.Map;

/**
 * The filter services manages item filters and applies the item filters to
 * lists of content ids. The filter service is a higher level service that uses
 * other services to implement filtering.
 * 
 * @author dougrand
 */
public interface IPSFilterService extends IPSCataloger
{
   /**
    * Create a new item filter
    * 
    * @param name a unique name, never <code>null</code> or empty
    * @param description a description, may be <code>null</code> or empty
    * @return a newly created filter object
    */
   IPSItemFilter createFilter(String name, String description);

   /**
    * Load one or more filters from the guids
    * 
    * @param ids a list of guids, may not be empty, never <code>null</code>
    * @return a list of filter objects, never <code>null</code> or empty
    * @throws PSNotFoundException if one or more ids are not found or invalid
    */
   List<IPSItemFilter> loadFilter(List<IPSGuid> ids) throws PSNotFoundException;

   /**
    * Load the item filter for the supplied id.
    * 
    * @param id the id of the item filter to load, not <code>null</code>.
    * @return the loaded item filter, never <code>null</code>.
    * @throws PSNotFoundException if no filter was found for the supplied id.
    */
   IPSItemFilter loadFilter(IPSGuid id) throws PSNotFoundException;

   /**
    * Loads one filter by name. The returned filter should be considered
    * read-only as this method uses an in-memory cache that is shared between
    * threads.
    * 
    * @param name name of filter, never <code>null</code> or empty
    * @return the item filter, never <code>null</code>
    * @throws PSFilterException if no filter is found with the given name
    */
   IPSItemFilter findFilterByName(String name) throws PSFilterException;
   
   /**
    * Loads one filter by id. The returned filter should be considered
    * read-only as this method uses an in-memory cache that is shared between
    * threads.
    * 
    * @param id id of filter, never <code>null</code>.
    * @return the item filter, may be <code>null</code> if filter not found.
    */
   IPSItemFilter findFilterByID(IPSGuid id) throws PSNotFoundException;

   /**
    * Find all filters for the supplied name.
    * 
    * @param name the name of the filter to find, may be <code>null</code> or
    *           empty. Finds all filters if <code>null</code> or empty, sql
    *           type (%) wildcards are supported.
    * @return a list with all found item filters for the supplied name, never
    *         <code>null</code>, may be empty, ascending alpha ordered by
    *         name.
    */
   List<IPSItemFilter> findFiltersByName(String name);

   /**
    * Loads one filter by the legacy authtype
    * 
    * @param authtype the authtype
    * @return the item filter, never <code>null</code>
    * @throws PSFilterException if no filter is found with the given authtype
    */
   IPSItemFilter findFilterByAuthType(int authtype) throws PSFilterException;

   /**
    * Get all the filters known to the system
    * 
    * @return a list of known filters, never <code>null</code>
    */
   List<IPSItemFilter> findAllFilters();

   /**
    * Saves or updates the filter to the repository
    * 
    * @param filter the filter, never <code>null</code>
    * @throws PSFilterException if there is a problem storing into the
    *            repository
    */
   void saveFilter(IPSItemFilter filter) throws PSFilterException;

   /**
    * Delete a filter from the repository, but will throw an exception if there
    * is an object with a reference to the filter.
    * 
    * @param filter the filter, never <code>null</code>
    */
   void deleteFilter(IPSItemFilter filter);

   /**
    * Create a new rule def. Note that rule definitions can be added or removed
    * from an {@link IPSItemFilter} but not cataloged, saved, loaded or
    * otherwise manipulated by themselves.
    * 
    * @param rule the rule, never <code>null</code> pr empty
    * @param params the parameters, never <code>null</code> but may be empty
    * @return the rule def, never <code>null</code>
    * @throws PSFilterException if the rule is not found
    */
   IPSItemFilterRuleDef createRuleDef(String rule, Map<String, String> params)
         throws PSFilterException;

   /**
    * Load a filter using an internal in-memory cache for improved performance.
    * The returned filter object should not be modified.
    *  
    * @param filterId the id of the filter to load, never <code>null</code>
    * @return the requested filter, see {@link #loadFilter(IPSGuid)} for the
    * semantics
    * @throws PSNotFoundException if the filter is not found.
    */
   public IPSItemFilter loadUnmodifiableFilter(IPSGuid filterId)
         throws PSNotFoundException;
}
