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
package com.percussion.sitemanage.web.service;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetDropCriteria;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetResourceType;
import com.percussion.pagemanagement.data.PSAbstractRegion;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionNode.PSRegionOwnerType;
import com.percussion.pagemanagement.data.PSRegionTree;
import com.percussion.pagemanagement.data.PSRegionTreeUtils;
import com.percussion.pagemanagement.data.PSRegionWidgets;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.web.service.PSPageRestClient;
import com.percussion.pagemanagement.web.service.PSRenderServiceClient;
import com.percussion.pagemanagement.web.service.PSTemplateServiceClient;
import com.percussion.pagemanagement.web.service.PSTestSiteData;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria.SkipItemsType;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.pathmanagement.web.service.PSPathServiceRestClient;
import com.percussion.share.IPSSitemanageConstants;
import com.percussion.share.async.IPSAsyncJob;
import com.percussion.share.async.PSAsyncJobStatus;
import com.percussion.share.data.PSEnumVals;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.test.PSObjectRestClient;
import com.percussion.share.test.PSObjectRestClient.DataValidationRestClientException;
import com.percussion.share.test.PSRestClient.RestClientException;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.share.test.PSTestDataCleaner;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.share.validation.PSValidationErrors.PSFieldError;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteCopyRequest;
import com.percussion.sitemanage.data.PSSiteProperties;
import com.percussion.sitemanage.data.PSSitePublishProperties;
import com.percussion.sitemanage.data.PSSiteStatisticsSummary;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteDataService.PublishType;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


public class PSSiteDataServiceTest extends PSRestTestCase<PSSiteRestClient> {
    
    private final static String SITE_NAME_PREFIX = "TestMyTestSite";
    private static PSTestSiteData testSiteData;
    PSTestDataCleaner<String> siteCleaner = new PSTestDataCleaner<String>() {
        @Override
        protected void clean(String id) throws Exception
        {
            restClient.delete(id);
        }
        
    };
    
    PSTestDataCleaner<String> folderCleaner = new PSTestDataCleaner<String>()
    {
        @Override
        protected void clean(String id) throws Exception
        {
            PSDeleteFolderCriteria criteria = new PSDeleteFolderCriteria();
            criteria.setPath(id);
            criteria.setSkipItems(SkipItemsType.NO);
            getPathServiceRestClient().deleteFolder(criteria);
        }
    };
    
    PSTestDataCleaner<String> pageCleaner = new PSTestDataCleaner<String>()
    {
        @Override
        protected void clean(String id) throws Exception
        {
            getPageRestClient().delete(id);
        }
    };
    
    @BeforeClass
    public static void setUp() throws Exception
    {
        testSiteData = new PSTestSiteData();
        testSiteData.setUp();
    }
    
    @Override
    protected PSSiteRestClient getRestClient(String baseUrl) {
        restClient = new PSSiteRestClient(baseUrl);
        return restClient;
    }
    
    private PSSiteTemplateRestClient getSiteTemplateServiceRestClient() throws Exception
    {
        PSSiteTemplateRestClient client = new PSSiteTemplateRestClient();
        client.setUrl(baseUrl);
        setupClient(client);
        return client;
    }
    
    private PSRenderServiceClient getRenderServiceClient() throws Exception
    {
        PSRenderServiceClient client = new PSRenderServiceClient();
        client.setUrl(baseUrl);
        setupClient(client);
        return client;
    }
    
    private PSPathServiceRestClient getPathServiceRestClient() throws Exception
    {
        PSPathServiceRestClient client = new PSPathServiceRestClient(baseUrl);
        setupClient(client);
        return client;
    }
    
    private PSPageRestClient getPageRestClient() throws Exception
    {
        PSPageRestClient client = new PSPageRestClient(baseUrl);
        setupClient(client);
        return client;
    }
    
    private PSTemplateServiceClient getTemplateServiceClient() throws Exception
    {
        PSTemplateServiceClient client = new PSTemplateServiceClient(baseUrl);
        setupClient(client);
        return client;
    }
    
    @After
    public void tearDownTest() {
        // switch to Admin user
        restClient.login("Admin", "demo");
 
        pageCleaner.clean();
        folderCleaner.clean();
        siteCleaner.clean();
    }
    
    @AfterClass
    public static void tearDown()
    {
        try
        {
            testSiteData.tearDown();
        }
        catch (Exception e)
        {
            log.error("Failed to tear down test site data", e);
        }
    }
    
