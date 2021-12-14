/******************************************************************************
 *
 * [ RxNewInstallDestinationModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.model;

import com.percussion.install.InstallUtil;
import com.percussion.install.RxFileManager;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAFileUtils;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.installer.RxVariables;
import com.percussion.installer.action.RxLogger;
import com.percussion.installer.action.RxStandAloneFlag;
import com.percussion.installer.action.RxUpdateUpgradeFlag;
import com.percussion.util.PSOsTool;
import com.percussion.utils.xml.PSEntityResolver;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;


/**
 * This is a model that maintains the default product install location, and
 * allows the end user to change it. When the panel is exited, it validates the
 * destination directory and updates the corresponding InstallAnywhere variable
 * with the chosen location.
 */
public class RxNewInstallDestinationModel extends RxIAModel
{
   /**
    * Constructs the destination model.
    * 
    * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxNewInstallDestinationModel(IPSProxyLocator locator)
   {
      super(locator);
   }
   
   /**
    *  Called just before destination panel exits.  Updates the new install
    *  location for the product.
    *
    *  @return <code>true</code> if the panel can be exited, <code>false</code>
    *  otherwise.
    */
   @Override
   public boolean queryExit()
   {
      if(!super.queryExit())
         return false;
      
      boolean canContinue = false;
      
      String destDir = getDestDir();
      
      canContinue = validateDestination(destDir);
      if (canContinue)
         canContinue = validateInstallType(destDir);
      
      if (canContinue)
      {
         updateInstallDir(destDir);
         InstallUtil.writePreviousVersion(destDir);
      }
      return canContinue;
   }
   
   @Override
   public boolean queryEnter()
   {
      String productInstallLocation = getRootDir();
      
      setDestDir(productInstallLocation);
      
      int type = RxInstallOptionModel.fetchInstallType();
      
      return (type == RxInstallOptionModel.RX_INSTALL_NEW || 
            type == RxInstallOptionModel.RX_INSTALL_UPGRADE_OTHER) ?
                  true : false;
   }
   
   /**
    * Helper method to validate the Rhythmyx root directory selected by
    * the user.
    * <p>
    * If the user arrived to this panel by selecting "Other..." in
    * the upgrade destination panel, then this validates that the directory
    * is a valid Rhythmyx directory (contains "rxconfig" directory). If not,
    * then it shows a warning that a new installation will be performed. If the
    * user clicks OK then <code>true</code> is returned and the installation
    * continues, otherwise <code>false</code> is returned and the user needs
    * to enter a different directory.
    * <p>
    * If the user arrived to this panel by selecting "New Install" type on the
    * installation type selection panel, then this validates that the directory
    * is not a valid Rhythmyx directory (should not contain "rxconfig"
    * directory). If this directory already contains one or more installed
    * Rx component then a warning is displayed that an upgrade of existing
    * components will be performed. If the user clicks OK then
    * <code>true</code> is returned and the installation continues, otherwise
    * <code>false</code> is returned and the user needs to enter a different
    * directory.
    * <p>
    *
    * @param rxRoot the Rhythmyx root directory, assumed not <code>null</code>.
    *
    * @return <code>true</code> if the specified destination directory exists
    * and is writable, <code>false</code> otherwise.
    */
   private boolean validateInstallType(String rxRoot)
   {
      boolean ret = true;
      String dirName = rxRoot;
      
      int type = RxInstallOptionModel.fetchInstallType();
      String guierr = null;
      String conerr = null;
      
      if (type == RxInstallOptionModel.RX_INSTALL_NEW)
      {
         if (RxFileManager.isCM1Dir(dirName))
         {
            File lib = new File(dirName + "/lib");
            File rxLib = new File(dirName + "/AppServer/server/rx/lib");
            if (lib.exists() || rxLib.exists())
            {
               guierr = RxInstallerProperties.getResources().getString(
               "newRxInstallWarn");
               conerr = RxInstallerProperties.getResources().getString(
               "newRxInstallWarnConsole");
            }
         }
      }
      else if (type == RxInstallOptionModel.RX_INSTALL_UPGRADE_OTHER)
      {
         if (!RxFileManager.isCM1Dir(dirName))
         {
            guierr = RxInstallerProperties.getResources().getString(
            "upgradeRxInstallWarn");
            conerr = RxInstallerProperties.getResources().getString(
            "upgradeRxInstallWarnConsole");
         }
      }
      
      if (guierr != null)
      {
         String installDir = RxInstallerProperties.getResources().getString(
         "installdir");
         int ans = getUserInput(installDir, guierr, conerr);
         
         ret = ans != BUTTON2_RESPONSE;
         
         if (ret && guierr.equals(RxInstallerProperties.getResources().
               getString("newRxInstallWarn")))
            RxUpdateUpgradeFlag.upgradeInstall();
      }
      return ret;
   }
   
