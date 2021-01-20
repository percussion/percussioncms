/******************************************************************************
 *
 * [ RxProtocolPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installer.model.PSDeliveryTierProtocolModel;


import java.awt.Choice;
import java.awt.Label;


/**
 * AWT Implementation of {@link PSDeliveryTierProtocolModel}.
 */
public class PSDeliveryTierProtocolPanel extends RxIAPanel
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();
      
      PSDeliveryTierProtocolModel pm = new PSDeliveryTierProtocolModel(this);
      setModel(pm);
      
      String label = RxInstallerProperties.getString("subProtocol");
      rxAdd(new Label(label));
      
      m_chSubProtocol = new Choice();
      
      for (String option : getDM().getDriverOptions())
      {
         if (option.equalsIgnoreCase(
               RxInstallerProperties.getResources().getString("embedded.name")))
         {
            m_chSubProtocol.add(
                  RxInstallerProperties.getResources().getString("embedded"));
         }
         else
         {
            m_chSubProtocol.add(option);
         }
      }
      rxAdd(m_chSubProtocol);
   }
   
   @Override
   public void entering()
   {
   }
   
   @Override
   public void entered()
   {
      String strDriver = getDM().getSubProtocol();
      
      if ((m_chSubProtocol != null) && (strDriver != null) &&
            (strDriver.trim().length() > 0))
         
         setSelection(m_chSubProtocol, strDriver);
   }
   
   @Override
   public void exiting()
   {
      if (m_chSubProtocol.getSelectedItem().equalsIgnoreCase(
            RxInstallerProperties.getResources().getString("embedded")))
      {
         getDM().setSubProtocol(
               RxInstallerProperties.getResources().getString("embedded.name"));
      }
      else
      {
         getDM().setSubProtocol(m_chSubProtocol.getSelectedItem());
      }
   }
   
   @Override
   public void propertyChanged(String propName)
   {
      super.propertyChanged(propName);
      
      if (propName.equalsIgnoreCase("SubProtocol"))
         setSelection(m_chSubProtocol, getDM().getSubProtocol());
   }
   
   /**
    * Helper method to access the model i.e. retrieve {@link PSDeliveryTierProtocolModel}.
    * 
    * @return the data model for this panel.
    */
   private PSDeliveryTierProtocolModel getDM()
   {
      return (PSDeliveryTierProtocolModel) getModel();
   }
   
   /**
    * Combo box for selecting the type of jdbc driver to use for connecting with
    * the Rhythmyx repository.  Initialized in the <code>initialize()</code>
    * method, never <code>null</code> or modified after that.
    */
   private Choice m_chSubProtocol = null;
}
