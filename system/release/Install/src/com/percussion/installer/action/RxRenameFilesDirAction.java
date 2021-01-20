/******************************************************************************
 *
 * [ RxRenameFilesDirAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.InstallUtil;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.util.IOTools;

import java.io.File;


/**
 * This action renames a file or directory. It writes a message in the log file
 * stating if it was successful or failed in renaming the file/directory.
 */
public class RxRenameFilesDirAction extends RxIAAction
{
   @Override   
   public void execute()
   {
      setSrcFile(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), SRC_FILE_VAR)));
      setDestFile(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), DEST_FILE_VAR)));
      setDelDestFileIfExists(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), DEL_DEST_FILE_VAR)).equalsIgnoreCase("true"));
      
      try
      {
         File fSrc = new File(m_srcFile);
         if (!fSrc.exists())
         {
            RxLogger.logInfo("Source file does not exist : " + m_srcFile);
            return;
         }
         
         File fDest = new File(m_destFile);
         if (fDest.exists())
         {
            if (!m_delDestFileIfExists)
            {
               RxLogger.logInfo("Destination file already exists : " +
                     m_destFile);
               return;
            }
            
            // delete the destination file/directory
            RxLogger.logInfo("Deleting destination file : " + m_destFile);
            IOTools.deleteFile(fDest);
            
            if (fDest.exists())
            {
               RxLogger.logError("Failed to delete destination file : " +
                     m_destFile);
               return;
            }
         }
         
         if (fSrc.renameTo(fDest))
         {
            RxLogger.logInfo("Renamed source file : " +
                  fSrc.getAbsolutePath() + " to : " +
                  fDest.getAbsolutePath());
         }
         else
         {
            RxLogger.logError("Failed to rename source file : " +
                  fSrc.getAbsolutePath() + " to : " +
                  fDest.getAbsolutePath());
         }
      }
      catch (Exception e)
      {
         RxLogger.logError("ERROR : " + e.getMessage());
         RxLogger.logError(e);
      }
   }
   
   /***************************************************************************
    * private functions
    ***************************************************************************/
   
   /***************************************************************************
    * Bean properties
    ***************************************************************************/
   
   /**
    * Returns the path of the source file/directory to rename.
    * @return the path of the source file/directory to rename,
    * never <code>null</code> or empty.
    */
   public String getSrcFile()
   {
      return m_srcFile;
   }
   
   /**
    * Sets the path of the source file/directory to rename.
    * @param srcFile the path of the source file/directory to rename,
    * never <code>null</code> or empty.
    * @throws IllegalArgumentException if srcFile is <code>null</code>
    * or empty.
    */
   public void setSrcFile(String srcFile)
   {
      if ((srcFile == null) || (srcFile.trim().length() < 1))
         throw new IllegalArgumentException(
         "srcFile may not be null or empty");
      m_srcFile = srcFile;
   }
   
   /**
    * Returns the path of the destination file/directory.
    * @return the path of the destination file/directory,
    * never <code>null</code> or empty.
    */
   public String getDestFile()
   {
      return m_destFile;
   }
   
   /**
    * Sets the path of the destination file/directory.
    * @param destFile the path of the destination file/directory,
    * never <code>null</code> or empty.
    * @throws IllegalArgumentException if destFile is <code>null</code>
    * or empty.
    */
   public void setDestFile(String destFile)
   {
      if ((destFile == null) || (destFile.trim().length() < 1))
         throw new IllegalArgumentException(
         "destFile may not be null or empty");
      m_destFile = destFile;
   }
   
   /**
    * Returns a boolean indicating if the destination file/directory should
    * be deleted if it already exists.
    * @return <code>true</code> if the destination file/directory should
    * be deleted if it already exists. If <code>false</code>
    * and destination file already exists then renaming will fail.
    */
   public boolean getDelDestFileIfExists()
   {
      return m_delDestFileIfExists;
   }
   
   /**
    * Sets a boolean indicating if the destination file/directory should
    * be deleted if it already exists.
    * @param delDestFileIfExists <code>true</code> if the destination
    * file/directory should be deleted if it already exists. If 
    * <code>false</code> and destination file already exists then renaming will
    * fail.
    */
   public void setDelDestFileIfExists(boolean delDestFileIfExists)
   {
      m_delDestFileIfExists = delDestFileIfExists;
   }
   
   
   /**************************************************************************
    * properties
    **************************************************************************/
   
   /**
    * Path of the source file/directory to rename, never <code>null</code>.
    */
   private String m_srcFile = "";
   
   /**
    * Path of the destination file/directory, never <code>null</code>.
    */
   private String m_destFile = "";
   
   /**
    * If <code>true</code> then deletes the destination file/directory if
    * it exists before renaming the source file/directory. If <code>false</code>
    * and destination file already exists then renaming will fail.
    */
   private boolean m_delDestFileIfExists = false;
   
   /**
    * The variable name for the source file parameter passed in via the IDE.
    */
   private static final String SRC_FILE_VAR = "srcFile";
   
   /**
    * The variable name for the destination file parameter passed in via the
    * IDE.
    */
   private static final String DEST_FILE_VAR = "destFile";
   
   /**
    * The variable name for the delete destination file/directory parameter
    * passed in via the IDE.
    */
   private static final String DEL_DEST_FILE_VAR = "delDestFileIfExists";
}








