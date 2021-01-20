/******************************************************************************
 *
 * [ RxDatabaseDriverLocationModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.model;

import com.percussion.install.InstallUtil;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAFileUtils;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.installer.action.RxLogger;
import com.percussion.utils.jdbc.PSDriverHelper;

import java.io.File;
import java.text.MessageFormat;


/**
 * This is a model that maintains the location of the database driver, and
 * allows the end user to change it. When the panel is exited, it validates the
 * driver exists in the path given.
 */
public class RxDatabaseDriverLocationModel extends RxIAModel
{
   /**
    * Constructs the destination model.
    * 
    * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxDatabaseDriverLocationModel(IPSProxyLocator locator)
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
      
      String driverLocation = getDriverLocation(); 
      canContinue = validateDriver(driverLocation);
      
      InstallUtil.addJarFileUrl(driverLocation);
      
      return canContinue;
   }
   
   @Override
   public boolean queryEnter()
   {
       return RxDatabaseModel.isMySql() ? true : false;
   }
   
   /**
    * Helper method to validate driver location.
    *
    * @param driverLocation - location of the driver
    *
    * @return <code>true</code> if the driver exists, <code>false</code> otherwise.
    */
   private boolean validateDriver(String driverLocation)
   {

      if (!((driverLocation == null) || (driverLocation.trim().length() == 0)))
      {
         try
         {
            // check if driver is a file
            File f = new File(driverLocation);
            if (!f.isFile())
            {
               // prompt driver doesn't exist
               Object[] args = {driverLocation};
               String existError = MessageFormat.format(
                  RxInstallerProperties.getResources().getString(
                        "driverDoesNotExistWarn"), args);
               validationError(existError, null, null);
               return false;
            }
            else
            {
               String driverClass = RxProtocolModel.fetchDriverClass();
               
               // check if file contains the driver class
               try
               {
                  PSDriverHelper.getDriver(driverClass, driverLocation);
               }
               catch (ClassNotFoundException e)
               {
                  // prompt file doesn't contain driver
                  Object[] args = {driverLocation, driverClass};
                  String invalidError = MessageFormat.format(
                     RxInstallerProperties.getResources().getString(
                           "driverNotValidWarn"), args);
                  validationError(invalidError, null, null);
                  return false;
               }
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
         String errorText = "Driver location may not be null or empty";
         setError(errorText);     
         return false;
      }
      return true;
   }
   
   /**
    * Accessor for the driver location.
    * 
    * @return the location of the database driver chosen for installation.
    */
   static public String getDriverLocation()
   {
      return ms_driverLocation;
   }
   
   /**
    * Mutator for the driver location.
    * 
    * @param driver the location where the installer can find the database driver.
    */
   static public void setDriverLocation(String driver)
   {
      ms_driverLocation = driver;
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
      return "Database Driver Location";
   }
   
   /**
    * The destination or installation directory.
    */
   private static String ms_driverLocation = "";
}
