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
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts between
 * <code>com.percussion.services.content.data.PSContentTypeSummaryChild</code> and
 * <code>com.percussion.webservices.content.PSContentTypeSummaryChild</code>
 */
public class PSContentTypeSummaryConverter extends PSConverter
{

   /**
    * @param beanUtils
    */
   public PSContentTypeSummaryConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   @SuppressWarnings("unchecked")
   @Override
   public Object convert(@SuppressWarnings("unused") Class type, Object value)
   {
      Object result = super.convert(type, value);

      if (isClientToServer(value))
      {
         com.percussion.webservices.content.PSContentTypeSummary orig =
            (com.percussion.webservices.content.PSContentTypeSummary) value;
         PSGuid guid = new PSGuid(PSTypeEnum.NODEDEF, orig.getId());

         PSContentTypeSummary dest = (PSContentTypeSummary) result;
         dest.setGuid(guid);         
         return dest;
      }
      else
      {
         PSContentTypeSummary orig = (PSContentTypeSummary) value;
         com.percussion.webservices.content.PSContentTypeSummary dest =
            (com.percussion.webservices.content.PSContentTypeSummary) result;

         PSDesignGuid guid = new PSDesignGuid(orig.getGuid());
         dest.setId(guid.getValue());
         
         return dest;
      }
   }

}

