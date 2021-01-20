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
package com.percussion.rx.publisher;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.rx.publisher.data.PSDemandWork;
import com.percussion.rx.publisher.data.PSPubItemStatus;
import com.percussion.utils.guid.IPSGuid;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The publisher business service is in charge of publishing content to the
 * publishing servers. Each published edition results in a
 * <code>PSPublishingJob</code>, which queues the edition's content lists and
 * tracks the current progress. If an edition is requested that is currently
 * publishing the call will fail.
 * <p>
 * Information about jobs is held for long period of time after the jobs
 * complete. After this time has past, the next call to
 * {@link #startPublishingJob(IPSGuid, IPSPublishingJobStatusCallback)} will cause "old" jobs to be reaped.
 * 
 * @author dougrand
 */
public interface IPSRxPublisherService
{
   /**
    * Start processing the given edition. This will cause a separate thread
    * to be spawned that is processing the edition asynchronously. The returned
    * job id can be used to query status and control the job.
    * <p>
    * If the edition is already being run then an 
    * {@link IllegalStateException} will be thrown.
    * 
    * @param edition the id of the edition to run, never <code>null</code>
    * @param callback to be notified object when the job is finished. It may be
    *    <code>null</code> if the notification is not needed.
    * 
    * @return the job id
    */
   long startPublishingJob(IPSGuid edition,
         IPSPublishingJobStatusCallback callback);
   
   /**
    * Get the current state of the job.
    * 
    * @param jobId the jobId, must match existing jobId or an
    *            {@link IllegalStateException} will be thrown.
    * @return a copy of the current status. The returned object is newly created
    *         for each call.
    */
   IPSPublisherJobStatus getPublishingJobStatus(long jobId);
   
   /**
    * Checks if the given job is active for this server.
    * 
    * <strong>WARNING the job maybe active on another server</strong>
    * @param jobId a valid job id.
    * @return true if active, false for all other cases.
    */
   boolean isJobActive(long jobId); 
   
   /**
    * Cancel the job. The jobId is pushed to the queues with a high priority
    * message. After receipt, the assembly and delivery managers will ignore
    * messages for that jobId, and the job itself is cancelled.
    * 
    * @param jobId the jobId to cancel, if not a current job then an 
    *       {@link IllegalStateException} will be thrown.
    */
   void cancelPublishingJob(long jobId);
   
   /**
    * Each running edition is represented by a unique job id. This method allows
    * the runtime system to find the corresponding job id for a given edition.
    * @param guid the GUID of the edition, never <code>null</code>.
    * @return the job id of the edition or <code>0</code> if the edition doesn't
    * have a running job.
    */
   long getEditionJobId(IPSGuid guid);
   
   
   /**
    * Returns the edition guid associated with the supplied job.
    * @param jobId Id of the job.
    * @return the edition guid of the job or <code>null</code> if the job doesn't have an associated edition.
    */
   IPSGuid getJobEditionId(long jobId);
   
   /**
    * Queues one or more content items for publishing using the given edition.
    * If the edition is already running, the items are queued and will be
    * published the next time the edition runs. The queue operation returns
    * an opaque identifier that can be used to query the status of the job. 
    * The status is retained for a long period of time, {@link #REAP_TIME}, 
    * and is then discarded.
    * <p>
    * The edition is checked to make sure it contains a single demand generator.
    * <p>
    * The status of this work is unknown, see {@link #getDemandWorkStatus(long)},
    * if the work has not been started.
    * 
    * @param editionid the edition id, must exist
    * @param work the work to publish, never <code>null</code>.
    * @param generator the name of the demand generator used by the edition 
    *                  being published, never <code>null</code> or empty.
    * 
    * @return an opaque id that can be used to check the status of the request.
    * 
    * @throws PSNotFoundException - if there is a issues getting a list of
    *                               editions for the supplied generator or
    *                               if the edition does not exist or does not 
    *                               contain the generator
    */
   long queueDemandWork(int editionid, PSDemandWork work, String generator)
   throws PSNotFoundException;

   /**
    * Queues one or more content items for publishing using the given edition.
    * If the edition is already running, the items are queued and will be
    * published the next time the edition runs. The queue operation returns an
    * opaque identifier that can be used to query the status of the job. The
    * status is retained for a long period of time, {@link #REAP_TIME}, and is
    * then discarded.
    * <p>
    * The edition is checked to make sure it contains the default demand
    * generator.
    * <p>
    * The status of this work is unknown, see {@link #getDemandWorkStatus(long)}
    * , if the work has not been started.
    * 
    * @param editionid the edition id, must exist
    * @param work the work to publish, never <code>null</code>.
    * 
    * @return an opaque id that can be used to check the status of the request.
    * 
    * @throws PSNotFoundException - if there is a issues getting a list of
    *                               editions for the supplied generator or
    *                               if the edition does not exist or does not 
    *                               contain the generator
    */
   long queueDemandWork(int editionid, PSDemandWork work)
      throws PSNotFoundException;
   
   /**
    * Retrieve the current work queued for the edition. The returned work will
    * be removed from the queue. This is used by the demand publishing generator
    * to retrieve the work.
    * 
    * @param edition the edition id
    * @return the work to be done, could be empty but not <code>null</code>.
    */
   Collection<PSDemandWork> getDemandWorkForEdition(int edition);
   
   /**
    * Get the status of a job queued with {@link #queueDemandWork(int, 
    * PSDemandWork)}. If the job is unknown then <code>null</code> is returned,
    * which may means the queued job has not been started.
    * 
    * @param requestId the request id returned from {@link 
    *   #queueDemandWork(int, PSDemandWork)}.
    *   
    * @return the status, or <code>null</code> if the status is unknown.
    */
   IPSPublisherJobStatus.State getDemandWorkStatus(long requestId);

   /**
    * Find and return the matching job for the given request id issued for 
    * a demand work item. The value <code>null</code> will be returned if the
    * request is not yet part of a job.
    * 
    * @param requestid the request id
    * @return the job id, or <code>null</code> if the job is unknown.
    */
   Long getDemandRequestJob(long requestid);

   /**
    * Set the state for a given item being processed in a given job. This method
    * should be called by the publishing system implementation only. This method
    * also updates the publishing log data stored in the database. Data stored
    * for the log is held in memory and updated in clumps to provide adequate
    * performance. It is important to call {@link #flushStatusToDatabase()} to
    * complete the storage of log data.
    * 
    * @param status the status update, never <code>null</code>
    */
   void updateItemState(PSPubItemStatus status);
  
 
}
