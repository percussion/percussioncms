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
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The tree render controls the behavior on screen. This one returns the 
 * rendered component if the tree contains an instance of 
 * <code>PSCheckBoxTreeNode</code>.
 */   
public class PSCheckboxTreeRenderer extends DefaultTreeCellRenderer 
   implements IPSCheckboxTreeRenderer
{
   /* (non-Javadoc)
    * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(
    *    javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, 
    *    boolean)
    */
   @Override
   public Component getTreeCellRendererComponent(JTree tree, Object value,
      boolean isSelected, boolean isExpanded, boolean isLeaf, int row,
      boolean ownsFocus)
   {
      if (value instanceof PSCheckboxTreeNode)
      {
         PSCheckboxTreeNode node = (PSCheckboxTreeNode) value;
         return node.getRenderedComponent();
      }

      return super.getTreeCellRendererComponent(tree, value, isSelected,
         isExpanded, isLeaf, row, ownsFocus);
   }
   
   /* (non-Javadoc)
    * @see IPSCheckboxTreeRenderer#getParameters()
    */
   public Map<String, String> getParameters()
   {
      return m_parameters;
   }

   /* (non-Javadoc)
    * @see IPSCheckboxTreeRenderer#setParameters()
    */
   public void setParameters(Map<String, String> parameters)
   {
      if (parameters == null)
         m_parameters = new HashMap<>();
      else
         m_parameters = parameters;
   }

   /**
    * The extra parameters set for this renderer, never <code>null</code>, may
    * be empty.
    */
   private Map<String, String> m_parameters = new HashMap<>();
   
   /**
    * Generated serial version id.
    */
   private static final long serialVersionUID = -5871298110935968333L;
}
