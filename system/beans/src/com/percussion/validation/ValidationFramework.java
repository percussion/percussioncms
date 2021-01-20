/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.validation;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.text.JTextComponent;



/** 
 * The generic "validation" framework for checking specified components within
 * a container.  This class is the interface between actual components that 
 * needs to be checked and the constraints to be checked against specified by 
 * the programmer.
 * <P>
 * <code>ValidationException</code> is thrown to signify an invalid value within
 * a component. <code>JOptionPane</code> is used to quickly pop up a warning 
 * message directing the user to the invalid component.  Then the focus is given
 * to invalid component for user's convenience.
 *
 * @see ValidationException
 * @see ValidationConstraint
 */
public class ValidationFramework
{
   
   /**
    * Default constructor. All components and constraints to be validated will
    * be <code>null</code>. The parent window is also <code>null</code>.
    */
   public ValidationFramework()
   {
   }
   
   /** 
    * Constructs a basic framework with specified component-constraint pairs and
    * a parent window.
    *
    * @param parent the parent window to be used for error dialogs, may not be 
    * <code>null</code>
    * @param components the array of components to be checked, may not be <code>
    * null</code> and its length must be equal to constraints length and the 
    * components in the array must not be <code>null</code>
    * @param constraints the array of constraints to use for validation, may not
    * be <code>null</code> and its length must be equal to components length and
    * the constraints in the array must not be <code>null</code>
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public ValidationFramework(Window parent, Object[] components, 
      ValidationConstraint[] constraints)
   {
      if(parent == null)
         throw new IllegalArgumentException(
            "the parent window may not be null.");
      
      if(components == null)
         throw new IllegalArgumentException("components may not be null.");
         
      if(constraints == null)
         throw new IllegalArgumentException("constraints may not be null.");
      
      if(components.length != constraints.length)
         throw new IllegalArgumentException(
            "components and constraints array lengths must match");
            
      for(int i=0; i<components.length; i++)
      {
         if(components[i] == null)
            throw new IllegalArgumentException(
               "component in the components array may not be null.");
      }
      
      for(int i=0; i<constraints.length; i++)
      {
         if(constraints[i] == null)
            throw new IllegalArgumentException(
               "constraint in the constraints array may not be null.");
      }
            
      setFramework(parent, components, constraints);
   }

   /** 
    * Reinitializes the framework with the specified components and constraints.
    * 
    * @param components the array of components to be checked, assumes not 
    * <code>null</code> and its length is equal to constraints length and the 
    * components in the array are not <code>null</code>
    * @param constraints the array of constraints to use for validation, assumes
    * not <code>null</code> and its length is equal to components length and
    * the constraints in the array are not <code>null</code>
    */
   private void setFramework(Object[] components, 
      ValidationConstraint[] constraints)
   {
      m_componentList = components;
      m_constraintList = constraints;
   }

