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

package com.percussion.delivery.integrations.ems.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
public class MCCalendar {
	
	public static final int VIEW_DAILY= 0;
	public static final int VIEW_WEEKLY = 1;
	public static final int VIEW_MONTHLY=3;
	public static final int VIEW_YEARLY=4;
	public static final int FORMAT_LIST=1;
	public static final int FORMAT_GRID=2;
	public static final int START_SUN=0;
	public static final int START_MON=1;
	public static final int START_TUE=2;
	public static final int START_WED=3;
	public static final int START_THU=4;
	public static final int START_FRI=5;
	public static final int START_SAT=6;

	public static final int SUBSCRIPTION_OFF=0;
	public static final int SUBSCRIPTION_AUTHENTICATED=1;
	public static final int SUBSCRIPTION_ALL=2;

	public static final int LISTTYPE_CONDENSED=1;
	public static final int LISTTYPE_STANDARD=2;
	public static final int LISTTYPE_DETAILED=3;

	private String calendarName;
	private Integer calendarId;
	private String calendarDescription;
	private String adminName;
	private String adminEmail;
	private String approvalEmail;
	private Boolean privateCalendar;
	private Boolean activeCalendar;
	private Boolean showCancelledEvents;
	private Integer defaultViewId;
	private Integer calendarFormatId;
	private Integer calendarGroupingId;
	private Boolean showWeekends;
	private Integer startWeekOn;
	private Boolean allowPublicSubmission;
	private Boolean showCalendarContactInfo;
	private Integer subscription;
	private Integer listTypeId;
	public String getCalendarName() {
		return calendarName;
	}
	public void setCalendarName(String calendarName) {
		this.calendarName = calendarName;
	}
	public Integer getCalendarId() {
		return calendarId;
	}
	public void setCalendarId(Integer calendarId) {
		this.calendarId = calendarId;
	}
	public String getCalendarDescription() {
		return calendarDescription;
	}
	public void setCalendarDescription(String calendarDescription) {
		this.calendarDescription = calendarDescription;
	}
	public String getAdminName() {
		return adminName;
	}
	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}
	public String getAdminEmail() {
		return adminEmail;
	}
	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}
	public String getApprovalEmail() {
		return approvalEmail;
	}
	public void setApprovalEmail(String approvalEmail) {
		this.approvalEmail = approvalEmail;
	}
	public Boolean getPrivateCalendar() {
		return privateCalendar;
	}
	public void setPrivateCalendar(Boolean privateCalendar) {
		this.privateCalendar = privateCalendar;
	}
	public Boolean getActiveCalendar() {
		return activeCalendar;
	}
	public void setActiveCalendar(Boolean activeCalendar) {
		this.activeCalendar = activeCalendar;
	}
	public Boolean getShowCancelledEvents() {
		return showCancelledEvents;
	}
	public void setShowCancelledEvents(Boolean showCancelledEvents) {
		this.showCancelledEvents = showCancelledEvents;
	}
	public Integer getDefaultViewId() {
		return defaultViewId;
	}
	public void setDefaultViewId(Integer defaultViewId) {
		this.defaultViewId = defaultViewId;
	}
	public Integer getCalendarFormatId() {
		return calendarFormatId;
	}
	public void setCalendarFormatId(Integer calendarFormatId) {
		this.calendarFormatId = calendarFormatId;
	}
	public Integer getCalendarGroupingId() {
		return calendarGroupingId;
	}
	public void setCalendarGroupingId(Integer calendarGroupingId) {
		this.calendarGroupingId = calendarGroupingId;
	}
	public Boolean getShowWeekends() {
		return showWeekends;
	}
	public void setShowWeekends(Boolean showWeekends) {
		this.showWeekends = showWeekends;
	}
	public Integer getStartWeekOn() {
		return startWeekOn;
	}
	public void setStartWeekOn(Integer startWeekOn) {
		this.startWeekOn = startWeekOn;
	}
	public Boolean getAllowPublicSubmission() {
		return allowPublicSubmission;
	}
	public void setAllowPublicSubmission(Boolean allowPublicSubmission) {
		this.allowPublicSubmission = allowPublicSubmission;
	}
	public Boolean getShowCalendarContactInfo() {
		return showCalendarContactInfo;
	}
	public void setShowCalendarContactInfo(Boolean showCalendarContactInfo) {
		this.showCalendarContactInfo = showCalendarContactInfo;
	}
	public Integer getSubscription() {
		return subscription;
	}
	public void setSubscription(Integer subscription) {
		this.subscription = subscription;
	}
	public Integer getListTypeId() {
		return listTypeId;
	}
	public void setListTypeId(Integer listTypeId) {
		this.listTypeId = listTypeId;
	}
	
	
}
