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
package com.percussion.monitor.process;

import com.percussion.search.PSSearchEditorChangeEvent;
import com.percussion.search.PSSearchIndexEventQueue;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author JaySeletz
 * 
 */
@Category(IntegrationTest.class)
public class PSSearchIndexProcessMonitorTest extends ServletTestCase
{

    @Test
    public void test() throws Exception
    {
        PSSearchIndexEventQueue eventQueue = PSSearchIndexEventQueue.getInstance();

        try
        {
            assertEquals("Running", eventQueue.getStatus());
            assertEquals(eventQueue.getStatus(), PSSearchIndexProcessMonitor.getStatus());

            eventQueue.pause();
            try {
                assertEquals("Paused", eventQueue.getStatus());
                int queueCount = eventQueue.size(); // may be > 0 from other tests?
                Thread.sleep(6000);
                assertEquals(queueCount, PSSearchIndexProcessMonitor.getCount());
    
                eventQueue.queueEvent(new PSSearchEditorChangeEvent(PSSearchEditorChangeEvent.ACTION_DELETE, 999999, 1,
                        310, true));
                queueCount++;
                assertEquals(queueCount, eventQueue.size());
                Thread.sleep(3000);
                assertEquals(queueCount, eventQueue.size());
                Thread.sleep(3000);
                assertEquals(queueCount, PSSearchIndexProcessMonitor.getCount());
                
                eventQueue.clearQueues();
                assertEquals(0, eventQueue.size());
            //  Process monitor updates every 2s
                Thread.sleep(6000);
                assertEquals(0, PSSearchIndexProcessMonitor.getCount());
            } finally {
                eventQueue.resume();
            }
            assertEquals("Running", eventQueue.getStatus());
            //  Process monitor updates every 5s
            Thread.sleep(6000);
            assertEquals(eventQueue.getStatus(), PSSearchIndexProcessMonitor.getStatus());
        }
        finally
        {
            eventQueue.resume();
        }

    }

}
