/******************************************************************************
 *
 * [ RxFileInstallAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.InstallUtil;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installanywhere.RxIAUtils;
import com.percussion.installer.RxVariables;
import com.percussion.util.IOTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * This action is used to install files.
 */
public class RxFileInstallAction extends RxIAAction
{
   @Override
   public void execute()
   {
      String strRootDir = getInstallValue(RxVariables.INSTALL_DIR);
      setFiles(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), SOURCE_FILES_VAR)));
      setInstallLocation(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), INSTALL_PATH_VAR)));
      
      if (strRootDir != null && m_sourceFiles != null)
      {
         if (m_strInstallPath != null && m_strInstallPath.length() > 0)
            strRootDir += File.separator + m_strInstallPath;
         
         for (int iFile = 0; iFile < m_sourceFiles.length; ++iFile)
            installBundledFile(strRootDir, m_sourceFiles[iFile]);
      }
   }
   
   /**
    * Installs a bundled file into the target dir.
    * @param strTargetInstallDir target directory, assumed not
    * <code>null</code>.
    * @param strFile bundled file, assumed not <code>null</code>.
    */
   private void installBundledFile(String strTargetInstallDir, String strFile)
   {
      try
      {
         //get the bundled resource file
         File sourceFile = getResourceFile(strFile);
         
         if (sourceFile != null)
         {
            String strFullPath = strTargetInstallDir + File.separator +
            sourceFile.getName();
            
            File file = new File(strFullPath);
            if (!file.exists())
            {
               File parentFile = file.getParentFile();
               if (!parentFile.exists())
                  parentFile.mkdirs();
               file.createNewFile();
            }
            
            //if the file is read-only, delete it, so that the
            //subsequent FileOutputStream will work correctly
            if (!file.canWrite())
            {
               file.delete();
            }
            
            //copy from source to destination
            RxLogger.logInfo("Installing file: " + strFullPath);
            IOTools.copyFileStreams(sourceFile, new File(strFullPath));
         }
      }
      catch (FileNotFoundException fnf)
      {
         RxLogger.logError("RxFileInstallAction#execute : " + fnf.getMessage());
         RxLogger.logError(fnf);
      }
      catch (IOException io)
      {
         RxLogger.logError("RxFileInstallAction#execute : " + io.getMessage());
         RxLogger.logError(io);
      }
   }
   
   /***********************************************************************
    * Variables - Bean properties
    ************************************************************************/
   
   /**
    * Accessor Source Files.
    *
    * @return the files to be installed.
    */
   public String[] getFiles()
   {
      return(m_sourceFiles);
   }
   
   /**
    * Mutator for Source Files.
    *
    * @param files the comma-separated list of files to be installed.
    */
   public void setFiles(String files)
   {
      String[] filesArr = RxIAUtils.toArray(files);
      
      for (int i = 0; i < filesArr.length; i++)
          filesArr[i] = RxIAUtils.fixupSourcePath(filesArr[i]);
                  
      m_sourceFiles = filesArr;
   }
   
   /**
    * The Mutator for the install location.
    *
    * @return The relative location to install the files.
    */
   public String getInstallLocation()
   {
      return(m_strInstallPath);
   }
   
   /**
    * The Mutator for the install location.
    *
    * @param path The relative location to install the files.
    */
   public void setInstallLocation(String path)
   {
      m_strInstallPath = path;
   }
   
   /**
    * The files and or directories to be installed.
    */
   private String[] m_sourceFiles = new String[0];
      
   /**
    * The relative path to install the files to.
    */
   private String m_strInstallPath = new String("upgrade");
   
   /**
    * The variable name for the source files parameter passed in via the IDE.
    */
   private static final String SOURCE_FILES_VAR = "sourceFiles";
   
   /**
    * The variable name for the install path parameter passed in via the IDE.
    */
   private static final String INSTALL_PATH_VAR = "installPath";
}
