/******************************************************************************
 *
 * [ RxFastForwardFeatureModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.model;

import com.percussion.installer.RxVariables;
import com.percussion.installanywhere.IPSProxyLocator;


/**
 * This model is responsible for maintaining the selection status and
 * availability of the Rhythmyx FastForward features.
 */

public class RxFastForwardFeatureModel extends RxComponentModel
{
   /**
    * Constructs the FastForward feature model.
    * 
   * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxFastForwardFeatureModel(IPSProxyLocator locator)
   {
      super(locator);
      setParent("Rhythmyx FastForward");
   }
   
   @Override
   public String getTitle()
   {
      return "FastForward Feature Selection";
   }
   
   @Override
   public String getRxInstallVariable()
   {
      return RxVariables.RX_FASTFORWARD_FEATURES;
   }
   
   @Override
   protected void initComponentsMap()
   {
      m_componentsMap.put(FF_APPS_NAME, 
            new RxComponent(
                  FF_APPS_NAME,
                  -1,
                  new String[0]));
      m_componentsMap.put(FF_SAMPLE_CONTENT_NAME, 
            new RxComponent(
                  FF_SAMPLE_CONTENT_NAME,
                  -1,
                  new String[0]));
   }
   
   /**
    * FastForward Applications feature name
    */
   public final static String FF_APPS_NAME = "FastForward Applications";
   
   /**
    * FastForward Sample Content feature name
    */
   public final static String FF_SAMPLE_CONTENT_NAME =
      "FastForward Sample Content";
}
