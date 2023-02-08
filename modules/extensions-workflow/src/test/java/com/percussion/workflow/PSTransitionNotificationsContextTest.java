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
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
