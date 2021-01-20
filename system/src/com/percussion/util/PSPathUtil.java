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
package com.percussion.util;

import org.apache.commons.lang.StringUtils;

/**
 * Utilities for working with paths
 */
public class PSPathUtil
{
   /**
    * The root folder for all sites. 
    */
   public static final String SITES_ROOT = "//Sites";
   
   /**
    * Private constructor so this class can not be instantiated
    * 
    */
   private PSPathUtil(){}
   
   /**
    * Determines if the path passed in resides under the //Sites
    * folder hierarchy
    * 
    * @param path the path to validate, cannot be <code>null</code>
    * @return <code>true</code> if this path is under the //Sites root
    */
   public static boolean isPathUnderSiteFolderRoot(String path)
   {
      if(path == null)
         throw new IllegalArgumentException("path cannot be null.");
      return path.startsWith(SITES_ROOT);
   }
   
   /**
    * Normalizes the supplied path to use "/" file separators.
    *
    * @param path The path to normalize, it may be <code>null</code> or empty.
    *
    * @return The normalized path, it may be <code>null</code> or empty if the
    * given path is <code>null</code> or empty.
    */
   public static String getNormalizedPath(String path)
   {
      if (StringUtils.isBlank(path))
         return path;

      return path.replace('\\', '/');
   }
}
