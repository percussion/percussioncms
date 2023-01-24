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
package com.percussion.services.security.data;

import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.IPSAcl;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A helper class to get the security related objects.
 */
public class PSSecurityUtils
{
   /**
    * Private ctor
    */
   private PSSecurityUtils()
   {

   }

   /**
    * Gets the list of visible community names for the supplied object Acl. If
    * the supplied acl is null then returns the supplied community list as is.
    * If there are no Acl entries then returns an empty list. If any community
    * acl entry is present and it has RUNTIME_VISIBLE permission then returns
    * all communities minus visibility by other community acls.
    * 
    * @param objAcl Object of IPSAcl for which the community visibility needed
    * @param commNames list of all community names. Must not be
    * <code>null</code>.
    * @return list of visible community names. Never <code>null</code>, may
    * be empty.
    */
   public static List<String> getVisibleCommunities(IPSAcl objAcl,
      List<String> commNames)
   {
      if (commNames == null)
      {
         throw new IllegalArgumentException("commNames must not be null");
      }

      if (objAcl == null)
         return commNames;

      List<String> retComm = new ArrayList<>();
      Collection<IPSAclEntry> allAclEntries = ((PSAclImpl) objAcl)
         .getEntries();

      if (allAclEntries.isEmpty())
         return retComm;

      IPSAclEntry anyCommAcl = null;
      List<IPSAclEntry> otherCommAcls = new ArrayList<>();

      for (IPSAclEntry aclEntry : allAclEntries)
      {
         if (!aclEntry.getTypedPrincipal().isCommunity())
            continue;
         else if (aclEntry.getTypedPrincipal().isSystemCommunity())
            anyCommAcl = aclEntry;
         else
            otherCommAcls.add(aclEntry);
      }
      // Add all communities if the any community acl exists and it has
      // RUNTIME_VISIBLE permission
      if (anyCommAcl != null
         && anyCommAcl.checkPermission(PSPermissions.RUNTIME_VISIBLE))
         retComm.addAll(commNames);

      // Filter the list based on other community acl permissions
      for (IPSAclEntry aclEntry : otherCommAcls)
      {
         String comm = ((PSAclEntryImpl)aclEntry).getName();
         if (aclEntry.checkPermission(PSPermissions.RUNTIME_VISIBLE))
         {
            // We need to add this community name to return list if we have not already
            // added part of any community permissions.
            if (!retComm.contains(comm) && commNames.contains(comm))
               retComm.add(comm);
         }
         else
         {
            retComm.remove(comm);
         }
      }
      return retComm;
   }

}
