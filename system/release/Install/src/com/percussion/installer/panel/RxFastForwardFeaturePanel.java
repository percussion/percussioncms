/******************************************************************************
 *
 * [ RxFastForwardFeaturePanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installer.model.RxFastForwardFeatureModel;

import java.awt.Label;


/**
 * The panel implementation used to gather Rhythmyx FastForward feature
 * installation selections.
 */
public class RxFastForwardFeaturePanel extends RxComponentPanel
{
   @Override
   public void initialize()
   {
      RxFastForwardFeatureModel fffm = new RxFastForwardFeatureModel(this);
      setModel(fffm);
      
      super.initialize();
           
      rxAdd(new Label(RxInstallerProperties.getResources().getString(
            "featurePanelDesc")));
   }
}
