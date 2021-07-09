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
