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

import com.percussion.services.workflow.data.PSNotificationDef;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;

/**
 * Converts between 
 * {@link com.percussion.services.workflow.data.PSNotificationDef}
 * and {@link com.percussion.webservices.system.PSNotificationDef}
 */
public class PSNotificationDefConverter extends PSConverter
{
   /**
    * See {@link PSConverter#PSConverter(BeanUtilsBean) super()}
    * 
    * @param beanUtils
    */
   public PSNotificationDefConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
      
      m_specialProperties.add("guid");
      m_specialProperties.add("workflowid");
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
      
      PSNotificationDef src = (PSNotificationDef) value; 
      com.percussion.webservices.system.PSNotificationDef tgt = 
         (com.percussion.webservices.system.PSNotificationDef) 
         super.convert(type, value);
      
      tgt.setId(src.getGUID().longValue());
      
      return tgt;
   }
}

