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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
