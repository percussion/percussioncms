/******************************************************************************
 *
 * [ RxServerConnectionConsole.java ]
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
import com.percussion.installer.model.PSDeliveryTierServerConnectionModel;


/**
 * Console Implementation of {@link PSDeliveryTierServerConnectionModel}.
 */
public class PSDeliveryTierServerConnectionConsole extends RxIAConsole
{
   @Override     
   public void execute()throws RxIAPreviousRequestException
   {
      // assert safe cast
      PSDeliveryTierServerConnectionModel dM = getDM();
     
      String strServer = dM.getDBServer(); 
      if (strServer == null || strServer.length() == 0)
         strServer = dM.getServerTextStr();
      String response = getConsoleUtils().promptAndGetValueWithDefaultValue(
            dM.getServerLabelExampleString(), strServer);
      
      if (response != null)
         dM.setDBServer(response);
      
      response = getConsoleUtils().promptAndGetValueWithDefaultValue(
            RxInstallerProperties.getString("user"), dM.getUSER());

      if (response != null)
         dM.setUSER(response);
      response = getConsoleUtils().promptAndGetSensitiveInformation(RxInstallerProperties.getString("password"));
      
      if (response != null)
         dM.setPWD(response);
   }
   
   @Override
   protected void initialize()
   {
      super.initialize();
      
      PSDeliveryTierServerConnectionModel scm = new PSDeliveryTierServerConnectionModel(this);
      setModel(scm);
   }
   
   /**
    * Helper method to access the model i.e. retrieve
    * {@link PSDeliveryTierServerConnectionModel}.
    * 
    * @return the data model for this console.
    */
   private PSDeliveryTierServerConnectionModel getDM()
   {
      return (PSDeliveryTierServerConnectionModel) getModel();
   }
}
