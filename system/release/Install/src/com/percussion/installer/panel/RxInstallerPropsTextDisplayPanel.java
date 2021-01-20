/******************************************************************************
 *
 * [ RxInstallerPropsTextDisplayPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.installanywhere.RxIAUtils;
import com.percussion.install.InstallUtil;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installer.model.RxInstallerPropsTextDisplayModel;


/**
 * AWT Implementation of {@link RxInstallerPropsTextDisplayModel}.
 */
public class RxInstallerPropsTextDisplayPanel extends RxTextDisplayPanel
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();
      
      RxInstallerPropsTextDisplayModel iptdm = 
         new RxInstallerPropsTextDisplayModel(this);
      setModel(iptdm);
   }
   
   @Override
   public void entering()
   {
   }
   
   @Override
   public void entered()
   {
      RxInstallerPropsTextDisplayModel dM = 
         (RxInstallerPropsTextDisplayModel) getModel();
      
      String installerProp = getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), dM.getPropertyNameVar()));
      String resolvedMsg = RxIAUtils.resolve(getVariableAccess(),
            RxInstallerProperties.getString(installerProp));
      
      setText(resolvedMsg);
      
      String navigationOpts = getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), dM.getNavigationOptionsVar()));
      setNxtButtonEnabled(navigationOpts.indexOf(NAV_OPT_NEXT) != -1);
      setPreviousButtonEnabled(navigationOpts.indexOf(NAV_OPT_PREVIOUS) != -1);
   }

   @Override
   public void exiting()
   {
   }
}
