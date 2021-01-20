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
