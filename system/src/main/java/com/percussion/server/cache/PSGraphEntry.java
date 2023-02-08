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

import com.percussion.design.objectstore.PSLocator;

import java.util.Objects;

/**
 * This is a container class, contains a pair values, a value and a related
 * objects.
 */
class PSGraphEntry implements Comparable<PSGraphEntry>
{
   /**
    * Constructs an instance from thesupplied parameters.
    *
    * @param value
    *           the value object, never <code>null</code>.
    * @param relationshipId
    *           the related object, never <code>null</code>.
    * @param sortRank 
    */
   public PSGraphEntry(PSLocator value, Integer relationshipId)
   {
      m_relationshipId = relationshipId;
      m_value = value;
      m_sortRank = -1;
   }

   /**
    * Constructs an instance from thesupplied parameters.
    *
    * @param value
    *           the value object, never <code>null</code>.
    * @param relationshipId
    *           the related object, never <code>null</code>.
    * @param sortRank 
    */
   public PSGraphEntry(PSLocator value, Integer relationshipId, int sortRank)
   {
      m_relationshipId = relationshipId;
      m_value = value;
      m_sortRank = sortRank;
   }

   /**
    * Returns the related object.
    *
    * @return the related object, never <code>null</code>.
    */
   public Integer getrelationshipId()
   {
      return m_relationshipId;
   }

   @Override
   public int compareTo(PSGraphEntry o)
   {
      int result = this.m_sortRank.compareTo(o.m_sortRank);

      if (result == 0) {

           result = this.m_relationshipId.compareTo(o.m_relationshipId);

      }

      return result;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSGraphEntry)) return false;
      PSGraphEntry that = (PSGraphEntry) o;
      return m_relationshipId.equals(that.m_relationshipId) &&
              Objects.equals(m_value, that.m_value) &&
              Objects.equals(m_sortRank, that.m_sortRank);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_relationshipId, m_value, m_sortRank);
   }

   /**
    * Returns the value object.
    *
    * @return the value object, never <code>null</code>.
    */
   public PSLocator getValue()
   {
      return m_value;
   }

   /**
    * Override {@link Object#toString()}
    */
   public String toString()
   {
      return "value=" + m_value.toString() + ", relationshipId="
            + m_relationshipId.toString();
   }
   
   /**
    * The related object, init by ctor, never <code>null</code> or modified 
    * after that.
    */
   private Integer m_relationshipId;

   /**
    * The value object, init by ctor, never <code>null</code> or modified 
    * after that.
    */
   private PSLocator m_value;

   private Integer m_sortRank = -1;
   

}
