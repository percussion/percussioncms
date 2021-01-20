/******************************************************************************
 *
 * [ RxServerFeaturePanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installer.model.RxServerFeatureModel;

import java.awt.Label;


/**
 * The panel implementation used to gather Rhythmyx server feature installation
 * selections.
 */
public class RxServerFeaturePanel extends RxComponentPanel
{
   @Override
   public void initialize()
   {
      RxServerFeatureModel sfm = new RxServerFeatureModel(this);
      setModel(sfm);
      
      super.initialize();
           
      rxAdd(new Label(RxInstallerProperties.getResources().getString(
            "featurePanelDesc")));
   }
}