   /**
    * Helper method to validate the data entered for the product destination.
    *
    * @param rxRoot the Rhythmyx root directory.
    *
    * @return <code>true</code> if the specified destination directory exists
    * and is writable, <code>false</code> otherwise.
    */
   private boolean validateDestination(String rxRoot)
   {
      String dirName = rxRoot;
      if (!((dirName == null) || (dirName.trim().length() == 0)))
      {
         try
         {
            // check if destination directory exists
            File f = new File(dirName);
            if (!f.isDirectory())
            {
               // prompt directory does not exist
               Object[] args = {dirName};
               String existError = MessageFormat.format(
                  RxInstallerProperties.getResources().getString(
                        "dirDoesNotExistWarn"), args);
                          
               String title = RxInstallerProperties.getResources().getString(
                     "dirDoesNotExistTitle");
               int ans = getUserInput(title, existError, null, "Yes", "No");
               
               if (ans == BUTTON2_RESPONSE)
                  return false;
            }
            if (PSOsTool.isWindowsPlatform())
            {
                // check if destination directory is special
                if (isSpecialWinDir(dirName))
                {
                    Object[] args = {getInstallValue(RxVariables.PRODUCT_NAME), dirName};
                    String dirWarn = MessageFormat.format(RxInstallerProperties.getResources().getString(
                             "winDirWarn"), args);
                               
                    int ans = getUserInput("Warning", dirWarn, null, "Yes", "No");
                    
                    if (ans == BUTTON2_RESPONSE)
                       return false;
                }
            }
            // check if destination directory is writable
            if (isDirectoryWritable(dirName))
            {
               if (RxStandAloneFlag.isStandalone() &&
                     InstallUtil.isServerRunning(dirName))
               {
                  String errorText = "A Percussion Server is running in the "
                     + "specified directory. Please stop the running server "
                     + "and proceed with the installation";
                  setError(errorText); 
                  validationError("", errorText, "");
                  return false;
               }
               return true;
            }
            else
            {
               // prompt directory not writable
               String writeError = "The directory " + dirName + " is not " +
               "writable.";
               
               getUserInput("Directory not writable", writeError, null);
               
               return false;
            }
         }
         catch (Exception e)
         {
            RxLogger.logInfo("ERROR : " + e.getMessage());
            RxLogger.logInfo(e);
         }
      }
      else
      {
         String errorText = "Directory name may not be null or empty";
         
         setError(errorText);         
      }
      return false;
   }
   
   /**
    * Accessor for the installation directory.
    * 
    * @return the directory chosen for installation.
    */
   public String getDestDir()
   {
      return m_destDir;
   }
   
   /**
    * Mutator for the installation directory.
    * 
    * @param dir the directory to which Rhythmyx will be installed.
    */
   public void setDestDir(String dir)
   {
      m_destDir = dir;
   }
   
   /**
    * Helper method to update the Rx InstallAnywhere install location with the
    * end user's new product destination selection.
    *
    * @param destination the new destination location selected by the end-user,
    * assumed not <code>null</code> and non-empty.
    */
   private void updateInstallDir(String destination)
   {
      File rootDir = new File(destination);
      rootDir.mkdirs();
      
      RxFileManager.setLongRootDir(resolveString(destination));
      setInstallValue(RxVariables.INSTALL_DIR, destination);
           
      RxFileManager.setRootDir(resolveString(destination));
      
      //set Rx root on the entity resolver, so that it can find DTDs 
      PSEntityResolver.getInstance().setResolutionHome(
            new File(RxFileManager.getRootDir()));
   }
   
