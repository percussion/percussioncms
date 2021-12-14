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

package com.percussion.extension;

import com.google.common.io.Files;
import com.percussion.design.objectstore.PSNonUniqueException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.util.PSIteratorUtils;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

/**
 * Unit tests for the JavaScript extension handler and supporting classes.
 */
public class PSJavaScriptTest extends TestCase
{
   public PSJavaScriptTest(String testName)
   {
      super(testName);
   }

   public void testAll() throws PSExtensionException, PSNonUniqueException, PSNotFoundException {

      File baseDir = Files.createTempDir();
      File expected = new File(baseDir, RESULT_FILE);
      if (expected.exists())
      {
         expected.delete();
      }
      PSExtensionManager mgr = new PSExtensionManager();
      Properties props = new Properties();
      props.setProperty(IPSExtensionHandler.INIT_PARAM_CONFIG_FILENAME,
         RESULT_FILE);
      mgr.init( baseDir, props );
      System.out.println( "Mgr inited successfully" );

      String handler = "JavaScript";
      PSExtensionRef ref = PSExtensionRef.handlerRef(handler);
      if (!mgr.exists(ref))
      {
         Properties initParams = new Properties();
         initParams.setProperty("className", PSJavaScriptExtensionHandler.class.getName());
         initParams.setProperty(
            IPSExtensionHandler.INIT_PARAM_CONFIG_FILENAME,
            IPSExtensionHandler.DEFAULT_CONFIG_FILENAME);
         initParams.setProperty(IPSExtensionDef.INIT_PARAM_REENTRANT, "yes");

         IPSExtensionDef def = new PSExtensionDef( ref,
            PSIteratorUtils.iterator( IPSExtensionHandler.class.getName()),
            null,
            initParams, null );
         Iterator res = PSIteratorUtils.emptyIterator();
         mgr.installExtension( def, res );
         System.out.println( handler + " installed successfully" );
      }
      mgr.startExtensionHandler( handler );
      System.out.println( handler + " started successfully" );
      mgr.stopExtensionHandler( handler );
      System.out.println( handler + " stopped successfully" );
      mgr.shutdown();
      System.out.println( "mgr shutdown successfully" );
      assertTrue(expected.exists());
      expected.delete();
   }

   /** collect all tests into a TestSuite and return it */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSJavaScriptTest("testAll"));
      return suite;
   }
   
   /**
    * Base directory for unit test resources
    */
   private static final String RESOURCE_BASE = "UnitTestResources";
   
   /**
    * File to which extension def is written.
    */
   private static final String RESULT_FILE = 
      "Extensions.xml";
}
