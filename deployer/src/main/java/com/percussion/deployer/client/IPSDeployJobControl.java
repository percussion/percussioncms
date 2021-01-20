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

package com.percussion.deployer.client;

import com.percussion.deployer.error.PSDeployException;

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
