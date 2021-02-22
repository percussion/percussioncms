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
