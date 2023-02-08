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

package test.percussion.soln.p13n.tracking.web;

import static org.junit.Assert.*;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.percussion.soln.p13n.tracking.IVisitorProfileDataService;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.ds.web.ProfileEditController;
import com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils;
import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.ISegmentTreeFactory;
import com.percussion.soln.segment.Segments;

//import static java.util.Arrays.*;
//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.matchers.JUnitMatchers.*;

/**
 * Scenario description: 
 * @author adamgent, Apr 11, 2008
 */
@RunWith(JMock.class)
public class ProfileEditControllerTest {

    Mockery context = new JUnit4Mockery();

    ProfileEditController controller;

    IVisitorProfileDataService dataService;
    ISegmentService segmentService;
    ISegmentTreeFactory segmentTreeFactory;

    @Before
    public void setUp() throws Exception {
        controller = new ProfileEditController();
        dataService = context.mock(IVisitorProfileDataService.class);
        segmentService = context.mock(ISegmentService.class);
        segmentTreeFactory = context.mock(ISegmentTreeFactory.class);
        controller.setVisitorProfileDataService(dataService);
        controller.setSegmentService(segmentService);
        controller.setSegmentTreeFactory(segmentTreeFactory);

    }

    @Test
    public void shouldSaveNewProfileOnlyToSession() throws Exception {
        /*
         * Given: We do not have a profile in the session yet and
         *        the profile we are working on does not exist in the repo.
         */
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setParameter("userId", "test_user");
        request.setParameter("segmentWeights[1]", "1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        

        
        /* 
         * Expect: to get Reference Data for View and NOT to save the profile
         */
        context.checking(new Expectations() {{ 
            one(dataService).retrieveTestProfiles();
            one(segmentTreeFactory).createSegmentTreeFromService(segmentService);
            one(segmentService).retrieveAllSegments();
            will(returnValue(new Segments()));
            never(dataService).save(with(any(VisitorProfile.class)));
        }});

        /*
         * When: Handle request
         */
        controller.handleRequest(request, response);
        
        /*
         * Then: We should now have a profile in the session.
         */
        VisitorProfile profile = VisitorTrackingWebUtils.getVisitorProfileFromSession(request.getSession());
        assertNotNull("We should have a profile in the session", profile);
        assertEquals("Profile should have segment weight", 
                (Integer) profile.getSegmentWeights().get("1"), new Integer(1));

        context.assertIsSatisfied();
    }

    
    @Test
    public void shouldReplaceProfileWithAnEmptyProfile() throws Exception {
        /*
         * Given: We have a profile in the session and we are requesting
         *        to clear it out with an empty profile.
         */
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setParameter("newProfile", "true");
        VisitorProfile oldProfile = new VisitorProfile();
        oldProfile.setUserId("old");
        VisitorTrackingWebUtils.setVisitorProfileToSession(request.getSession(),oldProfile);
        MockHttpServletResponse response = new MockHttpServletResponse();
        

        
        /* 
         * Expect: to get Reference Data for View and NOT to save the profile
         */
        context.checking(new Expectations() {{ 
            one(dataService).retrieveTestProfiles();
            one(segmentTreeFactory).createSegmentTreeFromService(segmentService);
            one(segmentService).retrieveAllSegments();
            will(returnValue(new Segments()));
            never(dataService).save(with(any(VisitorProfile.class)));
        }});

        /*
         * When: Handle request
         */
        controller.handleRequest(request, response);
        
        /*
         * Then: We should now have a profile in the session.
         */
        VisitorProfile profile = VisitorTrackingWebUtils.getVisitorProfileFromSession(request.getSession());
        assertNotNull("We should have a profile in the session", profile);
        assertNotSame("The new profile should not be the old profile", oldProfile, profile);
        assertTrue(profile.getUserId() == null);

        context.assertIsSatisfied();
    }
    
    @Test
    public void shouldSaveProfileOnlyToSession() throws Exception {
        /*
         * Given: We do not have a profile in the session yet and
         *        the profile we are working on does exist in the repo.
         */
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setParameter("userId", "test_user");
        request.setParameter("segmentWeights[1]", "1");
        request.setParameter("id", "1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        

        
        /* 
         * Expect: to get Reference Data for the View, retrieve the requested profile
         *        and NOT to save the profile.
         */
        final VisitorProfile createdProfile = new VisitorProfile();
        createdProfile.getSegmentWeights().put("1", 1);
        createdProfile.setId(1L);
        
        context.checking(new Expectations() {{ 
            one(dataService).retrieveTestProfiles();
            one(segmentTreeFactory).createSegmentTreeFromService(segmentService);
            one(segmentService).retrieveAllSegments();
            will(returnValue(new Segments()));
            
            one(dataService).find(1L);
            will(returnValue(createdProfile));
            
            never(dataService).save(with(any(VisitorProfile.class)));
        }});

        /*
         * When: Handle request
         */
        controller.handleRequest(request, response);
        
        /*
         * Then: We should now have a profile in the session.
         */
        VisitorProfile profile = VisitorTrackingWebUtils.getVisitorProfileFromSession(request.getSession());
        assertSame("Our created profile should be the same object as the one in the session",
                createdProfile, profile);
        assertNotNull("We should have a profile in the session", profile);
        assertEquals("Profile should have segment weight", 
                (Integer) profile.getSegmentWeights().get("1"), new Integer(1));

        context.assertIsSatisfied();
    }
    
    
    
    @Test
    public void shouldSaveProfileToRepositoryAndSession() throws Exception {
        /*
         * Given: We may or may not have a profile in the session yet and
         *        the profile we are working on does exist in the repo.
         */
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setParameter("userId", "test_user");
        request.setParameter("label", "test_label");
        request.setParameter("saveProfile", "true");
        request.setParameter("segmentWeights[2]", "2");
        request.setParameter("segmentWeights[1]", "");
        request.setParameter("id", "1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        VisitorProfile oldProfile = new VisitorProfile();
        VisitorTrackingWebUtils.setVisitorProfileToSession(request.getSession(), oldProfile);
        

        
        /* 
         * Expect: to get Reference Data for the View, retrieve the requested profile
         */
        final VisitorProfile createdProfile = new VisitorProfile();
        createdProfile.getSegmentWeights().put("1", 1);
        createdProfile.setId(1L);
        
        context.checking(new Expectations() {{ 
            one(dataService).retrieveTestProfiles();
            one(segmentTreeFactory).createSegmentTreeFromService(segmentService);
            one(segmentService).retrieveAllSegments();
            will(returnValue(new Segments()));
            
            one(dataService).find(1L);
            will(returnValue(createdProfile));
            
            one(dataService).save(with(same(createdProfile)));
        }});

        /*
         * When: Handle request
         */
        controller.handleRequest(request, response);
        
        /*
         * Then: We should now have a profile in the session.
         */
        VisitorProfile profile = VisitorTrackingWebUtils.getVisitorProfileFromSession(request.getSession());
        assertSame("Our created profile should be the same object as the one in the session",
                createdProfile, profile);
        assertNotSame("Our session profile should not be the same object as the old session object,",
                oldProfile, profile);
        assertNotNull("We should have a profile in the session", profile);
        assertEquals("Profile should have segment weight 2-2", 
                (Integer) profile.getSegmentWeights().get("2"), new Integer(2));
        assertNull("Profile should not have segment weight 1", (Integer) profile.getSegmentWeights().get("1"));

        context.assertIsSatisfied();
    }

}

