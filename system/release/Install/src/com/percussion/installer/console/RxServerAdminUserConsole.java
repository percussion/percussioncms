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
import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.installer.model.RxServerAdminUserModel;
import com.percussion.installer.model.RxServerPropertiesModel;
import com.percussion.util.PSOsTool;


/**
 * Console Implementation of {@link RxServerPropertiesModel}.
 */
public class RxServerAdminUserConsole extends RxIAConsole
{
   @Override     
   public void execute() throws RxIAPreviousRequestException
   {
      // assert safe cast
      RxServerAdminUserModel dM = getDataModel();

      getConsoleUtils().wprintln(RxInstallerProperties.getString(
      "authDesc"));
      
      String response = null;

      String strAdminNameLabel = RxInstallerProperties.getResources().
      getString("adminName");
      
      String strAdminPasswordLabel = RxInstallerProperties.getResources().
      getString("adminPassword");
      
      response = getConsoleUtils().promptAndGetValueWithDefaultValue(
            strAdminNameLabel, dM.getAdminUserName());
      
      if (response != null)
         dM.setAdminUserName(response);
      
      response = getConsoleUtils().promptAndGetSensitiveInformation(strAdminPasswordLabel);
      
      if (response != null)
         dM.setAdminUserPassword(response);  

   }

   @Override
   protected void initialize()
   {
      super.initialize();
      
      RxServerAdminUserModel aum = new RxServerAdminUserModel(this);
      setModel(aum);
   }
   
   /**
    * Helper method to access the model i.e. retrieve
    * {@link RxServerPropertiesModel}.
    * 
    * @return the data model for this console.
    */
   private RxServerAdminUserModel getDataModel()
   {
      return (RxServerAdminUserModel) getModel();
   }
}
