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
