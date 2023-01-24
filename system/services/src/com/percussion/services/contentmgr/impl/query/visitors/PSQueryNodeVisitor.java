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
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeVariable;
import com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode.Op;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.query.InvalidQueryException;

/**
 * The base class for query visitors, used for validation and transformation.
 * Call the visit method to call the top most visit method. Your methods will
 * need to walk down the tree structure as required. The base class demonstrates
 * the correct behavior for each value type.
 * 
 * @author dougrand
 * 
 */
public abstract class PSQueryNodeVisitor
{
   /**
    * Visit a value node
    * 
    * @param value the value, may be <code>null</code>
    * @return any value required, may be <code>null</code>
    * @throws InvalidQueryException
    */
   public IPSQueryNode visitValue(PSQueryNodeValue value)
         throws InvalidQueryException
   {
      return value;
   }
   
   /**
    * Visit a literal node for processing
    * 
    * @param literal the literal node, never <code>null</code>
    * @return the supplied node
    */
   public IPSQueryNode visitLiteral(PSQueryNodeLiteral literal)
   {
      if (literal == null)
      {
         throw new IllegalArgumentException("literal may not be null");
      }
      return literal;
   }

   /**
    * Visit an identifier node
    * 
    * @param identifier the id, never <code>null</code>
    * @return any value required, may be <code>null</code>
    * @throws InvalidQueryException
    */
   public IPSQueryNode visitIdentifier(PSQueryNodeIdentifier identifier)
         throws InvalidQueryException
   {
      if (identifier == null)
      {
         throw new IllegalArgumentException("identifier may not be null");
      }
      return identifier;
   }

   /**
    * Visit a function node. Default implementation copies the node in case
    * the parameter's are modified in their accept methods.
    * 
    * @param functionNode the function, never <code>null</code>
    * @return the result node, may be <code>null</code>
    * @throws InvalidQueryException
    */
   public IPSQueryNode visitFunction(@SuppressWarnings("unused")
   PSQueryNodeFunction functionNode) throws InvalidQueryException
   {
      if (functionNode == null)
      {
         throw new IllegalArgumentException("functionNode may not be null");
      }
      List<IPSQueryNode> params = new ArrayList<>();
      int count = functionNode.getParameterCount();
      for (int i = 0; i < count; i++)
      {
         IPSQueryNode param = functionNode.getParameter(i);
         params.add(param.accept(this));
      }
      return new PSQueryNodeFunction(functionNode.getName(), params);
   }

   /**
    * Visit a conjunction node, return a new node if the left or right values
    * have changed.
    * 
    * @param conjunctionNode the conjunction, never <code>null</code>
    * @return any node required, may be <code>null</code>
    * @throws InvalidQueryException
    */
   public IPSQueryNode visitConjunction(PSQueryNodeConjunction conjunctionNode)
         throws InvalidQueryException
   {
      if (conjunctionNode == null)
      {
         throw new IllegalArgumentException("conjunctionNode may not be null");
      }
      IPSQueryNode left = conjunctionNode.getLeft();
      IPSQueryNode right = conjunctionNode.getRight();
      Op op = conjunctionNode.getOp();

      IPSQueryNode nleft = left != null ? left.accept(this) : left;
      IPSQueryNode nright = right != null ? right.accept(this) : right;

      if (nleft != left || nright != right)
      {
         return new PSQueryNodeConjunction(nleft, nright, op);
      }
      else
      {
         return conjunctionNode;
      }
   }

   /**
    * Visit a comparison node, return a new node if the left or right values
    * have changed.
    * 
    * @param comparisonNode the comparison, never <code>null</code>
    * @return any value required, may be <code>null</code>
    * @throws InvalidQueryException
    */
   public IPSQueryNode visitComparison(PSQueryNodeComparison comparisonNode)
         throws InvalidQueryException
   {
      if (comparisonNode == null)
      {
         throw new IllegalArgumentException("comparisonNode may not be null");
      }
      IPSQueryNode left = comparisonNode.getLeft();
      IPSQueryNode right = comparisonNode.getRight();
      Op op = comparisonNode.getOp();

      IPSQueryNode nleft = left.accept(this);
      IPSQueryNode nright = right.accept(this);

      if (nleft != left || nright != right)
      {
         return new PSQueryNodeComparison(nleft, nright, op);
      }
      else
      {
         return comparisonNode;
      }
   }

   /**
    * The value is either the first value or the second element if the first is
    * not a value
    * 
    * @param left the left element in the comparison
    * @param right the right element in the comparison
    * @return the value or the second property
    */
   protected IPSQueryNode findValue(IPSQueryNode left, IPSQueryNode right)
   {
      if (left instanceof PSQueryNodeValue)
      {
         return left;
      }
      else
      {
         return right;
      }
   }

   /**
    * The property is the first property found
    * 
    * @param left the left element in the comparison
    * @param right the right element in the comparison
    * @return the first property found or <code>null</code> if there is no
    *         property
    */
   protected PSQueryNodeIdentifier findProperty(Object left, Object right)
   {
      if (left instanceof PSQueryNodeIdentifier)
      {
         return (PSQueryNodeIdentifier) left;
      }
      else if (right instanceof PSQueryNodeIdentifier)
      {
         return (PSQueryNodeIdentifier) right;
      }
      else
      {
         return null;
      }
   }

   /**
    * Process the property name to translate from a namespace name into a bean
    * name.
    * 
    * @param propertyName the property name, assumed non-<code>null</code>
    *           and not empty.
    * @return the processed property name
    */
   protected String getJavaBeanProperty(String propertyName)
   {
      int in = propertyName.indexOf(":");
      if (in > -1)
         return propertyName.substring(in + 1);
      else
         return propertyName;
   }

   /**
    * Take a node, which may be a literal value or a variable, and extract the
    * value.
    * 
    * @param value the value, never <code>null</code>
    * @param params parameters that hold values for variable references, may be
    *           <code>null</code>
    * @return the value, might be <code>null</code>
    * @throws InvalidQueryException
    */
   protected Object getNodeValue(PSQueryNodeValue value,
         Map<String, ? extends Object> params) throws InvalidQueryException
   {
      if (value == null)
      {
         throw new IllegalArgumentException("value may not be null");
      }
      Object object;
      if (value instanceof PSQueryNodeVariable)
      {
         PSQueryNodeVariable var = (PSQueryNodeVariable) value;
         object = params != null ? params.get(var.getVariable()) : null;
         if (object == null)
         {
            throw new InvalidQueryException("parameter " + var.getVariable()
                  + " not defined");
         }
         if (object instanceof String[])
         {
            // deref and grab the first value
            String str[] = (String[]) object;
            if (str.length == 0)
            {
               object = null;
            }
            else
            {
               object = str[0];
            }
         }
      }
      else
      {
         object = value.getValue();
      }
      return object;
   }
}
