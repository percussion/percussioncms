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
import com.percussion.cx.objectstore.PSProperties;
import com.percussion.utils.collections.PSIteratorUtils;

import javax.accessibility.AccessibleContext;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The splitplane that displays ancestors and descends of a content item
 * for a specific relationship.
 */
public class PSDependencyViewer extends JSplitPane
   implements IPSViewChangeListener, IPSActionListener, TreeSelectionListener
{
   /**
    * Constructs the dependency viewer with a horizontal-split to show its
    * dependencies.
    * @param actionManager the action manager to use for context sensitive menu
    * and load children dynamically, may not be <code>null</code>
    * @param itemNode the node that represents the item whose dependencies are
    * viewed with the properties defining the item and default relationship, may
    * not be <code>null</code>
    *
    * @throws IllegalArgumentException if actionManager is <code>null</code>.
    */
   public PSDependencyViewer(PSActionManager actionManager, PSNode itemNode)
   {
      if(actionManager == null)
         throw new IllegalArgumentException("actionManager may not be null.");

      if(itemNode == null)
         throw new IllegalArgumentException("itemNode may not be null.");

      init(actionManager, itemNode);
   }

   /**
    * Initializes this view with 'Descendants' tree on the top panel and the
    * 'Ancestors' tree in the bottom panel.
    *
    * @param mgr the manager to use to with the trees, assumed not <code>null
    * </code>
    * @param itemNode The node of the item for which descendants and ancestors 
    * will be viewed, assumed not <code>null</code>.
    */
   private void init(PSActionManager mgr, PSNode itemNode)
   {
      m_actManager = mgr;
      setOrientation(JSplitPane.VERTICAL_SPLIT);
      String dlNameStr =
            m_actManager.getApplet().getResourceString(getClass(), "@Descendents");
      JLabel descLabel = new JLabel(dlNameStr, SwingConstants.LEFT);
      char dlmnemonic =
         PSContentExplorerApplet.getResourceMnemonic(
            getClass(),
            "@Descendents", 'D');
      String dldesc =
            m_actManager.getApplet().getResourceString(
            getClass(),
            "@DescendentDescription");
      descLabel.setName(PSContentExplorerConstants.HEADING_COMP_NAME);
      if (dlmnemonic != 0) {
         descLabel.setDisplayedMnemonic(dlmnemonic);       
      }
      descLabel.setAlignmentX(CENTER_ALIGNMENT);
      descLabel.setBorder(BorderFactory.createRaisedBevelBorder());

      m_descNode = (PSNode)itemNode.clone();
      m_descNode.setProperty(PSItemRelationshipsManager.PROP_RS_LOOKUP_TYPE,
         PSItemRelationshipsManager.RS_LOOKUP_DESCENDANTS);

      m_descTree = new PSNavigationTree(m_descNode, ms_view, mgr, false);
      descLabel.setLabelFor(m_descTree);
      //m_descTree.setBorder(border);
      m_descTree.addTreeSelectionListener(this);
      AccessibleContext ctx = m_descTree.getAccessibleContext();
      ctx.setAccessibleName(dlNameStr);
      ctx.setAccessibleDescription(dldesc);
      
      JScrollPane descTreePanel = new JScrollPane(m_descTree);
      descTreePanel.setAlignmentX(CENTER_ALIGNMENT);
      descTreePanel.setBorder(BorderFactory.createEmptyBorder());
      JPanel topPanel = new JPanel(new BorderLayout());
      topPanel.add(descLabel, BorderLayout.NORTH);
      topPanel.add(descTreePanel, BorderLayout.CENTER);

      setTopComponent(topPanel);

      String ancNameStr = m_actManager.getApplet().getResourceString(
         getClass(), "@Ancestors");
      JLabel ancLabel = new JLabel(ancNameStr,SwingConstants.LEFT);
      char almnemonic = PSContentExplorerApplet.getResourceMnemonic(
         getClass(), "@Ancestors", 'A'); 
      String ancdesc = m_actManager.getApplet().getResourceString(
               getClass().getName() + "@AncestorDescription");         
      descLabel.setName(PSContentExplorerConstants.HEADING_COMP_NAME);          
      if (almnemonic != 0) {
         ancLabel.setDisplayedMnemonic(almnemonic);       
      }                 
      ancLabel.setName(PSContentExplorerConstants.HEADING_COMP_NAME);
      ancLabel.setAlignmentX(CENTER_ALIGNMENT);
      ancLabel.setBorder(BorderFactory.createRaisedBevelBorder());

      m_ancNode = (PSNode)itemNode.clone();
      m_ancNode.setProperty(PSItemRelationshipsManager.PROP_RS_LOOKUP_TYPE,
         PSItemRelationshipsManager.RS_LOOKUP_ANCESTORS);

      m_ancTree = new PSNavigationTree(m_ancNode, ms_view, mgr, false);
      ancLabel.setLabelFor(m_ancTree);
      //m_ancTree.setBorder(border);
      m_ancTree.addTreeSelectionListener(this);
      ctx = m_ancTree.getAccessibleContext();
      ctx.setAccessibleName(ancNameStr);
      ctx.setAccessibleDescription(ancdesc);
      ctx = m_ancTree.getAccessibleContext();
      ctx.setAccessibleName(ancNameStr);
      ctx.setAccessibleDescription(ancdesc);
      
      JScrollPane ancTreePanel = new JScrollPane(m_ancTree);
      ancTreePanel.setAlignmentX(CENTER_ALIGNMENT);
      ancTreePanel.setBorder(BorderFactory.createEmptyBorder());
      JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.add(ancLabel, BorderLayout.NORTH);
      bottomPanel.add(ancTreePanel, BorderLayout.CENTER);

      setBottomComponent(bottomPanel);

      setDividerLocation(150);
      setDividerSize(7);
   }

   //implements to update this view's data from supplied data object.
   public void viewDataChanged(Object data)
   {
      if(!(data instanceof PSProperties))
         throw new IllegalArgumentException(
            "data must be an instance of PSProperties");

      PSProperties props = (PSProperties)data;

      m_descNode.setProperty(IPSConstants.PROPERTY_RELATIONSHIP,
         props.getProperty(IPSConstants.PROPERTY_RELATIONSHIP));
      m_descNode.removeChildren();
      m_descTree.nodeChanged(m_descNode, PSNavigationTree.NODE_ROOT);

      m_ancNode.setProperty(IPSConstants.PROPERTY_RELATIONSHIP,
         props.getProperty(IPSConstants.PROPERTY_RELATIONSHIP));
      m_ancNode.removeChildren();
      m_ancTree.nodeChanged(m_ancNode, PSNavigationTree.NODE_ROOT);
   }
   
   //implements interface method
   public void actionExecuted(PSActionEvent event)
   {
      if(event == null)
         throw new IllegalArgumentException("event may not be null.");

      int nodeType = PSNavigationTree.getRefreshNodeType(
            event.getRefreshHint());
      if(nodeType != -1)
      {
         PSNode nodeToRefresh = m_descTree.getNode(nodeType);
         if(nodeToRefresh != null)
         {
            nodeToRefresh = m_actManager.refresh(nodeToRefresh);
            m_descTree.nodeChanged(nodeToRefresh, nodeType);
            m_descTree.selectNode(nodeToRefresh);
         }
         
         nodeToRefresh = m_ancTree.getNode(nodeType);
         if(nodeToRefresh != null)
         {
            nodeToRefresh = m_actManager.refresh(nodeToRefresh);
            m_ancTree.nodeChanged(nodeToRefresh, nodeType);
            m_ancTree.selectNode(nodeToRefresh);
         }
      }
   }

   //nothing todo
   public void actionInitiated(PSProcessMonitor monitor)
   {
   }
   
   /**
    * Adds the selection listener to its list of listeners to fire the
    * event when the selection has changed in tree view or table view.
    *
    * @param selListener the listener interested in selection changes, may not
    * be <code>null</code>
    */
   public void addSelectionListener(IPSSelectionListener selListener)
   {
      if(selListener == null)
         throw new IllegalArgumentException("selListener may not be null.");

      m_selListeners.add(selListener);
   }
   
   /**
    * Action method for selection change event in any tree. Constructs the 
    * <code>PSNavigationalSelection</code> object from the event and informs
    * the selection listeners of the change.
    *
    * @param e the event, assumed not <code>null</code> as this method is called
    * by Swing.
    */
   public void valueChanged(TreeSelectionEvent e)
   {
      PSNavigationTree srcTree = (PSNavigationTree)e.getSource();
      
      PSNode selNode = srcTree.getSelectedNode();
      if(selNode != null)
      {
         String path = srcTree.getSelectedPath();

         PSUiMode mode = new PSUiMode(ms_view, PSUiMode.TYPE_MODE_NAV);
         PSNode parent = srcTree.getSelectedParentNode();
         Iterator selection = PSIteratorUtils.iterator(selNode);
         PSNavigationalSelection sel =
            new PSNavigationalSelection(mode, parent, selection, path);

         informListeners(sel);
      }
   }
   
   /**
    * Selects the root node in the Descendents tree.
    */
   public void selectDefaultNode()
   {
      m_descTree.setSelectionRow(0);
   }
   
   /**
    * Notifies the selection listeners that the selection has changed.
    *
    * @param selection the new selection, assumed not <code>null</code>
    */
   private void informListeners(PSSelection selection)
   {
      Iterator listeners = m_selListeners.iterator();
      while(listeners.hasNext())
      {
         IPSSelectionListener listener = (IPSSelectionListener)listeners.next();
         listener.selectionChanged(selection);
      }
   }

   /**
    * The user object of the root node of the 'Descendants' tree, initialized in
    * the <code>init(PSActionManager, PSNode)</code> and never <code>null</code>
    * after that. This node will be set with properties for <code>
    * PROPERTY_RELATIONSHIP</code>('relationship') and
    * <code>PROP_RS_LOOKUP_TYPE</code>('relationship lookup type'). The lookup
    * type for this node is <code>RS_LOOKUP_DESCENDANTS</code>. Modified
    * through calls to the method <code>viewDataChanged(Object)</code> as the
    * 'relationship' property changes.
    */
   private PSNode m_descNode;

   /**
    * The user object of the root node of the 'Ancestors' tree, initialized in
    * the <code>init(PSActionManager, PSNode)</code> and never <code>null</code>
    * after that. This node will be set with properties for <code>
    * PROPERTY_RELATIONSHIP</code>('relationship') and
    * <code>PROP_RS_LOOKUP_TYPE</code>('relationship lookup type'). The lookup
    * type for this node is <code>RS_LOOKUP_ANCESTORS</code>. Modified
    * through calls to the method <code>viewDataChanged(Object)</code> as the
    * 'relationship' property changes.
    */
   private PSNode m_ancNode;

   /**
    * The tree that represents the descendants of an item for a specific
    * relationship. Initialized in the ctor and will be modified in <code>
    * viewDataChanged(Object)</code> as user changes the relationship.
    */
   private PSNavigationTree m_descTree;

   /**
    * The tree that represents the ancestors of an item for a specific
    * relationship. Initialized in the ctor and will be modified in <code>
    * viewDataChanged(Object)</code> as user changes the relationship.
    */
   private PSNavigationTree m_ancTree;
   
   /**
    * The action manager to use to refresh/load the children for a specific node,
    * initialized in the ctor and never <code>null</code> or modified after
    * that.
    */
   private PSActionManager m_actManager;
   
   /**
    * The list of selection listeners that are interested in selection changes
    * in this ancestor or descendents tree, initialized to an empty list and
    * listeners are being added through calls to <code>
    * addSelectionListener(IPSSelectionListener)</code>. Never <code>null</code>
    * after initialization.
    */
   private List m_selListeners = new ArrayList();
   
   /**
    * The constant representing this view.
    */
   private static final String ms_view = PSUiMode.TYPE_VIEW_DT;
}
