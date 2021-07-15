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
