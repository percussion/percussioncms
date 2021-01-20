/******************************************************************************
 *
 * [ RxSettingsPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;


import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installer.model.RxSettingsModel;

import java.awt.Label;
import java.awt.TextField;

/**
 * AWT Implementation of {@link RxSettingsModel}.
 */
public class RxSettingsPanel extends RxIAPanel
{
   @Override
   public void initialize()
   {
      super.initialize();
      
      RxSettingsModel sm = new RxSettingsModel(this);
      setModel(sm);
      
      rxAdd(new Label(RxInstallerProperties.getString(
            "rhythmyxSettingsPanel.NamingServicePort.label")));

      rxAdd(m_txtNamingServicePort = new TextField(
            RxSettingsModel.getNamingServicePort()));
      
      rxAdd(new Label(RxInstallerProperties.getString(
            "rhythmyxSettingsPanel.NamingServiceRMIPort.label")));

      rxAdd(m_txtNamingServiceRMIPort = new TextField(
            RxSettingsModel.getNamingServiceRMIPort()));
      
      rxAdd(new Label(RxInstallerProperties.getString(
            "rhythmyxSettingsPanel.InvokerJrmpServicePort.label")));

      rxAdd(m_txtInvokerJrmpServicePort = new TextField(
            RxSettingsModel.getInvokerJrmpServicePort()));
      
      rxAdd(new Label(RxInstallerProperties.getString(
            "rhythmyxSettingsPanel.InvokerPooledServicePort.label")));

      rxAdd(m_txtInvokerPooledServicePort = new TextField(
            RxSettingsModel.getInvokerPooledServicePort()));
      
      rxAdd(new Label(RxInstallerProperties.getString(
            "rhythmyxSettingsPanel.UIL2ServicePort.label")));

      rxAdd(m_txtUIL2ServicePort = new TextField(
            RxSettingsModel.getUIL2ServicePort()));
      
      rxAdd(new Label(RxInstallerProperties.getString(
            "rhythmyxSettingsPanel.AJP13ServicePort.label")));

      rxAdd(m_txtAJP13ServicePort = new TextField(
            RxSettingsModel.getAJP13ServicePort()));
   }
   
   @Override
   public void entering()
   {
      m_txtNamingServicePort.setText(RxSettingsModel.getNamingServicePort());
      m_txtNamingServiceRMIPort.setText(
            RxSettingsModel.getNamingServiceRMIPort());
      m_txtInvokerJrmpServicePort.setText(
            RxSettingsModel.getInvokerJrmpServicePort());
      m_txtInvokerPooledServicePort.setText(
            RxSettingsModel.getInvokerPooledServicePort());
      m_txtUIL2ServicePort.setText(RxSettingsModel.getUIL2ServicePort());
      m_txtAJP13ServicePort.setText(RxSettingsModel.getAJP13ServicePort());
   }   
   
   @Override
   public void entered()
   {
   }
   
   @Override
   public void exiting()
   {
      RxSettingsModel data = getDataModel();
      
      data.setNamingServicePort(m_txtNamingServicePort.getText());
      data.setNamingServiceRMIPort(m_txtNamingServiceRMIPort.getText());
      data.setInvokerJrmpServicePort(m_txtInvokerJrmpServicePort.getText());
      data.setInvokerPooledServicePort(m_txtInvokerPooledServicePort.getText());
      data.setUIL2ServicePort(m_txtUIL2ServicePort.getText());
      data.setAJP13ServicePort(m_txtAJP13ServicePort.getText());
   }   
      
   @Override
   public void propertyChanged(String propName)
   {
      if (propName.equalsIgnoreCase("NamingServicePort"))
         m_txtNamingServicePort.setText(RxSettingsModel.getNamingServicePort());
      if (propName.equalsIgnoreCase("NamingServiceRMIPort"))
         m_txtNamingServiceRMIPort.setText(
               RxSettingsModel.getNamingServiceRMIPort());
      if (propName.equalsIgnoreCase("InvokerJrmpServicePort"))
         m_txtInvokerJrmpServicePort.setText(
               RxSettingsModel.getInvokerJrmpServicePort());
      if (propName.equalsIgnoreCase("InvokderPooledServicePort"))
         m_txtInvokerPooledServicePort.setText(
               RxSettingsModel.getInvokerPooledServicePort());
      if (propName.equalsIgnoreCase("UIL2ServicePort"))
         m_txtUIL2ServicePort.setText(RxSettingsModel.getUIL2ServicePort());
      if (propName.equalsIgnoreCase("AJP13ServicePort"))
         m_txtAJP13ServicePort.setText(RxSettingsModel.getAJP13ServicePort());
   }
   
   /**
    * Helper method to access the model i.e. retrieve {@link RxSettingsModel}.
    * 
    * @return the data model for this panel.
    */
   private RxSettingsModel getDataModel()
   {
      return (RxSettingsModel) getModel();
   }
   
   /*************************************************************************
   * UI Component Variables
   *************************************************************************/

   /**
   * The naming service port text component.
   */
   private TextField m_txtNamingServicePort = null;
   
   /**
    * The naming service rmi port text component.
    */
   private TextField m_txtNamingServiceRMIPort = null;
   
   /**
    * The invoker jrmp service port text component.
    */
   private TextField m_txtInvokerJrmpServicePort = null;
   
   /**
    * The invoker pooled service port text component.
    */
   private TextField m_txtInvokerPooledServicePort = null;
   
   /**
    * The uil2 service port text component.
    */
   private TextField m_txtUIL2ServicePort = null;
   
   /**
    * The ajp13 service port text component.
    */
   private TextField m_txtAJP13ServicePort = null;
}
