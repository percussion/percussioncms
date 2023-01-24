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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Specifies a list of fields that are included in a particular content editor
 * view.
 */
public class PSView
{
   /**
    * Constructs a view with the specified name and list of fields.
    *
    * @param name The name of the view.  May not be <code>null</code> or empty.
    * @param fields An iterator over zero or more field names as Strings, never
    * <code>null</code> or empty, may not contain <code>null</code> or empty
    * entries.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSView(String name, Iterator fields)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      if (fields == null)
         throw new IllegalArgumentException("fields may not be null");

      m_name = name;
      m_fields = new ArrayList();

      while (fields.hasNext())
      {
         Object o = fields.next();
         if (!(o instanceof String))
            throw new IllegalArgumentException(
               "fields must contain only Strings");

         String field = (String)o;
         if (field.trim().length() == 0)
            throw new IllegalArgumentException(
               "fields may not contain empty Strings");

         m_fields.add(field);
      }
   }


   /**
    * @return The name of this view, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * @return An iterator over zero or more field names as non-<code>null</code>
    * Strings.  Never <code>null</code>.
    */
   public Iterator getFields()
   {
      return m_fields.iterator();
   }


   /**
    * compares this instance to another object.
    *
    * @param obj the object to compare
    * @return returns <code>true</code> if the object is a
    * PSView with identical values. Otherwise returns
    * <code>false</code>.
    */
   public boolean equals(Object obj)
   {
      boolean isMatch = true;
      if (!(obj instanceof PSView))
         isMatch = false;
      else
      {
         PSView other = (PSView)obj;
         if (!this.m_name.equals(other.m_name))
            isMatch = false;
         else if (!this.m_fields.equals(other.m_fields))
            isMatch = false;
      }

      return isMatch;
   }

   /**
    * Overridden to fullfill the contract that if t1 and t2 are 2 different
    * instances of this class and t1.equals(t2), t1.hashCode() ==
    * t2.hashCode().
    *
    * @return The sum of all the hash codes of the composite objects.
    */
   public int hashCode()
   {
      int hash = 0;
      hash += m_name.hashCode();
      hash += m_fields.hashCode();
      return hash;
   }

   /**
    * The name of the view, never <code>null</code>, empty, or modified after
    * construction.
    */
   private String m_name;

   /**
    * The names of the fields as Strings that are included in this view,
    * never <code>null</code> or modified after construction, may be empty.
    */
   private List m_fields;
}
