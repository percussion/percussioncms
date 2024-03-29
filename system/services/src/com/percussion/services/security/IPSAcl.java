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
import com.percussion.utils.guid.IPSGuid;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;

import java.security.acl.Acl;

/**
 * Extends th interface {@link Acl} to support the id fo rth eACL object.
 * 
 * @version 6.0
 */
public interface IPSAcl extends Acl
{
   /**
    * Convenience method that is equivalent to calling
    * <code>{@link #getGUID()}.getUUID()</code>.
    * 
    * @return The UUID of the ACL object.
    */
   public long getId();
   
   /**
    * Get the guid of the ACL
    * 
    * @return aclId normally not <code>null</code> unless the object is not
    * initialized properly.
    */
   public IPSGuid getGUID();

   /**
    * Convenience method that is equivalent to calling
    * <code>{@link #getObjectGuid()}.getUUID()</code>.
    * 
    * @return The UUID of the secured object.
    */
   public long getObjectId();
   
   /**
    * Get the guid of the secured object this ACL is associated with.
    * 
    * @return objectid normally not <code>null</code> unless the object is not
    * initialized properly.
    */
   public IPSGuid getObjectGuid();

   /**
    * Set the object by id to associate this ACL with.
    * 
    * @param objectId Id of the design object this ACL to associate with, must
    * not be <code>null</code>
    */
   public void setObjectId(long objectId);

   /**
    * Get the first owner principal found in the entry set. The search is in no
    * particular order.
    * 
    * @return first entry that has owner permission on the ACL.
    * @throws SecurityException if ACL does not have t least one entry with
    * owner permission, which will not happen in a properly initialized ACL.
    */
   public IPSTypedPrincipal getFirstOwner() throws SecurityException;

   /**
    * Create a new ACl entry without adding it to the ACL. The created entry
    * must be added to the ACL using
    * {@link Acl#addEntry(java.security.Principal, java.security.acl.AclEntry)}
    * for it to be part of the ACL. This will not check for the name and type
    * conflict with existing entries. No permissions are set for the entry.
    * 
    * @param name name of the entry or principal, must not be <code>null</code>
    * or empty.
    * @param type principal type, one of the enumerations
    * {@link IPSTypedPrincipal.PrincipalTypes},
    * must ot be <code>null</code>.
    * @return new ACL entry, never <code>null</code>.
    */
   public IPSAclEntry createEntry(String name, PrincipalTypes type);

   /**
    * Same as {@link #createEntry(String, 
    * IPSTypedPrincipal.PrincipalTypes)} and
    * additionally the specified permissions are set for the new entry.
    */
   public IPSAclEntry createEntry(String name, PrincipalTypes type,
      PSPermissions[] permissions);

   /**
    * Same as {@link #createEntry(String, 
    * IPSTypedPrincipal.PrincipalTypes)} except it
    * takes a {@link IPSTypedPrincipal} as input.
    * 
    * @param principal must not be <code>null</code>.
    * @return new ACL entry, never <code>null</code>.
    */
   public IPSAclEntry createEntry(IPSTypedPrincipal principal);

   /**
    * Same as {@link #createEntry(IPSTypedPrincipal)} and additionally the
    * specified permissions are set for the new entry.
    */
   public IPSAclEntry createEntry(IPSTypedPrincipal principal,
      PSPermissions[] permissions);

   /**
    * Create system user entry or system community entry.
    * 
    * @param isCommunity <code>true</code> to create an entry for
    * {@link com.percussion.services.security.PSTypedPrincipal#ANY_COMMUNITY_ENTRY}
    * or <code>false</code> to create entry for 
    * {@link com.percussion.services.security.PSTypedPrincipal#DEFAULT_USER_ENTRY}.
    * @return ACL entry as explained above, never <code>null</code>.
    */
   public IPSAclEntry createDefaultEntry(boolean isCommunity);

   /**
    * Same as {@link #createDefaultEntry(boolean)} and iddition it adds
    * specified permissions to the new entry.
    */
   public IPSAclEntry createDefaultEntry(boolean isCommunity,
      PSPermissions[] permissions);

   /**
    * Find the entry with supplied principal.
    * 
    * @param principal entry principal, must not be <code>null</code>.
    * @return match ACL entry, <code>null</code> if not found.
    */
   public IPSAclEntry findEntry(IPSTypedPrincipal principal);

   /**
    * Find an return the default entry for the principals
    * {@link com.percussion.services.security.PSTypedPrincipal#ANY_COMMUNITY_ENTRY} or
    * {@link com.percussion.services.security.PSTypedPrincipal#DEFAULT_USER_ENTRY}.
    * 
    * @param isCommunity <code>true</code> to find default community entry or
    * <code>false</code> to find default user entry.
    * @return <code>null</code> if not found one.
    */
   public IPSAclEntry findDefaultEntry(boolean isCommunity);

}
