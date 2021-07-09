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
