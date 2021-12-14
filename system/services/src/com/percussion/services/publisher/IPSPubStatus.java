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