    @Test
    public void testCreateSite() throws Exception {
        log.debug("testCreateSite");
        PSSite site = createSite();
        //Switch to non-Admin user
        restClient.login("Editor", "demo");
        //Should not be able to create the site
        try
        {
            restClient.save(site);
            fail("Non-Admin user should not be able to create a site");
        }
        catch (RestClientException e)
        {
            assertTrue(e.getResponseBody().contains("site.saveNotAuthorized"));
        }
        //Switch to Admin user
        restClient.login("Admin", "demo");
        //Should be able to create the site
        try
        {
            PSSite actual = restClient.save(site);
            // folder path is filled in during save
            site.setFolderPath(actual.getFolderPath());
            site.setSiteId(actual.getSiteId());
            assertEquals(site, actual);
        }
        catch (RestClientException e)
        {
            fail("Admin user should be able to create a site");
        }
    }

    @Test
    public void testEditSite() throws Exception
    {
        log.debug("testEditSite");
        PSSite site = createAndSaveSite();
        String siteName = site.getName();
        
        // updating with the same data, no change
        PSSiteProperties props = restClient.getProperties(site.getName());
        PSSiteProperties props_2 = restClient.updateProperties(props);
        
        assertEquals(props, props_2);
        
        // updating with different data
        props = restClient.getProperties(site.getName());
        props.setHomePageLinkText("Percussion Site");
        props.setDescription("Percussion Site Description");
        props.setName(props.getName() + System.currentTimeMillis());
        
        props_2 = restClient.updateProperties(props);
        assertEquals(props_2.getHomePageLinkText(), "Percussion Site");
        assertEquals(props_2.getDescription(), "Percussion Site Description");
        assertEquals(props_2.getName(), props.getName());

        // change the site name back
        props.setName(siteName);
        props_2 = restClient.updateProperties(props);
        assertEquals(props_2.getName(), siteName);
    }
    
    @Test
    public void testPublishEditSite() throws Exception
    {
        log.debug("testPublishEditSite");
        PSSite site = createAndSaveSite();
        String siteName = site.getName();
   
        //Update site publish properties with different data and test it
        PSSitePublishProperties publishprops1 = restClient.getSitePublishProperties(siteName);
        publishprops1.setSiteName(siteName);
        publishprops1.setFtpServerName("ftpserver");
        publishprops1.setFtpUserName("ftpuser");
        publishprops1.setPublishType(PublishType.valueOf("ftp"));
        
        PSSitePublishProperties publishprops2 = restClient.updateSitePublishProperties(publishprops1);
        assertEquals(publishprops2.getFtpServerName(), "ftpserver");
        assertEquals(publishprops2.getFtpUserName(), "ftpuser");
        assertEquals(publishprops2.getPublishType().toString(), "ftp");
    }
    
    public PSSite createAndSaveSite()
    {
        PSSite site = createSite();
                        
        return restClient.save(site);
    }
    
    
    @Test
    public void testImportSiteFromUrlAsync() throws Exception
    {
        PSSite site = createSite();
        site.setBaseUrl("http://samples.percussion.com/products/index.html");

        long jobId = restClient.importSiteFromUrlAsync(site);
        assertTrue(jobId > 0);

        // Poll for status until the job is completed or aborted.
        PSAsyncJobStatus jobStatus = null;
        do
        {
            jobStatus = testSiteData.getAsyncJobStatusRestClient().getStatus(Long.toString(jobId));
            assertNotNull(jobStatus);

        }
        while (!jobStatus.getStatus().equals(IPSAsyncJob.COMPLETE_STATUS)
                && !jobStatus.getStatus().equals(IPSAsyncJob.ABORT_STATUS));

        // Check that the import finished succesfully
        assertTrue(jobStatus.getStatus().equals(IPSAsyncJob.COMPLETE_STATUS));

        // Get imported site from job
        PSSite importedSite = restClient.getImportedSite(jobId);

        // Test that the result got from the job is the expected
        assertNotNull(importedSite);
        assertEquals(site.getName(), importedSite.getName());
    }
    
    @Test
    public void testGetSite() throws Exception {
        log.debug("testGetSite");
        PSSite newSite = createAndSaveSite();
        PSSite site = restClient.get(newSite.getName());
        assertEquals(newSite.getName(), site.getName());
    }
    
