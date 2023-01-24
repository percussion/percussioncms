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
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * The PSContentStatusContextTest class is a test class for the class
 * PSContentStatusContext. 
 */
@Category(IntegrationTest.class)
public class PSContentStatusContextTest extends PSAbstractWorkflowTest 
{

   private static final Logger log = LogManager.getLogger(PSContentStatusContextTest.class);

   /**
    * Constructor specifying command line arguments
    *
    * @param args   command line arguments - see  {@link #HelpMessage}
    *               for options.
    */   
   public PSContentStatusContextTest (String[] args)
   {
     m_sArgs = args;      
   }
   
  /* IMPLEMENTATION OF METHODS FROM CLASS PSAbstractWorkflowTest  */
   
   public void ExecuteTest(Connection connection)
      throws PSWorkflowTestException
   {
      log.info("Entering Method ExecuteTest");
      Exception except = null;
      String exceptionMessage = "";
      PSContentStatusContext context = null;
      
      try
      {
         log.info("contentID = {}", m_nContentID);
         PSContentStatusContext
               csc = new PSContentStatusContext(connection, m_nContentID);
         log.info(csc.toString(true));
         csc.setContentStateID(m_nStateID);
         csc.setContentCheckedOutUserName("");
         csc.setEditRevision(-1);
         Date now = new Date(new java.util.Date().getTime());
         Date startDate = null;
      
         Calendar workingVarCal =  Calendar.getInstance();
         workingVarCal = 
            PSWorkFlowUtils.incrementCalendar(workingVarCal,
                                              now,
                                              -60 * m_nSetBackHours);
         startDate = PSWorkFlowUtils.sqlDateFromCalendar(workingVarCal);
         csc.setStateEnteredDate(startDate);
         csc.setNextAgingDate(startDate);
         csc.setRepeatedAgingTransitionStartDate(startDate);
         csc.setNextAgingTransition(m_nTransitionID);

         csc.setStartDate(startDate);      

         csc.setReminderDate(PSWorkFlowUtils.sqlDateFromCalendar(
            PSWorkFlowUtils.incrementCalendar(workingVarCal,
                                              startDate,
                                              m_nReminderOffset)));

         csc.setExpiryDate(now);      

         csc.commit(connection);

         csc.close(); //release the JDBC resources
         csc = null;
         csc = new PSContentStatusContext(connection, m_nContentID);
         
         csc.close(); //release the JDBC resources
         
         log.info(csc.toString(true));
      }
      catch (SQLException e) 
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         exceptionMessage = "SQL exception: ";
         except = e;
      }
      catch (PSEntryNotFoundException e) 
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         exceptionMessage = "Entry not found";
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
   
   public int GetArgValues(int i)
   {
      
      if (m_sArgs[i].equals("-w") || m_sArgs[i].equals("-workflowid"))
      {
         m_nWorkflowID =  Integer.parseInt(m_sArgs[++i]);
      }
      
      if (m_sArgs[i].equals("-c") || m_sArgs[i].equals("-contentid"))
      {
         m_nContentID =  Integer.parseInt(m_sArgs[++i]);
      }
      
      if (m_sArgs[i].equals("-n") ||
          m_sArgs[i].equals("-nextagingtransition"))
      {
         m_nTransitionID =  Integer.parseInt(m_sArgs[++i]);
      }
      
      if (m_sArgs[i].equals("-s") || m_sArgs[i].equals("-stateid"))
      {
         m_nStateID =  Integer.parseInt(m_sArgs[++i]);
      }
      
      if (m_sArgs[i].equals("-b") || m_sArgs[i].equals("-setbackhours"))
      {
         m_nSetBackHours =  Integer.parseInt(m_sArgs[++i]);
      }

            
      if (m_sArgs[i].equals("-r") || m_sArgs[i].equals("-reminderoffset"))
      {
         m_nReminderOffset =  Integer.parseInt(m_sArgs[++i]);
      }
      
      if (m_sArgs[i].equals("-t") ||
          m_sArgs[i].equals("-tiprevision"))
      {
         m_nTipRevision =  Integer.parseInt(m_sArgs[++i]);
      }
      
      if (m_sArgs[i].equals("-u") || m_sArgs[i].equals("-username"))
      {
         m_sUserName =  m_sArgs[++i];
      }
      return i;
   }

   public String HelpMessage()
   {
      StringBuilder buf = new StringBuilder();
      buf.append("Options are:");
      buf.append("   -w, -workflowid        workflow ID");
      buf.append("   -c, -contentid         content ID");
      buf.append("   -n, -nextagingtransition next aging transition ID");
      buf.append("   -s, -stateid           initial state ID");
      buf.append("   -b, -setbackhours      set back in hours");
      buf.append("   -r, -reminderoffset    reminder offset in minutes");
      buf.append("   -t, -tiprevision       tip revision");
      buf.append("   -u, -username          checkout user name");
      buf.append("   -h, -help              help");
      return buf.toString();
   }

   public static void main(String[] args)
   {
      PSContentStatusContextTest wfTest =
            new PSContentStatusContextTest(args);
      wfTest.Test();
   }
   private int m_nWorkflowID = 1;
   private int m_nContentID = 302; 
   private int m_nTransitionID = 23;  
   private int m_nStateID = 1;
   private int m_nSetBackHours = 2;
   private int m_nReminderOffset = 50;
   private String m_sUserName = "";
   private int m_nTipRevision = 1;
}