   /**
    * This method will be executed during silent installation.  If the directory
    * doesn't exist and the createDirectoryResponse global wizard value is set to
    * YES_RESPONSE, then the directory will be created.
    */
   public void execute()
   {
      try
      {
         String productInstallLocation = getRootDir();
                  
         String resolvedDestination = "";
         if (productInstallLocation != null)
         {
            resolvedDestination = resolveString(productInstallLocation);
         }
         
         boolean dirExists = false;
         if (!((resolvedDestination == null) ||
               (resolvedDestination.trim().length() == 0)))
         {
            dirExists = RxIAFileUtils.fileExists(getFileService(), 
                  resolvedDestination) ?
                  true : false;
            if (!dirExists)
            {
               //prompt directory does not exist
               Object[] args = {resolvedDestination};
               String existError = MessageFormat.format(
                  RxInstallerProperties.getResources().getString(
                        "dirDoesNotExistWarn"), args);
                          
               String title = RxInstallerProperties.getResources().getString(
                     "dirDoesNotExistTitle");
                              
               int response = getUserInput(title, existError, null);
               
               if (response == BUTTON1_RESPONSE)
               {
                  RxIAFileUtils.createDirectory(getFileService(), 
                        resolvedDestination);
               }
            }
         }
      }
      catch (Exception e)
      {
         RxLogger.logError(e.getMessage());
         RxLogger.logError(e);
      }
   }
   
   @Override
   public String getTitle()
   {
      return "Installation Directory";
   }
   
   /**
    * Helper method to check whether the directory is writeable.
    *
    * @param dirName the directory hierarchy, assumed not <code>null</code>
    * and non-empty
    *
    * @return <code>true</code> if the directory is writable, <code>false</code>
    * otherwise.
    */
   private boolean isDirectoryWritable(String dirName)
   {
      try
      {
         createDirs(dirName);
         return true;
      }
      catch (Exception e)
      {
         RxLogger.logError(e.getMessage());
         RxLogger.logError(e);
         return false;
      }
   }
   
   /**
    *  Helper method to create directory(ies) hierarchy specified for product installation.
    *
    * @param dirName the directory hierarchy, assumed not <code>null</code>
    * and non-empty
    * @throws IOException if Installation Directory exists and its read-only
    */
   private void createDirs(String dirName)
   throws IOException
   {
      Stack<String> parents = new Stack<>();
      String parent = dirName;
      while (parent != null)
      {
         if (!RxIAFileUtils.fileExists(getFileService(), parent))
         {
            parents.push(parent);
         }
         else
         {
            break;
         }
         String parentName = RxIAFileUtils.getParent(getFileService(), parent);
         parent = parentName;
      }
      // Check if the directory already existed and has write permissions
      if (parents.isEmpty() && dirName != null)
      {
         if (!RxIAFileUtils.isDirectoryWritable(getFileService(), dirName))
         {
            throw new IOException("Directory exists and its read-only");
         }
         return;
      }
      
      Vector<String> created = new Vector<>();
      while (!parents.isEmpty())
      {
         parent = parents.pop();
         if (!RxIAFileUtils.fileExists(getFileService(), parent))
         {
            RxIAFileUtils.createDirectory(getFileService(), parent);
            created.addElement(parent);
         }
         else
         {
            if (!RxIAFileUtils.isDirectoryWritable(getFileService(), parent))
            {
               throw new IOException("Directory is read-only");
            }
         }
      }
      for (int i = created.size()-1; i >= 0; i--)
      {
         if (RxIAFileUtils.fileExists(getFileService(), created.elementAt(i)))
         {
            RxIAFileUtils.deleteDirectory(getFileService(),
                  created.elementAt(i));
         }
      }
   }
   
   /**
    * Determines if the specified directory is special for windows.  This means that the product may have issues during
    * startup if installed to the directory.
    * 
    * @param dir the absolute path of the directory.
    * 
    * @return <code>true</code> if the directory is special for windows, <code>false</code> otherwise.
    */
   private boolean isSpecialWinDir(String dir)
   {
      for (String specialWinDir : ms_specialWinDirs)
      {
         if (dir.startsWith(specialWinDir + "\\")
               || dir.equalsIgnoreCase(specialWinDir))
         {
            return true;
         }
      }

      return false;
   }
   
   /**
    * Set of directories which are considered special for windows.  See {@link #isSpecialWinDir(String)}.
    */
   private static Set<String> ms_specialWinDirs = new HashSet<>();
   
   /**
    * The destination or installation directory.
    */
   private String m_destDir = "";
   
   {
       ms_specialWinDirs.add(RxFileManager.getProgramDir());
       ms_specialWinDirs.add("C:\\Program Files");
   }

}
