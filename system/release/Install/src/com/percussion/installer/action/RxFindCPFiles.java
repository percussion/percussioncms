/******************************************************************************
 *
 * [ RxFindCPFiles.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;
import com.percussion.util.IOTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This action searches for .cp files under the Rhythmyx root directory.
 * If any are found, the {@link #ms_bCPFilesFound} flag is set to 
 * <code>true</code>.  A list of these files is maintained and each file is
 * copied to the upgrade/rx_cp_files directory.
 */
public class RxFindCPFiles extends RxIAAction
{
   @Override
   public void execute()
   {
      String rootDir = getInstallValue(RxVariables.INSTALL_DIR);
      try
      {
         if (rootDir.trim().length() < 1)
            return;

         rootDir = rootDir.trim();
         File dir = new File(rootDir);

         if (dir.exists())
            searchForCPFiles(dir.listFiles());
      }
      catch (Exception e)
      {
         RxLogger.logError("Search for .cp files failed in : " + rootDir);
         RxLogger.logError("Exception : " + e.getMessage());
      }
      
   }

   /**
    * Searches supplied directory for .cp files.  Adds each to list of .cp files
    * and copies each to the upgrade/rx_cp_files folder.
    * @param files files to search
    */
   private void searchForCPFiles(File[] files)
   {
      RxLogger.logInfo("Searching for .cp files");
      
      // first clear out .cp file list
      ms_cpFiles.clear();
      
      // reset flag
      ms_bCPFilesFound = false;
      
      String path = "";
      String name = "";
      File f = null;
      File p = null;
      File uf = null;
      for (int i = 0; files != null && i < files.length; i++)
      {
         try
         {
            f = files[i];
               
            if (!f.exists() || f.isDirectory())
               continue;
            
            // if it is a .cp file, add to list, set flag, and rename
            if (isCPFile(f))
            {
               name = f.getName();
               p = f.getParentFile();
               path = p.getAbsolutePath();
               uf = new File(path + File.separator + RX_CP_FILES_DIR +
                     File.separator + name);
               
               ms_cpFiles.add(path + File.separator + name);
               ms_bCPFilesFound = true;
               
               if (!uf.getParentFile().exists())
                  uf.getParentFile().mkdirs();
               
               if (uf.exists())
                  uf.delete();
               
               RxLogger.logInfo("Copying " + f.getAbsolutePath() + " to " +
                     uf.getAbsolutePath());
               IOTools.copyFileStreams(f, uf); 
            }
         }
         catch (Throwable e)
         {
            RxLogger.logError("Search for .cp files failed in: " + path);
            RxLogger.logError("Exception : " + e.getMessage());
         }
      }
      
      List cpFiles = getCpFiles();
      if (cpFiles.size() > 0)
      {
         String displayMessage = RxInstallerProperties.getString("cpFilesFound");
         displayMessage += "\n\n";
         displayMessage += RxInstallerProperties.getString("cpFilesFoundMsg");
         displayMessage += "\n\n";
                  
         for (int i=0; i < cpFiles.size(); i++)
         {
            displayMessage += (String) cpFiles.get(i) + "\n";
         }
         
         displayMessage += "\n";
         displayMessage += RxInstallerProperties.getString("docHelp");
         displayMessage += "\n\n";
         displayMessage += RxInstallerProperties.getString("northAmericaSupport");
         displayMessage += "\n\n";
         displayMessage += RxInstallerProperties.getString("europeSupport");
      
         setInstallValue(RxVariables.RX_CP_FILES_FOUND, "true");
         setInstallValue(RxVariables.RX_CP_FILES_MSG, displayMessage);
      }
      else
         setInstallValue(RxVariables.RX_CP_FILES_FOUND, "false");
   }

  /**************************************************************************
  * Bean property Accessors and Mutators
  **************************************************************************/
   
   /**
    *  Returns the list of .cp files
    *  
    *  @return a list of .cp files in the current rhythmyx installation, never
    *  <code>null</code>, may be empty.
    */
   public static List getCpFiles()
   {
      return ms_cpFiles;
   }
   
  /**************************************************************************
  * private function
  **************************************************************************/
   /**
    *  Determines if a given file is a .cp file
    *  
    *  @param f the file to check
    *  
    *  @return <code>true</code> if the file is a .cp file,
    *  <code>false<code> otherwise.
    */
   private boolean isCPFile(File f)
   {
      boolean cpFile = false;
      String fileName = f.getName();
      
      if (fileName.endsWith(".cp"))
         cpFile = true;
      else
      {
         int i;
         
         for (i=1; i <= 26; i++)
         {
            if (fileName.endsWith(".cp" + i))
            {
               cpFile = true;
               break;
            }
         }
      }
      
      return cpFile;
   }
   
  /**************************************************************************
  * Bean properties
  **************************************************************************/
 
   
  /**************************************************************************
   * Static variables
   **************************************************************************/  
  
  /**
   * List of .cp files (if any), never <code>null</code>,
   * may be empty.
   */
   private static List<String> ms_cpFiles = new ArrayList<>();
   
  /**
   * Flag for .cp files found
   */
   public static boolean ms_bCPFilesFound = false; 
   
  /**
   * The directory to which all .cp files will be moved during upgrade,
   * relative to the rhythmyx root.
   */
   public static final String RX_CP_FILES_DIR = 
      "upgrade" + File.separator + "rx_cp_files";
}






