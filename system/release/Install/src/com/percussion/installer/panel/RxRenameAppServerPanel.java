/******************************************************************************
 *
 * [ RxRenameAppServerPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.install.RxInstallerProperties;
import com.percussion.installer.model.RxRenameAppServerModel;


/**
 * AWT Implementation of {@link RxRenameAppServerModel}.
 */
public class RxRenameAppServerPanel extends RxTextDisplayPanel
{
   @Override
   public void initialize()
   {
      super.initialize();

      RxRenameAppServerModel rasm = new RxRenameAppServerModel(this);
      setModel(rasm);
      
      String displayText = RxInstallerProperties.getString(
            "renameAppServerMsg");
      setText(displayText);
   }
   
   @Override
   public void entering()
   {
   }
   
   @Override
   public void entered()
   {
   }
   
   @Override
   public void exiting()
   {
   }
}
