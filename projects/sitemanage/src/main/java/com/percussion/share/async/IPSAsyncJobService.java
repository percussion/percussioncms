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
package com.percussion.share.async;


/**
 * Service to start and manage jobs that need to run asynchronously.  
 * 
 * @author JaySeletz
 *
 */
public interface IPSAsyncJobService
{
    /**
     * Starts a job of the specified type.
     * 
     * @param jobType The type, must match a configured job type, not <code>null<code/> or empty.
     * @param config The configuration object expected by the specified type of job, may be <code>null</code> if not expected.
     * 
     * @return A job id, used to query for job status and to cancel the job.
     */
    public long startJob(String jobType, Object config);
    
    /**
     * Checks the status of the specified job. The result contains
     * a value between <code>1-100</code> to indicate the % done and a message.
     * <code>100</code> indicates that the job has completed successfully.  If
     * the job has terminated abnormally, <code>-1</code> is returned and
     * the message will indicate the error.  Once a completed or terminated status 
     * has been returned, the job status is no longer available.
     * 
     * @param jobId The id of the job, must be a valid job id.
     * 
     * @return The current status, or <code>null</code> if the job does not exist 
     * or if the job is no longer available.
     */
    public PSAsyncJobStatus getJobStatus(long jobId);
    
    /**
     * Attempts to stop the currently running job. Since job is running in its
     * own thread, it may complete on its own before noticing that it has been
     * requested to stop. This method does not return until the job is no longer running.
     * 
     * @param jobId The id of the job.  If not a currently running job, it is assumed the job has 
     * completed and the method simply returns. 
     */
    public void cancelJob(long jobId);

    /**
     * Get any result that the job might return.  The result returned is implementation
     * specific to the job.
     * 
     * @param jobId The id of the job.  If not a currently running job, it is assumed the job has 
     * completed and the method simply returns.
     * 
     * @return The result, may be <code>null</code> if the job does not return a result or if the
     * job is no longer available.
     */
    public Object getJobResult(long jobId);
    
}
