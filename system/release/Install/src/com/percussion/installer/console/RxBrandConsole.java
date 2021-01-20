/******************************************************************************
 *
 * [ RxBrandConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.install.InstallUtil;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.installer.model.RxBrandModel;


/**
 * Console Implementation of {@link RxBrandModel}.
 */
public class RxBrandConsole extends RxIAConsole
{
   @Override
   protected void execute() throws RxIAPreviousRequestException
   {
      // assert safe cast
      RxBrandModel model = getDataModel();
     
      String defaultLicense = model.getLicense();
      if (defaultLicense == null)
         defaultLicense = "";
      
      String lic = getConsoleUtils().promptAndGetValueWithDefaultValue(
         RxInstallerProperties.getResources().getString("license"),
         defaultLicense);
         
      model.setLicense(lic);         
      
      String defaultCode = model.getBrandCode();
      if (defaultCode == null)
         defaultCode = "";
      
      String prod = getConsoleUtils().promptAndGetValueWithDefaultValue(
         RxInstallerProperties.getResources().getString("productcode"),
         defaultCode);
         
      model.setBrandCode(prod);
   }
   
   @Override
   protected void initialize()
   {
      super.initialize();
      
      RxBrandModel bm = new RxBrandModel(this);
      setModel(bm);
      
      getDataModel().setIsBrander(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), RxBrandModel.IS_BRANDER_VAR)).
            equalsIgnoreCase("true"));
   }
   
   /**
    * The data model stores user input captured by this console.
    * 
    * @return the data model for this console.
    */
   private RxBrandModel getDataModel()
   {
      return (RxBrandModel)getModel();
   }
}
