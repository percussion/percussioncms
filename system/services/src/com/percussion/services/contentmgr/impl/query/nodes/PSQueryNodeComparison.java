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

import javax.jcr.query.InvalidQueryException;

/**
 * A comparison query node compares a property and an operator. The child values
 * are Java objects. When processing, one of these must be a property name that
 * is represented as an identifier and the other some literal value. The
 * operator may be any comparison.
 * 
 * @author dougrand
 * 
 */
public class PSQueryNodeComparison implements IPSQueryNode
{
   /**
    * The value of op must not be AND or OR
    */
   private Op m_op;

   /**
    * The values, one may be <code>null</code> and one must be an
    * identifier
    */
   private IPSQueryNode m_left, m_right;

   /**
    * Ctor for comparison node
    * 
    * @param left either the property or literal value, never <code>null</code>
    * @param right either the property or literal value, never <code>null</code>
    * @param op the operator, never <code>null</code>
    */
   public PSQueryNodeComparison(IPSQueryNode left, IPSQueryNode right, Op op) {
      if (left == null && right == null)
      {
         throw new IllegalArgumentException("left and right both may not be null");
      }
      if (op == null)
      {
         throw new IllegalArgumentException("op may not be null");
      }
      m_left = left;
      m_right = right;
      m_op = op;
   }

   public Op getOp()
   {
      return m_op;
   }

   public String toString()
   {
      StringBuilder b = new StringBuilder();
      b.append("qn-compare(");
      if (m_left != null)
      {
         b.append(m_left.toString());
      }
      else
      {
         b.append("<null>");
      }
      b.append(",");
      b.append(m_op);
      b.append(",");
      if (m_right != null)
      {
         b.append(m_right.toString());
      }
      else
      {
         b.append("<null>");
      }
      b.append(")");
      return b.toString();
   }

   /**
    * @return Returns the left.
    */
   public IPSQueryNode getLeft()
   {
      return m_left;
   }

   /**
    * @return Returns the right.
    */
   public IPSQueryNode getRight()
   {
      return m_right;
   }

   public IPSQueryNode accept(PSQueryNodeVisitor visitor) throws InvalidQueryException
   {
      return visitor.visitComparison(this);
   }

}
