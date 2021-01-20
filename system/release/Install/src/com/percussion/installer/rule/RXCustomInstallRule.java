package com.percussion.installer.rule;

import com.percussion.installanywhere.RxIARule;
import com.percussion.installer.model.RxCommunityInstallOptionModel;

public class RXCustomInstallRule extends RxIARule
{

   @Override
   protected boolean evaluate()
   {
     
      return RxCommunityInstallOptionModel.checkCustomInstall();
   }

}
