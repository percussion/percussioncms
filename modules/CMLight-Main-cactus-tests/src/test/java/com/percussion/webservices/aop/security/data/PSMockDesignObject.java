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
package com.percussion.webservices.aop.security.data;

import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.utils.guid.IPSGuid;

/**
 * Mock data object for testing
 */
public class PSMockDesignObject
{
   /**
    * Get the guid
    * 
    * @return The guid, may be <code>null</code>.
    */
   public IPSGuid getGuid()
   {
      return m_guid;
   }
   
   /**
    * Set the guid.
    * 
    * @param guid The guid, may not be <code>null</code>.
    */
   public void setGUID(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("guid may not be null");
      
      m_guid = guid;
   }
   


   /**
    * Create a mock object with the guid specified by the supplied acl.
    * 
    * @param acl The acl to use, may not be <code>null</code>.
    * 
    * @return The object, never <code>null</code>.
    */
   public static PSMockDesignObject createMockObject(IPSAcl acl)
   {
      if (acl == null)
         throw new IllegalArgumentException("acl may not be null");
      
      PSMockDesignObject obj = new PSMockDesignObject();
      IPSGuid guid = ((PSAclImpl)acl).getObjectGuid();
      obj.setGUID(guid);
      return obj;
   }
   
   /**
    * This objects guid, may be <code>null</code> if not set.
    */
   IPSGuid m_guid;
}

