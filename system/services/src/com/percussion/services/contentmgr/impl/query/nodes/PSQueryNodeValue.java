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
