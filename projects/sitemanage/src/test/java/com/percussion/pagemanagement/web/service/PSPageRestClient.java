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