    @Test
    public void testGetChoices() throws Exception {
        log.debug("testGetChoices");
        PSSite newSite = createAndSaveSite();
        PSSite newSite2 = createAndSaveSite();
        
        List<PSSiteSummary> sums = restClient.findAll();
        
        boolean newSiteFound = false;
        boolean newSite2Found = false;
        PSEnumVals choices = restClient.getChoices();
        List<PSEnumVals.EnumVal> entries = choices.getEntries();
        assertEquals(sums.size(), entries.size());
        for (PSEnumVals.EnumVal entry : entries)
        {
            String v = entry.getValue();
            if (v.equals(newSite.getName()))
            {
                newSiteFound = true;
            }
            else if (v.equals(newSite2.getName()))
            {
                newSite2Found = true;
            }
        }
        assertTrue(newSiteFound);
        assertTrue(newSite2Found);
    }
    
    @Test
    public void testGetSiteNotFound() throws Exception {
        log.debug("testGetSiteNotFound");
        assertSiteNotExist("DO_NOT_FIND_ME");
    }
    
    @Test
    public void testDeleteSite() throws Exception {
        log.debug("deleteSite");
        PSSite site = createAndSaveSite();
        //Switch to non-Admin user
        restClient.login("Editor", "demo");
        //Should not be able to delete the site
        try
        {
            restClient.delete(site.getName());
            fail("Non-Admin user should not be able to delete a site");
        }
        catch (RestClientException e)
        {
            assertTrue(e.getResponseBody().contains("site.deleteNotAuthorized"));
        }
        //Switch to Admin user
        restClient.login("Admin", "demo");
        //Should be able to delete the site
        try
        {
            restClient.delete(site.getName());
            assertSiteNotExist(site.getName());
        }
        catch (RestClientException e)
        {
            fail("Admin user should be able to delete a site");
        }
    }
    
    @Test
    public void testFailToSaveInvalidSite() throws Exception {
        log.debug("failToSaveInvalidSite");
        PSSite s = new PSSite();
        s.setName("");
        Set<String> expected = new HashSet<String>();
        //Fields that are invalid
        expected.addAll(asList("name","homePageTitle","navigationTitle", "templateName", "templateId", "label"));
        try {
            restClient.save(s);
        }
        catch (DataValidationRestClientException e) {
            PSValidationErrors errors = e.getErrors();
            //The name is invalid.
            //assertEquals("name", errors.getFieldErrors().get(0).getField());
            Set<String> actual = new HashSet<String>();
            for(PSFieldError fe : errors.getFieldErrors()) actual.add(fe.getField());
            assertEquals(expected,actual);
        }
        catch (PSObjectRestClient.DataRestClientException e)
        {
            String errorMsg = e.getMessage();
            assertTrue(errorMsg.indexOf("sitename may not be null or empty") == -1);
        }
    }

    @Test
    public void testCreateSiteWithErikJson() throws Exception {
        try {
            String siteJson = "{\"Site\":{\"name\":\"TestA\",\"label\":\"TestA\",\"description\":\"TestA\"," +
            		"\"homePageTitle\":\"Testpage1\",\"navigationTitle\":\"Testpage1\",\"templateId\":\"percSampleTemplate1\"}}";
            restClient.getRequestHeaders().put("Accept", "application/json");
            restClient.POST("/Rhythmyx/services/sitemanage/site/", siteJson, "application/json");
            fail("Exception should have been thrown");
        } catch (RestClientException re) {
            assertEquals(400,re.getStatus());
        }
        
        
    }
    
