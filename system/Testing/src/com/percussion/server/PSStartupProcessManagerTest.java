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

import static org.junit.Assert.*;

import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.io.FileUtils;
import org.apache.tika.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSStartupProcessManagerTest
{
   private static final String YES = "yes";
   private static final String NO = "no";
   private static final String TEST_PROP = "tested";

   @Test
   public void testProcess()
   {
      PSStartupProcessManager mgr = new PSStartupProcessManager();
      Properties props = new Properties();
      props.setProperty(TEST_PROP, NO);
      mgr.setStartupProperties(props);
      
      mgr.addStartupProcess(new IPSStartupProcess()
      {
         
         public void doStartupWork(Properties startupProps) throws Exception
         {
            if (NO.equals(startupProps.getProperty(TEST_PROP)))
            {
               startupProps.setProperty(TEST_PROP, YES);
            }
         }

         public void setStartupProcessManager(IPSStartupProcessManager mgr)
         {
            //noop
         }
      });
      
      mgr.runStartupProcesses(props);
      assertEquals(YES, props.getProperty(TEST_PROP));      
   }
   
   @Test
   public void testFileLoadAndSave() throws Exception
   {
      // copy file to temp file so we can write to it
      File testDir = new File("/com/percussion/server");
      File src = new File(testDir, "PSStartupProcessManager.properties");
      File tmpFile = new File(testDir, "test.properties");
      File tgtFile = new File(PSServer.getRxDir(), tmpFile.getPath());
      
      FileUtils.copyFile(src, tgtFile);
      assertTrue("tmpFile does not exist", tgtFile.exists());
      assertTrue("cannot read tmpFile", tgtFile.canRead());
      assertTrue("cannot write to tmpFile", tgtFile.canWrite());
      
      validateProperty(tgtFile, TEST_PROP, NO);
      
      PSStartupProcessManager mgr = new PSStartupProcessManager();
      mgr.setPropFilePath(tmpFile.getPath());
      mgr.addStartupProcess(new IPSStartupProcess()
      {
         
         public void doStartupWork(Properties startupProps) throws Exception
         {
            if (NO.equals(startupProps.getProperty(TEST_PROP)))
            {
               startupProps.setProperty(TEST_PROP, YES);
            }
         }

         public void setStartupProcessManager(IPSStartupProcessManager mgr)
         {
            // TODO Auto-generated method stub
            mgr.addStartupProcess(this);
         }
      });
      
      mgr.notifyEvent(new PSNotificationEvent(EventType.CORE_SERVER_INITIALIZED, null));
      
      validateProperty(tgtFile, TEST_PROP, YES);
   }

   protected void validateProperty(File tmpFile, String name, String value) throws IOException, FileNotFoundException
   {
      Properties props = new Properties();
      FileReader reader = new FileReader(tmpFile);
      try
      {
         props.load(reader);
         assertEquals(value, props.getProperty(name));
      }
      finally
      {
         IOUtils.closeQuietly(reader);
      }
   }
}
