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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
