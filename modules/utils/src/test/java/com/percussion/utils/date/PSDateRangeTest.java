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
package com.percussion.utils.date;

import com.percussion.utils.date.PSDateRange.Granularity;
import org.apache.commons.lang3.time.FastDateFormat;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author paulhoward
 */
public class PSDateRangeTest
{
   /**
    * Create a date from expected values, hiding the weird values you have to pass to the Date ctor.
    * 
    * @param year actual year 
    * @param month 1-based, from 1 to 12
    * @param day 1 based
    * @return Never <code>null</code>.
    */
   private Date createDate(int year, int month, int day)
   {
      Calendar cal = Calendar.getInstance();
      cal.set(year, month-1, day, 0, 0, 0);
      return cal.getTime();
   }

   @Test
   public void testCtor() throws Exception
   {
      Date d1 = createDate(2010, 9, 1);
      Date d2 = createDate(2010, 9, 7);
      PSDateRange range;
      try
      {
         range = new PSDateRange(d2, d1);
         fail();
      }
      catch (IllegalArgumentException e)
      { /* success */ }
      
      range = new PSDateRange(d1, null);
      assertTrue(range.getDaysInRange() > 49);
      
      range = new PSDateRange(null, null);
      assertEquals(1, range.getDaysInRange());

      d1 = createDate(2010, 9, 1);
      d2 = createDate(2010, 9, 1);
      range = new PSDateRange(d1, d2);
      
      try
      {
         range = new PSDateRange(d2, null, 5);
         fail();
      }
      catch (IllegalArgumentException e)
      { /* success */ }
      
      try
      {
         range = new PSDateRange(d2, Granularity.DAY, -1);
         fail();
      }
      catch (IllegalArgumentException e)
      { /* success */ }

      FastDateFormat formatter;
      formatter = FastDateFormat.getInstance("MM/dd/yyyy");
           
      d1 = createDate(2010, 10, 24);
      String d1Str = formatter.format(d1);
      d2 = createDate(2010, 10, 29);
      Date d3 = createDate(2010, 10, 28);
      String d3Str = formatter.format(d3);
                 
      range = new PSDateRange(d2, Granularity.DAY, 5);
      assertEquals(d1Str, formatter.format(range.getStart()));
      assertEquals(d3Str, formatter.format(range.getEnd()));
      assertEquals(Granularity.DAY, range.getGranularity());
      
      d1Str = formatter.format(createDate(2010, 10, 22));
      range = new PSDateRange(d2, Granularity.WEEK, 1);
      assertEquals(d1Str, formatter.format(range.getStart()));
      assertEquals(d3Str, formatter.format(range.getEnd()));
      assertEquals(Granularity.WEEK, range.getGranularity());
      
      d1Str = formatter.format(createDate(2010, 8, 29));
      range = new PSDateRange(d2, Granularity.MONTH, 2);
      assertEquals(d1Str, formatter.format(range.getStart()));
      assertEquals(d3Str, formatter.format(range.getEnd()));
      assertEquals(Granularity.MONTH, range.getGranularity());
      
      d1Str = formatter.format(createDate(2007, 10, 29));
      range = new PSDateRange(d2, Granularity.YEAR, 3);
      assertEquals(d1Str, formatter.format(range.getStart()));
      assertEquals(d3Str, formatter.format(range.getEnd()));
      assertEquals(Granularity.YEAR, range.getGranularity());
      
      d1 = (new PSDateRange(null, null)).getStart();
      range = new PSDateRange(d1, Granularity.DAY, 5);
      PSDateRange range2 = new PSDateRange(Granularity.DAY, 5);
      assertEquals(formatter.format(range.getStart()), formatter.format(range2.getStart()));
      assertEquals(formatter.format(range.getEnd()), formatter.format(range2.getEnd()));
      assertEquals(range.getGranularity(), range2.getGranularity());
   }

