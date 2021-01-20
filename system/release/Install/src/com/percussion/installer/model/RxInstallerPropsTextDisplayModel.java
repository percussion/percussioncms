/******************************************************************************
 *
 * [ RxInstallerPropsTextDisplayModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.model;

import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAModel;


/**
 * This model represents the panel/console used to display a text message
 * from the <code>RxInstallerProperties.properties</code> resource file.
 */
public class RxInstallerPropsTextDisplayModel extends RxIAModel
{
   /**
    * Constructs an {@link RxInstallerPropsTextDisplayModel} object.
    * 
    * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxInstallerPropsTextDisplayModel(IPSProxyLocator locator)
   {
      super(locator);
      setHideOnUpgrade(false);
   }
   
   @Override
   public String getTitle()
   {
      return "";
   }
   
   /**
    * The panel and console associated with this model are plugins which allow
    * parameters to be specified via the IDE.
    *  
    * @return the name of the property name variable specified via the IDE.
    */
   public String getPropertyNameVar()
   {
      return m_propertyNameVar;
   }
   
   /**
    * The panel and console associated with this model are plugins which allow
    * parameters to be specified via the IDE.
    *  
    * @return the name of the navigation options variable specified via the IDE.
    */
   public String getNavigationOptionsVar()
   {
      return m_navigationOptionsVar;
   }
   
   /**
    * See {@link #getPropertyNameVar()}.
    */
   private String m_propertyNameVar = "propertyName";
   
   /**
    * See {@link #getNavigationOptionsVar()}.
    */
   private String m_navigationOptionsVar = "navigationOptions";
}
