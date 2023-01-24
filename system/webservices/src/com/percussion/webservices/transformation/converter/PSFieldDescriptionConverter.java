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

import com.percussion.services.content.data.PSFieldDescription;
import com.percussion.services.content.data.PSFieldDescription.PSFieldTypeEnum;
import com.percussion.webservices.content.PSFieldDescriptionDataType;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.Converter;

/**
 * Converts between
 * <code>com.percussion.services.content.data.PSFieldDescription</code> and
 * <code>com.percussion.webservices.content.PSFieldDescription</code>
 */
public class PSFieldDescriptionConverter extends PSConverter
{

   /**
    * @param beanUtils
    */
   public PSFieldDescriptionConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);

      m_specialProperties.add("type");
      m_specialProperties.add("dataType");
   }

   @Override
   public Object convert(Class type, Object value)
   {
      Object result = super.convert(type, value);
      
      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      if (isClientToServer(value))
      {
         com.percussion.webservices.content.PSFieldDescription orig = 
            (com.percussion.webservices.content.PSFieldDescription) value;
         PSFieldDescription dest = (PSFieldDescription) result;
         PSFieldDescriptionDataType srcDT = orig.getDataType();

         Converter converter = factory.getConverter(
            PSFieldDescriptionDataType.class);
         
         PSFieldDescription.PSFieldTypeEnum tgtDT = 
            (PSFieldTypeEnum) converter.convert(
               PSFieldDescription.PSFieldTypeEnum.class, srcDT);  
         dest.setType(tgtDT.name());
      }
      else
      {
         PSFieldDescription orig = (PSFieldDescription) value;
         com.percussion.webservices.content.PSFieldDescription dest = 
            (com.percussion.webservices.content.PSFieldDescription) result;
         PSFieldDescription.PSFieldTypeEnum srcDT = 
            PSFieldDescription.PSFieldTypeEnum.valueOf(orig.getType());
         
         Converter converter = factory.getConverter(
            PSFieldDescription.PSFieldTypeEnum.class);
         
         PSFieldDescriptionDataType tgtDT = 
            (PSFieldDescriptionDataType) converter.convert(
               PSFieldDescriptionDataType.class, srcDT);
         dest.setDataType(tgtDT);
      }
      
      return result;
   }

}

