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
 * Represents a boolean value, allowing for some conversions to other types
 * @author dougrand
 *
 */
public class PSBooleanValue extends PSBaseValue<Boolean>
{ 
   /**
    * Ctor
    * @param value must be compatible with {@link Boolean#parseBoolean(String)}
    * and not <code>null</code>
    */
   public PSBooleanValue(String value) {
      m_value = Boolean.parseBoolean(value);
   }

   /**
    * Ctor
    * @param arg0
    */
   public PSBooleanValue(boolean arg0) {
      m_value = arg0;
   }

   public String getString() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      return m_value ? "true" : "false";
   }

   public InputStream getStream() throws IllegalStateException,
         RepositoryException
   {
      return PSValueConverter.convertToStream(getString());
   }

   public long getLong() throws ValueFormatException, IllegalStateException,
         RepositoryException
   {
      throw new ValueFormatException("Unsupported conversion");
   }

   public double getDouble() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      throw new ValueFormatException("Unsupported conversion");
   }

   public Calendar getDate() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      throw new ValueFormatException("Unsupported conversion");
   }

   public boolean getBoolean() throws ValueFormatException,
         IllegalStateException, RepositoryException
   {
      return m_value;
   }

   public int getType()
   {
      return PropertyType.BOOLEAN;
   }

   @Override
   public long getSizeInBytes()
   {
      return 4;
   }
   
   
}
