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

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSLogger;
import com.percussion.design.objectstore.PSTraceInfo;
import com.percussion.server.PSRequest;
import com.percussion.testing.IPSServerBasedJunitTest;
import com.percussion.testing.PSConfigHelperTestCase;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.FileInputStream;

import junit.framework.TestSuite;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import static org.junit.Assert.assertTrue;

/**
 *   Unit tests for the PSDebugLogHandler class
 */
@Category(IntegrationTest.class)
public class PSDebugLogHandlerTest extends PSConfigHelperTestCase
   implements IPSServerBasedJunitTest
{
   public PSDebugLogHandlerTest(String name)
   {
      super(name);
   }


   /**
    * The loadable handler will call this method once before any test method.
    *
    * @param req The request that was passed to the loadable handler.
    *            Never <code>null</code>;
    */
   @Override
   public void oneTimeSetUp(Object req) {

   }

   /* (non-Javadoc)
    * @see com.percussion.testing.IPSServerBasedJunitTest#oneTimeTearDown()
    */
   @AfterClass
   public void oneTimeTearDown() {
      // TODO Auto-generated method stub

   }

   @Test
   public void testHandler() throws Exception
   {
      /*
       * initialize DebugLogHandler with trace enabled,
       * and option basic request enabled
       */
      PSLogger logger = new PSLogger();
      

      // create an app to use
      FileInputStream in = new FileInputStream("ObjectStore/sys_ActionPage.xml");
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
      PSApplication app = new PSApplication(doc);


      PSTraceInfo traceInfo = app.getTraceInfo();
      
      PSDebugLogHandler dh = new PSDebugLogHandler(logger, traceInfo, app);
      
      /* add logHandler as listener to PSTraceInfo to be notified
       * of trace start/stop
       */
      traceInfo.addTraceStateListener(dh);
      
      traceInfo.setTraceEnabled(true);
      traceInfo.setTraceEnabled(PSTraceMessageFactory.BASIC_REQUEST_INFO_FLAG, true);
      
      // check that we get back what we gave it
      assertTrue(traceInfo == dh.getTraceInfo());
      assertTrue(traceInfo.isTraceEnabled(PSTraceMessageFactory.BASIC_REQUEST_INFO_FLAG) &&
         dh.isTraceEnabled(PSTraceMessageFactory.BASIC_REQUEST_INFO_FLAG));

      // make sure the listener is firing when trace disabled or enabled
      boolean enabled = dh.isTraceEnabled(PSTraceMessageFactory.BASIC_REQUEST_INFO_FLAG);
      assertTrue(enabled);
      // this should cause the handler to set its internal enabled to false.
      traceInfo.setTraceEnabled(false);
      // this should come back false even though the option is enabled
      enabled = dh.isTraceEnabled(PSTraceMessageFactory.BASIC_REQUEST_INFO_FLAG);
      assertTrue(!enabled);

      // this should cause the handler to set its internal enabled to true.
      traceInfo.setTraceEnabled(true);
      // this should come back true now since the option is enabled
      assertTrue(dh.isTraceEnabled(PSTraceMessageFactory.BASIC_REQUEST_INFO_FLAG));

   }


}
