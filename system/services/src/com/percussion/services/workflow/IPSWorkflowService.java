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
package com.percussion.services.workflow;

import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.services.workflow.data.PSContentAdhocUser;
import com.percussion.services.workflow.data.PSContentApproval;
import com.percussion.services.workflow.data.PSContentWorkflowState;
import com.percussion.services.workflow.data.PSNotification;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.workflow.PSWorkFlowUtils;

import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * The workflow service. At this point this provides methods to load the
 * workflows into memory.
 * <h2>Workflows</h2>
 * A workflow consists of a collection of states, which are arranged by 
 * pairwise relationships called transitions. An item can be in one workflow
 * state at a time. For any given state, there may be transitions available to
 * other states.
 * <p>
 * For an item in a given state, there are certain roles that are allowed
 * to manipulate the item. These roles are "assignee" roles. Other roles are
 * allowed access to the item only, these roles are "reader" roles. Additionally
 * there are "admin" roles and roles that are not included, in which case they
 * cannot act on the item. These possible <em>assignment types</em> will be
 * extended in the future.
 * <p>
 * One or more roles for a given state can allow <em>adhoc</em> assignment. 
 * Adhoc assignments allow the specification of a particular users or set
 * of users that have the assignee role. Normal adhoc requires that the user(s)
 * must also be members of the adhoc role. Anonymous adhoc allows any user to
 * be made an adhoc assignee, regardless of their normal membership in the role.
 * <p>
 * At this point one action plus one notification can be made on any given
 * transition. A notification sends electronic mail to:
 * <ul>
 * <li>Users in a given role
 * <li>Extra users in a CC list
 * </ul>
 * Notifications are sent asynchronously using the notification service.
 * <h2>Publishable State</h2>
 * A given workflow state has an attribute that describes what the publishable
 * state is when an item is in the given state. This value typical
 * describes if an item should be published, archived or ignored when 
 * publishing. Additionally, many implementations create a poor man's version
 * of staging by extending these values.
 *  
 * @see PSAssignmentTypeEnum for information about assignment types
 * @see IPSNotificationService for information about asynchronous notification
 * 
 * @author dougrand
 * 
 */
public interface IPSWorkflowService
{
   /**
    * Find all workflows for the specified name.
    * 
    * @param name the name of the workflow to find, may be <code>null</code>
    *           or empty in which case all workflows will be returned. Sql type
    *           (%) wildcards are supported.
    * 
    * @return a list of summaries for all found workflows for the supplied name,
    *         never <code>null</code>, may be empty.
    */
   public List<PSObjectSummary> findWorkflowSummariesByName(String name);

   /**
    * Load a workflow using a cached copy if possible. This is a fast call, but
    * will return a shared instance that must not be modified. The returned
    * workflow object is a complete workflow tree that includes all the 
    * other objects agregated by the workflow. This object graph can be 
    * traversed to discover workflow states and transitions without fear that
    * some portion of the tree has not been correctly loaded (causing a 
    * hibernate exception).
    * 
    * @param id The guid, may not be <code>null</code>.
    * 
    * @return the workflow, or <code>null</code> if the instance is not found.
    */
   public PSWorkflow loadWorkflow(IPSGuid id);

   /**
    * Load a workflow from the database
    * 
    * @param id The guid, may not be <code>null</code>.
    * 
    * @return the workflow, or <code>null</code> if the instance is not found.
    */
   public PSWorkflow loadWorkflowDb(IPSGuid id);
   
   /**
    * Load all workflows for the specified name. Unlike 
    * {@link #loadWorkflow(IPSGuid)}, this does not return a read-only copy 
    * of the workflow cached in memory. However, the returned object has been
    * properly configured so that all agregated data is available.
    * 
    * @param name the name if the workflow to find, may be <code>null</code>
    *           or empty in which case all workflows will be returned. Sql type
    *           (%) wildcards are supported.
    * @return a list with all found workflows for the supplied name, never
    *         <code>null</code>, may be empty.
    */
   public List<PSWorkflow> findWorkflowsByName(String name);

   /**
    * Save the designated workflow. Only used for testing at this point.
    * For internal use only.
    * 
    * @param workflow the workflow, never <code>null</code>
    */
   public void saveWorkflow(PSWorkflow workflow);
   
   /**
    * Delete the designated workflow. If the workflow does not exist, then this
    * call has no effect.
    * 
    * @param wfid the wfid guid, never <code>null</code>
    * @throws Exception 
    */
   public void deleteWorkflow(IPSGuid wfid) throws Exception;
   
