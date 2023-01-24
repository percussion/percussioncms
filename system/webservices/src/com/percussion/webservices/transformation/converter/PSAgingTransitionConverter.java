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

import com.percussion.services.workflow.data.PSAgingTransition;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.webservices.system.PSAgingTransitionType;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;

/**
 * Convert from {@link com.percussion.services.workflow.data.PSAgingTransition} to
 * {@link com.percussion.webservices.system.PSAgingTransition}
 */
public class PSAgingTransitionConverter extends PSTransitionBaseConverter
{

   /**
    * See {@link PSConverter#PSConverter(BeanUtilsBean)}
    * 
    * @param beanUtils
    */
   public PSAgingTransitionConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
      
      m_specialProperties.add("type");
   }

   @Override
   public Object convert(Class type, Object value)
   {
      if (value == null)
         return null;
      
      if (isClientToServer(value))
      {
         // only reading from server is supported
         throw new ConversionException(
            "Conversion not supported from client to server");
      }
      
      PSAgingTransition src = (PSAgingTransition) value; 
      com.percussion.webservices.system.PSAgingTransition tgt = 
         (com.percussion.webservices.system.PSAgingTransition) 
         super.convert( type, value);
      tgt.setType(PSAgingTransitionType.fromString(PSStringUtils.toCamelCase(
         src.getType().name())));
      
      return tgt;
   }
}

