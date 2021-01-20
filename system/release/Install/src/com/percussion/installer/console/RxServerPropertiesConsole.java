/******************************************************************************
 *
 * [ RxServerPropertiesConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installer.RxVariables;
import com.percussion.installer.model.RxServerPropertiesModel;
import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.util.PSOsTool;


/**
 * Console Implementation of {@link RxServerPropertiesModel}.
 */
public class RxServerPropertiesConsole extends RxIAConsole
{
   @Override     
   public void execute() throws RxIAPreviousRequestException
   {
      // assert safe cast
      RxServerPropertiesModel dM = getDataModel();

      String response = null;
      if (PSOsTool.isWindowsPlatform())
      {
         String strSvcNameLabel = RxInstallerProperties.getResources().
         getString("rhythmyxSvcName");
         
         String strSvcDescLabel = RxInstallerProperties.getResources().
         getString("rhythmyxSvcDesc");
         
         response = getConsoleUtils().promptAndGetValueWithDefaultValue(
               strSvcNameLabel, dM.getRhythmyxSvcName());
         
         if (response != null)
            dM.setRhythmyxSvcName(response);
         
         response = getConsoleUtils().promptAndGetValueWithDefaultValue(
               strSvcDescLabel, dM.getRhythmyxSvcDesc());
         
         if (response != null)
            dM.setRhythmyxSvcDesc(response);
      }
      
      String strPortLabel = RxInstallerProperties.getString(
      "RxPortPanelBean.BindPort.label");
      
      response = getConsoleUtils().promptAndGetValueWithDefaultValue(
            strPortLabel, dM.getBindPort());
      
      if (response != null)
         dM.setBindPort(response);
      
      getConsoleUtils().wprintln(RxInstallerProperties.getString(
            "additionalPortsNote"));
      
      boolean bresponse = getConsoleUtils().promptAndYesNoChoice(
            RxInstallerProperties.getString("customizePorts"));
      
      if (bresponse)
         setInstallValue(RxVariables.RX_CUSTOMIZE_PORTS, "true");
      else
         setInstallValue(RxVariables.RX_CUSTOMIZE_PORTS, "false");
   }

   @Override
   protected void initialize()
   {
      super.initialize();
      
      RxServerPropertiesModel spm = new RxServerPropertiesModel(this);
      setModel(spm);
   }
   
   /**
    * Helper method to access the model i.e. retrieve
    * {@link RxServerPropertiesModel}.
    * 
    * @return the data model for this console.
    */
   private RxServerPropertiesModel getDataModel()
   {
      return (RxServerPropertiesModel) getModel();
   }
}
