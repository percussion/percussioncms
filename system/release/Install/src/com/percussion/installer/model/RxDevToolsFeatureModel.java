/******************************************************************************
 *
 * [ RxDevToolsFeatureModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.model;

import com.percussion.installer.RxVariables;
import com.percussion.installanywhere.IPSProxyLocator;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * This model is responsible for maintaining the selection status and
 * availability of the Rhythmyx Development Tools features.
 */

public class RxDevToolsFeatureModel extends RxComponentModel
{
   /**
    * Constructs the devtools feature model.
    * 
   * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxDevToolsFeatureModel(IPSProxyLocator locator)
   {
      super(locator);
      setParent("Rhythmyx Development Tools");
   }
   
   @Override
   public String getTitle()
   {
      return "Feature Selection";
   }
   
   @Override
   public String getRxInstallVariable()
   {
      return RxVariables.RX_DEVTOOLS_FEATURES;
   }
   
   /**
    * This model maintains a static map which contains the set of Rhythmyx
    * development tools feature names along with their corresponding components.
    * It is used to initialize the components map and is also used by the brand
    * model when validating currently installed product features against the
    * current installation code.
    *  
    * @return the map containing Rhythmyx development tools features/components,
    * never <code>null</code> or empty.
    */
   public static Map<String, RxComponent> getDevToolsFeaturesMap()
   {
      return ms_devToolsFeaturesMap;
   }
   
   @Override
   protected void initComponentsMap()
   {
      m_componentsMap.putAll(getDevToolsFeaturesMap());
   }
   
   /**
    * Developer Tools feature name
    */
   public final static String DEVELOPER_TOOLS_NAME = "Developer Tools";
   
   /**
    * Documentation feature name
    */
   public final static String DOCUMENTATION_NAME = "Documentation";
   
   /**
    * See {@link #getDevToolsFeaturesMap()}.
    */
   private static final SortedMap<String, RxComponent> ms_devToolsFeaturesMap =
      new TreeMap<>();
   
   static
   {
      ms_devToolsFeaturesMap.put(DEVELOPER_TOOLS_NAME,
            new RxComponent(
                  DEVELOPER_TOOLS_NAME,
                  -1,
                  new String[0]));
      ms_devToolsFeaturesMap.put(DOCUMENTATION_NAME,
            new RxComponent(
                  DOCUMENTATION_NAME,
                  -1,
                  new String[0]));
   }
}
