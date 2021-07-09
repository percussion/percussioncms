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
package com.percussion.servlets;

import static com.percussion.servlets.PSSecurityFilter.NON_SECURE_HTTP_BIND_ADDRESS;
import static com.percussion.servlets.PSSecurityFilter.NON_SECURE_HTTP_BIND_HEADER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.percussion.server.PSServer;
import com.percussion.servlets.PSSecurityFilter.AuthType;
import com.percussion.servlets.PSSecurityFilter.SecurityEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletException;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.AfterClass;
import org.junit.Assert;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

/**
 * Test case for the {@link PSSecurityFilter} class. Tests security
 * configurations and pattern matching only.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
public class PSSecurityFilterTest
{
   @AfterClass
   protected void tearDown() throws Exception
   {
      System.setProperty(NON_SECURE_HTTP_BIND_ADDRESS, "");
      System.setProperty(NON_SECURE_HTTP_BIND_HEADER,"");

   }

   MockServletContext context = new MockServletContext() {
      @Override
      public String getRealPath(String path)
      {
         return new File(new File(TEST_FILE_DIR), path).getAbsolutePath();
      }

   };

   MockFilterConfig config = new MockFilterConfig(context);
   MockFilterChain chain = new MockFilterChain();
   MockHttpServletRequest request = new MockHttpServletRequest();
   MockHttpServletResponse response = new MockHttpServletResponse();

   /**
    * Test the security config init.
    * 
    * @throws Exception If there are any errors.
    */
   @Test
   public void test10InitSecurityConfiguration() throws Exception
   {
      ms_filter.initSecurityConfiguration(TEST_FILE_DIR);
      assertEquals(ms_configuredRequests, ms_filter.getConfiguredEntries());
      assertTrue(ms_filter.isSecureLogin());
   }

   @Test
   public void test20NonSecureRequestsForbidden() throws Exception {

      System.setProperty(NON_SECURE_HTTP_BIND_ADDRESS, "127.0.0.1");
      ms_filter.init(config);
      request.setRemoteAddr("192.168.1.1");
      ms_filter.doFilter(request, response, chain);
      assertEquals(403, response.getStatus());
   }

   @Test
   public void test30CrossSiteForgery() throws Exception {

      HttpClient client = new HttpClient();
      Credentials defaultcreds = new UsernamePasswordCredentials("admin1", "demo");
      client.getState().setCredentials(AuthScope.ANY, defaultcreds);

      checkRequestHeaderRedirectLogin(client, "Referer", "http://www.msn.com", "http://127.0.0.1:9992/Rhythmyx/test");

      checkRequestHeaderRedirectLogin(client, "Origin", "https://www.msn.com", "http://127.0.0.1:9992/Rhythmyx/test");
   }

   private void checkRequestHeaderRedirectLogin(HttpClient client, String strHeader, String strHdrVal, String strRequestURL) throws Exception {

      HttpMethod method = new GetMethod(strRequestURL);
      method.addRequestHeader("RX_USEBASICAUTH", "true");
      method.setDoAuthentication(true);
      assertEquals(200, client.executeMethod(method));

      method = new GetMethod(strRequestURL);
      method.setRequestHeader(strHeader, strHdrVal);
      client.executeMethod(method);
      assertEquals(method.getPath(), RHYTHMYX_CONTEXT_PATH + "/login"); // Redirected to login

      if (method != null)
         method.releaseConnection();
   }

   @Test
   public void test40NonSecureRequestsException() throws Exception {

      System.setProperty(NON_SECURE_HTTP_BIND_ADDRESS, "127.0.0.1");
      ms_filter.init(config);
      request.setRemoteAddr("127.0.0.1");
      assertTrue(ms_filter.isNonSecureHttpRequestAllowed(request));
      //System.setProperty(NON_SECURE_HTTP_BIND_ADDRESS, "");

   }

   @Test
   public void test50NonSecureRequestsHeaderForbidden() throws Exception {

      System.setProperty(NON_SECURE_HTTP_BIND_ADDRESS, "127.0.0.1");
      System.setProperty(NON_SECURE_HTTP_BIND_HEADER, "REAL_IP");
      ms_filter.init(config);
      request.setRemoteAddr("127.0.0.1");
      request.addHeader("REAL_IP", "192.168.1.1");
      ms_filter.doFilter(request, response, chain);
      assertEquals(403, response.getStatus());
   }

   @Test
   public void test60HTTPtoHTTPS() throws Exception {

      System.setProperty(NON_SECURE_HTTP_BIND_ADDRESS, "non_secure_address");
      System.setProperty(NON_SECURE_HTTP_BIND_HEADER, "REAL_IP");

      ms_filter.init(config);
      
      //Ensure the requireHTTPS property is not already set.
      Properties serverProperties = PSServer.getServerProps();
      assertNull(serverProperties.getProperty(require_HTTPS));      
      try {
      //now set the require property to true
      serverProperties.setProperty(require_HTTPS, "true");
      
      //set port to 443
      PSServer.ms_sslListenerPort = 443;

      //random address to go to via http
      String serverName = UUID.randomUUID().toString();
      request.setServerName(serverName);
      request.addHeader("REAL_IP", "non_secure_address");
                  
      ms_filter.doFilter(request, response, chain);
      
      //property should not have been modified
      assertEquals("true", PSServer.getServerProps().getProperty(require_HTTPS));

      //Check to see if it has become an https url now.
      String serverNamewithHttps = "https://"  + serverName;  
      assertEquals(serverNamewithHttps, response.getRedirectedUrl());
      } finally {
         serverProperties.setProperty(require_HTTPS, "false");
      }
   }

   /**
    * Test loading various security configurations.
    * 
    * @throws Exception if there are any errors.
    */
   @Test
   public void test70LoadConfig() throws Exception
   {
      for (int i = 0; i < ms_testSecure.length; i++)
      {
         doTestLoadConfig(i);
      }
   }

   /**
    * Tests loading a single security configuration.
    * 
    * @param index The index of the config to load.
    * 
    * @throws Exception If there are any errors.
    */
   public void doTestLoadConfig(int index) throws Exception
   {
      File securityConfig;
      securityConfig = new File(TEST_FILE_DIR, "test-security-conf-"
            + (index + 1) + ".xml");
      boolean isSecure = PSSecurityFilter.loadConfig(securityConfig, false, 
            new ArrayList<SecurityEntry>());

      assertEquals(isSecure, ms_testSecure[index]);
   }

   /**
    * Tests the pattern matching.
    * 
    * @throws Exception If there are any errors.
    */
   @Test
   public void test80Matching() throws Exception
   {  
      ms_filter.init(config);
      assertEquals(AuthType.BASIC, match("/user/basic", "GET"));
      assertEquals(AuthType.BASIC, match("/user/basics", "GET"));
      assertEquals(AuthType.BASIC, match("/user/basic/test.jsp", "GET"));
      assertEquals(AuthType.BASIC, match("/user/test/basic.jsp", "GET"));
      assertEquals(AuthType.FORM, match("/user/test/user.jsp", "GET"));
      assertEquals(AuthType.BASIC, match("/user/basic/basic.jsp", "GET"));
      assertEquals(AuthType.ANONYMOUS, match("/user/anon", "GET"));
      assertEquals(AuthType.ANONYMOUS, match("/user/anonymous", "GET"));
      assertEquals(AuthType.ANONYMOUS, match("/user/foo/anon.jsp", "GET"));
      assertEquals(AuthType.ANONYMOUS, match("/user/foo/xanon.jsp", "GET"));
      assertEquals(AuthType.BASIC, match("/", "PROPFIND"));
      assertEquals(AuthType.ANONYMOUS, match("/", "PROPPATCH"));
      assertEquals(AuthType.BASIC, match("/rxwebdav", "PROPPATCH"));
      assertEquals(AuthType.BASIC, match("/rxwebdav/foo", "PROPPATCH"));
      assertEquals(AuthType.BASIC, match("/rxwebdav/foo/bar", "GET"));
      assertEquals(AuthType.FORM, match("/", "GET"));
   }

   /**
    * Test the loaded filter configuration against the given method and url
    * @param url
    * @param method
    * @return the matching authtype
    * @throws ServletException 
    */
   private AuthType match(String url, String method) throws ServletException
   {
      MockHttpServletRequest mreq = new MockHttpServletRequest();
      mreq.setServletPath(url);
      mreq.setMethod(method);
      return ms_filter.calculateAuthType(mreq);
   }

   /**
    * 
    */
   private static final String TEST_FILE_DIR
   = "/com/percussion/servlets";

   /**
    * 
    */
   private static boolean[] ms_testSecure = new boolean[]
         {false, true, false};

   /**
    * 
    */
   private static PSSecurityFilter ms_filter = new PSSecurityFilter();

   /**
    * 
    */
   private static List<SecurityEntry> ms_configuredRequests = 
         new ArrayList<SecurityEntry>();

   static
   {
      ms_configuredRequests.add(new SecurityEntry(false, true, AuthType.BASIC,
            "/Design*"));
      ms_configuredRequests.add(new SecurityEntry(false, true, AuthType.BASIC,
            "/rxwebdav*"));
      ms_configuredRequests.add(new SecurityEntry(false, true,
            AuthType.ANONYMOUS, "/rxlogin.jsp"));
      ms_configuredRequests.add(new SecurityEntry(false, true,
            AuthType.ANONYMOUS, "/rxlogout.jsp"));
      ms_configuredRequests.add(new SecurityEntry(false, true,
            AuthType.ANONYMOUS, "/sys_deploymentHandler"));
      ms_configuredRequests.add(new SecurityEntry(false, true,
            AuthType.ANONYMOUS, "/login"));
      ms_configuredRequests.add(new SecurityEntry(false, true,
            AuthType.ANONYMOUS, "/logout"));

      ms_configuredRequests.add(new SecurityEntry(false, false, AuthType.BASIC,
            "PROPFIND"));
      ms_configuredRequests.add(new SecurityEntry(false, false, AuthType.ANONYMOUS,
            "PROPPATCH"));

      ms_configuredRequests.add(new SecurityEntry(true, true, AuthType.BASIC,
            "/user/basic*"));
      ms_configuredRequests.add(new SecurityEntry(true, true,
            AuthType.ANONYMOUS, "/user/anon*"));
      ms_configuredRequests.add(new SecurityEntry(true, true,
            AuthType.ANONYMOUS, "/user/*anon.jsp"));
      ms_configuredRequests.add(new SecurityEntry(true, true, AuthType.BASIC,
            "/user/*basic.jsp"));
   }

   private static final String RHYTHMYX_CONTEXT_PATH = "/Rhythmyx";
   private static final String require_HTTPS = "requireHTTPS";
}
