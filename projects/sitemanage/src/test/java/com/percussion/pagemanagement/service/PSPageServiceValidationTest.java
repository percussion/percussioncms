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
package com.percussion.pagemanagement.service;

import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.dao.IPSPageDaoHelper;
import com.percussion.pagemanagement.dao.IPSTemplateDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSRegionWidgets;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.service.impl.PSPageService;
import com.percussion.recent.service.rest.IPSRecentService;
import com.percussion.recycle.service.IPSRecycleService;
import com.percussion.share.dao.IPSContentItemDao;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.service.IPSDataItemSummaryService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSPropertiesValidationException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.publishing.IPSPublishingWs;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;

/**
 * Scenario description:
 * Test page validation. 
 * @author adamgent, Oct 15, 2009
 */
@RunWith(JMock.class)
@Ignore
@Category(IntegrationTest.class)
public class PSPageServiceValidationTest
{

    private Mockery context = new JUnit4Mockery();

    private PSPageService sut;

    private IPSContentWs contentWs;
    private IPSFolderHelper folderHelperWs;
    private IPSContentDesignWs contentDesignWs; 
    private IPSIdMapper idMapper;

    private IPSPageDao pageDao;
    private IPSPageDaoHelper pageDaoHelper;
    
    private IPSTemplateDao templateDao;
    
    private IPSWidgetAssetRelationshipService widgetAssetRelationshipService;
    private IPSWidgetService widgetService;
    
    private IPSItemWorkflowService itemWorkflowService;
    
    private IPSPublishingWs publishingWs; 
    private IPSContentItemDao contentItemDao;
    
    private IPSiteDao siteDao;
    
    private IPSPageTemplateService pageTemplateService;
    
    private IPSRecentService recentService;

    private IPSDataItemSummaryService dataItemSummaryService;

    private IPSRecycleService recycleService;


    @Before
    public void setUp() throws Exception
    {
        folderHelperWs = context.mock(IPSFolderHelper.class);
        contentWs = context.mock(IPSContentWs.class);
        contentDesignWs = context.mock(IPSContentDesignWs.class);
        pageDao = context.mock(IPSPageDao.class);
        pageDaoHelper = context.mock(IPSPageDaoHelper.class);
        idMapper = context.mock(IPSIdMapper.class);
        widgetAssetRelationshipService = context.mock(IPSWidgetAssetRelationshipService.class);
        widgetService = context.mock(IPSWidgetService.class);
        itemWorkflowService = context.mock(IPSItemWorkflowService.class);   
        publishingWs        = context.mock(IPSPublishingWs.class);
        contentItemDao = context.mock(IPSContentItemDao.class);
        templateDao = context.mock(IPSTemplateDao.class);
        siteDao = context.mock(IPSiteDao.class);
        pageTemplateService = context.mock(IPSPageTemplateService.class);
        recentService = context.mock(IPSRecentService.class);
        recycleService = context.mock(IPSRecycleService.class);
        dataItemSummaryService = context.mock(IPSDataItemSummaryService.class);
        
        //IPSContentWs contentWs, IPSContentDesignWs contentDesignWs, IPSSystemWs systemWs, IPSIdMapper idMapper
        sut = new PSPageService(folderHelperWs, contentDesignWs,contentWs,idMapper, pageDao, widgetAssetRelationshipService,
                widgetService, itemWorkflowService, publishingWs, pageDaoHelper, contentItemDao, templateDao,
                siteDao, pageTemplateService, recycleService, dataItemSummaryService);
        sut.setRecentService(recentService);
    }
    
    @Test
    public void shouldValidateWithOutFolderPathIfIdIsGiven() throws PSValidationException {
        /*
         * Given: a page that has been created.
         */
        PSPage page = new PSPage();
        page.setId("1");
        page.setName("name");
        page.setTemplateId("2");
        page.setTitle("title");
        page.setLinkTitle("dummy");

        /* 
         * Expect: no service to check folder path.
         */

        context.checking(new Expectations() {{
        }});

        /*
         * When: 
         */

        PSValidationErrors errors = sut.validate(page);
        
        /*
         * Then: we should have a validation errors object that has no errors.
         */
        
        assertNotNull(errors);

    }
    
