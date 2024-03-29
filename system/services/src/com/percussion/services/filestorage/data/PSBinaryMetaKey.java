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

package com.percussion.services.filestorage.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="PSX_BINARYMETAKEY")
public class PSBinaryMetaKey implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   @Id
   @GenericGenerator(name="id", strategy="com.percussion.data.utils.PSNextNumberHibernateGenerator")
   @GeneratedValue(generator="id")
   @Column(name = "ID", nullable = false)
   int id;
   @Column(name = "NAME", nullable = false, unique = true)
   String name;
   
   @Column(name = "ENABLED")
   private boolean enabled;
   
 
   @Version
   @Column(name = "version")
   private Integer version;
   
   
   
   public Integer getVersion()
   {
      return version;
   }


   public void setVersion(Integer version)
   {
      this.version = version;
   }

   public PSBinaryMetaKey() {
      
   }
   public PSBinaryMetaKey(String key)
   {
      this(key,true);
   }
   
   public PSBinaryMetaKey(String key,boolean enabled)
   {
      setEnabled(enabled);
      setName(key);
   }
   
   public int getId()
   {
      return id;
   }
   public void setId(int id)
   {
      this.id = id;
   }
   public String getName()
   {
      return name;
   }
   public void setName(String name)
   {
      this.name = name;
   }
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + id;
      return result;
   }
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PSBinaryMetaKey other = (PSBinaryMetaKey) obj;
      if (id != other.id)
         return false;
      return true;
   }
   
   public boolean isEnabled()
   {
      return enabled;
   }


   public void setEnabled(boolean enabled)
   {
      this.enabled = enabled;
   }
   
   
}
