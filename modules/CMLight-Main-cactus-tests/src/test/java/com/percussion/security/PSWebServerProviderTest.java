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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.security;

import com.percussion.design.objectstore.PSSecurityProviderInstance;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.server.IPSCgiVariables;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.security.PSServletRequestWrapper;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.utils.request.PSRequestInfo;

import java.util.Iterator;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Test the web server security provider.  Assumes one is configured.
 * Note, nightly integration build automatically creates a web security provider
 * for this test.
 */
@Category(IntegrationTest.class)
public class PSWebServerProviderTest extends ServletTestCase
{
   /**
    * Test authentication.
    * 
    * @throws Exception if there are any errors.
    */
   @SuppressWarnings(value={"unchecked"})
   public void testAuthentication() throws Exception
   {
      PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(
         PSRequestInfo.KEY_PSREQUEST);
      
      // this should create a mock servlet request so we can fudge the headers
      PSRequest newReq = req.cloneRequest();
      
      MockHttpServletRequest mockReq = null;
      HttpServletRequest servletReq = newReq.getServletRequest();
      if (!(servletReq instanceof MockHttpServletRequest))
      {
         if (servletReq instanceof PSServletRequestWrapper)
         {
            PSServletRequestWrapper psServletReq = 
               (PSServletRequestWrapper) servletReq;
            servletReq = (HttpServletRequest) psServletReq.getRequest();
            if (servletReq instanceof MockHttpServletRequest)
            {
               mockReq = (MockHttpServletRequest) servletReq;
            }
         }
      }
      else
      {
         mockReq = (MockHttpServletRequest) servletReq;
      }
      
      assertNotNull("no mock request found from cloned request", mockReq);
      
      // now add headers
      PSSecurityProviderInstance wssp = null;
      PSServerConfiguration serverConfig = PSServer.getServerConfiguration();
      Iterator spInsts = serverConfig.getSecurityProviderInstances().iterator();
      while (spInsts.hasNext())
      {
         PSSecurityProviderInstance inst = 
            (PSSecurityProviderInstance) spInsts.next();
         if (inst.getType() == PSSecurityProvider.SP_TYPE_WEB_SERVER)
         {
            wssp = inst;
            break;
         }
      }
    
      assertNotNull("no webserver sercurity provider configured", wssp);
      
      Properties props = wssp.getProperties();
      final String authUserHeader = props.getProperty(
         PSWebServerProvider.AUTHENTICATED_USER_HEADER);
      final String roleListHeader = props.getProperty(
         PSWebServerProvider.USER_ROLE_LIST_HEADER);
      final String roleDelim = props.getProperty(
         PSWebServerProvider.ROLE_LIST_DELIMITER);
      
      mockReq.addHeader(authUserHeader, "author1");
      mockReq.addHeader(roleListHeader, "Author" + roleDelim + "Editor" + 
         roleDelim + "QA");
      mockReq.addHeader(IPSCgiVariables.CGI_AUTH_TYPE, "Basic");
      
      final HttpServletRequest authReq = PSSecurityFilter.authenticate(mockReq,
            newReq.getServletResponse(), null, null);

      assertTrue(authReq.isUserInRole("Author"));
      assertTrue(authReq.isUserInRole("Editor"));
      assertTrue(authReq.isUserInRole("QA"));
   }
}

