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

import com.percussion.rx.jsf.PSTreeModel;
import com.percussion.rx.ui.jsf.beans.PSHelpTopicMapping;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.trinidad.model.RowKeyIndex;

/**
 * The base node in the tree model. The tree model references a hierarchy of
 * these nodes. The nodes consist of category and leaf nodes, and the model is a
 * kind of iterator on the hierarchy.
 * <p>
 * Each node is responsible for returning an outcome from the {@link #perform()}
 * method. This is responsible for determining the navigation from the view or
 * tree to a particular editor or view. For editors it also stores the GUID of
 * the selected object, which is used to shift the navigation view between the
 * editor version, disabled with the single edited object highlighted, and the
 * view version with the navigation tree all enabled.
 * <p>
 * Whether a node is shown enabled is calculated by the {@link #getEnabled()}
 * method. The overall implementation of the node stamp for the tree control
 * accesses getters on this class and subclasses.
 * <p>
 * It is important to note that the model, {@link PSTreeModel}, delegates most
 * methods to the node implementation. Also, the tree model has a direct
 * reference to every node in the tree.
 * 
 * @author dougrand
 * 
 */
public class PSNodeBase implements RowKeyIndex
{
   /**
    * The size of the label.
    * @see #getLabel()
    */
   private static final int LABEL_SIZE = 36;

   /**
    * Row key generator for nodes that have none assigned.
    */
   private static AtomicLong ms_rowkeys = new AtomicLong();

   /**
    * The title for the node, never <code>null</code> or empty after
    * construction.
    */
   private String m_title;
   
   /**
    * @see #getLabel()
    */
   private final String m_label;

   /**
    * The outcome is the view that the node points to. If the outcome is
    * <code>null</code> then the node will not be rendered as a command link.
    */
   private String m_outcome = null;

   /**
    * @see #setKey(Object)
    */
   protected Object m_key = null;

   /**
    * The tree model, set by the tree model and propagated by the category node.
    */
   private PSTreeModel m_model = null;

   /**
    * Wrapped data that is part of the tree contract.
    */
   private Object m_wrappedData = null;

   /**
    * The parent pointer, or <code>null</code> if there is no parent.
    */
   private PSNodeBase m_parent = null;
   
   /**
    * Whether this node is selected - used in list views for actions. 
    */
   private boolean m_selectedRow = false;

