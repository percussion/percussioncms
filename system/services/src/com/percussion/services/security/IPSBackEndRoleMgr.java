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

import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.security.data.PSBackEndRole;
import com.percussion.services.security.data.PSCommunityRoleAssociation;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.security.IPSPrincipalAttribute;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;

/**
 * Provides back-end specific catalog operations not supported by the standard
 * cataloging interfaces.
 */
public interface IPSBackEndRoleMgr
{
   /**
    * Get the list of all roles defined in the Rhythmyx back end.
    * 
    * @return A list of role names, never <code>null</code>, may be empty.
    */
   public List<String> getRhythmyxRoles();

   /**
    * Returns a list with all roles to which the supplied subject belongs.
    * 
    * @param subjectName the name of the subject to search for,
    * case-insensitive. If <code>null</code>, all roles are returned
    * regardless of whether they contain any subjects, ignoring the other
    * parameter values when searching. If empty, this property is ignored, but
    * roles are still searched using the other parameter values.
    * @param subjectType one of the PSSubject.SUBJECT_TYPE_xxx types, or 0 to
    * ignore this property.
    * 
    * @return a valid list of 0 or more <code>Strings</code>, each naming a
    * role. The list will not contain duplicates.
    */
   public List<String> getRhythmyxRoles(String subjectName, int subjectType); 
   
   /**
    * Set the specified subject to be a member of the specified roles.
    * If roles is empty, then remove the subject from all roles.
    * The subject will be removed from any roles that do not appear
    * in the specified roles.
    * The specified subject will be created if not exist.
    *
    * @param subjectName the name of the subject, not blank.
    * @param subjectType one of the PSSubject.SUBJECT_TYPE_xxx types.
    * @param roles a complete list of role names that the subject
    * will be set as a member of. It may be empty, but not <code>null</code>.
    * The subject will be removed if it is an empty list.
    */
   void setRhythmyxRoles(String subjectName, int subjectType, Collection<String> roles);

   /**
    * Set the specified subjects to be members of the specified roles.
    * If roles is empty, then remove the subjects from all roles.
    * The subjects will be removed from any roles that do not appear
    * in the specified roles.
    * The specified subjects will be created if not exists.
    *
    * @param subjectNames the list of names for subjects
    * @param subjectType one of the PSSubject.SUBJECT_TYPE_xxx types.
    * @param roles a complete list of role names that the subjects
    * will be set as members of. It may be empty, but not <code>null</code>.
    * The subjects will be removed if it is an empty list.
    */
   void setRhythmyxRoles(Collection<String> subjectNames, int subjectType, Collection<String> roles);
   
   /**
    * Get the role specific attributes of the specified role.
    * 
    * @param roleName The name of the role, may not be <code>null</code> or 
    * empty.
    * 
    * @return A set of attributes, never <code>null</code>, may be empty if
    * no attributes are defined for the specified role.
    */
   public Set<IPSPrincipalAttribute> getRoleAttributes(String roleName);

   /**
    * Gets the subjects with the attributes they have assigned for the specified
    * role.
    * 
    * @param roleName The name of the role, may not be <code>null</code> or
    * empty.
    * @param subjectNameFilter a filter to limit the subject lookup. If not
    * provided (<code>null</code> or empty), all members of the role are
    * returned. The filter uses SQL LIKE syntax ('%' matches 0 or more chars,
    * '_' matches any single char).
    * 
    * @return A set of subjects, each containing the resulting attributes as
    * {@link IPSPrincipalAttribute} objects.  Never <code>null</code>, may be
    * empty.
    */
   public Set<Subject> getRoleSubjectAttributes(String roleName,
      String subjectNameFilter);

   /**
    * Gets the matching backend subjects with the specified attributes.
    * 
    * @param subjectNameFilter The individual whose attributes you wish.
    *    Wildcards allowed following SQL LIKE syntax. If <code>null</code> or
    *    empty, all subjects are included.
    * @param attributeNameFilter  A single pattern used to select the desired
    *    attributes. Use SQL LIKE syntax. Supply empty or <code>null</code> to
    *    get all attributes.
    * @param includeEmptySubjects A flag to indicate whether subjects with
    *    no attributes should be included in the returned list. If <code>
    *    true</code>, they are included, otherwise, only subjects that
    *    have 1 or more attributes are included.
    *    
    * @return The set of subjects, never <code>null</code>, may be empty.
    */
   public Set<Subject> getGlobalSubjectAttributes(String subjectNameFilter, 
      String attributeNameFilter, boolean includeEmptySubjects);
   
   /**
    * Get the names of the roles that are assigned to the specified community.
    * 
    * @param communityId The id of the community.
    * 
    * @return A list of role names, never <code>null</code>, may be empty.
    */
   public List<String> getCommunityRoles(int communityId);

