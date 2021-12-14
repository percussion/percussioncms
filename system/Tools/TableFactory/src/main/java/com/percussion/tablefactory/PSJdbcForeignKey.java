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

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * This class is used to represent a set of foreign key contraints for a table,
 * and the action to perform when that table schema is used to create or modify a
 * table.
 */
public class PSJdbcForeignKey extends PSJdbcTableComponent
{
   /**
    * Basic constructor for this class.  Constructs a foreign Key definition
    * with a constraint on one column.  Additional columns may be added using
    * {@link #addColumn(String, String, String) addColumn}.
    *
    * @param colName The name of the column in the table with the constraint.
    * May not be <code>null</code> or empty.
    * @param extTable The table that contains the externally referenced column.
    * May not be <code>null</code> or empty.
    * @param extCol The external column whose value constrains the value of
    * the column referenced by colName. May not be <code>null</code> or empty.
    * @param action One of the <code>PSJdbcTableComponent.ACTION_xxx</code>
    *    constants.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSJdbcForeignKey(String colName, String extTable, String extCol,
      int action)
   {
      super(null, action);

      // this will validate for us
      addColumn(colName, extTable, extCol);
   }

   /**
    * Constructs a foreign Key definition from a list of constraints.
    *
    * @param cols A list of foreign key columns.  Each entry is a String[] with
    * 3 entries, the column name, the external table name, and the external
    * column name respectively, all not <code>null</code> or empty.  Each entry
    * in the list may not be <code>null</code>.  May not be <code>null</code> or
    * empty.
    * @param action One of the <code>PSJdbcTableComponent.ACTION_xxx</code>
    *    constants.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSJdbcForeignKey(Iterator cols, int action)
   {
      super(null, action);

      if (cols == null)
         throw new IllegalArgumentException("cols may not be null");

      while (cols.hasNext())
      {
         Object obj = cols.next();
         if (!(obj instanceof String[]))
            throw new IllegalArgumentException(
               "invalid entry in cols: not a String[]");

         String[] col = (String[])obj;
         if (col.length != 3)
            throw new IllegalArgumentException(
               "invalid entry in cols: wrong size");

         // this will validate for us
         try
         {
            addColumn(col[0], col[1], col[2]);
         }
         catch (IllegalArgumentException e)
         {
            throw new IllegalArgumentException("invalid entry in cols: " +
               e.toString());
         }
      }
   }

   /**
    * Constructs a foreign Key definition from a name and a list of constraints.
    *
    * @param fkName FK name, never <code>null</code> or <code>empty</code>.
    * @param cols A list of foreign key columns.  Each entry is a String[] with
    * 3 entries, the column name, the external table name, and the external
    * column name respectively, all not <code>null</code> or empty.  Each entry
    * in the list may not be <code>null</code>.  May not be <code>null</code> or
    * empty.
    * @param action One of the <code>PSJdbcTableComponent.ACTION_xxx</code>
    *    constants.
    */
   public PSJdbcForeignKey(String fkName, Iterator cols, int action)
   {
       this(cols, action);
       setName(fkName);
   }
   
