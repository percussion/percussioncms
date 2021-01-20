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

import javax.jcr.Value;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Base implementation class for JCR values
 * 
 * @author dougrand
 * @param <Type> the value type encapsulated by the subclass of this abstract
 * class.
 *
 */
public abstract class PSBaseValue<Type> implements Value, IPSJcrCacheItem
{
   /**
    * Holds value, never <code>null</code> after ctor
    */
   protected Type m_value;
   
   /**
    * Hold string value once loaded
    */
   protected String m_strValue = null;
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (! (obj instanceof PSBaseValue)) return false;
      
      PSBaseValue b = (PSBaseValue) obj;
      EqualsBuilder eb = new EqualsBuilder();
      
      return eb.append(m_value, b.m_value).isEquals();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      HashCodeBuilder b = new HashCodeBuilder();
      return b.append(m_value).toHashCode();
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }
   /*
    * (non-Javadoc)
    * @see com.percussion.utils.jsr170.IPSJcrCacheItem#getSizeInBytes()
    */
   public long getSizeInBytes()
   {
      return 8;
   } 
}
