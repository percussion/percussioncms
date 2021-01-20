/******************************************************************************
 *
 * [ RxDevToolsFeatureConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installer.model.RxDevToolsFeatureModel;


/**
 * The console implementation used to gather Rhythmyx development tools feature
 * installation selections.
 */
public class RxDevToolsFeatureConsole extends RxComponentConsole
{
   @Override
   public void initialize()
   {
      RxDevToolsFeatureModel dtfm = new RxDevToolsFeatureModel(this);
      setModel(dtfm);
      
      super.initialize();
   }
   
   @Override
   public String getPrompt()
   {
      return RxInstallerProperties.getResources().getString(
      "featureConsoleDesc");
   }
}
