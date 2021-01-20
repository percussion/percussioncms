/******************************************************************************
 *
 * [ RxPubDocsPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installer.model.RxPubDocsModel;

import java.awt.Label;
import java.awt.TextField;
import javax.swing.JTextArea;


/**
 * AWT Implementation of {@link RxPubDocsModel}.
 */
public class RxPubDocsPanel extends RxIAPanel
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();
      
      RxPubDocsModel pdm = new RxPubDocsModel(this);
      setModel(pdm);
      
      String strTrimRxPubDocsOptionLabel = RxInstallerProperties.getResources().
      getString("trimRxPubDocsOptionDesc");
      
      JTextArea desc = new JTextArea(strTrimRxPubDocsOptionLabel);
      desc.setEditable(false);
      desc.setLineWrap(true);
      desc.setWrapStyleWord(true);
      rxAdd(desc);
          
      // Date:
      rxAdd(1);
      rxAdd(new Label(RxInstallerProperties.getResources().getString(
            "removePubHistDateLabel")));
      rxAdd(m_txtDate = new TextField("", 10));
   }
   
   @Override
   public void entering()
   {
   }
   
   @Override
   public void entered()
   {
      m_txtDate.setText(RxPubDocsModel.getDate());
   }
   
   @Override
   public void exiting()
   {
      getDM().setDate(m_txtDate.getText());
   }
   
   @Override
   public void propertyChanged(String propName)
   {
      super.propertyChanged(propName);
      
      if (propName.compareTo("Date") == 0)
         m_txtDate.setText(RxPubDocsModel.getDate());
   }
   
   /**
    * Helper method to access the model i.e. retrieve {@link RxPubDocsModel}.
    * 
    * @return the data model for this panel.
    */
   private RxPubDocsModel getDM()
   {
      return (RxPubDocsModel) getModel();
   }
   
   /**
    * UI component for the trim date.
    */
   private TextField m_txtDate;
}
