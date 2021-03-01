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

package com.percussion.delivery.integrations.ems;

import com.percussion.delivery.integrations.ems.model.Booking;
import com.percussion.delivery.integrations.ems.model.Building;
import com.percussion.delivery.integrations.ems.model.EventType;
import com.percussion.delivery.integrations.ems.model.GroupType;
import com.percussion.delivery.integrations.ems.model.Status;
import com.percussion.security.xml.PSSecureXMLUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import service.web.api.ems.dea.ArrayOfInt;
import service.web.api.ems.dea.Service;
import service.web.api.ems.dea.ServiceSoap;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class EMSSOAPEventService implements IPSEMSEventService {

	private List<GroupType>groupTypes;	
	private List<EventType>eventTypes;
	private List<Building>buildings;
	private List<Status> statuses;
	
	private Service svc;
	private ServiceSoap soap;
	private String userName; 
	private String password;
	private String endpoint;

	
	private static Log log = LogFactory.getLog(EMSSOAPEventService.class);
	
	public static final int STATUS_TYPE_BOOKEDSPACE=-14;
	public static final int STATUS_TYPE_INFOONLY=-11;
	public static final int STATUS_TYPE_WAIT=-13;
	public static final int STATUS_TYPE_CANCEL=-12;
	
	public EMSSOAPEventService(String userName, String password, String endpoint){
		try {
			if(password == null || userName == null || endpoint == null || endpoint.equals("")){
				return;
			}
			
			this.password = password;
			this.userName = userName;
			this.endpoint = endpoint;
			
			svc = new Service(new URL(this.endpoint));
			soap = svc.getServiceSoap();
			
		} catch (MalformedURLException e) {
			log.error("Unable to configure EMS API Soap Client.", e);
		}
	
		
	}
	
	
	public List<Status> getStatus(){
		if(statuses == null){
			String xml = soap.getStatuses(userName, password);
			
			if(checkForErrors(xml)){
				log.error("getStatues Service returned the following errors:" + xml);
			}else{
				statuses = parseStatusXML(xml);
			}
		}
		
		return statuses;
		
	}
	
	private List<Status> parseStatusXML(String xml) {
		List<Status> ret = new ArrayList<>();
		DocumentBuilderFactory dbf = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
				false);
		dbf.setNamespaceAware(false);
		dbf.setValidating(false);
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			log.error("Error parsing Statuses:" +xml, e1);
		} 
		try {
			Document doc = db.newDocument();
			Node fragmentNode = db.parse(
				        new InputSource(new StringReader(xml)))
				        .getDocumentElement();
				    fragmentNode = doc.importNode(fragmentNode, true);
				    doc.appendChild(fragmentNode);
			NodeList groups = doc.getElementsByTagName("Data");
			for(int i=0;i<groups.getLength();i++){
				NodeList children = groups.item(i).getChildNodes();
				Status e = new Status();
				for(int j=0;j<children.getLength();j++){
					if(children.item(j).getNodeName() == "Description"){
						e.setDescription(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "ID"){
						e.setId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "DisplayOnWeb"){
						e.setDisplayOnWeb(Boolean.parseBoolean(children.item(j).getTextContent()));
					}
				}
				ret.add(e);
			}
			
		} catch (SAXException e) {
			log.error("Error parsing response: " + xml,e);
		} catch (IOException e) {
			log.error("Error parsing response: " + xml,e);
		}
		return ret;
	}


	@Override
	public List<Booking> getBookings(PSBookingsQuery query) {
		
		List<Booking> ret= new ArrayList<>();
		ArrayOfInt buildings = null;
		ArrayOfInt eventTypes = null;
		ArrayOfInt groups = null;
		ArrayOfInt statuses = null;
		
		if(query.getBuildingIds() != null && !query.getBuildingIds().isEmpty()){
			buildings = new ArrayOfInt();
			buildings.getInt().addAll(query.getBuildingIds());
		}
		
		if(query.getEventTypes() != null && !query.getEventTypes().isEmpty()){
			eventTypes = new ArrayOfInt();
			eventTypes.getInt().addAll(query.getEventTypes());

		}
		
		if(query.getGroupTypes() != null && !query.getGroupTypes().isEmpty()){
			groups = new ArrayOfInt();
			groups.getInt().addAll(query.getGroupTypes());
		}

		FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd hh:mm:ss");
		Date date = null;
		try {
			date = format.parse(query.getStartDate());
		} catch (ParseException e) {
			log.error(String.format("Error processing start date: {0}",query.getStartDate()), e);
		}

		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);

		XMLGregorianCalendar startDate = null;
		try {
			startDate =  DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		} catch (DatatypeConfigurationException e1) {
			log.error(String.format("Error processing gregorian start date: {0}",query.getStartDate()), e1);
		}

		try {
			date = format.parse(query.getEndDate());
		} catch (ParseException e) {
			log.error(String.format("Error processing end date: {0}",query.getEndDate()), e);
		}
		cal.setTime(date);
	
		XMLGregorianCalendar endDate = null;
		try {
			endDate =  DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		} catch (DatatypeConfigurationException e) {
			log.error(String.format("Error processing gregorian end date: {0}",query.getEndDate()), e);
		}

		String xml = soap.getBookings(userName, password, startDate, endDate, buildings, statuses, eventTypes, groups,false);
		
		if(checkForErrors(xml)){
			log.error("Bookings service returned the following errors:" + xml);
			return ret;
		}
		
		ret = parseBookingXML(xml);
		
		return ret;
		
	}
	
	private List<Booking>parseBookingXML(String xml){
		List<Booking> ret = new ArrayList<>();
		DocumentBuilderFactory dbf = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
				false);
		dbf.setNamespaceAware(false);
		dbf.setValidating(false);
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			log.error("Error parsing Buildings:" + xml,e1);
		} 
		try {
			Document doc = db.newDocument();
			Node fragmentNode = db.parse(
				        new InputSource(new StringReader(xml)))
				        .getDocumentElement();
				    fragmentNode = doc.importNode(fragmentNode, true);
				    doc.appendChild(fragmentNode);
			NodeList bookings = doc.getElementsByTagName("Data");
			
			for(int i=0;i<bookings.getLength();i++){
				NodeList children = bookings.item(i).getChildNodes();
				Booking b = new Booking();

				for(int j=0;j<children.getLength();j++){
					if(children.item(j).getNodeName() == "BookingDate"){
						b.setBookingDate(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "StartBookingDate"){
						b.setStartBookingDate(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "RoomDescription"){
						b.setRoomDescription(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "TimeEventStart"){
						b.setTimeEventStart(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "TimeEventEnd"){
						b.setTimeEventEnd(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "GroupName"){
						b.setGroupName(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "EventName"){
						b.setEventName(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "SetupTypeDescription"){
						b.setSetupTypeDescription(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "SetupCount"){
						b.setSetupCount(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "ReservationID"){
						b.setReservationID(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "EventCoordinator"){
						b.setEventCoordinator(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "GroupID"){
						b.setGroupID(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "VIP"){
						b.setVip(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "VIPEvent"){
						b.setVipEvent(Boolean.parseBoolean(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "ClosedAllDay"){
						b.setClosedAllDay(Boolean.parseBoolean(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "OpenTime"){
						b.setOpenTime(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "CloseTime"){
						b.setCloseTime(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "GroupTypeDescription"){
						b.setGroupTypeDescription((children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "EventTypeDescription"){
						b.setEventTypeDescription(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "Contact"){
						b.setContact(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "AltContact"){
						b.setAltContact(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "BookingID"){
						b.setBookingId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "TimeBookingStart"){
						b.setTimeBookingStart(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "TimeBookingEnd"){
						b.setTimeBookingEnd(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "GMTStartTime"){
						b.setGmtStartTime(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "GMTEndTime"){
						b.setGmtEndTime(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "TimeZone"){
						b.setTimeZone(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "BuildingCode"){
						b.setBuildingCode(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "Building"){
						b.setBuilding(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "RoomCode"){
						b.setRoomCode(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "Room"){
						b.setRoom(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "RoomID"){
						b.setRoomId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "BuildingID"){
						b.setBuildingId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "RoomTypeID"){
						b.setRoomTypeId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "RoomType"){
						b.setRoomType(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "HVACZone"){
						b.setHvacZone(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "StatusID"){
						b.setStatusID(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "StatusTypeID"){
						b.setStatusTypeId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "EventTypeID"){
						b.setEventTypeId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "DateAdded"){
						b.setDateAdded(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "AddedBy"){
						b.setAddedBy(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "DateChanged"){
						b.setDateChanged(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "ChangedBy"){
						b.setChangedBy(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "ContactEmailAddress"){
						b.setContactEmailAddress(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "CheckedIn"){
						b.setCheckedIn(Boolean.parseBoolean(children.item(j).getTextContent()));
					}
				}
				//Filter events that are cancelled or are on hold.
				if(b.getStatusTypeId() != STATUS_TYPE_CANCEL && b.getStatusTypeId() != STATUS_TYPE_WAIT){
					String bookxml = soap.getBooking(this.userName, this.password,b.getBookingId());
					ret.add(b);
				}
			}
		
		} catch (SAXException e) {
			log.error("Error parsing bookings:", e);
		} catch (IOException e) {
			log.error("Error parsing bookings:", e);
		}
		return ret;
	}

	@Override
	public List<EventType> getEventTypes() {
		if(eventTypes == null){
			String xml = soap.getEventTypes(userName, password);
			
			if(checkForErrors(xml)){
				log.error("getEventTypes Service returned the following errors:" + xml);
			}else{
				eventTypes = parseEventTypeXML(xml);
			}
		}
		return eventTypes;
	}
	
	private boolean checkForErrors(String xml){
		if(xml.contains("<Errors>")){
			return true;
		}else{
			return false;
		}
		
	}

	@Override
	public List<Building> getBuildings() {
		if(buildings == null){
			String xml = soap.getBuildings(userName, password);
			
			if(checkForErrors(xml)){
				log.error("Buildings service returned the following errors:" + xml);
			}else{
				buildings = parseBuildingXML(xml);
			}
		}
		return buildings;
	}

	private List<Building> parseBuildingXML(String xml) {
		List<Building> ret = new ArrayList<>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(false);
		dbf.setValidating(false);
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			log.error("Error configuring XML parser for :" + xml,e1);
		} 
		try {
			 /* <Data>
			    <Description>Z (old) Loker Student Union</Description>
			    <BuildingCode>(old) LSU</BuildingCode>
			    <ID>1</ID>
			    <TimeZoneDescription>Pacific Time (US &amp; Canada); Tijuana</TimeZoneDescription>
			    <TimeZoneAbbreviation>PT</TimeZoneAbbreviation>
			    <CurrentLocalTime>2018-05-14T12:51:08.493</CurrentLocalTime>
			  </Data>*/
			Document doc = db.newDocument();
			Node fragmentNode = db.parse(
				        new InputSource(new StringReader(xml)))
				        .getDocumentElement();
				    fragmentNode = doc.importNode(fragmentNode, true);
				    doc.appendChild(fragmentNode);
			NodeList buildings = doc.getElementsByTagName("Data");
			for(int i=0;i<buildings.getLength();i++){
				NodeList children = buildings.item(i).getChildNodes();
				Building b = new Building();
				for(int j=0;j<children.getLength();j++){
					if(children.item(j).getNodeName() == "Description"){
						b.setDescription(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "ID"){
						b.setId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "BuildingCode"){
						b.setBuildingCode(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "CurrentLocalTime"){
						b.setCurrentLocalTime(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "TimeZoneDescription"){
						b.setTimeZoneDescription(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "TimeZoneAbbreviation"){
						b.setTimeZoneAbbreviation(children.item(j).getTextContent());
					}
				}
				ret.add(b);
			}
			
		} catch (SAXException e) {
			log.error("Error parsing response: " + xml,e);
		} catch (IOException e) {
			log.error("Error parsing response " + xml,e);
		}
		return ret;
	}

	@Override
	public List<GroupType> getGroupTypes() {
		if(groupTypes == null){
			String xml = soap.getGroupTypes(userName, password);
			if(checkForErrors(xml)){
				log.error("Group Types service returned the following errors:" + xml);
			}else{
				groupTypes = parseGroupXML(xml);
			}
		}
		return groupTypes;
	}

	private List<GroupType> parseGroupXML(String xml) {
		List<GroupType> ret = new ArrayList<>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(false);
		dbf.setValidating(false);
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			log.error("Error parsiing Group Types:" + xml, e1);
		} 
		try {
			Document doc = db.newDocument();
			Node fragmentNode = db.parse(
				        new InputSource(new StringReader(xml)))
				        .getDocumentElement();
				    fragmentNode = doc.importNode(fragmentNode, true);
				    doc.appendChild(fragmentNode);
			NodeList groups = doc.getElementsByTagName("Data");
			for(int i=0;i<groups.getLength();i++){
				NodeList children = groups.item(i).getChildNodes();
				GroupType g = new GroupType();
				for(int j=0;j<children.getLength();j++){
					if(children.item(j).getNodeName() == "Description"){
						g.setDescription(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "ID"){
						g.setId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "AvailableOnWeb"){
						g.setAvailableOnWeb(Boolean.parseBoolean(children.item(j).getTextContent()));
					}
				}
				ret.add(g);
			}
			
		} catch (SAXException e) {
			log.error("Error parsing response: " + xml,e);
		} catch (IOException e) {
			log.error("Error parsing response " + xml,e);
		}
		return ret;
	}

	private List<EventType> parseEventTypeXML(String xml) {
		List<EventType> ret = new ArrayList<>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(false);
		dbf.setValidating(false);
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			log.error("Error parsing Event Types:" +xml, e1);
		} 
		try {
			Document doc = db.newDocument();
			Node fragmentNode = db.parse(
				        new InputSource(new StringReader(xml)))
				        .getDocumentElement();
				    fragmentNode = doc.importNode(fragmentNode, true);
				    doc.appendChild(fragmentNode);
			NodeList groups = doc.getElementsByTagName("Data");
			for(int i=0;i<groups.getLength();i++){
				NodeList children = groups.item(i).getChildNodes();
				EventType e = new EventType();
				for(int j=0;j<children.getLength();j++){
					if(children.item(j).getNodeName() == "Description"){
						e.setDescription(children.item(j).getTextContent());
					}else if(children.item(j).getNodeName() == "ID"){
						e.setId(Integer.parseInt(children.item(j).getTextContent()));
					}else if(children.item(j).getNodeName() == "DisplayOnWeb"){
						e.setDisplayOnWeb(Boolean.parseBoolean(children.item(j).getTextContent()));
					}
				}
				ret.add(e);
			}
			
		} catch (SAXException e) {
			log.error("Error parsing response: " + xml,e);
		} catch (IOException e) {
			log.error("Error parsing response: " + xml,e);
		}
		return ret;
	}
	
	

}
