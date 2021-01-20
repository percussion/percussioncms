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
package com.percussion.services.catalog.data;

import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.locking.data.PSObjectLock;
import com.percussion.services.locking.data.PSObjectLockSummary;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.data.PSUserAccessLevel;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.IPSXmlSerialization;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Container which holds common information available for all design objects.
 */
public class PSObjectSummary implements IPSCatalogSummary
{
   static
   {
      PSXmlSerializationHelper.addType(PSObjectSummary.class);
      PSXmlSerializationHelper.addType("locked", PSObjectLockSummary.class);
   }
   
   /**
    * The objects id, never <code>null</code>.
    */
   private long id;
   
   /**
    * The objects type, never <code>null</code>.
    */
   private PSTypeEnum type;
   
   /**
    * The objects name, never <code>null</code> or empty.
    */
   private String name;
   
   /**
    * The objects display label, defaults to the name if not supplied, never
    * <code>null</code> or empty.
    */
   private String label;
   
   /**
    * The objects description, may be <code>null</code> or empty.
    */
   private String description;
   
   /**
    * Flag to act as a latch indicating if permissions have been explicitly
    * set on this object, initially <code>false</code>, set to <code>true</code>
    * by the first call to {@link #setPermissions(PSUserAccessLevel)}.  This
    * value does not persist across serializations of this object.
    */
   private transient boolean m_arePermissionsValid = false;
   
   /**
    * The permissions of the requestor to the object which this summary
    * represents, never <code>null</code>.
    */
   private PSUserAccessLevel permissions = new PSUserAccessLevel(null);
   
   /**
    * Holds the lock information if this object is locked, <code>null</code>
    * otherwise.
    */
   private PSObjectLockSummary locked = null;

   /**
    * Default constructor.
    */
   public PSObjectSummary()
   {
   }
   
   /**
    * Convenience constructor that calls {@link #PSObjectSummary(IPSGuid, 
    * String, String, String) this(id, name, null, null)}.
    * @param id the id of the object for which this represents the summary,
    *    not <code>null</code>.
    * @param name the name of the object for which this represents the summary,
    *    not <code>null</code> or empty.
    */
   public PSObjectSummary(IPSGuid id, String name)
   {
      this(id, name, null, null);
   }

   /**
    * Convenience constructor that calls {@link #PSObjectSummary(IPSGuid, 
    * String, String, String) this(id, name, null, 
    * description)}.
    * @param id the id of the object for which this represents the summary,
    *    not <code>null</code>.
    * @param name the name of the object for which this represents the summary,
    *    not <code>null</code> or empty.
    * @param description the description of the object for which this
    *    represents the summary, may be <code>null</code> or empty.
    */
   public PSObjectSummary(IPSGuid id, String name, 
      String description)
   {
      this(id, name, null, description);
   }
   
   /**
    * Construct a new summary for the supplied parameters.
    * 
    * @param id the id of the object for which this represents the summary,
    *    not <code>null</code>.
    * @param name the name of the object for which this represents the summary,
    *    not <code>null</code> or empty.
    * @param label the display label of the object for which this represents 
    *    the summary, defaults to <code>name</code> if a blank was supplied.
    * @param description the description of the object for which this
    *    represents the summary, may be <code>null</code> or empty.
    */
   public PSObjectSummary(IPSGuid id, String name, 
      String label, String description)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");
      
      // label defaults to name if not supplied
      if (StringUtils.isBlank(label))
         this.label = name;
      
