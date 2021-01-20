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

package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;

import java.util.Iterator;

/**
 * This class represents a view that may be selected based on a condition as
 * well as the name.
 */
public class PSConditionalView extends PSView
{
   /**
    * Creates a conditional view with the supplied conditions.  See
    * {@link PSView base class} for more info.
    *
    * @param name The name of the view.  May not be <code>null</code> or empty.
    * @param fields An iterator over one or more field names as Strings, never
    * <code>null</code> or empty, may not contain <code>null</code> or empty
    * entries.
    * @param conditions A collection of PSConditional objects, may not be
    * <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSConditionalView(String name, Iterator fields,
      PSCollection conditions)
   {
      super(name, fields);

      if (conditions == null || conditions.size() == 0 ||
         !conditions.getMemberClassName().equals(PSConditional.class.getName()))
      {
         throw new IllegalArgumentException(
            "conditions must not be null and must contain at least one " +
               "PSConditional object");
      }

      m_conditions = conditions;

   }

   /**
    * Gets the conditions for this view.
    *
    * @return A collection of PSConditional objects, never <code>null</code> or
    * empty.  Modifications to this collection will be reflected by this object.
    */
   public PSCollection getConditions()
   {
      return m_conditions;
   }

   /**
    * compares this instance to another object.
    *
    * @param obj the object to compare
    * @return returns <code>true</code> if the object is a
    * PSConditionalView with identical values. Otherwise returns
    * <code>false</code>.
    */
   public boolean equals(Object obj)
   {
      boolean isMatch = true;
      if (!(obj instanceof PSConditionalView))
         isMatch = false;
      else if (!super.equals(obj))
         isMatch = false;
      else
      {
         PSConditionalView other = (PSConditionalView)obj;
         if (!this.m_conditions.equals(other.m_conditions))
            isMatch = false;
      }

      return isMatch;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      return super.hashCode();
   }



   /**
    * A collection of PSConditional objects used to determine if this view is to
    * be selected.  Initialized in the constructor, never <code>null</code>
    * after that, may be modified by another class.
    */
   private PSCollection m_conditions = null;
}
