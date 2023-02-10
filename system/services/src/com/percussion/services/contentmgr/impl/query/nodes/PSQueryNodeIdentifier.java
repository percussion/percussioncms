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
import com.percussion.utils.jsr170.PSStringEncoder;

import javax.jcr.query.InvalidQueryException;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a single query identifier
 * 
 * @author dougrand
 */
public class PSQueryNodeIdentifier implements IPSQueryNode
{
   /**
    * The name, never <code>null</code> or empty
    */
   private String m_name;

   /**
    * The type, may be set in the ctor or set after construction by the type
    * visitor
    */
   private int m_type = 0;

   /**
    * Ctor
    * 
    * @param name the name, never <code>null</code> or empty
    */
   public PSQueryNodeIdentifier(String name) {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      m_name = PSStringEncoder.decode(name);
   }
   
   /**
    * ctor
    * 
    * @param name the name, never <code>null</code> or empty
    * @param type the type
    */
   public PSQueryNodeIdentifier(String name, int type) {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      m_name = PSStringEncoder.decode(name);
      m_type = type;
   }   

   /**
    * Get the name of the identifier
    * 
    * @return the name, never <code>null</code> or empty
    */
   public String getName()
   {
      return m_name;
   }

   public String toString()
   {
      return "id(" + m_name + ")";
   }

   public Op getOp()
   {
      return null;
   }

   /**
    * Get the type
    * 
    * @return the type, a value from {@link javax.jcr.PropertyType}
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Set the type
    * 
    * @param type a value from {@link javax.jcr.PropertyType}
    */
   public void setType(int type)
   {
      m_type = type;
   }

   public IPSQueryNode accept(PSQueryNodeVisitor visitor) throws InvalidQueryException
   {
      return visitor.visitIdentifier(this);
   }
}
