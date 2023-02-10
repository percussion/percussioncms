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
package com.percussion.rx.config.test;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSSystemPackageTest extends PSConfigurationTest
{
   /**
    * Test a configure definition file with empty local configure file
    * 
    * @throws Exception if an error occurs.
    */
   public void testEmptyLocal() throws Exception
   {
      // Apply with changeOnly is FALSE
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, CONFIG_DEF, LOCAL_CFG);
      
      // Apply with changeOnly is TRUE
      PSConfigFilesFactoryTest factory = null;
      try
      {
         factory = PSConfigFilesFactoryTest.applyConfigAndReturnFactory(
               PKG_NAME, CONFIG_DEF, LOCAL_CFG, LOCAL_CFG, true, true);
      }
      finally
      {
         if (factory != null)
            factory.release();
      }
   }

   /**
    * Test a configure definition file with none empty default configure file
    * 
    * @throws Exception if an error occurs.
    */
   public void testNoneEmptyDefault() throws Exception
   {
      // There is always a system package "perc.SystemObjects" in a freshly 
      // installed server. We have to use the same package name to test
      // the system configuration; otherwise we will get validation error
      // for configuring the same design object/properties from different 
      // package
      
      PSConfigFilesFactoryTest.applyConfigAndReturnFactory(
            "perc.SystemObjects", CONFIG_DEF, DEFAULT_CFG);
      
      // don't call factory.release since we are testing the system package
   }

   public static final String PKG_NAME = "PSSystemPackageTest";
   
   public static final String CONFIG_DEF = PKG_NAME + "_configDef.xml";

   public static final String LOCAL_CFG = PKG_NAME + "_localConfig.xml";

   public static final String DEFAULT_CFG = PKG_NAME + "_defaultConfig.xml";

}
