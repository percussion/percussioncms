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
package com.percussion.pathmanagement.web.service;

import static java.util.Arrays.asList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetResourceType;
import com.percussion.assetmanagement.web.service.PSAssetServiceRestClient;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.web.service.PSItemWorkflowServiceRestClient;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSRegionNode.PSRegionOwnerType;
import com.percussion.pagemanagement.web.service.PSPageRestClient;
import com.percussion.pagemanagement.web.service.PSTemplateServiceClient;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria.SkipItemsType;
import com.percussion.pathmanagement.data.PSFolderPermission;
import com.percussion.pathmanagement.data.PSFolderPermission.Principal;
import com.percussion.pathmanagement.data.PSFolderPermission.PrincipalType;
import com.percussion.pathmanagement.data.PSFolderProperties;
import com.percussion.pathmanagement.data.PSItemByWfStateRequest;
import com.percussion.pathmanagement.data.PSMoveFolderItem;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.data.PSRenameFolderItem;
import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.pathmanagement.service.impl.PSPathItemService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.pathmanagement.service.impl.PSSitePathItemService;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.share.IPSSitemanageConstants;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.test.PSDataServiceRestClient;
import com.percussion.share.test.PSObjectRestClient;
import com.percussion.share.test.PSObjectRestClient.DataRestClientException;
import com.percussion.share.test.PSRestClient.RestClientException;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.share.test.PSTestDataCleaner;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.web.service.PSSiteRestClient;
import com.percussion.sitemanage.web.service.PSSiteTemplateRestClient;
import com.percussion.ui.service.IPSListViewHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PSPathServiceTest extends PSRestTestCase<PSPathServiceRestClient>
{
    static String letters = "abcdefghijklmnopqrstuvwxyz";
    
    static Integer DEFAULT_DATE_FORMAT_ID = 9;
    
    static PSPathServiceRestClient restClient;

    static PSPathServiceRestClient restClientEditor;

    static PSSiteRestClient siteRestClient;

    static PSSiteTemplateRestClient siteTemplateRestClient;
    
    private DecimalFormat decimalFormat = new DecimalFormat("000");

    static PSTestDataCleaner<String> siteCleaner = new PSTestDataCleaner<String>()
    {

        @Override
        protected void clean(String name) throws Exception
        {
            siteRestClient.delete(name);
        }

    };

    @Override
    protected PSPathServiceRestClient getRestClient(String baseUrl)
    {
        restClient = new PSPathServiceRestClient(baseUrl);
        return restClient;
    }

    @Before
    public void setup() throws Exception
    {
        restClientEditor = new PSPathServiceRestClient(baseUrl);
        siteRestClient = new PSSiteRestClient(baseUrl);
        siteTemplateRestClient = new PSSiteTemplateRestClient();

        setupClient(restClientEditor, "editor1", EI_COMMUNITYID);
        setupClient(siteRestClient, "admin1", EI_ADMIN_COMMUNITYID);
        setupClient(siteTemplateRestClient, "admin1", EI_ADMIN_COMMUNITYID);
    }

    @Test
    public void test010FindRoot() throws Exception
    {
        testFindRoot(SITE_ROOT);
        testFindRoot(ASSET_ROOT);
    }

    @Test
    public void test020FindChildren() throws Exception
    {
        testFindChildren(SITE_ROOT);
        testFindChildren(ASSET_ROOT);
    }
    
    @Test
    public void test030FindChildrenWithOptions() throws Exception
    {
        PSSite site = createSite();
        String sitePath = SITE_ROOT + site.getName();
        String path = sitePath + "/folder1";
        
        createNumberedPages(site, path, 1);
        restClient.addFolder(path + "/folder2");
        
        
        PSPagedItemList pagedItems = restClient.findChildren(sitePath, 1, 10, null, DEFAULT_DATE_FORMAT_ID, "sys_title", null);
        List<PSPathItem> pathItems = pagedItems.getChildrenInPage();
        boolean foundFolder = false;
        for (PSPathItem pathItem : pathItems)
        {
            if (pathItem.isFolder())
                foundFolder = true;
                
            assertFalse(pathItem.hasFolderChildren());
            assertFalse(pathItem.hasItemChildren());
            assertFalse(pathItem.hasSectionChildren());
        }
        assertTrue(foundFolder);
        
        foundFolder = false;
        pathItems = restClient.findChildren(sitePath);
        for (PSPathItem pathItem : pathItems)
        {
            if (pathItem.isFolder())
            {
                foundFolder = true;
                assertTrue(pathItem.hasFolderChildren());
                assertTrue(pathItem.hasItemChildren());
                assertFalse(pathItem.hasSectionChildren());
            }
            else
            {
                assertFalse(pathItem.hasFolderChildren());
                assertFalse(pathItem.hasItemChildren());
                assertFalse(pathItem.hasSectionChildren());
            }
        }
        assertTrue(foundFolder);
    }
    
    private void createNumberedPages(PSSite site, String path, int countOfPages) throws Exception
    {
        createNumberedPages(site, path, countOfPages, true, 0);
    }
    
    private void createNumberedPages(PSSite site, String path, int countOfPages, boolean shuffle, int secondsBetweenSaves) throws Exception
    {
        PSPathItem folderItem = restClient.addFolder(path);
        String templateId = getTemplateId(site);
        
        List<PSPage> pages = new ArrayList<PSPage>();
        
        for (int i=1; i<=countOfPages; i++)
        {
            pages.add(createNumberedPage(folderItem.getFolderPath(), i, templateId));
        }
        
        if (shuffle)
            Collections.shuffle(pages);
        
        for (PSPage page : pages)
        {
            getPageRestClient().save(page);
            
            if (secondsBetweenSaves > 0)
                Thread.sleep(1000 * secondsBetweenSaves);
        }
    }
    
    /**
     * Does not support countOfPages greater than letters.length()
     * @param site
     * @param path
     * @param countOfPages
     * @throws Exception
     */
    private void createAlphaNamedPages(PSSite site, String path, int countOfPages) throws Exception
    {
        PSPathItem folderItem = restClient.addFolder(path);
        String templateId = getTemplateId(site);
        
        List<PSPage> pages = new ArrayList<PSPage>();
        
        for (int i=1; i<=countOfPages; i++)
        {
            char letter = letters.charAt(i);
            
            pages.add(createAlphaNamedPage(folderItem.getFolderPath(), letter, templateId));
        }
        
        Collections.shuffle(pages);
        
        for (PSPage page : pages)
            getPageRestClient().save(page);
    }
    
    private void validateExtendedPathItem(PSPagedItemList pathItems,
            int itemsCountInPage, Integer totalItemsCount, Integer startIndex)
    {
        assertNotNull("pathItems not null", pathItems);
        assertNotNull("pathItems items not null", pathItems.getChildrenInPage());
        assertNotNull("pathItems items count not null", pathItems.getChildrenCount());
        
        assertEquals("pathItems items count in page", itemsCountInPage, pathItems.getChildrenInPage().size());
        assertEquals("pathItems children count", totalItemsCount, pathItems.getChildrenCount());
        assertEquals("pathItems start index", startIndex, pathItems.getStartIndex());
    }
    
    private void validatePages(PSPagedItemList pathItems, Integer expectedStartIndex)
    {
        PSPathItem pathItem;
        for (int i=0; i<pathItems.getChildrenInPage().size(); i++)
        {
            pathItem = pathItems.getChildrenInPage().get(i);
            
            assertEquals("page name " + i, "page" + decimalFormat.format(expectedStartIndex + i) + ".xml", pathItem.getName());
        }
    }
    
    private void validatePathItemIndexes(PSPagedItemList pathItems, String... expectedPathItemNames)
    {
        PSPathItem pathItem;
        
        for (int i=0; i<pathItems.getChildrenInPage().size(); i++)
        {
            pathItem = pathItems.getChildrenInPage().get(i);
            
            assertEquals("path item name " + i, expectedPathItemNames[i], pathItem.getName());
        }
    }
    
    private void validatePagesIndexes(PSPagedItemList pathItems, Integer... expectedStartIndexes)
    {
        PSPathItem pathItem;
        
        for (int i=0; i<pathItems.getChildrenInPage().size(); i++)
        {
            pathItem = pathItems.getChildrenInPage().get(i);
            
            assertEquals("page name " + i, "page" + decimalFormat.format(expectedStartIndexes[i]) + ".xml", pathItem.getName());
        }
    }
    
    
    public void test040FindChildren_StartIndex_IsInvalid() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        createNumberedPages(site, path, 5);
        
        // startIndex < 1
        try
        {
            restClient.findChildren(path, -1, 5, null);
            fail("Should have thrown an exception");
        }
        catch (DataRestClientException e)
        {
            assertEquals("error code", 500, e.getStatus());
            assertTrue("error details", e.getResponseBody().contains("java.lang.IllegalArgumentException"));
        }
        
        // startIndex == 0
        try
        {
            restClient.findChildren(path, 0, 5, null);
            fail("Should have thrown an exception");
        }
        catch (DataRestClientException e)
        {
            assertEquals("error code", 500, e.getStatus());
            assertTrue("error details", e.getResponseBody().contains("java.lang.IllegalArgumentException"));
        }
    }
    
    @Test
    public void test050FindChildren_MaxResults_IsInvalid() throws Exception
    {
        try
        {
            restClient.findChildren(SITE_ROOT, 1, -1, null);
            fail("Should have thrown an exception");
        }
        catch (DataRestClientException e)
        {
            assertEquals("error code", 500, e.getStatus());
            assertTrue("error details", e.getResponseBody().contains("java.lang.IllegalArgumentException"));
        }
    }
    
    @Test
    public void test060FindChildren_MaxResults_NotSpecified() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        createNumberedPages(site, path, 10);
        
        PSPagedItemList pathItems;
        
        // startIndex == pagesCount
        pathItems = restClient.findChildren(path, 3, null, null);
        validateExtendedPathItem(pathItems, 8, 10, 3);
        validatePages(pathItems, 3);
    }
    
    @Test
    public void test070FindChildren_StartIndex_GreaterThanItemsCount() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        createNumberedPages(site, path, 10);
        
        PSPagedItemList pathItems;
        int expectedStartIndex = 6;
        
        // startIndex == pagesCount
        pathItems = restClient.findChildren(path, 11, 5, null);
        validateExtendedPathItem(pathItems, 5, 10, expectedStartIndex);
        validatePages(pathItems, expectedStartIndex);
    }
    
    @Test
    public void test080FindChildren_StartIndex_GetLastItem() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        createNumberedPages(site, path, 5);
        
        PSPagedItemList pathItems;
        
        // startIndex == pagesCount
        pathItems = restClient.findChildren(path, 5, 1, null);
        validateExtendedPathItem(pathItems, 1, 5, 5);
        validatePages(pathItems, 5);
    }
    
    @Test
    public void test090FindChildren_StartIndex_GetFromBeginning() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        createNumberedPages(site, path, 5);
        
        PSPagedItemList pathItems;
        
        // startIndex == pagesCount
        pathItems = restClient.findChildren(path, 1, 5, null);
        validateExtendedPathItem(pathItems, 5, 5, 1);
        validatePages(pathItems, 1);
    }
    
    @Test
    public void test100FindChildren_MaxResults_EqualsToOne() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        createNumberedPages(site, path, 5);
        
        PSPagedItemList pathItems;
        
        pathItems = restClient.findChildren(path, 1, 1, null);
        validateExtendedPathItem(pathItems, 1, 5, 1);
        validatePages(pathItems, 1);
        
        pathItems = restClient.findChildren(path, 2, 1, null);
        validateExtendedPathItem(pathItems, 1, 5, 2);
        validatePages(pathItems, 2);
        
        pathItems = restClient.findChildren(path, 3, 1, null);
        validateExtendedPathItem(pathItems, 1, 5, 3);
        validatePages(pathItems, 3);
        
        pathItems = restClient.findChildren(path, 4, 1, null);
        validateExtendedPathItem(pathItems, 1, 5, 4);
        validatePages(pathItems, 4);
        
        pathItems = restClient.findChildren(path, 5, 1, null);
        validateExtendedPathItem(pathItems, 1, 5, 5);
        validatePages(pathItems, 5);
    }
    
    @Test
    public void test110FindChildren_MaxResults_GreaterThanItemsCount() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        createNumberedPages(site, path, 5);
        
        PSPagedItemList pathItems;
        
        pathItems = restClient.findChildren(path, 3, 5, null);
        validateExtendedPathItem(pathItems, 3, 5, 3);
        validatePages(pathItems, 3);
    }
    
    @Test
    public void test120FindChildren_Sorting_SortColumn_IsNull() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        createNumberedPages(site, path, 5);
        
        PSPagedItemList pathItems;
        
        // If sortColumn is null, then no sorting is done
        pathItems = restClient.findChildren(path, 3, 5, null, DEFAULT_DATE_FORMAT_ID, null, "asc");
        validateExtendedPathItem(pathItems, 3, 5, 3);
        validatePages(pathItems, 3);
    }
    
    @Test
    public void test130FindChildren_Sorting_SortOrder_IsNull() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        createNumberedPages(site, path, 5);
        
        PSPagedItemList pathItems;
        
        // If sortOrder is null, then no sorting is done
        pathItems = restClient.findChildren(path, 3, 5, null, DEFAULT_DATE_FORMAT_ID, "sys_title", null);
        validateExtendedPathItem(pathItems, 3, 5, 3);
        validatePages(pathItems, 3);
    }
    
    @Test
    public void test140FindChildren_Sorting_SortColumn_And_SortOrder_AreBoth_Null() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        createNumberedPages(site, path, 5);
        
        PSPagedItemList pathItems;
        
        // If sortColumn and sortOrder are null, then no sorting is done
        pathItems = restClient.findChildren(path, 3, 5, null, DEFAULT_DATE_FORMAT_ID, null, null);
        validateExtendedPathItem(pathItems, 3, 5, 3);
        validatePages(pathItems, 3);
    }
    
    @Test
    public void test150FindChildren_Sorting_SortBy_Name() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        createNumberedPages(site, path, 5);
        
        PSPagedItemList pathItems;
        
        // asc
        pathItems = restClient.findChildren(path, null, null, null, DEFAULT_DATE_FORMAT_ID, IPSListViewHelper.TITLE_NAME, "asc");
        validateExtendedPathItem(pathItems, 5, 5, 1);
        validatePagesIndexes(pathItems, 1, 2, 3, 4, 5);
        
        // desc
        pathItems = restClient.findChildren(path, null, null, null, DEFAULT_DATE_FORMAT_ID, IPSListViewHelper.TITLE_NAME, "desc");
        validateExtendedPathItem(pathItems, 5, 5, 1);
        validatePagesIndexes(pathItems, 5, 4, 3, 2, 1);
    }
    
    @Test
    public void test160FindChildren_Sorting_SortBy_Name_Group_Folders() throws Exception
    {
        // Create pages
        PSSite site = createSite();
        String pathPrefix = SITE_ROOT + site.getName();
        String path = pathPrefix + "/folder1";
        
        createNumberedPages(site, path, 5);
        
        // Create folders
        restClient.addFolder(pathPrefix + "/folder1/aaaFolder");
        restClient.addFolder(pathPrefix + "/folder1/zzzFolder");
        
        PSPagedItemList pathItems;
        
        // asc
        pathItems = restClient.findChildren(path, null, null, null, DEFAULT_DATE_FORMAT_ID, IPSListViewHelper.TITLE_NAME, "asc");
        validateExtendedPathItem(pathItems, 7, 7, 1);
        validatePathItemIndexes(pathItems,
                "aaaFolder",
                "page001.xml",
                "page002.xml",
                "page003.xml",
                "page004.xml",
                "page005.xml",
                "zzzFolder");
        
        // desc
        pathItems = restClient.findChildren(path, null, null, null, DEFAULT_DATE_FORMAT_ID, IPSListViewHelper.TITLE_NAME, "desc");
        validateExtendedPathItem(pathItems, 7, 7, 1);
        validatePathItemIndexes(pathItems,
                "zzzFolder",
                "page005.xml",
                "page004.xml",
                "page003.xml",
                "page002.xml",
                "page001.xml",
                "aaaFolder");
    }
    
    
    @Ignore("Folder doesn't have created date property")
    public void test170FindChildren_Sorting_SortBy_CreatedDate_Group_Folders() throws Exception
    {
        // Create pages
        PSSite site = createSite();
        String pathPrefix = SITE_ROOT + site.getName();
        String path = pathPrefix + "/folder1";
        int secondsBetweenSaves = 2;
        
        createNumberedPages(site, path, 5, false, secondsBetweenSaves);
        
        // Create folders
        restClient.addFolder(pathPrefix + "/folder1/aaaFolder");
        Thread.sleep(1000 * secondsBetweenSaves);
        restClient.addFolder(pathPrefix + "/folder1/zzzFolder");
        
        PSPagedItemList pathItems;
        
        // asc
        pathItems = restClient.findChildren(path, null, null, null, DEFAULT_DATE_FORMAT_ID,
                IPSListViewHelper.CONTENT_CREATEDDATE_NAME, "asc");
        validateExtendedPathItem(pathItems, 7, 7, 1);
        validatePathItemIndexes(pathItems,
                "page001.xml",
                "page002.xml",
                "page003.xml",
                "page004.xml",
                "page005.xml",
                "aaaFolder",
                "zzzFolder");
        
        // desc
        pathItems = restClient.findChildren(path, null, null, null, DEFAULT_DATE_FORMAT_ID,
                IPSListViewHelper.CONTENT_CREATEDDATE_NAME, "desc");
        validateExtendedPathItem(pathItems, 7, 7, 1);
        validatePathItemIndexes(pathItems,
                "zzzFolder",
                "aaaFolder",
                "page005.xml",
                "page004.xml",
                "page003.xml",
                "page002.xml",
                "page001.xml");
    }
    
    @Test
    public void test180FindChildren_Sorting_Plus_Paging() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        createNumberedPages(site, path, 5);
        
        PSPagedItemList pathItems;
        
        // asc
        pathItems = restClient.findChildren(path, 3, 5, null, DEFAULT_DATE_FORMAT_ID, IPSListViewHelper.TITLE_NAME, "asc");
        validateExtendedPathItem(pathItems, 3, 5, 3);
        validatePagesIndexes(pathItems, 3, 4, 5);
        
        // desc
        pathItems = restClient.findChildren(path, 3, 5, null, DEFAULT_DATE_FORMAT_ID, IPSListViewHelper.TITLE_NAME, "desc");
        validateExtendedPathItem(pathItems, 3, 5, 3);
        validatePagesIndexes(pathItems, 3, 2, 1);
    }
    
    @Test
    public void test190FindChildren_Paging_And_Sorting() throws Exception
    {
        PSSite site = createSite();
        String pagesPath = SITE_ROOT + site.getName() + "/folder1";
        
        createNumberedPages(site, pagesPath, 5);
        
        // create asset folder and sub-folders
        String testFolderPath = createTestFolderIfNeeded();
        String filterFolderPath = testFolderPath + "/TestFilter";
        PSPathItem subFolderItem = restClient.addFolder(filterFolderPath);
        
        for (int i = 0; i < 5; i++)
        {
            restClient.addFolder(filterFolderPath + "/folder" + i);
        }

        // create 10 assets in the folder, 5 file, 5 image
        for (int i = 0; i < 5; i++)
        {
            createAsset("testHtml" + i, subFolderItem.getFolderPath());
        }
        
        PSPagedItemList pathItems;
        pathItems = restClient.findChildren(filterFolderPath, 1, 5, null, DEFAULT_DATE_FORMAT_ID, null, null, IPSItemSummary.Category.ASSET.name(), "percRawHtmlAsset");
        validateExtendedPathItem(pathItems, 5, 5, 1);
        pathItems = restClient.findChildren(filterFolderPath, 1, 3, null, DEFAULT_DATE_FORMAT_ID, null, null, IPSItemSummary.Category.ASSET.name(), "percRawHtmlAsset");
        validateExtendedPathItem(pathItems, 3, 5, 1);
        pathItems = restClient.findChildren(filterFolderPath, 4, 3, null, DEFAULT_DATE_FORMAT_ID, null, null, IPSItemSummary.Category.ASSET.name(), "percRawHtmlAsset");
        validateExtendedPathItem(pathItems, 2, 5, 4);
        pathItems = restClient.findChildren(filterFolderPath, 4, 3, null, DEFAULT_DATE_FORMAT_ID, null, null, IPSItemSummary.Category.ASSET.name(), null);
        validateExtendedPathItem(pathItems, 2, 5, 4);
        pathItems = restClient.findChildren(filterFolderPath, 4, 3, null, DEFAULT_DATE_FORMAT_ID, null, null, null, "percRawHtmlAsset");
        validateExtendedPathItem(pathItems, 2, 5, 4);
        
        // query asset folder with folder cat, get folders, no assets
        pathItems = restClient.findChildren(filterFolderPath, 4, 3, null, DEFAULT_DATE_FORMAT_ID, null, null, null, "percRawHtmlAsset");
        validateExtendedPathItem(pathItems, 2, 5, 4);
        
        // query all with paging, get assets and folders
        pathItems = restClient.findChildren(filterFolderPath, 1, 5, null, DEFAULT_DATE_FORMAT_ID, null, null, null, null);
        validateExtendedPathItem(pathItems, 5, 10, 1);
                
        // query assets and pages, get all
        pathItems = restClient.findChildren(filterFolderPath, 1, 5, null, DEFAULT_DATE_FORMAT_ID, null, null, IPSItemSummary.Category.FOLDER.name() + "," + IPSItemSummary.Category.ASSET.name(), null);
        validateExtendedPathItem(pathItems, 5, 10, 1);
        
        pathItems = restClient.findChildren(filterFolderPath, 1, 5, null, DEFAULT_DATE_FORMAT_ID, null, null, null, "percRawHtmlAsset,Folder");
        validateExtendedPathItem(pathItems, 5, 10, 1);
        
        // query pages cat in asset folder (nothing back)
        pathItems = restClient.findChildren(filterFolderPath, 1, 5, null, DEFAULT_DATE_FORMAT_ID, null, null, IPSItemSummary.Category.PAGE.name(), null);
        validateExtendedPathItem(pathItems, 0, 0, 1);
        
        // query all from site folder
        pathItems = restClient.findChildren(pagesPath, 1, 5, null, DEFAULT_DATE_FORMAT_ID, null, null, null, null);
        validateExtendedPathItem(pathItems, 5, 5, 1);
        
        // query all pages with page cat, get all
        pathItems = restClient.findChildren(pagesPath, 1, 5, null, DEFAULT_DATE_FORMAT_ID, null, null, IPSItemSummary.Category.PAGE.name(), null);
        validateExtendedPathItem(pathItems, 5, 5, 1);
        
        // query just folders
        pathItems = restClient.findChildren(filterFolderPath, 1, 5, null, DEFAULT_DATE_FORMAT_ID, null, null, IPSItemSummary.Category.FOLDER.name(), null);
        validateExtendedPathItem(pathItems, 5, 5, 1);
        
        // query asset folder with section folder cat, get no folders
        pathItems = restClient.findChildren(filterFolderPath, 1, 5, null, DEFAULT_DATE_FORMAT_ID, null, null, IPSItemSummary.Category.SECTION_FOLDER.name(), null);
        validateExtendedPathItem(pathItems, 0, 0, 1);
        
        // query site root, get folder, try with section folder cat, no dice
        String sitePath = SITE_ROOT + site.getName();
        pathItems = restClient.findChildren(sitePath, 1, 5, null, DEFAULT_DATE_FORMAT_ID, null, null, IPSItemSummary.Category.FOLDER.name(), null);
        validateExtendedPathItem(pathItems, 1, 1, 1);
        pathItems = restClient.findChildren(sitePath, 1, 5, null, DEFAULT_DATE_FORMAT_ID, null, null, IPSItemSummary.Category.SECTION_FOLDER.name(), null);
        validateExtendedPathItem(pathItems, 0, 0, 1);

        // query sites root, get site
        pathItems = restClient.findChildren(SITE_ROOT, 1, 5, null, DEFAULT_DATE_FORMAT_ID, null, null, null, "site");
        assertTrue(pathItems.getChildrenCount() > 0);
    }
    
    @Test
    public void test200FindChildren_Child() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        int totalOfPages = 13;
        createNumberedPages(site, path, totalOfPages);
        
        PSPagedItemList pathItems;
        
        // First page, child at the end of the page
        pathItems = restClient.findChildren(path, null, 5, "page005.xml");
        validateExtendedPathItem(pathItems, 5, totalOfPages, 1);
        validatePages(pathItems, 1);
        
        // First page, child in the middle
        pathItems = restClient.findChildren(path, null, 5, "page003.xml");
        validateExtendedPathItem(pathItems, 5, totalOfPages, 1);
        validatePages(pathItems, 1);
        
        // Second page, child at the beginning
        pathItems = restClient.findChildren(path, null, 5, "page006.xml");
        validateExtendedPathItem(pathItems, 5, totalOfPages, 6);
        validatePages(pathItems, 6);
        
        // Second page, child at the end
        pathItems = restClient.findChildren(path, null, 5, "page010.xml");
        validateExtendedPathItem(pathItems, 5, totalOfPages, 6);
        validatePages(pathItems, 6);
        
        // Third page, child at the beginning
        pathItems = restClient.findChildren(path, null, 5, "page011.xml");
        validateExtendedPathItem(pathItems, 3, totalOfPages, 11);
        validatePages(pathItems, 11);
        
        // Third page, child in the middle
        pathItems = restClient.findChildren(path, null, 5, "page012.xml");
        validateExtendedPathItem(pathItems, 3, totalOfPages, 11);
        validatePages(pathItems, 11);
        
        // Third page, child at the end
        pathItems = restClient.findChildren(path, null, 5, "page013.xml");
        validateExtendedPathItem(pathItems, 3, totalOfPages, 11);
        validatePages(pathItems, 11);
    }
    
    @Test
    public void test210FindChildren_Child_NotFound_FirstPageIsReturned() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        int totalOfPages = 11;
        int startIndex = 1;
        createNumberedPages(site, path, totalOfPages);
        
        PSPagedItemList pathItems = restClient.findChildren(path, null, 5, "page013.xml");
        validateExtendedPathItem(pathItems, 5, totalOfPages, 1);
        validatePages(pathItems, startIndex);
    }
    
    @Test
    public void test220FindChildren_Child_MaxResultsIsInvalid() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        int totalOfPages = 5;
        createNumberedPages(site, path, totalOfPages);
        
        try
        {
            restClient.findChildren(path, null, 0, "page003.xml");
            fail("Should have thrown an exception");
        }
        catch (DataRestClientException e)
        {
            assertEquals("error code", 500, e.getStatus());
            assertTrue("error details", e.getResponseBody().contains("java.lang.IllegalArgumentException"));
        }
        
        try
        {
            restClient.findChildren(path, null, -1, "page003.xml");
            fail("Should have thrown an exception");
        }
        catch (DataRestClientException e)
        {
            assertEquals("error code", 500, e.getStatus());
            assertTrue("error details", e.getResponseBody().contains("java.lang.IllegalArgumentException"));
        }
    }
    
    @Test
    public void test230FindChildren_Child_HasPrecedence_OverStartIndex() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        int totalOfPages = 10;
        createNumberedPages(site, path, totalOfPages);
        
        PSPagedItemList pathItems = restClient.findChildren(path, 6, 5, "page003.xml");
        validateExtendedPathItem(pathItems, 5, totalOfPages, 1);
        validatePages(pathItems, 1);
    }
    
    @Test
    public void test240FindChildren_Child_IsInvalid() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        int totalOfPages = 10;
        createNumberedPages(site, path, totalOfPages);
        
        try
        {
            restClient.findChildren(path, null, 5, "");
            fail("Should have thrown an exception");
        }
        catch (DataRestClientException e)
        {
            assertEquals("error code", 500, e.getStatus());
            assertTrue("error details", e.getResponseBody().contains("java.lang.IllegalArgumentException"));
        }
    }
    
    
    @Ignore("Folder doesn't have created date property")
    public void test250FindChildren_WithoutPaging_With_DisplayFormat() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        createNumberedPages(site, path, 5);
        
        PSPagedItemList pathItems;
        
        // startIndex without displayFormat should return no displayProperties
        pathItems = restClient.findChildren(path, null, null, null);
        validateExtendedPathItem(pathItems, 5, 5, 1);
        validatePages(pathItems, 1);
        
        for (PSPathItem item : pathItems.getChildrenInPage())
        {
            assertTrue("displayProperties is null or empty", item.getDisplayProperties() == null || item.getDisplayProperties().isEmpty());
        }
        
        // startIndex with displayFormat should return something in displayProperties
        pathItems = restClient.findChildren(path, null, null, null, DEFAULT_DATE_FORMAT_ID);
        validateExtendedPathItem(pathItems, 5, 5, 1);
        validatePages(pathItems, 1);
        
        for (PSPathItem item : pathItems.getChildrenInPage())
        {
            assertTrue("displayProperties is not null nor empty", item.getDisplayProperties() != null && item.getDisplayProperties().size() > 0);
        }
    }
    
    
    @Ignore("Folder doesn't have created date property")
    public void test260FindChildren_StartIndex_With_DisplayFormat() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        createNumberedPages(site, path, 5);
        
        PSPagedItemList pathItems;
        
        // startIndex without displayFormat should return no displayProperties
        pathItems = restClient.findChildren(path, 2, 2, null);
        validateExtendedPathItem(pathItems, 2, 5, 2);
        validatePages(pathItems, 2);
        
        for (PSPathItem item : pathItems.getChildrenInPage())
        {
            assertTrue("displayProperties is null or empty", item.getDisplayProperties() == null || item.getDisplayProperties().isEmpty());
        }
        
        // startIndex with displayFormat should return something in displayProperties
        pathItems = restClient.findChildren(path, 2, 2, null, DEFAULT_DATE_FORMAT_ID);
        validateExtendedPathItem(pathItems, 2, 5, 2);
        validatePages(pathItems, 2);
        
        for (PSPathItem item : pathItems.getChildrenInPage())
        {
            assertTrue("displayProperties is not null nor empty", item.getDisplayProperties() != null && item.getDisplayProperties().size() > 0);
        }
    }
    
    
    @Ignore("Folder doesn't have created date property")
    public void test270FindChildren_Child_With_DisplayFormat() throws Exception
    {
        PSSite site = createSite();
        String path = SITE_ROOT + site.getName() + "/folder1";
        
        int totalOfPages = 10;
        createNumberedPages(site, path, totalOfPages);
        
        PSPagedItemList pathItems;
        
        // child without displayFormat should return no displayProperties
        pathItems = restClient.findChildren(path, null, 5, "page003.xml");
        validateExtendedPathItem(pathItems, 5, totalOfPages, 1);
        validatePages(pathItems, 1);
        
        for (PSPathItem item : pathItems.getChildrenInPage())
        {
            assertTrue("displayProperties is null or empty", item.getDisplayProperties() == null || item.getDisplayProperties().isEmpty());
        }
        
        // child with displayFormat should return something in displayProperties
        pathItems = restClient.findChildren(path, null, 5, "page007.xml", DEFAULT_DATE_FORMAT_ID);
        validateExtendedPathItem(pathItems, 5, totalOfPages, 6);
        validatePages(pathItems, 6);
        
        for (PSPathItem item : pathItems.getChildrenInPage())
        {
            assertTrue("displayProperties is not null nor empty", item.getDisplayProperties() != null && item.getDisplayProperties().size() > 0);
        }
    }

    /**
     * Testing folder permission affecting delete folders.
     * 
     * @throws Exception
     */
    @Ignore 
    public void test280FolderPermission() throws Exception
    {
        PSPathItem folder1 = restClient.addFolder(ASSET_ROOT + TEST_FOLDER);
        assertNotNull(folder1);

        String folder2Path = ASSET_ROOT + TEST_FOLDER + "/Folder2" + System.currentTimeMillis();
        PSPathItem folder2 = restClient.addFolder(folder2Path);
        assertNotNull(folder2);

        String folder3Path = folder2Path + "/Folder3" + System.currentTimeMillis();
        PSPathItem folder3 = restClient.addFolder(folder3Path);
        assertNotNull(folder3);

        setFolderPermission(folder3.getId(), PSFolderPermission.Access.READ);

        // has no ADMIN permission for the READONLY folder
        hasNoAdminPermission(folder3Path);

        setFolderPermission(folder3.getId(), PSFolderPermission.Access.WRITE);

        // has no ADMIN permission for the folder with WRITE sub-folder
        hasNoAdminPermission(folder2Path);

        assertTrue(deleteFolder(restClient, folder2Path) == 0);
    }

    /**
     * Tests delete and move (admin ACL) folder under a read-only parent folder
     * 
     * @throws Exception if an error occurs.
     */
    @Test
    public void test300ReadonlyParentFolder() throws Exception
    {
        PSPathItem folder1 = restClient.addFolder(ASSET_ROOT + TEST_FOLDER);
        assertNotNull(folder1);

        // prepare a read-only parent folder and a child folder with ADMIN ACL
        String folderReadonlyPath = ASSET_ROOT + TEST_FOLDER + "/FolderReadonly" + System.currentTimeMillis();
        PSPathItem folderReadonly = restClient.addFolder(folderReadonlyPath);
        assertNotNull(folderReadonly);
        setFolderPermission(folderReadonly.getId(), PSFolderPermission.Access.READ);

        String folder3Path = folderReadonlyPath + "/Folder3" + System.currentTimeMillis();
        PSPathItem folder3 = restClient.addFolder(folder3Path);
        assertNotNull(folder3);

        // delete a child folder under a read-only parent
        int undeleteItems = deleteFolder(restClientEditor, folder3Path);
        assertTrue(undeleteItems == 0);

        // prepare move a folder under read-only parent folder
        folder3 = restClient.addFolder(folder3Path);

        String folderTargetPath = ASSET_ROOT + TEST_FOLDER + "/FolderTarget" + System.currentTimeMillis();
        restClient.addFolder(folderTargetPath);

        // move the folder
        PSMoveFolderItem request = new PSMoveFolderItem();
        request.setTargetFolderPath(folderTargetPath);
        request.setItemPath(folder3Path);
        restClientEditor.moveItem(request);
    }

    /**
     * Testing on the specified folder has no ADMIN permission "editor1" user
     * (the user does not have "admin" role).
     * 
     * @param folderPath the folder without ADMIN access, assumed not blank.
     */
    private void hasNoAdminPermission(String folderPath)
    {
        String response = restClientEditor.validateFolderDelete(folderPath);
        assertEquals(PSPathItemService.FOLDER_HAS_NO_ADMIN_PERMISSION, response);

        int undeleteItems = deleteFolder(restClientEditor, folderPath);
        assertTrue("Cannot delete folder without ADMIN permission", undeleteItems > 0);
    }

    /**
     * Sets the folder permission of the specified folder to the specified ACL
     * 
     * @param id the ID of the specified folder, assumed not blank.
     * @param access the ACL to set to on the folder, assumed not
     * <code>null</code>.
     */
    private void setFolderPermission(String id, PSFolderPermission.Access access)
    {
        PSFolderProperties props = restClient.findFolderProperties(id);
        PSFolderPermission perm = props.getPermission();
        perm.setAccessLevel(access);
        restClient.saveFolderProperties(props);

        // ADMIN Access for "admin" role
        PSPathItem folder = restClient.findById(id);
        assertTrue(folder.getAccessLevel() == PSFolderPermission.Access.ADMIN);

        // READ access for "editor1" user
        folder = restClientEditor.findById(folder.getId());
        assertTrue(folder.getAccessLevel() == access);
    }

    
    public void testAddFolder() throws Exception
    {
        PSSite site = createSite();

        testAddFolder(SITE_ROOT + site.getName() + '/');
        testAddFolder(ASSET_ROOT);
    }

    private String getTemplateId(PSSite site)
    {
        return siteTemplateRestClient.findTemplatesBySite(site.getId()).get(0).getId();
    }

    private void makeTestFolderIfNeeded() throws Exception
    {
        try
        {
            restClient.find(ASSET_ROOT + TEST_FOLDER);
        }
        catch (RestClientException e)
        {
            restClient.addFolder(ASSET_ROOT + TEST_FOLDER);
        }
    }

    /**
     * Tests findFolderProperties and saveFolderProperties operations.
     * 
     * @throws Exception if an error occurs.
     */
    @Test
    public void test310FolderProperties() throws Exception
    {
        makeTestFolderIfNeeded();
        PSPathItem newFolder = restClient.addFolder(ASSET_ROOT + TEST_FOLDER + "/testFolderProperties");

        // validate the created folder permission == ADMIN
        PSFolderProperties props = restClient.findFolderProperties(newFolder.getId());
        PSFolderPermission perm = props.getPermission();
        PSFolderPermission.Access access = perm.getAccessLevel();
        assertTrue(access == PSFolderPermission.Access.ADMIN);
        assertNull(perm.getAdminPrincipals());
        assertNull(perm.getReadPrincipals());
        assertNull(perm.getWritePrincipals());

        perm.setAccessLevel(PSFolderPermission.Access.WRITE);
        List<Principal> adminUsers = new ArrayList<Principal>();
        Principal admin = new Principal();
        admin.setName("admin");
        admin.setType(PrincipalType.USER);
        adminUsers.add(admin);
        perm.setAdminPrincipals(adminUsers);
        List<Principal> writeUsers = new ArrayList<Principal>();
        Principal writer = new Principal();
        writer.setName("writer");
        writer.setType(PrincipalType.USER);
        writeUsers.add(writer);
        perm.setWritePrincipals(writeUsers);
        List<Principal> readUsers = new ArrayList<Principal>();
        Principal reader = new Principal();
        reader.setName("reader");
        reader.setType(PrincipalType.USER);
        readUsers.add(reader);
        perm.setReadPrincipals(readUsers);

        restClient.saveFolderProperties(props);

        // validate the created folder permission == WRITE and one ADMIN, WRITE,
        // READ user exist
        props = restClient.findFolderProperties(newFolder.getId());
        perm = props.getPermission();
        access = perm.getAccessLevel();
        assertTrue(access == PSFolderPermission.Access.WRITE);
        assertTrue(perm.getAdminPrincipals().size() == 1 && perm.getAdminPrincipals().get(0).equals(admin));
        assertTrue(perm.getWritePrincipals().size() == 1 && perm.getWritePrincipals().get(0).equals(writer));
        assertTrue(perm.getReadPrincipals().size() == 1 && perm.getReadPrincipals().get(0).equals(reader));

        // cannot save folder with non-unique name
        PSPathItem newFolder2 = restClient.addFolder(ASSET_ROOT + TEST_FOLDER + "/testFolderProperties-2");
        props.setName(newFolder2.getName());
        try
        {
            restClient.saveFolderProperties(props);
            assertTrue("Should fail to save non-unique folder name", false);
        }
        catch (PSObjectRestClient.DataRestClientException e)
        {
            String errorMsg = e.getMessage();
            assertTrue(errorMsg.indexOf("Cannot save folder properties") != -1);
        }

        // cannot find a non-existing folder
        newFolder = restClient.addFolder(ASSET_ROOT + TEST_FOLDER + "/testFolderProperties"
                + String.valueOf(System.currentTimeMillis()));
        deleteFolder(newFolder);
        try
        {
            restClient.findFolderProperties(newFolder.getId());
            fail("Should fail to find folder id=" + newFolder.getId());
        }
        catch (PSObjectRestClient.DataValidationRestClientException e)
        {
            assertTrue("Expect exception: " + e.getMessage(), true);
        }
    }

    private int deleteFolder(PSPathItem folder)
    {
        PSDeleteFolderCriteria criteria = new PSDeleteFolderCriteria();
        criteria.setPath(folder.getPath());
        criteria.setSkipItems(SkipItemsType.NO);
        return Integer.valueOf(restClient.deleteFolder(criteria));
    }

    @Test
    public void test320AddNewFolder() throws Exception
    {
        PSSite site = createSite();

        String templateId = getTemplateId(site);
        testAddNewFolder(SITE_ROOT + site.getName(), "//Sites/" + site.getName(), templateId);

        makeTestFolderIfNeeded();

        testAddNewFolder(ASSET_ROOT + TEST_FOLDER, ASSET_TEST_FOLDER, templateId);

        try
        {
            restClient.addNewFolder(SITE_ROOT);
            fail("Add new folder to " + SITE_ROOT + " should have thrown exception");
        }
        catch (RestClientException e)
        {
            assertEquals(500, e.getStatus());
        }

        int initialChildren = restClient.findChildren(ASSET_ROOT).size();
        PSPathItem newFolder1 = restClient.addNewFolder(ASSET_ROOT);
        assertEquals(initialChildren + 1, restClient.findChildren(ASSET_ROOT).size());
        assertTrue(newFolder1.getPath().startsWith("/" + ASSET_ROOT + newFolder1.getName()));

        PSPathItem newFolder2 = restClient.addNewFolder(ASSET_ROOT);
        assertEquals(initialChildren + 2, restClient.findChildren(ASSET_ROOT).size());

        assertFalse(newFolder1.getName().equalsIgnoreCase(newFolder2.getName()));

        // cleanup
        int undeleteItems = deleteFolder(newFolder1);
        assertTrue(undeleteItems == 0);
        assertEquals(initialChildren + 1, restClient.findChildren(ASSET_ROOT).size());

        deleteFolder(newFolder2);

        assertEquals(initialChildren, restClient.findChildren(ASSET_ROOT).size());
    }

    @Test
    public void test330DeleteFolder() throws Exception
    {
        PSSite site = createSite();

        String templateId = getTemplateId(site);
        testDeleteFolder(SITE_ROOT, site.getName(), "//Sites/" + site.getName(), templateId);

        createTestFolderIfNeeded();

        testDeleteFolder(ASSET_ROOT, TEST_FOLDER, ASSET_TEST_FOLDER, templateId);
    }

    @Test
    public void test340DeleteFolderPageWithLocalContent() throws Exception
    {
        PSSite site = createSite();

        String templateId = getTemplateId(site);

        PSPathItem siteResourcesFolder = restClient.addFolder(SITE_ROOT + site.getName() + "/resources");
        String siteResourcesPath = siteResourcesFolder.getPath();

        // create a local asset
        String resourceId = createAsset("ResourceAsset", null).getId();
        assetCleaner.add(resourceId);
        PSAsset assetResourceItem = getAssetClient().get(resourceId);
        assertEquals(resourceId, assetResourceItem.getId());

        // create a page
        PSPage pageNew = createPage(siteResourcesFolder.getFolderPath(), templateId);
        pageNew = getPageRestClient().save(pageNew);
        String pageId = pageNew.getId();
        pageCleaner.add(pageId);

        // add asset to page
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(pageId, 5, "widget5", resourceId, 1);
        getAssetClient().createAssetWidgetRelationship(awRel);

        // delete asset resources folder
        PSDeleteFolderCriteria criteria = new PSDeleteFolderCriteria();
        criteria.setPath(siteResourcesPath);
        restClient.deleteFolder(criteria);

        // make sure page and asset have been deleted
        assertItemPurged(getPageRestClient(), pageId);
        assertItemPurged(getAssetClient(), resourceId);

        // make sure folder has been deleted
        try
        {
            restClient.find(siteResourcesPath);
            fail("Folder: " + siteResourcesPath + " should have been deleted");
        }
        catch (RestClientException e)
        {
            // expected
        }
    }

    @Test
    public void test350RenameFolder() throws Exception
    {
        PSSite site = createSite();

        String templateId = getTemplateId(site);
        testRenameFolder(SITE_ROOT, site.getName(), "//Sites/" + site.getName(), templateId);

        createTestFolderIfNeeded();

        testRenameFolder(ASSET_ROOT, TEST_FOLDER, ASSET_TEST_FOLDER, templateId);
    }

    @Test
    public void test360ValidateDeleteFolder() throws Exception
    {
        PSSite site = createSite();

        String templateId = getTemplateId(site);
        testValidateDeleteSiteFolder(SITE_ROOT, site.getName(), "//Sites/" + site.getName(), templateId);

        createTestFolderIfNeeded();

        testValidateDeleteAssetFolder(ASSET_ROOT, TEST_FOLDER, ASSET_TEST_FOLDER, templateId);
    }

    private String createTestFolderIfNeeded()
    {
        String testFolderPath = ASSET_ROOT + TEST_FOLDER;
        try
        {
            restClient.find(testFolderPath);
        }
        catch (RestClientException e)
        {
            restClient.addFolder(testFolderPath);
        }
        
        return testFolderPath;
    }

    @Test
    public void test380FindItemPropertiesByWfState() throws Exception
    {
        PSSite site = createSite();

        // find pages in Draft state
        PSItemByWfStateRequest request = new PSItemByWfStateRequest();
        request.setPath(SITE_ROOT + site.getName());
        request.setWorkflow("Default Workflow");
        request.setState("Draft");
        List<PSItemProperties> props = restClient.findItemProperties(request);
        // should only be the home page
        assertEquals(1, props.size());
        PSItemProperties homePageProps = props.get(0);
        assertEquals(site.getHomePageTitle(), homePageProps.getName());
        
        String templateId = getTemplateId(site);
        String folder1Path = SITE_ROOT + site.getName() + "/folder1";
        String folder2Path = folder1Path + "/folder2";

        PSPathItem folder2 = restClient.addFolder(folder2Path);
        assertNotNull(folder2);
        assertNotNull(restClient.get(folder2Path));

        // add a page to folder
        PSPage pageNew = createPage("//Sites/" + site.getName(), templateId);
        String pageId = getPageRestClient().save(pageNew).getId();
        pageCleaner.add(pageId);
        
        props = restClient.findItemProperties(request);
        // should be two pages
        assertEquals(2, props.size());
        boolean homePage = false;
        boolean newPage = false;
        for (PSItemProperties itemProps : props)
        {
            if (itemProps.getName().equals(site.getHomePageTitle()))
            {
                homePage = true;
            }
            else if (itemProps.getId().equals(pageId))
            {
                newPage = true;
            }
        }
        assertTrue(homePage);
        assertTrue(newPage);
 
        // add page to sub-folder
        PSPage pageNew2 = createPage(folder2.getFolderPath(), templateId);
        String pageId2 = getPageRestClient().save(pageNew2).getId();
        pageCleaner.add(pageId2);
        
        // transition the page to pending
        getItemWorkflowClient().transition(pageId2, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);
        
        // find pages in Pending state
        request.setState("Pending");
        props = restClient.findItemProperties(request);
        // should be one page
        assertEquals(1, props.size());
        assertTrue(props.get(0).getId().equals(pageId2));
        
        // find pages in all states
        request.setState(null);
        props = restClient.findItemProperties(request);
        // should be three pages
        assertEquals(3, props.size());
        homePage = false;
        newPage = false;
        boolean pendingPage = false;
        for (PSItemProperties itemProps : props)
        {
            if (itemProps.getName().equals(site.getHomePageTitle()))
            {
                homePage = true;
            }
            else
            {
                String id = itemProps.getId();
                if (id.equals(pageId))
                {
                    newPage = true;
                }
                else if (id.equals(pageId2))
                {
                    pendingPage = true;
                }
            }
        }
        assertTrue(homePage);
        assertTrue(newPage);
        assertTrue(pendingPage);
        
        // find pages in Live state
        request.setState("Live");
        props = restClient.findItemProperties(request);
        // should be empty
        assertTrue(props.isEmpty());
        
        // test bad workflow
        request.setWorkflow("Bad Workflow");
        try
        {
            restClient.findItemProperties(request);
            fail("Unknown workflow was specified");
        }
        catch (RestClientException e)
        {
            assertEquals(500, e.getStatus());
        }
    }
    
    @Test
    public void test390FindLastExistingPath() throws Exception
    {
        PSSite site = createSite();

        assertEquals(site.getName(), restClient.findLastExistingPath(SITE_ROOT + site.getName()));
        assertEquals("", restClient.findLastExistingPath(SITE_ROOT + "foo"));
        assertEquals(site.getName(), restClient.findLastExistingPath(SITE_ROOT + site.getName() + "/foo"));
        
        restClient.addFolder(SITE_ROOT + site.getName() + "/foo");
        
        assertEquals(site.getName() + "/foo", restClient.findLastExistingPath(SITE_ROOT + site.getName() + "/foo"));
        assertEquals("", restClient.findLastExistingPath(SITE_ROOT + "foo/foo1/foo2"));
    }
    
    @AfterClass
    public static void tearDown()
    {
        restClient.login("admin1", "demo");

        tearDown(ASSET_ROOT);
        assetCleaner.clean();
        templateCleaner.clean();
        pageCleaner.clean();
        siteCleaner.clean();
    }

    private void testFindRoot(String path)
    {
        PSPathItem pathItem = restClient.get(path);
        assertNotNull(pathItem);
        assertEquals('/' + path, pathItem.getPath());
        assertFalse(pathItem.isLeaf());
    }

    @Test
    public void test400FindItemById() throws Exception
    {
        List<PSPathItem> pathItems = restClient.findChildren(ASSET_ROOT);
        assertNotNull(pathItems);
        assertNotNull(pathItems.size() > 0);

        String id = pathItems.get(0).getId();
        PSPathItem item = restClient.findById(id);
        assertNotNull(item);
        assertNotNull(item.getAccessLevel());

        // negative test
        PSLegacyGuid guid = new PSLegacyGuid(Integer.MAX_VALUE);
        try
        {
            item = restClient.findById(guid.toString());
            fail("Should fail to find an item with invalid ID");
        }
        catch (Exception e)
        {
            String errorMsg = e.getMessage();
            assertTrue(errorMsg.indexOf("HTTP Error code: 400") != -1);
            assertTrue(errorMsg.indexOf("Cannot find item with id =") != -1);
        }
    }

    private void testFindChildren(String path)
    {
        List<PSPathItem> pathItems = restClient.findChildren(path);
        assertNotNull(pathItems);
    }

    private void testAddFolder(String path)
    {
        String folder1Path = path + TEST_FOLDER;
        String folder2Path = folder1Path + "/folder2";
        String folder3Path = folder1Path + "/folder3/folder4";
        String folder5Path = folder1Path + "/folder5/folder5";

        try
        {
            restClient.get(folder1Path);
            PSDeleteFolderCriteria criteria = new PSDeleteFolderCriteria();
            criteria.setPath(folder1Path);
            criteria.setSkipItems(SkipItemsType.NO);
            assertTrue(restClient.deleteFolder(criteria).equals("0"));
        }
        catch (Exception e)
        {
            // should not exist
        }

        PSPathItem folder1 = restClient.addFolder(folder1Path);
        assertNotNull(folder1);
        assertNotNull(restClient.get(folder1Path));

        PSPathItem folder2 = restClient.addFolder(folder2Path);
        assertNotNull(folder2);
        assertNotNull(restClient.get(folder2Path));

        PSPathItem folder3 = restClient.addFolder(folder3Path);
        assertNotNull(folder3);
        assertNotNull(restClient.get(folder3Path));

        PSPathItem folder4 = restClient.addFolder(folder1Path);
        // Don't need to compare the Children properties in this case
        folder4.setHasFolderChildren(false);
        assertEquals(folder4, folder1);

        PSPathItem folder5 = restClient.addFolder(folder5Path);
        assertNotNull(folder5);
        assertNotNull(restClient.get(folder5Path));
    }

    private void testAddNewFolder(String path, String baseFolderPath, String templateId) throws Exception
    {
        int numChildren = restClient.findChildren(path).size();

        // add new folders under site folder
        PSPathItem newFolder = restClient.addNewFolder(path);
        PSPathItem newFolder2 = restClient.addNewFolder(path);
        PSPathItem newFolder3 = restClient.addNewFolder(path);
        assertEquals(numChildren + 3, restClient.findChildren(path).size());
        assertTrue(newFolder.getPath().endsWith("/New-Folder/"));
        assertTrue(newFolder2.getPath().endsWith("/New-Folder-2/"));
        assertTrue(newFolder3.getPath().endsWith("/New-Folder-3/"));

        // delete new folder 2
        int undeleteItems = deleteFolder(newFolder2);
        assertTrue(undeleteItems == 0);

        // add another new folder
        newFolder2 = restClient.addNewFolder(path);
        assertEquals(numChildren + 3, restClient.findChildren(path).size());
        assertTrue(newFolder2.getPath().endsWith("/New-Folder-2/"));

        // add a new folder under existing new folder
        PSPathItem newFolderFolder = restClient.addNewFolder(newFolder.getPath());
        assertEquals(1, restClient.findChildren(newFolder.getPath()).size());
        assertTrue(newFolderFolder.getPath().endsWith("/New-Folder/"));

        // add a page to folder
        PSPage pageNew = createPage(baseFolderPath, templateId);
        String pageId = getPageRestClient().save(pageNew).getId();
        pageCleaner.add(pageId);

        // add a new folder as sibling of page
        String tmpPath;
        if (path.startsWith("/"))
        {
            tmpPath = path.substring(1);
        }
        else
        {
            tmpPath = path;
        }

        String itemPath = tmpPath + '/' + pageNew.getName();
        PSPathItem pageItem = restClient.find(itemPath);
        PSItemProperties pageProps = restClient.findItemProperties(itemPath);
        PSPathItem newFolderSibling = restClient.addNewFolder(pageItem.getPath());
        assertEquals(numChildren + 5, restClient.findChildren(path).size());
        assertTrue(newFolderSibling.getPath().endsWith("/New-Folder-4/"));
        // property name is the link text/title for page
        assertEquals("dummy", pageProps.getName());

        // testing move item API
        if (path.startsWith(SITE_ROOT))
        {
            testMovePage(pageNew, baseFolderPath, templateId, newFolderFolder.getPath(), itemPath);
        }
    }

    private void testMovePage(PSPage existingPage, String baseFolderPath, String templateId, String targetFolder,
            String pagePath) throws Exception
    {
        PSMoveFolderItem request = new PSMoveFolderItem();
        request.setTargetFolderPath(targetFolder);
        request.setItemPath(PSPathUtils.getFinderPath(pagePath));
        restClient.moveItem(request);

        // create a page with the same page
        String oldName = existingPage.getName();
        PSPage page = createPage(baseFolderPath, templateId);
        page = getPageRestClient().save(page);
        page.setName(oldName);
        page = getPageRestClient().save(page);

        try
        {
            restClient.moveItem(request);
            fail("Cannot move item to a folder that contains items with the same name.");
        }
        catch (Exception e)
        {
            // will be here.
        }
        
        // delete the page with the same name
        getPageRestClient().delete(page.getId());
        
        try
        {
            restClient.moveItem(request);
        }
        catch (Exception e)
        {
            fail("Move item failed: item does not exist.");
        }
        
        // create another page to be moved
        page = createPage(baseFolderPath, templateId);
        page = getPageRestClient().save(page);
        
        pagePath = page.getFolderPath() + '/' + page.getName();
        request.setItemPath(PSPathUtils.getFinderPath(pagePath));
        
        // delete the target folder
        PSPathItem tgtFolder = restClient.find(targetFolder);
        deleteFolder(tgtFolder);
        
        try
        {
            restClient.moveItem(request);
        }
        catch (Exception e)
        {
            fail("Move item failed: target folder does not exist.");
        }
    }

    /**
     * Delete the specified folder.
     * 
     * @param path the path of the to be deleted folder, assumed not blank.
     * 
     * @return the number of undeleted items.
     */
    private int deleteFolder(PSPathServiceRestClient client, String path)
    {
        PSDeleteFolderCriteria criteria = new PSDeleteFolderCriteria();
        criteria.setPath(path);
        criteria.setSkipItems(PSDeleteFolderCriteria.SkipItemsType.NO);
        return Integer.valueOf(client.deleteFolder(criteria));
    }

    @SuppressWarnings("unchecked")
    private void testDeleteFolder(String path, String testFolder, String baseFolderPath, String templateId)
        throws Exception
    {
        String folderPath = path + testFolder;

        // add a page to the folder
        PSPage pageNew = createPage(baseFolderPath, templateId);
        String pageId = getPageRestClient().save(pageNew).getId();
        pageCleaner.add(pageId);

        // transition the page to pending
        getItemWorkflowClient().transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);

        String skipPath = folderPath + "/skip";
        Map<PSAsset, List> inUseAssetToPurgeItemsMap = createTestFolder(skipPath, baseFolderPath, templateId, pageId);
        Iterator<PSAsset> iter = inUseAssetToPurgeItemsMap.keySet().iterator();
        PSAsset inUseAsset = iter.next();
        List purgeItems = inUseAssetToPurgeItemsMap.get(inUseAsset);

        assertTrue(restClient.findChildren(skipPath).size() > 1);
        PSDeleteFolderCriteria criteria = new PSDeleteFolderCriteria();
        criteria.setPath(skipPath);
        criteria.setSkipItems(SkipItemsType.YES);
        String undeleteItems = restClient.deleteFolder(criteria);
        assertTrue(undeleteItems.equals("1"));
        List<PSPathItem> items = restClient.findChildren(skipPath);
        assertEquals(1, items.size());
        assertEquals(inUseAsset.getId(), items.get(0).getId());
        checkPurgedItems(purgeItems);

        String dontSkipPath = folderPath + "/dontSkip";
        inUseAssetToPurgeItemsMap = createTestFolder(dontSkipPath, baseFolderPath, templateId, pageId);
        iter = inUseAssetToPurgeItemsMap.keySet().iterator();
        inUseAsset = iter.next();
        purgeItems = inUseAssetToPurgeItemsMap.get(inUseAsset);
        if (path.startsWith(ASSET_ROOT))
        {
            purgeItems.add(inUseAsset);
        }

        assertTrue(restClient.findChildren(dontSkipPath).size() > 1);
        criteria.setPath(dontSkipPath);
        criteria.setSkipItems(SkipItemsType.NO);
        undeleteItems = restClient.deleteFolder(criteria);
        assertTrue(undeleteItems.equals("0"));
        checkPurgedItems(purgeItems);

        boolean folderExists = true;
        try
        {
            restClient.get(dontSkipPath);
        }
        catch (Exception e)
        {
            // folder should not exist
            folderExists = false;
        }

        assertFalse("Failed to delete folder: " + dontSkipPath, folderExists);

        String nullSkipPath = folderPath + "/nullSkip";
        inUseAssetToPurgeItemsMap = createTestFolder(nullSkipPath, baseFolderPath, templateId, pageId);
        iter = inUseAssetToPurgeItemsMap.keySet().iterator();
        inUseAsset = iter.next();
        purgeItems = inUseAssetToPurgeItemsMap.get(inUseAsset);

        assertTrue(restClient.findChildren(nullSkipPath).size() > 1);
        criteria = new PSDeleteFolderCriteria();
        criteria.setPath(nullSkipPath);
        undeleteItems = restClient.deleteFolder(criteria);
        assertEquals("1", undeleteItems);
        items = restClient.findChildren(nullSkipPath);
        assertEquals(1, items.size());
        assertEquals(inUseAsset.getId(), items.get(0).getId());
        checkPurgedItems(purgeItems);

        String emptySkipPath = folderPath + "/emptySkip";
        inUseAssetToPurgeItemsMap = createTestFolder(emptySkipPath, baseFolderPath, templateId, pageId);
        iter = inUseAssetToPurgeItemsMap.keySet().iterator();
        inUseAsset = iter.next();
        purgeItems = inUseAssetToPurgeItemsMap.get(inUseAsset);

        assertTrue(restClient.findChildren(emptySkipPath).size() > 1);
        criteria = new PSDeleteFolderCriteria();
        criteria.setPath(emptySkipPath);
        criteria.setSkipItems(SkipItemsType.EMPTY);
        assertEquals("1", restClient.deleteFolder(criteria));
        items = restClient.findChildren(emptySkipPath);
        assertEquals(1, items.size());
        assertEquals(inUseAsset.getId(), items.get(0).getId());
        checkPurgedItems(purgeItems);
    }

    private void testRenameFolder(String rootPath, String testFolder, String baseFolderPath, String templateId)
        throws Exception
    {
        String folder1Path = rootPath + testFolder + "/folder1";
        String folder2Path = folder1Path + "/folder2";

        try
        {
            restClient.get(folder2Path);
            PSDeleteFolderCriteria criteria = new PSDeleteFolderCriteria();
            criteria.setPath(folder2Path);
            criteria.setSkipItems(SkipItemsType.NO);
            assertTrue(restClient.deleteFolder(criteria).equals("0"));
        }
        catch (Exception e)
        {
            // should not exist
        }

        PSPathItem folder2 = restClient.addFolder(folder2Path);
        assertNotNull(folder2);
        assertNotNull(restClient.get(folder2Path));

        PSRenameFolderItem rfItem = new PSRenameFolderItem();
        rfItem.setPath(folder1Path);
        rfItem.setName("renamedfolder1");
        PSPathItem renamedFolder1 = restClient.renameFolder(rfItem);
        assertEquals(rfItem.getName(), renamedFolder1.getName());
        try
        {
            restClient.find(folder1Path);
            fail("Folder: " + folder1Path + " should have been renamed to: " + renamedFolder1.getName());
        }
        catch (RestClientException e)
        {
            assertEquals(500, e.getStatus());
        }

        // add a page to folder
        PSPage pageNew = createPage(baseFolderPath, templateId);
        String pageId = getPageRestClient().save(pageNew).getId();
        pageCleaner.add(pageId);

        // try to rename page
        PSPathItem item = restClient.find(rootPath + testFolder + '/' + pageNew.getName());
        try
        {
            rfItem.setPath(item.getPath());
            rfItem.setName("renamedItem");
            restClient.renameFolder(rfItem);
        }
        catch (RestClientException e)
        {
            // should fail
            assertEquals(500, e.getStatus());
        }

        try
        {
            rfItem.setPath(rootPath);
            rfItem.setName("renamedRoot");
            restClient.renameFolder(rfItem);
            fail("Rename folder: " + rfItem.getPath() + " to: " + rfItem.getName() + "should have thrown exception");
        }
        catch (RestClientException e)
        {
            assertEquals(500, e.getStatus());
        }
    }

    private void testValidateDeleteSiteFolder(String path, String testFolder, String baseFolderPath, String templateId)
        throws Exception
    {
        restClient.login("admin1", "demo");

        String folderPath = path + testFolder + "/folder";
        PSPathItem folderItem = restClient.addFolder(folderPath);
        assertTrue(restClient.findChildren(folderPath).isEmpty());
        String result = restClient.validateFolderDelete(folderPath);

        // nothing in use
        assertEquals(PSPathItemService.VALIDATE_SUCCESS, result);

        // add a page to base folder
        PSPage pageNew = createPage(baseFolderPath, templateId);
        String pageId = getPageRestClient().save(pageNew).getId();
        pageCleaner.add(pageId);

        // add asset to folder
        String assetId = createAsset("MyAsset", baseFolderPath + "/folder").getId();
        result = restClient.validateFolderDelete(folderPath);

        // nothing in use
        assertEquals(PSPathItemService.VALIDATE_SUCCESS, result);

        // add asset to page
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(pageId, 5, "widget5", assetId, 1);
        getAssetClient().createAssetWidgetRelationship(awRel);
        result = restClient.validateFolderDelete(folderPath);

        // not in use
        assertEquals(PSPathItemService.VALIDATE_SUCCESS, result);

        // transition the page to pending
        getItemWorkflowClient().transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);

        // in use
        assertEquals(PSSitePathItemService.PAGES_IN_USE_PAGES, restClient.validateFolderDelete(folderPath));

        // clear the relationship and asset
        getItemWorkflowClient().checkOut(pageId);
        getAssetClient().forceDelete(assetId);
       
        // add a page to the asset folder
        pageNew = createPage(baseFolderPath + "/folder", templateId);
        pageId = getPageRestClient().save(pageNew).getId();
        pageCleaner.add(pageId);

        // add a new asset to the page and folder
        assetId = createAsset("MyAsset2", baseFolderPath + "/folder").getId();
        assetCleaner.add(assetId);
        awRel = new PSAssetWidgetRelationship(pageId, 5, "widget5", assetId, 1);
        getAssetClient().createAssetWidgetRelationship(awRel);
        getAssetClient().addAssetToFolder(folderItem.getFolderPath(), assetId);

        // transition the page to pending
        getItemWorkflowClient().transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);

        result = restClient.validateFolderDelete(folderPath);

        // not in use
        assertEquals(PSPathItemService.VALIDATE_SUCCESS, result);

        // switch to unauthorized user
        restClient.login("author1", "demo");

        result = restClient.validateFolderDelete(folderPath);

        // should not be authorized
        assertEquals(PSSitePathItemService.PAGES_NOT_AUTHORIZED, result);
    }

    private void testValidateDeleteAssetFolder(String path, String testFolder, String baseFolderPath, String templateId)
        throws Exception
    {
        restClient.login("admin1", "demo");

        String folderPath = path + testFolder + "/folder";
        PSPathItem folderItem = restClient.addFolder(folderPath);
        assertTrue(restClient.findChildren(folderPath).isEmpty());
        String result = restClient.validateFolderDelete(folderPath);

        // nothing in use
        assertEquals(PSPathItemService.VALIDATE_SUCCESS, result);

        // add a page to base folder
        PSPage pageNew = createPage(baseFolderPath, templateId);
        String pageId = getPageRestClient().save(pageNew).getId();
        pageCleaner.add(pageId);

        // add asset to folder
        String assetId = createAsset("MyAsset", baseFolderPath + "/folder").getId();
        result = restClient.validateFolderDelete(folderPath);

        // nothing in use
        assertEquals(PSPathItemService.VALIDATE_SUCCESS, result);

        // add asset to page
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(pageId, 5, "widget5", assetId, 1);
        getAssetClient().createAssetWidgetRelationship(awRel);
        result = restClient.validateFolderDelete(folderPath);

        // not in use
        assertEquals(PSPathItemService.VALIDATE_SUCCESS, result);

        // transition the page to pending
        getItemWorkflowClient().transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);

        // in use
        assertEquals(PSAssetPathItemService.ASSETS_IN_USE_PAGES, restClient.validateFolderDelete(folderPath));

        // clear the relationship and asset
        getItemWorkflowClient().checkOut(pageId);
        getAssetClient().forceDelete(assetId);

        // add a page to the asset folder
        pageNew = createPage(baseFolderPath + "/folder", templateId);
        pageId = getPageRestClient().save(pageNew).getId();
        pageCleaner.add(pageId);

        // add a new asset to the page and folder
        assetId = createAsset("MyAsset2", baseFolderPath + "/folder").getId();
        assetCleaner.add(assetId);
        awRel = new PSAssetWidgetRelationship(pageId, 5, "widget5", assetId, 1);
        getAssetClient().createAssetWidgetRelationship(awRel);
        getAssetClient().addAssetToFolder(folderItem.getFolderPath(), assetId);

        // transition the page to pending
        getItemWorkflowClient().transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE);

        result = restClient.validateFolderDelete(folderPath);

        // not in use
        assertEquals(PSPathItemService.VALIDATE_SUCCESS, result);

        // switch to unauthorized user
        restClient.login("author1", "demo");

        result = restClient.validateFolderDelete(folderPath);

        // should not be authorized
        assertEquals(PSAssetPathItemService.ASSETS_NOT_AUTHORIZED, result);

        // add asset to template
        awRel = new PSAssetWidgetRelationship(templateId, 5, "widget5", assetId, 1);
        getAssetClient().createAssetWidgetRelationship(awRel);
        result = restClient.validateFolderDelete(folderPath);

        // not authorized and in use by template
        assertTrue(result.contains(PSAssetPathItemService.ASSETS_NOT_AUTHORIZED));
        assertTrue(result.contains(PSAssetPathItemService.ASSETS_IN_USE_TEMPLATES));

        // clear the asset from the template
        getAssetClient().clearAssetWidgetRelationship(awRel);
    }

    private PSSite createSite()
    {
        String siteName = TEST_SITE + System.currentTimeMillis();
        siteCleaner.add(siteName);
        PSSite site = new PSSite();
        site.setName(siteName);
        site.setLabel("My test site");
        site.setHomePageTitle("homePageTitle");
        site.setNavigationTitle("navigationTitle");
        site.setBaseTemplateName(IPSSitemanageConstants.PLAIN_BASE_TEMPLATE_NAME);
        site.setTemplateName("templateName");
        return siteRestClient.save(site);
    }

    private PSPage createPage(String folderPath, String templateId)
    {
        PSPage pageNew = new PSPage();
        // the .xml was added to test for bug cml-2262
        pageNew.setName(String.valueOf(System.currentTimeMillis()) + ".xml");
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

        return pageNew;
    }
    
    private PSPage createNumberedPage(String folderPath, int pageNumber, String templateId)
    {
        PSPage pageNew = new PSPage();
        // the .xml was added to test for bug cml-2262
        pageNew.setName("page" + decimalFormat.format(pageNumber) + ".xml");
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

        return pageNew;
    }
    
    private PSPage createAlphaNamedPage(String folderPath, char prefix, String templateId)
    {
        PSPage pageNew = new PSPage();
        // the .xml was added to test for bug cml-2262
        pageNew.setName(prefix + "-page.xml");
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

        return pageNew;
    }
    
    private PSAsset createAsset(String name, String folderPath) throws Exception
    {
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", name);
        asset.setType("percRawHtmlAsset");
        asset.getFields().put("html", "TestHTML");
        if (folderPath != null)
        {
            asset.setFolderPaths(asList(folderPath));
        }
        return getAssetClient().save(asset);
    }

    @SuppressWarnings("unchecked")
    private Map<PSAsset, List> createTestFolder(String path, String baseFolderPath, String templateId, String rootPageId)
        throws Exception
    {
        Map<PSAsset, List> itemsMap = new HashMap<PSAsset, List>();
        List itemsToPurge = new ArrayList();

        boolean purgeAssets = path.startsWith(ASSET_ROOT);

        PSPathItem folderItem = restClient.addFolder(path);

        // add sub-folder
        PSPathItem subFolderItem = restClient.addFolder(path + "/subFolder");

        // add asset to sub-folder
        PSAsset asset1 = createAsset("MyAsset", subFolderItem.getFolderPath());
        String subFolderAssetId = asset1.getId();
        assetCleaner.add(subFolderAssetId);

        // add two assets to folder
        PSAsset asset2 = createAsset("MyAsset1", folderItem.getFolderPath());
        String folderAssetId1 = asset2.getId();
        assetCleaner.add(folderAssetId1);
        PSAsset inUseAsset = createAsset("MyAsset2", folderItem.getFolderPath());
        String folderAssetId2 = inUseAsset.getId();
        assetCleaner.add(folderAssetId2);

        // add a page to the folder
        PSPage pageNew = createPage(folderItem.getFolderPath(), templateId);
        pageNew = getPageRestClient().save(pageNew);
        String pageId = pageNew.getId();
        pageCleaner.add(pageId);
        itemsToPurge.add(pageNew);

        // add asset to page (should be deleted)
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(pageId, 5, "widget5", folderAssetId1, 1);
        awRel.setResourceType(PSAssetResourceType.shared);
        
        getItemWorkflowClient().checkOut(pageId);
        
        getAssetClient().createAssetWidgetRelationship(awRel);

        // add asset to root folder page (should be skipped)
        getItemWorkflowClient().checkOut(rootPageId);        
        awRel = new PSAssetWidgetRelationship(rootPageId, 5, "widget5", folderAssetId2, 1);
        getAssetClient().createAssetWidgetRelationship(awRel);

        if (purgeAssets)
        {
            itemsToPurge.add(asset1);
            itemsToPurge.add(asset2);
        }

        itemsMap.put(inUseAsset, itemsToPurge);

        return itemsMap;
    }

    @SuppressWarnings("unchecked")
    private void checkPurgedItems(List purgedItems) throws Exception
    {
        for (Object purgedItem : purgedItems)
        {
            PSDataServiceRestClient rc = null;
            String id = null;
            if (purgedItem instanceof PSPage)
            {
                PSPage page = (PSPage) purgedItem;
                rc = getPageRestClient();
                id = page.getId();
            }
            else if (purgedItem instanceof PSAsset)
            {
                PSAsset asset = (PSAsset) purgedItem;
                rc = getAssetClient();
                id = asset.getId();
            }
            else
            {
                throw new Exception("Unrecognized item found" + purgedItem.toString());
            }

            assertItemPurged(rc, id);
        }
    }

    private void assertItemPurged(PSDataServiceRestClient< ? > rc, String id)
    {
        try
        {
            rc.get(id);
            fail("Failed to delete item " + id);
        }
        catch (RestClientException e)
        {
            // expected
        }
    }

    private static void tearDown(String path)
    {
        try
        {
            PSDeleteFolderCriteria criteria = new PSDeleteFolderCriteria();
            criteria.setPath(path + TEST_FOLDER);
            criteria.setSkipItems(SkipItemsType.NO);
            String deleteFolderResult = restClient.deleteFolder(criteria);
            //skipping until fix. CMS-1805
            if (!deleteFolderResult.equals("0"))
                System.out.println("FIXME- PSPathServiceTest.tearDown returned - "+deleteFolderResult+ "instead of 0");
            //assertTrue("Error cleaning up folder "+criteria.getPath()+"  Expected return 0 got, "+deleteFolderResult,deleteFolderResult.equals("0"));
            
        }
        catch (Exception e)
        {
            // may not exist if already cleaned up
        }
    }

    private static PSPageRestClient getPageRestClient() throws Exception
    {
        PSPageRestClient client = new PSPageRestClient(baseUrl);
        setupClient(client);
        return client;
    }

    private static PSTemplateServiceClient getTemplateClient() throws Exception
    {
        PSTemplateServiceClient client = new PSTemplateServiceClient(baseUrl);
        setupClient(client);
        return client;
    }

    private static PSAssetServiceRestClient getAssetClient() throws Exception
    {
        PSAssetServiceRestClient client = new PSAssetServiceRestClient(baseUrl);
        setupClient(client);
        return client;
    }

    private static PSItemWorkflowServiceRestClient getItemWorkflowClient() throws Exception
    {
        PSItemWorkflowServiceRestClient client = new PSItemWorkflowServiceRestClient(baseUrl);
        setupClient(client);
        return client;
    }

    static PSTestDataCleaner<String> pageCleaner = new PSTestDataCleaner<String>()
    {
        @Override
        protected void clean(String id) throws Exception
        {
            getPageRestClient().delete(id);
        }
    };

    static PSTestDataCleaner<String> templateCleaner = new PSTestDataCleaner<String>()
    {
        @Override
        protected void clean(String id) throws Exception
        {
            getTemplateClient().deleteTemplate(id);
        }
    };

    static PSTestDataCleaner<String> assetCleaner = new PSTestDataCleaner<String>()
    {
        @Override
        protected void clean(String id) throws Exception
        {
            getAssetClient().delete(id);
        }
    };

    private static final String TEST_FOLDER = "test";

    private static final String ASSET_TEST_FOLDER = PSAssetPathItemService.ASSET_ROOT + "/" + TEST_FOLDER;
    
    private static final String TEST_SITE = "MyTestSite";

    private static final String SITE_ROOT = PSPathUtils.SITES_FINDER_ROOT.substring(1) + '/';

    private static final String ASSET_ROOT = PSPathUtils.ASSETS_FINDER_ROOT.substring(1) + '/';

}
