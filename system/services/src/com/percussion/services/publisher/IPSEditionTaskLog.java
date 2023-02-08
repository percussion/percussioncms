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
