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
package com.percussion.services.sitemgr.data;


import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;

import java.io.IOException;
import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
 * Represents a single property for the site. Each property is keyed to 
 * a particular context.
 * @author dougrand
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSSiteProperty")
@Table(name = "RXASSEMBLERPROPERTIES")
public class PSSiteProperty implements IPSCatalogItem, Serializable
{
   /**
    * Serial id identifies versions of serialized data
    */
   private static final long serialVersionUID = 1L;
   
   static
   {
      // Register types with XML serializer for read creation of objects
      PSXmlSerializationHelper.addType("context", PSPublishingContext.class);
   }

   @Id
   @Column(name = "PROPERTYID")
   long     propertyId;
   
   @Version
   @Column(name = "VERSION")
   Integer  version;
   
   @ManyToOne(targetEntity = PSSite.class)
   @JoinColumn(name = "SITEID", nullable = false, insertable = false, updatable = false)
   IPSSite   site;
   
   @Column(name = "CONTEXTID")
   Integer  contextId;
   
   @Basic
   @Column(name = "PROPERTYNAME")
   String   name;
   
   @Basic
   @Column(name = "PROPERTYVALUE")
   String   value;

   /**
    * The database id for this object
    * @return Returns the propertyId, never <code>null</code> after persistence
    */
   public long getPropertyId()
   {
      return propertyId;
   }

   /**
    * @param propertyId The propertyId to set.
    */
   public void setPropertyId(long propertyId)
   {
      this.propertyId = propertyId;
   }



   /**
    * The parent site object
    * @return Returns the site, never <code>null</code>
    */
   public IPSSite getSite()
   {
      return site;
   }

   /**
    * @param site The site to set, may be <code>null</code> when disconnecting
    */
   public void setSite(IPSSite site)
   {
      this.site = site;
   }

   /**
    * Get the property name
    * @return Returns the name, never <code>null</code> or empty
    */
   public String getName()
   {
      return name;
   }

   /**
    * @param name The name to set, never <code>null</code> or empty
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      this.name = name;
   }

   /**
    * Get the value
    * @return Returns the value.
    */
   public String getValue()
   {
      return value;
   }

   /**
    * @param value The value to set.
    */
   public void setValue(String value)
   {
      this.value = value;
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
    * Set the object version. The version can only be set once in the life cycle
    * of this object.
    * 
    * @param version the version of the object, must be >= 0.
    */
   public void setVersion(Integer version)
   {
      if (this.version != null && version != null)
         throw new IllegalStateException("Version can only be set once");
      
      this.version = version;
   }


   /**
    * Get the publishing context ID
    * @return Returns the context ID, never <code>null</code>
    */
   public IPSGuid getContextId()
   {
      return new PSGuid(PSTypeEnum.CONTEXT, contextId);
   }

   /**
    * Set the publishing context id, cannot set if context ID is already set. 
    * Used by serialization only.
    * @param ctxId the content id
    */
   public void setContextId(IPSGuid ctxId)
   {
      if (ctxId == null)
         throw new IllegalArgumentException("ctxId may not be null.");
      
      if ( contextId != null )
         return;
      contextId = ctxId.getUUID();
   }
  
   /** (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if ( !(obj instanceof PSSiteProperty) )
         return false;
      PSSiteProperty b = (PSSiteProperty) obj;
      return new EqualsBuilder()
         .append(propertyId, b.propertyId)
         .append(name, b.name)
         .append(value, b.value)
         .isEquals();
   }
   
   /** (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(name).toHashCode();
   }
   
   /** (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this).toString();
   }

   /** (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }

   /** (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#fromXML(java.lang.String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);   }

   /** (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.SITE_PROPERTY, getPropertyId());
   }

   /** (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#setGUID(com.percussion.utils.guid.IPSGuid)
    */
   public void setGUID(IPSGuid newguid) throws IllegalStateException
   {
      if (newguid == null)
         throw new IllegalArgumentException("newguid may be not null.");
      
      setPropertyId(newguid.longValue()); 
   }
}
