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
package com.percussion.searchmanagement;

import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.searchmanagement.data.PSSearchCriteria;
import com.percussion.searchmanagement.service.IPSSearchService;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.test.PSServletTestCase;
import com.percussion.ui.service.IPSListViewHelper;
import com.percussion.ui.service.IPSListViewProcessor;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Map;

/**
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSSearchPagesByFieldsTest extends PSServletTestCase
{
    private PSSiteDataServletTestCaseFixture fixture;
    
    IPSSearchService searchService;
    
    String homePagePath;
    
    public IPSSearchService getSearchService()
    {
        return searchService;
    }

    public void setSearchService(IPSSearchService searchService)
    {
        this.searchService = searchService;
    }

    @Override
    public void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.setUp();
        homePagePath = fixture.site1.getFolderPath() + "/index.html";
        fixture.pageCleaner.add(homePagePath);
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception
    {
        fixture.tearDown();
    }
    
    public void testSearchForPage() throws Exception
    {
        PSPage homePage = fixture.getPageService().findPageByPath(homePagePath);
        assertNotNull(homePage);
      
        PSSearchCriteria criteria = new PSSearchCriteria();
        criteria.setFolderPath(fixture.site1.getFolderPath());
        Map<String, String> searchFields = new HashMap<String, String>();
        searchFields.put("sys_contentlastmodifier", "admin1");
        searchFields.put("sys_workflowid", "6");
        searchFields.put("sys_contentstateid", "1");
        searchFields.put("templateid", homePage.getTemplateId());
        
        
        criteria.setSearchFields(searchFields);
        criteria.setFormatId(-1);
        criteria.setMaxResults(null);
        criteria.setQuery("index.html");
        
        PSPagedItemList result = searchService.search(criteria);
        assertNotNull(result);
        Integer one = new Integer(1);
        //Items not indexed for 15s
        Thread.sleep(30000);
        result = searchService.search(criteria);
        assertNotNull(result);            
      
        assertEquals(one, result.getChildrenCount());
        assertEquals(1, result.getChildrenInPage().size());
        PSPathItem item = result.getChildrenInPage().get(0);
        assertEquals(homePage.getId(), item.getId());
        Map<String, String> displayProps = item.getDisplayProperties();
        
        assertNotNull(displayProps.get(IPSListViewHelper.CONTENT_LAST_MODIFIED_DATE_NAME));
        assertNotNull(displayProps.get(IPSListViewHelper.STATE_NAME));
        assertNotNull(displayProps.get(IPSListViewHelper.TITLE_NAME));
        assertNotNull(displayProps.get(IPSListViewHelper.CONTENT_LAST_MODIFIER_NAME));
        assertNotNull(displayProps.get(IPSListViewHelper.CONTENT_LAST_MODIFIED_DATE_NAME));
        assertNotNull(displayProps.get(IPSListViewProcessor.TEMPLATE_NAME));
        
        criteria.setQuery("");
        result = searchService.search(criteria);
        assertNotNull(result);
        assertTrue(result.getChildrenCount() >= 1);
    }
}
