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
 * PSNotificationsContextTest is a test class for the class
 * PSNotificationsContext 
 */
@Category(IntegrationTest.class)
public class PSNotificationsContextTest extends PSAbstractWorkflowTest 
{

   private static final Logger log = LogManager.getLogger(PSNotificationsContextTest.class);

   /**
    * Constructor specifying command line arguments
    *
    * @param args   command line arguments - see  {@link #HelpMessage}
    *               for options.
    */   public PSNotificationsContextTest (String[] args)
   {
      m_sArgs = args;
   }

/* IMPLEMENTATION OF METHODS FROM CLASS PSAbstractWorkflowTest  */   
   public void ExecuteTest(Connection connection)
      throws PSWorkflowTestException
   {
      log.info("\nExecuting test of PSNotificationsContext\n");
      Exception except = null;
      String exceptionMessage = "";
      int workflowID = 1;
      int notificationID =1;
      String subject = "";
      String body = "";
      
      PSNotificationsContext context = null;
      
      try 
      {
           context = new PSNotificationsContext(workflowID,
                                                notificationID,
                                                connection);
           subject = context.getSubject();
           log.info("subject = {}", subject);
           body = context.getBody();
           log.info("body = {}", body);
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
         log.info("\nEnd test of PSNotificationsContext\n");
         if (null != except) 
         {
            throw new PSWorkflowTestException(exceptionMessage,
                                              except);
         }
      }      
   }

   public static void main(String[] args)
   {
      PSNotificationsContextTest wfTest =
            new PSNotificationsContextTest(args);
      wfTest.Test();
   }
}
