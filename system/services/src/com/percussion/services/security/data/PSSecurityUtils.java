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
