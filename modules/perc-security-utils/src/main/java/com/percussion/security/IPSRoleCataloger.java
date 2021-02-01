/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

import java.util.Set;

/**
 * A role cataloger is used by Rhythmyx to determine the membership of a role
 * for such purposes as role based email notification.  It is also used during
 * authentication to determine the authenticated user's roles if the login
 * module that authenticated the user has not supplied them.  The Rhythmyx
 * login module will query all role catalogers to determine the user's roles
 * during authentication. 
 */
public interface IPSRoleCataloger
{
   /**
    * Get the name of the cataloger instance.  This is set at runtime by the 
    * system with a call to {@link #setName(String)}.  
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getName();
   
   /**
    * Sets the name of the cataloger instance.  This is called at runtime by the
    * system.   
    * 
    * @param name The name, may not be <code>null</code> or empty.  This is the
    * name specfied when registering the cataloger with the system.
    */
   public void setName(String name);
   
   /**
    * Get the optional description of the cataloger instance.  This is set at 
    * runtime by the system with a call to {@link #setDescription(String)}.
    * 
    * @return The description, may be <code>null</code> or empty.
    */
   public String getDescription();
   
   /**
    * Sets the description of the cataloger instance.  This is called at runtime 
    * by the system.   
    * 
    * @param desc The description, may be <code>null</code> or empty.  This is 
    * the description specfied when registering the cataloger with the system.
    */
   public void setDescription(String desc);
   
   /**
    * Catalog the members of the specified role.
    * 
    * @param roleName The name of the role, may not be <code>null</code> or
    * empty.
    * 
    * @return A set of principals specifying the names of the users in the role,
    * never <code>null</code>, may be empty if no matching role is found or
    * if the role has no members. Principles returned with
    * {@link IPSTypedPrincipal.PrincipalTypes#UNDEFINED} are ignored.
    * 
    * @throws PSSecurityCatalogException if there are any errors.
    */
   public Set<IPSTypedPrincipal> getRoleMembers(String roleName) 
      throws PSSecurityCatalogException;
   
   /**
    * Get the list of roles that the specified user is a member of.
    * 
    * @param user The user or group to check, may be <code>null</code> to get
    * all role names defined.
    * 
    * @return A set of role names, never <code>null</code>, may be empty if the
    * specified user is not in any roles, if a matching user is not known to
    * the cataloger, or if no roles are defined.
    * 
    * @throws PSSecurityCatalogException if there are any errors.
    */
   public Set<String> getUserRoles(IPSTypedPrincipal user) 
      throws PSSecurityCatalogException;
}

