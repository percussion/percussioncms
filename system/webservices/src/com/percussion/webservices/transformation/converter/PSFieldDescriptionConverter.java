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

