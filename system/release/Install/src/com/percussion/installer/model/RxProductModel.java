/******************************************************************************
 *
 * [ RxProductModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.model;

import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installer.RxVariables;
import com.percussion.util.IPSBrandCodeConstants;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * This model is responsible for maintaining the selection status and
 * availability of each of the Rhythmyx products.
 */

public class RxProductModel extends RxComponentModel
{
   /**
    * Constructs the product model.
    * 
   * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxProductModel(IPSProxyLocator locator)
   {
      super(locator);
   }
   
   @Override
   public String getTitle()
   {
      return "Product Selection";
   }
   
   /**
    * This model maintains a static map which contains the set of Rhythmyx
    * product names along with their corresponding components.  It is used to
    * initialize the components map and is also used by the brand model when
    * validating currently installed products against the current installation
    * code.
    *  
    * @return the products map containing Rhythmyx products/components,
    * never <code>null</code> or empty.
    */
   public static Map<String, RxComponent> getProductsMap()
   {
      return ms_productsMap;
   }
   
   @Override
   public String getRxInstallVariable()
   {
      return RxVariables.RX_INSTALL_PRODUCTS;
   }
   
   @Override
   protected void initComponentsMap()
   {
      m_componentsMap.putAll(getProductsMap());
   }
   
   /**
    * Repository product name
    */
   public final static String REPOSITORY_NAME = "Rhythmyx Repository Database";
   
   /**
    * Server product name
    */
   public final static String SERVER_NAME = "Rhythmyx Server";
   
   /**
    * Development Tools product name
    */
   public final static String DEVTOOLS_NAME = "Rhythmyx Development Tools";
   
   /**
    * Relative location of the repository properties file
    */
   private static String ms_strRepositoryPropsFile =
      "rxconfig/Installer/rxrepository.properties";
   
   /**
    * Relative location of the server properties file
    */
   private static String ms_strServerPropsFile =
      "rxconfig/Server/server.properties";
   
   /**
    * See {@link #getProductsMap()}.
    */
   private static final SortedMap<String, RxComponent> ms_productsMap =
      new TreeMap<String, RxComponent>();
   
   /**
    * Initializes the product map.
    */
   static
   {
      String[] repFiles = {ms_strRepositoryPropsFile};
      String[] srvFiles = {ms_strServerPropsFile};
                  
      ms_productsMap.put(REPOSITORY_NAME, new RxComponent(REPOSITORY_NAME,
            IPSBrandCodeConstants.REPOSITORY, repFiles));
      ms_productsMap.put(SERVER_NAME, new RxComponent(SERVER_NAME,
            IPSBrandCodeConstants.SERVER, srvFiles));
   }
}
