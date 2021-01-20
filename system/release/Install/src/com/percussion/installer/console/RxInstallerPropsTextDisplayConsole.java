/******************************************************************************
 *
 * [ RxInstallerPropsTextDisplayConsole.java ]
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
import com.percussion.installanywhere.RxIAConsoleUtils;
import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.installanywhere.RxIAUtils;
import com.percussion.installer.model.RxInstallerPropsTextDisplayModel;


/**
 * Console Implementation of {@link RxInstallerPropsTextDisplayModel}.
 */
public class RxInstallerPropsTextDisplayConsole extends RxIAConsole
{
   @Override     
   public void execute() throws RxIAPreviousRequestException
   {
      // assert safe cast
      RxInstallerPropsTextDisplayModel dM = 
         (RxInstallerPropsTextDisplayModel) getModel();

      RxIAConsoleUtils cu = getConsoleUtils();
      
      String installerProp = getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), dM.getPropertyNameVar()));
      String resolvedMsg = RxIAUtils.resolve(getVariableAccess(),
            RxInstallerProperties.getString(installerProp));
      cu.wprintln(resolvedMsg);
      cu.newline();
      
      String navigationOpts = getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), dM.getNavigationOptionsVar()));
      
      if (navigationOpts.indexOf(RxIAPanel.NAV_OPT_NEXT) != -1 &&
            navigationOpts.indexOf(RxIAPanel.NAV_OPT_PREVIOUS) != -1)
         cu.enterToContinue();
      else if (navigationOpts.indexOf(RxIAPanel.NAV_OPT_NEXT) != -1 &&
            navigationOpts.indexOf(RxIAPanel.NAV_OPT_PREVIOUS) == -1)
         cu.enterToContinuePrevDisabled();
      else if (navigationOpts.indexOf(RxIAPanel.NAV_OPT_NEXT) == -1 &&
            navigationOpts.indexOf(RxIAPanel.NAV_OPT_PREVIOUS) != -1)
         promptAndAbort(true);
      else
         promptAndAbort(false);
   }
   
   @Override
   protected void initialize()
   {
      super.initialize();
      
      RxInstallerPropsTextDisplayModel iptdm = 
         new RxInstallerPropsTextDisplayModel(this);
      setModel(iptdm);
   }
}
