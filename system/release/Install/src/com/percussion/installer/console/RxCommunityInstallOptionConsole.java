package com.percussion.installer.console;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.installer.model.RxCommunityInstallOptionModel;

public class RxCommunityInstallOptionConsole extends RxIAConsole
{

   @Override
   protected void execute() throws RxIAPreviousRequestException
   {

      RxCommunityInstallOptionModel model = getDataModel();
      
      int res = getConsoleUtils().createChoiceListAndGetValue(
            "Enter one of the options above",
            new String[] {
            RxInstallerProperties.getResources().getString("recommendedRxInstall"),
            RxInstallerProperties.getResources().getString("customRxInstall")},
            RxCommunityInstallOptionModel.fetchInstallType());
            
      if (res == 0)
         model.setInstallType(RxCommunityInstallOptionModel.RX_RECOMMENDED);
      else if (res == 1)
         model.setInstallType(RxCommunityInstallOptionModel.RX_CUSTOM);
      
   }
   
   @Override
   protected void initialize()
   {
      super.initialize();
      
      RxCommunityInstallOptionModel iom = new RxCommunityInstallOptionModel(this);
      setModel(iom);
   }
   
   /**
    * The data model stores user input captured by this console.
    * 
    * @return the data model for this console.
    */
   private RxCommunityInstallOptionModel getDataModel()
   {
      return (RxCommunityInstallOptionModel) getModel();
   }

}
