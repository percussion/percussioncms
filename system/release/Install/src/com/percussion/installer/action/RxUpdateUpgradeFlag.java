/******************************************************************************
 *
 * [ RxUpdateUpgradeFlag.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.InstallUtil;
import com.percussion.install.RxFileManager;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;
import com.percussion.util.PSOsTool;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;


/**
 * This action will check to see if this is an upgrade install.
 */
public class RxUpdateUpgradeFlag extends RxIAAction
{
   /**
    * Constructs an {@link RxUpdateUpgradeFlag}.
    */
   public RxUpdateUpgradeFlag()
   {
      super();
   }
   
   /**
    * Will check for the cm1install.properties file for previous installations.
    */
   @Override
   public void execute()
   {
      setPropertyName(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), PROP_NAME_VAR)));
      
      try
      {
         String programDir = getInstallValue(RxVariables.PROGRAMS_DIR);
         
         if (programDir.trim().length() < 1)
         {
            RxLogger.logInfo("Failed to obtain program files directory.");
            return;
         }
         
         RxFileManager.setProgramDir(programDir);
         String strInstallDirs = "";
                  
         if (PSOsTool.isWindowsPlatform())
         {
            // load the 
            // $(program_files)/Percussion/cm1install.properties file
            String winPropFilePath =
               RxFileManager.getWindowsSystemInstallationFile();
            Properties winProps = RxFileManager.loadProperties(winPropFilePath);
            strInstallDirs = winProps.getProperty(RxFileManager.INSTALL_PROP,
                  "");
                        
            // if $(user.home)/cm1install.properties exists then merge it with
            // the $(program_files)/Percussion/cm1install.properties and
            // then delete the properties file in the home directory
            String homeDirPropFilePath =
               RxFileManager.getUnixSystemInstallationFile();
            File homeDirPropFile = new File(homeDirPropFilePath);
            if (homeDirPropFile.exists())
            {
               Properties mergeProps =
                  RxFileManager.loadProperties(homeDirPropFilePath);
               String mergeDirs = mergeProps.getProperty(
                     RxFileManager.INSTALL_PROP, "");
               
               if (mergeDirs.trim().length() > 0)
               {
                  strInstallDirs += ";";
                  strInstallDirs += mergeDirs.trim();
               }
               
               // delete the old properties file
               homeDirPropFile.delete();
            }
         }
         else
         {
            // On Unix platform, load the $(user.home)/cm1install.properties file
            String unixPropFilePath =
               RxFileManager.getUnixSystemInstallationFile();
            Properties unixProps = RxFileManager.loadProperties(
                  unixPropFilePath);
            strInstallDirs = unixProps.getProperty(RxFileManager.INSTALL_PROP,
                  "");
         }
         
         ms_upgDirs.clear();
         
         StringTokenizer tokens = new StringTokenizer(strInstallDirs, ";");
         while (tokens.hasMoreTokens())
         {
            String strDir = tokens.nextToken().trim();
            if (checkValidCM1Dir(strDir))
               ms_upgDirs.add(strDir);
         }
         
         if (ms_upgDirs.size() > 0)
            ms_bUpgrade = true;
         
         // save the valid directories
         String newDirs = "";
         Iterator<?> rxDirIt = ms_upgDirs.iterator();
         while (rxDirIt.hasNext())
         {
            newDirs += (String)rxDirIt.next();
            newDirs += ";";
         }
         Properties newProps = RxFileManager.loadProperties(
               RxFileManager.getSystemInstallationFile());
         newProps.setProperty(getPropertyName(), newDirs);
                 
         RxFileManager.saveProperties(
               newProps, RxFileManager.getSystemInstallationFile());
         
         //set the default directory
         if(!isSilentInstall() || StringUtils.isEmpty(getInstallValue(RxVariables.INSTALL_DIR)))
         {
            String strInstDir = StringUtils.isNotEmpty(getInstallValue(RxVariables.CM1_SDK_INSTALL)) ?
                  RxFileManager.getDefaultSDKInstallationDirectory() : RxFileManager.getDefaultInstallationDirectory();
            setInstallValue(RxVariables.INSTALL_DIR, strInstDir);
         }
      }
      catch (Exception e)
      {
         RxLogger.logInfo("ERROR : " + e.getMessage());
         RxLogger.logInfo(e);
      }
   }
   
   /**
    * Determines whether the path specified by <code>strDir</code> is a
    * valid CM1 root directory. If <code>strDir</code> is an existing
    * directory and contains the "cm.war" deploy directory then it will be
    * considered a valid CM1 root directory.
    *
    * @param strDir directory path to check for valid CM1 installation,
    * may be <code>null</code> or empty in which case <code>false</code> is
    * returned.
    *
    * @return <code>true</code> if the file specified by <code>strDir</code>
    * is a valid CM1 root directory, <code>false</code> otherwise.
    */
   private boolean checkValidCM1Dir(String strDir)
   {
      if ((strDir == null) || (strDir.trim().length() < 1))
         return false;
      
      return RxFileManager.isCM1Dir(strDir);
   }
   
   /**************************************************************************
    * Bean Properties
    *************************************************************************/
   /**
    *  The Upgrade property accessor.
    *
    *  @return <code>true</code> if this is an upgrade install,
    *  <code>false</code> otherwise.
    */
   public boolean getUpgrade()
   {
      return ms_bUpgrade;
   }
   
   /**
    *  @return <code>true</code> if this is an upgrade install,
    *  <code>false</code> otherwise.
    */
   public static boolean isUpgrade()
   {
      return ms_bUpgrade;
   }
   
   /*************************************************************************
    * Public functions
    *************************************************************************/
   
   /**
    * Determine if the type of Rx installation is new install.
    *
    * @return <code>true</code> if the type of Rx installation is new install,
    * <code>false</code> otherwise
    */
   public static boolean checkNewInstall()
   {
      return ms_bUpgrade ? false : true;
   }
   
   /**
    * Determine if the type of Rx installation is upgrade install.
    *
    * @return <code>true</code> if the type of Rx installation is upgrade
    * install, <code>false</code> otherwise
    */
   public static boolean checkUpgradeInstall()
   {
      return ms_bUpgrade ? true : false;
   }
   
   /**
    * Sets the type of Rx installation to new install.
    */
   public static void newInstall()
   {
      ms_bUpgrade = false;
   }
   
   /**
    * Sets the type of Rx installation to upgrade install.
    */
   public static void upgradeInstall()
   {
      ms_bUpgrade = true;
   }
   
   /**
    * Returns the sorted set of previous Rhythmyx installations on the system.
    *
    * @return a sorted set containing <code>String</code> objects representing
    * the previous Rhythmyx installations on the system.
    */
   public static Set<String> getUpgradeDirectories()
   {
      return ms_upgDirs;
   }
   
   /*************************************************************************
    * Property Accessors and Mutators
    *************************************************************************/
   
   /**
    * Accessor for the property name property.
    * 
    * @return the name of the property used to store the Rhythmyx install
    * directories.
    */
   public String getPropertyName()
   {
      return m_propertyName;
   }
   
   /**
    * Mutator for the property name property.
    * 
    * @param propertyName the name of the property used to store the Rhythmyx
    * install directories.
    */
   public void setPropertyName(String propertyName)
   {
      m_propertyName = propertyName;
   }  
   
   /**************************************************************************
    * Static Variables
    *************************************************************************/
   
   /**
    * Upgrade flag.
    */
   public static boolean ms_bUpgrade = false;
   
   /**
    * Sorted Set of previous Rhythmyx installations as <code>String</code>
    * objects, never <code>null</code>, may be empty,
    * modified in the <code>execute()</code> method.
    */
   public static SortedSet<String> ms_upgDirs = new TreeSet<>();
   
   /**
    * The variable name for the property name parameter passed in via the IDE.
    */
   private static String PROP_NAME_VAR = "propertyName";
   
   /**************************************************************************
    * Variables
    *************************************************************************/
   
   /**************************************************************************
    * Properties
    *************************************************************************/
   
   /**
    * The installation directory property name.
    */
   private String m_propertyName = RxFileManager.INSTALL_PROP;
   
}
