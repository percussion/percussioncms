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