   @Test
   public void testMonthGranularity()
   {
      Date d1 = createDate(2010, 9, 1);
      Date d2 = createDate(2010, 9, 8);
      PSDateRange range = new PSDateRange(d1, d2);
      assertEquals(1, range.getMonthsInRange());
      
      d2 = createDate(2010, 9, 30);
      range = new PSDateRange(d1, d2);
      assertEquals(1, range.getMonthsInRange());
      
      d2 = createDate(2010, 9, 2);
      range = new PSDateRange(d1, d2);
      assertEquals(1, range.getMonthsInRange());
      
      d1 = createDate(2010, 9, 30);
      d2 = createDate(2010, 9, 30);
      range = new PSDateRange(d1, d2);
      assertEquals(1, range.getMonthsInRange());
      
      d2 = createDate(2010, 10, 1);
      range = new PSDateRange(d1, d2);
      assertEquals(2, range.getMonthsInRange());
      
      d1 = createDate(2010, 12, 31);
      d2 = createDate(2011, 1, 1);
      range = new PSDateRange(d1, d2);
      assertEquals(2, range.getMonthsInRange());
      
      d1 = createDate(2010, 12, 31);
      d2 = createDate(2011, 1, 2);
      range = new PSDateRange(d1, d2);
      assertEquals(2, range.getMonthsInRange());
      
      d1 = createDate(2010, 1, 31);
      d2 = createDate(2011, 1, 1);
      range = new PSDateRange(d1, d2);
      assertEquals(13, range.getMonthsInRange());
      
      d1 = createDate(2010, 1, 10);
      d2 = createDate(2011, 12, 15);
      range = new PSDateRange(d1, d2);
      assertEquals(24, range.getMonthsInRange());
      
      d1 = createDate(2010, 1, 1);
      d2 = createDate(2012, 1, 2);
      range = new PSDateRange(d1, d2);
      assertEquals(25, range.getMonthsInRange());
   }

   @Test
   public void testDayGranularity()
   {
      Date d1 = createDate(2010, 9, 1);
      Date d2 = createDate(2010, 9, 8);
      PSDateRange range = new PSDateRange(d1, d2);
      assertEquals(8, range.getDaysInRange());
      
      d2 = createDate(2010, 9, 30);
      range = new PSDateRange(d1, d2);
      assertEquals(30, range.getDaysInRange());
      
      d2 = createDate(2010, 9, 2);
      range = new PSDateRange(d1, d2);
      assertEquals(2, range.getDaysInRange());
      
      d1 = createDate(2010, 2, 28);
      d2 = createDate(2010, 3, 1);
      range = new PSDateRange(d1, d2);
      assertEquals(2, range.getDaysInRange());
      
      d2 = createDate(2010, 3, 2);
      range = new PSDateRange(d1, d2);
      assertEquals(3, range.getDaysInRange());
      
      d1 = createDate(2012, 2, 28);
      d2 = createDate(2012, 3, 1);
      range = new PSDateRange(d1, d2);
      assertEquals(3, range.getDaysInRange());
      
      d1 = createDate(2010, 12, 31);
      d2 = createDate(2011, 1, 2);
      range = new PSDateRange(d1, d2);
      assertEquals(3, range.getDaysInRange());
      
      d1 = createDate(2010, 1, 1);
      d2 = createDate(2010, 12, 31);
      range = new PSDateRange(d1, d2);
      assertEquals(365, range.getDaysInRange());
      
      // w/ leap day
      d1 = createDate(2012, 1, 1);
      d2 = createDate(2013, 1, 1);
      range = new PSDateRange(d1, d2);
      assertEquals(367, range.getDaysInRange());
   }

