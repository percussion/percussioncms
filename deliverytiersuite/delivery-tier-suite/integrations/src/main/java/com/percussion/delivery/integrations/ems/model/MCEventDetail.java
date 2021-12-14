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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.delivery.integrations.ems.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MCEventDetail {

	private static Integer HIGH_PRIORITY=1;
	private static Integer MEDIUM_PRIORITY=2;
	private static Integer LOW_PRIORITY=3;
	
	private Integer eventDetailID;
	private Integer eventID;
	private String title;
	private String description;
	private String location;
	private String locationUrl;
    private Boolean cancelled;
	private Boolean noEndTime;
    private Integer	priority; 
    private String eventDate;
    private String eventStartTime;
    private String eventEndTime;
	private Boolean isAllDayEvent;
	private Boolean isTimedEvent;
	private Integer eventTypeId;
	private String eventTypeName;
	private String contactName;
	private String contactEmail;
	private Boolean isReOccuring;
	private Boolean isOnMultipleCalendars;
	private Integer bookingID;
	private Integer reservationID;
	private Integer connectorID;
	private Boolean hideContactName;
	private Boolean hideContactEmail;
	private Boolean hideContactPhone;
	private String customLabelField1;
    private String customFieldDescription1;
    private String customUrl1;
    private String customLabelField2;
    private String customFieldDescription2;
    private String customUrl2;
    private String eventUpdatedBy;
    private String eventUpdatedDate;
    private String eventDetailUpdatedBy;
	private String eventDetailUpdatedDate;
	public static Integer getHIGH_PRIORITY() {
		return HIGH_PRIORITY;
	}
	public static void setHIGH_PRIORITY(Integer hIGH_PRIORITY) {
		HIGH_PRIORITY = hIGH_PRIORITY;
	}
	public static Integer getMEDIUM_PRIORITY() {
		return MEDIUM_PRIORITY;
	}
	public static void setMEDIUM_PRIORITY(Integer mEDIUM_PRIORITY) {
		MEDIUM_PRIORITY = mEDIUM_PRIORITY;
	}
	public static Integer getLOW_PRIORITY() {
		return LOW_PRIORITY;
	}
	public static void setLOW_PRIORITY(Integer lOW_PRIORITY) {
		LOW_PRIORITY = lOW_PRIORITY;
	}
	public Integer getEventDetailID() {
		return eventDetailID;
	}
	public void setEventDetailID(Integer eventDetailID) {
		this.eventDetailID = eventDetailID;
	}
	public Integer getEventID() {
		return eventID;
	}
	public void setEventID(Integer eventID) {
		this.eventID = eventID;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getLocationUrl() {
		return locationUrl;
	}
	public void setLocationUrl(String locationUrl) {
		this.locationUrl = locationUrl;
	}
	public Boolean getCancelled() {
		return cancelled;
	}
	public void setCancelled(Boolean cancelled) {
		this.cancelled = cancelled;
	}
	public Boolean getNoEndTime() {
		return noEndTime;
	}
	public void setNoEndTime(Boolean noEndTime) {
		this.noEndTime = noEndTime;
	}
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	public String getEventDate() {
		return eventDate;
	}
	public void setEventDate(String eventDate) {
		this.eventDate = eventDate;
	}
	public String getEventStartTime() {
		return eventStartTime;
	}
	public void setEventStartTime(String eventStartTime) {
		this.eventStartTime = eventStartTime;
	}
	public String getEventEndTime() {
		return eventEndTime;
	}
	public void setEventEndTime(String eventEndTime) {
		this.eventEndTime = eventEndTime;
	}
	public Boolean getIsAllDayEvent() {
		return isAllDayEvent;
	}
	public void setIsAllDayEvent(Boolean isAllDayEvent) {
		this.isAllDayEvent = isAllDayEvent;
	}
	public Boolean getIsTimedEvent() {
		return isTimedEvent;
	}
	public void setIsTimedEvent(Boolean isTimedEvent) {
		this.isTimedEvent = isTimedEvent;
	}
	public Integer getEventTypeId() {
		return eventTypeId;
	}
	public void setEventTypeId(Integer eventTypeId) {
		this.eventTypeId = eventTypeId;
	}
	public String getEventTypeName() {
		return eventTypeName;
	}
	public void setEventTypeName(String eventTypeName) {
		this.eventTypeName = eventTypeName;
	}
	public String getContactName() {
		return contactName;
	}
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	public String getContactEmail() {
		return contactEmail;
	}
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}
	public Boolean getIsReOccuring() {
		return isReOccuring;
	}
	public void setIsReOccuring(Boolean isReOccuring) {
		this.isReOccuring = isReOccuring;
	}
	public Boolean getIsOnMultipleCalendars() {
		return isOnMultipleCalendars;
	}
	public void setIsOnMultipleCalendars(Boolean isOnMultipleCalendars) {
		this.isOnMultipleCalendars = isOnMultipleCalendars;
	}
	public Integer getBookingID() {
		return bookingID;
	}
	public void setBookingID(Integer bookingID) {
		this.bookingID = bookingID;
	}
	public Integer getReservationID() {
		return reservationID;
	}
	public void setReservationID(Integer reservationID) {
		this.reservationID = reservationID;
	}
	public Integer getConnectorID() {
		return connectorID;
	}
	public void setConnectorID(Integer connectorID) {
		this.connectorID = connectorID;
	}
	public Boolean getHideContactName() {
		return hideContactName;
	}
	public void setHideContactName(Boolean hideContactName) {
		this.hideContactName = hideContactName;
	}
	public Boolean getHideContactEmail() {
		return hideContactEmail;
	}
	public void setHideContactEmail(Boolean hideContactEmail) {
		this.hideContactEmail = hideContactEmail;
	}
	public Boolean getHideContactPhone() {
		return hideContactPhone;
	}
	public void setHideContactPhone(Boolean hideContactPhone) {
		this.hideContactPhone = hideContactPhone;
	}
	public String getCustomLabelField1() {
		return customLabelField1;
	}
	public void setCustomLabelField1(String customLabelField1) {
		this.customLabelField1 = customLabelField1;
	}
	public String getCustomFieldDescription1() {
		return customFieldDescription1;
	}
	public void setCustomFieldDescription1(String customFieldDescription1) {
		this.customFieldDescription1 = customFieldDescription1;
	}
	public String getCustomUrl1() {
		return customUrl1;
	}
	public void setCustomUrl1(String customUrl1) {
		this.customUrl1 = customUrl1;
	}
	public String getCustomLabelField2() {
		return customLabelField2;
	}
	public void setCustomLabelField2(String customLabelField2) {
		this.customLabelField2 = customLabelField2;
	}
	public String getCustomFieldDescription2() {
		return customFieldDescription2;
	}
	public void setCustomFieldDescription2(String customFieldDescription2) {
		this.customFieldDescription2 = customFieldDescription2;
	}
	public String getCustomUrl2() {
		return customUrl2;
	}
	public void setCustomUrl2(String customUrl2) {
		this.customUrl2 = customUrl2;
	}
	public String getEventUpdatedBy() {
		return eventUpdatedBy;
	}
	public void setEventUpdatedBy(String eventUpdatedBy) {
		this.eventUpdatedBy = eventUpdatedBy;
	}
	public String getEventUpdatedDate() {
		return eventUpdatedDate;
	}
	public void setEventUpdatedDate(String eventUpdatedDate) {
		this.eventUpdatedDate = eventUpdatedDate;
	}
	public String getEventDetailUpdatedBy() {
		return eventDetailUpdatedBy;
	}
	public void setEventDetailUpdatedBy(String eventDetailUpdatedBy) {
		this.eventDetailUpdatedBy = eventDetailUpdatedBy;
	}
	public String getEventDetailUpdatedDate() {
		return eventDetailUpdatedDate;
	}
	public void setEventDetailUpdatedDate(String eventDetailUpdatedDate) {
		this.eventDetailUpdatedDate = eventDetailUpdatedDate;
	}
	
	
	
}
