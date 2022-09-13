package test.percussion.soln.p13n.tracking.web;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorTrackingActionRequest;
import com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils;

public class VisitorProfileWebUtilsTest {

    MockHttpServletRequest request;
    MockHttpServletResponse response;
    VisitorProfile profile;
    
    @Before
    public void setUp() throws Exception {
        request = new MockHttpServletRequest();
        request.setMethod("GET");
        response = new MockHttpServletResponse();
        profile = new VisitorProfile();
    }

    @Test
    public void shouldGetVisitorProfileIdFromCookie() {
        setProfileIdToCookie(1);
        assertEquals(new Long(1), VisitorTrackingWebUtils.getVisitorProfileIdFromCookie(request));
    }
   
    @Test
    public void shouldGetVisitorProfileIdFromCookieEvenIfIdIsNegative() {
        setProfileIdToCookie(-1);
        assertEquals(new Long(-1), VisitorTrackingWebUtils.getVisitorProfileIdFromCookie(request));
    }
    
    private void setProfileIdToCookie(long id) {
        Cookie c = new Cookie(VisitorTrackingWebUtils.VISITOR_PROFILE_ID_COOKIE_NAME, "" + id);
        request.setCookies(new Cookie[] {c});
    }
    
    @Test
    public void shouldGetVisitorProfileIdFromRequest() {
        request.setParameter(VisitorTrackingWebUtils.VISITOR_PROFILE_ID_REQUEST_PARAM, 
                "-1");
        assertEquals(new Long(-1), VisitorTrackingWebUtils.getVisitorProfileIdFromRequestParameters(request));
    }
    
    @Test
    public void shouldFailToGetVisitorProfileIdFromRequestParametersAndWillReturnNull() {
        request.setParameter(VisitorTrackingWebUtils.VISITOR_PROFILE_ID_REQUEST_PARAM, 
                "BAD_INPUT");
        assertNull(VisitorTrackingWebUtils.getVisitorProfileIdFromRequestParameters(request));
    }
    
    @Test
    public void testParameterizeRequest() throws Exception {
        VisitorTrackingActionRequest r = new VisitorTrackingActionRequest();
        r.setActionName("login");
        r.setUserId("agent");
        r.setVisitorProfileId(100);
        Map<String, Integer> w = new HashMap<String, Integer>();
        w.put("a", 1);
        w.put("b", 2);
        r.setSegmentWeights(w);
        Map<String, String> actual = VisitorTrackingWebUtils.parameterizeTrackingRequest(r);
        assertThat(actual, hasEntry("actionName", "login"));
        assertThat(actual, hasEntry("userId", "agent"));
        assertThat(actual, hasEntry("segmentWeights[a]", "1"));
        assertThat(actual, hasEntry("visitorProfileId", "100"));
    }
   

}
