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
package com.percussion.services.publisher.data;

// Generated Dec 16, 2005 4:46:50 PM by Hibernate Tools 3.1.0 beta1JBIDERC2

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.IPSXmlSerialization;

import java.io.IOException;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.xml.sax.SAXException;

/**
 * @see IPSEdition
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSEdition")
@Table(name = "RXEDITION")
public class PSEdition implements IPSCatalogItem, IPSEdition, Cloneable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   @Id
   private long editionid = -1L;
   
   @SuppressWarnings("unused")
   @Version
   private Integer version;

   @Basic
   private String displaytitle;

   @Basic
   private String editioncomment;

   @Basic
   private String editiontype;

   @Basic
   private Long destsite;

   @Basic
   private Integer priority;

   @Basic
   private Long pubserver;

   // Constructors

   /** default constructor */
   public PSEdition() {
   }

   /**
    * minimal constructor
    * 
    * @param editionid
    */
   public PSEdition(Integer editionid) {
      this.editionid = editionid;
   }

   // Property accessors
   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEdition#getId()
    */
   public long getId()
   {
      return this.editionid;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEdition#setId(java.lang.Integer)
    */
   public void setId(long id)
   {
      this.editionid = id;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEdition#getDisplayTitle()
    */
   public String getDisplayTitle()
   {
      return this.displaytitle;
   }
   
   /*
    *  (non-Javadoc)
    * @see com.percussion.services.publisher.IPSEdition#getName()
    */
   public String getName()
   {
      return getDisplayTitle();
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEdition#setDisplayTitle(java.lang.String)
    */
   public void setDisplayTitle(String displayTitle)
   {
      this.displaytitle = displayTitle;
   }
   
   /*
    *  (non-Javadoc)
    * @see com.percussion.services.publisher.IPSEdition#setName(java.lang.String)
    */
   public void setName(String name)
   {
      setDisplayTitle(name);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEdition#getComment()
    */
   public String getComment()
   {
      return this.editioncomment;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEdition#setComment(java.lang.String)
    */
   public void setComment(String comment)
   {
      this.editioncomment = comment;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEdition#getEditionType()
    */
   public PSEditionType getEditionType()
   {
      try
      {
         int et = Integer.parseInt(editiontype);
         return PSEditionType.valueOf(et);
      }
      catch (Exception e)
      {
         return PSEditionType.AUTOMATIC;
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEdition#setEditionType(com.percussion.services.publisher.data.PSEditionType)
    */
   public void setEditionType(PSEditionType editionType)
   {
      if (editionType == null)
      {
         throw new IllegalArgumentException("editionType may not be null");
      }
      this.editiontype = Integer.toString(editionType.getTypeId());
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEdition#getDestSite()
    */
   public IPSGuid getSiteId()
   {
      if (this.destsite == null)
         return null;
      
      return PSGuidUtils.makeGuid(this.destsite, PSTypeEnum.SITE);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEdition#setDestSite(java.lang.Integer)
    */
   public void setSiteId(IPSGuid siteId)
   {
      this.destsite = siteId.longValue();
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSEdition#getPubServerId()
    */
   public IPSGuid getPubServerId()
   {
      if (this.pubserver == null)
         return null;
      
      return PSGuidUtils.makeGuid(this.pubserver, PSTypeEnum.PUBLISHING_SERVER);
   }
   
   public IPSGuid getPubServerOrSiteId()
   {
      return pubserver == null ? getSiteId() : getPubServerId();
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSEdition#setPubServerId(com.percussion.utils.guid.IPSGuid)
    */
   public void setPubServerId(IPSGuid serverId)
   {
      pubserver = serverId.longValue();
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEdition#getPriority()
    */
   public Priority getPriority()
   {
      if (priority == null)
         return Priority.LOWEST;
      
      return convertIntToPriority(priority.intValue());
   }

   /**
    * Converts an integer to priority.
    * 
    * @param pvalue the priority in integer value. The value may be higher
    * or lower than the {@link Priority#HIGHEST} or {@link Priority#LOWEST}.
    *  
    * @return the priority, never <code>null</code>.
    */
   private Priority convertIntToPriority(int pvalue)
   {
      if (pvalue >= Priority.HIGHEST.getValue())
         return Priority.HIGHEST;
      
      if (pvalue == Priority.HIGH.getValue())
         return Priority.HIGH;
      if (pvalue == Priority.MEDIUM.getValue())
         return Priority.MEDIUM;
      if (pvalue == Priority.LOW.getValue())
         return Priority.LOW;

      return Priority.LOWEST;
      
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.data.IPSEdition#setPriority(java.lang.Integer)
    */
   public void setPriority(Priority ePriority)
   {
      if (ePriority == null)
      {
         throw new IllegalArgumentException("ePriority may not be null");
      }
      this.priority = ePriority.getValue();
   }

   /**
    * A convenient method. It is the same as {@link #setPriority(Priority)},
    * but it accept the integer value of the priority.
    * 
    * @param pvalue the integer value of the priority.
    */
   public void setPriorityInt(int pvalue)
   {
      this.priority = convertIntToPriority(pvalue).getValue();
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.IPSEdition#getGUID()
    */
   @IPSXmlSerialization(suppress=true)
   public IPSGuid getGUID()
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      return gmgr.makeGuid(editionid, PSTypeEnum.EDITION); 
   }

   /* (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#setGUID(com.percussion.utils.guid.IPSGuid)
    */
   public void setGUID(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("guid may not be null");
      
      if (editionid != -1L)
         throw new IllegalStateException("guid can only be set once");
      
      editionid = guid != null ? guid.getUUID() : null;
   }

   /**
    * Get the hibernate version information for this object.
    * 
    * @return returns the version, may be <code>null</code>.
    */
   public Integer getVersion()
   {
      return version;
   }
   
   /**
    * Modifies the hibernate version information for this object.
    * 
    * @param version The version to set.
    * 
    * @throws IllegalStateException if an attempt is made to set a previously
    * set version to a non-<code>null</code> value.
    */
   public void setVersion(Integer version) 
   {
      if (this.version != null && version != null)
         throw new IllegalStateException("Version can only be set once");
      
      this.version = version;
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEdition#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEdition#hashCode()
    */
   @Override
   public int hashCode()
   {
      return (int)editionid;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.publisher.data.IPSEdition#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#clone()
    */
   @Override
   public Object clone() throws CloneNotSupportedException
   {
      return super.clone();
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#fromXML(java.lang.String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSEdition#copy(com.percussion.services.publisher.IPSEdition)
    */
   public void copy(IPSEdition other)
   {
      if (other == null)
         throw new IllegalArgumentException("other may not be null.");
      if (!(other instanceof PSEdition))
         throw new IllegalArgumentException(
               "other must be instance of PSEdition");
      PSEdition src = (PSEdition) other;
      destsite = src.destsite;
      displaytitle = src.displaytitle;
      editioncomment = src.editioncomment;
      editiontype = src.editiontype;
      priority = src.priority;
   }
}
