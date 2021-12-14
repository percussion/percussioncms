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

import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.share.test.PSObjectRestClient;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.PSSiteTemplates;

import java.util.List;

public class PSSiteTemplateRestClient extends PSObjectRestClient {
    private String path = "/Rhythmyx/services/sitemanage/sitetemplates/";
    
    public List<PSTemplateSummary> save(PSSiteTemplates siteTemplates) {
        String response = postObjectToPath(path, siteTemplates);
        return objectsFromResponseBody(response, PSTemplateSummary.class);
    }
    
    public PSTemplateSummary createTemplateFromUrl(PSSiteTemplates siteTemplates) {
        String response = postObjectToPath(concatPath(path,"createFromUrl"), siteTemplates);
        return objectFromResponseBody(response, PSTemplateSummary.class);
    }
    
    public long createTemplateFromUrlAsync(PSSiteTemplates siteTemplates)
    {
        String response = postObjectToPath(concatPath(path, "createFromUrlAsync"), siteTemplates);
        return Long.parseLong(response);
    }
    
    public PSTemplateSummary getImportedTemplate(Long jobId)
    {
        return getObjectFromPath(concatPath(path, "getImportedTemplate", jobId.toString()), PSTemplateSummary.class);
    }
    
    public List<PSTemplateSummary> findTemplatesWithNoSite() {
        return getObjectsFromPath(concatPath(path,"nosites"), PSTemplateSummary.class);
    }
    
    public List<PSSiteSummary> findSitesByTemplate(String id) {
        return getObjectsFromPath(concatPath(path,"sites",id), PSSiteSummary.class);
    }
    
    public List<PSTemplateSummary> findTemplatesBySite(String id) {
        return getObjectsFromPath(concatPath(path,"templates",id), PSTemplateSummary.class);
    }
    
    public List<PSTemplateSummary> findTemplatesBySite(String id, String widgetId) {
        return getObjectsFromPath(concatPath(path,"templates",id,widgetId), PSTemplateSummary.class);
    }
}
