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
