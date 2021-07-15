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
package com.percussion.services.assembly.impl;

import com.percussion.utils.testing.UnitTest;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 * Test 
 * @author erikserating
 */
@Category(UnitTest.class)
public class PSDivTagCleanupTest
{
   /**
    * 
    */
   static final String ms_input = 
      "<div a='1' xmlns:goofy='http://www.goofy.org'>"
      + "<div class='rxbodyfield'><!-- comment -->"
      + "<el1 b='2'><el2 c='3' xmlns:bletch='somethingelse'/></el1>"
      + "</div>"
      + "<el1 xmlns:foobar='someotheruri'/>"
      + "</div>";
   
   /**
    * 
    */
   static final String ms_result = "<div a=\"1\"><!-- comment --><el1 b=\"2\">"
         + "<el2 c=\"3\"/></el1>"
         + "<el1/></div>";

   /**
    * Test cleanup
    */
   @Test
   //TODO: Fix me.  This test currently errors out if run with main build.  Passes locally.
   @Ignore
   public void testNSCleanup()
   {
      PSDivTagCleanup cleanup = new PSDivTagCleanup();
      String result = (String) cleanup.translate(ms_input);
      assertEquals(ms_result, result);
   }
}
