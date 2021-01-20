/******************************************************************************
 *
 * [ RxCompScanConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.install.InstallUtil;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAConsoleUtils;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.installer.RxVariables;
import com.percussion.installer.action.RxCompatibilityScan;
import com.percussion.installer.action.RxLogger;
import com.percussion.installer.model.RxCompScanModel;


/**
 * Console Implementation of {@link RxCompScanModel}.
 */
public class RxCompScanConsole extends RxIAConsole
{
   @Override
   protected void execute() throws RxIAPreviousRequestException
   {
      //Get console utilities
      RxIAConsoleUtils cu = getConsoleUtils();
      
      //Display progress message
      String compScanLabel = RxInstallerProperties.getString("compScanLabel");
      cu.wprintln(compScanLabel);
      
      //Get results of scan
      String results = RxCompatibilityScan.getResults();
      
      if (results.trim().length() > 0)
      {
         //There were errors/warnings, so display them
         cu.wprintlnWithBreaks(results);
         
         //Check for errors
         if (RxCompatibilityScan.getExceptionsStatus())
         {
            //Restore original Version.properties
            try
            {
               InstallUtil.restoreVersionPropertyFile(
                     getInstallValue(RxVariables.INSTALL_DIR));
            }
            catch (Exception e)
            {
               RxLogger.logError("RxCompScanConsole : Unable to restore " +
                     "Version.properties file");
            }
            
            cu.newline();
            promptAndAbort(false);
         }
      }
      else
      {
         //Compatibility scan was successful, display success message
         String compScanResults = RxInstallerProperties.getString(
               "compScanResults");
         String compScanSuccess = RxInstallerProperties.getString(
               "compScanSuccess");
         results = compScanResults + "\n\n\n" + compScanSuccess;
         cu.wprintln(results);
         cu.newline();
         cu.enterToContinue();
      }
   }
   
   @Override
   protected void initialize()
   {
      super.initialize();
      
      RxCompScanModel csm = new RxCompScanModel(this);
      setModel(csm);
   }
}
