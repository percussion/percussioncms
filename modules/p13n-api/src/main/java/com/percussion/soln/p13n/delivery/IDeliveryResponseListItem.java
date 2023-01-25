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

package com.percussion.soln.p13n.delivery;

import java.util.List;

/**
 * Represents a delivery list that has been or is going to be filtered on.
 * One might expect to get the snippets items from this list but 
 * because the items in the list change as the {@link IDeliverySnippetFilter}s
 * are running this is not the case.
 * To get the current list of snippets in the list see {@link IDeliverySnippetFilterContext}.
 * 
 * @see IDeliverySnippetFilterContext
 * @see IDeliveryResponseItem
 * @author adamgent
 *
 */
public interface IDeliveryResponseListItem extends IDeliverySegmentedItem {
    
 
    /**
     * Snippet filter ids.
     * @return never <code>null</code>.
     */
    List<String> getSnippetFilterIds();
}
