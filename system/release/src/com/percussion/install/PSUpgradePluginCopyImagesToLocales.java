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
package com.percussion.install;

import com.percussion.util.IOTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;


/**
 * This class copies files under rx_resources/images/en-us to each additional
 * locale subfolder under the images directory provided the files do not exist
 * in the additional locale directory and are not hidden. 
 */
public class PSUpgradePluginCopyImagesToLocales implements IPSUpgradePlugin
{
   /**
    * Implements the process function of IPSUpgardePlugin. 
    * Performs file/directory copy from en-us to all other locales. 
    * 
    * @param config PSUpgradeModule object.
    * @param elemData We do not use this element in this function.
    * @return <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      m_config = config;
      log("Performing locale image copy");
     
      List localeDirs = new ArrayList();
      File en_usImagesDir = new File(RxUpgrade.getRxRoot()
            + ENGLISH_LOCALE_IMAGES);
      File imagesDir = en_usImagesDir.getParentFile();
      File[] imagesFiles = imagesDir.listFiles();
      for (int i = 0; i < imagesFiles.length; i++)
      {
         File f = imagesFiles[i];
         if (f.isDirectory() && !f.getName().equals("en-us"))
         {
            log("Additional image directory found for locale " + f.getName());
            localeDirs.add(f);
         }
      }
      
      if (localeDirs.size() == 0)
      {
         log("No additional locales were found, image copy not required");
      }
      else
      {
         log("Copying images from locale en-us to additional locale image "
               + "directories");
         
         for (int i = 0; i < localeDirs.size(); i++)
         {
            File localeDir = (File) localeDirs.get(i);
            copyImageFiles(en_usImagesDir, localeDir);
         }
         
         log("Locale image copy complete");
      }
      
      return null;
   }

   /**
    * Copies files and directories from the source directory to the destination
    * directory.  Files which exist in the destination directory and/or are
    * hidden will be excluded.
    * 
    * @param srcDir the source directory, assumed not <code>null</code>.
    * @param destDir the destination directory, assumed not <code>null</code>.
    */
   private void copyImageFiles(File srcDir, File destDir)
   {
      File[] srcFiles = srcDir.listFiles();
      for (int i = 0; i < srcFiles.length; i++)
      {
         File srcFile = srcFiles[i];
         String srcFileName = srcFile.getName();
         File destFile = new File(destDir, srcFileName);
         
         if (srcFile.isHidden())
            continue;
         
         if (srcFile.isDirectory())
         {
            String destFileDirPath = destFile.getAbsolutePath();
            try
            {
               if (!destFile.exists())
               {
                  if (destFile.mkdir())
                     log("Created directory " + destFileDirPath);
               }
               
               copyImageFiles(srcFile, destFile);
            }
            catch (SecurityException se)
            {
               log("Could not create directory " + destFileDirPath +
                     " due to the following error : " + se.getMessage());
               se.printStackTrace(m_config.getLogStream());
            }
         }
         else
         {
            String srcFilePath = srcFile.getAbsolutePath();
            String destDirPath = destDir.getAbsolutePath();
            
            if (!destFile.exists())
            {
               try
               {
                  IOTools.copyFile(srcFile, destFile);
                  log("Copied " + srcFilePath + " to " + destDirPath);
               }
               catch (IOException ioe)
               {
                  log("Could not copy " + srcFilePath + " to " +
                        destDirPath + " due to the following error : " +
                        ioe.getMessage());
                  ioe.printStackTrace(m_config.getLogStream());
               }
            }
         }
      }
   }
   
   /**
    * Prints message to the log printstream if it exists or just sends it to
    * System.out
    * 
    * @param msg the message to be logged, can be <code>null</code>.
    */
   private void log(String msg)
   {
      if (msg == null)
      {
         return;
      }

      if (m_config != null)
      {
         m_config.getLogStream().println(msg);
      }
      else
      {
         System.out.println(msg);
      }
   }
   
   /**
    * The config module, initialized in
    * {@link #process(IPSUpgradeModule, Element)}.
    */
   private IPSUpgradeModule m_config;
   
   /**
    * Constant for the en-us locale images subfolder relative to the Rhythmyx
    * root.
    */
   private static String ENGLISH_LOCALE_IMAGES = "rx_resources/images/en-us";
}
