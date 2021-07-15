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
package com.percussion.rx.publisher;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamException;

import com.percussion.rx.publisher.impl.PSPublishingJob;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.utils.general.PSServiceConfigurationBean;
import com.percussion.utils.guid.IPSGuid;

/**
 * The publisher business service that contains public and non-public API.
 * <p>
 * This publisher business service is in charge of publishing content to the
 * publishing servers. Each published edition results in a
 * <code>PSPublishingJob</code>, which queues the edition's content lists and
 * tracks the current progress. If an edition is requested that is currently
 * publishing the call will fail.
 * <p>
 * Information about jobs is held for long period of time after the jobs
 * complete. After this time has past, the next call to
 * {@link #startPublishingJob(IPSGuid)} will cause "old" jobs to be reaped.
 * 
 * @author YuBingChen
 */
public interface IPSRxPublisherServiceInternal extends IPSRxPublisherService
{
   /**
    * The time before a job status can be removed from the (internal) job status
    * list after the job is started, due to no access. The time is expressed 
    * in milliseconds.
    */
   public static int REAP_TIME = 1000 * 3600;

   /**
    * Removes the status of the given publishing jobs, so that these job IDs
    * will not be returned by {@link #getActiveJobIds()} afterwards.
    * @param jobIds the ID of the publishing jobs.
    */
   void removePublishingJobStatus(Collection<Long> jobIds);
   
   
   /**
    * Called by the publishing handler to indicate that the stop message has
    * arrived, have done the commit process. This causes the job to enter a 
    * completed state.
    * 
    * @param jobId the Job ID.
    * @param hasError <code>true</code> if the encounter an unexpected error
    * during commit process. The error may logged by log4j (into server.log).
    */
   void acknowledgeJobCommit(long jobId, boolean hasError);
   
   /**
    * Get the collection of currently active jobs. Active jobs are those known
    * by the publishing system, whether completed or not, with only the most
    * recent for any one edition returned. This method
    * should be called by the publishing system implementation only.
    * <p>
    * Note, this call will remove the jobs that have finished over
    * {@link #REAP_TIME} before return the remaining jobs.
    * 
    * @return the collection, may be empty but never <code>null</code>.
    */
   Collection<Long> getActiveJobIds();
   
   /**
    * Get the collection of currently active jobs for the specified site.  See
    * {@link #getActiveJobIds()}.
    * 
    * @param siteId if <code>null</code>, then active jobs for all sites will
    * be returned.
    * 
    * @return the collection, may be empty but never <code>null</code>.
    */
   Collection<Long> getActiveJobIds(IPSGuid siteId);
   
   /**
    * Adds additional works for the given job id. This is intended for internal
    * use as part of the paging implementation. The method will dispatch the 
    * items to the given job, registering the work to be done and queuing the 
    * work on the assembly queue. This method will throw a runtime exception
    * if the job is not found or not active. 
    * 
    * @param jobId the job ID for the queued items.
    * @param items the items to queue, never <code>null</code>
    */
   void addWorksForJob(long jobId, List<IPSAssemblyItem> items);
   
   /**
    * Archive the publishing log from the database to an XML file for the 
    * given job ID.
    * 
    * @param jobid the publishing job ID.
    * @param req current request. It may be <code>null</code> if the caller
    *    ignores the returned URL or the caller is a background thread.
    *    
    * @return a URL can be used to view the archived XML file. It may be
    *    <code>null</code> if the request parameter is <code>null</code> or
    *    the default archive location is overridden by 
    *    <code>pubLogArchiveLocation</code> property in server.xml.
    *    
    * @throws IOException if failed to create or write to the archived file. 
    * @throws XMLStreamException if failed to write XML content to the file.  
    */
   String archivePubLog(long jobid, HttpServletRequest req)
      throws IOException, XMLStreamException;
   
   /**
    * Get the directory of the archive publishing log file.
    * @return the directory location, never <code>null</code> or empty.
    */
   String getArchiveLocation();

   /**
    * Gets a publishing job for the specified job ID.
    * 
    * @param jobID the ID of the job.
    * 
    * @return the publishing job. It may be <code>null</code> if cannot 
    * find the job.
    */
   PSPublishingJob getPublishingJob(long jobID);

   /**
    * Gets the configuration bean to access configuration properties.
    *  
    * @return the configurationBean
    */
   PSServiceConfigurationBean getConfigurationBean();
   
   public int flushStatusToDatabase(ConcurrentLinkedQueue<IPSPublisherItemStatus> updateQueue);

}
