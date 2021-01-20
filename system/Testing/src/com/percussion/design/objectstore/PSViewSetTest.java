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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the PSView and PSViewSet classes.
 */
public class PSViewSetTest extends TestCase
{
   // see base class
   public PSViewSetTest(String name)
   {
      super(name);
   }

   /**
    * Tests creating views, adding them to a view set and retrieving them from
    * the viewset by name.
    *
    * @throws Exception if the test fails or an error occurs
    */
   public void testViewSet() throws Exception
   {
      PSViewSet viewSet = new PSViewSet();

      List fieldList = new ArrayList();
      fieldList.add("field1");
      fieldList.add("field2");
      PSView view1 = new PSView("view1", fieldList.iterator());
      viewSet.addView(view1);

      assertEquals("getView", viewSet.getView("VIEW1"), view1);
      assertTrue("get non-existant view", null == viewSet.getView("VIEW2"));

      // test conditional views
      assertTrue("get invalid conditional views not null",
         viewSet.getCondtionalViews("VIEW2") != null);
      assertTrue("get invalid conditional views returns empty iterator",
         !viewSet.getCondtionalViews("VIEW2").hasNext());

      boolean didThrow = false;
      PSCollection conditions = new PSCollection(PSConditional.class);
      conditions.add(new PSConditional(new PSTextLiteral("a"), "=",
         new PSTextLiteral("b")));

      PSConditionalView condView = new PSConditionalView("VIEW1",
         fieldList.iterator(), conditions);
      viewSet.addConditionalView(condView);
      assertTrue("getCondView not null",
         viewSet.getCondtionalViews("VIEW1") != null);
      assertTrue("getCondView has next",
         viewSet.getCondtionalViews("VIEW1").hasNext());
      assertEquals("getCondView equals", condView,
         viewSet.getCondtionalViews("VIEW1").next());

   }

   // collect all tests into a TestSuite and return it - see base class
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSViewSetTest("testViewSet"));
      return suite;
   }


}
