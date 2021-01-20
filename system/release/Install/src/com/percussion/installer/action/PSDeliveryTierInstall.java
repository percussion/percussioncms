/******************************************************************************
 *
 * COPYRIGHT (c) 1999 - 2012 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.action;

import com.percussion.installer.RxVariables;
import com.percussion.installer.model.PSDeliveryTierFeatureModel;
import com.percussion.installer.model.RxComponentModel;
import com.percussion.installer.model.RxDevToolsFeatureModel;
import com.percussion.installer.model.RxModel;

public class PSDeliveryTierInstall extends RxInstall
{
   private static long EST_TIME_TO_INSTALL=2000;
   
   
   @Override 
   public void setAdditionalProperties()
   {
   
   //Set selected products (only devtools will be installed)
   ms_propertiesMap.put(REPOSITORY_PROP, NO_VAL);
   ms_propertiesMap.put(SERVER_PROP, NO_VAL);
   ms_propertiesMap.put(DEVTOOLS_PROP, NO_VAL);
   ms_propertiesMap.put(FF_PROP, NO_VAL);
   ms_propertiesMap.put(DTS_PROP, YES_VAL);
   ms_propertiesMap.put(INSTALL_TYPE_PROP,
         PSDeliveryTierUpgradeFlag.checkNewInstall() ? "new" : "upgrade");
   ms_propertiesMap.put(INSTALL_DIR_PROP,getInstallValue(RxVariables.INSTALL_DIR));
   ms_propertiesMap.put(DTS_SEVER_TYPE_PROP,getInstallValue(RxVariables.DTS_SERVER_TYPE));
   //Set selected features
   for (RxModel model : getModels())
   {
      if (model instanceof RxComponentModel)
      {
         if (model instanceof PSDeliveryTierFeatureModel)
         {
            setInstallDTSFeatures((PSDeliveryTierFeatureModel) model);
            continue;
         }
      }
   }
}
      
   @Override
   public long getEstTimeToInstall()
   {
      return EST_TIME_TO_INSTALL;
   }
}
