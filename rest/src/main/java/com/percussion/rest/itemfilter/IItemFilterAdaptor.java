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

package com.percussion.rest.itemfilter;

import com.percussion.rest.Guid;
import com.percussion.services.error.PSNotFoundException;

import java.util.List;

public interface IItemFilterAdaptor {

	/***
	 * Get a list of the ItemFilters available on the system populated with rules and parameters.
	 * @return A list of item filters
	 */
	public List<ItemFilter> getItemFilters();

	/***
	 * Update or create an ItemFilter
	 * @param filter  The filter to update or create.  
	 * @return The updated ItemFilter.
	 */
	public ItemFilter updateOrCreateItemFilter(ItemFilter filter);
	
	/***
	 * Delete the specified item filter.
	 * @param itemFilterId A valid ItemFilter id.  Filter must not be associated with any ContentLists or it won't be deleted.
	 */
	public void deleteItemFilter(Guid itemFilterId) throws PSNotFoundException;
	
	/***
	 * Get a single ItemFilter by id.
	 * @param itemFilterId  A Valid ItemFilter id
	 * @return The ItemFilter
	 */
	public ItemFilter getItemFilter(Guid itemFilterId) throws PSNotFoundException;
	
}