   @Test
   public void testWeekGranularity()
   {
      Date d1 = createDate(2010, 9, 1);
      Date d2 = createDate(2010, 9, 8);
      PSDateRange range = new PSDateRange(d1, d2);
      assertEquals(2, range.getWeeksInRange());
      
      // single day in week
      d1 = createDate(2010, 10, 3);
      d2 = createDate(2010, 10, 3);
      range = new PSDateRange(d1, d2);
      assertEquals(1, range.getWeeksInRange());
      
      //exactly 1 week
      d1 = createDate(2011, 1, 2);
      d2 = createDate(2011, 1, 8);
      range = new PSDateRange(d1, d2);
      assertEquals(1, range.getWeeksInRange());
      
      // week + trailing
      d1 = createDate(2011, 1, 2);
      d2 = createDate(2011, 1, 9);
      range = new PSDateRange(d1, d2);
      assertEquals(2, range.getWeeksInRange());
      
      // leading + week
      d1 = createDate(2010, 10, 1);
      d2 = createDate(2010, 10, 9);
      range = new PSDateRange(d1, d2);
      assertEquals(2, range.getWeeksInRange());
      
      // leading + week + trailing
      d1 = createDate(2010, 10, 2);
      d2 = createDate(2010, 10, 10);
      range = new PSDateRange(d1, d2);
      assertEquals(3, range.getWeeksInRange());
      
      d1 = createDate(2010, 10, 4);
      d2 = createDate(2010, 10, 22);
      range = new PSDateRange(d1, d2);
      assertEquals(3, range.getWeeksInRange());
      
      //week w/ leap day in it
      d1 = createDate(2012, 2, 26);
      d2 = createDate(2012, 3, 3);
      range = new PSDateRange(d1, d2);
      assertEquals(1, range.getWeeksInRange());
   }

   @Test
   public void testYearGranularity()
   {
      // single year
      Date d1 = createDate(2010, 9, 1);
      Date d2 = createDate(2010, 9, 8);
      PSDateRange range = new PSDateRange(d1, d2);
      assertEquals(1, range.getYearsInRange());
      
      // 2 years, jan 1
      d2 = createDate(2011, 1, 1);
      range = new PSDateRange(d1, d2);
      assertEquals(2, range.getYearsInRange());
      
      d2 = createDate(2011, 1, 2);
      range = new PSDateRange(d1, d2);
      assertEquals(2, range.getYearsInRange());
      
      d1 = createDate(2010, 12, 31);
      d2 = createDate(2011, 1, 1);
      range = new PSDateRange(d1, d2);
      assertEquals(2, range.getYearsInRange());
      
      // multiple years
      d1 = createDate(2010, 9, 30);
      d2 = createDate(2020, 7, 2);
      range = new PSDateRange(d1, d2);
      assertEquals(11, range.getYearsInRange());
   }

