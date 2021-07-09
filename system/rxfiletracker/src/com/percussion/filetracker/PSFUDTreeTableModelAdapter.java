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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.filetracker;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

/**
 * This is a wrapper class takes a TreeTableModel and implements
 * the table model interface. The implementation is trivial, with
 * all of the event dispatching support provided by the superclass:
 * the AbstractTableModel.
 *
 */

public class PSFUDTreeTableModelAdapter extends AbstractTableModel
{
   JTree m_Tree;
   PSFUDTreeTableModel m_TreeTableModel;

   public PSFUDTreeTableModelAdapter(PSFUDTreeTableModel treeTableModel,
                  JTree tree)
   {
      m_Tree = tree;
      m_TreeTableModel = treeTableModel;

      m_Tree.addTreeExpansionListener(new TreeExpansionListener()
      {
         // Don't use fireTableRowsInserted() here;
         // the selection model would get  updated twice.
         public void treeExpanded(TreeExpansionEvent event)
         {
            fireTableDataChanged();
         }

         public void treeCollapsed(TreeExpansionEvent event)
         {
            fireTableDataChanged();
         }
      });
   }

   /**
    * Returns the number of availible rows.
    *
    * @return row count as int.
    *
    */
   public int getRowCount()
   {
      return m_Tree.getRowCount();
   }

   /**
    * Returns Tree Node for a given row number.
    *
    * @param row as int
    *
    * @return tree node as Object (TreePath)
    *
    */
   protected Object nodeForRow(int row)
   {
      TreePath treePath = m_Tree.getPathForRow(row);
      return treePath.getLastPathComponent();
   }

   //Wrappers, implementing TableModel interface.

   /**
    * Returns the number ofs availible column.
    *
    * @return number of columns as int
    *
    */
   public int getColumnCount()
   {
      return m_TreeTableModel.getColumnCount();
   }

   /**
    * Returns the name for column number <code>column</code>.
    *
    * @param column number as int
    *
    * @return column name as String
    *
    */
   public String getColumnName(int column)
   {
      return m_TreeTableModel.getColumnName(column);
   }

   /**
    * Returns the type for column number <code>column</code>.
    *
    * @param column number as int
    *
    * @return column class as Class
    *
    */
   public Class getColumnClass(int column)
   {
      return m_TreeTableModel.getColumnClass(column);
   }

   /**
    * Returns the value to be displayed for node <code>node</code>,
    * at column number <code>column</code>.
    *
    * @param row number as int
    *
    * @param column number as int
    *
    * @return value of cell as Object
    *
    */
   public Object getValueAt(int row, int column)
   {
      return m_TreeTableModel.getValueAt(nodeForRow(row), column);
   }

   /**
    * Indicates whether the the value for node <code>node</code>,
    * at column number <code>column</code> is editable.
    *
    * @param row number as int
    *
    * @param column number as int
    *
    * @return true if editable, false otherwise
    *
    */
   public boolean isCellEditable(int row, int column)
   {
      return m_TreeTableModel.isCellEditable(nodeForRow(row), column);
   }

   /**
    * Sets the value for node <code>node</code>,
    * at column number <code>column</code>.
    *
    * @param value as Object
    *
    * @param row number as int
    *
    * @param column number as int
    *
    */
   public void setValueAt(Object value, int row, int column)
   {
      m_TreeTableModel.setValueAt(value, nodeForRow(row), column);
   }
}

