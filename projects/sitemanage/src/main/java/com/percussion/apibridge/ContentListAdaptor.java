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

package com.percussion.apibridge;

import com.percussion.rest.contentlists.ContentList;
import com.percussion.rest.contentlists.IContentListsAdaptor;
import com.percussion.rest.extensions.Extension;
import com.percussion.util.PSSiteManageBean;

import java.util.List;

@PSSiteManageBean
public class ContentListAdaptor implements IContentListsAdaptor {


    public ContentListAdaptor() {
    }

    /***
     * Get a list of available ContentListGenerators on the system.
     * @return A list of ContentListGenerators
     */
    @Override
    public List<Extension> getContentListGenerators() {
        return null;
    }

    /***
     * Get a list of available TemplateExpanders on the system.
     * @return
     */
    @Override
    public List<Extension> getTemplateExpanders() {
        return null;
    }

    /***
     * Get  a list of content lists for the specified edition.
     * @param editionId
     * @return
     */
    @Override
    public List<ContentList> getContentListsByEditionId(long editionId) {
        return null;
    }

    /***
     * Get a list of content lists that are currently unused.
     * @return
     * @param siteId
     */
    @Override
    public List<ContentList> getUnusedContentLists(long siteId) {
        return null;
    }

    /***
     * Create or update a ContentList
     * @param cl
     * @return The updated content list
     */
    @Override
    public ContentList createOrUpdateContentList(ContentList cl) {
        return null;
    }

    /***
     * Delete the specified content list.
     * @param id
     */
    @Override
    public void deleteContentList(long id) {

    }

    /***
     * Gets a content list by id.
     * @param id  Unique identifier for a content list.
     * @return The content list
     */
    @Override
    public ContentList getContentListById(long id) {
        return null;
    }
}
