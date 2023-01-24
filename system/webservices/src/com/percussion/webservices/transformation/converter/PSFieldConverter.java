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

