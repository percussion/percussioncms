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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for filtering list of files in a directory.
 *
 * @see File#listFiles(FilenameFilter) listFiles
 * @see File#list(FilenameFilter) list
 **/
public class PSFilenameFilter implements FilenameFilter
{

   /**
    * Constructor for filename filter with the specified allowed extensions
    * and flag to specify to include directories or not. Comparison for
    * extensions while filtering is case insensitive and extension entries
    * should not include "." in it. If an entry is empty, files with no
    * extension(no "." in it) or files with ending "." are allowed.
    *
    * @param fileExtensions list of file extensions to allow, if
    * <code>null</code> or empty any extension is allowed.
    * @param bIncludeDir flag If <code>true</code>, directories are also
    * included otherwise not.
    *
    **/
   public PSFilenameFilter(List fileExtensions, boolean bIncludeDir)
   {
      m_fileExtensions = fileExtensions;
      m_bIncludeDir = bIncludeDir;
   }

   /**
    * Convenience constructor for {@link #PSFilenameFilter(List, boolean)}.
    * Assumes <code>false</code> for including directories in filtering list.
    *
    * @param fileExtensions list of file extensions to allow, if
    * <code>null</code> or empty any extension is allowed.
    **/
   public PSFilenameFilter(List fileExtensions)
   {
      this(fileExtensions, false);
   }

   /**
    * Convenience constructor for adding a single extension to list of allowed
    * file extensions. Please see <code>PSFileNameFilter(List, boolean)</code>
    * for additional information about filtering file extensions.
    *
    * @param fileExtension Allowed extension, may not be <code>null</code>.
    * If it is empty, files with no extension or files with ending "." are
    * allowed.
    * @param bIncludeDir If <code>true</code>, directories are also included
    * otherwise not.
    *
    * @throws IllegalArgumentException if <code>fileExtension</code> is
    * <code>null</code>.
    **/
   public PSFilenameFilter(String fileExtension, boolean bIncludeDir)
   {
      if(fileExtension == null)
         throw new IllegalArgumentException(
            "The allowed file extension can not be null.");

      m_fileExtensions = new ArrayList();
      m_fileExtensions.add(fileExtension);

      m_bIncludeDir = bIncludeDir;
   }

   /**
    * Convenience constructor for {@link #PSFilenameFilter(String, boolean)}.
    * Assumes <code>false</code> for not to include directories in filtering
    * list.
    *
    * @param fileExtensions list of file extensions to allow, may not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>fileExtension</code> is
    * <code>null</code>.
    **/
   public PSFilenameFilter(String fileExtension)
   {
      this(fileExtension, false);
   }

   /**
    * Default constructor. Assumes <code>null</code> for list of allowed
    * extensions, so that it accepts any extension and <code>false</code> for
    * not to include directories in filtering list.
    **/
   public PSFilenameFilter()
   {
      m_fileExtensions = null;
      m_bIncludeDir = false;
   }

   /**
    * Implementation for the interface. If list of allowed extensions is
    * <code>null</code> it accepts any extension or file without extension too.
    * If the flag to include directories is <code>true</code>, it accepts
    * directories also, otherwise not.
    *
    * @see FilenameFilter#accept(File, String) accept
    **/
   public boolean accept(File dir, String name)
   {
      File file = new File(dir, name);

      boolean accept = false;

      if(file.isDirectory())
      {
         if(m_bIncludeDir)
            accept = true;
      }
      else {
         if(m_fileExtensions == null || m_fileExtensions.isEmpty())
            accept = true;
         else
         {
            String fileExtension = "";
            int index = name.lastIndexOf('.');
            if(index != -1)
               fileExtension = name.substring(index+1);
            //Files with no file extensions are allowed if an empty string
            //is an entry in file extensions list
            Iterator iter = m_fileExtensions.iterator();
            while(iter.hasNext())
            {
               if( fileExtension.equalsIgnoreCase((String)iter.next()) )
               {
                  accept = true;
                  break;
               }
            }
         }
      }
      return accept;
   }

   /**
    * List of allowed extensions, gets initialized in constructor. If it is
    * <code>null</code>, any extension is allowed. The default value is
    * <code>null</code>.
    **/
   private List m_fileExtensions;
   /**
    * Flag to indicate whether directories should be included in list of files
    * or not, gets initialized in constructor. The default value is
    * <code>false</code>.
    */
   private boolean m_bIncludeDir;
}
