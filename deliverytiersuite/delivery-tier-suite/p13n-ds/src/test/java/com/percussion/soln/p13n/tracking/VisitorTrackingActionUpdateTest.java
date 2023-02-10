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

package com.percussion.soln.p13n.tracking;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
import com.percussion.soln.p13n.tracking.action.impl.VisitorTrackingActionUpdate;

/**
 * Scenario description:
 * 
 * @author Stephen Bolton
 */
@RunWith(JMock.class)
public class VisitorTrackingActionUpdateTest {

	IVisitorTrackingAction action;
	
	Mockery context = new JUnit4Mockery();
	
	@Before
	public void setUp() throws Exception {	
	    action = new VisitorTrackingActionUpdate();
	}

	@Test
    public void shouldProcessUpdateAction() throws Exception {
        /*
         * Setup request
         */
        final long profileId = 1L;

        final VisitorTrackingActionRequest request = new VisitorTrackingActionRequest();
        request.setVisitorProfileId(profileId);
        request.setActionName("update");
        Map<String, Integer> reqSegmentWeights = new HashMap<String, Integer>();
        reqSegmentWeights.put("seg1", 1);
        reqSegmentWeights.put("seg2", 2);
        reqSegmentWeights.put("seg3", 3);
        request.setSegmentWeights(reqSegmentWeights);

        final IVisitorProfileDataService dataService = context
                .mock(IVisitorProfileDataService.class);
        /*
         * Expect: no collaboration
         */

        // Found profile, set values
        final VisitorProfile profile = new VisitorProfile();
        profile.setId(profileId);
        Map<String, Integer> segmentWeights = new HashMap<String, Integer>();
        segmentWeights.put("seg1", 1);
        segmentWeights.put("seg2", 2);
        profile.setSegmentWeights(segmentWeights);
        profile.setLastUpdated(null);

        /*
         * When: deliver() is called
         */

        VisitorProfile returnVal = action.processAction(request, profile,
                dataService);
        segmentWeights = profile.getSegmentWeights();

        context.assertIsSatisfied();

        assertEquals("seg1 not incremented by 1", 2, segmentWeights.get("seg1")
                .longValue());

        assertEquals("seg2 not incremented by 2", 4, segmentWeights.get("seg2")
                .longValue());

        assertEquals("seg3 that did not exist not incremented by 3", 3,
                segmentWeights.get("seg3").longValue());

        assertNotNull(returnVal);

    }
	
    @Test
    public void shouldProcessUpdateActionNoExistingProfile() throws Exception {
        /*
         * Setup request
         */
        final long profileId = 1L;

        final VisitorTrackingActionRequest request = new VisitorTrackingActionRequest();
        request.setVisitorProfileId(profileId);
        request.setActionName("update");
        Map<String, Integer> reqSegmentWeights = new HashMap<String, Integer>();
        reqSegmentWeights.put("seg1", 1);
        reqSegmentWeights.put("seg2", 2);
        reqSegmentWeights.put("seg3", 3);
        request.setSegmentWeights(reqSegmentWeights);

        final IVisitorProfileDataService dataService = context.mock(IVisitorProfileDataService.class);

        /*
         * Expect: no collaboration
         */

        // Found profile, set values
        final VisitorProfile profile = new VisitorProfile();
        final long newProfileId = UUID.randomUUID().getMostSignificantBits();
        profile.setId(newProfileId);
        Map<String, Integer> segmentWeights = new HashMap<String, Integer>();
        profile.setSegmentWeights(segmentWeights);

        /*
         * When: deliver() is called
         */
        VisitorProfile returnVal = action.processAction(request, profile, dataService);
        segmentWeights = profile.getSegmentWeights();

        context.assertIsSatisfied();

        assertEquals("Id in return value not the same as created by Data Service", profile, returnVal);

        assertEquals("seg1 not set to  1", 1, segmentWeights.get("seg1").longValue());

        assertEquals("seg2 not set to  2", 2, segmentWeights.get("seg2").longValue());

        assertEquals("seg3 not set to 3", 3, segmentWeights.get("seg3").longValue());

    }
	
}
