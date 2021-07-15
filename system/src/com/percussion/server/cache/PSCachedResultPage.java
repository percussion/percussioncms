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
package com.percussion.server.cache;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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

   /** (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj);
   }

   /** (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
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
