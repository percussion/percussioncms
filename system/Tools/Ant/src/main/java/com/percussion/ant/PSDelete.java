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
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.FileSet;




/**
 * This class extends the Ant Delte task so that we can now map
 * the files in one directory to another and delete the mapped files.
 */
public class PSDelete extends Delete
{
   /**
    * If false, note errors but continue.
    *
    * @param failonerror true or false
    */
   public void setFailOnError(boolean failonerror)
   {
      super.setFailOnError(failonerror);
      m_failonerror = failonerror;
   }

   public void setMappeddir(String path)
   {
      m_mappedDir = path;
   }

   /**
    * If true and the file does not exist, do not display a diagnostic
    * message or modify the exit status to reflect an error.
    * This means that if a file or directory cannot be deleted,
    * then no error is reported. This setting emulates the
    * -f option to the Unix &quot;rm&quot; command.
    * Default is false meaning things are &quot;noisy&quot;
    * @param quiet "true" or "on"
    */
   public void setQuiet(boolean quiet)
   {
      super.setQuiet(quiet);
      m_quiet = quiet;

      if (quiet)
      {
         m_failonerror = false;
      }
   }

   /**
    * If true, list all names of deleted files.
    *
    * @param verbose "true" or "on"
    */
   public void setVerbose(boolean verbose)
   {
      super.setVerbose(verbose);

      if (verbose)
      {
         m_verbosity = Project.MSG_INFO;
      }
      else
      {
         m_verbosity = Project.MSG_VERBOSE;
      }
   }
   
   /**
    * Delete the file(s).
    */
   public void execute() throws BuildException {
       if (usedMatchingTask) {
           log("DEPRECATED - Use of the implicit FileSet is deprecated.  "
               + "Use a nested fileset element instead.");
       }

       if (file == null && dir == null && filesets.size() == 0) {
           throw new BuildException("At least one of the file or dir "
                                    + "attributes, or a fileset element, "
                                    + "must be set.");
       }

       if (m_quiet && m_failonerror) {
           throw new BuildException("quiet and failonerror cannot both be "
                                    + "set to true", getLocation());
       }
        

       // delete the single file
       if (file != null) {
           if (file.exists()) {
               if (file.isDirectory()) {
                   log("Directory " + file.getAbsolutePath() 
                       + " cannot be removed using the file attribute.  "
                       + "Use dir instead.");
               } else {
                   log("Deleting: " + file.getAbsolutePath());

                   if (!file.delete()) {
                       String message = "Unable to delete file " 
                           + file.getAbsolutePath();
                       if (m_failonerror) {
                           throw new BuildException(message);
                       } else { 
                           log(message, m_quiet ? Project.MSG_VERBOSE 
                                              : Project.MSG_WARN);
                       }
                   }
               }
           } else {
               log("Could not find file " + file.getAbsolutePath() 
                   + " to delete.", 
                   Project.MSG_VERBOSE);
           }
       }

       // delete the directory
       
       if (dir != null && dir.exists() && dir.isDirectory() && 
           !usedMatchingTask) 
       {
           boolean isUsingMapping = 
              (m_mappedDir != null && m_mappedDir.trim().length() > 0);
           List<File> dirs = new ArrayList<File>();
           if(isUsingMapping)
           {
             dirs = getAllValidPackageDirs(dir);
           }
           else
           {
              dirs.add(dir);
           }
           
           Iterator it = dirs.iterator();
           while(it.hasNext())
           {
              File f = (File)it.next();
              if(isUsingMapping && !f.exists())
              {
                 if (m_verbosity == Project.MSG_VERBOSE)
                 {
                    log("Skipping non-existant directory "
                       + f.getAbsolutePath());
                 } 
              }
              else
              {
                 if (m_verbosity == Project.MSG_VERBOSE)
                 {
                    log("Deleting directory " + f.getAbsolutePath());
                 }
                 removeDir(f);
              }
              
           }   
       }

       // delete the files in the filesets
       for (int i = 0; i < filesets.size(); i++) {
           FileSet fs = (FileSet) filesets.elementAt(i);
           try {
               DirectoryScanner ds = fs.getDirectoryScanner(getProject());
               String[] files = ds.getIncludedFiles();
               String[] dirs = ds.getIncludedDirectories();
               removeFiles(fs.getDir(getProject()), files, dirs);
           } catch (BuildException be) {
               // directory doesn't exist or is not readable
               if (m_failonerror) {
                   throw be;
               } else {
                   log(be.getMessage(), 
                       m_quiet ? Project.MSG_VERBOSE : Project.MSG_WARN);
               }
           }
       }

       // delete the files from the default fileset
       if (usedMatchingTask && dir != null) {
           try {
               DirectoryScanner ds = super.getDirectoryScanner(dir);
               String[] files = ds.getIncludedFiles();
               String[] dirs = ds.getIncludedDirectories();
               removeFiles(dir, files, dirs);
           } catch (BuildException be) {
               // directory doesn't exist or is not readable
               if (m_failonerror) {
                   throw be;
               } else {
                   log(be.getMessage(), 
                       m_quiet ? Project.MSG_VERBOSE : Project.MSG_WARN);
               }
           }
       }
   }

