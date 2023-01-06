/******************************************************************************
 *
 * [ PSWizardStartFinishPanel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.wizard;

import com.percussion.cx.PSContentExplorerApplet;

/**
 * A standard wizard start or finish panel which does only show user 
 * instructions.
 */
public class PSWizardStartFinishPanel extends PSWizardPanel
{

   /**
    * Instantiate with applet to make config options from applet available to
    * panel
    */
   public PSWizardStartFinishPanel(PSContentExplorerApplet applet)
   {
      super(applet);
      initPanel(null);
   }

   /**
    * Construct a start or finish wizard panel.
    */

   public PSWizardStartFinishPanel()
   {
      initPanel(null);
   }
}
