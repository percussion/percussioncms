package test.percussion.soln.p13n.tracking.web;


import static com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import net.sf.json.JSONObject;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.percussion.soln.p13n.delivery.IDeliveryService;
import com.percussion.soln.p13n.tracking.IVisitorProfileDataService;
import com.percussion.soln.p13n.tracking.IVisitorTrackingService;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorTrackingActionRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingResponse;
import com.percussion.soln.p13n.tracking.ds.web.VisitorTrackingController;
import com.percussion.soln.p13n.tracking.web.VisitorTrackingWebMediator;


//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.matchers.JUnitMatchers.*;

/**
 * Scenario description: 
 * @author Steve Bolton
 * @author Adam Gent
 */
@RunWith(JMock.class)
public class VisitorTrackingControllerTest  {

    Mockery context = new JUnit4Mockery();

    VisitorTrackingController visitorTrackingController;
    IVisitorTrackingService visitorTrackingService;
    VisitorTrackingWebMediator mediator;
    
    IDeliveryService deliveryService;
    IVisitorProfileDataService visitorProfileDataService;
    MockHttpServletRequest request;
    MockHttpServletResponse response;
    VisitorProfile profile;
    VisitorTrackingResponse trackResponse;
    String expectedJsonString = "{\"errorId\":\"\",\"errorMessage\":\"\",\"status\":\"OK\"," +
    "\"visitorProfile\":null,\"visitorProfileId\":100}";
    

    @Before
    public void setUp() throws Exception {
        visitorTrackingController = new VisitorTrackingController();
        visitorTrackingService = context.mock(IVisitorTrackingService.class);
        visitorProfileDataService = context.mock(IVisitorProfileDataService.class);
        mediator = new VisitorTrackingWebMediator();
        mediator.setVisitorTrackingService(visitorTrackingService);
        visitorTrackingController.setVisitorTrackingHttpService(mediator);
        visitorTrackingController.setReturnProfile(false);
        request = new MockHttpServletRequest();
        request.setMethod("GET");
        response = new MockHttpServletResponse();
        
        profile = new VisitorProfile();
        profile.setId(100L);
        Map<String,Integer> segmentWeights = new HashMap<String,Integer>();
        segmentWeights.put("seg1",1);
        segmentWeights.put("seg2",2);
        profile.setSegmentWeights(segmentWeights);
        profile.setLastUpdated(null);
        
        trackResponse = new VisitorTrackingResponse();
        trackResponse.setStatus("OK");
        trackResponse.setVisitorProfile(profile);
        trackResponse.setVisitorProfileId(100);
        
    }

    public void setUpDefaultRequest() {
        request.setParameter("actionName", "test");
        request.setParameter("segmentWeights[testseg1]", "1");
        request.setParameter("segmentWeights[testseg2]", "2");
        request.setParameter("actionParameters[ap1]", "aaa");
        request.setParameter("actionParameters[ap2]", "1");
    }
    
    @Test
    public void shouldHandleAValidVisitorTrackingRequestProfileInSession() throws Exception {
        
        /*
         * Given: The profile is in the session
         */
        setUpDefaultRequest();
        setVisitorProfileToSession(request.getSession(), profile);
        
        //NOT request.setParameter("visitorProfileId", String.valueOf(profileId));

        /* 
         * Expect: The delivery service to process our request into a response
         *         with out using the visitor profile data service since our profile
         *         is in the session.
         */
        
        context.checking(new Expectations() {{ 
            one(visitorTrackingService).retrieveVisitor(with(any(VisitorTrackingRequest.class)));
            will(returnValue(profile));
            one(visitorTrackingService).track(with(any(VisitorTrackingActionRequest.class)));
            will(returnValue(trackResponse));
        }});
        
        /*
         * When: Handle a valid delivery request
         */
        visitorTrackingController.handleRequest(request, response);

        /*
         * Then: the json response should equal
         */
        String actualJsonString = response.getContentAsString();

        		
        context.assertIsSatisfied();
        assertEquals("JSON Object String should be the same",
                expectedJsonString,actualJsonString);
    }
    
    @Test
    public void shouldHandleAValidVisitorTrackingRequestProfileInCookie() throws Exception {
        
        /*
         * Given: The profile id is in the cookie (returning visitor).
         */
        setUpDefaultRequest();
        mediator.setUsingCookies(true);
        Cookie cookie = new Cookie("visitorProfileId", "100");
        request.setCookies(new Cookie[] {cookie});
        
        /* 
         * Expect: The delivery service to process our request into a response
         *         with out using the visitor profile data service since our profile
         *         is in the session.
         */
        
        context.checking(new Expectations() {{ 
            one(visitorTrackingService).retrieveVisitor(with(any(VisitorTrackingRequest.class)));
            will(returnValue(profile));
                    
            one(visitorTrackingService).track(with(any(VisitorTrackingActionRequest.class)));
            will(returnValue(trackResponse));
        }});
        
        /*
         * When: Handle a valid delivery request
         */
        visitorTrackingController.handleRequest(request, response);

        /*
         * Then: the json response should equal
         */
        String actualJsonString = response.getContentAsString();

                
        context.assertIsSatisfied();
        assertEquals("JSON Object String should be the same",
                expectedJsonString,actualJsonString);
    }
    
    

    @Test
    public void shouldHandleAnInValidTrackingRequest() throws Exception {
        /*
         * Given: an invalid request. No parameters are set.
         */
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        /* 
         * Expect: The service should NOT be called since spring will handle the validation.
         */
        context.checking(new Expectations() {{ }});

        /*
         * When: Handle the request
         */
        visitorTrackingController.handleRequest(request, response);

        /*
         * Then: The we should have a json object that indicates the error.
         */
        context.assertIsSatisfied();
        String actualJsonString = response.getContentAsString();
        VisitorTrackingResponse actualResponse = (VisitorTrackingResponse)
        JSONObject.toBean(JSONObject.fromObject(actualJsonString), VisitorTrackingResponse.class);
        
        assertEquals("Response status should be an error",
                "ERROR",actualResponse.getStatus());
    }

    
}
