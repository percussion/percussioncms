/******************************************************************************
 *
 * [ RxUpgradeAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.InstallUtil;
import com.percussion.install.RxUpgrade;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installanywhere.RxIAUtils;
import com.percussion.installer.RxVariables;


/**
 * This action runs when it is an upgrade.  The {@link #execute()} method in
 * turn calls {@link RxUpgrade#process(String, String)} to process the upgrade.
 */
public class RxUpgradeAction extends RxIAAction
{
   @Override
   public void execute()
   {
      m_strRootDir = getInstallValue(RxVariables.INSTALL_DIR);
      setUpgradeFileNames(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), UPG_FILES_VAR)));
        
      RxUpgrade upgrade = new RxUpgrade();
      String[] pluginFiles = getUpgradeFileNames();
      for (int i = 0; i < pluginFiles.length; i++)
      {
         String fileName = pluginFiles[i];
         try
         {
            upgrade.process(m_strRootDir, fileName);            
         }
         catch(Exception e)
         {
            RxLogger.logError("file: " + fileName + " " + e.getMessage());
            RxLogger.logError(e);
         }
      }
   }
   
   /**
    * Getter that returns UpgradeFileName.
    * @return Upgrade File Name that was set on this bean.
    */ 
   public String[] getUpgradeFileNames()
   {
      return m_strCfgFiles;
   }
   
   /**
    * Setter for UpgradeFileName.
    * @param fileNames comma-separated list of file names with upgrade plugin
    * registrations.  May be <code>null</code> or <code>empty</code>.
    */  
   public void setUpgradeFileNames(String fileNames)
   {
      m_strCfgFiles = RxIAUtils.toArray(fileNames);
   }    
   
   /**************************************************************************
    * Variables
    **************************************************************************/
   /**
    * Root dir of this installation.
    */
   private String m_strRootDir = "";
   
   /**
    * config file names for upgradeaction
    */
   private String[] m_strCfgFiles = {"rxupgrade.xml",
   "rxOrphanedDataCleanupPlugins.xml"};
   
   /**
    * The variable name for the upgrade file names parameter passed in via the
    * IDE.
    */
   private static final String UPG_FILES_VAR = "upgradeFileNames";
}
