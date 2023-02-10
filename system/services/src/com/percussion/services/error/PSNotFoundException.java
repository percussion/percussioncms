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

import com.percussion.error.PSException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;

/**
 * PSNotFoundException is thrown when cannot find a specified object.
 * This usually occurs when attempting to load an object by ID, but such 
 * object does not exist.
 *
 * @author Yu-Bing Chen
 */
public class PSNotFoundException extends PSException
{
   /**
    * Create an instance for cannot find object by ID.
    * @param id the ID of the none existence object, never <code>null</code>.
    */
   public PSNotFoundException(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id not may not null.");
            
      Object[] args = { id.longValue(),
            PSTypeEnum.valueOf(id.getType()).getDisplayName() };
   }

   public PSNotFoundException(int id)
   {
      Object[] args = { id};
   }
   
   /**
    * Create an instance for cannot find object by name
    * @param name the lookup name, may be <code>null</code> or empty.
    * @param type the type of the object, never <code>null</code>.
    */
   public PSNotFoundException(String name, PSTypeEnum type)
   {
      if (type == null)
         throw new IllegalArgumentException("type may not be null.");
      
      Object[] args = { name, type.getDisplayName() };
   }
   
   /**
    * Constructs an exception with the specified detail message.
    * @param errorMsg the specified detail message.
    */
   public PSNotFoundException(String errorMsg)
   {
      super(errorMsg);
   }

}
