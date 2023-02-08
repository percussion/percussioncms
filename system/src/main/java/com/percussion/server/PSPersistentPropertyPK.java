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
package com.percussion.server;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

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
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSPersistentPropertyPK)) return false;
      PSPersistentPropertyPK that = (PSPersistentPropertyPK) o;
      return Objects.equals(m_context, that.m_context) && Objects.equals(m_userName, that.m_userName) && Objects.equals(m_propertyName, that.m_propertyName) && Objects.equals(m_category, that.m_category);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_context, m_userName, m_propertyName, m_category);
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
