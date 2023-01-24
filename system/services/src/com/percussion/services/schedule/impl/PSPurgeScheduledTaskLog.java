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

import com.percussion.services.schedule.IPSSchedulingService;
import com.percussion.services.schedule.PSSchedulingServiceLocator;

import java.util.Date;
import java.util.Map;

/**
 * This is used to purge log entries older than a specified date for
 * scheduled tasks.
 *
 * @author Yu-Bing Chen
 */
public class PSPurgeScheduledTaskLog extends PSPurgeExpiredLog
{
   /**
    * This simply calls {@link IPSSchedulingService#deleteTaskLogsByDate(Date)}
    * 
    * @param beforeDate the job job expiration date, assumed not 
    *    <code>null</code>.
    * @param parameters the parameters of the extension, never <code>null</code>.
    */
   @Override
   protected void purgeLogEntries(Date beforeDate,
         Map<String, String> parameters)
   {
      if (beforeDate == null)
         throw new IllegalArgumentException("beforeDate may not be null.");

      IPSSchedulingService srv = PSSchedulingServiceLocator
            .getSchedulingService();
      srv.deleteTaskLogsByDate(beforeDate);
   }
}
