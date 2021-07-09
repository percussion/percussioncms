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
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents a row of data in a table.  A row contains a list of
 * PSJdbcColumnData objects, and specifies the action to take when processing
 * those columns.  It need not contain every column defined in the schema of the
 * table that contains this row - any columns not listed in this row will be
 * excluded from any insert or update sql statements during processing.
 * <p>
 * This class is readonly once instantiated, so that once a
 * PSJdbcTableData is created, it is immutable.  This is so once it is set on
 * a PSJdbcTableSchema object and validated, it cannot later be modified and
 * invalidated.
 */
public class PSJdbcRowData
{
   /**
    * Create a row from a list of column values
    *
    * @param columns an iterator over one or more {@link PSJdbcColumnData}
    * objects.  May not be <code>null</code> or empty.
    *
    * @param action one of the ACTION_xxx values, determines what action
    * will be taken when processing this row.
    *
    * @throws IllegalArgumentException if columns is <code>null</code> or
    * contains an object of the wrong type, or is empty.
    */
   public PSJdbcRowData(Iterator columns, int action)
   {
      if (columns == null)
         throw new IllegalArgumentException("columns may not be null");

      if (!columns.hasNext())
         throw new IllegalArgumentException("columns may not be empty");

      while (columns.hasNext())
      {
         Object colObj = columns.next();
         if (!(colObj instanceof PSJdbcColumnData))
            throw new IllegalArgumentException(
               "columns may only contain valid PSJdbcColumnData objects");

         m_columns.add(colObj);
      }

      switch (action)
      {
         case ACTION_INSERT:
         case ACTION_UPDATE:
         case ACTION_REPLACE:
         case ACTION_DELETE:
         case ACTION_INSERT_IF_NOT_EXIST:
            break;
         default:
            throw new IllegalArgumentException("invalid action");
      }

      m_action = action;
   }

   /**
    * Create an empty row object.
    *
    * @param tableName name of the table to which this row belongs, may not
    * be <code>null</code> or empty
    * @param parentRowData the reference to the parent row which contains this
    * child table row, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if tableName is <code>null</code> or
    * empty or parentRowData is <code>null</code>.
    */
   public PSJdbcRowData(String tableName, PSJdbcRowData parentRowData)
   {
      if (parentRowData == null)
         throw new IllegalArgumentException("parentRowData may not be null");

      m_parentRowData = parentRowData;
      setTableName(tableName);
   }

   /**
    * Create an empty row object.
    *
    * @param tableName name of the table to which this row belongs, may not
    * be <code>null</code> or empty
    *
    * @throws IllegalArgumentException if tableName is <code>null</code> or
    * empty.
    */
   public PSJdbcRowData(String tableName)
   {
      setTableName(tableName);
   }

   /**
    * Create this object from an Xml representation.
    *
    * @param sourceNode The element from which to get this object's state.
    * Element must conform to the definition for the row element in the
    * tabledata.dtd.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public PSJdbcRowData(Element sourceNode)
      throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      fromXml(sourceNode);
   }

   /**
    * Returns the reference to the parent row which contains this
    * child table row, may be <code>null</code> for row data corresponding to
    * parent tables, initialized in constructor for child table rows.
    *
    * @return the reference to the parent row, may be <code>null</code> for
    * row data corresponding to parent tables, initialized in constructor for
    * child table rows.
    */
   public PSJdbcRowData getParentRowData()
   {
      return m_parentRowData;
   }

   /**
    * Sets the name of the table to which this row of data belongs.
    *
    * @param tableName name of the table to which this row belongs, may not
    * be <code>null</code> or empty
    *
    * @throws IllegalArgumentException if tableName is <code>null</code> or
    * empty.
    */
   public void setTableName(String tableName)
   {
      if ((tableName == null) || (tableName.trim().length() == 0))
         throw new IllegalArgumentException(
            "tableName may not be null or empty");

      m_tableName = tableName;
   }

   /**
    * Returns the name of the table to which this row of data belongs.
    * @return the name of the table to which this row of data belongs, may be
    * <code>null</code> if a different constructor has been used which does not
    * set this variable
    */
   public String getTableName()
   {
      return m_tableName;
   }

