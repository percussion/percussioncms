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

