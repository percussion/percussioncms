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

import java.util.Date;

/**
 * Each publishing job that runs has a status record. This keeps track of when
 * the job started and ended, what edition it was created for, and it forms the
 * parent record to aggregate all of the item and edition task logging for the
 * job.
 * 
 * @author dougrand
 *
 */
public interface IPSPubStatus
{
   /**
    * The ending state of a publishing job.
    */
   public enum EndingState
   {
      /**
       * Starting status for a publishing job.
       */
      STARTED,
      
      /**
       * The status for a publishing job is finished normally.
       */
      COMPLETED,

      /**
       * The status for a publishing job is finished, but with failed items, 
       * edition tasks, Content List, commit or un-publishing. 
       */
      COMPLETED_W_FAILURE,

      /**
       * The status for a publishing job is canceled by a user.
       */
      CANCELED_BY_USER,

      /**
       * The status for a publishing job is terminated abnormally.
       */
      ABORTED,

      /**
       * The status for a publishing job is terminated abnormally as new datasource not found.
       */
      RESTARTNEEDED
   }

   /**
    * @return the statusId which identifies a particular job.
    */
   public long getStatusId();

   /**
    * @return the endDate, which is when the job completed. It may be 
    * <code>null</code> if it hasn't finished or abnormally shutdown in the
    * middle of publishing.
    */
   public Date getEndDate();

   /**
    * @return the startDate, which is when the job started.
    */
   public Date getStartDate();

   /**
    * @return the pubServerId, which belongs to the server where it is being published.
    */
   public Long getPubServerId();
   
   /**
    * @return the associated editionId
    */
   public long getEditionId();
   
   /**
    * @return the ending status of the publishing job, never <code>null</code>. 
    * If it is {@link EndingStatus#STARTED}, then it may still running or it may 
    * ended abnormally (such as server shutdown in the middle of publishing run).
    */
   public EndingState getEndingState();
   
   /**
    * Get the number of delivered items.
    * @return the count of items delivered. It may be <code>null</code> if not
    * defined.
    */
   public int getDeliveredCount();

   /**
    * Get the number of removed items.
    * @return the count of items removed. It may be <code>null</code> if not
    * defined.
    */
   public int getRemovedCount();

   /**
    * Get the number of items that failed from either assembly or delivery.
    * @return the count of items that failed from either assembly or delivery.
    * It may be <code>null</code> if not defined.
    */
   public int getFailedCount();

   /**
    * Is the publishing job hidden from the list view?
    * @return <code>true</code> if this job is hidden from the list view.
    */
   public boolean isHidden();

   /**
    * Gets the name and port of the server. It is in the format of <server-name>:<port>.
    * @return the server and and port. It may be <code>null</code>.
    */
   public abstract String getServer();

}
