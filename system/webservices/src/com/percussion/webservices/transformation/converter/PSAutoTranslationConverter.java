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
import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts between 
 * {@link com.percussion.services.content.data.PSAutoTranslation} and
 * {@link com.percussion.webservices.content.PSAutoTranslation}.
 */
public class PSAutoTranslationConverter extends PSConverter
{
   /**
    * See {@link PSConverter#PSConverter(BeanUtilsBean) super()} for details.
    * 
    * @param beanUtils
    */
   public PSAutoTranslationConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
      m_specialProperties.add("communityId");
      m_specialProperties.add("contentTypeId");
      m_specialProperties.add("workflowId");
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
         com.percussion.webservices.content.PSAutoTranslation orig = 
            (com.percussion.webservices.content.PSAutoTranslation) value;
         
         PSAutoTranslation dest = (PSAutoTranslation) result;
         
         // convert community id
         long id = orig.getCommunityId();
         if (id != 0)
            dest.setCommunityId(new PSDesignGuid(id).getUUID());
         
         // convert content type id
         id = orig.getContentTypeId();
         if (id != 0)
            dest.setContentTypeId(new PSDesignGuid(id).getUUID());
         
         // convert workflow id
         id = orig.getWorkflowId();
         if (id != 0)
            dest.setWorkflowId(new PSDesignGuid(id).getUUID());
      }
      else
      {
         PSAutoTranslation orig = (PSAutoTranslation) value;
         
         com.percussion.webservices.content.PSAutoTranslation dest = 
            (com.percussion.webservices.content.PSAutoTranslation) result;
         
         // convert community id
         IPSGuid guid = new PSGuid(PSTypeEnum.COMMUNITY_DEF, 
            orig.getCommunityId());
         dest.setCommunityId(new PSDesignGuid(guid).getValue());
         
         // convert content type id
         guid = new PSGuid(PSTypeEnum.NODEDEF, orig.getContentTypeId());
         dest.setContentTypeId(new PSDesignGuid(guid).getValue());
         
         // convert workflow id
         guid = new PSGuid(PSTypeEnum.WORKFLOW, orig.getWorkflowId());
         dest.setWorkflowId(new PSDesignGuid(guid).getValue());
      }
      
      return result;
   }
}

