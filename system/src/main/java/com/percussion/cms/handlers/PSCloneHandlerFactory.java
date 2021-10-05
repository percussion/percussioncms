/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
