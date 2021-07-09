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
