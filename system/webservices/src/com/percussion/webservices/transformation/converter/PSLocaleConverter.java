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
