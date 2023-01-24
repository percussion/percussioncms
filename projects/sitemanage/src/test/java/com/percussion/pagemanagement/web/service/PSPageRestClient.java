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
package com.percussion.pagemanagement.web.service;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.apache.commons.lang.Validate.notEmpty;

import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSSEOStatistics;
import com.percussion.pathmanagement.data.PSItemByWfStateRequest;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.test.PSDataServiceRestClient;
import com.percussion.share.test.PSRestTestCase;

import java.util.List;

public class PSPageRestClient extends PSDataServiceRestClient<PSPage>
{
    
    public PSPageRestClient(String baseUrl)
    {
        super(PSPage.class, baseUrl, "/Rhythmyx/services/pagemanagement/page/");
    }


    public PSPage load(String id)
    {
        return get(id);
    }
    
    public String getPageEditUrl(String id)
    {
        return GET(concatPath(getPath(), "pageEditUrl", id));
    }
    
    public String getPageViewUrl(String id)
    {
        return GET(concatPath(getPath(), "pageViewUrl", id));
    }
    
    public PSPage findPageByFullFolderPath(String fullPath) {
        notEmpty(fullPath, "fullPath");
        fullPath = removeStart(fullPath, "//");
        return getObjectFromPath(concatPath(getPath(), "folderpath", fullPath));
    }
    
    public void forceDelete(String pageId)
    {
        GET(concatPath(getPath(), "/forceDelete", pageId));
    }

    public PSNoContent validateDelete(String pageId)
    {
        return getObjectFromPath(concatPath(getPath(), "/validateDelete", pageId), PSNoContent.class);
    }
    
    public List<PSSEOStatistics> findNonSEOPages(PSItemByWfStateRequest request)
    {
        return postObjectToPathAndGetObjects(concatPath(getPath(), "/nonSEOPages"), request, PSSEOStatistics.class);
    }
    
    public PSPage createPage(String name, String folderPath) throws Exception
    {
        String templateId = getTemplateClient().getContentOnlyTemplateId();
        
        PSPage pageNew = new PSPage();
        pageNew.setName(name);
        pageNew.setTitle("title");
        pageNew.setFolderPath(folderPath);
        pageNew.setTemplateId(templateId);
        pageNew.setLinkTitle("dummy");
        PSRegionBranches br = new PSRegionBranches();
        PSRegion region = new PSRegion();
        region.setRegionId("test");
        br.setRegions(asList(region));
        pageNew.setRegionBranches(br);
              
        return save(pageNew);
    }
    
    private PSTemplateServiceClient getTemplateClient() throws Exception
    {
        PSTemplateServiceClient client = new PSTemplateServiceClient(getUrl());
        PSRestTestCase.setupClient(client);
        return client;
    }
}
