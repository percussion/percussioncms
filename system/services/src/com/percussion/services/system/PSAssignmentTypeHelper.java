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
package com.percussion.services.system;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.data.PSBackEndRole;
import com.percussion.services.security.data.PSCommunityRoleAssociation;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSAdhocTypeEnum;
import com.percussion.services.workflow.data.PSAssignedRole;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.services.workflow.data.PSContentAdhocUser;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.util.PSCms;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.timing.PSStopwatchStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class computes the assignment type for one or more content items. This
 * caches information obtained during the calculation for use in subsequent
 * calculations, so it should be discarded after a series of calculations have
 * been completed to avoid any usage of stale data.
 * 
 * @author dougrand
 */
public class PSAssignmentTypeHelper
{
   /**
    * Commons logger
    */
   static Log ms_logger = LogFactory.getLog("PSAssignmentTypeHelper");

   /**
    * The workflow service
    */
   private IPSWorkflowService m_workflow = PSWorkflowServiceLocator
         .getWorkflowService();

   /**
    * The guid manager
    */
   private IPSGuidManager m_gmgr = PSGuidManagerLocator.getGuidMgr();

   /**
    * The user, never <code>null</code> or empty after construction, constant
    * after construction
    */
   private String m_user;

   /**
    * The roles for the user, never <code>null</code> after construction,
    * never modified after construction
    */
   private Set<String> m_roles;


   /**
    * The user's community
    */
   private int m_userCommunityId;
   
   /**
    * Used to retrieve and cache backend role info for community role filtering,
    * <code>null</code> until first call to {@link #init(String, Collection)}.
    */
   private PSBackendRoleInfo m_backendRoleInfo;

   /**
    * Ctor
    * 
    * @param userName the user making the request, never <code>null</code>
    * @param roles the roles of the user, never <code>null</code>
    * @param community the community id
    */
   public PSAssignmentTypeHelper(String userName, 
         List<String> roles, int community) {
      init(userName, roles);

      m_userCommunityId = community;
   }

   /**
    * Package protected constructor for unit test use only
    * 
    * @param userName the user name, never <code>null</code> or empty
    * @param roles the roles of the user, never <code>null</code>
    */
   PSAssignmentTypeHelper(String userName, Collection<String> roles) {
      init(userName, roles);
      m_userCommunityId = 1001; // EI
   }

   /**
    * Initialize data
    * 
    * @param userName the user's name, never <code>null</code> or empty
    * @param roles the roles, never <code>null</code>
    */
   private void init(String userName, Collection<String> roles)
   {
      if (userName == null || StringUtils.isBlank(userName))
      {
         throw new IllegalArgumentException("userName may not be null or empty");
      }
      if (roles == null)
      {
         throw new IllegalArgumentException("roles may not be null");
      }
      m_user = userName;
      m_roles = new HashSet<String>();
      m_roles.addAll(roles);
      m_backendRoleInfo = new PSBackendRoleInfo();
   }

   /**
    * Determine the assignment type from the piece of content and its workflow
    * state. This gets two pieces of basic information: the user's roles that
    * apply to the current state, and the user's adhoc roles if one or more
    * roles for the state allow adhoc. The final roles are examined against the
    * state's roles to compute the assignment type.
    * 
    * @param id the content id, never <code>null</code>
    * @return an assignment type, never <code>null</code>
    * @throws PSSystemException if the workflow state cannot be loaded
    */
   public PSAssignmentTypeEnum getAssignmentType(IPSGuid id)
         throws PSSystemException
   {
      if (id == null)
      {
         throw new IllegalArgumentException("id may not be null");
      }
      int contentid = ((PSLegacyGuid) id).getContentId();
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start("getAssignmentType");
      PSAssignmentTypeEnum rval = PSAssignmentTypeEnum.NONE;
      try
      {
         PSComponentSummary sum = null;

         try
         {
            sws.start("getComponentSummary");
            sum = loadComponentSummary(contentid);
         }
         finally
         {
            sws.stop();
         }

         try
         {
            sws.start("canReadInFolders");
            if (!PSCms.canReadInFolders(contentid))
            {
               return PSAssignmentTypeEnum.NONE;
            }
         }
         finally
         {
            sws.stop();
         }

         PSWorkflow wf = loadWorkflow(sum.getWorkflowAppId());
         IPSGuid stateguid = m_gmgr.makeGuid(sum.getContentStateId(),
               PSTypeEnum.WORKFLOW_STATE);
         PSState state = wf.findState(stateguid);
         if (state == null)
         {
            throw new RuntimeException("No state found in WF " + 
               sum.getWorkflowAppId() + "for state ID " + 
               sum.getContentStateId());
         }
         
         rval = getAssignmentType(wf, state, sum.getCommunityId(), id);
      }
      catch (SQLException e)
      {
         throw new PSSystemException(
               IPSSystemErrors.ERROR_DETERMINING_FOLDER_READ, e, contentid);
      }
      finally
      {
         sws.stop();
      }
      return rval;
   }
   
