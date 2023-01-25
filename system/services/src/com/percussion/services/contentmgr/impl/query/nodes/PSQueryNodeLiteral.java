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

import javax.jcr.query.InvalidQueryException;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a literal chunk of HQL to be spliced into the final query.
 * 
 * @author dougrand
 */
public class PSQueryNodeLiteral implements IPSQueryNode
{
   /**
    * The literal hql to include in the final where clause, never
    * <code>null</code> after construction.
    */
   private String m_literal;

   /**
    * Ctor
    * @param literal The literal hql to include in the final where clause, never
    * <code>null</code> or empty.
    */
   public PSQueryNodeLiteral(String literal)
   {
      if (StringUtils.isBlank(literal))
      {
         throw new IllegalArgumentException("literal may not be null or empty");
      }
      m_literal = literal;
   }

   /**
    * @return the literal value supplied in the ctor, never <code>null</code>
    *         or empty, this value will be spliced into the output HQL by the
    *         where builder.
    */
   public String getLiteral()
   {
      return m_literal;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode#accept(com.percussion.services.contentmgr.impl.query.visitors.PSQueryNodeVisitor)
    */
   public IPSQueryNode accept(PSQueryNodeVisitor visitor)
         throws InvalidQueryException
   {
      return visitor.visitLiteral(this);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode#getOp()
    */
   public Op getOp()
   {
      return null;
   }
}
