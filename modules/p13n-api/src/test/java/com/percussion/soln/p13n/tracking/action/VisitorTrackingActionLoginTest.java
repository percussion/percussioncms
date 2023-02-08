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

package test.percussion.soln.p13n.tracking.action;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
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
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorTrackingActionRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingException;
import com.percussion.soln.p13n.tracking.action.impl.VisitorTrackingActionLogin;

/**
 * Scenario description: 
 * @author adamgent, Aug 23, 2009
 */
@RunWith(JMock.class)
public class VisitorTrackingActionLoginTest {

    Mockery context = new JUnit4Mockery();
    VisitorTrackingActionLogin action;
    IVisitorProfileDataService dataService;
    VisitorProfile profile;
    VisitorTrackingActionRequest request;

    @Before
    public void setUp() throws Exception {
        action = new VisitorTrackingActionLogin();
        dataService = context.mock(IVisitorProfileDataService.class);
        profile = new VisitorProfile();
        request = new VisitorTrackingActionRequest();
    }

    @Test
    public void shouldLoginMergeWeightsInProfileAndSetExplicitWeightsInRequest() throws Exception {
        /*
         * Given: We already have a profile in the system for the user test.
         * The user test is trying to login but they already have an anonymous profile
         * with some weights set.
         * 
         * When they login we would like to explicitly set the seg b to 400
         * and the rest added together.
         */
        //Anon profile.
        profile.setUserId(null);
        profile.getSegmentWeights().put("a", 100);
        profile.getSegmentWeights().put("b", 100);
        //requesting to login as test with b set to 400.
        request.setUserId("test");
        Map<String,Integer> actionWeights = new HashMap<String, Integer>();
        actionWeights.put("b", 400);
        request.setSegmentWeights(actionWeights);
        
        final VisitorProfile user = profile.clone();
        user.setUserId("test");
        // a and b same as above via clone.
        user.getSegmentWeights().put("b", 1000);
        user.getSegmentWeights().put("c", 300);
        
        profile.getSegmentWeights().put("d", 100);
        

        /* 
         * Expect: To look for the user with the id test.
         */

        context.checking(new Expectations() {{ 
            one(dataService).findByUserId("test");
            will(returnValue(user));
        }});

        /*
         * When: login
         */

        VisitorProfile actual = action.processAction(request, profile, dataService);
        /*
         * Then: we should have a profile with the following weights.
         */
        Map<String,Integer> e = new HashMap<String, Integer>();
        e.put("a", 200); // a is 200 because 100 + 100
        e.put("b", 400); // b was set to 400 in the request.
        e.put("c", 300);
        e.put("d", 100);
        assertEquals(e, actual.getSegmentWeights());
    }
    
    
    @Test
    public void shouldTurnAnonymousProfileInToUserProfileIfNeverLoggedIn() throws Exception {
        /*
         * Given: first time the user is logged in.
         */
        request.setUserId("test");
        Map<String,Integer> rw = new HashMap<String, Integer>();
        rw.put("a", 100);
        request.setSegmentWeights(rw);
        profile.getSegmentWeights().put("a", 200);

        /* 
         * Expect: To not find the user test.
         */

        context.checking(new Expectations() {{ 
            one(dataService).findByUserId("test");
            will(returnValue(null));
        }});

        /*
         * When: login.
         */
        VisitorProfile actual = action.processAction(request, profile, dataService);

        /*
         * Then: Our returned profile should have the user id test and the explicitly set weights.
         */
        assertEquals("test", actual.getUserId());
        assertThat(actual.getSegmentWeights(), hasEntry("a", 100));
    }
    
    @Test
    public void shouldSwitchUsersIfAlreadyLoggedInAndRequestUserNameIsDifferent() throws Exception {
        request.setUserId("new");
        Map<String,Integer> rw = new HashMap<String, Integer>();
        rw.put("a", 555);
        rw.put("b", 444);
        profile.getSegmentWeights().put("c", 200);
        profile.setUserId("old");
        final VisitorProfile newProfile = profile.clone();
        newProfile.setUserId("new");
        newProfile.setSegmentWeights(new HashMap<String, Integer>());
        newProfile.getSegmentWeights().put("d", 400);
        
        context.checking(new Expectations() {{ 
            one(dataService).findByUserId("new");
            will(returnValue(newProfile));
        }});
        
        VisitorProfile actual = action.processAction(request, profile, dataService);
        Map<String,Integer> w = actual.getSegmentWeights();
        assertThat(actual.getUserId(), is("new"));
        assertThat(w.get("a"), nullValue());
        assertThat(w.get("c"), nullValue());
        assertThat(w, hasEntry("d", 400));
        assertThat(actual, notNullValue());
        
    }
    
    @Test(expected=VisitorTrackingException.class)
    public void shouldFailIfUserIdIsMissingInRequest() throws Exception {
        request.setUserId("");
        action.processAction(request,profile,dataService);
    }

}
