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
