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
package com.percussion.utils.jsr170;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

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
