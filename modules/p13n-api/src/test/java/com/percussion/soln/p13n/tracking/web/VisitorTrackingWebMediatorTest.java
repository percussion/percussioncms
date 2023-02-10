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
import static org.hamcrest.Matchers.*;

import javax.servlet.http.Cookie;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.beans.HasPropertyWithValue;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.percussion.soln.p13n.tracking.IVisitorTrackingService;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.VisitorTrackingRequest;
import com.percussion.soln.p13n.tracking.web.VisitorTrackingWebMediator;
import com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils;


/**
 * Scenario description: 
 * @author adamgent, Mar 27, 2009
 */
@RunWith(JMock.class)
public class VisitorTrackingWebMediatorTest {

    Mockery context = new JUnit4Mockery();

    VisitorTrackingWebMediator mediator;

    IVisitorTrackingService tracking;
    MockHttpServletRequest request;
    MockHttpServletResponse response;
    VisitorProfile profile;

    @Before
    public void setUp() throws Exception {
        mediator = new VisitorTrackingWebMediator();
        tracking = context.mock(IVisitorTrackingService.class);
        mediator.setVisitorTrackingService(tracking);
        request = new MockHttpServletRequest();
        request.setMethod("GET");
        response = new MockHttpServletResponse();
        profile = new VisitorProfile();
        profile.setId(3L);

    }

    @Test
    public void shouldRetrieveVisitorProfileFromRequestParameters() {
        /*
         * Given: we provide a visitor profile id in the request parameter
         */
        mediator.setUsingRequestParameter(true);
        request.setParameter(VisitorTrackingWebUtils.VISITOR_PROFILE_ID_REQUEST_PARAM, "1"); 
        /* 
         * Expect: the tracking service to be called with a tracking request of id 1
         */
        context.checking(new Expectations() {{
            one(tracking).retrieveVisitor(with(aVisitorTrackRequestOfId(1L)));
            profile.setId(1L);
            will(returnValue(profile));
        }});
        /*
         * When: get the visitor profile from the request.
         */
        mediator.resolveProfile(null, request);
        
        /*
         * Then: we either have a profile or not but the tracking service was called.
         */
        
    }
    
    @Test
    public void shouldRetrieveVisitorProfileFromSession() {
        /*
         * Given: we provide a visitor profile in the session
         */
        mediator.setUsingRequestParameter(true);
        mediator.setUsingSession(true);
        profile.setId(3L);
        VisitorTrackingWebUtils.setVisitorProfileToSession(request.getSession(), profile);
        /* 
         * Expect: the tracking service should be called
         */
        context.checking(new Expectations() {{
            one(tracking).retrieveVisitor(with(aVisitorTrackRequestOfId(3L)));
            will(returnValue(profile));
        }});
        /*
         * When: get the visitor profile from the request.
         */
        mediator.resolveProfile(null, request);
        
        /*
         * Then: we have a profile from the session and the tracking service will be called.
         */
        
    }

