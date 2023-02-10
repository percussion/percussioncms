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
package com.percussion.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
