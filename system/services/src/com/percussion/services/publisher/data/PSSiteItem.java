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

// Generated Dec 16, 2005 4:46:47 PM by Hibernate Tools 3.1.0 beta1JBIDERC2

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

import com.percussion.services.publisher.IPSSiteItem;

/**
 * Data class for the site item table. This class adds the unpublishing 
 * information to the site item table.
 * 
 * Note, turn off the 2nd level cache for this object since it is always
 * retrieved from the database with complex queries, but never retrieved by
 * its primary key (which will potentially utilize the 2nd level).
 */
@Entity
@Table(name = "PSX_PUBLICATION_SITE_ITEM")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSSiteItem")
@NamedQueries(value =
{
      // get the site items by site, delivery context and content IDs
      @NamedQuery(name = "pssiteitem_query_joined_items_by_ids",
            query = "select i from PSSiteItem i "
            + "where i.siteId = :siteid and i.contextId = :context and "
            + "i.contentId in (:contentIds) and "
            + "i.status = i.operation"),
      @NamedQuery(name = "pssiteitem_server_query_joined_items_by_ids",
            query = "select i from PSSiteItem i "
            + "where i.serverId = :serverid and i.contextId = :context and "
            + "i.contentId in (:contentIds) and "
            + "i.status = i.operation"),
      // the same as "pssiteitem_query_joined_items_by_ids", except the
      // content IDs are in the temporary ID table.
      @NamedQuery(name = "pssiteitem_query_joined_items_by_tempid",  
            query = "select i from PSSiteItem i, PSTempId t "
            + "where i.siteId = :siteid and i.contextId = :context and "
            + "i.contentId = t.pk.itemId AND t.pk.id = :idset AND "
            + "i.status = i.operation"),
      @NamedQuery(name = "pssiteitem_server_query_joined_items_by_tempid",  
            query = "select i from PSSiteItem i, PSTempId t "
            + "where i.referenceId = i.referenceId and "
            + "i.serverId = :serverid and i.contextId = :context and "
            + "i.contentId = t.pk.itemId AND t.pk.id = :idset AND "
            + "i.status = i.operation"),
      @NamedQuery(name = "pssiteitem_query_joined_items", //
            query = "select i from PSSiteItem i, PSSite s "
            + "where i.siteId = s.siteId and "
            + "i.siteId = :siteid and i.contextId = :context and "
            + "i.status = i.operation"),
      @NamedQuery(name = "pssiteitem_pubserver_query_joined_items", //
            query = "select i from PSSiteItem i "
            + "where i.serverId = :serverid and i.contextId = :context and "
            + "i.status = i.operation"),
      @NamedQuery(name = "pssiteitem_query_at_location", //
            query = "select i from PSSiteItem i, PSSite s "
            + "where i.siteId = s.siteId and "
            + "i.siteId = :siteid and i.location = :location and "
            + "i.status = i.operation"),
      /**
       * Gets the (distinct) content IDs of the published items by the specified site and content types
       */
      // query with IN CLAUSE
      @NamedQuery(name = "lastPublishedItemsBySite_InClause", //
            query = "select distinct c.m_contentId from PSSiteItem i, PSComponentSummary c "
            + "where i.siteId = :siteId and "
            + "i.status = 0 and i.operation = 0 and i.contentId = c.m_contentId and "
            + "c.m_contentId in (:contentIds)"),
      // query join with temporary ID table
      @NamedQuery(name = "lastPublishedItemsBySite_TempId", //
            query = "select distinct i.contentId from PSSiteItem i, PSTempId t "
            + "where i.siteId = :siteId and "
            + "i.status = 0 and i.operation = 0 and i.contentId = t.pk.itemId and t.pk.id = :idset")})
            
            
public class PSSiteItem  implements java.io.Serializable, IPSSiteItem
{
   /**
    * The serial id.
    */
   private static final long serialVersionUID = 1L;

   /**
    * The id back to the pub doc information.
    */
   @Id
   public long referenceId;
   
   /**
    * The site id.
    */
   @Basic
   public long siteId;
   
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
    * The reference ID of the origin of the unpublishing operation. It may be
    * <code>null</code> if it is a publishing operation. 
    */
   @Basic
   public Long unpublishRefId;
   
   /**
    * The server id.
    */
   @Basic
   public Long serverId;
   
   /**
    * Hibernate version used to detect unsaved rows and optimistic locking 
    * failures.
    */
   //@Version
   @Basic
   public Integer version = 0;
   
   /**
    * The delivery context used.
    */
   @Basic
   public int contextId;

   /**
    * The unpublishing information that is available for the item. 
    */
   @Lob
   public byte[] unpublishInfo;

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
      PSSiteItem other = (PSSiteItem) obj;
      if (referenceId != other.referenceId)
         return false;
      return true;
   }

   @Override
   public Integer getContentId()
   {
      return contentId;
   }

   @Override
   public String getContentUrl()
   {
      return assemblyUrl;
   }

   @Override
   public Integer getContext()
   {
      return contextId;
   }

   @Override
   public Date getDate()
   {
      return date;
   }

   @Override
   public String getDeliveryType()
   {
      return deliveryType;
   }

   @Override
   public Integer getElapsedTime()
   {
      return elapsed;
   }

   @Override
   public Integer getFolderId()
   {
      return folderId;
   }

   @Override
   public String getLocation()
   {
      return location;
   }

   @Override
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

   @Override
   public int getPage()
   {
      return page == null ? 0 : page.intValue();
   }

   @Override
   public Integer getRevisionId()
   {
      return revisionId;
   }

   @Override
   public long getSiteId()
   {
      return siteId;
   }

   @Override
   public PSSiteItem getSiteItem()
   {
      return this;
   }

   @Override
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

   @Override
   public long getStatusId()
   {
      return statusId;
   }

   @Override
   public Long getTemplateId()
   {
      return templateId;
   }

   @Override
   public Integer getVersionId()
   {
      return version;
   }

   @Override
   public void setContentId(Integer contentid)
   {
      this.contentId = contentid;
   }

   @Override
   public void setContentUrl(String contenturl)
   {
      this.assemblyUrl = contenturl;
   }

   @Override
   public void setContext(Integer context)
   {
      this.contextId = context;
   }

   @Override
   public void setDate(Date pubdate)
   {
      this.date = pubdate;
   }

   @Override
   public void setElapsedTime(Integer elapsetime)
   {
      this.elapsed = elapsetime;
   }

   @Override
   public void setFolderId(Integer id)
   {
      this.folderId = id;
   }

   @Override
   public void setLocation(String location)
   {
      this.location = location;
   }

   @Override
   public void setOperation(Operation puboperation)
   {
      this.operation = (short) puboperation.ordinal();
   }

   @Override
   public void setRevisionId(Integer revisionid)
   {
      this.revisionId = revisionid;
   }

   @Override
   public void setSiteId(long siteid)
   {
      this.siteId = siteid;
   }

   @Override
   public void setStatus(Status pubstatus)
   {
      this.status = (short) pubstatus.ordinal();
   }

   @Override
   public void setTemplateId(Long templateid)
   {
      this.templateId = templateid;
   }

   @Override
   public void setVersionId(Integer versionid)
   {
      this.version = versionid;
   }
   
}