   @Test
   public void testGranularityBreakdown()
   {
      //days
      Date d1 = createDate(2010, 9, 1);
      Date d2 = createDate(2010, 9, 8);
      PSDateRange range = new PSDateRange(d1, d2);
      List<Date> results = range.getGranularityBreakdown();
      assertEquals(8, results.size());
      assertTrue(datesAreEqual(results.get(0), d1));
      assertTrue(datesAreEqual(results.get(4), createDate(2010, 9, 5)));
      assertTrue(datesAreEqual(results.get(results.size()-1), d2));
      
      //weeks
      // 1 day
      d1 = createDate(2010, 9, 1);
      d2 = createDate(2010, 9, 1);
      range = new PSDateRange(d1, d2, Granularity.WEEK);
      results = range.getGranularityBreakdown();
      assertEquals(1, results.size());
      assertTrue(datesAreEqual(results.get(0), d1));
      
      //exactly 1 week
      d1 = createDate(2010, 10, 3);
      d2 = createDate(2010, 10, 9);
      range = new PSDateRange(d1, d2, Granularity.WEEK);
      results = range.getGranularityBreakdown();
      assertEquals(1, results.size());
      assertTrue(datesAreEqual(results.get(0), d1));
      
      //leading + week
      d1 = createDate(2010, 10, 1);
      d2 = createDate(2010, 10, 9);
      range = new PSDateRange(d1, d2, Granularity.WEEK);
      results = range.getGranularityBreakdown();
      assertEquals(2, results.size());
      assertTrue(datesAreEqual(results.get(0), d1));
      assertTrue(datesAreEqual(results.get(1), createDate(2010, 10, 3)));
      
      //week + trailing
      d1 = createDate(2010, 10, 3);
      d2 = createDate(2010, 10, 13);
      range = new PSDateRange(d1, d2, Granularity.WEEK);
      results = range.getGranularityBreakdown();
      assertEquals(2, results.size());
      assertTrue(datesAreEqual(results.get(0), d1));
      assertTrue(datesAreEqual(results.get(1), createDate(2010, 10, 10)));
      
      //leading + week + trailing and month boundary
      d1 = createDate(2010, 9, 27);
      d2 = createDate(2010, 10, 16);
      range = new PSDateRange(d1, d2, Granularity.WEEK);
      results = range.getGranularityBreakdown();
      assertEquals(3, results.size());
      assertTrue(datesAreEqual(results.get(0), d1));
      assertTrue(datesAreEqual(results.get(1), createDate(2010, 10, 3)));
      assertTrue(datesAreEqual(results.get(2), createDate(2010, 10, 10)));
      
      //many weeks
      d1 = createDate(2010, 9, 26);
      d2 = createDate(2011, 2, 16);
      range = new PSDateRange(d1, d2, Granularity.WEEK);
      results = range.getGranularityBreakdown();
      assertEquals(21, results.size());
      assertTrue(datesAreEqual(results.get(0), d1));
      assertTrue(datesAreEqual(results.get(1), createDate(2010, 10, 3)));
      assertTrue(datesAreEqual(results.get(9), createDate(2010, 11, 28)));
      assertTrue(datesAreEqual(results.get(20), createDate(2011, 2, 13)));
      
      
      //months
      d1 = createDate(2010, 9, 1);
      d2 = createDate(2010, 9, 8);
      range = new PSDateRange(d1, d2, Granularity.MONTH);
      results = range.getGranularityBreakdown();
      assertEquals(1, results.size());
      assertTrue(datesAreEqual(results.get(0), d1));
      
      d1 = createDate(2010, 9, 3);
      d2 = createDate(2011, 8, 31);
      range = new PSDateRange(d1, d2, Granularity.MONTH);
      results = range.getGranularityBreakdown();
      assertEquals(12, results.size());
      assertTrue(datesAreEqual(results.get(0), d1));
      assertTrue(datesAreEqual(results.get(results.size()-1), createDate(2011, 8, 1)));
      
      d1 = createDate(2010, 9, 1);
      d2 = createDate(2010, 9, 30);
      range = new PSDateRange(d1, d2, Granularity.MONTH);
      results = range.getGranularityBreakdown();
      assertEquals(1, results.size());
      assertTrue(datesAreEqual(results.get(0), d1));
      
      
      //years
      d1 = createDate(2010, 9, 1);
      d2 = createDate(2010, 9, 8);
      range = new PSDateRange(d1, d2, Granularity.YEAR);
      results = range.getGranularityBreakdown();
      assertEquals(1, results.size());
      assertTrue(datesAreEqual(results.get(0), d1));

      d2 = createDate(2020, 12, 31);
      range = new PSDateRange(d1, d2, Granularity.YEAR);
      results = range.getGranularityBreakdown();
      assertEquals(11, results.size());
      assertTrue(datesAreEqual(results.get(0), d1));
      assertTrue(datesAreEqual(results.get(1), createDate(2011, 1, 1)));
      assertTrue(datesAreEqual(results.get(results.size()-1), createDate(2020, 1, 1)));
      
   }
   
   /**
    * Compares the year/month/day parts of two dates for equality. The time
    * component is ignored.
    * 
    * @param d1 Assumed not <code>null</code>.
    * @param d2 Assumed not <code>null</code>.
    * @return <code>true</code> if the date parts of the supplied dates are
    *         the same, otherwise <code>false</code>.
    */
   private boolean datesAreEqual(Date d1, Date d2)
   {
      DateTime actual = new DateTime(d1);
      DateTime expected = new DateTime(d2);
      return actual.getYear() == expected.getYear()
            && actual.getMonthOfYear() == expected.getMonthOfYear()
            && actual.getDayOfYear() == expected.getDayOfYear();

   }
}
