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

package com.percussion.pagemanagement.service;

import static com.percussion.assetmanagement.service.impl.PSPreviewPageUtils.getOrphanedAssetsSummaries;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSOrphanedAssetSummary;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetResourceType;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.assetmanagement.service.impl.PSWidgetAssetRelationshipService;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.pagemanagement.assembler.PSWidgetContentFinderUtils;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.dao.IPSPageDaoHelper;
import com.percussion.pagemanagement.dao.IPSTemplateDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionTree;
import com.percussion.pagemanagement.data.PSRegionWidgets;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.services.memory.PSCacheAccessLocator;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.IPSSystemWs;

import org.junit.Ignore;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
public class PSPageServiceTest extends PSServletTestCase
{

    private PSSiteDataServletTestCaseFixture fixture;

    @Override
    public void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.setUp();
        fixture.pageCleaner.add(fixture.site1.getFolderPath() + "/Page1");
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception
    {
        fixture.tearDown();
    }
    
    @Test
    public void test100FindPagesByTemplateId() throws Exception
    {
        String templateId = fixture.template1.getId();
        
        // no pages should be using the template
        Collection<Integer> pageIds = pageDaoHelper.findPageIdsByTemplate(templateId);
        assertTrue(pageIds.isEmpty());
        
        String siteFolderPath = fixture.site1.getFolderPath();
        
        // create some pages using the template
        String page1Id = createPage("Page1", "Page 1", templateId, siteFolderPath, "TestLink", "testurl.file", "","");
        assertNotNull(page1Id);
        
        String page2Id = createPage("Page2", "Page 2", templateId, siteFolderPath, "TestLink", "testurl.file", "","");
        assertNotNull(page2Id);
        
        // find the page
        PSPage page1 = pageService.find(page1Id);
        assertNotNull(page1);
        assertEquals("Page1", page1.getName());
        
        pageService.findPagesByTemplate(templateId, 1, 5, "name", "asc", null);
        
    }
    
