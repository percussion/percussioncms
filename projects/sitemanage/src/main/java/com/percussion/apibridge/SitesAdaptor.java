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


import com.percussion.cms.IPSConstants;
import com.percussion.error.PSException;
import com.percussion.rest.sites.ISiteAdaptor;
import com.percussion.rest.sites.Site;
import com.percussion.rest.sites.SiteList;
import com.percussion.rest.sites.SiteMapOptions;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.data.PSSiteSection;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.sitemanage.service.IPSSiteSectionService;
import com.percussion.util.PSSiteManageBean;
import com.percussion.webservices.publishing.IPSPublishingWs;
import com.redfin.sitemapgenerator.WebSitemapGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.net.MalformedURLException;
import java.util.List;

@PSSiteManageBean
@Lazy
public class SitesAdaptor implements ISiteAdaptor {
    private static final Logger log = LogManager.getLogger(IPSConstants.API_LOG);


    @Autowired
    IPSPublishingWs publishingWs;

    @Autowired
    IPSSiteDataService siteDataService;

    @Autowired
    IPSSiteSectionService siteSectionService;


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

        List<PSSiteSummary> sites = siteDataService.findAll();

        return ApiUtils.convertSiteSummaryList(sites);
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

    /**
     * Returns a sitemap with the specified options.
     *
     * @param siteName
     * @param options  Options to configure the sitemap
     * @return
     */
    @Override
    public String getSiteMap(String siteName, SiteMapOptions options) throws PSException {
        String ret = "";
        try {
            PSSiteSummary site = siteDataService.findByName(siteName);
            PSSiteSection root = siteSectionService.loadRoot(siteName);

            String rootPath = root.getFolderPath();
            WebSitemapGenerator wsg = new WebSitemapGenerator(site.getBaseUrl());


            ret = wsg.writeSitemapsWithIndexAsString();
        } catch (IPSDataService.DataServiceLoadException | PSValidationException | MalformedURLException e) {
            throw new PSException(e);
        }

        return ret;
    }
}
