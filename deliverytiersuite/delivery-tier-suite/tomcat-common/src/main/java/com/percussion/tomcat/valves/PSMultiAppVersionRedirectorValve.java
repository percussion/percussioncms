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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.percussion.tomcat.valves;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletException;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A valve that performs redirection of requests for a specified context 
 * to a different  application context based upon the version of the
 * application. 
 * 
 * The mappings are controlled by the contents of the version-map.properties
 * file specified by the mappingFile attribute of the Valve.
 * 
 * @author natechadwick
 *
 */
public class PSMultiAppVersionRedirectorValve extends ValveBase implements Lifecycle {

	public static String PERC_VERSION_HEADER = "perc-version";
	
	private static final Logger log = LogManager.getLogger(PSMultiAppVersionRedirectorValve.class);

	//Contains a file system pointer to the mapping configuration file
	private String mappingFile;
	
	//Contains the mapping properties. 
	private Properties properties = new Properties();
	
	//When true routing logic is attempted, when false it is skipped.
	protected ThreadLocal<Boolean> pipelining = new ThreadLocal<>();
	private PSVersionRoutingTable routingTable = new PSVersionRoutingTable();
	
	boolean started;
	    
    public boolean isStarted()
    {
        return started;
    }

	/**
	 * Returns the mapping file for this release. 
	 * 
	 * @return the mappingFile
	 */
	public String getMappingFile() {
		return this.mappingFile;
	}

	/**
	 * Specifies the mapping file for this release. 
	 * 
	 * <Valve className="com.percussion.tomcat.valves.PSMultiAppVersionRedirectorValve" mappingFile="${catalina.base}/conf/perc/version-mappings.properties" />
	 * @param mappingFile the mappingFile to set
	 */
	public void setMappingFile(String mappingFile) {
		this.mappingFile = mappingFile;
	}

	@Override
	public void startInternal() throws LifecycleException {
	
		started = false;
		
		if(log.isDebugEnabled())
			log.debug("start");
		
		log.info("Starting Multi App Version Redirector Valve");
		
		if(mappingFile!=null){
			try {
				File file = new File(mappingFile);
				properties.load(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				log.info("Could not find the version Mapping file specified: " + mappingFile  +". Multi Version Routing is disabled.");
			} catch (IOException e) {
				log.info("Could not access the version Mapping file specified: " + mappingFile  + "." + e.getMessage() + ". Multi Version Routing is disabled.");
			}
			
			
			//Try to parse out the property file.
			try{
				Enumeration<?> e = properties.propertyNames();

				while (e.hasMoreElements()) {
				 
				 String context = (String) e.nextElement();
				 String [] map = properties.getProperty(context).split(",");
				 
				 routingTable.addServiceContextVersionMap(context, 
						 map[0], 
						 map[1]);
			    }
				
				//if we got this far then we have a valid routine table. 
				started = true;
				log.info("Routing Table initialized");
	
			}catch(Exception e){
				log.error("Unable to initialize routing tables.", e);
			}
			
		}
		started = true;
		if (getContainer()!=null)
		    setState(LifecycleState.STARTING);
	}


	/* (non-Javadoc)
	 * @see org.apache.catalina.valves.ValveBase#invoke(org.apache.catalina.connector.Request, org.apache.catalina.connector.Response)
	 */
	@Override
	public void invoke(Request request, Response response) throws IOException,
			ServletException {

		if(log.isDebugEnabled())
			log.debug("invoke");
		
		if (pipelining.get() == Boolean.TRUE) {
			   getNext().invoke(request, response);
			   pipelining.set(null);
			   return;
		 }
		
		//Only apply routing logic if the valve is properly initialized.
		if(started){
			pipelining.set(Boolean.TRUE);
			String context = routingTable.determineRoute(request.getContextPath(),
					request.getHeader(PERC_VERSION_HEADER));
			
			if(!context.startsWith("/"))
				context="/"+context;
			
			//Make sure we don't re-route if the context is the same as the target. 
			if(context != null && !context.equals(request.getContextPath())){
					
	            StringBuffer sbUrl = request.getRequestURL ();
	            String sQueryString = request.getQueryString ();

	            if (sQueryString != null)
	            {
	                sbUrl.append ("?");
	                sbUrl.append (sQueryString);
	            }
	            
	            String sUrl = sbUrl.toString().replace(request.getContextPath(), context);
	            
	            response.setStatus (Response.SC_MOVED_PERMANENTLY);
	            response.setHeader ("Location",
	            response.encodeRedirectURL (sUrl));
                return;  
//				//Submit the change to the pipeline for processing. 
//			        try {
//					request.getConnector().getProtocolHandler().getAdapter().service(
//								request.getCoyoteRequest(),
//						        response.getCoyoteResponse());			        
//			        } catch (Exception e) {
//						throw new RuntimeException();
//					}
         		
			}
			
		Valve nextValve = getNext();
		if(nextValve!=null)
			nextValve.invoke(request, response);	
			
		}
		
		//Make sure thread local is cleared.
		pipelining.set(null);
					
	}
	
    @Override
    public void stopInternal() throws LifecycleException
    {
        started=false;
        if (getContainer()!=null)
            setState(LifecycleState.STOPPING);
    }

}
