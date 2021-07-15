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
package com.percussion.services.guidmgr.data;

import javax.persistence.Basic;
import javax.persistence.Column;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Entity;

/**
 * Storage for GUID allocation records.
 * 
 * @author dougrand
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSGuidGeneratorData")
@Table(name = "PSX_GUID_DATA")
public class PSGuidGeneratorData
{
   /**
    * The key, key <code>= 0</code> stores the siteid, otherwise what's stored
    * is the next id to be allocated.
    */
   @Id
   @Column(name = "ID")
   private int m_key;

   /**
    * Field used for hibernate to discover stale updates. Default value
    * indicates a new instance.
    */
   @Version
   @Column(name = "VERSION")
   private int m_version = -1;

   /**
    * The value
    */
   @Basic
   @Column(name = "NEXTID")
   private long m_value;

   /**
    * Used for persistance
    */
   protected PSGuidGeneratorData() {

   }

   /**
    * Ctor
    * 
    * @param key the key, which is typically the ordinal from
    *           {@link com.percussion.services.catalog.PSTypeEnum}
    * @param value
    * 
    */
   public PSGuidGeneratorData(int key, long value) {
      super();

      m_key = key;
      m_value = value;
   }

   /**
    * @return Returns the value.
    */
   public long getValue()
   {
      return m_value;
   }

   /**
    * @param value The value to set.
    */
   public void setValue(long value)
   {
      m_value = value;
   }

   /**
    * @return Returns the key.
    */
   public int getKey()
   {
      return m_key;
   }

   /**
    * @return Returns the version.
    */
   public int getVersion()
   {
      return m_version;
   }

   /**
    * @param key The key to set.
    */
   protected void setKey(int key)
   {
      m_key = key;
   }

   /**
    * @param version The version to set.
    */
   public void setVersion(Integer version)
   {
      m_version = version.intValue();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      EqualsBuilder builder = new EqualsBuilder();
      if (obj == null)
         return false;
      PSGuidGeneratorData other = (PSGuidGeneratorData) obj;

      return builder.append(m_key, other.m_key).append(m_version,
            other.m_version).append(m_value, other.m_value).isEquals();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      HashCodeBuilder builder = new HashCodeBuilder();

      return builder.append(m_key).append(m_value).toHashCode();
   }
}
