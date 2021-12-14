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
