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
package com.percussion.rx.design.impl;

import com.percussion.extension.PSExtensionRef;
import com.percussion.server.PSServer;
import com.percussion.utils.guid.IPSGuid;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

public class PSExtensionModel extends PSLimitedDesignModel
{
   @Override
   public Object load(IPSGuid guid)
   {
      throw new UnsupportedOperationException("load(IPSGuid) is not currently "
            + "implemented for design objects of type " + getType().name());
   }
   
   @Override
   public Object load(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      PSExtensionRef extRef = new PSExtensionRef(name);
      try
      {
         return PSServer.getExtensionManager(null).getExtensionDef(extRef);
      }
      catch (Exception e) 
      {
         String msg = "Failed to get the design object for name {0}";
         Object[] args = { name };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }
      
   }
   @Override
   public void delete(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      PSExtensionRef extRef = new PSExtensionRef(name);
      try
      {
         PSServer.getExtensionManager(null).removeExtension(extRef);
      }
      catch (Exception e) 
      {
         String msg = "Failed to delete the extension for name {0}";
         Object[] args = { name };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }
      
   }
}
