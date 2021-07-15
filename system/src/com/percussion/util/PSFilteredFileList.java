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
package com.percussion.util;


import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.percussion.utils.tools.PSPatternMatcher;

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
