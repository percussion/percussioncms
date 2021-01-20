/******************************************************************************
 *
 * [ RxInstallOptionTCPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;


import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installer.model.RxInstallOptionTCModel;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;


/**
 * AWT Implementation of {@link RxInstallOptionTCModel}.
 */
public class RxInstallOptionTCPanel extends RxIAPanel
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();
      
      String lblTypical =
         RxInstallerProperties.getResources().getString("typicalRxInstall");
      String lblCustom =
         RxInstallerProperties.getResources().getString("customRxInstall");
            
      m_checkGroup = new CheckboxGroup();
      
      rxAdd(m_checkTypical = new Checkbox(lblTypical, m_checkGroup, true));
      
      rxAdd(m_checkCustom = new Checkbox(lblCustom, m_checkGroup, false));
      
      RxInstallOptionTCModel iotcm = new RxInstallOptionTCModel(this);
      setModel(iotcm);
   }
   
   @Override
   protected void entered()
   {
   }
   
   @Override
   protected void exiting()
   {
      RxInstallOptionTCModel data = getDataModel();
      
      if (m_checkTypical.equals(m_checkGroup.getSelectedCheckbox()))
         data.setInstallType(RxInstallOptionTCModel.RX_INSTALL_TYPICAL);
      else if (m_checkCustom.equals(m_checkGroup.getSelectedCheckbox()))
         data.setInstallType(RxInstallOptionTCModel.RX_INSTALL_CUSTOM);
   }   
   
   @Override
   protected void entering()
   {
   }
   
   /**
    * The data model stores user input captured by this panel.
    * 
    * @return the data model for this panel.
    */
   private RxInstallOptionTCModel getDataModel()
   {
      return (RxInstallOptionTCModel) getModel();
   }
   
   /*************************************************************************
    * UI Component Variables
    *************************************************************************/
   
   /**
    * Checkbox group for displaying the Rhythmyx installation options,
    * initialized in the <code>initialize()</code> method, never
    * <code>null</code> or modified after that.
    */
   private CheckboxGroup m_checkGroup = null;
   
   /**
    * Checkbox for the typical installation option, initialized in the
    * <code>initialize()</code> method, never <code>null</code> or modified
    * after that.
    */
   private Checkbox m_checkTypical = null;
   
   /**
    * Checkbox for the custom installation option, initialized in the
    * <code>initialize()</code> method, never <code>null</code> or modified
    * after that.
    */
   private Checkbox m_checkCustom = null;
}
