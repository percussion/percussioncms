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

import java.util.Collections;
import java.util.List;

import javax.jcr.query.InvalidQueryException;

import org.apache.commons.lang.StringUtils;

/**
 * Query function. This is a placeholder for now, which allows a query to have
 * the function. But please note that the validation process will complain that
 * this is present.
 * 
 * @author dougrand
 * 
 */
public class PSQueryNodeFunction implements IPSQueryNode
{
   /**
    * The name of the function.
    */
   String m_functionName;

   /**
    * The parameters to the function, may be a mix of ids and values. Ids are
    * represented by PSQueryIdentifier, values by literal Java types
    */
   List<IPSQueryNode> m_parameters;

   /**
    * Ctor
    * 
    * @param fname function name, never <code>null</code> or empty
    * @param params the parameters, may be <code>null</code> or empty
    */
   public PSQueryNodeFunction(String fname, List<IPSQueryNode> params) {
      if (StringUtils.isBlank(fname))
      {
         throw new IllegalArgumentException("fname may not be null or empty");
      }
      m_functionName = fname;
      m_parameters = params;
   }

   public Op getOp()
   {
      return Op.FUNC;
   }

   public String toString()
   {
      StringBuilder b = new StringBuilder();
      boolean first = false;
      b.append("qn-function(");
      b.append(m_functionName);
      b.append(":");
      for (Object p : m_parameters)
      {
         if (!first)
         {
            b.append(",");
         }
         else
         {
            first = true;
         }
         b.append(p.toString());
      }
      b.append(")");
      return b.toString();
   }

   public IPSQueryNode accept(PSQueryNodeVisitor visitor)
         throws InvalidQueryException
   {
      return visitor.visitFunction(this);
   }

   /**
    * Get the name of the function
    * 
    * @return the name of the function, never <code>null</code> or empty
    */
   public String getName()
   {
      return m_functionName;
   }

   /**
    * Get a parameter
    * 
    * @param i the parameter index, must be greater than or equal to zero, and
    *           less than the count of parameters.
    * @return the parameter value as a query node
    */
   public IPSQueryNode getParameter(int i)
   {
      if (m_parameters == null || i < 0 || i >= m_parameters.size())
      {
         throw new IndexOutOfBoundsException();
      }
      return (IPSQueryNode) m_parameters.get(i);
   }

   /**
    * Get the number of parameters pass into the ctor
    * 
    * @return the number of parameters, never less than zero, zero if there are
    *         no parameters
    */
   public int getParameterCount()
   {
      return m_parameters != null ? m_parameters.size() : 0;
   }

   /**
    * Get the function name
    * 
    * @return the functionName supplied in the ctor, never <code>null</code>
    *         or empty.
    */
   public String getFunctionName()
   {
      return m_functionName;
   }

   /**
    * Get the parameters supplied to the ctor
    * 
    * @return the parameters as a read-only list or <code>null</code> if none
    *         were supplied
    */
   public List<IPSQueryNode> getParameters()
   {
      if (m_parameters == null)
         return null;
      else
         return Collections.unmodifiableList(m_parameters);
   }

}
