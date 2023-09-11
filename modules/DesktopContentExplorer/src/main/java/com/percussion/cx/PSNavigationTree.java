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

import com.percussion.cx.guitools.PSMouseAdapter;
import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.cx.objectstore.PSNode;
import com.percussion.utils.collections.PSIteratorUtils;
import org.apache.log4j.Logger;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * The tree that builds tree model as per 'sys_Node.dtd'. See {@link
 * com.percussion.cx.objectstore.PSNode } for more information. Uses
 * {@link com.percussion.cx.PSNavTreeNodeRenderer } to render the nodes of the tree. Displays
 * pop-up menu for right-click on a node.
 */
public class PSNavigationTree
        extends JTree
        implements DragGestureListener, DropTargetListener
{
   static Logger log = Logger.getLogger(PSNavigationTree.class);

   /**
    * Serializable id
    */
   private static final long serialVersionUID = 1L;

   /**
    * Constructs the tree with supplied node and sets the listeners and renderer
    * on this tree. By default, it tries to expand as defined by the supplied
    * node tree.
    *
    * @param root the root node of tree, may not be <code>null</code>
    * @param view the view this tree supports, must be one of the <code>
    * PSUiMode.TYPE_VIEW_xxx</code> values.
    * @param manager the manager to use to load the children, may not be <code>
    * null</code>
    * @param ignoreRoot supply <code>true</code> to not to show the root node
    * in the tree and do not consider the root in the path, otherwise supply
    * <code>false</code>
    */
   public PSNavigationTree(
           PSNode root,
           String view,
           PSActionManager manager,
           boolean ignoreRoot)
   {
      if (root == null)
         throw new IllegalArgumentException("root may not be null.");

      if (!PSUiMode.isValidView(view))
         throw new IllegalArgumentException("view is invalid");

      m_view = view;

      if (manager == null)
         throw new IllegalArgumentException("manager may not be null.");

      if (manager.getApplet() == null)
         throw new IllegalArgumentException("applet must not be null");

      m_actManager = manager;
      m_applet = manager.getApplet();
      m_ignoreRoot = ignoreRoot;

      getSelectionModel().setSelectionMode(
              TreeSelectionModel.SINGLE_TREE_SELECTION);
      PSTreeNode rootNode = new PSTreeNode(root);
      DefaultTreeModel model = new DefaultTreeModel(rootNode);
      setModel(model);

      //does not display root node
      if (ignoreRoot)
      {
         setRootVisible(false);
         setShowsRootHandles(true);
      }

      //expand root node by default
      expandNode(rootNode);

      //set client property
      putClientProperty("JTree.lineStyle", "Angled");
      //set renderer
      setCellRenderer(new PSNavTreeNodeRenderer(m_applet));

      //set expansion and collapsion listeners to update the state of the node,
      //so that on refresh it remembers the state.

      addTreeExpansionListener(new TreeExpansionListener()
      {
         //Updates the state of expansion of node got collapsed to 'false'.
         public void treeCollapsed(TreeExpansionEvent e)
         {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
            PSNode data = (PSNode) node.getUserObject();
            data.setExpand(false);
         }

         //Updates the state of expansion of node got expanded to 'true'.
         public void treeExpanded(TreeExpansionEvent e)
         {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
            PSNode data = (PSNode) node.getUserObject();
            data.setExpand(true);
         }
      });

      addMouseListener(new MouseActionListener());

      if (m_actManager.viewSupportsCopyPaste())
      {
         //CTL-C Action, keeps the selected nodes in copy clip board
         KeyStroke ksCTLCKeyRelease =
                 KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK, true);
         getInputMap().put(ksCTLCKeyRelease, "ctlcAction");
         AbstractAction ctlcAction = new AbstractAction()
         {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(
                    @SuppressWarnings("unused") ActionEvent e)
            {
               PSNode selNode = getSelectedNode();
               PSNode selParent = getSelectedParentNode();
               if (selNode != null && selParent != null)
               {
                  PSSelection sel =
                          new PSSelection(
                                  new PSUiMode(m_view, ms_mode),
                                  selParent,
                                  PSIteratorUtils.iterator(selNode));

                  m_actManager.getClipBoard().setClip(
                          PSClipBoard.TYPE_COPY,
                          sel);
               }
            }
         };
         getActionMap().put("ctlcAction", ctlcAction);

         //CTL-V Action, Displays pop-up menu for Paste Action of Copy clipboard
         KeyStroke ksCTLVKeyRelease =
                 KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK, true);
         getInputMap().put(ksCTLVKeyRelease, "ctlvAction");
         AbstractAction ctlvAction = new AbstractAction()
         {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(
                    @SuppressWarnings("unused") ActionEvent e)
            {
               TreePath selPath = PSNavigationTree.this.getSelectionPath();
               if (selPath != null)
               {
                  String pasteAction =
                          PSMenuAction.PREFIX_COPY_PASTE
                                  + IPSConstants.ACTION_PASTE;

                  PSMenuAction action =
                          new PSMenuAction(
                                  pasteAction,
                                  pasteAction,
                                  PSMenuAction.TYPE_MENU,
                                  "",
                                  PSMenuAction.HANDLER_CLIENT,
                                  0);

                  Rectangle rect =
                          PSNavigationTree.this.getPathBounds(selPath);
                  displayPopupMenu(action, selPath, rect.getLocation());
               }
            }
         };
         getActionMap().put("ctlvAction", ctlvAction);

         DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
                 this,
                 DnDConstants.ACTION_COPY_OR_MOVE,
                 this);

         new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
      }

      addFocusListener(new FocusListener() {
         public void focusGained(@SuppressWarnings("unused") FocusEvent e)
         {
            // Find a child to move the focus to if there is no
            // current selection
            PSNode node = getSelectedNode();
            if (node == null)
            {
               node = getRootNode();
               if (node != null)
               {
                  setSelectionInterval(0,0);
               }
            }
         }

         public void focusLost(@SuppressWarnings("unused") FocusEvent e)
         {
            // Ignore
         }
      });


   }

   /**
    * Override to set the row height based on the metrics of the font
    * to be set.
    * @see javax.swing.JComponent#setFont(java.awt.Font)
    */
   @Override
   public void setFont(Font font)
   {
      LineMetrics lm =
              font.getLineMetrics("", new FontRenderContext(null, true, true));
      int ht = (int) lm.getHeight() + 2;
      ht = ht < 17 ? 17 : ht;
      setRowHeight(ht);
      super.setFont(font);
   }

   /**
    * Convenience constructor for
    * {@link #PSNavigationTree(PSNode, String, PSActionManager, boolean)
    * PSNavigationTree(root, view, manager, true) }. See the link for more
    * information.
    */
   public PSNavigationTree(PSNode root, String view, PSActionManager manager)
   {
      this(root, view, manager, true);
   }

   /**
    * Get the list of currently expanded items within the tree.
    *
    * @return a list of expanded items as <code>String</code> objects
    */
   public List getExpandedList()
   {
      DefaultTreeModel model = getTreeModel();
      PSTreeNode root = (PSTreeNode) model.getRoot();

      TreePath tp = new TreePath(model.getPathToRoot(root));
      Enumeration e = getExpandedDescendants(tp);

      List ret = new ArrayList();
      while (e.hasMoreElements())
      {
         tp = (TreePath) e.nextElement();
         ret.add(convertTreePathToString(tp, true));
      }
      return ret;
   }

   /**
    * Expand all the nodes based on the specified path list.
    *
    * @param expandSet the list of strings representing the paths to the nodes,
    *    if a node is not found it is skipped.
    */
   public void setExpandedList(Set expandSet)
   {
      Iterator iter = expandSet.iterator();
      while (iter.hasNext())
      {
         String path = (String) iter.next();

         PSTreeNode treeNode = getTreeNode(path, true);
         if (treeNode != null)
         {
            PSNode node = (PSNode) treeNode.getUserObject();
            node.setExpand(true);

            expandNode(treeNode);
         }
      }
   }

   //implements interface method to store the node dragged into the clipboard.
   public void dragGestureRecognized(DragGestureEvent dge)
   {
      PSNode node = null;
      PSNode parent = null;
      Point loc = dge.getDragOrigin();
      TreePath path =
              getClosestPathForLocation((int) loc.getX(), (int) loc.getY());
      if (path != null)
      {
         node = getNode(path);
         if (path.getParentPath() != null)
            parent = getNode(path.getParentPath());
      }

      if (node != null && parent != null && m_actManager.canCopyOrMove(node))
      {
         try
         {
            PSClipBoard clipBoard = m_actManager.getClipBoard();
            PSSelection sel =
                    new PSSelection(
                            new PSUiMode(m_view, ms_mode),
                            parent,
                            PSIteratorUtils.iterator(node));
            clipBoard.setClip(PSClipBoard.TYPE_DRAG, sel);
            dge.startDrag(
                    DragSource.DefaultMoveNoDrop,
                    new PSDnDTransferable(),
                    new PSDragSourceAdapter());
         }
         catch (InvalidDnDOperationException e)
         {
            log.error("Invalid dnd operation",e);
         }
      }
   }

   /**
    * Save the current selection which will be restored on drag exit
    */
   public void dragEnter(@SuppressWarnings("unused") DropTargetDragEvent dtde)
   {
      m_saveDragSelection = getSelectionPaths();
      clearSelection();
      m_isInDragUnder = true;
   }

   /**
    * Provides drag over feedback for this action. If the tree node at the drag
    * location accepts Paste action, it accepts drag , otherwise it rejects the
    * drag. The node is refreshed to present visual changes.
    *
    * @param dtde the drag event on drop target, assumed not <code>null</code>
    * as is called by Swing model.
    */
   public void dragOver(DropTargetDragEvent dtde)
   {
      Point loc = dtde.getLocation();
      TreePath path = getPathForLocation((int) loc.getX(), (int) loc.getY());
      if (path == null)
      {
         dtde.rejectDrag();
      }
      else
      {
         PSTreeNode newTreeNode = (PSTreeNode) path.getLastPathComponent();
         m_curDropTargetNode = newTreeNode;
         PSNode node = (PSNode) m_curDropTargetNode.getUserObject();

         // only check the can paste on slots not slot items
         if (node.getType().equals(PSNode.TYPE_SLOT_ITEM))
         {
            PSTreeNode parent = (PSTreeNode) m_curDropTargetNode.getParent();
            node = (PSNode) parent.getUserObject();
         }

         if (m_actManager.canAcceptPaste(m_view, node, PSClipBoard.TYPE_DRAG))
         {
            dtde.acceptDrag(dtde.getDropAction());
            setSelectionRow(getRowForLocation(loc.x, loc.y));
         }
         else
         {
            dtde.rejectDrag();
            clearSelection();
         }
      }
   }

   //implements nothing
   public void dropActionChanged(
           @SuppressWarnings("unused") DropTargetDragEvent dtde)
   {
   }

   /**
    * Clears the drag-feed back if the current drag is not successful. Please
    * note this method is called from the {@link #drop(DropTargetDropEvent)}
    * method to clear the state after a drop.
    *
    * @param dte the event, assumed not <code>null</code> as is called by Swing
    * model.
    */
   public void dragExit(@SuppressWarnings("unused") DropTargetEvent dte)
   {
      setSelectionPaths(m_saveDragSelection);
      m_saveDragSelection = null;
      m_isInDragUnder = false;
   }

   /**
    * Implemented to accept the drop and show context-sensitive pop-up menu for
    * pasting. See the interface for more description of the method.
    */
   public void drop(DropTargetDropEvent dtde)
   {
      TreePath path = null;

      try
      {
         Component comp = dtde.getDropTargetContext().getComponent();
         comp.setCursor(Cursor.getDefaultCursor());

         String pasteAction =
                 PSMenuAction.PREFIX_DROP_PASTE + IPSConstants.ACTION_PASTE;

         PSMenuAction action =
                 new PSMenuAction(
                         pasteAction,
                         pasteAction,
                         PSMenuAction.TYPE_MENU,
                         "",
                         PSMenuAction.HANDLER_CLIENT,
                         0);

         Point loc = dtde.getLocation();
         path = getPathForLocation((int) loc.getX(), (int) loc.getY());

         //Accept drop, show menu and then undo-feed back if the popupmenu is not
         //visible.
         if (path != null)
         {
            m_curDropTargetNode = (PSTreeNode) path.getLastPathComponent();

            dtde.acceptDrop(dtde.getDropAction());
            final JPopupMenu popup = displayPopupMenu(action, path, loc);

            //create a thread to unset the node as not a drop target after the
            //pop-up menu is disappeared.
            Thread testThread = new Thread()
            {
               @Override
               public void run()
               {
                  try
                  {
                     while (popup != null && popup.isShowing())
                     {
                        sleep(10);
                     }
                  }
                  catch (InterruptedException e)
                  {
                     Thread.currentThread().interrupt();
                  }
               }
            };
            testThread.start();
         }
      }
      finally
      {
         // Force reset to current drop selection
         m_saveDragSelection = new TreePath[] { path };
         dtde.dropComplete(true);
         dragExit(dtde);
      }
   }

   /**
    * Gets the node as specified by node type.
    *
    * @param nodeType the node type, must be one of the NODE_xxx values.
    *
    * @return the node, may be <code>null</code> if the supplied node type is
    * either <code>NODE_SELECTED</code> or <code>NODE_SEL_PARENT</code> and
    * there is no selection or not found if it is being looked up.
    */
   public PSNode getNode(int nodeType)
   {
      PSNode node = null;
      PSTreeNode treeNode = getTreeNode(nodeType);
      if (treeNode != null)
         node = (PSNode) treeNode.getUserObject();

      return node;
   }

   /**
    * Gets the node represented by the path.
    *
    * @param path path of the node, assumed to be in the format
    * '//node1/node2/...', may not be <code>null</code> or empty.
    * @param byName supply <code>true</code> if the path names should match the
    * name of the nodes, <code>false</code> to match by display name (label).
    *
    * @return the matching node, may be <code>null</code> if not found.
    */
   public PSNode getNode(String path, boolean byName)
   {
      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty.");

      PSNode node = null;
      PSTreeNode treeNode = getTreeNode(path, byName);
      if (treeNode != null)
         node = (PSNode) treeNode.getUserObject();

      return node;
   }

   /**
    * Determines if the navigation tree is in the process of dirtying nodes.
    * Used to avoid loading children on refresh events caused by the process of
    * dirtying child nodes.
    *
    * @return <code>true</code> if it is, <code>false</code> if not.
    */
   public boolean isDirtyingNodes()
   {
      return m_isDirtying;
   }

   /**
    * @return Returns the isInDragUnder.
    */
   public boolean isInDragUnder()
   {
      return m_isInDragUnder;
   }

   /**
    * Finds first child node of the same type as the supplied node, with a
    * matching contentId.  Comparison for type is case-sensitive.  Does not
    * recurse into node's child nodes unless child node is a category.
    *
    * @param checkNode Checks this node and its children, assumed not
    * <code>null</code>.
    * @param matchNode Node to match against, assumed not <code>null</code>.
    *
    * @return The first matching node, or <code>null</code> if not found.
    */
   private PSNode getMatchingNode(PSNode checkNode, PSNode matchNode)
   {
      // check child nodes for matches
      PSNode match = null;
      Iterator children = checkNode.getChildren();
      if (children != null)
      {
         while (children.hasNext() && match == null)
         {
            PSNode test = (PSNode) children.next();
            if (test.isMatchingType(matchNode.getType())
                    && test.getContentId().equals(matchNode.getContentId()))
            {
               match = test;
            }
            else if (test.isOfType(PSNode.TYPE_CATEGORY))
            {
               match = getMatchingNode(test, matchNode);
            }
         }
      }
      return match;
   }

   /**
    * Marks any matching nodes as dirty.  Adds a clone of each supplied node as
    * a child to all nodes matching one of the specified types if no matching
    * node is found.  Match is determined using only the contentId of the nodes.
    * Clone is not added if parent has not yet had children loaded.
    *
    * @param node The node to dirty or add, may not be <code>null</code>.
    * @param types A collection of types defining which types of nodes the
    * child should be dirtied in/added to.  May not be <code>null</code> or
    * emtpy.
    */
   public void dirtyChildNodes(PSNode node, Collection types)
   {
      if (node == null)
         throw new IllegalArgumentException("node may not be null");
      if (types == null || types.isEmpty())
         throw new IllegalArgumentException("types may not be null or empty");

      DefaultTreeModel model = getTreeModel();
      PSTreeNode root = (PSTreeNode) model.getRoot();
      dirtyChildNode((PSNode) root.getUserObject(), node, types);
   }

   /**
    * Recursive worker method for {@link #dirtyChildNodes(PSNode, Collection)},
    * see that method for more info.
    *
    * @param parent The node to which a clone of the child node is recursively
    * added, assumed not <code>null</code>.
    * @param node The node to dirty or add, assumed not <code>null</code>.
    * @param types The collection of types, assumed not <code>null</code>.
    */
   private void dirtyChildNode(PSNode parent, PSNode node, Collection types)
   {
      PSNode addNode = null;
      if (types.contains(parent.getType()) && parent.getChildren() != null)
      {
         PSNode match = getMatchingNode(parent, node);
         if (match != null)
            match.setIsDirty(true);
         else
         {
            // clone it to add, but clear its children
            addNode = (PSNode) node.clone();
            addNode.setIsDirty(true);
            addNode.setChildren(null);
         }

         // collapse parent if not the actual node supplied - this node will
         // be automatically refreshed in the ui and does not need to be
         // collapsed.
         if (node != match && parent.hasChildOfType(PSNode.TYPE_CATEGORY))
         {
            PSTreeNode treeNode = getTreeNode(parent);
            if (treeNode != null)
            {
               /*
                * collapsing path will cause parent node to be selected, and
                * this would cause a refresh of the parent node's children,
                * which we don't want at this time.  So we need to mark this
                * tree as dirtying so the selection change won't trigger a
                * search.
                */
               m_isDirtying = true;
               collapsePath(new TreePath(treeNode.getPath()));
               m_isDirtying = false;
            }
         }
      }

      // now recurse children
      Iterator children = parent.getChildren();
      if (children != null)
      {
         while (children.hasNext())
            dirtyChildNode((PSNode) children.next(), node, types);
      }

      // if no match, add clone as child marked as dirty.  Must do this after
      // recursing children or we get into an infinite loop
      if (addNode != null)
         parent.addChild(addNode);

   }

   /**
    * Gets the node that is matching the supplied nodes from already loaded
    * nodes of the tree. Uses <code>equals()</code> method for matching.
    *
    * @param node the node to match, cannot be <code>null</code>
    *
    * @return the matching tree node, may be <code>null</code> if there is no
    * match.
    */
   public PSTreeNode getTreeNode(PSNode node)
   {
      if(node == null)
         throw new IllegalArgumentException("node cannot be null.");
      DefaultTreeModel model = getTreeModel();
      PSTreeNode checkNode = (PSTreeNode) model.getRoot();

      return getTreeNode(checkNode, node);
   }

   /**
    * Builds the fully qualified path of the parent of the supplied
    * <code>node</code>. The root is the parent of itself.
    *
    * @param node Never <code>null</code>.
    * @return A path of the form "//a/b/c". Never <code>null</code>, will be
    * empty if the supplied node is not found in this tree.
    *
    * @see #getPath(PSNode)
    */
   public String getParentPath(PSNode node)
   {
      String path = getPath(node);
      if (path.length() > 2)
      {
         int pos = path.lastIndexOf("/");
         path = path.substring(0, pos);
      }
      return path;
   }

   /**
    * Builds the fully qualified path of the supplied <code>node</code>,
    * including the node itself.
    *
    * @param node Never <code>null</code>.
    * @return A path of the form "//a/b/c". Never <code>null</code>, will be
    * empty if the supplied node is not found in this tree.
    *
    * @see #getParentPath(PSNode)
    */
   public String getPath(PSNode node)
   {
      if ( null == node)
      {
         throw new IllegalArgumentException("node cannot be null");
      }
      PSTreeNode treeNode = getTreeNode(node);
      if (null == treeNode)
         return "";

      Object[] path = treeNode.getUserObjectPath();
      String result = "/";
      //the root is hidden, so don't add it to the path
      for (int i=1; i < path.length; i++)
      {
         result += "/" + path[i].toString();
      }
      //if the root node is passed in, return //
      if (result.length() == 1)
         result += "/";
      return result;
   }

   /**
    * Recursive worker method for {@link  #getTreeNode(PSNode)}.
    *
    * @param checkNode the node to search for, assumed not to be <code>null
    * </code>.
    * @param nodeToMatch the node to check for, assumed not to be <code>null
    * </code>.
    *
    * @return the matching tree node, may be <code>null</code> if there is no
    * match.
    */
   private PSTreeNode getTreeNode(PSTreeNode checkNode, PSNode nodeToMatch)
   {
      PSTreeNode matchNode = null;

      PSNode node = (PSNode) checkNode.getUserObject();
      if (node.equals(nodeToMatch))
         matchNode = checkNode;
      else if (((PSNode) checkNode.getUserObject()).getChildren() != null)
      {
         int count = checkNode.getChildCount();
         for (int i = 0; i < count; i++)
         {
            PSTreeNode treeNode = (PSTreeNode) checkNode.getChildAt(i);
            matchNode = getTreeNode(treeNode, nodeToMatch);
            if (matchNode != null)
               break;
         }
      }
      return matchNode;
   }

   /**
    * Gets the tree node represented specified node type.
    *
    * @param nodeType the node type, assumed to be one of the NODE_xxx values.
    *
    * @return the node, may be <code>null</code> if the supplied node type is
    * either <code>NODE_SELECTED</code> or <code>NODE_SEL_PARENT</code> and
    * there is no selection
    */
   private PSTreeNode getTreeNode(int nodeType)
   {
      PSTreeNode treeNode = null;
      if (nodeType == NODE_SELECTED)
      {
         treeNode = getDynamicTreeNodeInPath(getSelectionPath());
      }
      else if (nodeType == NODE_SEL_PARENT)
      {
         treeNode =
                 getDynamicTreeNodeInPath(getSelectionPath().getParentPath());
      }
      else if (nodeType == NODE_ROOT)
      {
         treeNode = (PSTreeNode) ((DefaultTreeModel) getModel()).getRoot();
      }
      else
      {
         throw new IllegalArgumentException("invalid nodeType");
      }

      return treeNode;
   }

   /**
    * Walks up into the tree path until it finds a node with dynamic children.
    * Dynamic children are the result of the execution of children url of the
    * node.
    * @param path treepath of tree node to search the dynamic parent, if
    * <code>null</code> the result will be <code>null</code>.
    * @return Treenode with dynamic data node, may be <code>null</code>.
    */
   private PSTreeNode getDynamicTreeNodeInPath(TreePath path)
   {
      PSTreeNode dynNode = null;

      //Walk until you find a node with dynamic children
      while (path != null)
      {
         PSTreeNode treeNode = (PSTreeNode) path.getLastPathComponent();
         PSNode node = (PSNode) treeNode.getUserObject();
         if (node.hasDynamicChildren())
         {
            dynNode = treeNode;
            break;
         }
         path = path.getParentPath();
      }
      return dynNode;
   }
   /**
    * Gets user object that is represented by the root node of this tree.
    *
    * @return the node, never <code>null</code>
    */
   public PSNode getRootNode()
   {
      DefaultTreeModel model = getTreeModel();

      return (PSNode) ((DefaultMutableTreeNode) model.getRoot())
              .getUserObject();
   }

   /**
    * Gets user object that is represented by the selected node of this tree.
    *
    * @return the selected node, may be <code>null</code> if there is no
    * selection.
    */
   public PSNode getSelectedNode()
   {
      PSNode selNode = null;

      TreePath path = getSelectionPath();
      if (path != null)
         selNode = getNode(path);

      return selNode;
   }

   /**
    * Gets user object that is represented by the parent of the selected node of
    * this tree.
    *
    * @return the parent node of the selected node, may be <code>null</code> if
    * there is no selection.
    */
   public PSNode getSelectedParentNode()
   {
      PSNode selNode = null;

      TreePath path = getSelectionPath();

      if (path != null && path.getParentPath() != null)
         selNode = getNode(path.getParentPath());

      return selNode;
   }

   /**
    * Gets the user object of the last node in the supplied path.
    *
    * @param path the path, assumed not <code>null</code>
    *
    * @return the node, never <code>null</code>
    */
   PSNode getNode(TreePath path)
   {
      return (PSNode) ((DefaultMutableTreeNode) path.getLastPathComponent())
              .getUserObject();
   }

   /**
    * Gets the path of selection in the tree as a <code>String</code> in the
    * format '//Node/Child Node/...'.
    *
    * @return the path, may be <code>null</code> if there is no selection.
    */
   public String getSelectedPath()
   {
      return getSelectedPath(false);
   }

   /**
    * Gets the path of selection in the tree as a <code>String</code> in the
    * format '//Node/Child Node/...'.
    *
    * @param byName if true returns the path using the name of the node,
    *    otherwise returns the display name of the selected path
    *
    * @return the path, may be <code>null</code> if there is no selection.
    */
   public String getSelectedPath(boolean byName)
   {
      TreePath treePath = getSelectionPath();
      return convertTreePathToString(treePath, byName);
   }

   /**
    *
    * Gets the path of the specified Node in the tree as a <code>String</code>
    * in the format '//Node/Child Node/...'.
    *
    * @param node the node to convert to a string path
    *
    * @return the path, may be <code>null</code> if there is no selection.
    */
   public String convertNodeToPath(PSNode node)
   {
      PSTreeNode treeNode = getTreeNode(node);
      if (treeNode == null)
         return null;

      return convertTreePathToString(new TreePath(treeNode.getPath()), false);
   }

   /**
    * Converts a tree path to a <code>String</code> in the format '//Node/Child
    * Node/Child Node/...'.
    *
    * @param treePath the path to the tree object to be converted, must not be
    *    <code>null</code>
    * @param byName if true, covert using the name of the node, otherwise use
    *    the display name.
    *
    * @return the path
    */
   private String convertTreePathToString(TreePath treePath, boolean byName)
   {
      String path = null;
      if (treePath != null)
      {
         Object[] nodes = treePath.getPath();
         path = "//";

         //if we are ignoring root, start reading nodes from next level
         int i = 0;
         if (m_ignoreRoot)
            i = 1;

         for (; i < nodes.length; i++)
         {
            DefaultMutableTreeNode treeNode =
                    (DefaultMutableTreeNode) nodes[i];
            PSNode node = (PSNode) treeNode.getUserObject();
            path += byName ? node.getName() : node.getLabel();

            if (i != (nodes.length - 1))
               path += "/";
         }
      }
      return path;
   }

   /**
    * Notification method that this node has changed.  In general it will be
    * called by the container when the any node data gets changed. Refreshes
    * the tree node of supplied type with the new node and updates visually
    * also.
    *
    * @param newNode the user object to set with for a node of supplied node
    * type, may not be <code>null</code>
    * @param nodeType the type of node to refresh with new data, must be one of
    * the NODE_xxx values.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public void nodeChanged(PSNode newNode, int nodeType)
   {
      if (newNode == null)
         throw new IllegalArgumentException("newNode may not be null.");

      PSTreeNode treeNode = getTreeNode(nodeType);

      if (treeNode != null)
         refresh(newNode, treeNode);
   }

   /**
    * Refreshes the tree node that represents the supplied old node if it exists
    * in the tree with new node and updates visual representation also.
    *
    * @param newNode the new node to set with, may not be <code>null</code>
    * @param oldNode the old node to be replaced, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public void nodeChanged(PSNode newNode, PSNode oldNode)
   {
      if (newNode == null)
         throw new IllegalArgumentException("newNode may not be null.");

      if (oldNode == null)
         throw new IllegalArgumentException("oldNode may not be null.");

      PSTreeNode treeNode = getTreeNode(oldNode);

      if (treeNode != null)
         refresh(newNode, treeNode);
   }

   /**
    * Refreshes the tree node represented by the supplied tree path
    * new node and updates visual representation also.
    *
    * @param treePath the path of the tree node to update, may not be
    * <code>null</code>.
    * @param newNode the new node to set with, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public void nodeChanged(TreePath treePath, PSNode newNode)
   {
      if (newNode == null)
         throw new IllegalArgumentException("newNode may not be null.");

      if (treePath == null)
         throw new IllegalArgumentException("treePath may not be null.");
      PSTreeNode treeNode = getDynamicTreeNodeInPath(treePath);

      if (treeNode != null)
         refresh(newNode, treeNode);
   }

   /**
    * Replaces the user object represented by the tree node with the supplied
    * new node and refreshes the tree view.
    *
    * @param newNode the new node to update, assumed not <code>null</code>
    * @param treeNode the tree node whose visual representation need to be
    * updated, assumed not <code>null</code> and a node in the tree.
    */
   private void refresh(PSNode newNode, PSTreeNode treeNode)
   {
      if (treeNode.isLoadedWithChildren())
      {
         treeNode.removeAllChildren();
         treeNode.setLoadedChildren(false);
      }
      DefaultTreeModel model = getTreeModel();
      treeNode.setUserObject(newNode);
      model.nodeStructureChanged(treeNode);
      expandNode(treeNode);
   }

   /**
    * Gets the tree node identified by the supplied path. The search for nodes
    * will be from children of root. Loads dynamically the child nodes as part
    * of the search, so this method is expensive to call. Checks against the
    * label of the node case sensitively for a match.
    *
    * @param path the path of tree node, assuming in the format '//node1/node2'
    * @param byName suply <code>true</code> to match the path by name, <code>
    * false</code> to match by label (display name).
    *
    * @return the matching node, may be <code>null</code> if the match is not
    * found.
    */
   private PSTreeNode getTreeNode(String path, boolean byName)
   {
      StringTokenizer st = new StringTokenizer(path, "/");

      DefaultTreeModel model = getTreeModel();
      PSTreeNode checkNode = (PSTreeNode) model.getRoot();
      PSTreeNode matchNode = null;

      if (st.hasMoreTokens())
      {
         if (!m_ignoreRoot)
         {
            PSNode rootNode = (PSNode) checkNode.getUserObject();
            if (rootNode.getLabel().equals(st.nextToken()))
            {
               if (!st.hasMoreTokens())
                  matchNode = checkNode;
            }
         }

         while (st.hasMoreTokens())
         {
            String token = st.nextToken();
            int count = checkNode.getChildCount();
            for (int i = 0; i < count; i++)
            {
               PSTreeNode treeNode = (PSTreeNode) checkNode.getChildAt(i);
               PSNode node = (PSNode) treeNode.getUserObject();

               String checkName = byName ? node.getName() : node.getLabel();

               if (checkName.equals(token))
               {
                  if (st.hasMoreTokens())
                     checkNode = treeNode;
                  else
                     matchNode = treeNode;
                  break;
               }
            }
         }
      }

      return matchNode;
   }

   /**
    * Expands the supplied node and its children if the data node of this if
    * they are specified to expand recursively.
    *
    * @param treeNode the tree node to expand, assumed not <code>null</code>
    */
   private void expandNode(PSTreeNode treeNode)
   {
      //expand the node if the user node specifies to expand other wise not.
      PSNode node = (PSNode) treeNode.getUserObject();
      if (node.shouldExpand())
      {
         Iterator leafNodes = treeNode.getLeafChildren().iterator();
         while (leafNodes.hasNext())
         {
            DefaultMutableTreeNode childNode =
                    (DefaultMutableTreeNode) leafNodes.next();
            makeVisible(new TreePath(childNode.getPath()));
         }
      }
   }

   /**
    * Constructs the dyanmic tree node that loads children when it is expanded
    * only.
    */
   public class PSTreeNode
           extends JTree.DynamicUtilTreeNode
           implements Accessible
   {
      private static final long serialVersionUID = 1L;

      /**
       * Constructs the tree node with supplied node and allows children by
       * default. Has an empty children list initially and gets loaded with
       * child nodes when <code>loadChildren()</code> is called.
       *
       * @param node the user object of the node, may not be <code>null</code>
       *
       * @throws IllegalArgumentException if node is <code>null</code>
       */
      public PSTreeNode(PSNode node)
      {
         super(node, new Vector());

         if (node == null)
            throw new IllegalArgumentException("node may not be null.");
         node.setAssociatedTreeNode(this);
      }

      /**
       * Loads child nodes of this node and appends them if they are not loaded
       * yet. Each child node of this node gets expanded by default if the child
       * defined as to expand by default.
       */
      @Override
      protected void loadChildren()
      {
         if (loadedChildren)
            return;
         PSNode node = (PSNode) getUserObject();

         Iterator lchildren = node.getChildren();

         if (lchildren == null)
            lchildren = m_actManager.loadChildren(node);

         //remove just if later
         int i = 0;
         if (lchildren != null)
         {
            while (lchildren.hasNext())
            {
               PSNode child = (PSNode) lchildren.next();
               //Nodes that are container only should be added to the tree
               if (child.isContainer())
               {
                  PSTreeNode childNode = new PSTreeNode(child);
                  insert(childNode, i++);
               }
            }
         }

         loadedChildren = true;
      }

      /**
       * Gets the leaf child nodes (children, grand children ... that are leafs
       * (nodes that don't want to expand by default (expanded = false)) )
       * of this node. If this node is leaf, this will be added to the list and
       * returned.
       *
       * @return the list of leaf child nodes, never <code>null</code> or empty.
       */
      public List getLeafChildren()
      {
         DefaultMutableTreeNode node = this;

         List lchildren = new ArrayList();
         getLeafChildren(node, lchildren);

         return lchildren;
      }

      /**
       * Recursive worker method for {@link #getLeafChildren()} to get the leaf
       * nodes of the supplied node.
       *
       * @param node the root node to check, assumed not to be <code>null</code>
       * @param uchildren the list of child leaf nodes that gets updated,
       * assumed not to be <code>null</code>
       */
      @SuppressWarnings("unchecked")
      private void getLeafChildren(DefaultMutableTreeNode node, List uchildren)
      {
         //A node is leaf if it does not have any children loaded.
         PSNode userObj = (PSNode) node.getUserObject();
         if (node.isLeaf() || !userObj.shouldExpand())
            uchildren.add(node);
         else
         {
            for (Enumeration e = node.children(); e.hasMoreElements();)
            {
               getLeafChildren(
                       (DefaultMutableTreeNode) e.nextElement(),
                       uchildren);
            }
         }
      }

      /**
       * Sets the flag to specify whether children are loaded or not.
       *
       * @param flag supply <code>false</code> to make the node as not loaded
       * with children, otherwise <code>true</code>.
       */
      public void setLoadedChildren(boolean flag)
      {
         if (flag);
         loadedChildren = false;
      }

      /**
       * Check to see if this node is loaded with children or not.
       *
       * @return <code>true</code> if it is loaded, otherwise <code>false</code>
       */
      public boolean isLoadedWithChildren()
      {
         return loadedChildren;
      }

      /**
       * Updates the drop target flag. Should be called with <code>true</code>
       * when drag/drop is over this node, otherwise <code>false</code>. Updates
       * this node's visual representation.
       *
       * @param flag the drop target flag.
       */
      public void setAsDropTarget(boolean flag)
      {
         m_isDropTarget = flag;

         getTreeModel().nodeChanged(this);
      }

      /**
       * Checks whether this node represents a drop target or not for current
       * drag and drop operation.
       *
       * @return <code>true</code> if this node representing a drop target,
       * otherwise <code>false</code>
       */
      public boolean isDropTarget()
      {
         return m_isDropTarget;
      }

      /* (non-Javadoc)
       * @see javax.accessibility.Accessible#getAccessibleContext()
       */
      public AccessibleContext getAccessibleContext()
      {
         return m_accessibleContext;
      }

      /**
       * Overridden to link this tree node w/ the user object.
       *
       * @param userObject May be <code>null</code> to clear the object. Must
       * implement the {@link PSNavigationTree.IPSTreeNodeAssociation}.  When
       * called, the {@link PSNavigationTree.IPSTreeNodeAssociation
       * #setAssociatedTreeNode(PSNavigationTree.PSTreeNode)
       * setAssociatedTreeNode} is called with a ref to this node and if this
       * node already has a user object, the same method is called on it with
       * <code>null</code>.
       */
      @Override
      public void setUserObject(Object userObject)
      {
         if (null != userObject &&
                 !(userObject instanceof PSNavigationTree.IPSTreeNodeAssociation))
         {
            throw new IllegalArgumentException("userObject must implement "
                    + "PSNavigationTree.IPSTreeNodeAssociation");
         }
         PSNavigationTree.IPSTreeNodeAssociation uobj =
                 (PSNavigationTree.IPSTreeNodeAssociation) super.getUserObject();
         if (null != uobj)
            uobj.setAssociatedTreeNode(null);
         if (null != userObject)
         {
            ((PSNavigationTree.IPSTreeNodeAssociation) userObject)
                    .setAssociatedTreeNode(this);
         }
         super.setUserObject(userObject);
      }

      /**
       * Create the accessible context for this node
       */
      private AccessibleContext m_accessibleContext =
              new PSTreeNodeAccContext(PSNavigationTree.this, this);

      /**
       * The flag to indicate that whether this node is currently under drag
       * movement to represent a drop target, initialized to <code>false</code>
       * and modified through calls to <code>setAsDropTarget(boolean)</code>.
       */
      private boolean m_isDropTarget = false;
   }

   /**
    * Any object that wants to be set as the user object of a
    * {@link PSNavigationTree.PSTreeNode} must implement this interface.
    *
    * @author paulhoward
    */
   public interface IPSTreeNodeAssociation
   {
      /**
       * Called by the framework whenever the object implementing this
       * interface is set as the user object of a node.
       *
       * @param node The node that is 'taking ownership' of the object. May be
       * <code>null</code> to clear the association.
       */
      public void setAssociatedTreeNode(PSNavigationTree.PSTreeNode node);

      /**
       * Accessor for the tree node that this object is currently associated
       * with.
       *
       * @return The last node supplied in the
       * {@link #setAssociatedTreeNode(PSNavigationTree.PSTreeNode)
       * setAssociatedTreeNode}. <code>null</code> if that method has never
       * been called.
       */
      public PSNavigationTree.PSTreeNode getAssociatedTreeNode();
   }

   /**
    * Gets the model of this tree.
    *
    * @return the model, never <code>null</code>
    */
   public DefaultTreeModel getTreeModel()
   {
      return (DefaultTreeModel) getModel();
   }

   /**
    * The class that handles pop-up mouse clicks to show pop-up menu for the
    * path near the mouse-click.
    */
   private class MouseActionListener extends PSMouseAdapter
   {

      /**
       * Handles right-click mouse event to show the context-menu if the clicked
       * location represents a node in the tree.
       *
       * @param event event the mouse click event, assumed not <code>null</code>
       * this is called by Swing model.
       */
      @Override
      public void mouseWasClicked(MouseEvent event)
      {
         JTree tree = (JTree) event.getSource();
         TreePath path = tree.getPathForLocation(event.getX(), event.getY());
         if (PSCxUtil.isMouseMenuGesture(event, m_applet))
         {
            if (path != null)
            {
               tree.setSelectionPath(path);
               displayPopupMenu(null, path, event.getPoint());
            }
         }
         else if (
                 SwingUtilities.isLeftMouseButton(event)
                         && event.getClickCount() == 2)
         {
            //double-clcked a tree node in the tree, so execute default action
            if (path != null)
            {
               PSNode selNode = getNode(path);

               PSNode parentNode = null;
               if (path.getParentPath() != null)
                  parentNode = getNode(path.getParentPath());

               PSUiMode mode = new PSUiMode(m_view, ms_mode);
               PSSelection sel =
                       new PSSelection(
                               mode,
                               parentNode,
                               PSIteratorUtils.iterator(selNode));
               PSMenuAction defAction = m_actManager.findDefaultAction(sel);
               if (defAction != null)
                  m_actManager.executeAction(defAction, sel);
            }
         }
      }
   }

   /**
    * Displays context menu for the supplied action and the selection
    * represented by the last component of the supplied path.
    *
    * @param action the action that controls the pop-up menu to show, if
    * <code>null</code> the pop-up menu depends only on the last node of the
    * path.
    * @param path the path whose last component represents a node for which the
    * pop-up menu will be applied, assumed not <code>null</code>
    * @param loc the location where the pop-up menu should show, assumed not
    * <code>null</code>
    *
    * @return the popup menu shown, may be <code>null</code> if the popup menu
    * is not available for the supplied action and/or for the current selection
    */
   JPopupMenu displayPopupMenu(
           PSMenuAction action,
           TreePath path,
           Point loc)
   {
      DefaultMutableTreeNode node =
              (DefaultMutableTreeNode) path.getLastPathComponent();
      Iterator selNodes = PSIteratorUtils.iterator(node.getUserObject());

      PSNode parent = null;
      TreePath parentPath = path.getParentPath();
      if (parentPath != null)
      {
         parent =
                 (PSNode) ((DefaultMutableTreeNode) path
                         .getParentPath()
                         .getLastPathComponent())
                         .getUserObject();
      }

      PSNavigationalSelection selection =
              new PSNavigationalSelection(
                      new PSUiMode(m_view, ms_mode),
                      parent,
                      selNodes,
                      getSelectedPath());

      PSMenuAction popupMenuAction;
      if (action == null)
      {
         popupMenuAction = m_actManager.getContextMenu(selection);
      }
      else
      {  // if drag & drop, filter the action against both the target node
         // and the selected nodes in the clip board (if there is any)
         if (action.getName().startsWith(PSMenuAction.PREFIX_DROP_PASTE))
         {
            popupMenuAction = m_actManager.getContextMenu(action, selection,
                    PSClipBoard.TYPE_DRAG);
         }
         else
         {
            popupMenuAction = m_actManager.getContextMenu(action, selection);
         }
      }

      JPopupMenu popup = null;
      if (popupMenuAction != null)
      {
         PSContentExplorerMenu menu =
                 new PSContentExplorerMenu(
                         popupMenuAction,
                         new PSMenuSource(selection),
                         m_actManager);
         popup = menu.getPopupMenu();

         //Get menu items and show as pop-up menu
         if (menu.getMenuElements().length > 0)
         {
            popup.show(this, (int) loc.getX(), (int) loc.getY());
            PSCxUtil.adjustPopupLocation(popup);
         }
      }

      return popup;
   }

   /**
    * Given the path in the standard syntax (//path1/path2/...) it walks through
    * the tree, locates the required node and sets selection. Uses the label for
    * match.
    *
    * @param strPath must be a valid path as specified above, Nothing happens
    * if the path does not exist in the tree, may not be <code>null</code> or
    * empty.
    * @param byName the flag that specifies whether the path represents internal
    * names of the nodes or the display names (labels) of the nodes. Supply
    * <code>true</code> for former and vice-versa.
    *
    * @throws IllegalArgumentException if the supplied path string is not valid.
    */
   public void setSelectionPath(String strPath, boolean byName)
   {
      if (strPath == null || strPath.trim().length() == 0)
         throw new IllegalArgumentException(
                 "strPath may not be null or empty.");

      PSTreeNode treeNode = getTreeNode(strPath, byName);
      if (treeNode != null)
         setSelectionPath(new TreePath(treeNode.getPath()));
   }

   /**
    * Finds the matching nodes of the supplied nodes in the tree and calls
    * <code>refresh(PSTreeNode)</code> to refresh the UI. The match is by
    * checking the <code>equals()</code> on the user object.
    *
    * @param nodes the nodes to refresh, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if nodes are <code>null</code>
    */
   public void refreshNodes(Iterator nodes)
   {
      if (nodes == null)
         throw new IllegalArgumentException("nodes may not be null.");

      PSNode firstNode = null;
      while (nodes.hasNext())
      {
         PSNode refreshNode = (PSNode) nodes.next();
         PSTreeNode treeNode = getTreeNode(refreshNode);
         if (treeNode != null)
         {
            refresh(refreshNode, treeNode);
            if (firstNode == null)
               firstNode = refreshNode;
         }
      }

      //Focus always goes to the first node refreshed (Should it be the last one???)
      if (firstNode != null)
         selectNode(firstNode);
   }

   /**
    * Select the tree node that is matching the specified node if found.
    *
    * @param node the node to check, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if node is <code>null</code>
    */
   public void selectNode(PSNode node)
   {
      if (node == null)
         throw new IllegalArgumentException("node may not be null.");

      PSTreeNode treeNode = getTreeNode(node);
      if (treeNode != null)
      {
         TreePath path = new TreePath(treeNode.getPath());
         if (isPathSelected(path))
            clearSelection();

         setSelectionPath(path);
      }
   }


   /**
    * Find the tree node for the given parent node and add the given
    * child.
    *
    * @param parent the parent node, must never be <code>null</code> and
    *    must exist
    * @param child the child node, must never be <code>null</code>
    */
   public void addNodeChild(PSNode parent, PSNode child)
   {
      if (parent == null)
      {
         throw new IllegalArgumentException("parent must never be null");
      }
      if (child == null)
      {
         throw new IllegalArgumentException("child must never be null");
      }
      PSTreeNode parentTreeNode = getTreeNode(parent);
      if (parentTreeNode == null)
      {
         throw new IllegalArgumentException("parent must exist");
      }
      parentTreeNode.add(new PSTreeNode(child));
   }

   /**
    * Determines the type of node to be refreshed for the specified
    * hint.
    *
    * @param refreshHint not <code>null</code>.
    *
    * @return the node type which should be refreshed or -1 if the
    * refresh hint is not one of {@link PSActionEvent#REFRESH_NAV_ROOT},
    * {@link PSActionEvent#REFRESH_NAV_SELECTED}, or
    * {@link PSActionEvent#REFRESH_NAV_SEL_PARENT}.
    */
   public static int getRefreshNodeType(String refreshHint)
   {
      if (refreshHint == null)
      {
         throw new IllegalArgumentException("refreshHint may not be null");
      }

      int nodeType = -1;

      if(refreshHint.equalsIgnoreCase(PSActionEvent.REFRESH_NAV_ROOT))
         nodeType = NODE_ROOT;
      else if(refreshHint.equalsIgnoreCase(PSActionEvent.REFRESH_NAV_SELECTED))
         nodeType = NODE_SELECTED;
      else if(refreshHint.equalsIgnoreCase(PSActionEvent.REFRESH_NAV_SEL_PARENT))
         nodeType = NODE_SEL_PARENT;

      return nodeType;
   }

   /**
    * The action manager to use to load the children for a specific node,
    * initialized in the ctor and never <code>null</code> or modified after
    * that.
    */
   PSActionManager m_actManager;

   /**
    * The current view of the tree, one of the <code>PSUiMode.TYPE_VIEW_xxx
    * </code> values, initialized in the ctor and never <code>null</code>, empty
    * or modified after that.
    */
   String m_view;

   /**
    * The flag to indicate whether the root should be shown or not and should
    * be considered as part of the path or not. <code>true</code> indicates
    * not to show the root and vice-versa. Initialized in the ctor and never
    * modified after that.
    */
   private boolean m_ignoreRoot;

   /**
    * The mode (Navigation) that is represented by this tree. See {@link
    * PSUiMode#TYPE_MODE_NAV} for more info.
    */
   public static final String ms_mode = PSUiMode.TYPE_MODE_NAV;

   /**
    * The constant to indicate the root node.
    */
   public static final int NODE_ROOT = 0;

   /**
    * The constant to indicate the selected node.
    */
   public static final int NODE_SELECTED = 1;

   /**
    * The constant to indicate parent node of the selected node.
    */
   public static final int NODE_SEL_PARENT = 2;

   /**
    * The node on which current dnd operation is going on, initialized to <code>
    * null</code> and set with actual node whenever <code>
    * dragOver(DropTargetDragEvent)</code> or <code>drop(DropTargetDropEvent)
    * </code> is called.
    */
   private PSTreeNode m_curDropTargetNode = null;

   /**
    * Flag to indicate if tree is in the process of dirtying child nodes.  See
    * {@link #isDirtyingNodes()} for more information.  Modified during calls
    * to {@link #dirtyChildNodes(PSNode, Collection)}.
    */
   private boolean m_isDirtying = false;

   /**
    * This holds the current drag selection on drag enter. On drag exit the
    * selection is restored and this is nulled out. May contain <code>null</code>
    * if there is no current select.
    */
   private TreePath[] m_saveDragSelection = null;

   /**
    * Set in {@link #dragEnter(DropTargetDragEvent)} and reset in
    * {@link #dragExit(DropTargetEvent)}. Is used by the selection
    * listener when deciding if the selection should cause any side effects.
    */
   private boolean m_isInDragUnder = false;

   /**
    * A reference back to the applet that initiated this action manager.
    */
   private PSContentExplorerApplet m_applet;
}