    @Test
    public void test110CopyPage() throws Exception
    {
        // create the original page
        String name = "Page1";
        String title = "Page1";
        String folderPath = fixture.site1.getFolderPath();
        String linkTitle = "TestLink";
        String url = "testurl.file";
        String pageId = createPage(name, title, fixture.template1.getId(), folderPath, linkTitle, url, "true", "This is Page 1.");
        assertNotNull(pageId);
        
        // create a local asset
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", "LocalAsset");
        asset.setType("percRawHtmlAsset");
        asset.getFields().put("html", "TestHTML");
        asset.setFolderPaths(asList("//Folders"));
        String localAssetId = fixture.saveAsset(asset).getId();

        // we don't want the cleaner to auto remove the local asset
        // because auto removing the page will auto remove the asset
        // otherwise auto removing local asset that has already been
        // auto removed by auto removing the page will fail
        fixture.assetCleaner.remove(localAssetId);

        // add the local asset to the original page
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(pageId, 5, "widget5", localAssetId, 1);
        fixture.getAssetService().createAssetWidgetRelationship(awRel);
        
        // create a shared asset
        asset = new PSAsset();
        asset.getFields().put("sys_title", "SharedAsset");
        asset.setType("percRawHtmlAsset");
        asset.getFields().put("html", "TestHTML");
        asset.setFolderPaths(asList("//Folders"));
        String sharedAssetId = fixture.saveAsset(asset).getId();

        // add the shared asset to the original page
        awRel = new PSAssetWidgetRelationship(pageId, 6, "widget6", sharedAssetId, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        fixture.getAssetService().createAssetWidgetRelationship(awRel);
        
        
        
        // copy the original page
        String copiedPagePath = pageService.copy(pageId, false);
        PSPage copiedPage     = pageService.findPageByPath(copiedPagePath);
        
        // CM-126: pageService.findPageByPath(copiedPagePath) may return null
        assertNotNull(copiedPage);
        
        // add copied page to cleaner to be auto removed
        fixture.pageCleaner.add(copiedPagePath);
        
        // original and copied page IDs must be different
        assertFalse(pageId.equals(copiedPage.getId()));
        
        // verify local assets are different in original and copied pages
        // we only added one in original page
        Set<String> copiedLocalAssetIds  = widgetService.getLocalAssets(copiedPage.getId());
        for(String id : copiedLocalAssetIds) {
            assertFalse(id.equals(localAssetId));
        }

        // verify shared assets are the same in original and copied pages
        // we only added one in original page
        Set<String> copiedSharedAssetIds = widgetService.getSharedAssets(copiedPage.getId());
        for(String id : copiedSharedAssetIds) {
            assertTrue(id.equals(sharedAssetId));
        }
    }
    
    @Test
    public void test120ChangeTemplate() throws Exception
    {
        // create the page
        String name = "Page1";
        String title = "Page1";
        String folderPath = fixture.site1.getFolderPath();
        String linkTitle = "TestLink";
        String url = "testurl.file";
        PSPage page = new PSPage();
        
        page.setFolderPath(folderPath);
        page.setName(name);
        page.setTitle(title);
        page.setTemplateId(fixture.template1.getId());
        page.setFolderPath(folderPath);
        page.setLinkTitle(linkTitle);
        page.setNoindex("true");
        page.setDescription(title);
        
        page = fixture.createPage(page);
        
        assertNotNull(page);
        assertEquals(fixture.template1.getContentMigrationVersion(), page.getTemplateContentMigrationVersion());
        assertEquals(page.getTemplateId(), fixture.template1.getId());
        
        // create another template
        PSTemplateSummary templateSum2 = fixture.createTemplate("templateToChangeTo");
        assertNotNull(templateSum2);
        assertEquals(templateSum2.getContentMigrationVersion(), fixture.template1.getContentMigrationVersion());
        
        
        // Update the migration version of the 2nd template
        PSTemplate template2 = fixture.getTemplateService().load(templateSum2.getId());
        String newVersion = "2";
        template2.setContentMigrationVersion(newVersion);
        // need to save the template through the dao directly, as the content migration version is overriden in the 
        // service call
        template2 = templateDao.save(template2);
        assertEquals(newVersion, template2.getContentMigrationVersion());
        assertFalse(newVersion.equals(page.getTemplateContentMigrationVersion()));
        
        // change template
        fixture.getPageService().changeTemplate(page.getId(), template2.getId());
        page = fixture.getPageService().find(page.getId());
        assertEquals(template2.getId(), page.getTemplateId());
        assertEquals(template2.getContentMigrationVersion(), page.getTemplateContentMigrationVersion());
    }

    @Test
    public void test130SaveDelete() throws Exception
    {
        String assetId = null;
        PSAssetWidgetRelationship awRel = null;

        String name = "Page1";
        String title = "Page 1";
        String folderPath = fixture.site1.getFolderPath();
        String linkTitle = "TestLink";

        // create a page
        String pageId = createPage("Page1", "Page 1", fixture.template1.getId(), folderPath, "TestLink",
                "testurl.file", "true", "This is Page 1.");
        assertNotNull(pageId);

        // make sure it was saved to the correct folder
        boolean pageExists = false;
        IPSContentWs contentWs = PSContentWsLocator.getContentWebservice();
        List<PSItemSummary> items = contentWs.findFolderChildren(folderPath, true);
        for (PSItemSummary item : items)
        {
            if (item.getGUID().toString().equals(pageId))
            {
                pageExists = true;
                break;
            }
        }
        assertTrue("Could not find page " + name + " in folder " + folderPath, pageExists);

        // find the page
        PSPage page1 = pageService.find(pageId);
        assertNotNull(page1);
        assertEquals(name, page1.getName());
        assertEquals(title, page1.getTitle());
        assertEquals(fixture.template1.getId(), page1.getTemplateId());
        assertEquals(linkTitle, page1.getLinkTitle());

        PSPage page2 = pageService.findPage(name, folderPath);
        assertEquals(page2, page1);

        // cannot create another page with the same name in the same folder
        try
        {
            PSPage badPage = new PSPage();
            badPage.setName(name);
            badPage.setTitle(title);
            badPage.setTemplateId(fixture.template1.getId());
            badPage.setFolderPath(folderPath);
            badPage.setLinkTitle("dummy");
            pageService.save(badPage);
            fail("Should not be able to create page with same name in " + "same folder");
        }
        catch (Exception e)
        {
            assertTrue("Bean validation exception expected:", e instanceof PSBeanValidationException);
        }

        // create a local asset
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", "LocalAsset");
        asset.setType("percRawHtmlAsset");
        asset.getFields().put("html", "TestHTML");
        asset.setFolderPaths(asList("//Folders"));
        assetId = fixture.saveAsset(asset).getId();

        // add asset to the page
        awRel = new PSAssetWidgetRelationship(pageId, 5, "widget5", assetId, 1);
        fixture.getAssetService().createAssetWidgetRelationship(awRel);

        // wait before deleting (to allow for search index queue processing)
        Thread.sleep(8000);

        // delete the page
        pageService.delete(pageId);
        
        Thread.sleep(8000);
        /*
         * If we successfully deleted then we can remove the page and
         * asset from the cleaner.
         */
        fixture.pageCleaner.remove(folderPath + "/Page1");
        fixture.assetCleaner.remove(assetId);

        try
        {
            pageService.find(pageId);
            fail("Should not be able to find page.");
        }
        catch (Exception e)
        {
        }
        
        try
        {
            fixture.getAssetService().find(assetId);
            fail("Asset: " + assetId + " should have been deleted during deletion of Page: " + pageId);
        }
        catch (Exception e)
        {
            // expected
        }

    }

    @Test
    public void test140Update() throws Exception
    {
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship();

        String templateId = fixture.template1.getId();
        PSTemplateSummary template2 = fixture.createTemplateWithSite("TestTemplate2", fixture.site1.getId());

        String name = "Page1";
        String title = "Page 1";
        String folderPath = fixture.site1.getFolderPath();
        String linkTitle = "TestLink";
        String noindex = "true";
        String description = "This is Page 1.";

        // create a page
        String pageId = createPage("Page1", "Page 1", templateId, folderPath, "TestLink", 
                "testurl.file", noindex, description);
        assertNotNull(pageId);

        // find the page
        PSPage page1 = pageService.find(pageId);
        assertNotNull(page1);
        assertEquals(name, page1.getName());
        assertEquals(title, page1.getTitle());
        assertEquals(templateId, page1.getTemplateId());
        assertEquals(linkTitle, page1.getLinkTitle());
        assertEquals(noindex, page1.getNoindex());
        assertEquals(description, page1.getDescription());
        assertEquals("0", page1.getTemplateContentMigrationVersion());

        PSPage page2 = pageService.findPage(name, folderPath);
        assertEquals(page2, page1);

        // update the page
        PSPage page1Mod = pageService.find(pageId);
        assertEquals(page1Mod, page1);
        page1Mod.setTemplateId(template2.getId());
        String templateVersion = "1";
        page1Mod.setTemplateContentMigrationVersion("1");
        pageService.save(page1Mod);
        page1Mod = pageService.find(page1Mod.getId());
        assertFalse("Page was not updated correctly", page1Mod.equals(page1));
        assertEquals(templateVersion, page1Mod.getTemplateContentMigrationVersion());
        
        // create a local asset
        String assetId = createRawHtmlAsset();
        String assetId_2 = createRawHtmlAsset();

        // add asset to the page
        awRel = new PSAssetWidgetRelationship(pageId, 5, "percRawHtml", assetId, 0);
        fixture.getAssetService().createAssetWidgetRelationship(awRel);
        assertTrue(relationshipExists(pageId, assetId, PSWidgetAssetRelationshipService.LOCAL_ASSET_WIDGET_REL_FILTER));

        awRel = new PSAssetWidgetRelationship(pageId, 6, "percRawHtml", assetId_2, 0, "Raw-Html-Asset");
        fixture.getAssetService().createAssetWidgetRelationship(awRel);
        assertTrue(relationshipExists(pageId, assetId, PSWidgetAssetRelationshipService.LOCAL_ASSET_WIDGET_REL_FILTER));

        // match widget id for unnamed widget and asset 
        validateGetWidgetRelationship(pageId, null, 5, false, assetId);
        validateUpdateWidgetNameAPI(template2.getId(), "widget5-changed", 5);
        // both name and id match for named asset
        validateGetWidgetRelationship(pageId, "widget5-changed", 5, true, assetId);
        // match name, ignore slot/widget id for named asset
        validateGetWidgetRelationship(pageId, "widget5-changed", Integer.MAX_VALUE, true, assetId);        
        // match name, ignore slot/widget id for named asset
        validateGetWidgetRelationship(pageId, "Raw-Html-Asset", 5, true, assetId_2);
        
        // update the page
        pageService.save(page1Mod);

        // asset relationship should not have been removed
        assertTrue(
                relationshipExists(pageId, assetId, PSWidgetAssetRelationshipService.LOCAL_ASSET_WIDGET_REL_FILTER));
    }
    
    @Test
    public void test150FindPagesByTemplate() throws Exception
    {
        String templateId = fixture.template1.getId();
        
        // no pages should be using the template
        Collection<Integer> pageIds = pageDaoHelper.findPageIdsByTemplate(templateId);
        assertTrue(pageIds.isEmpty());
        
        String siteFolderPath = fixture.site1.getFolderPath();
        
        // create some pages using the template
        String page1Id = createPage("Page1", "Page 1", templateId, siteFolderPath, "TestLink", "testurl.file", "","");
        assertNotNull(page1Id);
        
        String page2Id = createPage("Page2", "Page 2", templateId, siteFolderPath, "TestLink", "testurl.file", "","");
        assertNotNull(page2Id);

        // two pages should now be using the template
        pageIds = pageDaoHelper.findPageIdsByTemplate(templateId);
        assertEquals(2, pageIds.size());

        Integer pageId1 = idMapper.getLocator(page1Id).getId();
        Integer pageId2 = idMapper.getLocator(page2Id).getId();
        
        assertTrue(pageIds.contains(pageId1));
        assertTrue(pageIds.contains(pageId2));
    }

    @Ignore("Error Message: null")
    @Test
    public void test160FindPagesBySiteAndTemplate() throws Exception
    {
        String templateId = fixture.template1.getId();
        String siteFolderPath = fixture.site1.getFolderPath();
        
        // no pages should be using the template
        List<PSPage> pages = pageDao.findPagesBySiteAndTemplate(siteFolderPath, templateId);
        assertTrue(pages.isEmpty());
        
        // create some pages using the template
        String page1Id = createPage("Page1", "Page 1", templateId, siteFolderPath, "TestLink", "testurl.file", "","");
        assertNotNull(page1Id);
        
        String page2Id = createPage("Page2", "Page 2", templateId, siteFolderPath, "TestLink", "testurl.file", "","");
        assertNotNull(page2Id);

        // two pages should now be using the template
        pages = pageDao.findPagesBySiteAndTemplate(siteFolderPath, templateId);
        assertEquals(2, pages.size());
        List<String> ids = new ArrayList<String>();
        for (PSPage page : pages)
        {
            ids.add(page.getId());
        }
        assertTrue(ids.contains(page1Id));
        assertTrue(ids.contains(page2Id));
        
        // test null path
        assertEquals(pages, pageDao.findPagesBySiteAndTemplate(null, templateId));
        
        // test empty path
        assertEquals(pages, pageDao.findPagesBySiteAndTemplate("", templateId));
    }
    
    @Test
    public void test170GetUnusedAssets_noUnusedAssets()
    {
        PSPage page = createAndSavePage();
        PSTemplate template = fixture.getTemplateService().load(page.getTemplateId());
        
        Set<PSOrphanedAssetSummary> unused = getOrphanedAssetsSummaries(page, template);
        assertNotNull(unused);
        assertTrue(unused.size() == 0);
    }
    
    @Test
    public void test180GetUnusedAssets_noMatchingTemplateWidgetUnnamed()
    {
        createTestGetUnusedAsset_noMatchingTemplateWidget(false);
    }

    @Test
    public void test190GetUnusedAssets_noMatchingTemplateWidgetNamed()
    {
        createTestGetUnusedAsset_noMatchingTemplateWidget(true);
    }
   
    @Test
    public void test200GetUnusedAssets_assetHiddenByTemplateAssetUnnamedWidgets()
    {
        createTestGetUnusedAsset_hiddenByTemplateAssets(false);
    }
    
    @Ignore
    public void test_fixmeGetUnusedAssets_assetHiddenByTemplateAsset()
    {
        createTestGetUnusedAsset_hiddenByTemplateAssets(true);
    }

    @Test
    public void test210GetUnusedAssets_assetOrphanByWidgetRenaming()
    {
        String richTextName = "Rich Text widget";
        String richTextDescription = "Description for the Rich Text widget";
        String rawHtmlName = "Raw HTMLwidget";
        String rawHtmlDescription = "Description for the Raw HTML widget";
        
        PSPage page = createAndSavePage();
        PSTemplate template = fixture.getTemplateService().load(page.getTemplateId());

        // add a widget on the template
        List<PSPair<PSWidgetItem, PSAsset>> widgetAssetPairsForPage = new ArrayList<PSPair<PSWidgetItem, PSAsset>>();
        PSPair<PSWidgetItem, PSAsset> richText = createRichTextWidgetItem(richTextName, richTextDescription, "1");
        PSPair<PSWidgetItem, PSAsset> rawHtml = createRawHtmlWidgetItem(rawHtmlName, rawHtmlDescription, "2");
        widgetAssetPairsForPage.add(richText);
        widgetAssetPairsForPage.add(rawHtml);

        addWidgetsToTemplateAndSave(template, widgetAssetPairsForPage);

        // add content to that widget in the page
        addContentToWidgetOnPageOrTemplate(page, template, widgetAssetPairsForPage, false);
        
        // check that at this point there is no orphan asset
        Set<PSOrphanedAssetSummary> unused = getOrphanedAssetsSummaries(page, template);
        assertNotNull(unused);
        assertTrue(unused.size() == 0);

        // rename the rich text widget in the template so it creates an orphan in the page
        renameWidgetOnTemplateAndSave(richText.getFirst(), template, richTextName + "2");

        // get the unused assets
        List<PSPair<PSWidgetItem, PSAsset>> unusedPairs = new ArrayList<PSPair<PSWidgetItem, PSAsset>>();
        unusedPairs.add(createRichTextWidgetItem(richTextName, richTextDescription, "1"));
        verifyOrphanAssets(page, template, unusedPairs);
        assertTrue(true);
    }

    
    private void validateGetWidgetRelationship(String pageId, String widgetName, long widgetId,
            boolean matchNamedAsset, String assetId)
    {
        PSWidgetItem wi = new PSWidgetItem();
        wi.setDefinitionId("percRawHtml");
        wi.setId(String.valueOf(widgetId));
        wi.setName(widgetName);
        
        IPSGuid id = idMapper.getItemGuid(pageId);
        PSLocator assetLoc = idMapper.getLocator(assetId);
        
        Collection<PSRelationship> rels = PSWidgetContentFinderUtils.getLocalSharedAssetRelationships(id.toString());
        Collection<PSRelationship> matchRels = PSWidgetContentFinderUtils.getMatchRelationships(rels,
                Collections.singletonList(wi));
        
        assertTrue(matchRels.size() == 1);
        assertTrue(rels.size() == 2);
        
        PSRelationship r = matchRels.iterator().next();
        
        assertTrue(assetLoc.getId() == r.getDependent().getId());
        
        if (matchNamedAsset)
        {
            String relWidgetName = r.getProperty(PSRelationshipConfig.PDU_WIDGET_NAME);
            assertTrue(widgetName.equals(relWidgetName));
        }
        else
        {
            String slotId = r.getProperty(PSRelationshipConfig.PDU_SLOTID);
            assertTrue(widgetId == Integer.parseInt(slotId));
        }
    }
    
    private void validateUpdateWidgetNameAPI(String templateId, String newWidgetName, int widgetId)
    {
        int rows = widgetAssetRelationshipDao.updateWidgetNameForRelatedPages(templateId, newWidgetName, widgetId);
        assertTrue(rows == 1);
        rows = widgetAssetRelationshipDao.updateWidgetNameForRelatedPages(templateId, "", widgetId);
        assertTrue(rows == 0);
        rows = widgetAssetRelationshipDao.updateWidgetNameForRelatedPages(templateId, null, widgetId);
        assertTrue(rows == 0);
        
        IPSCacheAccess cache = PSCacheAccessLocator.getCacheAccess();
        cache.clearRelationships();
    }
    
    /**
     * Creates and save a Raw HTML Asset.
     * 
     * @return a {@link String} with the asset id.
     */
    private String createRawHtmlAsset()
    {
        String assetId;
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", "LocalAsset" + System.currentTimeMillis());
        asset.setType("percRawHtmlAsset");
        asset.getFields().put("html", "TestHTML");
        asset.setFolderPaths(asList("//Folders"));
        assetId = fixture.getAssetService().save(asset).getId();
        
        return assetId;
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
        asset = fixture.getAssetService().save(asset);

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
        asset = fixture.getAssetService().save(asset);
        
        PSWidgetItem widget = new PSWidgetItem();
        widget.setDefinitionId("percRawHtml");
        widget.setDescription(description);
        widget.setName(name);
        widget.setId(slotid);
        
        return new PSPair<PSWidgetItem, PSAsset>(widget, asset);
    }

    /**
     * Iterates over the region tree of the template and then renames the widget
     * if the instance is found.
     * 
     * @param widgetToRename {@link PSWidgetItem} object, representing the
     *            widget to be renamed. assumed not <code>null</code>.
     * @param template {@link PStemplate} to get the widget and rename it.
     *            Assumed not <code>null</code>
     * @param newName {@link String} with the new name for the widget.
     */
    private void renameWidgetOnTemplateAndSave(PSWidgetItem widgetToRename, PSTemplate template, String newName)
    {
        PSRegionTree regionTree = template.getRegionTree();
        
        Set<PSRegionWidgets> regionWidgets = regionTree.getRegionWidgetAssociations(); 
        for(PSRegionWidgets widgetRegion : regionWidgets)
        {
            List<PSWidgetItem> widgetItems = widgetRegion.getWidgetItems(); 
            for(PSWidgetItem widgetItem : widgetItems)
            {
                if(equalsIgnoreCase(widgetItem.getId(), widgetToRename.getId()))
                {
                    widgetItem.setName(newName);
                    fixture.getTemplateService().save(template);
                    return;
                }
            }
        }
    }

    /**
     * Gets the orphan assets for the page, and then verifies that:<br>
     * <li>the orphan assets list is not <code>null</code>.<br> <li>the size of
     * the orphan assets list is the same as the widget asset pairs list. <li>
     * the orphan assets have the same slot id and widget name as they were in
     * the template.
     * 
     * @param page {@link PSPage} object representing the page.
     * @param template {@link PSTemplate} object representing the page's
     *            template.
     * @param widgetAssetPairs {@link List}<{@link PSPair}<{@link PSWidgetItem},
     *            {@link PSAsset}>> with the widgets added to the template, and
     *            the corresponding asset added to the page.
     */
    private void verifyOrphanAssets(PSPage page, PSTemplate template,
            List<PSPair<PSWidgetItem, PSAsset>> widgetAssetPairs)
    {
        Set<PSOrphanedAssetSummary> unused = getOrphanedAssetsSummaries(page, template);
        assertNotNull(unused);
        assertTrue(unused.size() == widgetAssetPairs.size());

        boolean validated = false;
        for (PSOrphanedAssetSummary orphan : unused)
        {
            for (PSPair<PSWidgetItem, PSAsset> pair : widgetAssetPairs)
            {
                PSWidgetItem widget = pair.getFirst();

                if (((isEmpty(orphan.getWidgetName()) && isEmpty(widget.getName())) || equalsIgnoreCase(
                        orphan.getWidgetName(), widget.getName()))
                        && equalsIgnoreCase(orphan.getSlotId(), widget.getId()))
                {
                    validated = true;
                    break;
                }
            }
            assertTrue(validated);
            validated = false;
        }
    }

    /**
     * Removes the given widget from the template region tree and then saves the
     * changed template.
     * 
     * @param template {@link PSTemplate} to remove the widget from. Assumed not
     *            <code>null</code>.
     * @param widgetAssetPairsToRemove {@link List}<{@link PSPair}<
     *            {@link PSWidgetItem}, {@link PSAsset}>> holding the widgets
     *            that are meant to be removed
     * @return the modified {@link PSTemplate} object. Never <code>null</code>.
     */
    private PSTemplate removeWidgetFromTemplateAndSave(PSTemplate template,
            List<PSPair<PSWidgetItem, PSAsset>> widgetAssetPairsToRemove)
    {
        for (PSPair<PSWidgetItem, PSAsset> pair : widgetAssetPairsToRemove)
        {
            Map<String, List<PSWidgetItem>> regionWidgetsMap = template.getRegionTree().getRegionWidgetsMap();
            for (String regionId : regionWidgetsMap.keySet())
            {
                regionWidgetsMap.get(regionId).remove(pair.getFirst());
            }
        }
        return fixture.getTemplateService().save(template);
    }

    /**
     * Adds the given widgets on the template or page (according to the last
     * parameter). Basically it creates instances of {@link PSRelationship}
     * where the owner is the page or the template, and the dependant is the
     * given asset, using the widget id (slot id) from the {@link PSWidgetItem}
     * object.
     * 
     * @param page {@link PSPage} the page to use on the relationship. Assumed
     *            not <code>null</code>.
     * @param template {@link PSTemplate} the template to use in the
     *            relationship. Assumed not <code>null</code>.
     * @param widgetAssetPairs {@link List}<{@link PSPair}<{@link PSWidgetItem},
     *            {@link PSAsset}>>
     * @param addOnTemplate if <code>true</code> the content will be added to
     *            the template. If <code>false</code> the content will be added
     *            into the template.
     * @return {@link List}<{@link PSAssetWidgetRelationship}> with the created
     *         relationships. Never <code>null</code> but may be empty.
     */
    private List<PSAssetWidgetRelationship> addContentToWidgetOnPageOrTemplate(PSPage page, PSTemplate template,
            List<PSPair<PSWidgetItem, PSAsset>> widgetAssetPairs, boolean addOnTemplate)
    {
        List<PSAssetWidgetRelationship> relationships = new ArrayList<PSAssetWidgetRelationship>();
        String ownerId = (addOnTemplate) ? template.getId() : page.getId();

        for (PSPair<PSWidgetItem, PSAsset> pair : widgetAssetPairs)
        {
            PSWidgetItem widget = pair.getFirst();
            PSAsset asset = pair.getSecond();

            PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(ownerId, Long.parseLong(widget.getId()),
                    widget.getDefinitionId(), asset.getId(), 0, widget.getName());
            fixture.getAssetService().createAssetWidgetRelationship(awRel);
            assertTrue(relationshipExists(ownerId, asset.getId(),
                    PSWidgetAssetRelationshipService.LOCAL_ASSET_WIDGET_REL_FILTER));

            relationships.add(awRel);
        }
        return relationships;
    }

    /**
     * Adds the given widgets to the template. Creates a region and adds the
     * widgets to it. Then it sets that region to a region tree and set it to
     * the template.
     * 
     * @param template {@link PSTemplate} object, must not be <code>null</code>.
     * @param widgetAssetPairs {@link List}<{@link PSWidgetItem}> with the
     *            widgets to add. May be empty but not <code>null</code>.
     */
    private void addWidgetsToTemplateAndSave(PSTemplate template, List<PSPair<PSWidgetItem, PSAsset>> widgetAssetPairs)
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

        fixture.getTemplateService().save(template);
    }