   /**
    * Loads a specified workflow state. This is a fast call, but will return a
    * shared instance that must not be modified. The returned instance is not
    * the same instance (by address) you will find by traversing the workflow
    * identified in the call. However, it will have the same values.
    * 
    * @param stateId the id of the specified state; it may not be
    *           <code>null</code>.
    * @param workflowId the id of the workflow which contains the specified
    *           state; it may not be <code>null</code>.
    * 
    * @return the specified workflow state. It may be <code>null</code> if the
    *         specified workflow state does not exist.
    */
   public PSState loadWorkflowState(IPSGuid stateId, IPSGuid workflowId);
   
   /**
    * Loads a specified workflow state. This is a fast call, but will return a
    * shared instance that must not be modified. The returned instance is not
    * the same instance (by address) you will find by traversing the workflow
    * identified in the call. However, it will have the same values.
    * 
    * @param stateName the name of the specified state; it may not be
    *           <code>null</code>.
    * @param workflowId the id of the workflow which contains the specified
    *           state; it may not be <code>null</code>.
    * 
    * @return the specified workflow state. It may be <code>null</code> if the
    *         specified workflow state does not exist.
    */
   public PSState loadWorkflowStateByName(String stateName, IPSGuid workflowId);

   /**
    * Creates a state for the specified workflow. Caller is responsible to 
    * set all properties (except the ID) for the created object. 
    * 
    * @param workflowId the workflow ID, not <code>null</code>.
    * 
    * @return the created state with a new ID, never <code>null</code>.
    */
   public PSState createState(IPSGuid workflowId);
   
   /**
    * Creates a transition for a specified workflow and state.
    * Caller is responsible to set all properties (except the ID)
    * for the created object. 
    * 
    * @param wfId the workflow ID, not <code>null</code>.
    * @param stateId the state ID, not <code>null</code>.
    * 
    * @return the created transition with a new ID, never <code>null</code>.
    */
   public PSTransition createTransition(IPSGuid wfId, IPSGuid stateId);
   
   /**
    * Creates a notification for a specified workflow and transition.
    * Caller is responsible to set all properties (except the ID)
    * for the created object. 
    * 
    * @param wfId the workflow ID, not <code>null</code>.
    * @param transitionId the transition ID, not <code>null</code>.
    * 
    * @return the created transition with a new ID, never <code>null</code>.
    */
   public PSNotification createNotification(IPSGuid wfId, IPSGuid transitionId);
   
   /**
    * Items transition from state to state in the workflow. Each state is
    * associated with a publishable flag that describes what the item's 
    * relationship is to the publishing system. This could also be described
    * as a "state". The values of this state are somewhat determined by the
    * implementation, but there are a few standard states including
    * public. An item in public state implies that it should be published. This
    * method determines if the given state and workflow describes a public 
    * state.
    * <p>
    * The publishable state is determined by the state having a content valid
    * value of 'y'.
    * 
    * @param stateid GUID of the state, must not be <code>null</code>.
    * @param workflowId GUID of the workflow, must not be <code>null</code>.
    * @return <code>true</code> if it is public, <code>false</code>
    *         otherwise.
    * @throws PSWorkflowException if the workflow is not found or if the state
    *            is not found within the workflow.
    */
   public boolean isPublic(IPSGuid stateid, IPSGuid workflowId)
         throws PSWorkflowException;
   
   /**
    * Find adhoc information, matching on user.  
    * 
    * @param username the user name, never <code>null</code> or empty
    * @return a list of zero or more adhoc information instances
    */
   public List<PSContentAdhocUser> findAdhocInfoByUser(String username); 
   
   /**
    * Find adhoc information, matching on item.  
    * 
    * @param contentId the item guid, never <code>null</code>
    * 
    * @return a list of zero or more adhoc information instances
    */
   public List<PSContentAdhocUser> findAdhocInfoByItem(IPSGuid contentId); 
   
   /**
    * Save the adhoc information for a particular content item
    * @param adhoc the adhoc information, never <code>null</code>
    */
   public void saveContentAdhocUser(PSContentAdhocUser adhoc);
   
   /**
    * Delete the adhoc information for a particular content item
    * @param adhoc the adhoc information, never <code>null</code>
    */
   public void deleteContentAdhocUser(PSContentAdhocUser adhoc);
   
   /**
    * Get the workflow state for a set of content ids. 
    * @param contentids a list of content guids for which to obtain the 
    * workflow state, never <code>null</code>.
    * @return a list of workflow states, the same length as the
    * input list, never <code>null</code>, but with unpredictable ordering.
    */
   public List<PSContentWorkflowState> 
      getWorkflowStateForContent(List<IPSGuid> contentids);
   
