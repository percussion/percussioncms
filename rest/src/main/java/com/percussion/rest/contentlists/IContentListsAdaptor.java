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
