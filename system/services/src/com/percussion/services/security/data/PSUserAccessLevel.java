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

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSUserAccessLevel{");
      sb.append("m_permissions=").append(m_permissions);
      sb.append('}');
      return sb.toString();
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
