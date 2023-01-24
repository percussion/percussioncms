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
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.net.URL;

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
