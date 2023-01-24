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

package com.percussion.secure.services;

import com.percussion.error.PSExceptionUtils;
import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.security.xml.PSXmlSecurityOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PSMembershipAuthUtils {

	private static final Logger log = LogManager.getLogger(PSMembershipAuthUtils.class);

	public static List<String> getAccessGroupsFromXML(String accessGroupFileName) {

		List<String> groups = new ArrayList<>();
       String accessString;
       
       WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
       ServletContext ctx = context.getServletContext();
       String filePath = ctx.getRealPath(accessGroupFileName);
       
       try
       {
	    
	        File accessGroupFile = new File(filePath);
	        
	        DocumentBuilderFactory dbFactory = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
					new PSXmlSecurityOptions(
							true,
							true,
							true,
							false,
							true,
							false
					));
	        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        Document doc = dBuilder.parse(accessGroupFile);
	        
	      // String accessString =  doc.getDocumentElement().getAttribute("access");
	       NodeList nodeList = doc.getElementsByTagName("intercept-url");
	       
	       for (int temp = 0; temp < nodeList.getLength(); temp++) {
	           Node node = nodeList.item(temp);
	           if(!((Element) node).getAttribute("access").isEmpty() && ((Element) node).getAttribute("access") != null) {
	               accessString = ((Element) node).getAttribute("access");
	               groups.addAll(Arrays.asList(accessString.split("\\s*,\\s*")));
	           }
	       }
	   }
	   catch (FileNotFoundException e)
	   {
	       log.error("FileNotFoundException in PSLdapUserDetailsMapper..... {}" ,PSExceptionUtils.getMessageForLog(e));
	       log.debug(PSExceptionUtils.getDebugMessageForLog(e));
	   }
	   catch (ParserConfigurationException e)
	   {
	      log.error("ParserConfigurationException in PSLdapUserDetailsMapper..... {}" ,PSExceptionUtils.getMessageForLog(e));
	      log.debug(PSExceptionUtils.getDebugMessageForLog(e));
	   }
       catch (IOException e)
       {
           log.error("IOException in PSLdapUserDetailsMapper..... {}" ,PSExceptionUtils.getMessageForLog(e));
           log.debug(PSExceptionUtils.getDebugMessageForLog(e));
       }
       catch (SAXException e)
       {
       	log.error("SAXException in PSLdapUserDetailsMapper..... {}" ,PSExceptionUtils.getMessageForLog(e));
       	log.debug(PSExceptionUtils.getDebugMessageForLog(e));
       }
       
       return groups;
   }
}
