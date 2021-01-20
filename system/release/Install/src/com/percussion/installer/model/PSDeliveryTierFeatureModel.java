/******************************************************************************
 *
 * COPYRIGHT (c) 1999 - 2012 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.model;

import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installer.RxVariables;


/***
 * 
 * Contains the feature selection for the Delivery Tier feature. 
 * 
 * @author nate chadwick
 *
 */
public class PSDeliveryTierFeatureModel extends RxComponentModel
{

   public final static String DTS_FEATURE_NAME = "Delivery Tier Services (DTS)";

   
   private static String MODEL_PARENT = "Delivery Tier Services (DTS)";
   
   public PSDeliveryTierFeatureModel(IPSProxyLocator locator)
   {
      super(locator);
      setParent(MODEL_PARENT);
   }
   
   @Override
   protected String getRxInstallVariable()
   {
      return RxVariables.PS_DTS_FEATURES;
   }
   
   @Override
   protected void initComponentsMap()
   {
      m_componentsMap.put(DTS_FEATURE_NAME, 
            new RxComponent(
                  DTS_FEATURE_NAME,
                  -1,
                  new String[0]));
      
      //TODO: Add more fine grained sub feature definition here. 
   } 
}
