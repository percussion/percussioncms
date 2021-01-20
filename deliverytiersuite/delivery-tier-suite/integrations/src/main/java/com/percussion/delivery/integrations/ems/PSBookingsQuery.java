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

import javax.xml.bind.annotation.XmlRootElement;

/***
 * A model object to hold query parameters to pass to the
 * backend Bookings service. 
 * 
 * @author natechadwick
 *
 */
@XmlRootElement
public class PSBookingsQuery {
	
	private List<Integer> buildingIds;
	private List<Integer> eventTypes;
	private List<Integer> groupTypes;
	private String startDate;
	private String endDate;
	public List<Integer> getBuildingIds() {
		return buildingIds;
	}
	public void setBuildingIds(List<Integer> buildingIds) {
		this.buildingIds = buildingIds;
	}
	public List<Integer> getEventTypes() {
		return eventTypes;
	}
	public void setEventTypes(List<Integer> eventTypes) {
		this.eventTypes = eventTypes;
	}
	public List<Integer> getGroupTypes() {
		return groupTypes;
	}
	public void setGroupTypes(List<Integer> groupTypes) {
		this.groupTypes = groupTypes;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	
}
