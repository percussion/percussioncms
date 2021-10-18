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
package com.percussion.security;

import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSGlobalSubject;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.error.PSExceptionUtils;
import com.percussion.security.IPSPrincipalAttribute.PrincipalAttributes;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.server.PSUserSession;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.IPSRoleMgr;
import com.percussion.services.security.PSJaasUtils;
import com.percussion.services.security.PSPrincipalAttribute;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.util.PSStringComparator;
import com.percussion.utils.tools.PSPatternMatcher;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The PSRoleManager class keeps track of roles, their contained subjects
 * and all related attributes. This does not provide authentication services
 * directly, but determines role membership based on prior authentications.
 * <p>The class implements the Singleton pattern. A long lived class should
 * get an instance to avoid the class being garbage collected.
 * <p>This class uses a cataloging application to obtain the meta-data.
 * Although other classes could make the request directly, they should not
 * because the role mgr may implement caching at some point. If it does,
 * we want all classes to have the same view of the data.
 * <p>The cataloging app is sys_roleCataloger. Each resource must either
 * return a totally empty document (no elements), or a valid document for
 * the request type. See the Rhythmyx 4.0 functional for a description of the
 * resource names and dtds they use.
 */
@SuppressWarnings(value={"unchecked"})
public class PSRoleManager
{
   /**
    * Gets the one and only instance of this class. If one doesn't exist, it
    * is created.
    *
    * @return A valid instance.
    */
   public static PSRoleManager getInstance()
   {
      if (null == ms_instance)
         ms_instance = new PSRoleManager();
      return ms_instance;
   }

   /** 
    * Convenience method that calls {@link #getRoles(String, int)} with a
    * subject name of <code>null</code> and a subject type of 0.
    * 
    * @return a list with all roles found for all defined security providers.
    *    The list is never <code>null</code>, may be empty and does not contain
    *    duplicates.
    */
   public List getRoles()
   {
      return getRoles(null, 0);
   }

   /** 
    * Get all roles from all specified security providers for the supplied 
    * parameters.
    * 
    * @param subjectName the name of the subject for which to filter the
    *    results. Provide <code>null</code> to ignore this filter.
    * @param subjectType the type of subjects for which to filter the results,
    *    one of <code>PSSubject.SUBJECT_TYPE_xxx</code>. Provide 0 to ignore 
    *    this filter.
    * @return a list with all roles from all specified security providers where
    *    the supplied subject is a member for the provided subject type. The
    *    list is never <code>null</code>, may be empty and does not contain 
    *    duplicates.
    */
   public List getRoles(String subjectName, int subjectType)
   {
      // use a set to remove duplicates
      Set resultSet = new HashSet();
      
      // add all roles from all security providers role catalogers
      resultSet.addAll(getSecurityProviderRoles(subjectName, subjectType));  

      // make sure we return an alpha ordered list
      List resultList =  new ArrayList(resultSet);
      Collections.sort(resultList);
      
      return resultList;
   }
   


   /**
    * Find a role definition and determine whether the userName is contained
    * within it.
    *
    * @param userName The userName to compare. It
    *    is used to cache the session's collection of roles.
    *    Never <code>null</code> or empty.
    *
    * @param roleName The role name. The case of the name is ignored.
    *    Never <code>null</code> or empty.
    *
    * @return     <code>true</code>    if the user is a member of roleName
    *             <code>false</code>   if the user is not a member of roleName,
    *                                  roleName is not a valid role,
    *                                  or if either parameter is invalid.
    */
   public boolean isMemberOfRole(String userName, String roleName)
   {
      if(null == userName || userName.trim().length() ==0 ||
         null == roleName || roleName.trim().length() == 0)
         return false;

      // this comparison should be case-sensitive
      return getRoles(userName, 0).contains(roleName);
   }

   /**
    * Just like {@link #memberRoleList(PSUserSession, PSSubject)}. The 
    * subject supplied to {@link #memberRoleList(PSUserSession, PSSubject)} 
    * will use type <code>PSSubject.SUBJECT_TYPE_USER</code>.
    */
   public List memberRoleList(PSUserSession session, String subjectName)
   {
      PSSubject subject = new PSGlobalSubject(subjectName, 
         PSSubject.SUBJECT_TYPE_USER, null);
         
      return memberRoleList(session, subject);
   }

   /**
    * Get the list of roles that the specified user/group is a member of.
    *
    * @param session The session of the request currently being processed. It
    *    may be used for caching. Used to retrieve the groups of which the
    *    subject is a member.  May not be <code>null</code>.
    * @param subject The subject to search for. Never <code>null</code>.
    * @return  A list of every role that contains the specified subject.  If the
    *    subject is a user, and the user is a member of any groups through the
    *    subject's security provider, then the list will also contain any roles
    *    that those groups are members of. Entries are of type String. Never
    *    <code>null</code>. The list will be in alpha order, and will not
    *    contain duplicates.
    * @throws PSSecurityException If the meta data to process the request
    *    can't be obtained.
    */
   public List<String> memberRoleList(PSUserSession session, PSSubject subject)
   {
      if (session == null)
         throw new IllegalArgumentException("session may not be null");

      if (subject == null)
         throw new IllegalArgumentException("subject may not be null");


      List<String> roles = null;

      if (subject.getType() == PSSubject.SUBJECT_TYPE_USER && 
         session.hasAuthenticatedUserEntry(subject.getName()))
      {
         return session.getUserRoles();
      }


      // use a set to build final distinct list
      // add the user's roles from all security providers
      Set<String> resultSet = new HashSet<>(getSecurityProviderRoles(subject.getName(),
              subject.getType()));

      if (subject.getType() == PSSubject.SUBJECT_TYPE_USER)
      {
         // Add any roles for each group the user is a member of.
         resultSet.addAll(getGroupRoles(subject));
      }

      roles =  new ArrayList(resultSet);

      // make sure we return an alpha ordered list
      Collections.sort(roles);

      return roles;
   }

