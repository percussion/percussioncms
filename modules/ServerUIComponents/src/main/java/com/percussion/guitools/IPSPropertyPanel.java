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

package com.percussion.guitools;

import javax.swing.*;
import java.util.List;

public interface IPSPropertyPanel
{

   /**
    * Convenience method for {@link #addPropertyRow(String, JComponent[])
    * addPropertyRow(String, control[])}.
    */
   public void addPropertyRow(String name, JComponent control);

   /**
    * Convenience method for {@link #addPropertyRow(String, JComponent[], 
    * JComponent, char, String) addPropertyRow(String, control[], control, 
    * mnemonic, null)}.
    */
   public void addPropertyRow(String name, JComponent control, char mnemonic);

   /**
    * Convenience method for {@link #addPropertyRow(String, JComponent[], 
    * JComponent, char, String) addPropertyRow(String, control[], mnControl, 
    * mnemonic, tooltip)}.
    */
   public void addPropertyRow(String name, JComponent control,
      JComponent mnControl, char mnemonic, String tooltip);

   /**
    * Convenience method for {@link #addPropertyRow(String, JComponent[], 
    * JComponent, char, String) addPropertyRow(String, control[], null, 0, 
    * null)}.
    */
   public void addPropertyRow(String name, JComponent[] controls);

   /**
    * Adds a row to the panel with label of specified name to the left column
    * and all the controls to the right column. Sets right alignment for label
    * and left alignment for controls. Label will be aligned vertically to the
    * center of controls.
    *
    * @param name The name to use for labeling the controls, may not be 
    *    <code>null</code> or empty.
    * @param controls the list of controls to add, may not be <code>null</code>
    *    or empty
    * @param mnemonicControl the control that set mnemonic with, it may be 
    *    <code>null</code> if no control is set with the specified mnemonic.
    * @param mnemonic the mnemonic key to use, provide 0 if no mnemonic is
    *    used.
    * @param tooltip the tooltip for the <code>mnemonicControl</code>. It may
    *    be <code>null</code> or empty if no tooltip to be set. 
    */
   public void addPropertyRow(String name, JComponent[] controls,
      JComponent mnemonicControl, char mnemonic, String tooltip);
   

   /**
    * Hides the row at the specified index.  Row may be later un-hidden by 
    * calling {@link #showRow(int)}
    * 
    * @param index The index of the row to hide, must be less than the value
    * returned by {@link #getRowCount()} and greater than or equal to 0.
    */
   public void hideRow(int index);

   /**
    * Shows the row at the specified index.  Only needs to be called if the row
    * was previously hidden by a call to {@link #hideRow(int)}.
    * 
    * @param index The index of the row to show, must be less than the value
    * returned by {@link #getRowCount()} and greater than or equal to 0.
    */
   public void showRow(int index);

   /**
    * Hides the row with the specified label.  Row may be later un-hidden by 
    * calling {@link #showRow(String)}
    * 
    * @param label The label of the row to hide, may not be <code>null</code> or 
    * empty.  Hides the first row with a {@link JLabel} that has a matching 
    * text, case-sensitive.
    * 
    * @return <code>true</code> if a matching row was found to hide, 
    * <code>false</code> otherwise. 
    */
   public boolean hideRow(String label);

   /**
    * Shows the row with the specified label.  Row may be later un-hidden by 
    * calling {@link #showRow(String)}
    * 
    * @param label The label of the row to show, may not be <code>null</code> or 
    * empty.  Hides the first row with a {@link JLabel} that has a matching 
    * text, case-sensitive.
    * 
    * @return <code>true</code> if a matching row was found to show, 
    * <code>false</code> otherwise. 
    */
   public boolean showRow(String label);

   /**
    * Get the number of rows that have been added.
    * 
    * @return The number.
    */
   public int getRowCount();
   
   /**
    * Gets List of components of the first row found with a <code>JLabel</code> 
    * that has text matching the supplied <code>label</code>.
    *  
    * @param label The text to match, assumed not <code>null</code> or empty.
    * 
    * @return The component list of the matching row, or <code>null</code> 
    * if no match is found.
    */
   public List getMatchingRowByLabel(String label);

}