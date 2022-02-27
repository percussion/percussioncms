/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

import org.apache.commons.lang3.time.FastDateFormat;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Date;

/**
 *   The PSDateFormatISO8601 formats dates in ISO 8601 format.
 *   (yyyyMMdd'T'HHmmssSSS). For instance, 19981231T00000000 represents
 *   midnight, December 31, 1998. mm are milliseconds, and they
 *   can be left out if desired.
 *
 *   Note that this class does not extend DateFormat, because the ISO 8601
 *   format is much less flexible than a generic (or even simple) date
 *   format.
 */
 
public class PSDateFormatISO8601 extends Format
{
   /**
    * Constructs an ISO 8601 date formatter.
    */
   public PSDateFormatISO8601()
   {
      m_df = FastDateFormat.getInstance("yyyyMMdd'T'HHmmssSSS");

   }
   
   /**
    * Formats the provided date into ISO 8601 and returns its string 
    * representation.
    *
    * @param date the date to be formated, not <code>null</code>.
    * @return a new String representing the given date in ISO 8601 format.
    * @throws IllegalArgumentException if the provided date is 
    *    <code>null</code>.
    */   
   public final String format(Date date) throws IllegalArgumentException
   {
      if (date == null)
         throw new IllegalArgumentException("date cannot be null");
      
      return m_df.format(date);
   }
   
   /**
    *   Formats a time object into an ISO 8601 time string.
    *
    *   @param ob the date object to be formatted, must be of type Date, not
    *    <code>null</code>.
    *
    *   @param appendTo the string buffer for the returning time string.
    *
    *   @param pos the field position.
    *   On input, the position at which to start parsing; on output,
    *   the position at which parsing terminated, or the start position if the
    *   parse failed.
    *
    *   set to the beginning and end of its field parameter, which
    *   must be one of:
    *   <UL>
    *      <LI><CODE>DateFormat.YEAR_FIELD</CODE>
    *      <LI><CODE>DateFormat.MONTH_FIELD</CODE>
    *      <LI><CODE>DateFormat.DATE_FIELD</CODE>
    *      <LI><CODE>DateFormat.HOUR_OF_DAY0_FIELD</CODE>
    *      <LI><CODE>DateFormat.MINUTE_FIELD</CODE>
     *      <LI><CODE>DateFormat.SECOND_FIELD</CODE>
     *      <LI><CODE>DateFormat.MILLISECOND_FIELD</CODE>
    *   </UL>
    *   @return the formatted ISO 8601 time string, appended at the
    *      position given.
    *   @throws IllegalArgumentException if any parameter is <code>null</code>.
    */
   public final StringBuffer format(Object ob, StringBuffer appendTo,
      FieldPosition pos)
      throws IllegalArgumentException
   {
      if (ob == null || appendTo == null || pos == null)
         throw new IllegalArgumentException("parameters cannot be null");
      
      return m_df.format(ob, appendTo, pos);
   }

   /**
    * Parses the provided string for an ISO 8601 formatted date.
    *
    *   @return   A new java.util.Date object built from the specified
    *   string, which contains an ISO 8601 format starting at position
    *   pos. Returns <CODE>null</CODE> if the format was invalid.
    *
    *   @param   source   The string containing an ISO 8601 date representation
    *
    *   @param   pos   On input, the position at which to start parsing; on output,
    *   the position at which parsing terminated, or the start position if the
    *   parse failed.
    */
   public Date parse(String source, ParsePosition pos)
   {
      return m_df.parse(source, pos);
   }

   /**
    *   A convenience method. Simply calls parse(String, ParsePosition).
    * See {@link #parseObject(String, ParsePosition)} for
    * description.
    */
   public Object parseObject(String source, ParsePosition pos)
   {
      return parse(source, pos);
   }
   
   /**
    * A simple date formatter, initialized during construction, never 
    * <code>null</code> after that.
    */
   private FastDateFormat m_df = null;
}
