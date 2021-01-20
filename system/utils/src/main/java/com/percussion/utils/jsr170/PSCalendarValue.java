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

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

/**
 * Represents a calendar value
 * @author dougrand
 *
 */
public class PSCalendarValue extends PSBaseValue<Calendar>
{  
   /**
    * Ctor
    * @param date the date, never <code>null</code>
    */
   public PSCalendarValue(Date date) {
      if (date == null)
      {
         throw new IllegalArgumentException("date may not be null");
      }
      m_value = new GregorianCalendar();
      m_value.setTime(date);
   }

   /**
    * Ctor
    * @param calendar the calendar value, never <code>null</code>
    */
   public PSCalendarValue(Calendar calendar) {
      if (calendar == null)
      {
         throw new IllegalArgumentException("calendar may not be null");
      }
      m_value = calendar;
   }

   public String getString() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      return PSValueConverter.convertToString(getDate());
   }

   public InputStream getStream() throws IllegalStateException,
         RepositoryException
   {
      return PSValueConverter.convertToStream(getString());
   }

   public long getLong() throws ValueFormatException, IllegalStateException,
         RepositoryException
   {
      return getDate().getTimeInMillis();
   }

   public double getDouble() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      return getDate().getTimeInMillis();
   }

   public Calendar getDate() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      return getValue();
   }

   public boolean getBoolean() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      throw new ValueFormatException("Cannot convert value");
   }

   public int getType()
   {
      return PropertyType.DATE;
   }  
   
   /**
    * Get the calendar data from the value;
    * @return the value, never <code>null</code>.
    */
   private Calendar getValue()
   {
      return m_value;
   }
}
