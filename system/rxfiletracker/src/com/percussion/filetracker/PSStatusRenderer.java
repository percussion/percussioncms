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

import java.awt.Color;
import java.awt.Component;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * This class extends DefaultTreeCellRenderer to display right icons and tool
 * tip text to indicate the current state of the nodes in the application.
 */
class PSStatusRenderer extends DefaultTreeCellRenderer
{
   /**
    * Default constructor. Initializes all icons.
    */
   public PSStatusRenderer()
   {
      try
      {
         URL url = getClass().getResource(MainFrame.getRes().
                                                getString("iconAppNode"));
         ms_IconApp = new ImageIcon(url);

         url = getClass().getResource(MainFrame.getRes().
                                                getString("iconAppNodeAbsent"));
         ms_IconAppAbsent = new ImageIcon(url);

         url = getClass().getResource(MainFrame.getRes().
                                             getString("iconContentTypeNode"));
         ms_IconContentType = new ImageIcon(url);

         url = getClass().getResource(MainFrame.getRes().
                                       getString("iconContentTypeNodeAbsent"));
         ms_IconContentTypeAbsent = new ImageIcon(url);

         url = getClass().getResource(MainFrame.getRes().
                                       getString("iconContentItemNode"));
         ms_IconContentItem = new ImageIcon(url);

         url = getClass().getResource(MainFrame.getRes().
                                    getString("iconContentItemNodeAbsent"));
         ms_IconContentItemAbsent = new ImageIcon(url);

         url = getClass().getResource(MainFrame.getRes().
                                             getString("iconFileNode"));
         ms_IconFile = new ImageIcon(url);

         url = getClass().getResource(MainFrame.getRes().
                                          getString("iconFileNodeAbsent"));
         ms_IconFileAbsent = new ImageIcon(url);

         url = getClass().getResource(MainFrame.getRes().
                                             getString("iconFileNodeInSync"));
         ms_IconFileInSync = new ImageIcon(url);

         url = getClass().getResource(MainFrame.getRes().
                                             getString("iconFileNodeRemoteNew"));
         ms_IconFileRemoteNew = new ImageIcon(url);

         url = getClass().getResource(MainFrame.getRes().
                                          getString("iconFileNodeLocalNew"));
         ms_IconFileLocalNew = new ImageIcon(url);
      }
      catch(Exception e) //never happens since we ship the images as part of JAR.
      {
         e.printStackTrace();
      }
   }

   /**
    * sets the icon and tool tip text for the supplied node.
    *
    * @param current node as IPSFUDNode
    *
    * @param true if current node is selected, false otherwise
    *
    */
   private void setIconAndToolTipText(IPSFUDNode node, boolean sel)
   {
      setToolTipText(node.getStatusText());

      int code = node.getStatusCode();
      if(node instanceof PSFUDAppNode)
      {
         switch(code)
         {
            case IPSFUDNode.STATUS_CODE_ABSENT:
               setIcon(ms_IconAppAbsent);
               break;
            default:
               setIcon(ms_IconApp);
               break;
         }
      }
      else if(node instanceof PSFUDContentTypeNode)
      {
         switch(code)
         {
            case IPSFUDNode.STATUS_CODE_ABSENT:
               setIcon(ms_IconContentTypeAbsent);
               break;
            default:
               setIcon(ms_IconContentType);
               break;
         }
      }
      else if(node instanceof PSFUDContentItemNode)
      {
         switch(code)
         {
            case IPSFUDNode.STATUS_CODE_ABSENT:
               setIcon(ms_IconContentItemAbsent);
               break;
            default:
               setIcon(ms_IconContentItem);
               break;
         }
      }
      else if(node instanceof PSFUDFileNode)
      {
         if(((PSFUDFileNode)node).isLocalCopy())
         {
            if(sel)
               setForeground(Color.lightGray);
            else
               setForeground(Color.blue);
         }

         switch(code)
         {
            case IPSFUDNode.STATUS_CODE_ABSENT:
               setIcon(ms_IconFileAbsent);
               break;
            case IPSFUDNode.STATUS_CODE_INSYNC:
               setIcon(ms_IconFileInSync);
               break;
            case IPSFUDNode.STATUS_CODE_REMOTENEW:
               setIcon(ms_IconFileRemoteNew);
               break;
            case IPSFUDNode.STATUS_CODE_LOCALNEW:
               setIcon(ms_IconFileLocalNew);
               break;
            default:
               setIcon(ms_IconFile);
               break;
         }
      }
   }


   /**
    * Override the method from the base class.
    */
   public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus)
   {
      super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus);

      if(!(value instanceof IPSFUDNode))
         return this;

      setIconAndToolTipText((IPSFUDNode)value, sel);

      return this;
   }

   /**
    * All icons indicating various status' for the nodes shall be created
    * only once
    */
   private static ImageIcon ms_IconApp = null;
   private static ImageIcon ms_IconAppAbsent = null;

   private static ImageIcon ms_IconContentType = null;
   private static ImageIcon ms_IconContentTypeAbsent = null;

   private static ImageIcon ms_IconContentItem = null;
   private static ImageIcon ms_IconContentItemAbsent = null;

   private static ImageIcon ms_IconFile = null;
   private static ImageIcon ms_IconFileAbsent = null;

   private static ImageIcon ms_IconFileInSync = null;
   private static ImageIcon ms_IconFileRemoteNew = null;
   private static ImageIcon ms_IconFileLocalNew = null;
}
