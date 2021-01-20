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
package com.percussion.services.notification.filemonitor;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

import java.lang.Thread;

import com.percussion.server.PSServer;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationServiceLocator;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.filemonitor.impl.PSFileMonitorService;
import com.percussion.util.PSPurgableTempFile;
import org.junit.experimental.categories.Category;

/**
 * Unit Test for  File Monitor Notification service {@link PSFileMonitorService}
 */
@Category(IntegrationTest.class)
public class PSFileMonitorServiceTest extends ServletTestCase 
      implements IPSNotificationListener
{
   private int NUM_FILE = 5;
   
   private boolean m_isSuccess = false;

   private File[] m_testFile = new File[NUM_FILE];
   private Boolean[] m_eventReceived = new Boolean[] {false, false, false, false, false};
   private Thread m_testThread = new Thread (new Runnable()
   {
      public void run()
      {
         doTest();
      }
   });
   
   public void testMonitorFile()
   {
      m_testThread.start();
      int waitInSec = 0;
      while (m_testThread.isAlive() && waitInSec++ < 120)
      {
         try
         {
            Thread.currentThread().sleep(1000);
         }
         catch (InterruptedException e)
         {
            // should not be here
            assertTrue("Unexpected thread exception", false);
         }
      }
      // assertTrue(m_isSuccess);
      if (m_isSuccess)
         System.out.println("testMonitorFile() ran successful.");
      else
         System.out.println("testMonitorFile() FAILED.");
   }

   public void doTest()
   {
      // Register with Rx Notification service. 
      //   This class will receive notification (callback to 
      //   "notifyEvent()"whenever a file change event occurs.
      IPSNotificationService notifyService = PSNotificationServiceLocator
         .getNotificationService();
      notifyService.addListener(EventType.FILE, this);

      try
      {
         // Get the file notification service and 
         PSFileMonitorService fileMonitorService = (PSFileMonitorService) PSFileMonitorServiceLocator
               .getFileMonitorService();
         int curDirWatcherCount = fileMonitorService.getDirWatcherCount();
   
         // Create NUM_FILE files.
         for (int i=0; i<NUM_FILE; i++)
         {
            m_testFile[i] = createTestFile();
            // Start monitoring the file.
            fileMonitorService.monitorFile( m_testFile[i]);
         }
         // give enough time to start & setup the dirWatcher thread
         m_testThread.sleep(10*1000);

         assertTrue(fileMonitorService.getDirWatcherCount() == 1 + curDirWatcherCount);
   
         // Change the files
         for (int i=0; i<NUM_FILE; i++)
         {
            //m_testThread.sleep(1000);
            writeToTestFile(m_testFile[i], "This is an update test.");
         }
     
         // Sleep for X seconds. 
         // This is to allow time the File Monitoring Service  
         // polling delay to expire.
         int tries = 0;
         while ((!recievedAll()) && tries++ < 60) 
            m_testThread.sleep(1000);
         
         for (int i=0; i<NUM_FILE; i++)
            assertTrue(m_eventReceived[i]);

         for (int i=0; i<NUM_FILE; i++)
            fileMonitorService.unmonitorFile( m_testFile[i]);
         
         assertTrue(fileMonitorService.getDirWatcherCount() == curDirWatcherCount);
         
         m_isSuccess = true;
      }
      catch (InterruptedException e)
      {
         assertTrue(false); 
      }
      finally
      {
         // Clean up
         notifyService.removeListener(EventType.FILE, this);
         for (int i=0; i<NUM_FILE; i++)
            m_testFile[i].delete();
      }
      
   }
   
   private boolean recievedAll()
   {
      for (int i=0; i<NUM_FILE; i++)
      {
         if (!m_eventReceived[i])
            return false;
      }
      return true;
   }
  
   /*
    * //see IPSNotificationListener.notifyEvent() method for details
    */
   public synchronized void notifyEvent(PSNotificationEvent event)
   {
      if (event == null || event.getType() != EventType.FILE
            || (!(event.getTarget() instanceof File)))
      {
         throw new IllegalArgumentException(
               "event may not be null amd must represent a file change event");
      }
      
      File tgtFile = (File) event.getTarget();
      int index = -1;
      for (int i=0; i<NUM_FILE; i++)
      {
         if (tgtFile.getAbsolutePath().equals(m_testFile[i].getAbsolutePath()))
         {
            index = i;
            break;
         }
      }
      assertTrue(index >= 0);
      
      m_eventReceived[index] = true;
      //System.out.println("notifyEvent - m_eventReceived: " + index);
   }

   
   private File createTestFile()
   {
   File tmpFile = null;
   File tmpDir = new File(PSServer.getRxDir(), "temp");
   try 
      {
         tmpFile = new PSPurgableTempFile("tmpFile", ".txt", tmpDir);
//         System.out.println("create tmpFile \"" + tmpFile.getAbsolutePath()
//               + "\".");

      }
      catch (IOException e)
      {
         System.out.println(
               "PSFileMonitorServiceTest.createTestFile: Failure to create temporarty file.");
         assertTrue(false); 
      }
      return tmpFile;
   }
   
   private void writeToTestFile(File testFile, String theString)
   {
      // Create file writers and output a string.
      try
      {
         FileWriter testFileWriter = new FileWriter(testFile, true);
         PrintWriter tfPrintWriter = new PrintWriter(testFileWriter);
         tfPrintWriter.println(theString);
         tfPrintWriter.flush();
      }
      catch (IOException e)
      {
         System.out.println("writeToTestFile: Failure to write to test file");
         assertTrue(false); // TODO: isn't there a better way to do this?
      }
      return;
   }
   

} //end
