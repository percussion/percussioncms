/******************************************************************************
 *
 * [ RxVersionBuildNumberRule.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.rule;

import com.percussion.install.RxFileManager;
import com.percussion.installanywhere.RxIARule;
import com.percussion.installer.RxVariables;
import com.percussion.installer.action.RxLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * This rule will return <code>true</code> when <code>evaluate</code> is invoked
 * if the installed build on the system lies between the specified minimum and
 * maximum builds.
 */
public abstract class RxVersionBuildNumberRule extends RxIARule
{
   @Override
   protected boolean evaluate()
   {
      return checkVersion();
   }
   
   /**************************************************************************
    * private functions
    **************************************************************************/
   
   /**
    * Returns the value of the specified property <code>propName</code>, -1
    * if the property does not exist or is not an integer.
    *
    * @param propName the name of the property whose value is required, may
    * not be <code>null</code> or empty
    * @param prop the properties object from which the value of the specified
    * property is to be obtained, may not be <code>null</code>
    *
    * @return the value of the specifed property, -1 if the specified property
    * does not exist or if any error occurs converting the property value to
    * integer.
    */
   private int getRequiredProperty(String propName, Properties prop)
   {
      if ((propName == null) || (propName.trim().length() < 1))
         throw new IllegalArgumentException("propName may not be null or empty");
      if (prop == null)
         throw new IllegalArgumentException("prop may not be null");
      
      String strValue = prop.getProperty(propName);
      if ((strValue == null) || (strValue.trim().length() < 1))
      {
         RxLogger.logInfo(
               "Value of property : " + propName + " is null or empty.");
         return -1;
      }
      
      int value = -1;
      try
      {
         value = Integer.parseInt(strValue);
      }
      catch (Throwable t)
      {
         RxLogger.logInfo(
               "Value of property : " + propName + " is invalid.");
         RxLogger.logInfo(t.getLocalizedMessage());
         return -1;
      }
      return value;
   }
   
   /**
    * Checks if the installed build on the system lies between the specified
    * builds.
    *
    * @return <code>true</code> if the installed build on the system lies
    * between the specified builds, <code>false</code> otherwise.
    */
   private boolean checkVersion()
   {
      int installMajorVersion = -1;
      int installMinorVersion = -1;
      int installBuild = -1;
      
      if (ms_versionProps == null)
      {
         InputStream ins = null;
         String strInstallDir = null;
         try
         {
            strInstallDir = getInstallValue(RxVariables.INSTALL_DIR);
                       
            if (strInstallDir == null)
               return false;
            
            if (!strInstallDir.endsWith(File.separator))
               strInstallDir += File.separator;
            
            // check if the "version.properties" file exists under the Rhythmyx
            // root directory
            File propFile = new File(strInstallDir +
                  RxFileManager.PREVIOUS_VERSION_PROPS_FILE);
            if (propFile.exists() && propFile.isFile())
            {
               // load the version.properties file
               ins = new FileInputStream(propFile);
               ms_versionProps = new Properties();
               ms_versionProps.load(ins);
            }
         }
         catch(Exception e)
         {
            RxLogger.logInfo("ERROR : " + e.getMessage());
            RxLogger.logInfo(e);
            return false;
         }
         finally
         {
            if (ins != null)
            {
               try
               {
                  ins.close();
               }
               catch(IOException e)
               {
               }
            }
         }
      }
      
      if (ms_versionProps != null)
      {
         installMajorVersion = getRequiredProperty(
               "majorVersion", ms_versionProps);
         
         installMinorVersion = getRequiredProperty(
               "minorVersion", ms_versionProps);
         
         installBuild = getRequiredProperty(
               "buildNumber", ms_versionProps);
      }
      
      if ((installMajorVersion == -1) ||
            (installMinorVersion == -1) ||
            (installBuild == -1))
         return false;
      
      if (installMajorVersion < getMajorVersionFrom())
         return false;
      
      if (installMajorVersion == getMajorVersionFrom())
      {
         // check the minor version in this case
         if (installMinorVersion < getMinorVersionFrom())
            return false;
         
         if (installMinorVersion == getMinorVersionFrom())
         {
            // check the build number in this case
            if (installBuild < getBuildFrom())
               return false;
         }
      }
      
      if (installMajorVersion > getMajorVersionTo())
         return false;
      
      if (installMajorVersion == getMajorVersionTo())
      {
         // check the minor version in this case
         if (installMinorVersion > getMinorVersionTo())
            return false;
         
         if (installMinorVersion == getMinorVersionTo())
         {
            // check the build number in this case
            if ((getBuildTo() != -1) && (installBuild > getBuildTo()))
               return false;
         }
      }
      
      return true;
   }
   
   /**************************************************************************
    * Bean property Accessors
    **************************************************************************/
   
   /**
    * The minimum major version number which can be upgraded by the build
    * currently being installed.
    *
    * @return the minimum major version number which can be upgraded by the
    * build currently being installed.
    */
   protected abstract int getMajorVersionFrom();
      
   /**
    * The minor version corresponding to <code>majorVersionFrom</code>.
    *
    * @return the minor version corresponding to <code>majorVersionFrom</code>.
    */
   protected abstract int getMinorVersionFrom();
  
   /**
    * The build number corresponding to <code>majorVersionFrom</code> and
    * <code>minorVersionFrom</code>
    *
    * @return the build number corresponding to <code>majorVersionFrom</code>
    * and <code>minorVersionFrom</code>
    */
   protected abstract int getBuildFrom();
  
   /**
    * The maximum major version number which can be upgraded by the build
    * currently being installed.
    *
    * @return the maximum major version number which can be upgraded by the
    * build currently being installed.
    */
   protected abstract int getMajorVersionTo();
   
   /**
    * The minor version corresponding to <code>majorVersionTo</code>.
    *
    * @return the minor version corresponding to <code>majorVersionTo</code>.
    */
   protected abstract int getMinorVersionTo();

   /**
    * The build number corresponding to <code>majorVersionTo</code> and
    * <code>minorVersionTo</code>
    *
    * @return the build number corresponding to <code>majorVersionTo</code> and
    * <code>minorVersionTo</code>
    */
   protected abstract int getBuildTo();
    
   /**************************************************************************
    * member variables
    **************************************************************************/
   
   /**
    * In memory representation of "version.properties" file under the
    * Rhythmyx root directory. May be <code>null</code>.
    */
   private static Properties ms_versionProps = null;
}

