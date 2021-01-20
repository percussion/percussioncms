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
package com.percussion.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.percussion.utils.jsr170.PSValueConverter;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
