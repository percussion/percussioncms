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

public interface ISiteAdaptor {


    /***
     * Find all sites.
     * @return SiteList
     */
    SiteList findAllSites();

    /***
     * Save a site
     * @param site
     */
    void saveSite(Site site);

    /***
     *
     * @param name
     * @return
     */
    Site findByName(String name);

    /***
     * find By Guid
     * @param guid
     * @return
     */
    Site findByGuid(String guid);

    /***
     * Delete the site
     * @param site
     */
    void deleteSite(Site site);

    /***
     * Create a new Site
     * @return
     */
    Site createSite();


}
