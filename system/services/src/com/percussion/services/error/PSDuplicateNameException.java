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
package com.percussion.services.error;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;

import org.apache.commons.lang.StringUtils;

/**
 * PSDuplicateNameException is thrown when the name of an object is not unique.
 * This usually occurs when attempting to save an object, but the name 
 * of the object has already been used by an existing object, which is the 
 * same type of the to be saved object.
 *
 * @author Yu-Bing Chen
 */
public class PSDuplicateNameException extends PSRuntimeException
{
   public PSDuplicateNameException(IPSGuid id, String name)
   {
      if (id == null)
         throw new IllegalArgumentException("id not may not null.");
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name not may not null or empty.");
      
      Object[] args = { id.longValue(),
            PSTypeEnum.valueOf(id.getType()).getDisplayName() };
      setMsgKeyAndArgs("service.exception@DuplicateName", args);
   }
}
