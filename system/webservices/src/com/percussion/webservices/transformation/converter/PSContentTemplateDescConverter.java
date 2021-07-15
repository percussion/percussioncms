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

