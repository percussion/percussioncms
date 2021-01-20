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
package com.percussion.services.publisher.data;


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;

import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Represents a single parameter to be passed to the generator by default.
 * 
 * @author dougrand
 * 
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, 
      region = "PSContentListGeneratorParam")
@Table(name = "PSX_CONTENTLIST_GEN_PARAM")
public class PSContentListGeneratorParam
{
   @Id
   @Column(name = "PARAM_ID")
   private long id;

   @SuppressWarnings("unused")
   @Version
   private int version;

   @Basic
   private String name;

   @Basic
   private String value;

   @ManyToOne(targetEntity = PSContentList.class)
   @JoinColumn(name = "CONTENT_LIST_ID")
   private PSContentList contentList;

   /**
    * @return Returns the contentList.
    */
   public PSContentList getContentList()
   {
      return contentList;
   }

   /**
    * @param contentList The contentList to set.
    */
   public void setContentList(PSContentList contentList)
   {
      this.contentList = contentList;
   }

   /**
    * @return Returns the id.
    */
   public long getId()
   {
      return id;
   }

   /**
    * @param id The id to set.
    */
   public void setId(long id)
   {
      this.id = id;
   }

   /**
    * @return Returns the name.
    */
   public String getName()
   {
      return name;
   }

   /**
    * @param name The name to set.
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
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
    * @return Returns the version.
    */
   public int getVersion()
   {
      return version;
   }

   /**
    * @param version The version to set.
    */
   public void setVersion(int version)
   {
      this.version = version;
   }

   /** (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object arg0)
   {
      if (!(arg0 instanceof PSContentListGeneratorParam))
         return false;
      PSContentListGeneratorParam parm = (PSContentListGeneratorParam) arg0;

      return new EqualsBuilder().append(name, parm.name).append(value,
            parm.value).isEquals();
   }

   /** (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return name.hashCode();
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