    /**
     * Creates the test scenario for the unused assets generated with no
     * matching template widgets. It creates a template and a page, then adds
     * widget to template, and asset to page. Then it removes the widget from
     * the template generating unused assets. The widgets will be created with
     * or without a name, depending on the parameter.
     * 
     * @param namedWidgets if <code>true</code>, the widgets will be created
     *            with a name. If <code>false</code>, widgets will be created
     *            with no name.
     */
    private void createTestGetUnusedAsset_noMatchingTemplateWidget(boolean namedWidgets)
    {
        String richTextName = (namedWidgets) ? "Rich Text widget" : "";
        String richTextDescription = (namedWidgets) ? "Description for the Rich Text widget" : "";
        String rawHtmlName = (namedWidgets) ? "Raw HTMLwidget" : "";
        String rawHtmlDescription = (namedWidgets) ? "Description for the Raw HTML widget" : "";

        PSPage page = createAndSavePage();
        PSTemplate template = fixture.getTemplateService().load(page.getTemplateId());

        // add a widget on the template
        List<PSPair<PSWidgetItem, PSAsset>> widgetAssetPairs = new ArrayList<PSPair<PSWidgetItem, PSAsset>>();
        PSPair<PSWidgetItem, PSAsset> richText = createRichTextWidgetItem(richTextName, richTextDescription, "1");
        PSPair<PSWidgetItem, PSAsset> rawHtml = createRawHtmlWidgetItem(rawHtmlName, rawHtmlDescription, "2");
        widgetAssetPairs.add(richText);
        widgetAssetPairs.add(rawHtml);

        addWidgetsToTemplateAndSave(template, widgetAssetPairs);

        // add content to that widget in the page
        addContentToWidgetOnPageOrTemplate(page, template, widgetAssetPairs, false);

        // remove that widget from the template
        template = removeWidgetFromTemplateAndSave(template, widgetAssetPairs);

        // get the unused assets
        verifyOrphanAssets(page, template, widgetAssetPairs);
    }

