/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
