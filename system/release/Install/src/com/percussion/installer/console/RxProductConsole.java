/******************************************************************************
 *
 * [ RxProductConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installer.model.RxProductModel;


/**
 * The console implementation used to gather Rhythmyx product installation
 * selections.
 */
public class RxProductConsole extends RxComponentConsole
{
   @Override
   public void initialize()
   {
      RxProductModel pm = new RxProductModel(this);
      setModel(pm);
      
      super.initialize();
   }

   @Override
   public String getPrompt()
   {
      return RxInstallerProperties.getResources().getString(
      "productConsoleDesc");
   }
}
