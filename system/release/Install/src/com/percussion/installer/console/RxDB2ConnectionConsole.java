/******************************************************************************
 *
 * [ RxDB2ConnectionConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installer.model.RxDB2ConnectionModel;
import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAPreviousRequestException;


/**
 * Console Implementation of {@link RxDB2ConnectionModel}.
 */
public class RxDB2ConnectionConsole extends RxIAConsole
{
   @Override
   protected void execute() throws RxIAPreviousRequestException
   {
      // assert safe cast
      RxDB2ConnectionModel dM = getDataModel();
      
      String strServerLabel = RxInstallerProperties.getResources().getString(
      "databaseServerLabel.db2");
      
      String serverStr = getConsoleUtils().promptAndGetValueWithDefaultValue(
            strServerLabel, dM.getDBServer());
      
      if (serverStr != null)
         dM.setDBServer(serverStr);
   }
   
   @Override
   protected void initialize()
   {
      super.initialize();
      
      RxDB2ConnectionModel cm = new RxDB2ConnectionModel(this);
      setModel(cm);
   }
   
   /**
    * The data model stores user input captured by this console.
    * 
    * @return the data model for this console.
    */
   private RxDB2ConnectionModel getDataModel()
   {
      return (RxDB2ConnectionModel)getModel();
   }
}
