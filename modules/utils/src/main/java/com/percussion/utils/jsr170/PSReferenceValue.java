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

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

/**
 * Value that holds a reference
 * 
 * @author dougrand
 */
public class PSReferenceValue extends PSBaseValue<Node>
{   
   /**
    * Constructor
    * @param arg0 the node value, may be <code>null</code>
    */
   public PSReferenceValue(Node arg0) {
      m_value = arg0;
   }

   public String getString() throws
         IllegalStateException, RepositoryException
   {
      if (m_value != null)
         return m_value.getUUID();
      else
         return null;
   }

   public InputStream getStream() throws IllegalStateException,
         RepositoryException
   {
      String stringrep = getString();
      if (stringrep != null) {
         return new ByteArrayInputStream(stringrep.getBytes(StandardCharsets.UTF_8));
      }
      else
         return null;
   }

   public long getLong() throws IllegalStateException,
         RepositoryException
   {
      throw new ValueFormatException("reference cannot be converted to long");
   }

   public double getDouble() throws
         IllegalStateException, RepositoryException
   {
      throw new ValueFormatException("reference cannot be converted to double");
   }

   public Calendar getDate() throws
         IllegalStateException, RepositoryException
   {
      throw new ValueFormatException("reference cannot be converted to date");
   }

   public boolean getBoolean() throws
         IllegalStateException, RepositoryException
   {
      throw new ValueFormatException("reference cannot be converted to boolean");
   }

   public int getType()
   {
      return PropertyType.REFERENCE;
   }

   @Override
   public long getSizeInBytes() throws RepositoryException {
      return ((IPSJcrCacheItem) m_value).getSizeInBytes() + 4;
   }

}
