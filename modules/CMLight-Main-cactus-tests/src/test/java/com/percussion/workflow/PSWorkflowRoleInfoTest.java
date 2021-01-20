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
   @SuppressWarnings("unchecked")
   public void testWorkflowRoleInfo() throws Exception
   {
      try
      {
         setupInfo();
         
         Connection connection = PSConnectionHelper.getDbConnection();

         PSStateRolesContext src = null;
         PSContentAdhocUsersContext cauc = null;
         List stateRoleIDNotificationList = null;
         List stateRoleNameNotificationList = null;
         List stateAdhocActorNotificationList = null;
         IPSRequestContext requestCtx = null;
         List actorRolesList = null;

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

         List<Integer> notifRoleIDs = new ArrayList<Integer>();
         notifRoleIDs.add(new Integer(TestRole.EDITOR.getRoleId()));
         notifRoleIDs.add(new Integer(TestRole.QA.getRoleId()));
         notifRoleIDs.add(new Integer(TestRole.EI_ADMIN_MEMBERS.getRoleId()));
         notifRoleIDs.add(new Integer(TestRole.EI_MEMBERS.getRoleId()));

         List<Integer> roleIDs = new ArrayList<Integer>();
         roleIDs.add(new Integer(TestRole.AUTHOR.getRoleId()));
         roleIDs.add(new Integer(TestRole.ADMIN.getRoleId()));
         roleIDs.addAll(notifRoleIDs);

         List rolesNotificationEnabled =
                 PSWorkflowRoleInfoStatic.filterRolesNotificationEnabled(
               roleIDs, src);
         assertTrue(CollectionUtils.isEqualCollection(notifRoleIDs, 
            rolesNotificationEnabled));
         assertFalse(CollectionUtils.isEqualCollection(roleIDs, 
            rolesNotificationEnabled));

         //used by PSExitAuthenticateUser
         List<String> assignedRoleNames = new ArrayList<String>();
         assignedRoleNames.add(TestRole.EDITOR.name());
         assignedRoleNames.add(TestRole.QA.name());
         assignedRoleNames.add(TestRole.EI_ADMIN_MEMBERS.name());
         assignedRoleNames.add(TestRole.AUTHOR.name());
         List<String> memberRoleNames = new ArrayList<String>();
         memberRoleNames.addAll(assignedRoleNames);
         memberRoleNames.add(TestRole.CI_MEMBERS.name());
         memberRoleNames.add(TestRole.ADMIN.name());

         String userRoleNames = PSWorkFlowUtils.listToDelimitedString(
            memberRoleNames, ",");
         List actorRoleIDList = PSWorkflowRoleInfoStatic.getActorRoles(contentid, src,
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
