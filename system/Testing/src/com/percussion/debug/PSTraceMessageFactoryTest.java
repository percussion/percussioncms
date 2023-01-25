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
