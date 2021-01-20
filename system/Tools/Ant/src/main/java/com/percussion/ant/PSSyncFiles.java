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
package com.percussion.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


/**
 *
 */
public class PSSyncFiles extends Task
{
   public void setFromdir(File fromDir)
   {
      m_fromDir = fromDir;
   }

   public void setTodir(File toDir)
   {
      m_toDir = toDir;
   }

   @Override
   public void execute()
      throws BuildException
   {
      if ((m_fromDir == null) || !m_fromDir.exists())
      {
         throw new BuildException(
            "fromdir must be set to an existing directory");
      }

      if (m_toDir == null)
      {
         throw new BuildException("todir must be set");
      }
      
      if(!m_toDir.exists())
      {
         log("creating destination directory: " + m_toDir.getAbsolutePath());
         m_toDir.mkdirs();
      }

      log("Scanning directories...please wait");
      setDeletes(m_toDir);
      setCopies(m_fromDir);

      
      // process deletes
      Iterator it = null;
      File current = null;
      log("Removing " + m_fileDeletes.size() + " files from destination");
      it = m_fileDeletes.iterator();
      while(it.hasNext())
      {
         current = (File)it.next();
         deleteFile(current);
      }
      log("Removing " + m_dirDeletes.size() + " directories from destination");
      it = m_dirDeletes.iterator();
      while(it.hasNext())
      {
         current = (File)it.next();
         deleteFile(current);
      }
            
      // process copies
      Set keys = m_dirCopies.keySet();
      log("Copying " + m_dirCopies.size() + " directories to" + " destination");
      it = keys.iterator();
      while(it.hasNext())
      {
         current = (File)it.next();
         m_dirCopies.get(current).mkdirs();         
      }
      keys = m_fileCopies.keySet();
      log("Copying " + m_fileCopies.size() + " files to destination");
      it = keys.iterator();
      try
      {
         while(it.hasNext())
         {
            current = (File)it.next();
            
            try
            {
               copyFile(current, m_fileCopies.get(current));
            }
            catch(FileNotFoundException fne)
            {
               // The file may have the read-only flag set so
               // try deleting the file before copying, if this
               // doesn't work we give up.
               deleteFile(m_fileCopies.get(current));
               copyFile(current, m_fileCopies.get(current));         
            }
         }
      }     
      catch (IOException e)
      {
         throw new BuildException(e);
      }
   }

   /**
    * Sets the lists of files and directories that are eligible for
    * copying.
    * @param dir
    */
   private void setCopies(File dir)
   {
      File[] files = dir.listFiles();

      for (int i = 0; i < files.length; i++)
      {
         if (files[i].isDirectory())
         {
            if (isCopyEligible(files[i]))
            {
               m_dirCopies.put(files[i], getDestinationFile(files[i]));
            }

            if (files[i].listFiles().length > 0)
            {
               setCopies(files[i]);
            }
         }
         else
         {
            if (isCopyEligible(files[i]))
            {
               m_fileCopies.put(files[i], getDestinationFile(files[i]));
            }
         }
      }
   }

   /**
    * Sets the lists of files and directories that are eligible for
    * deletion.
    * @param dir the top level directory, assumed not <code>null</code>
    */
   private void setDeletes(File dir)
   {
      File[] files = dir.listFiles();

      for (int i = 0; i < files.length; i++)
      {
         if (files[i].isDirectory())
         {
            if (
               (files[i].listFiles().length == 0) || !exists(files[i], DELETES))
            {
               m_dirDeletes.add(files[i]);
            }
            if(files[i].listFiles().length > 0)
            {
               setDeletes(files[i]);
            }
         }
         else
         {
            if (!exists(files[i], DELETES))
            {
               m_fileDeletes.add(files[i]);
            }
         }
      }
   }
   
   /**
    * Deletes a directory or file recursing to remove children
    * if this is a directory.
    * @param file
    */
   private void deleteFile(File file)
   {
      if(!file.exists())
         return;
      if(file.isDirectory())
      {         
         File[] children = file.listFiles();
         for(int i = 0; i < children.length; i++)
         {
            deleteFile(children[i]);      
         }       
      }
      file.delete();   
   }

   /**
    * Checks for the exsistance of the file in the to or from directory
    * based on the direction specified.
    * @param file the file to check, assumed not <code>null</code>
    * @param direction the direction which to check for existence
    * either DELETES or COPIES
    * @return <code>true</code> if the file exists
    */
   private boolean exists(File file, int direction)
   {
      String temp = null;
      String toDir = m_toDir.getAbsolutePath();
      String fromDir = m_fromDir.getAbsolutePath();

      if (direction == DELETES)
      {
         temp = fromDir + file.getAbsolutePath().substring(toDir.length());
      }
      else if (direction == COPIES)
      {
         temp = toDir + file.getAbsolutePath().substring(fromDir.length());
      }

      File checkFile = new File(temp);

      return checkFile.exists();
   }

   /**
    * Determines whether the specied file is eligible to be copied.
    * A file is considered eligible if it does not exist in the destination
    * or the source files last modified date 
    * @param file the file to check, assumed not <code>null</code>
    * @return <code>true</code> if the file is eligible to be copied
    */
   private boolean isCopyEligible(File file)
   {
      String toDir = m_toDir.getAbsolutePath();
      String fromDir = m_fromDir.getAbsolutePath();
      String temp = toDir + file.getAbsolutePath().substring(fromDir.length());
      File checkFile = new File(temp);
      return !checkFile.exists()
         || (file.lastModified() > checkFile.lastModified());
   }
   
   /**
    * Creates a destination file for the specified "from" file
    * @param file
    */
   private File getDestinationFile(File file)
   {
      String toDir = m_toDir.getAbsolutePath();
      String fromDir = m_fromDir.getAbsolutePath();
      String temp = toDir + file.getAbsolutePath().substring(fromDir.length());
      
      return new File(temp);
   }

   /**
    * Copies a file from one path to another
    * @param sourceFile
    * @param destFile
    * @throws IOException
    */
   private void copyFile(File sourceFile, File destFile)
      throws IOException
   {
      if(sourceFile == null)
         throw new IllegalArgumentException("sourceFile cannot be null");
      if(destFile == null)
         throw new IllegalArgumentException("destFile cannot be null");   
      FileInputStream in = null;
      FileOutputStream out = null;

      try
      {
         in = new FileInputStream(sourceFile);
         out = new FileOutputStream(destFile);

         byte[] buffer = new byte[8 * 1024];
         int count = 0;

         do
         {
            out.write(buffer, 0, count);
            count = in.read(buffer, 0, buffer.length);
         }
         while (count != -1);
      }
      finally
      {
         if (out != null)
         {
            out.close();
         }

         if (in != null)
         {
            in.close();
         }
      }
   }

   private File m_fromDir;
   private File m_toDir;
   private final Map<File, File> m_fileCopies = new HashMap<File, File>();
   private final Map<File, File> m_dirCopies = new HashMap<File, File>();
   private final List<File> m_fileDeletes = new ArrayList<File>();
   private final List<File> m_dirDeletes = new ArrayList<File>();
   private static final int DELETES = 0;
   private static final int COPIES = 1;
}
