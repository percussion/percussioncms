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
package com.percussion.server;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * The composit key for {@link PSPersistentProperty} object.  It is the 
 * primary key of the persistent properties table. This is only needed by 
 * Hibernate implementation, so it should only be accessible in package level. 
 */
@Embeddable
public class PSPersistentPropertyPK implements Serializable
{
   /**
    * Default constructor, which is needed by hibernate
    */
   private PSPersistentPropertyPK()
   {
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

   /**
    * Interrelated usages in which a property might be used e.g whether it's
    * designer or a system property.
    */
   @SuppressWarnings("unused")
   @Column(name = "CONTEXT", nullable = false)
   private String m_context = "";

   /**
    * The fully qualified name of the principal associated with the property.
    */
   @SuppressWarnings("unused")
   @Column(name = "USERNAME", nullable = false)
   private String m_userName  = "";

   /**
    * The case-sensitive name of the property to be persisted or overridden.
    */
   @SuppressWarnings("unused")
   @Column(name = "PROPERTYNAME", nullable = false)
   private String m_propertyName = "";

   /**
    * An arbitrary string used to group related properties together.
    * All categories beginning with sys_ are reserved by the system.
    */
   @SuppressWarnings("unused")
   @Column(name = "CATEGORY", nullable = false)
   private String m_category = "";
}
