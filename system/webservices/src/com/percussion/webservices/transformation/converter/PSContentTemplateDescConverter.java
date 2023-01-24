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

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.data.PSContentTemplateDesc;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Convert between {@link PSContentTemplateDesc} and
 * {@link com.percussion.webservices.content.data.PSContentTemplateDesc}.
 */
public class PSContentTemplateDescConverter extends PSConverter
{
   /**
    * See {@link PSConverter#PSConverter(BeanUtilsBean)}
    * 
    * @param beanUtils
    */
   public PSContentTemplateDescConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
      
      m_specialProperties.add("contentTypeId");
      m_specialProperties.add("templateId");
      m_specialProperties.add("guid");
   }

   @Override
   public Object convert(Class type, Object value)
   {
      Object result = super.convert(type, value);
      if (isClientToServer(value))
      {
         com.percussion.webservices.content.PSContentTemplateDesc orig = 
            (com.percussion.webservices.content.PSContentTemplateDesc) 
               value;
         PSContentTemplateDesc dest = (PSContentTemplateDesc) result;
         
         PSGuid ctGuid = new PSGuid(PSTypeEnum.NODEDEF, 
            orig.getContentTypeId());
         PSGuid templateGuid = new PSGuid(PSTypeEnum.TEMPLATE, 
            orig.getTemplateId());
         
         dest.setContentTypeId(ctGuid);
         dest.setTemplateId(templateGuid);
      }
      else
      {
         PSContentTemplateDesc orig = (PSContentTemplateDesc) value;
         com.percussion.webservices.content.PSContentTemplateDesc dest = 
            (com.percussion.webservices.content.PSContentTemplateDesc) 
               result;
         
         PSDesignGuid ctGuid = new PSDesignGuid(orig.getContentTypeId());
         PSDesignGuid templateGuid = new PSDesignGuid(orig.getTemplateId());
         
         dest.setContentTypeId(ctGuid.getValue());
         dest.setTemplateId(templateGuid.getValue());
      }
      
      return result;
   }   
}

