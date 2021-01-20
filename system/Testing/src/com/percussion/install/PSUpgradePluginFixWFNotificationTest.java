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
package com.percussion.install;

import static org.junit.Assert.*;

import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSAssignedRole;
import com.percussion.services.workflow.data.PSNotification;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSWorkflow;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.percussion.util.PSResourceUtils;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

/**
 * Loads a test workflow from xml and runs the plugin against it to ensure expected
 * changes are made.
 * 
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSUpgradePluginFixWFNotificationTest
{

   private static final String LIVE = "Live";
   private static final String QUICK_EDIT = "Quick Edit";
   private static final String PENDING = "Pending";
   private static final String ADMIN_ROLE_GUID = "0-25-9";
   
   @Before
   public void setup()
   {
      // initialize the locator for unit testing
      PSWorkflowServiceLocator.getWorkflowService();
   }

   @Test
   public void test() throws IOException, SAXException
   {
      PSUpgradePluginFixWFNotification plugin = new PSUpgradePluginFixWFNotification();
      plugin.setLogger(System.out);
      
      File wfFile = PSResourceUtils.getFile(PSUpgradePluginFixWFNotificationTest.class,"/com/percussion/rxupgrade/FixWFNotificationWorkflow.xml",null);
      PSWorkflow testWF = new PSWorkflow();
      testWF.fromXML(FileUtils.readFileToString(wfFile));
      
     // before, admin, pending=true, qe/live=false, means false
      setWFNotifForApproveStep(testWF, ADMIN_ROLE_GUID, true, false, false);
      plugin.fixWorkflow(testWF);
      // check that all transitions now have notification
      ensureNotificationsSet(testWF);
      checkWFNotifForApproveStep(testWF, ADMIN_ROLE_GUID, false, false, false);
      
      // before, admin, all true, means true (notify pending)
      setWFNotifForApproveStep(testWF, ADMIN_ROLE_GUID, true, true, true);
      plugin.fixWorkflow(testWF);
      checkWFNotifForApproveStep(testWF, ADMIN_ROLE_GUID, true, false, false);
      
      // before, admin, all false, means false
      setWFNotifForApproveStep(testWF, ADMIN_ROLE_GUID, false, false, false);
      plugin.fixWorkflow(testWF);
      checkWFNotifForApproveStep(testWF, ADMIN_ROLE_GUID, false, false, false);

   }
   
   @Test
   public void fixDefaultWorkflow()
   {
      IPSWorkflowService wfService = PSWorkflowServiceLocator.getWorkflowService();
      List<PSWorkflow> wfs = wfService.findWorkflowsByName("Default Workflow");
      assertTrue(wfs.size() == 1);
      PSWorkflow defaultWF = wfs.get(0);
      
      PSUpgradePluginFixWFNotification plugin = new PSUpgradePluginFixWFNotification();
      plugin.setLogger(System.out);
      
      plugin.fixWorkflow(defaultWF);
      
      wfService.saveWorkflow(defaultWF);
      
   }


   /**
    * @param testWF
    */
   private void ensureNotificationsSet(PSWorkflow testWF)
   {
      List<PSState> states = testWF.getStates();
      for (PSState state : states)
      {
         List<PSTransition> transitions = state.getTransitions();
         for (PSTransition transition : transitions)
         {
            List<PSNotification> notifications = transition.getNotifications();
            assertTrue(!notifications.isEmpty());
         }      
      }
   }

   private void setWFNotifForApproveStep(PSWorkflow workflow, String roleId, boolean notifyPending, boolean notifyQE, boolean notifyLive)
   {
      List<PSState> states = workflow.getStates();
      for (PSState state : states)
      {
         if (!(state.getName().equals(PENDING) || state.getName().equals(QUICK_EDIT) || state.getName().equals(LIVE)))
            continue;
         
         List<PSAssignedRole> roles = state.getAssignedRoles();
         for (PSAssignedRole role : roles)
         {
            if (role.getGUID().toString().equals(roleId))
            {
               if (state.getName().equals(PENDING))
                  role.setDoNotify(notifyPending);
               else if (state.getName().equals(QUICK_EDIT))
                  role.setDoNotify(notifyQE);
               else if (state.getName().equals(LIVE))
                  role.setDoNotify(notifyLive);
            }
         }
      }
   }
   
   
   private void checkWFNotifForApproveStep(PSWorkflow workflow, String roleId, boolean notifyPending, boolean notifyQE, boolean notifyLive)
   {
      List<PSState> states = workflow.getStates();
      for (PSState state : states)
      {
         if (!(state.getName().equals(PENDING) || state.getName().equals(QUICK_EDIT) || state.getName().equals(LIVE)))
            continue;
         
         List<PSAssignedRole> roles = state.getAssignedRoles();
         for (PSAssignedRole role : roles)
         {
            if (role.getGUID().toString().equals(roleId))
            {
               if (state.getName().equals(PENDING))
                  assertEquals(role.isDoNotify(), notifyPending);
               else if (state.getName().equals(QUICK_EDIT))
                  assertEquals(role.isDoNotify(), notifyQE);
               else if (state.getName().equals(LIVE))
                  assertEquals(role.isDoNotify(), notifyLive);
            }
         }
      }
   }


}
