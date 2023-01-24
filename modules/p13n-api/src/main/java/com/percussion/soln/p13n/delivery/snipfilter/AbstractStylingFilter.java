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

package com.percussion.soln.p13n.delivery.snipfilter;

import java.util.Iterator;
import java.util.List;

import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;

/**
 * Adapter that requires implementations to {@link #calculateStyle(IDeliverySnippetFilterContext, IDeliveryResponseSnippetItem, int)}.
 * The style is in many cases a CSS selector that is applied on the browser side.
 * @author adamgent
 *
 */
public abstract class AbstractStylingFilter implements IDeliverySnippetFilter {

    public List<IDeliveryResponseSnippetItem> filter(
            IDeliverySnippetFilterContext context,
            List<IDeliveryResponseSnippetItem> items) {
        Iterator<IDeliveryResponseSnippetItem> it = items.iterator();
        int i = 0;
        while(it.hasNext()) {
            IDeliveryResponseSnippetItem item = it.next();
            item.setStyle(calculateStyle(context, item, i));
        }
        return items;
    }
    
    public abstract String calculateStyle(
            IDeliverySnippetFilterContext context, 
            IDeliveryResponseSnippetItem item, 
            int index);

}
