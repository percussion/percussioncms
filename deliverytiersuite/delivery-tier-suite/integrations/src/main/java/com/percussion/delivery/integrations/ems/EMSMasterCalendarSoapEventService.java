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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.delivery.integrations.ems;

import com.percussion.delivery.integrations.ems.model.MCCalendar;
import com.percussion.delivery.integrations.ems.model.MCCalendarEntry;
import com.percussion.delivery.integrations.ems.model.MCEventDetail;
import com.percussion.delivery.integrations.ems.model.MCEventType;
import com.percussion.delivery.integrations.ems.model.MCGrouping;
import com.percussion.delivery.integrations.ems.model.MCLocation;
import com.percussion.security.xml.PSSecureXMLUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import service.web.api.ems.dea.MCAPIServiceLocator;
import service.web.api.ems.dea.MCAPIServiceSoap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EMSMasterCalendarSoapEventService implements IPSEMSMasterCalendarService {

	private MCAPIServiceSoap soap;
	private String mcUserName;
	private String mcPassword;
	private String mcEndpoint;
	
	private static final Logger log = LogManager.getLogger(EMSMasterCalendarSoapEventService.class);
	
	public EMSMasterCalendarSoapEventService(String mcUserName, String mcPassword, String mcEndpoint){
		this.mcUserName = mcUserName;
		this.mcPassword = mcPassword;
		this.mcEndpoint = mcEndpoint;
		if(mcPassword == null || mcUserName == null || mcEndpoint == null || mcEndpoint.equals("")){
			return;
		}
		MCAPIServiceLocator locator = new MCAPIServiceLocator();
		try {
			this.soap = locator.getMCAPIServiceSoap(new URL(this.mcEndpoint));
		} catch (MalformedURLException | ServiceException e) {
			log.error("Error connecting to remote Master Calendar API. Error: {}", e.getMessage());
			log.debug(e.getMessage(), e);
		}		
	}
	
	@Override
	public List<MCEventDetail> getMasterCalendarEvents(PSEventQuery query) {
		List<MCEventDetail> ret  = new ArrayList<>();
		String eventName = null;
		String location = null;
		int[] eventTypes = null;
		int[] calendars = null;
		
		try {
			FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd hh:mm:ss");
			Date date = null;
			try {
				date = (Date) format.parseObject(query.getStartDate());
			} catch (ParseException e) {
				log.error(String.format("Error processing start date: {}, Error: {}",query.getStartDate()), e.getMessage());
				log.debug(e.getMessage(), e);
			}
			Calendar startDate = Calendar.getInstance();
			startDate.setTime(date);
			
			try {
				date = (Date)format.parseObject(query.getEndDate());
			} catch (ParseException e) {
				log.error(String.format("Error processing end date: {}, Error: {}",query.getEndDate()), e.getMessage());
				log.debug(e.getMessage(), e);
			}
			Calendar endDate = Calendar.getInstance();
			endDate.setTime(date);
	
			if(query.getEventName() != null && !query.getEventName().trim().equals("")){
				eventName = query.getEventName();
			}
			
			if(query.getLocation() != null && !query.getLocation().trim().equals("")){
				location = query.getLocation();
			}
			
			if(query.getEventTypes() != null && !query.getEventTypes().isEmpty()){
				eventTypes = ArrayUtils.toPrimitive(query.getEventTypes().toArray(new Integer[query.getEventTypes().size()]));
			}
		
			if(query.getCalendars() != null && !query.getCalendars().isEmpty()){
				calendars = ArrayUtils.toPrimitive(query.getCalendars().toArray(new Integer[query.getCalendars().size()]));
			}
			
			String xml = soap.getEvents(mcUserName, mcPassword, startDate, endDate,eventName, location,calendars,eventTypes, null);
			if(checkForErrors(xml)){
				log.error("getEvents Service returned the following errors:{}", xml);
			}else{
				ret = parseEventDetailXML(xml);
			}
			
			
		} catch (RemoteException e) {
			log.error("An error occurred connecting to the Master Calendar API Error: {}", e.getMessage());
			log.debug(e.getMessage(), e);
			return ret;
		}
		
		return ret;
	}

	private boolean checkForErrors(String xml) {
				if(xml.contains("<Errors>")){
				return true;
			}else{
				return false;
			}
	}

	private List<MCEventDetail> parseEventDetailXML(String xml) {
		List<MCEventDetail> ret = new ArrayList<>();
		
		DocumentBuilderFactory dbf = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
				false
		);
		dbf.setNamespaceAware(false);
		dbf.setValidating(false);
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			log.error("Error parsing MCEventDetail:{}, Error: {}",xml, e1.getMessage());
			log.debug(e1.getMessage(), e1);
		} 
		try {
			Document doc = db.newDocument();
			Node fragmentNode = db.parse(
				        new InputSource(new StringReader(xml)))
				        .getDocumentElement();
				    fragmentNode = doc.importNode(fragmentNode, true);
				    doc.appendChild(fragmentNode);
			NodeList entries = doc.getElementsByTagName("Data");
			for(int i=0;i<entries.getLength();i++){
				NodeList children = entries.item(i).getChildNodes();
				MCEventDetail e = new MCEventDetail();
				for(int j=0;j<children.getLength();j++){
					if(children.item(j).getNodeName() == "EventDetailID"){
						e.setEventDetailID(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "EventID"){
						e.setEventID(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "Title"){
						e.setTitle(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "Description"){
						e.setDescription(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "Location"){
						e.setLocation(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "LocationUrl"){
						e.setLocationUrl(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "Canceled"){
						e.setCancelled(Boolean.parseBoolean(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "NoEndTime"){
						e.setNoEndTime(Boolean.parseBoolean(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "Priority"){
						e.setPriority(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "EventDate"){
						e.setEventDate(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "TimeEventStart"){
						e.setEventStartTime(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "TimeEventEnds"){
						e.setEventEndTime(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "IsAllDayEvent"){
						e.setIsAllDayEvent(Boolean.parseBoolean(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "IsTimedEvent"){
						e.setIsTimedEvent(Boolean.parseBoolean(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "EventTypeID"){
						e.setEventTypeId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "EventTypeName"){
						e.setEventTypeName(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "Contactname"){
						e.setContactName(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "ContactEmail"){
						e.setContactEmail(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "IsReOccurring"){
						e.setIsReOccuring(Boolean.parseBoolean(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "IsOnMultipleCalendars"){
						e.setIsOnMultipleCalendars(Boolean.parseBoolean(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "BookingID"){
						e.setBookingID(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "ReservationID"){
						e.setReservationID(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "ConnectorID"){
						e.setConnectorID(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "HideContactName"){
						e.setHideContactName(Boolean.parseBoolean(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "HideContactEmail"){
						e.setHideContactName(Boolean.parseBoolean(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "HideContactPhone"){
						e.setHideContactName(Boolean.parseBoolean(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "CustomFieldLabel1"){
						e.setCustomLabelField1(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "CustomFieldDescription1"){
						e.setCustomFieldDescription1(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "CustomUrl1"){
						e.setCustomUrl1(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "CustomFieldLabel2"){
						e.setCustomLabelField2(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "CustomFieldDescription2"){
						e.setCustomFieldDescription2(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "CustomUrl2"){
						e.setCustomUrl2(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "EventUpdatedBy"){
						e.setEventUpdatedBy(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "EventUpdatedDate"){
						e.setEventUpdatedDate(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "EventDetailUpdatedBy"){
						e.setEventDetailUpdatedBy(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "EventDetailUpdatedDate"){
						e.setEventDetailUpdatedDate(children.item(j).getTextContent());
					}
					
				}
				ret.add(e);
			}
			
		} catch (SAXException e) {
			log.error("Error parsing response: {}, Error: {}", xml,e.getMessage());
			log.debug(e.getMessage(), e);
		} catch (IOException e) {
			log.error("Error parsing response: {}, Error: {}", xml,e.getMessage());
			log.debug(e.getMessage(), e);
		}
		
		return ret;
	}

	@Override
	public List<MCEventDetail> getMasterCalendarFeaturedEvents(PSFeaturedEventsQuery query) {
		List<MCEventDetail> ret  = new ArrayList<>();
		String eventName = null;
		String location = null;
		int[] eventTypes = null;
		int[] calendars = null;
		
		try {
			FastDateFormat format =  FastDateFormat.getInstance("yyyy-MM-dd hh:mm:ss");
			Date date = null;
			try {
				date = (Date)format.parseObject(query.getStartDate());
			} catch (ParseException e) {
				log.error("Error processing start date: {} Error: {}",
						query.getStartDate(),
						e.getMessage());
				log.debug(e.getMessage(), e);
			}
			Calendar startDate = Calendar.getInstance();
			startDate.setTime(date);
			
			try {
				date = (Date)format.parseObject(query.getEndDate());
			} catch (ParseException e) {
				log.error("Error processing end date: {} Error: {}",
						query.getEndDate(),
						e.getMessage());
				log.debug(e.getMessage(), e);
			}
			Calendar endDate = Calendar.getInstance();
			endDate.setTime(date);
	
			if(query.getEventNameSearch() != null && !query.getEventNameSearch().trim().equals("")){
				eventName = query.getEventNameSearch();
			}
			
			if(query.getLocationNameSearch() != null && !query.getLocationNameSearch().trim().equals("")){
				location = query.getLocationNameSearch();
			}
			
			if(query.getEventTypesToSearch() != null && !query.getEventTypesToSearch().isEmpty()){
				eventTypes = ArrayUtils.toPrimitive(query.getEventTypesToSearch().toArray(new Integer[query.getEventTypesToSearch().size()]));
			}
		
			if(query.getCalendarsToSearch() != null && !query.getCalendarsToSearch().isEmpty()){
				calendars = ArrayUtils.toPrimitive(query.getCalendarsToSearch().toArray(new Integer[query.getCalendarsToSearch().size()]));
			}
			
			String xml = soap.getEvents(mcUserName, mcPassword, startDate, endDate,eventName, location,calendars,eventTypes, null);
			
			ret = parseEventDetailXML(xml);
		}catch(Exception e){
			log.error("Error while processing Featured Events, Error: {}", e.getMessage());
			log.debug(e.getMessage(), e);
		}
		
		return ret;
	}

	@Override
	public List<MCEventType> getMasterCalendarEventTypes() {
		List<MCEventType> ret = new ArrayList<>();
		
		try {
			String xml = soap.getEventTypes(mcUserName, mcPassword);
			if(!checkForErrors(xml) ){
				ret = parseEventTypesXML(xml);
			}else{
				log.error("An error was returned when getting EventTypes:{}", xml);
			}
			
		} catch (RemoteException e) {
			log.error("An error occurred pulling remote Event Types, Error: {}",e.getMessage());
			log.debug(e.getMessage(), e);
		}
		
		return ret;
	}

	private List<MCEventType> parseEventTypesXML(String xml) {
		List<MCEventType> ret = new ArrayList<>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(false);
		dbf.setValidating(false);
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			log.error("Error parsing MCEventType:{}, Error: {}",xml, e1.getMessage());
			log.debug(e1.getMessage(), e1);
		} 
		try {
			Document doc = db.newDocument();
			Node fragmentNode = db.parse(
				        new InputSource(new StringReader(xml)))
				        .getDocumentElement();
				    fragmentNode = doc.importNode(fragmentNode, true);
				    doc.appendChild(fragmentNode);
			NodeList entries = doc.getElementsByTagName("Data");
			for(int i=0;i<entries.getLength();i++){
				NodeList children = entries.item(i).getChildNodes();
				MCEventType e = new MCEventType();
				for(int j=0;j<children.getLength();j++){
					if(children.item(j).getNodeName() == "EventTypeID"){
						e.setEventTypeId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "Name"){
						e.setEventTypeLocationName(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "Color"){
						e.setEventTypeColor(children.item(j).getTextContent());
					}
				}
				ret.add(e);
			}
			
		} catch (SAXException e) {
			log.error("Error parsing response: {}, Error: {}", xml,e.getMessage());
			log.debug(e.getMessage(), e);
		} catch (IOException e) {
			log.error("Error parsing response: {}, Error: {}", xml,e.getMessage());
			log.debug(e.getMessage(), e);
		}
		return ret;
	}

	@Override
	public List<MCLocation> getMasterCalendarLocations() {
		List<MCLocation> ret = new ArrayList<>();
		
		try {
			String xml = soap.getLocations(mcUserName, mcPassword);
			if(!checkForErrors(xml)){
				ret = parseLocationsXML(xml);
			}else{
				log.error("An error was returned when getting Locations:{}", xml);
			}
		} catch (RemoteException e) {
			log.error("An error occurred pulling remote Locations {}",e.getMessage());
			log.debug(e.getMessage(), e);
		}
		
		
		return ret;
	}

	private List<MCLocation> parseLocationsXML(String xml) {
		List<MCLocation> ret = new ArrayList<>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(false);
		dbf.setValidating(false);
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			log.error("Error parsing MCLocation:{}, Error: {}",xml, e1.getMessage());
			log.debug(e1.getMessage(), e1);
		} 
		try {
			Document doc = db.newDocument();
			Node fragmentNode = db.parse(
				        new InputSource(new StringReader(xml)))
				        .getDocumentElement();
				    fragmentNode = doc.importNode(fragmentNode, true);
				    doc.appendChild(fragmentNode);
			NodeList entries = doc.getElementsByTagName("Data");
			for(int i=0;i<entries.getLength();i++){
				NodeList children = entries.item(i).getChildNodes();
				MCLocation e = new MCLocation();
				for(int j=0;j<children.getLength();j++){
					if(children.item(j).getNodeName() == "LocationID"){
						e.setLocationId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "Name"){
						e.setLocationName(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "Url"){
						e.setLocationUrl(children.item(j).getTextContent());
					}
				}
				ret.add(e);
			}
			
		} catch (SAXException e) {
			log.error("Error parsing response: {}, Error: {}", xml,e.getMessage());
			log.debug(e.getMessage(), e);
		} catch (IOException e) {
			log.error("Error parsing response: {}, Error: {}", xml,e.getMessage());
			log.debug(e.getMessage(), e);
		}
		return ret;
	}

	@Override
	public List<MCCalendar> getMasterCalendarCalendars() {
		List<MCCalendar> ret = new ArrayList<>();
		
		try {
			String xml = soap.getCalendars(mcUserName, mcPassword);
			if(!checkForErrors(xml)){
				List<MCCalendarEntry> entries = parseCalendarListXML(xml);
				for(MCCalendarEntry e : entries){
					xml = soap.getCalendar(mcUserName, mcPassword, e.getCalendarId());
					if(!checkForErrors(xml)){
						MCCalendar c = parseCalendarXML(xml);
						ret.add(c);
					}else{
						log.error("An error was returned when getting Calendar:{} : {}",e.getCalendarName(), xml);
					}
				}
			}else{
				log.error("An error was returned when getting Calendars:{}", xml);
			}
			
		} catch (RemoteException e) {
			log.error("An error occurred connecting to the Master Calendar API {}", e.getMessage());
			log.debug(e.getMessage(), e);
			return ret;
		}
		
		
		// TODO Auto-generated method stub
		return ret;
	}
	
	private MCCalendar parseCalendarXML(String xml){
		MCCalendar ret = new MCCalendar();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(false);
		dbf.setValidating(false);
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			log.error("Error parsing MCCalendar: {}, Error: {}",xml, e1.getMessage());
			log.debug(e1.getMessage(), e1);
		} 
		try {
			Document doc = db.newDocument();
			Node fragmentNode = db.parse(
				        new InputSource(new StringReader(xml)))
				        .getDocumentElement();
				    fragmentNode = doc.importNode(fragmentNode, true);
				    doc.appendChild(fragmentNode);
			NodeList entries = doc.getElementsByTagName("Data");
			for(int i=0;i<entries.getLength();i++){
				NodeList children = entries.item(i).getChildNodes();
				for(int j=0;j<children.getLength();j++){
					if(children.item(j).getNodeName() == "Name"){
						ret.setCalendarName(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "CalendarID"){
						ret.setCalendarId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "Description"){
						ret.setCalendarDescription(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "AdminName"){
						ret.setAdminName(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "AdminEmail"){
						ret.setAdminEmail(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "ApprovalEmail"){
						ret.setApprovalEmail(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "IsPrivate"){
						ret.setPrivateCalendar(Boolean.parseBoolean(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "IsActive"){
						ret.setActiveCalendar(Boolean.parseBoolean(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "ShowCancelledEvents"){
						ret.setShowCancelledEvents(Boolean.parseBoolean(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "DefaultViewID"){
						ret.setDefaultViewId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "CalendarFormatID"){
						ret.setCalendarFormatId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "GroupingID"){
						ret.setCalendarGroupingId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "ShowWeekends"){
						ret.setShowWeekends(Boolean.parseBoolean(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "StartWeekOn"){
						ret.setStartWeekOn(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "AllowPublicSubmission"){
						ret.setAllowPublicSubmission(Boolean.parseBoolean(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "ContactInfoPublic"){
						ret.setShowCalendarContactInfo(Boolean.parseBoolean(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "Subscription"){
						ret.setSubscription(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "ListTypeID"){
						ret.setListTypeId(Integer.parseInt(children.item(j).getTextContent()));
					}

				}
	
			}
			
		} catch (SAXException e) {
			log.error("Error parsing response: {}, Error: {}", xml,e.getMessage());
			log.debug(e.getMessage(), e);
		} catch (IOException e) {
			log.error("Error parsing response: {} Error: {}", xml,e.getMessage());
			log.debug(e.getMessage(), e);
		}
		
		return ret;
	}
	
	private List<MCCalendarEntry> parseCalendarListXML(String xml) {
		List<MCCalendarEntry> ret = new ArrayList<>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(false);
		dbf.setValidating(false);
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			log.error("Error parsing MCCalendars: {}, Error: {}",xml, e1.getMessage());
			log.debug(e1.getMessage(), e1);
		} 
		try {
			Document doc = db.newDocument();
			Node fragmentNode = db.parse(
				        new InputSource(new StringReader(xml)))
				        .getDocumentElement();
				    fragmentNode = doc.importNode(fragmentNode, true);
				    doc.appendChild(fragmentNode);
			NodeList entries = doc.getElementsByTagName("Data");
			for(int i=0;i<entries.getLength();i++){
				NodeList children = entries.item(i).getChildNodes();
				MCCalendarEntry e = new MCCalendarEntry();
				for(int j=0;j<children.getLength();j++){
					if(children.item(j).getNodeName() == "Name"){
						e.setCalendarName(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "CalendarID"){
						e.setCalendarId(Integer.parseInt(children.item(j).getTextContent()));
					}
				}
				ret.add(e);
			}
			
		} catch (SAXException e) {
			log.error("Error parsing response: {}, Error: {}", xml,e.getMessage());
			log.debug(e.getMessage(), e);
		} catch (IOException e) {
			log.error("Error parsing response: {}, Error: {}", xml,e.getMessage());
			log.debug(e.getMessage(), e);
		}
		return ret;
	}

	public List<MCGrouping>getMasterCalendarGroupings(){
		
		List<MCGrouping> ret = new ArrayList<>();
		
		String xml;
		try {
			xml = soap.getGroupings(mcUserName, mcPassword);
		
			if(!checkForErrors(xml)){
			
				ret = parseGroupingsXML(xml);
				
			}else{
				log.error("An error was returned when getting Groupings:{}", xml);
			}
			
		} catch (RemoteException e) {
			log.error("An unexpected error was returned by the remote server. Error: {}", e.getMessage());
			log.debug(e.getMessage(), e);
		}
		return ret;
	}

	private List<MCGrouping> parseGroupingsXML(String xml) {
		List<MCGrouping> ret = new ArrayList<>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(false);
		dbf.setValidating(false);
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			log.error("Error parsing MCGrouping: {}, Error: {}",xml, e1.getMessage());
			log.debug(e1.getMessage(), e1);
		} 
		try {
			Document doc = db.newDocument();
			Node fragmentNode = db.parse(
				        new InputSource(new StringReader(xml)))
				        .getDocumentElement();
				    fragmentNode = doc.importNode(fragmentNode, true);
				    doc.appendChild(fragmentNode);
			NodeList entries = doc.getElementsByTagName("Data");
			for(int i=0;i<entries.getLength();i++){
				NodeList children = entries.item(i).getChildNodes();
				MCGrouping e = new MCGrouping();
				for(int j=0;j<children.getLength();j++){
					if(children.item(j).getNodeName() == "GroupingID"){
						e.setGroupingId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "Name"){
						e.setName(children.item(j).getTextContent());
					}
				}
				ret.add(e);
			}
			
		} catch (SAXException e) {
			log.error("Error parsing response: {}, Error: {}", xml,e.getMessage());
			log.debug(e.getMessage(), e);
		} catch (IOException e) {
			log.error("Error parsing response: {}, Error: {}", xml,e.getMessage());
			log.debug(e.getMessage(), e);
		}
		return ret;
	}
	
}
