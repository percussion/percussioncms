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
package com.percussion.install;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSAssignedRole;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSTransitionRole;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.Validate;
import org.w3c.dom.Element;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

/**
 * Add the Designer Role to the system.  @Todo: Add the role to existing workflows as an assignee of every state as we do
 * for Admin role.
 * 
 * @author JaySeletz
 *
 */
public class PSUpgradePluginAddDesignerRole extends PSSpringUpgradePluginBase
{
   private static final String DESIGNER_ROLE_DESC = "Designers have the same abilities as Admins, except that they do not have the ability to manage workflows, users, roles, and folders, and cannot create, delete, or copy sites.";
   public static final String DESIGNER_ROLE_NAME = "Designer";
   
   private IPSBackEndRoleMgr backEndRoleMgr;
   private IPSWorkflowService wfService;
   private IPSGuidManager guidMgr;
   private PrintStream logger;

   public PSUpgradePluginAddDesignerRole()
   {
      super();
      backEndRoleMgr = PSRoleMgrLocator.getBackEndRoleManager();
      wfService = PSWorkflowServiceLocator.getWorkflowService();
      guidMgr = PSGuidManagerLocator.getGuidMgr();
   }

   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      logger = config.getLogStream();
      
      try
      {
         addRoleToSystem();
         addRoleToWorkflows();
      }
      catch (Exception e)
      {
         e.printStackTrace(logger);
         return new PSPluginResponse(PSPluginResponse.EXCEPTION,
               e.getLocalizedMessage());
      }

      return new PSPluginResponse(PSPluginResponse.SUCCESS, "");
   }

   
   /**
    * Add the designer role as an assignee to all workflows in the system
    */
   private void addRoleToWorkflows()
   {
      List<PSWorkflow> allWorkflows = wfService.findWorkflowsByName(null);
      for (PSWorkflow workflow : allWorkflows)
      {
         logger.println("Updating workflow: " + workflow.getName());
         addRoleToWorkflow(workflow);
         wfService.saveWorkflow(workflow);         
      }
   }


   /**
    * Add the designer role as an assignee to all states and transitions in the supplied workflow.
    * 
    * @param workflow The workflow, not <code>null</code>.
    */
   void addRoleToWorkflow(PSWorkflow workflow)
   {
      Validate.notNull(workflow);
      
      if (!workflow.getRoleIds(Collections.singletonList(DESIGNER_ROLE_NAME)).isEmpty())
      {
         logger.println("Workflow already has the " + DESIGNER_ROLE_NAME + " role.");
      }
      else
      {
         logger.println("Adding " + DESIGNER_ROLE_NAME + " role to workflow");
         IPSGuid roleId = guidMgr.createGuid(PSTypeEnum.WORKFLOW_ROLE);
         wfService.addRoleToWorkflow(roleId, DESIGNER_ROLE_NAME, workflow);
         setAsAssignee(roleId, workflow);
      }
   }

   /**
    * For all states in the supplied workflow, set any assigned roles matching the supplied roleId as an assignee, and add to
    * all transitions.  Assumes the role has already been added to the state.
    * 
    * @param roleId 
    * @param workflow
    */
   private void setAsAssignee(IPSGuid roleId, PSWorkflow workflow)
   {
      logger.println("Setting " + DESIGNER_ROLE_NAME + " role as assignee in all states");
      
      long wfId = workflow.getGUID().getUUID();
      List<PSState> states = workflow.getStates();
      for (PSState state : states)
      {
         List<PSAssignedRole> assignedRoles = state.getAssignedRoles();
         
         boolean found = false;
         for (PSAssignedRole assignedRole : assignedRoles) 
         {
             assignedRole.setWorkflowId(wfId);
             int asRoleId = assignedRole.getGUID().getUUID();
             if (asRoleId == roleId.getUUID())
             {
                logger.println("Setting " + DESIGNER_ROLE_NAME + " role as assignee for state: " + state.getName());
                assignedRole.setAssignmentType(PSAssignmentTypeEnum.ASSIGNEE);
                assignedRole.setDoNotify(false);
                found = true;
                break;
             }
         }
         
         if (!found)
         {
            logger.println("Error: " + DESIGNER_ROLE_NAME + " role not found as assigned role for state: " + state.getName());
            return;
         }
         
         logger.println("Adding " + DESIGNER_ROLE_NAME + " role to transitions");
         List<PSTransition> transitions = state.getTransitions();
         for (PSTransition transition : transitions)
         {
            addRoleToTransition(roleId, transition, workflow);
         }
         state.setTransitions(transitions);
      }

   }

   /**
    * Add a transition role for the supplied role id to the supplied transition
    * 
    * @param roleId The role Id to use.
    * @param workflow The workflow to update.
    */
   private void addRoleToTransition(IPSGuid roleId, PSTransition transition, PSWorkflow workflow)
   {
      List<PSTransitionRole> transRoles = transition.getTransitionRoles();
      for (PSTransitionRole transitionRole : transRoles)
      {
         if (transitionRole.getGUID().equals(roleId))
         {
            return;
         }
      }
      
      PSTransitionRole transRole = new PSTransitionRole();
      transRole.setRoleId(roleId.getUUID());
      transRole.setTransitionId(transition.getGUID().getUUID());
      transRole.setWorkflowId(workflow.getGUID().getUUID()); 
      transRoles.add(transRole);
      transition.setTransitionRoles(transRoles);      
   }
   
   /**
    * Set logger, only used by unit test, otherwise set by process() method
    * 
    * @param out print stream to log to.
    */
   void setLogger(PrintStream out)
   {
      logger = out;
   }
   
   /**
    * Add the role
    */
   private void addRoleToSystem()
   {
      logger.println("Adding " + DESIGNER_ROLE_NAME + " role to the system...");
      backEndRoleMgr.createRole(DESIGNER_ROLE_NAME, DESIGNER_ROLE_DESC);
      logger.println(DESIGNER_ROLE_NAME + " role successfully added to the system");
   }

}
