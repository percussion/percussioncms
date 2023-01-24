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
