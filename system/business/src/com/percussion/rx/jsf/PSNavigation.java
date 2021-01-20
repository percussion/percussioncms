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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.rx.jsf;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.rx.publisher.jsf.nodes.PSSiteContainerNode;
import com.percussion.utils.guid.IPSGuid;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.RowKeySetImpl;

/**
 * The navigation bean controls the design or runtime tree control in the
 * navigation facet of the UI. The contained tree model is compatible with
 * Trinidad's tree control.
 * 
 * @author dougrand
 */
public class PSNavigation
{
   /**
    * The tree model.
    */
   protected PSTreeModel m_tree;

   /**
    * This stores the shown rows.
    */
   protected RowKeySet m_disclosedRows;

   /**
    * The current item's guid. May be <code>null</code> if there is no current
    * item.
    */
   private IPSGuid m_currentItem;

   /**
    * The current category's key. Set when a user clicks on a category node.
    */
   private String m_currentCategoryKey = PSSiteContainerNode.PUB_DESIGN_SITE_VIEWS;

   /**
    * The starting node of the navigation instance. This node is used to 
    * populate the starting page of the current tab
    */
   private PSNodeBase m_startingNode;
   
   /**
    * Default constructor.
    */
   public PSNavigation() 
   {
   }

   /**
    * Sets the starting node of the current tab.
    * @param node the new starting node, never <code>null</code>.
    */
   protected void setStartingNode(PSNodeBase node)
   {
      if (node == null)
         throw new IllegalArgumentException("node must not be null.");
      
      m_startingNode = node;
   }
   
   /**
    * @return the starting node, which is used to populate the starting page
    * of the current tab. Never <code>null</code>.
    */
   public PSNodeBase getStartingNode()
   {
      if (m_startingNode == null)
         throw new IllegalArgumentException("m_startingNode must not be null.");

      return m_startingNode;
   }
   
   /**
    * Change the focus on status node if it is not already on it.
    * This must be called on the starting JSP/JSF page; otherwise the
    * focus node on the left tree and the breadcrumb may not be correct.
    * 
    * This is used in case user was not on the starting node, switch to 
    * different tab, then switch back to the current tab.
    * 
    * Must call {@link #setStartingNode} first.
    */
   protected void focusOnStartingNode()
   {
      if (m_startingNode == null)
         throw new IllegalStateException("m_startingNode must not be null.");
      
      setCurrentItemKey((String)m_startingNode.getKey());
   }
   
   /**
    * The purpose of this method is to provide an interface for JSF/JSP to call
    * {@link #focusOnStartingNode()}. Assumed this is evaluated to 
    * {@link java.lang.String} by JSP.
    * 
    * @return arbitrary string, never <code>null</code> or empty.
    */
   public String getFocusOnStartingNode()
   {
      focusOnStartingNode();
      return "invoke PSNavigation.focusOnStartingNode()";
   }
   
   /**
    * @return the tree
    */
   public PSTreeModel getTree()
   {
      return m_tree;
   }

   /**
    * @param tree the tree to set
    */
   public void setTree(PSTreeModel tree)
   {
      m_tree = tree;
   }

   /**
    * @return the disclosedRows
    */
   public RowKeySet getDisclosedRows()
   {
      if (m_disclosedRows == null)
      {
         m_disclosedRows = new RowKeySetImpl();
      }
      return m_disclosedRows;
   }

   /**
    * @param disclosedRows the disclosedRows to set
    */
   public void setDisclosedRows(RowKeySet disclosedRows)
   {
      m_disclosedRows = disclosedRows;
   }

   /**
    * Set the current item to be edited's guid.
    * 
    * @param guid the guid, <code>null</code> to reset.
    */
   public void setCurrentItemGuid(IPSGuid guid)
   {
      m_currentItem = guid;
   }

   /**
    * Get the current item that is being edited. The current item is set when
    * the user edits an item. The current item is then reset on the save or
    * cancel action when the user is finished with the item.
    * <p>
    * If the current item is set, that item's node is shown in the tree as
    * selected and all other nodes are grayed out. If no item is set then the
    * entire tree is active.
    * 
    * @return the current item's guid or <code>null</code> if there is no
    *         current item.
    */
   public IPSGuid getCurrentItemGuid()
   {
      return m_currentItem;
   }

   /**
    * Allows JSF to retrieve a node by name. Throws an exception if the node is
    * not found.
    * 
    * @param key the name of the node desired, never <code>null</code> or
    *            empty.
    * @return the node, never <code>null</code>.
    */
   public PSNodeBase getNode(String key)
   {
      notNullKey(key);
      PSNodeBase node = getTree().getNodeByKey(key);
      if (node == null)
      {
         throw new IllegalStateException("Node " + key + " not found");
      }
      return node;
   }

   /**
    * Get the nodes from the given category node.
    * 
    * @param key the key that specifies the category node, never
    *            <code>null</code> or empty.
    * @return the filtered list, never <code>null</code>.
    */
   public List<PSNodeBase> getFilteredNodeList(String key)
   {
      return getCategoryNode(key).getFilteredNodes();
   }

   /**
    * Get the specified category node.
    * 
    * @param key the key of the category node, never <code>null</code> or
    *            empty.
    * @return the category node, never <code>null</code>.
    */
   private PSCategoryNodeBase getCategoryNode(String key)
   {
      notNullKey(key);
      PSNodeBase cnode = getNode(key);
      if (cnode == null || (!(cnode instanceof PSCategoryNodeBase)))
      {
         throw new IllegalStateException("The key " + key
               + " references a non-existent or non-container node");
      }
      return (PSCategoryNodeBase) cnode;
   }

