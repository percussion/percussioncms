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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.services.security;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.security.data.PSUserAccessLevel;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.security.IPSTypedPrincipal;

import java.util.Collection;
import java.util.List;

/**
 * This interface consists of methods to help managing object ACLs.
 * <code>ACL</code> objects need the <code>Principal</code> who is accessing
 * or modifying it which is part of some user context information. This class
 * takes the user context information in the ctor and helps simplifying
 * operations involving the object ACL, user context information and may be some
 * other business rules.
 * <p>
 * All the <code>loadAclXXX</code> methods return read-only objects. These are
 * cached and may be shared among threads; the caller should never modify them.
 * If changes need to be made, either the {@link #loadAclsModifiable(List)} or
 * {@link #loadAclsForObjectsModifiable(List)} method should be used.
 * 
 * @author Ram
 * @version 6.0
 */
public interface IPSAclService
{
   /**
    * A convenience method that retrieves the acl for the object with the
    * supplied id and calls {@link #calculateUserAccessLevel(IPSAcl)}.
    * 
    * @param objectGuid The guid of the object for which the current user's
    * effective access level needs to be computed. Must not be <code>null</code>
    */
   PSUserAccessLevel getUserAccessLevel(IPSGuid objectGuid);
   
   /**
    * Computes the current user's effective access level to the object protected
    * by the supplied ACL. The effective access level of a user is the highest
    * permission he or she can get on the associated object based on all entries
    * in the ACL.
    * 
    * @param acl The acl which will be used to compute the access. If
    * <code>null</code>, all access is allowed. Must be an ACL previously 
    * returned by this interface.
    * 
    * @return the effective access level for the current user via the supplied
    * ACL, never <code>null</code>.
    */
   PSUserAccessLevel calculateUserAccessLevel(IPSAcl acl);
   
   /**
    * Create an acl for the specified object.
    * 
    * @param objGuid The guid of the object for which the acl will specify
    * permissions, may not be <code>null</code>.
    * @param owner The owner of the acl, may not be <code>null</code>.
    * @return The acl, never <code>null</code>.  This object will not have been
    * persisted.
    */
   IPSAcl createAcl(IPSGuid objGuid, IPSTypedPrincipal owner);

   /**
    * Load ACLs for given list of ACL GUIDs. These objects are cached and shared
    * between threads and should be treated read-only. See the class description
    * for more details.
    * 
    * @param aclGuids list of ACL <code>IPSGuid</code> objects to load the
    * ACLs for. May be <code>null</code> to return all ACLs. If not
    * <code>null</code>, then must not be empty.
    * 
    * @return list of <code>IPSAcl</code> objects, may be <code>null</code>
    * never empty.
    * 
    * @throws PSSecurityException If any of the specified acls cannot be loaded.
    */
   List<IPSAcl> loadAcls(List<IPSGuid> aclGuids)
      throws PSSecurityException;

   /**
    * Just like {@link #loadAcls(List)}, except the object is always retrieved
    * from the persistent storage, never from cache. See that method for 
    * parameter and return description.
    */
   List<IPSAcl> loadAclsModifiable(List<IPSGuid> aclGuids)
      throws PSSecurityException;
   
   /**
    * Load the ACL for the specified guid. These objects are cached and shared
    * between threads and should be treated read-only. See the class description
    * for more details.
    * 
    * @param aclGuid The guid of the acl to load, may not be <code>null</code>.
    * 
    * @return The acl, never <code>null</code>.
    * 
    * @throws PSSecurityException If the load fails.
    */
   IPSAcl loadAcl(IPSGuid aclGuid) throws PSSecurityException;
   
   /**
    * Load ACLs for given list of Object guids. These objects are cached and
    * shared between threads and should be treated read-only. See the class
    * description for more details.
    * 
    * @param objectGuids list of object <code>IPSGuid</code>s to load the
    *        ACLs for. Must not be <code>null</code> and no entry should be
    *        <code>null</code>.
    * 
    * @return One ACL for each corresponding object id. Some of the entries may
    *         be <code>null</code> if the object does not have an ACL. The
    *         results are in the same order as the supplied ids.
    */
   List<IPSAcl> loadAclsForObjects(List<IPSGuid> objectGuids);

   /**
    * Find all design object GUIDs visible in any of the supplied communities.
    * 
    * @param communityNames a list of community names for which to lookup all
    *        allowed objects. If <code>null</code> or empty, returns an empty
    *        list. Names are case-insensitive.
    * @return A set of GUIDs that are visible in at least one of the communities
    *         in the supplied list and has the supplied type, never
    *         <code>null</code>, may be empty.
    */
   Collection<IPSGuid> findObjectsVisibleToCommunities(
           List<String> communityNames, PSTypeEnum objectType);

   /**
    * Just like {@link #loadAclsForObjects(List)}, except the object is always
    * retrieved from the persistent storage, never from cache. See that method
    * for parameter and return description.
    */
   List<IPSAcl> loadAclsForObjectsModifiable(List<IPSGuid> objectGuids);

   /**
    * Load the ACL for the specified Object guids. These objects are cached and
    * shared between threads and should be treated read-only. See the class
    * description for more details.
    * 
    * @param objectGuid the object <code>IPSGuid</code> to load the ACL for.
    *        Must not be <code>null</code>.
    * 
    * @return The specified acl, may be <code>null</code>.
    */
   IPSAcl loadAclForObject(IPSGuid objectGuid);

   /**
    * Just like {@link #loadAclForObject(IPSGuid)}, except the object is always
    * retrieved from the persistent storage, never from cache. See that method
    * for parameter and return description.
    */
   IPSAcl loadAclForObjectModifiable(IPSGuid objectGuid);

   /**
    * Save the supplied ACLs to the system. The objects to be saved may be
    * created outside the service. No ids need to be allocated for the acl
    * entries as these will be allocated by the service. If modifying an
    * existing ACL, then it must have been loaded using either
    * {@link #loadAclsModifiable(List)} or
    * {@link #loadAclsForObjectsModifiable(List)}.
    * 
    * @param aclList List of ACLs to save. Must not be <code>null</code>.
    * Each entry must not have been loaded with any of the
    * <code>loadAclXXX</code> methods.
    * 
    * @throws PSSecurityException If the save fails.
    */
   List<IPSAcl>  saveAcls(List<IPSAcl> aclList) throws PSSecurityException;
   
    /**
    * Delete the specified acl from the system.
    * 
    * @param aclGuid The acl guid, may not be <code>null</code>.  If the 
    * specified acl does not exist the method simply returns without error.
    * 
    * @throws PSSecurityException If any delete fails.
    */
    void deleteAcl(IPSGuid aclGuid) throws PSSecurityException;
   
    /**
    * Filter list of objectIds by list of communities items are visible in
    * 
    * @param aclList List of ACLs to save. Must not be <code>null</code>.
    * 
    * @return The filtered list of object guids, may be <code>null</code>.
    */
    Collection<IPSGuid> filterByCommunities(List<IPSGuid> aclList, List<String> communityNames);

   /***
    * Clears the in memory acl cache
    */
   void clearCache();
}
