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
package com.percussion.rx.jsf;

import static org.apache.commons.lang.Validate.notNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.percussion.services.error.PSNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.trinidad.model.RowKeyIndex;

/**
 * The category node base implements the basic behavior needed for category
 * nodes. A category node implementation will override the
 * {@link #getChildren()} method contained in this class to provide the loading
 * mechanism for the class' children dynamically, or will calculate the contents
 * statically and add them using {@link #addNode(PSNodeBase)}.
 * <p>
 * To work within the Trinidad tree model, the collection must implement
 * {@link RowKeyIndex} so the tree model can delegate requests to it. It keeps
 * track of a current index being accessed. The index can be on an element or
 * point beyond the end of the child list or be <code>-1</code> which
 * indicates that it isn't currently set.
 * 
 * @author dougrand
 * 
 */
public class PSCategoryNodeBase extends PSNodeBase
{
   /**
    * The children of the category. This starts <code>null</code>. When the
    * {@link #getChildren()} method is called the collection is created by using
    * the appropriate cataloger. The exact semantics may be modified by a 
    * subclass.
    */
   protected List<PSNodeBase> m_children;

   /**
    * The current index into the collection. <code>-1</code> indicates that no
    * element is currently selected.
    */
   protected int m_index = -1;
   
   /**
    * A string to filter returned subnodes with. <code>null</code> or
    * empty indicates no filter. 
    */
   protected String m_filter;

   /**
    * Ctor.
    * 
    * @param title the node's title, never <code>null</code> or empty.
    * @param outcome the outcome, may be <code>null</code>.
    */
   public PSCategoryNodeBase(String title, String outcome)
   {
      super(title, outcome);
      setKeyFromOutcome();
   }

   /**
    * Constructor allowing to specify title, outcome and label.
    *
    * @param title never <code>null</code> or empty.
    * @param outcome the outcome, may be <code>null</code>.
    * @param label the value returned by {@link #getLabel()}.
    * Can be <code>null</code> or blank.
    * @see #getLabel() 
    */
   public PSCategoryNodeBase(String title, String outcome, String label) {
      super(title, outcome, label);
      setKeyFromOutcome();
   }

   /**
    * Gets the number of rows per page for the table that displays the child 
    * components.
    * 
    * @return the number of rows per page.
    */
   public int getPageRows()
   {
      return PSNodeBase.getPageRows(getRowCount());
   }
   
   @Override
   public boolean isContainer()
   {
      return true;
   }

   @Override
   public boolean isContainerEmpty()
   {
      return m_children == null || m_children.isEmpty();
   }

   /**
    * Add a node to this category.
    * 
    * @param node the node to add, never <code>null</code>.
    */
   public void addNode(PSNodeBase node)
   {
      notNullNode(node);
      if (m_children == null)
      {
         m_children = new ArrayList<>();
      }
      m_children.add(node);
      node.setParent(this);
      getModel().addNode(node);
   }

   /**
    * Throws IllegalArgumentException if the provided node is <code>null</code>.
    * @param node the node to validate.
    * An exception is thrown if <code>null</code>.
    */
   private void notNullNode(PSNodeBase node)
   {
      notNull(node, "node may not be null");
   }


   /**
    * Sets the key based on the outcome.
    */
   private void setKeyFromOutcome()
   {
      m_key = StringUtils.isBlank(getOutcome()) ? makeRowKey() : getOutcome();
   }

   @Override
   public List<? extends PSNodeBase> getChildren() throws PSNotFoundException {
      return m_children;
   }

   /**
    * Set child nodes to <code>null</code>, used as a way to flush the child
    * nodes. The derived class must retrieve the data from services.
    */
   public void resetChildren()
   {
      m_children = null;
   }
   
   /**
    * Remove the given node from this parent. The node will already be removed
    * from the model.
    * 
    * @param node the node to remove, never <code>null</code>.
    */
   @Override
   public void remove(PSNodeBase node)
   {
      notNullNode(node);
      m_children.remove(node);
   }

   @Override
   public boolean getEnabled()
   {
      return StringUtils.isNotBlank(getOutcome())
            && getModel().getNavigator().areCategoryNodesEnabled();
   }

   @Override
   public String toString()
   {
      return toString(0);
   }

   @Override
   public String toString(int indendation)
   {
      StringBuilder b = new StringBuilder();
      for (int i = 0; i < indendation; i++)
      {
         b.append(' ');
      }
      b.append(super.toString());
      b.append('\n');
      if (m_children != null)
      {
         for (PSNodeBase node : m_children)
         {
            b.append(node.toString(indendation + 2));
            b.append('\n');
         }
      }
      return b.toString();
   }

   @Override
   public int getRowCount()
   {
      return m_children == null ? -1 : m_children.size();
   }

   @Override
   public Object getRowData()
   {
      return getRowData(m_index);
   }

   @Override
   public Object getRowData(int i)
   {
      return isRowAvailable(i) ? m_children.get(i) : null;
   }

   @Override
   public int getRowIndex()
   {
      return m_index;
   }

