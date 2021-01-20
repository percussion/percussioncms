/******************************************************************************
 *
 * [ RxVerifyConnectionPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.installer.model.RxVerifyConnectionModel;


/**
 * AWT Implementation of {@link RxVerifyConnectionModel}.
 */
public class RxVerifyConnectionPanel extends RxTextDisplayPanel
{
   @Override
   public void initialize()
   {
      //must call base class
      super.initialize();
      
      RxVerifyConnectionModel vcm = new RxVerifyConnectionModel(this);
      setModel(vcm);
   }
      
   @Override
   public void entering()
   {
   }
   
   @Override
   public void entered()
   {
      setText(((RxVerifyConnectionModel) getModel()).getErrorMsg());
      setNxtButtonEnabled(false);
   }

   @Override
   public void exiting()
   {
   }
}
