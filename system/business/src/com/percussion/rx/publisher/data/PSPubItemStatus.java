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
package com.percussion.rx.publisher.data;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.rx.publisher.IPSPublisherItemStatus;
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Report the status for a single item being published. Sent by the delivery
 * handler and assembly handler as items are completed or fail.
 * <p>
 * When this message is sent for the delivered or failed case, the message is
 * used to update the site items and pub items table. There is information that
 * must be filled out for these cases, such data is noted in the accessor
 * methods. Data required for all uses of this message is contained in the ctor.
 * 
 * @author dougrand
 */
public class PSPubItemStatus implements Serializable, IPSPublisherItemStatus
{
   /**
    * The serialized id.
    */
   private static final long serialVersionUID = 1L;

   /**
    * The reference id, which was assigned by the publisher job. Reference ids
    * are globally unique, but are based on long integers which will essentially
    * never repeat.
    */
   private long m_referenceId;

   /**
    * The reference ID of the origin of the unpublishing operation. 
    */
   private Long m_unpublishRefId;

   private Long m_pubServerId;
   
   /**
    * The job being processed, assigned in the publishing system. Will never
    * repeat.
    */
   private long m_jobId;

   /**
    * The status of the given item.
    */
   private IPSPublisherJobStatus.ItemState m_state;

   /**
    * The elapsed time for the assembly.
    */
   private int m_elapsed;

   /**
    * The published date - set on construction since this is essentially the
    * same as the delivery time. Not mutable.
    */
   private Date m_publishedDate = new Date();

   /**
    * The assembly url.
    */
   private String m_assemblyUrl;

   /**
    * The published location relative to the destination site root.
    */
   private String m_publishedLocation;

   /**
    * The id of the published item.
    */
   private IPSGuid m_id;
   
   /**
    * The folder id of the published item.
    */
   private IPSGuid m_folderId;

   /**
    * The id of the published template.
    */
   private IPSGuid m_templateId;

   /**
    * <code>true</code> for publishing operations and false otherwise.
    */
   private boolean m_publish = true;
   
   /**
    * The site id.
    */
   private IPSGuid m_siteId;
   
   /**
    * The context.
    */
   private int m_deliveryContext;
   
   /**
    * The page number or <code>null</code> for no page number.
    */
   private Integer m_page;

   /**
    * The reference to the parent item if this is the status for a child
    * page, <code>null</code> otherwise.
    */
   private Long m_parentPageReference;
   
   /**
    * Information that can be used to unpublish the item
    */
   private byte[] m_unpublishingInformation;
   
   /**
    * The message or messages associated with the status.
    */
   private List<String> m_messages = new ArrayList<>();
   
   /**
    * The delivery type.
    */
   private String m_deliveryType;


   /**
    * Constructor.
    * 
    * @param refid the reference id
    * @param jobid the job id
    * @param state the state, never <code>null</code>
    */
   public PSPubItemStatus(long refid, long jobid, long pubServerId, int deliveryContext,
         IPSPublisherJobStatus.ItemState state) {
      if (state == null)
      {
         throw new IllegalArgumentException("state may not be null");
      }
      m_referenceId = refid;
      m_jobId = jobid;
      m_state = state;
      m_deliveryContext = deliveryContext; 
      m_pubServerId = pubServerId;
   }

   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.data.IPSPublisherItemStatus#getReferenceId()
    */
   public long getReferenceId()
   {
      return m_referenceId;
   }

   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.data.IPSPublisherItemStatus#getJobId()
    */
   public long getJobId()
   {
      return m_jobId;
   }

   public Long getPubServerId()
   {
      return m_pubServerId;
   }
   
