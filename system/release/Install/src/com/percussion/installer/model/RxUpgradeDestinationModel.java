/******************************************************************************
 *
 * [ RxUpgradeDestinationModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.model;

import com.percussion.install.InstallUtil;
import com.percussion.install.PSSystem;
import com.percussion.install.RxFileManager;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAFileUtils;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.installer.RxVariables;
import com.percussion.installer.action.RxStandAloneFlag;
import com.percussion.installer.action.RxUpdateUpgradeFlag;
import com.percussion.installer.action.RxLogger;
import com.percussion.util.PSOsTool;
import com.percussion.utils.xml.PSEntityResolver;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;


/**
 * This model represents an InstallAnywhere panel/console that allows the user
 * to select the Rhythmyx installation to upgrade.  The user can either select
 * the destination directory from the combo box or browse to an existing
 * Rhythmyx directory.
 */
public class RxUpgradeDestinationModel extends RxIAModel
{
   /**
    * Constructs the upgrade destination model.
    * 
    * @param locator the proxy object used to interact with the InstallAnywhere
    * runtime platform.
    */
   public RxUpgradeDestinationModel(IPSProxyLocator locator)
   {
      super(locator);
      m_upgradeDirs = getUpgradeDirectories();
   }
   
   /**
    * Returns if it is being used in the brander.
    * @return <code>true</code> if it is being used in the brander,
    * <code>false</code> otherwise
    */
   public boolean getIsBrander()
   {
      return m_isBrander;
   }
   
   /**
    * Sets if it is being used in the brander.
    * @param isBrander <code>true</code> if it is being used in the brander,
    * <code>false</code> otherwise
    */
   public void setIsBrander(boolean isBrander)
   {
      m_isBrander = isBrander;
   }
   
   /**
    *  Called just before destination panel exits.  Updates the product tree to
    *  set the new install location for the product.
    *
    *  @return <code>true</code> if the panel can be exited, <code>false</code>
    *  otherwise
    */
   @Override
   public boolean queryExit()
   {
      boolean canContinue = false;
      if (getDestDir().equals(RxFileManager.OTHER_DIR))
      {
         canContinue = true;
         //TODO: rename upgradeInstall to upgradeInstallOther
         RxInstallOptionModel.upgradeInstall(true);
      }
      else
      {
         canContinue = validateDestination();
         if (canContinue)
         {
            RxInstallOptionModel.upgradeInstall(false);
            updateProductTree(getDestDir());
            savePreviousVersion(getRootDir());
         }
      }
      
      return canContinue;
   }
   
   @Override
   public boolean queryEnter()
   {
      super.queryEnter();
      return m_isBrander ? true : RxInstallOptionModel.checkUpgradeInstall();
   }
   
