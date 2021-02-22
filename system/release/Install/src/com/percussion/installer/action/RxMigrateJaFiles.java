/******************************************************************************
 *
 * [ RxMigrateJaFiles.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;
import com.percussion.util.IOTools;
import com.percussion.util.PSOsTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;


/**
 * This action is executed on upgrade to migrate any java arguments specified in
 * .ja files for Rhythmyx launchers to a corresponding .lax properties file.
 */
public class RxMigrateJaFiles extends RxIAAction
{
   /**
    * See this method of <code>RxIAAction</code> for detailed
    * information.
    */
   @Override
   public void execute()
   {
      File rootDir = new File(getInstallValue(RxVariables.INSTALL_DIR));
      File[] rootFiles = rootDir.listFiles();
      for (File f : rootFiles)
      {
         if (f.isDirectory())
            continue;
         
         if (ms_jaFiles.contains(f.getName()))
            migrateFile(f);
      }
   }
   
   /**
    * Loads the contents of the given .ja file and adds them to the existing
    * value of the {@link #ms_laxPropertyName} property in the appropriate .lax
    * file.  The property is added if it does not exist.  The .ja file will
    * be moved to the directory defined by {@link #RX_JA_FILES_DIR}.
    * 
    * @param f the .ja file, assumed not <code>null</code>.
    */
   private void migrateFile(File f)
   {
      String fName = f.getName();
      
      RxLogger.logInfo("Migrating .ja file : " + f.getName());
      
      FileInputStream in = null;
      BufferedReader bReader = null;
      FileWriter fw = null;
      try
      {
         String fContent = IOTools.getFileContent(f);
         if (fContent.trim().length() > 0)
         {
            String fNameRoot = fName.substring(0, fName.indexOf(".ja"));
            if (PSOsTool.isUnixPlatform())
            {
               fNameRoot += ".bin";
            }
            
            String laxName = fNameRoot + ".lax";
            File laxFile = new File(f.getParentFile(), laxName);
            if (!laxFile.exists())
            {
               RxLogger.logInfo("Could not find .lax file : " +
                     laxFile.getName());
               return;
            }
            
            boolean propertyExists = false;
            String line = "";
            String output = "";
            in = new FileInputStream(laxFile);
            bReader = new BufferedReader(new InputStreamReader(in));
            while ((line = bReader.readLine()) != null)
            {
               if (line.trim().length() == 0)
               {
                  output += "\n";
                  continue;
               }
               
               if (output.trim().length() > 0)
                  output += "\n";
                              
               if (line.startsWith("#"))
                  output += line;
               else
               {
                  String[] lineArr = line.split("=");
                  String key = lineArr[0];
                  String val = line.substring(line.indexOf('=') + 1);
                  if (key.equals(ms_laxPropertyName))
                  {
                     if (val.indexOf(fContent) == -1)
                        val += " " + fContent;
                     else
                        continue;
                     
                     propertyExists = true;
                  }
                  
                  output += key + '=' + val;
               }
            }
            
            if (!propertyExists)
            {
               if (output.trim().length() > 0)
                  output += "\n";
               
               output += ms_laxPropertyName + '=' + fContent;
            }
                     
            bReader.close();
            in.close();
            
            //Write the lax file
            fw = new FileWriter(laxFile);
            fw.write(output);
            
            //Move the .ja file
            File upgJaFilesDir = new File(getInstallValue(
                  RxVariables.INSTALL_DIR), RX_JA_FILES_DIR);
            if (!upgJaFilesDir.exists())
               upgJaFilesDir.mkdirs();
            File newJaFile = new File(upgJaFilesDir, fName);
            f.renameTo(newJaFile);
         }
         else
            RxLogger.logInfo(".ja file is empty : " + fName);
      }
      catch (IOException ioe)
      {
         RxLogger.logError("RxMigrateFiles#migrateFile : " +
               ioe.getMessage());
      }
      finally
      {
         if (bReader != null)
         {
            try
            {
               bReader.close();
            }
            catch (IOException e)
            {
            }
         }
              
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (IOException e)
            {
            }
         }
         
         if (fw != null)
         {
            try
            {
               fw.close();
            }
            catch (IOException e)
            {
            }
         }
      }
   }
   
   /**
    * Set of possible Rhythmyx launcher .ja files as of 6.5.2.
    */
   private static Set<String> ms_jaFiles = new HashSet<>();
   
   /**
    * The name of the .lax property to which the java arguments will be moved.
    */
   private static String ms_laxPropertyName = "lax.nl.java.option.additional";
   
   /**
    * The directory to which all migrated .ja files will be moved during
    * upgrade, relative to the Rhythmyx root.
    */
   public static final String RX_JA_FILES_DIR =
      "upgrade" + File.separator + "rx_ja_files";
   
   static
   {
      ms_jaFiles.add("RhythmyxMultiServerManager.ja");
      ms_jaFiles.add("RhythmyxProcessDaemon.ja");
      ms_jaFiles.add("RhythmyxServer.ja");
      ms_jaFiles.add("RhythmyxServerAdministrator.ja");
      ms_jaFiles.add("RhythmyxServerPropertiesEditor.ja");
      ms_jaFiles.add("RhythmyxWorkbench.ja");
      ms_jaFiles.add("RhythmyxXSplit.ja");
      ms_jaFiles.add("Synchronizer.ja");
   }
}