   /**
    * Resets the framework with the parent window, components and constraints.
    *
    * @param parent the parent window to be used for error dialogs, may not be 
    * <code>null</code>
    * @param components the array of components to be checked, may not be <code>
    * null</code> and its length must be equal to constraints length and the 
    * components in the array must not be <code>null</code>
    * @param constraints the array of constraints to use for validation, may not
    * be <code>null</code> and its length must be equal to components length and
    * the constraints in the array must not be <code>null</code>
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public void setFramework( Window parent, Object[] components, 
      ValidationConstraint[] constraints)
   {
      if(parent == null)
         throw new IllegalArgumentException(
            "the parent window may not be null.");
      
      if(components == null)
         throw new IllegalArgumentException("components may not be null.");
         
      if(constraints == null)
         throw new IllegalArgumentException("constraints may not be null.");
      
      if(components.length != constraints.length)
         throw new IllegalArgumentException(
            "components and constraints array lengths must match");
            
      for(int i=0; i<components.length; i++)
      {
         if(components[i] == null)
            throw new IllegalArgumentException(
               "component in the components array may not be null.");
      }
      
      for(int i=0; i<constraints.length; i++)
      {
         if(constraints[i] == null)
            throw new IllegalArgumentException(
               "constraint in the constraints array may not be null.");
      }
         
      m_parentWindow = parent;
      setFramework( components, constraints );
   }

   /** 
    * Loops through all the components that needs validation and checks them 
    * against their appropriate constraints.  If the constraint throws a <code>
    * ValidationException</code>, the exception is caught and displays a warning
    * dialog and the component with the incorrect value is highlighted and 
    * focused. If the lists are <code>null</code>, then it always returns <code>
    * true</code>
    *
    * @return <code>true</code> if validation succeeds, otherwise <code>false
    * </code>
    * @see ValidationConstraint
    */
   public boolean checkValidity()
   {
      if(m_componentList == null || m_constraintList == null)
         return true;
         
      int counter = 0;
      try
      {
         for (counter = 0; counter < m_componentList.length; counter++)
         {
            m_constraintList[counter].checkComponent(
               m_componentList[counter]);
         }
      }
      catch (ValidationException e)
      {
         String label = null;
         if(m_componentList[counter] instanceof Component)
         {
            label = getLabelTextForComponent(
               (Component)m_componentList[counter]);
         }
         String message;
         if(m_constraintList[counter] instanceof ComponentValidationConstraint)
         {
            message = ((ComponentValidationConstraint)m_constraintList[counter])
               .getErrorText(label);
         }
         else
            message = m_constraintList[counter].getErrorText();
            
         JOptionPane.showMessageDialog( m_parentWindow, message,
            ms_res.getString("error"), JOptionPane.OK_OPTION);
      
         if (m_componentList[counter] instanceof JTextComponent)
         {
            ((JTextComponent)m_componentList[counter]).selectAll();
            ((Component)m_componentList[counter]).requestFocus();
         
            // if the error is at the password field, clear it.
            if (m_componentList[counter] instanceof JPasswordField)
               ((JPasswordField)m_componentList[counter]).setText( null );
         }
         else if (m_componentList[counter] instanceof JComboBox)
         {
            Component editor = ((JComboBox)
               m_componentList[counter]).getEditor().getEditorComponent();
         
            if ( editor instanceof JTextComponent )
               ((JTextComponent) editor).selectAll();
         
            editor.requestFocus();
         }
         return false; // meaning that validation failed; method cannot go on.
      }
      return true; // meaning that validation passed; method will go on.
   }
   
   /**
    * Gets the label text for the supplied component. 
    * 
    * @param comp the component to check for label, assumed not <code>null
    * </code>
    * 
    * @return the label text, may be <code>null</code> if the container of this
    * component does not contain a label that refers to this component. May be
    * empty if the label text is empty.
    */
   private String getLabelTextForComponent(Component comp)
   {
      String labelText = null;
      
      Container container = comp.getParent();
      if(container != null) 
      {
         Component[] comps = container.getComponents();      
         for (int i = 0; i < comps.length; i++) 
         {      
            if(comps[i] instanceof JLabel)
            {
               JLabel label = (JLabel)comps[i];
               if(label.getLabelFor() == comp)
               {
                  labelText = label.getText();
                  break;
               }
            }
         }
      }
      
      return labelText;
   }

   /**
    * The array of components to be validated. Initialized in the
    * constructor and may be modified through <code>
    * setFramework(Window, Object[], ValidationConstraint[])</code>. Never 
    * <code>null</code> after initialization.
    */
   private Object[] m_componentList = null;
   
   /**
    * The array of constraints used for validation. Initialized in the
    * constructor and may be modified through <code>
    * setFramework(Window, Object[], ValidationConstraint[])</code>. Never 
    * <code>null</code> after initialization.
    */
   private ValidationConstraint[] m_constraintList = null;

   /**
    * The parent window to be used to display error dialogs. Initialized in the
    * constructor and may be modified through <code>
    * setFramework(Window, Object[], ValidationConstraint[])</code>. Never 
    * <code>null</code> after initialization.
    */
   private Window m_parentWindow = null;
   
   /**
    * The fully qualified resource file name used for validations.
    */
   public static final String VALIDATION_RESOURCES = 
      "com.percussion.validation.ValidationResources";

   /**
    * The static resource bundle to provide the error messages, never <code>
    * null</code>
    */
   private static ResourceBundle ms_res = null;
   static
   {
      ms_res = ResourceBundle.getBundle(
         VALIDATION_RESOURCES, Locale.getDefault());
   }
}


