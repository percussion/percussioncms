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
package com.percussion.services.security.loginmods.data;

import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * An implementation of a security principal
 * 
 * @author dougrand
 */
public class PSPrincipal implements Principal, Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = -1299317290773308656L;
   
   /**
    * Name of the principal, never <code>null</code> or empty.
    */
   String m_name = null;
   
   /**
    * Default ctor
    * @param name name of principal, must never be <code>null</code> or empty
    */
   public PSPrincipal(String name)
   {
      if (name == null || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      
      m_name = name;
   }
   /* (non-Javadoc)
    * @see java.security.Principal#getName()
    */
   public String getName()
   {
      return m_name;
   }


   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSPrincipal)) return false;
      PSPrincipal that = (PSPrincipal) o;
      return Objects.equals(m_name, that.m_name);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_name);
   }
}
