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
