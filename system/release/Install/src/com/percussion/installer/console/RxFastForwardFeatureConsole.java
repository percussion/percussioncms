/******************************************************************************
 *
 * [ RxFastForwardFeatureConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installer.model.RxFastForwardFeatureModel;


/**
 * The console implementation used to gather Rhythmyx FastForward feature
 * installation selections.
 */
public class RxFastForwardFeatureConsole extends RxComponentConsole
{
   @Override
   public void initialize()
   {
      RxFastForwardFeatureModel fffm = new RxFastForwardFeatureModel(this);
      setModel(fffm);
      
      super.initialize();
   }
   
   @Override
   public String getPrompt()
   {
      return RxInstallerProperties.getResources().getString(
      "featureConsoleDesc");
   }
}
