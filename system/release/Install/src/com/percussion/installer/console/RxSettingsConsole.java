/******************************************************************************
 *
 * [ RxSettingsConsole.java ]
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
import com.percussion.installer.model.RxSettingsModel;


/**
 * Console Implementation of {@link RxSettingsModel}.
 */
public class RxSettingsConsole extends RxIAConsole
{
   @Override     
   public void execute()throws RxIAPreviousRequestException
   {
      RxSettingsModel data = getDataModel();
      
      String response = getConsoleUtils().promptAndGetValueWithDefaultValue(
           RxInstallerProperties.getString(
                  "rhythmyxSettingsPanel.NamingServicePort.label"),
                  RxSettingsModel.getNamingServicePort());
      
      if (response != null)
         data.setNamingServicePort(response);
      
      response = getConsoleUtils().promptAndGetValueWithDefaultValue(
            RxInstallerProperties.getString(
                  "rhythmyxSettingsPanel.NamingServiceRMIPort.label"),
                  RxSettingsModel.getNamingServiceRMIPort());
      
      if (response != null)
         data.setNamingServiceRMIPort(response);
      
      response = getConsoleUtils().promptAndGetValueWithDefaultValue(
            RxInstallerProperties.getString(
                  "rhythmyxSettingsPanel.InvokerJrmpServicePort.label"),
                  RxSettingsModel.getInvokerJrmpServicePort());
      data.setInvokerJrmpServicePort(response);
      
      response = getConsoleUtils().promptAndGetValueWithDefaultValue(
            RxInstallerProperties.getString(
                  "rhythmyxSettingsPanel.InvokerPooledServicePort.label"),
                  RxSettingsModel.getInvokerPooledServicePort());
      data.setInvokerPooledServicePort(response);
      
      response = getConsoleUtils().promptAndGetValueWithDefaultValue(
            RxInstallerProperties.getString(
                  "rhythmyxSettingsPanel.UIL2ServicePort.label"),
                  RxSettingsModel.getUIL2ServicePort());
      data.setUIL2ServicePort(response);
      
      response = getConsoleUtils().promptAndGetValueWithDefaultValue(
            RxInstallerProperties.getString(
                  "rhythmyxSettingsPanel.AJP13ServicePort.label"),
                  RxSettingsModel.getAJP13ServicePort());
      data.setAJP13ServicePort(response);
   }
   
   @Override
   protected void initialize()
   {
      super.initialize();
      
      RxSettingsModel sm = new RxSettingsModel(this);
      setModel(sm);
   }
   
   /**
    * Helper method to access the model i.e. retrieve {@link RxSettingsModel}.
    * 
    * @return the data model for this console.
    */
   private RxSettingsModel getDataModel()
   {
      return (RxSettingsModel) getModel();
   }
}
