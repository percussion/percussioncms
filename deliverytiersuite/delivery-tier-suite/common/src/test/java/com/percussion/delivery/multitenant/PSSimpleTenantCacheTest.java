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
package com.percussion.delivery.multitenant;

import java.util.Date;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Unit tests for the SimpleTenant Cache. 
 * 
 * @author natechadwick
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/beans.xml"})
public class PSSimpleTenantCacheTest {

	
	PSSimpleTenantCache cache;
	
	@Before
	public void setup(){
		cache = new PSSimpleTenantCache();
	}
	
	@After
	public void teardown(){
		cache = null;
	}
	
	
	/***
	 * Tests basic cache operations. 
	 */
	@Test
	public void testBasicOps(){
		PSTenantInfo t = new PSTenantInfo();
		
		t.clearAPIUsage();
		t.setAPIUsageStart(new Date());
		t.setLastAuthorizationCheckDate(new Date());
		t.setTenantId("007");
		
		cache.put(t);
		
		MockHttpServletRequest req = new MockHttpServletRequest();
		
		IPSTenantInfo u = cache.get(t.getTenantId(), req);
			
		Assert.assertEquals(1, u.getAPIUsage());
		Assert.assertEquals(t.getAPIUsageStart(), u.getAPIUsageStart());
		Assert.assertEquals(t.getLastAuthorizationCheckDate(), u.getLastAuthorizationCheckDate());
		Assert.assertEquals(t.getTenantId(),u.getTenantId());
		Assert.assertEquals(t, u);
		
		cache.clear();
		
		Assert.assertEquals(null, cache.get("007",null));
	}

	
}
