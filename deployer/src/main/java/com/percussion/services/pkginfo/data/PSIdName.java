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

package com.percussion.services.pkginfo.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Class representing a dependency id-name mapping, used to map deployer package
 * elements which use names as id's to a corresponding id.
 */
@Entity
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE, region = "PSIdName")
@Table(name = "PSX_ID_NAME")
public class PSIdName implements Serializable
{
   /**
    * Unique identifier for this entry is the user readable representation of a
    * GUID.
    */
   @Id
   @Column(name = "DEP_ID", nullable = false)   
   private String depId;
   
   /**
    * The name of the package element which corresponds to the GUID.
    */
   @Column(name = "DEP_NAME", nullable = false)
   private String depName;
   
   /**
    * Default private constructor.  Needed by hibernate.
    */
   @SuppressWarnings("unused")
   private PSIdName()
   {
   }
   
   /**
    * Creates an id-name mapping.
    * 
    * @param id The id, may not be <code>null</code> or empty.  Must be a
    * user readable representation of a GUID.  See {@link PSGuid#toString()}.
    * @param name The name, may not be <code>null</code> or empty.
    */
   public PSIdName(String id, String name)
   {
      if (StringUtils.isBlank(id))
         throw new IllegalArgumentException("id may not be null or empty");
      
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      depId = id;
      depName = name;
   }
   
   /**
    * Get the id uniquely identifying this element.
    * 
    * @return The GUID in user readable form, may be <code>null</code>.
    */
   public String getId()
   {
      return depId;
   }

   /**
    * Get the name of the dependency element which corresponds to the id.
    * 
    * @return The name, may be <code>null</code>.
    */
   public String getName()
   {
      return depName;
   }

   /**
    * Get the type of the dependency element which corresponds to the id.
    * 
    * @return The type, may be <code>null</code>.
    */
   public PSTypeEnum getType()
   {
      return PSTypeEnum.valueOf((new PSGuid(depId)).getType());
   }
   
   /**
    * Set the id.  See {@link #getId()}.
    * 
    * @param id The id, may not be <code>null</code> or empty.
    */
   public void setId(String id)
   {
      if (StringUtils.isBlank(id))
         throw new IllegalArgumentException("id may not be null or empty");
      
      depId = id;
   }

   /**
    * Set the name of the package element which corresponds to the id. See
    * {@link #getName()}.
    * 
    * @param name The name of the package element which corresponds to the id,
    * may not be <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      depName = name;
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof PSIdName))
         return false;
      if (this == obj)
         return true;

      PSIdName other = (PSIdName) obj;
      
      return new EqualsBuilder().append(depId, other.depId).append(
         depName, other.depName).isEquals();
   }

   @Override
   public int hashCode()
   {
      return (new PSGuid(depId)).hashCode();
   }
}
