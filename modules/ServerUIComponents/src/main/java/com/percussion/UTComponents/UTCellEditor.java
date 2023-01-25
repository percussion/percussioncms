/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.UTComponents;

import javax.swing.*;

/**
 * This extends <code>DefaultCellEditor</code> to accept panel with many
 * controls as editor component as <code>DefauleCellEditor</code> accepts only
 * <code>JComboBox</code>, <code>JCheckBox</code> and <code>JTextField</code>.
 */
public class UTCellEditor extends DefaultCellEditor
{
   /**
    * Constructor for creating a cell editor with panel as editor component.
    *
    * @param component the editor component, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if the component is <code>null</code>.
    *
    * @see UTEditorComponent
    */
   public UTCellEditor(final UTEditorComponent component)
   {
      /* Call dummy super constructor, as DefaultCellEditor does not provide
       * a default constructor to call. If we don't call the super class
       * constructor the compiler calls the default constructor and gives
       * compile error.
       */
      super(new JComboBox());

      if(component == null)
         throw new IllegalArgumentException("component can not be null");

      editorComponent = component;
      clickCountToStart = 2;

      //override the behavior of delegate object
      delegate = new EditorDelegate()
      {
         /**
          * Sets the value to the editing control of this editor component.
          * The value is set based on the editing control.
          *
          * @param value the value to be set.
          */
         public void setValue(Object value)
         {
            JComponent editor = component.getEditorComponent();

            if(editor instanceof JTextField)
            {
               JTextField textField = (JTextField)editor;
               textField.setText((value != null) ? value.toString() : "");
            }
            else if(editor instanceof JCheckBox )
            {
               JCheckBox checkBox = (JCheckBox)editor;

               boolean selected = false;
               if (value instanceof Boolean) {
                  selected = ((Boolean)value).booleanValue();
               }
               else if (value instanceof String) {
                  selected = ((String)value).equalsIgnoreCase("true");
               }
               checkBox.setSelected(selected);
            }
            else if(editor instanceof JComboBox)
            {
               JComboBox comboBox = (JComboBox)editor;
               comboBox.setSelectedItem(value);
            }
            else if(editor instanceof JList)
            {
               JList listBox = (JList)editor;
               listBox.setSelectedValue(value, true);
            }
         }

         /**
          * Gets value from editor control. The value is based on the editor
          * control. If the control is check box, it returns a
          * <code>Boolean</code> object.
          *
          * @return the cell editor value, may be <code>null</code>
          */
         public Object getCellEditorValue()
         {
            JComponent editor = component.getEditorComponent();
            if(editor instanceof JTextField)
            {
               JTextField textField = (JTextField)editor;
               return textField.getText();
            }
            else if(editor instanceof JCheckBox )
            {
               JCheckBox checkBox = (JCheckBox)editor;
               return checkBox.isSelected();
            }
            else if(editor instanceof JComboBox)
            {
               JComboBox comboBox = (JComboBox)editor;
               return comboBox.getSelectedItem();
            }
            else if(editor instanceof JList)
            {
               JList listBox = (JList)editor;
               return listBox.getSelectedValue();
            }
            return null;
         }
      };

      component.addActionListener(delegate);
   }
}
