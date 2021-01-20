/******************************************************************************
 *
 * [ RxCPFilesFoundRule.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.rule;

import com.percussion.installanywhere.RxIARule;
import com.percussion.installer.RxVariables;
import com.percussion.installer.action.RxLogger;
import com.percussion.util.IOTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * This rule will evaluate to <code>true</code> if .cp files have been found in
 * the installation directory.  A list of these files is maintained and each 
 * file is copied to the upgrade/rx_cp_files directory under the Rhythmyx root.
 */
public class RxCPFilesFoundRule extends RxIARule
{
   @Override
   protected boolean evaluate()
   {
      // search the directory for .cp files
      String directoryPath = getInstallValue(RxVariables.INSTALL_DIR);
      try
      {
         File dir = new File(directoryPath);

         if (dir.exists())
            return searchForCPFiles(dir.listFiles());
         else
            RxLogger.logInfo("RxCPFilesFoundRule#evaluate : directory " +
                  directoryPath + " does not exist");

      }
      catch (Exception e)
      {
         RxLogger.logError("Search for .cp files failed in : " + directoryPath);
         RxLogger.logError("Exception : " + e.getMessage());
      }
      
      return false;
   }
   
   /**
    *  Returns the list of .cp files
    *  
    *  @return a list of .cp files in the current rhythmyx installation, never
    *  <code>null</code>, may be empty.
    */
   public static List<String> getCpFiles()
   {
      return ms_cpFiles;
   }
   
   /**
    * Searches supplied directory for .cp files.  Adds each to list of .cp files
    * and copies each to the upgrade/rx_cp_files folder.
    * @param files files to search
    * 
    * @return <code>true</code> if .cp files were found, <code>false</code>
    * otherwise.
    */
   private boolean searchForCPFiles(File[] files)
   {
      RxLogger.logInfo("Searching for .cp files");
      
      // first clear out .cp file list
      ms_cpFiles = new ArrayList<String>();
      
      boolean foundCpFiles = false;
      
      String path = "";
      String name = "";
      File f = null;
      File p = null;
      File uf = null;
      for (int i = 0; files!=null && i < files.length; i++)
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
                                             
               if (!uf.getParentFile().exists())
                  uf.getParentFile().mkdirs();
               
               if (uf.exists())
                  uf.delete();
               
               String originalPath = f.getAbsolutePath();
               String newPath = uf.getAbsolutePath();
               RxLogger.logInfo("Copying " + originalPath + " to " +
                     newPath);
               IOTools.copyFileStreams(f, uf);
               
               ms_cpFiles.add(path + File.separator + name);
               foundCpFiles = true;
            }
         }
         catch (Throwable e)
         {
            RxLogger.logError("Search for .cp files failed in: " + path);
            RxLogger.logError("Exception : " + e.getMessage());
         }
      }
      
      return foundCpFiles;      
   }
   
   /**
    *  Determines if a given file is a .cp file
    *  
    *  @param f the file to check, assumed not <code>null</code>.
    *  
    *  @return <code>true</code> if the file is a .cp file, <code>false<code>
    *  otherwise.
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
   
   /**
    * List of .cp files (if any), never <code>null</code>,
    * may be empty.
    */
    private static List<String> ms_cpFiles = new ArrayList<String>();
    
   /**
    * The directory to which all .cp files will be moved during upgrade,
    * relative to the rhythmyx root.
    */
    public static final String RX_CP_FILES_DIR = 
       "upgrade" + File.separator + "rx_cp_files";
}

