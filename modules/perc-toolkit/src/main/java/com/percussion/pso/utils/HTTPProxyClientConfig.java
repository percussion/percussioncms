/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.utils;

import com.percussion.server.PSServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

/***
 * Contains the basic proxy client configuration
 * 
 * This file is read from rxconfig/Server/clientproxy.properties
 * proxyserver=
 * proxyport=
 * 
 * @author natechadwick
 *
 */
public class HTTPProxyClientConfig {

	 private static final Logger log = LogManager.getLogger(HTTPProxyClientConfig.class);
	   
	private String proxyServer;
	private String proxyPort;
	
	
	public void setProxyServer(String proxyServer) {
		this.proxyServer = proxyServer;
	}
	public String getProxyServer() {
		return proxyServer;
	}
	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}
	public String getProxyPort() {
		return proxyPort;
	}

	/***
	 * Loads the properties file and initializes the properties.  This could be made more efficient by
	 * moving it out of the constructor but this way the file can be changed without restarting the server.
	 */
	public HTTPProxyClientConfig(){
		 String propFile = PSServer.getRxFile(PSServer.BASE_CONFIG_DIR + "/Server/clientproxy.properties");
		 Properties props = new Properties();
		try {
				props.load(new FileInputStream(propFile));
		
				if(props.containsKey("proxyserver")){
					this.proxyServer = props.getProperty("proxyserver").trim();
				}else{
					this.proxyServer = "";
				}
				
				if(props.containsKey("proxyport")){
					this.proxyPort = props.getProperty("proxyport").trim();
				}else{
					this.proxyPort = "";
				}
				
		} catch (FileNotFoundException e) {
				log.debug(PSServer.BASE_CONFIG_DIR + "/Server/clientproxy.properties Configuration file not found.");
			} catch (Exception e) {
				log.error(e.getMessage());
				log.debug(e.getMessage(), e);
			}
	}
	
}
