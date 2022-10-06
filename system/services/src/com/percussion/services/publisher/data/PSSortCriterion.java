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
package com.percussion.services.publisher.data;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Objects;

/**
 * @author JaySeletz
 *
 */
public class PSSortCriterion
{
   public PSSortCriterion(String property, boolean isAscending)
   {
      Validate.notEmpty(property);
      
      m_property = property;
      m_sortOrder = isAscending;
   }

   public boolean isAscending()
   {
       return m_sortOrder;
   }

   public String getProperty()
   {
       return m_property;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSSortCriterion)) return false;
      PSSortCriterion that = (PSSortCriterion) o;
      return m_sortOrder == that.m_sortOrder && Objects.equals(m_property, that.m_property);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_property, m_sortOrder);
   }

   private final String m_property;

   private final boolean m_sortOrder;
}
