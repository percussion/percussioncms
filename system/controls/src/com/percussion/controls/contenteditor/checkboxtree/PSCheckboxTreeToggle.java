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
package com.percussion.controls.contenteditor.checkboxtree;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * The selection toggle controls the mouse behavior. A single click toggles
 * the selected state of the checkbox node.
 */
public class PSCheckboxTreeToggle extends MouseInputAdapter 
   implements IPSCheckboxTreeListener
{
   /**
    * Constructs a toggle event handler for use with the specified tree.
    * 
    * @param tree the tree that this node toggle is part of, not 
    *    <code>null</code>.
    */
   public PSCheckboxTreeToggle(JTree tree)
   {
      if (tree == null)
         throw new IllegalArgumentException("tree cannot be null");
      
      m_tree = tree;
   }

   /* (non-Javadoc)
    * @see javax.swing.event.TreeSelectionListener#valueChanged(
    *    javax.swing.event.TreeSelectionEvent)
    */
   public void valueChanged(TreeSelectionEvent event)
   {
      // assumes "single select" model
      TreePath path = event.getPath();
      togglePath(path);
   }

   /* (non-Javadoc)
    * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
    */
   @Override
   public void mousePressed(MouseEvent e)
   {
      TreePath selPath = m_tree.getPathForLocation(e.getX(), e.getY());
      if (selPath != null)
         togglePath(selPath);
   }

   /**
    * Toggle the current node
    * 
    * @param path the path to the current node, not <code>null</code>.
    */
   protected void togglePath(TreePath path)
   {
      if (path == null)
         throw new IllegalArgumentException("path cannot be null");
      
      Object node = path.getLastPathComponent();
      if (node instanceof PSCheckboxTreeNode)
      {
         PSCheckboxTreeNode ln = (PSCheckboxTreeNode) node;
         ln.toggleSelected();
         m_tree.repaint();
      }
   }
   
   /* (non-Javadoc)
    * @see IPSCheckboxTreeListener#getParameters()
    */
   public Map<String, String> getParameters()
   {
      return m_parameters;
   }

   /* (non-Javadoc)
    * @see IPSCheckboxTreeListener#setParameters()
    */
   public void setParameters(Map<String, String> parameters)
   {
      if (parameters == null)
         m_parameters = new HashMap<>();
      else
         m_parameters = parameters;
   }

   /**
    * The extra parameters set for this listener, never <code>null</code>, may
    * be empty.
    */
   private Map<String, String> m_parameters = new HashMap<>();

   /**
    * The containing tree, never <code>null</code> after construction.
    */
   private JTree m_tree = null;
}
