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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.util;

/**
 * The PSDate class stores and retrieves one-day information such as year, month,
 * date of month, hour, minute, and second. It is created for two reasons. First,
 * most constructors and public methods of java.util.Date have been deprecated,
 * which are not safe to use in the long run. Instead, java.util.Calendar is
 * recommended by Sun Microsystems. Second, in java.util.Calendar, some settings
 * affects information retrieving, such as get(Calendar.MONTH), unless the users
 * are fully aware of the pitfalls.
 * <p>
 * Note: as for now, in JDK 1.2's java.util.Calendar object, method get(Calendar.MONTH)
 * returns integers from 0 to 11, rather than 1 to 12, which sometimes causes
 * confusion and error.
 *
 * @author     Jian Huang
 * @version    2.0
 * @since      1.0
 */

public class PSDate
{
   /**
    * Construct a one-day PSDate object. For example, to store 14 o'clock, 23 minutes,
    * and 45 seconds on December 16, 1999, a PSDate object can be created as
    * PSDate myDate = new PSDate(1999, 12, 16, 14, 23, 45); Note: rule validation
    * will be checked, such as month should be between 1 and 12.
    */
   public PSDate(int year, int month, int day, int hour, int minute, int second)
   {
      super();
      setYearMonthDay(year, month, day);
      setTime(hour, minute, second);
   }

   /**
    * Store the given year, month, and date of month. For example,
    * setYearMonthDay(1999, 12, 16) stores December 16, 1999.
    */
   public void setYearMonthDay(int year, int month, int day)
   {
      if ((month < 1) || (month > 12))
         ruleViolation();
      if ((month == 2) && (isLeapYear(year))){
         if ((day < 1) || (day > 29))
            ruleViolation();
      }
      else{
         if ((day < 1) || (day > dayArray[month]))
            ruleViolation();
      }

      m_year = year;
      m_month = month;
      m_day = day;
   }

   /**
    * Get the formatted string of year, month, and date of month. The format
    * follows yyyy-MM-dd model, such as 1999-03-16.
    */
   public String getYearMonthDay()
   {
      String yr = String.valueOf(m_year);

      String mo = String.valueOf(m_month);
      if (m_month < 10)
         mo = "0" + mo;

      String da = String.valueOf(m_day);
      if (m_day < 10)
         da = "0" + da;

      return (yr + "-" + mo + "-" + da);
   }

   /**
    * Store the given hour, minute, and second. For example, set(18, 2, 31)
    * stores 18:02:31, which means 6:02:31 PM.
    */
   public void setTime(int hour, int minute, int second)
   {
      if ((hour < 0) || (hour >= 24))
         ruleViolation();
      if ((minute < 0) || (minute >= 60))
         ruleViolation();
      if ((second < 0) || (second >= 60))
         ruleViolation();

      m_hour = hour;
      m_minute = minute;
      m_second = second;
   }

   /**
    * Get the formatted string of hour, minute, and second. The format
    * follows HH:mm:ss model, such as 18:03:32.
    */
   public String getTime()
   {
      String hr = String.valueOf(m_hour);
      if (m_hour < 10)
         hr = "0" + hr;

      String mi = String.valueOf(m_minute);
      if (m_minute < 10)
         mi = "0" + mi;

      String se = String.valueOf(m_second);
      if (m_second < 10)
         se = "0" + se;

      return (hr + ":" + mi + ":" + se);
   }

   /**
    * Determine whether the input year is a leap year or not.
    */
   public static boolean isLeapYear(int year)
   {
      if ((year % 400 == 0) || (year % 100 != 0 && year % 4 == 0))
         return true;

      return false;
   }

   /**
    * Set the year.
    */
   public void setYear(int year)
   {
      m_year = year;
   }

   /**
    * Get the year of this date.
    */
   public int getYear()
   {
      return m_year;
   }

   /**
    * Set the month.
    */
   public void setMonth(int month)
   {
      if ((month < 1) || (month > 12))
         ruleViolation();

      m_month = month;
   }

   /**
    * Get the month of this date.
    */
   public int getMonth()
   {
      return m_month;
   }

   /**
    * Set the date of the month
    */
   public void setDateOfMonth(int day)
   {
      if ((m_month == 2) && (isLeapYear(m_year))){
         if ((day < 1) || (day > 29))
            ruleViolation();
      }
      else{
         if ((day < 1) || (day > dayArray[m_month]))
            ruleViolation();
      }

      m_day = day;
   }

   /**
    * Get the date of the month of this date.
    */
   public int getDateOfMonth()
   {
      return m_day;
   }

   /**
    * Set the hour.
    */
   public void setHour(int hour)
   {
      if ((hour < 0) || (hour >= 24))
         ruleViolation();

      m_hour = hour;
   }

   /**
    * Get the hour of this date.
    */
   public int getHour()
   {
      return m_hour;
   }

   /**
    * Set the minute.
    */
   public void setMinute(int minute)
   {
      if ((minute < 0) || (minute >= 60))
         ruleViolation();

      m_minute = minute;
   }

   /**
    * Get the minute of this date.
    */
   public int getMinute()
   {
      return m_minute;
   }

   /**
    * Set the second.
    */
   public void setSecond(int second)
   {
      if ((second < 0) || (second >= 60))
         ruleViolation();

      m_second = second;
   }

   /**
    * Get the second of this date.
    */
   public int getSecond()
   {
      return m_second;
   }

   /**
    * Return the string representation of this PSDate object with a format.
    * The format follows yyyy-MM-dd HH:mm:ss, such as 1999-02-16 14:08:45
    */
   public String toString()
   {
      return (getYearMonthDay() + " " + getTime());
   }

   private void ruleViolation()
   {
      int errCode = com.percussion.server.IPSServerErrors.ARGUMENT_ERROR;
      String arg0 = "violated calendar or time rules";
      throw new IllegalArgumentException(errCode + arg0);
   }

   private int[] dayArray = { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

   private int m_year;
   private int m_month;
   private int m_day;
   private int m_hour;
   private int m_minute;
   private int m_second;
}