   /**
    * Get all roles in which the supplied subject is a member.
    * 
    * @param subjectName the subject name for which to get all roles, may be
    *    <code>null</code> or empty.
    * @param subjectType one of the <code>PSSubject.SUBJECT_TYPE_xxx</code> 
    *    types, or 0 to ignore this parameter.
    * @return a set of all roles from all defined security provider role 
    *    catalogers in which the supplied subject is a member. Never 
    *    <code>null</code>, may be empty.
    */
   private Set<String> getSecurityProviderRoles(String subjectName, int subjectType)
   {
      Set<String> resultSet = new HashSet<>();
      IPSRoleMgr roleMgr = PSRoleMgrLocator.getRoleManager();
      List<String> definedRoles = roleMgr.getDefinedRoles();

      try
      {
         if (!StringUtils.isBlank(subjectName))
         {
            Set<String> userroles  = new HashSet<>();
            if (subjectType == 0 || subjectType == PSSubject.SUBJECT_TYPE_USER)
            {
               userroles.addAll(roleMgr.getUserRoles(
                  PSTypedPrincipal.createSubject(subjectName)));
            }
            
            if (subjectType == 0 || subjectType == PSSubject.SUBJECT_TYPE_GROUP)
            {
               userroles.addAll(roleMgr.getUserRoles(
                  PSTypedPrincipal.createGroup(subjectName)));
            }

            userroles.retainAll(definedRoles);
            resultSet.addAll(userroles);
            
         }
         else if (subjectType != 0)
         {
            // get all role members and filter by type
            for (String roleName : definedRoles)
            {
               if (!roleMgr.getRoleMembers(roleName, 
                  PSJaasUtils.getPrincipalType(subjectType)).isEmpty())
               {
                  resultSet.add(roleName);
               }  
            }
         }
         else
         {
            resultSet.addAll(definedRoles);
         }
      }
      catch (PSSecurityCatalogException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }
      
      return resultSet;
   }
   
   /**
    * Get all subjects for the supplied parameters.
    * 
    * @param subjectNameFilter a filter to limit the subject lookup.
    *    If not provided (<code>null</code> or empty), all members of the role
    *    are returned. The filter uses SQL LIKE syntax ('%' matches 0 or more
    *    chars, '_' matches any single char).
    * @param subjectType one of the <code>PSSubject.SUBJECT_TYPE_xxx</code> 
    *    flags to filter the results by type, provide 0 to return all types.
    * @param roleName The name of the role for which you wish to get the
    *    member list. If <code>null</code> or empty, all subjects matching the
    *    subjectNameFilter are returned. If a role by this name doesn't exist,
    *    an empty list is returned.
    * @param attributeNameFilter an attribute name by which to filter the
    *    returned subjects, <code>null</code> to ignore this filter.
    * @param includeEmptySubjects <code>true</code> to include subjects with 
    *    an empty attribute list, <code>false</code> otherwise.
    * @param backendType the backend request type, assumed to be one of 
    *    <code>BACKEND_xxx</code>.
    * @param communityId the community id by which to filter the subjects, 
    *    may be <code>null</code> to ignore the community filter.
    * @return a list with all subjects found for the supplied parameters from
    *    all defined security providers, never <code>null</code>, may be empty.
    */
   private List getSecurityProviderSubjects(String subjectNameFilter,
      int subjectType, String roleName, String attributeNameFilter,
      boolean includeEmptySubjects, String communityId, int backendType)
   {
      List<PSSubject> results = new ArrayList<>();

      try
      {
         IPSBackEndRoleMgr beRoleMgr = 
            PSRoleMgrLocator.getBackEndRoleManager();
         IPSRoleMgr roleMgr = PSRoleMgrLocator.getRoleManager();

         // create merged subject and group maps
         Map<String, PSSubject> subjectMap = new HashMap<>();
         Map<String, PSSubject> groupMap = new HashMap<>();
         
         // create lists to hold cataloged subjects and groups
         List<Subject> subjects = new ArrayList<>();
         List<Principal> groups = new ArrayList<>();
         
         // get role subjects if specified
         if (!StringUtils.isBlank(roleName))
         {
            // create lists to hold user and group names to catalog
            List<String> userNames = new ArrayList<>();
            Set<String> groupNames = new HashSet<>();
            
            // get list of subjects from backend role/subject attrs if specified            
            if (backendType == BACKEND_SUBJECT_ROLE_ATTRIBUTES)
            {
               Set<Subject> beSubjects = beRoleMgr.getRoleSubjectAttributes(
                  roleName, subjectNameFilter);
               for (Subject beSub : beSubjects)
               {
                  PSSubject pssubject = PSJaasUtils.convertSubject(beSub);
                  if (pssubject != null)
                  {
                     if (PSJaasUtils.isGroup(beSub))
                        mergeSubject(groupMap, pssubject);
                     else
                        mergeSubject(subjectMap, pssubject);
                  }
               }
               
               // only want to catalog these users and groups
               userNames.addAll(subjectMap.keySet());
               groupNames.addAll(groupMap.keySet());
            }
            else
            {
               // get all role memebers
               Set<IPSTypedPrincipal> roleMembers = roleMgr.getRoleMembers(
                  roleName);

               if (subjectType == 0 || subjectType == 
                  PSSubject.SUBJECT_TYPE_USER)
               {
                  userNames.addAll(filterPrincipals(roleMembers, 
                     PSSubject.SUBJECT_TYPE_USER, subjectNameFilter));
               }
               
               if (subjectType == 0 || subjectType == 
                  PSSubject.SUBJECT_TYPE_GROUP)
               {
                  groupNames.addAll(filterPrincipals(roleMembers, 
                     PSSubject.SUBJECT_TYPE_GROUP, subjectNameFilter));
               }               
            }
            
            // now find any users or groups from the catalogers to get all 
            // attributes
            if (!userNames.isEmpty())
               subjects.addAll(roleMgr.findUsers(userNames));
            
            if (!groupNames.isEmpty())
            {
               List<Principal> groupList = roleMgr.findGroups(subjectNameFilter, 
                  null, null);
               
               for (Principal group : groupList)
               {
                  if (groupNames.contains(group.getName()))
                  {
                     groups.add(group);
                  }
               }
            }
         }
         else
         {
            // no role specified just find all matching users and/or groups
            if (subjectType == 0 || subjectType == PSSubject.SUBJECT_TYPE_USER)
            {
               List<String> filterList = new ArrayList<>(1);
               if (!StringUtils.isBlank(subjectNameFilter))
                  filterList.add(subjectNameFilter);
               subjects.addAll(roleMgr.findUsers(filterList));
            }

            if (subjectType == 0 || subjectType == PSSubject.SUBJECT_TYPE_GROUP)
            {
               groups.addAll(roleMgr.findGroups(subjectNameFilter, 
                  null, null));               
            }
         }
         

         // build merged subject and group lists
         mergeSubjects(subjects, subjectMap);
         
         // handle groups, get attributes from backend cataloger if required
         for (Principal group : groups)
         {
            PSSubject groupSub = null;
            if (backendType == BACKEND_GLOBAL_ATTRIBUTES)
            {
               Iterator<Subject> groupSubs = 
                  beRoleMgr.getGlobalSubjectAttributes(group.getName(), 
                     attributeNameFilter, includeEmptySubjects).iterator();
               if (groupSubs.hasNext())
               {
                  Subject sub = groupSubs.next();
                  groupSub = PSJaasUtils.convertSubject(sub);
               }
            }
            
            if (groupSub == null)
            {
               groupSub = new PSGlobalSubject(group.getName(), 
                  PSSubject.SUBJECT_TYPE_GROUP, null);
            }
            
            mergeSubject(groupMap, groupSub);
         }
         
         // filter subject attributes
         results.addAll(subjectMap.values());
         results.addAll(groupMap.values());
         results = filterByAttributes(results, attributeNameFilter, 
            includeEmptySubjects);
         
         // filter by community
         if (communityId != null)
            results = filterSubjectsByCommunity(results, communityId, false);
      }
      catch (PSSecurityCatalogException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(),e);
      }


