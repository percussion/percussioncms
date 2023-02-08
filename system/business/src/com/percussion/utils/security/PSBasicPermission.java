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
package com.percussion.utils.security;

import java.security.acl.Permission;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A simple basic permission object that can be used with integers or enum
 * ordinal values
 * 
 * @author dougrand
 */
public class PSBasicPermission implements Permission
{
   /**
    * The permission, has the semantics that the caller wishes
    */
   private int m_perm;

   /**
    * Ctor
    * 
    * @param val the value
    */
   public PSBasicPermission(int val) {
      m_perm = val;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      EqualsBuilder b = new EqualsBuilder();
      PSBasicPermission objb = (PSBasicPermission) obj;
      return b.append(m_perm, objb.m_perm).isEquals();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      HashCodeBuilder hcb = new HashCodeBuilder();
      return hcb.append(m_perm).toHashCode();
   }

}
