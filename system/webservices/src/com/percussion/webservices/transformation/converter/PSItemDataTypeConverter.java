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

import com.percussion.cms.objectstore.PSItemFieldMeta;
import com.percussion.webservices.content.PSFieldDataType;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;

/**
 * Converts objects between the classes
 * {@link Integer} and 
 * {@link com.percussion.webservices.content.PSFieldDataType}.
 */
public class PSItemDataTypeConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSItemDataTypeConverter(BeanUtilsBean beanUtils)
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
      {
         PSFieldDataType orig = (PSFieldDataType) value;
         //FB:EC_UNRELATED_TYPES NC 1-17-16
         Integer dest = 0;
         if (orig.getValue().equals(PSFieldDataType.binary.getValue()))
            dest = PSItemFieldMeta.DATATYPE_BINARY;
         else if (orig.getValue().equals(PSFieldDataType.date.getValue()))
            dest = PSItemFieldMeta.DATATYPE_DATE;
         else if (orig.getValue().equals(PSFieldDataType.number.getValue()))
            dest = PSItemFieldMeta.DATATYPE_NUMERIC;
         else if (orig.getValue().equals(PSFieldDataType.text.getValue()))
            dest = PSItemFieldMeta.DATATYPE_TEXT;
         else
            throw new ConversionException("unknown item field data type");
         
         return dest;
      }
      else
      {
         Integer orig = (Integer) value;
         
         switch (orig)
         {
            case PSItemFieldMeta.DATATYPE_BINARY:
               return PSFieldDataType.binary;
            case PSItemFieldMeta.DATATYPE_DATE:
               return PSFieldDataType.date;
            case PSItemFieldMeta.DATATYPE_NUMERIC:
               return PSFieldDataType.number;
            case PSItemFieldMeta.DATATYPE_TEXT:
               return PSFieldDataType.text;
            default:
               throw new ConversionException("unknown item field data type");
         }
      }
   }
}
