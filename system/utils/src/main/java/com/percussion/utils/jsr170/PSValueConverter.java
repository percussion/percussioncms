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
package com.percussion.utils.jsr170;

import com.percussion.utils.date.PSConcurrentDateFormat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.jcr.ValueFormatException;

/**
 * Utility to convert values from one type to another
 * 
 * @author dougrand
 */
public class PSValueConverter
{
   static final PSConcurrentDateFormat ms_sdf = new PSConcurrentDateFormat(
         "yyyy-MM-dd HH:mm:ss");

   /**
    * An array of pre-set date pattern string to be used to determine whether a
    * given string/text is recognizable as a date. In order to be recognized as
    * a date more efficiently, it is better for a string to include year, month,
    * and date. Some popular date patterns are NOT supported here, such as
    * "dd/MM/yyyy" and any pattern using a two digit year cause confusion.
    * That's because in JAVA, for example, "03/30/1999" and "03/30/99" would be
    * recognized recognized respectively as March 30, 1999 AD and March 30, 99
    * AD. But in daily life, people tend to regard both expression as the same.
    */
   private static SimpleDateFormat[] ms_datePatternArray =
   {
   // Accurate ones should be listed first
         new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
         new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"),
         new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
         new SimpleDateFormat("yyyy-MMMM-dd 'at' hh:mm:ss aaa"),
         new SimpleDateFormat("yyyy-MMMM-dd HH:mm:ss"),
         new SimpleDateFormat("yyyy.MMMM.dd 'at' hh:mm:ss aaa"),
         new SimpleDateFormat("yyyy.MMMM.dd HH:mm:ss"),
         new SimpleDateFormat("yyyy.MMMM.dd 'at' hh:mm aaa"),
         new SimpleDateFormat("yyyy-MM-dd G 'at' HH:mm:ss"),
         new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss"),
         new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS"),
         new SimpleDateFormat("yyyy.MM.dd HH:mm:ss"),
         new SimpleDateFormat("yyyy/MM/dd G 'at' HH:mm:ss"),
         new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS"),
         new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"),
         new SimpleDateFormat("yyyy/MM/dd HH:mm"),
         new SimpleDateFormat("yyyy-MM-dd"),
         new SimpleDateFormat("yyyy.MM.dd"),
         new SimpleDateFormat("yyyy/MM/dd"),
         new SimpleDateFormat("yyyy-MMMM-dd"),
         new SimpleDateFormat("yyyy.MMMM.dd"), new SimpleDateFormat("yyyy")};

   /**
    * Correctly convert calendar value.
    * Note, this is a synchronized call (executing one thread at a time), 
    * as to prevent some of the non-thread safe (internal) operations. 
    * 
    * @param datestr a date string in ISO 8601 format
    * @return a calendar object, never <code>null</code>
    * @throws ValueFormatException if the string doesn't correspond to the date
    *            format
    */
   public static synchronized Calendar convertToCalendar(String datestr)
         throws ValueFormatException
   {
      Date d = null;
      
      for(SimpleDateFormat fmt : ms_datePatternArray)
      {
         try
         {
            d = fmt.parse(datestr);
            break;
         }
         catch(Exception e)
         {
            // Ignore, value will be null when this loop ends if nothing
            // converted.
         }
      }
      if (d == null)
      {
         throw new ValueFormatException("Could not convert date: " + datestr);
      }
      Calendar cal = new GregorianCalendar();
      cal.setTime(d);
      return cal;
   }

   /**
    * Convert millis to a calendar
    * 
    * @param millis number of millis
    * @return a calendar object, never <code>null</code>
    */
   public static synchronized Calendar convertToCalendar(long millis)
   {
      Calendar cal = new GregorianCalendar();
      cal.setTimeInMillis(millis);
      return cal;
   }

   /**
    * Convert calendar to string
    * 
    * @param cal calendar, never <code>null</code>
    * @return string in ISO 8601 format
    */
   public static String convertToString(Calendar cal)
   {
      if (cal == null)
      {
         throw new IllegalArgumentException("cal may not be null");
      }
      return ms_sdf.format(cal.getTime());
   }

   /**
    * Convert string to byte array input stream
    * 
    * @param val string, never <code>null</code>
    * @return stream
    */
   public static InputStream convertToStream(String val)
   {
      if (val == null)
      {
         throw new IllegalArgumentException("val may not be null");
      }
      return new ByteArrayInputStream(val.getBytes(StandardCharsets.UTF_8));
   }
}
