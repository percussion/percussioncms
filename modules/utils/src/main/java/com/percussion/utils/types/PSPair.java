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
package com.percussion.utils.types;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * This class holds a generic pair of objects
 * 
 * @author dougrand
 */
public class PSPair<A, B>
{
   private A m_first;

   private B m_second;

   /**
    * Default ctor
    */
   public PSPair()
   {
      // 
   }
   
   /**
    * Ctor to create an instance
    * 
    * @param first the first element, may be <code>null</code>
    * @param second the second element, may be <code>null</code>
    */
   public PSPair(A first, B second) {
      m_first = first;
      m_second = second;
   }

   /**
    * @return Returns the first.
    */
   public A getFirst()
   {
      return m_first;
   }

   /**
    * @return Returns the second.
    */
   public B getSecond()
   {
      return m_second;
   }

   
   /**
    * @param first The first to set.
    */
   public void setFirst(A first)
   {
      m_first = first;
   }

   /**
    * @param second The second to set.
    */
   public void setSecond(B second)
   {
      m_second = second;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @SuppressWarnings("unchecked")
   @Override
   public boolean equals(Object obj)
   {
      if(!(obj instanceof PSPair))
         return false;
      PSPair<A, B> b = (PSPair<A, B>) obj;
      return new EqualsBuilder().append(m_first, b.m_first).append(m_second,
            b.m_second).isEquals();

   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      HashCodeBuilder hc = new HashCodeBuilder();
      return hc.append(m_first).append(m_second).toHashCode();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE).append("first", m_first).append(
            "second", m_second).toString();
   }

}
