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
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * Propagates a file to any number of directories specified in
 * a directory set.
 */
public class PSPropagateFile extends Task
{

   /**
    * Sets the source file path
    * @param src should not be <code>null</code>.
    */
   public void setSrc(File src)
   {
      m_srcFile = src;
   }
   
   /**
    * Allow the adding of directory sets
    * @param ds the directory set , may be <ocd>null</code>.
    */
   public void addDirset(DirSet ds)
   {
      if(ds != null)
         m_fileSets.add(ds);
   }
   
   /** 
    * Executes the file propagation
    */
   @Override
   public void execute()
      throws BuildException
   {
      
      if (m_srcFile == null)
      {
         throw new BuildException(
            "The src attribute must be present.", location);
      }

      if (!m_srcFile.exists())
      {
         throw new BuildException(
            "src " + m_srcFile.toString() + " does not exist.", location);
      }

      try
      {
         List<String> directories = new ArrayList<String>();
         Iterator it = m_fileSets.iterator();
         FileUtils utils = FileUtils.newFileUtils();
         File baseDir = null;
         int count = 0;
         while(it.hasNext())
         {
            DirSet fs = (DirSet)it.next();           
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());          
            baseDir = ds.getBasedir();
            String[] dirs = ds.getIncludedDirectories();
            directories.addAll(Arrays.asList(dirs));
         }
         it = directories.iterator();
         
         while(it.hasNext())
         {
            
            
            File df = 
               new File(baseDir + "/" + 
                  (String)it.next() +
                  "/" +m_srcFile.getName());
                 
            utils.copyFile(m_srcFile, df);
            count++;
            
         }
         log("Propagated '" + m_srcFile.getName() +
            "' to " + count + " directories.");
         
      }
      catch (IOException ioe)
      {
         String msg =
            "Error copying file: " + m_srcFile.getAbsolutePath() + " due to " +
            ioe.getMessage();
         throw new BuildException(msg);
      }
   }

   private File m_srcFile;
   private List<DirSet> m_fileSets = new ArrayList<DirSet>();
}
