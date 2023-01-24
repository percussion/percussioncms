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
