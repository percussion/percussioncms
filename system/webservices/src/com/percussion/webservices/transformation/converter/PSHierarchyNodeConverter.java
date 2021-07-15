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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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

