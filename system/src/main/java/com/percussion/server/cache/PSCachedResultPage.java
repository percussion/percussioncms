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
package com.percussion.server.cache;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * A cached result page represents a single result from a query app
 * 
 * @author dougrand
 */
public class PSCachedResultPage implements Serializable
{
   /**
    * Generated id for class
    */
   private static final long serialVersionUID = 7758292294312394154L;
   
   /**
    * Holds the result data from the merging process
    */
   private byte[] m_resultData;
   
   /**
    * Holds the calculated mime type from the merging process
    */
   private String m_mimeType;
   
   /**
    * Ctor
    * @param type the type, may be <code>null</code>
    * @param data the data, may be <code>null</code>
    */
   public PSCachedResultPage(String type, byte[] data)
   {
      m_mimeType = type;
      m_resultData = data;
   }

   /**
    * @return Returns the mimeType.
    */
   public String getMimeType()
   {
      return m_mimeType;
   }

   /**
    * @return Returns the resultData.
    */
   public byte[] getResultData()
   {
      return m_resultData;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSCachedResultPage)) return false;
      PSCachedResultPage that = (PSCachedResultPage) o;
      return Arrays.equals(m_resultData, that.m_resultData) && Objects.equals(m_mimeType, that.m_mimeType);
   }

   @Override
   public int hashCode() {
      int result = Objects.hash(m_mimeType);
      result = 31 * result + Arrays.hashCode(m_resultData);
      return result;
   }

   /** (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      StringBuilder b = new StringBuilder(80);
      
      b.append("<CacheItem mimeType:");
      b.append(m_mimeType == null ? "none" : m_mimeType);
      b.append(" data-length:");
      b.append(m_resultData == null ? "none" : String.valueOf(m_resultData.length));
      b.append(">");
      
      return b.toString();
   }

   /**
    * Get the length of the enclosed data
    * @return the length, or <code>0</code> if there is no data.
    */
   public int getLength()
   {
      return m_resultData == null ? 0 : m_resultData.length;
   }
   
   
}
