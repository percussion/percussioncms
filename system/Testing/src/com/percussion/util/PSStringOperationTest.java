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

package com.percussion.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * The PSStringOperationTest is the unit test for PSStringOperation class which
 * handles some string manipulation.
 *
 * @author     Jian Huang
 * @version    2.0
 * @since      1.0
 */
public class PSStringOperationTest extends TestCase
{
   public PSStringOperationTest(String name)
   {
      super(name);
   }

   public void testReplaceAndToProperCase() throws Exception
   {
      PSStringOperation operation = new PSStringOperation();

      String src = "Simba is a bear bear cub, it is a famous bear";
      String sub = "bear";
      String rep = "lion";

      String expect1 = "Simba is a lion lion cub, it is a famous lion";
      String expect2 = "Simba is a lion cub, it is a famous lion";
      String expect3 = "Simba Is A Lion Cub, It Is A Famous Lion";
      String result;

      result = operation.replace(null, sub, rep); // no replacement
      assertTrue(result == null);

      result = operation.replace(src, null, rep); // no replacement
      assertTrue(result.equals(src));

      result = operation.replace(src, sub, null); // no replacement
      assertTrue(result.equals(src));

      result = operation.replace(src, sub, rep);
      assertTrue(result.equals(expect1));

      result = operation.replace(result, "lion lion", rep);
      assertTrue(result.equals(expect2));

      result = operation.toProperCase(null);
      assertTrue(result == null);

      result = operation.toProperCase(expect2);
      assertTrue(result.equals(expect3));

      result = operation.toProperCase("  this  is   only a   test");
      assertTrue(result.equals("This Is Only A Test"));
   }

   public void testDateFormatCase() throws Exception
   {
      PSStringOperation operation = new PSStringOperation();

      java.util.Date oneDate = new java.util.Date();
      String strFormat = "invalid format";
      String result;

      result = operation.dateFormat(null, oneDate);   // null input format pattern
      // PSStringOperation.dateFormat(...) method has been modified to use the
      // default Date format if a null format is specified.
      assertTrue(result != null);

      // PSStringOperation.dateFormat(...) method has been modified and now
      // it throws IllegalArgumentException if a null date is specified.
      boolean didThrow = false;
      try
      {
         result = operation.dateFormat(strFormat, null);  // null input reference date
      }
      catch (IllegalArgumentException e)
      {
          didThrow = true;
      }
      assertTrue(didThrow);

      // "Illegal pattern character"
      didThrow = false;
      try
      {
         result = operation.dateFormat(strFormat, oneDate); // invalid pattern
      }
      catch (IllegalArgumentException e)
      {
          didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try
      {
         result = operation.dateFormat("HH", oneDate);  // any valid pattern
      }
      catch (IllegalArgumentException e)
      {
          didThrow = true;
      }
      assertTrue(!didThrow);
   }

   /**
    * Test the {@link PSStringOperation#replace(String, char, char)} method.
    * 
    * @throws Exception if the test fails.
    */
   public void testCharReplace() throws Exception
   {
      assertEquals("foo:bar:doh", PSStringOperation.replace("foo;bar;doh", ';', 
         ':'));
      assertEquals(":foo:bar:doh", PSStringOperation.replace(";foo;bar;doh",';', 
         ':'));
      assertEquals(":", PSStringOperation.replace(";", ';', ':'));
      assertEquals("", PSStringOperation.replace("", ';',  ':'));
      assertEquals("::", PSStringOperation.replace(":", ';', ':'));
      assertNull(PSStringOperation.replace(null, ';', ':'));
      assertEquals("foo:bar:", PSStringOperation.replace("foo;bar;", ';', ':'));
      assertEquals("foo:bar::doh", PSStringOperation.replace("foo;bar:doh", ';', 
         ':'));
      assertEquals("foo:bar:doh::", PSStringOperation.replace("foo;bar;doh:", 
         ';', ':'));
      assertEquals("::foo:bar:doh", PSStringOperation.replace(":foo;bar;doh", 
         ';', ':'));         
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSStringOperationTest("testReplaceAndToProperCase"));
      suite.addTest(new PSStringOperationTest("testDateFormatCase"));
      suite.addTest(new PSStringOperationTest("testCharReplace"));
      return suite;
   }
}
