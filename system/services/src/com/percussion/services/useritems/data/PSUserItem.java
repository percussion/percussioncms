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

package com.percussion.services.useritems.data;

import com.percussion.share.data.PSAbstractDataObject;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Represents PSX_USERITEM table, a relationship table for user names and 
 * item ids with a type.
 */
@Entity
@Cache (usage=CacheConcurrencyStrategy.READ_WRITE, 
region = "PSUserItem")
@Table(name = "PSX_USERITEM")
public class PSUserItem extends PSAbstractDataObject
{
   @Id
   @Column(name = "USERITEMID")    
   private long userItemId= -1L;
   
   @Basic
   @Column(name = "USERNAME", nullable=false)
   private String userName;
   
   @Basic
   @Column(name = "ITEMID", nullable=false)
   private int itemId;

   @Basic
   @Column(name = "TYPE")
   private String type;

   public String getUserName()
   {
      return userName;
   }

   public void setUserName(String userName)
   {
      this.userName = userName;
   }

   public int getItemId()
   {
      return itemId;
   }

   public void setItemId(int itemId)
   {
      this.itemId = itemId;
   }

   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public long getUserItemId()
   {
      return userItemId;
   }

   public void setUserItemId(long userItemId)
   {
      this.userItemId = userItemId;
   }

}