   /**
    * Creates this object from its Xml representation.  See {@link #fromXml(
    * Element) fromXml} for more information.
    *
    * @param sourceNode The element from which this object is to be constructed.
    *    Element must conform to the definition for the foreignkey element in
    *    the tabledef.dtd.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if the Xml definition is invalid,
    *    or if there are any other errors.
    */
   public PSJdbcForeignKey(Element sourceNode)
      throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      fromXml(sourceNode);
   }

   /**
    * Adds a column definition.
    *
    * @param colName The name of the column in the table with the constraint.
    * May not be <code>null</code> or empty.
    * @param extTable The table that contains the externally referenced column.
    * May not be <code>null</code> or empty.
    * @param extCol The external column whose value constrains the value of
    * the column referenced by colName. May not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public void addColumn(String colName, String extTable, String extCol)
   {
      if (colName == null || colName.trim().length() == 0)
         throw new IllegalArgumentException("colName may not be null or empty");

      if (extTable == null || extTable.trim().length() == 0)
         throw new IllegalArgumentException(
            "extTable may not be null or empty");

      if (extCol == null || extCol.trim().length() == 0)
         throw new IllegalArgumentException("extCol may not be null or empty");

      String[] entry = {colName, extTable, extCol};
      m_columns.add(entry);
      m_tables.add(extTable);
   }

   /**
    * Returns the list of foreign key columns.
    *
    * @return An iterator over one or more foreign key columns.   Each entry is
    * a String[] with 3 entries, the column name, the external table name, and
    * the external column name respectively, all not <code>null</code> or empty.
    */
   public Iterator getColumns()
   {
      return m_columns.iterator();
   }

   /**
    * Returns the list of the internal column names included in the foreign key
    * columns.
    *
    * @return An iterator over one or more column names as Strings.
    * Never <code>null</code>.
    */
   public Iterator getInternalColumns()
   {
      List names = new ArrayList();
      Iterator cols = m_columns.iterator();
      while (cols.hasNext())
      {
         String[] col = (String[])cols.next();
         names.add(col[0]);
      }
      return names.iterator();
   }

   /**
    * Returns the list of foreign key columns that reference a particular
    * external table.
    *
    * @param tableName The external table name, may not be <code>null</code> or
    * emtpy.
    *
    * @return An iterator over one or more foreign key columns.   Each entry is
    * a String[] with 3 entries, the column name, the external table name, and
    * the external column name respectively, all not <code>null</code> or empty.
    * If there are no foreign keys defined that referenced the specifed
    * tablename, then an empty iterator is returned.  If the tableName provided
    * is from the list returned by {@link #getTables()}, the iterator is
    * guaranteed to have at least one element.
    *
    * @throws IllegalArgumentException if tableName is <code>null</code> or
    * empty.
    */
   public Iterator getColumns(String tableName)
   {
      if (tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName may not be null or empty");

      List tableCols = new ArrayList();
      if (m_tables.contains(tableName))
      {
         Iterator cols = m_columns.iterator();
         while (cols.hasNext())
         {
            String[] col = (String[])cols.next();
            if (col[1].equals(tableName))
               tableCols.add(col);
         }
      }

      return tableCols.iterator();
   }

    /**
     * Gets a list of all column names of the foreign key object.
     * 
     * @return The column names of the foreign key. Cannot be <code>null</code> or empty. 
     */

   List<String> getForeignKeyColumnNames()
   {
      Iterator<?> columnNameIterator = getColumns();
      ArrayList<String> columnList = new ArrayList<String>();
      while (columnNameIterator.hasNext())
      {
          String[] columnNames = (String[]) columnNameIterator.next();
          String columnName = columnNames[0];
          columnList.add(columnName);
      }
      return columnList;
   }

   /**
    * Returns an iterator over one or more tables that are referenced by this
    * foreign key constraint.
    *
    * @return The table names, never <code>null</code> or empty.
    */
   public Iterator getTables()
   {
      return m_tables.iterator();
   }

   /**
    * Restore this object from an Xml representation.
    *
    * @param sourceNode The element from which to get this object's state.
    *    Element must conform to the definition for the foreignKey
    *    element in the tabledef.dtd.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if the Xml definition contains
    *    any empty or duplicate column names, or if there are any other errors.
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

      m_columns.clear();;

      // allow base class to set its memebers
      getComponentState(sourceNode);

      // find first fkcolumn
      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);
      Element fkcolumn = walker.getNextElement(FK_COLUMN_EL,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      // make sure we get at least one
      do
      {
         if (fkcolumn == null)
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.XML_ELEMENT_NULL, FK_COLUMN_EL);

         // load it's child elements
         String colName = getRequiredElement(walker, NAME_EL);
         String extTable = getRequiredElement(walker, EXT_TABLE_EL);
         String extCol = getRequiredElement(walker, EXT_COLUMN_EL);

         addColumn(colName, extTable, extCol);

         fkcolumn = walker.getNextElement(FK_COLUMN_EL,
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      } while (fkcolumn != null);

   }

   /**
    * Serializes this object's state to Xml conforming with the tabledef.dtd.
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

      // first add base class state
      setComponentState(root);

      // now add each name
      Iterator cols = m_columns.iterator();
      while (cols.hasNext())
      {
         String[] colDef = (String[])cols.next();
         Element colEl = PSXmlDocumentBuilder.addEmptyElement(doc, root,
            FK_COLUMN_EL);
         PSXmlDocumentBuilder.addElement(doc, colEl, NAME_EL, colDef[0]);
         PSXmlDocumentBuilder.addElement(doc, colEl, EXT_TABLE_EL, colDef[1]);
         PSXmlDocumentBuilder.addElement(doc, colEl, EXT_COLUMN_EL, colDef[2]);
      }

      return root;
   }

   /**
    * Converts a single foreign key definition into a list where separate tables
    * are defined in seaparate key objects. This is provided to convert old xml
    * that is defined in a single foriegn key element into an object per foriegn
    * key contraint in the database and allow effective comparison between old
    * and new.
    * 
    * @return The A list of PSJdbcForeignKey objects never null <code>
    *    null</code>. If the foreign key does not reverence more than one table
    *         the list will contain a single instance of the current object.
    * 
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public List<PSJdbcForeignKey> normalizeForiegnKeys()
   {
      List<PSJdbcForeignKey> newKeysList = new ArrayList<PSJdbcForeignKey>();
      if (m_tables != null && m_tables.size() > 1)
      {
         Iterator tables = m_tables.iterator();
         int i = 0;
         while (tables.hasNext())
         {
            String tableName = (String) tables.next();
            List<String[]> tableCols = new ArrayList<String[]>();
            Iterator cols = m_columns.iterator();
            while (cols.hasNext())
            {
               String[] col = (String[]) cols.next();
               if (col[1].equals(tableName))
                  tableCols.add(col);
            }
            PSJdbcForeignKey newKey = new PSJdbcForeignKey(tableCols.iterator(), getAction());
            if (this.getName()!=null && this.getName().length()>0) {
               String name = this.getName();
               // Maintain existing name for first fk
               if (i++>1)
                  name+="_"+i;
               newKey.setName(name);
            }
            newKeysList.add(newKey);
         }
      }
      else
      {
         newKeysList.add(this);
      }
      return newKeysList;
   }
   
   /**
    * compares this column to another object.
    *
    * @param obj the object to compare
    * @return <code>true</code> if the object is a PSJdbcForeignKey with
    *    the same columns. Otherwise returns <code>false</code>.
    */
   public boolean equals(Object obj)
   {
      boolean isMatch = true;
      if (!(obj instanceof PSJdbcForeignKey))
         isMatch = false;
      else
      {
         PSJdbcForeignKey other = (PSJdbcForeignKey)obj;
         if (!super.equals(other))
            isMatch = false;
         else
            isMatch = isComponentEqual(other);
      }
      return isMatch;
   }

   /**
    * Compares this key object to another foreign key object. Ignores the name
    * and action of the keys while comparing. Returns <code>true</code> if both
    * keys comprise the same tables, and same columns from the corresponding
    * table, <code>false</code> otherwise.
    *
    * @param other the foreign key object to compare, may be <code>null</code>
    * in which case <code>false</code> is returned.
    *
    * @return <code>true</code> if this object and <code>other</code> comprise
    * the same tables, and same columns from the corresponding table.
    * Otherwise returns <code>false</code>.
    */
   public boolean isComponentEqual(PSJdbcForeignKey other)
   {
      if (other == null)
         return false;

      boolean isMatch = true;

      if (this.m_columns.size() != other.m_columns.size())
         isMatch = false;
      else if (this.m_tables.size() != other.m_tables.size())
         isMatch = false;
      else
      {
         /*
          * need to make sure all columns in this object exist in the other
          * without regard for the ordering of the lists
          */
         for (int i = 0; i < this.m_columns.size() && isMatch; i++)
         {
            String[] thisCol = (String[])this.m_columns.get(i);
            // walk other's columns to find a match
            boolean found = false;
            for (int j = 0; j < other.m_columns.size() && !found; j++)
            {
               String[] otherCol = (String[])other.m_columns.get(j);
               // see if the two column's are equal
               boolean equals = true;
               for (int k = 0; k < thisCol.length; k++)
               {
                  if (!thisCol[k].equals(otherCol[k]))
                  {
                     equals = false;
                     break;
                  }
               }
               if (equals)
                  found = true;
            }
            if (!found)
               isMatch = false;
         }
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
      hash += super.hashCode();
      Iterator i = m_columns.iterator();
      while (i.hasNext())
      {
         String[] col = (String[])i.next();
         hash += col[0].hashCode();
         hash += col[1].hashCode();
         hash += col[2].hashCode();
      }
      return hash;
   }

   /**
    * Used by <code>toXml</code> and <code>fromXml</code> to determine if the
    * name attribute is required.
    *
    * @return <code>true</code> if name is required, <code>false</code> if not.
    */
   protected boolean isNameRequired()
   {
      return (getName()!=null && getName().length()>0);
   }

   /**
    * Gets the element data from an required attribute or child element.
    * It is an error for a required node to be absent or empty.
    *
    * @param tree a valid PSXmlTreeWalker currently positioned at the element
    *        that is the parent of the required node. Assumed not <code>null
    *        </code>.
    * @param elemName the name of the node to retrieve data from; assumed not
    *        <code>null</code>
    *
    * @return not empty, not <code>null</code> String containing the element
    *         data from the specified node.
    *
    * @throws PSJdbcTableFacotryException if the specified node is missing,
    *         or empty.
    */
   private static String getRequiredElement(PSXmlTreeWalker tree,
      String elemName) throws PSJdbcTableFactoryException
   {
      String data = tree.getElementData(elemName, false);
      if (null == data || data.trim().length() == 0)
      {
         String parentName = tree.getCurrent().getNodeName();
         Object[] args = {  parentName, elemName, "null" };
         throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      return data;
   }


   /**
    * The name of this objects root Xml element.
    */
   public static String NODE_NAME = "foreignkey";

   /**
    * name of this container for error messages.
    */
   public static String CONTAINER_NAME = "foreign key";

   /**
    * A list of foreign key columns.  Each entry is a String[] with 3 entries,
    * the column name, the external table name, and the external column name
    * respectively, all not <code>null</code> or empty.  Each entry in the
    * list is not <code>null</code>.  Never <code>null</code> or empty.
    */
   private List m_columns = new ArrayList();

   /**
    * A Set of tables referenced by foreign key columns.  Each entry is the
    * tableName as a String.  Each entry in the list is not <code>null</code> or
    * empty. Never <code>null</code> or empty.
    */
   private Set m_tables = new HashSet();

   // xml elements
   private static final String FK_COLUMN_EL = "fkColumn";
   private static final String NAME_EL = "name";
   private static final String EXT_TABLE_EL = "externalTable";
   private static final String EXT_COLUMN_EL = "externalColumn";
}

