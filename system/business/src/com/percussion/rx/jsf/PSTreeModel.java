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

import com.percussion.rx.jsf.PSCategoryNodeBase;
import com.percussion.rx.jsf.PSNodeBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.model.MenuModel;

/**
 * The tree model provides a lazy loaded tree that is primarily managed by the
 * node implementations. The tree provides a basic navigation mechanism that 
 * allows the <code>tr:tree</code> to render the model. 
 * <p>
 * The actual tree is implemented by the node classes. The model is actually a
 * kind of tree walker, that maintains information about the current container
 * being referenced. 
 * <p>
 * Setting the key via {@link #setRowKey(Object)} will cause the model to now
 * point at the referenced node and make that node's parent the current 
 * container. 
 * <p>
 * Setting the row index {@link #setRowIndex(int)} simply navigates
 * within the current container. Note that the set index is not forced to be
 * an existing "row" within the container. It is acceptable to pass any zero or
 * greater value. If the index is not a value then {@link #isRowAvailable()}
 * will return <code>false</code>.
 * <p>
 * The methods for adding and removing nodes from the tree model are called by
 * the container nodes. 
 * <p>
 * The navigator provides access methods and is the actual managed bean that
 * is tied to JSF.
 * <h2>Row Key Implementation Notes</h2>
 * <p>
 * Due to limitations in the trinidad implementation, row keys presented by
 * the various methods that present row keys absolutely must use lists for the
 * keys. Internally the key map goes from a String identifier to a specific
 * node, which is quite efficient. Externally each node is identified by
 * the complete path going from root to leaf.
 * <p>
 * So a particular leaf would be represented by a path like this:
 * <br>
 * [ "rootkey", "parentkey", "leafkey" ]
 * 
 * @author dougrand
 *
 */
public class PSTreeModel extends MenuModel
{
   /**
    * The logger for the tree model.
    */
   private static final Log ms_log = LogFactory.getLog(PSTreeModel.class);
   
   /**
    * The current container, never <code>null</code> after construction.
    */
   PSNodeBase m_container = null;
   
   /**
    * Each added node is put in this map so it can be found, and removed
    * if the node is removed from its container.
    */
   Map<String,PSNodeBase> m_rowmap = new HashMap<>();

   /**
    * The focus row key. 
    */
   private Object m_focusRowKey; 
   
   /**
    * Navigator for this tree model, never <code>null</code> after ctor.
    */
   private PSNavigation m_navigator;
   
   /**
    * Create the model with the given root node.
    * @param root the root node, never <code>null</code>.
    * @param navigator the navigator, never <code>null</code>.
    */
   public PSTreeModel(PSCategoryNodeBase root, PSNavigation navigator)
   {
      if (root == null)
      {
         throw new IllegalArgumentException("root may not be null");
      }
      if (navigator == null)
      {
         throw new IllegalArgumentException("navigator may not be null");
      }
      m_navigator = navigator;
      m_container = root;
      m_container.setRowIndex(-1);
      addNode(root);
   }
   
   /**
    * @return the navigator. Never <code>null</code>.
    */
   public PSNavigation getNavigator()
   {
      return m_navigator;
   }

   @Override
   public void enterContainer()
   {
      ms_log.debug("Enter container");
      PSNodeBase current = (PSNodeBase) m_container.getRowData();
      if (current != null && current.isContainer())
      {
         ms_log.debug("Enter container: " + current.getTitle());
         m_container = current;
      }
   }

   @Override
   public void exitContainer()
   {
      if (m_container.getParent() != null)
      {
         ms_log.debug("Exit container");
         m_container = m_container.getParent();
      }
      else
      {
         //throw new IllegalStateException("Cannot exit root container");
         ms_log.warn("Exit container, but no parent");
      }
   }

   @SuppressWarnings("unchecked")
   @Override
   public Object getContainerRowKey(Object child)
   {
      Object key = null;
      if (child != null)
      {
         List<String> childkey = (List<String>) child;
         if (childkey.size() > 1)
         {
            key = childkey.subList(0, childkey.size() - 1);
         }
      }
  
      ms_log.debug("Get container row key: " + key);
      return key;
   }

   @Override
   public boolean isContainer()
   {
      boolean rval;
      PSNodeBase current = (PSNodeBase) m_container.getRowData();
      if (current == null)
      {
         // Don't know why the Row Data is not available. Doug's code (below) 
         //    throw new IllegalStateException("No current node");
         // caused a lot of (none reproducible) problems. Let's always return
         // FALSE for now.
         return false;
      }
      rval = current.isContainer();
      ms_log.debug("IsContainer: " + rval);
      return rval;
   }

   @Override
   public Object getRowKey()
   {
      Object key = null;
      PSNodeBase current = (PSNodeBase) m_container.getRowData();
      if (current != null)
      {
         key =  createExternalKey(current.getKey());
      }
      ms_log.debug("Get row key: " + key);
      return key;
   }

