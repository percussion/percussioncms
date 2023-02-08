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
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

/**
 * The tree node as used with the checkbox tree applet control.
 */
public class PSCheckboxTreeNode extends DefaultMutableTreeNode
{
   /**
    * Constructs a new checkbox tree node for the supplied parameters.
    * 
    * @param id the node identifier, not <code>null</code> or empty. Should
    *    be unique but uniqueness is not enforced.
    * @param label the nodes display label, not <code>null</code> or empty.
    */
   public PSCheckboxTreeNode(String id, String label)
   {
      setNodeId(id);
      setNodeName(label);
   }
   
   /**
    * Get the renderer for the tree component.
    * 
    * @return the component render, never <code>null</code>.
    */
   public Component getRenderedComponent()
   {
      if (m_selectable)
      {
         JCheckBox box = new JCheckBox(m_nodeName, m_selected);
         box.setContentAreaFilled(false);

         return box;
      }
      else
      {
         return new JLabel(m_nodeName);
      }
   }

   /**
    * Determines if this node is selecable or not.
    * 
    * @return <code>true<code> if the node can be selected, <code>false</code>
    *    otherwise.
    */
   public boolean isSelectable()
   {
      return m_selectable;
   }

   /**
    * gets the selection of the current node.
    * 
    * @return <code>true</code> if this node already selected.
    */
   public boolean isSelected()
   {
      return m_selected;
   }

   /**
    * Sets the selection flag on this node.
    * 
    * @param select <code>true</code> to select this node, <code>false</code>
    *    to de-select it.
    */
   public void setSelected(boolean select)
   {
      m_selected = select;
   }

   /**
    * Toggle the current selection.
    */
   public void toggleSelected()
   {
      setSelected(!isSelected());
   }

   /**
    * Get the node id.
    * 
    * @return the node id, never <code>null</code> or empty.
    */
   public String getNodeId()
   {
      return m_nodeId;
   }

   /**
    * Set a new node id.
    * 
    * @param id the new node id, not <code>null</code> or empty.
    */
   public void setNodeId(String id)
   {
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException(
            "id cannot be null or empty");
      
      m_nodeId = id;
   }

   /**
    * Get the node name.
    * 
    * @return the node name, never <code>null</code> or empty.
    */
   public String getNodeName()
   {
      return m_nodeName;
   }

   /**
    * Set a new node name.
    * 
    * @param name the new node name, not <code>null</code> or empty.
    */
   public void setNodeName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
            "name cannot be null or empty");
      
      m_nodeName = name;
   }

   /**
    * Set if this item is selectable.
    * 
    * @param selectable <code>true</code> if the item is selectable, 
    *    <code>false</code> otherwise.
    */
   public void setSelectable(boolean selectable)
   {
      m_selectable = selectable;
   }

   /**
    * Is this item selected, <code>true</code> if it is, <code>false</code>
    * otherwise.
    */
   private boolean m_selected = false;

   /**
    * Is this node selectable, <code>true</code> if it is, <code>false</code>
    * otherwise. If so, it will have a checkbox.
    */
   private boolean m_selectable;

   /**
    * The internal id value returned when selected, never <code>null</code>
    * or empty. Should be unique, but this class does not enforce uniqueness.
    */
   private String m_nodeId;

   /**
    * The name or label for this node, never <code>null</code> or empty.
    */
   private String m_nodeName;

   /**
    * Generated serial version id.
    */
   private static final long serialVersionUID = -1276983430140508363L;
}
