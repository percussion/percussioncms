/******************************************************************************
 *
 * [ RxUpgradePluginsConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.installer.model.RxUpgradePluginsModel;


/**
 * Console Implementation of {@link RxUpgradePluginsModel}.
 */
public class RxUpgradePluginsConsole extends RxIAConsole
{
   @SuppressWarnings("unused")
   @Override     
   public void execute() throws RxIAPreviousRequestException
   {
      // assert safe cast
      RxUpgradePluginsModel dM = (RxUpgradePluginsModel) getModel();

      getConsoleUtils().wprintln(dM.getDisplayMessage());
      getConsoleUtils().newline();
      getConsoleUtils().enterToContinuePrevDisabled();
   }
   
   @Override
   protected void initialize()
   {
      super.initialize();
      
      RxUpgradePluginsModel upm = new RxUpgradePluginsModel(this);
      setModel(upm);
   }
}
