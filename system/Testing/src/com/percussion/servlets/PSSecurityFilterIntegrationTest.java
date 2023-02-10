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

import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;

@Category(IntegrationTest.class)
public class PSSecurityFilterIntegrationTest {



    @Test
    public void test20NonSecureRequestsForbidden() throws Exception {

      /*  System.setProperty(NON_SECURE_HTTP_BIND_ADDRESS, "127.0.0.1");
        ms_filter.init(config);
        request.setRemoteAddr("192.168.1.1");
        ms_filter.doFilter(request, response, chain);
        assertEquals(403, response.getStatus());
   */
    }

    @Test
    public void test30CrossSiteForgery() throws Exception {
/*
        HttpClient client = new HttpClient();
        Credentials defaultcreds = new UsernamePasswordCredentials("admin1", "demo");
        client.getState().setCredentials(AuthScope.ANY, defaultcreds);

        checkRequestHeaderRedirectLogin(client, "Referer", "http://www.msn.com", "http://127.0.0.1:9992/Rhythmyx/test");

        checkRequestHeaderRedirectLogin(client, "Origin", "https://www.msn.com", "http://127.0.0.1:9992/Rhythmyx/test");
  */
    }

    @Test
    public void test60HTTPtoHTTPS() throws Exception {

     /*   System.setProperty(NON_SECURE_HTTP_BIND_ADDRESS, "non_secure_address");
        System.setProperty(NON_SECURE_HTTP_BIND_HEADER, "REAL_IP");

        ms_filter.init(config);

        //set port to 443
        PSServer.ms_sslListenerPort = 443;

        //random address to go to via http
        String serverName = UUID.randomUUID().toString();
        request.setServerName(serverName);
        request.addHeader("REAL_IP", "non_secure_address");

        ms_filter.doFilter(request, response, chain);

        //Check to see if it has become an https url now.
        String serverNamewithHttps = "https://"  + serverName;
        assertEquals(serverNamewithHttps, response.getRedirectedUrl());
*/
    }

    /**
     * Test the loaded filter configuration against the given method and url
     * @param url
     * @param method
     * @return the matching authtype
     * @throws ServletException
     */
    private PSSecurityFilter.AuthType match(String url, String method) throws ServletException
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
    private static List<PSSecurityFilter.SecurityEntry> ms_configuredRequests =
            new ArrayList<PSSecurityFilter.SecurityEntry>();

    static
    {
        ms_configuredRequests.add(new PSSecurityFilter.SecurityEntry(false, true, PSSecurityFilter.AuthType.BASIC,
                "/Design*"));
        ms_configuredRequests.add(new PSSecurityFilter.SecurityEntry(false, true, PSSecurityFilter.AuthType.BASIC,
                "/rxwebdav*"));
        ms_configuredRequests.add(new PSSecurityFilter.SecurityEntry(false, true,
                PSSecurityFilter.AuthType.ANONYMOUS, "/rxlogin.jsp"));
        ms_configuredRequests.add(new PSSecurityFilter.SecurityEntry(false, true,
                PSSecurityFilter.AuthType.ANONYMOUS, "/rxlogout.jsp"));
        ms_configuredRequests.add(new PSSecurityFilter.SecurityEntry(false, true,
                PSSecurityFilter.AuthType.ANONYMOUS, "/sys_deploymentHandler"));
        ms_configuredRequests.add(new PSSecurityFilter.SecurityEntry(false, true,
                PSSecurityFilter.AuthType.ANONYMOUS, "/login"));
        ms_configuredRequests.add(new PSSecurityFilter.SecurityEntry(false, true,
                PSSecurityFilter.AuthType.ANONYMOUS, "/logout"));
        ms_configuredRequests.add(new PSSecurityFilter.SecurityEntry(false, true,
                PSSecurityFilter.AuthType.ANONYMOUS, "/locale.jsp"));

        ms_configuredRequests.add(new PSSecurityFilter.SecurityEntry(false, false, PSSecurityFilter.AuthType.BASIC,
                "PROPFIND"));
        ms_configuredRequests.add(new PSSecurityFilter.SecurityEntry(false, false, PSSecurityFilter.AuthType.ANONYMOUS,
                "PROPPATCH"));

        ms_configuredRequests.add(new PSSecurityFilter.SecurityEntry(true, true, PSSecurityFilter.AuthType.BASIC,
                "/user/basic*"));
        ms_configuredRequests.add(new PSSecurityFilter.SecurityEntry(true, true,
                PSSecurityFilter.AuthType.ANONYMOUS, "/user/anon*"));
        ms_configuredRequests.add(new PSSecurityFilter.SecurityEntry(true, true,
                PSSecurityFilter.AuthType.ANONYMOUS, "/user/*anon.jsp"));
        ms_configuredRequests.add(new PSSecurityFilter.SecurityEntry(true, true, PSSecurityFilter.AuthType.BASIC,
                "/user/*basic.jsp"));
    }

    private static final String RHYTHMYX_CONTEXT_PATH = "/Rhythmyx";
    private static final String require_HTTPS = "requireHTTPS";
}
