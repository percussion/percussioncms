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
