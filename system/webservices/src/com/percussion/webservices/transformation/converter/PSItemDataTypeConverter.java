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
