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

package com.percussion.tablefactory;

import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents a list of row modifications for a single table. This
 * class is readonly once instantiated intentionally, so that once a
 * PSJdbcTableData is created, it is immutable.  This is so once it is set on
 * a PSJdbcTableSchema object and validated, and cannot later be modified and
 * invalidated.
 */
public class PSJdbcTableData
{
   /**
    * Convenience constructor that calls {@link #PSJdbcTableData(String,
    * Iterator, boolean) this(name, rows, true)}
    */
   public PSJdbcTableData(String name, Iterator rows)
   {
      this(name, rows, true);
   }

   /**
    * Constructs a table with no rows, using the supplied name.
    *
    * @param name The name of this table, may not be <code>null</code>.
    * @param rows An iterator over one or more PSJdbcRowData objects, may
    * be <code>null</code> in which case a table data object is created
    * containing no rows.
    * @param onCreateOnly Value of the onCreateOnly flag.  See
    * {@link #setOnCreateOnly(boolean)} for more info.
    *
    * @throws IllegalArgumentException if name is <code>null</code> or
    * empty.
    */
   public PSJdbcTableData(String name, Iterator rows, boolean onCreateOnly)
   {
      if ((name == null || name.trim().length() == 0))
         throw new IllegalArgumentException("name is invalid");

      m_name = name;
      m_onCreateOnly = onCreateOnly;

      while (rows != null && rows.hasNext())
      {
         Object rowObj = rows.next();
         if (!(rowObj instanceof PSJdbcRowData))
            throw new IllegalArgumentException("rows contains invalid object");

         addRow((PSJdbcRowData)rowObj);
      }
   }

   /**
    * Construct this object from an Xml representation. This constructor is
    * used for constructing tabledata for child tables.
    *
    * @param sourceNode The element from which to get this object's state.
    *    Element must conform to the definition for the table element in the
    *    tabledata.dtd.  May not be <code>null</code>.
    * @param parentRowData the reference to the parent row which contains this
    * child table data, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if sourceNode or parentRowData is
    * <code>null</code>.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public PSJdbcTableData(Element sourceNode, PSJdbcRowData parentRowData)
      throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (parentRowData == null)
         throw new IllegalArgumentException("parentRowData may not be null");

      m_parentRowData = parentRowData;
      fromXml(sourceNode);
   }

   /**
    * Construct this object from an Xml representation.
    *
    * @param sourceNode The element from which to get this object's state.
    *    Element must conform to the definition for the table element in the
    *    tabledata.dtd.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public PSJdbcTableData(Element sourceNode) throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      fromXml(sourceNode);
   }

   /**
    * Adds a row of data to this table.
    *
    * @param row The row to add, assumed not <code>null</code>.
    *
    */
   private void addRow(PSJdbcRowData row)
   {
      m_rows.add(row);
      Iterator columns = row.getColumns();
      while (columns.hasNext())
      {
         PSJdbcColumnData column = (PSJdbcColumnData)columns.next();
         m_columnNames.add(column.getName());
      }

      // track if this is not an insert
      if (row.getAction() != PSJdbcRowData.ACTION_INSERT)
      {
         m_hasUpdates = true;
         m_updateRows.add(row);
      }
   }

   /**
    * Return's this table's name.
    *
    * @return The name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Returns this tables rows of data.
    *
    * @return Iterator over zero or more rows of data.  Never <code>null</code>.
    */
   public Iterator getRows()
   {
      return m_rows.iterator();
   }

   /**
    * Gets the number of rows of data in this table.
    * 
    * @return the number of rows, always >= 0.
    */
   public int getRowCount()
   {
      return m_rows.size();
   }
   
   /**
    * Returns the distinct list columns used in this table data.
    *
    * @return An iterator over one or more column names as Strings.
    */
   public Iterator getColumnNames()
   {
      return m_columnNames.iterator();
   }

   /**
    * Determines if there are any rows with an action requiring either a primary
    * key or an update key.
    *
    * @return <code>true</code> if there are any rows with an action other than
    * {@link PSJdbcRowData#ACTION_INSERT}, <code>false</code> if not.
    */
   public boolean hasUpdates()
   {
      return m_hasUpdates;
   }

   /**
    * Convenience method to return only the rows with an action other than
    * {@link PSJdbcRowData#ACTION_INSERT}.
    *
    * @return An iterator over zero or more PSJdbcRowData objects, never <code>
    * null</code>.
    */
   public Iterator getUpdateRows()
   {
      return m_updateRows.iterator();
   }

   /**
    * @return The value of the onCreateOnly flag.  See {@link #setOnCreateOnly(
    * boolean)} for more info.
    */
   public boolean onCreateOnly()
   {
      return m_onCreateOnly;
   }

   /**
    * Sets the onCreateOnly flag.
    *
    * @param onCreateOnly The value of the onCreateOnly flag. <code>true</code>
    * if table data should only be processed if the table does not already exist
    * and is being created, <code>false</code> if data is to be processed
    * whether or not the table is being created.
    */
   public void setOnCreateOnly(boolean onCreateOnly)
   {
      m_onCreateOnly = onCreateOnly;
   }

