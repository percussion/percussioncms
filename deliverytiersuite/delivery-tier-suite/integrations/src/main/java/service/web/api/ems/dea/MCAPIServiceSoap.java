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

/**
 * MCAPIServiceSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package service.web.api.ems.dea;

public interface MCAPIServiceSoap extends java.rmi.Remote {
    public java.lang.String getEvents(java.lang.String userName, java.lang.String password, java.util.Calendar startDate, java.util.Calendar endDate, java.lang.String eventName, java.lang.String location, int[] calendars, int[] eventTypes, java.lang.String udqAnswer) throws java.rmi.RemoteException;
    public java.lang.String getFeaturedEvents(java.lang.String userName, java.lang.String password, java.util.Calendar startDate, java.util.Calendar endDate, java.lang.String eventName, java.lang.String location, int[] calendars, int[] eventTypes, java.lang.String udqAnswer) throws java.rmi.RemoteException;
    public java.lang.String getEvent(java.lang.String userName, java.lang.String password, int eventDetailId) throws java.rmi.RemoteException;
    public java.lang.String getSpecialDates(java.lang.String userName, java.lang.String password, java.util.Calendar startDate, java.util.Calendar endDate, java.lang.String eventName, int[] calendars) throws java.rmi.RemoteException;
    public java.lang.String getLocations(java.lang.String userName, java.lang.String password) throws java.rmi.RemoteException;
    public java.lang.String getEventTypes(java.lang.String userName, java.lang.String password) throws java.rmi.RemoteException;
    public java.lang.String getCalendars(java.lang.String userName, java.lang.String password) throws java.rmi.RemoteException;
    public java.lang.String getCalendar(java.lang.String userName, java.lang.String password, int calendarId) throws java.rmi.RemoteException;
    public java.lang.String getGroupings(java.lang.String userName, java.lang.String password) throws java.rmi.RemoteException;
    public java.lang.String getUdqs(java.lang.String userName, java.lang.String password, int eventId) throws java.rmi.RemoteException;
    public java.lang.String getComments(java.lang.String userName, java.lang.String password, int eventId, int reservationId, int bookingid) throws java.rmi.RemoteException;
    public java.lang.String addEvent(java.lang.String userName, java.lang.String password, java.util.Calendar eventDate, int[] calendars, java.lang.String title, java.lang.String titleUrl, java.lang.String description, java.util.Calendar timeEventStart, java.util.Calendar timeEventEnd, java.lang.String location, java.lang.String locationUrl, java.lang.String contactName, java.lang.String contactEmail, java.lang.String contactPhone, boolean isAllDayEvent, boolean isUntimed, boolean noEndTime, boolean canceled, java.lang.String customFieldLabel1, java.lang.String customFieldDescription1, java.lang.String customFieldUrl1, java.lang.String customFieldLabel2, java.lang.String customFieldDescription2, java.lang.String customFieldUrl2, int eventTypeID, java.lang.String department, boolean hideContactName, boolean hideContactEmail, boolean hideContactPhone) throws java.rmi.RemoteException;
    public java.lang.String addEventWithMultipleDates(java.lang.String userName, java.lang.String password, java.util.Calendar[] dates, int[] calendars, java.lang.String title, java.lang.String titleUrl, java.lang.String description, java.util.Calendar timeEventStart, java.util.Calendar timeEventEnd, java.lang.String location, java.lang.String locationUrl, java.lang.String contactName, java.lang.String contactEmail, java.lang.String contactPhone, boolean isAllDayEvent, boolean isUntimed, boolean noEndTime, boolean canceled, java.lang.String customFieldLabel1, java.lang.String customFieldDescription1, java.lang.String customFieldUrl1, java.lang.String customFieldLabel2, java.lang.String customFieldDescription2, java.lang.String customFieldUrl2, int eventTypeID, java.lang.String department, boolean hideContactName, boolean hideContactEmail, boolean hideContactPhone) throws java.rmi.RemoteException;
    public java.lang.String updateEvent(java.lang.String userName, java.lang.String password, int eventID, java.util.Calendar eventDate, int[] calendars, java.lang.String title, java.lang.String titleUrl, java.lang.String description, java.util.Calendar timeEventStart, java.util.Calendar timeEventEnd, java.lang.String location, java.lang.String locationUrl, java.lang.String contactName, java.lang.String contactEmail, java.lang.String contactPhone, boolean isAllDayEvent, boolean isUntimed, boolean noEndTime, boolean canceled, java.lang.String customFieldLabel1, java.lang.String customFieldDescription1, java.lang.String customFieldUrl1, java.lang.String customFieldLabel2, java.lang.String customFieldDescription2, java.lang.String customFieldUrl2, int eventTypeID, java.lang.String department, boolean hideContactName, boolean hideContactEmail, boolean hideContactPhone) throws java.rmi.RemoteException;
    public java.lang.String updateEventDate(java.lang.String userName, java.lang.String password, int eventDetailID, java.util.Calendar eventDate, java.lang.String title, java.lang.String titleUrl, java.lang.String description, java.util.Calendar timeEventStart, java.util.Calendar timeEventEnd, java.lang.String location, java.lang.String locationUrl, boolean isAllDayEvent, boolean isUntimed, boolean noEndTime, boolean canceled, java.lang.String customFieldLabel1, java.lang.String customFieldDescription1, java.lang.String customFieldUrl1, java.lang.String customFieldLabel2, java.lang.String customFieldDescription2, java.lang.String customFieldUrl2, int eventTypeID) throws java.rmi.RemoteException;
}
