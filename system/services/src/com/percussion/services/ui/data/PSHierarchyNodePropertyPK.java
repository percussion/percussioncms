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
package com.percussion.services.ui.data;

import java.io.Serializable;


import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * The primary key for {@link PSHierarchyNodeProperty} object. This is
 * needed for the persistent service layer.
 */
@Embeddable
public class PSHierarchyNodePropertyPK implements Serializable
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = 217903375911712767L;

   @Column(name = "NODE_ID", nullable = false)
   private long nodeId;

   @Column(name = "NAME", nullable = false)
   private String name;


   /**
    * Default ctor, needed by the services of the persistent layer.
    */
   private PSHierarchyNodePropertyPK()
   {
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;
      
      PSHierarchyNodePropertyPK other = (PSHierarchyNodePropertyPK) obj;

      return new EqualsBuilder()
         .append(nodeId, other.nodeId)
         .append(name, other.name)
         .isEquals();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      return new HashCodeBuilder(13, 3).appendSuper(super.hashCode())
            .append(nodeId)
            .append(name)
            .toHashCode();
   }
}

