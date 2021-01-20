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
package com.percussion.services.security;

import com.percussion.utils.security.IPSTypedPrincipal;
import com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes;

import java.security.acl.AclEntry;
import java.security.acl.Permission;

/**
 * This interface extends {@link AclEntry} to suport the type of the ACL entry.
 * 
 * @see java.security.acl.AclEntry
 * @version 6.0
 */
public interface IPSAclEntry extends AclEntry
{
   /**
    * Adds permissions from specified array to the entry. Safe to call even if a
    * specified permission already set previously. Specifying any empty
    * permission array does not touch remnove existing permissions.
    * 
    * @param permissions permission array, must not be <code>null</code>.
    */
   public void addPermissions(Permission[] permissions);
   
   /**
    * Remove all permission for this entry.
    */
   public void clearPermissions();

   /**
    * @deprecated
    * @see com.percussion.utils.security.IPSTypedPrincipal#isType(PrincipalTypes)
    */
   public boolean isType(PrincipalTypes entryType);

   /**
    * @deprecated
    * @see com.percussion.utils.security.IPSTypedPrincipal#isUser()
    */
   public boolean isUser();

   /**
    * @deprecated
    * @see com.percussion.utils.security.IPSTypedPrincipal#isGroup()
    */
   public boolean isGroup();

   /**
    * @deprecated
    * @see com.percussion.utils.security.IPSTypedPrincipal#isCommunity()
    */
   public boolean isCommunity();

   /**
    * @deprecated
    * @see com.percussion.utils.security.IPSTypedPrincipal#isRole()
    */
   public boolean isRole();

   /**
    * @deprecated
    * @see com.percussion.utils.security.IPSTypedPrincipal#isSystemEntry()
    */
   public boolean isSystemEntry();

   /**
    * @deprecated
    * @see com.percussion.utils.security.IPSTypedPrincipal#isSystemCommunity()
    */
   public boolean isSystemCommunity();

   /**
    * Check if this entry has the owner permission to the ACL. An owner of
    * the ACL is the entry that can modify the ACL.
    * 
    * @return <code>true</code> if this entry is an owner, <code>false</code>
    * otherwise.
    */
   boolean isOwner();

   /**
    * @deprecated
    * @see com.percussion.utils.security.IPSTypedPrincipal#getPrincipalType()
    */
   public PrincipalTypes getType();

   /**
    * @see java.security.Principal#getName()
    */
   public String getName();
   
   /**
    * Return the typed principal.
    * @return never <code>null</code>.
    */
   public IPSTypedPrincipal getTypedPrincipal();
   
}