    /**
     * Creates the test scenario for the unused assets generated with no page
     * assets overriden by template assets. It creates a template and a page,
     * then adds widget to template, asset to page, and then asset in the
     * template. That generates unused assets. The widgets will be created with
     * or without a name, depending on the parameter.
     * 
     * @param namedWidgets if <code>true</code>, the widgets will be created
     *            with a name. If <code>false</code>, widgets will be created
     *            with no name.
     */
    private void createTestGetUnusedAsset_hiddenByTemplateAssets(boolean namedWidgets)
    {
        String richTextName = (namedWidgets)? "Rich Text widget" : "";
        String richTextDescription = (namedWidgets)? "Description for the Rich Text widget" : "";
        String rawHtmlName = (namedWidgets)? "Raw HTMLwidget" : "";
        String rawHtmlDescription = (namedWidgets)? "Description for the Raw HTML widget" : "";
        
        PSPage page = createAndSavePage();
        PSTemplate template = fixture.getTemplateService().load(page.getTemplateId());

        // add a widget on the template
        List<PSPair<PSWidgetItem, PSAsset>> widgetAssetPairsForPage = new ArrayList<PSPair<PSWidgetItem, PSAsset>>();
        PSPair<PSWidgetItem, PSAsset> richText = createRichTextWidgetItem(richTextName, richTextDescription, "1");
        PSPair<PSWidgetItem, PSAsset> rawHtml = createRawHtmlWidgetItem(rawHtmlName, rawHtmlDescription, "2");
        widgetAssetPairsForPage.add(richText);
        widgetAssetPairsForPage.add(rawHtml);

        addWidgetsToTemplateAndSave(template, widgetAssetPairsForPage);

        // add content to that widget in the page
        addContentToWidgetOnPageOrTemplate(page, template, widgetAssetPairsForPage, false);

        // add content to that widget in the template
        List<PSPair<PSWidgetItem, PSAsset>> widgetAssetPairsForTemplate = new ArrayList<PSPair<PSWidgetItem, PSAsset>>();
        PSPair<PSWidgetItem, PSAsset> richText2 = createRichTextWidgetItem(richTextName, richTextDescription, "1");
        PSPair<PSWidgetItem, PSAsset> rawHtml2 = createRawHtmlWidgetItem(rawHtmlName, rawHtmlDescription, "2");
        widgetAssetPairsForTemplate.add(richText2);
        widgetAssetPairsForTemplate.add(rawHtml2);

        addContentToWidgetOnPageOrTemplate(page, template, widgetAssetPairsForTemplate, true);

        // get the unused assets
        verifyOrphanAssets(page, template, widgetAssetPairsForPage);
    }
    
