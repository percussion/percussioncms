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

package com.percussion.queue;

import com.percussion.queue.impl.PSSiteQueue;
import com.percussion.sitemanage.data.PSSite;

import java.util.List;

public interface IPSPageImportQueue
{

    void addCatalogedPageIds(PSSite s, String userAgent, List<Integer> ids);
    
    List<Integer> getImportingPageIds(Long siteId);
    
    List<Integer> getCatalogedPageIds(Long siteId);
    
    List<Integer> getImportedPageIds(Long siteId);
    
    void addImportedId(Long siteId, Integer id);
    /**
     * Remove the specified page from the specified site
     * @param siteName the name of the site, not blank.
     * @param pageId the imported page ID, not blank.
     */
    void removeImportPage(String siteName, String pageId);
    
    /**
     * Gets the page IDs that are cached for the specified site.
     * 
     * @param siteId the ID of the site, not <code>null</code>.
     * 
     * @return the page IDs, which are cloned from the cached info, never <code>null</code>.
     */
    PSSiteQueue getPageIds(Long siteId);

    public void dirtySiteQueue(Long siteId);
}
