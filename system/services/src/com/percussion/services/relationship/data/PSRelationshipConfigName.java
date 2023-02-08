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
package com.percussion.services.relationship.data;

import com.percussion.cms.IPSConstants;

import java.io.Serializable;


import javax.persistence.Basic;
import javax.persistence.Entity;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


/**
 * Represents a single back-end row value in 
 * {@link IPSConstants#PSX_RELATIONSHIPCONFIGNAME} table
 */
@Entity
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE, region = "PSRelationshipConfigName")
@Table(name = IPSConstants.PSX_RELATIONSHIPCONFIGNAME)
public class PSRelationshipConfigName implements Serializable
{
   /**
    * Generated number
    */
   private static final long serialVersionUID = 608168070484512836L;

   @Id
   private int config_id;
   
   @Basic
   private String config_name;

   /**
    * Field used for hibernate to discover stale updates. 
    */
   @Version
   @SuppressWarnings("unused")
   private Integer version;

   /**
    * Create an instance of the relationship name.
    * 
    * @param name the name of the relationship configuration, never 
    *   <code>null</code> or empty.
    * @param id the id of the relationship name.
    */
   public PSRelationshipConfigName(String name, int id)
   {
      config_id = id;
      config_name = name;
   }

   /**
    * Default ctor needed by Hibernate
    */
   private PSRelationshipConfigName()
   {
      // Empty
   }


   /**
    * @return the id of the relationship configuration.
    */
   public int getId()
   {
      return config_id;
   }

   /**
    * @return the name of the relationship configuration.
    */
   public String getName()
   {
      return config_name;
   }

   /**
    * @param name The name to set.
    */
   public void setName(String name)
   {
      config_name = name;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;

      PSRelationshipConfigName other = (PSRelationshipConfigName) obj;

      return new EqualsBuilder()
         .append(config_id, other.config_id)
         .append(config_name, other.config_name)
         .isEquals();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      return new HashCodeBuilder(13, 3).appendSuper(super.hashCode())
            .append(config_id)
            .append(config_name)
            .toHashCode();
   }
   
   
   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      return "<id=" + config_id + ",name=" + config_name + ">";
   }
}