   /**
    * Determine the assignment type from the workflow, workflow state, community, and piece of content (optional).
    * This gets two pieces of basic information: the user's roles that apply to the current state, and the user's adhoc
    * roles if the item is specified and one or more roles for the state allow adhoc. The final roles are examined
    * against the state's roles to compute the assignment type.
    * 
    * @param wf the workflow, never <code>null</code>
    * @param state the workflow state, never <code>null</code>
    * @param communityId the community id 
    * @param id the content id, may be <code>null</code> to exclude the user's adhoc roles from the calculation
    * @return an assignment type, never <code>null</code>
    * @throws SQLException if an error occurs.
    */
   public PSAssignmentTypeEnum getAssignmentType(PSWorkflow wf, PSState state, int communityId, IPSGuid id) throws SQLException
   {
      if (wf == null)
      {
         throw new IllegalArgumentException("wf may not be null");
      }
      
      if (state == null)
      {
         throw new IllegalArgumentException("state may not be null");
      }
      
      PSAssignmentTypeEnum rval = PSAssignmentTypeEnum.NONE;
      
      if (m_roles.contains(wf.getAdministratorRole()))
      {
         rval = PSAssignmentTypeEnum.ADMIN;
      }
      else
      {
         Set<Integer> roleids = wf.getRoleIds(m_roles);
         Set<Integer> assignedroleids = getAssignedRoles(state, roleids);
         
         if (id != null)
         {
            Set<Integer> adhocroles = getAdhocAssignmentTypeRoles(id, state,
                  roleids);
            // Now we just add the roles together to get a final tally of all
            // assigned or adhoc roles
            assignedroleids.addAll(adhocroles);
         }
         
         // now filter out community specific roles
         filterAssignedRolesByCommunity(communityId, wf,
            assignedroleids, m_backendRoleInfo);

         rval = PSAssignmentTypeEnum.NONE;

         // Get the highest assignment type. This calculation may get a bit
         // more complicated when we add editor and workflow types
         for (PSAssignedRole r : state.getAssignedRoles())
         {
            if (assignedroleids.contains(r.getGUID().getUUID()))
            {
               PSAssignmentTypeEnum type = r.getAssignmentType();
               if (type.getValue() > rval.getValue())
               {
                  rval = type;
               }
            }
         }
      }

      // Limit non-community actors to reader if they have any access
      if (communityId != m_userCommunityId
            && (rval.getValue() > PSAssignmentTypeEnum.READER.getValue()))
         rval = PSAssignmentTypeEnum.READER;
         
      /*
       * Override assignee permission if 
       * can't write to folder.
       * -Adam Gent
       */
      if ( (PSAssignmentTypeEnum.ASSIGNEE == rval || PSAssignmentTypeEnum.ADMIN == rval) 
            && PSCms.isFolderSecurityOverridesWorkflowSecurity() 
            && id != null
            && ! PSCms.canWriteInFolders(id.getUUID())) 
      {
         rval = PSAssignmentTypeEnum.READER;
      }

      return rval;
   }
   
   /**
    * Same as {@link #filterAssignedRolesByCommunity(int, Collection)} except
    * the content id is passed as a guid and the assigned roles are passed by
    * name instead of ID.
    * 
    * @param contentId The content of the item to check, may not be 
    * <code>null</code> and must be an existing item.
    * @param assignedRoleNames Collection of role names, may not be 
    * <code>null</code>, may be empty.
    */
   public static void filterAssignedRolesByCommunity(IPSGuid contentId,
      Collection<String> assignedRoleNames)
   {
      if (contentId == null)
         throw new IllegalArgumentException("contentId may not be null");
      
      if (assignedRoleNames == null)
         throw new IllegalArgumentException(
            "assignedRoleNames may not be null");
      
      if (assignedRoleNames.isEmpty())
         return;
      
      PSComponentSummary sum = loadComponentSummary(contentId.getUUID());
      
      filterAssignedRolesByCommunity(sum.getCommunityId(), 
         sum.getWorkflowAppId(), assignedRoleNames);
   }

