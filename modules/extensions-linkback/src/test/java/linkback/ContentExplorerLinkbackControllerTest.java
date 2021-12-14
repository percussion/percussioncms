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
 * test.percussion.soln.linkback ContentExplorerLinkbackControllerTest.java
 *  
 * @author JasonChu
 *
 */
package linkback;

import com.percussion.soln.linkback.codec.impl.StringLinkBackTokenImpl;
import com.percussion.soln.linkback.servlet.ContentExplorerLinkbackController;
import com.percussion.soln.linkback.utils.LinkbackUtils;
import com.percussion.util.IPSHtmlParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ContentExplorerLinkbackControllerTest {

    private static final Logger log = LogManager.getLogger(ActionPanelLinkbackControllerTest.class);

    ContentExplorerLinkbackController cut;
    String token_nocontentid;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        cut = new ContentExplorerLinkbackController();
        cut.setHelpViewName("helpview");
        cut.setErrorViewName("errorview");

        Map<String, Object> pmap = new HashMap<String, Object>();
        // deliberately missing contentid
        pmap.put(IPSHtmlParameters.SYS_REVISION, "1");
        pmap.put(IPSHtmlParameters.SYS_TEMPLATE, "301");
        pmap.put(IPSHtmlParameters.SYS_FOLDERID, "196");
        pmap.put(IPSHtmlParameters.SYS_SITEID, "102");
        StringLinkBackTokenImpl impl = new StringLinkBackTokenImpl();
        token_nocontentid = impl.encode(pmap);
    }

    @Test
    public final void testHandleRequestMissingContentId() {
        log.debug("Starting testing missing contentid");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addParameter(LinkbackUtils.LINKBACK_PARAM_NAME, token_nocontentid);
        request.setMethod("GET");

        try {
            ModelAndView mav = cut.handleRequest(request, response);
            assertNotNull(mav);
            assertEquals("errorview", mav.getViewName());
            log.debug(mav.getModel().get("message"));

        } catch (Exception ex) {
            log.error("Unexpected Exception " + ex, ex);
            fail("Exception");
        }
    }
}
