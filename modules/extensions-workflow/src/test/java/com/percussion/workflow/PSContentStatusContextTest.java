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
      System.out.println("Entering Method ExecuteTest");
      Exception except = null;
      String exceptionMessage = "";
      PSContentStatusContext context = null;
      
      try
      {
         System.out.println("contentID = " + m_nContentID);
         PSContentStatusContext
               csc = new PSContentStatusContext(connection, m_nContentID);
         System.out.println(csc.toString(true));
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
         
         // System.out.println(csc.toString(true));     
         csc.close(); //release the JDBC resources
         csc = null;
         csc = new PSContentStatusContext(connection, m_nContentID);
         
         csc.close(); //release the JDBC resources
         
         System.out.println(csc.toString(true));   
      }
      catch (SQLException e) 
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
         System.out.println("Exiting Method ExecuteTest");
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
      StringBuffer buf = new StringBuffer();
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
