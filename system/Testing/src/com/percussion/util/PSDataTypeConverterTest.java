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

import java.text.ParseException;

import junit.framework.TestCase;

/**
 * Unit test class for {@link PSDataTypeConverter}. 
 * 
 * @author paulhoward
 */
public class PSDataTypeConverterTest extends TestCase
{
   /**
    * Test transforming date from 1 string format to another.
    * 
    * @throws Exception
    */
   public void testTransformDateString()
      throws Exception
   {
      assertNull(PSDataTypeConverter.transformDateString(null, null, null,
            false));
      String testDate = "11.1995-30T23";
      String inputFormat = "MM.yyyy-dd'T'HH";
      String result = PSDataTypeConverter.transformDateString(testDate,
            inputFormat, null, false);
      assertTrue(result.equals("1995-11-30 23:00:00.000"));

      testDate = "11.1995-30";
      try
      {
         result = PSDataTypeConverter.transformDateString(testDate,
               inputFormat, null, true);
         fail("Should have thrown ex.");
      }
      catch (ParseException success)
      {
      }

      inputFormat = "MM.yyyy-dd";
      result = PSDataTypeConverter.transformDateString(testDate, inputFormat,
            null, false);
      assertTrue(result.equals("1995-11-30"));

      String outputFormat = "dd-yyyy.MM";
      result = PSDataTypeConverter.transformDateString(testDate, inputFormat,
            outputFormat, false);
      assertTrue(result.equals("30-1995.11"));

      testDate = "2000/1/24 23:21";
      outputFormat = "HH:mm yyyy-MM-dd";
      result = PSDataTypeConverter.transformDateString(testDate, null,
            outputFormat, false);
      assertTrue(result.equals("23:21 2000-01-24"));
      
      testDate = "19980101T131415123";
      outputFormat = "yyyy/MM/dd HH:mm:ss.SSS";
      result = PSDataTypeConverter.transformDateString(testDate, null,
            outputFormat, false);
      assertTrue(result.equals("1998/01/01 13:14:15.123"));
   }
}
