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
package com.percussion.monitor.process;

import static org.junit.Assert.*;

import com.percussion.monitor.service.PSMonitorService;

import org.junit.Test;

/**
 * @author JaySeletz
 *
 */
public class PSSiteCopyProcessMonitorTest
{

    @Test
    public void test()
    {
        PSSiteCopyProcessMonitor mon = new PSSiteCopyProcessMonitor();  // cause registration
        validateDesignator(PSSiteCopyProcessMonitor.MONITOR_DESIGNATOR);
        validateMessage(PSSiteCopyProcessMonitor.MONITOR_DESIGNATOR, PSSiteCopyProcessMonitor.IDLE_MSG, true);
        PSSiteCopyProcessMonitor.startSiteCopy("testSite");
        PSSiteCopyProcessMonitor.copyingAssetsFolder();
        validateMessage(PSSiteCopyProcessMonitor.MONITOR_DESIGNATOR, PSSiteCopyProcessMonitor.IDLE_MSG, false);
        PSSiteCopyProcessMonitor.siteCopyCompleted();
        validateMessage(PSSiteCopyProcessMonitor.MONITOR_DESIGNATOR, PSSiteCopyProcessMonitor.IDLE_MSG, true);                
    }

    private void validateDesignator(String designator)
    {
        assertEquals(designator, PSMonitorService.getMonitor(PSSiteCopyProcessMonitor.MONITOR_DESIGNATOR).getStats().getEntries().get("designator"));
    }
    
    private void validateMessage(String designator, String message, boolean match)
    {
        assertEquals(match, message.equals(PSMonitorService.getMonitor(designator).getStats().getEntries().get("message")));
    }
}
