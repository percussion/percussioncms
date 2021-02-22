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

package com.percussion.pagemanagement.service.impl;

import com.percussion.share.data.PSPagedObjectList;
import com.percussion.share.data.PSUnassignedResults;
import com.percussion.share.data.PSUnassignedResults.ImportStatus;
import com.percussion.share.data.PSUnassignedResults.ItemStatus;
import com.percussion.share.data.PSUnassignedResults.UnassignedItem;
import com.percussion.share.data.PSUnassignedResults.UnassignedItemList;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Mock data factory for unassigned pages.
 * 
 * @author YuBingChen
 */
public class PSMockDataForUnassignedPages
{
    public final static int MAX_COUNT = 32;
    public  final static int INC_COUNT = 4;
    public  final static int TRY_COUNT = 3;
    
    private List<PSPair<Integer, ItemStatus>> itemList;
    private int importedCount;
    private int totalCount;
    private int tryCount;
    private static Log log = LogFactory.getLog(PSMockDataForUnassignedPages.class);
            
    public PSMockDataForUnassignedPages()
    {
        init();
        itemList = createItemList();
    }
    
    public PSUnassignedResults getUnassignedResults(int startIndex, int maxResults)
    {
        log.info("startIndex: " + startIndex + ", maxResults: " + maxResults);
        
        PSUnassignedResults results = new PSUnassignedResults();
        
        int catalogedPage = totalCount - importedCount;
        ImportStatus status = new ImportStatus(catalogedPage, importedCount);
        results.setImportStatus(status);
        results.setUnassignedItemList(getUnassignedItemList(startIndex, maxResults));
        
        log.info("Results: " + results.toString());
        
        return results;
    }
            
    /**
     * Updates the unassigned pages, to simulate more pages have been imported and/or cataloged.
     * The cataloged pages will be increased by {@link #INC_COUNT} until {@link #MAX_COUNT}.
     */
    public void update()
    {
        if (importedCount == totalCount)
        {
            tryCount++;
            if (tryCount == TRY_COUNT)
            {
                init();
            }
        }
        else
        {
        
            totalCount = (totalCount >= MAX_COUNT) ? totalCount : totalCount + INC_COUNT;
            importedCount = (importedCount >= totalCount) ? importedCount : importedCount + 1;
        }
        
        itemList = createItemList();
    }
    
    public void importCompleted()
    {
        totalCount = MAX_COUNT;
        importedCount = totalCount;
        tryCount = 0;

        itemList = createItemList();
    }
    
    private UnassignedItemList getUnassignedItemList(int startIndex, int maxResults)
    {
        PSPagedObjectList<PSPair<Integer, ItemStatus>> pageGroup = null;
        pageGroup = PSPagedObjectList.getPage(itemList, startIndex, maxResults);
        
        List<UnassignedItem> items = new ArrayList<>();
        for (PSPair<Integer, ItemStatus> p : pageGroup.getChildrenInPage())
        {
            UnassignedItem item = getUnassignedItem(p.getFirst(), p.getSecond());
            items.add(item);
        }
        
        UnassignedItemList result = new UnassignedItemList();
        result.setChildrenCount(pageGroup.getChildrenCount());
        result.setStartIndex(pageGroup.getStartIndex());
        result.setChildrenInPage(items);
        
        return result;
    }
    
    private UnassignedItem getUnassignedItem(Integer id, ItemStatus status)
    {
        return new UnassignedItem("166775-101-1" + id, "page" + id + ".html", "/mock-site/page" + id + ".html", status);
    }
    
    private void init()
    {
        totalCount = INC_COUNT;
        importedCount = 0;
        tryCount = 0;
    }
    

    
    private List<PSPair<Integer, ItemStatus>> createItemList()
    {
        List<PSPair<Integer, ItemStatus>> result = new ArrayList<>();
        
        for (int i = 0; i < totalCount; i++)
        {
            ItemStatus status = ItemStatus.Cataloged;
            if (importedCount > 0)
            {
                if (i < importedCount)
                    status = ItemStatus.Imported;
                else if (i == importedCount)
                    status = ItemStatus.Importing;
            }
            PSPair<Integer, ItemStatus> item = new PSPair<>(i, status);
            result.add(item);
        }
        
        return result;
    }
}
