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
import com.percussion.webservices.content.PSFieldTransferEncoding;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;

/**
 * Converts objects between the classes
 * {@link Integer} and 
 * {@link com.percussion.webservices.content.PSFieldSourceType}.
 */
public class PSFieldTransferEncodingConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSFieldTransferEncodingConverter(BeanUtilsBean beanUtils)
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
         PSFieldTransferEncoding orig = (PSFieldTransferEncoding) value;
         
         Integer dest = 0;
         //FB: EC_UNRELATED_TYPES NC 1-17-16
         if (orig.getValue().equals(PSFieldTransferEncoding.base64.getValue()))
            dest = PSItemFieldMeta.ENCODING_TYPE_BASE64;
         else if (orig.getValue().equals(PSFieldTransferEncoding.none.getValue()))
            dest = PSItemFieldMeta.ENCODING_TYPE_NONE;
         else
            throw new ConversionException(
               "unknown item field transfer encoding");
         
         return dest;
      }
      else
      {
         Integer orig = (Integer) value;
         
         switch (orig)
         {
            case PSItemFieldMeta.ENCODING_TYPE_BASE64:
               return PSFieldTransferEncoding.base64;
            case PSItemFieldMeta.ENCODING_TYPE_NONE:
               return PSFieldTransferEncoding.none;
            default:
               throw new ConversionException(
                  "unknown item field transfer encoding");
         }
      }
   }
}
