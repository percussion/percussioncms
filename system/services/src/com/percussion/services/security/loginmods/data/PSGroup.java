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
package com.percussion.services.security.loginmods.data;

import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Implements a security group
 * 
 * @author dougrand
 */
public class PSGroup implements Group, Serializable
{
   /**
    * serial id
    */
   private static final long serialVersionUID = -6151575982833248128L;

   /**
    * Name of the group, never <code>null</code> or empty.
    */
   private String mi_name = null;

   /**
    * Set of principals, never <code>null</code> may be empty.
    */
   private Set<Principal> mi_principals = new HashSet<>();

   /**
    * Construct a group
    * 
    * @param name The name of the group, not <code>null</code> or empty.
    */
   public PSGroup(String name) 
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      mi_name = name;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.Group#addMember(java.security.Principal)
    */
   public boolean addMember(Principal user)
   {
      if (isMember(user))
         return false;
      else
      {
         mi_principals.add(user);
         return true;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.Group#isMember(java.security.Principal)
    */
   public boolean isMember(Principal member)
   {
      return mi_principals.contains(member);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.Group#removeMember(java.security.Principal)
    */
   public boolean removeMember(Principal user)
   {
      if (!isMember(user))
         return false;
      else
      {
         mi_principals.remove(user);
         return true;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.acl.Group#members()
    */
   @SuppressWarnings(value="unchecked")
   public Enumeration<? extends Principal> members()
   {
      return new IteratorEnumeration(mi_principals.iterator());
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.security.Principal#getName()
    */
   public String getName()
   {
      return mi_name;
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
