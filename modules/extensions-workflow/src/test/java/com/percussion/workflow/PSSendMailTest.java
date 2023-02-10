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

import com.percussion.utils.testing.IntegrationTest;
import com.percussion.workflow.model.PSMessagePackage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;


/**
 * PSSendMailTest is a test class for the method
 * PSExitNotifyAssignees.sendMail.
 */
@Category(IntegrationTest.class)
public class PSSendMailTest extends PSAbstractWorkflowTest 
{
   private static final Logger log = LogManager.getLogger(PSSendMailTest.class);
   /**
    * Constructor specifying command line arguments
    *
    * @param args   command line arguments - see  {@link #HelpMessage}
    *               for options.
    */   public PSSendMailTest (String[] args)
   {
      m_sArgs = args;
      m_bNeedConnection = false;
   }
/* IMPLEMENTATION OF METHODS FROM CLASS PSAbstractWorkflowTest  */
  public void ExecuteTest(Connection connection)
      throws PSWorkflowTestException
   {
      log.info("\nExecuting test of "
                         + "PSExitNotifyAssignees.sendMail\n");
      Exception except = null;
      String exceptionMessage = "";
      String msgFrom = "aaron_brandes";
      String msgTo = "admin1";
      String msgCc = null;       
      String mailSubject = "Important fewer mail for you";
      String mailBody = "Not kidding.";
      String mailURL = "www.percussion.com";

      // Parse the arguments if any
      if (null != m_sArgs) 
      { 
         for (int i = 0; i < m_sArgs.length; i++)
         {
            if (m_sArgs[i].equals("-f") || m_sArgs[i].equals("-from"))
            {
               msgFrom =  m_sArgs[++i];
            }
         
            if (m_sArgs[i].equals("-t") || m_sArgs[i].equals("-to"))
            {
               msgTo =  m_sArgs[++i];
            }

            if (m_sArgs[i].equals("-c") || m_sArgs[i].equals("-cc"))
            {
               msgCc =  m_sArgs[++i];
            }         
            if (m_sArgs[i].equals("-s") || m_sArgs[i].equals("-subject"))
            {
               mailSubject =  m_sArgs[++i];
            }
         
            if (m_sArgs[i].equals("-b") || m_sArgs[i].equals("-body"))
            {
               mailBody =  m_sArgs[++i];
            }

         
            if (m_sArgs[i].equals("-u") || m_sArgs[i].equals("-url"))
            {
               mailURL =  m_sArgs[++i];
            }
         
            if (m_sArgs[i].equals("-h") || m_sArgs[i].equals("-help"))
            {         
               log.info("Options are:");
               log.info("   -f, -from      MessageFrom");
               log.info("   -t, -to        MessageTo");
               log.info("   -c, -cc        MessageCc");
               log.info("   -s, -subject   MailSubject");
               log.info("   -b, -body      MailBody");
               log.info("   -u, -url       MailURL");
               log.info("   -h, -help      help");
               return;
            }          
         } // End for (int i = 0; i < m_sArgs.length; i++)
      } // End if (null != m_sArgs)    

      List<PSMessagePackage> mailPkgs = new ArrayList<>();

      PSMessagePackage mailPkg = new PSMessagePackage();
      mailPkg.setEmailBody(mailBody);
      mailPkg.setEmailToStr(msgTo);
      mailPkg.setSubj(mailSubject);
      mailPkg.setUserEmail(msgFrom);

      mailPkgs.add(mailPkg);

      try
      {
         PSExitNotifyAssignees.sendMail(mailPkgs);

      }/*
      catch (PSMailException e)
      {
         exceptionMessage = "Error Sending Mail";
         except = e;
      }*/
      finally 
      {
         log.info("\nEnd test of PSExitNotifyAssignees.sendMail\n");
         if (null != except) 
         {
            throw new PSWorkflowTestException(exceptionMessage,
                                              except);
         }
      }      
   }

   public static void main(String[] args)
   {
      PSSendMailTest wfTest = new PSSendMailTest(args);
      wfTest.Test();
   }   
   
}// PSSendMailTest
