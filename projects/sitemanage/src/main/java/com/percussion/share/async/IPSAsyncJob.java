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
 * A job that can be run asynchronously and report status
 * 
 * @author JaySeletz
 */
public interface IPSAsyncJob extends Runnable
{
   
    /**
     * Retrieves the current status of this job.
     * 
     * @return the percent complete for this job as a number between
     * <code>1-100</code> inclusive. Returns <code>-1</code> if the job
     * has terminated abnormally (see {@link #ABORT_STATUS}).
     */
    public int getStatus();
    
    /**
     * Determines if this job has completed.
     * 
     * @return <code>true</code> if the job has completed normally, has aborted, or has been cancelled
     * successfully, <code>false</code> if it has not yet completed.  Jobs must return
     * <code>true</code> before the {@link #run()} method exits to ensure proper freeing of 
     * resources (ideally in a finally block).
     */
    public boolean isCompleted();
    
    /**
     * Determines if this job has been cancelled. Should be polled by derived
     * classes from the {@link #run()} method to determine if the this job should cancel.
     * 
     * @return <code>true</code> if <code>cancelJob()</code> has been called, but
     * does not indicate if the job was successfully cancelled - this can only be
     * determined by a call to <code>isCompleted()</code>.
     */
    public boolean isCancelled();
    
    
    /**
     * Retrieves the ID of this job.
     * 
     * @return the ID.
     * 
     * @throws IllegalStateException if the id has not been initialized.
     */
    public long getId();
    
    /**
     * Sets the ID of this job.  Must be called before running the job.
     * 
     * @param id The job Id.
     */
    public void setId(long id);
    
    /**
     * Sets flag for this job to stop processing regardless of it's status and
     * perform any cleanup required. Callers should then check
     * {@link #isCompleted() isCompleted} to determine when the job has stopped.
     * At this point, if the return from {@link #getStatus()} is less than 100,
     * the job was cancelled, if it is equal to 100, then the cancel request was
     * too late and the job has already run to completion.
     */
    public void cancelJob();
    
    /**
     * Adds a listener to this job
     * 
     * @param listener The listener, may not be <code>null</code>.
     * 
     * @throws IllegalArgumentException If <code>listener</code> is 
     * <code>null</code>.
     */
    public void addJobListener(IPSAsyncJobListener listener);
    
    /**
     * Removes a listener from this job.
     * 
     * @param listener The listener, may not be <code>null</code>.
     * 
     * @throws IllegalArgumentException If <code>listener</code> is 
     * <code>null</code>.
     */
    public void removeJobListener(IPSAsyncJobListener listener);
    
    /**
     * Get this job's current status message.
     * 
     * @return The message, may be <code>null</code>, never empty.
     */
    public String getStatusMessage();
    
    /**
     * Provides the opportunity for the job to perform any initialization.
     * 
     * @param config Implementation specific object that provides any data needed by the job.  Will not be <code>null</code>.
     */
    public void init(Object config) throws IPSFolderService.PSWorkflowNotFoundException;
    
    /**
     * Determine if the job, having completed, can be discarded.  Each job must determine it's own lifecycle once it has
     * completed.  Completed job instances will be retained in memory at least until this method returns <code>true</code>.
     * 
     * @return <code>true</code> if the job can be discarded, <code>false</code> if it should continue to be retained in memory.
     */
    public boolean isDiscarded();
    

    /**
     * Get whatever result that the job may have.  May be called at any time after the job has been started, at least
     * until {@link #isDiscarded()} returns <code>true</code>.
     * 
     * @return The result, may be <code>null</code> if one is not ready, or if the job does not produce a result.
     */
    public Object getResult();
    
    /**
     * Status to indicate that job is aborted.
     */
    public static final int ABORT_STATUS = -1;
    
    /**
     * Status to indicate that the job has completed successfully.
     */
    public static final int COMPLETE_STATUS = 100;

}
