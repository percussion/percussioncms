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
package com.percussion.services.relationship.data;

import com.percussion.cms.IPSConstants;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipPropertyData;

import java.io.Serializable;
import java.util.*;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.FilterDef;


/**
 * Represents a single back-end row value in 
 * {@link IPSConstants#PSX_RELATIONSHIPS} table
 */
@Entity
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE, 
      region = "PSRelationshipData")
@Table(name = IPSConstants.PSX_RELATIONSHIPS)
@FilterDef(name="relationshipConfigFilter")
public class PSRelationshipData implements Serializable
{
   /**
    * Computed serial number
    */
   private static final long serialVersionUID = 1L;

   @Id
   @Column(name="RID")
   private int rid = UNKNOWN_ID;

   @Basic
   @Column(name="CONFIG_ID")
   private int config_id = -1;

   @Basic
   @Column(name="OWNER_ID")
   private int owner_id;

   @Basic
   @Column(name="OWNER_REVISION")
   private int owner_revision = -1;

   @Basic
   @Column(name="DEPENDENT_ID")
   private int dependent_id;

   @Basic
   @Column(name="DEPENDENT_REVISION")
   private int dependent_revision = -1;

   @Basic
   @Column(name="SLOT_ID")
   private Long slot_id;
   @Basic
   @Column(name="SORT_RANK")
   private Integer sort_rank;
   @Basic
   @Column(name="VARIANT_ID")
   private Long variant_id;
   @Basic
   @Column(name="FOLDER_ID")
   private Integer folder_id;
   @Basic
   @Column(name="SITE_ID")
   private Long site_id;
   @Basic
   @Column(name="INLINE_RELATIONSHIP")
   private String inline_relationship;

   @Basic
   @Column(name="WIDGET_NAME")
   private String widget_name;
   
   /**
    * @see {@link #getChildProperties()} for description.
    */
   @SuppressWarnings("unchecked")
   @Transient
   private List<PSRelationshipPropertyData> m_childProps = Collections.EMPTY_LIST;

   /**
    * The relationship config object, init by {@link #setConfig(PSRelationshipConfig)}
    */
   @Transient
   private PSRelationshipConfig m_config;

   /**
    * Create an instance of the relationship data.
    *
    * @param rId the relationship id. It must be {@link #UNKNOWN_ID} if this
    *   object does not persist in the repository.
    * @param configId the id of the relationship configuration.
    * @param config the relationship configuration, never <code>null</code>.
    * @param ownerId owner id.
    * @param ownerRev owner revision.
    * @param dependentId dependent id.
    * @param dependentRev dependent revision.
    */
   public PSRelationshipData(int rId,
         PSRelationshipConfig config, int ownerId, int ownerRev,
         int dependentId, int dependentRev)
   {
      if (config == null)
         throw new IllegalArgumentException("config must not be null.");

      rid = rId;
      owner_id = ownerId;
      owner_revision = ownerRev;
      dependent_id = dependentId;
      dependent_revision = dependentRev;

      m_config = config;
   }

   /**
    * Set a valid relationship configuration id. This can only be called if
    * this object does not have a valid config id.
    *
    * @param configId a valid config id, must be greater than zero.
    */
   public void setConfigId(int configId)
   {
      /*if (config_id != -1)
         throw new IllegalStateException("Cannot reset the relationship config id.");*/
      if (configId <= 0)
         throw new IllegalArgumentException("configId must be > 0.");

      config_id = configId;
   }

   /**
    * Sets the owner id.
    * @param ownerId the new owner id.
    */
   public void setOwnerId(int ownerId)
   {
      owner_id = ownerId;
   }

   /**
    * Sets the owner revision
    * @param ownerRev the new owner revision.
    */
   public void setOwnerRevision(int ownerRev)
   {
      owner_revision = ownerRev;
   }
   
   /**
    * Sets the dependent id
    * @param depId the new dependent id.
    */
   public void setDependentId(int depId)
   {
      dependent_id = depId;
   }
   
   /**
    * Sets the dependent revision.
    * @param depRev the new dependent revision.
    */
   public void setDependentRevision(int depRev)
   {
      dependent_revision = depRev;
   }
   
   /**
    * Determines if this object is persisted in the repository.
    *
    * @return <code>true</code> if this object does exist in the repository;
    *    otherwise return <code>false</code>.
    */
   public boolean isPersisted()
   {
      return rid != UNKNOWN_ID && m_isPersisted;
   }

