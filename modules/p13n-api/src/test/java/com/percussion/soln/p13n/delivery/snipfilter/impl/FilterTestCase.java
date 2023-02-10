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

package test.percussion.soln.p13n.delivery.snipfilter.impl;

import static java.util.Arrays.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;

import com.percussion.soln.p13n.delivery.IDeliveryResponseListItem;
import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;
import com.percussion.soln.p13n.delivery.data.DeliveryListItem;
import com.percussion.soln.p13n.delivery.data.DeliverySnippetItem;
import com.percussion.soln.p13n.delivery.impl.DeliveryContext;
import com.percussion.soln.p13n.delivery.impl.DeliveryResponseListItem;
import com.percussion.soln.p13n.delivery.impl.DeliveryResponseSnippetItem;
import com.percussion.soln.segment.Segment;

public abstract class FilterTestCase {

    protected IDeliverySnippetFilter filter;
    protected MockDeliverySnippetFilterContext snippetFilterContext;
    protected DeliveryListItem listItem;
    protected MockDeliveryResponseListItem list;
    protected MockDeliveryResponseSnippetItem snipA;
    protected MockDeliveryResponseSnippetItem snipB;
    protected MockDeliveryResponseSnippetItem snipC;
    protected List<IDeliveryResponseSnippetItem> originalSnippets;
    protected List<IDeliveryResponseSnippetItem> snippets;
    protected DeliverySnippetItem a;
    protected DeliverySnippetItem b;
    protected DeliverySnippetItem c;
    protected Segment segX;
    protected Segment segY;
    protected Segment segZ;

    public FilterTestCase() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        filter = createFilter();
        a = new DeliverySnippetItem();
        a.setId(1);
        b = new DeliverySnippetItem();
        b.setId(2);
        c = new DeliverySnippetItem();
        c.setId(3);
        segX = new Segment(); segX.setId("x");
        segY = new Segment(); segY.setId("y");
        segZ = new Segment(); segZ.setId("z");
        
        snipA = new MockDeliveryResponseSnippetItem(a, asList(segX), "A");
        snipB = new MockDeliveryResponseSnippetItem(b, asList(segX, segY),"B");
        snipC = new MockDeliveryResponseSnippetItem(c, asList(segX, segY, segZ), "C");
        
        snippets = new ArrayList<IDeliveryResponseSnippetItem>();
        snippets.addAll(asList(snipA,snipB,snipC));
        originalSnippets = new ArrayList<IDeliveryResponseSnippetItem>(snippets);
        
        listItem = new DeliveryListItem();
        listItem.setProperties(new HashMap<String, String>());
        
        list = new MockDeliveryResponseListItem(listItem, new ArrayList<Segment>());
        snippetFilterContext = new MockDeliverySnippetFilterContext(list,snippets);
        snippetFilterContext.setVisitorSegments(asList(segY,segZ));
    }
    
    protected abstract IDeliverySnippetFilter createFilter();
    
    
    protected List<IDeliveryResponseSnippetItem> snippets(IDeliveryResponseSnippetItem ...deliveryResponseSnippetItems) {
        return new ArrayList<IDeliveryResponseSnippetItem>(asList(deliveryResponseSnippetItems));
    }
    
    protected List<IDeliveryResponseSnippetItem> snippets() {
        return new ArrayList<IDeliveryResponseSnippetItem>();
    }
    
    public static class MockDeliveryResponseSnippetItem extends DeliveryResponseSnippetItem {

        private String name;
        @Override
        public String toString() {
            return "snip" + name;
            
        }

        public MockDeliveryResponseSnippetItem(DeliverySnippetItem itemData, Collection<? extends Segment> segments, String name) {
            super(itemData, segments);
            this.name = name;
        }

    }
    
    public static class MockDeliverySnippetFilterContext extends DeliveryContext {
        
        public MockDeliverySnippetFilterContext(IDeliveryResponseListItem listItem,
                List<IDeliveryResponseSnippetItem> snippets) {
            super(listItem, snippets);
            this.setSafeToRunFilters(true);
        }


        private List<Segment> visitorSegments;

        
        @Override
        public int getVisitorSegmentWeight(Segment segment) {
            if (visitorSegments.contains(segment)) return 1;
            return 0;
        }


        public List<Segment> getVisitorSegments() {
            return visitorSegments;
        }

        
        public void setVisitorSegments(List<Segment> visitorSegments) {
            this.visitorSegments = visitorSegments;
        }
        
    
    }
    
    public static class MockDeliveryResponseListItem extends DeliveryResponseListItem {

        public MockDeliveryResponseListItem(DeliveryListItem data, Collection<? extends Segment> segments) {
            super(data, segments);
        }
    }

}