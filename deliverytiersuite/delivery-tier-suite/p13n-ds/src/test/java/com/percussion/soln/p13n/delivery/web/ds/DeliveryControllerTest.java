package test.percussion.soln.p13n.delivery.web.ds;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.percussion.soln.p13n.delivery.DeliveryRequest;
import com.percussion.soln.p13n.delivery.DeliveryResponse;
import com.percussion.soln.p13n.delivery.DeliveryResponseItem;
import com.percussion.soln.p13n.delivery.IDeliveryService;
import com.percussion.soln.p13n.delivery.data.DeliveryListItem;
import com.percussion.soln.p13n.delivery.ds.web.DeliveryController;
import com.percussion.soln.p13n.delivery.web.DeliveryWebUtils;
import com.percussion.soln.p13n.tracking.IVisitorTrackingService;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorTrackingRequest;
import com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils;
import com.percussion.soln.p13n.tracking.web.VisitorTrackingWebMediator;


//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.matchers.JUnitMatchers.*;

/**
 * Scenario description: 
 * @author adamgent, Jan 25, 2008
 */
@RunWith(JMock.class)
public class DeliveryControllerTest {

    Mockery context = new JUnit4Mockery();

    DeliveryController deliveryController;

    IDeliveryService deliveryService;
    IVisitorTrackingService visitorTrackingService;
    
    private String listItemIdParam = "listItemId";
    MockHttpServletRequest request;
    MockHttpServletResponse response;

    @Before
    public void setUp() throws Exception {
        deliveryController = new DeliveryController();
        deliveryService = context.mock(IDeliveryService.class);
        deliveryController.setDeliveryService(deliveryService);
        visitorTrackingService = context.mock(IVisitorTrackingService.class);
        VisitorTrackingWebMediator mediator =  new VisitorTrackingWebMediator();
        mediator.setVisitorTrackingService(visitorTrackingService);
        deliveryController.setVisitorTrackingHttpService(mediator);
        
        request = new MockHttpServletRequest();
        request.setMethod("GET");
        response = new MockHttpServletResponse();
        
    }

    @Test
    public void shouldHandleAValidDeliveryRequest() throws Exception {
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        /*
         * Given: a valid delivery request.
         */
        final VisitorProfile profile = new VisitorProfile();
        request.setParameter(listItemIdParam, "1");
        VisitorTrackingWebUtils.setVisitorProfileToSession(request.getSession(), profile);
        
        final DeliveryResponse deliveryResponse = createDeliveryResponse();
        
        expectTrackAndDeliver(profile, deliveryResponse);

        /*
         * When: Handle a valid delivery request
         */
        deliveryController.handleRequest(request, response);

        /*
         * Then: TODO check to see if the behavior is correct.
         */
        String actualJsonString = response.getContentAsString();
        String expectedJsonString = "{\"errorId\":\"\"," +
        		"\"errorMessage\":\"\"," +
        		"\"listItemId\":0," +
        		"\"snippetItems\":[{\"rendering\":\"HELLO WORLD\",\"score\":0,\"style\":\"color:red\"}]," +
        		"\"status\":\"OK\"}";
        context.assertIsSatisfied();
        assertEquals("JSON Object String should be the same",
                expectedJsonString,actualJsonString);
    }
    

    @Test
    public void shouldHandleAValidDeliveryRequestAsJSONP() throws Exception {
        
        /*
         * For JSONP:
         * see http://docs.jquery.com/Ajax/jQuery.getJSON#urldatacallback
         * and http://bob.pythonmac.org/archives/2005/12/05/remote-json-jsonp/
         */
        
        /*
         * Given: a valid delivery request with a jsonp callback.
         */
        final VisitorProfile profile = new VisitorProfile();
        request.setParameter(listItemIdParam, "1");
        request.setParameter("jsoncallback", "MyJSONP");
        VisitorTrackingWebUtils.setVisitorProfileToSession(request.getSession(), profile);
        
        final DeliveryResponse deliveryResponse = createDeliveryResponse();
        
        expectTrackAndDeliver(profile, deliveryResponse);

        /*
         * When: Handle a valid delivery request
         */
        deliveryController.handleRequest(request, response);

        /*
         * Then: TODO check to see if the behavior is correct.
         */
        String actualJsonString = response.getContentAsString();
        String expectedJsonString = "MyJSONP({\"errorId\":\"\"," +
        		"\"errorMessage\":\"\"," +
        		"\"listItemId\":0," +
        		"\"snippetItems\":[{\"rendering\":\"HELLO WORLD\",\"score\":0,\"style\":\"color:red\"}]," +
        		"\"status\":\"OK\"})";
        context.assertIsSatisfied();
        assertEquals("JSON Object String should be the same",
                expectedJsonString,actualJsonString);
    }

