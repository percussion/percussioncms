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
