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


package com.percussion.server;

import com.percussion.utils.io.PathUtils;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.AfterClass;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.nio.channels.FileLock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Andriy Palamarchuk
 */
@Category(IntegrationTest.class)
public class PSServerTest
{
   /**
    * Name of system property storing Rhythmyx root directory name.
    * Used for testing.
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
      assertEquals(CURRENT_DIR, PathUtils.getRxDir(null));

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

}
