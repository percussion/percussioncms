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
package com.percussion.services.system.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;

/**
 * This object represents a single audit.
 */
public class PSAudit implements Serializable
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = 2299868760195078824L;

   private long id;
   
   private boolean publishable;
   
   private Date eventTime;
   
   private String actor;
   
   private long stateId;
   
   private String stateName;
   
   private long transitionId;
   
   private String transitionName;
   
   private String transitionComment;
   
   private boolean currentRevision;
   
   private boolean editRevision;

   /**
    * Default constructor.
    */
   public PSAudit()
   {
   }
   
   public long getId()
   {
      return id;
   }
   
   public void setId(long id)
   {
      this.id = id;
   }
   
   public String getActor()
   {
      return actor;
   }
   
   public void setActor(String actor)
   {
      this.actor = actor;
   }
   
   public boolean isCurrentRevision()
   {
      return currentRevision;
   }
   
   public void setCurrentRevision(boolean currentRevision)
   {
      this.currentRevision = currentRevision;
   }
   
   public boolean isEditRevision()
   {
      return editRevision;
   }
   
   public void setEditRevision(boolean editRevision)
   {
      this.editRevision = editRevision;
   }
   
   public Date getEventTime()
   {
      return eventTime;
   }
   
   public void setEventTime(Date eventTime)
   {
      this.eventTime = eventTime;
   }
   
   public boolean isPublishable()
   {
      return publishable;
   }
   
   public void setPublishable(boolean publishable)
   {
      this.publishable = publishable;
   }
   
   public long getStateId()
   {
      return stateId;
   }
   
   public void setStateId(long stateId)
   {
      this.stateId = stateId;
   }
   
   public String getStateName()
   {
      return stateName;
   }
   
   public void setStateName(String stateName)
   {
      this.stateName = stateName;
   }
   
   public long getTransitionId()
   {
      return transitionId;
   }
   
   public void setTransitionId(long transitionId)
   {
      this.transitionId = transitionId;
   }
   
   public String getTransitionName()
   {
      return transitionName;
   }
   
   public void setTransitionName(String transitionName)
   {
      this.transitionName = transitionName;
   }
   
   public String getTransitionComment()
   {
      return transitionComment;
   }
   
   public void setTransitionComment(String transitionComment)
   {
      this.transitionComment = transitionComment;
   }

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.ITEM_HISTORY, id);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#setGUID(IPSGuid)
    */
   public void setGUID(IPSGuid newguid) throws IllegalStateException
   {
      if (newguid == null)
         throw new IllegalArgumentException("newguid may not be null");

      if (id != 0)
         throw new IllegalStateException("cannot change existing guid");

      id = newguid.longValue();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSAudit)) return false;
      PSAudit psAudit = (PSAudit) o;
      return getId() == psAudit.getId() && isPublishable() == psAudit.isPublishable() && getStateId() == psAudit.getStateId() && getTransitionId() == psAudit.getTransitionId() && isCurrentRevision() == psAudit.isCurrentRevision() && isEditRevision() == psAudit.isEditRevision() && Objects.equals(getEventTime(), psAudit.getEventTime()) && Objects.equals(getActor(), psAudit.getActor()) && Objects.equals(getStateName(), psAudit.getStateName()) && Objects.equals(getTransitionName(), psAudit.getTransitionName()) && Objects.equals(getTransitionComment(), psAudit.getTransitionComment());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getId(), isPublishable(), getEventTime(), getActor(), getStateId(), getStateName(), getTransitionId(), getTransitionName(), getTransitionComment(), isCurrentRevision(), isEditRevision());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSAudit{");
      sb.append("id=").append(id);
      sb.append(", publishable=").append(publishable);
      sb.append(", eventTime=").append(eventTime);
      sb.append(", actor='").append(actor).append('\'');
      sb.append(", stateId=").append(stateId);
      sb.append(", stateName='").append(stateName).append('\'');
      sb.append(", transitionId=").append(transitionId);
      sb.append(", transitionName='").append(transitionName).append('\'');
      sb.append(", transitionComment='").append(transitionComment).append('\'');
      sb.append(", currentRevision=").append(currentRevision);
      sb.append(", editRevision=").append(editRevision);
      sb.append('}');
      return sb.toString();
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#fromXML(String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }
}

