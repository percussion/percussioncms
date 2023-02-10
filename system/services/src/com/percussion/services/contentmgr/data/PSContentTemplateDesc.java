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
package com.percussion.services.contentmgr.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Objects;

/**
 * Describes a single relationship between a content type and a template.
 * 
 * @author dougrand
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, 
      region="PSContentTemplateDesc")
@Table(name = "PSX_CONTENTTYPE_TEMPLATE")
public class PSContentTemplateDesc
{
   /**
    * The primary key of the table, never <code>null</code> for a valid db
    * object, although it will be <code>null</code> in a newly created instance.
    */
   @Id
   @Column(name = "TEMPLATE_TYPE_ID")
   private Long    m_templateTypeId;
   
   /**
    * The id of the content type in the association, never <code>null</code> for
    * a valid db object, although it can be <code>null</code> in an 
    * unpersisted object.
    */
   @Basic
   @Column(name = "CONTENTTYPEID")
   private Long    m_contenttypeid;
   
   /**
    * The version is used by Hibernate to track stale object update. It will
    * never be <code>null</code> for a persisted object.
    */
   @SuppressWarnings("unused")
   @Version
   @Column(name = "VERSION")
   private Integer m_version;  
   
   /**
    * The id of the template in the association, never <code>null</code> for
    * a valid db object, although it can be <code>null</code> in an 
    * unpersisted object.
    */
   @Basic
   @Column(name = "TEMPLATE_ID")
   private Long    m_templateid;

   /**
    * @return Returns the contenttypeid.
    */
   public IPSGuid getContentTypeId()
   {
      return new PSGuid(PSTypeEnum.NODEDEF, m_contenttypeid);
   }


   /**
    * Set the content type guid. Note that the guid for the content type
    * is just a package for the database id. This is not a true guid as it
    * is not transportable between implementations.
    * 
    * @param guid the guid value, never <code>null</code>
    */
   public void setContentTypeId(IPSGuid guid)
   {
      if (guid == null)
      {
         throw new IllegalArgumentException("guid may not be null");
      }
      m_contenttypeid = guid.longValue();
   }

   /**
    * @return Returns the template guid.
    */
   public IPSGuid getTemplateId()
   {
      return new PSGuid(PSTypeEnum.TEMPLATE, m_templateid);
   }

   /**
    * @param guid The template to set.
    */
   public void setTemplateId(IPSGuid guid)
   {
      m_templateid = guid.longValue();
   }

   /**
    * @return Returns the template type id that identifies this particular 
    * association between a template and content type.
    */
   public Long getId()
   {
      return m_templateTypeId;
   }

   /**
    * @param ttypeid The template type id to set.
    */
   public void setId(Long ttypeid)
   {
      m_templateTypeId = ttypeid;
   }
   
   /**
    * Get the guid form of the value returned by {@link #getContentTypeId()},
    * used to identify the object to which ACL enforcement should apply.
    * 
    * @return The guid, never <code>null</code>. 
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.NODEDEF, m_contenttypeid);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSContentTemplateDesc)) return false;
      PSContentTemplateDesc that = (PSContentTemplateDesc) o;
      return Objects.equals(m_templateTypeId, that.m_templateTypeId) && Objects.equals(m_contenttypeid, that.m_contenttypeid) && Objects.equals(m_version, that.m_version) && Objects.equals(m_templateid, that.m_templateid);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_templateTypeId, m_contenttypeid, m_version, m_templateid);
   }

   /**
    * (non-Javadoc)
    *
    * @see Object#toString()
    */
   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSContentTemplateDesc{");
      sb.append("m_templateTypeId=").append(m_templateTypeId);
      sb.append(", m_contenttypeid=").append(m_contenttypeid);
      sb.append(", m_version=").append(m_version);
      sb.append(", m_templateid=").append(m_templateid);
      sb.append('}');
      return sb.toString();
   }
}