    @Test
    public void shouldRetrieveVisitorProfileFromCooke() {
        /*
         * Given: we provide a visitor profile in the session
         */
        mediator.setUsingCookies(true);
        Cookie cookie = new Cookie(VisitorTrackingWebUtils.VISITOR_PROFILE_ID_COOKIE_NAME, ""+ 2L);
        request.setCookies(new Cookie[] { cookie});
        profile.setId(2L);
        
        /* 
         * Expect: the tracking service should be called
         */
        context.checking(new Expectations() {{
            one(tracking).retrieveVisitor(with(aVisitorTrackRequestOfId(2L)));
            will(returnValue(profile));
        }});
        /*
         * When: get the visitor profile from the request.
         */
        mediator.resolveProfile(null, request);
        
        /*
         * Then: we have a profile from the session and the tracking service will be called.
         */
        
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void shouldRetrieveProfileFromSessionIfSameAsCookie() throws Exception {
        mediator.setUsingCookies(true);
        mediator.setUsingSession(true);
        VisitorTrackingWebUtils.setVisitorProfileToSession(request.getSession(), profile);
        Cookie cookie = new Cookie(VisitorTrackingWebUtils.VISITOR_PROFILE_ID_COOKIE_NAME, ""+ 2L);
        context.checking(new Expectations() {{
            one(tracking).retrieveVisitor(with(allOf(
                    aVisitorTrackRequestWithProfile(profile),
                    aVisitorTrackRequestOfId(2L))));
            will(returnValue(profile));
        }});
        
        request.setCookies(new Cookie[] { cookie});
        profile.setId(2L);
        mediator.resolveProfile(null, request);
    }
    
    @Test
    public void shouldRetrieveProfileUsingCookieIfSessionProfileIsDifferent() throws Exception {
        mediator.setUsingCookies(true);
        mediator.setUsingSession(true);
        VisitorProfile sessionProfile = profile.clone();
        sessionProfile.setId(5L);
        assertThat(sessionProfile, is(not(equalTo(profile))));
        VisitorTrackingWebUtils.setVisitorProfileToSession(request.getSession(), sessionProfile);
        Cookie cookie = new Cookie(VisitorTrackingWebUtils.VISITOR_PROFILE_ID_COOKIE_NAME, ""+ 2L);
        context.checking(new Expectations() {{
            one(tracking).retrieveVisitor(with(aVisitorTrackRequestOfId(2L)));
            will(returnValue(profile));
        }});
        assertThat(sessionProfile, is(not(equalTo(profile))));
        request.setCookies(new Cookie[] { cookie});
        profile.setId(2L);
        mediator.resolveProfile(null, request);
    }
    

    @Test
    public void shouldCreateVisitorProfileFromServletRequest() {
        /*
         * Given: no visitor profile associated with user.
         */
        
        /* 
         * Expect: the tracking service should be called
         */
        context.checking(new Expectations() {{
            one(tracking).retrieveVisitor(with(aVisitorTrackRequestOfId(0L)));
            will(returnValue(profile));
        }});
        /*
         * When: get the visitor profile from the request.
         */
        mediator.resolveProfile(null, request);
        
        /*
         * Then: we have a profile from the session and the tracking service will be called.
         */
        
    }
    
    
    @Test
    public void shouldCreateVisitorProfileFromServletRequestIfCookieAndSessionAreBad() {
        /*
         * Given: the user has lots of bad profile info assocated to there request.
         */
        
        profile.setId(3L);
        mediator.setUsingSession(true);
        VisitorTrackingWebUtils.setVisitorProfileToSession(request.getSession(), profile);
        
        mediator.setUsingCookies(true);
        Cookie cookie = new Cookie(VisitorTrackingWebUtils.VISITOR_PROFILE_ID_COOKIE_NAME, ""+ 2L);
        request.setCookies(new Cookie[] { cookie});

        
        /* 
         * Expect: the tracking service should be called
         */
        final Sequence ss = context.sequence("strategySequence");
        context.checking(new Expectations() {{
            one(tracking).retrieveVisitor(with(aVisitorTrackRequestOfId(2L))); inSequence(ss);
            one(tracking).retrieveVisitor(with(aVisitorTrackRequestOfId(3L))); inSequence(ss);
            one(tracking).retrieveVisitor(with(aVisitorTrackRequestOfId(0L))); inSequence(ss);
            will(returnValue(new VisitorProfile()));
        }});
        /*
         * When: get the visitor profile from the request.
         */
        mediator.resolveProfile(null, request);
        
        /*
         * Then: we should have a new profile.
         */
        
    }
    
    @Test
    public void shouldReturnNullIfRequestParameterIsBad() {
        /*
         * Given: we provide a visitor profile id in the request parameter
         */
        mediator.setUsingRequestParameter(true);
        request.setParameter(VisitorTrackingWebUtils.VISITOR_PROFILE_ID_REQUEST_PARAM, "1");


        
        /* 
         * Expect: the tracking service should be called
         */
        final Sequence ss = context.sequence("strategySequence");
        context.checking(new Expectations() {{
            one(tracking).retrieveVisitor(with(HasPropertyWithValue.<VisitorTrackingRequest>hasProperty("createProfileWhenNotFound", equalTo(false))));
            inSequence(ss);
            will(returnValue(null));
        }});
        /*
         * When: get the visitor profile from the request.
         */
        VisitorProfile actual = mediator.resolveProfile(null, request);
        
        /*
         * Then: we should have a new profile.
         */
        assertNull(actual);
    }
    
    
    
    
    @Factory
    public  static VistorTrackingRequestIdMatcher aVisitorTrackRequestOfId( Long id ) {
        return new VistorTrackingRequestIdMatcher(id);
    }
    
    public static TypeSafeMatcher<VisitorTrackingRequest> aVisitorTrackRequestWithProfile(final VisitorProfile profile) {
        return new TypeSafeMatcher<VisitorTrackingRequest>() {

            VisitorProfile itemProfile;
            
            @Override
            public boolean matchesSafely(VisitorTrackingRequest item) {
                itemProfile = item.getVisitorProfile();
                return profile == itemProfile ||
                    profile.equals(item.getVisitorProfile());
            }

            public void describeTo(Description description) {
                description.appendText("Expected request with profile: ")
                    .appendValue(profile)
                    .appendText(" and request has profile: ")
                    .appendValue(itemProfile);
            }
            
        };
    }
    
    
    public static  class VistorTrackingRequestIdMatcher extends TypeSafeMatcher<VisitorTrackingRequest>  {

        private Long id;
        private Long actual;
        
        public VistorTrackingRequestIdMatcher(Long id) {
            super();
            this.id = id;
        }

        @Override
        public boolean matchesSafely(VisitorTrackingRequest item) {
            actual = item.getVisitorProfileId();
            return id == actual || id.equals(actual);            
        }

        public void describeTo(Description description) {
            description.appendText("Expecting VisitorTracking request with id: ")
                .appendValue(id)
                .appendText(" and request has id: ")
                .appendValue(actual);
        }

    }

}
