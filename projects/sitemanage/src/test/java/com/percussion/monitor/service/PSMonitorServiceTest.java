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
