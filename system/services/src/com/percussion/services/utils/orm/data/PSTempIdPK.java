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
package com.percussion.services.utils.orm.data;

import java.io.Serializable;

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
      return HashCodeBuilder.reflectionHashCode(this);
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
