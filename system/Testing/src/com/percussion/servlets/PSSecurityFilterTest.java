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
package com.percussion.servlets;

import com.percussion.cms.IPSConstants;
import com.percussion.server.PSServer;
import com.percussion.servlets.PSSecurityFilter.AuthType;
import com.percussion.servlets.PSSecurityFilter.SecurityEntry;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.testing.UnitTest;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.ServletException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.percussion.servlets.PSSecurityFilter.NON_SECURE_HTTP_BIND_ADDRESS;
import static com.percussion.servlets.PSSecurityFilter.NON_SECURE_HTTP_BIND_HEADER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test case for the {@link PSSecurityFilter} class. Tests security
 * configurations and pattern matching only.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(UnitTest.class)
public class PSSecurityFilterTest
{

   @ClassRule
   public static TemporaryFolder tempFolder = TemporaryFolder.builder().build();

   @AfterClass
   public static void tearDown() throws Exception
   {
      System.setProperty(NON_SECURE_HTTP_BIND_ADDRESS, "");
      System.setProperty(NON_SECURE_HTTP_BIND_HEADER,"");


   }

   public static void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation)
        throws IOException {
   Files.walk(Paths.get(sourceDirectoryLocation))
           .forEach(source -> {
              Path destination = Paths.get(destinationDirectoryLocation, source.toString()
                      .substring(sourceDirectoryLocation.length()));
              try {
                 Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
              } catch (IOException e) {
                 e.printStackTrace();
              }
           });
}

   @BeforeClass
   public static void setupClass() throws IOException, URISyntaxException {
      System.setProperty(PathUtils.DEPLOY_DIR_PROP, tempFolder.getRoot().getAbsolutePath());

      copyDirectory(Paths.get(
              PSSecurityFilterTest.class.getResource(PSSecurityFilterTest.TEST_FILE_DIR).toURI()).toString(),
              tempFolder.getRoot().getAbsolutePath() );

   }

   @Before
   public void setup(){
         ms_filter = new PSSecurityFilter();
   }

   MockServletContext context = new MockServletContext() {
      @Override
      public String getRealPath(String path)
      {
         if(path.startsWith("/"))
            path = path.substring(1);
         return tempFolder.getRoot().toPath().resolve(path).toAbsolutePath().toString();
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
      ms_filter.initSecurityConfiguration(tempFolder.getRoot().getAbsolutePath());
      assertEquals(ms_configuredRequests, ms_filter.getConfiguredEntries());
      assertTrue(ms_filter.isSecureLogin());
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
      securityConfig = new File(tempFolder.getRoot().getAbsolutePath(), "test-security-conf-"
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

   @Test
   @Ignore
   public void testAllowedOriginsOnInit() throws ServletException, IOException {
      ms_filter.init(config);
      assertTrue(ms_filter.allowedOrigins.isEmpty());

      updateServerProperties("mycms.percussion.marketing", "*");
      ms_filter.init(config);
      assertFalse(ms_filter.allowedOrigins.isEmpty());
      assertEquals("mycms.percussion.marketing",ms_filter.allowedOrigins.get(0));

      updateServerProperties("", "https://mycms.percussion.marketing:9991/, mycms.percussion.marketing, mycms");
      ms_filter.init(config);
      assertFalse(ms_filter.allowedOrigins.isEmpty());
      assertEquals("mycms.percussion.marketing",ms_filter.allowedOrigins.get(0));
      assertEquals("mycms.percussion.marketing",ms_filter.allowedOrigins.get(1));
      assertEquals("mycms",ms_filter.allowedOrigins.get(2));


   }

   @Test
   @Ignore
   public void testInjectedHostHeader() throws ServletException, IOException {
      request.addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36");
      request.addHeader("Referrer", "https://localhost:9991/login");
      request.addHeader("Cookie", "pssessionid=0f35a3c6ec0686f85b308920beb741d1f8a3fe7c2c6d6a54138c525945ffdff3;JSESSIONID=node0d5pyy5zahlb4lt6q5bfxq795675.node0");
      request.addHeader("Host", "appscanheaderinjection.com" );
      request.addHeader("OWASP-CSRFTOKEN", "RQTC-9SKO-HGL9-VRE5-0KWP-7RDS-70TX-3WVW, RQTC-9SKO-HGL9-VRE5-0KWP-7RDS-70TX-3WVW" );
      request.addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
      request.addHeader("Accept-Language", "en-US");
      request.setMethod("GET");
      request.setScheme("https");
      request.setServerPort(9991);
      request.setSecure(true);


      ms_filter.init(config);
      ms_filter.doFilter(request, response, chain);

      //Insecure - expect bad value
      assertEquals(response.getHeader("Location"), "/login?sys_redirect=https%3a%2f%2fappscanheaderinjection%2ecom%3a9991");

      //Configure with publicCMSHostName set
      updateServerProperties("mycms.percussion.marketing", "*");
      response = new MockHttpServletResponse();
      ms_filter.init(config);
      ms_filter.doFilter(request, response, chain);

      assertNotEquals(response.getHeader("Location"), "/login?sys_redirect=https%3a%2f%2fappscanheaderinjection%2ecom%3a9991");
      assertEquals(403, response.getStatus());

      request.removeHeader("Host");
      request.addHeader("Host", "mycms.percussion.marketing");
      response = new MockHttpServletResponse();
      ms_filter.doFilter(request, response, chain);
      assertEquals(302, response.getStatus());


      //Configure with allowedOrigins set
      updateServerProperties("", "https://mycms.percussion.marketing:9991/, mycms.percussion.marketing, mycms");
      request.removeHeader("Host");
      request.addHeader("Host", "mycms.percussion.marketing");
      response = new MockHttpServletResponse();
      assertNotEquals(response.getHeader("Location"), "/login?sys_redirect=https%3a%2f%2fappscanheaderinjection%2ecom%3a9991");
      assertEquals(200, response.getStatus());



   }

   /**
    * Utility method for updating the server.properties file with different values to
    * test behaviors
    * @param cmsHostName  The value for the publicCMSHostName property.
    * @param allowedOrigins The value for the allowedOrigins property
    * @throws IOException If an exception occurs
    */
   private void updateServerProperties(String cmsHostName, String allowedOrigins) throws IOException {
      Properties props = new Properties();

      try (FileInputStream in = new FileInputStream(
              tempFolder.getRoot().getAbsolutePath() + "/rxconfig/Server/server.properties")) {
         props.load(in);
      }

      try (FileOutputStream out = new FileOutputStream(tempFolder.getRoot().getAbsolutePath() + "/rxconfig/Server/server.properties")) {
         props.setProperty(IPSConstants.SERVER_PROP_PUBLIC_CMS_HOSTNAME, cmsHostName);
         props.setProperty(IPSConstants.SERVER_PROP_ALLOWED_ORIGINS, allowedOrigins);
         props.store(out, null);
      }
      PSServer.getServerProps(true);
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
   private static PSSecurityFilter ms_filter;

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
      ms_configuredRequests.add(new SecurityEntry(false, true,
              AuthType.ANONYMOUS, "/locale.jsp"));

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