    @Test
    @Ignore
    public void testCopy() throws Exception
    {
        log.debug("testCopy");

        PSSite site = createAndSaveSite();
        String siteId = site.getId();
        String siteName = site.getName();
        
        String copyName = "CopyOf" + siteName;
        PSSiteCopyRequest req = new PSSiteCopyRequest();
        req.setSrcSite(siteId);
        req.setCopySite(copyName);
        PSSite copy = restClient.copy(req);
        assertNotNull(copy);
        String copyId = copy.getId();
        siteCleaner.add(copyId);
        
        // check the name
        assertEquals(copy.getName(), copyName);
        
        // check the templates
        PSSiteTemplateRestClient tempClient = getSiteTemplateServiceRestClient();
        List<PSTemplateSummary> siteTemplates = tempClient.findTemplatesBySite(siteId);
        List<PSTemplateSummary> copyTemplates = tempClient.findTemplatesBySite(copyId);
        assertEquals(siteTemplates.size(), copyTemplates.size());
        
        PSTemplateSummary siteTemp = siteTemplates.get(0);
        String siteTempId = siteTemp.getId();
        PSTemplateSummary copyTemp = copyTemplates.get(0);
        assertFalse(copyTemp.getId().equals(siteTempId));
        
        // reset the id so we can compare the templates
        copyTemp.setId(siteTempId);
        copyTemp.setImageThumbPath("Thumbnail");
        siteTemp.setImageThumbPath(copyTemp.getImageThumbPath());
        assertEquals(siteTemp, copyTemp);
    
        // check the publishing configuration
        PSSitePublishProperties sitePubProps = restClient.getSitePublishProperties(siteName);
        String sitePubPropsId = sitePubProps.getId();
        PSSitePublishProperties copyPubProps = restClient.getSitePublishProperties(copyName);
        assertFalse(copyPubProps.getId().equals(sitePubPropsId));
        assertEquals(copyName, copyPubProps.getSiteName());
        
        // reset the id and name so we can compare the configurations
        copyPubProps.setId(sitePubPropsId);
        copyPubProps.setSiteName(siteName);
        assertEquals(sitePubProps, copyPubProps);
        
        // should not be able to copy to an existing site
        try
        {
            restClient.copy(req);
            fail("Should not be able to copy to an existing site");
        }
        catch (RestClientException e)
        {
            assertTrue(e.getResponseBody().contains("site.exists"));
        }
        
        // switch to non-Admin user
        restClient.login("Editor", "demo");
        // should not be able to copy the site
        try
        {
            req.setCopySite("SiteCreatedByEditor");
            restClient.copy(req);
            fail("Non-Admin user should not be able to copy a site");
        }
        catch (RestClientException e)
        {
            assertTrue(e.getResponseBody().contains("site.saveNotAuthorized"));
        }
             
        // create a page
        String pageId = testSiteData.createPage("testPage", testSiteData.site1.getFolderPath(),
                testSiteData.template1.getId());
        pageCleaner.add(pageId);
        
        PSPage page = testSiteData.getPageRestClient().load(pageId);
               
        //Create a new region
        String regionHtml = "<div>MY-CODE</div>"
                          + "<div class=\"perc-region\" id=\"leftsidebar\">"
                          + "#region('leftsidebar' '<div>' '<span>' '</span>' '</div>')"
                          + "</div>";
        PSRegion region = getRenderServiceClient().parse(regionHtml);
        
        //Get the child region
        PSAbstractRegion childRegion = PSRegionTreeUtils.getChildRegions(region).get(0);
        childRegion.setOwnerType(PSRegionOwnerType.PAGE);
        page.getRegionBranches().setRegions(asList((PSRegion) childRegion));
        
        //Create a new widget
        PSWidgetItem wi = new PSWidgetItem();
        wi.setDefinitionId("percPageAutoList");
        wi.setName("widget5");
        
        //Set the region widgets
        page.getRegionBranches().setRegionWidgets(childRegion.getRegionId(), asList(wi));
        
        pageId = testSiteData.getPageRestClient().save(page).getId();
        //Reload the page after save
        page = testSiteData.getPageRestClient().load(pageId);
        
        Set<PSRegionWidgets> regWidgs = page.getRegionBranches().getRegionWidgetAssociations();
        String widgetId = null;
        for (PSRegionWidgets psRegionWidgets : regWidgs)
        {
            widgetId = psRegionWidgets.getWidgetItems().get(0).getId();
        }
                        
        //create a page autolist
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", "autolist");
        asset.getFields().put("displaytitle", "Test Autolist");
        asset.getFields().put("query", testSiteData.site1.getFolderPath() + "%" +"samplequery");
        asset.getFields().put("title contains", "testPage");
        asset.getFields().put("site_path", testSiteData.site1.getFolderPath());
        asset.setType("percPageAutoList");
        asset = testSiteData.saveAsset(asset);
        String origAssetId = asset.getId();
        testSiteData.getAssetCleaner().remove(origAssetId);
        
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(pageId, Long.parseLong(widgetId),
                "percPageAutoList", origAssetId, 1, wi.getName());
        String awRelId = testSiteData.getAssetRestClient().createAssetWidgetRelationship(awRel);
        assertNotNull(awRelId); 
        restClient.login("Admin", "demo");
        
        List<PSAssetDropCriteria> orgDropCriteria = testSiteData.getAssetRestClient().
                                                    getWidgetAssetCriteria(pageId,true);
        String origWidgetName = orgDropCriteria.get(0).getWidgetName();
        String origWidgetId = orgDropCriteria.get(0).getWidgetId();
        
        req.setSrcSite(testSiteData.site1.getId());
        req.setCopySite("SiteWithCopy");
        PSSite siteAfterCopy = restClient.copy(req); 
        
        siteCleaner.add(siteAfterCopy.getId());
        PSPage pageAfterCopy = testSiteData.getPageRestClient().findPageByFullFolderPath(siteAfterCopy.getFolderPath() +
                                                                                       "/" + "testPage" );
        // the author should be the logged in user
        // http://bugs.percussion.local/browse/CML-4895
        String pageAfterCopyAuthor = getPageAfterCopyAuthor(siteAfterCopy, pageAfterCopy);
        assertEquals(pageAfterCopyAuthor, "Admin");
        
        List<PSAssetDropCriteria> copyDropCriteria = testSiteData.getAssetRestClient().
                                                     getWidgetAssetCriteria(pageAfterCopy.getId(),true);
        
        String copyWidgetName = copyDropCriteria.get(0).getWidgetName();
        String copyWidgetId = copyDropCriteria.get(0).getWidgetId();
        
        assertEquals(origWidgetName,copyWidgetName);
        assertEquals(origWidgetId, copyWidgetId);
        
        assertListAsset(page.getId(), pageAfterCopy.getId());
    }
    
