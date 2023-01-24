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

package com.percussion.apibridge;

import com.percussion.rest.contentlists.ContentList;
import com.percussion.rest.contentlists.IContentListsAdaptor;
import com.percussion.rest.extensions.Extension;
import com.percussion.util.PSSiteManageBean;

import java.util.Collections;
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
        return Collections.emptyList();
    }

    /***
     * Get a list of available TemplateExpanders on the system.
     * @return
     */
    @Override
    public List<Extension> getTemplateExpanders() {
        return Collections.emptyList();
    }

    /***
     * Get  a list of content lists for the specified edition.
     * @param editionId
     * @return
     */
    @Override
    public List<ContentList> getContentListsByEditionId(long editionId) {
        return Collections.emptyList();
    }

    /***
     * Get a list of content lists that are currently unused.
     * @return
     * @param siteId
     */
    @Override
    public List<ContentList> getUnusedContentLists(long siteId) {
        return Collections.emptyList();
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
