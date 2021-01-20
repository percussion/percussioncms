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

package com.percussion.filetracker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * This class creates a simple PSFUDJTreeTable component, by using a JTree as a
 * renderer (and editor) for the cells in a particular column in the JTable.
 *<p>
 * PSFUDJTreeTable is a combination of JTable and JTree. Acually derived from
 * JTable and uses JTree as cell renderer. This way we get a kind of tree list
 * view
 *
 */
public class PSFUDJTreeTable extends JTable
{
   protected TreeTableCellRenderer tree;

   /**
    * Constructor.
    *
    * @param tree table model as PSFUDTreeTableModel
    *
    */
   public PSFUDJTreeTable(PSFUDTreeTableModel treeTableModel)
   {
      super();

      // Create the tree. It will be used as a renderer and editor.
      tree = new TreeTableCellRenderer(treeTableModel);

      // Install a tableModel representing the visible rows in the tree.
      super.setModel(new PSFUDTreeTableModelAdapter(treeTableModel, tree));

      // Force the JTable and JTree to share their row selection models.
      tree.setSelectionModel(new DefaultTreeSelectionModel()
      {
         // Extend the implementation of the constructor, as if:
         /* public this() */
         {
            setSelectionModel(listSelectionModel);
         }
      }
      );

      // Install the tree editor renderer and editor.
      setDefaultRenderer(PSFUDTreeTableModel.class, tree);
      setDefaultEditor(PSFUDTreeTableModel.class, new TreeTableCellEditor());

      setShowGrid(true);
      setGridColor(new Color(223,223,223));
      setIntercellSpacing(new Dimension(1, 1));
      setRowHeight(20);

      this.getTableHeader().setReorderingAllowed(false);

      // Make the tree and table row heights the same.
      tree.setRowHeight(getRowHeight()+1); //add inter cell spacing too
      this.setCellSelectionEnabled(false);

      //Set a font that can display special characters like smart quotes.
      Font font = new Font("MS Sans Serif", Font.PLAIN, 12);
      tree.setFont(font);
      setFont(font);
      
      //Put the autoresize off
      sizeColumnsToFit(this.AUTO_RESIZE_OFF);
   }

   /**
    * Workaround for BasicTableUI anomaly. Make sure the UI never tries to
    * paint the editor. The UI currently uses different techniques to
    * paint the renderers and editors and overriding setBounds() below
    * is not the right thing to do for an editor. Returning -1 for the
    * editing row in this case, ensures the editor is never painted.
    */
   public int getEditingRow()
   {
      return (getColumnClass(editingColumn) == PSFUDTreeTableModel.class) ? 
            -1 : editingRow;
   }

   /**
    * Gets the list of all selected nodes.
    *
    * @return list of all selected PSFUDFileNode objects in the tree as an
    * ArrayList.
    *
    */
   public ArrayList getSelectedFileList()
   {
      ArrayList fileNodes = new ArrayList();
      TreePath[] paths = tree.getSelectionPaths();
      if(null == paths)
         return fileNodes;

      TreePath path = null;
      Object obj = null;
      for(int i=0; i<paths.length; i++)
      {
         path = paths[i];
         obj = path.getLastPathComponent();
         if(obj instanceof PSFUDFileNode)
            fileNodes.add(obj);
      }
      return fileNodes;
   }

   /**
    * Expands/Collapses all the nodes in the tree table
    *
    * @param bExpand <code>true</code> to expand <code>false</code> to collapse
    *
    */
   public void expandAll(boolean bExpand)
   {
      for(int i=0; i<tree.getRowCount(); i++)
      {
         if(bExpand)
            tree.expandRow(i);
         else
            tree.collapseRow(i);
      }
   }

   /**
    * The renderer used to display the tree nodes in the table , a JTree.
    */
   public class TreeTableCellRenderer
      extends JTree
      implements TableCellRenderer
   {
      protected int visibleRow;

      public TreeTableCellRenderer(TreeModel model)
      {
         super(model);
         PSStatusRenderer renderer = new PSStatusRenderer();
         renderer.setBorderSelectionColor(null);
         setCellRenderer(renderer);
      }

      public void setBounds(int x, int y, int w, int h)
      {
         super.setBounds(x, 0, w, PSFUDJTreeTable.this.getHeight());
      }

      public void paint(Graphics g)
      {
         g.translate(0, -visibleRow * getRowHeight());
         super.paint(g);
      }

      /**
       * Implementation of the method in the interface
       */
      public Component getTableCellRendererComponent(JTable table,
                         Object value,
                         boolean isSelected,
                         boolean hasFocus,
                         int row, int column)
      {
         if(isSelected)
            setBackground(table.getSelectionBackground());
         else
            setBackground(table.getBackground());

         visibleRow = row;
         return this;
      }
   }

   /**
    * The editor used to interact with tree nodes, a JTree.
    */
   public class TreeTableCellEditor
      extends AbstractCellEditor
      implements TableCellEditor
   {
      /**
       * Implementation of the method in the interface
       */
      public Component getTableCellEditorComponent(JTable table, Object value,
                       boolean isSelected, int r, int c)
      {
         return tree;
      }
   }
}

