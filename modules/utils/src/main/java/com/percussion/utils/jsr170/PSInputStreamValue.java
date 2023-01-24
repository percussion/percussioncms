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


import com.percussion.utils.io.PSReaderInputStream;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

/**
 * Value implementation for binary or large text data.
 * 
 * @author dougrand
 */
public class PSInputStreamValue extends PSBaseValue<InputStream>
{
   /**
    * Track the state of the stream. This boolean is set to <code>true</code>
    * when {@link #getStream()} is called.
    */
   boolean m_streamUsed = false;
   
   /**
    * Track the state of the instance. This boolean is set to <code>true</code>
    * when any get method is called other than {@link #getStream()}.
    */
   boolean m_getUsed = false;
   
   /**
    * The property type is usually binary unless
    * the inputted stream is {@link PSReaderInputStream}.
    */
   int m_propertyType = PropertyType.BINARY;
   
   /**
    * @param b boolean value, never <code>null</code>
    */
   PSInputStreamValue(InputStream b) {
      this(b, b instanceof PSReaderInputStream);
   }
   
   /**
    * 
    * 
    * @param b never <code>null</code>.
    * @param text if <code>true</code> {@link #getType()} will be {@link PropertyType#STRING}.
    */
   PSInputStreamValue(InputStream b, boolean text) {
      m_value = b;
      m_propertyType = text ? PropertyType.STRING : PropertyType.BINARY;
   }

   public String getString() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      if (m_strValue == null)
      {
         if (m_streamUsed) 
            throw new IllegalStateException("May not read from stream twice");

         try( Reader  r = new InputStreamReader(m_value, StandardCharsets.UTF_8)){
            StringBuilder b = new StringBuilder();
            char buf[] = new char[1024];
            int len;
            while((len = r.read(buf)) > 0)
            {
               b.append(buf, 0, len);
            }
            m_strValue = b.toString();
            m_getUsed = true;
         }
         catch (IOException e)
         {
            throw new IllegalStateException("Problem reading from stream",e);
         }
      }
      return m_strValue;
   }

   public InputStream getStream() throws IllegalStateException,
         RepositoryException
   {
      if (m_getUsed) 
         throw new IllegalStateException("May not read from stream twice");
      m_streamUsed = true;
      return m_value;
   }

   public long getLong() throws ValueFormatException, IllegalStateException,
         RepositoryException
   {
      try
      {
         return Long.parseLong(getString());
      }
      catch (NumberFormatException e)
      {
         throw new ValueFormatException(e);
      }
   }

   public double getDouble() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      try
      {
         return Double.parseDouble(getString());
      }
      catch (NumberFormatException e)
      {
         throw new ValueFormatException(e);
      }
   }

   public Calendar getDate() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      return PSValueConverter.convertToCalendar(getString());
   }

   public boolean getBoolean() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      return Boolean.parseBoolean(getString());
   }

   public int getType()
   {
      return m_propertyType;
   }
   
   @Override
   public long getSizeInBytes()
   {
      long size = 0;
      
      // We know the stream implementation
      if (m_value instanceof ByteArrayInputStream)
      {
         size += ((ByteArrayInputStream) m_value).available();
      }
      else if (m_strValue != null)
      {
         size += m_strValue.length() * 2;
      }
      
      return size;
   }   
}
