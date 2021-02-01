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

package com.percussion.security;

import java.security.Principal;
import java.util.Collection;

/**
 * Provides support for cataloging groups for a {@link PSSecurityProvider}.
 * Also responsible for determining if a particular subject is a member of one
 * of the groups supported by this group provider.
 */

public interface IPSGroupProvider
{
   /**
    * Determines if the specified user is a member of the specified group.
    * Nested groups are supported, although diffierent providers may limit the
    * number of levels that group may be nested.
    *
    * @param user The name of the user, may not be <code>null
    * </code> or empty.
    * @param group The name of the group, may not be <code>null</code> or empty.
    * Must be supported by this provider.  {@link #isGroupSupported(String)
    * isGroupSupported} may be used to check first.
    *
    * @return <code>true</code> if the user is a member of the group,
    * <code>false</code> if not.
    *
    * @throws IllegalArgumentException if user or group is inavlid.
    * @throws PSSecurityException if any errors occur.
    */
   public boolean isMember(String user,String group) throws PSSecurityException;

   /**
    * Get the collection of groups specified by this provider.
    *
    * @param filter a name to match on.  May contain valid wildcards, one of
    * the <code>IPSSecurityProviderMetaData.FILTER_MATCH_xxx</code> values. If
    * <code>null</code>, all groups are returned.  May not be empty.
    *
    * @return A collection of group names.  Never <code>null</code>, may be
    * empty.  Format is provider specific.
    *
    * @throws IllegalArgumentException if filter is invalid.
    * @throws PSSecurityException if any errors occur.
    */
   public Collection<String> getGroups(String filter) throws PSSecurityException;

   /**
    * Get the collection of groups which contain the specified user name.
    * 
    * @param userName The name of the user, never <code>null</code> or empty.
    * 
    * @return The collection of groups, never <code>null</code>, may be empty.
    */
   public Collection<String> getUserGroups(String userName);
   
   /**
    * Determines if this group provider supports calling {@link #isMember(
    * String, String) isMember} with the specified group.
    *
    * @param group The name of the group, may not be <code>null</code> or empty.
    *
    * @return <code>true</code> if it is supported, <code>false</code> if not.
    *
    * @throws IllegalArgumentException if group is invalid.
    */
   public boolean isGroupSupported(String group);

   /**
    * @return The type of security provider that may use this group provider.
    * One of the PSSecurityProvider.SP_TYPE_xxx types.
    */
   public int getType();

   /**
    * Returns the members of the supplied groups. Any group that can be resolved
    * should be removed from the supplied list of groups. Any groups that cannot
    * be resolved should be left in the supplied list.
    * <p>
    * The provider should handle resolving nested groups to some number of
    * levels and only names representing users should be returned, but this is
    * not required. All names returned will be treated as users for such
    * purposes as notification and community filtering.
    * 
    * @param groups A list of principals representing group names. Any of the
    * groups resolved by this call should be removed from the list before the
    * method returns.
    * 
    * @return A list of principals specifying the names of group
    * members, may be empty if there are no members or if no groups can be
    * resolved by this provider. If the provider cannot support this operation,
    * it should return an emtpy list and not modify the supplied list of groups.
    */
   public Collection<IPSTypedPrincipal> getGroupMembers(
      Collection<? extends Principal> groups);
}
