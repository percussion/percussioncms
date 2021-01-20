/******************************************************************************
 *
 * [ RxInstallOptionTCConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.installer.model.RxInstallOptionTCModel;


/**
 * Console Implementation of {@link RxInstallOptionTCModel}.
 */
public class RxInstallOptionTCConsole extends RxIAConsole
{
   @Override
   protected void execute() throws RxIAPreviousRequestException
   {
      RxInstallOptionTCModel model = getDataModel();
      
      int res = getConsoleUtils().createChoiceListAndGetValue(
            "Enter one of the options above",
            new String[] {
            RxInstallerProperties.getResources().getString("typicalRxInstall"),
            RxInstallerProperties.getResources().getString("customRxInstall")},
            RxInstallOptionTCModel.fetchInstallType());
            
      if (res == 0)
         model.setInstallType(RxInstallOptionTCModel.RX_INSTALL_TYPICAL);
      else if (res == 1)
         model.setInstallType(RxInstallOptionTCModel.RX_INSTALL_CUSTOM);
   }

   @Override
   protected void initialize()
   {
      super.initialize();
      
      RxInstallOptionTCModel iotcm = new RxInstallOptionTCModel(this);
      setModel(iotcm);
   }
   
   /**
    * The data model stores user input captured by this console.
    * 
    * @return the data model for this console.
    */
   private RxInstallOptionTCModel getDataModel()
   {
      return (RxInstallOptionTCModel) getModel();
   }
}
