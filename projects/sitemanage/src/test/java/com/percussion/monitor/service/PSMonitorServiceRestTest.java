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
