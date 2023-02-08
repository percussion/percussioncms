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
package com.percussion.utils.jsr170;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
