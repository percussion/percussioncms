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