   /**
    * Restore this object from an Xml representation.
    *
    * @param sourceNode The element from which to get this object's state.
    *    Element must conform to the definition for the table element in the
    *    tabledata.dtd.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public void fromXml(Element sourceNode) throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!sourceNode.getNodeName().equals(NODE_NAME))
      {
         Object[] args = {NODE_NAME, sourceNode.getNodeName()};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      m_rows = new ArrayList();
      m_columnNames = new HashSet();
      m_updateRows = new ArrayList();

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      // get name
      m_name = PSJdbcTableComponent.getAttribute(tree, NAME_ATTR, true);

      // get onCreateOnly - default to true if not specified
      String onCreateOnly = PSJdbcTableComponent.getAttribute(tree,
         ON_CREATE_ATTR, false);
      if (onCreateOnly != null && onCreateOnly.equalsIgnoreCase(XML_FALSE))
         m_onCreateOnly = false;
      else
         m_onCreateOnly = true;


      Element rowEl = tree.getNextElement(PSJdbcRowData.NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      while (rowEl != null)
      {
         PSJdbcRowData rowData = null;
         if (m_parentRowData == null)
            rowData = new PSJdbcRowData(m_name);
         else
            rowData = new PSJdbcRowData(m_name, m_parentRowData);
         rowData.fromXml(rowEl);
         addRow(rowData);

         rowEl = tree.getNextElement(PSJdbcRowData.NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
   }

   /**
    * Serializes this object's state to Xml conforming with the tabledata.dtd.
    *
    * @param doc The document to use when creating elements.  May not be <code>
    *    null</code>.
    *
    * @return The element containing this object's state, never <code>
    *    null</code>.
    *
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // create the root element
      Element   root = doc.createElement(NODE_NAME);
      root.setAttribute(NAME_ATTR, m_name);
      root.setAttribute(ON_CREATE_ATTR, m_onCreateOnly ? XML_TRUE : XML_FALSE);

      for (int i = 0; i < m_rows.size(); i++)
         root.appendChild(((PSJdbcRowData)m_rows.get(i)).toXml(doc));

      return root;
    }


   /**
    * compares this column to another object.
    *
    * @param obj the object to compare
    * @return <code>true</code> if the object is a PSJdbcTableData with
    *    the same column names and values. Otherwise returns <code>false</code>.
    */
   public boolean equals(Object obj)
   {
      boolean isMatch = true;
      if (!(obj instanceof PSJdbcTableData))
         isMatch = false;
      else
      {
         PSJdbcTableData other = (PSJdbcTableData)obj;
         if (!this.m_name.equals(other.m_name))
            isMatch = false;
         else if (this.m_onCreateOnly ^ other.m_onCreateOnly)
            isMatch = false;
         else if (this.m_hasUpdates ^ other.m_hasUpdates)
            isMatch = false;
         else if (!this.m_rows.equals(other.m_rows))
            isMatch = false;
      }

      return isMatch;
   }


   /**
    * Overridden to fullfill the contract that if t1 and t2 are 2 different
    * instances of this class and t1.equals(t2), t1.hashCode() ==
    * t2.hashCode().
    *
    * @return The sum of all the hash codes of the composite objects.
    */
   public int hashCode()
   {
      int hash = 0;
      if ( null != m_name )
         hash += m_name.hashCode();
      hash += Boolean.valueOf(m_onCreateOnly).hashCode();
      hash += m_rows.hashCode();
      return hash;
   }
   
   /**
    * Updates the value of the supplied column with the supplied value in every
    * row of table data.
    * 
    * @param colName The column name, may not be <code>null</code> or empty.
    * @param colValue The new column value.  May be <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   public void updateColumn(String colName, String colValue)
   {
      if (colName == null || colName.trim().length() == 0)
         throw new IllegalArgumentException("colName may not be null or empty");
      
      Iterator rows = m_rows.iterator();
      while (rows.hasNext())
      {
         PSJdbcRowData rowData = (PSJdbcRowData) rows.next();
         PSJdbcColumnData versionCol = rowData.getColumn(colName);
         if (versionCol != null)
            versionCol.setValue(colValue);
      }
   }

   /**
    * The name of this object's root Xml element.
    */
   public static String NODE_NAME = "table";

   /**
    * This table's name, never <code>null</code> or empty once constructed.
    */
   private String m_name = null;

   /**
    * If <code>true</code>, table data is only processed if the table does not
    * already exist and is being created.  If <code>false</code>, data is
    * processed whether or not the table is being created.  Initialized to
    * <code>true</code>.
    */
   private boolean m_onCreateOnly = true;

   /**
    * List of rows for this table, never <code>null</code>, may be empty.
    */
   private List m_rows = new ArrayList();

   /**
    * Set of column names used in this table's rows.  Never <code>null</code> or
    * emtpy.
    */
   private Set m_columnNames = new HashSet();

   /**
    * Tracks if any rows have been added with actions that update existing data.
    * <code>false</code> by default, set to <code>true</code> if {@link
    * #addRow(PSJdbcRowData) addRow} has been called with a row that has any
    * action type other than {@link PSJdbcRowData#ACTION_INSERT}
    */
   private boolean m_hasUpdates = false;

   /**
    * the reference to the parent row which contains this
    * child table data, may be <code>null</code> for table data corresponding to
    * parent tables, initialized in constructor for child tables.
    */
   private PSJdbcRowData m_parentRowData = null;

   /**
    * List of rows that contain update actions.  Includes any row for which
    * #addRow(PSJdbcRowData) addRow} has been called with a row that has any
    * action type other than {@link PSJdbcRowData#ACTION_INSERT}.  Never <code>
    * null</code>, may be empty.
    */
   private List m_updateRows = new ArrayList();

   // Constants for Xml Elements and Attibutes
   private static final String NAME_ATTR = "name";
   private static final String ON_CREATE_ATTR = "onCreateOnly";
   private static final String XML_TRUE = "yes";
   private static final String XML_FALSE = "no";
}

