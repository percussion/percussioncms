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

package com.percussion.ant.install;

import com.percussion.install.PSLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * PSUpdateWarFiles updates the War files by adding jar files to the
 * "WEB-INF/lib" directory during install.
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="updateWarFiles"
 *              class="com.percussion.ant.install.PSUpdateWarFiles"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to update the war file.
 *
 *  <code>
 *  &lt;updateWarFiles
 *   jarFiles="rxconfig/Installer/temp/soap/WEB-INF/lib/rxdbpublisher.jar,
 *             rxconfig/Installer/temp/soap/WEB-INF/lib/rxportal.jar"
 *   jarFilesPath="soap/WEB-INF/lib/" removeFiles="true"
 *   warFile="InstallableApps/RemotePublisher/Rhythmyx.war"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSUpdateWarFiles extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      try
      {
         m_warFilePath = getRootDir() + File.separator + m_warFilePath;
         File f = new File(m_warFilePath);
         if (!(f.exists() && f.isFile()))
         {
            PSLogger.logInfo(
                  "Warning - war file does not exist : " +  m_warFilePath);
            return;
         }
         if ((m_jarFiles == null) || (m_jarFiles.length == 0))
         {
            PSLogger.logInfo(
                  "Note - No jar file to be added to the war file : "
                  + m_warFilePath);
            return;
         }
         for (int i=0; i < m_jarFiles.length; i++)
         {
            if ((m_jarFiles[i] != null) && (m_jarFiles[i].trim().length() > 0))
            {
               String jarFilePath = getRootDir() + File.separator
               + m_jarFiles[i];
               m_jarFiles[i] = jarFilePath;
            }
         }

         updateWarFile();
      }
      catch (Exception e)
      {
         PSLogger.logInfo("ERROR : " + e.getMessage());
         PSLogger.logInfo(e);
      }
   }

   /***************************************************************************
    * private functions
    ***************************************************************************/

   /**
    * Updates the war file <code>warFile</code> by adding the jar files listed
    * in <code>jarFiles</code> to the  WEB-INF/lib directory of the war file.
    */

   public void updateWarFile()
   {
      File jarFile = new File(m_warFilePath);
      File tempJarFile = new File(m_warFilePath + ".tmp");
      JarFile srcJarFile = null;

      try
      {
         Set setJarFiles = new HashSet();
         for (int k=0; k < m_jarFiles.length; k++)
         {
            String jarFileName = jarFilesPath
            + new File(m_jarFiles[k]).getName();

            setJarFiles.add(jarFileName);
         }

         srcJarFile = new JarFile(jarFile);
         JarOutputStream tempJar = new JarOutputStream(
               new FileOutputStream(tempJarFile));

         byte[] buffer = new byte[1024];
         int bytesRead;

         try
         {
            String resolvedFile;

            if (!m_isRemoveFiles)
            {
               //add files
               for (int k=0;k < m_jarFiles.length;k++)
               {
                  // Open the given file.
                  resolvedFile = m_jarFiles[k];

                  FileInputStream file = new FileInputStream(resolvedFile);

                  try
                  {
                     // Create a jar entry and add it to the temp jar.
                     JarEntry entry = new JarEntry(jarFilesPath
                           + new File(resolvedFile).getName());

                     tempJar.putNextEntry(entry);

                     // Read the file and write it to the jar.
                     while ((bytesRead = file.read(buffer)) != -1)
                     {
                        tempJar.write(buffer, 0, bytesRead);
                     }

                     PSLogger.logInfo(entry.getName() + " added.");
                  }
                  catch (IOException ioe)
                  {
                     PSLogger.logInfo("ERROR : " + ioe.getMessage());
                     PSLogger.logInfo(ioe);
                  }
                  finally
                  {
                     if (file != null)
                     {
                        try
                        {
                           file.close();
                        }
                        catch (Exception e)
                        {
                           // no-op
                        }
                     }
                  }
               }
            }

            // Loop through the jar entries and add/remove them to/from the temp jar,
            // skipping the entry that was added/(to be removed) to/from the temp jar already.
            for (Enumeration srcJarEntries = srcJarFile.entries(); srcJarEntries.hasMoreElements(); )
            {
               // Get the next entry.
               JarEntry srcJarEntry = (JarEntry) srcJarEntries.nextElement();

               String entryName = srcJarEntry.getName();

               //copy src jar entries from the source to the dest except for those that were already added

               if (setJarFiles.contains(entryName)) //is it on the list we were given?
               {
                  String msg = m_isRemoveFiles ? " removed from archive : " :
                     " skiped - already added to archive : ";

                  PSLogger.logInfo("File : " + entryName + msg
                        + new File(m_warFilePath).getAbsolutePath());

                  continue; //skip this src. entry it was either copied already or we want to remove it
               }
               else
               {
                  // If the entry has not been added already or if removing not on the remove list, then copy it.
                  copyJarEntry(srcJarFile, tempJar, srcJarEntry);
               }
            }
         }
         catch (Exception ex)
         {
            PSLogger.logInfo(ex.getLocalizedMessage());
            PSLogger.logInfo(ex);
            tempJar.putNextEntry(new JarEntry("stub"));
         }
         finally
         {
            if (tempJar != null)
            {
               try
               {
                  tempJar.close();
               }
               catch (Exception e)
               {
                  // no-op
               }
            }
         }
      }
      catch (Exception ex)
      {
         PSLogger.logInfo(ex.getLocalizedMessage());
         PSLogger.logInfo(ex);
      }
      finally
      {
         if (srcJarFile != null)
         {
            try
            {
               srcJarFile.close();
            }
            catch (IOException e)
            {
               // no-op
            }
         }
      }
      if (!jarFile.delete())
         PSLogger.logInfo("Failed to delete : " + jarFile.getAbsolutePath());
      if (!tempJarFile.renameTo(jarFile))
         PSLogger.logInfo("Failed to rename : " + tempJarFile.getAbsolutePath()
               + " to : " + jarFile.getAbsolutePath());
   }

   /**
    * Copies jar entry from one jar to another.
    * @param srcJarFile src. jar file to copy entries from.
    * @param destJar dest. jar output stream to copy entry into.
    * @param srcJarEntry jarentry to copy to the dest jar.
    * @throws IOException
    */
   private void copyJarEntry(
         JarFile srcJarFile,
         JarOutputStream destJar,
         JarEntry srcJarEntry)
   throws IOException
   {
      byte[] buffer = new byte[1024];

      int bytesRead;

      // Get an input stream for the entry.
      InputStream entryStream = srcJarFile.getInputStream(srcJarEntry);

      // Read the entry and write it to the dest jar.
      destJar.putNextEntry(srcJarEntry);

      while ((bytesRead = entryStream.read(buffer)) != -1)
      {
         destJar.write(buffer, 0, bytesRead);
      }
   }

   /***************************************************************************
    * Bean properties
    ***************************************************************************/

   /**
    * Returns the absolute path of the war file.
    * @return the absolute path of the war file,
    * never <code>null</code> or empty
    */
   public String getWarFile()
   {
      return m_warFilePath;
   }

   /**
    * Sets the absolute path of the war file.
    * @param warFile the absolute path of the war file,
    * never <code>null</code> or empty
    * @throws IllegalArgumentException if warFile is <code>null</code>
    * or empty
    */
   public void setWarFile(String warFile)
   {
      if ((warFile == null) || (warFile.trim().length() < 1))
         throw new IllegalArgumentException(
         "warFile may not be null or empty");
      this.m_warFilePath = warFile;
   }

   /**
    * Returns the path of the jar files which should be prepended to the jar file
    * name, when adding the jar file to the new war file.
    * @return the path of the jar files which should be prepended to the jar file
    * name, when adding the jar file to the new war file,
    * never <code>null</code>, may be empty
    */
   public String getJarFilesPath()
   {
      return jarFilesPath;
   }

   /**
    * Sets the path of the jar files which should be prepended to the jar file
    * name, when adding the jar file to the new war file.
    * @param jarFilesPath the path of the jar files which should be prepended
    * to the jar file name, when adding the jar file to the new war file,
    * may be <code>null</code> or empty
    */
   public void setJarFilesPath(String jarFilesPath)
   {
      if (jarFilesPath == null)
         jarFilesPath = "";
      this.jarFilesPath = jarFilesPath;
   }

   /**
    * Returns the list of jar files to be added to the war file.
    * @return list of war to be added to the war file, may be <code>null</code>
    * or empty array.
    */
   public String[] getJarFiles()
   {
      return m_jarFiles;
   }

   /**
    * Sets the list of jar files to be added to the war file.
    * @param jarFiles list of jar files to be added to the war file,
    * may be <code>null</code> or empty array.
    */
   public void setJarFiles(String[] jarFiles)
   {
      this.m_jarFiles = jarFiles;
   }

   /**
    * Set isRemoveFiles.
    * @param b
    */
   public void setRemoveFiles(boolean b)
   {
      m_isRemoveFiles = b;
   }

   /**
    * Is RemoveFiles?.
    * @return
    */
   public boolean isRemoveFiles()
   {
      return m_isRemoveFiles;
   }


   /**************************************************************************
    * properties
    **************************************************************************/

   /**
    * Path of the war file to update, never <code>null</code> or empty.
    * The path contains the path of the war file relative to the Rhythmyx root.
    * The war file should already be installed on the destination system.
    */
   private String m_warFilePath =
      "InstallableApps/RemotePublisher/Rhythmyx.war";

   /**
    * The path of the jar files which should be prepended to the jar file name,
    * when adding the jar file to the new war file.
    * For example, for jar files in the soap.jar file, this will be equal
    * to "soap/WEB-INF/lib". Never <code>null</code>, may be empty.
    */
   private String jarFilesPath = "soap/WEB-INF/lib/";

   /**
    * The list of jar files to be added to the WEB-INF/lib directory of the war
    * file, may be <code>null</code> or empty array. The files in this list
    * should already be installed on the destination system. The path is
    * relative to the Rhythmyx root.
    */
   private String[] m_jarFiles = new String[]
                                            {
         "rxconfig/Installer/temp/soap/WEB-INF/lib/rxdbpublisher.jar",
         "rxconfig/Installer/temp/soap/WEB-INF/lib/rxportal.jar"
                                            };

   /**
    * Flag that indicates that we want to remove a file.
    */
   private boolean m_isRemoveFiles = false;

   /**************************************************************************
    * main
    **************************************************************************/

   public static void main(String[] args)
   {
      String warFile =
         "C:/Rhythmyx55_0219/InstallableApps/RemotePublisher/soap.war";

      //remove file
      String[] jarFiles = new String[]
                                     {
            "rxbea.jar", //file doesn't have to exist to be able to remove it
                                     };

      PSUpdateWarFiles warFileUpdate = new PSUpdateWarFiles();
      warFileUpdate.setRemoveFiles(true);
      warFileUpdate.setWarFile(warFile);
      warFileUpdate.setJarFiles(jarFiles);
      warFileUpdate.setJarFilesPath("WEB-INF/lib/");
      warFileUpdate.updateWarFile();

      //add file
      //file obviously must exist in this case
      jarFiles = new String[]
                            {
            "C:/Rhythmyx55_0219/InstallableApps/RemotePublisher/rxbea.jar",
                            };

      PSUpdateWarFiles warFileUpdate2 = new PSUpdateWarFiles();
      warFileUpdate2.setRemoveFiles(false);
      warFileUpdate2.setWarFile(warFile);
      warFileUpdate2.setJarFiles(jarFiles);
      warFileUpdate2.setJarFilesPath("WEB-INF/lib/");
      warFileUpdate2.updateWarFile();
   }
}




