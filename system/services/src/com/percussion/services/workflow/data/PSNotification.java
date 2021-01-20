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
package com.percussion.services.workflow.data;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.string.PSStringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

/**
 * Represents a transition notification
 */
@Entity
@Table(name = "TRANSITIONNOTIFICATIONS")
@IdClass(PSNotificationPK.class)
public class PSNotification implements Serializable, IPSCatalogItem
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = 1L;

   @Id
   @Column(name = "TRANSITIONNOTIFICATIONID", nullable = false)
   private long transNotificationId;
   
   @Id
   @Column(name = "NOTIFICATIONID", nullable = false)
   private long notificationId;
   
   @Id
   @Column(name = "TRANSITIONID", nullable = false)
   private long transitionId;
   
   @Id
   @Column(name = "WORKFLOWAPPID", nullable = false)
   private long workflowId;
   
   @Basic
   @Column(name="STATEROLERECIPIENTTYPES", nullable = false)
   private int stateRoleRecipientType = 
      PSStateRoleRecipientTypeEnum.TO_STATE_RECIPIENTS.getValue();
   
   @Basic
   @Column(name="ADDITIONALRECIPIENTLIST", nullable = true)
   private String recipients = "";
   
   @Basic
   @Column(name="CCLIST", nullable = true)
   private String ccRecipients = "";

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.WORKFLOW_TRANS_NOTIFICATION, transNotificationId);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#setGUID(IPSGuid)
    */
   public void setGUID(IPSGuid newguid) throws IllegalStateException
   {
      if (newguid == null)
         throw new IllegalArgumentException("newguid may not be null");

      if (transNotificationId != 0)
         throw new IllegalStateException("cannot change existing guid");

      transNotificationId = newguid.longValue();
   }
   
   /**
    * Get the state role recipient type.
    * 
    * @return the state role recipient type used for this notification,
    *    never <code>null</code>.
    */
   public PSStateRoleRecipientTypeEnum getStateRoleRecipientType()
   {
      return PSStateRoleRecipientTypeEnum.valueOf(stateRoleRecipientType);
   }
   
   /**
    * Set the state role recipient type.
    * 
    * @param type The type, may not be <code>null</code>.
    */
   public void setStateRoleRecipientType(PSStateRoleRecipientTypeEnum type)
   {
      if (type == null)
         throw new IllegalArgumentException("type may not be null");
      stateRoleRecipientType = type.getValue();
   }
   
   /**
    * Add a recipient to this notification's recipient list.
    * 
    * @param recipient The email address of the recipient, may not be 
    * <code>null</code> or empty.
    */
   public void addRecipient(String recipient)
   {
      if (StringUtils.isBlank(recipient))
         throw new IllegalArgumentException(
            "recipient may not be null or empty");
      
      if (StringUtils.isBlank(recipients))
         recipients = recipient;
      else
         recipients += ("," + recipient);
   }
   
   /**
    * Get all additional recipients for this notifications.
    * 
    * @return all additional recipients email addresses, never 
    *    <code>null</code>, may be empty.
    */
   public List<String> getRecipients()
   {
      if (recipients == null)
         return new ArrayList<String>();
      else
         return Arrays.asList(StringUtils.split(recipients, ','));
   }
   
   /**
    * Get the recipients list
    * 
    * @param recipientList The list, may be <code>null</code> or empty.
    */
   public void setRecipients(List<String> recipientList)
   {
      if (recipientList == null)
         recipientList = new ArrayList<String>();
      
      recipients = PSStringUtils.listToString(recipientList, ",");
   }

   /**
    * Add a CC recipient to this notification's recipient list.
    * 
    * @param recipient The email address of the recipient, may not be 
    * <code>null</code> or empty.
    */
   public void addCCRecipient(String recipient)
   {
      if (StringUtils.isBlank(recipient))
         throw new IllegalArgumentException(
            "recipient may not be null or empty");
      
      if (StringUtils.isBlank(ccRecipients))
         ccRecipients = recipient;
      else
         ccRecipients += ("," + recipient);
   }

   /**
    * Get all additional CC recipients for this notification.
    * 
    * @return all additional CC recipients email addresses, never 
    *    <code>null</code>, may be empty.
    */
   public List<String> getCCRecipients()
   {
      if (ccRecipients == null)
         return new ArrayList<String>();
      else
         return Arrays.asList(StringUtils.split(ccRecipients, ','));
   }
   
   /**
    * Set the list of additional CC recipients for this notification.
    * 
    * @param ccRecipientList The list of recipients, may be <code>null</code> or 
    * empty.
    */
   public void setCCRecipients(List<String> ccRecipientList)
   {
      if (ccRecipientList == null)
         ccRecipientList = new ArrayList<String>();
      
      this.ccRecipients = PSStringUtils.listToString(ccRecipientList, ",");
   }
   
   /**
    * Get the id of the transition for which this notification is specified.
    * 
    * @return the id.
    */
   public long getTransitionId()
   {
      return transitionId;
   }

   /**
    * Set the id of the transition for which this notification is specified.
    * 
    * @param transId the id.
    */
   public void setTransitionId(long transId)
   {
      this.transitionId = transId;
   }

   /**
    * Get the id of the workflow for which is notification is specified.
    * 
    * @return the id
    */
   public long getWorkflowId()
   {
      return workflowId;
   }

   /**
    * Set the id of the workflow for which is notification is specified.
    * 
    * @param workflowId the id.
    */
   public void setWorkflowId(long workflowId)
   {
      this.workflowId = workflowId;
   }   

   public long getNotificationId()
   {
      return notificationId;
   }
   
   public void setNotificationId(long id)
   {
      notificationId = id;
   }
   
   /**
    * Copy all properties from the source to current object, except the ID.
    * @param src the source object, never <code>null</code>.
    */
   public void copy(PSNotification src)
   {
      notNull(src);
      
      setCCRecipients(src.getCCRecipients());
      setNotificationId(getNotificationId());
      setRecipients(src.getRecipients());
      setStateRoleRecipientType(src.getStateRoleRecipientType());
      setTransitionId(src.getTransitionId());
      setWorkflowId(src.getWorkflowId());
   }
   
   @Override
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
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
   
   static
   {
      // Register types with XML serializer for read creation of objects
      PSXmlSerializationHelper.addType("recipient", String.class);
      PSXmlSerializationHelper.addType("ccrecipient", String.class);
   }   
   
   /**
    * Enumeration of the workflow state role recipient types.
    */
   public enum PSStateRoleRecipientTypeEnum
   {
      /** 
       * Indicates no state role notification recipients 
       */
      NO_STATE_RECIPIENTS(0),

      /** 
       * Indicates only to-state role notification recipients 
       */
      TO_STATE_RECIPIENTS(1),

      /** 
       * Indicates only from-state role notification recipients 
       */
      FROM_STATE_RECIPIENTS(2),

      /** 
       * Indicates both to-state and from-state role notification recipients 
       */
      TO_AND_FROM_STATE_RECIPIENTS(3);
      
      /**
       * Get the enum with the matching value.
       * 
       * @param value The value.
       * 
       * @return The enum, never <code>null</code>.
       */
      public static PSStateRoleRecipientTypeEnum valueOf(int value)
      {
         for (PSStateRoleRecipientTypeEnum type : values())
         {
            if (type.mi_value == value)
            {
               return type;
            }
         }
         
         throw new IllegalArgumentException("invalid value");
      }
      
      /**
       * Get the value of this enum.
       * 
       * @return The value.
       */
      public int getValue()
      {
         return mi_value;
      }
      
      private PSStateRoleRecipientTypeEnum(int value)
      {
         mi_value = value;
      }
      
      /**
       * The enum value
       */
      private int mi_value;      
   }
}

