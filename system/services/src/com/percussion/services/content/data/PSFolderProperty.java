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
package com.percussion.services.content.data;

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.data.IPSCloneTuner;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.IPSXmlSerialization;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
 * This object represents a single keyword.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSFolderProperty")
@Table(name = "PSX_PROPERTIES")
public class PSFolderProperty implements Serializable
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static long serialVersionUID = 1L;

   @Id
   @Column(name = "CONTENTID", nullable = false)
   private long contentID;

   @Id
   @Column(name = "REVISIONID", nullable = false)
   private long revisionID;

   @Id
   @Column(name = "SYSID", nullable = false)
   private long sysID;

   @Id
   @Column(name = "PROPERTYNAME", nullable = false, length = 50)
   private String propertyName;

   @Basic
   @Column(name = "PROPERTYVALUE", nullable = true,  length = 4000)
   private String propertyValue;
   
   @Basic
   @Column(name = "DESCRIPTION", nullable = true, length = 255)
   private String description;




   public long getContentID()
   {
      return contentID;
   }


   public void setContentID(long contentID)
   {
      this.contentID = contentID;
   }


   public long getRevisionID()
   {
      return revisionID;
   }


   public void setRevisionID(long revisionID)
   {
      this.revisionID = revisionID;
   }


   public long getSysID()
   {
      return sysID;
   }


   public void setSysID(long sysID)
   {
      this.sysID = sysID;
   }


   public String getPropertyName()
   {
      return propertyName;
   }


   public void setPropertyName(String propertyName)
   {
      this.propertyName = propertyName;
   }



   public String getDescription()
   {
      return description;
   }


   public void setDescription(String description)
   {
      this.description = description;
   }


   public String getPropertyValue()
   {
      return propertyValue;
   }


   public void setPropertyValue(String propertyValue)
   {
      this.propertyValue = propertyValue;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSFolderProperty)) return false;
      PSFolderProperty that = (PSFolderProperty) o;
      return getContentID() == that.getContentID() && getRevisionID() == that.getRevisionID() && getSysID() == that.getSysID() && getPropertyName().equals(that.getPropertyName()) && Objects.equals(getPropertyValue(), that.getPropertyValue()) && Objects.equals(getDescription(), that.getDescription());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getContentID(), getRevisionID(), getSysID(), getPropertyName(), getPropertyValue(), getDescription());
   }
}
