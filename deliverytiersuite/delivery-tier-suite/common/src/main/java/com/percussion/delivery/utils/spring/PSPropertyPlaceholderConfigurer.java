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