   /**
    * Throws IllegalArgumentException if the provided key is null.
    * 
    * @param key the key to validate. If null, throws IllegalArgumentException.
    */
   private void notNullKey(String key)
   {
      notNull(key, "key may not be null or empty");
   }

   /**
    * Accessor for the node list.
    * 
    * @return the site nodes, never <code>null</code>.
    */
   public List<PSNodeBase> getList()
   {
      return getFilteredNodeList(m_currentCategoryKey);
   }

   /**
    * A convenience method that indicates whether this node has a filter
    * attached.
    */
   public void clearFilter()
   {
      setFilter(null);
   }
   
   /**
    * Get the filter from the current category node.
    * 
    * @return the filter, could be empty or <code>null</code>.
    */
   public String getFilter()
   {
      return getCategoryNode(m_currentCategoryKey).getFilter();
   }

   /**
    * Set the filter for the current category node.
    * 
    * @param filter the filter, may be <code>null</code> or empty.
    */
   public void setFilter(String filter)
   {
      getCategoryNode(m_currentCategoryKey).setFilter(filter);
   }

   /**
    * @return the current category key, never <code>null</code> or empty.
    */
   public String getCurrentCategoryKey()
   {
      return m_currentCategoryKey;
   }
   
   /**
    * Change the current category key.
    * 
    * @param rowKey the category key, never <code>null</code> or empty.
    */
   public void setCurrentCategoryKey(String rowKey)
   {
      if (StringUtils.isBlank(rowKey))
      {
         throw new IllegalArgumentException("rowKey may not be null or empty");
      }
      PSNodeBase referenceNode = m_tree.getNodeByKey(rowKey);
      if (referenceNode == null)
      {
         throw new IllegalStateException(
               "May not set current category key to value not in tree: "
                     + rowKey);
      }
      if (!(referenceNode instanceof PSCategoryNodeBase))
      {
         throw new IllegalStateException("Category key " + rowKey
               + " must reference a category node");
      }

      if (!rowKey.equals(m_currentCategoryKey) && getCollectionNode() != null)
      {
         // turn off the filter on category change
         getCollectionNode().setFilter(null);
      }
      m_currentCategoryKey = rowKey;
   }

   /**
    * Get the current selected item as a string, used for selections in the list
    * views.
    * 
    * @return the current key, may be <code>null</code> if nothing is
    *         selected.
    */
   public String getCurrentItemKey()
   {
      List<String> key = getTree().getFocusRowKey();
      if (key != null && key.size() > 0)
      {
         return key.get(key.size() - 1);
      }
      else
      {
         return null;
      }
   }

   /**
    * Set the current selected item.
    * 
    * @param key the selected item, may be <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public void setCurrentItemKey(String key)
   {
      Object old = getTree().getRowKey();
      getTree().setRowKey(key);
      getTree().setFocusRowKey((List<String>) getTree().getRowKey());
      getTree().setRowKey(old);

      // Add to shown
      m_disclosedRows.add(getTree().getFocusRowKey());
   }

   /**
    * Get the current selected node from the tree.
    * 
    * @return the current selected node, or <code>null</code> if nothing is
    *         selected.
    */
   public PSNodeBase getCurrentNode()
   {
      String currentKey = getCurrentItemKey();
      return getNodeByKey(currentKey);
   }

   /**
    * Return the current node if found one; if current node does not exist
    * then return the current collection node if exists; otherwise return
    * the starting node.
    * 
    * @return the either current node or collection node. 
    * Never <code>null</code>.
    */
   public PSNodeBase getActiveNode()
   {
      // 
      PSNodeBase activeNode = getCurrentNode();
      if (activeNode == null)
         activeNode = getCollectionNode();
      
      if (activeNode == null)
         activeNode = getStartingNode();
      
      if (activeNode == null)
         throw new IllegalStateException("ActiveNode==NULL is unexpected.");
      
      return activeNode;
   }
   /**
    * Get the node specified by the given key.
    * 
    * @param key the key, may be <code>null</code>
    * @return the node in the model specified by the key, or <code>null</code>
    *         if the key is <code>null</code> or if the node cannot be found.
    */
   private PSNodeBase getNodeByKey(String key)
   {
      if (key != null)
      {
         PSTreeModel model = getTree();
         return model.getNodeByKey(key);
      }
      else
      {
         return null;
      }
   }

   /**
    * Indicates whether the navigation bean category nodes are enabled. The
    * navigation bean can use this method to enforce disabling of the category
    * nodes.
    * 
    * @return <code>true</code> if the category nodes should be enabled.
    */
   public boolean areCategoryNodesEnabled()
   {
      return true;
   }

   /**
    * Return the node that is specified by the current category key.
    * 
    * @return the collection node, or <code>null</code> if nothing is
    *         selected, or the corresponding node cannot be found.
    */
   public PSCategoryNodeBase getCollectionNode()
   {
      return (PSCategoryNodeBase) getNodeByKey(m_currentCategoryKey);
   }
   
   /**
    * The outcome for navigating to a warning message screen when user 
    * picked an action that will act on a selected item, but the user hasn't 
    * selected anything (from a list) yet. 
    */
   public final static String NONE_SELECT_WARNING = "no-selection-warning";
}
