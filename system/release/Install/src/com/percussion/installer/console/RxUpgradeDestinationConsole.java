/******************************************************************************
 *
 * [ RxUpgradeDestinationConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.install.InstallUtil;
import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.installer.model.RxUpgradeDestinationModel;


/**
 * Console Implementation of {@link RxUpgradeDestinationModel}.
 */
public class RxUpgradeDestinationConsole extends RxIAConsole
{
   @Override
   public void execute() throws RxIAPreviousRequestException
   {
      // assert safe cast
      RxUpgradeDestinationModel baseM = getDataModel();
      
      String[] upgradeDirs = baseM.getUpgradeDirs();
      
      if (upgradeDirs != null && upgradeDirs.length > 1)
      {
         int res1 = getConsoleUtils().createChoiceListAndGetValue(
               "Directory", upgradeDirs, 0);
         
         baseM.setDestDir(upgradeDirs[res1]);
      }
      else
      {
         String dir = getConsoleUtils().promptAndGetValueWithDefaultValue(
               "Enter a directory", baseM.getDestDir());
         baseM.setDestDir(dir);
      }
   }
   
   @Override
   protected void initialize()
   {
      super.initialize();
      
      RxUpgradeDestinationModel udm = new RxUpgradeDestinationModel(this);
      setModel(udm);
      
      getDataModel().setIsBrander(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), RxUpgradeDestinationModel.IS_BRANDER_VAR)).
            equalsIgnoreCase("true"));
   }
    
   /**
    * The data model stores user input captured by this console.
    * 
    * @return the data model for this console.
    */
   private RxUpgradeDestinationModel getDataModel()
   {
      return (RxUpgradeDestinationModel) getModel();
   }
}
