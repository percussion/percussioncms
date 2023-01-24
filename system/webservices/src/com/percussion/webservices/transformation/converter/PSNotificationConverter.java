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

import com.percussion.services.workflow.data.PSNotification;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.webservices.system.PSNotificationStateRoleRecipientType;

import java.util.List;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;

/**
 * Convert between {@link com.percussion.services.workflow.data.PSNotification}
 * and {@link com.percussion.webservices.system.PSNotification}
 */
public class PSNotificationConverter extends PSConverter
{
   /**
    * See {@link PSConverter#PSConverter(BeanUtilsBean) super()}
    * 
    * @param beanUtils
    */
   public PSNotificationConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
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
      
      PSNotification src = (PSNotification) value;
      
      com.percussion.webservices.system.PSNotification tgt = 
         new com.percussion.webservices.system.PSNotification();
      
      tgt.setId(src.getGUID().longValue());
      tgt.setStateRoleRecipientType(
         PSNotificationStateRoleRecipientType.fromString(
            PSStringUtils.toCamelCase(src.getStateRoleRecipientType().name())));

      List<String> srcList;
      srcList = src.getRecipients();
      tgt.setRecipients(srcList.toArray(new String[srcList.size()]));
      srcList = src.getCCRecipients();
      tgt.setCCRecipients(srcList.toArray(new String[srcList.size()]));
      return tgt;
   }
}

