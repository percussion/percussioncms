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


package com.percussion.server;

import java.io.File;
import java.nio.channels.FileLock;

import com.percussion.utils.io.PathUtils;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


import org.junit.AfterClass;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

/**
 * @author Andriy Palamarchuk
 */
@Category(IntegrationTest.class)
public class PSServerTest
{
   /**
    * Name of system property storing Rhythmyx root directory name.
    * Used for testing.
    * @see #getRxDir()
    */
   static final String DEPLOY_DIR_PROP = "rxdeploydir";

   /**
    *
    */
   public static final String SERVER_TEST_PROPERTIES = "serverTest.properties";

   /**
    *
    */
   public static final String SERVER_TEST_PROPERTIES_DIRECTORY = "/com/percussion/server";

   /**
    * Tests how default directory is defined. 
    */
   public void testInitRxDir()
   {
      //not defined
      System.getProperties().remove(DEPLOY_DIR_PROP);
      assertEquals(CURRENT_DIR, PathUtils.getRxDir(null));

      //empty value
      System.getProperties().setProperty(DEPLOY_DIR_PROP, "");
      assertEquals(CURRENT_DIR, PathUtils.getRxDir(null));

      //non-existing value
      System.getProperties().setProperty(DEPLOY_DIR_PROP, "Non-Existing Dir");
      Logger.getRootLogger().setLevel(Level.OFF);
      assertEquals(CURRENT_DIR, PathUtils.getRxDir(null));
      Logger.getRootLogger().setLevel(CURRENT_LOGGING_LEVEL);

      //existing dir
      System.getProperties().setProperty(DEPLOY_DIR_PROP,
              CURRENT_DIR.getAbsoluteFile().getParent());
      assertEquals(CURRENT_DIR.getAbsoluteFile().getParentFile(), PathUtils.getRxDir(null));
      assertFalse(CURRENT_DIR.equals(PathUtils.getRxDir(null)));

      //
   }

   /**
    * Test file locking.
    */
   public void testFileLocks()
   {
      //create file lock
      System.getProperties().remove(DEPLOY_DIR_PROP);
      String rxdir = PathUtils.getRxDir(null).getAbsolutePath();
      System.out.println("rxdir is: " + rxdir);
      FileLock fl = PSServer.createServerStartupFileLock("running");
      assertNotNull(fl);

      // must be able to release the current lock
      if ( fl != null )
      {
         PSServer.destroyStartupFileLock(fl);
         if (fl.isValid() )
            assert(false);
      }

      fl = PSServer.createServerStartupFileLock("running");
      // relocking must fail..
      FileLock fl1 = PSServer.createServerStartupFileLock("running");
      assertNull(fl1);

   }
   /**
    * @see junit.framework.TestCase#tearDown()
    */
   @AfterClass
   protected void tearDown() throws Exception
   {
      restoreDeployDirProperty();
      restoreLogging();
   }

   /**
    * Restores console settings.
    */
   private void restoreLogging()
   {
      Logger.getRootLogger().setLevel(CURRENT_LOGGING_LEVEL);
   }

   /**
    * Restores deployment directory property value.
    */
   private void restoreDeployDirProperty()
   {
      if (DEPLOY_DIR_PROP_DEFINED)
      {
         System.setProperty(DEPLOY_DIR_PROP, CURRENT_DEPLOY_DIR);
      }
      else
      {
         System.getProperties().remove(DEPLOY_DIR_PROP);
      }
   }

   /**
    * Current directory constant.
    */
   private static final File CURRENT_DIR = new File(".");

   /**
    * Whether deployment directory property is defined.
    */
   private final boolean DEPLOY_DIR_PROP_DEFINED =
           System.getProperties().contains(DEPLOY_DIR_PROP);

   /**
    * Original value of deployment directory property.
    */
   private final String CURRENT_DEPLOY_DIR =
           System.getProperty(DEPLOY_DIR_PROP);

   /**
    * Original root logger logging level.
    */
   private final Level CURRENT_LOGGING_LEVEL = Logger.getRootLogger().getLevel();
}
