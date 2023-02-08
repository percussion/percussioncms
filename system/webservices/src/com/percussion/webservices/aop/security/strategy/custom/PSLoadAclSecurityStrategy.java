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
package com.percussion.webservices.aop.security.strategy.custom;

import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.data.PSUserAccessLevel;
import com.percussion.webservices.aop.security.strategy.PSLoadSecurityStrategy;

import java.util.Set;

/**
 * Implements the load acl security strategy.  Allows unlocked load by user
 * with either {@link PSPermissions#OWNER} or {@link PSPermissions#READ} 
 * permissions, and locked load by user with {@link PSPermissions#OWNER}
 * permission.
 */
public class PSLoadAclSecurityStrategy extends PSLoadSecurityStrategy
{
   /**
    * Overrides base class behavior to implement the rules defined for this
    * strategy (see class header for details).
    */
   @Override
   protected boolean hasAccess(PSPermissions perm, 
      PSUserAccessLevel accessLevel)
   {
      Set<PSPermissions> perms = accessLevel.getPermissions();
      if (perm.equals(PSPermissions.UPDATE)) 
      { 
         return perms.contains(PSPermissions.OWNER);
      }
      else if (perm.equals(PSPermissions.READ)) 
      {
         return (perms.contains(PSPermissions.OWNER) || 
            perms.contains(PSPermissions.READ));
      }
      else
         return super.hasAccess(perm, accessLevel);
   }
}

