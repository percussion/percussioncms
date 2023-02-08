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
package com.percussion.content.ui.aa;

/**
 * Node type definitions for the nodes used in the AA.
 */
public enum PSAANodeType
{
   /**
    * AA parent page
    */
   AA_NODE_TYPE_PAGE(0, "/Rhythmyx/sys_resources/images/page.gif"),
   /**
    * AA slot
    */
   AA_NODE_TYPE_SLOT(1, "/Rhythmyx/sys_resources/images/slot.gif"),
   /**
    * AA snippet
    */
   AA_NODE_TYPE_SNIPPET(2, "/Rhythmyx/sys_resources/images/snippet.gif"),
   /**
    * CE Field
    */
   AA_NODE_TYPE_FIELD(3, "/Rhythmyx/sys_resources/images/field.gif");
   /**
    * Construct a type enum.
    * 
    * @param ord The ordinal value to use
    * @param iconUrl The icon URL for the node, may be <code>null</code> or
    * empty.
    */
   private PSAANodeType(int ord, String iconUrl)
   {
      if (ord > Short.MAX_VALUE)
      {
         throw new IllegalArgumentException("Ordinal value too large");
      }

      mi_ordinal = (short) ord;

      mi_iconUrl = iconUrl;
   }

   /**
    * Returns the ordinal value for the enumeration.
    * 
    * @return the ordinal
    */
   public short getOrdinal()
   {
      return mi_ordinal;
   }

   /**
    * Get the icon url associated with the enumeration.
    * 
    * @return the icon url, may be <code>null</code> or empty.
    */
   public String getIconUrl()
   {
      return mi_iconUrl;
   }

   /**
    * Lookup enum value by ordinal. Ordinals should be unique. If they are not
    * unique, then the first enum value with a matching ordinal is returned.
    * 
    * @param s ordinal value
    * @return an enumerated value or <code>null</code> if the ordinal does not
    * match
    */
   public static PSAANodeType valueOf(int s)
      throws IllegalArgumentException
   {
      PSAANodeType types[] = values();
      for (int i = 0; i < types.length; i++)
      {
         if (types[i].getOrdinal() == s)
            return types[i];
      }
      return null;
   }

   /**
    * Ordinal value, initialized in the ctor, and never modified.
    */
   private short mi_ordinal;

   /**
    * Key value, initialized for legacy types in the ctor, never modified, may
    * be <code>null</code>, never empty.
    */
   private String mi_iconUrl = null;


}
