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

import com.percussion.delivery.integrations.ems.model.Booking;
import com.percussion.delivery.integrations.ems.model.Building;
import com.percussion.delivery.integrations.ems.model.EventType;
import com.percussion.delivery.integrations.ems.model.GroupType;
import com.percussion.delivery.integrations.ems.model.MCCalendar;
import com.percussion.delivery.integrations.ems.model.MCEventDetail;
import com.percussion.delivery.integrations.ems.model.MCEventType;
import com.percussion.delivery.integrations.ems.model.MCGrouping;
import com.percussion.delivery.integrations.ems.model.MCLocation;
import com.percussion.delivery.integrations.ems.model.Status;
import com.percussion.delivery.utils.lookup.PSLookup;
import com.percussion.delivery.utils.lookup.PSXEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 *
 * Provides a lightweight wrapper proxy for the EMS SOAP API.
 *
 *
 */
@Path("/integrations/ems")
@Component
public class PSEmsProxyRestService{

    @Autowired
	private EMSSOAPEventService service;
    @Autowired
	private EMSMasterCalendarSoapEventService mcService;
	
	public EMSMasterCalendarSoapEventService getMcService() {
		return mcService;
	}

	@Autowired
	public void setMcService(EMSMasterCalendarSoapEventService mcService) {
		this.mcService = mcService;
	}

	public EMSSOAPEventService getService() {
		return service;
	}

	@Autowired
	public void setService(EMSSOAPEventService service) {
		this.service = service;
	}


	@GET
    @Path("/groups")
    @Produces(MediaType.APPLICATION_XML)
	public PSLookup getGroups() {
		List<GroupType> groups = service.getGroupTypes();
		PSLookup lookup = new PSLookup();
		
		for(GroupType g: groups){
			if(g.isAvailableOnWeb()){
				lookup.add(new PSXEntry(String.valueOf(g.getId()), g.getDescription()));
			}
		}
		
		return lookup;
	}


	@GET
    @Path("/buildings")
    @Produces(MediaType.APPLICATION_XML)
	public PSLookup getBuildings() {
		List<Building> buildings = service.getBuildings();
		PSLookup lookup = new PSLookup();
		
		for(Building b: buildings){
			lookup.add(new PSXEntry(String.valueOf(b.getId()), b.getDescription()));
		}
		return lookup;
	}

	
	@GET
    @Path("/eventtypes")
    @Produces(MediaType.APPLICATION_XML)
	public PSLookup getEventTypes() {
			List<EventType> events = service.getEventTypes();
			PSLookup lookup = new PSLookup();
			
			for(EventType e: events){
				if(e.isDisplayOnWeb())
					lookup.add(new PSXEntry(String.valueOf(e.getId()), e.getDescription()));
			}
			return lookup;
	}
	
	@GET
    @Path("/statuses")
    @Produces(MediaType.APPLICATION_XML)
	public PSLookup getStatuses() {
			List<Status> statuses = service.getStatus();
			PSLookup lookup = new PSLookup();
			
			for(Status e: statuses){
				if(e.isDisplayOnWeb())
					lookup.add(new PSXEntry(String.valueOf(e.getId()), e.getDescription()));
			}
			return lookup;
	}

	@POST
    @Path("/bookings")
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<Booking> getBookings(PSBookingsQuery query) {
		return service.getBookings(query);
	}
	
	public PSEmsProxyRestService(){}
	
	@POST
	@Path("/mc/featuredevents")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<MCEventDetail> getFeaturedEvents(PSFeaturedEventsQuery query){
		return mcService.getMasterCalendarFeaturedEvents(query);
	}

	@POST
	@Path("/mc/events")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<MCEventDetail> getEvents(PSEventQuery query){
		return mcService.getMasterCalendarEvents(query);
	}
	
	@GET
    @Path("/mc/locations")
    @Produces(MediaType.APPLICATION_XML)
	public PSLookup getMCLocations(){
		PSLookup lookup = new PSLookup();
		List<MCLocation> types =mcService.getMasterCalendarLocations();
		for(MCLocation t: types){
			lookup.add(new PSXEntry(String.valueOf(t.getLocationId()), t.getLocationName()));
		}
		return lookup;
	}
	
	@GET
    @Path("/mc/groupings")
    @Produces(MediaType.APPLICATION_XML)
	public PSLookup getMCGroupings(){
		PSLookup lookup = new PSLookup();
		List<MCGrouping> types =mcService.getMasterCalendarGroupings();
		for(MCGrouping t: types){
			lookup.add(new PSXEntry(String.valueOf(t.getGroupingId()), t.getName()));
		}
		return lookup;
	}
	
	@GET
    @Path("/mc/eventtypes")
    @Produces(MediaType.APPLICATION_XML)
	public PSLookup getMCEventTypes(){
		PSLookup lookup = new PSLookup();
		
		List<MCEventType> types = mcService.getMasterCalendarEventTypes();
		for(MCEventType t: types){
			lookup.add(new PSXEntry(String.valueOf(t.getEventTypeId()), t.getEventTypeLocationName()));
		}
		return lookup;
	}
	
	@GET
    @Path("/mc/calendars")
    @Produces(MediaType.APPLICATION_XML)
	public PSLookup getMCCalendars(){
		PSLookup lookup = new PSLookup();
		
		List<MCCalendar> cals = mcService.getMasterCalendarCalendars();
		for(MCCalendar c: cals){
			if(c.getPrivateCalendar() == false && c.getActiveCalendar()  == true)
				lookup.add(new PSXEntry(String.valueOf(c.getCalendarId()), c.getCalendarName()));
		}
		return lookup;
	}
	
}