   /**
    * Sets the boolean indicating if INSERT INTO... SELECT... statement should
    * be used for inserting this row into the database instead of
    * INSERT INTO ...VALUES...
    *
    * @param bUseInsertIntoSelectStmt <code>true</code> if INSERT INTO...
    * SELECT... statement should be used for inserting this row into the
    * database instead of INSERT INTO ...VALUES...
    * else <code>false</code>
    */
   public void setUseInsertIntoSelectStmt(boolean bUseInsertIntoSelectStmt)
   {
      m_bUseInsertIntoSelectStmt = bUseInsertIntoSelectStmt;
   }

   /**
    * Returns the boolean indicating if INSERT INTO... SELECT... statement
    * should be used for inserting this row into the database instead of
    * INSERT INTO ...VALUES...
    * @return <code>true</code> if INSERT INTO...SELECT... statement should
    * be used for inserting this row into the database instead of
    * INSERT INTO ...VALUES... else <code>false</code>
    */
   public boolean getUseInsertIntoSelectStmt()
   {
      return m_bUseInsertIntoSelectStmt;
   }

   /**
    * @return The value of the onTableCreateOnly flag.
    * See {@link #setOnTableCreateOnly(boolean)} for more info.
    */
   public boolean onTableCreateOnly()
   {
      return m_bOnTableCreateOnly;
   }

   /**
    * Sets the onTableCreateOnly flag.
    *
    * @param bOnTableCreateOnly The value of the onTableCreateOnly flag.
    * <code>true</code> if row data should only be processed if the table
    * containing this row data does not already exist
    * and is being created, <code>false</code> if data is to be processed
    * whether or not the table containing this row data is being created.
    */
   public void setOnTableCreateOnly(boolean bOnTableCreateOnly)
   {
      m_bOnTableCreateOnly = bOnTableCreateOnly;
   }

   /**
    * Returns an iterator over 1 or more PSJdbcColumnData objects.
    *
    * @return The iterator, never <code>null</code>.
    */
   public Iterator getColumns()
   {
      return m_columns.iterator();
   }

   /**
    * Returns an iterator over the child table data objects list.
    *
    * @return The iterator, never <code>null</code>.
    */
   public Iterator getChildTables()
   {
      return m_childTables.iterator();
   }

   /**
    * Convenience method that calls 
    * {@link #getColumn(String, boolean) getColumn(name, false)}
    */
   public PSJdbcColumnData getColumn(String name)
   {
      return getColumn(name, false);
   }

   /**
    * Returns the column with the specified name.
    *
    * @param name The name of the column, may not be <code>null</code> or empty.
    * @param ignoreCase <code>true</code> to compare the column name case-
    * insenstive, <code>false</code> to compare it case-sensitive.
    * 
    * @return The column, may be <code>null</code> if not contained in this row.
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public PSJdbcColumnData getColumn(String name, boolean ignoreCase)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      PSJdbcColumnData result = null;

      Iterator cols = m_columns.iterator();
      while (cols.hasNext() && result == null)
      {
         PSJdbcColumnData col = (PSJdbcColumnData)cols.next();
         if (ignoreCase && col.getName().equalsIgnoreCase(name))
            result = col;
         else if (!ignoreCase && col.getName().equals(name))
            result = col;
      }

      return result;
   }

   /**
    * adds a column to the row object
    *
    * @param colData column data containing the column name and value
    * May not be <code>null</code>.
    */
   public void addColumn(PSJdbcColumnData colData)
   {
      if (colData == null)
         throw new IllegalArgumentException("column data may not be null");

      m_columns.add(colData);
   }
   
   /**
    * Add a child table.
    * 
    * @param childTable the child table to add, not <code>null</code>.
    */
   public void addChildTable(PSJdbcTableData childTable)
   {
      if (childTable == null)
         throw new IllegalArgumentException("child table may not be null");
      
      m_childTables.add(childTable);
   }

   /**
    * Removes the column for the supplied name. Does nothing if no column is
    * found with the same name.
    *
    * @param name the column name to be removed, not <code>null</code> or
    *    empty.
    */
   public void removeColumn(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      for (int i=0; i<m_columns.size(); i++)
      {
         PSJdbcColumnData col = (PSJdbcColumnData) m_columns.get(i);
         if (col.getName().equals(name))
         {
            m_columns.remove(i);
            break;
         }
      }
   }

