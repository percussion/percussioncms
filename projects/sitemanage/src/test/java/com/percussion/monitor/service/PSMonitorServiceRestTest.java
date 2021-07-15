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

import com.percussion.assetmanagement.web.service.PSAssetServiceRestClient;
import com.percussion.share.test.PSRestTestCase;

import org.junit.Test;

public class PSMonitorServiceRestTest extends PSRestTestCase<PSMonitorServiceRestClient> {

	@Test
	public void test() {
		String monitor = getRestClient(super.baseUrl).getMonitor("fubar");
		String monitor2 = getRestClient(super.baseUrl).getMonitor("barfu");
		String listOfMonitors = getRestClient(super.baseUrl).getMonitorList();
		assertTrue(listOfMonitors.toUpperCase().contains("PSMONITOR"));
		assertTrue(listOfMonitors.contains("fubar"));
		assertTrue(listOfMonitors.contains("barfu"));
		String list = getRestClient(super.baseUrl).getMonitorList();
		String all = getRestClient(super.baseUrl).getAllMonitors();
		assertTrue(list.contains("fubar"));
		assertTrue(list.contains("barfu"));
		assertTrue(all.contains("fubar"));
		assertTrue(all.contains("barfu"));
	}

	@Override
	protected PSMonitorServiceRestClient getRestClient(String baseUrl) {
		return new PSMonitorServiceRestClient(baseUrl);
	}
}
