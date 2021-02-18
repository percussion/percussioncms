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

package com.percussion.monitor.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.TreeMap;


@Path("/monitor")
@Component("monitorService")
@Lazy
public class PSMonitorService {
	
	private static Map<String, PSMonitor> monitors = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	
	private static PSMonitorDesignators designatorNames = new PSMonitorDesignators();
	
	public static IPSMonitor registerMonitor(String monitorDesignation, String name)
	{
		
		if ((monitorDesignation == null) ||  (monitorDesignation.toLowerCase().equals("all")))
		{
			throw new IllegalArgumentException("Illegal monitor designator");
		}
		
		PSMonitor monitor = null;
		if (monitors.containsKey(monitorDesignation))
		{
			return monitors.get(monitorDesignation);
		}
		monitor = new PSMonitor();
		monitor.setStat("name", name);
		monitor.setStat("designator", monitorDesignation);
		monitors.put(monitorDesignation, monitor);
		designatorNames.designator.add(monitorDesignation);
		monitor.setMessage("No information available");
		return monitor;
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/list")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public static PSMonitorDesignators getMonitorDesignators()
	{
	    return designatorNames;
	}
	
	@GET
	@Path("/{monitorDesignator}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public static IPSMonitor getMonitor(@PathParam("monitorDesignator") String monitorDesignator)
	{
		if (!monitors.containsKey(monitorDesignator))
		{
			registerMonitor(monitorDesignator, "No Name Available");
		}
		return monitors.get(monitorDesignator);
	}
	
	@GET
	@Path("/all")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public static PSMonitorList getMonitors()
	{
		PSMonitorList monitorList = new PSMonitorList();
		monitorList.addEntriesToList(monitors);
		return monitorList;
		
	}
	
	
}
