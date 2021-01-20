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
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.sql.Connection;

/**
 * PSSendNotificationsTest is a test class for the method
 * PSExitNotifyAssignees.sendNotifications.
 */
@Category(IntegrationTest.class)
public class PSSendNotificationsTest extends PSAbstractWorkflowTest 
{
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
      System.out.println("\nExecuting test of " +
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
         System.out.println("\nEnd test of sendNotifications\n");
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
