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
