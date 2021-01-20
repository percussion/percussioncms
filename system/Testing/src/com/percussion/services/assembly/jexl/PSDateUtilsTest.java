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
package com.percussion.services.assembly.jexl;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

/**
 * Test date utilities
 * 
 * @author dougrand
 */
public class PSDateUtilsTest extends TestCase
{
   private static final PSDateUtils ms_utils = new PSDateUtils();

   private static Date ms_test1 = null;
   
   private static Calendar ms_testcal1 = null;

   private static Date ms_test2 = null;

   static
   {
      Calendar cal = new GregorianCalendar();

      cal.set(Calendar.YEAR, 2006);
      cal.set(Calendar.MONTH, Calendar.MAY);
      cal.set(Calendar.DAY_OF_MONTH, 17);

      ms_test1 = cal.getTime();
      ms_testcal1 = (Calendar) cal.clone();

      cal.set(Calendar.DAY_OF_MONTH, 20);

      ms_test2 = cal.getTime();
   }

   /**
    * Test method for
    * 'com.percussion.services.assembly.jexl.PSDateUtils.startOfWeek(Date, int)'
    */
   public void testStartOfWeek()
   {
      Date d = ms_utils.startOfWeek(ms_test1, 0);
      checkField(d, Calendar.DAY_OF_MONTH, 14);
      d = ms_utils.startOfWeek(ms_test2, 0);
      checkField(d, Calendar.DAY_OF_MONTH, 14);
      d = ms_utils.startOfWeek(ms_test1, -1);
      checkField(d, Calendar.DAY_OF_MONTH, 7);
      d = ms_utils.startOfWeek(ms_test1, 1);
      checkField(d, Calendar.DAY_OF_MONTH, 21);
      
      d = ms_utils.startOfWeek(ms_testcal1, 1);
      checkField(d, Calendar.DAY_OF_MONTH, 21);
      
      d = ms_utils.startOfWeek(ms_test1, -2);
      checkField(d, Calendar.DAY_OF_MONTH, 30);
      checkField(d, Calendar.MONTH, Calendar.APRIL);
      d = ms_utils.startOfWeek(ms_test1, 2);
      checkField(d, Calendar.DAY_OF_MONTH, 28);
      d = ms_utils.startOfWeek(ms_test1, 3);
      checkField(d, Calendar.MONTH, Calendar.JUNE);
      checkField(d, Calendar.DAY_OF_MONTH, 4);
   }

   /**
    * Test the given date for a given value
    * 
    * @param date the date, assumed not <code>null</code>
    * @param field the calendar field
    * @param val the value
    */
   private void checkField(Date date, int field, int val)
   {
      Calendar cal = new GregorianCalendar();
      cal.setTime(date);
      int calval = cal.get(field);
      assertEquals(val, calval);
   }

   /**
    * Test method for
    * 'com.percussion.services.assembly.jexl.PSDateUtils.startOfMonth(Date,
    * int)'
    */
   public void testStartOfMonth()
   {
      Date d = ms_utils.startOfMonth(ms_test1, 0);
      checkField(d, Calendar.DAY_OF_MONTH, 1);
      checkField(d, Calendar.MONTH, Calendar.MAY);
      checkField(d, Calendar.YEAR, 2006);
      
      d = ms_utils.startOfMonth(ms_test1, -1);
      checkField(d, Calendar.DAY_OF_MONTH, 1);
      checkField(d, Calendar.MONTH, Calendar.APRIL);
      d = ms_utils.startOfMonth(ms_test1, 1);
      checkField(d, Calendar.DAY_OF_MONTH, 1);
      checkField(d, Calendar.MONTH, Calendar.JUNE);
      
      d = ms_utils.startOfMonth(ms_testcal1, 1);
      checkField(d, Calendar.DAY_OF_MONTH, 1);
      checkField(d, Calendar.MONTH, Calendar.JUNE);
      
      d = ms_utils.startOfMonth(ms_test1, -5);
      checkField(d, Calendar.DAY_OF_MONTH, 1);
      checkField(d, Calendar.MONTH, Calendar.DECEMBER);
      checkField(d, Calendar.YEAR, 2005);
      
      d = ms_utils.startOfMonth(ms_test1, 8);
      checkField(d, Calendar.DAY_OF_MONTH, 1);
      checkField(d, Calendar.MONTH, Calendar.JANUARY);
      checkField(d, Calendar.YEAR, 2007);
   }

   /**
    * Test method for
    * 'com.percussion.services.assembly.jexl.PSDateUtils.startOfYear(Date, int)'
    */
   public void testStartOfYear()
   {
      Date d = ms_utils.startOfYear(ms_test1, 0);
      checkField(d, Calendar.DAY_OF_MONTH, 1);
      checkField(d, Calendar.MONTH, Calendar.JANUARY);
      checkField(d, Calendar.YEAR, 2006);
      
      d = ms_utils.startOfYear(ms_test1, -1);
      checkField(d, Calendar.DAY_OF_MONTH, 1);
      checkField(d, Calendar.MONTH, Calendar.JANUARY);
      checkField(d, Calendar.YEAR, 2005);
      
      d = ms_utils.startOfYear(ms_testcal1, -1);
      checkField(d, Calendar.DAY_OF_MONTH, 1);
      checkField(d, Calendar.MONTH, Calendar.JANUARY);
      checkField(d, Calendar.YEAR, 2005);
      
      d = ms_utils.startOfYear(ms_test1, 1);
      checkField(d, Calendar.DAY_OF_MONTH, 1);
      checkField(d, Calendar.MONTH, Calendar.JANUARY);
      checkField(d, Calendar.YEAR, 2007);
   }
   
   /**
    * Test offset calc for day
    */
   public void testDateOffset()
   {
      Date d = ms_utils.startOfYear(ms_test1, 0);
      
      Date cd = ms_utils.dayOffset(d, 1);
      checkField(cd, Calendar.DAY_OF_MONTH, 2);
      checkField(cd, Calendar.MONTH, Calendar.JANUARY);
      checkField(cd, Calendar.YEAR, 2006);
      
      cd = ms_utils.dayOffset(d, -1);
      checkField(cd, Calendar.DAY_OF_MONTH, 31);
      checkField(cd, Calendar.MONTH, Calendar.DECEMBER);
      checkField(cd, Calendar.YEAR, 2005);      
   }

}
