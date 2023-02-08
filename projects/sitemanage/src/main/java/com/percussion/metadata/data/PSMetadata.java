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
package com.percussion.metadata.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractDataObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author erikserating
 *
 */
@Entity
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE, 
      region = "PSMetadata")
@Table(name = "PSX_METADATA")
@XmlRootElement(name = "metadata")
@JsonRootName("metaData")
public class PSMetadata extends PSAbstractDataObject
{
   private static final long serialVersionUID = 1L;
   
   @Id
   @Column(name = "METAKEY")
   private String key;
   
   @Basic
   @Column(name = "DATA")
   private String data;

   /**
    * @param key
    * @param data
    */
   public PSMetadata(String key, String data)
   {
      this.key = key;
      this.data = data;
   }

   /**
    * 
    */
   public PSMetadata()
   {
      
   }

   /**
    * @return the key
    */
   public String getKey()
   {
      return key;
   }

   /**
    * @param key the key to set
    */
   public void setKey(String key)
   {
      this.key = key;
   }

   /**
    * @return the data
    */
   public String getData()
   {
      return data;
   }

   /**
    * @param data the data to set
    */
   public void setData(String data)
   {
      this.data = data;
   }
   
   
   
}
