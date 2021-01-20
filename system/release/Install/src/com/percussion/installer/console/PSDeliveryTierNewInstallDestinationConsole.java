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
import com.percussion.installer.model.PSDeliveryTierNewInstallDestinationModel;
import com.percussion.installer.model.RxNewInstallDestinationModel;


/**
 * Console Implementation of {@link RxNewInstallDestinationModel}.
 */
public class PSDeliveryTierNewInstallDestinationConsole extends RxIAConsole
{
   @Override
   protected void execute() throws RxIAPreviousRequestException
   {
      PSDeliveryTierNewInstallDestinationModel model = getDataModel();
      
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
      
      PSDeliveryTierNewInstallDestinationModel nidm = new PSDeliveryTierNewInstallDestinationModel(
            this);
      setModel(nidm);
   }
   
   /**
    * The data model stores user input captured by this console.
    * 
    * @return the data model for this console.
    */
   private PSDeliveryTierNewInstallDestinationModel getDataModel()
   {
      return (PSDeliveryTierNewInstallDestinationModel) getModel();
   }
}
