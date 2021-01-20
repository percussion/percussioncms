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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.guidmgr.data;


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;

import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Storage for GUID allocation records.
 *  
 * @author dougrand
 */
@Entity
@Table(name = "NEXTNUMBER")
public class PSNextNumber
{
   @Id
   @Column(name = "KEYNAME")
   String key;
   
   @Basic
   @Column(name = "NEXTNR")
   Integer next;

   /**
    * Default public ctor
    */
   public PSNextNumber()
   {
      key = null;
      next = 0;
   }
   
   /**
    * Ctor 
    * @param key the key, never <code>null</code> or empty
    * @param initial the initial next number value
    */
   public PSNextNumber(String key, int initial) {
      if (StringUtils.isBlank(key))
      {
         throw new IllegalArgumentException("key may not be null or empty");
      }
      this.key = key;
      next = initial;
   }

   /**
    * @return Returns the key.
    */
   public String getKey()
   {
      return key;
   }

   /**
    * @param key The key to set.
    */
   public void setKey(String key)
   {
      this.key = key;
   }

   /**
    * @return Returns the next.
    */
   public Integer getNext()
   {
      return next;
   }

   /**
    * @param next The next to set.
    */
   public void setNext(Integer next)
   {
      this.next = next;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return key.hashCode();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "<NextNumber key=" + key + " next=" + next + ">";
   }
}
