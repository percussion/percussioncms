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

package com.percussion.queue.impl;

import com.percussion.queue.IPSPageImportQueue;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.share.service.IPSSystemProperties;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

@Category(IntegrationTest.class)
public class PSPageImportQueueTest extends PSServletTestCase
{
    IPSPageImportQueue m_importQueue;
    private IPSSystemProperties m_systemProps;
    private IPSPerformPageImport m_systemPageImporter;
    
    protected void setUp() throws Exception
    {
        super.setUp();
        m_importQueue = (IPSPageImportQueue) getBean("pageImportQueue");
        m_systemProps = ((PSPageImportQueue) m_importQueue).getSystemProps();

        m_systemPageImporter = ((PSPageImportQueue)m_importQueue).getPageImporter();
        ((PSPageImportQueue)m_importQueue).setPageImporter(new PageImportTester());
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        ((PSPageImportQueue)m_importQueue).setPageImporter(m_systemPageImporter);
        ((PSPageImportQueue) m_importQueue).setSystemProps(m_systemProps);
    }

    private void setMaxImportCount(String max)
    {
        PSMockSystemProps testProps = new PSMockSystemProps();
        testProps.setMax(max);
        ((PSPageImportQueue) m_importQueue).setSystemProps(testProps);
    }
    
    static AtomicInteger idCounter = new AtomicInteger();
    
    static int ms_totalCounter = 0;
    
    private List<Integer> getNextCatalogedPageIds()
    {
        List<Integer> ids = new ArrayList<Integer>();
        for (int i=0; i<3; i++)
        {
            int id = idCounter.getAndIncrement();
            ids.add(id);
        }
        ms_totalCounter += ids.size();
        return ids;
    }
    
    public void testImportQueue() throws Exception
    {
        setMaxImportCount("-1");
        
        final Long SITE_ID = 1000L;
        PSSite s = new PSSite();
        s.setSiteId(SITE_ID);
        s.setName("TestImportQueue");
        List<Integer> idList = getNextCatalogedPageIds();
        
        m_importQueue.addCatalogedPageIds(s, "FakeAgent", idList);

        waitForQueueWakeup(m_importQueue, s);
        
        while (true)
        {
        	for (Integer i : m_importQueue.getImportingPageIds(SITE_ID))
        	{
        		m_importQueue.addImportedId(SITE_ID, i);
        	}
        	List<Integer> ids = m_importQueue.getImportingPageIds(SITE_ID);
            List<Integer> catalogIds = m_importQueue.getCatalogedPageIds(SITE_ID);
            
            if (ids.size() == 0 && catalogIds.isEmpty())
                break;
                
            System.out.println("[TEST] importing pages with ids: " + ids);
            Thread.sleep(500);
        }
        
        List<Integer> importedIds = m_importQueue.getImportedPageIds(SITE_ID);
        
        
        validateSiteDeleteNotification(SITE_ID);
    }

    private void validateSiteDeleteNotification(final Long siteId)
    {
        PSSiteQueue sq = m_importQueue.getPageIds(siteId);
        assertTrue(sq.getImportedIds().size() > 0);
        
        IPSGuid guid = new PSGuid(PSTypeEnum.SITE, siteId);
        PSNotificationEvent event = new PSNotificationEvent(PSNotificationEvent.EventType.SITE_DELETED, guid);
        ((PSPageImportQueue)m_importQueue).notifyEvent(event);
        
        sq = m_importQueue.getPageIds(siteId);
        
        ms_totalCounter = 0;
    }

    private void waitForQueueWakeup(IPSPageImportQueue importQueue, PSSite s) throws InterruptedException
    {
        Thread.sleep(200);
    }
    
    private class PSMockSystemProps extends Properties implements IPSSystemProperties
    {
        public void setMax(String value)
        {
            setProperty(IMPORT_PAGE_MAX, value);
        }
    }
    
    private class PageImportTester implements IPSPerformPageImport
    {
        public void performPageImport(PSSite site, Integer id, String userAgent) throws InterruptedException
        {
            System.out.println("Importing page id: " + id);
            Thread.sleep(1000);
        }
    }
}
