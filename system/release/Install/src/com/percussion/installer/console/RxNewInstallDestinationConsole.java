/******************************************************************************
 *
 * [ RxNewInstallDestinationConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.installer.model.RxNewInstallDestinationModel;


/**
 * Console Implementation of {@link RxNewInstallDestinationModel}.
 */
public class RxNewInstallDestinationConsole extends RxIAConsole
{
   @Override
   protected void execute() throws RxIAPreviousRequestException
   {
      RxNewInstallDestinationModel model = getDataModel();
      
      String dir =  
            getConsoleUtils().promptAndGetValueWithDefaultValue(
               "Enter a directory:", model.getDestDir());
           
      if (dir != null)
         model.setDestDir(dir);
   }
   
   @Override
   protected void initialize()
   {
      super.initialize();
      
      RxNewInstallDestinationModel nidm = new RxNewInstallDestinationModel(
            this);
      setModel(nidm);
   }
   
   /**
    * The data model stores user input captured by this console.
    * 
    * @return the data model for this console.
    */
   private RxNewInstallDestinationModel getDataModel()
   {
      return (RxNewInstallDestinationModel) getModel();
   }
}
