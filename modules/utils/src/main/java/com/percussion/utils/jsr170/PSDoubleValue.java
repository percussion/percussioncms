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

/**
 * Represent a double precision floating point value
 * 
 * @author dougrand
 */
public class PSDoubleValue extends PSBaseValue<Double>
{   
   /**
    * Ctor
    * @param number any number, never <code>null</code>
    */
   public PSDoubleValue(Number number) {
      if (number == null)
      {
         throw new IllegalArgumentException("number may not be null");
      }
      m_value = number.doubleValue();
   }

   /**
    * Ctor
    * @param value must be valid number
    * @throws ValueFormatException if invalid numeric format
    */
   public PSDoubleValue(String value) throws ValueFormatException {
      try
      {
         m_value = Double.parseDouble(value);
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
      return PSValueConverter.convertToStream(getString());
   }

   public long getLong() throws ValueFormatException, IllegalStateException,
         RepositoryException
   {
      return m_value.longValue();
   }

   public double getDouble() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      return m_value;
   }

   public Calendar getDate() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      return PSValueConverter.convertToCalendar(getLong());
   }

   public boolean getBoolean() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      throw new ValueFormatException("Unsupported conversion");
   }

   public int getType()
   {
      return PropertyType.DOUBLE;
   }    
}
