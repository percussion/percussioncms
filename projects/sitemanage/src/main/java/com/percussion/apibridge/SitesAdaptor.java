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

package com.percussion.apibridge;


import com.percussion.rest.sites.ISiteAdaptor;
import com.percussion.rest.sites.Site;
import com.percussion.rest.sites.SiteList;
import com.percussion.util.PSSiteManageBean;
import com.percussion.webservices.publishing.IPSPublishingWs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@PSSiteManageBean
@Lazy
public class SitesAdaptor implements ISiteAdaptor {
    private static final Logger log = LogManager.getLogger(SitesAdaptor.class);


    @Autowired
    IPSPublishingWs publishingWs;


    /***
     * ctor
     */
    public SitesAdaptor(){}

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
