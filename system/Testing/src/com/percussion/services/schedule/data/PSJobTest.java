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
package com.percussion.services.schedule.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * @author Andriy Palamarchuk
 */
public class PSJobTest extends PSScheduleTestBase
{
   public void testApply()
   {
      try
      {
         new PSTestSchedule().apply(null);
         fail();
      }
      catch (IllegalArgumentException expected) {}

      final PSTestSchedule schedule = new PSTestSchedule();
      schedule.apply(createFullSchedule());
      assertTrue(
            EqualsBuilder.reflectionEquals(createFullSchedule(), schedule));
   }

   public void testSetNotifyWhen()
   {
      final PSJob schedule = new PSTestSchedule();

      // notification id is null
      try {
         schedule.setNotifyWhen(null);
         fail();
      } catch (IllegalArgumentException expected) {}
   }

   public void testSetNotificationId() {
      final PSJob schedule = new PSTestSchedule();
      assertNull(schedule.getNotificationTemplateId());
      assertEquals(PSNotifyWhen.NEVER, schedule.getNotifyWhen());
      schedule.setNotificationTemplateId(null);

      schedule.setNotifyWhen(PSNotifyWhen.ALWAYS);
      try
      {
         schedule.setNotificationTemplateId(null);
         fail();
      }
      catch (IllegalArgumentException expected) {}
      schedule.setNotificationTemplateId(createTemplateGuid());
      
      schedule.setNotifyWhen(PSNotifyWhen.NEVER);
      schedule.setNotificationTemplateId(null);
      
      try{
         schedule.setNotificationTemplateId(
               new PSGuid(UUID, PSTypeEnum.SCHEDULED_TASK, HOST_ID));
         fail();
      }
      catch (IllegalArgumentException expected) {}
   }
   
   @Override
   protected PSJob createSchedule()
   {
      return new PSTestSchedule();
   }
   
   /**
    * A test implementation of the abstract schedule class.
    */
   private static final class PSTestSchedule extends PSJob
   {
      public PSTestSchedule()
      {
         super();
      }

      /**
       * Serializable class version number.
       */
      private static final long serialVersionUID = 1L;
   }
}
