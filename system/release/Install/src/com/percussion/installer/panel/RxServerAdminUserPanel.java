/******************************************************************************
 *
 * [ RxServerPropertiesPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import java.awt.Label;
import java.awt.TextField;

import javax.swing.JTextArea;

import com.percussion.install.InstallUtil;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installer.model.RxServerAdminUserModel;
import com.percussion.installer.model.RxServerPropertiesModel;
import com.percussion.util.PSOsTool;


/**
 * AWT Implementation of {@link RxServerAdminUserModel}.
 */
public class RxServerAdminUserPanel extends RxIAPanel
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();
      
      RxServerAdminUserModel aum = new RxServerAdminUserModel(this);
      setModel(aum);
      
      JTextArea note = new JTextArea(RxInstallerProperties.getString(
      "authDesc"));
      note.setBackground(super.getBackground());
      note.setEditable(false);
      note.setLineWrap(true);
      note.setWrapStyleWord(true);
      rxAdd(note);
      rxAdd(2);
      
      String strAdminNameLabel = RxInstallerProperties.getResources().
      getString("adminName");
      
      rxAdd(new Label(strAdminNameLabel));
      rxAdd(m_txtAdminName);
      
      rxAdd(2);
      
      String strAdminPasswordLabel = RxInstallerProperties.getResources().
      getString("adminPassword");
      
      rxAdd(new Label(strAdminPasswordLabel));
      rxAdd(m_txtAdminPassword);
      m_txtAdminPassword.setEchoChar('*');

   }
   
   @Override
   public void entering()
   {
   }
   
   @Override
   public void entered()
   {
      m_txtAdminName.setText(getDM().getAdminUserName());
      m_txtAdminPassword.setText(getDM().getAdminUserPassword());
   }
   
   @Override
   public void exiting()
   {
      getDM().setAdminUserName(m_txtAdminName.getText());
      getDM().setAdminUserPassword(m_txtAdminPassword.getText());
   }
   
   /**
    * Helper method to access the model i.e. retrieve
    * {@link RxServerPropertiesModel}.
    * 
    * @return the data model for this panel.
    */
   private RxServerAdminUserModel getDM()
   {
      return (RxServerAdminUserModel) getModel();
   }
   
   /**
    * The service name text component.
    */
   private TextField m_txtAdminName = new TextField();
   
   /**
    * The service description text component.
    */
   private TextField m_txtAdminPassword = new TextField();
}
