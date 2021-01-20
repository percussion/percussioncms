package com.percussion.installer.rule;

import com.percussion.installanywhere.RxIARule;
import com.percussion.installer.model.RxCommunityInstallOptionModel;

public class RXDefaultInstallRule extends RxIARule
{

   @Override
   protected boolean evaluate()
   {
     
      return RxCommunityInstallOptionModel.checkRecommendedInstall();
   }   


}
