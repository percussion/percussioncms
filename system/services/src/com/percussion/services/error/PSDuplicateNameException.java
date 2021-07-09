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
