/******************************************************************************
 *
 * [ RxDevToolsFeaturePanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installer.model.RxDevToolsFeatureModel;

import java.awt.Label;


/**
 * The panel implementation used to gather Rhythmyx Developemnt Tools feature
 * installation selections.
 */
public class RxDevToolsFeaturePanel extends RxComponentPanel
{
   @Override
   public void initialize()
   {
      RxDevToolsFeatureModel dtfm = new RxDevToolsFeatureModel(this);
      setModel(dtfm);
      
      super.initialize();
           
      rxAdd(new Label(RxInstallerProperties.getResources().getString(
            "featurePanelDesc")));
   }
}
