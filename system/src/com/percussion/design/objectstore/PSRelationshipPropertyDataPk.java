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
package com.percussion.design.objectstore;

import java.io.Serializable;


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * The primary key for {@link PSRelationshipPropertyData} object. This is
 * needed for the persistent service layer.
 */
@Embeddable
public class PSRelationshipPropertyDataPk implements Serializable
{
   /**
    * The relationship (or parent) id.
    */
   @Basic
   @Column(name = "RID")
   private int m_rid;

   /**
    * The name of the property, never <code>null</code>
    */
   @Basic
   @Column(name = "PROPERTYNAME")
   private String m_propertyName;


   /**
    * Default ctor, needed by the services of the persistent layer.
    */
   private PSRelationshipPropertyDataPk()
   {
      // Empty
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof PSRelationshipPropertyDataPk))
         return false;
      PSRelationshipPropertyDataPk other = (PSRelationshipPropertyDataPk) obj;

      return new EqualsBuilder()
         .append(m_propertyName, other.m_propertyName)
         .append(m_rid, other.m_rid)
         .isEquals();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      return new HashCodeBuilder(13, 3).appendSuper(super.hashCode())
            .append(m_rid)
            .append(m_propertyName)
            .toHashCode();
   }
}
