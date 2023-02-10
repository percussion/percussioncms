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
import com.percussion.services.schedule.data.PSScheduledTask.ByLabelComparator;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * @author Andriy Palamarchuk
 */
public class PSScheduleTest extends PSScheduleTestBase
{
   public void testApply()
   {
      try
      {
         new PSScheduledTask().apply(null);
         fail();
      }
      catch (IllegalArgumentException expected) {}

      final PSScheduledTask clone = new PSScheduledTask();
      clone.apply(createFullSchedule());
      assertTrue(EqualsBuilder.reflectionEquals(
            createFullSchedule(), clone));
   }

   public void testSetIdNull()
   {
      final PSScheduledTask schedule = new PSScheduledTask();
      try
      {
         schedule.setId(null);
         fail();
      }
      catch (NullPointerException expected) {}
   }

   public void testSetIdWrongType()
   {
      new PSGuid(HOST_ID, PSTypeEnum.SCHEDULED_TASK, UUID);
      
      final PSScheduledTask schedule = new PSScheduledTask();
      try
      {
         schedule.setId(new PSGuid(HOST_ID, PSTypeEnum.ACTION, UUID));
         fail();
      }
      catch (IllegalArgumentException expected) {}
   }
   
   public void testEqualsHash()
   {
      // no ids
      final PSScheduledTask s1 = new PSScheduledTask();
      final PSScheduledTask s2 = new PSScheduledTask();
      assertEquals(s1.hashCode(), s2.hashCode());
      assertEquals(s1, s1);
      assertFalse(s1.equals(s2));

      // ids
      s1.setId(createGuid());
      assertFalse(s1.equals(new PSScheduledTask()));
      assertFalse(new PSScheduledTask().equals(s1));
      s2.setId(s1.getId());
      assertEquals(s1, s2);
      assertEquals(s1.hashCode(), s2.hashCode());
      
      // other fields do not affect hash code
      assertEquals(s1, createFullSchedule());
      assertEquals(s1.hashCode(), createFullSchedule().hashCode());
   }

   public void testByLabelComparator()
   {
      final ByLabelComparator c = new ByLabelComparator();
      final PSScheduledTask s1 = new PSScheduledTask();
      s1.setName(LABEL1);
      assertEquals(0, c.compare(s1, s1));

      final PSScheduledTask s2 = new PSScheduledTask();
      s2.setName(LABEL2);
      assertEquals(-1, c.compare(s1, s2));
      assertEquals(1, c.compare(s2, s1));
   }

   /**
    * Creates a schedule with all the properties specified.
    * @return a new schedule. Never <code>null</code>.
    */
   @Override
   protected PSScheduledTask createFullSchedule()
   {
      final PSScheduledTask schedule = (PSScheduledTask) super.createFullSchedule();
      setExtendedData(schedule);
      return schedule;
   }

   /**
    * Assigns sample data specific to Schedule.
    * @param schedule the schedule to fill with the data.
    * Assumed not null.
    */
   private void setExtendedData(final PSScheduledTask schedule)
   {
      schedule.setId(createGuid());
      schedule.setCronSpecification("cron spec");
   }

   @Override
   protected PSJob createSchedule()
   {
      return new PSScheduledTask();
   }

   public void testSerializeEmpty()
   {
      final PSScheduledTask schedule = new PSScheduledTask();
      assertTrue(EqualsBuilder.reflectionEquals(
            schedule, SerializationUtils.clone(schedule)));
   }

   public void testSerializeFull()
   {
      assertTrue(EqualsBuilder.reflectionEquals(
            createFullSchedule(),
            SerializationUtils.clone(createFullSchedule())));
   }

   /**
    * Sample label.
    */
   private static final String LABEL1 = "Label 1";

   /**
    * Sample label.
    */
   private static final String LABEL2 = "Label 2";
}