    private void expectTrackAndDeliver(final VisitorProfile profile, final DeliveryResponse deliveryResponse) {
        context.checking(new Expectations() {{
            one(visitorTrackingService).retrieveVisitor(with(any(VisitorTrackingRequest.class)));
            will(returnValue(profile));
            one(deliveryService).deliver(with(any(DeliveryRequest.class)));
            will(returnValue(deliveryResponse));
        }});
    }

    @Test
    public void shouldHandleAnInValidDeliveryRequest() throws Exception {
        /*
         * Given: an invalid request. No parameters are set.
         */
        
        /* 
         * Expect: The service should NOT be called since spring will handle the validation.
         */
        context.checking(new Expectations() {{ 
            never(deliveryService).deliver(with(any(DeliveryRequest.class)));
            one(visitorTrackingService).retrieveVisitor(with(any(VisitorTrackingRequest.class)));
            will(returnValue(new VisitorProfile()));

        }});

        /*
         * When: Handle the request
         */
        deliveryController.handleRequest(request,response);

        /*
         * Then: The we should have a json object that indicates the error.
         */
        context.assertIsSatisfied();
        String actualJsonString = response.getContentAsString();
        DeliveryResponse actualResponse = (DeliveryResponse) 
            JSONObject.toBean(JSONObject.fromObject(actualJsonString), DeliveryResponse.class);
        ;
        assertEquals("Response status should be an error",
                "ERROR",actualResponse.getStatus());
        assertEquals("field.required", actualResponse.getErrorId());
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }
    
    
    @Test
    public void shouldHandleAValidJSONPost() throws Exception {
        

        /*
         * Given: a valid delivery request.
         */
        request.setMethod("POST");
        
        
        final VisitorProfile profile = new VisitorProfile();
        VisitorTrackingWebUtils.setVisitorProfileToSession(request.getSession(), profile);
        String json = createJSONDeliveryRequest();
        request.setContent(json.getBytes("UTF-8"));
        request.setContentType("application/json");
        
        /* 
         * Expect: The delivery service to process our request into a response
         *         with out using the visitor profile data service since our profile
         *         is in the session.
         */
        final DeliveryResponse deliveryResponse = createDeliveryResponse();
        
        expectTrackAndDeliver(profile, deliveryResponse);

        /*
         * When: Handle a valid delivery request
         */
        deliveryController.handleRequest(request, response);

        /*
         * Then: TODO check to see if the behavior is correct.
         */
        assertJsonResponse();
    }

