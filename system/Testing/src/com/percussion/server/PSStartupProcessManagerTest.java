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
