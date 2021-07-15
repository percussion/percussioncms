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

import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSRole;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.security.data.PSRoleAttributesAttribute;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts objects between the classes 
 * <code>com.percussion.design.objectstore.PSRole</code> and 
 * <code>com.percussion.webservices.security.data.PSRole</code>.
 */
public class PSRoleConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSRoleConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @SuppressWarnings("unchecked")
   @Override
   public Object convert(Class type, Object value)
   {
      if (value == null)
         return null;
      
      if (isClientToServer(value))
      {
         com.percussion.webservices.security.data.PSRole source = 
            (com.percussion.webservices.security.data.PSRole) value;

         PSRole target = new PSRole(source.getName());
         
         Long id = source.getId();
         if (id != null)
            target.setId((new PSGuid(id)).getUUID());
         
         PSRoleAttributesAttribute[] sourceAttrs = source.getAttributes();
         if (sourceAttrs != null)
         {
            PSAttributeList targetAttrs = new PSAttributeList();
            for (int i=0; i<sourceAttrs.length; i++)
            {
               PSRoleAttributesAttribute sourceAttr = sourceAttrs[i];
               
               PSAttribute targetAttr = new PSAttribute(sourceAttr.getName());
               targetAttr.setValues(Arrays.asList(sourceAttr.getValue()));
               targetAttrs.add(targetAttr);
            }
         }
         
         return target;
      }
      else
      {
         PSRole source = (PSRole) value;

         com.percussion.webservices.security.data.PSRole target = 
            new com.percussion.webservices.security.data.PSRole();

         IPSGuid guid = source.getGUID();
         if (guid != null)
            target.setId(new PSDesignGuid(guid).getValue());
         
         target.setName(source.getName());
         
         PSAttributeList sourceAttrs = source.getAttributes();
         PSRoleAttributesAttribute[] targetAttrs = 
            new PSRoleAttributesAttribute[sourceAttrs.size()];
         for (int i=0; i<sourceAttrs.size(); i++)
         {
            PSAttribute sourceAttr = (PSAttribute) sourceAttrs.get(i);
            
            PSRoleAttributesAttribute targetAttr = 
               new PSRoleAttributesAttribute();
            targetAttr.setName(sourceAttr.getName());
            List<String> values = sourceAttr.getValues();
            String[] valuesArray = values.toArray(new String[values.size()]);
            targetAttr.setValue(valuesArray);
            
            targetAttrs[i] = targetAttr;
         }
         
         return target;
      }
   }
}

