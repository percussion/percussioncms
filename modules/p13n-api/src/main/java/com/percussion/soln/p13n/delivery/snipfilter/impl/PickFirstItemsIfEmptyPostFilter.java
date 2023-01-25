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

package com.percussion.soln.p13n.delivery.snipfilter.impl;

import java.util.ArrayList;
import java.util.List;

import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;

/**
 * A deprecated filter that will make sure there is always one snippet in the list.
 * Please use {@link ListSizeFilter} instead of this filter.
 * @author adamgent
 *
 */
public class PickFirstItemsIfEmptyPostFilter implements IDeliverySnippetFilter {

    public List<IDeliveryResponseSnippetItem> filter(IDeliverySnippetFilterContext context, List<IDeliveryResponseSnippetItem> items) {
        if (items.isEmpty() && ! context.getResponseSnippetItems().isEmpty()) {
            List<IDeliveryResponseSnippetItem> snippets = new ArrayList<IDeliveryResponseSnippetItem>();
            snippets.add(context.getResponseSnippetItems().get(0));
            return snippets;
        }
        return items;
    }

}
