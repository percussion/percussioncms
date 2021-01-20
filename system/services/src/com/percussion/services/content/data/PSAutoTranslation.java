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
package com.percussion.services.content.data;

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.IPSXmlSerialization;

import java.io.IOException;
import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.xml.sax.SAXException;

/**
 * This object represents a single auto translation definition.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSAutoTranslation")
@Table(name = "PSX_AUTOTRANSLATION")
@IdClass(PSAutoTranslationPK.class)
public class PSAutoTranslation
      implements
         IPSCatalogSummary,
         Serializable,
         IPSCatalogItem
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = -7673907948835742673L;

   @Id
   @Column(name = "CONTENTTYPEID", nullable = false)
   private long contentTypeId;
   
   /**
    * The content type name for this auto translation, initialized while
    * constructed, never <code>null</code> or empty.
    */
   @Transient
   private String contentTypeName;

   @Basic
   @Column(name = "WORKFLOWID", nullable = false)
   private long workflowId;
   
   /**
    * The workflow name for this auto translation, initialized while
    * constructed, never <code>null</code> or empty.
    */
   @Transient
   private String workflowName;

   @Basic
   @Column(name = "COMMUNITYID", nullable = false)
   private long communityId;

   /**
    * The community name for this auto translation, initialized while
    * constructed, never <code>null</code> or empty.
    */
   @Transient
   private String communityName;

   /**
    * The locale code for this auto translation, initialized while
    * constructed, never <code>null</code> or empty.
    */
   @Id
   @Column(name = "LOCALE", nullable = false)
   private String locale;
   
   @Version
   @Column(name = "VERSION")
   private Integer m_version = null;
   
   /**
    * All auto translations are represented by a single guid for locking 
    * purposes, and this method gets an instance of that guid.
    *  
    * @return The guid, never <code>null</code>.
    */
   public static IPSGuid getAutoTranslationsGUID()
   {
      return new PSGuid(PSTypeEnum.AUTO_TRANSLATIONS, 0);
   }
   
   /**
    * Get the key representation of this object.
    * 
    * @return The key, never <code>null</code>.
    */
   @IPSXmlSerialization(suppress = true) 
   public PSAutoTranslationPK getKey()
   {
      return new PSAutoTranslationPK(contentTypeId, locale);
   }
   
   /**
    * Get the guid of this object, always the result of 
    * {@link #getAutoTranslationsGUID()}
    * 
    * @return The guid, never <code>null</code>.
    */
   @IPSXmlSerialization(suppress = true) 
   public IPSGuid getGUID()
   {
      return getAutoTranslationsGUID();
   }
   
   /**
    * Get the community id of this auto translation
    * 
    * @return The id
    */
   public long getCommunityId()
   {
      return communityId;
   }

   /**
    * Set the community id of this auto translation
    * 
    * @param id The id
    */
   public void setCommunityId(long id)
   {
      communityId = id;
   }

   /**
    * Get the content type id of this auto translation
    * 
    * @return The id
    */
   public long getContentTypeId()
   {
      return contentTypeId;
   }

   /**
    * Set the content type id of this auto translation
    * 
    * @param id The id
    */   
   public void setContentTypeId(long id)
   {
      contentTypeId = id;
   }

   /**
    * Get the workflow id of this auto translation
    * 
    * @return The id
    */   
   public long getWorkflowId()
   {
      return workflowId;
   }

   /**
    * Set the workflow id of this auto translation
    * 
    * @param id The id
    */   
   public void setWorkflowId(long id)
   {
      workflowId = id;
   }   
   
   /**
    * Get the content type name of this auto translation.
    * 
    * @return the content type name, may be <code>null</code> or empty if not
    * set.
    */
   @IPSXmlSerialization(suppress = true) 
   public String getContentTypeName()
   {
      return contentTypeName;
   }
   
   /**
    * Set the content type name for this auto translation.  This may be set for
    * informational purposes, but is not persisted with this object.
    * 
    * @param name the content type name, may be <code>null</code> or
    *    empty.
    */
   public void setContentTypeName(String name)
   {
      contentTypeName = name;
   }
   
   /**
    * Get the workflow name of this auto translation.
    * 
    * @return the workflow name, may be <code>null</code> or empty if not set.
    */
   @IPSXmlSerialization(suppress = true) 
   public String getWorkflowName()
   {
      return workflowName;
   }
   
   /**
    * Set the workflow name for this auto translation. This may be set for
    * informational purposes, but is not persisted with this object.
    * 
    * @param name the workflow name, may be <code>null</code> or
    *    empty.
    */
   public void setWorkflowName(String name)
   {
      workflowName = name;
   }
   
   /**
    * Get the community name of this auto translation.
    * 
    * @return the community name, may be <code>null</code> or empty if not set.
    */
   @IPSXmlSerialization(suppress = true) 
   public String getCommunityName()
   {
      return communityName;
   }
   
   /**
    * Set the community name for this auto translation.  This may be set for
    * informational purposes, but is not persisted with this object.
    * 
    * @param name the community name, may be <code>null</code> or
    *    empty.
    */
   public void setCommunityName(String name)
   {
      communityName = name;
   }
   
   /**
    * Get the locale code of this auto translation.
    * 
    * @return the locale code, never <code>null</code> or empty.
    */
   public String getLocale()
   {
      return locale;
   }
   
   /**
    * Set a new locale code for this auto translation.
    * 
    * @param lang the new locale code, not <code>null</code> or
    *    empty.
    */
   public void setLocale(String lang)
   {
      if (StringUtils.isBlank(lang))
         throw new IllegalArgumentException(
            "locale cannot be null or empty");
      
      locale = lang;
   }
   
   /**
    * Set the version.  There are only limited cases where this needs to be 
    * used, such as with web services.
    * 
    * @param version The new version, may not be <code>null</code>.
    */
   public void setVersion(Integer version)
   {
      if (this.m_version != null && version != null)
         throw new IllegalStateException("version can only be initialized once");

      if (version != null && version.intValue() < 0)
         throw new IllegalArgumentException("version must be >= 0");

      this.m_version = version;

   }
   
   /**
    * Get the version.  There are only limited cases where this needs to be 
    * used, such as with web services.
    * 
    * @return The version, may be <code>null</code> if it has not been set.
    */
   @IPSXmlSerialization(suppress = true) 
   public Integer getVersion()
   {
      return m_version;
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
    * @see com.percussion.services.catalog.IPSCatalogSummary#getName()
    */
   @IPSXmlSerialization(suppress = true) 
   public String getName()
   {
      return communityName + "-" + contentTypeName + "-" + workflowName + "-"
         + locale;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogSummary#getLabel()
    */
   @IPSXmlSerialization(suppress = true) 
   public String getLabel()
   {
      return getName();
   }

   /* (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogSummary#getDescription()
    */
   @IPSXmlSerialization(suppress = true) 
   public String getDescription()
   {
      return getName();
   }

   // see base class
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }

   // see base class
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   // see base class
   public void setGUID(IPSGuid newguid) throws IllegalStateException
   {
      return;
   }
}

