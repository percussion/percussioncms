/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
