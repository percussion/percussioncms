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
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
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

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSGroup)) return false;
      PSGroup psGroup = (PSGroup) o;
      return Objects.equals(mi_name, psGroup.mi_name) && Objects.equals(mi_principals, psGroup.mi_principals);
   }

   @Override
   public int hashCode() {
      return Objects.hash(mi_name, mi_principals);
   }
}
