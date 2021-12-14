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
package com.percussion.services.security;

import java.security.acl.Permission;

public enum PSPermissions implements Permission
{
   /**
    * ACL permission Read.
    */
   READ(10),
   /**
    * ACL permission Update.
    */
   UPDATE(20),
   /**
    * ACL permission Delete.
    */
   DELETE(30),
   /**
    * ACL permission Runtime Visibility.
    */
   RUNTIME_VISIBLE(40),
   /**
    * Owner of the ACL means access to modify the ACL
    */
   OWNER(100);

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
   private PSPermissions(int ord)
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
    * @param s ordinal value must be a valid ordinal number for this
    * enumeration
    * @return an enumerated value if the ordinal does not match
    */
   public static PSPermissions valueOf(int s)
   {
      PSPermissions types[] = values();
      for (int i = 0; i < types.length; i++)
      {
         if (types[i].getOrdinal() == s)
            return types[i];
      }
      throw new IllegalArgumentException("Invalid ordinal " + s //$NON-NLS-1$
         + " for PSPermissions enumeration"); //$NON-NLS-1$
   }
}
