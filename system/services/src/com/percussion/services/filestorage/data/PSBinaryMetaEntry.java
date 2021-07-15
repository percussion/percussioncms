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
