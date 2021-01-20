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


import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.testing.IntegrationTest;
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
      System.out.println(
         "\nExecuting test of PSContentStatusHistoryContext\n");
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
         System.out.println("Test read of PSContentStatusHistoryContext");
         
         System.out.println("contentID = " + contentID);
         cshc = new PSContentStatusHistoryContext(workflowID,
                                                  connection,
                                                  contentID);
         cshc.close(); //release the JDBC resources
         System.out.println(cshc.toString());

         System.out.println("Test creation of PSContentStatusHistoryContext");
         csc = new PSContentStatusContext(connection, contentID);
         System.out.println("csc = " + csc);
         contentLastModifiedDate = csc.getContentLastModifiedDate();

         calender.setTime(contentLastModifiedDate);
           
         System.out.println("StatusContext contentLastModifiedDate = " +
                            DateString(contentLastModifiedDate));
         System.out.println("StatusContext contentLastModifiedDate = " +
                            contentLastModifiedDate);           
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
               System.out.println("Transition context not found.");
               throw  e;
            }
         }

         Integer temp = getNextNumber("CONTENTSTATUSHISTORY",
                                      connection);
         int contentstatushistoryid = temp.intValue();
         System.out.println("contentstatushistoryid = " + contentstatushistoryid);
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
            System.out.println("cshc = " + cshc);
           
            System.out.println("cshc = " + cshc.toString());
         } catch (IllegalArgumentException e) 
         {
            System.out.println("error creating PSContentStatusHistoryContext");
            throw  e;
         }      
        
         contentLastModifiedDate = cshc.getContentLastModifiedDate();
        
         System.out.println("HistoryContext contentLastModifiedDate = " +
                            DateString(contentLastModifiedDate));
        
         
         eventTime = cshc.getEventTime();

         System.out.println("HistoryContext eventTime = " +
                            eventTime);
        
      } catch (SQLException e) 
      {
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
         System.out.println("\nEnd test of PSContentStatusHistoryContext\n");
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
