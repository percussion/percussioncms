/******************************************************************************
 *
 * [ RxDB2ConnectionPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installer.model.RxDB2ConnectionModel;
import com.percussion.installanywhere.RxIAPanel;

import java.awt.Label;
import java.awt.TextField;


/**
 * AWT Implementation of the RxDB2Connectionmodel.
 */
public class RxDB2ConnectionPanel extends RxIAPanel
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();

      RxDB2ConnectionModel cm = new RxDB2ConnectionModel(this);
      setModel(cm);
      
      String strServerLabel = RxInstallerProperties.getResources().getString(
            "databaseServerLabel.db2");

      // Database Server:
      rxAdd(new Label(strServerLabel));
      rxAdd(m_serverTxt = new TextField("", 10));
   }

   @Override
   public void entering()
   {
   }
   
   @Override
   public void entered()
   {
      m_serverTxt.setText(getDM().getDBServer());
   }

   @Override
   public void exiting()
   {
      getDM().setDBServer(m_serverTxt.getText());
   }

   @Override
   public void propertyChanged(String propName)
   {
      super.propertyChanged(propName);

      if ( propName.compareTo("Server") == 0 )
         m_serverTxt.setText(getDM().getDBServer());
   }

   /**
    * The data model stores user input captured by this panel.
    * 
    * @return the data model for this panel.
    */
   private RxDB2ConnectionModel getDM()
   {
      return (RxDB2ConnectionModel) getModel();
   }

   /**
    * UI components.
    */
   private TextField m_serverTxt;
}
