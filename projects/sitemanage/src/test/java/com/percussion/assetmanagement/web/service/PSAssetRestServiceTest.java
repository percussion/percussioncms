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

package com.percussion.assetmanagement.web.service;

import static com.percussion.pagemanagement.parser.PSTemplateRegionParser.parse;

import static java.util.Arrays.asList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetDropCriteria;
import com.percussion.assetmanagement.data.PSAssetEditUrlRequest;
import com.percussion.assetmanagement.data.PSAssetEditor;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetResourceType;
import com.percussion.assetmanagement.data.PSContentEditCriteria;
import com.percussion.assetmanagement.forms.data.PSFormSummary;
import com.percussion.assetmanagement.service.impl.PSAssetRestService;
import com.percussion.cms.IPSConstants;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.web.service.PSItemWorkflowServiceRestClient;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSRegionNode.PSRegionOwnerType;
import com.percussion.pagemanagement.data.PSRegionTree;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.data.PSWidgetSummary;
import com.percussion.pagemanagement.web.service.PSPageRestClient;
import com.percussion.pagemanagement.web.service.PSTemplateServiceClient;
import com.percussion.pagemanagement.web.service.PSTestSiteData;
import com.percussion.pagemanagement.web.service.PSWidgetServiceTest.PSWidgetRestClient;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria.SkipItemsType;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.pathmanagement.web.service.PSPathServiceRestClient;
import com.percussion.share.test.PSRestClient.RestClientException;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.share.test.PSTestDataCleaner;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PSAssetRestServiceTest extends PSRestTestCase<PSAssetServiceRestClient>
{
    private static PSTestSiteData testSiteData;
        
    private static final String WIDGET_REGION_HTML = "<div class=\"perc-region\" id=\"container\">"
       + "<div>CODE</div>"
       + "<div class=\"perc-region\" id=\"my-widget-region\">"
       + "#region('my-widget-region' '<div>' '<span>' '</span>' '</div>')"
       + "</div>"
       + "</div>";

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
            getPageRestClient().delete(id);
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

    PSTestDataCleaner<String> assetCleaner = new PSTestDataCleaner<String>()
    {
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

    {
        assetCleaner.setFailOnErrors(true);
        templateCleaner.setFailOnErrors(true);
        pageCleaner.setFailOnErrors(true);
        folderCleaner.setFailOnErrors(true);
    }

    @Override
    protected PSAssetServiceRestClient getRestClient(String url)
    {
        return new PSAssetServiceRestClient(url);
    }

    @Test
    public void test010AssetCreation() throws Exception
    {
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", "MyAsset");
        asset.setType("percRawHtmlAsset");
        asset.getFields().put("html", "TestHTML");
        asset.setFolderPaths(asList("//Folders"));
        asset = restClient.save(asset);
        assertNotNull(asset);
        assertNotNull(asset.getId());
        assetCleaner.add(asset.getId());
    }

    @Test
    public void test020AssetEditUrl() throws Exception
    {
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", "MyTestAsset");
        asset.setType("percRawHtmlAsset");
        asset.getFields().put("html", "TestHTML");
        asset.setFolderPaths(asList("//Folders"));
        asset = restClient.save(asset);
        assertNotNull(restClient.getAssetEditUrl(asset.getId()));
        assetCleaner.add(asset.getId());
    }

    @Test
    public void test030GetAssetEditors() throws Exception
    {
        List<PSAssetEditor> assetEditors = getAssetEditUrlRestClient().getAssetEditors();

        // Verify we found editors
        assertFalse(assetEditors.isEmpty());

        // Loop through List and make sure data was returned.
        for (PSAssetEditor assetEditor : assetEditors)
        {
            assertTrue(StringUtils.isNotBlank(assetEditor.getIcon()));
            assertTrue(StringUtils.isNotBlank(assetEditor.getTitle()));
            assertTrue(StringUtils.isNotBlank(assetEditor.getUrl()));
            assertNotNull(assetEditor.getWorkflowId());
        }
    }

    @Test
    public void test040CreateAssetWidgetRelationship() throws Exception
    {
        String pageId = null;
        String templateId = testSiteData.template1.getId();
        
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
        region.setRegionId("id");
        br.setRegions(asList(region));
        pageNew.setRegionBranches(br);
        pageId = getPageRestClient().save(pageNew).getId();
        pageCleaner.add(pageId);

        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(testSiteData.template2.getId(), 5, "widget5",
                pageId, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        String awRelId = restClient.createAssetWidgetRelationship(awRel);
        assertNotNull(awRelId);
        restClient.clearAssetWidgetRelationship(awRel);
    }

    @Test
    public void test050GetContentEditCriteria() throws Exception
    {
        String templateId = testSiteData.template1.getId();

        PSPage pageNew = new PSPage();
        pageNew.setName("TestPage");
        pageNew.setTitle("Test Page");
        pageNew.setFolderPath(testSiteData.site1.getFolderPath());
        pageNew.setTemplateId(templateId);
        pageNew.setLinkTitle("dummy");

        PSRegion region = new PSRegion();
        region.setOwnerType(PSRegionOwnerType.PAGE);
        region.setRegionId("Test");

        PSWidgetSummary widgetSum = getWidgetClient().get("percRawHtml");

        PSWidgetItem wi = new PSWidgetItem();
        wi.setDefinitionId(widgetSum.getId());

        PSRegionBranches br = new PSRegionBranches();
        br.setRegionWidgets("Test", asList(wi));
        br.setRegions(asList(region));

        pageNew.setRegionBranches(br);

        String pageId = getPageRestClient().save(pageNew).getId();
        pageCleaner.add(pageId);
        assertNotNull(pageId);

        PSRegionTree regTree = new PSRegionTree();
        regTree.setRootRegion(parse(null, WIDGET_REGION_HTML).getRootNode());
        regTree.setRegionWidgetAssociations(br.getRegionWidgetAssociations());

        PSTemplate template = getTemplateClient().loadTemplate(templateId);
        template.setRegionTree(regTree);
        PSTemplate savedTemplate = getTemplateClient().saveTemplate(template);
        wi = savedTemplate.getRegionTree().getRegionWidgetsMap().get("Test").get(0);

        // no properties
        validateGetAssetEditUrls(pageId, PSAssetEditUrlRequest.PAGE_PARENT, wi, pageId);
        validateGetAssetEditUrls(templateId, PSAssetEditUrlRequest.TEMPLATE_PARENT, wi, pageId);

        // one hidden property, value=true
        // wi.getProperties().put(PSWidgetProperty.HIDE_FIELD_PREFIX + "html",
        // true);

        validateWithProperty(pageId, wi, templateId);

        // one visible property
        // wi.getProperties().put(PSWidgetProperty.HIDE_FIELD_PREFIX + "html",
        // false);
        validateWithProperty(pageId, wi, templateId);
    }

    @Test
    public void test060GetWidgetAssetCriteria() throws Exception
    {
        String template1Id = testSiteData.template1.getId();
        String template2Id = testSiteData.template2.getId();

        PSPage pageNew = new PSPage();
        pageNew.setName("TestPage");
        pageNew.setTitle("Test Page");
        pageNew.setFolderPath(testSiteData.site1.getFolderPath());
        pageNew.setTemplateId(template1Id);
        pageNew.setLinkTitle("dummy");

        PSRegion region = new PSRegion();
        region.setOwnerType(PSRegionOwnerType.PAGE);
        region.setRegionId("Test");

        PSWidgetItem wi = new PSWidgetItem();
        wi.setDefinitionId("percRawHtml");

        PSRegionBranches br = new PSRegionBranches();
        br.setRegionWidgets("Test", asList(wi));
        br.setRegions(asList(region));

        pageNew.setRegionBranches(br);

        String pageId = getPageRestClient().save(pageNew).getId();
        pageCleaner.add(pageId);
        assertNotNull(pageId);

        PSRegionTree regTree = new PSRegionTree();
        regTree.setRegionWidgetAssociations(br.getRegionWidgetAssociations());
        regTree.setRootRegion(parse(null, WIDGET_REGION_HTML).getRootNode());

        PSTemplate template = getTemplateClient().loadTemplate(template2Id);
        template.setRegionTree(regTree);
        getTemplateClient().saveTemplate(template);

        // get Drop Criteria

        List<PSAssetDropCriteria> dropCriteria = getAssetEditUrlRestClient().getWidgetAssetCriteria(pageId, true);
        PSAssetDropCriteria crit = dropCriteria.get(0);
        assertFalse(StringUtils.isBlank(crit.getOwnerId()));
        assertFalse(StringUtils.isBlank(crit.getWidgetId()));
        assertFalse(StringUtils.isBlank(String.valueOf(crit.isAppendSupport())));
        assertFalse(StringUtils.isBlank(String.valueOf(crit.isMultiItemSupport())));
        assertFalse(StringUtils.isBlank(crit.getSupportedCtypes().toString().trim()));
        assertFalse(crit.getExistingAsset());

        // templates with no asset relationship
        dropCriteria = getAssetEditUrlRestClient().getWidgetAssetCriteria(template2Id, false);
        crit = dropCriteria.get(0);
        assertFalse(crit.getExistingAsset());
        // template with asset relationship
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(template2Id, Long.parseLong(crit.getWidgetId()),
                "widget_test", pageId, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        restClient.createAssetWidgetRelationship(awRel);
        dropCriteria = getAssetEditUrlRestClient().getWidgetAssetCriteria(template2Id, false);
        crit = dropCriteria.get(0);
        assertTrue(crit.getExistingAsset());
        
        // clear the asset from the template to allow asset cleanup
        restClient.clearAssetWidgetRelationship(awRel);
    }

    @Test
    public void test070AddAssetToFolder() throws Exception
    {
        String pageId = null;
        String templateId = testSiteData.template1.getId();

        String folderPath = testSiteData.site1.getFolderPath();
        PSPage pageNew = new PSPage();
        pageNew.setName("testPageNew11");
        pageNew.setTitle("test new page title");
        pageNew.setFolderPath(folderPath);
        pageNew.setTemplateId(templateId);
        pageNew.setLinkTitle("dummy");

        PSRegionBranches br = new PSRegionBranches();
        PSRegion region = new PSRegion();
        region.setRegionId("Test");
        region.setOwnerType(PSRegionOwnerType.PAGE);
        br.setRegions(asList(region));
        pageNew.setRegionBranches(br);
        pageId = getPageRestClient().save(pageNew).getId();
        pageCleaner.add(pageId);

        String testFolderPath = PSPathUtils.ASSETS_FINDER_ROOT.substring(1) + "/Test";
        PSPathItem folderItem = getPathServiceRestClient().addFolder(testFolderPath);
        folderCleaner.add(testFolderPath);
        List<PSPathItem> items = getPathServiceRestClient().findChildren(testFolderPath);
        assertTrue(items.isEmpty());
        restClient.addAssetToFolder(folderItem.getFolderPath(), pageId);
        items = getPathServiceRestClient().findChildren(testFolderPath);
        assertEquals(1, items.size());
        assertEquals(pageId, items.get(0).getId());

        try
        {
            restClient.addAssetToFolder(null, folderItem.getId());
            fail("Rest client should have thrown exception");
        }
        catch (RestClientException re)
        {
            /*
             * TODO should adding an asset to a folder throw a 500?
             */
            assertEquals(500, re.getStatus());
        }

        try
        {
            restClient.addAssetToFolder(pageId, null);
            fail("Rest client should have thrown exception");
        }
        catch (RestClientException re)
        {
            assertEquals(500, re.getStatus());
        }

        try
        {
            restClient.addAssetToFolder("", folderItem.getId());
            fail("Rest client should have thrown exception");
        }
        catch (RestClientException re)
        {
            assertEquals(500, re.getStatus());
        }

        try
        {
            restClient.addAssetToFolder(pageId, "");
            fail("Rest client should have thrown exception");
        }
        catch (RestClientException re)
        {
            assertEquals(500, re.getStatus());
        }

    }

    @Test
    public void test080DeleteAsset() throws Exception
    {
        restClient.switchCommunity(1002);

        PSAsset asset = restClient.createAsset("testAsset", null);
        String id = asset.getId();
        assetCleaner.add(id);

        PSItemWorkflowServiceRestClient wfClient = getItemWorkflowServiceRestClient();

        // transition item to pending state
        wfClient.transition(id, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);

        // switch to unauthorized user
        restClient.login("author1", "demo");

        // should not have access to item
        try
        {
            restClient.delete(id);
            fail("Current user should not be authorized to delete asset: " + id);
        }
        catch (RestClientException e)
        {
            assertTrue(e.getStatus() == 400 && e.getResponseBody().contains("asset.deleteNotAuthorized"));
        }

        // switch to Admin
        restClient.login("admin1", "demo");

        // should have access to item
        try
        {
            restClient.delete(id);
            assetCleaner.remove(id);
        }
        catch (RestClientException e)
        {
            fail("Current user should be authorized to delete asset: " + id);
        }

        // create asset
        asset = restClient.createAsset("testAsset", "//Folders");
        id = asset.getId();
        assetCleaner.add(id);

        // add it to a template
        String templateId = testSiteData.template1.getId();
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(templateId, 5, "widget5", id, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        restClient.createAssetWidgetRelationship(awRel);

        // should not be able to delete asset
        try
        {
            restClient.delete(id);
            fail("Should not be able to delete asset: " + id + ".  It is used by a template");
        }
        catch (RestClientException e)
        {
            assertTrue(e.getStatus() == 400 && e.getResponseBody().contains("asset.deleteTemplates"));
        }

        // remove the asset from the template
        restClient.clearAssetWidgetRelationship(awRel);

        String siteFolderPath = testSiteData.site1.getFolderPath();

        // add it to a page
        String pageId = testSiteData.createPage("testPage", siteFolderPath, templateId);
        testSiteData.getPageCleaner().remove(siteFolderPath + "/testPage");
        pageCleaner.add(pageId);
        awRel = new PSAssetWidgetRelationship(pageId, 5, "widget5", id, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        restClient.createAssetWidgetRelationship(awRel);

        // transition the page to pending
        wfClient.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);

        // should not be able to delete asset
        try
        {
            restClient.delete(id);
            fail("Should not be able to delete asset: " + id + ".  It is used by an approved page");
        }
        catch (RestClientException e)
        {
            assertTrue(e.getStatus() == 400 && e.getResponseBody().contains("asset.deleteApprovedPages"));
        }

        // transition the page to quick edit
        wfClient.checkOut(pageId);

        // should not be able to delete asset
        try
        {
            restClient.delete(id);
            fail("Should not be able to delete asset: " + id + ".  It is used by an approved page");
        }
        catch (RestClientException e)
        {
            assertTrue(e.getStatus() == 400 && e.getResponseBody().contains("asset.deleteApprovedPages"));
        }

        // remove the asset from the page
        restClient.clearAssetWidgetRelationship(awRel);

        // should not be able to delete asset
        try
        {
            restClient.delete(id);
            fail("Should not be able to delete asset: " + id + ".  It is used by an non-tip revison page in quick "
                    + "edit");
        }
        catch (RestClientException e)
        {
            assertTrue(e.getStatus() == 400 && e.getResponseBody().contains("asset.deleteApprovedPages"));
        }

        // transition the page to pending
        wfClient.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);

        // should be able to delete asset
        try
        {
            restClient.delete(id);
            assetCleaner.remove(id);
        }
        catch (RestClientException e)
        {
            fail("Asset: " + id + " should have been deleted");
        }
    }

    @Test
    public void test090ForceDeleteAsset() throws Exception
    {
        restClient.switchCommunity(1002);

        PSAsset asset = restClient.createAsset("testAsset", null);
        String id = asset.getId();
        assetCleaner.add(id);

        // add it to a template
        String templateId = testSiteData.template1.getId();
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(templateId, 5, "widget5", id, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        restClient.createAssetWidgetRelationship(awRel);

        // force delete
        try
        {
            restClient.forceDelete(id);
            assetCleaner.remove(id);
        }
        catch (RestClientException e)
        {
            fail("Asset: " + id + " should have been deleted");
        }

        // create another asset
        asset = restClient.createAsset("testAsset", null);
        id = asset.getId();
        assetCleaner.add(id);

        String siteFolderPath = testSiteData.site1.getFolderPath();

        // add it to a page
        String pageId = testSiteData.createPage("testPage", siteFolderPath, templateId);
        testSiteData.getPageCleaner().remove(siteFolderPath + "/testPage");
        pageCleaner.add(pageId);
        awRel = new PSAssetWidgetRelationship(pageId, 5, "widget5", id, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        restClient.createAssetWidgetRelationship(awRel);

        // transition the page to pending
        getItemWorkflowServiceRestClient().transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);

        // force delete
        try
        {
            restClient.forceDelete(id);
            assetCleaner.remove(id);

            getItemWorkflowServiceRestClient().checkOut(pageId);

            restClient.clearAssetWidgetRelationship(awRel);
        }
        catch (RestClientException e)
        {
            fail("Asset: " + id + " should have been deleted");
        }
    }

    @Test
    public void test100ValidateDelete() throws Exception
    {
        restClient.switchCommunity(1002);

        PSAsset asset = restClient.createAsset("testAsset", null);
        String id = asset.getId();
        assetCleaner.add(id);

        PSItemWorkflowServiceRestClient wfClient = getItemWorkflowServiceRestClient();

        // transition item to pending state
        wfClient.transition(id, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);

        // switch to unauthorized user
        restClient.login("author1", "demo");

        // should not have access to item
        try
        {
            assertNotNull(restClient.validateDelete(id));
            fail("Current user should not be authorized to delete asset: " + id);
        }
        catch (RestClientException e)
        {
            assertTrue(e.getStatus() == 400 && e.getResponseBody().contains("asset.deleteNotAuthorized"));
        }

        // switch to Admin
        restClient.login("admin1", "demo");

        // should have access to item
        try
        {
            assertNotNull(restClient.validateDelete(id));
            restClient.delete(id);
            assetCleaner.remove(id);
        }
        catch (RestClientException e)
        {
            fail("Current user should be authorized to delete asset: " + id);
        }

        // create asset
        asset = restClient.createAsset("testAsset", "//Folders");
        id = asset.getId();
        assetCleaner.add(id);

        // add it to a template
        String templateId = testSiteData.template1.getId();
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(templateId, 5, "widget5", id, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        restClient.createAssetWidgetRelationship(awRel);

        // should not be able to delete asset
        try
        {
            assertNotNull(restClient.validateDelete(id));
            fail("Should not be able to delete asset: " + id + ".  It is used by a template");
        }
        catch (RestClientException e)
        {
            assertTrue(e.getStatus() == 400 && e.getResponseBody().contains("asset.deleteTemplates"));
        }

        // remove the asset from the template
        restClient.clearAssetWidgetRelationship(awRel);

        String siteFolderPath = testSiteData.site1.getFolderPath();

        // add it to a page
        String pageId = testSiteData.createPage("testPage", siteFolderPath, templateId);
        testSiteData.getPageCleaner().remove(siteFolderPath + "/testPage");
        pageCleaner.add(pageId);
        awRel = new PSAssetWidgetRelationship(pageId, 5, "widget5", id, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        restClient.createAssetWidgetRelationship(awRel);

        // transition the page to pending
        wfClient.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);

        // should not be able to delete asset
        try
        {
            assertNotNull(restClient.validateDelete(id));
            fail("Should not be able to delete asset: " + id + ".  It is used by an approved page");
        }
        catch (RestClientException e)
        {
            assertTrue(e.getStatus() == 400 && e.getResponseBody().contains("asset.deleteApprovedPages"));
        }

        // transition the page to review
        wfClient.checkOut(pageId);
        wfClient.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_ARCHIVE);

        // should be able to delete asset
        try
        {
            assertNotNull(restClient.validateDelete(id));
            restClient.forceDelete(id);
            assetCleaner.remove(id);

            getItemWorkflowServiceRestClient().checkOut(pageId);

            restClient.clearAssetWidgetRelationship(awRel);
        }
        catch (RestClientException e)
        {
            fail("Asset: " + id + " should have been deleted");
        }
    }

    @Test
    public void test110RemoveResource() throws Exception
    {
        restClient.switchCommunity(1002);

        String site1Path = testSiteData.site1.getFolderPath();

        PSAsset asset = restClient.createAsset("testAsset", site1Path);
        String id = asset.getId();
        assetCleaner.add(id);

        PSItemWorkflowServiceRestClient wfClient = getItemWorkflowServiceRestClient();

        // transition item to pending state
        wfClient.transition(id, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);

        // switch to unauthorized user
        restClient.login("author1", "demo");

        // should not have access to item
        try
        {
            restClient.remove(id, site1Path);
            fail("Current user should not be authorized to remove resource: " + id);
        }
        catch (RestClientException e)
        {
            assertTrue(e.getStatus() == 400 && e.getResponseBody().contains("resource.removeNotAuthorized"));
        }

        // switch to Admin
        restClient.login("admin1", "demo");

        // should have access to item
        try
        {
            restClient.remove(id, site1Path);
        }
        catch (RestClientException e)
        {
            fail("Current user should be authorized to remove resource: " + id);
        }

        // add asset back to site
        restClient.addAssetToFolder(site1Path, id);

        // add it to a template
        String templateId = testSiteData.template1.getId();
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(templateId, 5, "widget5", id, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        restClient.createAssetWidgetRelationship(awRel);

        // should be able to remove resource
        try
        {
            restClient.remove(id, site1Path);
        }
        catch (RestClientException e)
        {
            fail("Should able to remove resource: " + id + ".  It is used by a template");
        }

        // remove the asset from the template
        restClient.clearAssetWidgetRelationship(awRel);

        // add asset back to site
        restClient.addAssetToFolder(site1Path, id);

        // add it to a page
        String pageId = testSiteData.createPage("testPage", site1Path, templateId);
        pageCleaner.add(pageId);
        awRel = new PSAssetWidgetRelationship(pageId, 5, "widget5", id, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        restClient.createAssetWidgetRelationship(awRel);

        // transition the page to pending
        wfClient.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);

        // should not be able to remove asset
        try
        {
            restClient.remove(id, site1Path);
            fail("Should not be able to remove resource: " + id + ".  It is used by an approved page");
        }
        catch (RestClientException e)
        {
            assertTrue(e.getStatus() == 400 && e.getResponseBody().contains("resource.removeApprovedPages"));
        }

        // transition the page to review
        wfClient.checkOut(pageId);
        wfClient.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_ARCHIVE);

        // should be able to remove resource
        try
        {
            restClient.remove(id, site1Path);

            getItemWorkflowServiceRestClient().checkOut(pageId);

            restClient.clearAssetWidgetRelationship(awRel);
        }
        catch (RestClientException e)
        {
            fail("Resource: " + id + " should have been removed");
        }
    }

    @Test
    public void test120ForceRemoveResource() throws Exception
    {
        restClient.switchCommunity(1002);

        String site1Path = testSiteData.site1.getFolderPath();

        PSAsset asset = restClient.createAsset("testAsset", site1Path);
        String id = asset.getId();
        assetCleaner.add(id);

        // add asset to site
        restClient.addAssetToFolder(site1Path, id);

        // add it to a page
        String pageId = testSiteData.createPage("testPage", site1Path, testSiteData.template1.getId());
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(pageId, 5, "widget5", id, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        restClient.createAssetWidgetRelationship(awRel);

        // transition the page to pending
        getItemWorkflowServiceRestClient().transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);

        // force remove
        try
        {
            restClient.forceRemove(id, site1Path);
            assetCleaner.remove(id);
        }
        catch (RestClientException e)
        {
            fail("Resource: " + id + " should have been removed");
        }
    }

    @Ignore("Not ready yet. Ignore it to fix the build.")
    @Test
    public void test130GetForms() throws Exception
    {
        restClient.switchCommunity(1002);

        // get current number of forms
        Collection<PSFormSummary> forms = restClient.getForms();
        int totalForms = forms.size();

        // create a form
        PSAsset form1 = new PSAsset();
        form1.getFields().put("sys_title", "testForm1");
        form1.getFields().put("formtitle", "TestFormOne");
        form1.setType(PSAssetRestService.FORM_CONTENT_TYPE);
        form1.getFields().put("description", "TestForm1");
        form1 = restClient.save(form1);
        String form1Id = form1.getId();
        assetCleaner.add(form1Id);

        // should be no additional forms
        forms = restClient.getForms();
        assertEquals(totalForms, forms.size());

        // transition form to Live state
        getItemWorkflowServiceRestClient().transition(form1Id, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        getItemWorkflowServiceRestClient().transition(form1Id, "forcetolive");

        // should be one additional form
        forms = restClient.getForms();
        assertEquals(totalForms + 1, forms.size());

        boolean foundForm1 = false;
        Iterator<PSFormSummary> iter = forms.iterator();
        while (iter.hasNext())
        {
            String id = iter.next().getId();
            if (form1Id.equals(id))
            {
                foundForm1 = true;
                break;
            }
        }
        assertTrue(foundForm1);

        // create another form
        PSAsset form2 = new PSAsset();
        form2.getFields().put("sys_title", "testForm2");
        form2.getFields().put("formtitle", "TestFormTwo");
        form2.setType(PSAssetRestService.FORM_CONTENT_TYPE);
        form2.getFields().put("description", "TestForm2");
        form2 = restClient.save(form2);
        String form2Id = form2.getId();
        assetCleaner.add(form2Id);

        // transition form to Live state
        getItemWorkflowServiceRestClient().transition(form2Id, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        getItemWorkflowServiceRestClient().transition(form2Id, "forcetolive");

        // should be two additional forms
        forms = restClient.getForms();
        assertEquals(totalForms + 2, forms.size());
        foundForm1 = false;
        boolean foundForm2 = false;
        iter = forms.iterator();
        while (iter.hasNext())
        {
            String id = iter.next().getId();
            if (form1Id.equals(id))
            {
                foundForm1 = true;
            }

            if (form2Id.equals(id))
            {
                foundForm2 = true;
            }
        }
        assertTrue(foundForm1);
        assertTrue(foundForm2);

        // transition form to Quick Edit state (from Live)
        getItemWorkflowServiceRestClient().checkOut(form2Id);

        // should still be two additional forms
        forms = restClient.getForms();
        assertEquals(totalForms + 2, forms.size());
        foundForm1 = false;
        foundForm2 = false;
        iter = forms.iterator();
        while (iter.hasNext())
        {
            String id = iter.next().getId();
            if (form1Id.equals(id))
            {
                foundForm1 = true;
            }

            if (form2Id.equals(id))
            {
                foundForm2 = true;
            }
        }
        assertTrue(foundForm1);
        assertTrue(foundForm2);
    }

    @After
    public void tearDown()
    {
        assetCleaner.clean();
        pageCleaner.clean();
        templateCleaner.clean();
        folderCleaner.clean();
    }

    @AfterClass
    public static void cleanup() throws Exception
    {
        testSiteData.tearDown();
    }

    private void validateWithProperty(String pageId, PSWidgetItem wi, String templateId) throws Exception
    {
        PSPage page = getPageRestClient().load(pageId);
        page.getRegionBranches().setRegionWidgets("Test", asList(wi));
        getPageRestClient().save(page);

        PSTemplate template = getTemplateClient().loadTemplate(templateId);
        template.getRegionTree().setRegionWidgetAssociations(page.getRegionBranches().getRegionWidgetAssociations());
        getTemplateClient().saveTemplate(template);

        validateGetAssetEditUrls(pageId, PSAssetEditUrlRequest.PAGE_PARENT, wi, pageId);
        validateGetAssetEditUrls(templateId, PSAssetEditUrlRequest.TEMPLATE_PARENT, wi, pageId);
    }

    private void validateGetAssetEditUrls(String parentId, String parentType, PSWidgetItem widget, String assetId)
            throws Exception
    {
        // generate the create request url
        PSAssetEditUrlRequest req = new PSAssetEditUrlRequest();
        req.setParentId(parentId);
        req.setType(parentType);
        req.setWidgetId(widget.getId());
        PSContentEditCriteria editCriteria = getAssetEditUrlRestClient().getContentEditCriteria(req);
        String createUrl = editCriteria.getUrl();
        assertFalse(StringUtils.isBlank(createUrl));
        if (!editCriteria.getProducesResource())
        {
            assertFalse(StringUtils.isBlank(editCriteria.getContentName()));
        }
        String hiddenProp = null;
        // List<PSWidgetProperty> widgetProps = widget.getProperties();
        // for (PSWidgetProperty widgetProp : widgetProps)
        // {
        // String name = widgetProp.getName();
        // if (name.startsWith(PSWidgetProperty.HIDE_FIELD_PREFIX))
        // {
        // String value = widgetProp.getValue();
        // if (value.equals("true") || value.equals("yes"))
        // {
        // hiddenProp =
        // name.substring(PSWidgetProperty.HIDE_FIELD_PREFIX.length());
        // break;
        // }
        // }
        // }

        validateUrl(createUrl, hiddenProp, true);

        // generate the edit request url
        req.setAssetId(assetId);
        editCriteria = getAssetEditUrlRestClient().getContentEditCriteria(req);
        String url = editCriteria.getUrl();
        assertFalse(StringUtils.isBlank(url));
        validateUrl(url, hiddenProp, false);
        if (!editCriteria.getProducesResource())
        {
            assertFalse(StringUtils.isNotBlank(editCriteria.getContentName()));
        }

    }

    private void validateUrl(String url, String hiddenProp, boolean isCreate)
    {
        if (hiddenProp == null)
        {
            assertTrue(url.indexOf("sys_view=" + IPSConstants.SYS_HIDDEN_FIELDS_VIEW_NAME) != -1);
        }
        else
        {
            assertTrue(url.indexOf("sys_view=" + IPSConstants.SYS_HIDDEN_FIELDS_VIEW_NAME + hiddenProp) != -1);
        }

        if (isCreate)
        {
            assertTrue(url.indexOf("sys_revision") == -1);
            assertTrue(url.indexOf("sys_contentid") == -1);
        }
        else
        {
            assertTrue(url.indexOf("sys_revision") != -1);
            assertTrue(url.indexOf("sys_contentid") != -1);
        }
    }

    private PSPageRestClient getPageRestClient() throws Exception
    {
        PSPageRestClient client = new PSPageRestClient(baseUrl);
        setupClient(client);
        return client;
    }

    private PSTemplateServiceClient getTemplateClient() throws Exception
    {
        PSTemplateServiceClient client = new PSTemplateServiceClient(baseUrl);
        setupClient(client);
        return client;
    }

    private PSAssetServiceRestClient getAssetEditUrlRestClient() throws Exception
    {
        PSAssetServiceRestClient client = new PSAssetServiceRestClient(baseUrl);
        setupClient(client);
        return client;
    }

    private PSWidgetRestClient getWidgetClient() throws Exception
    {
        PSWidgetRestClient client = new PSWidgetRestClient(baseUrl);
        setupClient(client);
        return client;
    }

    private PSPathServiceRestClient getPathServiceRestClient() throws Exception
    {
        PSPathServiceRestClient client = new PSPathServiceRestClient(baseUrl);
        setupClient(client);
        return client;
    }

    private PSItemWorkflowServiceRestClient getItemWorkflowServiceRestClient() throws Exception
    {
        PSItemWorkflowServiceRestClient client = new PSItemWorkflowServiceRestClient(baseUrl);
        setupClient(client);
        return client;
    }
}
