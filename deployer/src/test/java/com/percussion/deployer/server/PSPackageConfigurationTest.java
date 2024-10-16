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
package com.percussion.deployer.server;

import com.percussion.error.IPSDeploymentErrors;
import com.percussion.error.PSDeployException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.util.PSResourceUtils;
import com.percussion.utils.testing.UnitTest;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the PSPackageConfiguration, which include
 * PSDependencyMap and PSDependencyDef classes
 */
@Category(UnitTest.class)
public class PSPackageConfigurationTest
{

   private static final Logger log = LogManager.getLogger(PSPackageConfigurationTest.class);

   public PSPackageConfigurationTest()
   {
      super();
   }

   /**
    * Tests the map class.
    */
   @Test
   public void testMap() throws Exception
   {
      PSDependencyMap map = getConfig(RESOURCE_D0C).getDependencyMap();

      Iterator<PSDependencyDef> defs = map.getDefs();
      assertTrue(defs != null);
      assertTrue(defs.hasNext());
      while (defs.hasNext())
      {
         PSDependencyDef def = defs.next();
         assertTrue(def != null);
      }

      PSDependencyDef def = map.getDependencyDef("foo");
      assertNull(def);

   }

   /**
    * (positive) Tests the order definition of the configuration.
    */
   @Test
   public void testDeployOrder() throws Exception
   {
      PSPackageConfiguration config = getConfig(RESOURCE_D0C);
      List<String> order = config.getDeployOrder();
      PSDependencyMap map = config.getDependencyMap();

      assertTrue(order != null);

      for (String type : order)
      {
         PSDependencyDef def = map.getDependencyDef(type);
         assertTrue(def != null);
      }
   }

   /**
    * Negative test the order definition.
    * 
    * @throws Exception if error occurs.
    */
   @Test
   public void testInvalidDeployOrderDef() throws Exception
   {
      try
      {
         getConfig(MissDeployEl);
         assertTrue(false);
      }
      catch (PSDeployException e)
      {
         assertTrue(e.getErrorCode() == IPSDeploymentErrors.INCOMPLATE_ORDER_DEF);
      }

      try
      {
         getConfig(MissNonDeployEl);
         assertTrue(false);
      }
      catch (PSDeployException e)
      {
         assertTrue(e.getErrorCode() == IPSDeploymentErrors.INVALID_NUM_CHILD_DEFS);
      }

      try
      {
         getConfig(InvalidNonDeployElParent);
         assertTrue(false);
      }
      catch (PSDeployException e)
      {
         assertTrue(e.getErrorCode() == IPSDeploymentErrors.CANNOT_FIND_PARENT_DEP_DEF);
      }
   }
   
   /**
    * Load the package configuration from the source.
    *
    * @return The configuration, never <code>null</code>.
    *
    * @throws Exception if there are any errors.
    */
   private PSPackageConfiguration getConfig(String filePath) throws Exception
   {
      FileInputStream in = null;
      try
      {
         File f = PSResourceUtils.getFile(PSPackageConfigurationTest.class,filePath,null);

         in = new FileInputStream(f);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         PSPackageConfiguration config = new PSPackageConfiguration(doc.getDocumentElement(), false);
         return config;
      }
      catch (Exception e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         throw e;
      }
      finally
      {
         if (in != null)
            try {in.close();} catch(IOException e){}
      }
   }


   private static final String UNIT_TEST_DIR =
           "/com/percussion/config/";

   private static final String RESOURCE_D0C =UNIT_TEST_DIR+
      "sys_PackageConfiguration.xml";

   // Invalid config files for negative test
   
   /**
    * File contains less deployable elements in the order definition.
    */
   private static final String MissDeployEl = UNIT_TEST_DIR + "sys_PkgConfig_MissDeployEl.xml";

   /**
    * File contains less non-deployable elements in the order definition.
    * The parent element is "Custom"
    */
   private static final String MissNonDeployEl = UNIT_TEST_DIR + "sys_PkgConfig_MissNonDeployEl.xml";

   /**
    * File contains less deployable elements in the order definition.
    * The parent of non-deployable elements is not "Custom" 
    */
   private static final String InvalidNonDeployElParent = UNIT_TEST_DIR + "sys_PkgConfig_InvalidNonDeployElParent.xml";
  
}
