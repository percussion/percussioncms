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

import com.percussion.error.PSExceptionUtils;
import com.percussion.rx.publisher.IPSRxPublisherServiceInternal;
import com.percussion.rx.publisher.PSRxPubServiceInternalLocator;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This is used to purge publishing log entries older than a specified date.
 *
 * The additional context variable can be used in notification template is
 * : <TABLEBORDER="1">
 * <TR>
 * <TH>Variable Name</TH>
 * <TH>Description</TH>
 * </TR>
 * <TR>
 * <TD>$enableArchive</TD>
 * <TD>Enable archiving the to be purged logs before purging them. 
 * The logs will be archived if this is true; otherwise the logs will be
 * just purged from the database. The location of the archived files is
 * specified by the archiveLocation property of sys_rxpublisherservice bean</TD>
 * </TR>
 * </TABLE>
 *
 * @author Yu-Bing Chen
 */
public class PSPurgePublishingLog extends PSPurgeExpiredLog
{
   /**
    * Deletes all publishing job logs that were executed before the given date.
    * 
    * @param beforeDate the job job expiration date, assumed not 
    *    <code>null</code>.
    * @param parameters the parameters of the extension, never <code>null</code>.
    */
   @Override
   protected void purgeLogEntries(Date beforeDate, Map<String, String> parameters)
   {
      if (beforeDate == null)
         throw new IllegalArgumentException("beforeDate may not be null.");
      if (parameters == null)
         throw new IllegalArgumentException("parameters may not be null.");
      
      String enableValue = parameters.get("enableArchive");
      boolean enableArchive = "true".equalsIgnoreCase(enableValue);
      
      IPSPublisherService srv = PSPublisherServiceLocator.getPublisherService();
      IPSRxPublisherServiceInternal rxsrv = PSRxPubServiceInternalLocator
            .getRxPublisherService();

      List<Long> jobIds = srv.findExpiredAndHiddenJobs(beforeDate);
      for (Long jobId : jobIds)
      {
         if (enableArchive)
         {
            try
            {
               rxsrv.archivePubLog(jobId, null);
            }
            catch (Exception e)
            {
               log.error(PSExceptionUtils.getMessageForLog(e));
               log.debug(PSExceptionUtils.getDebugMessageForLog(e));
               log.error("Failed to archive publishing log for job ID: {} : {}", jobId,PSExceptionUtils.getMessageForLog(e));
            }
         }
         
         srv.purgeJobLog(jobId);
      }
   }
   
   /**
    * logger for this class.
    */
   private static final Logger log = LogManager.getLogger(PSPurgePublishingLog.class);
}
