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

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSSiteItem.Operation;
import com.percussion.services.publisher.IPSSiteItem.Status;

/**
 * This class represents a single publishing event. It may contain messages
 * from assembly and/or delivery. Each item in an edition is recorded in this
 * on a per location basis. Records from this table are retained for site items
 * purposes even after the log is purged. Records that are retained for site 
 * items, but which have been "purged" will be marked hidden.
 * <p>
 * A pub item is created on the first message arriving at the status handler
 * for a particular reference id. This allows multiple messages for a given
 * reference id (a.k.a. a specific work item) to be merged into one record.
 */
@Entity
@Table(name = "PSX_PUBLICATION_DOC")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "pubstatus")
@NamedQueries(value =
{
      @NamedQuery(name = "item_completion", query = 
         "select item.statusId, count(item.referenceId) from PSPubItem item "
            + "where item.status = :status and item.operation = :operation "
            + "and item.statusId in (:ids) group by item.statusId"),
      /*
       * For the following queries, note the information from the 
       * method description: 
       * 
       * The first element in the information is the delivery type
       * name, the second is the reference ID and the third is the 
       * unpublishing info as a <code>byte[]</code> array. The unpublishing
       * information may be <code>null</code>. The fourth is the folder id.
       */
      @NamedQuery(name = "site_item_and_doc", query = 
         "select item.deliveryType, item.referenceId, item.unpublishInfo, item.folderId " 
            + "from PSSiteItem item "
            + "where item.contentId = :contentId and " 
            + "item.templateId = :templateId and "
            + "item.siteId = :siteId and item.contextId = :context and "
            + "item.location = :location"),
            
      @NamedQuery(name = "site_item_and_doc_with_server_id", query = 
         "select item.deliveryType, item.referenceId, item.unpublishInfo, item.folderId " 
            + "from PSSiteItem item "
            + "where item.contentId = :contentId and " 
            + "item.templateId = :templateId and "
            + "item.serverId = :serverId and item.contextId = :context and "
            + "item.location = :location"),

        @NamedQuery(name = "getPostDate", query =
                "select min(item.date) from PSPubItem item "
                        + "where item.contentId = :contentId ")
            
})
public class PSPubItem implements java.io.Serializable, IPSPubItemStatus
{
   /**
    * Serial id
    */
   private static final long serialVersionUID = 11235143613461461L;

   /**
    * The reference id, this is the unique id per row.
    */
   @Id
   public long referenceId;
  
   
   /**
    * Hibernate version used to detect unsaved rows and optimistic locking 
    * failures.
    */
   //@Version
   @Basic
   public Integer version = 0;
   
   /**
    * A flag that indicates if the record has been "purged", but needs to be 
    * maintained because it is referenced by a site item.
    * It is 'Y' if the record is marked as hidden; otherwise it is not hidden.
    * Default to not hidden. 
    */
   @Basic
   public Character hidden;
   
   /**
    * The reference ID of the origin of the unpublishing operation. It may be
    * <code>null</code> if it is a publishing operation. 
    */
   @Basic
   public Long unpublishRefId;
   
   /**
    * The status id is the foreign key joining this back to the 
    * PSX_PUBLICATION_STATUS table represented by {@link PSPubStatus}.
    */
   @Basic
   public Long statusId;
   
   /**
    * References the content id that is being published.
    */
   @Basic
   public int contentId;
   
   /**
    * References the revision of the content.
    */
   @Basic
   public int revisionId;
   
   /**
    * If present, this references the folder that contains the content item.
    */
   @Basic
   public Integer folderId;
   
   /**
    * If present, this references the template that was used to assemble
    * the item.
    */
   @Basic
   public Long templateId;
   
   /**
    * If present, this is the filesystem location of the item published.
    */
   @Basic
   public String location;
   
   /**
    * If present, this is the page number for the item. Only used for paged
    * items.
    */
   @Basic
   public Integer page;
   
