/******************************************************************************
 *
 * [ RxInstallOptionPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;


import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installer.model.RxInstallOptionModel;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;


/**
 * AWT Implementation of {@link RxInstallOptionModel}.
 */
public class RxInstallOptionPanel extends RxIAPanel
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();
      
      String lblNew =
         RxInstallerProperties.getResources().getString("newRxInstall");
      String lblUpg =
         RxInstallerProperties.getResources().getString("upgradeRxInstall");
      
      m_checkGroup = new CheckboxGroup();
      
      rxAdd(m_checkNew = new Checkbox(lblNew, m_checkGroup, true));
      
      rxAdd(m_checkUpgrade = new Checkbox(lblUpg, m_checkGroup, false));
      
      RxInstallOptionModel iom = new RxInstallOptionModel(this);
      setModel(iom);
   }
   
   @Override
   protected void entered()
   {
   }
   
   @Override
   protected void exiting()
   {
      RxInstallOptionModel data = getDataModel();
      
      if (m_checkNew.equals(m_checkGroup.getSelectedCheckbox()))
         data.setInstallType(RxInstallOptionModel.RX_INSTALL_NEW);
      else if (m_checkUpgrade.equals(m_checkGroup.getSelectedCheckbox()))
         data.setInstallType(RxInstallOptionModel.RX_INSTALL_UPGRADE);
   }   
   
   @Override
   protected void entering()
   {
      m_checkGroup.setSelectedCheckbox(getDataModel().checkUpgradeInstall() ? m_checkUpgrade : m_checkNew);
   }
   
   /**
    * The data model stores user input captured by this panel.
    * 
    * @return the data model for this panel.
    */
   private RxInstallOptionModel getDataModel()
   {
      return (RxInstallOptionModel) getModel();
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
    * Checkbox for the new installation option, initialized in the
    * <code>initialize()</code> method, never <code>null</code> or modified
    * after that.
    */
   private Checkbox m_checkNew = null;
   
   /**
    * Checkbox for the upgrade installation option, initialized in the
    * <code>initialize()</code> method, never <code>null</code> or modified
    * after that.
    */
   private Checkbox m_checkUpgrade = null;
}
