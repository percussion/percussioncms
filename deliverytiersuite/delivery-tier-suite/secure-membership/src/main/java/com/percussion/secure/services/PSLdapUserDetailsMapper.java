
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

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.security.xml.PSXmlSecurityOptions;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
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
import java.util.Collection;
import java.util.List;

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

       return new User(
               originalUser.getUsername(), 
               originalUser.getPassword() != null? originalUser.getPassword():"", 
               originalUser.isEnabled(), 
               originalUser.isAccountNonExpired(), 
               originalUser.isCredentialsNonExpired(), 
               originalUser.isAccountNonLocked(), 
               allAuthorities );

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
                new PSXmlSecurityOptions(
                        true,
                        true,
                        true,
                        false,
                        true,
                        false
                )
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
