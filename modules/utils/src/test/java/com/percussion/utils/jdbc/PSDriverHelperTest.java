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
package com.percussion.utils.jdbc;

import junit.framework.TestCase;

import java.sql.Driver;

import static com.percussion.util.PSResourceUtils.getResourcePath;

/**
 * Test case for the {@link PSDriverHelper} class
 */
public class PSDriverHelperTest extends TestCase
{
   /**
    * Constant for the driver 1 class.
    */
   public static final String TEST_DRIVER1_CLASS =
      "net.sourceforge.jtds.jdbc.Driver";
   
   /**
    * Constant for the non-driver class.
    */
   public static final String TEST_CLASS =
      "net.sourceforge.jtds.util.Logger";
   
   /**
    * Constant for the driver 1 file.
    */
   public static final String TEST_DRIVER1_FILE = getResourcePath(
           PSDriverHelperTest.class,"/com/percussion/utils/jdbc/jtds.jar");
   
   /**
    * Constant for the driver 2 class.
    */   
   public static final String TEST_DRIVER2_CLASS =
      "oracle.jdbc.OracleDriver";
   
   /**
    * Constant for the driver 2 file.
    */
   public static final String TEST_DRIVER2_FILE =getResourcePath(PSDriverHelperTest.class,"/com/percussion/utils/jdbc/ojdbc6.jar");

   /**
    * Test loading drivers
    * 
    * @throws Exception If the test fails.
    */
   public void testGetDriver() throws Exception
   {
      // Load a driver
      Driver driver = PSDriverHelper.getDriver(TEST_DRIVER1_CLASS,
            TEST_DRIVER1_FILE);
      assertNotNull(driver);
      String driverName = driver.getClass().getName();
      
      // Re-load the same driver
      assertEquals(driverName, PSDriverHelper.getDriver(TEST_DRIVER1_CLASS, 
                  TEST_DRIVER1_FILE).getClass().getName());
      
      // Load a different driver
      assertFalse(PSDriverHelper.getDriver(TEST_DRIVER2_CLASS,
            TEST_DRIVER2_FILE).getClass().getName().equals(driverName));
   
      // Load a regular class
      try
      {
         PSDriverHelper.getDriver(TEST_CLASS, TEST_DRIVER1_FILE);
         fail("driver should not be found");
      }
      catch (Exception e)
      {
         // expected
      }
      
      // Load a non-existent class
      try
      {
         PSDriverHelper.getDriver("foo", TEST_DRIVER1_FILE);
         fail("driver should not be found");
      }
      catch (Exception e)
      {
         // expected
      }
      
      // Load from a non-existent file
      try
      {
         PSDriverHelper.getDriver("foo", "foo.jar");
         fail("driver should not be found");
      }
      catch (Exception e)
      {
         // expected
      }
   }
   
}

