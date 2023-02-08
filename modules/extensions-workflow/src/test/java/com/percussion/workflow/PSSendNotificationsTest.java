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
import com.percussion.utils.testing.IntegrationTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;

import java.sql.Connection;

/**
 * PSSendNotificationsTest is a test class for the method
 * PSExitNotifyAssignees.sendNotifications.
 */
@Category(IntegrationTest.class)
public class PSSendNotificationsTest extends PSAbstractWorkflowTest 
{

   private static final Logger log = LogManager.getLogger(PSSendNotificationsTest.class);

   /**
    * Constructor specifying command line arguments
    *
    * @param args   command line arguments - ignored
    */   public PSSendNotificationsTest (String[] args)
   {
      m_sArgs = args;
   }
   
/* IMPLEMENTATION OF METHODS FROM CLASS PSAbstractWorkflowTest  */   
   public void ExecuteTest(Connection connection)
      throws PSWorkflowTestException
   {
      log.info("\nExecuting test of " +
                         "PSExitNotifyAssignees.sendNotifications\n");
      Exception except = null;
      String exceptionMessage = "";
      int workflowID = 1;
      int transitionID = 1;
      int oldStateID = 3;
      int newStateID = 2;
      String userName = "Admin1";
      IPSRequestContext request = null;
      IWorkflowRoleInfo wfRoleInfo = null;
      String contentURL = null;
      String communityId = null;
      try 
      {
         PSExitNotifyAssignees.sendNotifications(-1, -1,
                                                 contentURL,
                                                 workflowID,
                                                 transitionID,
                                                 oldStateID,
                                                 newStateID,
                                                 userName,
                                                 wfRoleInfo,
                                                 request,
                                                 connection, communityId);
      }
      catch (Exception e) 
      {
         exceptionMessage = "Exception: ";
         except = e;
      }
      finally 
      {
         log.info("\nEnd test of sendNotifications\n");
         if (null != except) 
         {
            throw new PSWorkflowTestException(exceptionMessage,
                                              except);
         }
      }      
   }

   public static void main(String[] args)
   {
      PSSendNotificationsTest wfTest = new PSSendNotificationsTest(args);
      wfTest.Test();
   }
}