   /**
    * The date that the item was published.
    */
   @Basic
   @Column(name="PUBLISHING_DATE")
   public Date date;
   
   /**
    * The operation. The value here is from the ordinal of {@link Operation}.
    */
   @Basic
   public Short operation;
   
   /**
    * The assembly url. This is going to be a synthetic assembly url as the
    * actual assembly is done "internally" via a service call.
    */
   @Basic
   public String assemblyUrl;
   
   /**
    * The elapsed time for the assembly in milliseconds.
    */
   @Basic
   public Integer elapsed;
   
   /**
    * The status of the operation, the value here is from the ordinal of
    * {@link Status}. Defaults to {@link Status#UNDEFINED}.
    */
   @Basic
   public Short status = (short) Status.UNDEFINED.ordinal();
   
   /**
    * The delivery type used to deliver the content.
    */
   @Basic
   public String deliveryType;
   
   /**
    * Any messages, generally from failures, will be recorded here. Multiple
    * messages should be separated by newlines.
    */
   @Lob
   @Basic(fetch = FetchType.EAGER)
   public String message;

   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPubItemStatus#getReferenceId()
    */
   public long getReferenceId()
   {
      return referenceId;
   }

   /**
    * Gets the reference ID of the origin of the unpublishing operation.
    * 
    * @return the unpublish reference ID. It may be <code>null</code> if it is
    * a publishing operation.
    */
   public Long getUnpublishRefId()
   {
      return unpublishRefId;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPubItemStatus#getStatusId()
    */
   public long getStatusId()
   {
      return statusId;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPubItemStatus#getContentId()
    */
   public int getContentId()
   {
      return contentId;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPubItemStatus#getRevisionId()
    */
   public int getRevisionId()
   {
      return revisionId;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPubItemStatus#getFolderId()
    */
   public Integer getFolderId()
   {
      return folderId;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPubItemStatus#getTemplateId()
    */
   public Long getTemplateId()
   {
      return templateId;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPubItemStatus#getLocation()
    */
   public String getLocation()
   {
      return location;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPubItemStatus#getDate()
    */
   public Date getDate()
   {
      return date;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPubItemStatus#getDeliveryType()
    */
   public String getDeliveryType()
   {
      return deliveryType;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPubItemStatus#getOperation()
    */
   public Operation getOperation()
   {
      Operation rval = null;
      for(Operation r : Operation.values())
      {
         if (r.ordinal() == operation)
         {
            rval = r;
            break;
         }
      }
      return rval;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPubItemStatus#getAssemblyUrl()
    */
   public String getAssemblyUrl()
   {
      return assemblyUrl;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPubItemStatus#getElapsed()
    */
   public Integer getElapsed()
   {
      return elapsed;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPubItemStatus#getStatus()
    */
   public Status getStatus()
   {
      Status rval = null;
      for(Status s : Status.values())
      {
         if (s.ordinal() == status)
         {
            rval = s;
            break;
         }
      }
      return rval;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPubItemStatus#getHidden()
    */
   public boolean isHidden()
   {
      return hidden != null && hidden.charValue()== 'Y';
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSPubItemStatus#getMessage()
    */
   public String getMessage()
   {
      return message;
   }

   

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + (int) (referenceId ^ (referenceId >>> 32));
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PSPubItem other = (PSPubItem) obj;
      if (referenceId != other.referenceId)
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "PSPubItem [statusId=" + statusId + ", contentId=" + contentId
            + ", revisionId=" + revisionId + ", folderId=" + folderId
            + ", templateId=" + templateId + ", referenceId=" + referenceId
            + ", assemblyUrl=" + assemblyUrl + ", hidden=" + hidden
            + ", unpublishRefId=" + unpublishRefId + ", location=" + location
            + ", page=" + page + ", date=" + date + ", operation=" + operation
            + ", elapsed=" + elapsed + ", status=" + status + ", deliveryType="
            + deliveryType + ", message=" + message + "]";
   }
   
   
   
   
}
