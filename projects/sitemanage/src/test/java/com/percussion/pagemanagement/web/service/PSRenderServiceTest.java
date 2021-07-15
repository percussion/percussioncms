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
package com.percussion.pagemanagement.web.service;

import static com.percussion.pagemanagement.data.PSRegionTreeUtils.getChildRegions;
import static com.percussion.pagemanagement.parser.PSTemplateRegionParser.parse;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.contains;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetResourceType;
import com.percussion.pagemanagement.data.PSAbstractRegion;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSRegionTree;
import com.percussion.pagemanagement.data.PSRegionWidgetAssociations;
import com.percussion.pagemanagement.data.PSRenderResult;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.data.PSAbstractPersistantObject;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.share.test.PSTestUtils;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/**
 * Tests render a page and a template.
 * 
 * @author adamgent
 *
 */
@Category(IntegrationTest.class)
public class PSRenderServiceTest
{
    private static final String HTML_CONTENT = "TestHTML";
    private static final String HTML_CONTENT_2 = "TestHTML_2";
    
    private static final String CONTAINER_REGION_ID = "container";

    private static final String MY_WIDGET_REGION = "my-widget-region";

    static PSTestSiteData testSiteData;

    static PSRenderServiceClient renderClient;

    private static String pageId;
    private static PSTemplate template;
    private static PSTemplate template2;
    private static PSTemplate template3;
    private static PSPage page;
    private static PSPage page_2;
    private static PSAsset asset;
    private static PSAsset asset_2;

    @BeforeClass
    public static void setUp() throws Exception
    {
        testSiteData = new PSTestSiteData();
        renderClient = new PSRenderServiceClient();
        PSRestTestCase.setupClient(renderClient);
        testSiteData = new PSTestSiteData();
        testSiteData.setUp();
        pageId = testSiteData.createPage("MyPage", testSiteData.site1.getFolderPath(), testSiteData.template1.getId());
        page = testSiteData.getPageRestClient().load(pageId);
        
        String templateId = testSiteData.template1.getId();
        
        template =setupTemplate(templateId);
        template2 = setupTemplate(testSiteData.template2.getId());
        template3 = testSiteData.getTemplateServiceClient().createTemplate("SiteTemplateServiceTest3", testSiteData.baseTemplateId);
        testSiteData.assignTemplatesToSite(testSiteData.site1.getId(), template3.getId());
        
        String pageId_2 = testSiteData.createPage("MyPage_2", testSiteData.site1.getFolderPath(), template2.getId());
        page_2 = testSiteData.getPageRestClient().load(pageId_2);
        
        asset = createHtmlSharedAsset("MyAsset", HTML_CONTENT);
        asset_2 = createHtmlSharedAsset("MyAsset_2", HTML_CONTENT_2);
    }

    private static PSAsset createHtmlSharedAsset(String title, String content)
    {
        PSAsset htmlAsset = new PSAsset();
        htmlAsset.getFields().put("sys_title", title);
        htmlAsset.setType("percRawHtmlAsset");
        htmlAsset.getFields().put("html", content);
        htmlAsset.setFolderPaths(asList("//Folders/$System$/Assets"));
        htmlAsset = testSiteData.saveAsset(htmlAsset);
        return htmlAsset;
    }
    
    public static PSTemplate setupTemplate(String templateId) {
        PSTemplate template = testSiteData.getTemplateServiceClient().loadTemplate(templateId);
        template.setTheme("percussion");
        assertNotNull(template);
        PSRegionTree regionTree = template.getRegionTree();
        assertNotNull(regionTree);
        PSWidgetItem wi = testSiteData.createWidgetItem("TESTME", PSTestSiteData.TEST_WIDGET_DEFINITION);
        regionTree.setRegionWidgets(MY_WIDGET_REGION, asList(wi));
        
        regionTree.setRootRegion(parse(null, getHtml("widget_template.html")).getRootNode());
        template = testSiteData.getTemplateServiceClient().save(template);
        
        
        return template;
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        testSiteData.tearDown();
    }

    @Test
    public void testRenderPageRegion() throws Exception 
    {
        PSRenderResult result = renderClient.renderRegion(page, CONTAINER_REGION_ID);
        assertRenderResult(result);
    }
    
    @Test(expected=Exception.class)
    public void testRenderPageRegionValidationFailure() throws Exception 
    {
        PSPage badPage = new PSPage();
        renderClient.renderRegion(badPage, "rid-1");
        
    }
    
    @Test
    public void testRenderTemplateRegion() throws Exception 
    {
        PSRenderResult result = renderClient.renderRegion(template, CONTAINER_REGION_ID);
        assertRenderResult(result);
    }
    
