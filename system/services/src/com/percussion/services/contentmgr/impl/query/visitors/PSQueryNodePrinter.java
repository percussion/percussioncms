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
