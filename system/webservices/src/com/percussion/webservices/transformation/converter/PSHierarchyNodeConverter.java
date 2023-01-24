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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.webservices.ui.data.PSHierarchyNodePropertiesProperty;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts objects between the classes 
 * <code>com.percussion.services.ui.data.PSHierarchyNode</code> and 
 * <code>com.percussion.webservices.ui.data.PSHierarchyNode</code>.
 */
public class PSHierarchyNodeConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSHierarchyNodeConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);

      m_specialProperties.add("id");
      m_specialProperties.add("parentId");
      m_specialProperties.add("properties");
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @Override
   public Object convert(Class type, Object value)
   {
      Object result = super.convert(type, value);
      
      if (isClientToServer(value))
      {
         com.percussion.webservices.ui.data.PSHierarchyNode orig = 
            (com.percussion.webservices.ui.data.PSHierarchyNode) value;
         
         PSHierarchyNode dest = (PSHierarchyNode) result;

         dest.setGUID(new PSDesignGuid(orig.getId()));
         if (orig.getParentId() != 0)
            dest.setParentId(new PSDesignGuid(orig.getParentId()));
         
         // convert properties
         for (PSHierarchyNodePropertiesProperty property : orig.getProperties())
            dest.addProperty(property.getName(), property.getValue());
      }
      else
      {
         PSHierarchyNode orig = (PSHierarchyNode) value;
         
         com.percussion.webservices.ui.data.PSHierarchyNode dest = 
            (com.percussion.webservices.ui.data.PSHierarchyNode) result;
         
         dest.setId(new PSDesignGuid(orig.getGUID()).getValue());
         if (orig.getParentId() != null)
            dest.setParentId(new PSDesignGuid(orig.getParentId()).getValue());

         // convert properties
         PSHierarchyNodePropertiesProperty[] properties = 
            new PSHierarchyNodePropertiesProperty[orig.getProperties().size()];
         dest.setProperties(properties);
         int index = 0;
         for (String propertyName : orig.getProperties().keySet())
         {
            String propertyValue = orig.getProperty(propertyName);
            properties[index++] =  new PSHierarchyNodePropertiesProperty(
               orig.getGUID().longValue(), propertyName, propertyValue);
         }
      }
      
      return result;
   }
}

