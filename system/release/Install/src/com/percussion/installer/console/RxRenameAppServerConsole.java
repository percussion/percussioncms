/******************************************************************************
 *
 * [ RxRenameAppServerConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.installer.model.RxRenameAppServerModel;


/**
 * Console Implementation of {@link RxRenameAppServerModel}.
 */
public class RxRenameAppServerConsole extends RxIAConsole
{
   @Override     
   public void execute() throws RxIAPreviousRequestException
   {
      String text = RxInstallerProperties.getString("renameAppServerMsg");
      getConsoleUtils().wprintln(text);
      getConsoleUtils().newline();
      getConsoleUtils().enterToContinue();
   }
   
   @Override
   protected void initialize()
   {
      super.initialize();
      
      RxRenameAppServerModel rasm = new RxRenameAppServerModel(this);
      setModel(rasm);
   }
}
