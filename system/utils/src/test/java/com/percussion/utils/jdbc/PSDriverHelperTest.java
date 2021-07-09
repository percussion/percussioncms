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
package com.percussion.utils.jdbc;

import java.sql.Driver;

import junit.framework.TestCase;

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

