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
package com.percussion.services.publisher.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.publisher.IPSEditionTaskLog;
import com.percussion.utils.guid.IPSGuid;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * This represents a single running of a single edition task. As each task is
 * run, the data is recorded in these records.
 * 
 * @author dougrand
 */
@Entity
@Table(name = "PSX_EDITION_TASK_LOG")
public class PSEditionTaskLog implements IPSEditionTaskLog
{
   @Id
   long referenceId;

   @Version
   Integer version;

   @Basic
   long jobId;

   @Basic
   boolean status;

   @Basic
   Integer elapsed;

   @Basic
   long taskId;

   @Basic
   String message;

   /**
    * Default ctor
    */
   public PSEditionTaskLog() {
      // 
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionTaskLog#getReferenceId()
    */
   public long getReferenceId()
   {
      return referenceId;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionTaskLog#setReferenceId(java.lang.Long)
    */
   public void setReferenceId(long referenceId)
   {
      this.referenceId = referenceId;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionTaskLog#getVersion()
    */
   public Integer getVersion()
   {
      return version;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionTaskLog#setVersion(java.lang.Integer)
    */
   public void setVersion(Integer version)
   {
      this.version = version;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionTaskLog#getJobId()
    */
   public long getJobId()
   {
      return jobId;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionTaskLog#setJobId(java.lang.Long)
    */
   public void setJobId(long jobId)
   {
      this.jobId = jobId;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionTaskLog#getStatus()
    */
   public boolean getStatus()
   {
      return status;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionTaskLog#setStatus(java.lang.Boolean)
    */
   public void setStatus(boolean status)
   {
      this.status = status;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionTaskLog#getElapsed()
    */
   public Integer getElapsed()
   {
      return elapsed;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionTaskLog#setElapsed(java.lang.Integer)
    */
   public void setElapsed(Integer elapsed)
   {
      this.elapsed = elapsed;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionTaskLog#getTaskId()
    */
   public IPSGuid getTaskId()
   {
      return PSGuidUtils.makeGuid(taskId, PSTypeEnum.EDITION_TASK_DEF);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionTaskLog#setTaskId(java.lang.Long)
    */
   public void setTaskId(IPSGuid taskGuid)
   {
      if (taskGuid == null)
         throw new IllegalArgumentException("taskGuid may not be null.");
      
      this.taskId = taskGuid.longValue();
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionTaskLog#getMessage()
    */
   public String getMessage()
   {
      return message;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEditionTaskLog#setMessage(java.lang.String)
    */
   public void setMessage(String message)
   {
      this.message = message;
   }
}
