/******************************************************************************
 *
 * [ RxRenameAppServerModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.model;

import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.installer.action.RxLogger;

import java.io.File;


/**
 * This model represents a panel/console warning that the AppServer directory
 * could not be renamed for upgrade.  It then attempts to rename the directory
 * to AppServer.bak on each call to <code>queryExit</code>, deleting the file if
 * it already exists.  The upgrade will not continue until this is successful.
 */
public class RxRenameAppServerModel extends RxIAModel
{
   /**
    * Constructs an {@link RxRenameAppServerModel} object.
    *  
    * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxRenameAppServerModel(IPSProxyLocator locator)
   {
      super(locator);
      setHideOnUpgrade(false);
   }
   
   @Override
   public boolean queryExit()
   {
      if (!super.queryExit())
         return false;
      
      String errMsg = "";
      boolean error = false;
      
      String rootDir = getRootDir();
      String appServerDir = APPSERVER_DIR;
      
      File appServerFile = new File(rootDir, appServerDir);
      if (!appServerFile.exists())
      {
         RxLogger.logInfo("Source directory does not exist : "
               + appServerDir);
         return true;
      }
      
      String appServerDirBak = appServerDir + ".bak";
      File appServerBakFile = new File(rootDir, appServerDirBak);
      if (appServerBakFile.exists())
      {
         // delete the destination file/directory
         RxLogger.logInfo("Deleting destination directory : "
               + appServerDirBak);
         if (!appServerBakFile.delete())
         {
            errMsg = "Failed to delete destination directory : " 
               + appServerDirBak;
            error = true;
         }
      }
      
      if (appServerFile.renameTo(appServerBakFile))
      {
         RxLogger.logInfo("Renamed source directory : " +
               appServerFile.getAbsolutePath() + " to : " +
               appServerBakFile.getAbsolutePath());
      }
      else
      {
         errMsg = "Failed to rename source directory :\n" +
            appServerFile.getAbsolutePath() + ".  The directory is in use.";
         error = true;
      }
      
      if (error)
      {
         validationError("", errMsg, "");
         return false;
      }
      else
         return true;
   }
   
   @Override
   public String getTitle()
   {
      // Return the empty string here to override the generic title of the 
      // parent class.  A specific title is not required for this model's
      // associated panel/console.
      return "";
   }
   
   /*************************************************************************
    * private variables.
    *************************************************************************/
   
   /**
    * Constant for the AppServer directory.
    */
   private static String APPSERVER_DIR = "AppServer";
}


