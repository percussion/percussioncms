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

package com.percussion.queue.impl;


import static org.junit.Assert.*;

import com.percussion.monitor.process.PSImportProcessMonitor;
import com.percussion.sitemanage.data.PSSite;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class PSSiteQueueTest
{

    final static String USER_AGENT = "FAKE AGENT";
    private static PSImportProcessMonitor monitor = new PSImportProcessMonitor();
    
    @Test
    public void testInit()
    {
        PSSite s = new PSSite();
        PSSiteQueue sq = new PSSiteQueue(s, USER_AGENT);
        
        assertTrue(sq.getImportingIds().size() == 0);
        assertTrue(sq.getCatalogedIds().isEmpty());
        assertTrue(sq.getImportedIds().isEmpty());
        assertEquals(0, monitor.getCatalogCount());
    }
    
    @Test
    public void testAll_ButMaxCount()
    {
        PSSiteQueue sq = createSiteQueue();

        // check cataloged page IDs
        assertTrue(sq.getCatalogedIds().size() == 2);
        assertTrue(sq.getCatalogedIds().get(0) == 10);
        assertTrue(sq.getCatalogedIds().get(1) == 20);
        assertTrue(sq.getImportingIds().size() == 0);
        assertEquals(2, monitor.getCatalogCount());

        
        // check the effect of getNextId()
        sq.setMaxImportCount(-1);
        Integer importingId = sq.getNextId();
        
        assertTrue(importingId.intValue() == 10);
        assertTrue(sq.getCatalogedIds().size() == 1);
        assertTrue(sq.getCatalogedIds().get(0) == 20);
        assertEquals(1, monitor.getCatalogCount());

        importingId = sq.getNextId();
        assertTrue(importingId.intValue() == 20);
        assertTrue(sq.getCatalogedIds().size() == 0);
        assertEquals(0, monitor.getCatalogCount());
        
        importingId = sq.getNextId();
        assertTrue(importingId == null);
        
        
        assertTrue(sq.getImportingIds().size() == 2);
        
        
    }

    @Test
    public void testRemoveImportingId()
    {
        PSSiteQueue sq = createSiteQueue();
        assertTrue(sq.getCatalogedIds().size() == 2);
       

        sq.setMaxImportCount(-1);
        assertTrue(sq.getImportingIds().size() == 0);
        
        Integer i = sq.getNextId();
        assertNotNull(i);
        
        assertTrue(sq.getCatalogedIds().size() == 1);
       
        
        sq.removeImportingId(i);
      
        assertNotNull(sq.getNextId());
        assertTrue(sq.getImportingIds().size() > 0);
        
        assertTrue(sq.getCatalogedIds().size() == 0);
  
        
        assertNull(sq.getNextId());
        assertTrue(sq.getCatalogedIds().size() == 0);
       
    }
    
    @Test
    public void testRemoveImportedPageId()
    {
        PSSiteQueue sq = createSiteQueue(2, 10, 20);
        assertTrue(sq.getImportedIds().size() == 10);

        List<Integer> ids = sq.getImportedIds();
        assertTrue(ids.get(0).intValue() == 1);
        assertTrue(ids.get(5).intValue() == 6);
        
        sq.removeImportedId(6);
        assertTrue(sq.getImportedIds().size() == 9);
    }
    
    @Test
    public void testMaxImportCountNoLimit()
    {
        PSSiteQueue sq = createSiteQueue();
        assertTrue(sq.getCatalogedIds().size() == 2);
       
        
        sq.setMaxImportCount(-1);
        while (sq.getNextId() != null);
        
        assertTrue(sq.getCatalogedIds().size() == 0);
       
    }
    
    @Test
    public void testMaxImportCount()
    {
        PSSiteQueue sq = createSiteQueue();
        
        // max == 0
        assertTrue(sq.getMaxImportCount() == 0);
        assertTrue(sq.getCatalogedIds().size() == 2);
        assertEquals(2, monitor.getCatalogCount());
        
        assertTrue(sq.getNextId() == null);
        assertTrue(sq.getCatalogedIds().size() == 2);
        assertEquals(0, monitor.getCatalogCount());
        
        // max == 1
        sq.setMaxImportCount(1);
        assertNull(sq.getNextId());

        assertTrue(sq.getCatalogedIds().size() == 2);
     

        // max == 2
        sq.setMaxImportCount(2);
        assertNull(sq.getNextId());

        assertTrue(sq.getCatalogedIds().size() == 2);
   

        
        // max == 4
        sq.setMaxImportCount(4);
        assertNotNull(sq.getNextId());
        assertNotNull(sq.getNextId());

        assertTrue(sq.getCatalogedIds().size() == 0);

        // this call to move the importing ID into imported IDs
        assertNull(sq.getNextId());

        assertTrue(sq.getCatalogedIds().size() == 0);
    }
    
    @Test
    public void testContainsPagesForImport()
    {
        PSSiteQueue sq = createSiteQueue(10, 2, 2);
        assertTrue(sq.getImportingIds().size() == 0);
        assertFalse(sq.containsPagesForImport());
        assertEquals(10, monitor.getCatalogCount());
        
        sq = createSiteQueue(10, 2, 4);
        assertTrue(sq.getImportingIds().size() == 0);
        assertTrue(sq.containsPagesForImport());
        assertEquals(10, monitor.getCatalogCount());
        
        Integer importingId = sq.getNextId();
        assertTrue(sq.getImportingIds().size() > 0);
        assertTrue(importingId != null);
        assertTrue(sq.containsPagesForImport());
        assertEquals(9, monitor.getCatalogCount());
        
        importingId = sq.getNextId();
        assertTrue(sq.getImportingIds().size() > 0);
        assertTrue(importingId != null);
        assertEquals(8, monitor.getCatalogCount());

    }
    
    private PSSiteQueue createSiteQueue()
    {
        return createSiteQueue(2, 2, 0);
    }
    
    private PSSiteQueue createSiteQueue(int catalogCount, int importCount, int maxImport)
    {
        PSSite s = new PSSite();
        PSSiteQueue sq = new PSSiteQueue(s, USER_AGENT);
        sq.setMaxImportCount(maxImport);

        List<Integer> catalogedIds = new ArrayList<Integer>();
        for (int i=1; i <= catalogCount; i++)
        {
            catalogedIds.add(i * 10);
        }
        sq.addCatalogedIds(catalogedIds);

        List<Integer> importedIds = new ArrayList<Integer>();
        for (int i=1; i <= importCount; i++)
        {
            importedIds.add(i);
        }
        sq.addImportedIds(importedIds);
        return sq;
    }
}
