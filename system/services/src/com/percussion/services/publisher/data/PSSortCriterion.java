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
