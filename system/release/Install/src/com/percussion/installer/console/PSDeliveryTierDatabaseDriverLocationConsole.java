/******************************************************************************
 *
 * [ PSDeliveryTierDatabaseDriverLocationConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.installer.model.PSDeliveryTierDatabaseDriverLocationModel;


/**
 * Console Implementation of {@link PSDeliveryTierDatabaseDriverLocationModel}.
 */
public class PSDeliveryTierDatabaseDriverLocationConsole extends RxIAConsole
{
   @Override
   protected void execute() throws RxIAPreviousRequestException
   {
      PSDeliveryTierDatabaseDriverLocationModel model = getDataModel();
      
      String driver =  
            getConsoleUtils().promptAndGetValueWithDefaultValue(
               "Please enter driver location:", model.getDriverLocation());
           
      if (driver != null)
         model.setDriverLocation(driver);
   }
   
   @Override
   protected void initialize()
   {
      super.initialize();
      
      PSDeliveryTierDatabaseDriverLocationModel ddlm = new PSDeliveryTierDatabaseDriverLocationModel(
            this);
      setModel(ddlm);
   }
   
   /**
    * The data model stores user input captured by this console.
    * 
    * @return the data model for this console.
    */
   private PSDeliveryTierDatabaseDriverLocationModel getDataModel()
   {
      return (PSDeliveryTierDatabaseDriverLocationModel) getModel();
   }
}

