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
package com.percussion.services.contentmgr.impl.legacy;

import com.percussion.services.contentmgr.impl.IPSTypeKey;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents a child content type for lookup in the configuration
 * 
 * @author dougrand
 */
public class PSContentChildTypeKey implements IPSTypeKey
{
   private long m_contentTypeId;  
   private int m_childTypeId;
   
   /**
    * Ctor for child type key
    * @param ctid content type id
    * @param childid child id
    */
   public PSContentChildTypeKey(long ctid, int childid)
   {
      m_contentTypeId = ctid;
      m_childTypeId = childid;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      PSContentChildTypeKey other = (PSContentChildTypeKey) obj;
      EqualsBuilder eb = new EqualsBuilder();
      return eb.append(m_contentTypeId, other.m_contentTypeId)
         .append(m_childTypeId, other.m_childTypeId).isEquals();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      HashCodeBuilder hb = new HashCodeBuilder();
      return hb.append(m_contentTypeId).append(m_childTypeId)
         .append(m_childTypeId).toHashCode();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      StringBuilder s = new StringBuilder(40);
      s.append("<" + getClass().getCanonicalName());
      s.append("contentType=");
      s.append(m_contentTypeId);
      s.append(" child=");
      s.append(m_childTypeId);
      s.append(" >");
      return s.toString();
   }

   public long getContentType()
   {
      return m_contentTypeId;
   }
}
