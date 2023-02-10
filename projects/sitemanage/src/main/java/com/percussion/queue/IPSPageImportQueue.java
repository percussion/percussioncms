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
