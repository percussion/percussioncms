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

package com.percussion.server.webservices.crosssite;

import org.apache.commons.lang.StringUtils;

/**
 * Enumeration for the remove from folder action categories that are processed
 * by this processor per item being processed basis. Each category is identified
 * by a unique ordinal value and name.
 */
public enum PSRemoveActionCategoryEnum
{
   /**
    * Folder remove action category -> item exists in only one site folder
    */
   ACTION_CATEGORY_ONLY_SITEFOLDER(1, "only site folder"),

   /**
    * Folder remove action category -> item exists on the same site but multiple
    * folders
    */
   ACTION_CATEGORY_SAMESITE_MULTIPLE_FOLDERS(2, "same site multpile folders"),

   /**
    * Folder remove action category -> item exists only one folder in one site
    * but also exists in other sites.
    */
   ACTION_CATEGORY_MULTIPLE_SITES(3, "multiple sites"),

   /**
    * Folder remove action category -> item being processed is not under any
    * site folder, meaning it is from a non-site folder.
    */
   ACTION_CATEGORY_NONSITE_FOLDER(4, "non-site folder");

   /**
    * Ordinal value, initialized in the ctor, and never modified.
    */
   private int mi_ordinal;

   /**
    * Name value for the action category, initialized in the ctor, never
    * modified.
    */
   private String mi_name = null;

   /**
    * Returns the ordinal value for the enumeration.
    * 
    * @return the ordinal
    */
   public int getOrdinal()
   {
      return mi_ordinal;
   }

   /**
    * Returns the action category name value for the enumeration.
    * 
    * @return the name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return mi_name;
   }

   /**
    * Ctor taking the ordinal value and name of the action category.
    * 
    * @param ord unique ordianl value for the action caegory.
    * @param name name of the action category, must not be <code>null</code>
    * or empty.
    */
   private PSRemoveActionCategoryEnum(int ord, String name)
   {
      mi_ordinal = ord;
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      mi_name = name;
   }
}
