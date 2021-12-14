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

import com.percussion.util.PSCollection;

import java.util.Iterator;



/**
* This class is a wrapper for a PSCollection of conditionals
* so we can define a specific <code>toString()</code> method to print out
* the collection of conditionals.
*/
public class PSConditionalSet extends PSCollection
{
    /**
     * Construct PSConditionalSet from a PSCollection of
     * PSConditional objects
     * @param coll PSCollection of PSConditional objects. Must not
     * be <code>null</code>.
     */
    public PSConditionalSet(PSCollection coll)
    {
       this();
       if(coll == null)
          throw new IllegalArgumentException("coll may not be null.");
       addAll(coll);
    }

    /**
     * Constructs an empty PSConditionalSet
     */
    public PSConditionalSet()
    {
       super(PSConditional.class);
    }

   /**
    * Set the maximum <code>toString()</code> display length.
    * The {@link #m_maxLength) member is modified.
    * @param length the maximum display length
    * for toString
    */
   public void setMaxDisplayLength(int length)
   {
      m_maxLength = length;
   }


   /**
    * Takes the collection of PSConditional objects and merges them
    * together as a string for display adding parens if needed. The
    * string will be truncated to m_maxLength which defaults to 42.
    *
    * @param coll the PSCollection of PSConditional objects. Can be
    * <code>null</code>.    *
    *
    * @return a String representing the collection of conditionals
    * for display. May be empty.
    *
    */
   public String toString()
   {
      if(this.isEmpty())
         return "";

      StringBuilder sb = new StringBuilder();
      Iterator it = this.iterator();
      PSConditional cond = null;
      String openPara = "";
      String closePara = "";
      if(this.size()>1)
      {
         openPara = "(";
         closePara = ")";
      }
      while(it.hasNext())
      {
         cond = (PSConditional)it.next();
         String value = cond.getValue() != null ? 
            cond.getValue().getValueDisplayText() : null;
         String variable = cond.getVariable() != null ? 
            cond.getVariable().getValueDisplayText() : null;
         String op = cond.getOperator();
         String bool = cond.getBoolean();
         sb.append(openPara + variable);
         sb.append(" ");
         sb.append(op);
         sb.append(" ");
         sb.append(value + closePara);
         if(it.hasNext())
            sb.append(" " + bool + " ");
      }
      if(sb.length() <= m_maxLength)
         return sb.toString();

      return sb.toString().substring(
         0,m_maxLength-(sb.length()>=3?3:sb.length())) + "...";
   }

   /**
    * toString max display length. Defaults to 42. Modified
    * in {@link #setMaxDisplayLength(int)}.
    */
   private int m_maxLength = 42;

}
