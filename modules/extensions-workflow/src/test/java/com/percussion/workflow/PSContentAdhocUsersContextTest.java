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

import com.percussion.error.PSExceptionUtils;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 * The PSContentAdhocUsersContextTest class is a test class for the class
 * PSContentAdhocUsersContext.
 */
@Category(IntegrationTest.class)
public class PSContentAdhocUsersContextTest extends PSAbstractWorkflowTest
{

   private static final Logger log = LogManager.getLogger(PSContentAdhocUsersContextTest.class);

   /**
    * Constructor specifying command line arguments
    *
    * @param args   command line arguments - see  {@link #HelpMessage}
    *               for options.
    */
   public PSContentAdhocUsersContextTest (String[] args)
   {
      m_sArgs = args;
   }

/* IMPLEMENTATION OF METHODS FROM CLASS PSAbstractWorkflowTest  */

   public void ExecuteTest(Connection connection)
      throws PSWorkflowTestException
   {
      log.info("\nExecuting test of PSContentAdhocUsersContext\n");
      Exception except = null;
      String exceptionMessage = "";
      int workflowID = 1;
      int contentID = 302;
      int newContentID = 305;

      PSContentAdhocUsersContext context = null;
      int minAssignmentType = PSWorkFlowUtils.ASSIGNMENT_TYPE_ASSIGNEE;
      int assignmentType = 0;
      String user = "";
      HashMap assnMap = null;
      PSContentAdhocUsersContext cauc =
            new PSContentAdhocUsersContext(newContentID);
      int GERTRUDE_ADHOC_NORMAL_STATE_ROLEID_ARRAY[]  = {2, 3};

      int SHANIA_ADHOC_NORMAL_STATE_ROLEID_ARRAY[] = {1, 3};
      String ADHOC_ANONYMOUS_USER_NAMES_ARRAY[] = {"Yves", "Alice", "Malcolm"};
      int ADHOC_ANONYMOUS_USER_ROLEID_ARRAY[] = {4, 7};
      List adhocAnonymousUserNames =
            PSWorkFlowUtils.arrayToList(ADHOC_ANONYMOUS_USER_NAMES_ARRAY);


      List userAdhocAnonymousRoles =
            PSWorkFlowUtils.arrayToList(ADHOC_ANONYMOUS_USER_ROLEID_ARRAY);
      int ADHOC_TEST_ARRAY[] = {1, 2, 3, 4, 5, 6, 7};
      List adhocTestList = PSWorkFlowUtils.arrayToList(ADHOC_TEST_ARRAY);

      int  recordCount = 0;

      try
      {
         context = new PSContentAdhocUsersContext(contentID,
                                                  connection);
         log.info("context = {}", context);


         recordCount = cauc.emptyAdhocUserEntries(connection);
         log.info("records deleted = {}", recordCount);

          cauc =
            new PSContentAdhocUsersContext(newContentID);

         cauc.addUserAdhocNormalRoleIDs(
            "Gertrude",
            PSWorkFlowUtils.arrayToList(
               GERTRUDE_ADHOC_NORMAL_STATE_ROLEID_ARRAY));
         cauc.addUserAdhocNormalRoleIDs(
            "Shania",
            PSWorkFlowUtils.arrayToList(
               SHANIA_ADHOC_NORMAL_STATE_ROLEID_ARRAY));
         cauc.setAdhocAnonymousUsersAndRoles(adhocAnonymousUserNames,
                                             userAdhocAnonymousRoles);
         recordCount = cauc.commit(connection);
         log.info("records inserted = {}", recordCount);

         cauc = null;

         cauc = new PSContentAdhocUsersContext(newContentID,
                                               connection);
         log.info("cauc = {}", cauc);
         log.info("State 1 has adhoc users = {}", cauc.hasAdhocUsers(1));
         log.info("State 6 has adhoc users = {}", cauc.hasAdhocUsers(6));
         log.info("Empty roles in the list {} are {}", adhocTestList, cauc.getEmptyAdhocRoles(adhocTestList));
      }
      catch (SQLException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         exceptionMessage = "SQL exception: ";
         except = e;
      }
      catch (PSRoleException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         exceptionMessage = "Role exception ";
         except = e;
      }
      finally
      {
         log.info("\nEnd test of PSContentAdhocUsersContext\n");
         if (null != except)
         {
            throw new PSWorkflowTestException(exceptionMessage,
                                              except);
         }
      }
   }
   public static void main(String[] args)
   {
      PSContentAdhocUsersContextTest wfTest =
            new PSContentAdhocUsersContextTest(args);
      wfTest.Test();
   }
}
