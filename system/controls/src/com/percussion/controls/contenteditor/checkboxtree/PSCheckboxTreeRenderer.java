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
