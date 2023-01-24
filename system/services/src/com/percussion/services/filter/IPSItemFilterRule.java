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

import com.percussion.extension.IPSExtension;
import com.percussion.services.filter.impl.PSBaseFilter;

import java.util.List;
import java.util.Map;

/**
 * A filter rule defines a single criteria that may be used in an item filter.
 * Each filter rule processes all filter items. Each item will be processed
 * in one of the following three ways:
 * <ul>
 * <li>If the item is acceptable to the rule, it can be passed without change
 * <li>If the item does not meet the rule, it will be not be returned in the
 * output list
 * <li>If the item meets the rule, it may require a different revision than 
 * the original item specifies. In this case the item is cloned with a different
 * item id.
 * </ul>
 * Filter rules can be based on the base class {@link PSBaseFilter}, which 
 * provides an abstract base on which to build a rule. However, building from
 * scratch is not notably more difficult. Note that rules are extensions and
 * must implement the {@link IPSExtension} methods as well. 
 * <p>
 * Filter rules are defined through the extensions manager.
 * <p>
 * Note that the revision of the item in the list is not predictable unless some
 * earlier rule has taken care to set it. Different invokers may or may not
 * pick a specific revision.
 * 
 * @author dougrand
 */
public interface IPSItemFilterRule extends IPSExtension
{
   /**
    * The value of this parameter is a {@link List} of values. Each value is of
    * type <code>Character</code>. These values are compared against the
    * validity field for the content item's workflow state.
    */
   public static final String VALIDSTATES = "VALIDSTATES";

   /**
    * The value of this parameter is a {@link List} of folder paths expressed as
    * strings.
    */
   public static final String FOLDERPATHS = "FOLDERPATHS";

   /**
    * Each filter rule has a priority. Rules are applied in descending priority
    * order. Rules with a higher priority should be more restrictive to allow a
    * more efficient processing of items.
    * 
    * @return a number greater than or equal to zero, with higher priorities run
    *         before lower priorities. If two rules have an identical priority
    *         the ordering is not deterministic.
    */
   int getPriority();

   /**
    * A filter takes a set of references to content items and returns a new set
    * of items that match the filter. Filters are called with a set of
    * parameters that are specific to each filter, with a set of parameter
    * string values predefined on the interface.
    * <p>
    * If an {@link IPSFilterItem} passed to this method needs to be "changed",
    * then a new copy should be cloned, modified and returned. The original
    * item should be left unchanged.
    * 
    * @param items a list of items to filter, never <code>null</code>
    * @param params a map of parameter values, may be empty but never
    *           <code>null</code>
    * @return a list of {@link IPSFilterItem}, never <code>null</code> but
    *         may be empty if no items match the filter. 
    * @throws PSFilterException if there is an error while filtering the content
    */
   List<IPSFilterItem> filter(List<IPSFilterItem> items,
         Map<String, String> params) throws PSFilterException;
}
