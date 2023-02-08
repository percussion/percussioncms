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

