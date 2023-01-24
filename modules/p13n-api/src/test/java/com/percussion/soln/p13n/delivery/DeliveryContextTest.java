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

package test.percussion.soln.p13n.delivery;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.percussion.soln.p13n.delivery.IDeliveryResponseListItem;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;
import com.percussion.soln.p13n.delivery.impl.DeliveryContext;
import com.percussion.soln.p13n.delivery.impl.DeliverySegmentUtil;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.Segments;

//import static java.util.Arrays.*;
//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.matchers.JUnitMatchers.*;

/**
 * Scenario description: 
 * @author adamgent, Jul 15, 2008
 */
@RunWith(JMock.class)
public class DeliveryContextTest {

    Mockery mockery = new JUnit4Mockery();
    DeliveryContext deliveryContext;
    ISegmentService segmentService;
    IDeliveryResponseListItem listItem;
    IDeliveryResponseSnippetItem snipItemA;
    IDeliveryResponseSnippetItem snipItemB;
    VisitorProfile visitorProfile;
    

    @Before
    public void setUp() throws Exception {
        
        segmentService = mockery.mock(ISegmentService.class);
        listItem = mockery.mock(IDeliveryResponseListItem.class);
        snipItemA = mockery.mock(IDeliveryResponseSnippetItem.class, "snipItemA");
        snipItemB = mockery.mock(IDeliveryResponseSnippetItem.class, "snipItemB");
        visitorProfile = new VisitorProfile();

    }

    @Test
    public void shouldCreateDeliveryContext() throws Exception {
        /*
         * Given: TODO initial setup.
         */

        /* 
         * Expect: TODO expect some methods to be called on the dataService.
         */

        mockery.checking(new Expectations() {{
           List<String> emptyList = emptyList();
           one(segmentService).retrieveSegments(with(equal(emptyList)));
           will(returnValue(new Segments()));
           
        }});

        /*
         * When: TODO executes some behavior on the SUT
         */
        deliveryContext = 
            new DeliveryContext(listItem, asList(snipItemA, snipItemB),visitorProfile, segmentService);
        /*
         * Then: TODO check to see if the behavior is correct.
         */

        mockery.assertIsSatisfied();
    }
    
    
    @Test
    public void shouldGetSegmentsForVisitorProfile() throws Exception {
        /*
         * Given: a profile with some valid segment ids and some invalid segment ids.
         */

        /* 
         * Expect: the segment service to return a list with null 
         * entries for the invalid segment ids.
         */
        visitorProfile.getSegmentWeights().put("1", 1);
        visitorProfile.getSegmentWeights().put("2", 1);
        visitorProfile.getSegmentWeights().put("3", 1);
        final Segment segA = new Segment();
        final Segment segB = new Segment();
        
        mockery.checking(new Expectations() {{
           one(segmentService).retrieveSegments(with(containsValues(asList("1","2", "3"))));
           will(returnValue(new Segments(asList(segA,null, null))));
           
           one(segmentService).retrieveSegmentsWithNameOrAlias(with(isOneOf("1","2","3")));
           will(returnValue(new Segments(asList(segB))));
           
           one(segmentService).retrieveSegmentsWithNameOrAlias(with(isOneOf("1","2","3")));
           will(returnValue(new Segments()));
        }});

        /*
         * When:
         */
        Collection<? extends Segment> segments = 
            DeliverySegmentUtil.getVisitorProfileSegments(segmentService, visitorProfile, null);
        /*
         * Then: we should only have one segment.
         */
        
        assertEquals("we should only have one segment", 2, segments.size());
        assertSame(segA, segments.iterator().next());
        mockery.assertIsSatisfied();
    }
    
    @Factory
    public static Matcher<List<String>> containsValues( List<String> list ) {
        return new SetEqualMatcher(list);
    } 
    
    public static class SetEqualMatcher extends TypeSafeMatcher<List<String>> {

        private Set<String> values;
        
        public SetEqualMatcher(Collection<String> values) {
            super();
            this.values = new HashSet<String>(values);
        }

        @Override
        public boolean matchesSafely(List<String> item) {
            if (values == null) throw new IllegalStateException("values cannot be null");
            return values.equals(new HashSet<String>(item));
        }

        public void describeTo(Description description) {
            description.appendText(" a collection that contains all of ").appendValue(values);
        }
    
    }
    

}
