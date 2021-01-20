/******************************************************************************
 *
 * [ RxDevToolsInstall.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;



import com.percussion.installer.model.RxComponentModel;
import com.percussion.installer.model.RxDevToolsFeatureModel;
import com.percussion.installer.model.RxModel;


/**
 * This action sets the necessary properties collected from the devtools
 * installer.
 */
public class RxDevToolsInstall extends RxInstall
{
   @Override 
   public void setAdditionalProperties()
   {
      super.setAdditionalProperties();
      
      //Set selected products (only devtools will be installed)
      ms_propertiesMap.put(REPOSITORY_PROP, NO_VAL);
      ms_propertiesMap.put(SERVER_PROP, NO_VAL);
      ms_propertiesMap.put(DEVTOOLS_PROP, YES_VAL);
      ms_propertiesMap.put(FF_PROP, NO_VAL);
      
      //Set selected features
      for (RxModel model : getModels())
      {
         if (model instanceof RxComponentModel)
         {
            //Set devtools features
            if (model instanceof RxDevToolsFeatureModel)
            {
               setInstallDevToolsFeatures((RxDevToolsFeatureModel) model);
               continue;
            }
         }
      }
   }
   
   @Override
   public long getEstTimeToInstall()
   {
      return 2000;
   }
}
