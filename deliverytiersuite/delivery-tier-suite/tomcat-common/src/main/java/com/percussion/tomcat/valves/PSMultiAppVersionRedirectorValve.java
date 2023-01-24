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

import com.percussion.error.PSExceptionUtils;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import static javax.servlet.http.HttpServletResponse.SC_MOVED_PERMANENTLY;

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
	public synchronized void startInternal() throws LifecycleException {
	
		started = false;
		
		log.debug("start");
		
		log.info("Starting Multi App Version Redirector Valve");
		
		if(mappingFile!=null){
			try {
				File file = new File(mappingFile);
				try(FileInputStream fis = new FileInputStream(file)) {
					properties.load(fis);
				}
			} catch (FileNotFoundException e) {
				log.warn("Could not find the version Mapping file specified: {} Multi Version Routing is disabled. Error: {}",
						mappingFile,
						PSExceptionUtils.getMessageForLog(e));
			} catch (IOException e) {
				log.warn("Could not access the version Mapping file specified: {}. Error: {}. Multi Version Routing is disabled.",
						mappingFile,
						PSExceptionUtils.getMessageForLog(e));
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

		log.debug("invoke");
		
		if (pipelining.get() == Boolean.TRUE) {
			   getNext().invoke(request, response);
			   pipelining.remove();
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
			if(!context.equals(request.getContextPath())){
					
	            StringBuffer sbUrl = request.getRequestURL ();
	            String sQueryString = request.getQueryString ();

	            if (sQueryString != null)
	            {
	                sbUrl.append ("?");
	                sbUrl.append (sQueryString);
	            }
	            
	            String sUrl = sbUrl.toString().replace(request.getContextPath(), context);
	            
	            response.setStatus (SC_MOVED_PERMANENTLY);
	            response.setHeader ("Location",
	            response.encodeRedirectURL (sUrl));
                return;
			}
			
		Valve nextValve = getNext();
		if(nextValve!=null)
			nextValve.invoke(request, response);	
			
		}
		
		//Make sure thread local is cleared.
		pipelining.remove();
					
	}
	
    @Override
    public synchronized void stopInternal() throws LifecycleException
    {
        started=false;
        if (getContainer()!=null)
            setState(LifecycleState.STOPPING);
    }

}
