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

import com.percussion.search.objectstore.PSWSSearchField;
import com.percussion.webservices.common.ConnectorTypes;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts objects between the classes
 * {@link com.percussion.search.objectstore.PSWSSearchField.PSConnectorEnum} and 
 * {@link com.percussion.webservices.common.ConnectorTypes}.
 */
public class PSConnectorTypesConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSConnectorTypesConverter(BeanUtilsBean beanUtils)
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
         return PSWSSearchField.PSConnectorEnum.valueOf(
            value.toString().toUpperCase());
      else
         return ConnectorTypes.fromString(
            value.toString().toLowerCase());
   }
}
