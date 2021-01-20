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

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

/**
 * Represents a string value for JSR-170
 * 
 * @author dougrand
 */
public class PSStringValue extends PSBaseValue<String>
{
   /**
    * Ctor
    * 
    * @param string new value,
    */
   public PSStringValue(String string) {
      m_value = string;
   }

   public String getString() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      return m_value;
   }

   public InputStream getStream() throws IllegalStateException,
         RepositoryException
   {
       if(m_value == null || m_value.trim().equals(""))
           return null;

       return PSValueConverter.convertToStream(m_value);
   }

   public long getLong() throws ValueFormatException, IllegalStateException,
         RepositoryException
   {
       if(m_value == null || m_value.trim().equals(""))
           return 0;

      try
      {
         return Long.parseLong(m_value);
      }
      catch (NumberFormatException e)
      {
         throw new ValueFormatException(e);
      }
   }

   public double getDouble() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
    if(m_value == null ||  m_value.trim().equals(""))
        return 0;

      try
      {
         return Double.parseDouble(m_value);
      }
      catch (NumberFormatException e)
      {
         throw new ValueFormatException(e);
      }
   }

   public Calendar getDate() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
       if(m_value != null && m_value !="")
            return PSValueConverter.convertToCalendar(m_value);
       else
           return null;
   }

   public boolean getBoolean() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
       if(m_value != null && m_value!="")
          return Boolean.parseBoolean(m_value);
       else
           return false;
   }

   public int getType()
   {
      return PropertyType.STRING;
   }
   
   @Override
   public long getSizeInBytes()
   {
      if (m_value != null && m_value!="")
      {
         return m_value.length() * 2;
      }
      else
      {
         return 0;
      }
   }   
}
