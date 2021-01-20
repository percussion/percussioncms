/******************************************************************************
 *
 * [ RxServerConnectionPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installer.model.RxServerConnectionModel;

import java.awt.Label;
import java.awt.TextField;


/**
 * AWT Implementation of {@link RxServerConnectionModel}.
 */
public class RxServerConnectionPanel extends RxIAPanel
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();
      
      RxServerConnectionModel scm = new RxServerConnectionModel(this);
      setModel(scm);
      getDM();
      
      // Database Server:
      rxAdd(m_lblServer = new Label(RxInstallerProperties.getString(
      "databaseServer")));
      rxAdd(m_DBServer = new TextField());
      
      // Login User Name/ID:
      rxAdd(3);
      rxAdd(new Label(RxInstallerProperties.getString("user")));
      rxAdd(m_USER = new TextField());
      
      //PWD:
      rxAdd(3);
      rxAdd(new Label(RxInstallerProperties.getString("password")));
      rxAdd(m_PWD = new TextField());
      m_PWD.setEchoChar('*');
   }
   
   @Override
   public void entering()
   {
   }
   
   @Override
   public void entered()
   {
      String strServer = getDM().getDBServer();
      String strUser   = getDM().getUSER();
      String pwd       = "";
      m_lblServer.setText(getDM().getServerLabelExampleString());
      
      if (strServer == null || strServer.length() == 0)
         m_DBServer.setText(getDM().getServerTextStr());
      m_USER.setText((strUser == null) ? "" : strUser);
      m_PWD.setText((pwd == null) ? "" : pwd);
   }
   
   @Override
   public void exiting()
   {
      getDM().setDBServer(m_DBServer.getText());
      getDM().setUSER(m_USER.getText());
      getDM().setPWD(m_PWD.getText());
   }
   
   @Override
   public void propertyChanged(String propName)
   {
      super.propertyChanged(propName);
      if (propName.equalsIgnoreCase("RxDBServer"))
         m_DBServer.setText(getDM().getDBServer());
      else if (propName.equalsIgnoreCase("RxUSER"))
         m_USER.setText(getDM().getUSER());
      else if (propName.equalsIgnoreCase("RxPWD"))
         m_PWD.setText(getDM().getPWD());    
   }
   
   /**
    * Cache the model since it is used heavily.
    * 
    * @return the server connection model for this panel.
    */
   private RxServerConnectionModel getDM()
   {
      if (m_dM == null )
         m_dM = (RxServerConnectionModel) getModel();
      return m_dM;
   }
   
   /**
    * Database server url ui component.
    */
   private TextField m_DBServer;
   
   /**
    * Database user login id ui component.
    */
   private TextField m_USER;
   
   /**
    * Database user login password ui component.
    */
   private TextField m_PWD;
   
   /**
    * Example database server url ui component.
    */
   private Label m_lblServer;
   
   /**
    * Maintains the server connection model for this panel, never
    * <code>null</code> after {@link #getDM()} is called.
    */
   private static RxServerConnectionModel m_dM  = null;
}
