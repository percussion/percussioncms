/******************************************************************************
 *
 * COPYRIGHT (c) 1999 - 2012 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installer.model.PSDeliveryTierFeatureModel;

import java.awt.Label;

public class PSDeliveryTierFeaturePanel extends RxComponentPanel
{
   @Override
   public void initialize()
   {
      PSDeliveryTierFeatureModel model = new PSDeliveryTierFeatureModel(this);
      setModel(model);
      
      super.initialize();
           
      rxAdd(new Label(RxInstallerProperties.getResources().getString(
            "featurePanelDesc")));
   }
}
