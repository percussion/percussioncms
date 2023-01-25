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
package com.percussion.services.security;

import com.percussion.security.IPSTypedPrincipal;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;

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
    * @see IPSTypedPrincipal#isType(PrincipalTypes)
    */
   public boolean isType(PrincipalTypes entryType);

   /**
    * @deprecated
    * @see IPSTypedPrincipal#isUser()
    */
   public boolean isUser();

   /**
    * @deprecated
    * @see IPSTypedPrincipal#isGroup()
    */
   public boolean isGroup();

   /**
    * @deprecated
    * @see IPSTypedPrincipal#isCommunity()
    */
   public boolean isCommunity();

   /**
    * @deprecated
    * @see IPSTypedPrincipal#isRole()
    */
   public boolean isRole();

   /**
    * @deprecated
    * @see IPSTypedPrincipal#isSystemEntry()
    */
   public boolean isSystemEntry();

   /**
    * @deprecated
    * @see IPSTypedPrincipal#isSystemCommunity()
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
    * @see IPSTypedPrincipal#getPrincipalType()
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