    /**
     * Gets the author of the page that is passed as the second parameter. The site is used to
     * get its children. The page must be a child of the site, otherwise it will return <code>null</code>
     * 
     * @param siteAfterCopy the site in which we are going to seek for the page
     * @param pageAfterCopy the page that we are looking the author for
     * @return the author of the page or <code>null</code> if we do not find it
     */
    private String getPageAfterCopyAuthor(PSSite siteAfterCopy, PSPage pageAfterCopy)
    {
        String siteAfterCopyPath = siteAfterCopy.getFolderPath().substring(1);
        
        int startIndex = 1;         // begin with the first element.
        int maxResults = 9;         // set 100 as max (should be less elements)
        int displayFormatId = 9;    // the same as the list view in the finder
        String child = null;        // setting the child to null means it won't be used
        
        PSPagedItemList siteChildren = testSiteData.getPathRestClient().findChildren(siteAfterCopyPath, 
                   startIndex, maxResults, child, displayFormatId);
        
        // find the test page
        String author = null;
        for(PSPathItem pathItem : siteChildren.getChildrenInPage())
        {
            if(pathItem.getId().equals(pageAfterCopy.getId()))
            {
                author = pathItem.getDisplayProperties().get("sys_contentcreatedby");
            }
        }
        
        return author;
    }