   @Override
   public Object getRowKey()
   {
      final PSNodeBase node = (PSNodeBase) getRowData();
      return node == null ? null : node.getKey();
   }

   @Override
   public boolean isRowAvailable()
   {
      return isRowAvailable(m_index);
   }

   @Override
   public boolean isRowAvailable(int index)
   {
      return m_children != null
            && index >= 0 && index < m_children.size()
            && m_children.get(index) != null;
   }

   @Override
   public void setRowIndex(int i)
   {
      m_index = i;
   }

   @Override
   public void setRowKey(Object key)
   {
      if (key == null)
      {
         throw new IllegalArgumentException("key may not be null");
      }
      
      if (m_children == null) return;
      String comparekey = key.toString();
      int current = m_index;
      for(m_index = 0; m_index < m_children.size(); m_index++)
      {
         if (isRowAvailable())
         {
            if (comparekey.equals(getRowKey()))
            {
               return;
            }
         }
      }

      // Revert if not found
      m_index = current;
   }

   /**
    * The filter string is used as a pattern source to limit the child nodes
    * returned. The character '*' acts as a wildcard. By default the string is
    * treated as a left substring match, case not significant.
    * @return the filter, may be empty or <code>null</code>.
    */
   public String getFilter()
   {
      return m_filter;
   }

   /**
    * @param filter the filter to set.
    * No filtering is done if <code>null</code> or blank.
    */
   public void setFilter(String filter)
   {
      m_filter = filter;
   }
   
   /**
    * Get nodes that match the current set pattern.
    * @return the matching nodes, may be empty. If it is not empty, then
    * there always be one selected node.
    */
   public List<PSNodeBase> getFilteredNodes() throws PSNotFoundException {
      if (getChildren() == null)
      {
         return Collections.emptyList();
      }
      if (StringUtils.isBlank(m_filter))
      {
         return Collections.unmodifiableList(m_children);
      }

      List<PSNodeBase> rval = new ArrayList<>();
      final String star = "*";
      final String allPattern = ".*";
      String pattern = m_filter.replaceAll("\\*", allPattern);
      if (!m_filter.endsWith(star))
      {
         pattern = pattern + allPattern;
      }
      if (!m_filter.startsWith(star))
      {
         pattern = allPattern + pattern;
      }
      pattern = pattern.toLowerCase();
      boolean hasSelected = false;
      for (PSNodeBase node : m_children)
      {
         String label = node.getTitle();
         if (Pattern.matches(pattern, label.toLowerCase()))
         {
            if (node.getSelectedRow())
               hasSelected = true;
            
            rval.add(node);
            continue;
         }
         // un-select node that is not visible.
         if (node.getSelectedRow())
            node.setSelectedRow(false);
      }
      // make sure there is a selected visible row.
      if (rval.size() > 0 && (!hasSelected))
      {
         rval.get(0).setSelectedRow(true);
      }
      
      return Collections.unmodifiableList(rval);
   }
   
   /**
    * @return the name of the css class to use when rendering this node's
    * link in the navigation tree.
    */
   @Override
   public String getNavLinkClass()
   {
      return "treenode";
   }
   
   /*
    * The action for a category does nothing but return the outcome that moves
    * to the "view" for the category. Subclasses can add more behavior if
    * appropriate.
    */
   @Override
   public String perform()
   {
      PSNavigation nav = getModel().getNavigator();
      nav.setCurrentCategoryKey((String) m_key);
      return super.perform();
   }

   /**
    * Find a selected node and return it. This method allows for the possibility
    * that we might allow multi selection in some collections, and therefore 
    * returns the first match that fulfills the criteria.
    * 
    * @param clazz if not <code>null</code> then limit the returned node to
    * the given class.
    * @return the selected node or <code>null</code> if one is not found.
    */
   @SuppressWarnings("unchecked")
   public PSNodeBase findSelected(Class clazz)
   {
      if (m_children == null) return null;
      
      for(PSNodeBase node : m_children)
      {
         if (node.getSelectedRow())
         {
            if (clazz == null || clazz.isAssignableFrom(node.getClass()))
            {
               return node;
            }
         }
      }
      return null;
   }
   
   /**
    * Same as <code>findSelected(null)</code>.
    * @return the currently selected node of any class.
    * <code>null</code> if no node is currently selected.
    */
   public PSNodeBase findSelected()
   {
      return findSelected(null);
   }
   
   /**
    * Provides all the names for the type of the object contained in this
    * category. Is used for verifying a name for uniqueness.
    * The subclasses need to implement this method only if their editors
    * validate for unique names. 
    * @return all the names of the object type the category corresponds to.
    * The default implementation returns an empty set.
    * Never <code>null</code>. 
    */
   public Set<Object> getAllNames()
   {
      return Collections.emptySet();
   }
   
   /**
    * Returns the outcome to return to the corresponding view.
    * The default implementation throwns a.
    */
   public String returnToListView()
   {
      throw new UnsupportedOperationException(
            "returnToListView is not implemented for category node \""
            + getTitle() + "\"");
   }
}
