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
      StringBuffer sb = new StringBuffer();
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
