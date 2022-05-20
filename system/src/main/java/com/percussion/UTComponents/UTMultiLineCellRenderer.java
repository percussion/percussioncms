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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.Serializable;

/**
 * the cell renderer when JTextArea object is stored within a table cell.
 */
public class UTMultiLineCellRenderer extends JTextArea
   implements TableCellRenderer, ListCellRenderer, Serializable
{
   public UTMultiLineCellRenderer()
   {
      super();
      noFocusBorder = new EmptyBorder(1, 2, 1, 2);
      setLineWrap(true);
      setWrapStyleWord(true);
      setOpaque(true);
      setBorder(noFocusBorder);
   }

   public void setForeground(Color c)
   {
      super.setForeground(c);
      unselectedForeground = c;
   }

   public void setBackground(Color c)
   {
      super.setBackground(c);
      unselectedBackground = c;
   }

   public void updateUI()
   {
      super.updateUI();
      setForeground(null);
      setBackground(null);
   }

   public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column)
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
         if (table.isCellEditable(row, column))
         {
            super.setForeground( UIManager.getColor("Table.focusCellForeground") );
            super.setBackground( UIManager.getColor("Table.focusCellBackground") );
         }
      }
      else
      {
         setBorder(noFocusBorder);
      }

      setValue(value);

      return this;
   }

   public Component getListCellRendererComponent(JList list, Object value,
      int index, boolean isSelected, boolean cellHasFocus)
   {
      if (isSelected)
      {
         super.setForeground(list.getSelectionForeground());
         super.setBackground(list.getSelectionBackground());
      }
      else
      {
         super.setForeground((unselectedForeground != null) ? unselectedForeground
            : list.getForeground());
         super.setBackground((unselectedBackground != null) ? unselectedBackground
            : list.getBackground());
      }

      setFont(list.getFont());

      if (cellHasFocus)
      {
         setBorder(UIManager.getBorder("List.focusCellHighlightBorder"));
      }
      else
      {
         setBorder(noFocusBorder);
      }

      setValue(value);

      return this;
   }

   protected void setValue(Object value)
   {
      setText((value == null) ? "" : value.toString());
   }

   public static class UIResource extends UTMultiLineCellRenderer
      implements javax.swing.plaf.UIResource
   {
   }

   protected static Border noFocusBorder;

   private Color unselectedForeground;
   private Color unselectedBackground;

}


