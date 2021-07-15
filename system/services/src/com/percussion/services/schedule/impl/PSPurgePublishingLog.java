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
package com.percussion.services.schedule.impl;

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
               log.error(e.getMessage());
               log.debug(e.getMessage(), e);
               log.error("Failed to archive publishing log for job ID: {} : {}", jobId, e.getMessage());
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
