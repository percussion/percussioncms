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
package com.percussion.cms.handlers;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.server.config.PSServerConfigException;

/**
 * Based on the objects this factory provides access to the correct clone
 * handler.
 */
public class PSCloneHandlerFactory
{
   /**
    * Creates the correct clone handler for the supplied object type.
    * 
    * @param type the object type to get a clone handler for, might be 
    *    <code>null</code>. If <code>null</code> this defaults to "item".
    * @param copyHandler the copy handler to be used to create copies for the
    *    provided object type, not <code>null</code>.
    * @return the clone handler for the supplied object type, never 
    *    <code>null</code>.
    * @throws PSServerConfigException if a requested clone handler 
    *    configuration failed to load.
    * @throws PSUnknownNodeTypeException if a configuration loaded has an 
    *    invalid XML structure.
    * @throws IllegalArgumentException if the supplied type is unkown or the 
    *    copy handler is <code>null</code>.
    */
   public static IPSCloneHandler getCloneHandler(String type, 
      IPSCopyHandler copyHandler) 
      throws PSServerConfigException, PSUnknownNodeTypeException
   {
      if (copyHandler == null)
        throw new IllegalArgumentException("copy handler cannot be null");
        
      IPSCloneHandler handler = null;
      
      // create the correct clone handler
      switch (getObjectTypeId(type))
      {
         case ITEM_ID:
            handler = new PSConditionalCloneHandler(copyHandler);
            break;
      }

      // if we don't have one yet, we don't know the supplied type
      if (handler == null)
         throw new IllegalArgumentException("Unknown object type");

      return handler;
   }
   
   /**
    * Resolves the object id for the supplied object type string.
    * 
    * @param type the object type to get the id for, defaults to "item" if
    *    <code>null</code> is provided.
    * @return the object id, UNDEFINED_ID if not known for the supplied type.
    */
   public static int getObjectTypeId(String type)
   {
      // default to item if not provided
      if (type == null)
         type = ITEM;
         
      if (type.equals(ITEM))
         return ITEM_ID;
      else
         return UNDEFINED_ID;
   }
   
   /**
    * The type String used for objects of type "item".
    */
   public static final String ITEM = "item";
   
   /**
    * The id used for objects of type "item".
    */
   private static final int ITEM_ID = 1;
   
   /**
    * The id used for unknown object types.
    */
   private static final int UNDEFINED_ID = -1;
}
