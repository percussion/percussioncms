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
/**
 * 
 */
package com.percussion.sitemanage.web.service;

import com.percussion.share.data.PSEnumVals;
import com.percussion.share.test.PSDataServiceRestClient;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteCopyRequest;
import com.percussion.sitemanage.data.PSSiteProperties;
import com.percussion.sitemanage.data.PSSitePublishProperties;
import com.percussion.sitemanage.data.PSSiteStatisticsSummary;
import com.percussion.sitemanage.data.PSSiteSummary;

import java.util.List;

public class PSSiteRestClient extends PSDataServiceRestClient<PSSite> {
    public PSSiteRestClient(String url) {
        super(PSSite.class, url, "/Rhythmyx/services/sitemanage/site/");
    }
    
    public List<PSSiteSummary> findAll() {
        return getObjectsFromPath(getPath(), PSSiteSummary.class);
    }
    
    public PSEnumVals getChoices() {
        return getObjectFromPath(concatPath(getPath(), "choices"), PSEnumVals.class);
    }
    
    public PSSiteSummary find(String id) {
        return getObjectFromPath(concatPath(getPath(), "summary", id));
    }

    public PSSiteProperties getProperties(String siteName)
    {
        return getObjectFromPath(concatPath(getPath(), "properties", siteName), PSSiteProperties.class);
    }

    public PSSiteProperties updateProperties(PSSiteProperties props)
    {
        String resp = postObjectToPath(getPath() + "updateProperties", props);
        props = objectFromResponseBody(resp, PSSiteProperties.class);
        return props;
    }
    
    public PSSitePublishProperties getSitePublishProperties(String siteName)
    {
        PSSitePublishProperties objectFromPath = getObjectFromPath(concatPath(getPath(), "publishProperties", siteName),
                PSSitePublishProperties.class);
        return objectFromPath;
    }
    
    public PSSitePublishProperties updateSitePublishProperties(PSSitePublishProperties publishProps)
    {
        String resp = postObjectToPath(getPath() + "updatePublishProperties", publishProps);
        publishProps = objectFromResponseBody(resp, PSSitePublishProperties.class);
        return publishProps;
    }
    
    public PSSite copy(PSSiteCopyRequest req)
    {
        String resp = postObjectToPath(getPath() + "copy", req);
        return objectFromResponseBody(resp, PSSite.class);
    }
    
    @Override
    public String POST(String path, String body, String contentType) {
        return super.POST(path, body, contentType);
    }

    public String deleteSite(String id)
    {
        return super.DELETE(getPath() + id);
    }
    
    public long importSiteFromUrlAsync(PSSite site)
    {
        String response = postObjectToPath(concatPath(getPath(), "importFromUrlAsync"), site);
        return Long.parseLong(response);
    }
    
    public PSSite getImportedSite(Long jobId)
    {
        return getObjectFromPath(concatPath(getPath(), "getImportedSite", jobId.toString()), PSSite.class);
    }
    
    public PSSiteStatisticsSummary getSiteStatistics(String siteId)
    {
        return getObjectFromPath(concatPath(getPath(), "statistics", siteId), PSSiteStatisticsSummary.class);
    }

}
