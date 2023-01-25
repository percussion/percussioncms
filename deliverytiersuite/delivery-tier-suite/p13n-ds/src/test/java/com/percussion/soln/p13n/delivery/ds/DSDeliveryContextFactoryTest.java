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

package com.percussion.soln.p13n.delivery.ds;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.Set;
import java.util.TreeSet;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.percussion.soln.p13n.delivery.DeliveryRequest;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.delivery.data.DeliveryListItem;
import com.percussion.soln.p13n.delivery.data.DeliverySnippetItem;
import com.percussion.soln.p13n.delivery.data.IDeliveryDataService;
import com.percussion.soln.p13n.delivery.ds.DSDeliveryContextFactory;
import com.percussion.soln.p13n.tracking.IVisitorProfileDataService;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.Segments;

//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.matchers.JUnitMatchers.*;

/**
 * Scenario description: Create a context from a delivery request.
 * @author adamgent, Jan 24, 2008
 */
@RunWith(JMock.class)
public class DSDeliveryContextFactoryTest {

    Mockery context = new JUnit4Mockery();

    DSDeliveryContextFactory factory;

    IDeliveryDataService deliveryDataService;
    
    ISegmentService segmentService;
    
    IVisitorProfileDataService profileRepository;
    final DeliveryListItem listItem = new DeliveryListItem();
    final DeliverySnippetItem snipItem = new DeliverySnippetItem();
    final Segment segmentA = makeSegment("segmentA");
    final Segment segmentB = makeSegment("segmentB");
    

    @Before
    public void setUp() throws Exception {
        factory = new DSDeliveryContextFactory();
        deliveryDataService = context.mock(IDeliveryDataService.class);
        segmentService = context.mock(ISegmentService.class);
        profileRepository = context.mock(IVisitorProfileDataService.class);
        
        factory.setDeliveryDataService(deliveryDataService);
        factory.setSegmentService(segmentService);
        listItem.setContentId(1);
        snipItem.setContentId(2);
        listItem.setSnippets(asList(snipItem));
        listItem.setSegmentIds(asSet("b"));
        snipItem.setSegmentIds(asSet("a","b"));
        
    }

    
    @Test
    public void shouldCreateContextFromDeliveryRequest() throws Exception {
        
        /*
         * Given: See setup.
         */
        
        //associate the visitor profile with segment A.
        //final IVisitorProfile profile = stubs.stubProfile(asList("a"));
        final VisitorProfile profile = new VisitorProfile(1, "test","Test");
        profile.getSegmentWeights().put("a", 1);
        
        DeliveryRequest request = createRequest(1, profile);

        /* 
         * Expect: To retrieve the delivery items, the visitor profiles,
         *         segments for the delivery items, and segments for the profile.
         */
        context.checking(new Expectations() {{
            //expect the delivery data service to be called
            one(deliveryDataService).getListItems(asList(1L));
            will(returnValue(asList(listItem)));
            
            //expect to retrieve segments for the profile.
            one(segmentService).retrieveSegments(asList("a"));
            will(returnValue(makeSegments(segmentA)));
            
            //expect to retrieve segments for the snippet item.
            one(segmentService).retrieveSegments(asList("a","b"));
            will(returnValue(makeSegments(segmentA,segmentB)));
            
            //expect to retrieve segments for the snippet item.
            one(segmentService).retrieveSegments(asList("b"));
            will(returnValue(makeSegments(segmentB)));
            
        }});

        /*
         * When: we use the factory to create the context.
         */
        IDeliverySnippetFilterContext deliveryContext = factory.createContext(request);
        
        /*
         * Then: We should have a valid delivery context.
         */

        context.assertIsSatisfied();
        assertNotNull(deliveryContext);
    }
    
    @Test
    public void shouldCreateContextFromDeliveryRequestWithNullProfile() throws Exception {
        
        /*
         * Given: see setup
         */
        final VisitorProfile profile = null;
        
        DeliveryRequest request = createRequest(1, profile);

        /* 
         * Expect: To retrieve the delivery items, the visitor profiles,
         *         segments for the delivery items, and segments for the profile.
         */
        context.checking(new Expectations() {{
            //expect the delivery data service to be called
            one(deliveryDataService).getListItems(asList(1L));
            will(returnValue(asList(listItem)));
            
            //expect not retrieve segments for our profile since its null.
            
            //expect to retrieve segments for the snippet item.
            one(segmentService).retrieveSegments(asList("a","b"));
            will(returnValue(makeSegments(segmentA,segmentB)));
            
            //expect to retrieve segments for the snippet item.
            one(segmentService).retrieveSegments(asList("b"));
            will(returnValue(makeSegments(segmentB)));
            
        }});

        /*
         * When: we use the factory to create the context.
         */
        IDeliverySnippetFilterContext deliveryContext = factory.createContext(request);
        
        /*
         * Then: We should have a valid delivery context.
         */

        context.assertIsSatisfied();
        assertNotNull(deliveryContext);
    }
    
    @Test
    public void shouldCreateContextFromDeliveryRequestThatContainsAListItem() throws Exception {
        
        /*
         * Given: see setup. Visitor profile, delivery request with list item
         *        and factory with listItemAllowed in request.
         */
        

        factory.setAllowListItemInRequest(true);
        final VisitorProfile profile = null;
        DeliveryRequest request = createRequest(1, profile);
        request.setListItem(listItem);


        /* 
         * Expect: To retrieve the delivery items, the visitor profiles,
         *         segments for the delivery items, and segments for the profile.
         */
        context.checking(new Expectations() {{
            //expect the delivery data service NEVER to be called
            never(deliveryDataService).getListItems(asList(1L));
            
            //expect not retrieve segments for our profile since its null.
            
            //expect to retrieve segments for the snippet item.
            one(segmentService).retrieveSegments(asList("a","b"));
            will(returnValue(makeSegments(segmentA,segmentB)));
            
            //expect to retrieve segments for the snippet item.
            one(segmentService).retrieveSegments(asList("b"));
            will(returnValue(makeSegments(segmentB)));
            
        }});

        /*
         * When: we use the factory to create the context.
         */
        IDeliverySnippetFilterContext deliveryContext = factory.createContext(request);
        
        /*
         * Then: We should have a valid delivery context.
         */

        context.assertIsSatisfied();
        assertNotNull(deliveryContext);
    }   
    
    private static <T> Set<T> asSet(T ... ts) {
        return new TreeSet<T>(asList(ts));
    }
    
    
    protected DeliveryRequest createRequest(Number id, VisitorProfile profile) {
        DeliveryRequest request = new DeliveryRequest();
        request.setListItemId(id.longValue());
        request.setVisitorProfile(profile);
        return request;
    }
    
    public Segment makeSegment(String name) {
        Segment seg = new Segment();
        seg.setName(name);
        return seg;
    }
    
    public Segments makeSegments(Segment ... segs) {
        return new Segments(asList(segs));
    }
}
