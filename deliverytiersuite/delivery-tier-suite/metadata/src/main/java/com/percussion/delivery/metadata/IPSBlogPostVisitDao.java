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