   @Override
   public int getRowCount()
   {
      PSNodeBase container = getCurrentContainer();
      
      if (container == null || container.getChildren() == null)
      {
         ms_log.debug("Get row count: no container or no children");
         return -1;
      }
      else
      {
         int count = container.getChildren().size();
         ms_log.debug("Get row count: " + count);
         return count;
      }
   }

   /**
    * Get the current container from the stack of containers.
    * @return the current container, never <code>null</code>.
    */
   private PSNodeBase getCurrentContainer()
   {
      return m_container;
   }

   @Override
   public Object getRowData()
   {
      PSNodeBase current = (PSNodeBase) m_container.getRowData();
      if (current == null)
      {
         ms_log.debug("Get row data: no current item");
         return null;
      }
      else
      {
         ms_log.debug("Get row data: " + current.getTitle());
         return current;
      }
   }

   @Override
   public int getRowIndex()
   {
      PSNodeBase container = getCurrentContainer();
      int index = container.getRowIndex();
      ms_log.debug("Get row index: " + index);
      return index;
   }

   @Override
   public Object getWrappedData()
   { 
      PSNodeBase root = m_container;
      while(root.getParent() != null)
      {
         root = root.getParent();
      }
      return root;
   }

   @Override
   public boolean isRowAvailable()
   {
      boolean avail = m_container.isRowAvailable();
      ms_log.debug("IsRowAvailable: " + avail);
      return avail;
   }

   @SuppressWarnings("unchecked")
   @Override
   public void setRowKey(Object key)
   {
      if (key == null)
      {
         // Move to root
         while(m_container.getParent() != null)
         {
            m_container.setRowIndex(-1);
            m_container = m_container.getParent();
         }
         m_container.setRowIndex(-1);
         return;
      }
      
      if (key instanceof List)
      {
         List<Object> listkey = (List<Object>) key;
         if (listkey.size() >= 1)
            key = listkey.get(listkey.size() - 1);
         else
         {
            setRowKey(null);
            return;
         }
      }
      
      PSNodeBase node = m_rowmap.get(key.toString());
      if (node != null && node.getParent() != null)
      {
         m_container = node.getParent();
         m_container.setRowKey(node.getKey());
         ms_log.debug("Set row key: " + key + " new container: " 
               + m_container.getTitle() + "(" + m_container.getKey() + ")");

      }
      else
      {
         ms_log.debug("Set row key: " + key + " no container or no node found");
      }
   }

   @Override
   public void setRowIndex(int index)
   {
      ms_log.debug("Set row index: " + index);
      PSNodeBase container = getCurrentContainer();
      container.setRowIndex(index);
   }

   @Override
   public void setWrappedData(Object arg0)
   {
      ms_log.debug("Set wrapped data: " + arg0);
      throw new UnsupportedOperationException("Cannot reset data");  
   }

   /**
    * Add a node to the model.
    * @param node the node to add, not <code>null</code>.
    */
   public void addNode(PSNodeBase node)
   {
      if (node == null)
      {
         throw new IllegalArgumentException("node may not be null");
      }
      node.setModel(this);
      m_rowmap.put((String) node.getKey(), node);
   }
   
   /**
    * Remove node from the model. Does not remove the node from the node tree,
    * that must be done explicitly.
    * 
    * @param node the node to remove, never <code>null</code>.
    */
   public void removeNode(PSNodeBase node)
   {
      if (node == null)
      {
         throw new IllegalArgumentException("node may not be null");
      }
      m_rowmap.remove(node.getKey());
   }

   @Override
   public List<String> getFocusRowKey()
   {
      if (m_focusRowKey == null)
         return null;
      else
         return createExternalKey(m_focusRowKey);
   }
   
   /**
    * Calculate and return a complete row key from root to leaf, but omit
    * the root node itself.
    * 
    * @param internalKey the internal key, assumed never <code>null</code>.
    * @return the complete row path, never <code>null</code>.
    */
   private List<String> createExternalKey(Object internalKey)
   {      
      PSNodeBase base = m_rowmap.get(internalKey);
      List<String> rval = new ArrayList<>();
      while(base != null && base.getParent() != null)
      {
         rval.add(0, (String) base.getKey());
         base = base.getParent();
      }
      return rval;
   }

   /**
    * Set the focus row for the model. Since the passed key is really a path,
    * we only need to store the final component.
    * 
    * @param key the focus row, may be <code>null</code> if there is no 
    * selected row.
    */
   public void setFocusRowKey(List<String> key)
   {
      m_focusRowKey = key.get(key.size() - 1);
   }
   
   /**
    * Get the named node from the model.
    * 
    * @param key the key, never <code>null</code> or empty.
    * @return the node, or <code>null</code> if not found.
    */
   public PSNodeBase getNodeByKey(String key)
   {
      if (StringUtils.isBlank(key))
      {
         throw new IllegalArgumentException(
               "key may not be null or empty");
      }
      return m_rowmap.get(key);
   }
}
