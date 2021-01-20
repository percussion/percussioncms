/******************************************************************************
 *
 * [ RxComponentModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.model;

import com.percussion.install.Code;
import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.installer.action.RxLogger;
import com.percussion.installer.action.RxUpdateUpgradeFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * This model is responsible for maintaining the selection status and
 * availability of Rhythmyx components.
 */

public class RxComponentModel extends RxIAModel
{
   /**
    * Constructs the component model.
    * 
   * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxComponentModel(IPSProxyLocator locator)
   {
      super(locator);
      setHideOnUpgrade(false);
      initComponentsMap();
   }
   
   @Override
   public boolean queryEnter()
   {
      boolean shouldEnter = true;
      
      // Set all licensed components as available for install
      setAvailableComponents();
      
      // All licensed, installed components need to be selected for upgrade
      if (RxUpdateUpgradeFlag.checkUpgradeInstall())
      {
         String rootDir = getRootDir();
         for (String name : getComponentNames())
         {
            RxComponent component = getComponent(name);
            if (component == null)
            {
               RxLogger.logError("RxComponentModel#queryEnter : Could not " +
                     "find component object for " + name);
               continue;
            }
            
            if (component.isInstalled(rootDir) && component.isAvailable())
               component.setSelected(true);
         }
         
         if (RxInstallOptionTCModel.checkTypicalInstall())
         {
            // Don't show panel
            shouldEnter = false;
         }
      }
      else
      {
         // Set all components as selected for typical, new install
         if (RxInstallOptionTCModel.checkTypicalInstall())
         {
            setSelectedComponents();
                     
            // Don't show panel
            shouldEnter = false;
         }
      }
      
      // Save the current selections for typical installs
      if (RxInstallOptionTCModel.checkTypicalInstall())
      {
         setInstallValue(getRxInstallVariable(),
               getSelectedComponentNamesAsString());
      }
      
      if (!shouldEnter)
         return false;
      else
         return super.queryEnter();
   }
   
   @Override
   public boolean queryExit()
   {
      if (!super.queryExit())
         return false;
      
      setInstallValue(getRxInstallVariable(),
            getSelectedComponentNamesAsString());
        
      return true;
   }
   
   /**
    * The licensed component names are displayed by the panel/console for user
    * selection.
    * 
    * @return the set of all component names.
    */
   public Set<String> getComponentNames()
   {
      return m_componentsMap.keySet();
   }
   
   /**
    * Each component name is associated with a component object which maintains
    * additional information about the component.
    * 
    * @param name the name of the component, may not be <code>null</code>.
    * 
    * @return the <code>RxComponent</code> object which represents the
    * component, may be <code>null</code>.
    */
   public RxComponent getComponent(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name may not be null");
      
      return m_componentsMap.get(name);
   }
   
   @Override
   public String getTitle()
   {
      return "Component Selection";
   }
     
   /**
    * Initializes the map used to maintain installation component information.
    */
   protected void initComponentsMap()
   {
   }
   
   /**
    * Sets all licensed components as available for install.  The FastForward
    * product component is never available for upgrade installs.
    */
   protected void setAvailableComponents()
   {
      Code c = RxBrandModel.fetchBrandCode();
      for (String name : getComponentNames())
      {
         RxComponent component = getComponent(name);
         if (component == null)
         {
            RxLogger.logError("RxComponentModel#setAvailableComponents : " +
                  "Could not find component object for " + name);
            continue;
         }
         
         boolean isLicensed;
         if (c == null)
         {
            // Brand panel/console was not displayed, so everything is licensed
            isLicensed = true;
         }
         else
         {
            int code = component.getCode();
            isLicensed = code == -1 || c.isComponentLicensed(code);
         }
         
         component.setAvailable(isLicensed);
      }
   }
   
   /**
    * Sets all components as selected for install.
    */
   protected void setSelectedComponents()
   {
      for (String name : getComponentNames())
      {
         RxComponent component = getComponent(name);
         if (component == null)
         {
            RxLogger.logError("RxComponentModel#setSelectedComponents : " +
                  "Could not find component object for " + name);
            continue;
         }
         
         component.setSelected(true);
      }
   }
   
   /**
    * Builds a list of the currently selected component names.
    * 
    * @return list of available and selected component names, never
    * <code>null</code>, may be empty.
    */
   public List<String> getSelectedComponentNames()
   {
      ArrayList<String> names = new ArrayList<String>();
      for (String name : getComponentNames())
      {
         RxComponent component = getComponent(name);
         if (component == null)
         {
            RxLogger.logError("RxComponentModel#getSelectedComponentNames : " +
                  "Could not find component object for " + name);
            continue;
         }
         
         if (component.isAvailable() && component.isSelected())
            names.add(name);
      }
      
      return names;
   }
   
   /**
    * Builds a comma-separated string of the currently selected component names.
    * 
    * @return list of available and selected component names, never
    * <code>null</code>, may be empty.
    */
   public String getSelectedComponentNamesAsString()
   {
      String selectedComponents = "";
      for (String component : getSelectedComponentNames())
      {
         if (selectedComponents.trim().length() > 0)
            selectedComponents += ", ";
         
         selectedComponents += component;
      }
      
      return selectedComponents;
   }
   
   /**
    * The selected components will be stored as a comma-separated list by a
    * Rhythmyx install variable.
    * 
    * @return the name of the Rhythmyx install variable used to store the list
    * of selected components, never <code>null</code> or empty.
    */
   protected String getRxInstallVariable()
   {
      return "RX_INSTALL_COMPONENTS";
   }
   
   /**
    * Each component may have a parent component, indicating that it is a 
    * sub-feature of Rhythmyx product.
    * 
    * @return the name of this component's parent, may be <code>null</code>.
    */
   public String getParent()
   {
      return m_parent;
   }
   
   /**
    * Sets the parent component name.
    * 
    * @param parent valid names include the following:
    * {@link RxProductModel#REPOSITORY_NAME},
    * {@link RxProductModel#SERVER_NAME}
    * {@link RxProductModel#DEVTOOLS_NAME}
    */
   protected void setParent(String parent)
   {
      if (!parent.equals(RxProductModel.REPOSITORY_NAME) &&
            !parent.equals(RxProductModel.SERVER_NAME) &&
            !parent.equals(RxProductModel.DEVTOOLS_NAME))
      {
         throw new IllegalArgumentException("parent must be one of: " +
               RxProductModel.REPOSITORY_NAME + "," +
               RxProductModel.SERVER_NAME + "," +
               RxProductModel.DEVTOOLS_NAME);
      }
      
      m_parent = parent;
   }
   
   /**
    * Map containing available Rhythmyx name/component pairs.
    */
   protected SortedMap<String, RxComponent> m_componentsMap = 
      new TreeMap<String, RxComponent>();
   
   /**
    * See {@link #getParent()}, {@link #setParent(String)}.
    */
   protected String m_parent = null;
}
