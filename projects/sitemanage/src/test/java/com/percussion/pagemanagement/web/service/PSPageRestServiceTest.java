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

package com.percussion.pagemanagement.web.service;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetResourceType;
import com.percussion.assetmanagement.web.service.PSAssetServiceRestClient;
import com.percussion.cms.IPSConstants;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.web.service.PSItemWorkflowServiceRestClient;
import com.percussion.pagemanagement.data.PSNonSEOPagesRequest;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSSEOStatistics;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.data.PSRegionNode.PSRegionOwnerType;
import com.percussion.pagemanagement.data.PSSEOStatistics.SEO_ISSUE;
import com.percussion.pagemanagement.data.PSSEOStatistics.SEO_SEVERITY;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.share.test.PSTestDataCleaner;
import com.percussion.share.test.PSRestClient.RestClientException;

import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/**
 * Saves a page with the rest service.
 * @author adamgent
 *
 */
@Category(IntegrationTest.class)
public class PSPageRestServiceTest extends PSRestTestCase<PSPageRestClient>
{
    private static PSTestSiteData testSiteData;
    
    @BeforeClass
    public static void setUp() throws Exception
    {
        testSiteData = new PSTestSiteData();
        testSiteData.setUp();
    }
    
    PSTestDataCleaner<String> pageCleaner = new PSTestDataCleaner<String>()
    {

        @Override
        protected void clean(String id) throws Exception
        {
            restClient.delete(id);
        }
    };

    PSTestDataCleaner<String> templateCleaner = new PSTestDataCleaner<String>()
    {

        @Override
        protected void clean(String id) throws Exception
        {
            getTemplateClient().deleteTemplate(id);
        }
    };
    {
        templateCleaner.setFailOnErrors(true);
        pageCleaner.setFailOnErrors(true);
    }

    @Override
    protected PSPageRestClient getRestClient(String url)
    {
        return new PSPageRestClient(url);
    }

    @Test
    public void testCreate() throws Exception
    {
        PSPage rvalue = restClient.createPage("Test", testSiteData.site1.getFolderPath());
        assertNotNull(rvalue);
        assertNotNull(rvalue.getId());
        String pageId = rvalue.getId();
        pageCleaner.add(pageId);
        templateCleaner.add(rvalue.getTemplateId());
    }
    
    @Test
    public void testCreateWithRegionAndWidget() throws Exception
    {
        log.debug("testCreateWithRegionAndWidget");
        String templateId = null;
        String pageId = null;
        templateId = getTemplateClient().getContentOnlyTemplateId();
        templateCleaner.add(templateId);

        String folderPath = testSiteData.site1.getFolderPath();
        PSPage pageNew = new PSPage();
        pageNew.setName("testPageNew11");
        pageNew.setTitle("test new page title");
        pageNew.setFolderPath(folderPath);
        pageNew.setTemplateId(templateId);
        pageNew.setLinkTitle("dummy");
        PSRegionBranches br = new PSRegionBranches();
        PSRegion region = new PSRegion();
        region.setOwnerType(PSRegionOwnerType.PAGE);
        region.setRegionId("Test");
        
        PSWidgetItem wi = new PSWidgetItem();
        wi.setDefinitionId("percRawHtml");

        br.setRegionWidgets("Test", asList(wi));
        br.setRegions(asList(region));
        
        pageNew.setRegionBranches(br);
        PSPage rpage = restClient.save(pageNew);
        pageId = rpage.getId();
        pageCleaner.add(pageId);
        
        PSPage page = restClient.load(pageId);
        assertNotNull(page);
        assertNotNull(page.getRegionBranches());
        assertNotNull(page.getRegionBranches().getRegions());
        assertNotNull(page.getRegionBranches().getRegionWidgetAssociations());
        assertFalse("We should have widget assocations", page.getRegionBranches().getRegionWidgetAssociations().isEmpty());
        List<PSRegion> regions = page.getRegionBranches().getRegions();
        assertNotNull(regions);
        assertFalse(regions.isEmpty());
        PSRegion r = regions.get(0);
        assertNotNull(r);
        assertEquals("percRawHtml", 
                page.getRegionBranches().getRegionWidgetAssociations().iterator().next().getWidgetItems().get(0).getDefinitionId());
    }