   /**
    * Set the persistent status. It is typically used in conjunction with
    * {@link #setId(int)} when creating a new object.
    *
    * @param isPersisted the to be set persistent status. <code>true</code> if
    *    the object is already persisted in the repository; otherwise
    *    <code>false</code>.
    */
   public void setPersisted(boolean isPersisted)
   {
      m_isPersisted = isPersisted;
      if (!isPersisted)
      {
         for (PSRelationshipPropertyData prop : m_childProps)
         {
            prop.setPersisted(false);
         }
      }
   }

   /**
    * Set a valid relationship id. This should only be used by the relationship
    * service, which is used to insert a new relationship data into the persistent
    * repository.
    *
    * @param id the relationship id, must be greater than <code>0</code>.
    */
   public void setId(int id)
   {
      if (id <= 0)
         throw new IllegalArgumentException("rid must be > 0.");
      if (this.rid > 0)
         throw new IllegalStateException("A valid rid cannot be reseted.");

      this.rid = id;
   }

   /**
    * Default ctor needed by Hibernate
    */
   public PSRelationshipData()
   {
      // Empty
   }


   /**
    * @return the id of the relationship configuration.
    */
   public int getId()
   {
      return rid;
   }

   /**
    * @return the id of the relationship configuration. It may be
    *   <code>-1</code> if has not been set.
    */
   public int getConfigId()
   {
      return config_id;
   }

   /**
    * @return the owner id of the object.
    */
   public int getOwnerId()
   {
      return owner_id;
   }

   /**
    * @return the dependent id of the object.
    */
   public int getDependentId()
   {
      return dependent_id;
   }

   /**
    * @return the owner revision. It may be <code>-1</code> if unknown.
    */
   public int getOwnerRevision()
   {
      return owner_revision;
   }

   /**
    * @return the dependent revision. It may be <code>-1</code> if unknown.
    */
   public int getDependentRevision()
   {
      return dependent_revision;
   }

   /**
    * Set the related config object. 
    *
    * @param config the related config object, never <code>null</code>.
    */
   public void setConfig(PSRelationshipConfig config)
   {
      if (config == null)
         throw new IllegalArgumentException("config must not be null");

      m_config = config;
   }

   /**
    * Get child properties that is stored in 
    * {@link IPSConstants#PSX_RELATIONSHIPPROPERTIES} table.
    *
    * @return the child properties, never <code>null</code>, but may be empty.
    */
   @SuppressWarnings("unchecked")
   public Collection<PSRelationshipPropertyData> getChildProperties()
   {
      return m_childProps;
   }

   /**
    * Set the properties. This must be called after a call to
    * {@link #setConfig(PSRelationshipConfig)}. This must not be called if
    * the relationship does not have child properties.
    *
    * @param props the child properties, never <code>null</code>.
    */
   public void setProperties(
         Collection<PSRelationshipPropertyData> props)
   {
      if (props == null)
         throw new IllegalArgumentException("childProps must not be null");

      if (m_config == null)
         throw new IllegalStateException("Must call setConfig(PSRelationshipConfig) first.");
      
      if (m_config.getCustomPropertyNames().isEmpty())
         throw new IllegalStateException("there is no (additional) child properties for this relationship.");

      if (m_childProps == Collections.EMPTY_LIST)
         m_childProps = new ArrayList<PSRelationshipPropertyData>();
      else
         m_childProps.clear();
      
      // set the persisted flag for all properties
      for (PSRelationshipPropertyData prop : props)
         prop.setPersisted(isPersisted());
      
      m_childProps.addAll(props);
   }

   /**
    * Add the supplied property.
    *
    * @param prop the to be added property, never <code>null</code>.
    */
   public void addProperty (PSRelationshipPropertyData prop)
   {
      if (m_childProps == Collections.EMPTY_LIST)
         m_childProps = new ArrayList<PSRelationshipPropertyData>();

      prop.setPersisted(isPersisted());
      m_childProps.add(prop);
   }
   
   /**
    * Get the property from the given property name.
    *
    * @param propertyName the property name, may be <code>null</code> or empty.
    *
    * @return the specified property, may be <code>null</code> if cannot
    *   find a property with the supplied name.
    */
   public PSRelationshipPropertyData getProperty(String propertyName)
   {
      for (PSRelationshipPropertyData prop : m_childProps)
      {
         if (prop.getName().equalsIgnoreCase(propertyName))
            return prop;
      }
      return null;
   }

   /**
    * Set the slot id from the given value.
    * 
    * @param slotId the new slot id
    */
   public void setSlotId(long slotId)
   {
      slot_id = new Long(slotId);
   }

