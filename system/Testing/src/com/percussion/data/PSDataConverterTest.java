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
