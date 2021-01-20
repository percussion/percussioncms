/******************************************************************************
 *
 * [ RxCompatibilityScan.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;



import com.percussion.install.InstallUtil;
import com.percussion.install.PSPluginResponse;
import com.percussion.install.RxInstallerProperties;
import com.percussion.install.RxUpgrade;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;

import java.util.ArrayList;


/**
 * This action runs a series of pre-upgrade validation plugins and gathers the
 * appropriate results information to be displayed to the user.
 */
public class RxCompatibilityScan extends RxIAAction
{
   @Override 
   public void execute()
   {
      //set display message
      setProgressDescription(RxInstallerProperties.getString("compScanLabel"));
            
      //set scanning status flag
      ms_bScanning = true;
      
      // execute the pre-upgrade plugin validation checks
      ms_strRootDir = getInstallValue(RxVariables.INSTALL_DIR);
      
      RxUpgrade upgrade = new RxUpgrade();
      String pluginFile = getPreUpgradeFileName();
      
      try
      {
         // run plugins
         upgrade.process(ms_strRootDir, pluginFile);            
      }
      catch(Exception e)
      {
         RxLogger.logError("file: " + pluginFile + " " + e.getMessage());
         RxLogger.logError(e);
      }
      finally
      {
         // scanning is complete
         ms_bScanning = false;
      }
   }
   
   /**
    * Getter that returns scanning results.
    * @return results of compatibility scan.
    */
   public static String getResults()
   {
      String results = "";
      ArrayList pluginResponses = RxUpgrade.getResponses();      
      
      if (pluginResponses.size() > 0)
      {
         ms_bExceptions = false;
         
         //Build display message      
         for (int i=0; i < pluginResponses.size(); i++)
         {
            PSPluginResponse response = 
               (PSPluginResponse) pluginResponses.get(i);
            if (response.getType() == PSPluginResponse.EXCEPTION ||
                  response.getType() == PSPluginResponse.WARNING)
            {
               if (results.trim().length() == 0)
                  results += RxInstallerProperties.getString("compScanResults")
                          + "\n";
               results += "\n\n" + response.getMessage();
            }
            if (response.getType() == PSPluginResponse.EXCEPTION)
               ms_bExceptions = true;
         }
      
         if (results.trim().length() > 0)
         {
            //There are results, so add support, status info
            String docHelp = RxInstallerProperties.getString("docHelp");
            String northAmericaSupport = RxInstallerProperties.getString(
                  "northAmericaSupport");
            String europeSupport = RxInstallerProperties.getString(
                  "europeSupport");
            String upgradeStopped = RxInstallerProperties.getString(
                  "upgradeCantContinue");
      
            results += "\n\n" + docHelp + "\n\n" + northAmericaSupport +
                       "\n\n" + europeSupport + "\n\n";
            
            if (ms_bExceptions)
            {
               results += upgradeStopped;
             
               try
               {
                  // Restore original repository properties file
                  InstallUtil.restoreRepositoryPropertyFile(ms_strRootDir);
               }
               catch (Exception e)
               {
                  RxLogger.logError("RxCompatibilityScan#getResults() : "
                        + "Error restoring repository properties file.");
                  RxLogger.logError(e);
               }
            }
         }
      }
      return results;
   }
   
   /**
    * Getter that returns Pre-UpgradeFileName.
    * @return Pre-Upgrade File Name that was set on this bean.
    */ 
   public String getPreUpgradeFileName()
   {
       return m_strCfgFile;
   }
   
   /**
    * Getter that returns exceptions status.
    * @return <code>true</code> if exceptions were found, <code>false</code>
    * otherwise.
    */
   public static boolean getExceptionsStatus()
   {
      return ms_bExceptions;
   }
   
   /**
    * Getter that returns scanning status.
    * @return <code>true</code> if scanning is in progress, <code>false</code>
    * otherwise.
    */
   public static boolean getScanStatus()
   {
      return ms_bScanning;
   }
   
   /**
    * Root dir of this installation.
    */
   private static String ms_strRootDir = "";
   
   /**
    * config file name for pre-upgradeaction
    */
   private String m_strCfgFile = "rxpreupgrade.xml";
   
   /**
    * Flag for scanning status.  
    */
   private static boolean ms_bScanning = true;
   
   /**
    * Exceptions status
    */
   private static boolean ms_bExceptions = false;
}
