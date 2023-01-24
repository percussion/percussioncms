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

package com.percussion.EditableListBox;

import java.awt.Color;
import java.awt.Component;
import java.io.Serializable;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 * The standard class for rendering (displaying) individual cells
 * in a EditableListBox.
 * <p>
 *
 * @see JTable
 */
public class EditableListBoxCellRenderer extends JLabel
    implements TableCellRenderer, Serializable
{

    protected static Border noFocusBorder; 

    // We need a place to store the color the JLabel should be returned 
    // to after its foreground and background colors have been set 
    // to the selection background color. 
    // These ivars will be made protected when their names are finalized. 
    private Color unselectedForeground;
    private Color unselectedBackground; 

    /**
     * Creates a default table cell renderer.
     */
    public EditableListBoxCellRenderer()
    {
      super();
      noFocusBorder = new EmptyBorder(1, 2, 1, 2);
      setOpaque(true);
      setBorder(noFocusBorder);
    }

    /**
     * Overrides <code>JComponent.setForeground</code> to specify
     * the unselected-foreground color using the specified color.
     */
    public void setForeground(Color c)
    {
      super.setForeground(c);
      unselectedForeground = c;
    }

    /**
     * Overrides <code>JComponent.setForeground</code> to specify
     * the unselected-background color using the specified color.
     */
    public void setBackground(Color c)
    {
      super.setBackground(c);
      unselectedBackground = c;
    }

    /**
     * Notification from the UIManager that the L&F has changed. 
     * Replaces the current UI object with the latest version from the 
     * UIManager.
     *
     * @see javax.swing.JComponent#updateUI
     */
    public void updateUI()
    {
      super.updateUI();
      setForeground(null);
      setBackground(null);
    }
    
    // implements javax.swing.table.TableCellRenderer
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row, int column)
    {
      if (isSelected)
      {
         super.setForeground(table.getSelectionForeground());
         super.setBackground(table.getSelectionBackground());
      }
       else
      {
         super.setForeground((unselectedForeground != null) ? unselectedForeground
                                                            : table.getForeground());
        super.setBackground((unselectedBackground != null) ? unselectedBackground
                                                            : table.getBackground());
      }

       setFont(table.getFont());

       if (hasFocus)
      {
         setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
      }
      else
      {
         setBorder(noFocusBorder);
      }

      // if cell value has an image, show it; else do nothing
      // if image is null, nothing is displayed.
      if (value instanceof ICellImageHelper)
      {
        setIcon(((ICellImageHelper)value).getImage());
      }
      
      setValue(value);

      return this;
    }

    protected void setValue(Object value)
    {
      setText((value == null) ? "" : value.toString());
    }


    /**
     * A subclass of DefaultTableCellRenderer that implements UIResource.
     * DefaultTableCellRenderer doesn't implement UIResource
     * directly so that applications can safely override the
     * cellRenderer property with DefaultTableCellRenderer subclasses.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases.  The current serialization support is appropriate
     * for short term storage or RMI between applications running the same
     * version of Swing.  A future release of Swing will provide support for
     * long term persistence.
     */
    public static class UIResource extends EditableListBoxCellRenderer
        implements javax.swing.plaf.UIResource
    {}

}
