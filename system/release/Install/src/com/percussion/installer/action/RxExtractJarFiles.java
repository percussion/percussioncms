/******************************************************************************
 *
 * [ RxExtractJarFiles.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.InstallUtil;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installanywhere.RxIAUtils;
import com.zerog.ia.api.pub.IASys;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * This action extracts a jar file into a given location.
 */
public class RxExtractJarFiles extends RxIAAction
{
   @Override
   public void execute()
   {
      try
      {
         setJarFile(getInstallValue(InstallUtil.getVariableName(
               getClass().getName(), JAR_FILE_VAR)));
         File f = new File(m_jarFile);
         
         if (!(f.exists() && f.isFile()))
         {
            RxLogger.logWarn("jar file does not exist : " +  m_jarFile);
            return;
         }
         
         setDestinationDir(getInstallValue(InstallUtil.getVariableName(
               getClass().getName(), DEST_DIR_VAR)));
         
         if ((m_destinationDir == null) || 
               (m_destinationDir.trim().length() < 1))
         {
            m_destinationDir = f.getParentFile().getAbsolutePath();
         }
         
         File fDest = new File(m_destinationDir);
         if (!(fDest.exists() && fDest.isDirectory()))
            fDest.mkdirs();
         
         String output = "Expanding: " + f.getName() + " to: " +
            m_destinationDir;
         RxLogger.logInfo(output);
         IASys.out.println("");
         IASys.out.print(output);
         setProgressStatusText(output);
         
         List<String> fileList = new ArrayList<>();
         JarFile jar = new JarFile(m_jarFile);
         
         for (Enumeration entries = jar.entries(); entries.hasMoreElements(); )
         {
            // Get the next entry.
            JarEntry entry = (JarEntry) entries.nextElement();
            String entryName = entry.getName();
            fileList.add(entryName);
         }
         
         byte[] buffer = new byte[1024];
         int bytesRead;
         int numFiles = fileList.size();
         int modifier = numFiles/10;
                 
         for (int k=0; k < numFiles; k++)
         {
            if (k != 0 && k % modifier == 0)
               IASys.out.print('.');
            
            String jarFileLoc = m_destinationDir + File.separator +
               fileList.get(k);
            File makeLocation = new File(jarFileLoc).getParentFile();
            makeLocation.mkdirs();
            JarFile jf = null;
            jf = new JarFile(m_jarFile);
            File fJarFile = new File(jarFileLoc);
            
            if (jarFileLoc.endsWith(File.separator) ||
                  jarFileLoc.endsWith("/"))
            {
               fJarFile.mkdir();
               continue;
            }
            
            if (fJarFile.exists() && fJarFile.isFile())
            {
               // delete the file if it exists, else the write operation may
               // fail if the file is readonly.
               fJarFile.delete();
            }
            
            fJarFile.createNewFile();
            
            FileOutputStream fos = null;
            InputStream is = null;
            String text = "";
            try
            {
               String entry = fileList.get(k);
               is = jf.getInputStream(jf.getEntry(entry));
               fos = new FileOutputStream(fJarFile);
               
               text = "expanding " + entry + " to " +
                     fJarFile.getAbsolutePath();
               setProgressStatusText(RxIAUtils.truncateMsg(text));
                                             
               while ((bytesRead = is.read(buffer)) != -1)
               {
                  fos.write(buffer, 0, bytesRead);
               }
            }
            catch (Exception e)
            {
               RxLogger.logError("RxExtractJarFiles " + e.getMessage());
               RxLogger.logError(e);
            }
            finally
            {
               if (fos != null)
               {
                  try
                  {
                     fos.close();
                  }
                  catch (Exception e)
                  {
                     // no-op
                  }
               }
               if (is != null)
               {
                  try
                  {
                     is.close();
                  }
                  catch (Exception e)
                  {
                     // no-op
                  }
               }
            }
         }
         
         IASys.out.println("Done");
      }
      catch (Exception e)
      {
         RxLogger.logError(e.getMessage());
         RxLogger.logError(e);
      }
   }
   
   @Override
   public long getEstTimeToInstall()
   {
      return 600;
   }
   
   /***************************************************************************
    * Bean properties
    ***************************************************************************/
   
   /**
    * Returns the absolute path of the jar file to extract.
    * @return the absolute path of the jar file to extract,
    * never <code>null</code> or empty
    */
   public String getJarFile()
   {
      return m_jarFile;
   }
   
   /**
    * Sets the absolute path of the jar file to extract.
    * @param jarFile the absolute path of the jar file to extract,
    * never <code>null</code> or empty
    * @throws IllegalArgumentException if jarFile is <code>null</code>
    * or empty
    */
   public void setJarFile(String jarFile)
   {
      if ((jarFile == null) || (jarFile.trim().length() < 1))
         throw new IllegalArgumentException(
         "jarFile may not be null or empty");
      m_jarFile = jarFile;
   }
   
   /**
    * Returns the absolute path of the directory in which to extract files.
    * @return the absolute path of the directory in which to extract files,
    * never <code>null</code> or empty
    */
   public String getDestinationDir()
   {
      return m_destinationDir;
   }
   
   /**
    * Sets the absolute path of the directory in which to extract files.
    * @param destinationDir the absolute path of the directory in which to
    * extract files, never <code>null</code> or empty
    */
   public void setDestinationDir(String destinationDir)
   {
      m_destinationDir = destinationDir;
   }
   
   /**************************************************************************
    * properties
    **************************************************************************/
   
   /**
    * Path of the jar file to extract, never <code>null</code> or empty.
    * The path can contain $ string alias, since the path is resolved during
    * install.
    */
   private String m_jarFile = 
      "$INSTALLER_LAUNCH_DIR$/../../resource/server.zip";
   
   /**
    * The directory in which to extract files, may be <code>null</code> or
    * empty, in which case the jar file is extracted in the directory where
    * it exists. The path can contain $ string alias, since the path is
    * resolved during install.
    */
   private String m_destinationDir = "";
   
   /**
    * The name of the jar file parameter passed in via the IDE.
    */
   private static String JAR_FILE_VAR = "jarFile";
   
   /**
    * The name of the destination directory parameter passed in via the IDE.
    */
   private static String DEST_DIR_VAR = "destinationDir";
}