    private void assertJsonResponse() throws UnsupportedEncodingException {
        String actualJsonString = response.getContentAsString();
        String expectedJsonString = "{\"errorId\":\"\"," +
                "\"errorMessage\":\"\"," +
                "\"listItemId\":0," +
                "\"snippetItems\":[{\"rendering\":\"HELLO WORLD\",\"score\":0,\"style\":\"color:red\"}]," +
                "\"status\":\"OK\"}";
        assertEquals("JSON Object String should be the same",
                expectedJsonString,actualJsonString);
    }
    
    
    @Test
    public void testRealJSONPost1() throws Exception {
        

        /*
         * Given: a valid delivery request.
         */
        request.setMethod("POST");
        
        
        final VisitorProfile profile = new VisitorProfile();
        VisitorTrackingWebUtils.setVisitorProfileToSession(request.getSession(), profile);
        String json = "{\"listItem\":{\"contentId\":724,\"contentType\":\"\",\"id\":724," +
        		"\"properties\":{\"rx:soln_list_dateRangeStart\":\"\",\"rx:soln_list_titleContains\":\"\"," +
        		"\"rx:soln_list_maxResults\":\"\",\"rx:soln_list_type\":\"Auto\"," +
        		"\"rx:soln_list_dateRangeEnd\":\"\",\"rx:soln_list_snippet\":\"rffSnTitleLink\"," +
        		"\"rx:soln_p13n_filterMin\":\"\",\"rx:soln_p13n_filterMax\":\"\",\"rx:soln_list_jcrQuery\":\"\"," +
        		"\"rx:soln_list_contentType\":\"rffEvent\"},\"segmentIds\":[]," +
        		"\"snippetFilterIds\":[\"soln.p13n.filter.BestMatchScoring\",\"soln.p13n.filter.SortBasedOnScore\"]," +
        		"\"snippets\":[{\"contentId\":722,\"contentType\":\"\",\"id\":0,\"properties\":{}," +
        		"\"rendering\":\"\\n    <span class=\\\"lead_snippet\\\">\\n\\t<a href=\\\"$pagelink\\\" class=\\\"titlelink\\\">\\n\\t  Chris' Birthday\\t</a>\\n    </span>\\n  \"," +
        		"\"segmentIds\":[\"718\"]}]},\"listItemId\":724,\"visitorProfile\":null}";
        request.setContent(json.getBytes("UTF-8"));
        request.setContentType("application/json; charset=utf-8");
        
        /* 
         * Expect: The delivery service to process our request into a response
         *         with out using the visitor profile data service since our profile
         *         is in the session.
         */
        final DeliveryResponse deliveryResponse = createDeliveryResponse();
        
        expectTrackAndDeliver(profile, deliveryResponse);

        /*
         * When: Handle a valid delivery request
         */
        deliveryController.handleRequest(request, response);

        assertJsonResponse();
    }


    private DeliveryResponse createDeliveryResponse() {
        final DeliveryResponse deliveryResponse = new DeliveryResponse();
        DeliveryResponseItem item = new DeliveryResponseItem();
        item.setRendering("HELLO WORLD");
        item.setStyle("color:red");
        deliveryResponse.setSnippetItems(asList(item));
        deliveryResponse.setStatus("OK");
        return deliveryResponse;
    }
    
    
    @Test
    public void shouldHandleInValidJSONPost() throws Exception {
        

        /*
         * Given: a valid delivery request.
         */
        request.setMethod("POST");
        
        
        final VisitorProfile profile = new VisitorProfile();
        VisitorTrackingWebUtils.setVisitorProfileToSession(request.getSession(), profile);
        String json = "{e}";
        request.setContent(json.getBytes("UTF-8"));
        request.setContentType("application/json");
        
        /* 
         * Expect: The delivery service never to be called as
         * we will have json errors before we reach.
         */
        
        context.checking(new Expectations() {{ 
            one(visitorTrackingService).retrieveVisitor(with(any(VisitorTrackingRequest.class)));
            will(returnValue(profile));
            never(deliveryService).deliver(with(any(DeliveryRequest.class)));
        }});

        /*
         * When: Handle a valid delivery request
         */
        deliveryController.handleRequest(request, response);

        /*
         * Then: we should fail
         */
        
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        
    }

    private String createJSONDeliveryRequest() {
        DeliveryRequest dr = new DeliveryRequest();
        DeliveryListItem listItem = new DeliveryListItem();
        listItem.setContentType("PromoList");
        listItem.setContentId(301);
        listItem.setSnippetFilterIds(asList("filterA", "filterB"));
        HashSet<String> segIds = new HashSet<String>();
        segIds.add("segA");
        segIds.add("segB");
        listItem.setSegmentIds(segIds);
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("a", "1");
        listItem.setProperties(props);
        dr.setListItem(listItem);
        String json = DeliveryWebUtils.requestToJson(dr).toString();
        return json;
    }
    
    @Test
    public void shouldJSONToDeliveryRequest() throws Exception {
        TestDeliveryController controller = new TestDeliveryController();
        String jsonDr = createJSONDeliveryRequest();
        DeliveryRequest dr = controller.jsonToDeliveryRequest(jsonDr);
        log.debug("JSON - " + jsonDr);
        log.debug("Object - " + dr);
        assertEquals(dr.getListItem().getContentType(), "PromoList");
        assertEquals(dr.getListItem().getSnippetFilterIds(), asList("filterA", "filterB"));

    }
    
    
    public static class TestDeliveryController extends DeliveryController {
        @Override
        public DeliveryRequest jsonToDeliveryRequest(String json) {
            return super.jsonToDeliveryRequest(json);
        }
    }
    
    
    
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(DeliveryControllerTest.class);
    
}
