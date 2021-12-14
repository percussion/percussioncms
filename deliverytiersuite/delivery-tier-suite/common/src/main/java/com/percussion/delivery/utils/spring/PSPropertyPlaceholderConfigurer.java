/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.delivery.utils.spring;


import com.percussion.delivery.utils.security.PSSecureProperty;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.util.StringValueResolver;

import javax.annotation.Nonnull;

/**
 * @author erikserating
 *
 */
public class PSPropertyPlaceholderConfigurer
      extends
        PropertySourcesPlaceholderConfigurer
{

   protected String key = null;
   
   /* (non-Javadoc)
    * @see org.springframework.beans.factory.config.PropertyResourceConfigurer#convertPropertyValue(java.lang.String)
    */
   @Override
   @Nonnull
   protected String convertPropertyValue(@Nonnull String originalValue)
   {
      if(PSSecureProperty.isValueClouded(originalValue))
         return PSSecureProperty.getValue(originalValue, key);
      return originalValue;
   }

   //Workaround for bug spring-framework/issues/13568
   protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
                                      final StringValueResolver valueResolver) {

      super.doProcessProperties(beanFactoryToProcess,
              new StringValueResolver() {
                 @Override
                 public String resolveStringValue(String strVal) {
                    return convertPropertyValue(valueResolver.resolveStringValue(strVal));
                 }
              }
      );
   }

   /**
    * @return the key
    */
   public String getKey()
   {
      return key;
   }

   /**
    * @param key the key to set
    */
   public void setKey(String key)
   {
      this.key = key;
   }   
   
   
}