    @Test
    public void shouldValidateWithFolderPathIfIdIsNotGiven() throws Exception
    {
        /*
         * Given: given a page that has not been created.
         */
        PSPage page = new PSPage();
        page.setFolderPath("//Sites/Blah");
        page.setName("name");
        page.setTemplateId("2");
        page.setTitle("title");
        page.setLinkTitle("dummy");

        /* 
         * Expect: to use the collaborat to find if the item exist with the folder path.
         */
        context.checking(new Expectations() {{
            one(contentWs).getIdByPath("//Sites/Blah/" + "name");
            will(returnValue(null));
        }});

        /*
         * When: we validate
         */
        PSValidationErrors errors = sut.validate(page);
        /*
         * Then: we should not throw an exception.
         */
        
        assertNotNull(errors);

    }
    
    
    @Test(expected=PSBeanValidationException.class)
    public void shouldValidateWithFolderPathIfIdIsNotGivenAndFailIfItemExists() throws Exception
    {
        /*
         * Given: given a page that has not been created.
         */
        PSPage page = new PSPage();
        page.setFolderPath("//Sites/Blah");
        page.setName("name");
        page.setTemplateId("2");
        page.setTitle("title");
        page.setLinkTitle("dummy");

        /* 
         * Expect: to use the collaborat to find if the item exist with the folder path.
         */
        final IPSGuid guid = context.mock(IPSGuid.class); 
        context.checking(new Expectations() {{
            one(contentWs).getIdByPath("//Sites/Blah/" + "name");
            will(returnValue(guid));
        }});

        /*
         * When: we validate
         */
        sut.validate(page);
        /*
         * Then: we should throw an exception.
         */

    }
    
    @Test(expected=PSBeanValidationException.class)
    public void shouldInvalidateWithMissingBranches() throws Exception
    {
        
        PSPage page = new PSPage();
        page.setId("1");
        page.setName("Title");
        page.setTemplateId("2");
        page.setTitle("title");
        page.setRegionBranches(null);
        page.setLinkTitle("dummy");
        sut.validate(page);
        
    }
    
    @Test(expected=PSBeanValidationException.class)
    public void shouldInValidateBranchOverridesWithNoRegionId() throws PSValidationException {
        /*
         * Given: a page that has been created
         * with page branches.
         */
        PSPage page = new PSPage();
        page.setId("1");
        page.setName("name");
        page.setTemplateId("2");
        page.setTitle("title");
        page.setLinkTitle("dummy");

        PSRegionBranches branches = new PSRegionBranches();
        PSRegion region = new PSRegion();
        region.setRegionId(null);
        branches.setRegions(asList(region));
        
        page.setRegionBranches(branches);
        
        /* 
         * Expect: no service calls.
         */
        
        /*
         * When: we validate
         */
        sut.validate(page);
        
    }
    
    @Test(expected=PSBeanValidationException.class)
    public void shouldFailIfDuplicateRegionWidgetAssocations() throws PSValidationException {
        /*
         * Given: a page that has been created
         * with page branches.
         */
        PSPage page = new PSPage();
        page.setId("1");
        page.setName("name");
        page.setTemplateId("2");
        page.setTitle("title");
        page.setLinkTitle("dummy");

        PSRegionBranches branches = new PSRegionBranches();
        Set<PSRegionWidgets> regionWidgets = new HashSet<PSRegionWidgets>();
        PSRegionWidgets a = new PSRegionWidgets();
        a.setRegionId("a");
        
        PSRegionWidgets b = new PSRegionWidgets();
        //same id
        b.setRegionId("a");
        List<PSWidgetItem> items = asList(new PSWidgetItem());
        b.setWidgetItems(items);
        
        regionWidgets.add(a);
        regionWidgets.add(b);
        
        branches.setRegionWidgetAssociations(regionWidgets);
        
        page.setRegionBranches(branches);
        
        /* 
         * Expect: to validate our given widget item from above.
         * we don't really care what happens here so long as the widget item is valid
         */
        context.checking(new Expectations() {{
            one(widgetService).validateWidgetItem(with(any(PSWidgetItem.class)));
            will(returnValue(new PSPropertiesValidationException(new PSWidgetItem(),"Blah")));
        }});
        
        /*
         * When: we validate
         */
        sut.validate(page);
        
    }
    
    
    @Test(expected=PSBeanValidationException.class)
    public void shouldFailIfDuplicateRegionIds() throws PSValidationException {
        /*
         * Given: a page that has been created
         * with page branches.
         */
        PSPage page = new PSPage();
        page.setId("1");
        page.setName("name");
        page.setTemplateId("2");
        page.setTitle("title");
        page.setLinkTitle("dummy");

        PSRegionBranches branches = new PSRegionBranches();
        PSRegion regionA = new PSRegion();
        regionA.setRegionId("a");
        PSRegion regionB = new PSRegion();
        regionB.setRegionId("a");
        regionB.setStartTag("differentthena");
        
        branches.setRegions(asList(regionA,regionB));
        
        page.setRegionBranches(branches);
        
        /* 
         * Expect: no service calls
         */
  
        
        /*
         * When: we validate
         */
        sut.validate(page);
        
    }
    
    

}
