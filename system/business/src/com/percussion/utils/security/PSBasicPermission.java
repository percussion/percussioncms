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
