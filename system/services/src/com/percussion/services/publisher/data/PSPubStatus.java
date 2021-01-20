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
package com.percussion.services.publisher.data;

import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.services.publisher.IPSPubStatus;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

/**
 * The pub status record aggregates all the per document records for a 
 * particular job. Each job will have a separate status id that will be recorded
 * in this table along with the edition id.
 * 
 * @author dougrand
 *
 */
@Entity
@Table(name = "PSX_PUBLICATION_STATUS")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "pubstatus")
@NamedQueries(value= {
@NamedQuery(name="unfinishedPubStatusForServer", 
      query="select p from PSPubStatus p where " +
      "p.endDate is null and " +
      "p.startDate is not null and " +
      "p.endingStatus = " + 0 + 
      " and (p.server is null or p.server = :server)")
})

public class PSPubStatus implements IPSPubStatus
{
   /**
    * The unique status id for this run of the edition.
    */
   @Id
   @Column(name="STATUS_ID")
   private long statusId;
   
   /**
    * Hibernate version used to detect unsaved rows and optimistic locking 
    * failures.
    */
   @SuppressWarnings("unused")
   @Version
   @Column(name="VERSION")
   private Integer version;
   
   /**
    * The Id of the server where it is being published
    */
   @Basic
   @Column(name="PUBSERVERID")
   private Long pubServerId;
   
   /**
    * The end date of the edition being run.
    */
   @Basic
   @Column(name="END_DATE")
   private Date endDate;
   
   /**
    * The start date of the edition being run.
    */
   @Basic
   @Column(name="START_DATE")
   private Date startDate;
      
   /**
    * The edition id.
    */
   @Basic
   @Column(name="EDITION_ID")
   private long editionId;
   
   /**
    * The ending status. It is either <code>null</code> or one of the
    * values defined by ENDED_XXX.
    */
   @Basic
   @Column(name="ENDING_STATUS")
   private Integer endingStatus = EndingState.STARTED.ordinal();
   
   @Basic
   @Column(name="SERVER")
   private String server;
   
   /**
    * A flag that indicates if the record has been "purged", but needs to be 
    * maintained because it is referenced by a site item.
    * It is 'Y' if the record is marked as hidden; otherwise it is not hidden.
    * Default to not hidden. 
    */
   @Basic
   @Column(name="HIDDEN")
   private Character hidden;
   
   /**
    * The number of delivered items.
    */
   @Basic
   private int deliveredCount;
   
   /**
    * The number of removed items.
    */
   @Basic
   private int removedCount;
   
   /**
    * the number of items that failed to publish or unpublish.
    */
   @Basic
   private int failedCount;
   
   /**
    * Default constructorr, needed by hibernate.
    */
   @SuppressWarnings("unused")
   private PSPubStatus()
   {
      //
   }

   /**
    * Creates an instance of publishing job from the given parameters.
    * 
    * @param statusId the id (also the primary key in the database) of the
    *    publishing job.
    * @param editionId the Edition ID of the publishing job.
    * @param startDate the starting date/time of the publishing job, never
    *    <code>null</code>.
    */
   public PSPubStatus(long statusId, long editionId, Date startDate)
   {
      assert startDate != null;
      
      this.statusId = statusId;
      this.editionId = editionId;
      this.startDate = startDate;
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSPubStatus#getStatusId()
    */
   public long getStatusId()
   {
      return statusId;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSPubStatus#getEndDate()
    */
   public Date getEndDate()
   {
      return endDate;
   }
   
   /**
    * Set the end date and time of the publishing job.
    * 
    * @param eDate the ending date/time of the publishing job. Never 
    * <code>null</code>. 
    */
   public void setEndDate(Date eDate)
   {
      assert eDate != null;
      
      endDate = eDate;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSPubStatus#getStartDate()
    */
   public Date getStartDate()
   {
      return startDate;
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSPubStatus#getPubServerId()
    */
   public Long getPubServerId()
   {
      return pubServerId;
   }
   
   /**
    * Set the publish server id
    * 
    * @param pubServerId
    */
   public void setPubServerId(Long pubServerId)
   {
      this.pubServerId = pubServerId;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSPubStatus#getEditionId()
    */
   public long getEditionId()
   {
      return editionId;
   }

   /**
    * Set the ending status.
    * 
    * @param ending the ending status, never <code>null</code>.
    */
   public void setEndingStatus(EndingState ending)
   {
      assert ending != null;
      
      endingStatus = ending.ordinal();
   }
   
   /*
    * //see base class method for details
    */
   public EndingState getEndingState()
   {
      if (endingStatus == null || endingStatus == EndingState.STARTED.ordinal())
         return EndingState.STARTED;
      
      if (endingStatus == EndingState.COMPLETED.ordinal())
         return EndingState.COMPLETED;

      if (endingStatus == EndingState.COMPLETED_W_FAILURE.ordinal())
         return EndingState.COMPLETED_W_FAILURE;

      if (endingStatus == EndingState.CANCELED_BY_USER.ordinal())
         return EndingState.CANCELED_BY_USER;

      if (endingStatus == EndingState.RESTARTNEEDED.ordinal())
         return EndingState.RESTARTNEEDED;
      
      return EndingState.ABORTED;
   }   
   
   
   /**
    * Get the number of delivered items.
    * @return the count of items delivered.
    */
   public int getDeliveredCount()
   {
      return deliveredCount;
   }
   
   /**
    * Set the number of delivered items
    * @param numOfDelivered the number.
    */
   public void setDelivered(int numOfDelivered)
   {
      deliveredCount = numOfDelivered;
   }

   /**
    * Get the number of removed items.
    * @return the count of items removed.
    */
   public int getRemovedCount()
   {
      return removedCount;
   }

   /**
    * Sets the number of removed items.
    * @param numOfRemoved the new count.
    */
   public void setRemoved(int numOfRemoved)
   {
      removedCount = numOfRemoved;
   }
   
   /**
    * Get the number of items that failed from either assembly or delivery.
    * @return the count of items that failed from either assembly or delivery.
    */
   public int getFailedCount()
   {
      return failedCount;
   }
   
   /**
    * Set the number of failed items.
    * @param numOfFailed the new count.
    */
   public void setFailed(int numOfFailed)
   {
      failedCount = numOfFailed;
   }

   public boolean isHidden()
   {
      return hidden != null && hidden.charValue()== 'Y';
   }
   
   public String getServer()
   {
      return server;
   }
   
   /**
    * Sets the server name and port.
    * @param server the server name and port, it may be <code>null</code>.
    */
   public void setServer(String server)
   {
      this.server = server;
   }

   @Override
   public String toString()
   {
      return "PSPubStatus [statusId=" + statusId + ", endingStatus="
            + endingStatus + ", startDate=" + startDate + ", endDate="
            + endDate + ", editionId=" + editionId + ", server=" + server
            + ", deliveredCount=" + deliveredCount + ", removedCount="
            + removedCount + ", failedCount=" + failedCount + ", hidden="
            + hidden + "]";
   }
   
   
}
