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
package com.percussion.utils;

import com.percussion.utils.jsr170.PSValueConverter;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class PSValueConverterTest extends TestCase
{
   public PSValueConverterTest(String name) {
      super(name);
   }

   public static TestSuite suite()
   {
      return new TestSuite(PSValueConverterTest.class);
   }

   public void testConversionMillis() throws Exception
   {
      long now = System.currentTimeMillis();
      Calendar cal = new GregorianCalendar();
      cal.setTimeInMillis(now);
      Calendar cval = PSValueConverter.convertToCalendar(now);
      assertEquals(cal, cval);
   }
   
   public void testConversionDateString() throws Exception
   {
      Calendar cal = PSValueConverter.convertToCalendar("2001/08/31");
      assertEquals(8, cal.get(Calendar.MONTH) + 1);
      assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
      assertEquals(2001, cal.get(Calendar.YEAR));
      
      cal = PSValueConverter.convertToCalendar("2100-05-04 11:31:23");
      assertEquals(5, cal.get(Calendar.MONTH) + 1);
      assertEquals(4, cal.get(Calendar.DAY_OF_MONTH));
      assertEquals(2100, cal.get(Calendar.YEAR));
      assertEquals(11, cal.get(Calendar.HOUR));
      assertEquals(31, cal.get(Calendar.MINUTE));
      assertEquals(23, cal.get(Calendar.SECOND));
   }
   
   public void testConversion() throws Exception
   {
      Calendar cal = new GregorianCalendar(1985,11,22);
      String date = PSValueConverter.convertToString(cal);
      assertEquals("1985-12-22 00:00:00", date);
   }
}
