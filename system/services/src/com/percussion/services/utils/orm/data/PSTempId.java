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

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSTempId)) return false;
      PSTempId psTempId = (PSTempId) o;
      return Objects.equals(getPk(), psTempId.getPk());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getPk());
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
