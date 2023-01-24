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
package com.percussion.services.workflow.data;

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

/**
 * Represents a workflow notification definitions
 */
@Entity
@Table(name = "NOTIFICATIONS")
@IdClass(PSNotificationDefPK.class)
public class PSNotificationDef implements Serializable, IPSCatalogItem
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = 1L;
   @Id
   @Column(name = "WORKFLOWAPPID", nullable = false)
   private long workflowId;
   
   @Id
   @Column(name = "NOTIFICATIONID", nullable = false)
   private long notificationId;
   
   @Basic
   @Column(name="SUBJECT", nullable = true)
   private String subject;
   
   @Basic
   @Column(name="BODY", nullable = true)
   private String body;
   
   @Basic
   @Column(name="DESCRIPTION", nullable = true)
   private String description;

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.WORKFLOW_NOTIFICATION, notificationId);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#setGUID(IPSGuid)
    */
   public void setGUID(IPSGuid newguid) throws IllegalStateException
   {
      if (newguid == null)
         throw new IllegalArgumentException("newguid may not be null");

      if (notificationId != 0)
         throw new IllegalStateException("cannot change existing guid");

      notificationId = newguid.longValue();
   }

   /**
    * Get the subject text for this notification.
    * 
    * @return the notification subject text, may be <code>null</code> or empty.
    */
   public String getSubject()
   {
      return subject;
   }
   
   /**
    * Set the subject of this notification.
    * 
    * @param sub The subject, may be <code>null</code> or empty.
    */
   public void setSubject(String sub)
   {
      subject = sub;
   }

   /**
    * The body text for this notification.
    * 
    * @return the notification body text, may be <code>null</code> or empty.
    */
   public String getBody()
   {
      return body;
   }
   
   /**
    * Set the body text.
    * 
    * @param bodyText The text, may be <code>null</code> or empty.
    */
   public void setBody(String bodyText)
   {
      body = bodyText;
   }

   /**
    * A description for this notification.
    * 
    * @return the notification description, may be <code>null</code> or empty.
    */
   public String getDescription()
   {
      return description;
   }
   
   /**
    * Set the description
    * 
    * @param desc The description, may be <code>null</code>.
    */
   public void setDescription(String desc)
   {
      description = desc;
   }

   /**
    * Get the workflow id of this state
    * 
    * @param id The id.
    */
   public void setWorkflowId(long id)
   {
      workflowId = id;
   }

   /**
    * Get the workflow id
    * 
    * @return The id.
    */
   public long getWorkflowId()
   {
      return workflowId;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSNotificationDef)) return false;
      PSNotificationDef that = (PSNotificationDef) o;
      return getWorkflowId() == that.getWorkflowId() && notificationId == that.notificationId && Objects.equals(getSubject(), that.getSubject()) && Objects.equals(getBody(), that.getBody()) && Objects.equals(getDescription(), that.getDescription());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getWorkflowId(), notificationId, getSubject(), getBody(), getDescription());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSNotificationDef{");
      sb.append("workflowId=").append(workflowId);
      sb.append(", notificationId=").append(notificationId);
      sb.append(", subject='").append(subject).append('\'');
      sb.append(", body='").append(body).append('\'');
      sb.append(", description='").append(description).append('\'');
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

