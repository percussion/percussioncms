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
package com.percussion.services.notification;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the notification service api
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSNotificationServiceTest
{
   /**
    * Test class for listener tracks how many times it's been called with a
    * matching file.
    */
   static class TestNListener implements IPSNotificationListener
   {
      /**
       * Store the match value, not <code>null</code> after ctor
       */
      private File m_matchFile = null;

      /**
       * Match count
       */
      private int m_count = 0;

      /**
       * Complete call count
       */
      private int m_ccount = 0;

      /**
       * Ctor
       * 
       * @param mf match file, never <code>null</code>
       */
      public TestNListener(File mf) {
         if (mf == null)
         {
            throw new IllegalArgumentException("mf may not be null");
         }
         m_matchFile = mf;
      }

      public void notifyEvent(PSNotificationEvent notification)
      {
         if (notification.getType().equals(EventType.FILE))
         {
            if (m_matchFile.equals(notification.getTarget()))
            {
               m_count++;
            }
         }
         else if (notification.getType().equals(EventType.OBJECT_INVALIDATION))
         {
            m_ccount++;
         }

      }

      /**
       * Get count of completion calls
       * 
       * @return the count, &gt;= 0
       */
      public int getCcount()
      {
         return m_ccount;
      }

      /**
       * Get count of matching file calls
       * 
       * @return the count, &gt;= 0
       */
      public int getCount()
      {
         return m_count;
      }
   }

   /**
    * Test class for listener tracks how many times it's been called.
    */
   static class TestTListener implements IPSNotificationListener
   {
      /**
       * Call count
       */
      private int m_ccount = 0;

      /**
       * Ctor
       */
      public TestTListener() {
      }

      public void notifyEvent(PSNotificationEvent notification)
      {
         if (notification.getType().equals(EventType.OBJECT_INVALIDATION))
         {
            m_ccount++;
         }
         else
         {
            throw new RuntimeException("Serious problem in test");
         }

      }

      /**
       * Get count of calls
       * 
       * @return the count, &gt;= 0
       */
      public int getCcount()
      {
         return m_ccount;
      }
   }

   /**
    * Test notifications by creating sample listener classes
    * 
    * @throws InterruptedException
    */
   @Test
   public void testNotifications()
   {
      IPSNotificationService nsvc = PSNotificationServiceLocator
            .getNotificationService();

      TestNListener xyz = new TestNListener(new File("xyz"));
      TestNListener abc = new TestNListener(new File("abc"));

      try
      {
         nsvc.addListener(EventType.FILE, xyz);
         nsvc.addListener(EventType.OBJECT_INVALIDATION, xyz);
         nsvc.addListener(EventType.FILE, abc);
         nsvc.addListener(EventType.OBJECT_INVALIDATION, abc);

         PSNotificationEvent xyz_event = new PSNotificationEvent(
               EventType.FILE, new File("xyz"));
         PSNotificationEvent abc_event = new PSNotificationEvent(
               EventType.FILE, new File("abc"));

         nsvc.notifyEvent(xyz_event);

         assertEquals(0, xyz.getCcount());
         assertEquals(1, xyz.getCount());
         assertEquals(0, abc.getCcount());
         assertEquals(0, abc.getCount());


         PSNotificationEvent xyz_completion = new PSNotificationEvent(
               EventType.OBJECT_INVALIDATION, new File("xyz"));

         nsvc.notifyEvent(abc_event);
         nsvc.notifyEvent(xyz_completion);

         assertEquals(1, xyz.getCcount());
         assertEquals(1, xyz.getCount());
         assertEquals(1, abc.getCcount());
         assertEquals(1, abc.getCount());

         
         nsvc.removeListener(EventType.OBJECT_INVALIDATION, abc);
         nsvc.addListener(EventType.FILE, abc);

         nsvc.notifyEvent(abc_event);

         assertEquals(1, xyz.getCcount());
         assertEquals(1, xyz.getCount());
         assertEquals(1, abc.getCcount());
         assertEquals(2, abc.getCount());
      }
      finally
      {
         // Cleanup
         nsvc.removeListener(EventType.FILE, xyz);
         nsvc.removeListener(EventType.OBJECT_INVALIDATION, xyz);
         nsvc.removeListener(EventType.FILE, abc);
         nsvc.removeListener(EventType.OBJECT_INVALIDATION, abc);
      }

   }

   /**
    * Test topic notifications
    * 
    * @throws InterruptedException
    */
   @Test
   public void testTopicNotifications() throws InterruptedException
   {
      IPSNotificationService nsvc = PSNotificationServiceLocator
            .getNotificationService();

      TestTListener tlistener = new TestTListener();

      try
      {
         nsvc.addListener(EventType.OBJECT_INVALIDATION, tlistener);

         PSNotificationEvent evt = new PSNotificationEvent(
               EventType.OBJECT_INVALIDATION, new PSGuid());

         nsvc.notifyEvent(evt);

         Thread.sleep(1000L); // Wait for notification
         assertTrue(tlistener.getCcount() > 0);
      }
      finally
      {
         nsvc.removeListener(EventType.OBJECT_INVALIDATION, tlistener);
      }
   }

   /**
    * Test helpers
    * 
    * @throws InterruptedException
    */
   @Test
   public void testHelpers() throws InterruptedException
   {
      IPSNotificationService nsvc = PSNotificationServiceLocator
            .getNotificationService();

      TestTListener tlistener = new TestTListener();

      try
      {
         nsvc.addListener(EventType.OBJECT_INVALIDATION, tlistener);

         PSNotificationHelper
               .notifyInvalidation(new PSGuid(PSTypeEnum.ACL, 123));

         Thread.sleep(1000L); // Wait for notification
         assertTrue(tlistener.getCcount() > 0);
      }
      finally
      {
         nsvc.removeListener(EventType.OBJECT_INVALIDATION, tlistener);
      }
   }
}
