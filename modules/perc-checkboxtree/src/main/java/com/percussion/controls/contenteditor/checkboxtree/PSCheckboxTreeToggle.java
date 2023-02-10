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
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