    @Test
    public void testCopyWithAssets() throws Exception
    {
        log.debug("testCopyWithAssets");
        PSSite site = testSiteData.site1;
        String siteId = site.getId();
        String siteName = site.getName();
                
        //Create a folder in the Assets Library
        String testFolderPath = PSPathUtils.ASSETS_FINDER_ROOT + "/Test";
        getPathServiceRestClient().addFolder(testFolderPath);
        folderCleaner.add(testFolderPath);
        
        //Create a page autolist under the folder
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", "autolist");
        asset.getFields().put("displaytitle", "Test Autolist");
        asset.getFields().put("query", "select rx:sys_contentid, rx:sys_folderid from rx:percPage where jcr:path like '"
                + site.getFolderPath() + "%'");
        asset.getFields().put("title contains", "testPage");
        asset.getFields().put("site_path", site.getFolderPath());
        asset.setFolderPaths(asList(PSPathUtils.getFolderPath(testFolderPath)));
        asset.setType("percPageAutoList");
        asset = testSiteData.saveAsset(asset);
        String origAssetId = asset.getId();
        testSiteData.getAssetCleaner().remove(origAssetId);
        
        //Create a page
        String pageId = testSiteData.createPage("testPage", site.getFolderPath(), testSiteData.template1.getId());
        PSPage page = testSiteData.getPageRestClient().load(pageId);
                       
        //Create a new region
        String regionHtml = "<div>MY-CODE</div>"
                          + "<div class=\"perc-region\" id=\"leftsidebar\">"
                          + "#region('leftsidebar' '<div>' '<span>' '</span>' '</div>')"
                          + "</div>";
        PSRegion region = getRenderServiceClient().parse(regionHtml);
        
        //Get the child region
        PSAbstractRegion childRegion = PSRegionTreeUtils.getChildRegions(region).get(0);
        childRegion.setOwnerType(PSRegionOwnerType.PAGE);
        page.getRegionBranches().setRegions(asList((PSRegion) childRegion));
        
        //Create a new widget
        PSWidgetItem wi = new PSWidgetItem();
        wi.setDefinitionId("percPageAutoList");
        wi.setName("widget5");
        
        //Set the region widgets
        page.getRegionBranches().setRegionWidgets(childRegion.getRegionId(), asList(wi));
        
        pageId = testSiteData.getPageRestClient().save(page).getId();
        //Reload the page after save
        page = testSiteData.getPageRestClient().load(pageId);
        
        Set<PSRegionWidgets> regWidgs = page.getRegionBranches().getRegionWidgetAssociations();
        String widgetId = null;
        for (PSRegionWidgets psRegionWidgets : regWidgs)
        {
            widgetId = psRegionWidgets.getWidgetItems().get(0).getId();
        }
                
        //Add the page auto list to the page
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(pageId, Long.parseLong(widgetId),
                "percPageAutoList", origAssetId, 1, wi.getName());
        awRel.setResourceType(PSAssetResourceType.shared);
        String awRelId = testSiteData.getAssetRestClient().createAssetWidgetRelationship(awRel);
        assertNotNull(awRelId); 
        
        List<PSAssetDropCriteria> orgDropCriteria = testSiteData.getAssetRestClient().
                                                    getWidgetAssetCriteria(pageId,true);
        String origWidgetName = orgDropCriteria.get(0).getWidgetName();
        String origWidgetId = orgDropCriteria.get(0).getWidgetId();
        
        String copyName = "CopyOf" + siteName;
        PSSiteCopyRequest req = new PSSiteCopyRequest();
        req.setSrcSite(siteId);
        req.setCopySite(copyName);
        req.setAssetFolder("Test");
        
        String newAssetFolder = PSPathUtils.ASSETS_FINDER_ROOT + '/' + copyName;
        
        // delete new asset folder if it exists
        try
        {
            getPathServiceRestClient().find(newAssetFolder);
            PSDeleteFolderCriteria criteria = new PSDeleteFolderCriteria();
            criteria.setPath(newAssetFolder);
            criteria.setSkipItems(SkipItemsType.NO);
            getPathServiceRestClient().deleteFolder(criteria);
        }
        catch (Exception e)
        {
            // folder doesn't exist            
        }
                
        PSSite copy = restClient.copy(req);
        assertNotNull(copy);
        String copyId = copy.getId();
        siteCleaner.add(copyId);
        
        // make sure the asset folder was copied
        try
        {
            getPathServiceRestClient().find(newAssetFolder);
            folderCleaner.add(newAssetFolder);
        }
        catch (Exception e)
        {
            fail("Folder '" + newAssetFolder + "' should have been created");         
        }
        
        // folder should contain copy of page auto list
        List<PSPathItem> children = getPathServiceRestClient().findChildren(newAssetFolder);
        assertEquals(1, children.size());
        
        PSPathItem child = children.get(0);
        assertEquals(asset.getName(), child.getName());
        assertTrue(!child.getId().equals(asset.getId()));
        
        // check the name
        assertEquals(copy.getName(), copyName);
        
        // check the templates
        PSSiteTemplateRestClient tempClient = getSiteTemplateServiceRestClient();
        List<PSTemplateSummary> siteTemplates = tempClient.findTemplatesBySite(siteId);
        List<PSTemplateSummary> copyTemplates = tempClient.findTemplatesBySite(copyId);
        assertEquals(siteTemplates.size(), copyTemplates.size());
        
        PSTemplateSummary siteTemp = siteTemplates.get(0);
        String siteTempId = siteTemp.getId();
        PSTemplateSummary copyTemp = copyTemplates.get(0);
        assertFalse(copyTemp.getId().equals(siteTempId));
        
        // reset the id so we can compare the templates
        copyTemp.setId(siteTempId);
        copyTemp.setImageThumbPath("Thumbnail");
        siteTemp.setImageThumbPath(copyTemp.getImageThumbPath());
        assertEquals(siteTemp, copyTemp);
    
        // check the publishing configuration
        PSSitePublishProperties sitePubProps = restClient.getSitePublishProperties(siteName);
        String sitePubPropsId = sitePubProps.getId();
        PSSitePublishProperties copyPubProps = restClient.getSitePublishProperties(copyName);
        assertFalse(copyPubProps.getId().equals(sitePubPropsId));
        assertEquals(copyName, copyPubProps.getSiteName());
        
        // reset the id and name so we can compare the configurations
        copyPubProps.setId(sitePubPropsId);
        copyPubProps.setSiteName(siteName);
        assertEquals(sitePubProps, copyPubProps);
        
        PSPage pageAfterCopy = testSiteData.getPageRestClient().findPageByFullFolderPath(copy.getFolderPath() +
                "/" + "testPage" );

        List<PSAssetDropCriteria> copyDropCriteria = testSiteData.getAssetRestClient().
        getWidgetAssetCriteria(pageAfterCopy.getId(),true);

        String copyWidgetName = copyDropCriteria.get(0).getWidgetName();
        String copyWidgetId = copyDropCriteria.get(0).getWidgetId();

        assertEquals(origWidgetName,copyWidgetName);
        assertEquals(origWidgetId, copyWidgetId);

        assertListAsset(page.getId(), pageAfterCopy.getId());
        
    }
    