      return results;
   }

   /**
    * Filters the supplied list of subjects by the supplied attribute filter.
    * 
    * @param subjects The list of subjects to filter, assumed not
    * <code>null</code>.
    * @param attributeNameFilter an attribute name by which to filter the
    * returned subjects, <code>null</code> to ignore this filter.  Only 
    * attributes matching this filter are included in the returned subjects.
    * @param includeEmptySubjects <code>true</code> to include subjects with
    * an empty attribute list, <code>false</code> to only include subjects
    * with non-empty attribute lists after applying the attribute name filter.
    * 
    * @return The filtered list, never <code>null</code>.
    */
   private List<PSSubject> filterByAttributes(List<PSSubject> subjects,
      String attributeNameFilter, boolean includeEmptySubjects)
   {
      if (StringUtils.isBlank(attributeNameFilter))
         return subjects;
      
      PSPatternMatcher patternMatch = PSPatternMatcher.SQLPatternMatcher(
         attributeNameFilter);
      patternMatch.setCaseSensitive(false);
      
      List<PSSubject> results = new ArrayList<>();
      for (PSSubject subject : subjects)
      {
         // pattern match on all attributes
         PSAttributeList newAttrs = new PSAttributeList();
         Iterator attrs = subject.getAttributes().iterator();
         while (attrs.hasNext())
         {
            PSAttribute attr = (PSAttribute) attrs.next();
            if (!patternMatch.doesMatchPattern(attr.getName()))
               continue;
            
            if (!attr.getValues().isEmpty())
               newAttrs.add(attr);
         }

         if (includeEmptySubjects || newAttrs.iterator().hasNext())
         {
            results.add(new PSGlobalSubject(subject.getName(), 
               subject.getType(), newAttrs));
         }
      }
      
      return results;
   }

   /**
    * Filters the supplied subjects by the specified community id.
    * 
    * @param subjects The subjects to filter, assumed not <code>null</code>.
    * @param communityId The community id to use.  If not a valid community id, 
    * then an empty list is returned.
    * @param expandGroups <code>true</code> to expand the membership of
    * any community members that are groups, <code>false</code> to treat the 
    * groups as individuals.
    * 
    * @return The filtered subjects, never <code>null</code>.
    */
   private List<PSSubject> filterSubjectsByCommunity(List<PSSubject> subjects, 
      String communityId, boolean expandGroups)
   {
      List<PSSubject> results = new ArrayList<>();
      
      Map<String, PSSubject> subMap = 
         new HashMap<>(subjects.size());
      
      Set<IPSTypedPrincipal> principals = new HashSet<>();
      for (PSSubject subject : subjects)
      {
         subMap.put(subject.getName() + ":" + subject.getType(), subject);
         principals.add(PSJaasUtils.subjectToPrincipal(subject));
      }
      
      principals = filterByCommunity(principals, communityId, expandGroups);

         
      // build return list
      for (IPSTypedPrincipal principal : principals)
      {
         String key = principal.getName() + ":" + PSJaasUtils.getSubjectType(
            principal.getPrincipalType());
         PSSubject sub = subMap.get(key);
         if (sub != null)
            results.add(sub);
      }
      
      return results;
   }

   /**
    * Filters the supplied subjects by the specified community id.
    * 
    * @param principals The principals to filter, assumed not <code>null</code>.
    * @param communityId The community id to use.  If not a valid community id, 
    * then an empty list is returned.
    * @param expandGroups <code>true</code> to expand the membership of
    * any community members that are groups, <code>false</code> to treat the 
    * groups as individuals.
    * 
    * @return The filtered subjects, never <code>null</code>.
    */
   public Set<IPSTypedPrincipal> filterByCommunity(
      Collection<IPSTypedPrincipal> principals, String communityId, 
      boolean expandGroups)
   {
      Set<IPSTypedPrincipal> results = new HashSet<>();
      
      int commId;
      try
      {
         commId = Integer.parseInt(communityId);
      }
      catch (Exception e)
      {
         return results;
      }
      
      IPSBackEndRoleMgr beRoleMgr = PSRoleMgrLocator.getBackEndRoleManager();
      List<String> roles = beRoleMgr.getCommunityRoles(commId);
      
      try
      {
         // get community members
         Set<IPSTypedPrincipal> communityMembers = 
            new HashSet<>();
         IPSRoleMgr roleMgr = PSRoleMgrLocator.getRoleManager();
         for (String role : roles)
         {
            communityMembers.addAll(roleMgr.getRoleMembers(role));
         }
         
         // expand groups
         if (expandGroups)
         {
            communityMembers = expandGroups(communityMembers);
         }
         
         // filter the principal list with the results
         results.addAll(principals);
         results.retainAll(communityMembers);
      }
      catch (PSSecurityCatalogException e)
      {
         log.error("Failed to filter by community : {}", e.getMessage());
         log.debug(e.getMessage(),e);
      }
      
      return results;
   }   
   
   /**
    * Attempts to replace any subjects in the supplied list that are goups with
    * subjects representing the members of the groups. Handling of nested groups
    * is implementation specific depending on the cataloger that resolves the
    * group. Any supplied subjects that are users are simply added to the
    * returned set.
    * 
    * @param subjects A set of subjects, may contain both users and groups.
    * Never <code>null</code>, may be empty.
    * 
    * @return A new set containing all users supplied, and any users that were
    * the result of expanding the membership of any groups supplied, and any
    * groups that could not be expanded or that were returned as members of a
    * supplied group.  Subjects added as expanded group members will not have
    * any attributes loaded. 
    */
   public Set<PSSubject> expandGroups(Set<PSSubject> subjects)
   {
      Set<PSSubject> expanded = new HashSet<>();
      
      Map<String, PSSubject> groupMap = new HashMap<>();
      Set<IPSTypedPrincipal> principals = new HashSet<>();
      // add any users to the result list, build set of groups to expand
      for (PSSubject subject : subjects)
      {
         if (subject.isGroup())
         {
            principals.add(PSJaasUtils.subjectToPrincipal(subject));
            // save it in case it's not expanded
            groupMap.put(subject.getName(), subject);
         }
         else
            expanded.add(subject);
      }

      // walk results
      for (IPSTypedPrincipal principal : expandGroups(principals))
      {
         if (principal.getPrincipalType().equals(PrincipalTypes.GROUP))
         {
            String groupName = principal.getName();
            // see if not expanded
            PSSubject groupSub = groupMap.get(groupName);
            if (groupSub != null)
               expanded.add(groupSub);
            else
               expanded.add(PSJaasUtils.principalToSubject(principal));
         }
         else
            expanded.add(PSJaasUtils.principalToSubject(principal));
      }
      
      return expanded;
   }
   
   /**
    * Expands the membership of all groups in the supplied collection.  Any 
    * non-group entries are simply added to the results.
    * 
    * @param principals Collection of principals which may contain groups to be
    * expanded, assumed not <code>null</code>.
    * 
    * @return The resulting set of principals, may still contain groups that 
    * were not able to be expanded.
    */
   private Set <IPSTypedPrincipal> expandGroups(
      Collection <IPSTypedPrincipal> principals)
   {
      Set<IPSTypedPrincipal> expandedMembers = 
         new HashSet<>();
      Set <IPSTypedPrincipal> groups = 
         new HashSet<>();
      filterPrincipalsByType(principals, expandedMembers, groups);
      
      expandedMembers.addAll(PSRoleMgrLocator.getRoleManager().getGroupMembers(
         groups));
      
      // add whatever groups could not be expanded
      expandedMembers.addAll(groups);
      
      return expandedMembers;
   }

   /**
    * Filters the supplied principals by the supplied type and optional pattern.
    * 
    * @param roleMembers The set of principals to filter, assumed not
    * <code>null</code>.
    * @param subjectType The subject type to filter on, one of the
    * <code>PSSubject.SUBJECT_TYPE_XXX</code> values.
    * @param subjectNameFilter Optional subject name filter.  The filter uses 
    * SQL LIKE syntax ('%' matches 0 or more chars, '_' matches any single 
    * char), may be <code>null</code>. 
    * 
    * @return The list, never <code>null</code>, may be empty.
    */
   private List<String> filterPrincipals(Set<IPSTypedPrincipal> roleMembers, 
      int subjectType, String subjectNameFilter)
   {
      PSPatternMatcher patternMatch = null;
      if (!StringUtils.isBlank(subjectNameFilter))
      {
         patternMatch = PSPatternMatcher.SQLPatternMatcher(
            subjectNameFilter);
         patternMatch.setCaseSensitive(false);
      }
      
      List<String> results = new ArrayList<>();
      for (IPSTypedPrincipal principal : roleMembers)
      {
         String name = principal.getName();
         if (patternMatch == null || patternMatch.doesMatchPattern(name))
         {
            
            if (principal.getPrincipalType().equals(
               PSJaasUtils.getPrincipalType(subjectType)))
            {
               results.add(name);
            }
         }
      }
      
      return results;
   }

   /**
    * Converts the supplied subjects to {@link PSSubject} objects and merges
    * them into the supplied map.
    * 
    * @param subjects The subjects to process, assumed not <code>null</code>.
    * @param subjectMap Map of subjects where key is the subject name, assumed 
    * not <code>null</code>.  If a processed subject is already found in the 
    * map, it's attributes are added to the existing subject, otherwise it is
    * added to the map.
    */
   private void mergeSubjects(Collection<Subject> subjects, 
      Map<String, PSSubject> subjectMap)
   {
      for (Subject subject : subjects)
      {
         PSSubject pssubject = PSJaasUtils.convertSubject(subject);
         if (pssubject != null)
         {
            mergeSubject(subjectMap, pssubject);
         }
      }
   }

   /**
    * Merges the supplied subject into the supplied map.
    * 
    * @param subjectMap Map of subjects where key is the subject name, assumed 
    * not <code>null</code>.  If a processed subject is already found in the 
    * map, it's attributes are added to the existing subject, otherwise it is
    * added to the map.
    * @param subject The subject to process, assumed not <code>null</code>.
    */
   private void mergeSubject(Map<String, PSSubject> subjectMap, 
      PSSubject subject)
   {
      PSSubject curSub = subjectMap.get(subject.getName());
      if (curSub != null)
         curSub.getAttributes().mergeAttributes(subject.getAttributes());
      else
         subjectMap.put(subject.getName(), subject);
   }

   /**
    * Convenience method that calls {@link #roleMembers(String, int, String) 
    * roleMembers(roleName, memberFlags, null)}.
    */
   public List roleMembers(String roleName, int memberFlags)
   {
      return roleMembers(roleName, memberFlags, null);
   }


   /**
    * Get the list of members for the role specified.
    *
    * @param roleName The name of the role. Supply <code>null</code> or
    *    empty to get all subjects regardless of their role membership.
    * @param memberFlags 0 or more of the PSSubject.SUBJECT_TYPE_xxx flags,
    *    OR'd together. To get all types, pass in 0.
    * @param subjectNameFilter Can be used to limit the returned members. If
    *    <code>null</code> or empty, all members are returned. The filter
    *    uses SQL LIKE syntax.
    * @return  The list of members(members will be type <code>PSSubject</code>).
    *    Never <code>null</code>.
    * @throws PSSecurityException If the meta data to process the request
    *    can't be obtained.
    */
   public List roleMembers(String roleName, int memberFlags, 
      String subjectNameFilter)
   {
      Set members = getSubjects(roleName, subjectNameFilter);
      if (members.size() > 0 && memberFlags > 0)
      {
         Iterator iter = members.iterator();
         while (iter.hasNext())
         {
            PSSubject member = (PSSubject) iter.next();
            if ((member.getType() & memberFlags) == 0)
               iter.remove();
         }
      }
      
      return new ArrayList(members);
   }

   /**
    * Get all role attributes for the supplied role name.
    * 
    * @param roleName the role name for which to get the role attributes, not
    *    <code>null</code> or empty.
    * @return a valid list of 0 or more <code>PSAttribute</code> objects. They 
    *    are ordered in ascending alpha order by attribute name. There will
    *    be no duplicates. The caller takes ownership of the list.
    */
   public List getRoleAttributes(String roleName)
   {
      if (roleName == null)
         throw new IllegalArgumentException("roleName cannot be null");
      
      roleName = roleName.trim();
      if (roleName.length() == 0)
         throw new IllegalArgumentException("roleName cannot be empty");

      Set<IPSPrincipalAttribute> attrs = 
         PSRoleMgrLocator.getBackEndRoleManager().getRoleAttributes(roleName);
      List<PSAttribute> attrList = new ArrayList<>(attrs.size());

      for (IPSPrincipalAttribute attribute : attrs)
      {
         attrList.add(PSJaasUtils.convertAttribute(attribute));
      }

      final PSStringComparator comp = new PSStringComparator(
         PSStringComparator.SORT_CASE_INSENSITIVE_ASC);
      Comparator<PSAttribute> comparator = new Comparator<PSAttribute>() {

         public int compare(PSAttribute o1, PSAttribute o2)
         {
            return comp.compare(o1.getName(), o2.getName());
         }};
         
      Collections.sort(attrList, comparator);
      
      return attrList;
   }

   /**
    * Dynamically gets the global and role specific attributes for the
    * specified subject(s). The attributes are returned associated with their
    * subject. If an attribute appears in both the global and role list, the
    * one in the role list is returned. If you want to distinguish between
    * global and role attributes, call <code>getSubjectGlobalAttributes()</code> 
    * or <code>getSubjectRoleAttributes()</code> directly.
    *
    * @param subjectNameFilter The individual whose attributes you wish.
    *    Wildcards allowed following SQL LIKE syntax. If <code>null</code> or
    *    empty, all subjects are included.
    * @param subjectType One of the PSSubject.SUBJECT_TYPE_xxx flags.
    *    Provide 0 to ignore this property.
    * @param roleName If <code>null</code> only global subject attributes are
    *    returned. Otherwise, both role specific and global attributes are
    *    returned. If an attribute occurs in both the global and role list, the
    *    role specific one will be returned. Wildcards not allowed.
    * @param attributeNameFilter  A single pattern used to select the desired
    *    attributes. Use SQL LIKE syntax. Supply empty or <code>null</code> to
    *    get all attributes.
    * @return a valid list of 0 or more PSSubjects containing 1 or more
    *    attributes, ordered in ascending alpha order by subject name. The
    *    caller takes ownership of the list.
    */
   public List getSubjectAttributes(String subjectNameFilter, int subjectType, 
      String roleName, String attributeNameFilter)
   {
      List results;
      if (null == roleName || roleName.trim().length() == 0)
      {
         results = getSubjectGlobalAttributes(subjectNameFilter, subjectType, 
            roleName, attributeNameFilter);
      }
      else
      {
         results = getSubjectRoleAttributes(subjectNameFilter, subjectType, 
            roleName, attributeNameFilter);
         List global = getSubjectGlobalAttributes(subjectNameFilter, 
            subjectType, roleName, attributeNameFilter);

         Iterator iter = global.iterator();
         int size = results.size();
         while (iter.hasNext())
         {
            PSAttribute attrib = (PSAttribute) iter.next();
            for (int i = 0; i < size; i++)
            {
               PSAttribute roleAttrib = (PSAttribute) results.get(i);
               int comparison = roleAttrib.getName().compareTo(attrib.getName());
               if (comparison == 0)
                  break;
               else if (comparison > 0)
               {
                  int pos = i - 1;
                  if (pos < 0)
                     pos = 0;
                  results.add(pos, attrib);
                  break;
               }
            }
         }
      }

      return results;
   }

   /**
    * Gets only the global attributes for a set of subjects, using
    * {@link #DEFAULT_INCLUDE_EMPTY_SUBJECTS} as the includeEmptySubjects
    * parameter. See {@link #getSubjectGlobalAttributes(String, int, String, 
    * String, boolean) getSubjectGlobalAttributes} for details of params.
    */
   public List getSubjectGlobalAttributes(String subjectNameFilter, 
      int subjectType, String roleName, String attributeNameFilter)
   {
      return getSubjectGlobalAttributes(subjectNameFilter, subjectType,
         roleName, attributeNameFilter, DEFAULT_INCLUDE_EMPTY_SUBJECTS);
   }

   /**
    * Convenience method which calls {@link #getSubjectGlobalAttributes(String, 
    * int, String, String, boolean, String)} with <code>communityId</code> 
    * parameter set to <code>null</code>.
    */
   public List getSubjectGlobalAttributes(String subjectNameFilter, 
      int subjectType, String roleName, String attributeNameFilter, 
      boolean includeEmptySubjects)
   {
      return getSubjectGlobalAttributes(subjectNameFilter, subjectType,
            roleName, attributeNameFilter, includeEmptySubjects, null);
   }

   /**
    * Provides a list of subjects with attributes that match any supplied
    * filters.
    * <p>
    * This method may be slower when called with <code>includeEmptySubjects =
    * true</code> than when called with <code>false</code>.
    *
    * @param subjectNameFilter The individual whose attributes you wish.
    *    Wildcards allowed following SQL LIKE syntax. If <code>null</code> or
    *    empty, all subjects are included.
    * @param subjectType One of the PSSubject.SUBJECT_TYPE_xxx flags.
    *    Provide 0 to ignore this property.
    * @param roleName If <code>null</code> only global subject attributes are
    *    returned. Otherwise, both role specific and global attributes are
    *    returned. If an attribute occurs in both the global and role list, the
    *    role specific one will be returned. Wildcards not allowed.
    * @param attributeNameFilter  A single pattern used to select the desired
    *    attributes. Use SQL LIKE syntax. Supply empty or <code>null</code> to
    *    get all attributes.
    * @param includeEmptySubjects A flag to indicate whether subjects with
    *    no attributes should be included in the returned list. If <code>
    *    true</code>, they are included, otherwise, only subjects that
    *    have 1 or more attributes are included.
    * @param communityId the community to which the subjects must be a member
    *    of, may be <code>null</code> or empty in which case no filtering is
    *    done based on community
    * @return a valid list of 0 or more PSSubjects containing either 1 or more
    *    attributes (if includeEmptySubjects is <code>false</code>) or 0 or
    *    more attributes (if includeEmptySubjects is <code>true</code>),
    *    ordered in ascending alpha order by subject name. The caller
    *    takes ownership of the list.
    */
   public List getSubjectGlobalAttributes(String subjectNameFilter,
      int subjectType, String roleName, String attributeNameFilter,
      boolean includeEmptySubjects, String communityId)
   {
      // use treeset to prevent duplicates and enforce ordering
      Set subjects = new TreeSet(PSSubject.getSubjectIdentifierComparator());
      
      // add all subjects from all security providers
      subjects.addAll(getSecurityProviderSubjects(subjectNameFilter, 
         subjectType, roleName, attributeNameFilter, includeEmptySubjects, 
         communityId, BACKEND_GLOBAL_ATTRIBUTES));         

      return new ArrayList(subjects);
   }

   /**
    * Convenience method that calls {@link #getSubjects(String, String, int, 
    * String, String, boolean)} with parameters to ignore the filters 
    * subjectType, attributeNameFilter and communityId and to include empty
    * subjects.
    */
   public Set getSubjects(String roleName, String subjectNameFilter)
   {
      return getSubjects(roleName, subjectNameFilter, 0, null, null, true);
   }
   
   /**
    * Get all subjects for the supplied parematers from all security providers.
    * See {@link IPSInternalRoleCataloger#getSubjects(String, String, int, 
    * String, boolean)} for parameter description.
    * 
    * @return a set of subjects retrieved from all defined security providers
    *    for the supplied parameters, never <code>null</code>, may be empty.
    */
   public Set getSubjects(String roleName, String subjectNameFilter, 
      int subjectType, String attributeNameFilter, String communityId, 
      boolean includeEmpty)
   {
      // use treeset to prevent duplicates and enforce ordering
      Set results = new TreeSet(PSSubject.getSubjectIdentifierComparator());
      
      // add all subjects from all security providers
      results.addAll(getSecurityProviderSubjects(subjectNameFilter, subjectType, 
         roleName, attributeNameFilter,includeEmpty, communityId, 
         BACKEND_DEFAULT));
         
      return results;
   }

   /**
    * Returns the roles the user is a member of through group membership.
    *
    * @param subject The subject to check, assumed not <code>null</code>.
    * 
    * @return The list of role names as Strings, never <code>null</code>, may
    *    be emtpy, may contain duplicate rolenames.
    */
   private List<String> getGroupRoles(PSSubject subject) 
   {
      List<String> results = new ArrayList<>();

      try
      {
         IPSRoleMgr roleMgr = PSRoleMgrLocator.getRoleManager();
         for (Principal group : roleMgr.getUserGroups(
            PSJaasUtils.subjectToPrincipal(subject)))
         {
            results.addAll(roleMgr.getUserRoles(PSTypedPrincipal.createGroup(
               group.getName())));
         }
      }
      catch (PSSecurityCatalogException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(),e);
      }
      
      return results;
   }

   /**
    * Gets only the role specific attributes for a set of subjects. A subject
    * will not be included in the returned list if it does not have a role
    * attribute that matches the <code>attributeNameFilter</code>.
    * 
    * @param subjectNameFilter the subject name filter for which to get the
    *    role attributes, may be <code>null</code> or empty. This filter is
    *    ignored if <code>null</code> or empty.
    * @param subjectType One of the <code>PSSubject.SUBJECT_TYPE_xxx</code> 
    *    flags. Provide 0 to ignore this property. If 0 is provided, the
    *    type <code>PSSubject.SUBJECT_TYPE_USER</code> is used as default.
    * @param roleName the name of the role for which to filter the attributes,
    *    not <code>null</code> or empty.
    * @param attributeNameFilter an attribute name based on which to filter the
    *    results, may be <code>null</code> or empty in which case this filter
    *    is ignored.
    * @return a valid list of 0 or more PSSubjects with their attributes,
    *    no duplicates and ordered in ascending alpha order by subject name. 
    *    The caller takes ownership of the list.
    */
   public List getSubjectRoleAttributes(String subjectNameFilter,
      int subjectType, String roleName, String attributeNameFilter)
   {
      if (roleName == null)
         throw new IllegalArgumentException("roleName cannot be null");
         
      roleName = roleName.trim();
      if (roleName.length() == 0)
         throw new IllegalArgumentException("roleName cannot be empty");

      // use treeset to prevent duplicates and enforce ordering
      Set subjects = new TreeSet(PSSubject.getSubjectIdentifierComparator());
      
      // add all subjects from all security providers
      subjects.addAll(getSecurityProviderSubjects(subjectNameFilter, 
         subjectType, roleName, attributeNameFilter, false, null, 
         BACKEND_SUBJECT_ROLE_ATTRIBUTES));         
         
      return new ArrayList(subjects);
   }
   
   /**
    * Get all email addresses from all subjects that belong to the supplied role
    * for all defined security providers.  Any groups in the role membership
    * will be expanded to obtain email addresses from the users.
    * 
    * @param roleName the role for which to get all subject emails, not
    * <code>null</code> or empty.
    * @param emailAttributeName The email attribute name used for subjects that
    * are not returned with {@link PSPrincipalAttribute} typed as an email
    * address, may be empty or <code>null</code>. 
    * @param community the community for which to filter the result, may be
    * <code>null</code> or empty to ignore the community filter.  Any groups
    * that are members of the community will be expanded to use the members
    * when filtering the results.
    * @param subjectsWithoutEmail an empty set in which all subjects (as
    * <code>PSSubject</code> objects) without an email address will be
    * returned. May be <code>null</code> if users without email are not of
    * interest.
    * 
    * @return a set of email addresses as <code>String</code> objects in alpha
    * ascending order, never <code>null</code>, may be empty.
    */
   public Set<String> getRoleEmailAddresses(String roleName, 
      String emailAttributeName, String community, Set subjectsWithoutEmail)
   {
      if (StringUtils.isBlank(roleName))
         throw new IllegalArgumentException(
            "roleName may not be null or empty");
      
      Set<String> emailAddrs = new HashSet<>();
      
      try
      {
         IPSRoleMgr roleMgr = PSRoleMgrLocator.getRoleManager();
         Set<IPSTypedPrincipal> roleMembers = expandGroups(
            roleMgr.getRoleMembers(roleName));
         
         if (!StringUtils.isBlank(community))
         {
            roleMembers = filterByCommunity(roleMembers, community, true);
         }
         
         Set<IPSTypedPrincipal> principalsWithoutEmail = 
            new HashSet<>();
         emailAddrs.addAll(getSubjectEmailAddresses(roleMembers, 
            emailAttributeName, principalsWithoutEmail));
         
         if (subjectsWithoutEmail != null)
         {
            for (IPSTypedPrincipal principal : principalsWithoutEmail)
            {
               subjectsWithoutEmail.add(PSJaasUtils.principalToSubject(
                  principal));
            }
         }
      }
      catch (PSSecurityCatalogException e)
      {
         log.error("Failed to get role email addresses {}", e.getMessage());
         log.debug(e.getMessage(),e);
      }
      
      return emailAddrs;
   }
   
   /**
    * Get email addresses for all of the supplied principals.  
    * 
    * @param principals The list of principals for which email addresses are to
    * be returned, assumed not <code>null</code>, may be empty.
    * @param emailAttributeName The email attribute name used for subjects that
    * are not returned with {@link PSPrincipalAttribute} typed as an email
    * address, may be empty or <code>null</code>.
    * @param principalsWithoutEmail an empty set in which all principals without
    * an email address will be returned. May be <code>null</code> if users
    * without email are not of interest.
    * 
    * @return The email addresses, never <code>null</code>, will be empty if
    * an empty list of principals is supplied.
    */
   public Collection<String> getSubjectEmailAddresses(
      Collection<IPSTypedPrincipal> principals, 
      String emailAttributeName, 
      Set<IPSTypedPrincipal> principalsWithoutEmail)
   {
      Collection<String> addrs = new ArrayList<>();

      if (principals.isEmpty())
         return addrs; 
      
      // sort by type
      Map<String, IPSTypedPrincipal> usernames = 
         new HashMap<>();
      Map<String, IPSTypedPrincipal> groupnames = 
         new HashMap<>();
      
      for (IPSTypedPrincipal principal : principals)
      {
         if (principal.getPrincipalType().equals(PrincipalTypes.GROUP))
         {
            groupnames.put(principal.getName(), principal);
         }
         else
         {
            usernames.put(principal.getName(), principal);
         }
      }
      
      IPSRoleMgr roleMgr = PSRoleMgrLocator.getRoleManager();
      try
      {
         // get subjects and check for email attr by type and name if supplied
         Set<PrincipalAttributes> types = null;
         if (StringUtils.isBlank(emailAttributeName))
         {
            types = new HashSet<>();
            types.add(PrincipalAttributes.EMAIL_ADDRESS);
         }
         
         for (Subject subject : roleMgr.findUsers(
            new ArrayList<>(usernames.keySet()), null, null, types))
         {
            String email = processSubjectEmail(emailAttributeName, usernames, 
               subject);
            if (!StringUtils.isBlank(email))
               addrs.add(email);
         }
         
         // group attributes can only be defined in the backend
         if (!groupnames.isEmpty())
         {
            IPSBackEndRoleMgr beRoleMgr = 
               PSRoleMgrLocator.getBackEndRoleManager();
            for (String groupName : new ArrayList<String>(groupnames.keySet()))
            {
               for (Subject subject : beRoleMgr.getGlobalSubjectAttributes(
                  groupName, null, true))
               {
                  String email = processSubjectEmail(emailAttributeName, 
                     groupnames, subject);
                  if (!StringUtils.isBlank(email))
                     addrs.add(email);
               }
            }
         }
         
         // add users and groups without email
         if (principalsWithoutEmail != null)
         {
            principalsWithoutEmail.addAll(usernames.values());
            principalsWithoutEmail.addAll(groupnames.values());
         }
      }
      catch (PSSecurityCatalogException e)
      {
         log.error("Failed to lookup email addresses : {}", e.getMessage());
         log.debug(e.getMessage(),e);
      }
      
      return addrs;
   }

   /**
    * Attempt to obtain the email address from the supplied subject using the
    * specified attribute name.  
    * 
    * @param emailAttributeName The email attribute name used for subjects that
    * are not returned with {@link PSPrincipalAttribute} typed as an email
    * address, may be empty or <code>null</code>.
    * @param names Map of users being processed, where the key is the user's
    * name and the value is the principle from which the name was obtained,
    * assumed not <code>null</code>. If an email address is specified in the
    * supplied subject, the matching user is removed from this map.
    * @param subject The subject from which the email address is to be obtained
    * if possible, assumed not <code>null</code>.
    * 
    * @return The email address of the subject, may be <code>null</code> or 
    * empty if one is not specified.
    */
   private String processSubjectEmail(String emailAttributeName, 
      Map<String, IPSTypedPrincipal> names, Subject subject)
   {
      String email = PSJaasUtils.getSubjectAttributeValue(subject, 
         PrincipalAttributes.EMAIL_ADDRESS);
      if (StringUtils.isBlank(email) && 
         !StringUtils.isBlank(emailAttributeName))
      {
         email = PSJaasUtils.getSubjectAttributeValue(subject, 
            emailAttributeName);
      }
      
      if (!StringUtils.isBlank(email))
      {
         names.remove(
            PSJaasUtils.subjectToPrincipal(subject).getName());
      }
      
      return email;
   }


   
   /**
    * Get all email addresses from the supplied subject.
    * 
    * @param subjectName the subject from which to get all emails, not
    * <code>null</code> or empty.
    * @param emailAttributeName The email attribute name used for subjects that
    * are not returned with {@link PSPrincipalAttribute} typed as an email
    * address, may be empty or <code>null</code>. 
    * @param community the community for which to filter the result, may be
    * <code>null</code> or empty to ignore the community filter.
    * 
    * @return a set of email addresses as <code>String</code> objects, never
    * <code>null</code>, may be empty.
    */
   public Set getSubjectEmailAddresses(String subjectName, 
      String emailAttributeName, String community)
   {
      Set<String> addrs = new HashSet<>();
      try
      {
         IPSRoleMgr roleMgr = PSRoleMgrLocator.getRoleManager();
         Set<PrincipalAttributes> types = null;
         if (StringUtils.isBlank(emailAttributeName))
         {
            types = new HashSet<>();
            types.add(PrincipalAttributes.EMAIL_ADDRESS);
         }
         
         // first try as a user name
         List<String> names = new ArrayList<>(1);
         names.add(subjectName);
         List<Subject> subjects = roleMgr.findUsers(names, null, null, types);
         
         Set<IPSTypedPrincipal> principals = new HashSet<>();
         if (!subjects.isEmpty())
            principals.add(PSJaasUtils.subjectToPrincipal(subjects.get(0)));
         else
         {
            // try as a group
            List<Principal> groups = roleMgr.findGroups(subjectName, null, 
               null);
            if (!groups.isEmpty())
            {
               principals.add(PSTypedPrincipal.createGroup(
                  groups.get(0).getName()));
            }
         }
         
         if (!StringUtils.isBlank(community))
            principals = filterByCommunity(principals, community, true);
         
         if (!principals.isEmpty())
            addrs.addAll(getSubjectEmailAddresses(principals, 
               emailAttributeName, null));         
      }
      catch (PSSecurityCatalogException e)
      {
         log.error("Failed to lookup subject email addresses : {}", e.getMessage());
         log.debug(e.getMessage(),e);
      }
      
      return addrs;
   }
   
   /**
    * Filters the supplied collection of principals by type.  Those of undefined
    * type are discarded.
    * 
    * @param principals The collection to filter, assumed not <code>null</code>.
    * @param users The set to which principals representing users are added, 
    * assumed not <code>null</code>.
    * @param groups The set to which principals representing groups are added, 
    * assumed not <code>null</code>.
    */
   private void filterPrincipalsByType(Collection<IPSTypedPrincipal> principals, 
      Set<IPSTypedPrincipal> users, Set<IPSTypedPrincipal> groups)
   {
      for (IPSTypedPrincipal member : principals)
      {
         if (member.getPrincipalType().equals(PrincipalTypes.SUBJECT))
         {
            users.add(member);
         }
         else if (member.getPrincipalType().equals(PrincipalTypes.GROUP))
         {
            groups.add(member);
         }
      }    
   }
   
   /**
    * Private to implement the singleton pattern.
    */
   private PSRoleManager()
   {
   }

   /**
    * The name of this (pseudo-) security provider.
    */
   public static final String SP_NAME = "Role";


   /**
    * The singleton instance of this class. Initialized the first time it
    * is requested, then never <code>null</code> after that.
    */
   private static PSRoleManager ms_instance;

   /**
    * The logger to use, never <code>null</code>.
    */
   private static final Logger log = LogManager.getLogger(PSRoleManager.class);
   
   /**
    * The default value for including empty subjects (those with no attributes)
    * for methods that do not explicitly provide the option.
    */
   private final static boolean DEFAULT_INCLUDE_EMPTY_SUBJECTS = false;

   
   /**
    * Specifies the default request handling for backend security providers.
    */
   private static final int BACKEND_DEFAULT = 0;
   
   /**
    * Specifies a special handling is required for a global attributes 
    * request for backend security providers.
    */
   private static final int BACKEND_GLOBAL_ATTRIBUTES = 1;
   
   /**
    * Specifies a special handling is required for a subject role attributes 
    * request for backend security providers.
    */
   private static final int BACKEND_SUBJECT_ROLE_ATTRIBUTES = 2;
}
