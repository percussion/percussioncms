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
