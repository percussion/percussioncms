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

package com.percussion.delivery.integrations.ems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
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