    @Test(expected=Exception.class)
    public void testRenderTemplateRegionValidationFailure() throws Exception 
    {
        PSTemplate badTemplate = new PSTemplate();
        renderClient.renderRegion(badTemplate, "rid-1");
        
    }
    
    
    private void assertRenderResult(PSRenderResult result) {
        assertNotNull("result should not be null", result);
        assertEquals(CONTAINER_REGION_ID, result.getRegionId());
        assertNotNull(result.getResult());
    }
    
    @Test
    public void testRenderPage() throws Exception
    {
        String actual = renderClient.renderPage(pageId);
        assertNotNull(actual);
        assertTrue(isNotBlank(actual));
        log.info("Rendering: " + actual);
    }

    @Test
    public void testRenderPageForEdit() throws Exception
    {
        String actual = renderClient.renderPageForEdit(pageId);
        assertNotNull(actual);
        assertTrue(isNotBlank(actual));
        log.info("Rendering: " + actual);
    }

    @Test
    public void testRenderTemplate() throws Exception
    {
        String templateId = testSiteData.template1.getId();
        String actual = renderClient.renderTemplate(templateId);
        assertNotNull(actual);
        assertTrue(isNotBlank(actual));
        log.info("Rendering: " + actual);
    }
    
    @Test
    public void testRenderWidgetItem() throws Exception
    {

        String id = template.getRegionTree().getRegionWidgetsMap().get(MY_WIDGET_REGION).get(0).getId();
        assertTrue("WidgetItem should have an Id.",isNotBlank(id));
        
        assertNotNull(template);
        String actual = renderClient.renderTemplate(template.getId());
        
        assertTrue(isNotBlank(actual));
        assertTrue("Widget should render properly", contains(actual, "TESTME"));
        log.info("Rendering: " + actual);
    }
    
    @Test
    public void testRenderPageWithWidgetItemButTemplateWillWidgetShow() throws Exception
    {
        
        assertNotNull(page.getRegionBranches());
        PSRegionBranches branches = page.getRegionBranches();
        PSWidgetItem wi = testSiteData.createWidgetItem("adam", PSTestSiteData.TEST_WIDGET_DEFINITION);
        //wi.getProperties().put("num_entries", 10);
        //wi.getProperties().put("day_of_week", "tuesday");
        Map<String,Object> expected = new HashMap<String, Object>(wi.getProperties());
        branches.setRegionWidgets(MY_WIDGET_REGION, asList(wi));
        
        PSPage newPage = testSiteData.getPageRestClient().save(page);
        wi = newPage.getRegionBranches().getRegionWidgetsMap().get(MY_WIDGET_REGION).get(0);
        assertEquals("Expect properties to be saved and returned", expected, wi.getProperties());
        assertTrue("WidgetItem should have an Id.",isNotBlank(wi.getId()));
        
        String actual = renderClient.renderPage(newPage.getId());
        
        assertTrue(isNotBlank(actual));
        assertTrue("Widget should render properly", contains(actual, "TESTME"));
        assertTrue("Widget should have decoration", contains(actual, "perc-widget"));
        log.info("Rendering: " + actual);
    }
    
    
    /**
     * Tests the template widget/asset cannot be override by the widget/asset changes.
     * This is true when the template already has a widget in a region, which 
     * cannot be overwrite by a page's widget on the same region.
     * 
     * @throws Exception
     */
    @Test
    public void testRenderPageCannotOverrideTemplateWidgetItemAndAsset() throws Exception
    {
        log.info("testRenderPageWithWidgetItemAndAsset");
        
        String beforeChangePage = renderClient.renderPage(pageId);
        
        PSPage page = testSiteData.getPageRestClient().load(pageId);
        
        assertNotNull(page.getRegionBranches());
        PSRegionBranches branches = page.getRegionBranches();
        PSWidgetItem wi = testSiteData.createWidgetItem("adam", "percRawHtml" );
        branches.setRegionWidgets(MY_WIDGET_REGION, asList(wi));
        page.setRegionBranches(branches);
        
        PSPage newPage = testSiteData.getPageRestClient().save(page);
        String widgetItemId = assertWidgetRegion(newPage, MY_WIDGET_REGION);

        PSAssetWidgetRelationship awRel = createAssetRelationship(newPage, widgetItemId, "adam", "percRawHtml", asset.getId());
        
        String afterChangePage = renderClient.renderPage(newPage.getId());
        
        log.info("Rendering: " + afterChangePage);
        
        // render before page change should be the same after page change
        // as the region cannot be overrite by the page
        assertTrue(beforeChangePage.equals(afterChangePage));
        
        PSRenderResult renderResult = renderClient.renderRegion(newPage, MY_WIDGET_REGION);
        String renderRegion = renderResult.getResult();

        // render whole page is different render a specific region
        assertFalse(renderRegion.equals(afterChangePage));
        
        testSiteData.getAssetRestClient().clearAssetWidgetRelationship(awRel);
    }

