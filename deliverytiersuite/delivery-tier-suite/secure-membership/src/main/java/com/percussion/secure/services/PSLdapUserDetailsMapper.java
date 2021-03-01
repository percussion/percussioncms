
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collection;

import com.percussion.security.xml.PSSecureXMLUtils;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
* Class to handle User Details Mapping for authorization.
* 
* @author Shweta Patel
*
*/
public class PSLdapUserDetailsMapper extends LdapUserDetailsMapper
{
   private static final String ROLE_TEST = "role_test";
   private static final String ROLE_ADMIN = "Domain Admins";
   private String accessGroupFileName;
   
@Override
   public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authority) 
   {
       List<String> groups = getAccessGroupsFromXML();
       UserDetails originalUser = super.mapUserFromContext( ctx, username, authority );

       originalUser.getAuthorities();

       List<SimpleGrantedAuthority> allAuthorities = new ArrayList<>();

       for (GrantedAuthority auth : authority) {
           if (auth != null && !auth.getAuthority().isEmpty()) {
               if(groups != null && !groups.isEmpty() && groups.contains("'"+(auth.getAuthority()).toUpperCase()+"'"))
                  allAuthorities.add((SimpleGrantedAuthority)auth);
            }
       }

       User newUser = 
               new User( 
               originalUser.getUsername(), 
               originalUser.getPassword() != null? originalUser.getPassword():"", 
               originalUser.isEnabled(), 
               originalUser.isAccountNonExpired(), 
               originalUser.isCredentialsNonExpired(), 
               originalUser.isAccountNonLocked(), 
               allAuthorities );

               return newUser;
   }
   
   public List<String> getAccessGroupsFromXML() {
       
       List<String> groups = new ArrayList<>();
       String accessString;
       
       WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
       ServletContext ctx = context.getServletContext();
       String filePath = ctx.getRealPath(accessGroupFileName);
       
       try
    {
        File accessGroupFile = new File(filePath);
        
        DocumentBuilderFactory dbFactory = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
                false
        );
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(accessGroupFile);
        
      // String accessString =  doc.getDocumentElement().getAttribute("access");
        Element root = doc.getDocumentElement();

        NodeList nodeList = root.getElementsByTagName("security:intercept-url");
       
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
   
   public String getAccessGroupFileName()
    {
        return accessGroupFileName;
    }
    
    public void setAccessGroupFileName(String accessGroupFileName)
    {
        this.accessGroupFileName = accessGroupFileName;
    }
}
