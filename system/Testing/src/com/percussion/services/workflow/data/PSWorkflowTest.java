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
package com.percussion.services.workflow.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Unit tests for the {@link PSWorkflow} class.
 */
public class PSWorkflowTest extends TestCase
{
   public PSWorkflowTest(String name)
   {
      super(name);
   }
   
   private PSWorkflowRole createWorkflowRole(int roleId)
   {
      PSWorkflowRole role = new PSWorkflowRole();
      role.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_ROLE, roleId));
      role.setName("role_" + roleId);
      role.setDescription("desc_" + roleId);
      
      return role;
   }

   public void testAll() throws Exception
   {
      PSWorkflowRole role = createWorkflowRole(24);
      PSWorkflowRole role2 = new PSWorkflowRole();
      role2.fromXML(role.toXML());
      assertTrue(role.equals(role2));
      
      List<PSWorkflowRole> roles = new ArrayList<PSWorkflowRole>();
      roles.add(role);
      role2 = createWorkflowRole(25);
      roles.add(role2);
      Set<Integer> roleIds = new HashSet<Integer>();
      roleIds.add(role.getGUID().getUUID());
      roleIds.add(role2.getGUID().getUUID());
      Set<String> roleNames = new HashSet<String>();
      roleNames.add(role.getName());
      roleNames.add(role2.getName());
      
      List<String> recipients = new ArrayList<String>();
      recipients.add("recipient_1");
      recipients.add("recipient_2");
      
      PSNotification notification = new PSNotification();
      notification.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_NOTIFICATION, 48));
      notification.setRecipients(recipients);
      notification.setCCRecipients(recipients);
      PSNotification notification2 = new PSNotification();
      notification2.fromXML(notification.toXML());
      assertTrue(notification.equals(notification2));
      
      List<PSNotification> notifications = new ArrayList<PSNotification>();
      notifications.add(notification);
      notifications.add(notification2);
      
      PSTransition transition = createTransition(72, roles, notifications);
      PSTransition transition2 = new PSTransition();
      transition2.fromXML(transition.toXML());
      assertTrue(transition.equals(transition2));
      
      List<PSTransition> transitions = new ArrayList<PSTransition>();
      transitions.add(transition);
      transition2 = createTransition(82, roles, notifications);
      transitions.add(transition2);
      
      PSAgingTransition agingTransition = createAgingTransition(60);
      PSAgingTransition agingTransition2 = new PSAgingTransition();
      agingTransition2.fromXML(agingTransition.toXML());
      assertTrue(agingTransition.equals(agingTransition2));
      
      List<PSAgingTransition> agingTransitions = new ArrayList<PSAgingTransition>();
      agingTransitions.add(agingTransition);
      agingTransition2 = createAgingTransition(50);
      agingTransitions.add(agingTransition2);
      
      PSState state = new PSState();
      state.setName("state_1");
      state.setAgingTransitions(agingTransitions);
      state.setTransitions(transitions);
      state.setDescription("desc_1");
      
      PSAssignedRole assignedRole = new PSAssignedRole();
      assignedRole.setStateId(state.getStateId());
      PSAssignedRole assignedRole2 = new PSAssignedRole();
      assignedRole2.fromXML(assignedRole.toXML());
      assertTrue(assignedRole.equals(assignedRole2));
      
      List<PSAssignedRole> assignedRoles = new ArrayList<PSAssignedRole>();
      assignedRoles.add(assignedRole);
      assignedRole2 = new PSAssignedRole();
      assignedRole2.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_ROLE, assignedRole.getGUID().getUUID()+1));
      assignedRoles.add(assignedRole2);
      
      state.setAssignedRoles(assignedRoles);
      PSState state2 = new PSState();
      state2.fromXML(state.toXML());
      assertTrue(state.equals(state2));
      
      List<PSState> states = new ArrayList<PSState>();
      states.add(state);
      states.add(state2);
      
      PSNotificationDef notificationDef = new PSNotificationDef();
      notificationDef.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_NOTIFICATION, 36));
      notificationDef.setSubject("subject_1");
      notificationDef.setBody("body_1");
      PSNotificationDef notificationDef2 = new PSNotificationDef();
      notificationDef2.fromXML(notificationDef.toXML());
      assertTrue(notificationDef.equals(notificationDef2));
      
      List<PSNotificationDef> notificationDefs = 
         new ArrayList<PSNotificationDef>();
      notificationDefs.add(notificationDef);
      notificationDefs.add(notificationDef2);
      
      PSWorkflow wf = new PSWorkflow();
      wf.setGUID(new PSGuid(PSTypeEnum.WORKFLOW, 12));
      wf.setName("name");
      wf.setDescription("description");
      wf.setAdministratorRole("admin");
      wf.setInitialStateId(1);
      wf.setStates(states);
      wf.setRoles(roles);
      wf.setNotificationDefs(notificationDefs);
      wf.setVersion(0);
      System.out.println(wf.toXML());

      PSWorkflow wf2 = new PSWorkflow();
      wf2.fromXML(wf.toXML());
      assertTrue(wf.equals(wf2));
      
      assertEquals(roles, wf.getRoles());
      assertEquals(roleNames, wf.getRoleNames(roleIds));
      assertEquals(roleIds, wf.getRoleIds(roleNames));

   }

   private PSAgingTransition createAgingTransition(int uuId)
   {
      PSAgingTransition agingTransition = new PSAgingTransition();
      agingTransition.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_TRANSITION, uuId));
      agingTransition.setLabel("agingLabel_" + uuId);
      agingTransition.setDescription("agingDesc_" + uuId);
      return agingTransition;
   }

   private PSTransition createTransition(int uuId, List<PSWorkflowRole> roles,
         List<PSNotification> notifications)
   {
      PSTransition transition = new PSTransition();
      transition.setGUID(new PSGuid(PSTypeEnum.WORKFLOW_TRANSITION, uuId));
      transition.setLabel("tranLabel_" + uuId);
      transition.setNotifications(notifications);
      transition.setDescription("tranDesc_" + uuId);
      List<PSTransitionRole> transRoles = new ArrayList<PSTransitionRole>();
      for (PSWorkflowRole wfrole : roles)
      {
         PSTransitionRole transRole = new PSTransitionRole();
         transRole.setWorkflowId(wfrole.getWorkflowId());
         transRole.setTransitionId(transition.getGUID().longValue());
         transRole.setRoleId(wfrole.getGUID().longValue());
         transRoles.add(transRole);
      }
      transition.setTransitionRoles(transRoles);
      return transition;
   }
}