    /**
     * Creates and save a page.
     * 
     * @return the {@link PSPage} saved object.
     */
    private PSPage createAndSavePage()
    {
        String templateId = fixture.template1.getId();

        String name = "Page1";
        String title = "Page 1";
        String folderPath = fixture.site1.getFolderPath();
        String linkTitle = "TestLink";
        String noindex = "true";
        String description = "This is Page 1.";

        String pageId = createPage("Page1", title, templateId, folderPath, linkTitle, 
                "testurl.file", noindex, description);
        assertNotNull(pageId);
        
        PSPage page2 = pageService.findPage(name, folderPath);
        assertNotNull(page2);

        return page2;
    }

    /**
     * Determines if a relationship exists for the specified owner, dependent,
     * and name.
     * 
     * @param ownerId the owner id, assumed not <code>null</code>. This is the
     *            {@link IPSGuid} representation in string format.
     * @param depId the dependent id, assumed not <code>null</code>. This is the
     *            {@link IPSGuid} representation in string format.
     * @param relName the name of the relationship type to filter by, assumed
     *            not <code>null</code>.
     * 
     * @return <code>true</code> if the relationship exists, <code>false</code>
     *         otherwise.
     * 
     * @throws PSErrorException
     */
    private boolean relationshipExists(String ownerId, String depId, String relName) throws PSErrorException
    {
        notNull(ownerId, "ownerId");
        notNull(depId, "depId");
        notNull(relName, "relName");
        PSRelationshipFilter filter = new PSRelationshipFilter();
        filter.setName(relName);
        filter.setOwnerId(idMapper.getGuid(ownerId).getUUID());
        filter.setDependentId(idMapper.getGuid(depId).getUUID());
        List<PSRelationship> rels = systemWs.loadRelationships(filter);

        return !rels.isEmpty();
    }

