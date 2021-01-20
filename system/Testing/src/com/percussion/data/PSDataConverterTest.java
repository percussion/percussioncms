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

package com.percussion.data;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * The PSDataConverterTest class is for unit test of PSDataConverter class.
 *
 * @author   Jian Huang
 * @version   1.0
 * @since   1.0
 */
public class PSDataConverterTest extends TestCase
{
   public PSDataConverterTest(String name)
   {
      super(name);
   }

   // this class is used to keep track of expected parse results for dates
   private static class DateTest
   {
      public DateTest(String dateText,
         int year, int month, int day, int hours, int minutes, int seconds, long milliseconds)
      {
         this.dateText = dateText;
         this.month = month;
         this.year = year;
         this.day = day;
         this.hours = hours;
         this.minutes = minutes;
         this.seconds = seconds;
         this.milliseconds = milliseconds;
      }
      
      public String dateText;
      public int month; // 0-11
      public int year; // whatever
      public int day; // 1-31

      public int hours; // 0-23
      public int minutes; // 0-59
      public int seconds; // 0-59
      public long milliseconds;
   }

   // test date conversion with many popular formats
   // TODO: For every format used in PSDataConverter.ms_datePatternArray,
   // compose several dates using that format
   public void testDateConversion() throws Exception
   {
      // a bunch of dates, all earlier than today
      DateTest[] myTests = {
         new DateTest("1999", 1999, 0, 1, 0, 0, 0, 0),
         new DateTest("1999-08-20 AD at 02:33:45", 1999, 7, 20, 2, 33, 45, 0),
         new DateTest("1900/03/31 02:30", 1900, 2, 31, 2, 30, 0, 0),
         new DateTest("1999-02-23 23:23:23.123", 1999, 1, 23, 23, 23, 23, 123),
         new DateTest("Friday, August 20, 1999", 1999, 7, 20, 0, 0, 0, 0),
         new DateTest("May 1999", 1999, 4, 1, 0, 0, 0, 0),
         new DateTest("1999.July.20 at 8:34 PM", 1999, 6, 20, 20, 34, 0, 0),
         new DateTest("1999.July.20 at 8:34 AM", 1999, 6, 20, 8,  34, 0, 0)
      };

      for (int i = 0; i < myTests.length; i++)
      {
         DateTest t = myTests[i];
         Date day = PSDataConverter.parseStringToDate(t.dateText);
         Calendar cal = new GregorianCalendar();
         cal.setTime(day);
         assertEquals("For year in  " + t.dateText, t.year, cal.get(Calendar.YEAR));
         assertEquals("For month in " + t.dateText, t.month, cal.get(Calendar.MONTH));
         assertEquals("For day in  " + t.dateText, t.day, cal.get(Calendar.DAY_OF_MONTH));
         assertEquals("For hours in " + t.dateText, t.hours, cal.get(Calendar.HOUR_OF_DAY));
         assertEquals("For minutes in  " + t.dateText, t.minutes, cal.get(Calendar.MINUTE));
         assertEquals("For seconds in " + t.dateText, t.seconds, cal.get(Calendar.SECOND));
         // TODO: test milliseconds
      }
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSDataConverterTest("testDateConversion"));
      return suite;
   }

}
