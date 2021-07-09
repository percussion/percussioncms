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
