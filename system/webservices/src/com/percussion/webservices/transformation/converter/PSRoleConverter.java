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

