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

import com.percussion.search.objectstore.PSWSSearchParams;
import com.percussion.search.objectstore.PSWSSearchRequest;
import com.percussion.webservices.content.PSSearchParams;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.Converter;

/**
 * Converts objects between the classes 
 * <code>com.percussion.search.objectstore.PSWSSearchRequest</code> and 
 * <code>com.percussion.webservices.content.PSSearch</code>.
 */
public class PSSearchConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSSearchConverter(BeanUtilsBean beanUtils)
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
      
      if (isClientToServer(value))
      {
         com.percussion.webservices.content.PSSearch source = 
            (com.percussion.webservices.content.PSSearch) value;
         
         Converter converter = getConverter(PSSearchParams.class);
         PSWSSearchParams params = (PSWSSearchParams) converter.convert(
            PSWSSearchParams.class, source.getPSSearchParams());
         
         PSWSSearchRequest target = new PSWSSearchRequest(params);
         target.setCaseInsensitiveSearch(source.isUseDbCaseSensitivity());
         target.setUseExternalSearchEngine(source.isUseExternalSearchEngine());
         
         return target;
      }
      else
      {
         PSWSSearchRequest source = (PSWSSearchRequest) value;
         
         Converter converter = getConverter(PSWSSearchParams.class);
         PSSearchParams params = (PSSearchParams) converter.convert(
            PSSearchParams.class, source.getSearchParams());

         com.percussion.webservices.content.PSSearch target = 
            new com.percussion.webservices.content.PSSearch();
         target.setPSSearchParams(params);
         target.setUseDbCaseSensitivity(source.isCaseInsensitiveSearch());
         target.setUseExternalSearchEngine(source.useExternalSearchEngine());

         return target;
      }
   }
}
