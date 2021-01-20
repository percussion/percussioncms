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
package com.percussion.deployer.objectstore.idtypes;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * A binding can have name, will have index and value. Index is the location
 * of this expression, in a group of expressions. This is not a template 
 * binding, but a name value pair in which the value must be a JEXL exp.
 * Currently, templates use this notion of bindings
 *
 * @author vamsinukala
 */

public class PSJexlBinding
{
   /** 
    * The name of this binding, some bindings may not have a name i.e may be 
    * <code>null</code> or empty
    */
   private String m_name;

   /**
    * the expression referenced in this binding never <code>null</code>
    */
   private String m_expression;

   /**
    * the position of this binding in the list of bindings.
    */
   private int m_index;

   /**
    * ctor: this is also called copy ctor.. does that matter?
    * @param b binding never <code>null</code>
    */
   public PSJexlBinding(PSJexlBinding b) {
      if (b == null)
         throw new IllegalArgumentException("binding may not be null");
      setName(b.getName());
      setExpression(b.getExpression());
      setIndex(b.getIndex());
   }

   /**
    * ctor
    * @param ix the position of this binding always >= 0
    * @param name the name of this binding, may be <code>null</code> in which
    * case the index will be used
    * @param value the expression that this binding refers to never 
    *        <code>null</code>
    */
   public PSJexlBinding(int ix, String name, String value) {
      if (ix < 0)
         throw new IllegalStateException("ix may not be less than 1");
      setIndex(ix);
      setName(name);
      setExpression(value);
   }

   /**
    * accessor to return the expression referenced by this binding
    * @return the value of this binding never <code>null</code>
    */
   public String getExpression()
   {
      if (StringUtils.isBlank(m_expression))
         throw new IllegalStateException("jexl expression may not be null");
      return m_expression;
   }

   /**
    * Accessor to set the value of this binding 
    * @param expression never <code>null</code>
    */
   public void setExpression(String expression)
   {
      if (StringUtils.isBlank(expression))
         throw new IllegalArgumentException("expression may not be null");
      this.m_expression = expression;
   }

   /**
    * the position of this binding in the list of bindings 
    * @return the index for this binding never <code>null</code> and is always
    * >= 0
    */
   public int getIndex()
   {
      return m_index;
   }

   /**
    * see above, a simple setter
    * @param index always >= 0
    */
   public void setIndex(int index)
   {
      if (index < 0)
         throw new IllegalStateException("index may not be less than 0");
      this.m_index = index;
   }

   /**
    * return the name of this binding 
    * @return the name, may be <code>null</code>
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * set the name of this binding; may be <code>null</code> or empty
    * @param name the name of this binding
    */
   public void setName(String name)
   {
      this.m_name = name;
   }

   @Override
   // see Object
   public boolean equals(Object other)
   {
      boolean isEqual = true;
      PSJexlBinding b = (PSJexlBinding) other;
      if (getIndex() != b.getIndex())
         isEqual = false;
      else if (!getExpression().equals(b.getExpression()))
         isEqual = false;
      else if (StringUtils.isNotBlank(getName())
            && StringUtils.isNotBlank(b.getName())
            && !getName().equals(b.getName()))
         isEqual = false;
      return isEqual;
   }

   @Override
   // see Object
   public int hashCode()
   {
      if (StringUtils.isNotBlank(getName()))
         return getName().hashCode() + getExpression().hashCode() + getIndex()
               + super.hashCode();
      else
         return getExpression().hashCode() + getIndex() + super.hashCode();
   }

   /**
    * make a copy of the binding
    */
   public PSJexlBinding clone()
   {
      PSJexlBinding b = new PSJexlBinding(getIndex(), getName(),
            getExpression());
      b.setExpression(new String(getExpression()));
      return b;
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this,
            ToStringStyle.MULTI_LINE_STYLE).toString();
   }
}
