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

import com.percussion.services.security.PSPermissions;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Provides the sum of a user's permissions for a given resource.
 */
public class PSUserAccessLevel
{
   /**
    * Default ctor.
    * 
    * @param permissions The collection all permissions the user has for a given
    * resource, may be <code>null</code> or empty if the user has no 
    * permissions.
    */
   public PSUserAccessLevel(Collection<PSPermissions> permissions)
   {
      m_permissions = new HashSet<>();
      if (permissions != null)
         m_permissions.addAll(permissions);
   }

   /**
    * @return <code>true</code> if this access level has read pemission, false
    * otherwise.
    */
   public boolean hasReadAccess()
   {
      return m_permissions.contains(PSPermissions.READ);
   }

   /**
    * @return <code>true</code> if this access level has update pemission,
    * false otherwise.
    */
   public boolean hasUpdateAccess()
   {
      return m_permissions.contains(PSPermissions.UPDATE);
   }

   /**
    * @return <code>true</code> if this access level has delete pemission,
    * false otherwise.
    */
   public boolean hasDeleteAccess()
   {
      return m_permissions.contains(PSPermissions.DELETE);
   }

   /**
    * @return <code>true</code> if this access level has runtime permission,
    * false otherwise.
    */
   public boolean hasRuntimeAccess()
   {
      return m_permissions.contains(PSPermissions.RUNTIME_VISIBLE);
   }
   

   /**
    * @return <code>true</code> if this access level has owner permission,
    * false otherwise.
    */
   public boolean hasOwnerAccess()
   {
      return m_permissions.contains(PSPermissions.OWNER);
   }
   
   /**
    * Get the set of permissions represented by this object.
    * 
    * @return The set, never <code>null</code>.  Modifications to this set will
    * affect this object.
    */
   public Set<PSPermissions> getPermissions()
   {
      return m_permissions;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }
   
   
   /**
    * Set of all permissions, never <code>null</code> after construction, may
    * be empty. 
    */
   private Set<PSPermissions> m_permissions;

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSUserAccessLevel)) return false;
      PSUserAccessLevel that = (PSUserAccessLevel) o;
      if(m_permissions.size()!= that.m_permissions.size()) return false;

      return m_permissions.containsAll(that.m_permissions);
   }

   @Override
   public int hashCode() {
      return m_permissions.hashCode();
   }

}
