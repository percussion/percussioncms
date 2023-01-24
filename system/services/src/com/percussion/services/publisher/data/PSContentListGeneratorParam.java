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

   /**
    * (non-Javadoc)
    *
    * @see Object#toString()
    */
   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSContentListGeneratorParam{");
      sb.append("id=").append(id);
      sb.append(", version=").append(version);
      sb.append(", name='").append(name).append('\'');
      sb.append(", value='").append(value).append('\'');
      sb.append(", contentList=").append(contentList);
      sb.append('}');
      return sb.toString();
   }
}