    //Fixme
    @Test(expected=Exception.class)
    public void testCreateSiteWithJsonValidationFailure() throws Exception {
        String siteJson = "{\"PSSite\":{\"name\":\"JsonTest\",\"label\":\"Json Test PSSite\"}}";
        restClient.getRequestHeaders().put("Accept", "application/json");
        String response = restClient.POST("/Rhythmyx/services/sitemanage/site/", siteJson, "application/json");
        assertNotNull(response);
        //restClient.delete("JsonTest");
        
    }
       
    private void assertSiteNotExist(String siteId) {
        try {
            restClient.get(siteId);
            fail("Rest client should have thrown an exception");
        } catch (RestClientException e) {
            assertEquals(500, e.getStatus());
        }
    }
    
    private PSSite createSite()
    {
        PSSite site = new PSSite();
        site.setName(SITE_NAME_PREFIX + "--" + System.currentTimeMillis());
        site.setLabel("My test site");
        site.setHomePageTitle("homePageTitle");
        site.setNavigationTitle("navigationTitle");
        site.setBaseTemplateName(IPSSitemanageConstants.PLAIN_BASE_TEMPLATE_NAME);
        site.setTemplateName("templateName");
        
        siteCleaner.add(site.getId());
             
        return site;
    }
      
    /**
     * Asserts that a page auto list asset exists on each of the specified pages with a different id and query field
     * value.
     * 
     * @param origId id of the original page.
     * @param copyId id of the copied page.
     */
    private void assertListAsset(String origId, String copyId)
    {
        String orgRenderStr = testSiteData.getRenderServiceClient().renderPageForEdit(origId);
        String orgAssetid = null;
        String token = "assetId=\"";
        if(orgRenderStr.indexOf(token) != -1)
        {
            orgRenderStr = orgRenderStr.substring(orgRenderStr.indexOf(token) + token.length());
            orgAssetid = orgRenderStr.substring(0,orgRenderStr.indexOf("\""));
        }
        
        String newRenderStr = testSiteData.getRenderServiceClient().renderPageForEdit(copyId);
        String newAssetid = null;
        if(newRenderStr.indexOf(token) != -1)
        {
            newRenderStr = newRenderStr.substring(newRenderStr.indexOf(token) + token.length());
            newAssetid = newRenderStr.substring(0,newRenderStr.indexOf("\""));
        }
        
        assertNotNull(orgAssetid);
        assertNotNull(newAssetid);
        
        assertTrue(!orgAssetid.equals(newAssetid));
        
        //load original asset summary
        PSAsset origAsset =  testSiteData.getAssetRestClient().load(orgAssetid);
        
        //load new asset summary
        PSAsset newAsset =  testSiteData.getAssetRestClient().load(newAssetid);
        
        assertTrue(origAsset.getType().equals("percPageAutoList"));
        assertTrue(newAsset.getType().equals("percPageAutoList"));
        
        Map<String, Object>  origFields = origAsset.getFields();
        
        Map<String, Object>  newFields = newAsset.getFields();
        
        String origQuery = (String)origFields.get("query");
        
        String newQuery = (String)newFields.get("query");
        
        assertTrue(!origQuery.equals(newQuery));
    }
    
