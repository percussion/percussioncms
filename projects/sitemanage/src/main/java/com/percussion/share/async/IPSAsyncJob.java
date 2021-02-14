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
    public void init(Object config);
    
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
