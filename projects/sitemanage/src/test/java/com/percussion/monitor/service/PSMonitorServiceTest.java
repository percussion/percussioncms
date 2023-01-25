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

package com.percussion.monitor.service;

import static org.junit.Assert.*;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;

import com.percussion.monitor.service.IPSMonitor;
import com.percussion.monitor.service.PSMonitorService;
import com.percussion.share.data.PSMapWrapper;
import org.junit.experimental.categories.Category;

import java.util.Map;

@Category(IntegrationTest.class)
public class PSMonitorServiceTest {

	@Test
	public void testMonitorReferences() {
		IPSMonitor monitor = PSMonitorService.registerMonitor("TESTMONITOR", "testMonitorName");
		IPSMonitor extraMonitor =  PSMonitorService.registerMonitor("EXTRA_TESTMONITOR", "testMonitorName");
		monitor.setMessage("FUBAR");
		monitor.setStatus("BARFU");
		assertTrue(PSMonitorService.getMonitorDesignators().designator.size() == 2);
		PSMapWrapper wrapper = PSMonitorService.getMonitor("TESTMONITOR").getStats();
		Map map = wrapper.getEntries();
		assertTrue(map.get("message").equals("FUBAR"));
		assertTrue(map.get("status").equals("BARFU"));
	}
	
	@Test
	public void testDuplicateDesignation()
	{
		
		IPSMonitor monitor = PSMonitorService.registerMonitor("TESTMONITOR", "testMonitorName");
		IPSMonitor monitorDeuce = PSMonitorService.registerMonitor("TESTMONITOR", "testMonitorName");
		
	}

}
