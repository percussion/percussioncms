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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *   Unit tests for the PSDebugManager class
 */
@Category(IntegrationTest.class)
public class PSDebugManagerTest extends PSConfigHelperTestCase
   implements IPSServerBasedJunitTest
{
   public PSDebugManagerTest(String name)
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
   public void testManager() throws Exception
   {
      // create an app to use
      FileInputStream in = new FileInputStream("ObjectStore/sys_ActionPage.xml");
      Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
      PSApplication app = new PSApplication(doc);
      String appName = app.getName();


      // get instance of manager
      PSDebugManager mgr = PSDebugManager.getDebugManager();

      // get another instance and check that they are equal
      PSDebugManager otherMgr = PSDebugManager.getDebugManager();
      assertEquals(mgr, otherMgr);

      // create and register a debugloghandler
      PSLogger logger = new PSLogger();
      PSTraceInfo traceInfo = new PSTraceInfo();
      PSDebugLogHandler dh = new PSDebugLogHandler(logger, traceInfo, app);
      mgr.registerLogHandler(dh, appName);

      // get some stuff from it and verify it's correct
      PSDebugLogHandler otherDh = mgr.getLogHandler(appName);
      assertEquals(dh, otherDh);

      PSTraceFlag flag = new PSTraceFlag();
      mgr.setTraceOptionsFlag(appName, flag);
      PSTraceFlag otherFlag = mgr.getTraceOptionsFlag(appName);
      assertTrue(flag == otherFlag);

      // now test the trace file
      PSTraceWriter writer = mgr.getTraceWriter(app);
      writer.write(appName);
      writer.flush();
      writer.close();

      // get it again
      writer = mgr.getTraceWriter(app);
      writer.close();
   }


}
