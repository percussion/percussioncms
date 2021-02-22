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
package com.percussion.services.security;

import com.percussion.security.IPSPrincipalAttribute;

import java.io.Serializable;
import java.util.List;

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
   
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj);
   }
   
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
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

