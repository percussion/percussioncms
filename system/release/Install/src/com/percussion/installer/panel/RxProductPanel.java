/******************************************************************************
 *
 * [ RxProductPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installer.model.RxProductModel;

import java.awt.Label;


/**
 * The panel implementation used to gather Rhythmyx product installation
 * selections.
 */
public class RxProductPanel extends RxComponentPanel
{
   @Override
   public void initialize()
   {
      RxProductModel pm = new RxProductModel(this);
      setModel(pm);
      
      super.initialize();
           
      rxAdd(new Label(RxInstallerProperties.getResources().getString(
            "productPanelDesc")));
   }
}
