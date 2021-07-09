/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
