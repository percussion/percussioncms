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

 package com.percussion.cx;

import com.percussion.cx.objectstore.PSNode;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.guitools.PSDialog;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * Utility class of static helper method for the Content Explorer
 * applet. Can not be instantiated.
 */
 public final class PSCxUtil
 {
   // Private constructor so that this class can not be
   // instantiated.
   private PSCxUtil(){}

     public static void showStackTraceDialog(Throwable throwable,
                                             String title) {/*from  w  w w .  j av a2s  .  co  m*/
         String message = throwable.getMessage() == null ? throwable
                 .toString() : throwable.getMessage();
         showStackTraceDialog(throwable, title, message);
     }

     public static void showStackTraceDialog(Throwable throwable,
                                             String title, String message) {
         Window window = DefaultKeyboardFocusManager
                 .getCurrentKeyboardFocusManager().getActiveWindow();
         showStackTraceDialog(throwable, window, title, message);
     }

     public static void showStackTraceDialog(Throwable throwable,
                                             Component parentComponent, String title, String message) {
         final String more = "More";
         // create stack strace panel
         JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         JLabel label = new JLabel(more + ">>");
         labelPanel.add(label);

         JTextArea straceTa = new JTextArea();
         final JScrollPane taPane = new JScrollPane(straceTa);
         taPane.setPreferredSize(new Dimension(360, 240));
         taPane.setVisible(false);
         // print stack trace into textarea
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         throwable.printStackTrace(new PrintStream(out));
         straceTa.setForeground(Color.RED);
         straceTa.setText(new String(out.toByteArray()));

         final JPanel stracePanel = new JPanel(new BorderLayout());
         stracePanel.add(labelPanel, BorderLayout.NORTH);
         stracePanel.add(taPane, BorderLayout.CENTER);

         label.setForeground(Color.BLUE);
         label.setCursor(new Cursor(Cursor.HAND_CURSOR));
         label.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 JLabel tmpLab = (JLabel) e.getSource();
                 if (tmpLab.getText().equals(more + ">>")) {
                     tmpLab.setText("<<" + more);
                     taPane.setVisible(true);
                 } else {
                     tmpLab.setText(more + ">>");
                     taPane.setVisible(false);
                 }
                 SwingUtilities.getWindowAncestor(taPane).pack();
             };
         });

         JPanel panel = new JPanel(new BorderLayout());
         panel.add(new JLabel(message), BorderLayout.NORTH);
         panel.add(stracePanel, BorderLayout.CENTER);

         JOptionPane pane = new JOptionPane(panel, JOptionPane.ERROR_MESSAGE);
         JDialog dialog = pane.createDialog(parentComponent, title);
         int maxWidth = Toolkit.getDefaultToolkit().getScreenSize().width * 2 / 3;
         if (dialog.getWidth() > maxWidth) {
             dialog.setSize(new Dimension(maxWidth, dialog.getHeight()));
             setLocationRelativeTo(dialog, parentComponent);
         }
         dialog.setResizable(true);
         dialog.pack();
         dialog.setVisible(true);
         dialog.dispose();
     }

     public static void setLocationRelativeTo(Component c1, Component c2) {
         Container root = null;

         if (c2 != null) {
             if (c2 instanceof Window || c2 instanceof Applet) {
                 root = (Container) c2;
             } else {
                 Container parent;
                 for (parent = c2.getParent(); parent != null; parent = parent
                         .getParent()) {
                     if (parent instanceof Window
                             || parent instanceof Applet) {
                         root = parent;
                         break;
                     }
                 }
             }
         }

         if ((c2 != null && !c2.isShowing()) || root == null
                 || !root.isShowing()) {
             Dimension paneSize = c1.getSize();

             Point centerPoint = GraphicsEnvironment
                     .getLocalGraphicsEnvironment().getCenterPoint();
             c1.setLocation(centerPoint.x - paneSize.width / 2,
                     centerPoint.y - paneSize.height / 2);
         } else {
             Dimension invokerSize = c2.getSize();
             Point invokerScreenLocation = c2.getLocation(); // by longrm:
             // c2.getLocationOnScreen();

             Rectangle windowBounds = c1.getBounds();
             int dx = invokerScreenLocation.x
                     + ((invokerSize.width - windowBounds.width) >> 1);
             int dy = invokerScreenLocation.y
                     + ((invokerSize.height - windowBounds.height) >> 1);
             Rectangle ss = root.getGraphicsConfiguration().getBounds();

             // Adjust for bottom edge being offscreen
             if (dy + windowBounds.height > ss.y + ss.height) {
                 dy = ss.y + ss.height - windowBounds.height;
                 if (invokerScreenLocation.x - ss.x + invokerSize.width / 2 < ss.width / 2) {
                     dx = invokerScreenLocation.x + invokerSize.width;
                 } else {
                     dx = invokerScreenLocation.x - windowBounds.width;
                 }
             }

             // Avoid being placed off the edge of the screen
             if (dx + windowBounds.width > ss.x + ss.width) {
                 dx = ss.x + ss.width - windowBounds.width;
             }
             if (dx < ss.x)
                 dx = ss.x;
             if (dy < ss.y)
                 dy = ss.y;

             c1.setLocation(dx, dy);
         }
     }


     /**
    * Adjusts popup location to try to keep the popup within the
    * specified bounds.
    * @param popup the popup to be adjusted. Can not be <code>null</code>.
    * @return Point representing the new location
    */
   public static Point adjustPopupLocation(JPopupMenu popup)
   {
      Dimension popupDims = popup.getSize();
      Point loc = popup.getLocationOnScreen();
      
      Rectangle bounds = PSDialog.getScreenBoundsAt(loc);
     
      int x = loc.x;
      int y = loc.y;

      // Do we need to adjust the Y coordinate?
      if((bounds.getMaxY() - (popupDims.getHeight() + loc.getY())) < 0)
      y = (int)Math.max(
         loc.getY() - Math.max(popupDims.getHeight() -
            (bounds.getMaxY() - loc.getY()), 0.0) - 40, 0.0);
      // Do we need to adjust the X coordinate?
      if((bounds.getMaxX() - (popupDims.getWidth() + loc.getX())) < 0)
      x = (int)Math.max(
         loc.getX() - Math.max(popupDims.getWidth() -
            (bounds.getMaxX() - loc.getX()), 0.0), 0.0);
      loc.setLocation(x, y);

      popup.setLocation(loc);

      return loc;

   }
   
   

   /**
    * Will determine if this is a mouse menu gesture, for Windows this
    * would be a right button click and for Mac this would be a control-click.
    * This is only used in a mouse event that captures a button click
    * or release.
    * @param event the <code>MouseEvent</code> object, cannot be
    * <code>null</code>.
    * @return <code>true</code> if this is a menu event.
    */
   public static boolean isMouseMenuGesture(MouseEvent event, PSContentExplorerApplet applet)
   {
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      
      if(applet.isMacPlatform() && event.isControlDown() ||
         SwingUtilities.isRightMouseButton(event))
      {
         return true;
      }
      return false;
   }


   /**
    * Returns a valid window background color for windows and
    * Mac
    * @return valid window background Color, Never <code>null</code>
    */
   public static Color getWindowBkgColor(PSContentExplorerApplet applet)
   {
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      
      return applet.isMacPlatform()
         ? Color.white
         : SystemColor.window;
   }
   
   /**
    * Indicates if the folder should be marked for publish, either because it
    * is itself marked or one of its ancestors is marked.
    * 
    * @param node PSNode representing a folder, may be <code>null</code>.
    * @param parentnode PSNode representing the parent folder, 
    * may be <code>null</code>.
    * @param skipSelfCheck, if <code>true</code> then only check for marked
    * ancestors and ignore if self is marked
    * @return <code>true</code> if the folder should be marked.
    */
   public static boolean shouldFolderBeMarked(
           PSNode node, PSNode parentnode, boolean skipSelfCheck, PSContentExplorerApplet applet)
   {
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      
      boolean shouldMark = false;
      boolean firstIteration = true;
      if(node == null && parentnode == null)
         return false;
      // If node passed in is null but parentnode exists
      // then we start the check at the parentnode level.
      // (this is the case for a new folder)
      if(node == null && parentnode != null)
      {
         node = parentnode;
         skipSelfCheck = false;
      }
      TreeNode treeNode = node.getAssociatedTreeNode();
      while(treeNode != null)
      {
         if(!(firstIteration && skipSelfCheck))
         {
            PSNode data = 
               (PSNode)((PSNavigationTree.PSTreeNode)treeNode)
                  .getUserObject();
            if (data.isAnyFolderType() 
               && applet.getFlaggedFolderSet().contains(
                     data.getContentId()))
            {   
               shouldMark = true;
               break;
            }
         }  
         treeNode = treeNode.getParent();
         firstIteration = false;
      }
      
      return shouldMark;
   }
   
   /**
    * Gets the icon paths for the supplied item locators.
    * 
    * @param items list of locators must not be <code>null</code>.
    * @return Map of content ids and their icon paths. Never <code>null</code>
    *         may be empty if there is an error getting the icons. The path may
    *         be empty for content ids whose contenttypes do not have icons
    *         associated with them.
    */
   public static Map<String,String> getItemIcons(List<PSLocator> items, PSContentExplorerApplet applet)
   {
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      
      Map<String, String> itemIconMap = new HashMap<String, String>();
      // create xml document from the given list of items
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "ItemLocators");
      for (PSLocator locator : items)
      {
         root.appendChild(locator.toXml(doc));
      }
      try
      {
         Map<String, String> params = new HashMap<String, String>();
         params.put("ItemLocators", PSXmlDocumentBuilder.toString(doc));
         Document respDoc = applet.getApplet().getXMLDocument(
               "../sys_cxSupport/getItemIcons.xml", params);
         NodeList nl = respDoc.getElementsByTagName("Item");
         for (int i = 0; i < nl.getLength(); i++)
         {
            Element elem = (Element) nl.item(i);
            itemIconMap.put(elem.getAttribute("cid"), elem
                  .getAttribute("path"));
         }
      }
      catch (Exception e)
      {
         applet.getApplet().debugMessage(e);
      }
      return itemIconMap;
   }

     private static ResourceBundle m_res = null;
     public static ResourceBundle getResources()
     {
         if (m_res == null)
             m_res = ResourceBundle.getBundle(
                     getResourceName(),
                     Locale.getDefault());
         return m_res;
     }

     /**
      * Gets resource file name.
      *
      * @return resource file name, never <code>null</code> or empty.
      */
     public static String getResourceName()
     {
         return "com.percussion.E2Designer.admin.PSServerAdminResources";
     }

 }