   /**
    * Ctor.
    * 
    * @param title never <code>null</code> or empty.
    * @param outcome the outcome, may be <code>null</code>.
    */
   public PSNodeBase(String title, String outcome) {
      this(title, outcome, null);
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
   public PSNodeBase(String title, String outcome, String label) {
      if (StringUtils.isBlank(title))
      {
         throw new IllegalArgumentException(TITLE_NOT_NULL_OR_EMPTY);
      }
      m_title = title;
      m_outcome = outcome;
      m_key = makeRowKey();
      m_label = label;
   }

   /**
    * Create a row key for nodes that have no row key assigned.
    * 
    * @return a row key, never <code>null</code> or empty.
    */
   protected String makeRowKey()
   {
      return Long.toString(ms_rowkeys.incrementAndGet());
   }

   /**
    * Base action, which does nothing. 
    * 
    * @return the registered outcome, may be <code>null</code>.
    */
   public String perform()
   {
      PSNavigation navigator = getModel().getNavigator();
      navigator.setCurrentItemKey((String) getKey());      
      return getOutcome();
   }

   /**
    * This is used (or called) on the tree node. It defaults to be the same
    * as {@link #perform()}, unless override by derived classes/nodes.
    * 
    * @return the registered outcome, may be <code>null</code>.
    */
   public String performOnTreeNode()
   {
      return perform();
   }
   
   /**
    * Get the outcome, which tells the system what to do next. A particular node
    * implementation will set data on a managed bean and will cause the outcome
    * to navigate to a particular list or edit view.
    * 
    * @return the outcome, if <code>null</code> then the node does not point
    *         to a view and will not be rendered as a link.
    */
   public String getOutcome()
   {
      return m_outcome;
   }

   /**
    * Set the outcome. See {@link #getOutcome()} for information about this
    * property.
    * 
    * @param outcome the outcome, may be <code>null</code>
    */
   public void setOutcome(String outcome)
   {
      m_outcome = outcome;
   }

   /**
    * If this node is a category node, then it is a container.
    * 
    * @return <code>true</code> for a container node.
    */
   public boolean isContainer()
   {
      return false;
   }

   /**
    * For category nodes, tells if the node is empty. This will return
    * <code>false</code> for uninitialized containers that lazy load. For
    * initialized containers it will return <code>false</code> if the
    * container is empty. For non-containers it will return <code>true</code>,
    * but should never be called by the table model.
    * 
    * @return <code>true</code> if this node is a container and contains no
    *         elements after initialization, and <code>true</code> for
    *         non-container nodes.
    */
   public boolean isContainerEmpty()
   {
      return true;
   }

   /**
    * A container node will have children.
    * 
    * @return the children, it may be <code>null</code> or empty collection 
    *    for non-containers or a container does not have any children.
    */
   @SuppressWarnings("unchecked")
   public List<? extends PSNodeBase> getChildren()
   {
      return Collections.EMPTY_LIST;
   }

   /**
    * An enabled node is rendered normally. Disabled nodes are rendered with
    * grayed text and no link.
    * 
    * @return <code>true</code> if the node is enabled.
    */
   public boolean getEnabled()
   {
      return true;
   }

   /**
    * The node's title is used as the link or displayed text in the rendered
    * tree. 
    * 
    * @return the title, never <code>null</code> or empty.
    */
   public String getTitle()
   {
      return m_title;
   }

   /**
    * Returns the label as specified by
    * {@link #PSNodeBase(String, String, String)}.
    * If no label was specified returns the title.
    * @return the label or title if the label is not specified.
    * No more than {@link #LABEL_SIZE} characters,
    * never <code>null</code> or empty.
    */
   public String getLabel()
   {
      final String s = StringUtils.isBlank(m_label) ? m_title : m_label;
      return StringUtils.abbreviate(s, LABEL_SIZE);
   }
   
   /**
    * @return the name of the css class to use when rendering this node's
    * link in the navigation tree.
    */
   public String getNavLinkClass()
   {
      return "datadisplay";
   }
   
   /**
    * @param title the title to set, never <code>null</code> or empty
    */
   public void setTitle(String title)
   {
      if (StringUtils.isBlank(title))
      {
         throw new IllegalArgumentException(TITLE_NOT_NULL_OR_EMPTY);
      }
      m_title = title;
   }

   /**
    * The row key is a unique identifier for each node in the tree.
    * 
    * @return the key, never <code>null</code>
    */
   public Object getKey()
   {
      return m_key;
   }

   /**
    * The model is the iterator over the node tree. This is setup by the model
    * on creation and propagated to children by the category nodes.
    * 
    * @return the model, never <code>null</code> for correctly created nodes
    *         once they are in a managed category node.
    */
   public PSTreeModel getModel()
   {
      return m_model;
   }

   /**
    * @param model the model to set, set to <code>null</code> to detach from
    *            the model.
    */
   public void setModel(PSTreeModel model)
   {
      m_model = model;
   }

   /**
    * @return the wrappedData
    */
   public Object getWrappedData()
   {
      return m_wrappedData;
   }

   /**
    * @param wrappedData the wrappedData to set
    */
   public void setWrappedData(Object wrappedData)
   {
      m_wrappedData = wrappedData;
   }

   /**
    * Every attached node has a parent. The exception is the root category node
    * which has no parent. Nodes can be detached, and will have no parent.
    * 
    * @return the parent, may be <code>null</code>.
    */
   public PSNodeBase getParent()
   {
      return m_parent;
   }

   /**
    * @param parent the parent to set, may be <code>null</code>.
    */
   public void setParent(PSNodeBase parent)
   {
      m_parent = parent;
   }

   /**
    * Remove this node from it's parent. In addition, remove this node from the
    * tree model.
    */
   public void remove()
   {
      if (m_parent != null)
      {
         m_parent.remove(this);
      }
      m_model.removeNode(this);
      this.setModel(null);
   }

   /**
    * Remove the given node from this parent. The node will already be removed
    * from the model.
    * 
    * @param node the node to remove, never <code>null</code>.
    */
   public void remove(PSNodeBase node)
   {
      throw new UnsupportedOperationException(
            "Remove not supported on this node type");
   }

   /**
    * Is this node selected in the tree.
    * 
    * @return <code>true</code> if this node is selected.
    */
   public boolean getSelected()
   {
      List<String> focus = getModel().getFocusRowKey();
      String keystr;
      if (focus != null && focus.size() > 0)
      {
         keystr = focus.get(0);
      }
      else
      {
         return false;
      }
      return m_key.equals(keystr);
   }

   /**
    * @return <code>true</code> if this node represents the currently selected
    * row in the UI. The selected state reflects which row is "active".
    */
   public boolean getSelectedRow()
   {
      return m_selectedRow;
   }

   /**
    * @param selectedRow the selectedRow to set
    */
   public void setSelectedRow(boolean selectedRow)
   {
      m_selectedRow = selectedRow;
   }

   @Override
   public String toString()
   {
      StringBuilder b = new StringBuilder();
      b.append(getClass().getSimpleName());
      b.append(" title=");
      b.append(m_title);
      b.append(" outcome=");
      b.append(m_outcome);
      b.append(" key=");
      b.append(getKey());
      return b.toString();
   }

   /**
    * Version of toString used to pretty print.
    * 
    * @param indendation count of characters to indent the output
    * @return the output
    */
   public String toString(int indendation)
   {
      String str = toString();
      StringBuilder b = new StringBuilder(str.length() + indendation);
      for (int i = 0; i < indendation; i++)
      {
         b.append(' ');
      }
      b.append(str);
      return b.toString();
   }

   public int getRowCount()
   {
      throw new UnsupportedOperationException(
            "getRowCount not implemented for this node type");
   }

   public Object getRowData()
   {
      throw new UnsupportedOperationException(
            "getRowData not implemented for this node type");
   }

   public Object getRowData(@SuppressWarnings("unused") int arg0)
   {
      throw new UnsupportedOperationException(
            "getRowData(int) not implemented for this node type");
   }

   public int getRowIndex()
   {
      throw new UnsupportedOperationException(
            "getRowIndex not implemented for this node type");
   }

   public Object getRowKey()
   {
      throw new UnsupportedOperationException(
            "getRowKey not implemented for this node type");
   }

   public boolean isRowAvailable()
   {
      throw new UnsupportedOperationException(
            "isRowAvailable not implemented for this node type");
   }

   public boolean isRowAvailable(@SuppressWarnings("unused") int arg0)
   {
      throw new UnsupportedOperationException(
            "isRowAvailable not implemented for this node type");
   }

   public void setRowIndex(@SuppressWarnings("unused") int arg0)
   {
      throw new UnsupportedOperationException(
            "setRowIndex not implemented for this node type");
   }

   public void setRowKey(@SuppressWarnings("unused") Object arg0)
   {
      throw new UnsupportedOperationException(
            "setRowKey not implemented for this node type");
   }

   /**
    * Sets the key, but be very careful that the node isn't in a tree yet.
    * @param key the key to set
    */
   public void setKey(Object key)
   {
      m_key = key;
   }

   /**
    * Get the help file name for the current node.
    * Note, the derived class must implement {@link #getHelpTopic()}
    * 
    * @return  the help file name, never <code>null</code> or empty.
    */
   public String getHelpFile()
   {
      return PSHelpTopicMapping.getFileName( getHelpTopic() );
   }

   /**
    * Get the number of rows per page for paginated table from a given
    * total number of rows.
    * 
    * @param totalRows the total number of rows of the table.
    * 
    * @return the number of rows per page.
    */
   public static int getPageRows(int totalRows)
   {
      return Math.max(totalRows/MAX_RANGE_PAGES, MIN_ROW_PER_PAGE);
   }
   
   /**
    * The outcome for navigating to a warning message screen when user 
    * picked an action that will act on a selected item, but the user hasn't 
    * selected anything (from a list) yet. 
    */
   public final static String NONE_SELECT_WARNING = "no-selection-warning";
   
   /**
    * The minimum number of rows per paginated table.
    */
   private final static int MIN_ROW_PER_PAGE = 25;
   
   /**
    * The maximum number of rows in the drop down list of the paginated table.
    */
   private final static int MAX_RANGE_PAGES = 30;

   /**
    * Get the help topic of the node. This must be implemented by the derived 
    * node.
    * @return the help topic, never <code>null</code> or empty.
    */
   protected String getHelpTopic()
   {
      throw new UnsupportedOperationException(
         "getHelpTopic not implemented for this node type");      
   }
   
   /**
    * Error message for the exception when title is null or empty.
    */
   private static final String TITLE_NOT_NULL_OR_EMPTY =
         "Title may not be null or empty";
}
