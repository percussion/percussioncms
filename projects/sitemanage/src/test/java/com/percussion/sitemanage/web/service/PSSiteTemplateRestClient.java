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
