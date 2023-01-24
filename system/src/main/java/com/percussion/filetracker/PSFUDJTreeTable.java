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
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;

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

