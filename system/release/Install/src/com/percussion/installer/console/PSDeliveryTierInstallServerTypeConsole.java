package com.percussion.installer.console;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.installer.RxVariables;
import com.percussion.installer.model.PSDeliveryTierInstallOptionModel;
import com.percussion.installer.model.PSDeliveryTierInstallServerTypeModel;

public class PSDeliveryTierInstallServerTypeConsole extends RxIAConsole
{

   @Override
   protected void execute() throws RxIAPreviousRequestException
   {
      PSDeliveryTierInstallServerTypeModel model = getDataModel();
      int res = getConsoleUtils().createChoiceListAndGetValue(
            "Enter one of the options above",
            new String[] {
            RxInstallerProperties.getResources().getString("newProductionInstall"),
            RxInstallerProperties.getResources().getString("newStagingInstall")},
            0);
      if (res == 0){
         model.setServerType(PSDeliveryTierInstallServerTypeModel.PRODUCTION);
         setInstallValue(RxVariables.DTS_SERVER_TYPE, "production");
      }
      else if (res == 1){
         model.setServerType(PSDeliveryTierInstallServerTypeModel.STAGING);
         setInstallValue(RxVariables.DTS_SERVER_TYPE, "staging");
      }
   }
   
   @Override
   protected void initialize()
   {
      super.initialize();
      
      PSDeliveryTierInstallServerTypeModel stm = new PSDeliveryTierInstallServerTypeModel(this);
      setModel(stm);
   }
   
   
   
   /**
    * The data model stores user input captured by this console.
    * 
    * @return the data model for this console.
    */
   private PSDeliveryTierInstallServerTypeModel getDataModel()
   {
      return (PSDeliveryTierInstallServerTypeModel) getModel();
   }
}
