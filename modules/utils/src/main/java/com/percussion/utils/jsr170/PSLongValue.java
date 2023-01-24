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
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Represents a long integer value
 * 
 * @author dougrand
 */
public class PSLongValue extends PSBaseValue<Long>
{
   /**
    * Ctor
    * 
    * @param number any number, never <code>null</code>
    */
   public PSLongValue(Number number) {
      if (number == null)
      {
         throw new IllegalArgumentException("number may not be null");
      }
      m_value = number.longValue();
   }

   /**
    * Ctor
    * 
    * @param value number as a string
    * @throws ValueFormatException if the number is the wrong format
    */
   public PSLongValue(String value) throws ValueFormatException {
      try
      {
         m_value = new Long(value);
      }
      catch (NumberFormatException e)
      {
         throw new ValueFormatException(e);
      }
   }

   public String getString() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      return m_value.toString();
   }

   public InputStream getStream() throws IllegalStateException,
         RepositoryException
   {
      try(InputStream io = PSValueConverter.convertToStream(getString())) {
         return io;
      } catch (IOException e) {
         throw new IllegalStateException("Error reading getString()");
      }
   }

   public long getLong() throws ValueFormatException, IllegalStateException,
         RepositoryException
   {
      return m_value;
   }

   public double getDouble() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      return m_value.doubleValue();
   }

   public Calendar getDate() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      return PSValueConverter.convertToCalendar(m_value);
   }

   public boolean getBoolean() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      throw new ValueFormatException("Unsupported conversion");
   }

   public int getType()
   {
      return PropertyType.LONG;
   }
}
