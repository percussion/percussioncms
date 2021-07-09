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
package com.percussion.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Fails build if finds files from directory one which do not match the
 * equivalently named files from directory two.
 */
public class PSCompareFiles extends Task
{
   /**
    * This directory contains files which will be scanned and compared
    * against those found in the directory specified by {@link #m_dir2}.
    * 
    * @param dir the first directory of files to be compared.
    */
   public void setDir1(File dir)
   {
      m_dir1 = dir;
   }
   
   /**
    * This directory contains files which will be scanned and compared
    * against those found in the directory specified by {@link #m_dir1}.
    * 
    * @param dir the second directory of files to be compared.
    */
   public void setDir2(File dir)
   {
      m_dir2 = dir;
   }
   
   /**
    * Executes the task.  For each file under the first directory it walks the
    * list of files under the second directory in search of a file with the
    * same name.  If one is found, the two files are compared based on size.  If
    * the files do not match, the file name is logged and a build exception is
    * thrown. 
    * @see org.apache.tools.ant.Task#execute()
    */
   @Override
   public void execute() throws BuildException
   {
      List<String> modFileNames = new ArrayList<String>();
      
      if (m_dir1 == null)
      {
         throw new BuildException("dir1 may not be null");
      }
      
      if (!m_dir1.isDirectory())
      {
         throw new BuildException("dir1 must be a directory");
      }
      
      File[] dir1Files = m_dir1.listFiles();
      
      if (m_dir2 == null)
      {
         throw new BuildException("dir2 may not be null");
      }
      
      if (!m_dir2.isDirectory())
      {
         throw new BuildException("dir2 must be a directory");
      }
      
      File[] dir2Files = m_dir2.listFiles();
      
      for (File dir1File : dir1Files)
      {
         String dir1FileName = dir1File.getName();
         for (File dir2File : dir2Files)
         {
            if (dir2File.getName().equals(dir1FileName))
            {
               if (dir2File.length() != dir1File.length())
                  modFileNames.add(dir1FileName);
               
               break;
            }
         }
      }
      
      String errMsg = "The following files found in both " +
         m_dir1.getAbsolutePath() + " and " + m_dir2.getAbsolutePath() + " " +
         "should be of the same size but are not: ";
      boolean throwException = !modFileNames.isEmpty();
      
      int i = 0;
      for (String fileName : modFileNames)
      {
         errMsg += fileName;
         
         if (i < modFileNames.size() - 1)
            errMsg += ", ";
         
         i++;
         
         throwException = true;
      }
      
      if (throwException)
      {
         throw new BuildException(errMsg);
      }
   }
   
   /**
    * The first directory of files.  This directory forms the base of the
    * comparison.
    */
   private File m_dir1;
   
   /**
    * The second directory of files.  This directory is compared to the first
    * directory.
    */
   private File m_dir2;
}
