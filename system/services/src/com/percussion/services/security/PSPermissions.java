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
