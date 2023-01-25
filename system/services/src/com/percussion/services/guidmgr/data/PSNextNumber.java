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
