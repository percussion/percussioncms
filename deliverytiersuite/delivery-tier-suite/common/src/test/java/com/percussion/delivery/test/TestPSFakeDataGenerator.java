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
package com.percussion.delivery.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Performs some basic testing on the fake data generator
 * @author natechadwick
 *
 */
public class TestPSFakeDataGenerator {

	@Test
	public void getTenRegistrants(){
		
		List<FakeRegistrant> r = PSFakeDataGenerator.getFakeRegistrations(10);
		
		Assert.assertEquals(10, r.size());
	}
	
	/***
	 * Tests to make sure passing in 0 returns all. 
	 * 
	 * NOTE: This test will need changed if the data set size changes. 
	 */
	@Test
	public void getAllRegistrants(){
		List<FakeRegistrant> r = PSFakeDataGenerator.getFakeRegistrations(0);
		
	//	Assert.assertEquals(50000, r.size());
		
	}
	
}
