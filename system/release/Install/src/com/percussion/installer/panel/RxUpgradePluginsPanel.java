/******************************************************************************
 *
 * [ RxUpgradePluginsPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.installer.model.RxUpgradePluginsModel;


/**
 * AWT Implementation of {@link RxUpgradePluginsModel}.
 */
public class RxUpgradePluginsPanel extends RxTextDisplayPanel
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();
      
      RxUpgradePluginsModel upm = new RxUpgradePluginsModel(this);
      setModel(upm);
   }
   
   @Override
   public void entering()
   {
   }
   
   @Override
   public void entered()
   {
      setText(getDM().getDisplayMessage());
      setPreviousButtonEnabled(false);
   }

   @Override
   public void exiting()
   {
   }
   
   /**
    * Helper method to access the model i.e. retrieve
    * {@link RxUpgradePluginsModel}.
    * 
    * @return the data model for this panel.
    */
   private RxUpgradePluginsModel getDM()
   {
      return (RxUpgradePluginsModel) getModel();
   }
}
