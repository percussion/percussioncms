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

import com.percussion.utils.security.IPSTypedPrincipal;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Simple implementation of the {@link IPSTypedPrincipal} interface.
 */
public class PSTypedPrincipal implements IPSTypedPrincipal, Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Factory method to create a principal typed as a subject.
    * 
    * @param name The name, may not be <code>null</code> or empty.
    * 
    * @return The principal, never <code>null</code>.
    */
   public static PSTypedPrincipal createSubject(String name)
   {
      return new PSTypedPrincipal(name, PrincipalTypes.SUBJECT);
   }

   /**
    * Factory method to create a principal typed as a group.
    * 
    * @param name The name, may not be <code>null</code> or empty.
    * 
    * @return The principal, never <code>null</code>.
    */
   public static PSTypedPrincipal createGroup(String name)
   {
      return new PSTypedPrincipal(name, PrincipalTypes.GROUP);
   }

   /**
    * Factory method to create a principal typed as undefined.
    * 
    * @param name The name, may not be <code>null</code> or empty.
    * 
    * @return The principal, never <code>null</code>.
    */
   public static PSTypedPrincipal createUndefined(String name)
   {
      return new PSTypedPrincipal(name, PrincipalTypes.UNDEFINED);
   }

   /**
    * Construct a typed principal.
    * 
    * @param name The name of the principal, may not be <code>null</code> or
    * empty.
    * @param type The type of principal, may not be <code>null</code>.
    */
   public PSTypedPrincipal(String name, PrincipalTypes type)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");

      if (type == null)
         throw new IllegalArgumentException("type may not be null");

      m_name = name;
      m_type = type;
   }

   /**
    * Default ctor. Added to keep serializers happy. Should not be used.
    */
   public PSTypedPrincipal()
   {

   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.utils.security.IPSTypedPrincipal#getPrincipalType()
    */
   public PrincipalTypes getPrincipalType()
   {
      return m_type;
   }

   /*
    * (non-Javadoc)
    * 
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
      return m_name + " : " + m_type;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.utils.security.IPSTypedPrincipal#isType(com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes)
    */
   public boolean isType(PrincipalTypes principalType)
   {
      return m_type.equals(principalType);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.utils.security.IPSTypedPrincipal#isCommunity()
    */
   public boolean isCommunity()
   {
      return m_type.equals(PrincipalTypes.COMMUNITY);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.utils.security.IPSTypedPrincipal#isRole()
    */
   public boolean isRole()
   {
      return m_type.equals(PrincipalTypes.ROLE);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.utils.security.IPSTypedPrincipal#isUser()
    */
   public boolean isUser()
   {
      return m_type.equals(PrincipalTypes.USER);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.utils.security.IPSTypedPrincipal#isGroup()
    */
   public boolean isGroup()
   {
      return m_type.equals(PrincipalTypes.GROUP);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.utils.security.IPSTypedPrincipal#isSubject()
    */
   public boolean isSubject()
   {
      return m_type.equals(PrincipalTypes.SUBJECT);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.utils.security.IPSTypedPrincipal#isSystemEntry()
    */
   public boolean isSystemEntry()
   {
      return m_type.equals(PrincipalTypes.USER)
         && m_name.equals(DEFAULT_USER_ENTRY);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.utils.security.IPSTypedPrincipal#isSystemCommunity()
    */
   public boolean isSystemCommunity()
   {
      return m_type.equals(PrincipalTypes.COMMUNITY)
         && m_name.equals(ANY_COMMUNITY_ENTRY);
   }

   /**
    * The type, never <code>null</code> or modified after construction.
    */
   PrincipalTypes m_type;

   /**
    * The name, The type, never <code>null</code>, empty, or modified after
    * construction.
    */
   String m_name;

   /**
    * The name of the principle found in the entry to be used as the default
    * only if no user, group, or role entries are matched.
    */
   public static final String DEFAULT_USER_ENTRY = "Default";

   /**
    * The name of the principle found in the entry to be used only if no other
    * community entry is matched.
    */
   public static final String ANY_COMMUNITY_ENTRY = "AnyCommunity";
}
