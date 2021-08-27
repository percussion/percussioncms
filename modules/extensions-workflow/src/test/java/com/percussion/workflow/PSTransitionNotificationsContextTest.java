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
package com.percussion.workflow;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;

import java.sql.Connection;

/**
 * The PSTransitionNotificationsContextTestclass is a test class for the class
 * PSTransitionNotificationsContext. 
 */
@Category(IntegrationTest.class)
public class PSTransitionNotificationsContextTest
   extends PSAbstractWorkflowTest 
{

   private static final Logger log = LogManager.getLogger(PSTransitionNotificationsContextTest.class);

   /**
    * Constructor specifying command line arguments
    *
    * @param args   command line arguments - see  {@link #HelpMessage}
    *               for options.
    */
   public PSTransitionNotificationsContextTest (String[] args)
   {
      m_sArgs = args;
   }
   
/* IMPLEMENTATION OF METHODS FROM CLASS PSAbstractWorkflowTest  */   
   
   public void ExecuteTest(Connection connection)
      throws PSWorkflowTestException
   {
      log.info("\nExecuting test of PSTransitionNotificationsContext"
                         + "\n");
      Exception except = null;
      String exceptionMessage = "";
      PSTransitionNotificationsContext context = null;
      
      int notificationID = 0;
      int stateRoleRecipientTypes = 0;
      String additionalRecipientList = "";
      String CCList = "";
      
      try
      {
         context =
               new PSTransitionNotificationsContext(m_nWorkflowID,
                                                    m_nTransitionID,
                                                    connection);
         log.info(
            "Notification count for TransitionID {}, workflowID {} is {}", m_nWorkflowID, m_nTransitionID, context.getCount()
            + "\n");
         
         do
         {
            notificationID = context.getNotificationID();
            log.info("notificationID = {}", notificationID);
         
            stateRoleRecipientTypes = context.getStateRoleRecipientTypes();
            log.info("   stateRoleRecipientTypes = {}", stateRoleRecipientTypes);
         
            additionalRecipientList = context.getAdditionalRecipientList();
            log.info("   additionalRecipientList = {}", additionalRecipientList);
         
            CCList = context.getCCList();
            log.info("   CCList = {} \n", CCList);
         }
         while (context.moveNext());
      }
      catch (Exception e) 
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(),e);
         exceptionMessage = "Exception: ";
         except = e;
      }
      finally 
      {
      log.info("\nEnd test of PSTransitionNotificationsContext"
                         + "\n");
         if (null != except) 
         {
            throw new PSWorkflowTestException(exceptionMessage,
                                              except);
         }
      }      
   }
   
   public int GetArgValues(int i)
   {
      if (m_sArgs[i].equals("-w") || m_sArgs[i].equals("-workflowid"))
      {
         m_nWorkflowID =  Integer.parseInt(m_sArgs[++i]);
      }

      if (m_sArgs[i].equals("-t") || m_sArgs[i].equals("-transitionid"))
      {
         m_nTransitionID =  Integer.parseInt(m_sArgs[++i]);
      }
      return i;
   }
   
   public String HelpMessage()
   {
      StringBuilder buf = new StringBuilder();
      buf.append("Options are:\n");
      buf.append("   -w, -workflowid        workflow ID\n");
      buf.append("   -t, -transitionid      transition ID\n");
      buf.append("   -h, -help              help\n");
      return buf.toString();
   }
   
   public static void main(String[] args)
   {
      PSTransitionNotificationsContextTest wfTest =
            new PSTransitionNotificationsContextTest(args);
      wfTest.Test();
   }
   private int m_nWorkflowID = 1;
   private int m_nTransitionID = 27;  
}