    /**
     * Render a page with page's asset on a template widget
     * 
     * @throws Exception
     */
    @Test
    public void testRenderPageWithPageAsset() throws Exception
    {
        log.info("testRenderPageWithPageAsset");
        
        PSPair<PSTemplate, PSWidgetItem> pair = addHtmlWidget(template2, MY_WIDGET_REGION);
        template2 = pair.getFirst();
        
        String beforeChangePage = renderClient.renderPage(page_2.getId());
        
        PSWidgetItem wi = pair.getSecond();
        PSAssetWidgetRelationship awRel = createAssetRelationship(page_2, wi.getId(), wi.getName(), "percRawHtml", asset_2.getId());
        
        String afterChangePage = renderClient.renderPage(page_2.getId());
        
        // clean up the relationship created above.
        testSiteData.getAssetRestClient().clearAssetWidgetRelationship(awRel); 
        
        // the rendered page result does NOT contain the asset_2's content before.
        assertFalse(beforeChangePage.indexOf(HTML_CONTENT_2) >= 0);

        assertFalse(beforeChangePage.equals(afterChangePage));

        // the rendered result contains the asset_2's content, which is linked to the page
        assertTrue(afterChangePage.indexOf(HTML_CONTENT_2) >= 0);
    }

    @Test
    public void testRenderTemplateWithWidgetItemAndAsset() throws Exception
    {
        log.info("testRenderTemplateWithWidgetItemAndAsset");
        
        template2 = addHtmlWidget(template2, MY_WIDGET_REGION).getFirst();
        
        String widgetItemId = assertWidgetRegion(template2.getRegionTree(), MY_WIDGET_REGION);

        PSAssetWidgetRelationship awRel = createAssetRelationship(template2, widgetItemId, "adam", "percRawHtml", asset.getId());
        
        String actual = renderClient.renderTemplate(template2.getId());
        
        log.info("Rendering: " + actual);
        
        assertAsset(actual);
        
        PSRenderResult renderResult = renderClient.renderRegion(template2, MY_WIDGET_REGION);
        actual = renderResult.getResult();
        
        assertAsset(actual);
        
        //clear the asset from the template to allow asset cleanup
        testSiteData.getAssetRestClient().clearAssetWidgetRelationship(awRel);        
    }

    /**
     * Adds a HTML widget into a given template and a region.
     * 
     * @param template the template, not <code>null</code>.
     * @param regionId the regiion ID, not blank.
     * 
     * @return the modified template and the added widget, never <code>null</code>.
     */
    private PSPair<PSTemplate, PSWidgetItem> addHtmlWidget(PSTemplate template, String regionId)
    {
        assertNotNull(template.getRegionTree());
        PSRegionTree tree = template.getRegionTree();
        PSWidgetItem wi = testSiteData.createWidgetItem("adam", "percRawHtml");
        tree.setRegionWidgets(regionId, asList(wi));
        template.setRegionTree(tree);
        
        template = testSiteData.getTemplateServiceClient().save(template);
        
        wi = assertWidgetItemFromRegion(template.getRegionTree(), regionId);
        
        PSPair<PSTemplate, PSWidgetItem> pair = new PSPair<PSTemplate, PSWidgetItem>(template, wi);
        return pair;
    }
    
    @Ignore("The default CSS is no longer valid")
    public void testRenderCss() throws Exception
    {
        String actual = renderClient.renderTemplate(template3.getId());
        
        assertTrue(contains(actual, "percussion/perc_theme.css"));
    }

    private void assertAsset(String actual)
    {
        assertTrue(isNotBlank(actual));
        assertTrue("Widget should render content properly. Did not find 'TestHTML' marker, actual: " 
                + actual
                , contains(actual, HTML_CONTENT));
        assertTrue("Widget should have decoration", contains(actual, "perc-widget"));
    }

    private PSAssetWidgetRelationship createAssetRelationship(PSAbstractPersistantObject newPage,
            String widgetItemId, String widgetName, String widgetDefName, String assetId)
    {
        PSAssetWidgetRelationship awr = new PSAssetWidgetRelationship();
        awr.setAssetId(assetId);
        awr.setAssetOrder(0);
        awr.setWidgetId(Long.parseLong(widgetItemId));
        awr.setWidgetName(widgetDefName);
        awr.setWidgetInstanceName(widgetName);
        awr.setOwnerId(newPage.getId());
        awr.setResourceType(PSAssetResourceType.shared);
        String rid = testSiteData.getAssetRestClient().createAssetWidgetRelationship(awr);
        assertNotNull("rid", rid);
        log.info("RID: " + rid);
        
        return awr;
    }
    
