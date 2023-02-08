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

/*
 * test.percussion.soln.linkback GenericLinkbackControllerTest.java
 *  
 * @author DavidBenua
 *
 */
package linkback;

import com.percussion.soln.linkback.codec.impl.StringLinkBackTokenImpl;
import com.percussion.soln.linkback.servlet.GenericLinkbackController;
import com.percussion.soln.linkback.utils.LinkbackUtils;
import com.percussion.util.IPSHtmlParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * 
 * 
 * @author DavidBenua
 * 
 */
public class GenericLinkbackControllerTest {

    private static final Logger log = LogManager.getLogger(GenericLinkbackControllerTest.class);

    GenericLinkbackController cut;

    String token;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        cut = new GenericLinkbackController();
        Map<String, Object> pmap = new HashMap<String, Object>();
        pmap.put(IPSHtmlParameters.SYS_CONTENTID, "1013");
        pmap.put(IPSHtmlParameters.SYS_REVISION, "1");
        pmap.put(IPSHtmlParameters.SYS_TEMPLATE, "301");
        pmap.put(IPSHtmlParameters.SYS_FOLDERID, "196");
        pmap.put(IPSHtmlParameters.SYS_SITEID, "102");
        StringLinkBackTokenImpl impl = new StringLinkBackTokenImpl();
        token = impl.encode(pmap);

    }

    /**
     * Test method for
     * {@link com.percussion.soln.linkback.servlet.GenericLinkbackController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
     * .
     */
    @Test
    public final void testHandleRequestPositive() {
        log.debug("Starting positive test");
        cut.setRedirectPath("/foo/bar");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addParameter(LinkbackUtils.LINKBACK_PARAM_NAME, token);
        request.setMethod("GET");
        
        cut.setHelpViewName("helpview");
        cut.setErrorViewName("errorview");

        cut.setRequiredParameterNames(Arrays.<String> asList(new String[] { "sys_contentid" }));
        cut.setOptionalParameterNames(Arrays.<String> asList(new String[] { "sys_revision" }));

        try {
            ModelAndView mav = cut.handleRequest(request, response);
            assertNotNull(mav);
            /*
             * We should have been redirected.
             */
            assertEquals(RedirectView.class, mav.getView().getClass());
            assertTrue(mav.getModel().containsKey("sys_contentid"));
            String cid = (String) mav.getModel().get("sys_contentid");
            assertEquals("1013", cid);
            assertTrue(mav.getModel().containsKey("sys_revision"));
            String rev = (String) mav.getModel().get("sys_revision");
            assertEquals("1", rev);
        } catch (Exception ex) {
            log.error("Unexpected Exception " + ex, ex);
            fail("Exception");
        }

    }
    
    @Test
    public final void testHandleRequestToHelp() {
        log.debug("Starting positive test");
        cut.setRedirectPath("/foo/bar");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Notice we do not add the linkback http parameter.
        request.setMethod("GET");
        
        cut.setHelpViewName("helpview");
        cut.setErrorViewName("errorview");

        cut.setRequiredParameterNames(Arrays.<String> asList(new String[] { "sys_contentid" }));
        cut.setOptionalParameterNames(Arrays.<String> asList(new String[] { "sys_revision" }));

        try {
            ModelAndView mav = cut.handleRequest(request, response);
            assertNotNull(mav);
            /*
             * We should have been sent to the help view.
             */
            assertEquals("helpview", mav.getViewName());
        } catch (Exception ex) {
            log.error("Unexpected Exception " + ex, ex);
            fail("Exception");
        }

    }
    
    
    @Test
    public final void testHandleRequestToErrorWhenLinkbackTokenIsEmtpy() {
        log.debug("Starting positive test");
        cut.setRedirectPath("/foo/bar");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setMethod("GET");
        request.addParameter(LinkbackUtils.LINKBACK_PARAM_NAME, "");
        
        cut.setHelpViewName("helpview");
        cut.setErrorViewName("errorview");

        cut.setRequiredParameterNames(Arrays.<String> asList(new String[] { "sys_contentid" }));
        cut.setOptionalParameterNames(Arrays.<String> asList(new String[] { "sys_revision" }));

        try {
            ModelAndView mav = cut.handleRequest(request, response);
            assertNotNull(mav);
            /*
             * We should have been sent to the help view.
             */
            assertEquals("errorview", mav.getViewName());
        } catch (Exception ex) {
            log.error("Unexpected Exception " + ex, ex);
            fail("Exception");
        }

    }

    @Test
    public final void testHandleRequestMissingRequired() {
        log.debug("Starting positive test");
        cut.setRedirectPath("/foo/bar");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addParameter(LinkbackUtils.LINKBACK_PARAM_NAME, token);
        request.setMethod("GET");

        cut.setRequiredParameterNames(Arrays.<String> asList(new String[] { "doesnotexist" }));
        cut.setHelpViewName("helpview");
        cut.setErrorViewName("errorview");
        try {
            ModelAndView mav = cut.handleRequest(request, response);
            assertNotNull(mav);
            assertEquals("errorview", mav.getViewName());

        } catch (Exception ex) {
            log.error("Unexpected Exception " + ex, ex);
            fail("Exception");
        }

    }
}
