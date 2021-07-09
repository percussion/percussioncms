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

package com.percussion.searchmanagement;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.web.service.PSAssetServiceRestClient;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.pathmanagement.web.service.PSPathServiceRestClient;
import com.percussion.searchmanagement.data.PSSearchCriteria;
import com.percussion.share.test.PSObjectRestClient.DataRestClientException;
import com.percussion.share.test.PSRestClient.RestClientException;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.share.test.PSTestDataCleaner;
import com.percussion.ui.service.IPSListViewHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore public class PSSearchServiceTest extends PSRestTestCase<PSSearchServiceRestClient>
{
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSSearchServiceTest.class);
    
    private static final String ASSET_ROOT = PSPathUtils.ASSETS_FINDER_ROOT.substring(1) + '/';
    
    private static final DecimalFormat decimalFormat = new DecimalFormat("000");
    
    private static PSSearchServiceRestClient searchServiceClient;
    
    private static PSPathServiceRestClient pathServiceRestClient;
    
    private static List<String> assetNamesCreationOrder;
    
    private PSSearchCriteria searchCriteria;

    static PSTestDataCleaner<String> assetCleaner = new PSTestDataCleaner<String>()
    {
        @Override
        protected void clean(String id) throws Exception
        {
            getAssetClient().delete(id);
        }
    };
    
    static PSTestDataCleaner<String> folderCleaner = new PSTestDataCleaner<String>()
    {
        @Override
        protected void clean(String id) throws Exception
        {
            PSDeleteFolderCriteria criteria = new PSDeleteFolderCriteria();
            criteria.setPath(id);
            pathServiceRestClient.deleteFolder(criteria);
        }
    };
    
    @Override
    protected PSSearchServiceRestClient getRestClient(String baseUrl)
    {
        return searchServiceClient;
    }

    @BeforeClass
    public static void setupSuite() throws Exception
    {
        pathServiceRestClient = new PSPathServiceRestClient(baseUrl);
        setupClient(pathServiceRestClient);
        
        searchServiceClient = new PSSearchServiceRestClient(baseUrl);
        setupClient(searchServiceClient);
        
        String folderName = "folder" + System.currentTimeMillis();
        PSPathItem folderItem = pathServiceRestClient.addFolder(ASSET_ROOT + folderName);
        folderCleaner.add(folderItem.getPath());
        
        log.info("Creating assets");
        assetNamesCreationOrder = createNumberedAssets(folderItem, 10, true, 2);
        
        // Wait the server to finish up indexing stuff
        while (!testDataSuccessfullyAdded())
        {
            log.info("Assets not indexed yet");
            Thread.sleep(3000);
        }
    }
    
    private static boolean testDataSuccessfullyAdded()
    {
        PSSearchCriteria searchCriteria = new PSSearchCriteria();
        searchCriteria.setQuery("sys_title:asset0??");
        searchCriteria.setFormatId(9);
        searchCriteria.setStartIndex(1);
        searchCriteria.setMaxResults(null);
        searchCriteria.setSortColumn(IPSListViewHelper.CONTENT_CREATEDDATE_NAME);
        searchCriteria.setSortOrder("asc");
        
        PSPagedItemList pathItems = searchServiceClient.search(searchCriteria);
        
        if (pathItems == null || pathItems.getChildrenInPage() == null ||
                pathItems.getChildrenInPage().size() < 10 || pathItems.getChildrenCount() < 10)
            return false;
        
        PSPathItem pathItem;
        for (int i=0; i<pathItems.getChildrenInPage().size(); i++)
        {
            if (i >= assetNamesCreationOrder.size())
                return false;
            
            pathItem = pathItems.getChildrenInPage().get(i);
            
            if (!StringUtils.equals(assetNamesCreationOrder.get(i), pathItem.getName()))
                return false;
        }
        
        return true;
    }

    @Before
    public void setup()
    {
        searchCriteria = new PSSearchCriteria();
        searchCriteria.setQuery("sys_title:asset???");
        searchCriteria.setFormatId(9);
        searchCriteria.setStartIndex(1);
        searchCriteria.setMaxResults(10);
    }
    
    @AfterClass
    public static void tearDown() throws Exception
    {
        assetCleaner.clean();
        folderCleaner.clean();
    }

    private static PSAssetServiceRestClient getAssetClient() throws Exception
    {
        PSAssetServiceRestClient client = new PSAssetServiceRestClient(baseUrl);
        setupClient(client);
        return client;
    }
    
    private static List<String> createNumberedAssets(PSPathItem folderItem, int count, boolean shuffle, int secondsBetweenSaves) throws Exception
    {
        List<PSAsset> assets = new ArrayList<PSAsset>();
        List<String> assetNamesCreationOrder = new ArrayList<String>();
        
        for (int i=1; i<=count; i++)
        {
            assets.add(createNumberedAsset(folderItem, i));
        }
        
        if (shuffle)
            Collections.shuffle(assets);
        
        for (PSAsset asset : assets)
        {
            PSAsset savedAsset;
            try
            {
                savedAsset = getAssetClient().save(asset);
                assetNamesCreationOrder.add(savedAsset.getName());
            }
            catch (Exception e)
            {
                continue;
            }
            
            assetCleaner.add(savedAsset.getId());
            
            if (secondsBetweenSaves > 0)
                Thread.sleep(1000 * secondsBetweenSaves);
        }
        
        return assetNamesCreationOrder;
    }
    
    private static PSAsset createNumberedAsset(PSPathItem folderPath, int assetNumber) throws Exception
    {
        PSAsset asset = new PSAsset();
        asset.getFields().put("sys_title", "asset" + decimalFormat.format(assetNumber));
        asset.setType("percRawHtmlAsset");
        asset.getFields().put("html", "TestHTML");
        if (folderPath != null)
        {
            asset.setFolderPaths(asList(folderPath.getFolderPath()));
        }
        return asset;
    }
    
    private static void validateExtendedPathItem(PSPagedItemList pathItems, int itemsCountInPage,
            Integer totalItemsCount, Integer startIndex)
    {
        assertNotNull("pathItems not null", pathItems);
        assertNotNull("pathItems items not null", pathItems.getChildrenInPage());
        assertNotNull("pathItems items count not null", pathItems.getChildrenCount());
        
        assertEquals("pathItems items count in page", itemsCountInPage, pathItems.getChildrenInPage().size());
        assertEquals("pathItems children count", totalItemsCount, pathItems.getChildrenCount());
        assertEquals("pathItems start index", startIndex, pathItems.getStartIndex());
    }
    
    private static void validateAssets(PSPagedItemList pathItems, Integer expectedStartIndex)
    {
        PSPathItem pathItem;
        for (int i=0; i<pathItems.getChildrenInPage().size(); i++)
        {
            pathItem = pathItems.getChildrenInPage().get(i);
            
            assertEquals("asset name " + i, assetNamesCreationOrder.get(expectedStartIndex - 1 + i), pathItem.getName());
        }
    }
    
    private void validateAssetIndexes(PSPagedItemList pathItems, Integer... expectedStartIndexes)
    {
        PSPathItem pathItem;
        
        for (int i=0; i<pathItems.getChildrenInPage().size(); i++)
        {
            pathItem = pathItems.getChildrenInPage().get(i);
            
            assertEquals("asset name " + i, "asset" + decimalFormat.format(expectedStartIndexes[i]), pathItem.getName());
        }
    }
    
    private void validatePathItemIndexes(PSPagedItemList pathItems, int startIndex, String... expectedPathItemNames)
    {
        PSPathItem pathItem;
        
        for (int i=0; i<pathItems.getChildrenInPage().size(); i++)
        {
            pathItem = pathItems.getChildrenInPage().get(i);
            
            assertEquals("path item name " + i, expectedPathItemNames[startIndex -1 + i], pathItem.getName());
        }
    }

    @Test
    public void testNotNullSearch() throws Exception
    {
        // create asset html item
        PSAsset assetHtml = new PSAsset();
        String assetTitle = "testAssetHtmlSearch" + System.currentTimeMillis();
        assetHtml.getFields().put("sys_title", assetTitle);
        assetHtml.setType("percRawHtmlAsset");
        assetHtml.getFields().put("html", "TestHTML");
        assetHtml.setFolderPaths(asList("//Folders"));
        assetHtml = getAssetClient().save(assetHtml);

        // delete the created asset
        assetCleaner.add(assetHtml.getId());

        // call the service
        PSSearchCriteria searchCriteria = new PSSearchCriteria();
        searchCriteria.setQuery(assetTitle);
        searchCriteria.setStartIndex(1);
        searchCriteria.setFormatId(9);
        PSPagedItemList extendedPathItems = searchServiceClient.search(searchCriteria);
        assertNotNull(extendedPathItems);
        List<PSPathItem> queryResults = extendedPathItems.getChildrenInPage();
        assertNotNull(queryResults);
        log.info("FTS result: " + queryResults.toString());

    }

    @Test
    public void testNoResultFound() throws Exception
    {
        PSSearchCriteria searchCriteria = new PSSearchCriteria();
        searchCriteria.setQuery("sys_contentcreatedby:administrator");
        searchCriteria.setFormatId(9);
        searchCriteria.setStartIndex(1);
        List<PSPathItem> queryResults = searchServiceClient.search(searchCriteria).getChildrenInPage();
        assertTrue(queryResults.size() == 0);
    }

    @Test
    public void testInvalidQuery() throws Exception
    {
        try
        {
            PSSearchCriteria searchCriteria = new PSSearchCriteria();
            searchCriteria.setQuery("*{}");
            searchCriteria.setFormatId(9);
            searchServiceClient.search(searchCriteria);
            
            fail("Should have thrown an exception");
        }
        catch (RestClientException e)
        {
            log.info("Exception thrown because of an invalid Lucene query " + e);
        }
    }
    
    @Test
    public void testPaging_StartIndex_IsInvalid() throws Exception
    {
        // startIndex < 1
        searchCriteria.setStartIndex(-1);
        searchCriteria.setMaxResults(5);
        
        try
        {
            searchServiceClient.search(searchCriteria);
            fail("Should have thrown an exception");
        }
        catch (DataRestClientException e)
        {
            assertEquals("error code", 500, e.getStatus());
            // FIXME the response body should have the exception below
//            assertTrue("error details", e.getResponseBody().contains("java.lang.IllegalArgumentException"));
        }
        
        // startIndex == 0
        searchCriteria.setStartIndex(0);
        
        try
        {
            searchServiceClient.search(searchCriteria);
            fail("Should have thrown an exception");
        }
        catch (DataRestClientException e)
        {
            assertEquals("error code", 500, e.getStatus());
            // FIXME the response body should have the exception below
//            assertTrue("error details", e.getResponseBody().contains("java.lang.IllegalArgumentException"));
        }
    }
    
    @Test
    public void testPaging_MaxResults_IsInvalid() throws Exception
    {
        searchCriteria.setStartIndex(1);
        searchCriteria.setMaxResults(-1);
        
        try
        {
            searchServiceClient.search(searchCriteria);
            fail("Should have thrown an exception");
        }
        catch (DataRestClientException e)
        {
            assertEquals("error code", 500, e.getStatus());
            // FIXME the response body should have the exception below
//            assertTrue("error details", e.getResponseBody().contains("java.lang.IllegalArgumentException"));
        }
    }
    
    @Test
    public void testPaging_MaxResults_NotSpecified() throws Exception
    {
        searchCriteria.setStartIndex(3);
        searchCriteria.setMaxResults(null);
        
        PSPagedItemList pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 8, 10, 3);
        validateAssets(pathItems, 3);
    }
    
    @Test
    public void testPaging_StartIndex_GreaterThanItemsCount() throws Exception
    {
        int expectedStartIndex = 6;
        
        searchCriteria.setStartIndex(11);
        searchCriteria.setMaxResults(5);
        
        PSPagedItemList pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 5, 10, expectedStartIndex);
        validateAssets(pathItems, expectedStartIndex);
    }
    
    @Test
    public void testPaging_StartIndex_GetLastItem() throws Exception
    {
        int expectedStartIndex = 10;
        
        searchCriteria.setStartIndex(10);
        searchCriteria.setMaxResults(1);
        
        PSPagedItemList pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 1, 10, expectedStartIndex);
        validateAssets(pathItems, expectedStartIndex);
    }
    
    @Test
    public void testPaging_StartIndex_GetFromBeginning() throws Exception
    {
        int expectedStartIndex = 1;
        
        searchCriteria.setStartIndex(1);
        searchCriteria.setMaxResults(10);
        
        PSPagedItemList pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 10, 10, expectedStartIndex);
        validateAssets(pathItems, expectedStartIndex);
    }
    
    @Test
    public void testPaging_MaxResults_EqualsToOne() throws Exception
    {
        searchCriteria.setStartIndex(1);
        searchCriteria.setMaxResults(1);
        PSPagedItemList pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 1, 10, 1);
        validateAssets(pathItems, 1);
        
        searchCriteria.setStartIndex(2);
        searchCriteria.setMaxResults(1);
        pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 1, 10, 2);
        validateAssets(pathItems, 2);
        
        searchCriteria.setStartIndex(3);
        searchCriteria.setMaxResults(1);
        pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 1, 10, 3);
        validateAssets(pathItems, 3);
        
        searchCriteria.setStartIndex(8);
        searchCriteria.setMaxResults(1);
        pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 1, 10, 8);
        validateAssets(pathItems, 8);
        
        searchCriteria.setStartIndex(9);
        searchCriteria.setMaxResults(1);
        pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 1, 10, 9);
        validateAssets(pathItems, 9);
        
        searchCriteria.setStartIndex(10);
        searchCriteria.setMaxResults(1);
        pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 1, 10, 10);
        validateAssets(pathItems, 10);
    }
    
    @Test
    public void testPaging_MaxResults_GreaterThanItemsCount() throws Exception
    {
        searchCriteria.setStartIndex(3);
        searchCriteria.setMaxResults(10);
        
        PSPagedItemList pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 8, 10, 3);
        validateAssets(pathItems, 3);
    }
    
    @Test
    public void testSorting_SortColumn_IsNull() throws Exception
    {
        searchCriteria.setStartIndex(3);
        searchCriteria.setMaxResults(10);
        searchCriteria.setSortColumn(null);
        searchCriteria.setSortOrder("asc");
        
        PSPagedItemList pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 8, 10, 3);
        validateAssets(pathItems, 3);
    }
    
    @Test
    public void testSorting_SortOrder_IsNull() throws Exception
    {
        searchCriteria.setStartIndex(3);
        searchCriteria.setMaxResults(10);
        searchCriteria.setSortColumn(IPSListViewHelper.TITLE_NAME);
        searchCriteria.setSortOrder(null);
        
        PSPagedItemList pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 8, 10, 3);
        validateAssets(pathItems, 3);
    }
    
    @Test
    public void testSorting_SortColumn_And_SortOrder_AreBoth_Null() throws Exception
    {
        searchCriteria.setStartIndex(3);
        searchCriteria.setMaxResults(10);
        searchCriteria.setSortColumn(null);
        searchCriteria.setSortOrder(null);
        
        PSPagedItemList pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 8, 10, 3);
        validateAssets(pathItems, 3);
    }
    
    @Test
    public void testSorting_SortBy_Name() throws Exception
    {
        searchCriteria.setStartIndex(1);
        searchCriteria.setMaxResults(null);
        searchCriteria.setSortColumn(IPSListViewHelper.TITLE_NAME);
        searchCriteria.setSortOrder("asc");
        
        PSPagedItemList pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 10, 10, 1);
        validateAssetIndexes(pathItems, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        searchCriteria.setStartIndex(1);
        searchCriteria.setMaxResults(null);
        searchCriteria.setSortColumn(IPSListViewHelper.TITLE_NAME);
        searchCriteria.setSortOrder("desc");
        
        pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 10, 10, 1);
        validateAssetIndexes(pathItems, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
    }
    
    @Test
    public void testSorting_SortBy_CreatedDate() throws Exception
    {
        searchCriteria.setStartIndex(1);
        searchCriteria.setMaxResults(null);
        searchCriteria.setSortColumn(IPSListViewHelper.CONTENT_CREATEDDATE_NAME);
        searchCriteria.setSortOrder("asc");
        
        PSPagedItemList pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 10, 10, 1);
        validatePathItemIndexes(pathItems, 1, assetNamesCreationOrder.toArray(new String[0]));
        
        searchCriteria.setStartIndex(1);
        searchCriteria.setMaxResults(null);
        searchCriteria.setSortColumn(IPSListViewHelper.CONTENT_CREATEDDATE_NAME);
        searchCriteria.setSortOrder("desc");
        
        List<String> reversedAssetNamesCreationOrder = new ArrayList<String>(assetNamesCreationOrder);
        Collections.reverse(reversedAssetNamesCreationOrder);
        
        pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 10, 10, 1);
        validatePathItemIndexes(pathItems, 1, reversedAssetNamesCreationOrder.toArray(new String[0]));
    }
    
    @Test
    public void testSorting_Plus_Paging() throws Exception
    {
        searchCriteria.setStartIndex(3);
        searchCriteria.setMaxResults(10);
        searchCriteria.setSortColumn(IPSListViewHelper.CONTENT_CREATEDDATE_NAME);
        searchCriteria.setSortOrder("asc");
        
        PSPagedItemList pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 8, 10, 3);
        validatePathItemIndexes(pathItems, 3, assetNamesCreationOrder.toArray(new String[0]));
        
        searchCriteria.setStartIndex(3);
        searchCriteria.setMaxResults(10);
        searchCriteria.setSortColumn(IPSListViewHelper.CONTENT_CREATEDDATE_NAME);
        searchCriteria.setSortOrder("desc");
        
        List<String> reversedAssetNamesCreationOrder = new ArrayList<String>(assetNamesCreationOrder);
        Collections.reverse(reversedAssetNamesCreationOrder);
        
        pathItems = searchServiceClient.search(searchCriteria);
        validateExtendedPathItem(pathItems, 8, 10, 3);
        validatePathItemIndexes(pathItems, 3, reversedAssetNamesCreationOrder.toArray(new String[0]));
    }
}
