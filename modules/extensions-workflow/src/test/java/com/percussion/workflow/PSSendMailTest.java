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