    /**
     * Create a Rich text widget item, and its corresponding asset, with the
     * given widget name, widget description, and slot id (or widget id). The
     * asset is saved into the system.
     * 
     * @param name {@link String} with the name of the widget. May be blank.
     * @param description {@link String} with the description of the widget. May
     *            be blank.
     * @param slotid {@link String} with the widget id. May be blank.
     * @return {@link PSPair}<{@link PSWidgetItem}, {@link PSAsset}> never
     *         <code>null</code>, contains the widget item in the first place,
     *         and the asset in the second place.
     */
    private PSPair<PSWidgetItem, PSAsset> createRichTextWidgetItem(String name, String description, String slotid)
    {
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", "LocalAsset" + System.currentTimeMillis());
        asset.setType("percRichTextAsset");
        asset.getFields().put("text", "Test Rich text");
        asset.setFolderPaths(asList("//Folders"));
        asset = testSiteData.saveAsset(asset);

        PSWidgetItem widget = new PSWidgetItem();
        widget.setDefinitionId("percRichText");
        widget.setDescription(description);
        widget.setName(name);
        widget.setId(slotid);

        return new PSPair<PSWidgetItem, PSAsset>(widget, asset);
    }

    /**
     * Create a Raw HTML widget item, and its corresponding asset, with the
     * given widget name, widget description, and slot id (or widget id). The
     * asset is saved into the system.
     * 
     * @param name {@link String} with the name of the widget. May be blank.
     * @param description {@link String} with the description of the widget. May
     *            be blank.
     * @param slotid {@link String} with the widget id. May be blank.
     * @return {@link PSPair}<{@link PSWidgetItem}, {@link PSAsset}> never
     *         <code>null</code>, contains the widget item in the first place,
     *         and the asset in the second place.
     */
    private PSPair<PSWidgetItem, PSAsset> createRawHtmlWidgetItem(String name, String description, String slotid)
    {
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", "LocalAsset" + System.currentTimeMillis());
        asset.setType("percRawHtmlAsset");
        asset.getFields().put("html", "Test Rich text");
        asset.setFolderPaths(asList("//Folders"));
        asset = testSiteData.saveAsset(asset);
        
        PSWidgetItem widget = new PSWidgetItem();
        widget.setDefinitionId("percRawHtml");
        widget.setDescription(description);
        widget.setName(name);
        widget.setId(slotid);
        
        return new PSPair<PSWidgetItem, PSAsset>(widget, asset);
    }

    /**
     * Adds the given widgets to the template. Creates a region and adds the
     * widgets to it. Then it sets that region to a region tree and set it to
     * the template.
     * 
     * @param template {@link PSTemplate} object, must not be <code>null</code>.
     * @param widgetAssetPairs {@link List}<{@link PSWidgetItem}> with the
     *            widgets to add. May be empty but not <code>null</code>.
     * @throws Exception 
     */
    private void addWidgetsToTemplateAndSave(PSTemplate template, List<PSPair<PSWidgetItem, PSAsset>> widgetAssetPairs) throws Exception
    {
        notNull(template);
        notNull(widgetAssetPairs);

        List<PSWidgetItem> widgets = new ArrayList<PSWidgetItem>();
        for (PSPair<PSWidgetItem, PSAsset> pair : widgetAssetPairs)
        {
            widgets.add(pair.getFirst());
        }

        PSRegion region = new PSRegion();
        region.setRegionId("region");
        region.setStartTag("<div id=\"region\" class=\"perc-region\" >");
        region.setEndTag("</div>");

        PSRegionTree regTree = new PSRegionTree();
        regTree.setRegionWidgets(region.getRegionId(), widgets);
        regTree.setRootRegion(region);

        template.setRegionTree(regTree);

        PSTemplateServiceClient tempServiceClient = getTemplateServiceClient();
        tempServiceClient.save(template);
    }
    
    /**
     * Creates a new page with the given name assigned to template given as parameter into the specified folder.
     * 
     * @param name {@link String} the page name, must not be <code>null</code>.
     * @param template {@link PSTemplate} object, must not be <code>null</code>.
     * @param templateId {@link String} the template for the page, must not be <code>null</code
     *
     * @throws Exception 
     */
    private String createPage(String name, String folderPath, String templateId) throws Exception
    {
        notEmpty(templateId);
        String pageId = null;

        PSPage page = new PSPage();
        page.setName(name);
        page.setTitle(name);
        page.setFolderPath(folderPath);
        page.setTemplateId(templateId);
        page.setLinkTitle("dummy");

        PSPage r = testSiteData.getPageRestClient().save(page);
        pageId = r.getId();

        pageCleaner.add(pageId);
        
        return pageId;
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSSiteDataServiceTest.class);

}
