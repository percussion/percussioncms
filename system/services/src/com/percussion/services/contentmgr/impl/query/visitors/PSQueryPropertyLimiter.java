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
package com.percussion.services.contentmgr.impl.query.visitors;

import com.percussion.services.contentmgr.impl.PSContentUtils;
import com.percussion.services.contentmgr.impl.legacy.PSTypeConfiguration;
import com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeComparison;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeConjunction;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeIdentifier;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeValue;
import com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode.Op;

import java.util.HashSet;
import java.util.Set;

import javax.jcr.PropertyType;
import javax.jcr.query.InvalidQueryException;

/**
 * This class performs two functions. First, it finds properties that don't
 * exist for the given configuration and transforms comparisons that use them to
 * the boolean value <code>false</code>. Secondly, it propagates that value
 * upward and removes any AND clauses that reference the value.
 * 
 * @author dougrand
 * 
 */
public class PSQueryPropertyLimiter extends PSQueryNodeVisitor
{
   /**
    * The type configuration, may be modified by the accessors
    */
   PSTypeConfiguration m_config;

   /**
    * All the JCR properties for the given configuration, initialized in the
    * ctor and read-only afterward.
    */
   Set<String> m_props;

   /**
    * @return Returns the config.
    */
   public PSTypeConfiguration getConfig()
   {
      return m_config;
   }

   /**
    * @param config The config to set.
    */
   public void setConfig(PSTypeConfiguration config)
   {
      if (config == null)
      {
         throw new IllegalArgumentException("config may not be null");
      }
      m_config = config;
      m_props = new HashSet<>();
      m_props.addAll(m_config.getAllJSR170Properties());
   }

   /** (non-Javadoc)
    * @see com.percussion.services.contentmgr.impl.query.visitors.PSQueryNodeVisitor#visitComparison(com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeComparison)
    */
   @Override
   public IPSQueryNode visitComparison(PSQueryNodeComparison comparisonNode)
         throws InvalidQueryException
   {
      if (m_config == null)
      {
         throw new IllegalStateException("Set config before calling");
      }

      IPSQueryNode left = comparisonNode.getLeft();
      IPSQueryNode right = comparisonNode.getRight();
      IPSQueryNode nleft = null, nright = null;
      if (left != null)
      {
         nleft = left.accept(this);
      }
      if (right != null)
      {
         nright = right.accept(this);
      }

      // If the node was replaced in the comparison, that means that the 
      // property was missing and a new comparison has been returned that
      // evaluates to false, which should be returned
      if (left != null && left != nleft)
      {
         return nleft;
      }
      if (right != null && right != nright)
      {
         return nright;
      }

      return comparisonNode;
   }

   /**
    * Check if the passed node represents a literal <code>false</code> value.
    * 
    * @param node the passed node, assumed never <code>null</code> or empty
    * @return <code>true</code> if the node is a literal <code>false</code>
    */
   private boolean isFalse(IPSQueryNode node)
   {
      if (node instanceof PSQueryNodeValue)
      {
         PSQueryNodeValue value = (PSQueryNodeValue) node;
         if (value != null && value.getValue().equals(Boolean.FALSE))
         {
            return true;
         }
      }
      return false;
   }

   /** (non-Javadoc)
    * @see com.percussion.services.contentmgr.impl.query.visitors.PSQueryNodeVisitor#visitConjunction(com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeConjunction)
    */
   @Override
   public IPSQueryNode visitConjunction(PSQueryNodeConjunction conjunctionNode)
         throws InvalidQueryException
   {
      if (m_config == null)
      {
         throw new IllegalStateException("Set config before calling");
      }

      boolean isand = conjunctionNode.getOp().equals(IPSQueryNode.Op.AND);
      boolean isor = conjunctionNode.getOp().equals(IPSQueryNode.Op.OR);

      IPSQueryNode left = conjunctionNode.getLeft();
      IPSQueryNode right = conjunctionNode.getRight();
      IPSQueryNode nleft = null, nright = null;
      if (left != null)
      {
         nleft = left.accept(this);
      }
      if (right != null)
      {
         nright = right.accept(this);
      }

      // For and operations, if something is false the expression is false
      if (isand)
      {
         if (left != null && left instanceof PSQueryNodeIdentifier
               && isFalse(nleft))
         {
            return nleft;
         }
         if (right != null && right instanceof PSQueryNodeIdentifier
               && isFalse(nright))
         {
            return nright;
         }
      }
      // For or operations, if something is false it can just be removed
      else if (isor)
      {
         if (left != null && left instanceof PSQueryNodeIdentifier
               && isFalse(nleft))
         {
            return nright;
         }
         if (right != null && right instanceof PSQueryNodeIdentifier
               && isFalse(nright))
         {
            return nleft;
         }
      }

      if (left != nleft || right != nright)
      {
         return new PSQueryNodeConjunction(nleft, nright, conjunctionNode
               .getOp());
      }
      else
      {
         return conjunctionNode;
      }
   }

   /** (non-Javadoc)
    * @see com.percussion.services.contentmgr.impl.query.visitors.PSQueryNodeVisitor#visitIdentifier(com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeIdentifier)
    */
   @Override
   public IPSQueryNode visitIdentifier(PSQueryNodeIdentifier identifier) throws InvalidQueryException
   {
      String name = identifier.getName();
      String exname = PSContentUtils.externalizeName(name);

      if (!m_props.contains(name) && !m_props.contains(exname)
            && !PSContentUtils.isNonPropertyRef(name))
      {
         PSQueryNodeValue val = new PSQueryNodeValue(1L);
         val.setType(PropertyType.LONG);
         return new PSQueryNodeComparison(val, val, Op.NE);
      }
      else
      {
         return super.visitIdentifier(identifier);
      }
   }

}
