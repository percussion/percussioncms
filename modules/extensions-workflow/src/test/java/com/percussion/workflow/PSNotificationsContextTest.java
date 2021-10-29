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
