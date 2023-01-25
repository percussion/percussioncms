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

import com.percussion.monitor.service.PSMonitor;
import com.percussion.monitor.service.PSMonitorService;

import java.util.List;
import java.util.Set;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSProcessMonitorServerTest extends ServletTestCase
{
    public void test() throws Exception
    {
        List<PSMonitor> monList = PSMonitorService.getMonitors().monitor;
        
        Set<String> designators = PSMonitorService.getMonitorDesignators().designator;
        
        String[] expected = new String[] {"Import", "Publishing", "SearchIndex", "SiteCopy", "Thumbnail", "WorkflowAssignment"};
        assertEquals(expected.length, monList.size());
        
        for (String designator : expected)
        {
            assertTrue(designators.contains(designator));
            assertTrue(PSMonitorService.getMonitor(designator) != null);
        }
    }
}
