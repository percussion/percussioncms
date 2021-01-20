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
package com.percussion.dashboardmanagement.service.impl;

import com.percussion.dashboardmanagement.data.PSDashboard;
import com.percussion.dashboardmanagement.data.PSDashboardConfiguration;
import com.percussion.dashboardmanagement.data.PSGadget;
import com.percussion.dashboardmanagement.service.IPSDashboardDataService;
import com.percussion.dashboardmanagement.service.IPSDashboardService;
import com.percussion.share.service.IPSDataService.DataServiceNotFoundException;
import com.percussion.webservices.PSWebserviceUtils;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/dashboard")
@Component("dashboardService")
public class PSDashboardService implements IPSDashboardService {

	private IPSDashboardDataService dashboardDataService;	
	private static final Log log = LogFactory.getLog(PSDashboardService.class);
	
	@Autowired
	public PSDashboardService(IPSDashboardDataService dashboardDataService)
	{
		super();
		this.dashboardDataService = dashboardDataService;
	}

	@GET
	@Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public PSDashboard load() throws PSDashboardNotFoundException
	{

		String user = getUserName();
		if (log.isTraceEnabled())
			log.trace("Loading dashboard: " + user);
		PSDashboard dashboard;
		try {
			dashboard = dashboardDataService.find(user);
		} catch (DataServiceNotFoundException e) {
			log.debug("Creating default dashboard for user: " + user);
			dashboard = createDefaultDashboard(user);
		}
		return dashboard;
	}
	
	private String getUserName()
	{
		return PSWebserviceUtils.getUserName();
	}

	@POST
	@Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public PSDashboard save(PSDashboard dashboard)
			throws PSDashboardNotFoundException, PSDashboardServiceException
	{
		String user = getUserName();
		if (log.isTraceEnabled())
			log.trace("Saving dashboard for user: " + user);
		dashboard.setId(user);
		return dashboardDataService.save(dashboard);
	}
	
	
	private PSDashboard createDefaultDashboard(String user)
	{
		PSDashboardConfiguration config = new PSDashboardConfiguration();
		List<Boolean> displays = new ArrayList<Boolean>();
		for(int i=0; i<this.defaultDashboardColumnConfig.length; i++)
			displays.add(this.defaultDashboardColumnConfig[i]);
//		config.setColumnsDisplayed(displays);
		
		PSDashboard dashboard = new PSDashboard();
		dashboard.setGadgets(this.createGadgetList(this.alexGadgetUrls));
		dashboard.setDashboardConfiguration(config);
		dashboard.setId(user);
		return dashboard;
	}
	
	private ArrayList<PSGadget> createGadgetList(String[] urlList)
	{
		ArrayList<PSGadget> list = new ArrayList<PSGadget>(urlList.length);
		for(int i=0; i<urlList.length; i++)
		{
			String url = urlList[i];
			PSGadget gadget = new PSGadget();
			String name = url.substring(url.lastIndexOf('/')+1, url.lastIndexOf('.'));
			String firstLetter = name.substring(0,1);  // Get first letter
			String remainder   = name.substring(1);	// Get remainder of word.
			String capitalized = firstLetter.toUpperCase() + remainder.toLowerCase();
//	 		gadget.setName(capitalized);
			gadget.setUrl(url);
			gadget.setInstanceId(Integer.parseInt(this.alexGadgetIds[i]));
			gadget.setCol(this.alexGadgetLayout[i][0]);
			gadget.setRow(this.alexGadgetLayout[i][1]);
//			gadget.setId(this.alexGadgetIds[i]);
//			gadget.setXPosition(this.alexGadgetLayout[i][0]);
//			gadget.setYPosition(this.alexGadgetLayout[i][1]);
			list.add(gadget);
		}
		return list;
	}
	String[] allGadgetUrls =
	{
		"http://annunziato.org/gadgets/inbox.xml",
		"http://www.google.com/ig/modules/horoscope.xml",
		"http://www.labpixies.com/campaigns/todo/todo.xml",
		"http://www.labpixies.com/campaigns/weather/weather.xml",
		"http://www.labpixies.com/campaigns/calendar/calendar.xml",
		"http://www.labpixies.com/campaigns/wiki/wiki.xml",
		"http://localhost:9982/shindig/gadgets/hello_world.xml"
	};

	String[] alexGadgetUrls =
	{
			"http://www.labpixies.com/campaigns/todo/todo.xml",
			"http://www.labpixies.com/campaigns/wiki/wiki.xml",
			"http://www.labpixies.com/campaigns/weather/weather.xml",
			"http://www.labpixies.com/campaigns/calendar/calendar.xml",
			"http://www.google.com/ig/modules/horoscope.xml"
	};
	int[][] alexGadgetLayout = {{0,0},{0,1},{0,2},{1,0},{1,1}};
	String[] alexGadgetIds = {"123","234","345","456","567"};

	String[] bobGadgetUrls =
	{
			"http://www.labpixies.com/campaigns/weather/weather.xml",
			"http://www.labpixies.com/campaigns/calendar/calendar.xml",
			"http://www.labpixies.com/campaigns/wiki/wiki.xml",
	};
	int[][] bobGadgetLayout = {{0,0},{1,0},{1,1}};

	Boolean[] defaultDashboardColumnConfig = {new Boolean(true), new Boolean(true), new Boolean(false)};
	
	List<PSGadget> alexGadgets = new ArrayList<PSGadget>();
	List<PSGadget> bobGadgets = new ArrayList<PSGadget>();
}