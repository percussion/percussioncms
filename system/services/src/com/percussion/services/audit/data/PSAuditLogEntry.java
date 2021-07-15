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

package com.percussion.services.audit.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Class representing an audit entry, used to save information regarding the
 * modification of a design object.
 */
@Entity
@Table(name = "PSX_DESIGN_AUDIT_LOG")
public class PSAuditLogEntry implements Serializable
{
   /**
    * Unique identifier for this entry
    */
   @Id
   @Column(name = "AUDIT_ID", nullable = false)   
   private long id;
   
   /**
    * The date of this audit entry
    */
   @Column(name = "AUDIT_DATE", nullable = false)
   private Date auditDate;
   
   /**
    * The UUID of the object for which the audit event occurred.
    */
   @SuppressWarnings("unused")
   @Column(name = "OBJECT_ID", nullable = false)
   private long objectId;
   
   /**
    * The short value of the type of object for which the audit event occurred. 
    */
   @Column(name = "OBJECT_TYPE", nullable = false)
   private int objectType;
   
   /**
    * The long value of the GUID of the object for which the audit event 
    * occurred.
    */
   @Column(name = "OBJECT_GUID", nullable = false)
   private long objectGuid;
   
   /**
    * The name of the user that generated this entry
    */
   @Column(name = "USERNAME", nullable = false)
   private String userName;
   
   /**
    * The type of action, one of the <code>ACTION_XXX</code> constants.
    */
   @Column(name = "ACTION", nullable = false)
   private String action;
   
   
   /**
    * Get the date when this event was generated.
    * 
    * @return The date, may be <code>null</code>.
    */
   public Date getDate()
   {
      return auditDate;
   }

   /**
    * Get the GUID uniquely identifying this event.
    * 
    * @return The GUID, may be <code>null</code>.
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.INTERNAL, id);
   }

   /**
    * Get the GUID of the audited object.
    * 
    * @return The GUID, may be <code>null</code>.
    */
   public IPSGuid getObjectGUID()
   {
      return new PSGuid(PSTypeEnum.valueOf(objectType), objectGuid);
   }

   /**
    * Get the name of the user that caused this event.
    * 
    * @return The name, may be <code>null</code> or empty.
    */
   public String getUserName()
   {
      return userName;
   }

   /**
    * Set the audit date.  See {@link #getDate()}.
    * 
    * @param date The date of the event, may not be <code>null</code>.
    */
   public void setDate(Date date)
   {
      if (date == null)
         throw new IllegalArgumentException("auditDate may not be null");
      
      auditDate = date;
   }

   /**
    * Set the event GUID.  See {@link #getGUID()}.
    * 
    * @param guid The GUID, may not be <code>null</code>.
    */
   public void setGUID(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("guid may not be null");
      
      id = guid.longValue();
   }

   /**
    * Set the audited object GUID.  See {@link #getObjectGUID()}.
    * 
    * @param guid The GUID, may not be <code>null</code>.
    */
   public void setObjectGUID(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("guid may not be null");
      
      objectGuid = guid.longValue();
      objectId = guid.getUUID();
      objectType = guid.getType();
   }

   /**
    * Set the name of the user causing the event.  See {@link #getUserName()}.
    * 
    * @param name The name of the user, not <code>null</code> or empty.
    */
   public void setUserName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      userName = name;
   }

   /**
    * Get the action
    * 
    * @return Returns the action.
    */
   public AuditTypes getAction()
   {
      return AuditTypes.valueFromString(action);
   }

   /**
    * @param auditAction The action to set.
    */
   public void setAction(AuditTypes auditAction)
   {
      action = auditAction.getStringValue();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof PSAuditLogEntry))
         return false;
      if (this == obj)
         return true;

      PSAuditLogEntry other = (PSAuditLogEntry) obj;
      
      // need special handling of date field as Hibernate creates a Timestamp 
      // instance with a milliseconds value 
      long thisTime = getTruncatedTime(auditDate);
      long otherTime = getTruncatedTime(other.auditDate);
      
      
      return new EqualsBuilder().append(id, other.id).append(
         objectGuid, other.objectGuid).append(action, other.action).append(
         userName, other.userName).append(thisTime, otherTime).isEquals();
   }

   @Override
   public int hashCode()
   {
      return getGUID().getUUID();
   }

   /**
    * Get the value of the supplied date as a long, first truncating any 
    * millisecond component
    * 
    * @param date The date to truncate, may be <code>null</code>.
    * 
    * @return The millisecond value of the truncated date, or <code>0</code> if
    * the date is <code>null</code>.
    */
   private long getTruncatedTime(Date date)
   {
      if (date == null)
         return 0;
      
      Calendar thisCal = Calendar.getInstance();
      thisCal.setTime(date);
      thisCal.clear(Calendar.MILLISECOND);
      return thisCal.getTimeInMillis();      
   }

   @Override
   public String toString()
   {
      return id + " - " + getUserName() + " - " + getObjectGUID() + " - " + 
         getTruncatedTime(auditDate);
   }   

   /**
    * Represents the type of audit event.
    */
   public enum AuditTypes
   {
      /**
       * Represents an save event.
       */
      SAVE("save"),
      
      /**
       * Represents a delete event.
       */
      DELETE("delete");
      
      /**
       * The value of the audit type
       */
      private String mi_value;

      /**
       * Constructor for the value
       * 
       * @param value The internal value of the type.
       */
      private AuditTypes(String value)
      {
         mi_value = value;
      }
      
      /**
       * Get the type's internal value
       * 
       * @return The value.
       */
      private String getStringValue()
      {
         return mi_value;
      }
      
      /**
       * Obtain an instance of this enum based on the internal string value.
       * 
       * @param type The internal string value as obtained by 
       * {@link #getStringValue()}, not <code>null</code>, must be valid.
       * 
       * @return The instance, never <code>null</code>.
       */
      private static AuditTypes valueFromString(String type)
      {
         AuditTypes types[] = values();
         for (int i = 0; i < types.length; i++)
         {
            if (types[i].mi_value == type)
               return types[i];
         }

         throw new IllegalArgumentException("Invalid type" + type);
      }
   }
}
