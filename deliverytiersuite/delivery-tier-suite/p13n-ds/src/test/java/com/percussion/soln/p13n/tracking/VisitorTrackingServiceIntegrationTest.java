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

package com.percussion.soln.p13n.tracking;

import static integrationtest.spring.SpringSetup.*;
import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.percussion.soln.p13n.tracking.VisitorTrackingResponse;
import com.percussion.soln.p13n.tracking.ds.web.VisitorTrackingController;
import com.percussion.soln.p13n.tracking.web.VisitorTrackingWebMediator;
import com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils;

public class VisitorTrackingServiceIntegrationTest {
    static VisitorTrackingController trackController;
    MockHttpServletRequest request;
    MockHttpServletResponse response;
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(VisitorTrackingServiceTest.class);
    
    @BeforeClass
    public static void setupSpring() throws Exception {
        loadXmlBeanFiles("file:ds/webapp/WEB-INF/applicationContext.xml",
                "file:ds/webapp/WEB-INF/spring/ds/applicationContext-ds.xml",
                "file:ds/webapp/WEB-INF/track-servlet.xml",
                "file:ds/webapp/WEB-INF/spring/ds/data-beans.xml",
                "classpath:META-INF/p13n/spring/**/*.xml",
                "classpath:integrationtest/p13n/ds/test-beans.xml");
        trackController = getBean("visitorTrackingController", VisitorTrackingController.class);
    }
    

    @Before
    public void setUp() throws Exception {
        request = new MockHttpServletRequest();
        request.setMethod("POST");
        response = new MockHttpServletResponse();
    }

    public void setUpDefaultRequest() {
        request.setParameter("actionName", "update");
        request.setParameter("segmentWeights[testseg1]", "1");
        request.setParameter("segmentWeights[testseg2]", "2");
        request.setParameter("actionParameters[ap1]", "aaa");
        request.setParameter("actionParameters[ap2]", "1");
    }
    
    private VisitorTrackingResponse convertResponse(MockHttpServletResponse response) 
        throws UnsupportedEncodingException {
        String content = response.getContentAsString();
        log.debug("Convert JSON is: " + content);
        Object obj = JSONObject.toBean(JSONObject.fromObject(content), VisitorTrackingResponse.class);
        return (VisitorTrackingResponse) obj;
    }
    
    @Test
    public void testTrackWithEmptyRequest() throws Exception {
        trackController.handleRequest(request, response);
        VisitorTrackingResponse v = convertResponse(response);
        assertEquals("ERROR", v.getStatus());
    }
    
    @Test
    public void testTrackSettingCookie() throws Exception {
        //We allow the following casting because its an integration test.
        ((VisitorTrackingWebMediator)trackController.getVisitorTrackingHttpService()).setUsingCookies(true);
        setUpDefaultRequest();
        trackController.handleRequest(request, response);
        VisitorTrackingResponse v = convertResponse(response);
        assertEquals("OK", v.getStatus());
        assertNotNull("A cookie should have been set.", 
                response.getCookie(VisitorTrackingWebUtils.VISITOR_PROFILE_ID_COOKIE_NAME));
    }

}
