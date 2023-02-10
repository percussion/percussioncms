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
package com.percussion.fastforward.sfp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A List object that produces a SQL "IN" clause. The only restriction placed on
 * objects in this list is that they must have a <code>toString()</code>
 * method that returns their value. <code>java.lang.Integer</code> is a good
 * example of a class that can be added to this list.
 * 
 * @author DavidBenua
 *  
 */
public class PSSqlInList extends ArrayList implements List
{
   /**
    * Creates a list with a specificied capacity. The created object is 
    * {@link #TYPE_LITERAL}.
    * 
    * @param initialCapacity
    */
   public PSSqlInList(int initialCapacity)
   {
      super(initialCapacity);
   }

   /**
    * Creates an empty list. The created object is {@link #TYPE_LITERAL}.
    */
   public PSSqlInList()
   {
      super();
   }

   /**
    * Creates a list containing the members of the specified collection.
    * The created object is {@link #TYPE_LITERAL}.
    * 
    * @param c
    */
   public PSSqlInList(Collection c)
   {
      super(c);
   }

   /**
    * Render a member object as a String.
    * 
    * @param obj the object to render
    * @return the string value, or
    */
   private String getValue(Object obj)
   {
      if (obj == null)
      {
         if (m_type == TYPE_NUMERIC)
            return "";
         else
            return "''";
      }
      else
      {
         if (m_type == TYPE_NUMERIC)
            return obj.toString();
         else
            return "'" + obj.toString() + "'";
      }
   }

   /**
    * Creates a string representation of the list suitable for use in a SQL
    * <code>IN</code> clause.
    * <p>
    * If the list is empty, this method will return an in list that is
    * syntactically correct but will not match any items. Callers who desire
    * different behavior should call <code>isEmpty()</code> directly.
    */
   public String toString()
   {
      if (this.isEmpty())
      {
         if (m_type == TYPE_NUMERIC)
            return "()";
         else
            return "('')";
      }
      Iterator it = this.iterator();
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      sb.append("(");
      while (it.hasNext())
      {
         if (!first)
         {
            sb.append(",");
         }
         sb.append(getValue(it.next()));
         first = false;
      }

      sb.append(")");
      return sb.toString();
   }

   /**
    * Set the IN clause to the supplied type.
    * 
    * @param type the IN clause type, must be one of the TYPE_XXX.
    */
   public void setType(int type)
   {
      if (type != TYPE_NUMERIC && type != TYPE_LITERAL)
         throw new IllegalArgumentException("type must be one of the TYPE_XXX");
      
      m_type = type;
   }
   
   /**
    * The type of the IN clause. Default to 
    * {@link #TYPE_LITERAL}.
    */
   private int m_type = TYPE_LITERAL;
   
   /**
    * Numeric values in the IN clause
    */
   public static final int TYPE_NUMERIC = 0;
   
   /**
    * Literal values in the IN clause
    */
   public static final int TYPE_LITERAL = 1;
}