      setGUID(id);
      setName(name);
      setLabel(label);
      setDescription(description);

   }
   
   /**
    * Copy constructor.
    * 
    * @param summary the object summary to copy, not <code>null</code>.
    */
   public PSObjectSummary(IPSCatalogSummary summary)
   {
      if (summary == null)
         throw new IllegalArgumentException("summary cannot be null");
      
      if (summary.getGUID() == null)
         throw new IllegalArgumentException(
            "summary.getGuid() cannot return null");
      
      if (StringUtils.isBlank(summary.getName()))
         throw new IllegalArgumentException(
            "summary.getName() cannot return a null or empty string");
      
      if (StringUtils.isBlank(summary.getLabel()))
         throw new IllegalArgumentException(
            "summary.getLabel() cannot return a null or empty string");
      
      setGUID(summary.getGUID());
      setName(summary.getName());
      setLabel(summary.getLabel());
      setDescription(summary.getDescription());
   }

   
   /* (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogSummary#getDescription()
    */
   public String getDescription()
   {
      return description;
   }
   
   /**
    * Set a new description.
    * 
    * @param description the new description, may be <code>null</code> or
    *    empty.
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(type, id);
   }
   
   /**
    * Set the guid.
    * 
    * @param guid the new guid to set, not <code>null</code>.
    */
   public void setGUID(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("id may not be null");

      this.id = new PSDesignGuid(guid).getValue();
      this.type = PSTypeEnum.valueOf(guid.getType());
   }
   
   /**
    * Get the object id.
    * 
    * @return the object id.
    */
   @IPSXmlSerialization(suppress=true)
   public long getId()
   {
      return id;
   }
   
   /**
    * Set the object id.
    * 
    * @param id the new object id.
    */
   public void setId(long id)
   {
      this.id = id;
   }

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getLabel()
    */
   public String getLabel()
   {
      return label;
   }
   
   /**
    * Set the object label. Defaults to the object name if <code>null</code> or
    * empty.
    * 
    * @param label the new object label, may be <code>null</code> or empty.
    */
   public void setLabel(String label)
   {
      if (StringUtils.isBlank(label))
         label = name;

      this.label = label;
   }

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getName()
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * Set the object name.
    * 
    * @param name the new object name, not <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");

      this.name = name;
   }
   
   /**
    * Get the object type.
    *
    * @return the object type, never <code>null</code> or empty.
    */
   public String getType()
   {
      //TODO:  It is unclear if this field is actually used anywhere - i think the type is set on the Guid so this may not be required.
      if(type==null)
         type=PSTypeEnum.INVALID;

      return type.toString();
   }
   
   /**
    * Set the object type.
    * 
    * @param type the new object type, not <code>null</code> or empty.
    */
   public void setType(String type)
   {
      if (StringUtils.isBlank(type))
         throw new IllegalArgumentException("type cannot be null or empty");

      this.type = PSTypeEnum.valueOf(type);
   }
   
   /**
    * Get the permissions of the requestor to this object.  If 
    * {@link #arePermissionsValid()} returns <code>false</code>, then the 
    * permissions created by default during construction are returned and do
    * not represent the current requestor's permissions. See that method for
    * more info.
    * 
    * @return The permissions, never <code>null</code>.
    */
   @IPSXmlSerialization(suppress=true)
   public PSUserAccessLevel getPermissions()
   {
      return permissions;
   }
   
   /**
    * Set the requestor's permissions to this object.
    * 
    * @param perms the requestor's permissions, may be <code>null</code>.
    */
   public void setPermissions(PSUserAccessLevel perms)
   {
      if (perms == null)
         perms = new PSUserAccessLevel(null);
      
      permissions = perms;
      m_arePermissionsValid = true;
   }
   
   /**
    * Determine if {@link #getPermissions()} returns valid permissions or simply
    * the default permissions created when this object is constructed.  Will
    * return <code>true</code> if {@link #setPermissions(PSUserAccessLevel)}
    * has been called after construction.
    *
    * @return <code>true</code> if they are valid, <code>false</code> if they
    * are the default permissions.
    */
   public boolean arePermissionsValid()
   {
      return m_arePermissionsValid;
   }
   
   /**
    * Gets the permissions serialized to a string, specifically for xml
    * serialization. The permissions are converted to a string using
    * the to string method
    * @return a string, never <code>null</code>, but can be empty
    */
   public String getPermissionValue()
   {
      StringBuilder rval = new StringBuilder();
      for(PSPermissions p : permissions.getPermissions())
      {
         if (rval.length() > 0)
         {
            rval.append(',');
         }
         rval.append(p.name());
      }
      return rval.toString();
   }
   
   /**
    * Set the permissions that were serialized to a string, this method
    * is specifically for xml serialization.
    * 
    * @param permissions_str the permissions, may be empty or <code>null</code>
    */
   public void setPermissionValue(String permissions_str)
   {
      if (permissions_str != null)
      {
         String perms[] = permissions_str.split(",");
         for(int i = 0; i < perms.length; i++)
         {
            PSPermissions p = PSPermissions.valueOf(perms[i]);
            permissions.getPermissions().add(p);
         }
      }
   }
   
   /**
    * Sort the supplied summaries in the same order as the provided ids. If an
    * id doesn't have a summary, it is skipped.
    * 
    * @param ids the ids which provide the sort order, not <code>null</code>,
    *    may be empty. Must have the same size as <code>summaries</code>.
    * @param summaries the summaries to sort against the supplied ids order, not
    *    <code>null</code>, may be empty. This method takes ownership of the 
    *    list and may modify its membership.
    * @return a new list with the sorted summaries, never <code>null</code>, 
    *    may be empty. 
    */
   public static List<IPSCatalogSummary> sortByIds(IPSGuid[] ids, 
      List<IPSCatalogSummary> summaries)
   {
      if (ids == null)
         throw new IllegalArgumentException("ids cannot be null");
      
      if (summaries == null)
         throw new IllegalArgumentException("summaries cannot be null");
            
      if (ids.length < summaries.size())
         throw new IllegalArgumentException(
            "ids must be equal or larger than summaries");
      
      List<IPSCatalogSummary> sortedValues = new ArrayList<IPSCatalogSummary>();
      for (int i=0; i<ids.length; i++)
      {
         IPSGuid id = ids[i];
         
         for (int j=0; j<summaries.size(); j++)
         {
            IPSCatalogSummary value = summaries.get(j);
            if (value.getGUID().equals(id))
            {
               sortedValues.add(summaries.remove(j));
               break;
            }
         }
      }
      
      return sortedValues;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSObjectSummary)) return false;
      PSObjectSummary that = (PSObjectSummary) o;
      return getId() == that.getId() &&
              StringUtils.equals(getType(),that.getType()) &&
              StringUtils.equals(getName(),that.getName()) &&
              StringUtils.equals(getLabel(), that.getLabel()) &&
              StringUtils.equals(getDescription(),that.getDescription()) &&
              Objects.equals(getLocked(),that.getLocked()) &&
              Objects.equals(getPermissions(),that.getPermissions());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getId(), getType(), getName(), getLabel(), getDescription(), getPermissions(), getLocked());
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }
   
   /**
    * Get the lock information.
    * 
    * @return the lock information, may be <code>null</code> if this object
    *    is not locked.
    */
   public PSObjectLockSummary getLocked()
   {
      return locked;
   }
   
   /**
    * Set the new lock information.
    * 
    * @param locked the new lock information, may be <code>null</code> if this
    *    object is not locked.
    */
   public void setLocked(PSObjectLockSummary locked)
   {
      this.locked = locked;
   }
   
   /**
    * The the lock info from the supplied lock.
    * 
    * @param lock the lock from which to set the lock info, not 
    *    <code>null</code>.
    */
   public void setLockedInfo(PSObjectLock lock)
   {
      if (lock == null)
         throw new IllegalArgumentException("lock cannot be null");
      
      setLockedInfo(lock.getLockSession(), lock.getLocker(), 
         lock.getRemainingTime());
   }
   
   /**
    * Set the new lock information.
    * 
    * @param session the session that has the object locked, not 
    *    <code>null</code> or empty.
    * @param locker the user who has the object locked, not <code>null</code>
    *    or empty.
    * @param remainingTime the remaining time of the lock, must be > 0.
    */
   public void setLockedInfo(String session, String locker, long remainingTime)
   {
      locked = new PSObjectLockSummary(session, locker, remainingTime);
   }
   
   /**
    * Is this object locked?
    * 
    * @return <code>true</code> if ot is, <code>false</code> otherwise.
    */
   @IPSXmlSerialization(suppress=true)
   public boolean isObjectLocked()
   {
      return locked != null;
   }
}

