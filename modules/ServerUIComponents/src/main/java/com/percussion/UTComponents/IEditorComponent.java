/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