   public void setPubServerId(Long id)
   {
      m_pubServerId = id;
   }
   
   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.data.IPSPublisherItemStatus#getState()
    */
   public IPSPublisherJobStatus.ItemState getState()
   {
      return m_state;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj);
   }

   /* (non-Javadoc) 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return (int) m_referenceId;
   }

   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.data.IPSPublisherItemStatus#getAssemblyUrl()
    */
   public String getAssemblyUrl()
   {
      return m_assemblyUrl;
   }

   /**
    * Set the assembly url, see {@link #getAssemblyUrl()} for details.
    * 
    * @param assemblyUrl the assembly url, may be <code>null</code> or empty.
    */
   protected void setAssemblyUrl(String assemblyUrl)
   {
      m_assemblyUrl = assemblyUrl;
   }

   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.data.IPSPublisherItemStatus#getElapsed()
    */
   public int getElapsed()
   {
      return m_elapsed;
   }

   /**
    * Set the elapsed time in milliseconds.
    * 
    * @param elapsed the elapsed time in milliseconds, should be <code>0</code>
    * or nearly so for unpublish and error cases.
    */
   protected void setElapsed(int elapsed)
   {
      m_elapsed = elapsed;
   }

   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.data.IPSPublisherItemStatus#getId()
    */
   public IPSGuid getId()
   {
      return m_id;
   }
   
   /**
    * Set the guid of the content item.
    * 
    * @param id the content item guid, may be <code>null</code>.
    */
   protected void setId(IPSGuid id)
   {
      m_id = id;
   }
 
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSPublisherItemStatus#getFolderId()
    */
   public IPSGuid getFolderId()
   {
      return m_folderId;
   }

   /**
    * @param folderId the folderId to set, may be <code>null</code>.
    */
   public void setFolderId(IPSGuid folderId)
   {
      m_folderId = folderId;
   }

   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.data.IPSPublisherItemStatus#isPublish()
    */
   public boolean isPublish()
   {
      return m_publish;
   }

   /**
    * Set whether this is a publish request, see {@link #isPublish()} for 
    * semantics.
    * 
    * @param publish new value
    */
   public void setPublish(boolean publish)
   {
      m_publish = publish;
   }

   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.data.IPSPublisherItemStatus#getPublishedLocation()
    */
   public String getPublishedLocation()
   {
      return m_publishedLocation;
   }

   /**
    * Set the published location.
    * @param publishedLocation may be <code>null</code> or empty for a 
    * non-terminal message.
    */
   protected void setPublishedLocation(String publishedLocation)
   {
      m_publishedLocation = publishedLocation;
   }

   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.data.IPSPublisherItemStatus#getTemplateId()
    */
   public IPSGuid getTemplateId()
   {
      return m_templateId;
   }

   /**
    * Set the published template GUID.
    * 
    * @param templateId the new value, it may be <code>null</code> for an
    * unpublished item.
    */
   protected void setTemplateId(IPSGuid templateId)
   {
      m_templateId = templateId;
   }

   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.data.IPSPublisherItemStatus#getPublishedDate()
    */
   public Date getPublishedDate()
   {
      return m_publishedDate;
   }

   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.data.IPSPublisherItemStatus#getSiteId()
    */
   public IPSGuid getSiteId()
   {
      return m_siteId;
   }

   /**
    * Set the site id
    * @param siteId the site id, might be <code>null</code>.
    */
   public void setSiteId(IPSGuid siteId)
   {
      m_siteId = siteId;
   }
   
   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.data.IPSPublisherItemStatus#getContext()
    */
   public int getDeliveryContext()
   {
      return m_deliveryContext;
   }

   /**
    * Set the delivery context.
    * 
    * @param context
    */
   public void setDeliveryContext(int deliveryContext)
   {
      m_deliveryContext = deliveryContext;
   }
   
   

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSPublisherItemStatus#getUnpublishingInformation()
    */
   public byte[] getUnpublishingInformation()
   {
      return m_unpublishingInformation;
   }

   /**
    * @param unpublishingInformation the unpublishingInformation to set
    */
   public void setUnpublishingInformation(byte[] unpublishingInformation)
   {
      m_unpublishingInformation = unpublishingInformation;
   }
   
   /*
    * //see base class method for details
    */
   public Long getUnpublishRefId()
   {
      return m_unpublishRefId;
   }

   /**
    * @return the deliveryType
    */
   public String getDeliveryType()
   {
      return m_deliveryType;
   }

   /**
    * @param deliveryType the deliveryType to set
    */
   public void setDeliveryType(String deliveryType)
   {
      m_deliveryType = deliveryType;
   }

   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSPublisherItemStatus#getMessages()
    */
   public String[] getMessages()
   {
      String[] rval = new String[m_messages.size()];
      m_messages.toArray(rval);
      return rval;
   }
   
   /**
    * Add a message to the list
    * @param message the message, never <code>null</code> or empty.
    */
   public void addMessage(String message)
   {
      if (StringUtils.isBlank(message))
      {
         throw new IllegalArgumentException("message may not be null or empty");
      }
      m_messages.add(message);
   }
   
   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSPublisherItemStatus#getPage()
    */
   public Integer getPage()
   {
      return m_page;
   }
   
   /**
    * Set the page number
    * @param page the page number, may be <code>null</code> 
    */
   public void setPage(Integer page)
   {
      m_page = page;
   }

   /* (non-Javadoc)
    * @see com.percussion.rx.publisher.IPSPublisherItemStatus#getParentPageReferenceId()
    */
   public Long getParentPageReferenceId()
   {
      return m_parentPageReference;
   }
   
   /**
    * Set the parent reference id
    * @param refid the parent reference id, may be <code>null</code>.
    */
   public void setParentPageReferenceId(Long refid)
   {
      m_parentPageReference = refid;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this,
            ToStringStyle.MULTI_LINE_STYLE);
   }

   /**
    * Extract relevant information from an assembly work item. Used to do 
    * the bulk of the initialization for the after assembly case.
    * 
    * @param work the work item, never <code>null</code>.
    */
   public void extractInfo(IPSAssemblyItem work)
   {
      setAssemblyUrl(work.getAssemblyUrl());
      setElapsed(work.getElapsed());
      setId(work.getId());
      setPublish(work.isPublish());
      setPublishedLocation(work.getDeliveryPath());
      setSiteId(work.getSiteId());
      setTemplateId(work.getOriginalTemplateGuid());
      setDeliveryContext(work.getDeliveryContext());
      setPage(work.getPage());
      setParentPageReferenceId(work.getParentPageReferenceId());
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      if (work.getFolderId() > 0)
      {
         setFolderId(gmgr.makeGuid(new PSLocator(work.getFolderId())));
      }
      else
      {
         setFolderId(null);
      }
      setDeliveryType(work.getDeliveryType());
      m_unpublishRefId = work.getUnpublishRefId();
   }

}
