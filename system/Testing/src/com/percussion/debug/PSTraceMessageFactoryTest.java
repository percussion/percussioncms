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

package com.percussion.debug;

import com.percussion.design.objectstore.PSTraceOption;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *   Unit tests for the PSTraceMessageFactoryTest class
 */
public class PSTraceMessageFactoryTest extends TestCase
{
   public PSTraceMessageFactoryTest(String name)
   {
      super(name);
   }

   public void testMessages() throws Exception
   {
      // create each message from its flag and be sure it's the correct type
      ArrayList list = PSTraceMessageFactory.getPossibleOptions();
      Iterator options = list.iterator();
      PSTraceOption option;

      while (options.hasNext())
      {
         option = (PSTraceOption)options.next();
         int flag = option.getFlag();
         IPSTraceMessage message =
            (IPSTraceMessage)PSTraceMessageFactory.getTraceMessage(flag);
            
         assertEquals(message.getTypeFlag(), flag);

         switch(flag)
         {
            case PSTraceMessageFactory.APP_HANDLER_PROC_FLAG:
               assertTrue(message instanceof PSTraceAppHandlerProc);
               break;
            case PSTraceMessageFactory.APP_SECURITY_FLAG:
               assertTrue(message instanceof PSTraceAppSecurity);
               break;
            case PSTraceMessageFactory.BASIC_REQUEST_INFO_FLAG:
               assertTrue(message instanceof PSTraceBasicRequest);
               break;
            case PSTraceMessageFactory.CONDITIONAL_EVAL_FLAG:
               assertTrue(message instanceof PSTraceConditionalEval);
               break;
            case PSTraceMessageFactory.DB_POOL_FLAG:
               assertTrue(message instanceof PSTraceDBPool);
               break;
            case PSTraceMessageFactory.EXIT_EXEC_FLAG:
               assertTrue(message instanceof PSTraceExitExecution);
               break;
            case PSTraceMessageFactory.EXIT_PROC_FLAG:
               assertTrue(message instanceof PSTraceExitProc);
               break;
            case PSTraceMessageFactory.FILE_INFO_FLAG:
               assertTrue(message instanceof PSTraceFileInfo);
               break;
            case PSTraceMessageFactory.INIT_HTTP_VAR_FLAG:
               assertTrue(message instanceof PSTraceHtmlCgi);
               break;
            case PSTraceMessageFactory.MAPPER_FLAG:
               assertTrue(message instanceof PSTraceMapper);
               break;
            case PSTraceMessageFactory.OUTPUT_CONV_FLAG:
               assertTrue(message instanceof PSTraceOutputConversion);
               break;
            case PSTraceMessageFactory.POST_EXIT_CGI_FLAG:
               assertTrue(message instanceof PSTraceHtmlCgi);
               break;
            case PSTraceMessageFactory.POST_EXIT_XML_FLAG:
               assertTrue(message instanceof PSTracePostExitXml);
               break;
            case PSTraceMessageFactory.POST_PREPROC_HTTP_VAR_FLAG:
               assertTrue(message instanceof PSTraceHtmlCgi);
               break;
            case PSTraceMessageFactory.RESULT_SET:
               assertTrue(message instanceof PSTraceResultSet);
               break;
            case PSTraceMessageFactory.RESOURCE_HANDLER_FLAG:
               assertTrue(message instanceof PSTraceResourceHandler);
               break;
            case PSTraceMessageFactory.SESSION_INFO_FLAG:
               assertTrue(message instanceof PSTraceSessionInfo);
               break;
            default:
               assertTrue(false);
         }
      }
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSTraceMessageFactoryTest("testMessages"));
       return suite;
   }


}
