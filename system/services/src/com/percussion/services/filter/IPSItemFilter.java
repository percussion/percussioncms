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
package com.percussion.services.filter;

import com.percussion.services.catalog.IPSCatalogItem;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Filters narrow lists of content items down for use in content lists and slot
 * content finders. An item filter may aggregate any number of rules, with as few
 * as zero rules. A filter with no rules does no filtering, i.e. may be
 * considered an "identity" filter.
 * <p>
 * Filters also can adjust the items contained in the set of items
 * to be returned. A filter rule that modifies an id will not modify the 
 * original guid. A specific example of this is the preview filter rule, which
 * replaces the item id with the id of the current or edit revision of the item
 * when appropriate.
 * 
 * @see IPSItemFilterRule 
 * @see IPSItemFilterRuleDef
 * 
 * @author dougrand
 */
public interface IPSItemFilter extends IPSCatalogItem
{

   /**
    * A filter takes a list of elements that reference content items and returns
    * a new list with items that don't match the filter removed.
    * <p>
    * If one or more items in the returned set need to be modified in some
    * fashion, follow the rules specified on
    * {@link IPSItemFilterRule#filter(List, Map)}.
    * 
    * 
    * @param items the input items to filter, never <code>null</code>
    * @param params programmatic parameters to the rules in the filter, may be
    *           <code>null</code> or empty
    * 
    * @return a list of {@link IPSFilterItem}s, never <code>null</code> but
    *         may be empty if no ids match the filter.
    * @throws PSFilterException if there is a problem while filtering the
    *            content
    */
   List<IPSFilterItem> filter(List<IPSFilterItem> items,
         Map<String, String> params) throws PSFilterException;

   /**
    * The name of the filter. Filter names must be unique.
    * 
    * @return the name of the filter, never <code>null</code> or empty
    */
   String getName();

   /**
    * Set the name of the filter. If the name is not unique, this method throws
    * an exception
    * 
    * @param name the new name, never <code>null</code> or empty
    * @throws PSFilterException if the name is not unique
    */
   void setName(String name) throws PSFilterException;

   /**
    * Get the parent filter. If the parent is defined, then the associated rules
    * will be combined for filtering. The rule order is only determined by the
    * priorities of the rules, not by the chaining order.
    * 
    * @return Returns the parentFilter, may be <code>null</code>.
    */
   IPSItemFilter getParentFilter();

   /**
    * @param parentFilter The parentFilter to set, may be <code>null</code>
    */
   void setParentFilter(IPSItemFilter parentFilter);

   /**
    * A human readable description of the filter.
    * 
    * @return the description, should not be <code>null</code> or empty
    */
   String getDescription();

   /**
    * Set the description of the filter
    * 
    * @param description the new description, may be <code>null</code> or
    *           empty
    */
   void setDescription(String description);

   /**
    * A value that can be used when translating from an old authtype value to an
    * item filter. Not every filter is required to have this defined.
    * 
    * @return a value matching the old authtype that the filter replaced. May be
    *         <code>null</code> for a new filter with no old authtype
    *         equivalent.
    */
   Integer getLegacyAuthtypeId();

   /**
    * Set a new authtype id
    * 
    * @param authTypeId the new authtype id, may be <code>null</code>
    */
   void setLegacyAuthtypeId(Integer authTypeId);

   /**
    * The list of rules that define this filter. To modify the list it is best
    * to use {@link #addRuleDef(IPSItemFilterRuleDef)} and
    * {@link #removeRuleDef(IPSItemFilterRuleDef)}.
    * 
    * @return the set of rules, may be empty but never <code>null</code>
    */
   Set<IPSItemFilterRuleDef> getRuleDefs();

   /**
    * Set new rule definitions.
    * 
    * @param ruleDefs the new filter rules, may be <code>null</code> or empty.
    */
   void setRuleDefs(Set<IPSItemFilterRuleDef> ruleDefs);

   /**
    * Add a rule def to the filter. This method takes care of the details of
    * associating the rule def with the filter.
    * 
    * @param def the rule def, never <code>null</code>
    */
   void addRuleDef(IPSItemFilterRuleDef def);

   /**
    * Remove a rule def from the filter. This method takes care of the details
    * of removing the rule def with the filter. The rule def in storage will be
    * removed when the filter is saved.
    * 
    * @param def the rule def, never <code>null</code>
    */
   void removeRuleDef(IPSItemFilterRuleDef def);

}
