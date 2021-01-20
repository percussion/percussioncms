/******************************************************************************
 *
 * [ RxDelFileFolders.java ]
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

import java.io.File;


/**
 * This action is used to delete files and folders.
 */
public class RxDelFileFolders extends RxIAAction
{
   @Override   
   public void execute()
   {
      setFiles(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), FILES_VAR)));
      setDirectories(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), DIRS_VAR)));
      setIfEmpty(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), IF_EMPTY_VAR)).equalsIgnoreCase("true"));
      setIncludeSubDirs(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), INCLUDE_SUBDIRS_VAR)).equalsIgnoreCase(
            "true"));
      
      // delete the files
      for (int i = 0; i < m_files.length; i++)
      {
         String filePath = "";
         try
         {
            filePath = m_files[i];
            File file = new File(filePath);
            if (file.isFile())
            {
               RxLogger.logInfo("deleting file : " + filePath);
               file.delete();
            }
         }
         catch (Exception e)
         {
            RxLogger.logInfo("Failed to delete file : " + filePath);
            RxLogger.logInfo("Exception : " + e.getMessage());
         }
      }
      
      if (getFileService() == null)
      {
         RxLogger.logError("RxDelFileFolders#execute : Failed to get file " +
         "service");
         return;
      }
      
      // delete the folders
      for (int i = 0; i < m_directories.length; i++)
      {
         String dirPath = "";
         try
         {
            dirPath = m_directories[i];
            File file = new File(dirPath);
            if (file.isDirectory())
            {
               RxLogger.logInfo("Deleting folder : " + dirPath);
               int result = getFileService().deleteDirectory(
                     dirPath, m_ifEmpty, m_includeSubDirs);
               if (result == getFileService().SUCCESS)
                  RxLogger.logInfo("Deleted folder : " + dirPath);
               else if (result == getFileService().DEFERRED)
                  RxLogger.logInfo("Deletion deferred : " + dirPath);
            }
         }
         catch (Exception e)
         {
            RxLogger.logError("Failed to delete folder : " + dirPath);
            RxLogger.logError("Exception : " + e.getMessage());
         }
      }
   }
   
   /**************************************************************************
    * Bean property Accessors and Mutators
    **************************************************************************/
   
   /**
    *  Returns the files to be deleted.
    *
    *  @return <code>String</code> array containing the absolute path of files
    *  to be deleted, never <code>null</code>, may be an empty array.
    */
   public String[] getFiles()
   {
      return m_files;
   }
   
   /**
    *  Sets the files to be deleted.
    *
    *  @param files <code>String</code> containing comma-separated absolute
    *  paths of the files to be deleted, may be <code>null</code> or empty.
    */
   public void setFiles(String files)
   {
      m_files = RxIAUtils.toArray(files);
   }
   
   /**
    *  Returns the directories to be deleted.
    *
    *  @return <code>String</code> containing the absolute path of directories
    *  to be deleted, never <code>null</code>, may be an empty array.
    */
   public String[] getDirectories()
   {
      return m_directories;
   }
   
   /**
    *  Sets the directories to be deleted.
    *
    *  @param directories <code>String</code> containing absolute paths of the
    *  directories to be deleted, may be <code>null</code> or empty.
    */
   public void setDirectories(String directories)
   {
      m_directories = RxIAUtils.toArray(directories);
   }
   
   /**
    *  Returns the flag indicating if only empty directories should be deleted.
    *
    *  @return the flag indicating if only empty directories should be deleted.
    */
   public boolean getIfEmpty()
   {
      return m_ifEmpty;
   }
   
   /**
    *  Sets the flag indicating if only empty directories should be deleted.
    *
    *  @param ifEmpty the flag indicating if only empty directories should be
    *  deleted.
    */
   public void setIfEmpty(boolean ifEmpty)
   {
      m_ifEmpty = ifEmpty;
   }
   
   /**
    *  Returns the flag indicating whether or not subdirectories should also be
    *  deleted.
    *
    *  @return the flag indicating whether or not subdirectories should also be
    *  deleted.
    */
   public boolean getIncludeSubDirs()
   {
      return m_includeSubDirs;
   }
   
   /**
    *  Sets the flag indicating whether or not subdirectories should also be
    *  deleted.
    *
    *  @param includeSubDirs the flag indicating whether or not subdirectories
    *  should also be deleted.
    */
   public void setIncludeSubDirs(boolean includeSubDirs)
   {
      m_includeSubDirs = includeSubDirs;
   }
   
   /**************************************************************************
    * private function
    **************************************************************************/
   
   /**************************************************************************
    * Bean properties
    **************************************************************************/
   
   /**
    * Files to be deleted, never <code>null</code>, may be an empty array.
    */
   private String[] m_files = new String[0];
   
   /**
    * Directories to be deleted, never <code>null</code>, may be an empty array.
    */
   private String[] m_directories = new String[0];
   
   /**
    * If <code>true</code>, only empty directories are deleted.
    */
   private boolean m_ifEmpty = false;
   
   /**
    * A flag indicating whether or not subdirectories should also be deleted. If
    * the value is <code>true</code>, the {@link #m_ifEmpty} flag applies to
    * each directory that was considered for deletion.
    */
   private boolean m_includeSubDirs = true;
   
   /**
    * The variable name for the files parameter passed in via the IDE.
    */
   private static final String FILES_VAR = "files";
   
   /**
    * The variable name for the directories parameter passed in via the
    * IDE.
    */
   private static final String DIRS_VAR = "directories";
   
   /**
    * The variable name for the delete empty directories parameter passed in
    * via the IDE.
    */
   private static final String IF_EMPTY_VAR = "ifEmpty";
   
   /**
    * The variable name for the include subdirectories parameter passed in via
    * the IDE.
    */
   private static final String INCLUDE_SUBDIRS_VAR = "includeSubDirs";
}






