/******************************************************************************
 *
 * [ PSDeliveryTierFeatureConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2012 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installer.model.PSDeliveryTierFeatureModel;

/***
 * Handles the console view of the DTS feature.
 * @author Nate
 *
 */
public class PSDeliveryTierFeatureConsole extends RxComponentConsole
{
   @Override
   public void initialize()
   {
      PSDeliveryTierFeatureModel model = new PSDeliveryTierFeatureModel(this);
      setModel(model);
      
      super.initialize();
   }
   
   @Override
   public String getPrompt()
   {
      return RxInstallerProperties.getResources().getString(
      "featureConsoleDesc");
   }

}
