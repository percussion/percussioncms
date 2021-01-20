/******************************************************************************
 *
 * [ RxInstallOptionConsole.java ]
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
import com.percussion.installer.model.RxInstallOptionModel;


/**
 * Console Implementation of {@link RxInstallOptionModel}.
 */
public class RxInstallOptionConsole extends RxIAConsole
{
   @Override
   protected void execute() throws RxIAPreviousRequestException
   {
      RxInstallOptionModel model = getDataModel();
      
      int res = getConsoleUtils().createChoiceListAndGetValue(
            "Enter one of the options above",
            new String[] {
            RxInstallerProperties.getResources().getString("newRxInstall"),
            RxInstallerProperties.getResources().getString("upgradeRxInstall")},
            RxInstallOptionModel.fetchInstallType());
            
      if (res == 0)
         model.setInstallType(RxInstallOptionModel.RX_INSTALL_NEW);
      else if (res == 1)
         model.setInstallType(RxInstallOptionModel.RX_INSTALL_UPGRADE);
   }

   @Override
   protected void initialize()
   {
      super.initialize();
      
      RxInstallOptionModel iom = new RxInstallOptionModel(this);
      setModel(iom);
   }
   
   /**
    * The data model stores user input captured by this console.
    * 
    * @return the data model for this console.
    */
   private RxInstallOptionModel getDataModel()
   {
      return (RxInstallOptionModel) getModel();
   }
}
