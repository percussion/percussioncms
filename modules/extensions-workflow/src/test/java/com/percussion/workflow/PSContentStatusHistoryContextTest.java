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
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * The PSContentStatusHistoryContextTest class is a test class for the class
 *  PSContentStatusHistoryContext
 */
@Category(IntegrationTest.class)
public class PSContentStatusHistoryContextTest extends PSAbstractWorkflowTest 
{

   private static final Logger log = LogManager.getLogger(PSContentStatusHistoryContextTest.class);

   /**
    * Constructor specifying command line arguments
    *
    * @param args   command line arguments.
    */   
   public PSContentStatusHistoryContextTest (String[] args)
   {
      m_sArgs = args;
   }

   /* IMPLEMENTATION OF METHODS FROM CLASS PSAbstractWorkflowTest  */
      @Override
      public void ExecuteTest(Connection connection)
      throws PSWorkflowTestException
   {
      log.info("\nExecuting test of PSContentStatusHistoryContext\n");
      Exception except = null;
      String exceptionMessage = "";
      int workflowID = 1;
      int contentID = 302;
      int transitionID = 3;
      int baseRevisionNum = 1;
      String userName = "Aaron";
      String sessionID = "mysession01";
      String  transitionComment = "";
      PSContentStatusContext csc = null;
      Date contentLastModifiedDate = null;
      Date eventTime = null;
      PSContentStatusHistoryContext cshc = null;
      Calendar calender = Calendar.getInstance();
      String  stateAssignedRole = "TestProgram";
      try 
      {
         log.info("Test read of PSContentStatusHistoryContext");
         
         log.info("contentID = {}", contentID);
         cshc = new PSContentStatusHistoryContext(workflowID,
                                                  connection,
                                                  contentID);
         cshc.close(); //release the JDBC resources
         log.info(cshc.toString());

         log.info("Test creation of PSContentStatusHistoryContext");
         csc = new PSContentStatusContext(connection, contentID);
         log.info("csc = {}", csc);
         contentLastModifiedDate = csc.getContentLastModifiedDate();

         calender.setTime(contentLastModifiedDate);
           
         log.info("StatusContext contentLastModifiedDate = {}", DateString(contentLastModifiedDate));
         log.info("StatusContext contentLastModifiedDate = {}", contentLastModifiedDate);
         csc.close(); //release the JDBC resources

         int nWorkFlowAppID = csc.getWorkflowID();
        
         IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
         PSStatesContext sc = (PSStatesContext) 
            cms.loadWorkflowState(nWorkFlowAppID, csc.getContentStateID());
                    
         // if it's not a checkin or checkout, get the transition context
         PSTransitionsContext tc = null;
         if (0 != transitionID) 
         {      
            try
            {
               tc = new PSTransitionsContext(transitionID,
                                             nWorkFlowAppID,
                                             connection);
            }
            catch(PSEntryNotFoundException e)
            {
               log.error(PSExceptionUtils.getMessageForLog(e));
               log.debug(PSExceptionUtils.getDebugMessageForLog(e));
               log.info("Transition context not found.");
               throw  e;
            }
         }

         Integer temp = getNextNumber("CONTENTSTATUSHISTORY",
                                      connection);
         int contentstatushistoryid = temp.intValue();
         log.info("contentstatushistoryid = {}", contentstatushistoryid);
         cshc= null;
        
         try 
         {
            cshc =
                  new PSContentStatusHistoryContext(contentstatushistoryid,
                                                    nWorkFlowAppID,
                                                    connection,
                                                    contentID,
                                                    csc,
                                                    sc,
                                                    tc,
                                                    transitionComment,
                                                    userName,
                                                    sessionID,
                                                    stateAssignedRole,
                                                    baseRevisionNum);
            log.info("cshc = {}", cshc);
           
            log.info("cshc = {}", cshc.toString());
         } catch (IllegalArgumentException e) 
         {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            log.info("error creating PSContentStatusHistoryContext");
            throw  e;
         }      
        
         contentLastModifiedDate = cshc.getContentLastModifiedDate();
        
         log.info("HistoryContext contentLastModifiedDate = {}", DateString(contentLastModifiedDate));
        
         
         eventTime = cshc.getEventTime();

         log.info("HistoryContext eventTime = {}", eventTime);
        
      } catch (SQLException e) 
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         exceptionMessage = "SQL exception: ";
         except = e;
      }
      catch (PSEntryNotFoundException e) 
      {
         exceptionMessage = "Entry not found";
         except = e;
      }
      finally 
      {
         log.info("\nEnd test of PSContentStatusHistoryContext\n");
         if (null != except) 
         {
            throw new PSWorkflowTestException(exceptionMessage,
                                              except);
         }
      }      
   }

   public static void main(String[] args)
   {
      PSContentStatusHistoryContextTest wfTest =
            new PSContentStatusHistoryContextTest(args);
      wfTest.Test();
   }
}// PSContentStatusHistoryContextTest