   /**
    * Helper method to validate the data entered for the product destination.
    *
    * @return <code>true</code> if the specified destination directory exists
    * and is writable, <code>false</code> otherwise.
    */
   private boolean validateDestination()
   {
      String dirName = getDestDir();
      boolean dirExists = false;
      if (dirName.trim().length() > 0 && !dirName.equals(""))
      {
         // check if destination writable
         try
         {
            if (isDirectoryWritable(dirName))
            {
               //check if the destination directory exits.
               //if it does not exist create one after asking the user's
               //permission.
               dirExists = RxIAFileUtils.fileExists(getFileService(), dirName) ?
                     true : false;
               if (!dirExists)
               {
                  // prompt directory does not exist
                  Object[] args = {dirName};
                  String existError = MessageFormat.format(
                     RxInstallerProperties.getResources().getString(
                           "dirDoesNotExistWarn"), args);
                  
                  String title = RxInstallerProperties.getResources().getString(
                     "dirDoesNotExistTitle");
                  int ans = getUserInput(title, existError, null);
                  
                  if (ans == BUTTON1_RESPONSE)
                     return true;
               }
               else
               {
                  if (RxStandAloneFlag.isStandalone() &&
                        InstallUtil.isServerRunning(dirName))
                  {
                     String errorText = "A Rhythmyx Server is running in the " +
                     "specified directory. Please stop the running server " +
                     "and proceed with the installation";
                     setError(errorText); 
                     validationError("", errorText, "");
                     return false;
                  }
                  return true;
               }
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
      return false;
   }
   
   /**
    * Helper method to update the product tree with the end user's
    * new product destination selection.
    *
    * @param destination the new destination location selected by the end-user.
    */
   private void updateProductTree(String destination)
   {
      // if the destination has spaces in the directory name. Convert to
      // a shortpath ... ON WINDOZE
      if ( PSOsTool.isWindowsPlatform() && destination.indexOf(" ") > 0 )
      {
         destination = PSSystem.jniGetShortPathName(destination);
      }
      
      String resolvedDestination = resolveString(destination);
      setInstallValue(RxVariables.INSTALL_DIR, resolvedDestination);
      RxFileManager.setRootDir(resolvedDestination);
      
      //set Rx root on the entity resolver, so that it can find DTDs 
      PSEntityResolver.getInstance().setResolutionHome(
            new File(RxFileManager.getRootDir()));
   }
   
   /**
    * Helper method to check whether the directory is writeable.
    *
    * @param dirName the directory hierarchy.
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
         RxLogger.logInfo("ERROR : " + e.getMessage());
         RxLogger.logInfo(e);
         return false;
      }
   }
   
   /**
    * Helper method to create directory(ies) hierarchy specified for product
    * installation.
    *
    * @param dirName the directory hierarchy
    * @throws IOException if Installation Directory exists and its read-only
    */
   private void createDirs(String dirName)
   throws IOException
   {
      Stack<String> parents = new Stack<String>();
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
      
      Vector<String> created = new Vector<String>();
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
    * Creates the "PreviousVersion.properties" file in the Rhythmyx root
    * directory. This file contains the version information of Rhythmyx
    * prior to upgrade.
    * 
    * @param rootDir the root installation directory
    * 
    * @throws IllegalArgumentException if <code>wiz</code> is <code>null</code>
    */
   public static void savePreviousVersion(String rootDir)
   {
      InstallUtil.writePreviousVersion(rootDir);
   }
   
   /**
    * Method to read and return Upgrade directories, this is a sorted set of
    * previous installations
    * @return array of directory names
    */
   public String[] getUpgradeDirectories()
   {   
      Set upgradeDirs = RxUpdateUpgradeFlag.getUpgradeDirectories();
      String[] dirs = new String[upgradeDirs.size() + 1];
      Iterator rxDirIt = upgradeDirs.iterator();
      int i = 0;
      while (rxDirIt.hasNext())
      {
         dirs[i] = (String)rxDirIt.next();
         i++;
      }
      dirs[i] = RxFileManager.OTHER_DIR;   
      
      return dirs;
   }
   
   /**
    * @return Returns the m_upgradeDirs.
    */
   public String[] getUpgradeDirs()
   {
      return m_upgradeDirs;
   }
   /**
    * @param dirs The m_upgradeDirs to set.
    */
   public void setUpgradeDirs(String[] dirs)
   {
      m_upgradeDirs = dirs;
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
   
   @Override
   public String getTitle()
   {
      return "Installation Directory";
   }
   
   @Override
   protected boolean validateModel()
   {     
      return true;
   }   
   
   /**
    * The variable name of the isBrander parameter passed to this model's
    * corresponding panel and console via the IDE.
    */
   public static final String IS_BRANDER_VAR = "isBrander";   
   
   /***********************************************************************
    * Member Variables
    ***********************************************************************/
   
   /**
    * Directory paths stored in this variable will be available to be selected
    * for upgrade.
    */
   private String[] m_upgradeDirs = {""};
   
   /**
    * Property to determine if it is being used in the brander
    */
   private boolean m_isBrander = false;
   
   /**
    * The destination or installation directory chosen for upgrade.
    */
   private String  m_destDir = ""; 
}


