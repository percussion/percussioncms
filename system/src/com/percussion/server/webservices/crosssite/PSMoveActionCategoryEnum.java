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

package com.percussion.server.webservices.crosssite;

import org.apache.commons.lang.StringUtils;

/**
 * Enumeration for the folder action categories that are processed by this
 * processor. Each category is identified by a unique ordinal value and name.
 */
public enum PSMoveActionCategoryEnum
{
   /**
    * Folder move action category "reorganize" -> move within the same site to
    * another folder
    */
   ACTION_CATEGORY_REORGANIZE(1, "reorganize"),

   /**
    * Folder move action category "reclassify" -> move from a folder one site to
    * a folder on another site.
    */
   ACTION_CATEGORY_RECLASSIFY(2, "reclassify"),

   /**
    * Folder move action category "archive" -> move from a site folder to
    * nonsite folder
    */
   ACTION_CATEGORY_ARCHIVE(3, "stage/archive"),

   /**
    * Folder move action category "deploy" -> nonsite folder to site folder
    */
   ACTION_CATEGORY_DEPLOY(4, "deploy/recover"),

   /**
    * Folder move action category "none" -> nonsite folder to nonsite folder
    */
   ACTION_CATEGORY_NONE(5, "non-site folder to non-site folder");

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
   private PSMoveActionCategoryEnum(int ord, String name)
   {
      mi_ordinal = ord;
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      mi_name = name;
   }
}
