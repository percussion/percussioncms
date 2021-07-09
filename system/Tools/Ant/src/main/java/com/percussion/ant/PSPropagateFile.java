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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.util.FileUtils;


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
