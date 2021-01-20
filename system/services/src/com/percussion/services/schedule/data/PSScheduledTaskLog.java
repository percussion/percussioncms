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
package com.percussion.services.schedule.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.utils.guid.IPSGuid;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * It contains the information of executing a scheduled task. This creates
 * immutable objects. Note, the second level cache is off (configured in
 * ehcache.xml) because the system does not load individual log entry.
 * 
 * @author Yu-Bing Chen
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSScheduledTaskLog")
@Table(name = "PSX_SCH_TASK_LOG")
public class PSScheduledTaskLog
{
   /**
    * Not to expose the default constructor.
    */
   @SuppressWarnings("unused")
   private PSScheduledTaskLog()
   {}

   /**
    * Constructs a (immutable) task log with the supplied parameters.
    * 
    * @param logId the ID of the log, it may not be <code>null</code>.
    * @param taskId the ID of the task, it may not be <code>null</code>.
    * @param startTime the starting time of the executed task, it may not be
    * <code>null</code>.
    * @param endTime the finished time of the task execution. it may not be
    * <code>null</code>.
    * @param isSuccess <code>true</code> if the task has been executed
    * successfully.
    */
   public PSScheduledTaskLog(IPSGuid logId, IPSGuid taskId, Date startTime,
         Date endTime, boolean isSuccess)
   {
      this.log_id = logId.longValue();
      this.task_id = taskId.longValue();
      this.start_time = startTime;
      this.end_time = endTime;
      this.is_success = isSuccess ? 'Y' : 'N';
   }

   /**
    * Constructs a (immutable) task log with the supplied parameters.
    * 
    * @param logId the ID of the log, it may not be <code>null</code>.
    * @param taskId the ID of the task, it may not be <code>null</code>.
    * @param startTime the starting time of the executed task, it may not be
    * <code>null</code>.
    * @param endTime the finished time of the task execution. it may not be
    * <code>null</code>.
    * @param isSuccess <code>true</code> if the task has been executed
    * successfully.
    * @param problemDesc the description of the failure problem. It may be 
    * <code>null</code> or empty.
    * @param server the name or IP address of the server that executed the job.
    * It may be <code>null</code> or empty if unknown.
    */
   public PSScheduledTaskLog(IPSGuid logId, IPSGuid taskId, Date startTime,
         Date endTime, boolean isSuccess, String problemDesc, String server)
   {
      this(logId, taskId, startTime, endTime, isSuccess);
      this.problem_desc = problemDesc;
      this.server = server;
   }

   @Override
   public boolean equals(Object o1)
   {
      if (!(o1 instanceof PSScheduledTaskLog))
         return false;

      PSScheduledTaskLog other = (PSScheduledTaskLog) o1;

      return new EqualsBuilder().append(log_id, other.log_id).append(task_id,
            other.task_id).append(start_time, other.start_time).append(
            end_time, other.end_time).append(is_success, other.is_success)
            .isEquals();
   }

   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(log_id).append(task_id).append(
            start_time).append(end_time).append(is_success).toHashCode();
   }

   /**
    * Get the ID of the task log.
    * 
    * @return the task log ID, never <code>null</code>.
    */
   public IPSGuid getId()
   {
      return PSGuidUtils.makeGuid(log_id, PSTypeEnum.SCHEDULE_TASK_LOG);
   }

   /**
    * Get the ID of the executed task.
    * 
    * @return the task ID, never <code>null</code>.
    */
   public IPSGuid getTaskId()
   {
      return PSGuidUtils.makeGuid(task_id, PSTypeEnum.SCHEDULED_TASK);
   }

   /**
    * Get the starting time of the task execution.
    * 
    * @return the starting time, never <code>null</code>.
    */
   public Date getStartTime()
   {
      return start_time;
   }

   /**
    * Gets the elapse time of the execution.
    * 
    * @return the elapse time.
    */
   public long getElapsed()
   {
      return end_time.getTime() - start_time.getTime();
   }

   /**
    * Gets the finished time of the task execution.
    * 
    * @return the finished time, never <code>null</code>.
    */
   public Date getEndTime()
   {
      return end_time;
   }

   /**
    * Determines if the execution was successful or not.
    * 
    * @return <code>true</code> if the task execution was successful;
    * otherwise return <code>false</code>.
    */
   public boolean isSuccess()
   {
      return is_success == 'Y';
   }


   /**
    * Get the description of the failure problem.
    * @return the problem description, it may be <code>null</code> or empty.
    */
   public String getProblemDesc()
   {
      return problem_desc;
   }
   
   /**
    * Gets the server name or IP address that invoked the task.
    * @return the server name or IP address. It may be <code>null</code> or
    *    empty.
    */
   public String getServer()
   {
      return server;
   }
   
   /**
    * See {@link #getId()}
    */
   @Id
   private long log_id;

   @Version
   private int version;
   
   /**
    * See {@link #getTaskId()}
    */
   @Basic
   private long task_id;

   /**
    * See {@link #getStartTime()}
    */
   @Basic
   private Date start_time;

   /**
    * See {@link #getEndTime()}
    */
   @Basic
   private Date end_time;

   /**
    * See {@link #isSuccess()}
    */
   @Basic
   private char is_success;

   @Basic
   private String server;
   
   @Lob
   @Basic(fetch = FetchType.EAGER)
   private String problem_desc;
}