    @Test
    public void testGetPageEditUrl() throws Exception
    {
        PSPage page = restClient.createPage("Test", testSiteData.site1.getFolderPath());
        pageCleaner.add(page.getId());
        templateCleaner.add(page.getTemplateId());
        
        String url = restClient.getPageEditUrl(page.getId());
        assertFalse(StringUtils.isBlank(url));
        assertTrue(url.indexOf("sys_view=" + IPSConstants.SYS_HIDDEN_FIELDS_VIEW_NAME) != -1);
        assertTrue(url.indexOf("sys_revision") != -1);
        assertTrue(url.indexOf("sys_contentid") != -1);
        assertTrue(url.indexOf("sys_command=edit") != -1);
    }
    
    @Test
    public void testGetPageViewUrl() throws Exception
    {
        PSPage page = restClient.createPage("Test2", testSiteData.site1.getFolderPath());
        pageCleaner.add(page.getId());
        templateCleaner.add(page.getTemplateId());
        
        String url = restClient.getPageViewUrl(page.getId());
        assertFalse(StringUtils.isBlank(url));
        assertTrue(url.indexOf("sys_view=" + IPSConstants.SYS_HIDDEN_FIELDS_VIEW_NAME) != -1);
        assertTrue(url.indexOf("sys_revision") != -1);
        assertTrue(url.indexOf("sys_contentid") != -1);
        assertTrue(url.indexOf("sys_command=preview") != -1);
    }
    
