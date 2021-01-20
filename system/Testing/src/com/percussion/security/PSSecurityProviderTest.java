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
package com.percussion.security;

import com.percussion.server.IPSCgiVariables;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestTest;
import com.percussion.testing.IPSCustomJunitTest;
import com.percussion.utils.testing.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertTrue;


/**
 * Security provider tests. Not part of the nightly test execution, only used 
 * for debugging purposes.
 * 
 * This test needs the NT Guest account activated. It is excluded to run 
 * on nightly builds. 
 */
@Category(UnitTest.class)
public class PSSecurityProviderTest
   implements IPSCustomJunitTest
{
   public PSSecurityProviderTest()
   {

   }

   /**
    * Helper to create a new request used for testing.
    * 
    * @return the newly created request , never <code>null</code>.
    */
   protected PSRequest createRequest()
   {
      HashMap cookies = new HashMap();
      HashMap cgiVars = new HashMap();

      cgiVars.put(IPSCgiVariables.CGI_PS_REQUEST_TYPE, "design-");

      PSRequest req = PSRequestTest.makeRequest("/foo", "/foo", 
         new HashMap(), // params
         cgiVars, // cgi vars
         cookies, // cookies
         null, new ByteArrayOutputStream());

      return req;
   }

   /**
    * Test the web server security provider.
    * 
    * @throws Exception If the test fails 
    */
   @Test
   public void WebProviderTest() throws Exception
   {
      Properties props = new Properties();
      props.setProperty(PSWebServerProvider.AUTHENTICATED_USER_HEADER,
         IPSCgiVariables.CGI_AUTH_USER_NAME);
      props.setProperty(PSWebServerProvider.ROLE_LIST_DELIMITER, ";");
      props.setProperty(PSWebServerProvider.USER_ROLE_LIST_HEADER,
         "HTTP_RXUSERROLES");
      PSWebServerProvider webProvider = new PSWebServerProvider(props,
         "testInstance");

      PSUserEntry ent;

      // Step 1: Send a basic authentication with user admin1
      Map<String, String> cgiVars = new HashMap<String, String>();
      cgiVars.put(IPSCgiVariables.CGI_AUTH_USER_NAME, "admin1");
      cgiVars.put("HTTP_RXUSERROLES", "Admin");
      cgiVars.put(IPSCgiVariables.CGI_AUTH_TYPE, "Basic");
      ent = webProvider.authenticate("admin1", cgiVars);

      if (!ent.getName().equals("admin1"))
         assertTrue("Name not retrieved in user entry - " + ent.getName()
            + " was returned", false);

      // Step 2: Send a NTLM authentication with user admin1
      cgiVars = new HashMap<String, String>();
      // TODO Fix test?
      // req.setCgiVariables(cgiVars);
      cgiVars.put(IPSCgiVariables.CGI_AUTH_USER_NAME, "admin1");
      cgiVars.put("HTTP_RXUSERROLES", "Admin");
      cgiVars.put(IPSCgiVariables.CGI_AUTH_TYPE, "NTLM");

      ent = webProvider.authenticate("admin1", cgiVars);
      if (!ent.getName().equals("admin1"))
         assertTrue("Name not retrieved in user entry - " + ent.getName()
            + " was returned", false);

      // Step 3: Send a NTLM authentication with user admin1
      cgiVars = new HashMap<String, String>();
      // TODO Fix test?
      // req.setCgiVariables(cgiVars);
      cgiVars.put("HTTP_RXUSERROLES", "Admin");
      cgiVars.put(IPSCgiVariables.CGI_CERT_SUBJECT,
         "CN=Bugs Bunny,OU=TestOrgUnit,O=Earth,C=USA");

      ent = webProvider.authenticate("admin1", cgiVars);
      if (!ent.getName().equals("Bugs Bunny"))
         assertTrue("Name not retrieved in user entry - " + ent.getName()
            + " was returned", false);
   }
}
