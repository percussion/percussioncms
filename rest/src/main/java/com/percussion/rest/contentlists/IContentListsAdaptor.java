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

package com.percussion.rest.contentlists;

import com.percussion.rest.extensions.Extension;

import java.util.List;

public interface IContentListsAdaptor {
	
	/***
	 * Get a list of available ContentListGenerators on the system.
	 * @return A list of ContentListGenerators
	 */
	public List<Extension> getContentListGenerators();
	
	/***
	 * Get a list of available TemplateExpanders on the system.
	 * @return
	 */
	public List<Extension> getTemplateExpanders();
	
	/***
	 * Get  a list of content lists for the specified edition.
	 * @param editionId
	 * @return
	 */
	public List<ContentList> getContentListsByEditionId(long editionId);
	
	/***
	 * Get a list of content lists that are currently unused.
	 * @return
	 */
	public List<ContentList> getUnusedContentLists(long siteId);
	
	/***
	 * Create or update a ContentList
	 * @param cl
	 * @return The updated content list
	 */
	public ContentList createOrUpdateContentList(ContentList cl);
	
	/***
	 * Delete the specified content list.
	 * @param id
	 */
	public void deleteContentList(long id);

	/***
	 * Gets a content list by id.
	 * @param id  Unique identifier for a content list.
	 * @return The content list
	 */
	public ContentList getContentListById(long id);
}
