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

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author natechadwick
 * 
 *
 */
public class PSMultiAppVersionRedirectorValveTest {

	private static String PERC_VERSION_HEADER="perc-version";
	private static String TEST_SERVICE = "perc-comments-services";
	
	private Request getTestRequest(){
		Connector connector = new Connector();


		Request ret = connector.createRequest();

		org.apache.coyote.Request cr = new org.apache.coyote.Request();

		cr.getMimeHeaders().addValue(PERC_VERSION_HEADER).setString("2.9.0");
		ret.setCoyoteRequest(cr);
		
		ret.setRemoteAddr("10.10.10.10");
		ret.setRemoteHost("remote-origin");
		
		return ret;
	}
	
	private void validateTestRequest(Request r){
		Assert.assertEquals("Remote address was changed by valve.", r.getRemoteAddr(), "10.10.10.10");
		Assert.assertEquals("Remote origin was changed by valve", r.getRemoteHost(), "remote-origin");
		Assert.assertEquals("Version was changed by valve", r.getHeader(PERC_VERSION_HEADER), "2.9.0");
	}
	
	/***
	 * Make sure the valve doesn't crash if the properties file is missing. 
	 * 
	 * @throws IOException
	 * @throws ServletException
	 */
	@Test
	public void testNoPropertiesFile() throws IOException, ServletException{
		PSMultiAppVersionRedirectorValve valve = new PSMultiAppVersionRedirectorValve();
		Request req = getTestRequest();
		
		valve.setMappingFile(null);
		req.setPathInfo(TEST_SERVICE);
		valve.invoke(req, null);
		validateTestRequest(req);
	}
	
	// Digital Clarity Group
	/***
	 * Make sure the sample properties File Loads and Parses OK and that the version is
	 * rewritten to the sample context.
	 * @throws IOException
	 * @throws ServletException
	 * @throws URISyntaxException
	 * @throws LifecycleException 
	 */
	@Ignore
	@Test 
	public void testWithPropertiesFile() throws IOException, ServletException, URISyntaxException, LifecycleException{
		PSMultiAppVersionRedirectorValve valve = new PSMultiAppVersionRedirectorValve();
		Request req = getTestRequest();
		
		URL filePath = this.getClass().getResource("mapping.properties");
		String file = new File(filePath.toURI()).getCanonicalPath();
	
		valve.setMappingFile(file);
		Assert.assertEquals("Mapping file name not set correctly", file, valve.getMappingFile());
		
		valve.startInternal();
		
		req.setPathInfo(TEST_SERVICE);
		
		try{
		valve.invoke(req, null);
		}catch (RuntimeException e){ /*this is expected due to no connector.*/}
		validateTestRequest(req);
		
	}
	
	@Ignore
	@Test
	public void testWithBadPropertiesFile() throws IOException, URISyntaxException, ServletException, LifecycleException{
	
		PSMultiAppVersionRedirectorValve valve = new PSMultiAppVersionRedirectorValve();
		Request req = getTestRequest();
		
		URL filePath = this.getClass().getResource("bad-mapping-1.properties");
		String file = new File(filePath.toURI()).getCanonicalPath();

		valve.setMappingFile(file);
		Assert.assertEquals("Mapping file name not set correctly", file, valve.getMappingFile());
		
		valve.startInternal();
		
		req.setPathInfo(TEST_SERVICE);
		
		try{
			valve.invoke(req, null);
			}catch (RuntimeException e){ /*this is expected due to no connector.*/}
			validateTestRequest(req);
		
	}
	
	
}