   /**
    * Returns this row's action.
    *
    * @return The action, one of the ACTION_xxx types.
    */
   public int getAction()
   {
      return m_action;
   }

   /**
    * Loads the child tables of this row of data from the XML
    * element.
    *
    * @param sourceNode The element from which to load the child tables
    * of this row of data. May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   private void loadChildTables(Element sourceNode)
      throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element table = tree.getNextElement(PSJdbcTableData.NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      while (table != null)
      {
         /*
          * Create the child table data in memory. Child tables obtain the
          * value of foreign key columns from the parent row.
          * pass a reference of the parent table row to the child table data
          * which in turn will pass it to the child row data. this way the
          * the child row data can get information about the parent row data
          */
         PSJdbcTableData childTableData = new PSJdbcTableData(table, this);
         String childTableName = childTableData.getName();
         PSJdbcTableSchema childTableSchema =
            PSJdbcTableFactory.getTableSchemaCollection().getTableSchema(
               childTableName);
         if (childTableSchema != null)
         {
            for (PSJdbcForeignKey foreignKey : childTableSchema.getForeignKeys()) 
            {
               Iterator cols = foreignKey.getColumns(m_tableName);
               if (cols.hasNext())
               {
                  while (cols.hasNext())
                  {
                     String[] col = (String[])cols.next();
                     String childTableColName = col[0];
                     String parentTableColName = col[2];

                     PSJdbcColumnData parentColData =
                        getColumn(parentTableColName);
                     PSJdbcColumnData childColData = null;

                     boolean bUseInsertIntoSelectStmt = false;
                     // parentColData or its value may be null if the corresponding
                     // column 'parentTableColName' is populated using a sequence
                     if ((parentColData == null) ||
                        (parentColData.getValue() == null))
                     {
                        // set the variable so the INSERT statement for the
                        // child rows in this case uses
                        // INSERT INTO ... SELECT statement instead of
                        // INSERT INTO ... VALUES statement
                        bUseInsertIntoSelectStmt = true;
                     }
                     else
                     {
                        childColData = new PSJdbcColumnData(childTableColName,
                           parentColData.getValue(),
                           parentColData.getEncoding());
                     }
                     Iterator childTableRows = childTableData.getRows();
                     while (childTableRows.hasNext())
                     {
                        PSJdbcRowData childTableRowData =
                           (PSJdbcRowData)childTableRows.next();

                        // if the column already exist remove it first
                        if (childTableRowData.getColumn(childTableColName) != null)
                           childTableRowData.removeColumn(childTableColName);

                        if (bUseInsertIntoSelectStmt)
                        {
                           childTableRowData.setUseInsertIntoSelectStmt(true);
                        }
                        else
                        {
                           childTableRowData.addColumn(childColData);
                        }
                     }
                  }
                  m_childTables.add(childTableData);
               }
            }
         }

         table = tree.getNextElement(PSJdbcTableData.NODE_NAME,
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
   }

