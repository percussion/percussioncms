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
