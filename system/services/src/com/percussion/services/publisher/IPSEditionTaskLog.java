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
package com.percussion.services.publisher;

import com.percussion.utils.guid.IPSGuid;

/**
 * This represents a single running of a single edition task. As each task is
 * run, the data is recorded in these records.
 * 
 * @author dougrand
 */
public interface IPSEditionTaskLog
{

   /**
    * The reference id is a unique value generated for each task and each item
    * published. The reference id will not recycle within any reasonable time
    * period.
    * 
    * @return the referenceId, may be <code>null</code> for a new object that
    *         hasn't been initialized.
    */
   public abstract long getReferenceId();

   /**
    * @param referenceId the referenceId to set
    */
   public abstract void setReferenceId(long referenceId);

   /**
    * The hibernate version for optimistic locking and tracking persistent
    * objects.
    * 
    * @return the version, <code>null</code> if the object hasn't been
    *         persisted yet.
    */
   public abstract Integer getVersion();

   /**
    * @param version the version to set
    */
   public abstract void setVersion(Integer version);

   /**
    * A reference to the specific job that this task was run for.
    * 
    * @return the jobId, <code>null</code> only if uninitialized.
    */
   public abstract long getJobId();

   /**
    * @param jobId the jobId to set
    */
   public abstract void setJobId(long jobId);

   /**
    * @return the status, <code>true</code> if the task executed successfully.
    */
   public abstract boolean getStatus();

   /**
    * @param status the status to set
    */
   public abstract void setStatus(boolean status);

   /**
    * @return the elapsed time to run the task in milliseconds
    */
   public abstract Integer getElapsed();

   /**
    * @param elapsed the elapsed to set
    */
   public abstract void setElapsed(Integer elapsed);

   /**
    * The referenced task that was executed.
    * 
    * @return the taskId, never <code>null</code> if the instance has been
    *         initialized.
    */
   public abstract IPSGuid getTaskId();

   /**
    * @param taskId the taskId to set
    */
   public abstract void setTaskId(IPSGuid taskId);

   /**
    * @return the message, if any, from the executed task. May be a failure 
    * message or just feedback from the execution.
    */
   public abstract String getMessage();

   /**
    * @param message the message to set
    */
   public abstract void setMessage(String message);

}
