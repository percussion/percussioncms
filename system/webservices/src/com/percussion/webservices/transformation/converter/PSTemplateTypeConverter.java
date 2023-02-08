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

import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.webservices.assembly.data.TemplateType;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang.StringUtils;

/**
 * Converts objects between the classes 
 * <code>IPSAssemblyTemplate.TemplateType</code> and 
 * <code>TemplateType</code>.
 */
public class PSTemplateTypeConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSTemplateTypeConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }
   
   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @Override
   public Object convert(Class type, Object value)
   {
      if (value == null)
         return null;
      
      if (isClientToServer(value))
         return IPSAssemblyTemplate.TemplateType.valueOf(
            StringUtils.capitalize(value.toString()));
      else
         return TemplateType.fromString(
            value.toString().toLowerCase());
   }
}

