/******************************************************************************
 *
 * [ RxCompScanModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.model;

import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.installer.action.RxCompatibilityScan;


/**
 * This model represents the panel/console used to display compatibility
 * information gathered by {@link RxCompatibilityScan}.
 */
public class RxCompScanModel extends RxIAModel
{
   /**
    * Constructs an {@link RxCompScanModel} object.
    * 
    * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxCompScanModel(IPSProxyLocator locator)
   {
      super(locator);
      setHideOnUpgrade(false);
   }
   
   @Override
   public String getTitle()
   {
      // Return the empty string here to override the generic title of the 
      // parent class.  A specific title is not required for this model's
      // associated panel/console.
      return "";
   }
}