    /**
     * Creates and saves a page using the testcase fixture {@link PSSiteDataServletTestCaseFixture}.
     * 
     * @param name assumed not <code>null</code>.
     * @param title assumed not <code>null</code>.
     * @param templateId assumed not <code>null</code>.
     * @param folderPath assumed not <code>null</code>.
     * @param linkTitle assumed not <code>null</code>.
     * @param url assumed not <code>null</code>.
     * @param noindex assumed not <code>null</code>.
     * @param description assumed not <code>null</code>.
     * 
     * @return the id of the created page, never blank.
     */
    private String createPage(String name, String title, String templateId, String folderPath, String linkTitle,
            String url, String noindex, String description)
    {
        PSPage page = new PSPage();
        page.setFolderPath(folderPath);
        page.setName(name);
        page.setTitle(title);
        page.setTemplateId(templateId);
        page.setFolderPath(folderPath);
        page.setLinkTitle(linkTitle);
        page.setNoindex(noindex);
        page.setDescription(description);
        
        return fixture.createPage(page).getId();
    }
    
    public IPSPageService getPageService()
    {
        return pageService;
    }

    public void setPageService(IPSPageService pageService)
    {
        this.pageService = pageService;
    }

    public IPSIdMapper getIdMapper()
    {
        return idMapper;
    }

    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }

    public IPSSystemWs getSystemWs()
    {
        return systemWs;
    }

    public void setSystemWs(IPSSystemWs systemWs)
    {
        this.systemWs = systemWs;
    }

    public IPSPageDao getPageDao()
    {
        return pageDao;
    }

    public IPSPageDaoHelper getPageDaoHelper()
    {
        return pageDaoHelper;
    }

    public void setPageDao(IPSPageDao pageDao)
    {
        this.pageDao = pageDao;
    }

    public void setPageDaoHelper(IPSPageDaoHelper pageDaoHelper)
    {
        this.pageDaoHelper = pageDaoHelper;
    }

    public IPSWidgetAssetRelationshipService getWidgetService()
    {
        return widgetService;
    }

    public void setWidgetService(IPSWidgetAssetRelationshipService widgetService)
    {
        this.widgetService = widgetService;
    }

    public void setWidgetAssetRelationshipDao(IPSWidgetAssetRelationshipDao widgetAssetRelationshipDao)
    {
        this.widgetAssetRelationshipDao = widgetAssetRelationshipDao;
    }
    
    private IPSWidgetAssetRelationshipDao widgetAssetRelationshipDao;
    
    public IPSTemplateDao getTemplateDao()
    {
        return templateDao;
    }

    public void setTemplateDao(IPSTemplateDao templateDao)
    {
        this.templateDao = templateDao;
    }

    private IPSPageService pageService;

    private IPSIdMapper idMapper;

    private IPSSystemWs systemWs;
    
    private IPSPageDao pageDao;

    private IPSPageDaoHelper pageDaoHelper;
    
    private IPSWidgetAssetRelationshipService widgetService;
    
    private IPSTemplateDao templateDao;
    
}
