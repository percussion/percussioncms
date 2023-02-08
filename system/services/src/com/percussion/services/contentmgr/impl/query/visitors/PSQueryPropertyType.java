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

import com.percussion.services.contentmgr.impl.PSContentUtils;
import com.percussion.services.contentmgr.impl.legacy.PSTypeConfiguration;
import com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeIdentifier;

import java.sql.Blob;
import java.sql.Clob;
import java.util.Date;

import javax.jcr.PropertyType;
import javax.jcr.query.InvalidQueryException;

/**
 * Lookup the types of the properties in the tree
 * @author dougrand
 *
 */
public class PSQueryPropertyType extends PSQueryNodeVisitor
{
   /**
    * The type configuration to use when determining the type of a given
    * identifier
    */
   private PSTypeConfiguration m_tc = null;
   
   
   /**
    * ctor
    */
   public PSQueryPropertyType()
   {
   }
   
   /**
    * Set the type configuration
    * @param tc the type configuration, never <code>null</code>
    */
   public void setConfig(PSTypeConfiguration tc)
   {
      if (tc == null)
      {
         throw new IllegalArgumentException("tc may not be null");
      }
      m_tc = tc;

   }

   /** (non-Javadoc)
    * @see com.percussion.services.contentmgr.impl.query.visitors.PSQueryNodeVisitor#visitIdentifier(com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeIdentifier)
    */
   @Override
   public IPSQueryNode visitIdentifier(PSQueryNodeIdentifier identifier) throws InvalidQueryException
   {
      String name = identifier.getName();
      if (!PSContentUtils.isNonPropertyRef(name))
      {
         Class type = m_tc.getPropertyType(name);
         identifier.setType(calculateType(type));
      }
      return identifier;
   }
   
   /**
    * Takes the property type class and calculates the type value
    * 
    * @param typeClass the class, assumed not <code>null</code>
    * @return the type value
    * @throws InvalidQueryException
    */
   private int calculateType(Class typeClass) throws InvalidQueryException
   {
      if (typeClass == byte[].class || typeClass == Blob.class)
      {
         return PropertyType.BINARY;
      }
      else if (typeClass == Boolean.class)
      {
         return PropertyType.BOOLEAN;
      }
      else if (typeClass == Date.class)
      {
         return PropertyType.DATE;
      }
      else if (typeClass == Double.class)
      {
         return PropertyType.DOUBLE;
      }
      else if (typeClass == String.class || typeClass == Clob.class)
      {
         return PropertyType.STRING;
      }
      else if (typeClass == Long.class)
      {
         return PropertyType.LONG;
      }
      throw new InvalidQueryException("Unknown type class "
            + typeClass.getCanonicalName());
   }
}