   /**
    * Load the component summary for the supplied content id
    * 
    * @param contentId The ID of the summary to load, must be an existing
    * item.
    * 
    * @return The summary, never <code>null</code>.
    */
   private static PSComponentSummary loadComponentSummary(int contentId)
   {
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary sum = cms.loadComponentSummary(contentId);

      if (sum == null)
      {
         throw new IllegalArgumentException("Not a valid content id: "
               + contentId);
      }
      return sum;
   }

   /**
    * Checks each of the supplied WF roles to see if they also belong to the
    * supplied item's community, removing those that belong to a community other
    * than that of the item, keeping those that match or that do not belong to
    * any community.
    * 
    * @param contentId The content id of the item to check, must be an existing
    * item.
    * @param assignedRoleIds The role ids to filter, may not be 
    * <code>null</code>, may be empty
    */
   public static void filterAssignedRolesByCommunity(int contentId, 
      Collection<Integer> assignedRoleIds)
   {
      if (assignedRoleIds == null)
         throw new IllegalArgumentException("assignedRoleIds may not be null");
      
      if (assignedRoleIds.isEmpty())
         return;
      
      PSComponentSummary sum = loadComponentSummary(contentId);
      PSWorkflow wf = loadWorkflow(sum.getWorkflowAppId());    
      
      Set<Integer> roleIds = new HashSet<Integer>(assignedRoleIds);
      filterAssignedRolesByCommunity(sum.getCommunityId(), wf, roleIds, 
         new PSBackendRoleInfo());
      assignedRoleIds.clear();
      assignedRoleIds.addAll(roleIds);
   }
   
   /**
    * Checks each of the supplied WF roles to see if they also belong to the
    * specified community, removing those that belong to a community other
    * than the one specified, keeping those that match or that do not belong to
    * any community.
    * 
    * @param communityId The ID of the community to use for filtering.
    * @param wfId The ID of the workflow, must be an existing workflow.
    * @param roleNames The list of workflow role names to filter, may not be 
    * <code>null</code>.
    */
   public static void filterAssignedRolesByCommunity(int communityId, int wfId, 
      Collection<String> roleNames)
   {
      if (roleNames == null)
         throw new IllegalArgumentException("roleNames may not be null");
      
      PSWorkflow wf = loadWorkflow(wfId);
      PSBackendRoleInfo roleInfo = new PSBackendRoleInfo();
      Set<Integer> roleIds = wf.getRoleIds(roleNames);
      filterAssignedRolesByCommunity(communityId, wf, roleIds, 
         roleInfo);
      Set<String> filteredRoleNames = wf.getRoleNames(roleIds);
      roleNames.retainAll(filteredRoleNames);
   }