   /**
    * @return the slot id. It is <code>-1</code> if the value is unknown (or
    *         <code>null</code> in the repository).
    */
   public long getSlotId()
   {
      return slot_id == null ? -1 : slot_id.longValue();
   }

   /**
    * Set the sort rank from the given value.
    * 
    * @param sortRank the new sort rank.
    */
   public void setSortRank(int sortRank)
   {
      sort_rank = new Integer(sortRank);
   }

   /**
    * @return the sort rank. It is <code>-1</code> if the value is 
    *    unknown (or <code>null</code> in the repository).
    */
   public int getSortRank()
   {
      return sort_rank == null ? -1 : sort_rank.intValue();
   }
   
   /**
    * Set the template id from the given value.
    * 
    * @param variantId the new template id.
    */
   public void setVariantId(long variantId)
   {
      variant_id = new Long(variantId);
   }

   /**
    * @return the template id. It is <code>-1</code> if the value is unknown
    *         (or <code>null</code> in the repository).
    */
   public long getVariantId()
   {
      return variant_id == null ? -1 : variant_id.longValue();
   }
   
   /**
    * Set the folder id from the given value.
    * @param folderId the new folder id.
    */
   public void setFolderId(int folderId)
   {
      folder_id = new Integer(folderId);
      if (folder_id == 0)
         folder_id = -1;
   }

   /**
    * @return the folder id. It is <code>-1</code> if the value is 
    *    unknown (or <code>null</code> in the repository).
    */
   public int getFolderId()
   {
      return folder_id == null ? -1 : folder_id.intValue();
   }
   
   /**
    * Set the site id from the given value.
    * @param siteId the new site id.
    */
   public void setSiteId(long siteId)
   {
      site_id = new Long(siteId);
      if (site_id ==0)
         siteId = -1;
   }

   /**
    * @return the site id. It is <code>-1</code> if the value is unknown (or
    *         <code>null</code> in the repository).
    */
   public long getSiteId()
   {
      return site_id == null ? -1 : site_id.longValue();
   }

   /**
    * Set the inline relationship property from the given value.
    * 
    * @param inlineValue the new sort rank.
    */
   public void setInlineRelationship(String inlineValue)
   {
      inline_relationship = inlineValue;
   }

   /**
    * @return the inline relationship property. It may be <code>null</code> or
    *   empty.
    */
   public String getInlineRelationship()
   {
      return inline_relationship;
   }
   
   /**
    * Sets the widget name property.
    * @param name the new name, may be <code>null</code> or empty.
    */
   public void setWidgetName(String name)
   {
      widget_name = name;
   }
   
   /**
    * Gets the widget name property. This name may relate to {@link #getSiteId()}, which may be the widget instance ID.
    * @return the widget name, may be <code>null</code> or empty.
    */
   public String getWidgetName()
   {
      return widget_name;
   }
   
   /**
    * Get the related config object, must call {@link #setConfig(PSRelationshipConfig)}
    * first.
    *
    * @return the related config object, never <code>null</code>.
    */
   public PSRelationshipConfig getConfig()
   {
      if (m_config == null)
         throw new IllegalStateException("m_config has not been set yet.");
      return m_config;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSRelationshipData)) return false;
      PSRelationshipData that = (PSRelationshipData) o;
      return rid == that.rid;
   }

   @Override
   public int hashCode() {
      return Objects.hash(rid);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("rid=" + rid);
      buffer.append(",config_id=" + config_id);
      buffer.append(",owner_id=" + owner_id);
      buffer.append(",owner_revision=" + owner_revision);
      buffer.append(",dependent_id=" + dependent_id);
      buffer.append(",dependent_revision=" + dependent_revision);
      if (slot_id != null)
         buffer.append(",slot_id=" + slot_id);
      if (sort_rank != null)
         buffer.append(",sort_rank=" + sort_rank);
      if (variant_id != null)
         buffer.append(",variant_id=" + variant_id);
      if (folder_id != null)
         buffer.append(",folder_id=" + folder_id);
      if (site_id != null)
         buffer.append(",site_id=" + site_id);
      if (inline_relationship != null)
         buffer.append(",inline_relationship=" + inline_relationship);
      if (widget_name != null)
         buffer.append(",widget_name=" + widget_name);

      return buffer.toString();
   }

   /**
    * Indicate if this object has been persisted in the repository.
    * <code>true</code> if it is persisted; otherwise <code>false</code>.
    */
   @Transient
   private boolean m_isPersisted = true;

   /**
    * The unknown id is used when creating a new relationship data that not
    * exists in the repository.
    */
   public final static int UNKNOWN_ID = -1;
}
