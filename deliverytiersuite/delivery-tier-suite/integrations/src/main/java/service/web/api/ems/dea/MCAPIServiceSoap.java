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
