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


import com.percussion.utils.tools.PSPatternMatcher;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class provides methods to get a list of all files based on the supplied
 * filter. If the filter includes the directories then it recurses into the
 * directories to search for the files. For example, this class allows to search
 * for all  *.xsl files under Rhythmyx root directory recursing into every
 * subdirectory. {@link FilenameFilter} controls whether to include the
 * directories while search or not.
 * @see FilenameFilter
 */

public class PSFilteredFileList
{
   /**
    * Constructor which takes FilenameFilter object.
    * @param filter must not be <code>null</code>
    * @throws IllegalArgumentException if the argument supplied is <code>null</code>
    * @see FilenameFilter
    */
   public PSFilteredFileList(FilenameFilter filter)
   {
      if(filter == null)
         throw new IllegalArgumentException("file name filter must not be null");
      m_FileNameFilter = filter;
   }

   /**
    * Method to get a list files matching the filter criteria.
    * @param dirName Name of the directory to search for the files, must not be
    * <code>null</code>
    * @return List of files satisfying the criteria, may be <code>null</code>
    * or <code>empty</code>.
    * @throws IllegalArgumentException if the argument supplied is <code>null</code>
    */
   public List getFiles(String dirName)
   {
      if(dirName == null)
         throw new IllegalArgumentException("dirName must not be null");

      File dir = new File(dirName);
      if(!dir.exists())
         return null;
      return getFiles(dir);
   }

   /**
    * Method to get a list files matching the filter criteria.
    * @param dir Directory to search for the files, must not be <code>null</code>
    * @return List of files satisfying the criteria, may be <code>null</code>
    * or <code>empty</code>.
    * @throws IllegalArgumentException if the argument supplied is <code>null</code>
    */
   public List getFiles(File dir)
   {
      if(dir == null)
         throw new IllegalArgumentException("dir must not be null");
      List list = new ArrayList();
      getFiles(dir, list);
      List files = new ArrayList();
      Iterator iter = list.iterator();
      Object obj = null;
      while(iter.hasNext())
      {
         obj = iter.next();
         if(((File)obj).isDirectory())
            continue;
         files.add(obj);
      }
      list = null;
      return files;
   }

   /**
    * Helper method to recurse through the subdirectories to get the files
    * matching the filtering criteria.
    * @param dir Directory to search from, if <code>null</code> returned doing
    * nothing.
    * @param list List object to which the new files will be added. Must not be
    * <code>null</code>
    * @throws IllegalArgumentException if the argument <em>list</em>is <code>null</code>
    */
   private void getFiles(File dir, List list)
   {
      if(list==null)
         throw new IllegalArgumentException("list must must not be null");
      if(dir == null)
         return;
      if(dir.isFile())
      {
         list.add(dir);
         return;
      }
      File[] files = dir.listFiles(m_FileNameFilter);
      for(int i=0; files!=null&&i<files.length; i++)
      {
         if(files[i].isDirectory())
         {
            getFiles(files[i], list);
         }
         else
         {
            list.add(files[i]);
         }
      }
   }

   /**
    * File name filter applied during the search. Never <code>null</code> after
    * this class object is initialized.
    */
   protected FilenameFilter m_FileNameFilter = null;

   /*
    * main method for test purpose
    * @param args
    */
   public static void main(String[] args)
   {
      String LOCADEF_DIR = "e:/Rhythmyx";

      PSPatternMatcher pattern = new PSPatternMatcher('?', '*', "*.xsl");
      PSFileFilter filter = new PSFileFilter(PSFileFilter.IS_FILE|PSFileFilter.IS_DIRECTORY);
      filter.setNamePattern(pattern);
      PSFilteredFileList  lister = new PSFilteredFileList(filter);
      lister.getFiles(LOCADEF_DIR);
   }
}
