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
package com.percussion.services.utils.orm.data;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Primary key for temp ids
 * 
 * @author dougrand
 */
@Embeddable
public class PSTempIdPK implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = -7788659264801764534L;
   
   /**
    * The id for the item ids held in a group. Each group has a different
    * id.
    */
   @Column(name = "ID", nullable = false)
   private long id;
   
   /**
    * The item id, there will be many of these for a given id.
    */
   @Column(name = "ITEM_ID", nullable = false)
   private long itemId;
   
   /**
    * Default Ctor
    */
   public PSTempIdPK()
   {
   }
   
   /**
    * Ctor - note that the id + itemId combination must be unique since this
    * is a primary key.
    * 
    * @param id the id, any long integer
    * @param itemId the an item, any long integer
    */
   public PSTempIdPK(long id, long itemId)
   {
      this.id = id;
      this.itemId = itemId;
   }

   /**
    * @return the id
    */
   public long getId()
   {
      return id;
   }

   /**
    * @param id the id to set
    */
   public void setId(long id)
   {
      this.id = id;
   }

   /**
    * @return the itemId
    */
   public long getItemId()
   {
      return itemId;
   }

   /**
    * @param itemId the itemId to set
    */
   public void setItemId(long itemId)
   {
      this.itemId = itemId;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSTempIdPK)) return false;
      PSTempIdPK that = (PSTempIdPK) o;
      return getId() == that.getId() && getItemId() == that.getItemId();
   }

   @Override
   public int hashCode() {
      return Objects.hash(getId(), getItemId());
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "<TempId " + id + ", " + itemId + ">";
   }
   
   

}
