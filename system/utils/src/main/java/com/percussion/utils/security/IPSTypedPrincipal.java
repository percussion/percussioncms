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
package com.percussion.utils.security;

import java.security.Principal;

/**
 * Represents a principal with a specified type.
 */
public interface IPSTypedPrincipal extends Principal
{
   /**
    * Types of principals that may be represented.
    */
   public enum PrincipalTypes 
   {
      /**
       * Indicates the principal represents a community name. 
       */
      COMMUNITY(10),
      /**
       * Indicates the principal represents a role name. 
       */
      ROLE(20),
      /**
       * Indicates the principal represents a user name. 
       */
      USER(30),
      /**
       * Indicates the principal represents a group name. 
       */
      GROUP(40),

      /**
       * Indicates the principal represents a subject name. 
       */
      SUBJECT(50),
      
      /**
       * Indicates the type the principal represents is undefined.
       */
      UNDEFINED(-1);
      
      /**
       * Ordinal value, initialized in the ctor, and never modified.
       */
      private short ordinal;

      /**
       * Returns the ordinal value for the enumeration.
       * 
       * @return the ordinal
       */
      public short getOrdinal()
      {
         return ordinal;
      }

      /**
       * Private ctor using the ordinal value
       * 
       * @param ord The ordingal value, must be a valid value
       */
      private PrincipalTypes(int ord)
      {
         if (ord > Short.MAX_VALUE)
         {
            throw new IllegalArgumentException("Ordinal value too large"); 
         }
         ordinal = (short) ord;
      }

      /**
       * Lookup enum value by ordinal. Ordinals should be unique. If they are
       * not unique, then the first enum value with a matching ordinal is
       * returned. Exception is thrown if ordinal value not found in the
       * enumeration.
       * 
       * @param s ordinal value must be a valid ordianl number for this
       * enumeration
       * @return an enumerated value if the ordinal does not match
       * @throws IllegalArgumentException if the ordinal does not have
       * corresponding enumeration.
       */
      public static PrincipalTypes valueOf(int s) throws IllegalArgumentException
      {
         PrincipalTypes types[] = values();
         for (int i = 0; i < types.length; i++)
         {
            if (types[i].getOrdinal() == s)
               return types[i];
         }
         throw new IllegalArgumentException("Invalid ordinal " + s //$NON-NLS-1$
            + " for PrincipalTypes enumeration"); //$NON-NLS-1$
      }
   }

   /**
    * Test if the principaltype specified matches with this.
    * 
    * @param principalType Entry type to check, must be one fo the PrincipalTypes
    * enumerations.
    * @return <code>true</code> if supplied entry type matches with this
    * object's type <code>false</code> otherwise.
    */
   public boolean isType(PrincipalTypes principalType);

   /**
    * Is this principal a community?
    * 
    * @return <code>true</code> if this entry type is community
    * <code>false</code> otherwise.
    */
   public boolean isCommunity();

   /**
    * Is this ACL entry a community?
    * 
    * @return <code>true</code> if this entry type is role <code>false</code>
    * otherwise.
    */
   public boolean isRole();

   /**
    * Is this principal a user?
    * 
    * @return <code>true</code> if this entry type is user (or system entry)
    * <code>false</code> otherwise.
    */
   public boolean isUser();

   /**
    * Is this principal a group?
    * 
    * @return <code>true</code> if this entry type is group <code>false</code>
    * otherwise.
    */
   public boolean isGroup();
   
   /**
    * Is this principal a subject?
    * 
    * @return <code>true</code> if this principal type is subject<code>false</code>
    * otherwise.
    */
   public boolean isSubject();
   
   /**
    * Is this principal a system entry?
    * 
    * @return <code>true</code> if this entry type special user entry
    * <code>false</code> otherwise.
    */
   public boolean isSystemEntry();

   /**
    * Is this principal a system community?
    * 
    * @return <code>true</code> if this entry system community
    * <code>false</code> otherwise.
    */
   public boolean isSystemCommunity();

   /**
    * Get principal type.
    * 
    * @return one of the PrincipalTypes enumerations.
    */
   public PrincipalTypes getPrincipalType();
}

