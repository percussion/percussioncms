/******************************************************************************
 *
 * [ RxResolveTempDir.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;



import com.percussion.installanywhere.RxIAAction;
import com.installshield.wizard.service.system.SystemUtilService;

import org.apache.commons.lang.StringUtils;


/**
 * This action will resolve the temp directory variable which will be set as a
 * user variable on the installation system.  This is needed for launcher
 * errors with remote connections to Windows 2003/2000 servers where the user 
 * has deleted the contents of the temp directory.
 */
public class RxResolveTempDir extends RxIAAction
{
   @Override 
   public void execute()
   {
      try
      {
         if (StringUtils.isBlank(System.getenv().get(TMPDIR_VARIABLE_NAME)))
         {
            String tempDir = "C:\\WINDOWS\\TEMP"; 
            
            getSystemUtilService().setEnvironmentVariable(TMPDIR_VARIABLE_NAME,
                  tempDir,
                  SystemUtilService.SYSTEM_LEVEL_ENVIRONMENT_VARIABLE);
            RxLogger.logInfo("System environment variable "
                  + TMPDIR_VARIABLE_NAME + " has been set to: " + tempDir);
         }
      }
      catch (Exception se)
      {
         RxLogger.logInfo("ERROR : " + se.getMessage());
         RxLogger.logInfo(se);
      }
   }
   
   /**
    * Name of the system environment variable to be set.
    */
   private final String TMPDIR_VARIABLE_NAME = "TMPDIR";
}
