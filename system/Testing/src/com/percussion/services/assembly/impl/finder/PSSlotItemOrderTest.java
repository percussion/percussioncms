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

package com.percussion.services.assembly.impl.finder;

import com.percussion.services.assembly.impl.finder.PSContentFinderBase.ContentItem;
import com.percussion.services.assembly.impl.finder.PSContentFinderBase.ContentItemOrder;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.utils.guid.IPSGuid;
import org.jmock.Expectations;

import org.jmock.Mockery;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertTrue;

/**
 * Unit test class for 
 * com.percussion.services.assembly.impl.finder.PSBaseSlotContentFinder.SlotItemOrder
 */

public class PSSlotItemOrderTest
{
    Mockery context = new Mockery();;

   /**
    * Tests SlotItemOrder.compare() with variously configured 
    * <code>SlotItem</code>s.
    */
   @Test
   @Ignore("TODO: This test is broken, please fix me")
   public void testOrder()
   {
      Comparator<ContentItem> c = new ContentItemOrder();
      int result;

      IPSGuid itemId = context.mock(IPSGuid.class,"itemId");
      IPSGuid templateId = context.mock(IPSGuid.class, "templateId");

     
      // #1: check with valid sort ranks
       ContentItem item1 = (ContentItem) context.mock(IPSFilterItem.class,"item1");
      item1.setItemId(itemId);
      item1.setTemplate(templateId);
      item1.setSortrank(1);

       ContentItem item2 = (ContentItem)context.mock(IPSFilterItem.class, "item2");
      item2.setSortrank(2);
      item2.setTemplate(templateId);
      item2.setItemId(itemId);

      context.checking(new Expectations() {
          {
              never (item1);
          }

      });
      //itemId.expects(never());
      result = c.compare(item1, item2);
      assertTrue(result < 0);
      result = c.compare(item2, item1);
      assertTrue(result > 0);
      context.assertIsSatisfied();
     
      // #2: equal sort ranks, no relationship ids = should use item id
      item1.setSortrank(0);
      item2.setSortrank(0);

      context.checking(new Expectations() {
           {
               atLeast(1).of(itemId).longValue(); will(onConsecutiveCalls(returnValue((long)1), returnValue((long)2)));

           }

       });

      result = c.compare(item1, item2);;
      assertTrue(result<0);
      context.assertIsSatisfied();

      
      // #3: equal sort ranks, only one relationship id = should use item id
       IPSGuid relationshipId = context.mock(IPSGuid.class,"relationshipId");



      item1.setRelationshipId((IPSGuid)relationshipId);

       context.checking(new Expectations() {
           {
               never(relationshipId);
               atLeast(1).of(itemId).longValue(); will(onConsecutiveCalls(returnValue((long)1), returnValue((long)2)));
           }

       });


      result = c.compare(item1, item2);
      assertTrue(result < 0);
      context.assertIsSatisfied();

      // #4: equal sort ranks, both relationship id = should use relationship id
      item2.setRelationshipId(relationshipId);

       context.checking(new Expectations() {
           {

               atLeast(1).of(relationshipId).longValue(); will(onConsecutiveCalls(returnValue((long)20), returnValue((long)10)));
               never(itemId);
           }

       });

       result = c.compare(item1, item2);
       assertTrue(result > 0);
        context.assertIsSatisfied();

   }
   
}
