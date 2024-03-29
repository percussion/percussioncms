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

package service.web.api.ems.dea;

public class MCAPIServiceSoapProxy implements MCAPIServiceSoap {
  private String _endpoint = null;
  private MCAPIServiceSoap mCAPIServiceSoap = null;
  
  public MCAPIServiceSoapProxy() {
    _initMCAPIServiceSoapProxy();
  }
  
  public MCAPIServiceSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initMCAPIServiceSoapProxy();
  }
  
  private void _initMCAPIServiceSoapProxy() {
    try {
      mCAPIServiceSoap = (new MCAPIServiceLocator()).getMCAPIServiceSoap();
      if (mCAPIServiceSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)mCAPIServiceSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)mCAPIServiceSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (mCAPIServiceSoap != null)
      ((javax.xml.rpc.Stub)mCAPIServiceSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public MCAPIServiceSoap getMCAPIServiceSoap() {
    if (mCAPIServiceSoap == null)
      _initMCAPIServiceSoapProxy();
    return mCAPIServiceSoap;
  }
  
  public java.lang.String getEvents(java.lang.String userName, java.lang.String password, java.util.Calendar startDate, java.util.Calendar endDate, java.lang.String eventName, java.lang.String location, int[] calendars, int[] eventTypes, java.lang.String udqAnswer) throws java.rmi.RemoteException{
    if (mCAPIServiceSoap == null)
      _initMCAPIServiceSoapProxy();
    return mCAPIServiceSoap.getEvents(userName, password, startDate, endDate, eventName, location, calendars, eventTypes, udqAnswer);
  }
  
  public java.lang.String getFeaturedEvents(java.lang.String userName, java.lang.String password, java.util.Calendar startDate, java.util.Calendar endDate, java.lang.String eventName, java.lang.String location, int[] calendars, int[] eventTypes, java.lang.String udqAnswer) throws java.rmi.RemoteException{
    if (mCAPIServiceSoap == null)
      _initMCAPIServiceSoapProxy();
    return mCAPIServiceSoap.getFeaturedEvents(userName, password, startDate, endDate, eventName, location, calendars, eventTypes, udqAnswer);
  }
  
  public java.lang.String getEvent(java.lang.String userName, java.lang.String password, int eventDetailId) throws java.rmi.RemoteException{
    if (mCAPIServiceSoap == null)
      _initMCAPIServiceSoapProxy();
    return mCAPIServiceSoap.getEvent(userName, password, eventDetailId);
  }
  
  public java.lang.String getSpecialDates(java.lang.String userName, java.lang.String password, java.util.Calendar startDate, java.util.Calendar endDate, java.lang.String eventName, int[] calendars) throws java.rmi.RemoteException{
    if (mCAPIServiceSoap == null)
      _initMCAPIServiceSoapProxy();
    return mCAPIServiceSoap.getSpecialDates(userName, password, startDate, endDate, eventName, calendars);
  }
  
  public java.lang.String getLocations(java.lang.String userName, java.lang.String password) throws java.rmi.RemoteException{
    if (mCAPIServiceSoap == null)
      _initMCAPIServiceSoapProxy();
    return mCAPIServiceSoap.getLocations(userName, password);
  }
  
  public java.lang.String getEventTypes(java.lang.String userName, java.lang.String password) throws java.rmi.RemoteException{
    if (mCAPIServiceSoap == null)
      _initMCAPIServiceSoapProxy();
    return mCAPIServiceSoap.getEventTypes(userName, password);
  }
  
  public java.lang.String getCalendars(java.lang.String userName, java.lang.String password) throws java.rmi.RemoteException{
    if (mCAPIServiceSoap == null)
      _initMCAPIServiceSoapProxy();
    return mCAPIServiceSoap.getCalendars(userName, password);
  }
  
  public java.lang.String getCalendar(java.lang.String userName, java.lang.String password, int calendarId) throws java.rmi.RemoteException{
    if (mCAPIServiceSoap == null)
      _initMCAPIServiceSoapProxy();
    return mCAPIServiceSoap.getCalendar(userName, password, calendarId);
  }
  
  public java.lang.String getGroupings(java.lang.String userName, java.lang.String password) throws java.rmi.RemoteException{
    if (mCAPIServiceSoap == null)
      _initMCAPIServiceSoapProxy();
    return mCAPIServiceSoap.getGroupings(userName, password);
  }
  
  public java.lang.String getUdqs(java.lang.String userName, java.lang.String password, int eventId) throws java.rmi.RemoteException{
    if (mCAPIServiceSoap == null)
      _initMCAPIServiceSoapProxy();
    return mCAPIServiceSoap.getUdqs(userName, password, eventId);
  }
  
  public java.lang.String getComments(java.lang.String userName, java.lang.String password, int eventId, int reservationId, int bookingid) throws java.rmi.RemoteException{
    if (mCAPIServiceSoap == null)
      _initMCAPIServiceSoapProxy();
    return mCAPIServiceSoap.getComments(userName, password, eventId, reservationId, bookingid);
  }
  
  public java.lang.String addEvent(java.lang.String userName, java.lang.String password, java.util.Calendar eventDate, int[] calendars, java.lang.String title, java.lang.String titleUrl, java.lang.String description, java.util.Calendar timeEventStart, java.util.Calendar timeEventEnd, java.lang.String location, java.lang.String locationUrl, java.lang.String contactName, java.lang.String contactEmail, java.lang.String contactPhone, boolean isAllDayEvent, boolean isUntimed, boolean noEndTime, boolean canceled, java.lang.String customFieldLabel1, java.lang.String customFieldDescription1, java.lang.String customFieldUrl1, java.lang.String customFieldLabel2, java.lang.String customFieldDescription2, java.lang.String customFieldUrl2, int eventTypeID, java.lang.String department, boolean hideContactName, boolean hideContactEmail, boolean hideContactPhone) throws java.rmi.RemoteException{
    if (mCAPIServiceSoap == null)
      _initMCAPIServiceSoapProxy();
    return mCAPIServiceSoap.addEvent(userName, password, eventDate, calendars, title, titleUrl, description, timeEventStart, timeEventEnd, location, locationUrl, contactName, contactEmail, contactPhone, isAllDayEvent, isUntimed, noEndTime, canceled, customFieldLabel1, customFieldDescription1, customFieldUrl1, customFieldLabel2, customFieldDescription2, customFieldUrl2, eventTypeID, department, hideContactName, hideContactEmail, hideContactPhone);
  }
  
  public java.lang.String addEventWithMultipleDates(java.lang.String userName, java.lang.String password, java.util.Calendar[] dates, int[] calendars, java.lang.String title, java.lang.String titleUrl, java.lang.String description, java.util.Calendar timeEventStart, java.util.Calendar timeEventEnd, java.lang.String location, java.lang.String locationUrl, java.lang.String contactName, java.lang.String contactEmail, java.lang.String contactPhone, boolean isAllDayEvent, boolean isUntimed, boolean noEndTime, boolean canceled, java.lang.String customFieldLabel1, java.lang.String customFieldDescription1, java.lang.String customFieldUrl1, java.lang.String customFieldLabel2, java.lang.String customFieldDescription2, java.lang.String customFieldUrl2, int eventTypeID, java.lang.String department, boolean hideContactName, boolean hideContactEmail, boolean hideContactPhone) throws java.rmi.RemoteException{
    if (mCAPIServiceSoap == null)
      _initMCAPIServiceSoapProxy();
    return mCAPIServiceSoap.addEventWithMultipleDates(userName, password, dates, calendars, title, titleUrl, description, timeEventStart, timeEventEnd, location, locationUrl, contactName, contactEmail, contactPhone, isAllDayEvent, isUntimed, noEndTime, canceled, customFieldLabel1, customFieldDescription1, customFieldUrl1, customFieldLabel2, customFieldDescription2, customFieldUrl2, eventTypeID, department, hideContactName, hideContactEmail, hideContactPhone);
  }
  
  public java.lang.String updateEvent(java.lang.String userName, java.lang.String password, int eventID, java.util.Calendar eventDate, int[] calendars, java.lang.String title, java.lang.String titleUrl, java.lang.String description, java.util.Calendar timeEventStart, java.util.Calendar timeEventEnd, java.lang.String location, java.lang.String locationUrl, java.lang.String contactName, java.lang.String contactEmail, java.lang.String contactPhone, boolean isAllDayEvent, boolean isUntimed, boolean noEndTime, boolean canceled, java.lang.String customFieldLabel1, java.lang.String customFieldDescription1, java.lang.String customFieldUrl1, java.lang.String customFieldLabel2, java.lang.String customFieldDescription2, java.lang.String customFieldUrl2, int eventTypeID, java.lang.String department, boolean hideContactName, boolean hideContactEmail, boolean hideContactPhone) throws java.rmi.RemoteException{
    if (mCAPIServiceSoap == null)
      _initMCAPIServiceSoapProxy();
    return mCAPIServiceSoap.updateEvent(userName, password, eventID, eventDate, calendars, title, titleUrl, description, timeEventStart, timeEventEnd, location, locationUrl, contactName, contactEmail, contactPhone, isAllDayEvent, isUntimed, noEndTime, canceled, customFieldLabel1, customFieldDescription1, customFieldUrl1, customFieldLabel2, customFieldDescription2, customFieldUrl2, eventTypeID, department, hideContactName, hideContactEmail, hideContactPhone);
  }
  
  public java.lang.String updateEventDate(java.lang.String userName, java.lang.String password, int eventDetailID, java.util.Calendar eventDate, java.lang.String title, java.lang.String titleUrl, java.lang.String description, java.util.Calendar timeEventStart, java.util.Calendar timeEventEnd, java.lang.String location, java.lang.String locationUrl, boolean isAllDayEvent, boolean isUntimed, boolean noEndTime, boolean canceled, java.lang.String customFieldLabel1, java.lang.String customFieldDescription1, java.lang.String customFieldUrl1, java.lang.String customFieldLabel2, java.lang.String customFieldDescription2, java.lang.String customFieldUrl2, int eventTypeID) throws java.rmi.RemoteException{
    if (mCAPIServiceSoap == null)
      _initMCAPIServiceSoapProxy();
    return mCAPIServiceSoap.updateEventDate(userName, password, eventDetailID, eventDate, title, titleUrl, description, timeEventStart, timeEventEnd, location, locationUrl, isAllDayEvent, isUntimed, noEndTime, canceled, customFieldLabel1, customFieldDescription1, customFieldUrl1, customFieldLabel2, customFieldDescription2, customFieldUrl2, eventTypeID);
  }
  
  
}
