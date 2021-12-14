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
package com.percussion.services.guidmgr;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;


/**
 * A helper class to aid in constructing guids
 * 
 * @author dougrand
 */
public class PSGuidHelper
{   
   /**
    * Generate just the UUID part of a guid. Useful for initializing 
    * child table ids
    * @param e the enumeration to use
    * @return just the UUID portion
    */
   public static long generateNextLong(PSTypeEnum e)
   {
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid next = mgr.createGuid(e);
      return next.getUUID();
   }
   
   /**
    * Generate just the UUID part of a guid. Useful for initializing 
    * child table ids
    * @param e the enumeration to use
    * @return the new GUID, never <code>null</code>
    */
   public static IPSGuid generateNext(PSTypeEnum e)
   {
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid next = mgr.createGuid(e);
      return next;
   }   
}
