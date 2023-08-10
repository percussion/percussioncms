/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.workflow;

import com.percussion.server.IPSRequestContext;
import com.percussion.services.system.PSAssignmentTypeHelperTest;
import com.percussion.services.system.PSAssignmentTypeHelperTest.TestRole;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.collections.CollectionUtils;
import org.junit.experimental.categories.Category;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * The PSWorkflowRoleInfoTest class is a test class for static methods of the
 * class PSWorkflowRoleInfo.
 */

@Category(IntegrationTest.class)
public class PSWorkflowRoleInfoTest extends ServletTestCase
{
   /**
    * Setup additional information needed for tests.
    * 
    * @throws PSORMException
    */
   public static void setupInfo() throws PSORMException
   {
      PSAssignmentTypeHelperTest.setupInfo();
   }
   
   /**
    * Teardown additional information created during {@link #setupInfo()}
    * 
    * @throws PSORMException
    */
   public static void teardownInfo() throws PSORMException
   {
      PSAssignmentTypeHelperTest.teardownInfo();
   }
   
   /**
    * Test the {@link PSWorkflowRoleInfo} class.
    * 
    * @throws Exception If the test fails.
    */
   public void testWorkflowRoleInfo() throws Exception
   {
      try
      {
         setupInfo();
         
         Connection connection = PSConnectionHelper.getDbConnection();

         PSStateRolesContext src;
         PSContentAdhocUsersContext cauc;
         List stateRoleIDNotificationList = null;
         List stateRoleNameNotificationList;
         List stateAdhocActorNotificationList = null;
         IPSRequestContext requestCtx;
         List<String> actorRolesList;

         IPSSecurityWs secWs = PSSecurityWsLocator.getSecurityWebservice();
         requestCtx = secWs.getRequestContext();

         int contentid = PSAssignmentTypeHelperTest.COMM_CIDS[0];
         PSAssignmentTypeHelperTest.updateComponentSummary(contentid, 
            WORKFLOW_ID, PSAssignmentTypeHelperTest.COMMTEST_STATE_ID);

         src = new PSStateRolesContext(
            WORKFLOW_ID,
            connection,
            PSAssignmentTypeHelperTest.COMMTEST_STATE_ID,
            PSWorkFlowUtils.ASSIGNMENT_TYPE_NOT_IN_WORKFLOW);

         assertTrue(CollectionUtils.isEqualCollection(src.getStateRoleNames(),
                 PSWorkflowRoleInfoStatic.roleIDListToRoleNameList(src.getStateRoleIDs(),
               src)));

         List<Integer> notifRoleIDs = new ArrayList<>();
         notifRoleIDs.add(TestRole.EDITOR.getRoleId());
         notifRoleIDs.add(TestRole.QA.getRoleId());
         notifRoleIDs.add(TestRole.EI_ADMIN_MEMBERS.getRoleId());
         notifRoleIDs.add(TestRole.EI_MEMBERS.getRoleId());

         List<Integer> roleIDs = new ArrayList<>();
         roleIDs.add(TestRole.AUTHOR.getRoleId());
         roleIDs.add(TestRole.ADMIN.getRoleId());
         roleIDs.addAll(notifRoleIDs);

         List rolesNotificationEnabled =
                 PSWorkflowRoleInfoStatic.filterRolesNotificationEnabled(
               roleIDs, src);
         assertTrue(CollectionUtils.isEqualCollection(notifRoleIDs, 
            rolesNotificationEnabled));
         assertFalse(CollectionUtils.isEqualCollection(roleIDs, 
            rolesNotificationEnabled));

         //used by PSExitAuthenticateUser
         List<String> assignedRoleNames = new ArrayList<>();
         assignedRoleNames.add(TestRole.EDITOR.name());
         assignedRoleNames.add(TestRole.QA.name());
         assignedRoleNames.add(TestRole.EI_ADMIN_MEMBERS.name());
         assignedRoleNames.add(TestRole.AUTHOR.name());
         List<String> memberRoleNames = new ArrayList<>(assignedRoleNames);
         memberRoleNames.add(TestRole.CI_MEMBERS.name());
         memberRoleNames.add(TestRole.ADMIN.name());

         String userRoleNames = PSWorkFlowUtils.listToDelimitedString(
            memberRoleNames, ",");
         List<Integer> actorRoleIDList = PSWorkflowRoleInfoStatic.getActorRoles(contentid, src,
            PSAssignmentTypeHelperTest.ADHOC_USER_ANON, userRoleNames,
            connection, true);
         actorRolesList = PSWorkflowRoleInfoStatic.roleIDListToRoleNameList(
            actorRoleIDList, src);
         assertTrue(CollectionUtils.isEqualCollection(assignedRoleNames, 
            actorRolesList));

         //used by PSExitAddPossibleTransitions
         assignedRoleNames.remove(TestRole.EI_ADMIN_MEMBERS.name());
         actorRoleIDList = PSWorkflowRoleInfoStatic.getActorRoles(contentid, src,
            PSAssignmentTypeHelperTest.ADHOC_USER_NORMAL, userRoleNames,
            connection, false);
         actorRolesList = PSWorkflowRoleInfoStatic.roleIDListToRoleNameList(
            actorRoleIDList, src);
         assertTrue(CollectionUtils.isEqualCollection(assignedRoleNames, 
            actorRolesList));

         assertEquals(PSAssignmentTypeEnum.ASSIGNEE.getValue(),
                 PSWorkflowRoleInfoStatic.getAssignmentType(src, actorRoleIDList));

         stateRoleIDNotificationList =
                 PSWorkflowRoleInfoStatic.getStateRoleIDNotificationList(src, contentid);
         System.out.println("\nstateRoleIDNotificationList = "
            + stateRoleIDNotificationList);

         stateRoleNameNotificationList =
                 PSWorkflowRoleInfoStatic.getStateRoleNameNotificationList(src, contentid);
         System.out.println("stateRoleNameNotificationList = "
            + stateRoleNameNotificationList);

         cauc = new PSContentAdhocUsersContext(contentid, connection);

         stateAdhocActorNotificationList =
                 PSWorkflowRoleInfoStatic.getStateAdhocActorNotificationList(
               cauc, src, contentid, requestCtx, false); // no role validation
         System.out.println("\nstateAdhocActorNotificationList = " +
            stateAdhocActorNotificationList);      

         contentid = PSAssignmentTypeHelperTest.COMM_CIDS[1];
         stateRoleIDNotificationList =
                 PSWorkflowRoleInfoStatic.getStateRoleIDNotificationList(src, contentid);
         System.out.println("\nstateRoleIDNotificationList = "
            + stateRoleIDNotificationList);

         stateRoleNameNotificationList =
                 PSWorkflowRoleInfoStatic.getStateRoleNameNotificationList(src, contentid);
         System.out.println("stateRoleNameNotificationList = "
            + stateRoleNameNotificationList);

         cauc = new PSContentAdhocUsersContext(contentid, connection);

         stateAdhocActorNotificationList =
                 PSWorkflowRoleInfoStatic.getStateAdhocActorNotificationList(
               cauc, src, contentid, requestCtx, false); // no role validation
         System.out.println("\nstateAdhocActorNotificationList = " +
            stateAdhocActorNotificationList);      

         PSAssignmentTypeHelperTest.updateComponentSummary(contentid, 
            WORKFLOW_ID, PSAssignmentTypeHelperTest.DRAFT_STATE_ID);
         src = new PSStateRolesContext(
            WORKFLOW_ID,
            connection,
            PSAssignmentTypeHelperTest.DRAFT_STATE_ID,
            PSWorkFlowUtils.ASSIGNMENT_TYPE_NOT_IN_WORKFLOW);

         actorRoleIDList.clear();
         actorRoleIDList.add(TestRole.EDITOR.getRoleId());      
         assertEquals(PSAssignmentTypeEnum.NONE.getValue(),
                 PSWorkflowRoleInfoStatic.getAssignmentType(src, actorRoleIDList));

         actorRoleIDList.add(TestRole.QA.getRoleId());
         assertEquals(PSAssignmentTypeEnum.READER.getValue(),
                 PSWorkflowRoleInfoStatic.getAssignmentType(src, actorRoleIDList));

         actorRoleIDList.add(TestRole.AUTHOR.getRoleId());      
         assertEquals(PSAssignmentTypeEnum.ASSIGNEE.getValue(),
                 PSWorkflowRoleInfoStatic.getAssignmentType(src, actorRoleIDList));

         actorRoleIDList.add(TestRole.ADMIN.getRoleId());      
         assertEquals(PSAssignmentTypeEnum.ADMIN.getValue(),
                 PSWorkflowRoleInfoStatic.getAssignmentType(src, actorRoleIDList));
      }
      finally
      {
         teardownInfo();
      }      
   }
   
   /**
    * Constant for the ID of the workflow used for all testing.
    */
   private static final int WORKFLOW_ID = PSAssignmentTypeHelperTest.TEST_WF_ID;
}
