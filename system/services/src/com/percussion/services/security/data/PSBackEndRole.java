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
package com.percussion.services.security.data;


import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

/**
 * Represents a role definition in the Rx backend.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSBackEndRole")
@Table(name = "PSX_ROLES")
public class PSBackEndRole
{
   /**
    * Get the role id.
    * 
    * @return The id.
    */
   public long getId()
   {
      return id;
   }
   
   /**
    * Set the role id.
    * 
    * @param roleId The id to set.
    */
   public void setId(long roleId)
   {
      id = roleId;
   }
   
   /**
    * Get the name of the role.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * Set the name of the role.
    * 
    * @param roleName The name, never <code>null</code> or empty.
    */
   public void setName(String roleName)
   {
      if (StringUtils.isBlank(roleName))
         throw new IllegalArgumentException("name may not be null or empty");
      
      name = roleName;
   }
   
   /**
    * Get the normalized name, used for case-insensitive lookups.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getNormalizedName()
   {
      return normalizedName;
   }
   
   /**
    * Set the normalized name.  See {@link #getNormalizedName()}.
    * 
    * @param normName The name, may not be <code>null</code> or empty.
    */
   public void setNormalizedName(String normName)
   {
      if (StringUtils.isBlank(normName))
         throw new IllegalArgumentException(
            "normalizedName may not be null or empty");
      
      normalizedName = normName;
   }
   
   /**
    * Get the description of the role.
    * 
    * @return The description, may be <code>null</code> or empty.
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * Set the description of the role.
    * 
    * @param description
    */
   public void setDescription(String description)
   {
      this.description = description;
   }
   
   /**
    * The id of the role.
    */
   @Id
   @Column(name = "ID")
   private long id;

   /**
    * The component id of the role, not currently exposed.
    */
   @SuppressWarnings("unused")
   @Column(name = "COMPONENTID")
   private long componentId;

   /**
    * The name of the role, never <code>null</code> or empty.
    */
   @Basic
   @Column(name = "NAME")
   private String name;

   /**
    * The normalized name, never <code>null</code> or empty.
    */
   @Basic
   @Column(name = "NORMALNAME")
   private String normalizedName;
   
   /**
    * See {@link #getDescription()}, {@link #setDescription(String)}.
    */
   @Basic
   @Column(name = "DESCRIPTION")
   private String description;
  
}