   /**
    * Load all communities for the supplied ids.
    * 
    * @param ids the ids for which to load the communities, not
    * <code>null</code> or empty and no entry can be <code>null</code>.
    * 
    * @return all requested communities in read-only mode, never
    * <code>null</code>. The returned array may be smaller than the
    * supplied array if any of the ids do not reference existing communities.
    * The returned communities are in the same order as the supplied ids.
    */
   public PSCommunity[] loadCommunities(IPSGuid[] ids);
   
   /**
    * Load all roles for the supplied ids. It is an error if no role
    * exists for any of the supplied ids.
    * 
    * @param ids the ids for which to load the roles, not 
    *    <code>null</code> or empty.
    * @return all requested roles, never <code>null</code> or empty.
    */
   public PSBackEndRole[] loadRoles(IPSGuid[] ids);
   
   /**
    * Find all backend roles for the supplied name.
    * 
    * @param name the name of the role to find, may be <code>null</code> 
    *    or empty. Finds all backend roles if <code>null</code> or empty, sql 
    *    type (%) wildcards are supported.
    * @return all found roles for the supplied name, never 
    *    <code>null</code>, may be empty.
    */
   public List<PSBackEndRole> findRolesByName(String name);


   /**
    * Find all backend roles for the supplied name.
    *
    * @param id the id of the role to find
    * @return the Back End Role, or <code>null</code> if the role
    *       does not exists.
    */
   public PSBackEndRole findRoleById(long id);
   
   /**
    * Create a new role for the supplied name.
    * 
    * @param name the name for the new role, not <code>null</code> or
    *    empty.  May be mixed case, but normalized version (all lowercase) 
    *    of the name is used for checking uniqueness. 
    * @return the new created Back End Role, or <code>null</code> if the role
    *   already exists. 
    */
   public PSBackEndRole createRole(String name);
   
   /**
    * Create a new role for the supplied name and description.
    * 
    * @param name the name for the new role, not <code>null</code> or
    *    empty.  May be mixed case, but normalized version (all lowercase) 
    *    of the name is used for checking uniqueness. 
    * @param description the description for the new role.
    * @return the new created Back End Role, or <code>null</code> if the role
    *   already exists. 
    */
   public PSBackEndRole createRole(String name, String description);
   
   /**
    * Deletes the specified role if found.
    * 
    * @param name the name for the new role, not <code>null</code> or
    *    empty.  May be mixed case, but normalized version (all lowercase) 
    *    of the name is used for checking uniqueness. 
    */
   public void deleteRole(String name);
   
   /**
    * Create a new community for the supplied name.
    * 
    * @param name the name for the new community, not <code>null</code> or
    *    empty, must be unique across all existing communities.
    * @param description a description for the new community, may be 
    *    <code>null</code> or empty.
    * @return the new created community, never <code>null</code>. The returned
    *    community is not saved to the repository but contains the correct
    *    pre-allocated id.
    */
   public PSCommunity createCommunity(String name, String description);
   
   /**
    * Find all communities for the supplied name.
    * 
    * @param name the name of the community to find, may be <code>null</code> 
    *    or empty. Finds all communities if <code>null</code> or empty, sql 
    *    type (%) wildcards are supported.
    * @return all found communities for the supplied name, never 
    *    <code>null</code>, may be empty.
    */
   public List<PSCommunity> findCommunitiesByName(String name);
   
   /**
    * Load the community for the supplied id.
    * 
    * @param id the id of the community to find, not <code>null</code>. Must
    *    be the id of an existing community.
    * @return the loaded community, never <code>null</code>.
    * @throws PSSecurityException if no community exists for the supplied id.
    */
   public PSCommunity loadCommunity(IPSGuid id) throws PSSecurityException;
   
   /**
    * Save the supplied community.
    * 
    * @param community the community to save, not <code>null</code>.
    */
   public void saveCommunity(PSCommunity community);
   
   /**
    * Delete the community for the supplied id.
    * 
    * @param id the id of the community to save, not <code>null</code>. If no
    *    community exists for the supplied id will be ignored.
    */
   public void deleteCommunity(IPSGuid id);
   
   /**
    * Find the community associations for the specified role ids
    * 
    * @param roleIds The list of role ids, never <code>null</code> or empty.
    * 
    * @return The list of associations, never <code>null</code>, may be
    * empty, is not guaranteed to contain associations for all of the supplied
    * role IDs, and my return multiple associations for a given role ID.
    */
   public List<PSCommunityRoleAssociation> findCommunitiesByRole(
      List<IPSGuid> roleIds);
   
   /**
    * Sets the provided email on the provided subject 
    * @param subjectName subjectName never <code>null</code> or empty.
    * @param subjectEmail subjectEmail never <code>null</code> might be empty.
    */
   public void setSubjectEmail(String subjectName, String subjectEmail);
   
   /**
    * Updates the description on the role for the provided roleName
    * @param roleName roleName never <code>null</code> or empty.
    * @param description description never <code>null</code> might be empty.
    * @return the updated role never <code>null</code>  
    */
   public PSBackEndRole update(String roleName, String description);
}
