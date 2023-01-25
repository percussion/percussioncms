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

package com.percussion.pathmanagement.service;


import static com.percussion.pagemanagement.service.impl.PSMockDataForUnassignedPages.INC_COUNT;
import static com.percussion.pagemanagement.service.impl.PSMockDataForUnassignedPages.MAX_COUNT;
import static com.percussion.pagemanagement.service.impl.PSMockDataForUnassignedPages.TRY_COUNT;
import static org.junit.Assert.*;

import com.percussion.pagemanagement.service.impl.PSMockDataForUnassignedPages;
import com.percussion.share.data.PSUnassignedResults;
import com.percussion.share.data.PSUnassignedResults.ImportStatus;
import com.percussion.share.data.PSUnassignedResults.UnassignedItemList;

import org.junit.Test;

public class PSMockDataForUnassignedPagesTest
{
    /**
     * Test initial status
     * 
     * @throws Exception
     */
    @Test
    public void testInitial() throws Exception
    {
        PSMockDataForUnassignedPages mockData = new PSMockDataForUnassignedPages();
        
        PSUnassignedResults results = mockData.getUnassignedResults(1, 10);
        ImportStatus status = results.getImportStatus();
        
        assertTrue(status.getCatalogedPageCount() == INC_COUNT);
        assertTrue(status.getImportedPageCount() == 0);
        
        UnassignedItemList itemList = results.getUnassignedItemList();
        assertTrue(itemList.getChildrenCount() == INC_COUNT);
        assertTrue(itemList.getStartIndex() == 1);
        assertTrue(itemList.getChildrenInPage().size() == INC_COUNT);
    }

    @Test
    public void testImportCompleted()
    {
        PSMockDataForUnassignedPages mockData = new PSMockDataForUnassignedPages();
        mockData.importCompleted();
        
        PSUnassignedResults results = mockData.getUnassignedResults(1, 10);
        ImportStatus status = results.getImportStatus();

        assertTrue(status.getCatalogedPageCount() == 0);
        assertTrue(status.getImportedPageCount() == MAX_COUNT);

        UnassignedItemList itemList = results.getUnassignedItemList();
        assertTrue(itemList.getChildrenCount() == MAX_COUNT);
        assertTrue(itemList.getStartIndex() == 1);
        assertTrue(itemList.getChildrenInPage().size() == 10);
    }
        
    /**
     * Test update status
     * 
     * @throws Exception
     */
    @Test
    public void testUpdate() throws Exception
    {
        validateUpdate();
    }
    
    /**
     * Test retry status
     * 
     * @throws Exception
     */
    @Test
    public void testRetrye() throws Exception
    {
        PSMockDataForUnassignedPages mockData = validateUpdate();
        
        PSUnassignedResults results = mockData.getUnassignedResults(1, 10);
        ImportStatus status = results.getImportStatus();
        int total = status.getCatalogedPageCount() + status.getImportedPageCount();

        
        // in retry state
        assertTrue(total == MAX_COUNT);
        
        int tryCount = 0;
        do 
        {
            mockData.update();
            tryCount++;            
            
            if (tryCount == TRY_COUNT)
                break;
            
            results = mockData.getUnassignedResults(1, 10);
            UnassignedItemList itemList = results.getUnassignedItemList();
            assertTrue(itemList.getChildrenCount() == MAX_COUNT);
        }
        while (true);
        
        // back to initial state
        results = mockData.getUnassignedResults(1, 10);
        status = results.getImportStatus();
        
        assertTrue(status.getCatalogedPageCount() == INC_COUNT);
        assertTrue(status.getImportedPageCount() == 0);

    }
    
    private PSMockDataForUnassignedPages validateUpdate()
    {
        PSMockDataForUnassignedPages mockData = new PSMockDataForUnassignedPages();
        
        int catalogedPage = INC_COUNT;
        int importedPage = 0;
        int total;
        
        do
        {
            mockData.update();
            PSUnassignedResults results = mockData.getUnassignedResults(1, 10);
            ImportStatus status = results.getImportStatus();
            total = status.getCatalogedPageCount() + status.getImportedPageCount();
            
            importedPage += 1;
            catalogedPage -= 1; 
            if (total <= MAX_COUNT && (importedPage+catalogedPage) < MAX_COUNT)
                catalogedPage += INC_COUNT;
            
            
            assertTrue(status.getCatalogedPageCount() == catalogedPage);
            assertTrue(status.getImportedPageCount() == importedPage);
            
            UnassignedItemList itemList = results.getUnassignedItemList();
            assertTrue(itemList.getChildrenCount() == total);
            assertTrue(itemList.getStartIndex() == 1);
            
            if (total >= 10)
                assertTrue(itemList.getChildrenInPage().size() == 10);
            else
                assertTrue(itemList.getChildrenInPage().size() == total);
        }
        while (importedPage < MAX_COUNT );

        return mockData;
    }
}
