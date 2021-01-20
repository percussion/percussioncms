/******************************************************************************
 *
 * [ RxDatabaseDriverLocationConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.installer.model.RxDatabaseDriverLocationModel;


/**
 * Console Implementation of {@link RxDatabaseDriverLocationModel}.
 */
public class RxDatabaseDriverLocationConsole extends RxIAConsole
{
   @Override
   protected void execute() throws RxIAPreviousRequestException
   {
      RxDatabaseDriverLocationModel model = getDataModel();
      
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
      
      RxDatabaseDriverLocationModel ddlm = new RxDatabaseDriverLocationModel(
            this);
      setModel(ddlm);
   }
   
   /**
    * The data model stores user input captured by this console.
    * 
    * @return the data model for this console.
    */
   private RxDatabaseDriverLocationModel getDataModel()
   {
      return (RxDatabaseDriverLocationModel) getModel();
   }
}