   /**
    * Find approval information, matching on user. 
    * 
    * @param username the user name, never <code>null</code> or empty
    * @return a list of zero or more approval instances, never 
    * <code>null</code>.
    */
   public List<PSContentApproval> findApprovalsByUser(String username); 
   
   
   /**
    * Find approval information, matching on item. 
    * 
    * @param contentid the id for which approvals should be found 
    * <code>null</code>
    * 
    * @return a list of zero or more approval instances, never 
    * <code>null</code>.
    */
   public List<PSContentApproval> findApprovalsByItem(IPSGuid contentid);    
   
   /**
    * Save the approval information for a particular content item
    * 
    * @param approval the approval information, never <code>null</code>
    */
   public void saveContentApproval(PSContentApproval approval);
   
   /**
    * Delete the approval information for a particular content item
    * @param contentid the id for which approvals should be deleted, never 
    * <code>null</code>
    */
   public void deleteContentApprovals(IPSGuid contentid);
   
   /**
    * Convenience method that calls
    * {@link PSWorkflowActionsHelper#getAllWorkflowActions()} within a 
    * transaction boundary for efficiency.
    * 
    * @param contentids A list of content ids, not <code>null</code> or empty,
    * for which actions will be calculated.
    * @param assignmentTypes The assignment types for each of the supplied
    * content ids, not <code>null</code> or empty, must contain the same
    * number of elements as the content id list.
    * @param userName The name of the user for whom the actions will be
    * calculated, not <code>null</code> or empty.
    * @param userRoles The names of the roles the user is a member of, not
    * <code>null</code>, may be empty.
    * @param locale The locale to use for localizing action labels, may be 
    * <code>null</code> or empty to use the default locale.
    * 
    * @return The list of actions, never <code>null</code>, may be empty.
    * 
    * @throws PSWorkflowException If there are any errors.
    */
   List<PSMenuAction> getAllWorkflowActions(List<IPSGuid> contentids,
      List<PSAssignmentTypeEnum> assignmentTypes, String userName,
      List<String> userRoles, String locale) throws PSWorkflowException;
   
   /**
    * Update the version of a specified workflow.  Workflow versions with
    * <code>null</code> values will be set to 0, all other values will be
    * incremented by one.
    * 
    * @param id the id of the workflow whose version will be updated, never
    * <code>null</code>.
    */
   public void updateWorkflowVersion(IPSGuid id);
   
   /**
    * Adds the specified role to the specified workflow. The role will be added
    * to all states with read-only permission.
    * <p>
    * This can be used to add the given role to all workflows.
    * 
    * @param wfId the ID of the workflow. If it is <code>null</code>, then add
    * the given role to all workflows.
    * @param roleName the name of the role, not <code>null</code> or empty.
    * The role name must not be exist in the specified workflow.
    */
   void addWorkflowRole(IPSGuid wfId, String roleName);
   
   /**
    * Add a role to the specified workflow, but does not save the workflow.  Use
    * {@link #addWorkflowRole(IPSGuid, String)} to add the role to all workflows and save
    * the changes.
    * 
    * @param id the ID to use for the created role, if <code>null</code>, an ID is generated.
    * @param roleName the name of the role, not empty.
    * @param wf the workflow, not <code>null</code>.
    */
   void addRoleToWorkflow(IPSGuid id, String roleName, PSWorkflow wf);
 
   /**
    * Removes the specified role from the specified workflow.
    * This can be used to remove the role from all workflows.
    * 
    * @param wfId the ID of the workflow. If it is <code>null</code>, then remove
    * the role from all workflows.
    * @param roleName the name of the role, not <code>null</code> or empty.
    * 
    * @return <code>true</code> if a role with the specified name has been removed;
    * otherwise no role has been removed.
    */
   boolean removeWorkflowRole(IPSGuid wfId, String roleName);
   
   public PSWorkflow getDefaultWorkflow();
   
   /**
    * Gets the name of the default workflow.
    * 
    * @return the name of the default workflow. Never empty or <code>null</code>.
    * @throws RuntimeException if the workflow name in the property files is empty, 
    * don't exist in the file or not exist in CM1.
    */
   public String getDefaultWorkflowName();
   
   /**
    * Gets the default workflow ID.
    * 
    * @return the workflow ID, never <code>null</code>.
    */
   public IPSGuid getDefaultWorkflowId();


   void copyWorkflowToRole(String fromRole, String toRole);

}
