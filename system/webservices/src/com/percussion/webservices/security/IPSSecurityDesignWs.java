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
package com.percussion.webservices.security;

import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.security.data.PSCommunityVisibility;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.security.PSSecurityCatalogException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.aop.security.IPSWsParameter;

import java.rmi.RemoteException;
import java.util.List;

/**
 * This interface defines all security design related webservices.
 */
public interface IPSSecurityDesignWs
{
   /**
    * Tests if there is a valid Rhythmyx user for the supplied name.
    * 
    * @param user the user name to test, not <code>null</code> or empty.
    * @return <code>true</code> if we found a valid Rhythmy user for the 
    *    supplied name, <code>false</code> otherwise.
    * @throws PSSecurityCatalogException for any error validation the supplied
    *    user.
    */
   public boolean isValidRhythmyxUser(String user) 
      throws PSSecurityCatalogException;
   
   /**
    * Creates new community definitions for the supplied parameters. The new 
    * communities are not persisted until you call 
    * {@link #saveCommunities(List, boolean, String, String)} for the returned 
    * objects.
    *  
    * @param names the names of the new communities, not <code>null</code> or
    *    empty. Must be unique across all defined communities in the system.
    * @param session the rhythmyx session for which to lock the created
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to lock the created objects, not 
    *    <code>null</code> or empty. 
    * @return the new communities for the requested names and defaults for all
    *    other properties, never <code>null</code> or empty.
    */
   public List<PSCommunity> createCommunities(List<String> names, 
      String session, String user);
   
   /**
    * Finds all community summaries for the supplied name. 
    * 
    * @param name the name of the community to find, may be <code>null</code> 
    *    or empty in which case all summaries will be returned, wildcards are 
    *    accepted.
    * @return all found community summaries for the supplied name, never 
    *    <code>null</code>, may be empty, ascending alpha ordered by name.
    */
   public List<IPSCatalogSummary> findCommunities(String name);

   /**
    * Load all communities for the supplied ids. It is an error if no community
    * exists for any of the supplied ids.
    * 
    * @param ids the ids for which to load the communities, not 
    *    <code>null</code> or empty.
    * @param lock <code>true</code> to lock the loaded communities for edit,
    *    <code>false</code> to load them read-only.
    * @param overrideLock <code>true</code> to allow the requesting user to
    *    override existing locks he owns in a different session, 
    *    <code>false</code> otherwise.
    * @param session the rhythmyx session for which to lock the returned
    *    objects, not <code>null</code> or empty if lock is <code>true</code>. 
    * @param user the user for which to lock the returned objects, not 
    *    <code>null</code> or empty if lock is <code>true</code>. 
    * @return all requested communities in the requested mode, never 
    *    <code>null</code> or empty.
    * @throws PSErrorResultsException if any of the requested objects could not
    *    be loaded or locked.
    */
   public List<PSCommunity> loadCommunities(List<IPSGuid> ids, 
      @IPSWsParameter(isLockParameter=true) boolean lock, boolean overrideLock, 
      String session, String user) 
      throws PSErrorResultsException;
   
   /**
    * Save all supplied communities. New communities will be inserted, 
    * existing communities will be updated. If the community names are 
    * changed during this save, we also rename all ACL entries for all
    * visible objects of the renamed communities.
    * 
    * @param communities the communities to save, not <code>null</code> or
    *    empty.
    * @param release <code>true</code> to release all object locks after the 
    *    save, <code>false</code> to keep the locks. All locks will be
    *    released, no matter whether the operation was successful or not.
    * @param session the rhythmyx session for which to release the saved
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to release the saved objects, not 
    *    <code>null</code> or empty. 
    * @throws PSErrorsException if any of the supplied objects could not
    *    be saved or if we failed to rename ACl entries.
    */
   public void saveCommunities(List<PSCommunity> communities, 
      boolean release, String session, String user) throws PSErrorsException;
   
   /**
    * Delete all communities specified through the supplied ids. We ignore
    * ids for which we can't find a community. Only objects that are unlocked 
    * or locked by the requesting user and session can be deleted, for all 
    * other cases an error will be returned.
    * <p>
    * All locks for successfully deleted objects will be released, locks which
    * exist for objects that failed to be deleted remain untouched.
    * 
    * @param ids the ids of all communities to be deleted, not 
    *    <code>null</code> or empty.
    * @param ignoreDependencies <code>true</code> to ignore the dependency
    *    check before the delete, <code>false</code> otherwise.
    * @param session the rhythmyx session for which to delete the specified
    *    objects, not <code>null</code> or empty. 
    * @param user the user for which to delete the specified objects, 
    *    not <code>null</code> or empty. 
    * @throws PSErrorsException if any of the supplied objects could not
    *    be deleted.
    */
   public void deleteCommunities(List<IPSGuid> ids, boolean ignoreDependencies, 
      String session, String user) throws PSErrorsException;
   
   /**
    * Finds all role summaries for the supplied name. 
    * 
    * @param name the name of the role to find, may be <code>null</code> 
    *    or empty in which case all summaries will be returned, wildcards are 
    *    accepted.
    * @return all found role summaries for the supplied name, never 
    *    <code>null</code>, may be empty, ascending alpha ordered by name.
    */
   public List<IPSCatalogSummary> findRoles(String name);
   
   /**
    * Get the summaries for all design objects which are visible for the 
    * supplied communities.
    * 
    * @param ids a list with all community ids for which to get the design 
    *    object visibility, not <code>null</code> or empty, must be ids of 
    *    existing communities.
    * @param type the object type for which to get the community design 
    *    object visibility, may be <code>null</code>. The design object 
    *    visibility for all supported object types will be returned if not 
    *    supplied.
    * @param session the rhythmyx session to use not <code>null</code> or empty. 
    * @param user the user to use, not <code>null</code> or empty. 
    * @return a list of design object visibilities, one for each requested 
    *    community, never <code>null</code> or empty.
    * @throws PSErrorResultsException for any error looking up the community 
    *    design object visibilities.
    * @throws RemoteException for any unexpected error.
    */
   public List<PSCommunityVisibility> getVisibilityByCommunity(
      List<IPSGuid> ids, PSTypeEnum type, String session, String user) 
      throws PSErrorResultsException, RemoteException;
}

