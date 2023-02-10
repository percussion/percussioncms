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

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.percussion.soln.p13n.delivery.DeliveryResponse;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContext;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilterContextFactory;
import com.percussion.soln.p13n.delivery.DeliveryRequest;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;
import com.percussion.soln.p13n.delivery.IDeliveryResponseListItem;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;
import com.percussion.soln.p13n.delivery.impl.AbstractDeliveryService;
import com.percussion.soln.p13n.delivery.impl.SpringDeliveryService;
import com.percussion.soln.p13n.tracking.VisitorProfile;

//import static org.hamcrest.CoreMatchers.*;
//import static org.hamcrest.Matchers.*;
//import static org.junit.matchers.JUnitMatchers.*;

@RunWith(JMock.class)
public class AbstractDeliveryServiceTest {
    //TODO: More tests needed.
    Mockery context = new JUnit4Mockery();
    AbstractDeliveryService deliveryService;
    DeliveryRequest deliveryRequest;
    IDeliverySnippetFilterContextFactory contextFactory;
    Sequence filterChain;
    IDeliverySnippetFilter filter;
    IDeliveryResponseListItem filterItem;
    IDeliverySnippetFilterContext deliveryContext;
    
    @Before
    public void setUp() throws Exception {
        deliveryService = new SpringDeliveryService();
        deliveryRequest = createDeliveryRequest(1, null);
        contextFactory = context.mock(IDeliverySnippetFilterContextFactory.class);
        filter = context.mock(IDeliverySnippetFilter.class);
        deliveryService.setDeliveryContextFactory(contextFactory);
        filterChain = context.sequence("filterChain");
        filterItem = context.mock(IDeliveryResponseListItem.class);
        deliveryContext = context.mock(IDeliverySnippetFilterContext.class);
    }

    protected void expectSnipItem(final IDeliveryResponseSnippetItem snipItem) throws Exception {
        context.checking(new Expectations() {{ 
            atLeast(1).of(snipItem).getRendering();
            atLeast(1).of(snipItem).getStyle();
            atLeast(1).of(snipItem).getScore();
        }});
    }
    
    protected DeliveryRequest createDeliveryRequest(Number id, VisitorProfile visitorProfile) {
        DeliveryRequest r = new DeliveryRequest();
        r.setListItemId(id.longValue());
        r.setVisitorProfile(visitorProfile);
        return r;
    }
    /*
     * Expect Context Creation
     */
    protected void expectContextCreation() throws Exception {
        context.checking(new Expectations() {{ 
            one(contextFactory).createContext(deliveryRequest);
            will(returnValue(deliveryContext));
            
            
            atLeast(1).of(deliveryContext).getResponseListItem();
            will(returnValue(filterItem));
            
            allowing(filterItem).getId();
            will(returnValue("1"));
        }});
        
    }
    
    @Test
    public void shouldDeliverFilteredContent() throws Exception {
        /*
         * Given: we are requesting for a filter item that uses the filter "mock_filter"
         *        and we have the "mock_filter" registered.
         */
//        final int filterItemId = 1;
//        final String profileId = "2";
        deliveryService.registerSnippetFilter("mock_filter", filter);
        
        final IDeliveryResponseSnippetItem snipItem = context.mock(IDeliveryResponseSnippetItem.class);
        final List<IDeliveryResponseSnippetItem> snipItems = asList(snipItem);
        
        /* 
         * Expect: To create a DeliveryContext and then execute our mock filter.
         */
        
        expectContextCreation();
        context.checking(new Expectations() {{ 
            
            
            //Expect the delivery service to check from the context
            //whether or not to process the snippets through the filter chain.
            //In this case we expect to run the filters.
            one(deliveryContext).isSafeToRunFilters();
            will(returnValue(true));
            inSequence(filterChain);
            
            atLeast(1).of(filterItem).getSnippetFilterIds();
            will(returnValue(asList("mock_filter")));;
            
            atLeast(1).of(deliveryContext).getResponseSnippetItems();
            will(returnValue(snipItems));
            
            //Expect our mock filter to be called.
            one(filter).filter(deliveryContext, snipItems);
            will(returnValue(snipItems));
            
            //Expect that the service will check to see if our mock filter has requested to stop.
            allowing(deliveryContext).isSafeToRunFilters();
            will(returnValue(true));
            inSequence(filterChain);
            
            //Now we expect a delivery response to be made 
            //from the processed snippet item.
            expectSnipItem(snipItem);
            
        }});
         
        /*
         * When: we tell the delivery service to delivery the goods.
         */
         DeliveryResponse response = deliveryService.deliver(deliveryRequest);
         
        /*
         * Then: we should have an ok response.
         */
         assertNotNull("Response should not be null", response);
         assertEquals("Response should have an ok status", "OK", response.getStatus());
    }