    @Ignore("This test is not really needed because templates override pages")
    @Test
    public void testRenderPageOverrides() throws Exception
    {
        log.info("testRenderPageOverrides");
        String pageId = testSiteData.createPage("MyPagePageOverrides", testSiteData.site1.getFolderPath(), testSiteData.template1.getId());
        PSPage page = testSiteData.getPageRestClient().load(pageId);
        
        
        assertNotNull(page.getRegionBranches());
        PSRegionBranches branches = page.getRegionBranches();
        
        PSWidgetItem wi = testSiteData.createWidgetItem("adam", "percRawHtml" );
        String html = getHtml("region_override.html");
        
        PSRegion parsed = renderClient.parse(html);
        parsed.setRegionId(MY_WIDGET_REGION);
        
        branches.setRegionWidgets("page-subregion", asList(wi));
        branches.setRegions(asList(parsed));
        page.setRegionBranches(branches);
        
        PSPage newPage = testSiteData.getPageRestClient().save(page);
        String widgetItemId = assertWidgetRegion(newPage, "page-subregion");
        
        PSAssetWidgetRelationship awRel = createAssetRelationship(newPage, widgetItemId, "adam", "percRawHtml", asset.getId());
        
        String actual = renderClient.renderPage(newPage.getId());
        
        log.info("Rendering: " + actual);
        
        assertTrue(isNotBlank(actual));
        assertTrue("Widget should render content properly", contains(actual, HTML_CONTENT));
        assertTrue("Should render page subregion", contains(actual, "page-subregion"));
        assertTrue("Widget should have decoration", contains(actual, "perc-widget"));
        
        //clear the asset from the template to allow asset cleanup
        testSiteData.getAssetRestClient().clearAssetWidgetRelationship(awRel);        
    }

    private String assertWidgetRegion(PSPage newPage, String regionId)
    {
        Map<String, List<PSWidgetItem>> wr = newPage.getRegionBranches().getRegionWidgetsMap();
        log.debug("WidgetRegions: " + wr);
        assertNotNull("Should have widget region", wr.get(regionId));
        String widgetItemId = wr.get(regionId).get(0).getId();
        assertEquals(1, wr.get(regionId).size());
        assertEquals("percRawHtml", wr.get(regionId).get(0).getDefinitionId());
        return widgetItemId;
    }
    
    private String assertWidgetRegion(PSRegionWidgetAssociations assocations, String regionId)
    {
        Map<String, List<PSWidgetItem>> wr = assocations.getRegionWidgetsMap();
        log.debug("WidgetRegions: " + wr);
        assertNotNull("Should have widget region", wr.get(regionId));
        String widgetItemId = wr.get(regionId).get(0).getId();
        assertEquals(1, wr.get(regionId).size());
        assertEquals("percRawHtml", wr.get(regionId).get(0).getDefinitionId());
        return widgetItemId;
    }

    private PSWidgetItem assertWidgetItemFromRegion(PSRegionWidgetAssociations assocations, String regionId)
    {
        Map<String, List<PSWidgetItem>> wr = assocations.getRegionWidgetsMap();
        log.debug("WidgetRegions: " + wr);
        assertNotNull("Should have widget region", wr.get(regionId));
        PSWidgetItem wi = wr.get(regionId).get(0);
        assertEquals(1, wr.get(regionId).size());
        assertEquals("percRawHtml", wr.get(regionId).get(0).getDefinitionId());
        return wi;
    }

    @Test
    public void testParse() throws Exception {
         PSRegion region = renderClient.parse(getHtml("widget_template.html"));
         List<? extends PSAbstractRegion> children = getChildRegions(region);
         assertNotNull(children);
         assertEquals(CONTAINER_REGION_ID, children.get(0).getRegionId());
         assertEquals(MY_WIDGET_REGION, getChildRegions(children.get(0)).get(0).getRegionId());
         
         
         
    }
    
    @Test
    public void testErik() throws Exception {
         String erik = "<div class=\"perc-region\" id=\"container\">" +
         		"<div class=\"perc-vertical\">" +
         		"<div class=\"perc-region perc-region-leaf\" id=\"mainRegion\">" +
         		"<div class=\"perc-vertical\"></div></div></div></div>";
         
         PSRegion region = renderClient.parse(erik);
         List<? extends PSAbstractRegion> children = getChildRegions(region);
         assertNotNull(children);
         assertEquals(CONTAINER_REGION_ID, children.get(0).getRegionId());
         assertEquals("mainRegion", getChildRegions(children.get(0)).get(0).getRegionId());
         log.debug(PSSerializerUtils.marshal(region));
         
    }
    
    private static String getHtml(String name) {
        return PSTestUtils.resourceToString(PSRenderServiceTest.class, name);
    }

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSRenderServiceTest.class);
}
