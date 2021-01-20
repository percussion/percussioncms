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
