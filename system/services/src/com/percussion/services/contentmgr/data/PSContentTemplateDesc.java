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

   /** (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object arg0)
   {
       if(!(arg0 instanceof PSContentTemplateDesc))
           return false;
       
      EqualsBuilder builder = new EqualsBuilder();
      PSContentTemplateDesc b = (PSContentTemplateDesc) arg0;
      
      return builder.append(getContentTypeId(), b.getContentTypeId())
         .append(getId(), b.getId())
         .append(getTemplateId(), b.getTemplateId())
         .isEquals();
   }

   /** (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   /** (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }  
   
   
}
