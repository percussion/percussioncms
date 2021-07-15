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
package com.percussion.services.security.loginmods.data;

import java.io.Serializable;
import java.security.Principal;

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
   

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj); 
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }   
}
