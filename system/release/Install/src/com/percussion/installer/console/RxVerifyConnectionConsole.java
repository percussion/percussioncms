/******************************************************************************
 *
 * [ RxVerifyConnectionConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.installer.model.RxVerifyConnectionModel;


/**
 * Console Implementation of {@link RxVerifyConnectionModel}.
 */
public class RxVerifyConnectionConsole extends RxIAConsole
{
   @Override     
   public void execute() throws RxIAPreviousRequestException
   {
      getConsoleUtils().wprintln(
            ((RxVerifyConnectionModel) getModel()).getErrorMsg());
      getConsoleUtils().newline();
      
      promptAndAbort(true);
   }
   
   @Override
   protected void initialize()
   {
      super.initialize();
      
      RxVerifyConnectionModel vcm = new RxVerifyConnectionModel(this);
      setModel(vcm);
   }
}
