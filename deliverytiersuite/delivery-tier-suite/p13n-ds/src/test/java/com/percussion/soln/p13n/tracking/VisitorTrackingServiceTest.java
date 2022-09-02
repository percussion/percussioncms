package test.percussion.soln.p13n.tracking;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.percussion.soln.p13n.tracking.IVisitorProfileDataService;
import com.percussion.soln.p13n.tracking.IVisitorTrackingAction;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorTrackingActionRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingException;
import com.percussion.soln.p13n.tracking.VisitorTrackingResponse;
import com.percussion.soln.p13n.tracking.impl.VisitorTrackingService;

/**
 * Scenario description:
 * 
 * @author adamgent
 */
@RunWith(JMock.class)
public class VisitorTrackingServiceTest {
	private VisitorTrackingService tracker;
	IVisitorTrackingAction action;
	Mockery context = new JUnit4Mockery();
	IVisitorProfileDataService dataService;
	VisitorProfile profile;
	VisitorTrackingActionRequest request;
	
	@Before
	public void setUp() throws Exception {
	    final Map<String, IVisitorTrackingAction> trackingActions = 
	        new HashMap<String, IVisitorTrackingAction>();
		VisitorTrackingService myTracker = new VisitorTrackingService() {
		    {
		        setTrackingActions(trackingActions);
		    }
		};
		dataService = context.mock(IVisitorProfileDataService.class);
		myTracker.setVisitorProfileDataService(dataService);
		action = context.mock(IVisitorTrackingAction.class);
		
		tracker = myTracker;
		profile = new VisitorProfile();
	    profile.setId(1L);
        Map<String,Integer> segmentWeights = new HashMap<String,Integer>();
        segmentWeights.put("seg1",1);
        segmentWeights.put("seg2",2);
        profile.setSegmentWeights(segmentWeights);
        
        request = new VisitorTrackingActionRequest();
        request.setVisitorProfileId(profile.getId());
        
        trackingActions.put("update", action);
	}

	@Test
	public void shouldProcessActionWithProfileNotInRequest() throws Exception {
		/*
		 * Setup request
		 */
		request.setActionName("update");
	
		/*
		 * Expect: The service to retrieve our profile and call our action.
		 */;    
        
		context.checking(new Expectations() {{
		    
		    one(dataService).find(1L);
		    will(returnValue(profile));
		    
		    one(action).processAction(
		            with(equal(request)),
		            with(equal(profile)),
		            with(any(IVisitorProfileDataService.class)));
		    will(returnValue(profile));
		    
            one(dataService).save(profile);
            will(returnValue(profile));
		}});
		/*
		 * When: track() is called
		 */
		VisitorTrackingResponse response = tracker.track(request);
		
		/*
		 * Then: should create a new visitor profile with valid id
		 */
		 context.assertIsSatisfied();
		 
		 assertEquals("Response status should be an error",
	                "OK",response.getStatus());
	}
	
	   @Test
	    public void shouldProcessActionWithProfileInRequest() throws Exception {
	        /*
	         * Setup request
	         */
	        request.setActionName("update");
	        request.setVisitorProfile(profile);
	    
	        /*
	         * Expect: The service to retrieve our profile and call our action.
	         */;    
	        
	        context.checking(new Expectations() {{
	            
	            // The data service should not be used because 
	            // the profile is in the request.
	            never(dataService).find(with(any(Long.class)));
	            
	            one(action).processAction(
	                    with(equal(request)),
	                    with(equal(profile)),
	                    with(any(IVisitorProfileDataService.class)));
	            will(returnValue(profile));
	            
	            one(dataService).save(profile);
	            will(returnValue(profile));
	        }});
	        /*
	         * When: track() is called
	         */
	        VisitorTrackingResponse response = tracker.track(request);
	        
	        /*
	         * Then: should create a new visitor profile with valid id
	         */
	         context.assertIsSatisfied();
	         
	         assertEquals("Response status should be an error",
	                    "OK",response.getStatus());
	    }
	   
	@Test
	public void shouldFailIfActionDoesNotExist()  throws Exception{
		/*
		 * Setup request
		 */
		request.setActionName("doesnotexist");
		
		/*
		 * Expect: no collaboration
		 */
		
		final VisitorTrackingResponse visitorTrackingResponse = new VisitorTrackingResponse();
        visitorTrackingResponse.setStatus("OK");
        
		context.checking(new Expectations() {{ 
          one(dataService).find(1L);
            will(returnValue(profile));
            
		    never(action).processAction(
		            with(any(VisitorTrackingActionRequest.class)),
		            with(any(VisitorProfile.class)),
		            with(any(IVisitorProfileDataService.class)));
		}});
		
		/*
		 * When: track() is called
		 */
		
		VisitorTrackingResponse response = tracker.track(request);
		
		/*
		 * Then: should create a new visitor profile with valid id
		 */
		 assertEquals("Response status should be an error",
	                "ERROR",response.getStatus());
   
	}
	
    @Test
    public void shouldNotSaveProfileIfProfileIsLocked() throws Exception {
        /*
         * Setup request make sure the profile is locked.
         */
        profile.setLockProfile(true);
        request.setActionName("test");
        
        /*
         * Expect an action that will alter the profile.
         */
        tracker.registerVisitorTrackingAction("test", new IVisitorTrackingAction() {
            public VisitorProfile processAction(
                    VisitorTrackingActionRequest request,
                    VisitorProfile profile, IVisitorProfileDataService ds)
                    throws VisitorTrackingException {
                profile.getSegmentWeights().put("altered", 1);
                return profile;
            }
        });
        request.setVisitorProfile(profile);
    
        /*
         * Expect: The service to retrieve our profile and call our action.
         */;    
        
        context.checking(new Expectations() {{
            
            // The data service should not be used because 
            // the profile is in the request.
            never(dataService).find(with(any(Long.class)));
            
            /*
             * Visitor Profile is locked and should not be saved.
             */
            never(dataService).save(with(any(VisitorProfile.class)));
            
        }});
        /*
         * When: track() is called
         */
        VisitorTrackingResponse response = tracker.track(request);
        
        context.assertIsSatisfied();
        
        assertEquals("Response status should not be an error",
                    "WARN",response.getStatus());
        assertTrue("The profile should not be altered", 
                response.getVisitorProfile().getSegmentWeights().get("altered") == null);
    }
    
    @Test
    public void shouldRetrieveVisitor() {
        /*
         * Given: a visitor Id.
         */
        final Long visitorId = 1L;
        request.setId(visitorId);
        request.setVisitorProfile(null);

        /* 
         * Expect: The data service to find a profile;
         */
        final VisitorProfile profile = new VisitorProfile();
        profile.setId(visitorId);
        context.checking(new Expectations() {{
            one(dataService).find(visitorId);
            will(returnValue(profile));
        }});

        /*
         * When:
         */
        VisitorProfile actual = tracker.retrieveVisitor(request);

        /*
         * Then: 
         */
        assertEquals(1L,actual.getId());
        
    }
   
}
