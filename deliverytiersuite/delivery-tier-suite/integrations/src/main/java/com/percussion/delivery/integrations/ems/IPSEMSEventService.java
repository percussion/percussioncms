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

import java.util.List;

import com.percussion.delivery.integrations.ems.model.Booking;
import com.percussion.delivery.integrations.ems.model.Building;
import com.percussion.delivery.integrations.ems.model.EventType;
import com.percussion.delivery.integrations.ems.model.GroupType;
import com.percussion.delivery.integrations.ems.model.MCCalendar;
import com.percussion.delivery.integrations.ems.model.MCEventDetail;
import com.percussion.delivery.integrations.ems.model.MCEventType;
import com.percussion.delivery.integrations.ems.model.MCLocation;

public interface IPSEMSEventService {
	
	public static String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
	public static String TIME_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
	public static String DATETIME_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
	
	/***
	 * Specifies the default cache timeout for remote method calls
	 * that are cache-able
	 */
	public static final int DEFAULT_CACHE_TIMEOUT=60000;
	
    public List<Booking>getBookings(PSBookingsQuery query);
    
	public List<EventType>getEventTypes();
	public List<Building>getBuildings();
	public List<GroupType>getGroupTypes();
	
}
