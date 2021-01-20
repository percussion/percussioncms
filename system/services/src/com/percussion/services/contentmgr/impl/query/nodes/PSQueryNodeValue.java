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
package com.percussion.services.contentmgr.impl.query.nodes;

import com.percussion.services.contentmgr.impl.query.visitors.PSQueryNodeVisitor;
import com.percussion.services.contentmgr.impl.query.visitors.PSQueryWhereBuilder;

import javax.jcr.query.InvalidQueryException;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents a single value in the query tree
 * 
 * @author dougrand
 */
public class PSQueryNodeValue implements IPSQueryNode
{
   /**
    * The value of the node, may be <code>null</code> after construction
    */
   Object m_value;

   /**
    * The type, only set once we're ready to output in the where builder. If the
    * type is unset when getting the output (as a string), then an exception
    * will be thrown.
    */
   int m_type = -1;

   /**
    * Ctor
    * 
    * @param val the value
    */
   public PSQueryNodeValue(Object val) {
      m_value = val;
   }

   public Op getOp()
   {
      return null;
   }

   public IPSQueryNode accept(PSQueryNodeVisitor visitor)
         throws InvalidQueryException
   {
      return visitor.visitValue(this);
   }

   @Override
   public String toString()
   {
      return m_value != null ? m_value.toString() : "<<null>>";
   }

   /**
    * Get the type of the value. The value type must be set by the creator
    * before this node is used
    * 
    * @return the type, the value matches a type from
    *         {@link javax.jcr.PropertyType} if it is valid. A value of
    *         <code>-1</code> means the type was never set. Other values
    *         may be set to indicate extraordinary situations, c.f. 
    *         <code>FORCE_INTEGER</code> in {@link PSQueryWhereBuilder}
    *         for an example.
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Set a new type
    * 
    * @param type
    */
   public void setType(int type)
   {
      m_type = type;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object arg0)
   {
      PSQueryNodeValue val = (PSQueryNodeValue) arg0;
      return new EqualsBuilder().append(getValue(), val.getValue()).isEquals();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(m_value).toHashCode();
   }

   /**
    * Get the value of this node
    * @return the value
    */
   public Object getValue()
   {
      return m_value;
   }

}
