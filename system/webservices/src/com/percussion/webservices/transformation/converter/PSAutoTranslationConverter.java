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

