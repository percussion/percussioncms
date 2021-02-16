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

package com.percussion.secure.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PSMembershipAuthUtils {
	
	public static List<String> getAccessGroupsFromXML(String accessGroupFileName) {

		List<String> groups = new ArrayList<>();
       String accessString;
       
       WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
       ServletContext ctx = context.getServletContext();
       String filePath = ctx.getRealPath(accessGroupFileName);
       
       try
       {
	    
	        File accessGroupFile = new File(filePath);
	        
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
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
	       System.out.println("FileNotFoundException in PSLdapUserDetailsMapper..... " + e);
	   }
	   catch (ParserConfigurationException e)
	   {
	       System.out.println("ParserConfigurationException in PSLdapUserDetailsMapper..... " + e);
	   }
       catch (IOException e)
       {
           System.out.println("IOException in PSLdapUserDetailsMapper..... " + e);
       }
       catch (SAXException e)
       {
           System.out.println("SAXException in PSLdapUserDetailsMapper..... " + e);
       }
       
       return groups;
   }
}