   // ************************************************************************
   //   protected and private methods
   // ************************************************************************
 
   
   protected void removeDir(File d) {
       String[] list = d.list();
       if (list == null) {
           list = new String[0];
       }
       for (int i = 0; i < list.length; i++) {
           String s = list[i];
           File f = new File(d, s);
           if (f.isDirectory()) {
               removeDir(f);
           } else {
               log("Deleting " + f.getAbsolutePath(), m_verbosity);
               if (!f.delete()) {
                   String message = "Unable to delete file " 
                       + f.getAbsolutePath();
                   if (m_failonerror) {
                       throw new BuildException(message);
                   } else {
                       log(message,
                           m_quiet ? Project.MSG_VERBOSE : Project.MSG_WARN);
                   }
               }
           }
       }
       log("Deleting directory " + d.getAbsolutePath(), m_verbosity);
       if (!d.delete()) {
           String message = "Unable to delete directory " 
               + d.getAbsolutePath();
           if (m_failonerror) {
               throw new BuildException(message);
           } else {
               log(message,
                   m_quiet ? Project.MSG_VERBOSE : Project.MSG_WARN);
           }
       }
   }

   
   
   /**
    * Will change the file to be deleted to the specified mapped
    * file. This only works if "dir" & "mappedDir" have been
    * specified.
    * @param fileToMutate the file to mutate, cannot be <code>null</code>.
    * @return the mutated file, may be <code>null</code> if
    * mappingDir and dir exist but the new mapped dir could not be found.
    */
   private File mutateFile(File fileToMutate)
   {
      if (fileToMutate == null)
      {
         throw new BuildException(
            "Illegal Argument: File cannot be null in mutate file method.");
      }

      if (dir != null && m_mappedDir != null &&
         m_mappedDir.trim().length() > 0)
      {
         String f = fileToMutate.getAbsolutePath();

         if (f.startsWith(dir.getAbsolutePath()))
         {
            
            fileToMutate =
               new File(
                  m_mappedDir + f.substring(dir.getAbsolutePath().length()));
         }
         else
         {
            
            return null;
         }
      }

      return fileToMutate;
   }
   
   private List<File> getAllValidPackageDirs(File srcDir)
   {
      List<File> dirs = new ArrayList<File>();
      if(srcDir.isDirectory())
      {
         File[] children = srcDir.listFiles();
         for(int i = 0; i < children.length; i++)
         {
            if(children[i].isDirectory())
            dirs.addAll(getAllValidPackageDirs(children[i]));
         }
         if(containsFiles(srcDir))
            dirs.add(mutateFile(srcDir));
         
      }
      return dirs;
   }
   
   /**
    * Validates whether or not this directory contains
    * files. 
    * @param dirToCheck the directory to check, cannot be <code>null</code>.
    * @return <code>true</code> if this directory contains files,
    * else <code>false</code>.
    */
   private boolean containsFiles(File dirToCheck)
   {
      if(dirToCheck == null)
         throw new IllegalArgumentException("Directory cannot be null.");
      if(dirToCheck.isDirectory())
      {
        File[] files = dirToCheck.listFiles();
        for(int i = 0; i < files.length; i++)
        {
           if(files[i].isFile())
              return true;
        }
      }
      return false;
   }

   private String m_mappedDir;
   private boolean m_failonerror = true;
   private boolean m_quiet = false;
   private int m_verbosity = Project.MSG_VERBOSE;
}
