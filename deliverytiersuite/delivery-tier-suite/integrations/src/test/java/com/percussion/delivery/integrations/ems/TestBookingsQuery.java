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

package com.percussion.delivery.integrations.ems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import com.percussion.delivery.integrations.ems.model.Booking;

import junit.framework.TestCase;

public class TestBookingsQuery extends TestCase {
	
	
	@Test
	public void testJsonFormat() throws JsonGenerationException, JsonMappingException, IOException{
		
//		ObjectMapper mapper = new ObjectMapper();
//		PSBookingsQuery obj = new PSBookingsQuery();
//		
//		ArrayList<Integer> ids = new ArrayList<Integer>();
//		ids.add(1);
//		ids.add(2);
//		ids.add(3);
//		
//		obj.setBuildingIds(ids);
//		obj.setEventTypes(ids);
//		obj.setGroupTypes(ids);
//		obj.setStartDate("2018-03-21");
//		obj.setEndDate("2018-12-30");
//	
//		String json = mapper.writeValueAsString(obj);
//		
//		System.out.println(json);
//		System.out.println(Calendar.YEAR);
//		
//		Booking booking = new Booking();
//		
//		System.out.println(mapper.writeValueAsString(booking));
		
		System.out.println(Calendar.DAY_OF_MONTH);
		
	}

}
