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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

/**
 * Stores the metadata entries for the item
 * @author stephenbolton
 *
 */
@Entity
@Table(name="PSX_BINARYMETAENTRY")
public class PSBinaryMetaEntry implements Serializable
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
   
   @ManyToOne
   @JoinColumn(name="KEY_ID")
   PSBinaryMetaKey key;
   
   
   @Column(name = "VALUE")
   String value;
  
   @ManyToOne(fetch=FetchType.LAZY)
   @JoinColumn(name="BINARY_ID")
   private PSBinary binary; 
   
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

   
   public PSBinary getBinary()
   {
      return binary;
   }

   public void setBinary(PSBinary binary)
   {
      this.binary = binary;
   }
   
   public PSBinaryMetaEntry() 
   {
      
   }
   public PSBinaryMetaEntry(PSBinaryMetaKey key, String value)
   {
      this.value=value;
      this.key=key;
   }
   public int getId()
   {
      return id;
   }
   public void setId(int id)
   {
      this.id = id;
   }
   public PSBinaryMetaKey getKey()
   {
      return key;
   }
   public void setKey(PSBinaryMetaKey key)
   {
      this.key = key;
   }
   public String getValue()
   {
      return (value==null) ? "" : value;
   }
   public void setValue(String value)
   {
      this.value = (value==null) ? "" : value;
   }
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + id;
      result = prime * result + ((key == null) ? 0 : key.hashCode());
      result = prime * result + ((value == null) ? 0 : value.hashCode());
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
      PSBinaryMetaEntry other = (PSBinaryMetaEntry) obj;
      if (id != other.id)
         return false;
      if (key == null)
      {
         if (other.key != null)
            return false;
      }
      else if (!key.equals(other.key))
         return false;
      if (value == null)
      {
         if (other.value != null)
            return false;
      }
      else if (!value.equals(other.value))
         return false;
      return true;
   }


   @Override
   public String toString()
   {
      return "PSBinaryMetaEntry [id=" + id + ", key=" + key + ", value=" + value + ", binaryid=" + binary.getId() + ", version="
            + version + "]";
   }
   
   
}
