/*
 *     Percussion CMS
 *     Copyright (C) Percussion Software, Inc.  1999-2020
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *      Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.percussion.tomcat.valves;

import java.util.HashMap;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides a basic data structure for indexing the routine table 
 * used by the version redirector valve. 
 * 
 * @author natechadwick
 *
 */
public class PSVersionRoutingTable {
	
	private HashMap<String, HashMap<String,String>> serviceContexts;
	private static final Logger log = LogManager.getLogger(PSVersionRoutingTable.class);
	/**
	 * @return the serviceContexts
	 */
	public HashMap<String, HashMap<String,String>> getServiceContexts() {
		return serviceContexts;
	}

	/**
	 * @param serviceContexts the serviceContexts to set
	 */
	public void setServiceContexts(HashMap<String, HashMap<String,String>> serviceContexts) {
		this.serviceContexts = serviceContexts;
	}

	public void addServiceContext(String context){
		if(serviceContexts == null)
			serviceContexts = new HashMap<>();
		if(!serviceContexts.containsKey(context)){
			serviceContexts.put(context, new HashMap<>());
		}
	}
	
	public void addServiceContextVersionMap(String context, String version, String dest){

		//Make sure the context is added
		addServiceContext(context);
		
		HashMap<String,String> routes = serviceContexts.get(context);
		
		routes.put(version, dest);
	
		serviceContexts.put(context,routes);
	}
	
	/***
	 * Attempts to find a route for the specified context and version. 
	 * @param context
	 * @param requestVer
	 * @return a context
	 */
	public String determineRoute(String context, String requestVer){
		
		if(context == null)
			context= "";
		
		//Default to the original request context
		String ret = context;
		
		try{
		HashMap<String,String> routes = serviceContexts.get(context);
		
		
		if(routes.containsKey(requestVer)){
			ret = routes.get(requestVer);
		}else if (requestVer == null){
			if(routes.containsKey(""))
				ret = routes.get("");
		}else if(routes.containsKey("<"+requestVer)){
			ret = routes.get("<"+requestVer);
		}
		}catch(Exception e){
			log.error(String.format("Unable to determine route for Context: %s and Version: %s",context,requestVer));
		}
		return ret;
	}

}
