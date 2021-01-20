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
package com.percussion.services.publisher.data;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Embedded primary key class for the edition content list.
 * 
 * @author dougrand
 */
@Embeddable
public class PSEditionContentListPK implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 379367292031206623L;

   long editionclistid = -1L;

   long editionid;

   long contentlistid;

   /**
    * @return the contentlistid
    */
   public long getContentlistid()
   {
      return contentlistid;
   }

   /**
    * @param contentlistid the contentlistid to set
    */
   public void setContentlistid(long contentlistid)
   {
      this.contentlistid = contentlistid;
   }

   /**
    * @return the editionclistid
    */
   public long getEditionclistid()
   {
      return editionclistid;
   }

   /**
    * @param editionclistid the editionclistid to set
    */
   public void setEditionclistid(long editionclistid)
   {
      this.editionclistid = editionclistid;
   }

   /**
    * @return the editionid
    */
   public long getEditionid()
   {
      return editionid;
   }

   /**
    * @param editionid the editionid to set
    */
   public void setEditionid(long editionid)
   {
      this.editionid = editionid;
   }

   /**
    * Determines if the object has been initialized with a valid id.
    * 
    * @return <code>true</code> if the object has been initialized,
    * <code>false</code> otherwise.
    */
   public boolean isInitialized()
   {
      return editionclistid != -1L;
   }
   
   @Override
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }
}
