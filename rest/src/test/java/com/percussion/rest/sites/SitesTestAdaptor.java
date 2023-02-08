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

package com.percussion.rest.sites;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class SitesTestAdaptor implements ISiteAdaptor {
    /***
     * Find all sites.
     * @return SiteList
     */
    @Override
    public SiteList findAllSites() {
        return null;
    }

    /***
     * Save a site
     * @param site
     */
    @Override
    public void saveSite(Site site) {

    }

    /***
     *
     * @param name
     * @return
     */
    @Override
    public Site findByName(String name) {
        return null;
    }

    /***
     * find By Guid
     * @param guid
     * @return
     */
    @Override
    public Site findByGuid(String guid) {
        return null;
    }

    /***
     * Delete the site
     * @param site
     */
    @Override
    public void deleteSite(Site site) {

    }

    /***
     * Create a new Site
     * @return
     */
    @Override
    public Site createSite() {
        return null;
    }

}
