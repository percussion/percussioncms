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
package com.percussion.cx.objectstore;

import com.percussion.cms.objectstore.PSFolderPermissions;
import com.percussion.cms.objectstore.PSObjectPermissions;
import com.percussion.cx.IPSConstants;
import com.percussion.cx.PSNavigationTree;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSEntrySet;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The class that is used to represent menu actions as defined by
 * 'sys_Node.dtd'.
 */
@SuppressWarnings("unchecked")
public class PSNode implements IPSComponent, Cloneable,
   PSNavigationTree.IPSTreeNodeAssociation
{
   //see PSNavigationTree.IPSTreeNodeAssociation
   public PSNavigationTree.PSTreeNode getAssociatedTreeNode()
   {
      return m_parentNode;
   }

   //see PSNavigationTree.IPSTreeNodeAssociation
   public void setAssociatedTreeNode(PSNavigationTree.PSTreeNode node)
   {
      m_parentNode = node;
   }

   /**
    * Constructs the node with supplied values.
    *
    * @param name name of the action, may not be <code>null</code> or empty.
    * @param label label to show as a menu/menu item, may not be <code>null
    * </code> or empty.
    * @param type type of the action, must be one of the TYPE_XXX values.
    * @param childrenURL relative url to the applet to execute for loading the
    * children, may be <code>null</code> or empty.
    * @param iconKey the icon key for the node, may be <code>null</code> or empty
    * in which case the type becomes the icon key.
    * @param expand supply <code>true</code> to show this node as expanded,
    * otherwise <code>false</code>
    *
    * @param permissions access mask for determining the level of access for the
    * user accessing this component. This should be used if this object
    * represents a node of type "folder". For other object types, specify a
    * value of "-1". For objects of type "folder" this must be a valid access
    * level (should be non-negative).
    *
    * @throws IllegalArgumentException if <code>name</code> or
    * <code>label</code> or <code>type</code> or <code>permissions</code> is
    * invalid
    */
   public PSNode(
      String name,
      String label,
      String type,
      String childrenURL,
      String iconKey,
      boolean expand,
      int permissions)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty.");

      if (label == null || label.trim().length() == 0)
         throw new IllegalArgumentException("label may not be null or empty.");

      if (!ms_nodeTypes.contains(type))
         throw new IllegalArgumentException("type must be one of the TYPE_xxx values of this class");

      m_name = name;
      m_label = label;
      m_type = type;
      if (iconKey == null || iconKey.length() < 1)
         m_iconKey = type;
      else
         m_iconKey = iconKey;

      m_childrenURL = (childrenURL == null) ? "" : childrenURL;
      m_isExpand = expand;

      if (isAnyFolderType())
      {
         if (permissions < 0)
            throw new IllegalArgumentException(
               "Invalid permissions for folder : " + name);
         m_permissions = new PSFolderPermissions(permissions);
      }
   }

   /**
    * Constructs the node object from the supplied element. See {@link
    * #toXml(Document) } for the expected form of xml.
    *
    * @param element the element to load from, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if element is <code>null</code>
    * @throws PSUnknownNodeTypeException if element is not of expected format.
    */
   public PSNode(Element element) throws PSUnknownNodeTypeException
   {
      if (element == null)
         throw new IllegalArgumentException("element may not be null.");

      fromXml(element);
   }

   // implements interface method, see toXml(Document) for the expected format
   //of the xml element.
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null.");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE,
            args);
      }

      m_name = sourceNode.getAttribute(NAME_ATTR);
      if (m_name == null || m_name.length() < 1)
         m_name = "name_missing";
      m_label = sourceNode.getAttribute(LABEL_ATTR);
      if (m_label == null || m_label.length() < 1)
         m_label = m_name;
      m_type =
         PSComponentUtils.getEnumeratedAttribute(
            sourceNode,
            TYPE_ATTR,
            ms_nodeTypes);
      String hint = sourceNode.getAttribute(HELP_TYPE_HINT_ATTR);
      if(hint != null && hint.length() > 0)
      { 
         if(!ms_helpTypeHints.contains(hint))
         {
            Object[] args = { XML_NODE_NAME, HELP_TYPE_HINT_ATTR, "null" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR,
               args);
         }
         m_helpTypeHint = hint;
      }
      
      String url = sourceNode.getAttribute(CHILD_URL_ATTR);
      m_childrenURL = (url == null) ? "" : url;

      if (isAnyFolderType())
      {
         String permissions = sourceNode.getAttribute(PERMISSIONS_ATTR);
         if ((permissions == null) || (permissions.trim().length() < 1))
         {
            Object[] args = { XML_NODE_NAME, PERMISSIONS_ATTR, "null" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR,
               args);
         }

         try
         {
            m_permissions =
               new PSFolderPermissions(Integer.parseInt(permissions.trim()));
         }
         catch (NumberFormatException e)
         {
            String[] args = { XML_NODE_NAME, PERMISSIONS_ATTR, permissions };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR,
               args);
         }
      }

      String iconKey = sourceNode.getAttribute(ICONKEY_ATTR);
      m_iconKey = (iconKey == null || iconKey.length() < 1) ? m_type : iconKey;

      String expand = sourceNode.getAttribute(EXPANDED_ATTR);
      m_isExpand = PSComponentUtils.XML_BOOLEAN_TRUE.equalsIgnoreCase(expand);

      Element propsEl =
         PSComponentUtils.getChildElement(
            sourceNode,
            PSProperties.XML_NODE_NAME,
            false);
      m_props = null;

      if (propsEl != null)
         m_props = new PSProperties(propsEl);

      //Get TableMeta
      m_tableMeta = null;
      Element tableMetaEl =
         PSComponentUtils.getChildElement(sourceNode, TABLEMETA_NODE, false);
      if (tableMetaEl != null && tableMetaEl.hasChildNodes())
         m_tableMeta = new TableMeta(tableMetaEl);

      //Get RowData
      m_rowData = null;
      Element rowDataEl =
         PSComponentUtils.getChildElement(sourceNode, ROWDATA_NODE, false);
      if (rowDataEl != null)
         m_rowData = new RowData(rowDataEl);

      m_children = null;
      Iterator childNodes =
         PSComponentUtils.getChildElements(sourceNode, XML_NODE_NAME);
      if (childNodes.hasNext())
      {
         m_children = new ArrayList();
         while (childNodes.hasNext())
            m_children.add(new PSNode((Element)childNodes.next()));
      }
   }

   /**
    * Replaces the child matching the supplied child's name with the supplied
    * child.
    *
    * @param newChild the new child to replace, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if newChild is <code>null</code>
    * @throws IllegalStateException if matching child did not find or does not
    * loaded with children
    */
   public void replaceChild(PSNode newChild)
   {
      if (newChild == null)
         throw new IllegalArgumentException("newChild may not be null.");

      if (m_children == null)
         throw new IllegalStateException("children are not loaded to replace");

      Iterator children = m_children.iterator();
      int i = 0;
      int childIndex = -1;
      while (children.hasNext())
      {
         PSNode child = (PSNode)children.next();
         if (child.getName().equals(newChild.getName()))
            childIndex = i;
         i++;
      }

      if (childIndex == -1)
         throw new IllegalArgumentException("children are not found to replace");

      m_children.set(childIndex, newChild);

   }

   /**
    * Adds the supplied child to its children list.
    *
    * @param child the child to add, may not be <code>null</code>
    */
   public void addChild(PSNode child)
   {
      if (child == null)
         throw new IllegalArgumentException("child may not be null.");

      if (m_children == null)
         m_children = new ArrayList();

      m_children.add(child);
   }

   /**
    * Implements the IPSComponent interface method to produce XML representation
    * of this object. See the interface for description of the method and
    * parameters.
    * <p>
    * The xml format is:
    * <pre><code>
    * &lt;!ELEMENT Node (Props?, TableMeta?, RowData?, Node*)>
    * &lt;!ATTLIST Node
    *    name CDATA #REQUIRED
    *    type (ROOT | SYSTEMCATEGORY | SYSTEMFOLDER | SYSTEMVIEW | CATEGORY |
    *          FOLDER | VIEW | NEWSEARCH | SAVEDSEARCH | SLOT | SLOTITEM | ITEM)
    *          "ROOT"
    *    helptypehint (SEARCHES) "" 
    *    label CDATA #REQUIRED
    *    expanded (false | true) "false"
    *    childrenurl CDATA #IMPLIED
    *    permissions CDATA #IMPLIED
    * >
    * &lt;!ELEMENT TableMeta (Column+)>
    * &lt;!ELEMENT Column (EMPTY)>
    * &lt;!ATTLIST Column
    *    name CDATA #REQUIRED
    *    type (String | Number | Date | Binary) "String"
    * >
    * &lt;!Element RowData (ColumnData+)>
    * &lt;!ELEMENT ColumnData (Value)>
    * &lt;!ATTLIST ColumnData
    *    name CDATA #REQUIRED>
    * &lt;!ELEMENT Value (ANY)>
    * </code></pre>
    * @param doc the Xml document, must not be <code>null</code>.
    *
    * @return the action element, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null.");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(NAME_ATTR, m_name);
      root.setAttribute(LABEL_ATTR, m_label);
      root.setAttribute(TYPE_ATTR, m_type);
      if(m_helpTypeHint != null)
         root.setAttribute(HELP_TYPE_HINT_ATTR, m_helpTypeHint);
      root.setAttribute(ICONKEY_ATTR, m_iconKey);
      root.setAttribute(CHILD_URL_ATTR, m_childrenURL);
      root.setAttribute(
         EXPANDED_ATTR,
         m_isExpand
            ? PSComponentUtils.XML_BOOLEAN_TRUE
            : PSComponentUtils.XML_BOOLEAN_FALSE);

      if (isAnyFolderType())
      {
         root.setAttribute(
            PERMISSIONS_ATTR,
            "" + m_permissions.getPermissions());
      }

      if (m_props != null)
      {
         Element props = m_props.toXml(doc);
         if (props != null)
            root.appendChild(props);
      }

      if (m_tableMeta != null)
         root.appendChild(m_tableMeta.toXml(doc));

      if (m_rowData != null)
         root.appendChild(m_rowData.toXml(doc));

      if (m_children != null)
      {
         Iterator children = m_children.iterator();
         while (children.hasNext())
            root.appendChild(((PSNode)children.next()).toXml(doc));
      }

      return root;
   }

   /**
    * Finds whether this node should display as expanded or not.
    *
    * @return <code>true</code> if it should be expanded, otherwise <code>
    * false</code>
    */
   public boolean shouldExpand()
   {
      return m_isExpand;
   }

   /**
    * Sets the expansion state of this node.
    *
    * @param isExpand supply <code>true</code> to expand, <code>false</code> to
    * show in collapsed state.
    */
   public void setExpand(boolean isExpand)
   {
      m_isExpand = isExpand;
   }

   /**
    * Sets the show/hide state of this node.
    * This flag is used to hide a node from Content Explorer temporarily. For
    * example, we do not want to show the New Search node until search is
    * performed at least once.<code>true</code> indicates to hide, <code>false</code>
    * indicates to show. Defaults to <code> false</code>.
    *
    * @param hide supply <code>true</code> to hide, <code>false</code> to
    * show in Content Explorer
    */
   public void setHidden(boolean hide)
   {
      if (hide)
      {
         setProperty(
            IPSConstants.PROPERTY_HIDDEN,
            IPSConstants.PROPERTY_TRUE);
      }
      else
      {
         setProperty(
            IPSConstants.PROPERTY_HIDDEN,
            IPSConstants.PROPERTY_FALSE);
      }
   }

   /**
    * Gets the tabular display format of this node's children. The display
    * format provides each column definition in the table with column name and
    * supported data type for that column values. The allowed data types are
    * DATA_TYPE_xxx values.
    *
    * @return the format, list of <code>Map.Entry</code> objects with key as
    * column name and value is data type. Both are <code>String</code> objects.
    * May be <code>null</code> if it is not supported. Never empty.
    */
   public Iterator getChildrenDisplayFormat()
   {
      Iterator columnDefs = null;

      if (m_tableMeta != null)
         columnDefs = m_tableMeta.getColumnDefs();

      return columnDefs;
   }

   /**
    * Sets/Replaces the display format of the node to display its children in
    * the table as defined by the column definitions.
    * 
    * @param columnDefs An iterator over one or more column defitions defined by
    * <code>Map.Entry</code> objects.  See {@link #getChildrenDisplayFormat()}
    * for more info.  May be <code>null</code> to clear the display format, may
    * not be empty.
    */
   public void setChildrenDisplayFormat(Iterator columnDefs)
   {
      if (columnDefs != null)
      {
         if (!columnDefs.hasNext())
            throw new IllegalArgumentException("columnDefs may not be empty");

         m_tableMeta = new TableMeta(columnDefs);
      }
      else
         m_tableMeta = null;
   }

   /**
    * Sets/Replaces the display format of the node to display its children in
    * the table as defined by the supplied element.
    *
    * @param tableMeta the element defining the format, may not be <code>null
    * </code> and must conform to the format of <code>TABLEMETA</code>
    * element as defined in {@link #toXml(Document)}.
    *
    * @throws PSUnknownNodeTypeException if the element is not of expected
    * format.
    */
   public void setChildrenDisplayFormat(Element tableMeta)
      throws PSUnknownNodeTypeException
   {
      if (tableMeta == null)
         throw new IllegalArgumentException("tableMeta may not be null.");

      m_tableMeta = new TableMeta(tableMeta);
   }

   /**
    * Clears the current display format of the children.
    */
   public void clearChildrenDisplayFormat()
   {
      m_tableMeta = null;
   }

   /**
    * Gets the data of this node to display in tabular format according to its
    * parent's display format for children. See {@link
    * #getChildrenDisplayFormat() } for more information.
    *
    * @return the data, each entry represents data for a column with key as
    * column name (<code>String</code>) and value is value for that column
    * (<code>Object</code>). The value object depends on the data type of the
    * column. May be <code>null</code> if it is not supported. Any additions or
    * deletions to returned map does not effect this node's row data in any way.
    */
   public Map getRowData()
   {
      Map rowData = null;

      if (m_rowData != null)
      {
         Iterator columns = m_rowData.getRowData();
         rowData = new HashMap();
         while (columns.hasNext())
         {
            Map.Entry entry = (Map.Entry)columns.next();
            rowData.put(entry.getKey(), entry.getValue());
         }
      }

      return rowData;
   }

   /**
    * Sets the row data of this node. See {@link #getRowData()} for more info.
    *
    * @param rowData a map of row data whose key is column name as <code>
    * String</code> and value is column value as an <code>Object</code>. May not
    * be <code>null</code> or empty.
    */
   public void setRowData(Map rowData)
   {
      if (rowData == null || rowData.isEmpty())
         throw new IllegalArgumentException("rowData may not be null or empty.");

      m_rowData = new RowData(rowData);
   }

   /**
    * Gets properties associated with this node.
    *
    * @return the properties, may be <code>null</code>
    */
   public PSProperties getProperties()
   {
      return m_props;
   }

   /**
    * Gets children nodes of this node.
    *
    * @return the iterator over zero or more <code>PSNode</code> objects, may be
    * <code>null</code> if this node is not set with children.
    */
   public Iterator getChildren()
   {
      if (m_children != null)
         return m_children.iterator();
      else
         return null;
   }
   
   /**
    * Returns the number of children in the node.
    * @return Returns the number of children in the node, or <code>-1</code> if there
    * is no child list.
    */
   public int getChildCount()
   {
      if (m_children != null)
      {
         return m_children.size();
      }
      else
      {
         return -1;
      }
   }

   /**
    * Nodes of type 'ROOT','SystemCategory' and 'Category' do not support
    * dynamic children unless it has a dynamic url. All other nodes support/can
    * have dynamic children.
    *
    * @return <code>true</code> if it can load children dynamically, otherwise
    * <code>false</code>
    */
   public boolean hasDynamicChildren()
   {
      boolean dynamic = true;

      if ((isOfType(PSNode.TYPE_ROOT)
         || isOfType(PSNode.TYPE_SYS_CATEGORY)
         || isOfType(PSNode.TYPE_CATEGORY))
         && getChildrenURL().length() == 0)
      {
         dynamic = false;
      }

      return dynamic;
   }

   /**
    * All nodes except nodes of type <code>TYPE_ITEM</code> are container nodes.
    * Nodes that are containers are only showed in the navigational tree.
    *
    * @return <code>true</code> if this node represents a container node,
    * otherwise <code>false</code>
    */
   public boolean isContainer()
   {
      return !(getType().equals(TYPE_ITEM) || getType().equals(TYPE_FOLDER_REF));
   }

   /**
    * Get the show/hide state of this node.
    *
    * @return <code>true</code> if the node is marked to be hidden in Content
    * Explorer, <code>false</code> otherwise.
    * @see #setHidden(boolean)
    */
   public boolean isHidden()
   {
      String hidden = m_props.getProperty(IPSConstants.PROPERTY_HIDDEN, "");
      return IPSConstants.PROPERTY_TRUE.equalsIgnoreCase(hidden);
   }

   /**
    * Sets children on this node. This replaces any previous children. The
    * supplied iterator is looped-through in this method, so <code>hasNext()
    * </code> invocation will return <code>false</code> after this method
    * execution.
    *
    * @param children the list of <code>PSNode</code> objects, may be <code>
    * null</code> to clear the children.
    *
    * @throws IllegalArgumentException if children is invalid.
    */
   public void setChildren(Iterator children)
   {
      if (children == null)
         m_children = null;
      else
      {
         m_children = new ArrayList();
         while (children.hasNext())
         {
            Object obj = children.next();
            if (obj instanceof PSNode)
            {
               m_children.add(obj);
            }
            else
               throw new IllegalArgumentException("Elements of children must be instances of PSNode");
         }
      }
   }
   
   /**
    * Removes the children of this node if it has any.
    */
   public void removeChildren()
   {
      if (m_children != null)
         m_children = null;
   }

   /**
    * Gets sort order of this node.
    *
    * @return the sort order.
    */
   public int getSortOrder()
   {
      Integer sortOrder = (Integer)ms_nodeOrder.get(getType());
      if (sortOrder == null)
         return 0;
      else
         return sortOrder.intValue();
   }

   /**
    * Gets the internal name of this node.
    *
    * @return the label, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Gets the display label of this node.
    *
    * @return the label, never <code>null</code> or empty.
    */
   public String getLabel()
   {
      return m_label;
   }

   /**
    * Gets the type of this node.
    *
    * @return the type, never <code>null</code> or empty, will be one of the
    * TYPE_xxx values.
    */
   public String getType()
   {
      return m_type;
   }
   
   /**
    * Gets the help type hint for this node.
    * 
    * @return the help type hint. May be <code>null</code> or empty.
    */
   public String getHelpTypeHint()
   {
      return m_helpTypeHint;
   }   

   /**
    * Gets the icon key for this node.
    *
    * @return the icon key, never <code>null</code> or empty, will be one of the
    * TYPE_xxx values if the icon key is not available.
    */
   public String getIconKey()
   {
      return (m_iconKey == null || m_iconKey.length() < 1) ? m_type : m_iconKey;
   }

   /**
    * Get the system property <code>PROPERTY_SEARCHID</code>. This property is
    * not be applicable to every type of node in which case an empty string is
    * returned. Folder, View, Search Result nodes have search attached
    * to them.
    * @return search id which is one of the optional property of the
    * node. Never <code>null</code>, may be <code>empty</code>.
    */
   public String getSearchId()
   {
      return getProp(IPSConstants.PROPERTY_SEARCHID);
   }
   
   /**
    * Set the system property <code>PROPERTY_SEARCHID</code> for the given
    * node.
    * @param searchId which is one of the optional property of the
    * node. Never <code>null</code>, may be <code>empty</code>.
    */
   public void setSearchId(String searchId)
   {
      if (searchId == null)
      {
         throw new IllegalArgumentException("searchId must never be null");
      }
      setProperty(IPSConstants.PROPERTY_SEARCHID, searchId);
   }   

   /**
    * Get the system property NODE_PROPERTY_DISPLAYFORMATID. This property is
    * not be applicable to every type of node in which case an empty string is
    * returned. Folder, View, Search Result nodes have display format attached
    * to them.
    * @return display format id which is one of the optional property of the
    * node. Never <code>null</code>, may be <code>empty</code>.
    */
   public String getDisplayFormatId()
   {
      return getProp(IPSConstants.PROPERTY_DISPLAYFORMATID);
   }

   /**
    * Set the system property NODE_PROPERTY_DISPLAYFORMATID. This property is
    * not be applicable to every type of node in which case an empty string is
    * returned. Folder, View, Search Result nodes have display format attached
    * to them.  Has no efffect if the id being set is the same as the one 
    * currently stored in this object.  Otherwise also calls 
    * {@link #setLastSortColumns(List) setLastSortColumns(null)} and 
    * {@link #setLastSortedAsc(boolean) setLastSortedAsc(true)}.
    * 
    * @param df The display format id, may not be <code>null</code> or empty.
    */
   public void setDisplayFormatId(String df)
   {
      if (!getDisplayFormatId().equals(df))
      {
         setProperty(IPSConstants.PROPERTY_DISPLAYFORMATID, df);
         setLastSortColumns(null);
         setLastSortedAsc(true);
      }      
   }

   /**
    * Get the system property <code>PROPERTY_CONTENTID</code>. This property is
    * not be applicable to every type of node in which case an empty string is
    * returned. Folder and Item nodes have contentid property.
    * @return content id which is one of the optional property of the
    * node. Never <code>null</code>, may be <code>empty</code>.
    */
   public String getContentId()
   {
      return getProp(IPSConstants.PROPERTY_CONTENTID);
   }

   /**
    * Get the system property <code>PROPERTY_REVISION</code>. This property is
    * not be applicable to every type of node in which case an empty string is
    * returned. Folder and Item nodes have revision property, though revision
    * for Folder may not make sense.
    * @return revsion which is one of the optional property of the
    * node. Never <code>null</code>, may be <code>empty</code>.
    */
   public String getRevision()
   {
      return getProp(IPSConstants.PROPERTY_REVISION);
   }

   /**
    * Return the relationship id for this Node. This is only valid for slot item
    * nodes, if not a slot item, then this will return -1.
    *
    * @return an int of the internal relationship id specifying the relationship
    * for this node, if the node is not a slot item, returns -1.
    */
   public int getRelationshipId()
   {
      String val = getProp(IPSConstants.PROPERTY_RELATIONSHIPID);
      try
      {
         return Integer.parseInt(val);
      }
      catch (NumberFormatException ex)
      {
         return -1;
      }
   }

   /**
    * Return the variant id for this Node.
    *
    * @return The variant id, never <code>null</code>, may be empty if not set.
    */
   public String getVariantId()
   {
      return getProp(IPSConstants.PROPERTY_VARIANTID);
   }

   /**
    * Return the slot id for this Node.
    *
    * @return The id, never <code>null</code>, may be empty if not set.
    */
   public String getSlotId()
   {
      return getProp(IPSConstants.PROPERTY_SLOTID);
   }

   /**
    * Return the sort rank for this Node.
    *
    * @return The value, never <code>null</code>, may be empty if not set.
    */
   public int getSortRank()
   {
      String val = getProp(IPSConstants.PROPERTY_SORTRANK);
      try
      {
         return Integer.parseInt(val);
      }
      catch (NumberFormatException ex)
      {
         return 0;
      }
   }

   /**
    * Return the allowed content for this Node.
    *
    * @return The value, never <code>null</code>, may be empty if not set.
    */
   public String getAllowedContent()
   {
      return getProp(IPSConstants.PROPERTY_ALLOWEDCONTENT);
   }

   /**
    * Return the object type for this Node.
    *
    * @return The value, never <code>null</code>, may be empty if not set.
    */
   public String getObjectType()
   {
      return getProp(IPSConstants.PROPERTY_OBJECTTYPE);
   }

   /**
    * Return the assignment type for this Node.
    *
    * @return The value, never <code>null</code>, may be empty if not set.
    */
   public String getAssignmentType()
   {
      return getProp(IPSConstants.PROPERTY_ASSIGNMENTTYPE);
   }

   /**
    * Return the assignment type id for this Node.
    *
    * @return The value, never <code>null</code>, may be empty if not set.
    */
   public String getAssignmentTypeID()
   {
      return getProp(IPSConstants.PROPERTY_ASSIGNMENTTYPEID);
   }

   /**
    * Return the assignment type for this Node.
    *
    * @return The value, never <code>null</code>, may be empty if not set.
    */
   public String getPublishableType()
   {
      return getProp(IPSConstants.PROPERTY_PUBLISHABLETYPE);
   }

   /**
    * Return the content type for this Node.
    *
    * @return The value, never <code>null</code>, may be empty if not set.
    */
   public String getContentTypeId()
   {
      return getProp(IPSConstants.PROPERTY_CONTENTTYPEID);
   }

   /**
    * Return the workflow app id of this Node.
    *
    * @return The value, never <code>null</code>, may be empty if not set.
    */
   public String getWorkflowAppId()
   {
      return getProp(IPSConstants.PROPERTY_WORKFLOWID);
   }

   /**
    * Return the current status of this Node.
    *
    * @return The value, never <code>null</code>, may be empty if not set.
    */
   public String getCheckOutStatus()
   {
      return getProp(IPSConstants.PROPERTY_CHECKOUTSTATUS);
   }

   /**
    * Return the slot node associated with this node.
    *
    * @return The value, never <code>null</code>, may be empty if not set.
    */
   public Object getSlotNode()
   {
      return m_props.getPropertyObj(IPSConstants.PROPERTY_SLOT_NODE);
   }

   /**
    * Helper to get the property from the property set.
    *
    * @param key the property key to lookup, may not be <code>null</code> or
    * empty.
    *
    * @return the value for the specified key, never <code>null</code>, may be
    * empty if the key is not found or the properties have not been inited
    */
   public String getProp(String key)
   {
      if (key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty.");

      if (m_props == null)
         return "";

      return m_props.getProperty(key, "");
   }

   /**
    * Gets the url to load children of this node.
    *
    * @return the url, never <code>null</code>, may be empty.
    */
   public String getChildrenURL()
   {
      return m_childrenURL;
   }

   /**
    * Returns a <code>PSObjectPermissions</code> object which encapsulates an
    * access mask. This mask determines the level of access for the user
    * accessing this node. Currently this mask is relevant only for
    * "folders".
    *
    * @return the permissions set on the component encapsulated by this object,
    * never <code>null</code> if the object is of type "folder", always
    * <code>null</code> otherwise.
    */
   public PSObjectPermissions getPermissions()
   {
      return m_permissions;
   }

   /**
    * Sets the permissions for this node.
    *
    * @param permissions the permissions for this node, may not be
    *    <code>null</code> if this node represents any "folder" object.
    * @see #getPermissions()
    */
   public void setPermissions(PSObjectPermissions permissions)
   {
      if (isAnyFolderType())
      {
         if (permissions == null)
            throw new IllegalArgumentException("permissions may not be null");
      }
      
      m_permissions = permissions;
   }

   /**
    * Determine if this node is of type system folder. The current defined
    * system folder types are:
    * <ul>
    *    <li>Sites</li>
    *    <li>Folders</li>
    * </ul>
    *
    * @return <code>true</code> if it is of type system folder, 
    *    <code>false</code> otherwise.
    */
   public boolean isSystemFolderType()
   {
      return ms_systemFolderTypes.contains(getType());
   }

   /**
    * Determins if this node is of type folder, calls 
    * {@link #isFolderType(String) isFolderType(getType())}.
    */
   public boolean isFolderType()
   {
      return isFolderType(getType());
   }
   
   /**
    * Determine if the specified type is of type folder. The current defined
    * folder types are:
    * <ul>
    *    <li>Folder</li>
    *    <li>Site</li>
    *    <li>SiteSubfolder</li>
    * </ul>
    * 
    * @param type The type to check, may not be <code>null</code> or empty.
    *
    * @return <code>true</code> if it is of type folder, <code>false</code>
    *    otherwise.
    */
   public static boolean isFolderType(String type)
   {
      if (StringUtils.isBlank(type))
         throw new IllegalArgumentException("type may not be null or empty");
      
      return ms_folderTypes.contains(type);
   }   

   /**
    * Determine if this node is of any folder type. This is the ORed result of
    * {@link #isSystemFolderType()} and {@link #isFolderType()}.
    *
    * @return <code>true</code> if it is of any folder type, <code>false</code>
    *    otherwise.
    */
   public boolean isAnyFolderType()
   {
      return isSystemFolderType() || isFolderType();
   }
   
   /**
    * Is this node a site folder? Site folders are determined through the fact
    * that they are listed as <code>FOLDER_ROOT</code> in the 
    * <code>RXSITES</code> table.
    * 
    * @return <code>true</code> if this is a site folder, <code>false</code>
    *    otherwise.
    */
   public boolean isSiteFolder()
   {
      return getType().equals(TYPE_SITE);
   }
   
   /**
    * Is this node a site subfolder? All folders which have a site folder or
    * site subfolder as their parent are considered site subfolders.
    * 
    * @return <code>true</code> if this is a site subfolder, <code>false</code>
    *    otherwise.
    */
   public boolean isSiteSubfolder()
   {
      return getType().equals(TYPE_SITESUBFOLDER);
   }

   /**
    * Determine if this node is a type of search node.  This means its children
    * are loaded using some type of search or view.
    * 
    * @return <code>true</code> if it is a search type, <code>false</code>
    * otherwise.
    */
   public boolean isSearchType()
   {
      return ms_searchTypes.contains(getType());
   }

   /**
    * Determine if this node is a type of item node.  
    * 
    * @return <code>true</code> if it is an item type, <code>false</code>
    * otherwise.
    */
   public boolean isItemType()
   {
      String type = getType();

      return type.equals(TYPE_ITEM)
         || type.equals(TYPE_DTITEM)
         || type.equals(TYPE_SLOT_ITEM);
   }

   /**
    * Get a readonly collection of folder types.  See {@link #isFolderType()} 
    * for more info.
    * 
    * @return The collection, never <code>null</code> or empty.
    */
   public static Collection getFolderTypes()
   {
      return Collections.unmodifiableCollection(ms_folderTypes);
   }

   /**
    * Get a readonly collection of search types.  See {@link #isSearchType()} 
    * for more info.
    * 
    * @return The collection, never <code>null</code> or empty.
    */
   public static Collection getSearchTypes()
   {
      return Collections.unmodifiableCollection(ms_searchTypes);
   }

   /**
    * Gets the string representation of this node (label of the node).
    *
    * @return the name, never <code>null</code> or empty.
    */
   @Override
   public String toString()
   {
      return m_label;
   }

   //implements interface method.
   @Override
   public boolean equals(Object obj)
   {
      boolean equals = true;

      if (!(obj instanceof PSNode))
         equals = false;
      else
      {
         PSNode other = (PSNode)obj;
         if (!m_name.equals(other.m_name))
            equals = false;
         else if (!m_label.equals(other.m_label))
            equals = false;
         else if (!m_type.equals(other.m_type))
            equals = false;
         else if (!m_childrenURL.equals(other.m_childrenURL))
            equals = false;
         else if (!m_iconKey.equals(other.m_iconKey))
            equals = false;
         else if (m_isExpand ^ other.m_isExpand)
            equals = false;
         else if (m_dynamicPropsLoaded ^ other.m_dynamicPropsLoaded)
            equals = false;
         else if (m_props == null ^ other.m_props == null)
            equals = false;
         else if (m_props != null && !m_props.equals(other.m_props))
            equals = false;
         else if (m_children == null ^ other.m_children == null)
            equals = false;
         else if (m_children != null && !m_children.equals(other.m_children))
            equals = false;
         else if (m_tableMeta == null ^ other.m_tableMeta == null)
            equals = false;
         else if (
            m_tableMeta != null && !m_tableMeta.equals(other.m_tableMeta))
            equals = false;
         else if (m_rowData == null ^ other.m_rowData == null)
            equals = false;
         else if (m_rowData != null && !m_rowData.equals(other.m_rowData))
            equals = false;
         else if (m_truncated != other.m_truncated)
            equals = false;
      }

      return equals;
   }

   //implements interface method.
   @Override
   public int hashCode()
   {
      int code = 0;

      code =
         m_name.hashCode()
            + m_label.hashCode()
            + m_type.hashCode()
            + m_childrenURL.hashCode()
            + m_iconKey.hashCode()
            + (m_isExpand ? 1 : 0)
            + (m_dynamicPropsLoaded ? 1 : 0)
            + (m_props == null ? 0 : m_props.hashCode())
            + (m_children == null ? 0 : m_children.hashCode())
            + (m_tableMeta == null ? 0 : m_tableMeta.hashCode())
            + (m_rowData == null ? 0 : m_rowData.hashCode());

      return code;
   }

   // see IPSDataComponent
   @Override
   public Object clone()
   {
      PSNode copy = null;
      try
      {
         copy = (PSNode)super.clone();
         if (m_children != null)
         {
            copy.m_children = new ArrayList();

            Iterator i = m_children.iterator();
            while (i.hasNext())
            {
               PSNode obj = (PSNode)i.next();
               copy.m_children.add(obj.clone());
            }
         }

         if (m_props != null)
            copy.m_props = (PSProperties)m_props.clone();

         if (m_tableMeta != null)
            copy.m_tableMeta = (TableMeta)m_tableMeta.clone();

         if (m_rowData != null)
            copy.m_rowData = (RowData)m_rowData.clone();
         
         copy.setTruncated(isTruncated());
      }
      catch (CloneNotSupportedException ex)
      { /* ignore */
      }

      return copy;
   }

   /**
    * The class that defines the display format of its children in a table.
    */
   private class TableMeta implements IPSComponent, Cloneable
   {
      /**
       * Constructs this object from the supplied element. See {@link
       * #toXml(Document) } for the expected form of xml.
       *
       * @param el the element to load from, may not be <code>null</code>
       *
       * @throws IllegalArgumentException if element is <code>null</code>
       * @throws PSUnknownNodeTypeException if element is not of expected format.
       */
      public TableMeta(Element el) throws PSUnknownNodeTypeException
      {
         if (el == null)
            throw new IllegalArgumentException("el may not be null.");

         fromXml(el);
      }

      /**
       * Constructs this object from the supplied column definitions. 
       *
       * @param columnDefs An iterator over one or more <code>Map.Entry</code>
       * objects.  See {@link PSNode#setChildrenDisplayFormat(Iterator)} for 
       * more info.  May not be <code>null</code> or empty.
       */
      public TableMeta(Iterator columnDefs)
      {
         if (columnDefs == null || !columnDefs.hasNext())
            throw new IllegalArgumentException("columnDefs may not be null or empty");

         while (columnDefs.hasNext())
         {
            Object entry = columnDefs.next();
            if (!(entry instanceof Map.Entry))
               throw new IllegalArgumentException("columnDefs must contain Map.Entry objects");

            m_columnDefs.add(entry);
         }
      }

      //implements interface method
      public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
      {
         if (sourceNode == null)
            throw new IllegalArgumentException("sourceNode may not be null.");

         if (!TABLEMETA_NODE.equals(sourceNode.getNodeName()))
         {
            Object[] args = { TABLEMETA_NODE, sourceNode.getNodeName()};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE,
               args);
         }

         Iterator columns =
            PSComponentUtils.getChildElements(sourceNode, COLUMN_NODE);
         if (!columns.hasNext())
         {
            Object[] args = { TABLEMETA_NODE, COLUMN_NODE, "null" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD,
               args);
         }

         while (columns.hasNext())
         {
            Element columnDef = (Element)columns.next();
            String name =
               PSComponentUtils.getRequiredAttribute(columnDef, NAME_ATTR);
            /*
             * TODO: The following never finds the true value as it is not an
             * attribute - see toXml for details.  Not changing now due to risk
             * and not needed by production code.  Check if RowData.fromXml()
             * has the same issue.
             */
            String dataType =
               PSComponentUtils.getEnumeratedAttribute(
                  columnDef,
                  TYPE_ATTR,
                  ms_dataTypes);

            PSEntrySet entry = new PSEntrySet(name, dataType);

            m_columnDefs.add(entry);
         }
      }

      //implements interface method, see toXml(Document) of containing class
      //for format.
      public Element toXml(Document doc)
      {
         if (doc == null)
            throw new IllegalArgumentException("doc may not be null.");

         Element root = doc.createElement(TABLEMETA_NODE);
         Iterator columns = m_columnDefs.iterator();
         while (columns.hasNext())
         {
            Map.Entry column = (Map.Entry)columns.next();
            Element colEl = doc.createElement(COLUMN_NODE);
            colEl.setAttribute(NAME_ATTR, (String)column.getKey());
            colEl.appendChild(doc.createTextNode((String)column.getValue()));
            /*
             * todo: uncomment next line to fix method. Not implementing now due
             * to poosible risk.
             */ 
            //root.appendChild(colEl);
         }

         return root;
      }

      /**
       * Gets the column definitions represented by this metadata.
       *
       * @return an iterator over one or more <code>Map.Entry</code> whose
       * key represents column name and value the datatype. The allowed
       * datatypes are DATA_TYPE_xxx values.
       */
      public Iterator getColumnDefs()
      {
         return m_columnDefs.iterator();
      }

      //implements interface method.
      @Override
      public boolean equals(Object obj)
      {
         boolean equals = false;

         if (obj instanceof TableMeta)
         {
            TableMeta other = (TableMeta)obj;
            if (m_columnDefs.equals(other.m_columnDefs))
               equals = true;
         }

         return equals;
      }

      //implements interface method.
      @Override
      public int hashCode()
      {
         return m_columnDefs.hashCode();
      }

      //implements interface method
      @Override
      public Object clone()
      {
         TableMeta copy = null;
         try
         {
            copy = (TableMeta)super.clone();
            List columnDefs = new ArrayList();
            columnDefs.addAll(m_columnDefs);
            copy.m_columnDefs = columnDefs;            
         }
         catch (CloneNotSupportedException ex)
         { /* does not happen */
         }

         return copy;
      }

      /**
       * The list of column definitions of this table meta, with column name as
       * key (<code>String</code>) and datatype as value (<code>String</code>)
       * Initialized in the ctor, and never <code>null</code> or empty.
       */
      private List m_columnDefs = new ArrayList();
   }

   /**
    * The class that defines the row data of this node to display in tabular
    * format as per its parent's table metadata.
    */
   private class RowData implements IPSComponent, Cloneable
   {
      /**
       * Constructs the row data from supplied map.
       *
       * @param columnData the data for each column (name-value), may not be
       * <code>null</code> or empty.
       *
       * @throws IllegalArgumentException if rowData is invalid.
       */
      public RowData(Map columnData)
      {
         if (columnData == null || columnData.isEmpty())
            throw new IllegalArgumentException("columnData may not be null or empty.");

         m_columnData = columnData;
      }

      /**
       * Constructs this object from the supplied element. See {@link
       * #toXml(Document) } for the expected form of xml.
       *
       * @param el the element to load from, may not be <code>null</code>
       *
       * @throws IllegalArgumentException if element is <code>null</code>
       * @throws PSUnknownNodeTypeException if element is not of expected format.
       */
      public RowData(Element el) throws PSUnknownNodeTypeException
      {
         if (el == null)
            throw new IllegalArgumentException("el may not be null.");

         fromXml(el);
      }

      //implements interface method
      public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
      {
         if (sourceNode == null)
            throw new IllegalArgumentException("sourceNode may not be null.");

         if (!ROWDATA_NODE.equals(sourceNode.getNodeName()))
         {
            Object[] args = { ROWDATA_NODE, sourceNode.getNodeName()};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE,
               args);
         }

         Iterator columns =
            PSComponentUtils.getChildElements(sourceNode, COLDATA_NODE);
         if (!columns.hasNext())
         {
            Object[] args = { ROWDATA_NODE, "null" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD,
               args);
         }

         while (columns.hasNext())
         {
            Element columnData = (Element)columns.next();
            String name =
               PSComponentUtils.getRequiredAttribute(columnData, NAME_ATTR);

            Object value;
            if (columnData.getFirstChild() instanceof Text)
            {
               value = ((Text)columnData.getFirstChild()).getData();
            }
            else
            {
               value = ""; //default to empty string
            }

            m_columnData.put(name, value);
         }
      }

      //implements interface method, see toXml(Document) of containing class
      //for format.
      public Element toXml(Document doc)
      {
         if (doc == null)
            throw new IllegalArgumentException("doc may not be null.");

         Element root = doc.createElement(ROWDATA_NODE);
         Iterator columns = m_columnData.entrySet().iterator();
         while (columns.hasNext())
         {
            Map.Entry column = (Map.Entry)columns.next();
            Element colEl = doc.createElement(COLDATA_NODE);
            colEl.setAttribute(NAME_ATTR, (String)column.getKey());
            colEl.appendChild(doc.createTextNode(column.getValue().toString()));
            /*
             * todo: uncomment next line to fix method. Not implementing now due
             * to poosible risk.
             */ 
            //root.appendChild(colEl);
         }
         return root;
      }

      /**
       * Gets the row data.
       *
       * @return an iterator over one or more <code>Map.Entry</code> whose
       * key represents column name and value represents data.
       */
      public Iterator getRowData()
      {
         return m_columnData.entrySet().iterator();
      }

      //implements interface method.
      @Override
      public boolean equals(Object obj)
      {
         boolean equals = false;

         if (obj instanceof RowData)
         {
            RowData other = (RowData)obj;
            if (m_columnData.equals(other.m_columnData))
               equals = true;
         }

         return equals;
      }

      //implements interface method.
      @Override
      public int hashCode()
      {
         return m_columnData.hashCode();
      }

      //implements interface method
      @Override
      public Object clone()
      {
         RowData copy = null;
         try
         {
            copy = (RowData)super.clone();
            copy.m_columnData = new HashMap(m_columnData);
         }
         catch (CloneNotSupportedException ex)
         { /* does not happen */
         }

         return copy;
      }

      /**
       * The list of column data entries of this row data, with column name as
       * key (<code>String</code>) and data as value (<code>Object</code>)
       * Initialized in the ctor, and never <code>null</code> or empty.
       */
      private Map m_columnData = new HashMap();
   }

   /**
    * Replace the existing properties (if any) with the supplied one. This is
    * useful when creating new new object not using the fromXml() method.
    * @param props may be <code>null</code>.
    * @return new properties object, is <code>null</code> if the supplied
    * parameter is <code>null</code>.
    */
   public PSProperties setProperties(PSProperties props)
   {
      m_props = props;
      return m_props;
   }

   public void setProperty(String key, String value)
   {
      if (key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty.");

      if (m_props == null)
         m_props = new PSProperties();

      m_props.setProperty(key, value);
   }

   public void setPropertyObj(String key, Object obj)
   {
      if (m_props == null)
         m_props = new PSProperties();

      m_props.setPropertyObj(key, obj);
   }

   /**
    * Change the label of the node.
    * @param newLabel new label for the node to set, may not be <code>null</code>
    * or <code>empty</code>
    */
   public void setLabel(String newLabel)
   {
      if (newLabel == null || newLabel.trim().length() == 0)
         throw new IllegalArgumentException("newLabel may not be null or empty");

      m_label = newLabel;
   }

   /**
    * Change the type of this node.
    * @param newType new type to set to the node, may not be <code>null</code>
    * or <code>empty</code> and must be one of the valid types.
    */
   public void setType(String newType)
   {
      if (!ms_nodeTypes.contains(newType))
         throw new IllegalArgumentException("type must be one of the TYPE_xxx values of this class");

      m_type = newType;
   }
   
   /**
    * Sets the help type hint for this node.
    * @param hint help type hint to set to the node, may be 
    * <code>null</code> or must be one of the valid help type hints.
    */
   public void setHelpTypeHint(String hint)
   {
     if (hint != null && !ms_helpTypeHints.contains(hint))
        throw new IllegalArgumentException(
           "type must be one of the HELP_TYPE_HINT_xxx values of this class");
     m_helpTypeHint = hint;
   }

   /**
    * Easy method to check if this node type equals the supplied type.
    * Check is case-sensitive.
    *
    * @param type the node type,  may be <code>null</code> in which case the
    * result will be <code>false</code>.
    *
    * @return <code>true</code> if the node type equals the specified type,
    * <code>false</code> otherwise.
    */
   public boolean isOfType(String type)
   {
      return getType().equals(type);
   }
   
   /**
    * Easy method to check if this node type matches with the supplied type.
    * Check is case-sensitive.
    *
    * @param type the node type,  may be <code>null</code> in which case the
    * result will be <code>false</code>.
    *
    * @return <code>true</code> if the node type mactches with specified type,
    * <code>false</code> otherwise.
    */
   public boolean isMatchingType(String type)
   {
      // first check equivalence
      String thisType = getType();
      if (thisType.equals(type))
         return true;
      
      // now check match on folder and folder ref.
      if (isFolderType(thisType) && TYPE_FOLDER_REF.equals(type))
         return true;
      
      return thisType.equals(TYPE_FOLDER_REF) && isFolderType(type);
   }   

   /**
    * Sets whether dynamic properties are loaded.  These properties are not
    * supplied when the node is created, and are loaded lazily for performance
    * reasons and added to the node when needed.  Should be called supplying
    * <code>true</code> for the <code>loaded</code> param once this has been
    * done.
    * 
    * @param loaded <code>true</code> to indicate that the properties returned
    * by {@link #getProperties()} included dynamic properties, 
    * <code>false</code> if not.
    */
   public void setAreDynamicPropsLoaded(boolean loaded)
   {
      m_dynamicPropsLoaded = loaded;
   }

   /**
    * Determines if dynamic properties have been loaded and set on this object.
    * See {@link #setAreDynamicPropsLoaded(boolean)} for more info.
    * 
    * @return <code>true</code> if they have been loaded, <code>false</code>
    * otherwise.
    */
   public boolean areDynamicPropsLoaded()
   {
      return m_dynamicPropsLoaded;
   }

   /**
    * Sets whether this node should be marked as dirty, meaning it needs to be
    * refreshed.
    * 
    * @param isDirty <code>true</code> to indicate it is dirty, 
    * <code>false</code> otherwise.
    */
   public void setIsDirty(boolean isDirty)
   {
      m_isDirty = isDirty;
   }

   /**
    * Determines if this node is marked as dirty.  See 
    * {@link #setIsDirty(boolean)} for more info.
    * 
    * @return <code>true</code> it it is dirty, <code>false</code> otherwise.
    */
   public boolean isDirty()
   {
      return m_isDirty;
   }
   
   /**
    * Check if this node has fewer children than requested.
    * @return <code>true</code> if this node has fewer children than exist.
    */
   public boolean isTruncated()
   {
      return m_truncated;
   }
   
   /**
    * Set the state of this node, see {@link #isTruncated()} for details.
    * @param truncated the new state of truncation for this node.
    */
   public void setTruncated(boolean truncated)
   {
      m_truncated = truncated;
   }   

   /**
    * Gets the children of this node for which {@link #isDirty()} returns
    * <code>true</code>.
    * 
    * @param recurse <code>true</code> to perform this check recursively against
    * children of this node, <code>false</code> otherwise.
    * 
    * @return An iterator over zero or more child <code>PSNode</code> objects, 
    * never <code>null</code>, may be empty.
    */
   public Iterator getDirtyChildren(boolean recurse)
   {
      List dirty = new ArrayList();
      Iterator children = getChildren();
      if (children != null)
      {
         while (children.hasNext())
         {
            PSNode child = (PSNode)children.next();
            if (child.isDirty())
               dirty.add(child);
            if (recurse)
            {
               Iterator dirtyChildren = child.getDirtyChildren(recurse);
               while (dirtyChildren.hasNext())
                  dirty.add(dirtyChildren.next());
            }
         }
      }

      return dirty.iterator();
   }

   /**
    * Clears the dirty flag for children of this node for which 
    * {@link #isDirty()} returns <code>true</code>.
    * 
    * @param recurse <code>true</code> to perform this check recursively against
    * children of this node, <code>false</code> otherwise.
    */
   public void clearDirtyChildren(boolean recurse)
   {
      Iterator dirty = getDirtyChildren(recurse);
      while (dirty.hasNext())
          ((PSNode)dirty.next()).setIsDirty(false);
   }

   /**
    * Determines if there are any children of this node for which 
    * {@link #isDirty()} returns <code>true</code>.  It will check child nodes 
    * recursively only if {@link #isFolderType()} returns <code>false</code>
    * for the node whose children are being checked. 
    * 
    * @return <code>true</code> if any children are found to be dirty, 
    * <code>false</code> otherwise.
    */
   public boolean hasDirtyChildren()
   {
      return hasDirtyChildren(!isAnyFolderType());
   }

   /**
    * Determines if there are any children of this node for which 
    * {@link #isDirty()} returns <code>true</code>.
    * 
    * @param recurse <code>true</code> to perform this check recursively against
    * children of this node, <code>false</code> otherwise.
    * 
    * @return <code>true</code> if any children are found to be dirty, 
    * <code>false</code> otherwise.
    */
   private boolean hasDirtyChildren(boolean recurse)
   {
      boolean hasDirty = false;

      Iterator children = getChildren();
      if (children != null)
      {
         while (children.hasNext() && !hasDirty)
         {
            PSNode child = (PSNode)children.next();
            if (child.isDirty())
               hasDirty = true;
            else if (recurse)
               hasDirty = child.hasDirtyChildren(recurse);
         }
      }

      return hasDirty;
   }
   
   /**
    * Find the first child node with a matching content id and node type
    *  
    * @param contentId The content id of the node to find.
    * @param nodeType The type to match, not <code>null</code> or empty, one of
    * the <code>TYPE_XXX</code> values.
    * @param recurse <code>true</code> to perform this check recursively against
    * children of this node, <code>false</code> to check immediate children
    * only.
    * 
    * @return the matching node or <code>null</code> if not found.
    */
   public PSNode findChildNode(String contentId, String nodeType, 
      boolean recurse)
   {
      PSNode result = null;
      
      Iterator children = getChildren();
      if (children == null)
         return result;
      
      while (children.hasNext() && result == null)
      {
         PSNode child = (PSNode) children.next();
         if (child.getType().equals(nodeType))
         {
            String test = child.getProp(IPSHtmlParameters.SYS_CONTENTID);
            if (contentId.equals(test))
            {
               result = child;
            }
         }

         if (result != null && recurse)
         {
            result = child.findChildNode(contentId, nodeType, recurse);
         }         
      }
      
      return result;
   }   

   /**
    * Easy method to check if the node type specified by the string belongs to
    * one of the valid node types defined in this class.
    *
    * @param type the node type, may be <code>null</code> in which case the
    * result will be <code>false</code>.
    *
    * @return <code>true</code> if the specified type is one of the known node
    * types, otherwise <code>false</code>.
    */
   public static boolean isTypeValid(String type)
   {
      if (type != null)
         return ms_nodeTypes.contains(type);

      return false;
   }
   
   /**
    * Saves the most recent search result doc.
    * 
    * @param doc The doc, may be <code>null</code> to clear the results.
    */
   public void setSearchResults(Document doc)
   {
      m_searchResults = doc;
      m_searchResultCount = -1;
   }
   
   /**
    * Gets the most recent search results for this node, may be 
    * <code>null</code> if none have been set or if they have been cleared.
    * 
    * @return The results doc, may be <code>null</code>.
    */
   public Document getSearchResults()
   {
      return m_searchResults;
   }
   
   /**
    * Determines if this node contains a child node of the specified type
    * 
    * @param type The node type, expected to be one of the <code>TYPE_xxx</code>
    * values.  May not be <code>null</code> or empty.
    *   
    * @return <code>true</code> if a child node of the specified type is found,
    * <code>false</code> otherwise.
    */
   public boolean hasChildOfType(String type)
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");
      
      boolean result = false;
      
      Iterator children = m_children.iterator();
      while (children.hasNext() && result == false)
      {
         PSNode child = (PSNode)children.next();
         if (child.isOfType(type))
         {
            result = true;
         }
      }
      
      return result;
   }
   
   /**
    * Derives a result count from the search result document if there is one.
    * If there is no document, then the count is left at <code>-1</code>.
    * This means that it will always be <code>-1</code> for non search nodes.
    * @return a count of the search results, or <code>-1</code> if the node
    * is not a search node. Search nodes will have a result that is zero or
    * greater.
    */
   public int getSearchResultCount()
   {
      if (m_searchResultCount >= 0)
      {
         return m_searchResultCount;
      }
      else
      {
         Document d = getSearchResults();
         
         if (d != null)
         {
            Element el = d.getDocumentElement();
            NodeList results = el.getElementsByTagName("Result");
            m_searchResultCount = results != null ? results.getLength() : 0;
         }
         
         return m_searchResultCount;
      }
   } 
   
   /**
    * Call this to change the search result count. The only valid use of this
    * is for the folder nodes, to fix the count for display purposes after 
    * one or more items are moved.
    */
   public void fixSearchResultCount()
   {
      m_searchResultCount = getChildCount();
   }

   /**
    * A node can be quick loaded, which indicates that it needs to be entirely 
    * refreshed on next access. This is used to partially load folder 
    * information to speed applet loading.
    * @return <code>true</code> if the node is quick loaded
    */
   public boolean isQuickLoaded()
   {
      return m_quickLoaded;
   }
   
   /**
    * Set the quickload state, see {@link #isQuickLoaded()} for more info.
    * @param quickLoaded the new state value
    */
   public void setQuickLoaded(boolean quickLoaded)
   {
      m_quickLoaded = quickLoaded;
   }

   /**
    * Get the indexes of the last column used for sorting.
    * 
    * @return A list of column indexes as <code>Integer</code> objects, or 
    * <code>null</code> if no previous list is currently stored.
    */
   public List getLastSortColumns()
   {
      return m_lastSortCols;
   }
   
   /**
    * Determine if the last sort was ascending or descending.
    * 
    * @return <code>true</code> if it was ascending, <code>false</code> if 
    * descending.  Value should be ignored if {@link #getLastSortColumns()}
    * returns <code>null</code>.
    */
   public boolean getLastSortedAsc()
   {
      return m_lastSortedAsc;
   }
   
   /**
    * Set the last column indexes used for sorting.  See 
    * {@link #getLastSortColumns()} for more info.
    * 
    * @param indexes The indexes of the last sorted columns as 
    * <code>Integer</code> objects, or <code>null</code> to clear the list.  
    */
   public void setLastSortColumns(List indexes)
   {
      m_lastSortCols = indexes;
   }
   
   /**
    * Set if last sorting was ascending or descending.  See 
    * {@link #getLastSortedAsc()} for more info.
    * 
    * @param ascending <code>true</code> if ascending, <code>false</code>
    * otherwise.
    */
   public void setLastSortedAsc(boolean ascending)
   {
      m_lastSortedAsc = ascending;
   }
   
   /**
    * Get any child actions cached in this node for the specifie url.
    * 
    * @param url The url used to obtain the child actions from the server, may
    * not be <code>null</code> or empty.
    * 
    * @return The list, or <code>null</code> if not yet cached.
    */
   public List<PSMenuAction> getChildMenuActions(String url)
   {
      if (StringUtils.isBlank(url))
         throw new IllegalArgumentException("url may not be null or empty");
      
      return m_childMenuCache.get(url);
   }
   
   /**
    * Set the child actions for the specified url.  
    * See {@link #getChildMenuActions(String)}. 
    * 
    * @param url The url, may not be <code>null</code> or empty.
    * @param actions The list of actions to cache, may not be <code>null</code>.
    */
   public void setChildMenuActions(String url, List<PSMenuAction> actions)
   {
      if (StringUtils.isBlank(url))
         throw new IllegalArgumentException("url may not be null or empty");
      if (actions == null)
         throw new IllegalArgumentException("actions may not be null");
      
      m_childMenuCache.put(url, actions);
   }
   
   /**
    * Create a dummy node representing a new item with a content id of -1 and a
    * dirty flag set to <code>true</code>.  
    * 
    * @return The node, never <code>null</code>.
    */
   public static PSNode createDirtyNewItemNode()
   {
      PSNode tmpNode = new PSNode("tmp", "tmp", TYPE_ITEM, null,
         null, false, -1);
      tmpNode.setProperty(IPSConstants.PROPERTY_CONTENTID, "-1");
      tmpNode.setIsDirty(true);
      return tmpNode;
   }

   /**
    * Holds the quickloaded state, set in {@link #setQuickLoaded(boolean)}.
    */
   private boolean m_quickLoaded = false;
   
   /**
    * Name of the node, initialized in constructor and never <code>null</code>,
    * empty or modified after that.
    */
   private String m_name;

   /**
    * Type of the node, initialized in constructor and never <code>null</code>,
    * empty or modified after that, will be one of the TYPE_xxx values.
    */
   private String m_type;
   
   /**
    * An extra bit of information to help determine the type of help that
    * should be displayed. May be <code>null</code>.
    */
   private String m_helpTypeHint;

   /**
    * Display label of the node, initialized in constructor and never <code>null
    * </code>, empty or modified after that.
    */
   private String m_label;

   /**
    * The url to get children of the node, initialized in constructor and never
    * <code>null</code> or modified after that. May be empty.
    */
   private String m_childrenURL;

   /**
    * The icon key name for the node to use when rendering in the applet. This
    * is read from an optional attribute "iconKey" of the Node XML. If the
    * attribute  is absent the mandatory attribute "type" becomes the iconKey.
    * Initialized in the constructor and never <code>null</code> or <code>empty</code>
    * modified after that.
    */
   private String m_iconKey;

   /**
    * The flag to indicate that whether this node should be expanded or
    * collapsed when it is represented as tree node. <code>true</code> indicates
    * to expand, <code>false</code> indicates to collapse. Defaults to <code>
    * false</code> if not specified. Initialized in the constructor and may be
    * modified through calls to <code>setExpand(boolean)</code>.
    */
   private boolean m_isExpand;

   /**
    * The properties associated with the node, initialized in ctor and may be
    * <code>null</code> if not specified.
    */
   private PSProperties m_props = null;

   /**
    * The metadata of this node's children to display in tabular format,
    * initialized in ctor and may be <code>null</code> if not specified.
    */
   private TableMeta m_tableMeta = null;

   /**
    * The row data of this node according to its parent's table metadata to
    * display in tabular format, initialized in ctor and may be <code>null
    * </code> if not specified.
    */
   private RowData m_rowData = null;

   /**
    * The list of child nodes, initialized to <code>null</code> and gets filled
    * as it is loaded from xml or through calls to <code>setChildren(Iterator)
    * </code>. May be <code>null</code> if it is not yet loaded with children.
    * May be empty after initialization.
    */
   private List m_children = null;

   /**
    * Specifies the permissions set on the node encapsulated by this object for
    * the user accessing the item. Currently this has relevance only if the
    * encapsulated node is of type "folder".
    * Initialized in the constructor or <code>fromXml()</code> method if the
    * object type is "folder".
    * Never <code>null</code> if this node if for a folder object, otherwise
    * <code>null</code>. Never modified after initialization.
    */
   private PSObjectPermissions m_permissions = null;

   /**
    * The results of the most recent search, may be <code>null</code> if none
    * have been set or if they have been cleared.  Modified by 
    * {@link #setSearchResults(Document)}.
    */
   private Document m_searchResults = null;
   
   /**
    * The number of results in {@link #m_searchResults}. This is initialized
    * the first time {@link #getSearchResultCount()} is called, and is 
    * reset in {@link #setSearchResults(Document)}. 
    */
   private int m_searchResultCount = -1;

   /**
    * The constant to indicate 'ROOT' node.
    */
   public static final String TYPE_ROOT = "ROOT";

   /**
    * The constant to indicate 'SYSTEMCATEGORY' node.
    */
   public static final String TYPE_SYS_CATEGORY = "SystemCategory";

   /**
    * The constant to indicate 'SYSTEMFOLDER' node.
    */
   public static final String TYPE_SYS_FOLDERS = "SystemFolder";

   /**
    * The constant to indicate 'SYSTEMSITE' node.
    */
   public static final String TYPE_SYS_SITES = "SystemSite";
   
   /**
    * The type constant used for 'Site' nodes.
    */
   public static final String TYPE_SITE = "Site";
   
   /**
    * The type constant used for 'SiteSubfolder' nodes.
    */
   public static final String TYPE_SITESUBFOLDER = "SiteSubfolder";

   /**
    * The constant to indicate 'SYSTEMVIEW' node.
    */
   public static final String TYPE_SYS_VIEW = "SystemView";

   /**
    * The constant to indicate 'CATEGORY' node.
    */
   public static final String TYPE_CATEGORY = "Category";

   /**
    * The constant to indicate 'FOLDER' node.
    */
   public static final String TYPE_FOLDER = "Folder";

   /**
    * The constant to indicate that this node represents a folder but is not 
    * actually a folder. Such a node 'looks' like a folder (similar icon),
    * but has no children and has its own menu context. This node ignores
    * permissions.
    */
   public static final String TYPE_FOLDER_REF = "FolderRef";

   /**
    * The constant to indicate 'VIEW' node.
    */
   public static final String TYPE_VIEW = "View";

   /**
    * The constant to indicate 'NEWSEARCH' node.
    */
   public static final String TYPE_NEW_SRCH = "NewSearch";

   /**
    * The constant to indicate 'EMPTYSEARCH' node.
    */
   public static final String TYPE_EMPTY_SRCH = "EmptySearch";

   /**
    * The constant to indicate 'SAVEDSEARCH ' node.
    */
   public static final String TYPE_SAVE_SRCH = "SavedSearch";

   /**
    * The constant to indicate 'PARENT' node.
    */
   public static final String TYPE_PARENT = "Parent";

   /**
    * The constant to indicate 'SLOT' node.
    */
   public static final String TYPE_SLOT = "Slot";

   /**
    * The constant to indicate 'SLOTITEM' node.
    */
   public static final String TYPE_SLOT_ITEM = "SlotItem";

   /**
    * The constant to indicate 'ITEM' node.
    */
   public static final String TYPE_ITEM = "Item";

   /**
    * The constant to indicate 'DT_ITEM' node.
    */
   public static final String TYPE_DTITEM = "DTItem";

   /**
    * The constant to indicate 'CUSTOMSEARCH ' node.
    */
   public static final String TYPE_CUSTOM_SRCH = "CustomSearch";

   /**
    * The constant to indicate 'STANDARDSEARCH ' node.
    */
   public static final String TYPE_STANDARD_SRCH = "StandardSearch";
   
   /** 
    * The help type hint constant for the 'Searches' node.
    */
   public static final String HELP_TYPE_HINT_SRCH = "Searches";

   /**
    * The constant to indicate the 'String' data type of column data.
    */
   public static final String DATA_TYPE_TEXT = "Text";

   /**
    * The constant to indicate the 'String' data type of column data.
    */
   public static final String DATA_TYPE_NUMBER = "Number";

   /**
    * The constant to indicate the 'String' data type of column data.
    */
   public static final String DATA_TYPE_DATE = "Date";

   /**
    * The constant to indicate the 'String' data type of column data.
    */
   public static final String DATA_TYPE_IMAGE = "Image";

   /**
    * The list of node types.
    */
   private static List ms_nodeTypes = new ArrayList();

   /**
    * A list of node types that represent system folder objects. Initialized by
    * static initializer, never modified after that.
    */
   private static List ms_systemFolderTypes = new ArrayList();
   
   /**
    * A list of node types that represent folder objects. Initialized by
    * static initializer, never modified after that.
    */
   private static List ms_folderTypes = new ArrayList();

   /**
    * The list of node types that represent search or view objects.  Initialized 
    * by a static initializer, never modified after that.
    */
   private static List ms_searchTypes = new ArrayList();

   /**
    * The list of data types for a column data. 
    */
   static List ms_dataTypes = new ArrayList();
   
   /**
    * The list of allowed help type hints. Initialized 
    * by a static initializer, never modified after that.
    */
   private static List ms_helpTypeHints = new ArrayList();

   /**
    * Determines if dynamic properties have been set on this object.  Initially
    * <code>false</code>, modified by calls to 
    * {@link #setAreDynamicPropsLoaded(boolean)}.  
    */
   private boolean m_dynamicPropsLoaded = false;

   /**
    * Determines if this node has been marked as dirty.  Initially 
    * <code>false</code>, modified by calls to {@link #setIsDirty(boolean)}.
    */
   private boolean m_isDirty = false;
   
   /**
    * Indicates if this node has fewer items than requested in a search. Only
    * modified through the accessors on this class.
    */
   private boolean m_truncated = false;
   
   /**
    * The list of indexs of the last columns used for sorting.  Initially 
    * <code>null</code> or if not specified.  Previous value is reset to 
    * <code>null</code> when {@link #setDisplayFormatId(String)} is called with 
    * a different display format id than is was previsously set.
    */
   private List m_lastSortCols = null;
   
   /**
    * Determines if the last sort direction was ascending.  <code>true</code> if
    * it was, <code>false</code> if was descending.  Initially 
    * <code>true</code>, should be ignored if the value of 
    * {@link #m_lastSortCols} is <code>null</code>. 
    */
   private boolean m_lastSortedAsc = true;

   /**
    * Storage location for the tree node that owns this node as its user 
    * object. Will be <code>null</code> if this node is not owned by a tree
    * node, otherwise it will be valid. Only manipulated by the 
    * <code>[get,set]AssociatedTreeNode</code> methods.
    */
   private PSNavigationTree.PSTreeNode m_parentNode;
   
   /**
    * Cache of dynamic child menu actions where the key is the url and the value
    * is the resulting action list, never <code>null</code>.
    */
   private Map<String, List<PSMenuAction>> m_childMenuCache = 
      new HashMap<String, List<PSMenuAction>>();   

   /**
    * The sort order for the nodes, currently contains entries for types <code>
    * TYPE_FOLDER</code> and <code>TYPE_ITEM</code> only. <code>TYPE_FOLDER
    * </code> comes before <code>TYPE_ITEM</code> in ascending order. All other
    * node types have equal priority in ordering.
    */
   private static Map ms_nodeOrder = new HashMap();
   static 
   {
      ms_nodeTypes.add(TYPE_ROOT);
      ms_nodeTypes.add(TYPE_SYS_CATEGORY);
      ms_nodeTypes.add(TYPE_SYS_FOLDERS);
      ms_nodeTypes.add(TYPE_SYS_SITES);
      ms_nodeTypes.add(TYPE_SITE);
      ms_nodeTypes.add(TYPE_SITESUBFOLDER);
      ms_nodeTypes.add(TYPE_SYS_VIEW);
      ms_nodeTypes.add(TYPE_CATEGORY);
      ms_nodeTypes.add(TYPE_FOLDER);
      ms_nodeTypes.add(TYPE_FOLDER_REF);
      ms_nodeTypes.add(TYPE_VIEW);
      ms_nodeTypes.add(TYPE_NEW_SRCH);
      ms_nodeTypes.add(TYPE_EMPTY_SRCH);
      ms_nodeTypes.add(TYPE_SAVE_SRCH);
      ms_nodeTypes.add(TYPE_PARENT);
      ms_nodeTypes.add(TYPE_SLOT);
      ms_nodeTypes.add(TYPE_SLOT_ITEM);
      ms_nodeTypes.add(TYPE_ITEM);
      ms_nodeTypes.add(TYPE_DTITEM);
      ms_nodeTypes.add(TYPE_STANDARD_SRCH);
      ms_nodeTypes.add(TYPE_CUSTOM_SRCH);

      ms_dataTypes.add(DATA_TYPE_TEXT);
      ms_dataTypes.add(DATA_TYPE_NUMBER);
      ms_dataTypes.add(DATA_TYPE_DATE);
      ms_dataTypes.add(DATA_TYPE_IMAGE);

      ms_nodeOrder.put(TYPE_FOLDER, new Integer(1));
      ms_nodeOrder.put(TYPE_SITE, new Integer(1));
      ms_nodeOrder.put(TYPE_SITESUBFOLDER, new Integer(1));
      ms_nodeOrder.put(TYPE_ITEM, new Integer(2));

      ms_searchTypes.add(TYPE_NEW_SRCH);
      ms_searchTypes.add(TYPE_EMPTY_SRCH);
      ms_searchTypes.add(TYPE_SAVE_SRCH);
      ms_searchTypes.add(TYPE_CUSTOM_SRCH);
      ms_searchTypes.add(TYPE_STANDARD_SRCH);
      ms_searchTypes.add(TYPE_VIEW);

      ms_systemFolderTypes.add(TYPE_SYS_FOLDERS);
      ms_systemFolderTypes.add(TYPE_SYS_SITES);
      
      ms_folderTypes.add(TYPE_FOLDER);
      ms_folderTypes.add(TYPE_SITE);
      ms_folderTypes.add(TYPE_SITESUBFOLDER);
      
      ms_helpTypeHints.add(HELP_TYPE_HINT_SRCH);      
   }

   /**
    * The constant to indicate root node name.
    */
   public static final String XML_NODE_NAME = "Node";

   /**
    * The constant to indicate Table Meta node name.
    */
   public static final String TABLEMETA_NODE = "TableMeta";

   //xml constants for Attributes
   private static final String NAME_ATTR = "name";
   private static final String LABEL_ATTR = "label";
   private static final String TYPE_ATTR = "type";
   private static final String HELP_TYPE_HINT_ATTR = "helptypehint";
   private static final String CHILD_URL_ATTR = "childrenurl";
   private static final String ICONKEY_ATTR = "iconkey";
   private static final String EXPANDED_ATTR = "expanded";
   private final static String PERMISSIONS_ATTR = "permissions";
   //xml constants for Elements
   private static final String COLUMN_NODE = "Column";
   private static final String ROWDATA_NODE = "RowData";
   private static final String COLDATA_NODE = "ColumnData";

}
