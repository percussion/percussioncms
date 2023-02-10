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
import junit.framework.TestCase;

/**
 * Base for the schedule classes test cases.
 * @author Andriy Palamarchuk
 */
public abstract class PSScheduleTestBase extends TestCase
{
   /**
    * Creates a schedule with all the properties specified.
    * @return a new schedule. Never <code>null</code>.
    */
   protected PSJob createFullSchedule()
   {
      final PSJob schedule = createSchedule();
      
      schedule.setId(createGuid());
      schedule.setName("Label 1");
      schedule.setExtensionName("extension 1");
      schedule.setNotificationTemplateId(createTemplateGuid());
      schedule.setEmailAddresses("To everybody good");
      schedule.setNotifyWhen(PSNotifyWhen.ALWAYS);
      schedule.getParameters().put("param1", "value1");
      schedule.setNotify("roleToNotify");
      
      return schedule;
   }

   /**
    * Creates an empty instance of the schedule class under test.
    * @return a new empty schedule. Not <code>null</code>.
    */
   protected abstract PSJob createSchedule();

   /**
    * Creates a sample schedule notification template GUID. 
    */
   protected PSGuid createTemplateGuid()
   {
      return new PSGuid(
            UUID, PSTypeEnum.SCHEDULE_NOTIFICATION_TEMPLATE, HOST_ID);
   }

   /**
    * Creates a sample schedule GUID. 
    */
   protected PSGuid createGuid()
   {
      return new PSGuid(HOST_ID, PSTypeEnum.SCHEDULED_TASK, UUID);
   }

   /**
    * Sample GUID UUID.
    */
   protected static final int UUID = 123;

   /**
    * Sample GUID host id.
    */
   protected static final int HOST_ID = 10;
}
