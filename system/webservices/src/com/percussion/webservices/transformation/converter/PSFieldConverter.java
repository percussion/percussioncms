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

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.webservices.content.PSField;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.lang.StringUtils;

/**
 * Converts objects between the classes
 * {@link com.percussion.cms.objectstore.PSItemField} and
 * {@link com.percussion.webservices.content.PSField}
 */
public class PSFieldConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSFieldConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @SuppressWarnings("unchecked")
   @Override
   public Object convert(Class type, Object value)
   {
      if (value == null)
         return null;
      
      try
      {
         if (isClientToServer(value))
         {
            PSField orig = (PSField) value;
            
            String contentType = orig.getContentType();
            if (StringUtils.isBlank(contentType))
               throw new ConversionException(
                  "You must set the contentType for each field to convert.");
            
            PSItemField dest = PSItemConverterUtils.createItemField(contentType, 
               orig.getName());
            PSItemConverterUtils.toServerFieldValues(dest, orig);

            return dest;
         }
         else
         {
            PSItemField orig = (PSItemField) value;

            return PSItemConverterUtils.toClientField(orig, this);
         }
      }
      catch (PSCmsException e)
      {
         throw new ConversionException(e.getLocalizedMessage());
      }
   }
}

