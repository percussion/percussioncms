/******************************************************************************
 *
 * [ RxPubDocsConsole.java ]
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
import com.percussion.installer.model.RxPubDocsModel;


/**
 * Console Implementation of {@link RxPubDocsModel}.
 */
public class RxPubDocsConsole extends RxIAConsole
{
   @Override     
   public void execute() throws RxIAPreviousRequestException
   {
      RxPubDocsModel dM = getDataModel();
      
      String strTrimRxPubDocsOptionLabel = RxInstallerProperties.getResources().
      getString("trimRxPubDocsOptionDesc");
      
      getConsoleUtils().wprintln(strTrimRxPubDocsOptionLabel);
      getConsoleUtils().wprintln("");
      
      String response = getConsoleUtils().promptAndGetValueWithDefaultValue(
            RxInstallerProperties.getResources().getString(
                  "removePubHistDateLabel"), RxPubDocsModel.getDate());
      
      if (response != null)
         dM.setDate(response);
   }
   
   @Override
   protected void initialize()
   {
      super.initialize();
      
      RxPubDocsModel pdm = new RxPubDocsModel(this);
      setModel(pdm);
   }
   
   /**
    * Helper method to access the model i.e. retrieve {@link RxPubDocsModel}.
    * 
    * @return the data model for this console.
    */
   private RxPubDocsModel getDataModel()
   {
      return (RxPubDocsModel) getModel();
   }
}
