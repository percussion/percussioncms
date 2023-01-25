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
package com.percussion.share.async;


import com.percussion.foldermanagement.service.IPSFolderService;

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
    public long startJob(String jobType, Object config) throws IPSFolderService.PSWorkflowNotFoundException;
    
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
