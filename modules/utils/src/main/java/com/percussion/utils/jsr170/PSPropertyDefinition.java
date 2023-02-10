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
package com.percussion.utils.jsr170;

import org.apache.commons.lang.StringUtils;

import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

/**
 * Basic property definition implementation, not all fields implemented
 * 
 * @author dougrand
 * 
 */
public class PSPropertyDefinition implements PropertyDefinition
{
   /**
    * The property name, never <code>null</code> or empty after construction.
    */
   private String m_name;

   /**
    * If <code>true</code>, this is a multi valued property
    */
   private boolean m_isMultiple;
   
   /**
    * Contains the declared type, see {@link PropertyType} for values
    */
   private int m_type;

   /**
    * The node type of the containing node, never <code>null</code> after
    * ctor
    */
   private NodeType m_nodeType;

   /**
    * Ctor
    * 
    * @param name the name of the property, never <code>null</code> or empty
    * @param multiple if <code>true</code> this property takes multiple values
    * @param type the type of the property, see {@link PropertyType} for values
    * @param nodeType the node type of the containing node for this property,
    * never <code>null</code>
    */
   public PSPropertyDefinition(String name, boolean multiple, int type,
         NodeType nodeType) {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      if (nodeType == null)
      {
         throw new IllegalArgumentException("nodeType may not be null");
      }
      m_name = name;
      m_isMultiple = multiple;
      m_type = type;
      m_nodeType = nodeType;
   }

   public int getRequiredType()
   {
      return m_type;
   }

   public String[] getValueConstraints()
   {
      throw new UnsupportedOperationException("Not implemented");
   }

   public Value[] getDefaultValues()
   {
      throw new UnsupportedOperationException("Not implemented");
   }

   public boolean isMultiple()
   {
      return m_isMultiple;
   }

   public NodeType getDeclaringNodeType()
   {
      return m_nodeType;
   }

   public String getName()
   {
      return m_name;
   }

   public boolean isAutoCreated()
   {
      throw new UnsupportedOperationException("Not implemented");
   }

   public boolean isMandatory()
   {
      throw new UnsupportedOperationException("Not implemented");
   }

   public int getOnParentVersion()
   {
      throw new UnsupportedOperationException("Not implemented");
   }

   public boolean isProtected()
   {
      return false;
   }

}
