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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.guidmgr.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;

/**
 * Allows construction of a guid from the guid value only, without any type
 * checking other than ensuring the value specifies a valid type.  Intended for
 * internal use with the design webservices, where the full guid value is 
 * provided only as a long.
 */
public class PSDesignGuid extends PSGuid
{
   /**
    * 
    */
   private static final long serialVersionUID = -7095060778250604874L;

   /**
    * Construct a guid from a value that specifies a valid type as well as the 
    * value.
    * 
    * @param value The guid value as a long, must specify a valid guid type as 
    *    well.
    */
   public PSDesignGuid(long value)
   {
      m_guid = value;
      validateGuid();
   }
   
   /**
    * Constructs a design guid from the supplied guid.
    * 
    * @param guid the guid to construct this design guid from, not 
    *    <code>null</code>. The supplied guid must specify a valid type.
    */
   public PSDesignGuid(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("guid cannot be null");
      
      if (!(guid instanceof PSGuid))
         throw new IllegalArgumentException("guid must be of instance PSGuid");
      
      m_guid = ((PSGuid) guid).m_guid;
      validateGuid();
   }
   
   /**
    * See base class for details.
    * 
    * @param type
    * @param value
    */
   public PSDesignGuid(PSTypeEnum type, long value)
   {
      super(type, value);
   }

   /**
    * Get the complete guid value, including all parts such as the UUID, the
    * type ID and the host ID.
    * 
    * @return the complete guid value allows users to referenence rhythmyx 
    *    objects uniquely, including the UUID, the type ID and the host ID.
    */
   public long getValue()
   {
      return m_guid;
   }
   
   /**
    * Validates the guid to make sure that it contains a type and that we 
    * know the specified type.
    */
   private void validateGuid()
   {
      if (getType() == 0)
         throw new IllegalArgumentException("Type must be specified");

      PSTypeEnum type = PSTypeEnum.valueOf(getType());
      if (type == null)
         throw new IllegalArgumentException("value must include a valid type");
   }
}

