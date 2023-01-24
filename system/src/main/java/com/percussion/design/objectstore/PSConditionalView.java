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