   /**
    * Restore this object from an Xml representation.
    *
    * @param sourceNode The element from which to get this object's state.
    *    Element must conform to the definition for the row element in the
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

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      m_action = PSJdbcTableComponent.getEnumeratedAttributeIndex(
         tree, ACTION_ATTR, ACTION_ENUM);

      // get onTableCreateOnly flag - default to false if not specified
      m_bOnTableCreateOnly = false;
      String onTableCreateOnly = PSJdbcTableComponent.getAttribute(tree,
         ON_TABLE_CREATE_ONLY_ATTR, false);
      if (onTableCreateOnly != null && onTableCreateOnly.equalsIgnoreCase(XML_TRUE))
         m_bOnTableCreateOnly = true;

      Element colEl = tree.getNextElement(PSJdbcColumnData.NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (colEl == null)
      {
         Object[] args = {NODE_NAME, PSJdbcColumnData.NODE_NAME, "null"};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      while (colEl != null)
      {
         m_columns.add(new PSJdbcColumnData(colEl));

         colEl = tree.getNextElement(PSJdbcColumnData.NODE_NAME,
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }

      if ((m_tableName == null) || (m_tableName.trim().length() == 0))
      {
         // no child tables can be obtained if table name is not set
         return;
      }
      loadChildTables(sourceNode);
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

      root.setAttribute(ACTION_ATTR, ACTION_ENUM[m_action]);
      root.setAttribute(ON_TABLE_CREATE_ONLY_ATTR,
         m_bOnTableCreateOnly ? XML_TRUE : XML_FALSE);

      for (int i = 0; i < m_columns.size(); i++)
         root.appendChild(((PSJdbcColumnData)m_columns.get(i)).toXml(doc));
         
      // append all child tables
      for (int i=0; i<m_childTables.size(); i++)
      {
         PSJdbcTableData child = (PSJdbcTableData) m_childTables.get(i);
         
         Element childtable = doc.createElement("childtable");
         childtable.setAttribute("name", child.getName());
         root.appendChild(childtable);
         
         Iterator rows = child.getRows();
         while (rows.hasNext())
            childtable.appendChild(((PSJdbcRowData)rows.next()).toXml(doc));
      }

      return root;
    }

   /**
    * compares this column to another object.
    *
    * @param obj the object to compare
    * @return <code>true</code> if the object is a PSJdbcRowData with
    *    the same column names and values. Otherwise returns <code>false</code>.
    */
   public boolean equals(Object obj)
   {
      boolean isMatch = true;
      if (!(obj instanceof PSJdbcRowData))
         isMatch = false;
      else
      {
         PSJdbcRowData other = (PSJdbcRowData)obj;
         if (this.m_action != other.m_action)
            isMatch = false;
         else if (!this.m_columns.equals(other.m_columns))
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
      hash += (new Integer(m_action)).hashCode();
      hash += m_columns.hashCode();

      return hash;
   }

   /**
    * The name of this objects root Xml element.
    */
   public static String NODE_NAME = "row";

   /**
    * Constant for the create action.  This will cause this row to be
    * inserted if it does not already exist.
    */
   public static final int ACTION_INSERT = 0;

   /**
    * Constant for the update action.  This will cause this row to be
    * update if it already exists
    */
   public static final int ACTION_UPDATE = 1;

   /**
    * Constant for the replace action.  This will cause this row to be
    * inserted, first deleting it if it already exists.
    */
   public static final int ACTION_REPLACE = 2;

   /**
    * Constant for the delete action.  This will cause this row to be
    * deleted if it already exists.
    */
   public static final int ACTION_DELETE = 3;

   /**
    * Constant for the action to insert rows if they do not already exist in
    * the table.
    */
   public static final int ACTION_INSERT_IF_NOT_EXIST = 4;

   /**
    * An array of XML attribute values for the action. They are
    * specified at the index matching the constant's internal value for that
    * action.
    */
   private static final String[] ACTION_ENUM = {"n", "u", "r", "d", "i"};

   /**
    * List of PSJdbcColumnData objects for this row.  Never <code>null</code>,
    * never empty after construction.
    */
   private List m_columns = new ArrayList();

   /**
    * List of PSJdbcTableData objects of the child tables for this row.
    * May be empty.
    */
   private List m_childTables = new ArrayList();

   /**
    * Action for this row, one of the ACTION_xxx constants, defaults to
    * {@link #ACTION_INSERT}.
    */
   private int m_action = ACTION_INSERT;

   /**
    * Name of the table containing this row, may be <code>null</code>
    * or empty
    */
   private String m_tableName = null;

   /**
    * the boolean indicating if INSERT INTO... SELECT... statement
    * should be used for inserting this row into the database instead of
    * INSERT INTO ...VALUES...
    */
   private boolean m_bUseInsertIntoSelectStmt = false;

   /**
    * the reference to the parent row which contains this
    * child table row, may be <code>null</code> for row data corresponding to
    * parent tables, initialized in constructor for child table rows.
    */
   private PSJdbcRowData m_parentRowData = null;

   /**
    * stores the value of attribute <code>ON_TABLE_CREATE_ONLY_ATTR</code>,
    * defaults to <code>false</code>, so that the row data is processed even
    * if the table had not been created. This default value maintains the
    * existing behaviour of database publisher.
    */
   private boolean m_bOnTableCreateOnly = false;

   // Constants for Xml Elements and Attibutes
   private static final String ACTION_ATTR = "action";
   private static final String NAME_ATTR = "name";
   private static final String ON_TABLE_CREATE_ONLY_ATTR = "onTableCreateOnly";
   private static final String XML_TRUE = "yes";
   private static final String XML_FALSE = "no";
}

