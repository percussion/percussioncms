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
package com.percussion.services.schedule.impl;

import com.percussion.services.schedule.data.PSScheduledTask;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;

/**
 * Utility class for storing and retrieving {@link PSScheduledTask} object from
 * {@link JobDetail}.
 */
public class PSScheduleUtils
{
   /**
    * Convenience method to retrieve a stored schedule from the given job.
    * 
    * @param jobDetail the job contains a stored schedule, never 
    *    <code>null</code>
    *    
    * @return the stored schedule. It may be <code>null</code> if the job does
    *    not contain a stored schedule.
    */
   public static PSScheduledTask getStoredSchedule(JobDetail jobDetail)
   {
      final JobDataMap jobData = jobDetail.getJobDataMap();
      return (PSScheduledTask) jobData.get(SCHEDULE_KEY);
   }
   
   /**
    * Stores a schedule object as {@link PSScheduleUtils} in a Quartz job 
    * detail object.
    * 
    * @param jobDetail the job detail object to store the schedule in, never
    *    <code>null</code>
    * @param schedule the schedule to store, never <code>null</code>.
    */
   public static void storeScheduleInJob(PSScheduledTask schedule,
         JobDetail jobDetail)
   {
      final JobDataMap jobData = jobDetail.getJobDataMap();
      final PSScheduledTask storedSchedule = new PSScheduledTask();
      storedSchedule.apply(schedule);
      jobData.put(SCHEDULE_KEY, storedSchedule);
   }
   
   /**
    * Creates the context variables for the finished execution. Add '$' to
    * all parameters and add $sys.executionElapsedTime and 
    * $sys.executionDatetime to the returned map.
    * 
    * @param params the parameters of the extension, assumed not 
    *    <code>null</code>
    * @param startTime starting time of the execution.
    * @param endTime ending time of the execution.
    * 
    * @return the context variables, never <code>null</code> or empty.
    */
   public static Map<String, Object> getContextVars(
         Map<String, String> params, long startTime, long endTime)
   {
      if (params == null)
         throw new IllegalArgumentException("params may not be null.");
      
      Map<String,Object> vars = new HashMap<>();
      for (Map.Entry<String, String> entry : params.entrySet())
      {
         if (entry.getKey().startsWith("$"))
            vars.put(entry.getKey(), entry.getValue());
         else
            vars.put("$" + entry.getKey(), entry.getValue());
      }
      
      vars.put("$sys.executionElapsedTime", "" + (endTime - startTime));
      vars.put("$sys.executionDatetime", new Date(startTime));

      return vars;
   }
   
   /**
    * The key under which {@link PSScheduleUtils} is stored in the job
    * properties.
    */
   private static final String SCHEDULE_KEY = "jobSchedule";   
}