   /**
    * Load the workflow for the specfied ID.
    * 
    * @param wfId The workflow, must exist.
    * 
    * @return The workflow object, never <code>null</code>.
    */
   private static PSWorkflow loadWorkflow(int wfId)
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid workflowguid = gmgr.makeGuid(wfId, PSTypeEnum.WORKFLOW);
      IPSWorkflowService wfsvc = PSWorkflowServiceLocator.getWorkflowService();
      PSWorkflow wf = wfsvc.loadWorkflow(workflowguid);
      if (wf == null)
      {
         throw new IllegalArgumentException("No workflow found for ID: " + 
            wfId);
      }
      return wf;
   }

   /**
    * Checks each of the assigned WF roles to see if they also belong to the
    * specified community, removing those that belong to a community other than
    * the one that is specified, keeping those that match or that do not belong
    * to any community.
    * 
    * @param communityId The community id to match
    * @param wf The workflow that defines the roles being filtered, assumed not 
    * <code>null</code>.
    * @param assignedWFRoleIds The set of workflow role IDs to filter, assumed 
    * not <code>null</code>, may be empty.  Note that these IDs are different
    * from the back-end role IDs.
    * @param beRoleInfo Used to cache and retrieve backend role info,
    * assumed not <code>null</code>. 
    */
   private static void filterAssignedRolesByCommunity(int communityId, 
      PSWorkflow wf, Set<Integer> assignedWFRoleIds, 
      PSBackendRoleInfo beRoleInfo)
   {
      if (! isFilterAssignedRolesByCommunity())
         return;

      if (ms_logger.isDebugEnabled())
      {
         ms_logger
               .debug("Begin filterAssignedRolesByCommunity(): communityId="
                     + communityId + ", workflow(name,id)=(" + wf.getName()
                     + ", " + wf.getGUID().getUUID()
                     + "), WF Role IDs to be filtered, 'assignedWFRoleIds'="
                     + assignedWFRoleIds.toString());
      }
      
      if (assignedWFRoleIds.isEmpty())
         return;

      Set<String> assignedRoleNames = wf.getRoleNames(assignedWFRoleIds);
      
      Set<Long> assignedRoleIds = beRoleInfo.getBackendRoleIds(
         assignedRoleNames);

      if (ms_logger.isDebugEnabled())
      {
         ms_logger
               .debug("Backend Role IDs (of 'assignedWFRoleIds'), 'assignedRoleIds'="
                     + assignedRoleIds.toString()
                     + ", Role Names="
                     + assignedRoleNames.toString());
      }
      
      if (assignedRoleIds.isEmpty())
      {
         // system is misconfigured, but warnings will have already been logged.
         return;
      }
      
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
      List<IPSGuid> roleGuids = new ArrayList<IPSGuid>(assignedRoleIds.size());
      for (Long id : assignedRoleIds)
      {
         roleGuids.add(guidMgr.makeGuid(id, PSTypeEnum.ROLE));
      }

      
      List<PSCommunityRoleAssociation> commRoles = 
         beRoleInfo.getCommunityRoleAssociations(roleGuids);

      Set<Long> rolesWithCommunities = new HashSet<Long>();
      Set<Long> roleMatches = new HashSet<Long>();
      for (PSCommunityRoleAssociation cr : commRoles)
      {
         Long roleId = new Long(cr.getRoleId());
         rolesWithCommunities.add(roleId);
         if (cr.getCommunityId() == communityId)
            roleMatches.add(roleId);
      }
      
      Set<Long> rolesWithoutCommunties = new HashSet<Long>(
         assignedRoleIds);
      rolesWithoutCommunties.removeAll(rolesWithCommunities);
      assignedRoleIds.retainAll(roleMatches);
      assignedRoleIds.addAll(rolesWithoutCommunties);
      
      // now filter the supplied set of wf role ids
      Set<String> filteredNames = beRoleInfo.getBackendRoleNames(
         assignedRoleIds);      
      assignedWFRoleIds.retainAll(wf.getRoleIds(filteredNames));

      if (ms_logger.isDebugEnabled())
      {
         ms_logger
               .debug("Role Ids (in 'assignedRoleIds') used by any communities, 'rolesWithCommunities'="
                     + rolesWithCommunities.toString());
         ms_logger.debug("Role IDs with the community(id=" + communityId
               + ") 'roleMatches'= " + roleMatches.toString());

         ms_logger
               .debug("Filtered backend role IDs (assignedRoleIds - rolesWithCommunities + roleMatches)="
                     + assignedRoleIds.toString());
         ms_logger.debug("Filtered backend role names="
               + filteredNames.toString());
         ms_logger
               .debug("End filterAssignedRolesByCommunity(): filtered WF role IDs="
                     + assignedWFRoleIds.toString());
      }
      
   }

   /**
    * See {@link #isFilterAssignedRolesByCommunity()
    */
   private static Boolean ms_isFilterAssignedRolesByCommunity = null;
   
   /**
    * Determines if the assignment type calculation filtered by community roles is enabled.
    * @return <code>true</code> if it is enabled.
    */
   static public boolean isFilterAssignedRolesByCommunity()
   {
      if (ms_isFilterAssignedRolesByCommunity != null)
         return ms_isFilterAssignedRolesByCommunity;

      Properties props = PSServer.getServerProps();
      if (props == null)
         return true;
      
      String filterByCommunity = props.getProperty("assignmentTypeCalculationFilterCommunityRoles", "true");
      ms_isFilterAssignedRolesByCommunity = "true".equals(filterByCommunity);
      
      if (ms_logger.isDebugEnabled())
      {
         ms_logger.debug("Server property 'assignmentTypeCalculationFilterCommunityRoles' = '" + ms_isFilterAssignedRolesByCommunity + "'");
      }
      return ms_isFilterAssignedRolesByCommunity;
   }
   
   /**
    * This is used to enable or disable the filtering the workflow roles by
    * community roles during assignment type calculation.
    * 
    * @param isFilterByCommunity <code>true</code> if enable the filtering feature;
    * otherwise disable the filtering feature.
    */
   static public void setFilterAssignedRolesByCommnity(boolean isFilterByCommunity)
   {
      ms_isFilterAssignedRolesByCommunity = isFilterByCommunity;
   }
   
   /**
    * If there are roles in the state that are adhoc, check whether the user is
    * in the adhoc role. This is done by loading that user's adhoc assignments
    * into a content id to adhoc information map. Three conditions will exist
    * for any given role that allows adhoc:
    * <ul>
    * <li>The user has no assignment, in which case we don't include the role
    * <li>The role allows anonymous adhoc and the user has an assignment. In
    * this case the user gets the role, whether the user has the role or not, as
    * long as the assigned role matches.
    * <li>The role is not anonymous adhoc and the user had an assignment. In
    * this case the user only gets the role if the user is in that role.
    * </ul>
    * 
    * @param id the content item affected
    * @param state the state, assumed never <code>null</code>
    * @param roleids the list of user role ids for this workflow, assumed never
    *           <code>null</code>
    * @return a list of effective roles to be merged in, never <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private Set<Integer> getAdhocAssignmentTypeRoles(IPSGuid id, PSState state,
      Set<Integer> roleids)
   {
      Set<Integer> rval = Collections.EMPTY_SET;
      for (PSAssignedRole role : state.getAssignedRoles())
      {
         if (!role.getAdhocType().equals(PSAdhocTypeEnum.DISABLED))
         {
            if (rval == Collections.EMPTY_SET)
            {
               rval = new HashSet<Integer>();
            }
            int roleid = role.getGUID().getUUID();
            if (isUserInAdhocRole(id, roleid, roleids, role.getAdhocType()))
            {
               rval.add(roleid);
            }
         }
      }
      return rval;
   }

   /**
    * Does the user have the specified adhoc role. A user does if the user has
    * been adhoc assigned to the role for the given content id and adhoc type.
    * If the adhoc type is <code>ENABLED</code> and not <code>ANONYMOUS</code>
    * then the role also has to be one of the user's assigned roles.  If no one
    * is assigned to the adhoc role, then the user is considered to have the
    * adhoc role.
    * 
    * @param id the content id involved
    * @param roleid the role id being checked for
    * @param userRoles the workflow roles the user has
    * @param adhocType the adhoc type, assumed never <code>null</code>
    * @return <code>true</code> if the given role is an adhoc role for the
    *         user
    */
   private boolean isUserInAdhocRole(IPSGuid id, int roleid,
         Set<Integer> userRoles, PSAdhocTypeEnum adhocType)
   {
      
      List<PSContentAdhocUser> adhocs = m_workflow.findAdhocInfoByItem(id);
      
      // If no one is assigned adhoc, treat it like regular assignment
      if (adhocs.isEmpty())
      {
         return userRoles.contains(roleid);
      }
      
      boolean checkroles = adhocType.equals(PSAdhocTypeEnum.ENABLED);
      for (PSContentAdhocUser au : adhocs)
      {
         if (!StringUtils.equalsIgnoreCase(m_user, au.getUser()) || (roleid != au.getRoleId())
               || (au.getAdhocType() != adhocType.getValue()))
            continue;
         if (checkroles)
         {
            if (userRoles.contains(roleid))
               return true;
         }
         else
         {
            return true;
         }
      }
      
      return false;
   }

   /**
    * Get the assigned role ids for the state that apply to the user. If a role
    * uses adhoc, then skip the role here.
    * 
    * @param state the state, assumed never <code>null</code>
    * @param roleids the user's role ids, assumed never <code>null</code>
    * @return the state's roles that correspond to roles assigned to the user
    */
   private Set<Integer> getAssignedRoles(PSState state, Set<Integer> roleids)
   {
      Set<Integer> rids = new HashSet<Integer>();
      for (PSAssignedRole role : state.getAssignedRoles())
      {
         Integer roleid = role.getGUID().getUUID();
         if (role.getAdhocType().equals(PSAdhocTypeEnum.DISABLED)
               && roleids.contains(roleid))
         {
            rids.add(roleid);
         }
      }
      return rids;
   }
   
   /**
    * Retrieves, caches and processes backend role information.    
    * TODO: move the caching into the PSBackEndRoleMgr service layer
    */
   static class PSBackendRoleInfo
   {
      /**
       * Cached map of backend role ids to role names, never <code>null</code>.
       */
      private Map<Long, String> mi_roleNameMap = new HashMap<Long, String>();
      
      /**
       * Cached map of role names to backend role ids, never <code>null</code>.
       */
      private Map<String, Long> mi_roleIdMap = new HashMap<String, Long>();

      /**
       * Cached map of role ids to community role associations
       */
      private Map<Long, List<PSCommunityRoleAssociation>> mi_communityRoleMap = 
         new HashMap<Long, List<PSCommunityRoleAssociation>>();
      
      /**
       * Get the back-end role IDs that correspond to the supplied role names.
       * 
       * @param roleNames The role names, may not be <code>null</code>, may be 
       * empty.
       * 
       * @return The IDs, may not contain the same number of elements as the 
       * supplied names if a match for the name is not found (in which case a 
       * warning is logged), never <code>null</code>, may be empty.
       */
      public Set<Long> getBackendRoleIds(Collection<String> roleNames)
      {
         if (roleNames == null)
            throw new IllegalArgumentException("roleNames may not be null");
         
         Set<String> roleNameSet = new HashSet<String>(roleNames);
         Set<Long> assignedRoleIds = new HashSet<Long>();
         
         IPSBackEndRoleMgr mgr = PSRoleMgrLocator.getBackEndRoleManager();
         for (String roleName : roleNameSet)
         {
            long id;
            if (mi_roleIdMap.containsKey(roleName))
               id = mi_roleIdMap.get(roleName);
            else
            {
               List<PSBackEndRole> beRoles = mgr.findRolesByName(roleName);
               if (beRoles.isEmpty())
               {
                  ms_logger.warn(
                     "No matching backend role found for Workflow role: " 
                     + roleName);
                  continue;
               }
               id = beRoles.get(0).getId();
               mi_roleNameMap.put(id, roleName);
               mi_roleIdMap.put(roleName, id);
            }
            assignedRoleIds.add(id);
         }
         return assignedRoleIds;
      }

      /**
       * Get the names of the supplied back-end role IDs. Supplied IDs are
       * assumed to have been originally obtained via
       * {@link #getBackendRoleIds(Collection)}
       * 
       * @param roleIds The back-end role IDs for which names are to be
       * returned, may not be <code>null</code>.
       * 
       * @return The set of names, never <code>null</code>, may not contain
       * the same number of elements as the supplied list of IDs if all of the
       * IDs were not obtained by calling 
       * {@link #getBackendRoleIds(Collection)}.
       */
      public Set<String> getBackendRoleNames(Collection<Long> roleIds)
      {
         if (roleIds == null)
            throw new IllegalArgumentException("roleIds may not be null");
         
         Set<String> filteredNames = new HashSet<String>();
         for (long id : roleIds)
         {
            filteredNames.add(mi_roleNameMap.get(id));
         }
         return filteredNames;
      }

      /**
       * Get the community roles associations for the specified back-end role
       * IDs
       * 
       * @param roleGuids The list of IDs, may not be <code>null</code> or
       * empty.
       * 
       * @return The list of associations, never <code>null</code>, may be
       * empty.
       */
      public List<PSCommunityRoleAssociation> getCommunityRoleAssociations(
         List<IPSGuid> roleGuids)
      {
         if (roleGuids == null || roleGuids.isEmpty())
            throw new IllegalArgumentException(
               "roleGuids may not be null or empty");
         
         List <PSCommunityRoleAssociation> results = 
            new ArrayList<PSCommunityRoleAssociation>();
         
         // first check the cache
         List<IPSGuid> toFindList = new ArrayList<IPSGuid>();
         for (IPSGuid guid : roleGuids)
         {
            if (!mi_communityRoleMap.containsKey(guid.longValue()))
               toFindList.add(guid);
            else
               results.addAll(mi_communityRoleMap.get(guid.longValue()));
         }
         
         // find those that are missing
         if (!toFindList.isEmpty()) 
         {
            IPSBackEndRoleMgr roleMgr = PSRoleMgrLocator.getBackEndRoleManager();
            List <PSCommunityRoleAssociation> commRoles = 
               roleMgr.findCommunitiesByRole(toFindList);
            results.addAll(commRoles);
            
            // now add the results to the cache
            for (PSCommunityRoleAssociation assoc : commRoles)
            {
               List<PSCommunityRoleAssociation> assocList = 
                  mi_communityRoleMap.get(assoc.getRoleId());
               if (assocList == null)
               {
                  assocList = new ArrayList<PSCommunityRoleAssociation>();
                  mi_communityRoleMap.put(assoc.getRoleId(), assocList);
               }
               assocList.add(assoc);
            }
         }
         
         return results;
      }
   }
}
