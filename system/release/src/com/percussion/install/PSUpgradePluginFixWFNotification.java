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

import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSAssignedRole;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.services.workflow.data.PSNotification;
import com.percussion.services.workflow.data.PSNotificationDef;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.guid.IPSGuid;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.w3c.dom.Element;

/**
 * Fix WF notification by 1. Add missing "to state" notifications to all transitions so that any transition may cause a notification
 * for the roles with "notify" in that "to state", and 2. Fix the "Approved" step notification settings: if notify is found on roles 
 * in all 3 Approved states, this means it was set from the UI, so remove it from the corresponding roles in the Live and Quick Edit 
 * steps to match the new/correct setting for the Approved step.  If of the 3 states a role has notify set only in Quick Edit, 
 * remove it (it was copied when WF was created and is not set intentionally).
 * 
 * @author JaySeletz
 *
 */
public class PSUpgradePluginFixWFNotification extends PSSpringUpgradePluginBase
{
   private IPSWorkflowService wfService;
   private PrintStream logger;
   
   // Constant for Quick Edit step
   private static final String QUICK_EDIT_STATE = "Quick Edit";
   
   // Constant for Pending step
   private static final String PENDING_STATE = "Pending";
   
   // Constant for Live step
   private static final String LIVE_STATE = "Live";
   
   public PSUpgradePluginFixWFNotification()
   {
      super();
      wfService = PSWorkflowServiceLocator.getWorkflowService();
   }
   
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      logger = config.getLogStream();      

      try
      {
         fixAllWorkflows();
      }
      catch (Exception e)
      {
         return new PSPluginResponse(PSPluginResponse.EXCEPTION,
               e.getLocalizedMessage());
      }

      return new PSPluginResponse(PSPluginResponse.SUCCESS, "");
   }

   /**
    * Find all workflows, and apply the fixes to each
    */
   private void fixAllWorkflows()
   {
      List<PSWorkflow> allWorkflows = wfService.findWorkflowsByName(null);
      for (PSWorkflow workflow : allWorkflows)
      {
         fixWorkflow(workflow);
         wfService.saveWorkflow(workflow);         
      }
   }

   /**
    * Fix up a single workflow, changes are not persisted, exposed w/package access for unit testing.
    * 
    * @param workflow The workflow to process, not <code>null</code>.
    */
   void fixWorkflow(PSWorkflow workflow)
   {
      Validate.notNull(workflow);
      
      logger.println("Updating workflow: " + workflow.getName());
      IPSGuid wfGuid = workflow.getGUID();
      
      Map<String, PSState> stepMap = new HashMap<>();
      List<PSState> states = workflow.getStates();
      for (PSState state : states)
      {
         logger.println("Updating transition notifications for state: " + state.getName());
         // ensure all transitions have notification
         ensureTransitionNotifications(workflow, wfGuid, state);
         
         stepMap.put(state.getName(), state);
      }
      
      fixApproveStates(stepMap);
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
    * Fixup role notification settings on approval states
    *  
    * @param stepMap Map of state name to states.
    */
   private void fixApproveStates(Map<String, PSState> stepMap)
   {
      logger.println("Fixing approval states");
      PSState quickEdit = stepMap.get(QUICK_EDIT_STATE);
      PSState pending = stepMap.get(PENDING_STATE);
      PSState live = stepMap.get(LIVE_STATE);
      
      if (quickEdit == null || pending == null || live == null)
      {
         logger.println("All approval states not found, no update will be performed.");
         return;
      }
      
      List<PSAssignedRole> qeRoles = quickEdit.getAssignedRoles();
      for (PSAssignedRole role : qeRoles)
      {
         if (role.getAssignmentType().equals(PSAssignmentTypeEnum.ASSIGNEE))
         {
            PSAssignedRole pRole = getMatchingRole(role, pending);
            PSAssignedRole lRole = getMatchingRole(role, live);
            if (pRole == null && lRole == null)
            {
               logger.println("No matching role found in pending and/or live states, no update will be performed for roles w/guid: " + role.getGUID());
               continue;
            }
            
            if (role.isDoNotify() && pRole.isDoNotify() && lRole.isDoNotify())
            {
               // this means user has set notify via UI, leave only on Pending
               logger.println("Leaving notify setting on Pending for role: " + role.getGUID());
               role.setDoNotify(false);
               lRole.setDoNotify(false);
            }
            else
            {
               // notif not intended by user, clear all
               logger.println("Clearing notify setting for role: " + role.getGUID());
               role.setDoNotify(false);
               lRole.setDoNotify(false);
               pRole.setDoNotify(false);
            }
         }
      }
      
   }

   /**
    * Find the matching role in the supplied state
    * 
    * @param role The role to match
    * @param state The state to check
    * 
    * @return The role, <code>null</code> if no match is found.
    */
   private PSAssignedRole getMatchingRole(PSAssignedRole role, PSState state)
   {
      PSAssignedRole result = null;
      
      List<PSAssignedRole> roles = state.getAssignedRoles();
      for (PSAssignedRole test : roles)
      {
         if (test.getGUID().equals(role.getGUID()))
         {
            result = test;
            break;
         }
      }
      
      return result;
   }

   private void ensureTransitionNotifications(PSWorkflow workflow, IPSGuid wfGuid, PSState state)
   {
      List<PSTransition> transitions = state.getTransitions();
      for (PSTransition transition : transitions)
      {
         List<PSNotification> notifications = transition.getNotifications();
         if (notifications.isEmpty())
         {
             long notificationId = getNotificationId(workflow);
             if (notificationId == -1)
             {
                logger.println("No notification found in Workflow: " + workflow.getName());
             }
             
             PSNotification notif = createNotification(transition.getGUID(), wfGuid, notificationId);
             notifications.add(notif);
             transition.setNotifications(notifications);                        
         }
      }

      
      // BEN HACK - you MUST set transitions back on the state for them to be automagically converted to hibernate
      //  objects, otherwise your changes won't be persisted!!!
      state.setTransitions(transitions);
   }
   
   /**
    * Helper method to get the transition object from a state given the transition name
    * @param currentState the state that contains the transitions assumed not
    *            <code>null</code>.
    * @param transitionName the transition name to find assumed not <code>null</code>.
    * * @return the notification Id, -1 if the there is no transition
    *         definitions for the workflow.
    */
   private long getNotificationId(PSWorkflow workflow)
   {
       long notificationId = -1;
       List<PSNotificationDef> notifsDef = workflow.getNotificationDefs();
       notificationId = notifsDef.get(0).getGUID().getUUID();
       return notificationId;
   }

   /**
    * Creates a notification object with supplied name and workflow and transition guids.
    * @param transitionGuid used as the transition id of the transition, assumed not <code>null</code>
    * @param workflowGuid The new transition is created with this workflow id, assumed not <code>null</code>
    */
   private PSNotification createNotification(IPSGuid transitionGuid, IPSGuid workflowGuid, long notificationId)
   {
       PSNotification notification = wfService.createNotification(workflowGuid, transitionGuid);
       notification.setNotificationId(notificationId);
       return notification;
   }
}
