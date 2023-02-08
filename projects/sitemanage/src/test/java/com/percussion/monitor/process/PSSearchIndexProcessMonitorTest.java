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
