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
 * Enumeration for the cross site link types that are processed by this
 * processor. Each type is identified by a unique ordinal value and name. These
 * map closely to the terminology used in the specification.
 */
public enum PSCrossSiteLinkTypeEnum
{
   /**
    * Cross site link type Folder action category "site only"
    */
   CROSSSITE_LINK_SITE_ONLY(1, "site only"),

   /**
    * Cross site link type Folder action category "folder only"
    */
   CROSSSITE_LINK_FOLDER_ONLY(2, "folder only"),

   /**
    * Folder action category "site and folder"
    */
   CROSSSITE_LINK_BOTH(3, "site and folder"),

   /**
    * Folder action category "no site or folder"
    */
   CROSSSITE_LINK_NONE(4, "no site or folder");

   /**
    * Ordinal value, initialized in the ctor, and never modified.
    */
   private int mi_ordinal;

   /**
    * Name value for the cross site link type, initialized in the ctor, never
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
    * Returns the link type name value for the enumeration.
    * 
    * @return the name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return mi_name;
   }

   /**
    * Ctor taking the ordinal value and name of the link type.
    * 
    * @param ord unique ordianl value for the action caegory.
    * @param name name of the action category, must not be <code>null</code>
    * or empty.
    */
   private PSCrossSiteLinkTypeEnum(int ord, String name)
   {
      mi_ordinal = ord;
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      mi_name = name;
   }
}