    @Test
    public void shouldDeliverFilteredContentUsingPostFilter() throws Exception {
        /*
         * Given: we are requesting for a filter item that uses the filter "mock_filter"
         *        and we have the "mock_filter" registered.
         */
//        final int filterItemId = 1;
//        final String profileId = "2";
        deliveryService.registerSnippetFilter("mock_filter", filter);
        final IDeliverySnippetFilter postFilter = context.mock(IDeliverySnippetFilter.class, "PostFilter");
        deliveryService.registerSnippetFilter("post_filter", postFilter);
        deliveryService.setPostSnippetFilters(asList("post_filter"));
        
        final IDeliveryResponseSnippetItem snipItem = context.mock(IDeliveryResponseSnippetItem.class);
        final List<IDeliveryResponseSnippetItem> snipItems = asList(snipItem);
        
        
        /* 
         * Expect: To create a DeliveryContext and then execute our mock filter.
         */
        expectContextCreation();
        context.checking(new Expectations() {{ 

            
            //Expect the delivery service to check from the context
            //whether or not to process the snippets through the filter chain.
            //In this case we expect to run the filters.
            one(deliveryContext).isSafeToRunFilters();
            will(returnValue(true));
            inSequence(filterChain);
            
            atLeast(1).of(filterItem).getSnippetFilterIds();
            will(returnValue(asList("mock_filter")));;
            
            atLeast(1).of(deliveryContext).getResponseSnippetItems();
            will(returnValue(snipItems));
            
            //Expect our mock filter to be called.
            one(filter).filter(deliveryContext, snipItems);
            will(returnValue(snipItems));
            
            //Expect that the service will check to see if our mock filter has requested to stop.
            allowing(deliveryContext).isSafeToRunFilters();
            will(returnValue(true));
            inSequence(filterChain);
            
            //Now we expect a delivery response to be made 
            //from the processed snippet item.
            expectSnipItem(snipItem);
            
            //Now we expect our post filter to run
            one(postFilter).filter(deliveryContext, snipItems);
            will(returnValue(snipItems));
            
        }});
         
        /*
         * When: we tell the delivery service to delivery the goods.
         */
         DeliveryResponse response = deliveryService.deliver(deliveryRequest);
         
        /*
         * Then: we should have an ok response.
         */
         assertNotNull("Response should not be null", response);
         assertEquals("Response should have an ok status", "OK", response.getStatus());
    }

    
    @Test
    public void shouldDeliverUnFilteredContent() throws Exception {
        /*
         * Given: we are requesting for a filter item that uses the filter "mock_filter"
         *        and we have the "mock_filter" registered.
         */
//        final int filterItemId = 1;
//        final String profileId = "2";
        deliveryService.registerSnippetFilter("mock_filter", filter);
        
        final IDeliveryResponseSnippetItem snipItem = context.mock(IDeliveryResponseSnippetItem.class);
        final List<IDeliveryResponseSnippetItem> snipItems = asList(snipItem);
        
        
        /* 
         * Expect: To create a DeliveryContext and then execute our mock filter.
         */
        expectContextCreation();
        context.checking(new Expectations() {{ 
            
            
            //Expect the delivery service to check from the context
            //whether or not to process the snippets through the filter chain.
            //In this case we expect to NOT run the filters.
            one(deliveryContext).isSafeToRunFilters();
            will(returnValue(false));
            
            atLeast(1).of(deliveryContext).getResponseSnippetItems();
            will(returnValue(snipItems));
            
            //Expect our mock filter not to be called.
            never(filterItem).getSnippetFilterIds();
            never(filter).filter(deliveryContext, snipItems);
            
            //Now we expect a delivery response to be made 
            //from the processed snippet item.
            expectSnipItem(snipItem);
            
        }});
         
        /*
         * When: we tell the delivery service to delivery the goods.
         */
         DeliveryResponse response = deliveryService.deliver(deliveryRequest);
         
        /*
         * Then: we should have an ok response.
         */
         assertNotNull("Response should not be null", response);
         assertEquals("Response should have an ok status", "OK", response.getStatus());
    }
    
    
    @Test
    public void shouldStopAfterFirstFilter() throws Exception {
        /*
         * Given: we are requesting for a filter item that uses the filter "mock_filter"
         *        and we have the "mock_filter" registered.
         */
//        final int filterItemId = 1;
//        final String profileId = "2";
        deliveryService.registerSnippetFilter("mock_filter", filter);
        
        final IDeliveryResponseSnippetItem snipItem = context.mock(IDeliveryResponseSnippetItem.class);
        final List<IDeliveryResponseSnippetItem> snipItems = asList(snipItem);
        
        
        /* 
         * Expect: To create a DeliveryContext and then execute our mock filter.
         */
        expectContextCreation();
        context.checking(new Expectations() {{ 
            
            //Expect the delivery service to check from the context
            //whether or not to process the snippets through the filter chain.
            //In this case we expect only one of our filters to run.
            one(deliveryContext).isSafeToRunFilters();
            will(returnValue(true));
            inSequence(filterChain);
            //Expect our first filter when executed to set the context
            //unsafe to execute filters.
            allowing(deliveryContext).isSafeToRunFilters();
            will(returnValue(false));
            inSequence(filterChain);
            
            atLeast(1).of(deliveryContext).getResponseSnippetItems();
            will(returnValue(snipItems));
            
            //Expect our mock filter to be called just once.
            one(filterItem).getSnippetFilterIds();
            will(returnValue(asList("mock_filter","mock_filter")));
            one(filter).filter(deliveryContext, snipItems);
            will(returnValue(snipItems));
            
            //Now we expect a delivery response to be made 
            //from the processed snippet item.
            expectSnipItem(snipItem);
            
        }});
         
        /*
         * When: we tell the delivery service to delivery the goods.
         */
         DeliveryResponse response = deliveryService.deliver(deliveryRequest);
         
        /*
         * Then: we should have an ok response.
         */
         assertNotNull("Response should not be null", response);
         assertEquals("Response should have an ok status", "OK", response.getStatus());
    }
}
