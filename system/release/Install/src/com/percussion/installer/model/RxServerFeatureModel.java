/******************************************************************************
 *
 * [ RxServerFeatureModel.java ]
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
 * availability of each of the Rhythmyx Server features.
 */

public class RxServerFeatureModel extends RxComponentModel
{
   /**
    * Constructs the server feature model.
    * 
   * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxServerFeatureModel(IPSProxyLocator locator)
   {
      super(locator);
      setParent(RxProductModel.SERVER_NAME);
   }
   
   @Override
   public String getTitle()
   {
      return "Server Feature Selection";
   }
  
   @Override
   public String getRxInstallVariable()
   {
      return RxVariables.RX_SERVER_FEATURES;
   }
   
   /**
    * This model maintains a static map which contains the set of Rhythmyx
    * server feature names along with their corresponding components.  It is
    * used to initialize the components map and is also used by the brand model
    * when validating currently installed product features against the current
    * installation code.
    *  
    * @return the map containing Rhythmyx server features/components, never
    * <code>null</code> or empty.
    */
   public static Map<String, RxComponent> getServerFeaturesMap()
   {
      return ms_serverFeaturesMap;
   }
   
   @Override
   protected void initComponentsMap()
   {
      m_componentsMap.putAll(getServerFeaturesMap());
   }
   
   /**
    * Server core feature name
    */
   public final static String SERVER_CORE_NAME = "Server Core";
   
   /**
    * Documentation feature name
    */
   public final static String DOCUMENTATION_NAME = "Documentation";
   
   /**
    * Development tools remote installer feature name
    */
   public final static String DEVTOOLS_REMOTE_INSTALLER_NAME =
      "Development Tools Remote Installer";
   
   /**
    * See {@link #getServerFeaturesMap()}.
    */
   private static final SortedMap<String, RxComponent> ms_serverFeaturesMap =
      new TreeMap<>();
   
   static
   {
      ms_serverFeaturesMap.put(SERVER_CORE_NAME,
            new RxComponent(
                  SERVER_CORE_NAME,
                  -1,
                  new String[0]));
      
      ms_serverFeaturesMap.put(DOCUMENTATION_NAME,
            new RxComponent(
                  DOCUMENTATION_NAME,
                  -1,
                  new String[0]));
      ms_serverFeaturesMap.put(DEVTOOLS_REMOTE_INSTALLER_NAME,
            new RxComponent(
                  DEVTOOLS_REMOTE_INSTALLER_NAME,
                  -1,
                  new String[0]));
   }
}
