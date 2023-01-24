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

package com.percussion.filetracker;

import javax.swing.*;
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

