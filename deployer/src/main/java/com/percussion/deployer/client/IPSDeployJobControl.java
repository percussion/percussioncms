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

package com.percussion.deployer.client;

import com.percussion.error.PSDeployException;

/**
 * Interface returned when a deployment job is started. Provides ability to 
 * query the job for status and cancel the job.
 */
public interface IPSDeployJobControl 
{
   /**
    * Checks the status of the specified Deployment job. The result contains a 
    * value between <code>1-100</code> to indicate the % done.
    * <code>100</code> indicates that the job has completed. A result of -1 
    * indicates that there has been an error and the job completed abnormally. 
    * 
    * @return The status.
    * 
    * @throws PSDeployException if there are any errors.
    */
   public int getStatus() throws PSDeployException;
   
   /**
    * Attempts to stop the currently running job.  Since job is running in its 
    * own thread, it may complete on its own before noticing that it has been 
    * requested to stop.  This method may take some time to return as it will 
    * wait for the job to respond to the cancel request (or finish). 
    * 
    * @return  Returns one of 3 possible result codes:<br> 
    * {@link #JOB_CANCELLED} - Job cancelled successfully<br> 
    * {@link #JOB_COMPLETED} - Job completed before cancelled<br> 
    * {@link #JOB_ABORTED} - Job aborted<br>
    * 
    * @throws PSDeployException if there are any errors.
    */
   public int cancelDeployJob() throws PSDeployException;
   
   /**
    * Get the status message generated with the last call to 
    * {@link #getStatus()}.
    * 
    * @return The status message, may be <code>null</code> or empty.  If 
    * {@link #getStatus()} has not been called, this will return 
    * <code>null</code>.
    * 
    * @throws PSDeployException if there are any errors.
    */
   public String getStatusMessage() throws PSDeployException;
   
   /**
    * Gets the id of the job this controller is handling.
    * 
    * @return The job id.
    */
   public int getJobId();

   /**
    * Constant to indicate the job completed before it could be cancelled.  May 
    * be returned by call to {@link #cancelDeployJob()}.
    */
   public static final int JOB_COMPLETED = 0;

   /**
    * Constant to indicate the job was successfully cancelled.  May be returned 
    * by call to {@link #cancelDeployJob()}.
    */
   public static final int JOB_CANCELLED = 1;

   /**
    * Constant to indicate the job aborted before is could be cancelled.  May be 
    * returned by call to {@link #cancelDeployJob()}.
    */
   public static final int JOB_ABORTED = 2;
   
}
