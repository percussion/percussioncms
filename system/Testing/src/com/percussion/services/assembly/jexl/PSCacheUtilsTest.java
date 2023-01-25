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

package com.percussion.services.assembly.jexl;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.TimeUnit;

@Category(IntegrationTest.class)
public class PSCacheUtilsTest extends TestCase {
	
	private static Logger logger = Logger.getLogger(PSCacheUtilsTest.class.getName());
	private static final PSCacheUtils cache_utils = new PSCacheUtils();
	private static String cache_value;

	@Before
	protected void setUp() throws Exception {
		
		cache_utils.put("testPrev", "I am testing prev!", PSCacheUtils.getVelocityPrevCache());
		cache_utils.put("testPub", "I am testing pub!", PSCacheUtils.getVelocityPubCache());
		
		super.setUp();
	}

	@After
	protected void tearDown() throws Exception {
		
		cache_utils.flush(PSCacheUtils.getVelocityPrevCache());
		cache_utils.flush(PSCacheUtils.getVelocityPubCache());
		super.tearDown();
	}

	@Test
	public void testGet() {
		
		// test cache object in preview cache
		cache_value = (String)cache_utils.get("testPrev", PSCacheUtils.getVelocityPrevCache());
		assertNotNull(cache_value);
		assertTrue(cache_value == "I am testing prev!");
		cache_value = null;
		assertNull(cache_value);
		
		// test cache object in public cache
		cache_value = (String)cache_utils.get("testPub", PSCacheUtils.getVelocityPubCache());
		assertNotNull(cache_value);
		assertTrue(cache_value == "I am testing pub!");
		cache_value = null;
		assertNull(cache_value);
		
		// try to access cache object from incorrect cache
		cache_value = (String)cache_utils.get("testPrev", PSCacheUtils.getVelocityPubCache());
		assertNull(cache_value);
		
		cache_value = (String)cache_utils.get("testPub", PSCacheUtils.getVelocityPrevCache());
		assertNull(cache_value);
	}

	@Test
	public void testPut() {
		
		// test putting cache object in preview cache
		cache_utils.put("testPrev", "I am testing prev from the put test method!", PSCacheUtils.getVelocityPrevCache());
		cache_value = (String)cache_utils.get("testPrev", PSCacheUtils.getVelocityPrevCache());
		assertTrue(cache_value == "I am testing prev from the put test method!");
		
		// test putting cache object in public cache
		cache_utils.put("testPub", "I am testing pub from the put test method!", PSCacheUtils.getVelocityPubCache());
		cache_value = (String)cache_utils.get("testPub", PSCacheUtils.getVelocityPubCache());
		assertTrue(cache_value == "I am testing pub from the put test method!");
	}

	
	@Test
	public void testFlush() {
		
		cache_utils.flush(PSCacheUtils.getVelocityPrevCache());
		cache_utils.flush(PSCacheUtils.getVelocityPubCache());
		
		cache_value = (String)cache_utils.get("testPrev", PSCacheUtils.getVelocityPrevCache());
		assertNull(cache_value);
		
		cache_value = (String)cache_utils.get("testPub", PSCacheUtils.getVelocityPubCache());
		assertNull(cache_value);
	}

	@Test
	public void testFlushString() {
		
		// test with preview cache
		cache_utils.put("testPrev", "I am testing prev from the flushstring test method!", PSCacheUtils.getVelocityPrevCache());
		cache_value = (String)cache_utils.get("testPrev", PSCacheUtils.getVelocityPrevCache());
		assertTrue(cache_value == "I am testing prev from the flushstring test method!");
		
		cache_utils.flush("testPrev", PSCacheUtils.getVelocityPrevCache());
		cache_value = (String)cache_utils.get("testPrev", PSCacheUtils.getVelocityPrevCache());
		assertNull(cache_value);
		
		// test with publish cache
		cache_utils.put("testPub", "I am testing pub from the flushstring test method!", PSCacheUtils.getVelocityPubCache());
		cache_value = (String)cache_utils.get("testPub", PSCacheUtils.getVelocityPubCache());
		assertTrue(cache_value == "I am testing pub from the flushstring test method!");
		
		cache_utils.flush("testPub", PSCacheUtils.getVelocityPubCache());
		cache_value = (String)cache_utils.get("testPub", PSCacheUtils.getVelocityPubCache());
		assertNull(cache_value);
	}
	
	@Test
	public void testSetTimeToLive() {
		// test with preview cache
		cache_utils.put("testSetTimeToLivetestPrev", "I am testing prev from the testSetTimeToLive test method!", PSCacheUtils.getVelocityPrevCache());
		cache_value = (String)cache_utils.get("testSetTimeToLivetestPrev", PSCacheUtils.getVelocityPrevCache());
		assertTrue(cache_value == "I am testing prev from the testSetTimeToLive test method!");
		
		cache_utils.setTimeToLive("testSetTimeToLivetestPrev", PSCacheUtils.getVelocityPrevCache(), 1);
		
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			logger.error("Exception occured while sleep", e);
		}
		
		cache_value = (String)cache_utils.get("testSetTimeToLivetestPrev", PSCacheUtils.getVelocityPrevCache());
		assertNull(cache_value);
		
		// test with preview cache
		cache_utils.put("testSetTimeToLivetestPub", "I am testing pub from the testSetTimeToLive test method!", PSCacheUtils.getVelocityPubCache());
		cache_value = (String)cache_utils.get("testSetTimeToLivetestPub", PSCacheUtils.getVelocityPubCache());
		assertTrue(cache_value == "I am testing pub from the testSetTimeToLive test method!");
		
		cache_utils.setTimeToLive("testSetTimeToLivetestPub", PSCacheUtils.getVelocityPubCache(), 1);
		
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			logger.error("Exception occured while sleep", e);
		}
		
		cache_value = (String)cache_utils.get("testSetTimeToLivetestPub", PSCacheUtils.getVelocityPubCache());
		assertNull(cache_value);
	}
	
	@Test
	public void testSetTimeToIdle() {
		
		// test with preview cache
		cache_utils.put("testSetTimeToIdletestPrev", "I am testing prev from the testSetTimeToIdle test method!", PSCacheUtils.getVelocityPrevCache());
		cache_value = (String)cache_utils.get("testSetTimeToIdletestPrev", PSCacheUtils.getVelocityPrevCache());
		assertTrue(cache_value == "I am testing prev from the testSetTimeToIdle test method!");
		
		cache_utils.setTimeToIdle("testSetTimeToIdletestPrev", PSCacheUtils.getVelocityPrevCache(), 1);
		
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			logger.error("Exception occured while sleep", e);
		}
		
		cache_value = (String)cache_utils.get("testSetTimeToIdletestPrev", PSCacheUtils.getVelocityPrevCache());
		assertNull(cache_value);
		
		// test with preview cache
		cache_utils.put("testSetTimeToIdletestPub", "I am testing pub from the testSetTimeToIdle test method!", PSCacheUtils.getVelocityPubCache());
		cache_value = (String)cache_utils.get("testSetTimeToIdletestPub", PSCacheUtils.getVelocityPubCache());
		assertTrue(cache_value == "I am testing pub from the testSetTimeToIdle test method!");
		
		cache_utils.setTimeToIdle("testSetTimeToIdletestPub", PSCacheUtils.getVelocityPubCache(), 1);
		
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			logger.error("Exception occured while sleep", e);
		}
		
		cache_value = (String)cache_utils.get("testSetTimeToIdletestPub", PSCacheUtils.getVelocityPubCache());
		assertNull(cache_value);
	}
}
