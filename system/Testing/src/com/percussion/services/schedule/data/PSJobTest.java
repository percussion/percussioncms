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
