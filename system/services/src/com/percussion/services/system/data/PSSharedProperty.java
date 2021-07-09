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

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;

import java.io.IOException;
import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.xml.sax.SAXException;

/**
 * This object represents a single shared property.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, 
      region = "PSSharedProperty")
@Table(name = "PSX_SHARED_PROPERTIES")
public class PSSharedProperty implements Serializable, IPSCatalogItem, Comparable
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = -79604122602978810L;

   @Id
   @Column(name = "ID", nullable = false)
   private long id;

   @Version
   @Column(name = "VERSION")
   private Integer version;
   
   @Basic
   @Column(name = "NAME", nullable = false, length = 50)
   private String name;
   
   @Basic
   @Column(name = "VALUE", nullable = true, length = 250)
   private String value;

   /**
    * Default constructor.
    */
   public PSSharedProperty()
   {
   }
   
   /**
    * Construct a new shared property for the supplied parameters.
    * 
    * @param name the property name, not <code>null</code> or empty.
    * @param value the property value, may be <code>null</code> or empty.
    */
   public PSSharedProperty(String name, String value)
   {
      setName(name);
      setValue(value);
   }

   /* (non-Javadoc)
    * @see IPSCatalogSummary#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.SHARED_PROPERTY, id);
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
   
   /**
    * Get the object version.
    * 
    * @return the object version, <code>null</code> if not initialized yet.
    */
   public Integer getVersion()
   {
      return version;
   }
   
   /**
    * Set the object version.
    * 
    * @param version the version of the object, must be >= 0.
    */
   public void setVersion(Integer version)
   {
      if (version < 0)
         throw new IllegalArgumentException("version must be >= 0");
      
      this.version = version;
   }
   
   /**
    * Get the property name.
    * 
    * @return the property name, may be <code>null</code>, not empty.
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * Set a new property name.
    * 
    * @param name the new property name, not <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");
      
      this.name = name;
   }
   
   /**
    * Get the property value.
    * 
    * @return the property valud, may be <code>null</code> or empty.
    */
   public String getValue()
   {
      return value;
   }
   
   /**
    * Set a new property value.
    * 
    * @param value the new property value, may be <code>null</code> or empty.
    */
   public void setValue(String value)
   {
      this.value = value;
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
    * @see java.lang.Comparable#compareTo(T)
    */
   public int compareTo(Object o)
   {
      if (!(o instanceof PSSharedProperty))
         throw new IllegalArgumentException(
            "o must be of type PSSharedproperty");
      
      PSSharedProperty property = (PSSharedProperty) o;
      return getName().compareToIgnoreCase(property.getName());
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

