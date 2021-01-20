/******************************************************************************
 *
 * [ RxComponentPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.panel;

import com.percussion.installanywhere.RxIAPanel;
import com.percussion.installer.RxVariables;
import com.percussion.installer.action.RxLogger;
import com.percussion.installer.action.RxUpdateUpgradeFlag;
import com.percussion.installer.model.RxComponent;
import com.percussion.installer.model.RxComponentModel;

import java.awt.Checkbox;
import java.util.Map;
import java.util.TreeMap;



/**
 * This is a panel which allows for the selection of licensed components for
 * install.
 */
public class RxComponentPanel extends RxIAPanel
{
   @Override
   public void initialize()
   {
      super.initialize();
      getDM();
   }
   
   @Override
   protected void entering()
   {
      clearComponents();
      addComponents();
   }

   @Override
   protected void entered()
   {
   }
   
   @Override
   protected void exiting()
   {
      setComponentsSelected();
   }
   
   /**
    * Updates both the component map and panel container with the currently
    * available components and corresponding checkboxes.
    */
   private void addComponents()
   {
      for (String name : m_dM.getComponentNames())
      {
         if (!m_checkboxMap.containsKey(name))
         {
            RxComponent component = m_dM.getComponent(name);
            if (component == null)
            {
               RxLogger.logError("RxComponentPanel#addComponents : Could not " +
                     "find component object for " + name);
               continue;
            }
            
            if (component.isAvailable())
            {
               Checkbox chkBox = new Checkbox(name);
               chkBox.setBackground(super.getBackground());
               chkBox.setState(component.isSelected());
               
               if (RxUpdateUpgradeFlag.checkUpgradeInstall() &&
                     component.isInstalled(getInstallValue(
                           RxVariables.INSTALL_DIR)))
                  chkBox.setEnabled(false);
               
               m_checkboxMap.put(name, chkBox);
               rxAdd(chkBox);
            }
         }
      }
   }
   
   /**
    * Removes all components from the component map as well as the panel's
    * container.
    */
   private void clearComponents()
   {
      for (String name : m_dM.getComponentNames())
      {
         if (m_checkboxMap.containsKey(name))
         {
            Checkbox chkBox = m_checkboxMap.get(name);
            m_checkboxMap.remove(name);
            getRxPanel().remove(chkBox);
         }
      }
   }
   
   /**
    * Updates the selection status of all components based on the status of
    * their corresponding checkboxes.
    */
   private void setComponentsSelected()
   {
      for (String name : m_dM.getComponentNames())
      {
         if (m_checkboxMap.containsKey(name))
         {
            RxComponent component = m_dM.getComponent(name);
            if (component == null)
            {
               RxLogger.logError("RxComponentPanel#setComponentSelected : " +
                     "Could not find component object for " + name);
               continue;
            }
            
            Checkbox chkBox = m_checkboxMap.get(name);
            component.setSelected(chkBox.getState());
         }
      }
   }
   
   /**
    * The data model stores user input captured by this panel.
    * 
    * @return the data model for this panel.
    */
   private RxComponentModel getDM()
   {
      if ( m_dM == null)
         m_dM = (RxComponentModel) getModel();
      return m_dM;
   }
   
   /**
    * Associates a Rhythmyx component name with a checkbox used for selection.
    * Will maintain the set of currently available components.
    */
   private Map<String, Checkbox> m_checkboxMap =
      new TreeMap<String, Checkbox>();
   
   /**
    * Maintains the component model for this panel, never <code>null</code>
    * after {@link #getDM()} is called.
    */
   protected RxComponentModel m_dM = null;
}
