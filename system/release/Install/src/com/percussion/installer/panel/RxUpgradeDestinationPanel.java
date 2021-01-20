/******************************************************************************
 *
 * [ RxUpgradeDestinationPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.install.InstallUtil;
import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installer.model.RxUpgradeDestinationModel;

import java.awt.Choice;
import java.awt.Label;


/**
 * AWT Implementation of {@link RxUpgradeDestinationModel}.
 */
public class RxUpgradeDestinationPanel extends RxIAPanel
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();

      RxUpgradeDestinationModel udm = new RxUpgradeDestinationModel(this);
      setModel(udm);
      
      rxAdd(new Label("Directory:"));

      m_destinationChoiceField = new Choice();
      
      for (String directory : getDM().getUpgradeDirectories())
         m_destinationChoiceField.add(directory);
      
      rxAdd("LeftTopWide", m_destinationChoiceField);
      
      getDM().setIsBrander(getInstallValue(InstallUtil.getVariableName(
            getClass().getName(), RxUpgradeDestinationModel.IS_BRANDER_VAR)).
            equalsIgnoreCase("true"));
   }
   
   @Override
   public void entered()
   {
      m_destinationChoiceField.removeAll();
      for (String directory : getDM().getUpgradeDirectories())
         m_destinationChoiceField.add(directory);
   }

   @Override
   public void exiting()
   {
      getDM().setDestDir(m_destinationChoiceField.getSelectedItem());
   }
 
   @Override
   public void entering()
   {      
   }
   
   /**
    * The data model stores user input captured by this panel.
    * 
    * @return the data model for this panel.
    */
   private RxUpgradeDestinationModel getDM()
   {
      return (RxUpgradeDestinationModel) getModel();
   }

   /**
    * Combo box for selecting one of the Rhythmyx directories for upgrade
    * Initialized in the <code>initialize()</code> method, never
    * <code>null</code> or modified after that.
    */
   private Choice m_destinationChoiceField = null;
}
