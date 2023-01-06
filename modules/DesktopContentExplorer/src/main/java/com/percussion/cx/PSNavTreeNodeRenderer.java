/******************************************************************************
 *
 * [ PSNavTreeNodeRenderer.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.cx;

import com.percussion.border.PSFocusBorder;
import com.percussion.cx.objectstore.PSNode;

import javax.accessibility.AccessibleContext;
import javax.swing.BorderFactory;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;

/**
* The default node renderer to use with the navigation tree to display
* different icons based on each node type and expansion state. Sets the
* background selection color, font and text foreground color based on the
* display options set by user.
*/
public class PSNavTreeNodeRenderer extends DefaultTreeCellRenderer
{   
   
   public PSNavTreeNodeRenderer(PSContentExplorerApplet applet){
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      
      m_applet = applet; 
   }
   
   /**
    * Overridden to implement the behavior defined in the class description.
    * Gets the icons, colors and font from <code>UIManager</code> user
    * defaults. Displays the node with different background color if the node
    * rendering is undergoing a drag as a drop-target. It shows different 
    * colors depending on whether the node accepts the drop (selection color)
    * or not (light gray). See super's description for parameters and more 
    * information.
    */
   public Component getTreeCellRendererComponent(JTree tree, Object value,
      boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
   {
      PSDisplayOptions dispOptions =
         (PSDisplayOptions)UIManager.getDefaults().get(
            PSContentExplorerConstants.DISPLAY_OPTIONS);

      if (dispOptions != null)
      {
         setFont(dispOptions.getFont());
         setBackgroundSelectionColor(dispOptions.getHighlightColor());
         setTextSelectionColor(dispOptions.getHighlightTextColor());
         setTextNonSelectionColor(dispOptions.getForeGroundColor());
         setBorderSelectionColor(dispOptions.getFocusColor());
      }

      if (hasFocus)
      {
         setBorder(new PSFocusBorder(1, dispOptions, true));
      }
      else
      {
         setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
      }
      
      super.getTreeCellRendererComponent(
         tree, value, sel, expanded, leaf, row, hasFocus);
      
      PSNavigationTree.PSTreeNode targetNode = (PSNavigationTree.PSTreeNode)value;
      PSNode data = (PSNode)targetNode.getUserObject();
      String iconKey = data.getIconKey();
      if(PSCxUtil.shouldFolderBeMarked(data, null, false, m_applet))
      {
         iconKey += "Marked";
      }
      setIcon(PSImageIconLoader.loadIcon(iconKey, expanded, m_applet));

      // Set the accessibilityContext
      accessibleContext = 
         new PSTreeNodeAccContext((PSNavigationTree) tree, targetNode);

      return this;
   }
   
   
   
   /* (non-Javadoc)
    * @see java.awt.Component#getAccessibleContext()
    */
   public AccessibleContext getAccessibleContext()
   {
      return accessibleContext;
   }
   
   /**
    * A reference back to the applet.
    */
   private PSContentExplorerApplet m_applet;

}
