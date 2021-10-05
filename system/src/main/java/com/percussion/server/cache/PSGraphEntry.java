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
