package com.percussion.installer.console;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.installer.model.PSDeliveryTierInstallOptionModel;
import com.percussion.installer.model.RxInstallOptionModel;

/***
 * Presents the New/Upgrade installation screen.
 * @author Nate
 *
 */
public class PSDeliveryTierInstallOptionsConsole extends RxIAConsole
{
   @Override
   protected void execute() throws RxIAPreviousRequestException
   {
      PSDeliveryTierInstallOptionModel model = getDataModel();
      int mode=0;
      boolean upgrade=PSDeliveryTierInstallOptionModel.checkUpgradeInstall();
      if(upgrade){
         getConsoleUtils().wprintlnWithBreaks(RxInstallerProperties.getResources().getString("dts.cm1InstallDetected"));              
         getConsoleUtils().wprintlnWithBreaks("");
         mode=1;
      }
      
      int res = getConsoleUtils().createChoiceListAndGetValue(
            "Enter one of the options above",
            new String[] {
            RxInstallerProperties.getResources().getString("newRxInstall"),
            RxInstallerProperties.getResources().getString("upgradeRxInstall")},
            mode);
            
      if (res == 0)
         model.setInstallType(PSDeliveryTierInstallOptionModel.INSTALL_NEW);
      else if (res == 1)
         model.setInstallType(PSDeliveryTierInstallOptionModel.INSTALL_UPGRADE);
   }

   @Override
   protected void initialize()
   {
      super.initialize();
      
      PSDeliveryTierInstallOptionModel iom = new PSDeliveryTierInstallOptionModel(this);
      setModel(iom);
   }
   
   /**
    * The data model stores user input captured by this console.
    * 
    * @return the data model for this console.
    */
   private PSDeliveryTierInstallOptionModel getDataModel()
   {
      return (PSDeliveryTierInstallOptionModel) getModel();
   }
}
