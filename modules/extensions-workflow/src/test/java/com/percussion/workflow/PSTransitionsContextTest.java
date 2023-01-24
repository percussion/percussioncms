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
 * The PSTransitionsContextTest class is a test class for the class
 * PSTransitionsContext. 
 */
@Category(IntegrationTest.class)
public class PSTransitionsContextTest extends PSAbstractWorkflowTest 
{

   private static final Logger log = LogManager.getLogger(PSTransitionsContextTest.class);

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
      log.info("\nExecuting test of PSTransitionsContext\n");
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
                     log.info(context.toString(true));
                  }
                  else 
                  {
                     log.info("Not an aging transition. ID = {}", m_nTransitionID);
                  }               }
               else 
               {
                  log.info(context.toString());
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
            log.info(
               "Number of transitions from state {} is {}.\n", m_nStateID, context.getTransitionCount());
            do
            {
               if (m_bAgingOnly) 
               {
                  if (!context.isAgingTransition()) 
                  {
                     continue;
                  }
                  
                  log.info(context.toString(true));
               }
               else 
               {
                  log.info(context.toString());
               }
            }
            while (context.moveNext());
            context.close();
         }
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
         log.info("\nEnd test of PSTransitionsContext\n");
         if (null != except) 
         {
            throw new PSWorkflowTestException(exceptionMessage,
                                              except);
         }
      }      
   }

   public int GetArgValues(int i)
   {
      log.info("GetArgValues");
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
