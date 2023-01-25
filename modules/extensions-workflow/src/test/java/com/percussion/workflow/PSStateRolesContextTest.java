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

/**
 * The PSStateRolesContextTest class is a test class for the class
 * StateRolesContext.
 */
@Category(IntegrationTest.class)
public class PSStateRolesContextTest extends PSAbstractWorkflowTest 
{

   private static final Logger log = LogManager.getLogger(PSStateRolesContextTest.class);

   public PSStateRolesContextTest (String[] args)
   {
      
   }
   
   public void ExecuteTest(Connection connection)
      throws PSWorkflowTestException
   {
      log.info("Entering Method ExecuteTest");
      Exception except = null;
      String exceptionMessage = "";
      PSStateRolesContext context = null;
      int workflowID = 1;
      int stateID = 3;
      int assignmentType = 0;
      
      try
      {
         context = new PSStateRolesContext(workflowID,
                                           connection,
                                           stateID,
                                           assignmentType);
         log.info("context = {}", context);
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
         exceptionMessage = "Role exception";
         except = e;
      }
      catch(PSEntryNotFoundException e)
      {
         log.info("State roles context not found.");
         except = e;
      }
      finally 
      {
         log.info("Exiting Method ExecuteTest");
         if (null != except) 
         {
            throw new PSWorkflowTestException(exceptionMessage,
                                              except);
         }
      }      
   }
   public static void main(String[] args)
   {
      PSStateRolesContextTest wfTest = new PSStateRolesContextTest(args);
      wfTest.Test();
   }
}
