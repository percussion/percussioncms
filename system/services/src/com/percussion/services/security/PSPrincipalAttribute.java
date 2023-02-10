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
package com.percussion.services.security;

import com.percussion.security.IPSPrincipalAttribute;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Simple implementation of the {@link IPSPrincipalAttribute} interface.
 */
public class PSPrincipalAttribute implements IPSPrincipalAttribute, Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Construct an attribute.
    * 
    * @param name The name of the attribute, may not be <code>null</code> or
    * empty.
    * @param type The type, may not be <code>null</code>.
    * @param values The values, may not be <code>null</code>.
    */
   public PSPrincipalAttribute(String name, PrincipalAttributes type, 
      List<String> values)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      if (type == null)
         throw new IllegalArgumentException("type may not be null");
      
      if (values == null)
         throw new IllegalArgumentException("values may not be null");
      
      m_name = name;
      m_type = type;
      m_values = values;
   }
   
   /* (non-Javadoc)
    * @see com.percussion.security.IPSPrincipalAttribute#getValues()
    */
   public List<String> getValues()
   {
      return m_values;
   }

   /* (non-Javadoc)
    * @see com.percussion.security.IPSPrincipalAttribute#getAttributeType()
    */
   public PrincipalAttributes getAttributeType()
   {
      return m_type;
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
      if (!(o instanceof PSPrincipalAttribute)) return false;
      PSPrincipalAttribute that = (PSPrincipalAttribute) o;
      return Objects.equals(m_values, that.m_values) && m_type == that.m_type && Objects.equals(m_name, that.m_name);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_values, m_type, m_name);
   }

   public String toString()
   {
      return m_name + " : " + m_type + " : " + m_values;
   }

   /**
    * List of values supplied to ctor, never modified after that.
    */
   List<String> m_values;
   
   /**
    * Type supplied to the ctor, never modified after that.
    */
   PrincipalAttributes m_type;
   
   /**
    * The name supplied to the ctor, never modified after that.
    */
   String m_name;
}

