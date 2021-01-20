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
