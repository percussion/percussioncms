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
