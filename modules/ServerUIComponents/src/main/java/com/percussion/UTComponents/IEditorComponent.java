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
import java.awt.event.ActionListener;

/**
 * This interface defines methods which are required for using any
 * object which extends <code>JComponent</code> as editor component in
 * {@link UTCellEditor} which is used as table cell editor.
 */
public interface IEditorComponent
{
   /**
    * Returns the basic editor component in an editor. For example it can be any
    * of the following controls, 'JTextField', 'JComboBox', 'JCheckBox', 'JList'.
    *
    * @return the editor component, may not be <code>null</code>
    */
   public JComponent getEditorComponent();

   /**
    * Add listener to the component on whose action the cell editing to be
    * stopped.
    *
    * @param l the action listener, may not be <code>null</code>
    */
   public void addActionListener(ActionListener l);
}
