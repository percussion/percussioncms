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
package com.percussion.services.contentmgr.impl.query.visitors;

import com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeComparison;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeConjunction;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeFunction;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeIdentifier;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeLiteral;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeValue;

import javax.jcr.query.InvalidQueryException;

/**
 * Pretty printer, primarily for testing and debug purposes. Use the
 * static method of this class as a detail formatter for IPSQueryNode
 * 
 * @author dougrand
 *
 */
public class PSQueryNodePrinter extends PSQueryNodeVisitor
{
   /**
    * Builder for use in output
    */
   private StringBuilder m_builder = new StringBuilder();
   
   /**
    * The current indentation level
    */
   private int m_indent = -2;
   
   /**
    * Pretty print the given query
    * @param n the query node root to pretty print, never <code>null</code>
    * @return the pretty print output, never <code>null</code> or empty
    * @throws InvalidQueryException
    */
   public static String prettyPrint(IPSQueryNode n)
   throws InvalidQueryException
   {
      if (n == null)
      {
         throw new IllegalArgumentException("n may not be null");
      }
      PSQueryNodePrinter p = new PSQueryNodePrinter();
      n.accept(p);
      return p.m_builder.toString();
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.contentmgr.impl.query.PSQueryNodeVisitor#visitComparison(com.percussion.services.contentmgr.impl.query.PSQueryNodeComparison)
    */
   @Override
   public IPSQueryNode visitComparison(PSQueryNodeComparison comparisonNode) throws InvalidQueryException
   {
      start();
      indent();
      comparisonNode.getLeft().accept(this);
      m_builder.append(' ');
      m_builder.append(comparisonNode.getOp().toString());
      m_builder.append(' ');
      comparisonNode.getRight().accept(this);
      m_builder.append('\n');
      end();
      return comparisonNode;
   }
   

   /* (non-Javadoc)
    * @see com.percussion.services.contentmgr.impl.query.PSQueryNodeVisitor#visitConjunction(com.percussion.services.contentmgr.impl.query.PSQueryNodeConjunction)
    */
   @Override
   public IPSQueryNode visitConjunction(PSQueryNodeConjunction conjunctionNode) throws InvalidQueryException
   {
      start();
      indent();
      m_builder.append("{\n");
      if (conjunctionNode.getLeft() != null)
         conjunctionNode.getLeft().accept(this);
      indent();
      m_builder.append(conjunctionNode.getOp().toString());
      m_builder.append('\n');
      conjunctionNode.getRight().accept(this);
      indent();
      m_builder.append("}\n");
      end();
      return conjunctionNode;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.contentmgr.impl.query.PSQueryNodeVisitor#visitFunction(com.percussion.services.contentmgr.impl.query.PSQueryNodeFunction)
    */
   @Override
   public IPSQueryNode visitFunction(PSQueryNodeFunction functionNode) throws InvalidQueryException
   {
      start();
      indent();
      m_builder.append(functionNode.getFunctionName());
      m_builder.append('(');
      m_builder.append(functionNode.getParameters());
      m_builder.append(')');
      m_builder.append('\n');
      end();
      return functionNode;
   }

   @Override
   public IPSQueryNode visitIdentifier(PSQueryNodeIdentifier identifier)
   {
      m_builder.append(identifier.getName());
      return identifier;
   }
   
   @Override
   public IPSQueryNode visitLiteral(PSQueryNodeLiteral lit)
   {
      m_builder.append(lit.getLiteral());
      return lit;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.contentmgr.impl.query.visitors.PSQueryNodeVisitor#visitValue(com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeValue)
    */
   @Override
   public IPSQueryNode visitValue(PSQueryNodeValue value) throws InvalidQueryException
   {
      if (value.getValue() != null)
         m_builder.append(value.getValue());
      else
         m_builder.append("<<null>>");
      return value;
   }
   
   /**
    * Start a section, increment indentation
    */
   private void start()
   {
      m_indent += 2;
   }
   
   /**
    * Indent a line
    */
   private void indent()
   {
      for(int i = 0; i < m_indent; i++)
      {
         m_builder.append(' ');
      }
   }

   /**
    * End a section, decrement indentation
    */
   private void end()
   {
      m_indent -= 2;
   }
}
