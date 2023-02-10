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

package com.percussion.category.dao;

import com.percussion.utils.guid.IPSGuid;

import java.util.List;
import java.util.Set;

/**
 * @author chriswright
 */
public interface IPSCategoryDao {
    /**
     * @param ids     the category IDs which will be used to remove from
     *                the ct_page child categories table.
     * @param pageIds IDs corresponding to Page Ids that use the categories
     *                that can be used to evict the pages
     *                using the contained category IDs from the
     *                hibernate cache. These should be passed in along with
     *                the category IDs so they can be evicted from cache. May not
     *                be <code>null</code> or <code>empty</code>.
     *                <p>
     *                {@link #getPageIdsFromCategoryIds(Set)} can be used to obtain the page IDs first.
     */
    void delete(Set<String> ids, List<IPSGuid> pageIds);

    /**
     * @param ids the Category IDs which can be used to retrieve the corresponding
     *            page IDs that use the category.  May return <code>empty</code>,
     *            never <code>null</code>.
     * @return a Set of IPSGuids representing the page IDs provided by the input.
     */
    List<Integer> getPageIdsFromCategoryIds(Set<String> ids);
}
