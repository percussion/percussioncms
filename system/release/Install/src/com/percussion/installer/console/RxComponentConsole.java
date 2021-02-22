/******************************************************************************
 *
 * [ RxComponentConsole.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.installer.console;

import com.percussion.installanywhere.RxIAConsole;
import com.percussion.installanywhere.RxIAPreviousRequestException;
import com.percussion.installer.RxVariables;
import com.percussion.installer.action.RxLogger;
import com.percussion.installer.action.RxUpdateUpgradeFlag;
import com.percussion.installer.model.RxComponent;
import com.percussion.installer.model.RxComponentModel;

import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;


/**
 * Console Implementation of {@link RxComponentModel}.
 */
public class RxComponentConsole extends RxIAConsole
{
   @Override
   protected void execute() throws RxIAPreviousRequestException
   {
      clearComponents();
      addComponents();
            
      Vector<String> choices = new Vector<>(m_choiceSet);
      int[] currentSelections = getSelections(choices);
            
      int[] newSelections = 
         getConsoleUtils().createChoiceListAndGetMultipleValuesWithDefaults(
               getPrompt(), choices, currentSelections);
                       
      if (newSelections != null)
         setSelections(choices, newSelections);   
   }
   
   @Override
   protected void initialize()
   {
      super.initialize();
      getDataModel();
   }
   
   /**
    * This console will display a prompt followed by a checkbox group of
    * available components available for selection.
    * 
    * @return the prompt to be displayed.
    */
   protected String getPrompt()
   {
      return "Select the components you would like to install:";
   }
   
   /**
    * Updates the choice set with the currently available components.
    */
   private void addComponents()
   {
      for (String name : m_dM.getComponentNames())
      {
         if (!m_choiceSet.contains(name))
         {
            RxComponent component = m_dM.getComponent(name);
            if (component == null)
            {
               RxLogger.logError("RxComponentConsole#addComponents : " +
                     "Could not find component object for " + name);
               continue;
            }
            
            if (component.isAvailable())
               m_choiceSet.add(name);
         }
      }
   }
   
   /**
    * Removes all components from the available choices and selected sets.
    */
   private void clearComponents()
   {
      for (String name : m_dM.getComponentNames())
      {
         m_choiceSet.remove(name);
         m_selectedSet.remove(name);
      }
   }
   
   /**
    * Determines the currently selected components from the given choices.
    * 
    * @param choices the vector of currently available choices, assumed not
    * <code>null</code>.
    * 
    * @return an int[] whose values correspond to the indices of the currently
    * selected components in the given choices vector, never <code>null</code>,
    * may be empty.
    */
   private int[] getSelections(Vector<String> choices)
   {
      Set<Integer> selectedSet = new TreeSet<>();
      for (int i = 0; i < choices.size(); i++)
      {
         String name = choices.elementAt(i);
         RxComponent component = m_dM.getComponent(name);
         if (component == null)
         {
            RxLogger.logError("RxComponentConsole#getSelections : Could not " +
                  "find component object for " + name);
            continue;
         }
         
         if (component.isSelected() || 
               (RxUpdateUpgradeFlag.checkUpgradeInstall() &&
                     component.isInstalled(getInstallValue(
                     RxVariables.INSTALL_DIR))))
            selectedSet.add(new Integer(i + 1));
      }
      
      Object[] selectedSetArr = selectedSet.toArray();
      int[] selections = new int[selectedSetArr.length];
      for (int i = 0; i < selections.length; i++)
         selections[i] = ((Integer) selectedSetArr[i]).intValue();
      
      return selections;
   }
   
   /**
    * Sets the currently selected components.
    * 
    * @param choices the vector of currently available choices, assumed not
    * <code>null</code>. 
    * @param selections int[] whose values correspond to the indices of the
    * currently selected components in the given choices array, never
    * <code>null</code>.
    */
   private void setSelections(Vector<String> choices, int[] selections)
   {
      for (int i = 0; i < choices.size(); i++)
      {
         String name = choices.elementAt(i);
         RxComponent component = m_dM.getComponent(name);
         if (component == null)
         {
            RxLogger.logError("RxComponentConsole#setSelections : Could not " +
                  "find component object for " + name);
            continue;
         }
         
         boolean isSelected = false;
         for (int j = 0; j < selections.length; j++)
         {
            if (selections[j] == i)
               isSelected = true;
         }
         
         component.setSelected(isSelected);
      }
   }
     
   /**
    * The data model stores user input captured by this console.
    * 
    * @return the data model for this console.
    */
   private RxComponentModel getDataModel()
   {
      if (m_dM == null)
         m_dM = (RxComponentModel) getModel();
      
      return m_dM;
   }
   
   /**
    * Will maintain the set of currently available components to be displayed
    * for selection.
    */
   private Set<String> m_choiceSet = new TreeSet<>();
   
   /**
    * Will maintain the set of currently selected components.
    */
   private Set<String> m_selectedSet = new TreeSet<>();
   
   /**
    * Maintains the component model for this console, never <code>null</code>
    * after {@link #getDataModel()} is called.
    */
   protected RxComponentModel m_dM = null;
}
