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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;

/**
 * 
 * Adapter that requires implementations specify if an {@link IDeliveryResponseSnippetItem} should be removed or not.
 * 
 * @author adamgent
 *
 */
public abstract class AbstractRemovalFilter implements IDeliverySnippetFilter {

    public List<IDeliveryResponseSnippetItem> filter(
            IDeliverySnippetFilterContext context,
            List<IDeliveryResponseSnippetItem> items) {
        List<IDeliveryResponseSnippetItem> filteredItems = new ArrayList<IDeliveryResponseSnippetItem>(items);
        Iterator<IDeliveryResponseSnippetItem> it = filteredItems.iterator();
        int i = 0;
        while(it.hasNext()) {
            IDeliveryResponseSnippetItem item = it.next();
            if (removeItem(context, item, i)) 
                it.remove();
            ++i;
        }
        return filteredItems;
    }
    
    /**
     * 
     * @param context
     * @param item
     * @param index
     * @return <code>true</code> to remove, <code>false</code> to keep.
     */
    public abstract boolean removeItem(IDeliverySnippetFilterContext context, IDeliveryResponseSnippetItem item, int index);
    
}
