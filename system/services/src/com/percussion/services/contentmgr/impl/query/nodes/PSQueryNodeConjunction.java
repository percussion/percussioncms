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
 * This query node represents a conjunction condition. It may join two other
 * nodes or negate a node
 * 
 * @author dougrand
 */
public class PSQueryNodeConjunction implements IPSQueryNode
{
   /**
    * The value of op must be either NOT, AND or OR
    */
   private Op m_op;

   /**
    * The left and right nodes
    */
   private IPSQueryNode m_left, m_right;

   /**
    * Ctor to create a conjunction node
    * 
    * @param left the left value, never <code>null</code> unless op is NOT
    * @param right the right value, never <code>null</code>
    * @param op the operator, never <code>null</code>
    */
   public PSQueryNodeConjunction(IPSQueryNode left, IPSQueryNode right, Op op) {
      if (left == null && !op.equals(Op.NOT))
      {
         throw new IllegalArgumentException(
               "left may not be null unless op is NOT");
      }
      if (right == null)
      {
         throw new IllegalArgumentException("right may not be null");
      }
      if (op == null)
      {
         throw new IllegalArgumentException("op may not be null");
      }
      m_left = left;
      m_right = right;
      m_op = op;
   }

   /**
    * The left subnode of this query conjunction
    * 
    * @return the left subnode, never <code>null</code>
    */
   public IPSQueryNode getLeft()
   {
      return m_left;
   }

   /**
    * The right subnode of this query conjunction
    * 
    * @return the right subnode, never <code>null</code>
    */
   public IPSQueryNode getRight()
   {
      return m_right;
   }

   public Op getOp()
   {
      return m_op;
   }

   public String toString()
   {
      StringBuilder b = new StringBuilder();
      b.append("qn-conjunction(");
      if (m_left != null)
      {
         b.append(m_left.toString());
         b.append(",");
      }
      b.append(m_op);
      b.append(",");
      b.append(m_right.toString());
      b.append(")");
      return b.toString();
   }

   public IPSQueryNode accept(PSQueryNodeVisitor visitor) throws InvalidQueryException
   {
      return visitor.visitConjunction(this);
   }
}
