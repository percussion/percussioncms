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
import java.sql.SQLException;

/**
 * The PSTransitionsContextTest class is a test class for the class
 * PSTransitionsContext. 
 */
@Category(IntegrationTest.class)
public class PSTransitionsContextTest extends PSAbstractWorkflowTest 
{
   /**
    * Constructor specifying command line arguments
    *
    * @param args   command line arguments - see  {@link #HelpMessage}
    *               for options.
    */
   public PSTransitionsContextTest (String[] args)
   {
      m_sArgs = args;  
   }
   
   public void ExecuteTest(Connection connection)
      throws PSWorkflowTestException
   {
      System.out.println("\nExecuting test of PSTransitionsContext\n");
      Exception except = null;
      String exceptionMessage = "";
      PSTransitionsContext context = null;

      try
      {
         if (m_bDoTransition) 
         {
            context =  new PSTransitionsContext(m_nTransitionID,
                                                m_nWorkflowID, 
                                                connection);
            context.close();
            if (m_bAgingOnly) 
               {
                  if (context.isAgingTransition()) 
                  {
                     System.out.println(context.toString(true));
                  }
                  else 
                  {
                     System.out.println("Not an aging transition. ID = " +
                                        m_nTransitionID);
                  }               }
               else 
               {
                  System.out.println(context.toString());
               }
         }

         /*
          * Print state info if it was requested, or no transition was
          * specified
          */
         if (m_bDoState || !m_bDoTransition) 
         {
         
            context =  new PSTransitionsContext(m_nWorkflowID,
                                                connection,
                                                m_nStateID);
            System.out.println(
               "Number of transitions from state " + m_nStateID + 
               " is " + context.getTransitionCount() + ".\n");
            do
            {
               if (m_bAgingOnly) 
               {
                  if (!context.isAgingTransition()) 
                  {
                     continue;
                  }
                  
                  System.out.println(context.toString(true));
               }
               else 
               {
                  System.out.println(context.toString());
               }
            }
            while (context.moveNext());
            context.close();
         }
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
         System.out.println("\nEnd test of PSTransitionsContext\n");
         if (null != except) 
         {
            throw new PSWorkflowTestException(exceptionMessage,
                                              except);
         }
      }      
   }

   public int GetArgValues(int i)
   {
      System.out.println("GetArgValues");
      if (m_sArgs[i].equals("-w") || m_sArgs[i].equals("-workflowid"))
      {
         m_nWorkflowID =  Integer.parseInt(m_sArgs[++i]);
      }
 
      if (m_sArgs[i].equals("-t") || m_sArgs[i].equals("-transitionid"))
      {
         m_nTransitionID =  Integer.parseInt(m_sArgs[++i]);
         m_bDoTransition = true;
      }
      
      if (m_sArgs[i].equals("-s") || m_sArgs[i].equals("-stateid"))
      {
         m_nStateID =  Integer.parseInt(m_sArgs[++i]);
         m_bDoState = true;
      }
      if (m_sArgs[i].equals("-a") || m_sArgs[i].equals("-agingonly"))
      {
         m_bAgingOnly = true;
      }
      return i;
   }      


   public String HelpMessage()
   {
      StringBuilder buf = new StringBuilder();
      buf.append("Options are:\n");
      buf.append("   -w, -workflowid        workflow ID\n");
      buf.append("   -t, -transitionid      transition ID\n");
      buf.append("   -s, -stateid           from state ID\n");
      buf.append("   -a  -agingonly         only print aging transition info"
                 + "\n");
      buf.append("   -h, -help          help\n");
      return buf.toString();
   }
      
   public static void main(String[] args)
   {
      PSTransitionsContextTest wfTest = new PSTransitionsContextTest(args);
      wfTest.Test();
   }
   private int m_nWorkflowID = 1;
   private int m_nTransitionID = 23;  
   private int m_nStateID = 1;
   private boolean m_bAgingOnly = false;
   private boolean m_bDoState = false;
   private boolean m_bDoTransition = false;
}
