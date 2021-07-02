/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

import com.percussion.delivery.integrations.ems.IPSEMSEventService;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.ParseException;
import java.util.Date;

/***
 * <Data>
 *  <BookingDate>2018-08-03T00:00:00</BookingDate>
    <StartBookingDate>2018-08-03T00:00:00</StartBookingDate>
    <RoomDescription>WH Claudia Hampton Hall - Welch Hall 165</RoomDescription>
    <TimeEventStart>2018-08-03T08:00:00</TimeEventStart>
    <TimeEventEnd>2018-08-03T14:00:00</TimeEventEnd>
    <GroupName>Government and Community Relations</GroupName>
    <EventName>Sharefest Youth Development Academy</EventName>
    <SetupTypeDescription>As Is</SetupTypeDescription>
    <SetupCount>192</SetupCount>
    <ReservationID>21076</ReservationID>
    <EventCoordinator />
    <GroupID>1415</GroupID>
    <VIP xml:space="preserve"> </VIP>
    <VIPEvent>false</VIPEvent>
    <ClosedAllDay>false</ClosedAllDay>
    <OpenTime>1900-01-01T00:00:00</OpenTime>
    <CloseTime>1900-01-01T00:00:00</CloseTime>
    <GroupTypeDescription>Campus Department</GroupTypeDescription>
    <EventTypeDescription>Community Relations</EventTypeDescription>
    <Contact>Khaleah Bradshaw 3819</Contact>
    <AltContact />
    <BookingID>75349</BookingID>
    <TimeBookingStart>2018-08-03T08:00:00</TimeBookingStart>
    <TimeBookingEnd>2018-08-03T14:00:00</TimeBookingEnd>
    <GMTStartTime>2018-08-03T15:00:00</GMTStartTime>
    <GMTEndTime>2018-08-03T21:00:00</GMTEndTime>
    <TimeZone>PT</TimeZone>
    <BuildingCode>WH</BuildingCode>
    <Building>James L. Welch Hall</Building>
    <RoomCode>WH165</RoomCode>
    <Room>Claudia Hampton Hall - Welch Hall 165</Room>
    <RoomID>274</RoomID>
    <BuildingID>5</BuildingID>
    <RoomTypeID>76</RoomTypeID>
    <RoomType>Classroom</RoomType>
    <HVACZone />
    <StatusID>58</StatusID>
    <StatusTypeID>-11</StatusTypeID>
    <EventTypeID>278</EventTypeID>
    <GroupTypeID>61</GroupTypeID>
    <DateAdded>2018-03-02T09:57:07.16</DateAdded>
    <AddedBy>Khaleah Bradshaw</AddedBy>
    <DateChanged>2018-03-02T10:34:34.113</DateChanged>
    <ChangedBy>Aly Hudspeth</ChangedBy>
    <ContactEmailAddress>kbradshaw@csudh.edu</ContactEmailAddress>
    <CheckedIn>false</CheckedIn>
  </Data>
 * @author natechadwick
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Booking {

	private static final Logger log = LogManager.getLogger(Booking.class);
	
	public Date getBookingDate() {
		return bookingDate;
	}
	public void setBookingDate(String bookingDate) {
		
		try {
			this.bookingDate = FastDateFormat.getInstance(IPSEMSEventService.DATE_FORMAT_STRING).parse(bookingDate.replace("T", " " ));
		} catch (ParseException e) {
			log.error("Error setting BookingDate with value " + bookingDate + " and format: " + IPSEMSEventService.DATE_FORMAT_STRING,e);
		}
	}
	public Date getStartBookingDate() {
		return startBookingDate;
	}
	public void setStartBookingDate(String startBookingDate) {
		try {
			this.startBookingDate = FastDateFormat.getInstance(IPSEMSEventService.DATE_FORMAT_STRING).parse(startBookingDate.replace("T", " " ));
		} catch (ParseException e) {
			log.error("Error setting StartBookingDate with value " + startBookingDate + " and format: " + IPSEMSEventService.DATE_FORMAT_STRING,e);
		}
	}
	public String getRoomDescription() {
		return roomDescription;
	}
	public void setRoomDescription(String roomDescription) {
		this.roomDescription = roomDescription;
	}
	public Date getTimeEventStart() {
		return timeEventStart;
	}
	public void setTimeEventStart(String timeEventStart) {

		try {
			this.timeEventStart = FastDateFormat.getInstance(IPSEMSEventService.TIME_FORMAT_STRING).parse(timeEventStart.replace("T", " " ));
		} catch (ParseException e) {
			log.error("Error setting TimeEventStart with value " + timeEventStart + " and format: " + IPSEMSEventService.TIME_FORMAT_STRING,e);
		}
	}
	public Date getTimeEventEnd() {
		return timeEventEnd;
	}
	public void setTimeEventEnd(String timeEventEnd) {
		try {
			this.timeEventEnd = FastDateFormat.getInstance(IPSEMSEventService.TIME_FORMAT_STRING).parse(timeEventEnd.replace("T", " " ));
		} catch (ParseException e) {
			log.error("Error setting TimeEventEnd with value " + timeEventEnd + " and format: " + IPSEMSEventService.TIME_FORMAT_STRING,e);
		}
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public String getSetupTypeDescription() {
		return setupTypeDescription;
	}
	public void setSetupTypeDescription(String setupTypeDescription) {
		this.setupTypeDescription = setupTypeDescription;
	}
	public int getSetupCount() {
		return setupCount;
	}
	public void setSetupCount(int setupCount) {
		this.setupCount = setupCount;
	}
	public int getReservationID() {
		return reservationID;
	}
	public void setReservationID(int reservationID) {
		this.reservationID = reservationID;
	}
	public int getGroupID() {
		return groupID;
	}
	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}
	public String getVip() {
		return vip;
	}
	public void setVip(String vip) {
		this.vip = vip;
	}
	public boolean isVipEvent() {
		return vipEvent;
	}
	public void setVipEvent(boolean vipEvent) {
		this.vipEvent = vipEvent;
	}
	public boolean isClosedAllDay() {
		return closedAllDay;
	}
	public void setClosedAllDay(boolean closedAllDay) {
		this.closedAllDay = closedAllDay;
	}
	public Date getOpenTime() {
		return openTime;
	}
	public void setOpenTime(String openTime) {
		try {
			this.openTime  =FastDateFormat.getInstance(IPSEMSEventService.TIME_FORMAT_STRING).parse(openTime.replace("T", " " ));
		} catch (ParseException e) {
			log.error("Error setting OpenTime with value " + openTime + " and format: " + IPSEMSEventService.TIME_FORMAT_STRING,e);
		}
	}
	public Date getCloseTime() {
		return closeTime;
	}
	public void setCloseTime(String closeTime) {
		try {
			this.closeTime =  FastDateFormat.getInstance(IPSEMSEventService.TIME_FORMAT_STRING).parse(closeTime.replace("T", " " ));
		} catch (ParseException e) {
			log.error("Error setting CloseTime with value " + closeTime + " and format: " + IPSEMSEventService.TIME_FORMAT_STRING,e);
		}
	}
	public String getGroupTypeDescription() {
		return groupTypeDescription;
	}
	public void setGroupTypeDescription(String groupTypeDescription) {
		this.groupTypeDescription = groupTypeDescription;
	}
	public String getEventTypeDescription() {
		return eventTypeDescription;
	}
	public void setEventTypeDescription(String eventTypeDescription) {
		this.eventTypeDescription = eventTypeDescription;
	}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	public String getAltContact() {
		return altContact;
	}
	public void setAltContact(String altContact) {
		this.altContact = altContact;
	}
	public int getBookingId() {
		return bookingId;
	}
	public void setBookingId(int bookingId) {
		this.bookingId = bookingId;
	}
	public Date getTimeBookingStart() {
		return timeBookingStart;
	}
	public void setTimeBookingStart(String timeBookingStart) {
		
		try {
			this.timeBookingStart = FastDateFormat.getInstance(IPSEMSEventService.TIME_FORMAT_STRING).parse(timeBookingStart.replace("T", " " ));
		} catch (ParseException e) {
			log.error("Error setting timeBookingStart with value " + timeBookingStart + " and format: " + IPSEMSEventService.TIME_FORMAT_STRING,e);
		}
	}
	public Date getTimeBookingEnd() {
		return timeBookingEnd;
	}
	public void setTimeBookingEnd(String timeBookingEnd) {
		try {
			this.timeBookingEnd = FastDateFormat.getInstance(IPSEMSEventService.TIME_FORMAT_STRING).parse(timeBookingEnd.replace("T", " " ));
		} catch (ParseException e) {
			log.error("Error setting timeBookingEnd with value " + timeBookingEnd + " and format: " + IPSEMSEventService.TIME_FORMAT_STRING,e);
		}
	}
	public Date getGmtStartTime() {
		return gmtStartTime;
	}
	public void setGmtStartTime(String gmtStartTime) {
		try {
			this.gmtStartTime = FastDateFormat.getInstance(IPSEMSEventService.TIME_FORMAT_STRING).parse(gmtStartTime.replace("T", " " ));
		} catch (ParseException e) {
			log.error("Error setting gmtStartTime with value " + gmtStartTime + " and format: " + IPSEMSEventService.TIME_FORMAT_STRING,e);
		}
	}
	public Date getGmtEndTime() {
		return gmtEndTime;
	}
	public void setGmtEndTime(String gmtEndTime) {
		try {
			this.gmtEndTime = FastDateFormat.getInstance(IPSEMSEventService.TIME_FORMAT_STRING).parse(gmtEndTime.replace("T", " " ));
		} catch (ParseException e) {
			log.error("Error setting setGmtEndTime with value " + gmtEndTime + " and format: " + IPSEMSEventService.TIME_FORMAT_STRING,e);
		}
	}
	public String getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	public String getBuildingCode() {
		return buildingCode;
	}
	public void setBuildingCode(String buildingCode) {
		this.buildingCode = buildingCode;
	}
	public String getBuilding() {
		return building;
	}
	public void setBuilding(String building) {
		this.building = building;
	}
	public String getRoomCode() {
		return roomCode;
	}
	public void setRoomCode(String roomCode) {
		this.roomCode = roomCode;
	}
	public String getRoom() {
		return room;
	}
	public void setRoom(String room) {
		this.room = room;
	}
	public int getRoomId() {
		return roomId;
	}
	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}
	public int getBuildingId() {
		return buildingId;
	}
	public void setBuildingId(int buildingId) {
		this.buildingId = buildingId;
	}
	public int getRoomTypeId() {
		return roomTypeId;
	}
	public void setRoomTypeId(int roomTypeId) {
		this.roomTypeId = roomTypeId;
	}
	public String getRoomType() {
		return roomType;
	}
	public void setRoomType(String roomType) {
		this.roomType = roomType;
	}
	public String getHvacZone() {
		return hvacZone;
	}
	public void setHvacZone(String hvacZone) {
		this.hvacZone = hvacZone;
	}
	public int getStatusID() {
		return statusID;
	}
	public void setStatusID(int statusID) {
		this.statusID = statusID;
	}
	public int getStatusTypeId() {
		return statusTypeId;
	}
	public void setStatusTypeId(int statusTypeId) {
		this.statusTypeId = statusTypeId;
	}
	public int getEventTypeId() {
		return eventTypeId;
	}
	public void setEventTypeId(int eventTypeId) {
		this.eventTypeId = eventTypeId;
	}
	public int getGoupTypeId() {
		return goupTypeId;
	}
	public void setGoupTypeId(int goupTypeId) {
		this.goupTypeId = goupTypeId;
	}
	public Date getDateAdded() {
		return dateAdded;
	}
	public void setDateAdded(String dateAdded) {
		try {
			this.dateAdded = FastDateFormat.getInstance(IPSEMSEventService.DATE_FORMAT_STRING).parse(dateAdded.replace("T", " " ));
		} catch (ParseException e) {
			log.error("Error setting setDateAdded with value " + dateAdded + " and format: " + IPSEMSEventService.DATE_FORMAT_STRING,e);
		}
	}
	public String getAddedBy() {
		return addedBy;
	}
	public void setAddedBy(String addedBy) {
		this.addedBy = addedBy;
	}
	public Date getDateChanged() {
		return dateChanged;
	}
	public void setDateChanged(String dateChanged) {
		try {
			this.dateChanged =  FastDateFormat.getInstance(IPSEMSEventService.DATE_FORMAT_STRING).parse(dateChanged.replace("T", " " ));
		} catch (ParseException e) {
			log.error("Error setting setDateChanged with value " + dateChanged + " and format: " + IPSEMSEventService.DATE_FORMAT_STRING,e);
		}
	}
	public String getChangedBy() {
		return changedBy;
	}
	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}
	public String getContactEmailAddress() {
		return contactEmailAddress;
	}
	public void setContactEmailAddress(String contactEmailAddress) {
		this.contactEmailAddress = contactEmailAddress;
	}
	public boolean isCheckedIn() {
		return checkedIn;
	}
	public void setCheckedIn(boolean checkedIn) {
		this.checkedIn = checkedIn;
	}
	
	
	
	public String getEventCoordinator() {
		return eventCoordinator;
	}
	public void setEventCoordinator(String eventCoordinator) {
		this.eventCoordinator = eventCoordinator;
	}



	private Date bookingDate;
	private Date startBookingDate;
	private String roomDescription;
	private Date timeEventStart;
	private Date timeEventEnd;
	private String groupName;
	private String eventName;
	private String setupTypeDescription;
	private int setupCount;
	private int reservationID;
	private String eventCoordinator;
	private int groupID;
	private String vip;
	private boolean vipEvent;
	private boolean closedAllDay;
	private Date openTime;
	private Date closeTime;
	private String groupTypeDescription;
	private String eventTypeDescription;
	private String contact;
	private String altContact;
	private int bookingId;
	private Date timeBookingStart;
	private Date timeBookingEnd;
	private Date gmtStartTime;
	private Date gmtEndTime;
	private String  timeZone;
	private String buildingCode;
	private String building;
	private String roomCode;
	private String room;
	private int roomId;
	private int buildingId;
	private int roomTypeId;
	private String roomType;
	private String hvacZone;
	private int statusID;
	private int statusTypeId;
	private int eventTypeId;
	private int goupTypeId;
	private Date dateAdded;
	private String addedBy;
	private Date dateChanged;
	private String changedBy;
	private String contactEmailAddress;
	private boolean checkedIn;
	
}	
