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

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents a single temporary item. These temporary items are created in 
 * large groups, each identified by a particular id. They are destroyed in the
 * same fashion. These ids are used to join with other objects in order to 
 * limit large searches, i.e. the content ids to be checked in a filter.
 * 
 * @author dougrand
 */
@Entity
@Table(name = "PSX_TEMPIDS")
public class PSTempId  implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = -1366422409329232247L;
   
   /**
    * Primary key
    */
   @EmbeddedId
   private PSTempIdPK pk;
   
   /**
    * Default Ctor, used only for hibernate to create these objects for 
    * persistance purposes.
    */
   public PSTempId()
   {
   }
   
   /**
    * Ctor
    * @param pk primary key, never <code>null</code>
    */
   public PSTempId(PSTempIdPK pk)
   {
      if (pk == null)
      {
         throw new IllegalArgumentException("pk may not be null");
      }
      this.pk = pk;
   }

   /**
    * @return the id, never <code>null</code>.
    */
   public PSTempIdPK getPk()
   {
      return pk;
   }

   /**
    * @param id the id to set, never <code>null</code>.
    */
   public void setPk(PSTempIdPK id)
   {
      if (id == null)
      {
         throw new IllegalArgumentException("id may not be null");
      }
      this.pk = id;
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
      return pk.toString();
   }
}
