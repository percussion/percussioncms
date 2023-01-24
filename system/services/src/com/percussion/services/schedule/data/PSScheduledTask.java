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

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
import org.quartz.CronTrigger;

/**
 * This represents a single recorded periodic task. The tasks are loaded at
 * system start to create jobs that are periodically run. 
 * <p>
 * This schedule data is stored by Quartz in serialized form with Quartz job 
 * properties.
 * 
 * @author Doug Rand
 */
public class PSScheduledTask extends PSJob
{
   /**
    * Creates a new schedule.
    */
   public PSScheduledTask()
   {
   }
   
   /**
    * Note, this implementation copies only properties, common for this schedule
    * and the provided one.
    * {@inheritDoc}.
    */
   @Override
   public void apply(PSJob schedule)
   {
      super.apply(schedule);
      if (schedule instanceof PSScheduledTask)
      {
         final PSScheduledTask s = (PSScheduledTask) schedule;
         setCronSpecification(s.getCronSpecification());
      }
   }

   /**
    * The cron specification is a blank separated list of cron specifications.
    * This is documented as part of the Quartz {@link CronTrigger} class as well
    * as most UNIX documentation.
    * @return the cron specification.
    * Not <code>null</code> or blank after is set. 
    */
   public String getCronSpecification()
   {
      return m_cronSpecification;
   }

   /**
    * @param cronSpecification the cronSpecification to set.
    * Not <code>null</code> or blank.
    * @see #setCronSpecification(String)
    */
   public void setCronSpecification(String cronSpecification)
   {
      m_cronSpecification = cronSpecification;
   }

   /**
    * Compares schedules by label. 
    */
   public static class ByLabelComparator implements Comparator<PSScheduledTask>
   {
      // see base
      public int compare(PSScheduledTask s1, PSScheduledTask s2)
      {
         return s1.getName().compareTo(s2.getName());
      }
   }

   public String toString()
   {
      if (StringUtils.isBlank(getServer()))
         return getName() + "(" + getId().getUUID() + ") Cron=" + getCronSpecification();
      else
         return getName() + "(" + getId().getUUID() + ") Cron=" + getCronSpecification() + " Server=" + getServer();
   }
   
   /**
    * Serialized class version number.
    */
   private static final long serialVersionUID = 1L;

   /**
    * @see #getCronSpecification()
    */
   private String m_cronSpecification;
}
