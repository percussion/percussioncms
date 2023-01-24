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

import com.percussion.i18n.PSLocale;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts objects between the classes 
 * <code>com.percussion.i18n.PSLocale</code> and 
 * <code>com.percussion.webservices.content.PSLocale</code>.
 */
public class PSLocaleConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSLocaleConverter(BeanUtilsBean beanUtils)
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
         if (value instanceof com.percussion.webservices.content.PSLocale)
         {
            com.percussion.webservices.content.PSLocale source = 
               (com.percussion.webservices.content.PSLocale) value;
   
            PSLocale target = new PSLocale(source.getCode(), source.getLabel(), 
               source.getDescription(), source.isEnabled() ? 
                  PSLocale.STATUS_ACTIVE : PSLocale.STATUS_INACTIVE);
            PSGuid guid = new PSGuid(PSTypeEnum.LOCALE, source.getId());
            target.setLocaleId((int) guid.longValue());
            
            return target;
         }
         else
         {
            com.percussion.webservices.security.data.PSLocale source = 
               (com.percussion.webservices.security.data.PSLocale) value;
   
            PSLocale target = new PSLocale(source.getCode(), source.getLabel(), 
               source.getDescription(), source.isEnabled() ? 
                  PSLocale.STATUS_ACTIVE : PSLocale.STATUS_INACTIVE);
            PSGuid guid = new PSGuid(PSTypeEnum.LOCALE, source.getId());
            target.setLocaleId((int) guid.longValue());
            
            return target;
         }
      }
      else
      {
         PSLocale source = (PSLocale) value;
         PSDesignGuid guid = new PSDesignGuid(PSTypeEnum.LOCALE, 
            source.getLocaleId());
         
         if (type.getName().equals(
            com.percussion.webservices.content.PSLocale.class.getName()))
         {
            com.percussion.webservices.content.PSLocale target = 
               new com.percussion.webservices.content.PSLocale(
                  guid.getValue(), 
                  source.getDescription(),
                  source.getLanguageString(), 
                  source.getDisplayName(), 
                  source.getStatus() == PSLocale.STATUS_ACTIVE);
            
            return target;
         }
         else
         {
            com.percussion.webservices.security.data.PSLocale target = 
               new com.percussion.webservices.security.data.PSLocale(
                  guid.getValue(), 
                  source.getDescription(),
                  source.getLanguageString(), 
                  source.getDisplayName(), 
                  source.getStatus() == PSLocale.STATUS_ACTIVE);
            
            return target;
         }
      }
   }
}