    @Test
    public void testDeletePage() throws Exception
    {
        String templateId = testSiteData.template1.getId();
        String id = testSiteData.createPage("testDeletePage", testSiteData.site1.getFolderPath(), templateId);
                       
        PSItemWorkflowServiceRestClient wfClient = getItemWorkflowServiceRestClient();
        
        // transition item to pending state
        wfClient.transition(id, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        
        // switch to unauthorized user
        restClient.login("author1", "demo");
        
        // should not have access to item
        try
        {
            restClient.delete(id);
            fail("Current user should not be authorized to delete page: " + id);
        }
        catch (RestClientException e)
        {
            assertTrue(e.getStatus() == 400 && e.getResponseBody().contains("page.deleteNotAuthorized"));
        }
        
        // switch to Admin
        restClient.login("admin1", "demo");
        
        // should have access to item
        try
        {
            restClient.delete(id);
            testSiteData.pageCleaner.remove(testSiteData.site1.getFolderPath() + "/testDeletePage");
        }
        catch (RestClientException e)
        {
            fail("Current user should be authorized to delete page: " + id);
        }
        
        // create page
        id = testSiteData.createPage("testDeletePage", testSiteData.site1.getFolderPath(), templateId);
                
        PSAssetServiceRestClient assetClient = getAssetClient();
        
        // add it to a template
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(testSiteData.template2.getId(), 5, "widget5",
                id, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        assetClient.createAssetWidgetRelationship(awRel);
        
        // should not be able to delete page
        try
        {
            restClient.delete(id);
            fail("Should not be able to delete pageasset: " + id + ".  It is used by a template");
        }
        catch (RestClientException e)
        {
            assertTrue(e.getStatus() == 400 && e.getResponseBody().contains("page.deleteTemplates"));
        }
        
        // remove the page from the template
        assetClient.clearAssetWidgetRelationship(awRel);
                    
        // add it to another page
        String pageId = testSiteData.createPage("testDeletePage2", testSiteData.site1.getFolderPath(), templateId);
        awRel = new PSAssetWidgetRelationship(pageId, 5, "widget5", id, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        assetClient.createAssetWidgetRelationship(awRel);
        
        // transition the page to pending
        wfClient.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        
        // should not be able to delete page
        try
        {
            restClient.delete(id);
            fail("Should not be able to delete page: " + id + ".  It is used by an approved page");
        }
        catch (RestClientException e)
        {
            assertTrue(e.getStatus() == 400 && e.getResponseBody().contains("page.deleteApprovedPages"));
        }
        
        // transition the page
        transitionPendingToArchive(wfClient, pageId);
              
        // should be able to delete page
        try
        {
            restClient.delete(id);
            testSiteData.pageCleaner.remove(testSiteData.site1.getFolderPath() + "/testDeletePage");
            
            getItemWorkflowServiceRestClient().checkOut(pageId);
            
            assetClient.clearAssetWidgetRelationship(awRel);
        }
        catch (RestClientException e)
        {
            fail("Page: " + id + " should have been deleted");
        }
    }
    
    @Test
    public void testForceDeletePage() throws Exception
    {
        String templateId = testSiteData.template1.getId();
        String id = testSiteData.createPage("testForceDeletePage", testSiteData.site1.getFolderPath(), templateId);
                
        PSItemWorkflowServiceRestClient wfClient = getItemWorkflowServiceRestClient();
        
        // transition item to pending state
        wfClient.transition(id, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        
        // switch to unauthorized user
        restClient.login("author1", "demo");
        
        // force delete
        try
        {
            restClient.forceDelete(id);
            testSiteData.pageCleaner.remove(id);
        }
        catch (RestClientException e)
        {
            fail("Page: " + id + " should have been deleted");
        }
        
        // switch to admin
        restClient.login("admin1", "demo");
        
        // create page
        id = testSiteData.createPage("testForceDeletePage", testSiteData.site1.getFolderPath(), templateId);
                
        PSAssetServiceRestClient assetClient = getAssetClient();
        
        // add it to a template
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(templateId, 5, "widget5", id, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        assetClient.createAssetWidgetRelationship(awRel);
        
        // force delete
        try
        {
            restClient.forceDelete(id);
            testSiteData.pageCleaner.remove(testSiteData.site1.getFolderPath() + "/testForceDeletePage");
        }
        catch (RestClientException e)
        {
            fail("Page: " + id + " should have been deleted");
        }
        
        // create page
        id = testSiteData.createPage("testForceDeletePage", testSiteData.site1.getFolderPath(), templateId);
                
        // add it to another page
        String pageId = testSiteData.createPage("testForceDeletePage2", testSiteData.site1.getFolderPath(), templateId);
        awRel = new PSAssetWidgetRelationship(pageId, 5, "widget5", id, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        assetClient.createAssetWidgetRelationship(awRel);
        
        // transition the page to pending
        wfClient.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        
        // force delete
        try
        {
            restClient.forceDelete(id);
            testSiteData.pageCleaner.remove(testSiteData.site1.getFolderPath() + "/testForceDeletePage");
        }
        catch (RestClientException e)
        {
            fail("Page: " + id + " should have been deleted");
        }
    }
    
    @Test
    public void testValidateDeletePage() throws Exception
    {
        String templateId = testSiteData.template1.getId();
        String id = testSiteData.createPage("testValidateDeletePage", testSiteData.site1.getFolderPath(), templateId);
                       
        PSItemWorkflowServiceRestClient wfClient = getItemWorkflowServiceRestClient();
        
        // transition item to pending state
        wfClient.transition(id, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        
        // switch to unauthorized user
        restClient.login("author1", "demo");
        
        // should not have access to item
        try
        {
            assertNotNull(restClient.validateDelete(id));
            fail("Current user should not be authorized to delete page: " + id);
        }
        catch (RestClientException e)
        {
            assertTrue(e.getStatus() == 400 && e.getResponseBody().contains("page.deleteNotAuthorized"));
        }
        
        // switch to Admin
        restClient.login("admin1", "demo");
        
        // should have access to item
        try
        {
            assertNotNull(restClient.validateDelete(id));
            restClient.delete(id);
            testSiteData.pageCleaner.remove(testSiteData.site1.getFolderPath() + "/testValidateDeletePage");
        }
        catch (RestClientException e)
        {
            fail("Current user should be authorized to delete page: " + id);
        }
        
        // create page
        id = testSiteData.createPage("testValidateDeletePage", testSiteData.site1.getFolderPath(), templateId);
                
        PSAssetServiceRestClient assetClient = getAssetClient();
        
        // add it to a template
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(testSiteData.template2.getId(), 5, "widget5",
                id, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        assetClient.createAssetWidgetRelationship(awRel);
        
        // should not be able to delete page
        try
        {
            assertNotNull(restClient.validateDelete(id));
            fail("Should not be able to delete pageasset: " + id + ".  It is used by a template");
        }
        catch (RestClientException e)
        {
            assertTrue(e.getStatus() == 400 && e.getResponseBody().contains("page.deleteTemplates"));
        }
        
        // remove the page from the template
        assetClient.clearAssetWidgetRelationship(awRel);
                    
        // add it to another page
        String pageId = testSiteData.createPage("testValidateDeletePage2", testSiteData.site1.getFolderPath(),
                templateId);
        awRel = new PSAssetWidgetRelationship(pageId, 5, "widget5", id, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        assetClient.createAssetWidgetRelationship(awRel);
        
        // transition the page to pending
        wfClient.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        
        // should not be able to delete page
        try
        {
            assertNotNull(restClient.validateDelete(id));
            fail("Should not be able to delete page: " + id + ".  It is used by an approved page");
        }
        catch (RestClientException e)
        {
            assertTrue(e.getStatus() == 400 && e.getResponseBody().contains("page.deleteApprovedPages"));
        }
        
        // transition the page
        transitionPendingToArchive(wfClient, pageId);
        
        // should be able to delete page
        try
        {
            assertNotNull(restClient.validateDelete(id));
            restClient.delete(id);
            testSiteData.pageCleaner.remove(testSiteData.site1.getFolderPath() + "/testValidateDeletePage");
            
            getItemWorkflowServiceRestClient().checkOut(pageId);
            
            assetClient.clearAssetWidgetRelationship(awRel);
        }
        catch (RestClientException e)
        {
            fail("Current user should be able to delete page: " + id);
        }
    }
    
    @Test
    public void testFindNonSEOPages() throws Exception
    {
        // find pages in Draft state
        PSNonSEOPagesRequest request = new PSNonSEOPagesRequest();
        request.setPath(testSiteData.site1.getFolderPath());
        request.setWorkflow("Default Workflow");
        request.setState("Draft");
        request.setSeverity(SEO_SEVERITY.ALL);
        List<PSSEOStatistics> stats = restClient.findNonSEOPages(request);
        
        // should find home page (missing description)
        assertEquals(1, stats.size());
        assertTrue(stats.get(0).getIssues().contains(SEO_ISSUE.MISSING_DESCRIPTION));
        
        // create page
        String pageId = testSiteData.createPage("testFindNonSEOPages", testSiteData.site1.getFolderPath(),
                testSiteData.template1.getId());
        
        stats = restClient.findNonSEOPages(request);
        
        // should find two pages (missing description) 
        assertEquals(2, stats.size());
        assertTrue(stats.get(0).getIssues().contains(SEO_ISSUE.MISSING_DESCRIPTION));
        assertTrue(stats.get(1).getIssues().contains(SEO_ISSUE.MISSING_DESCRIPTION));
        assertFalse(stats.get(1).getIssues().contains(SEO_ISSUE.DEFAULT_TITLE));
        
        // change the page browser title to default, set the description
        PSPage page = restClient.load(pageId);
        page.setTitle(page.getLinkTitle());
        page.setDescription("This is a test page.");
        restClient.save(page);
        
        stats = restClient.findNonSEOPages(request);
        
        // should find two pages
        assertEquals(2, stats.size());
        for (PSSEOStatistics stat : stats)
        {
            if (stat.getPageSummary().getId().equals(pageId))
            {
                assertTrue(stat.getIssues().contains(SEO_ISSUE.DEFAULT_TITLE));
                assertFalse(stat.getIssues().contains(SEO_ISSUE.MISSING_DESCRIPTION));
            }
        }
            
        // test keywords
        String keywordNoSpaces = "Keyword";
        String keywordOneSpace = "Key word";
        String keywordManySpaces = "Key  wo   rd";
                
        request.setKeyword(keywordNoSpaces);
        
        // should not find any pages
        assertTrue(restClient.findNonSEOPages(request).isEmpty());
        
        page.setDescription(page.getDescription() + ' ' + keywordNoSpaces);
        restClient.save(page);
        
        stats = restClient.findNonSEOPages(request);
        
        // should find one page
        assertEquals(1, stats.size());
        assertFalse(stats.get(0).getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_DESCRIPTION));
        assertTrue(stats.get(0).getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_LINK));
        assertTrue(stats.get(0).getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_TITLE));
        
        request.setKeyword(keywordOneSpace);
        
        page.setLinkTitle(keywordOneSpace);
        restClient.save(page);
        
        stats = restClient.findNonSEOPages(request);
        
        // should find one page
        assertEquals(1, stats.size());
        assertFalse(stats.get(0).getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_LINK));
        assertTrue(stats.get(0).getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_DESCRIPTION));
        assertTrue(stats.get(0).getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_TITLE));
        
        request.setKeyword(keywordManySpaces);
        
        page.setTitle("Key wo rd");
        restClient.save(page);
        
        stats = restClient.findNonSEOPages(request);
        
        // should find one page
        assertEquals(1, stats.size());
        assertFalse(stats.get(0).getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_TITLE));
        assertTrue(stats.get(0).getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_DESCRIPTION));
        assertTrue(stats.get(0).getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_LINK));
        
        request.setKeyword(keywordOneSpace);
        
        page.setLinkTitle("Key_word");
        restClient.save(page);
        
        stats = restClient.findNonSEOPages(request);
        
        // should find one page
        assertEquals(1, stats.size());
        assertFalse(stats.get(0).getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_LINK));
        assertTrue(stats.get(0).getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_DESCRIPTION));
        assertTrue(stats.get(0).getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_TITLE));
        
        page.setLinkTitle("Key-word");
        restClient.save(page);
        
        stats = restClient.findNonSEOPages(request);
        
        // should find one page
        assertEquals(1, stats.size());
        assertFalse(stats.get(0).getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_LINK));
        assertTrue(stats.get(0).getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_DESCRIPTION));
        assertTrue(stats.get(0).getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_TITLE));
 
        request.setKeyword(keywordNoSpaces);
        
        // create another page
        String page2Id = testSiteData.createPage("testFindNonSEOPages2", testSiteData.site1.getFolderPath(),
                testSiteData.template1.getId());
        PSPage page2 = restClient.load(page2Id);
        page2.setDescription(keywordNoSpaces);
        page2.setLinkTitle(keywordNoSpaces);
        page2.setTitle(keywordNoSpaces);
        restClient.save(page2);
        
        stats = restClient.findNonSEOPages(request);
        
        // should find two pages
        assertEquals(2, stats.size());
        for (PSSEOStatistics stat : stats)
        {
            if (stat.getPageSummary().getId().equals(page2Id))
            {
                assertFalse(stat.getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_DESCRIPTION));
                assertFalse(stat.getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_LINK));
                assertFalse(stat.getIssues().contains(SEO_ISSUE.MISSING_KEYWORD_TITLE));
            }
        }
        
        String keywords = "an example of multiple keywords";
        request.setKeyword(keywords);
        
        // should not find any pages
        assertTrue(restClient.findNonSEOPages(request).isEmpty());
        
        page.setDescription("This is" + keywords);
        restClient.save(page);
        
        // should not find any pages
        assertTrue(restClient.findNonSEOPages(request).isEmpty());
        
        page.setDescription(keywords + " shown here.");
        restClient.save(page);
        
        // should find one page
        assertEquals(1, restClient.findNonSEOPages(request).size());
        
        page.setLinkTitle("This_is_an_example_of_multiple_keywords");
        restClient.save(page);
        
        // should find one page
        assertEquals(1, restClient.findNonSEOPages(request).size());
        
        page.setLinkTitle("This-is-an-example-of-multiple-keywords-shown-here");
        restClient.save(page);
        
        // should find one page
        assertEquals(1, restClient.findNonSEOPages(request).size());
        
        page.setTitle("This is " + keywords + " shown here.");
        restClient.save(page);
        
        stats = restClient.findNonSEOPages(request);
        
        // should find one page with no issues
        assertEquals(1, stats.size());
        assertTrue(stats.get(0).getIssues().isEmpty());
        
        // test bad workflow
        request.setWorkflow("Bad Workflow");
        try
        {
            restClient.findNonSEOPages(request);
            fail("Unknown workflow was specified");
        }
        catch (RestClientException e)
        {
            assertEquals(500, e.getStatus());
        }
        
        // test severity
        request.setWorkflow("Default Workflow");
        request.setKeyword("");
        
        request.setSeverity(SEO_SEVERITY.MEDIUM);
        assertEquals(1, restClient.findNonSEOPages(request).size());
        
        request.setSeverity(SEO_SEVERITY.MODERATE);
        assertEquals(2, restClient.findNonSEOPages(request).size());
        
        request.setKeyword(keywords);
                        
        page.setTitle(keywords);
        page.setLinkTitle(keywords);
        page.setDescription("");
        restClient.save(page);
        
        request.setSeverity(SEO_SEVERITY.SEVERE);
        assertTrue(restClient.findNonSEOPages(request).isEmpty());

        request.setSeverity(SEO_SEVERITY.HIGH);
        assertTrue(restClient.findNonSEOPages(request).isEmpty());
        
        request.setSeverity(SEO_SEVERITY.MODERATE);
        assertEquals(1, restClient.findNonSEOPages(request).size());
        
        page.setDescription(page.getTitle());
        restClient.save(page);
        
        request.setSeverity(SEO_SEVERITY.MEDIUM);
        assertTrue(restClient.findNonSEOPages(request).isEmpty());

        request.setSeverity(SEO_SEVERITY.MODERATE);
        assertTrue(restClient.findNonSEOPages(request).isEmpty());
        
        request.setSeverity(SEO_SEVERITY.ALL);
        assertEquals(1, restClient.findNonSEOPages(request).size());
        
        page.setTitle(keywords + " foo");
        page.setDescription(keywords);
        restClient.save(page);
        
        request.setSeverity(SEO_SEVERITY.MODERATE);
        assertTrue(restClient.findNonSEOPages(request).isEmpty());
        
        request.setSeverity(SEO_SEVERITY.ALL);
        assertEquals(1, restClient.findNonSEOPages(request).size());
    }
    
    /**
     * Transitions the specified item to the Archive state.
     * 
     * @param wfClient The item workflow service rest client.
     * @param id of the item assumed to be in the Pending state.
     */
    private void transitionPendingToArchive(PSItemWorkflowServiceRestClient wfClient, String id)
    {
        // transition the page to archive
        wfClient.transition(id, IPSItemWorkflowService.TRANSITION_TRIGGER_EDIT);
        wfClient.transition(id, IPSItemWorkflowService.TRANSITION_TRIGGER_ARCHIVE);
    }
    
    @After
    public void tearDown()
    {
        pageCleaner.clean();
        templateCleaner.clean();
    }
    
    @AfterClass
    public static void cleanup() throws Exception
    {
        testSiteData.tearDown();
    }
    
    private PSTemplateServiceClient getTemplateClient() throws Exception
    {
        PSTemplateServiceClient client = new PSTemplateServiceClient(baseUrl);
        setupClient(client);
        return client;
    }
    
    private PSItemWorkflowServiceRestClient getItemWorkflowServiceRestClient() throws Exception
    {
        PSItemWorkflowServiceRestClient client = new PSItemWorkflowServiceRestClient(baseUrl);
        setupClient(client);
        return client;
    }
    
    private PSAssetServiceRestClient getAssetClient() throws Exception
    {
        PSAssetServiceRestClient client = new PSAssetServiceRestClient(baseUrl);
        setupClient(client);
        return client;
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSPageRestServiceTest.class);
}
