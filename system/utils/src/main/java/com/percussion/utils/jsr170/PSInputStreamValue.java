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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.apache.commons.io.IOUtils;

import com.percussion.utils.io.PSReaderInputStream;

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
            StringBuffer b = new StringBuffer();
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
