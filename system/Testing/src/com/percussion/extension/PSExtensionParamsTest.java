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
package com.percussion.extension;

import com.percussion.data.PSConversionException;
import com.percussion.design.objectstore.PSDateLiteral;
import com.percussion.design.objectstore.PSLiteral;
import com.percussion.design.objectstore.PSSingleHtmlParameter;
import com.percussion.design.objectstore.PSTextLiteral;

import java.util.Date;

import junit.framework.TestCase;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * Test extensions params
 * 
 * @author dougrand
 */
public class PSExtensionParamsTest extends TestCase
{
   /**
    * @throws Exception
    */
   public void testRequiredArgs() throws Exception
   {
      Object params[] =
      {"1", null, "2", 4, "-1"};

      PSExtensionParams ep = new PSExtensionParams(params);

      assertEquals("1", ep.getStringParam(0, null, true));
      assertEquals("2", ep.getStringParam(2, null, true));

      assertEquals("1", ep.getStringParam(0, null, false));
      assertEquals("0", ep.getStringParam(1, "0", false));
      assertEquals("2", ep.getStringParam(2, null, false));

      assertEquals(4, ep.getNumberParam(3, 0, true));

      assertEquals(-1, ep.getNumberParam(4, 0, true));
      // Missing arg
      try
      {
         ep.getStringParam(1, null, true);
         assertFalse(true);
      }
      catch (PSConversionException e)
      {
         // OK
      }

      // Arg of wrong type
      assertEquals("4", ep.getStringParam(3, null, true));

      // Missing arg
      try
      {
         ep.getStringParam(5, null, true);
         assertFalse(true);
      }
      catch (PSConversionException e)
      {
         // OK
      }
   }

   /**
    * @throws Exception
    */
   public void testReplacementValues() throws Exception
   {
      PSSingleHtmlParameter a = new PSSingleHtmlParameter("sys_a");
      a.setValueText("xyz");

      PSLiteral b = new PSTextLiteral("abc");
      Date today = new Date();
      PSLiteral c = new PSDateLiteral(today, FastDateFormat.getInstance(
            "yyyy/MM/dd HH:mm:ss.SSS"));

      Object params[] =
      {a, b, c};

      PSExtensionParams ep = new PSExtensionParams(params);
      assertEquals("xyz", ep.getStringParam(0, "", true));
      assertEquals("abc", ep.getStringParam(1, "", true));
      assertEquals(today, ep.getDateParam(2, null, true));
   }
   
   /**
    * Test cases that the numeric param should be <code>null</code> and
    * not <code>0</code>
    * @throws Exception
    */
   public void testNumericEmptyHandling() throws Exception
   {
      Object params[] = {"", null};
      
      PSExtensionParams ep = new PSExtensionParams(params);
      assertNull(ep.getNumberParam(0, null, false));
      assertNull(ep.getNumberParam(1, null, false));
   }

   /**
    * Test default cases
    * @throws Exception 
    */
   public void testDefaults() throws Exception
   {
      Object params[] =
      {"1", null, "2", 4};

      PSExtensionParams ep = new PSExtensionParams(params);
      assertTrue(3 == ep.getNumberParam(1, "3", false).intValue());
      assertTrue(3 == ep.getNumberParam(1, 3, false).intValue());
   }
}
