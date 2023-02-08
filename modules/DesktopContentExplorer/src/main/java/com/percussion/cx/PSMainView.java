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

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * The view panel that supports <code>PSUiMode.TYPE_VIEW_CX</code> and <code>
 * PSUiMode.TYPE_VIEW_IA</code> views.
 */
public class PSMainView extends JSplitPane implements TreeSelectionListener,
    ListSelectionListener, IPSActionListener, TreeWillExpandListener {
    /**
     * The navigational tree that represents the left component of this main view
     * panel, initialized in <code>init(PSNode)</code> and never <code>null
     * </code> or modified after that.
     */
    private PSNavigationTree m_navTree;

    /**
     * The display panel that represents the right component of this main view
     * panel, initialized in <code>init(PSNode)</code> and never <code>null
     * </code> or modified after that.
     */
    private PSMainDisplayPanel m_mainDisplayPanel;

    /**
     * The list of selection listeners that are interested in selection changes
     * in this view panel (tree or table), initialized to an empty list and
     * listeners are being added through calls to <code>
     * addSelectionListener(IPSSelectionListener)</code>. Never <code>null</code>
     * after initialization.
     */
    private List m_selListeners = new ArrayList();

    /**
     * The current view of UI, initialized in the constructor and never <code>
     * null</code>, empty or modified after that.
     */
    private String m_view;

    /**
     * The parent applet container that holds action manager, global menu bar and
     * other details, initialized in the ctor and never <code>null</code> or
     * modified after that.
     */
    private PSContentExplorerApplet m_applet;

    /**
     * Constructs the split pane with tree on left and table on right.
     *
     * @param navTree the root node with its children to represent its left
     * navigation pane.
     * @param view the view type, must be one of the <code>PSUiMode.TYPE_VIEW_CX
     * </code> and <code>PSUiMode.TYPE_VIEW_IA</code> views.
     * @param applet the parent container of this panel, may not be <code>null
     * </code>
     *
     * @throws IllegalArgumentException if any parameter is invalid.
     */
    public PSMainView(PSNode navTree, String view,
        PSContentExplorerApplet applet) {
        if (navTree == null) {
            throw new IllegalArgumentException("navTree may not be null.");
        }

        if (!(PSUiMode.TYPE_VIEW_CX.equals(view) ||
                PSUiMode.TYPE_VIEW_IA.equals(view) ||
                PSUiMode.TYPE_VIEW_RC.equals(view))) {
            throw new IllegalArgumentException(
                "view must be one of the following:" + PSUiMode.TYPE_VIEW_CX +
                "," + PSUiMode.TYPE_VIEW_IA + "," + PSUiMode.TYPE_VIEW_RC);
        }

        if (applet == null) {
            throw new IllegalArgumentException("applet may not be null.");
        }

        m_view = view;
        m_applet = applet;

        init(navTree);
    }

    /**
     * Initializes the split pane with navigational tree panel on the left and
     * a table panel on the right. The table panel displays the children of the
     * selected node in the navigation pane.
     *
     * @param root the root node of the tree, assumed not <code>null</code>
     */
    private void init(PSNode root) {
        MouseActionListener mouseListener = new MouseActionListener();

        //Left Panel
        m_navTree = new PSNavigationTree(root, m_view,
                m_applet.getActionManager());
        m_navTree.addTreeSelectionListener(this);
        /* m_navTree.addFocusListener(new FocusListener(){
        
            public void focusGained(FocusEvent e)
            {
               PSNavigationTree navTree = (PSNavigationTree)e.getSource();
               String path = navTree.getSelectedPath();
               PSUiMode mode = new PSUiMode(m_view, PSUiMode.TYPE_MODE_NAV);
               PSNode parent = m_navTree.getSelectedParentNode();
               Iterator selection = PSIteratorUtils.iterator(navTree.getSelectedNode());
               PSNavigationalSelection sel =
                  new PSNavigationalSelection(mode, parent, selection, path);
        
               informListeners(sel);
            }
        
            public void focusLost(@SuppressWarnings("unused") FocusEvent e)
            {
            }
            });
         */
        m_navTree.addTreeWillExpandListener(this);

        boolean sortView = true;

        if (m_view.equals(PSUiMode.TYPE_VIEW_IA)) {
            m_navTree.setCellRenderer(new PSIAViewNodeRenderer(m_applet));
            sortView = false;
        }

        Color bkgColor = m_applet.isMacPlatform() ? Color.white
                                                  : SystemColor.window;
        m_navTree.addMouseListener(mouseListener);
        m_navTree.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        m_navTree.setBackground(bkgColor);

        JScrollPane treePanel = new JScrollPane(m_navTree);
        treePanel.setBorder(BorderFactory.createEmptyBorder());
        setLeftComponent(treePanel);
        m_mainDisplayPanel = new PSMainDisplayPanel(m_view,
                m_applet.getActionManager(), sortView);
        m_mainDisplayPanel.setBorder(BorderFactory.createEmptyBorder());
        m_mainDisplayPanel.getTable().getSelectionModel()
                          .addListSelectionListener(this);
        m_mainDisplayPanel.getTable().addMouseListener(mouseListener);

        setRightComponent(m_mainDisplayPanel);
        setDividerLocation(200);
        setDividerSize(7);
    }

    /**
     * Adds the selection listener to its list of listeners to fire the
     * event when the selection has changed in tree view or table view.
     *
     * @param selListener the listener interested in selection changes, may not
     * be <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public void addSelectionListener(IPSSelectionListener selListener) {
        if (selListener == null) {
            throw new IllegalArgumentException("selListener may not be null.");
        }

        m_selListeners.add(selListener);
    }

    /**
     * Action method for selection change event in navigational tree. Constructs
     * the <code>PSNavigationalSelection</code> object from the event and informs
     * the selection listeners of the change.
     *
     * @param e the event, assumed not <code>null</code> as this method is called
     * by Swing.
     */
    public void valueChanged(@SuppressWarnings("unused")
    TreeSelectionEvent e) {
        /*
         * Ignore selections from drag under events
         */
        if (m_navTree.isInDragUnder()) {
            return;
        }

        PSNode selNode = m_navTree.getSelectedNode();

        if (selNode != null) {
            String path = m_navTree.getSelectedPath();

            PSUiMode mode = new PSUiMode(m_view, PSUiMode.TYPE_MODE_NAV);
            PSNode parent = m_navTree.getSelectedParentNode();
            Iterator selection = PSIteratorUtils.iterator(selNode);
            PSNavigationalSelection sel = new PSNavigationalSelection(mode,
                    parent, selection, path);

            informListeners(sel);

            // Refresh child nodes if necessary - don't do this if nav tree is in 
            // process of dirtying nodes as this will cause a "premature" refresh.
            if (!m_navTree.isDirtyingNodes()) {
                refreshChildNodes(m_navTree.getSelectionPath(), selNode);
            }

            // Don't sort the display panel if in doc assembly and a slot is
            // selected.  Sort everything else.
            boolean sortView = true;

            if (m_view.equals(PSUiMode.TYPE_VIEW_IA) &&
                    selNode.getType().equals(PSNode.TYPE_SLOT)) {
                sortView = false;
            }

            m_mainDisplayPanel.setIsSortingEnabled(sortView);
            m_mainDisplayPanel.setData(selNode, parent);

            //Repaint the tree in Item Assembly view to reflect the slot that
            //can take the items in the search results be different.
            if (m_view.equals(PSUiMode.TYPE_VIEW_IA)) {
                m_navTree.repaint();
            }
        }
    }

    /**
     * Action method for expansion event in navigational tree. Updates child tree
     * nodes to reflect current state of data.
     *
     * @param e the event, assumed not <code>null</code> as this method is called
     * by Swing.
     *
     * @throws ExpandVetoException never
     */
    @SuppressWarnings("unused")
    public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
        TreePath treePath = e.getPath();
        DefaultMutableTreeNode tmp = (DefaultMutableTreeNode) treePath.getLastPathComponent();
        PSNode node = (PSNode) tmp.getUserObject();

        //Display children of the node in the right component to display its
        //children.
        refreshChildNodes(treePath, node);
    }

    // see interface
    @SuppressWarnings("unused")
    public void treeWillCollapse(TreeExpansionEvent event)
        throws ExpandVetoException {
        //noop
    }

    /**
     * Notifies the selection listeners that the selection has changed.
     *
     * @param selection the new selection, assumed not <code>null</code>
     */
    private void informListeners(PSSelection selection) {
        Iterator listeners = m_selListeners.iterator();

        while (listeners.hasNext()) {
            IPSSelectionListener listener = (IPSSelectionListener) listeners.next();
            listener.selectionChanged(selection);
        }
    }

    /**
     * Loads/re-loads child nodes if required and re-freshes nav tree and right
     * hand pane.
     *
     * @param treePath The tree path of the tree node in the nav tree that
     * should be updated, assumed not <code>null</code>.
     * @param node The node whose children may need to be refreshed, assumed not
     * <code>null</code>.
     */
    private void refreshChildNodes(TreePath treePath, PSNode node) {
        Iterator children = node.getChildren();

        // don't check for dirty if option is not enabled in server config
        if ((children == null) || (node.hasDirtyChildren()) ||
                ((!m_applet.getActionManager().isQuickLoading(node)) &&
                node.isQuickLoaded())) {
            children = m_applet.getActionManager().loadChildren(node);
            m_navTree.nodeChanged(treePath, node);
        }
    }

    /**
     * Action method for selection change event in main display table. Constructs
     * the <code>PSSelection</code> object from the event (selected rows) and
     * informs the selection listeners of the change.
     *
     * @param e the event, assumed not <code>null</code> as this method is called
     * by Swing.
     */
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            return;
        }

        Iterator selNodes = m_mainDisplayPanel.getSelectedRowNodes();

        if (selNodes.hasNext()) {
            PSNode parent = m_navTree.getSelectedNode();
            PSSelection sel = new PSSelection(new PSUiMode(m_view,
                        PSMainDisplayPanel.ms_mode), parent, selNodes);
            informListeners(sel);
        }
    }

    //implements interface method
    @SuppressWarnings("unchecked")
    public void actionExecuted(PSActionEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event may not be null.");
        }

        PSNode nodeToRefresh = null;
        String refreshHint = event.getRefreshHint();
        int nodeType = PSNavigationTree.getRefreshNodeType(refreshHint);

        if (nodeType != -1) {
            nodeToRefresh = m_navTree.getNode(nodeType);

            if (nodeToRefresh != null) {
                boolean wasExpanded = nodeToRefresh.shouldExpand();

                if (event.isFullRefresh()) {
                    nodeToRefresh.clearDirtyChildren(true);
                }

                nodeToRefresh = m_applet.getActionManager()
                                        .refresh(nodeToRefresh);
                nodeToRefresh.setExpand(wasExpanded);
                m_navTree.nodeChanged(nodeToRefresh, nodeType);
                m_navTree.selectNode(nodeToRefresh);
            }
        } else if (refreshHint.equals(PSActionEvent.REFRESH_NODES)) {
            Iterator nodes = event.getRefreshNodes();
            PSNode oldNode = null;
            PSNode newNode = null;
            boolean wasExpanded = false;

            while (nodes.hasNext()) {
                oldNode = (PSNode) nodes.next();
                wasExpanded = oldNode.shouldExpand();

                if (event.isFullRefresh()) {
                    oldNode.clearDirtyChildren(true);
                }

                newNode = m_applet.getActionManager().refresh(oldNode);
                newNode.setExpand(wasExpanded);
                m_navTree.nodeChanged(newNode, oldNode);
            }

            //We always keep the last node in the list as selected
            if (newNode != null) {
                m_navTree.selectNode(newNode);
            }
        } else if (refreshHint.equals(PSActionEvent.DIRTY_NODES)) {
            // try to locate and dirty current nodes
            // if none found, insert new dirty node into each view/search/folder
            // so the new items/folders may appear on selective refresh
            List dirtyList = PSIteratorUtils.cloneList(event.getRefreshNodes());
            Iterator dirtyNodes = dirtyList.iterator();

            while (dirtyNodes.hasNext()) {
                PSNode node = (PSNode) dirtyNodes.next();
                List types = new ArrayList();
                types.addAll(PSNode.getFolderTypes());
                types.addAll(PSNode.getSearchTypes());
                m_navTree.dirtyChildNodes(node, types);
            }
        }
    }

    //noop
    public void actionInitiated(
        @SuppressWarnings("unused")
                PSProcessMonitor monitor) {
    }

    /**
     * Access method for the navigation tree object.
     * @return the navigation tree object associated with the view, never
     * <code>null</code>.
     */
    public PSNavigationTree getNavTree() {
        return m_navTree;
    }

    /**
     * Access method for the main display panel.
     * @return returns the main display panel, never <code>null</code>.
     */
    public PSMainDisplayPanel getMainDisplayPanel() {
        return m_mainDisplayPanel;
    }

    /**
     * The class that handles pop-up mouse clicks to show pop-up menu. Displays
     * the global menu as pop-up menu if the mouse click is on the left panel
     * that contains navigation tree and the clicked location does not represent
     * a node.
     */
    private class MouseActionListener extends PSMouseAdapter {
        /**
         * Handles right-click mouse event to show the global-menu if the clicked
         * location does not represent a node in the navigation tree that is in
         * the left panel.
         *
         * @param event event the mouse click event, assumed not <code>null</code>
         * this is called by Swing model.
         */
        public void mouseWasClicked(MouseEvent event) {
            if (PSCxUtil.isMouseMenuGesture(event, m_applet)) {
                if (event.getSource() == m_navTree) {
                    TreePath path = m_navTree.getPathForLocation(event.getX(),
                            event.getY());

                    if (path == null) {
                        PSContentExplorerMenuBar menuBar = m_applet.getGlobalMenuBar();

                        Point point = event.getLocationOnScreen();
                        int x = point.x;
                        int y = point.y;

                        Component component = (Component) event.getSource();
                        Point componentLocation = component.getLocationOnScreen();
                        x = x - componentLocation.x;
                        y = y - componentLocation.y;
                        menuBar.getPopupMenu().show(component, x, y);
                    }
                }
            } else if (SwingUtilities.isLeftMouseButton(event) &&
                    (event.getClickCount() == 2)) {
                if (event.getSource() == m_mainDisplayPanel.getTable()) {
                    PSNode selNode = null;

                    //This will always have only one entry because, double-click removes
                    //the previous selection and makes the clicked row alone as selected.
                    //even if multiple rows comes, take the first one.
                    Iterator selNodes = m_mainDisplayPanel.getSelectedRowNodes();

                    if (selNodes.hasNext()) {
                        selNode = (PSNode) selNodes.next();
                    } else {
                        return;
                    }

                    if (selNode.isContainer()) {
                        m_navTree.selectNode(selNode);
                    } else //execute the default action for the node if applicable
                     {
                        //parent node a node selected in display panel is the node
                        //selected in the navigation tree.
                        PSNode parentNode = m_navTree.getSelectedNode();
                        PSUiMode mode = new PSUiMode(m_view,
                                PSMainDisplayPanel.ms_mode);
                        PSSelection selection = new PSSelection(mode,
                                parentNode, PSIteratorUtils.iterator(selNode));
                        PSMenuAction defAction = m_applet.getActionManager()
                                                         .findDefaultAction(selection);

                        if (defAction != null) {
                            m_applet.getActionManager()
                                    .executeAction(defAction, selection);
                        }
                    }
                }
            }
        }
    }

    /**
     * The renderer to work with 'IA' view to display the 'slot' in different
     * font if the current selected node can be dropped on to the current slot.
     * A node can be dropped on to a slot if it represents a search result whose
     * name is the slot name or if it is a child node of such node.
     */
    private class PSIAViewNodeRenderer extends PSNavTreeNodeRenderer {
        public PSIAViewNodeRenderer(PSContentExplorerApplet m_applet) {
            super(m_applet);
        }

        /**
         * Overridden to implement the description specified in this class
         * description. See super class for description of the parameter and
         * return values.
         */
        public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            @SuppressWarnings("hiding")
        boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded,
                leaf, row, hasFocus);

            boolean isSlotSearchNode = false;
            DefaultMutableTreeNode tmp = (DefaultMutableTreeNode) value;
            PSNode node = (PSNode) tmp.getUserObject();

            // If selected node is a search result in IA View, then apply
            // yellow color to the slot that matches the search result name.
            if (node.isOfType(PSNode.TYPE_SLOT)) {
                TreePath path = m_navTree.getSelectionPath();

                if (path != null) {
                    Object[] nodes = path.getPath();

                    for (int i = nodes.length - 1; i >= 0; i--) {
                        tmp = (DefaultMutableTreeNode) nodes[i];

                        PSNode selNode = (PSNode) tmp.getUserObject();

                        if (selNode.isOfType(PSNode.TYPE_NEW_SRCH) &&
                                node.equals(selNode.getSlotNode())) {
                            isSlotSearchNode = true;

                            break;
                        }
                    }
                }
            }

            /**
             * @todo get color from option manager
             */
            if (isSlotSearchNode) {
                setBackgroundNonSelectionColor(Color.yellow);
            } else {
                setBackgroundNonSelectionColor(UIManager.getColor(
                        "Tree.textBackground"));
            }

            return this;
        }
    }
}
;
