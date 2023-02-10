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

import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PSDateFormatISO8601Test extends TestCase
{
   public PSDateFormatISO8601Test(String name)
   {
      super(name);
   }
   
   /**
    * Midnight is a weird case -- there could be two different representations
    * (i.e., July 3rd, 24:00:00 is the same time as July 4th, 00:00:00). We
    * should prefer the more standard 00:00:00 notation (that is, the next day's
    * midnight), so we test that we use the next day (and if on New Year's,
    * exactly when the big apple falls in Times Square, use the next year).
    *
    */
   public void testMidnight()
   {
      // (int year, int month, int date, int hour, int minute, int second)
      //FB: DMI_BAD_MONTH NC - 1-17-16
      GregorianCalendar cal = new GregorianCalendar(1999, 11, 31, 0, 0, 0);
      PSDateFormatISO8601 fmt = new PSDateFormatISO8601();
      String dateStr = fmt.format(cal.getTime());
      assertEquals(dateStr, "19991231T000000000");
      Date formattedDate = fmt.parse(dateStr, new ParsePosition(0));
      assertEquals(formattedDate, cal.getTime());
   }
   
   /**
    *   Test the time formatting for the current time, breaking the formatted
    *   string up into substrings and then reparsing them and comparing them
    *   to the current time's numeric values.
    */
   public void testNow() throws Exception
   {
      GregorianCalendar now = new GregorianCalendar();
      PSDateFormatISO8601 fmt = new PSDateFormatISO8601();
      String dateStr   = fmt.format(now.getTime());
   
      String yearStr   = dateStr.substring(0, 4);
      String monthStr   = dateStr.substring(4, 6);
      String dayStr   = dateStr.substring(6, 8);
      String sepStr   = dateStr.substring(8, 9);
      String hourStr   = dateStr.substring(9, 11);
      String minStr   = dateStr.substring(11, 13);
      String secStr   = dateStr.substring(13, 15);
      String msecStr   = dateStr.substring(15, 18);
      
      assertEquals(Integer.parseInt(yearStr, 10), now.get(Calendar.YEAR));
      assertEquals(Integer.parseInt(monthStr, 10), (1 + now.get(Calendar.MONTH)));
      assertEquals(Integer.parseInt(dayStr, 10), now.get(Calendar.DAY_OF_MONTH));
      assertEquals(sepStr, "T");
      assertEquals(Integer.parseInt(hourStr, 10), now.get(Calendar.HOUR_OF_DAY));
      assertEquals(Integer.parseInt(minStr, 10), now.get(Calendar.MINUTE));
      assertEquals(Integer.parseInt(secStr, 10), now.get(Calendar.SECOND));
      assertEquals(Integer.parseInt(msecStr, 10), now.get(Calendar.MILLISECOND));
      Date formattedDate = fmt.parse(dateStr, new ParsePosition(0));
      assertEquals(formattedDate, now.getTime());
   }
   
   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSDateFormatISO8601Test("testMidnight"));
      suite.addTest(new PSDateFormatISO8601Test("testNow"));
      return suite;
   }
}
