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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PSFeaturedEventsQuery {

	private String startDate;
	private String endDate;
	private String eventNameSearch;
	private String locationNameSearch;
	private List<Integer> calendarsToSearch;
	private List<Integer> eventTypesToSearch;
	private String uDQanswer;
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
	public String getEventNameSearch() {
		return eventNameSearch;
	}
	public void setEventNameSearch(String eventNameSearch) {
		this.eventNameSearch = eventNameSearch;
	}
	public String getLocationNameSearch() {
		return locationNameSearch;
	}
	public void setLocationNameSearch(String locationNameSearch) {
		this.locationNameSearch = locationNameSearch;
	}
	public List<Integer> getCalendarsToSearch() {
		return calendarsToSearch;
	}
	public void setCalendarsToSearch(List<Integer> calendarsToSearch) {
		this.calendarsToSearch = calendarsToSearch;
	}
	public List<Integer> getEventTypesToSearch() {
		return eventTypesToSearch;
	}
	public void setEventTypesToSearch(List<Integer> eventTypesToSearch) {
		this.eventTypesToSearch = eventTypesToSearch;
	}
	public String getuDQanswer() {
		return uDQanswer;
	}
	public void setuDQanswer(String uDQanswer) {
		this.uDQanswer = uDQanswer;
	}
	
	
	
}
