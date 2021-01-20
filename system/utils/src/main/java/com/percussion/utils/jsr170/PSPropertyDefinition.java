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
package com.percussion.utils.jsr170;

import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.lang.StringUtils;

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
