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

package com.percussion.delivery.metadata;

import java.util.Collection;
import java.util.List;

import com.percussion.delivery.metadata.rdbms.impl.PSDbBlogPostVisit;

/**
 *
 */
public interface IPSBlogPostVisitDao
{

    /**
     * Deletes multiple page visit entries.
     * 
     * @param pagepaths collection of page path strings whose visits needs to be deleted. 
     * Cannot be <code>null</code> may be empty.         
     */
    public void delete(Collection<String> pagepaths);

    /**
     * Deletes a single page visit entry.
     * 
     * @param pagepath The page path of the page visit entry that should be
     *            deleted. Cannot be <code>null</code> nor empty.
     * @return <code>true</code> if a delete operation actually occurred.            
     */
    public boolean delete(String pagepath);

    /**
     * Saves multiple page path entries.
     * 
     * @param visits collection of entries to be saved, cannot be
     *            <code>null</code>, may be empty.
     */
    public void save(Collection<IPSBlogPostVisit> visits);

    /**
     * Saves a single page visit entry.
     * 
     * @param visit A {@link IPSBlogPostVisit} instance to store in the
     *            database. Cannot be <code>null</code>.
     */
    public void save(IPSBlogPostVisit visit);

    /**
     * Returns list of most visited pages within the supplied number of days, limits to the supplied limit amount.
     * 
     * @param sectionPath the path of the page
     * @param days the number of days to filter the query by
     * @param limit the limit of items on the query
     * @param sortOrder sort the query by asc or desc
     * @return A list String pagepaths. Never <code>null</code>, may be empty.
     */
    public List<String> getTopVisitedPages(String sectionPath, int days, int limit, String sortOrder);

    /**
     * Finds a page visit according to the given pagepath.
     * 
     * @param pagepath The pagepath of the page visit entry to return. Cannot be
     *            <code>null</code> nor empty.
     * @return The page visit entry with the pagepath specified. If no entry is
     *         found, null is returned.
     */
    public List<PSDbBlogPostVisit> findBlogPostVisit(String pagepath);

    public void updatePostsAfterSiteRename(String prevSiteName,
                                           String newSiteName) throws Exception;
    
}
