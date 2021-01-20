/******************************************************************************
 *
 * [ RxProtocolConsole.java ]
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
import com.percussion.installer.model.RxProtocolModel;


/**
 * Console Implementation of {@link RxProtocolModel}.
 */
public class RxProtocolConsole extends RxIAConsole
{
   @Override
   public void execute() throws RxIAPreviousRequestException
   {
      // Console should show "Embedded" as driver option, but
      // save the actual driver name
      String embeddedName = 
         RxInstallerProperties.getResources().getString("embedded.name");
      String embedded =
         RxInstallerProperties.getResources().getString("embedded");
      String mysqlName = 
         RxInstallerProperties.getResources().getString("mysqlname");
      String mysql =
         RxInstallerProperties.getResources().getString("mysql");
      String[] driverOptions = getDM().getDriverOptions();
      for (int i = 0; i < driverOptions.length; i++)
      {
         if (driverOptions[i].equalsIgnoreCase(embeddedName))
         {
            driverOptions[i] = embedded;
            break;
         }
         else if (driverOptions[i].equalsIgnoreCase(mysqlName))
         {
            driverOptions[i] = mysql;
            break;
         }
      }
      
      String strDriver = getDM().getSubProtocol();
      if (strDriver != null)
      {
         if (strDriver.equalsIgnoreCase(embeddedName))
         {
            strDriver = embedded;
         }
         else if (strDriver.equalsIgnoreCase(mysqlName))
         {
            strDriver = mysql;
         }
      }
      
      int selectedIndex = getItemIndex(driverOptions, strDriver);
          
      int res1 = getConsoleUtils().createChoiceListAndGetValue(
            RxInstallerProperties.getString("subProtocol"), driverOptions,
            selectedIndex);
      
      String selection = driverOptions[res1];
      if (selection.equalsIgnoreCase(embedded))
      {
         selection = embeddedName;
      }
          
      getDM().setSubProtocol(selection);
   }
   
   @Override
   protected void initialize()
   {
      super.initialize();
      
      RxProtocolModel pm = new RxProtocolModel(this);
      setModel(pm);
   }
   
   /**
    * Helper method to access the model i.e. retrieve {@link RxProtocolModel}.
    * 
    * @return the data model for this console.
    */
   private RxProtocolModel getDM()
   {
      return (RxProtocolModel) getModel();
   }
}
