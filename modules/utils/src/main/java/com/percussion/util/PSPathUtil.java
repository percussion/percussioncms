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
