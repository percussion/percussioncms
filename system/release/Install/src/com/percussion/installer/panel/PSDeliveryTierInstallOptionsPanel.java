/******************************************************************************
 *
 * COPYRIGHT (c) 1999 - 2012 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installer.model.PSDeliveryTierInstallOptionModel;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import javax.swing.JTextArea;

/***
 * Displays the panel for selecting a new or upgrade install.
 * 
 * @author Nate
 *
 */
@SuppressWarnings("serial")
public class PSDeliveryTierInstallOptionsPanel extends RxIAPanel
{
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
   
   /**
    * Label that is used to display a message indicating that CMS was detected
    * in an upgrade scenario.
    */
   private JTextArea m_cm1_detected = null;
   
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();
      
      
      m_checkGroup = new CheckboxGroup();
      
      rxAdd(m_checkNew = new Checkbox(
            RxInstallerProperties.getResources().getString("newRxInstall"), 
            m_checkGroup, 
            true));
      
      rxAdd(m_checkUpgrade = new Checkbox(
            RxInstallerProperties.getResources().getString("upgradeRxInstall"),
            m_checkGroup, 
            false));
      
      rxAdd(m_cm1_detected = new JTextArea(
            RxInstallerProperties.getResources().getString("dts.cm1InstallDetected")));
      
      m_cm1_detected.setEditable(false);
      m_cm1_detected.setLineWrap(true);
      m_cm1_detected.setWrapStyleWord(true);
      m_cm1_detected.setBackground(this.getBackground());
      
      PSDeliveryTierInstallOptionModel iom = new PSDeliveryTierInstallOptionModel(this);
      setModel(iom);
   }
   
   @Override
   protected void entered()
   {
   }
   
   @Override
   protected void exiting()
   {
      PSDeliveryTierInstallOptionModel data = getDataModel();
      
      if (m_checkNew.equals(m_checkGroup.getSelectedCheckbox()))
         data.setInstallType(PSDeliveryTierInstallOptionModel.INSTALL_NEW);
      else if (m_checkUpgrade.equals(m_checkGroup.getSelectedCheckbox()))
         data.setInstallType(PSDeliveryTierInstallOptionModel.INSTALL_UPGRADE);
   }   
   
   @Override
   protected void entering()
   {
      boolean upgrade = PSDeliveryTierInstallOptionModel.checkUpgradeInstall();
      m_checkGroup.setSelectedCheckbox( upgrade ? m_checkUpgrade : m_checkNew);
      
      //hide the upgrade label on new, show the label on upgrade
     if(upgrade)
        m_cm1_detected.setVisible(true);
     else
        m_cm1_detected.setVisible(false);
   }
   
   /**
    * The data model stores user input captured by this panel.
    * 
    * @return the data model for this panel.
    */
   private PSDeliveryTierInstallOptionModel getDataModel()
   {
      return (PSDeliveryTierInstallOptionModel) getModel();
   }
   


}
