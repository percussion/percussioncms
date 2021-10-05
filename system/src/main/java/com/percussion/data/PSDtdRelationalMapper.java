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

package com.percussion.data;

import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.xml.PSDtdAttribute;
import com.percussion.xml.PSDtdDataElement;
import com.percussion.xml.PSDtdElement;
import com.percussion.xml.PSDtdElementEntry;
import com.percussion.xml.PSDtdNode;
import com.percussion.xml.PSDtdNodeList;
import com.percussion.xml.PSDtdTree;
import com.percussion.xml.PSDtdTreeVisitor;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/****************************************************************************
 * The PSDtdRelationalMapper class is used to map from an XML DTD to a
 * collection of relational table schemata. These tables have implicit,
 * invisible, unique ID columns that are used with joining to represent
 * the link between parent and child in the data source.
 ***************************************************************************/
public class PSDtdRelationalMapper implements PSDtdTreeVisitor
{
   public static void main(String[] args)
   {
      // do some sort of test
   }

   /**
    * @author   unascribed
    *
    * @version 1.1 1999/6/3
    *
    * Constructs a mapper that can be used to build table schemata
    * for the DTD.
    *
    * @param   dtd   The DTD for which to build schemata.
    *
    */
   public PSDtdRelationalMapper(PSDtdTree dtd)
   {
      this(dtd, null);
   }

   /**
    * @author   unascribed
    *
    * @version 1.1 1999/6/3
    *
    * Constructs a mapper that can be used to build table schemata
    * for the DTD.
    *
    * @param   dtd   The DTD for which to build schemata.
    *
    * @param   traceOut   The trace print stream, or null if tracing is
    * disabled.
    */
   public PSDtdRelationalMapper(PSDtdTree dtd, PrintStream traceOut)
   {
      m_dtdTree = dtd;
      m_tables = new HashMap();
      m_tablesInOrder = new ArrayList();
      m_currentColumnPrefix = "";
      m_parentNames = new HashMap();

      if (traceOut != null)
         enableTrace(traceOut);

      buildTables();

      // lose reference to DTD tree in order to reduce memory usage
      // we don't need it any more.
      m_dtdTree = null;
   }

   /**
    * @author   chadloder
    *
    * @version 1.0 1999/6/3
    *
    * Private utility method to build the tables. Called from the
    * constructor.
    */
   private void buildTables()
   {
      PSDtdElementEntry root = m_dtdTree.getRoot();
      root.acceptVisitor(this, null);

      if (false) // if (m_trace)
      {
         printTableDefs(m_traceOut);
      }
   }

   /**
    *   @author chad loder
    *
    *   @version   1.0 1999/6/13
    *
    *   Prints the given table defitions to a print stream.
    *
    *   @param   out
    *
    */
   public void printTableDefs(PrintStream out)
   {
      for (Iterator i = m_tablesInOrder.iterator(); i.hasNext(); )
      {
         TableDef table = (TableDef)i.next();
         out.println(
            "******************** " + table.getName() +
            " ********************"
            );

         for (int j = 1; j <= table.getNumColumns(); j++)
         {
            ColumnDef col = table.getColumn(j);
            out.print("|\t" + col.getName());
            ColumnDef fkey = col.getForeignKey();
            if (fkey != null)
            {
               out.print(" (->"
                  + fkey.getTable().getName() + "." + fkey.getName()
                  + ")");
            }
            out.print(" ");
         }
         out.println("\r\n\r\n");
      }
   }

   /**
    *   @author chad loder
    *
    *   @version   1.0 1999/6/13
    *
    *   Gets the number of tables defined.
    *
    *   @return int
    */
   public int getNumTables()
   {
      return m_tablesInOrder.size();
   }

   /**
    * @author   chad loder
    *
    * @version 1.0 1999/6/10
    *
    * Gets the table with the given ordinal.
    *
    * @param   ordinal 1-based
    *
    * @return   TableDef
    */
   public TableDef getTable(int ordinal)
   {
      return (TableDef)m_tablesInOrder.get(ordinal - 1);
   }


   /**
    * @author chad loder
    *
    * @version   1.0 1999/6/13
    *
    * Gets the table with the given name. Returns null if no
    * such table
    *
    * @param   tableName
    *
    * @return TableDef
    */
   public TableDef getTable(String tableName)
   {
      return (TableDef)m_tables.get(tableName);
   }

   public int getTableOrdinal(String tableName)
   {
      TableDef tab = getTable(tableName);
      if (tab == null)
         return -1;
      return m_tablesInOrder.indexOf(tab) + 1;
   }

   /////////////////////// PSDtdTreeVisitor implementation ////////////////////

   public Object visit(PSDtdNode node, Object data)
   {
      // this should not happen, as PSDtdNode is an abstract class.
      //--+trace("Visit PSDtdNode; data = " + data);
      node.childrenAccept(this, data);
      return null;
   }

   public Object visit(PSDtdElementEntry node, Object data)
   {
      //--+trace("Visit PSDtdElementEntry (" + node.getElement().getName() +
      //    "); data = " + data);

      PSDtdElement el = node.getElement();

      int occ = node.getOccurrenceType();

      boolean hasCharData = el.hasCharacterData();
      if (hasCharData)
      {
         //--+trace("Element " + el.getName() + " can have char data.");
      }

      TableDef parentTable = (TableDef)data;

      boolean newParent = false;
      if (parentTable == null)
      {
         // the parent doesn't have a table, so we need to create one
         newParent = true;
         parentTable = new TableDef(node.getElement().getName());
         m_tables.put(node.getElement().getName(), parentTable);
         m_tablesInOrder.add(parentTable);
      }

      // we also treat ( FOO | BAR | BAZ )* by looking at the parent and
      // seeing if it is an option list (as opposed to a sequence), and if
      // so, if the parent can occur more than once, then create a separate
      // table for each child in the option list
      boolean parentRepeatableOptionList = false;
      PSDtdNode parentNode = node.getParentNode();
      if (parentNode != null)
      {
         int parentOcc = parentNode.getOccurrenceType();
         if (parentOcc == PSDtdNode.OCCURS_ATLEASTONCE || parentOcc == PSDtdNode.OCCURS_ANY)
         {
            if (parentNode instanceof PSDtdNodeList)
            {
               PSDtdNodeList nodeList = (PSDtdNodeList)parentNode;
               if (nodeList.getType() == PSDtdNodeList.OPTIONLIST)
               {
                  //--+trace("Element " + el.getName() + " is member of repeatable option list.");
                  parentRepeatableOptionList = true;
               }
            }
         }
      }

      // if there can be many of these elements within the parent
      if (// parentRepeatableOptionList ||
         occ == PSDtdNode.OCCURS_ATLEASTONCE ||
         occ == PSDtdNode.OCCURS_ANY)
      {
         //--+trace("Element " + el.getName() + " occurs more than once. Table candidate.");

         PSDtdElementEntry parentElEntry = node.getParentElement();
//          if (parentElEntry == null)
//          {
//             throw new RuntimeException("Error: " + el.getName() + " has no parent element.");
//          }

         TableDef table = (TableDef)m_tables.get(el.getName() );

         if (table == null)
         {
            //--+trace("Creating new table.");
            table = new TableDef(el.getName());

            // ColumnDef uniqueIDCol = new ColumnDef(makeIdColName(el.getName()));

            // add a unique ID column for this table
            // table.setUniqueIDColumn(uniqueIDCol);
         }

         ColumnDef parentIDColumn = parentTable.getUniqueIDColumn();

         if (parentIDColumn == null)
         {
            // we have a parent table, but it doesn't have a unique ID column yet,
            // because no one has tried to link to it
            // so add the unique ID column to the parent table before proceeding
            parentTable.setUniqueIDColumn(new ColumnDef(
               makeIdColName(parentTable.getName())));
            parentIDColumn = parentTable.getUniqueIDColumn();
         }

         ColumnDef parentJoin = new ColumnDef(
            makeParentIdColName(el.getName()),
            parentIDColumn);

         table.addColumn(parentJoin);

         // if this element can have character data, then add a data column whose
         // name is the same as the element
         if (hasCharData)
         {
            table.addColumn(new ColumnDef(el.getName() + "/" + el.getName()));
         }

         // add columns for each attribute of this element
         for (int i = 0; i < el.getNumAttributes(); i++)
         {
            PSDtdAttribute attr = el.getAttribute(i);
            table.addColumn(new ColumnDef(el.getName() + "/@" + attr.getName()));
         }

         m_tables.put(el.getName(), table);
         m_tablesInOrder.add(table);

         String oldPrefix = m_currentColumnPrefix;
         m_currentColumnPrefix = table.getName();
         node.childrenAccept(this, table);
         m_currentColumnPrefix = oldPrefix;
      }
      else
      {
         // if this element can have character data, then add a data column
         if (hasCharData)
         {
            if (m_currentColumnPrefix.length() > 0)
               parentTable.addColumn(new ColumnDef(m_currentColumnPrefix + "/" + el.getName()));
            else
               parentTable.addColumn(new ColumnDef(el.getName()));
         }

         // add columns for each attribute of this element
         for (int i = 0; i < el.getNumAttributes(); i++)
         {
            PSDtdAttribute attr = el.getAttribute(i);
            if (m_currentColumnPrefix.length() > 0)
               parentTable.addColumn(new ColumnDef(m_currentColumnPrefix + "/" +
                  el.getName() + "/@" + attr.getName()));
            else
               parentTable.addColumn(new ColumnDef(el.getName() + "/@" + attr.getName()));

         }

         String oldPrefix = m_currentColumnPrefix;
         if (m_currentColumnPrefix.length() > 0)
            m_currentColumnPrefix = m_currentColumnPrefix + "/" + el.getName();
         else
            m_currentColumnPrefix = el.getName();

         node.childrenAccept(this, parentTable);
         m_currentColumnPrefix = oldPrefix;
      }

      //--+trace("Finished visit of " + el.getName());
      return null;
   }

   public Object visit(PSDtdNodeList node, Object data)
   {
      //--+trace("Visit PSDtdNodeList; data = " + data);

      node.childrenAccept(this, data);
      return null;
   }

   public Object visit(PSDtdDataElement node, Object data)
   {
      //--+trace("Visit PSDtdDataElement; data = " + data);
      node.childrenAccept(this, data);
      return null;
   }

   /**
    * @author   chadloder
    *
    * @version 1.0 1999/6/3
    *
    * Enables debug tracing to the given stream.
    *
    * @param   out   The PrintStream to which debug output will
    * be printed. This can be System.out or any other PrintStream.
    *
    */
   public void enableTrace(PrintStream out)
   {
      m_traceOut = out;
      m_trace = true;
   }

   /**
    * @author   chadloder
    *
    * @version 1.0 1999/6/3
    *
    * Turns off debug tracing. If it was already off, it will stay off.
    * If it was already on, the reference to the print stream will be
    * discarded.
    */
   public void disableTrace()
   {
      m_trace = false;
      m_traceOut = null;
   }

   /**
    * @author   chadloder
    *
    * @version 1.0 1999/6/3
    *
    * If tracing is enabled, prints the string followed by a newline
    * to the debug stream. If tracing is not enabled, does nothing.
    *
    * @param   s   The string to be printed
    *
    */
   private void trace(String s)
   {
      if (m_trace)
         m_traceOut.println(s);
   }


   /**
    * Class to hold a column definition, possibly with a
    * foreign key column.
    */
   public class ColumnDef
   {
      public ColumnDef(String columnName)
      {
         this(columnName, null);
      }

      public ColumnDef(String columnName, ColumnDef foreignKey)
      {
         m_colName = columnName;

         // if the given foreign key is not null, then set our key to it
         if (foreignKey != null)
            setForeignKey(foreignKey);

         m_pKeys = new ArrayList();
         m_ordinal = 0;
      }

      public String getName()
      {
         return m_colName;
      }

      public boolean hasForeignKey()
      {
         return (null != m_fgnKey);
      }

      public ColumnDef getForeignKey()
      {
         return m_fgnKey;
      }

      public void setForeignKey(ColumnDef foreignKey)
      {
         m_fgnKey = foreignKey;
         m_fgnKey.isForeignKeyFor(this);
      }

      private void isForeignKeyFor(ColumnDef pKeyCol)
      {
         m_pKeys.add(pKeyCol);
      }

      public int getNumPkeyReferences()
      {
         return m_pKeys.size();
      }

      public ColumnDef getPkeyReference(int i)
      {
         return (ColumnDef)m_pKeys.get(i);
      }

      public void setTable(TableDef table)
      {
         m_table = table;
      }

      public TableDef getTable()
      {
         return m_table;
      }

      public int getOrdinal()
      {
         return m_ordinal;
      }

      public PSResultSetColumnMetaData toColumnMetaData()
      {
         PSResultSetColumnMetaData meta
            = new PSResultSetColumnMetaData(m_colName, java.sql.Types.VARCHAR, 4096);

         meta.setTableName(m_table.getName());

         return meta;
      }

      void setOrdinal(int ord)
      {
         m_ordinal = ord;
      }

      private String m_colName;
      private ColumnDef m_fgnKey; // could be null
      private TableDef m_table;
      private List m_pKeys;
      private int m_ordinal;
   } // end inner class ColumnDef


   /**
    * Class to hold a table definition
    *
    */
   public class TableDef
   {
      public TableDef(String tableName)
      {
         m_tableName = tableName;
         m_columns = new ArrayList();
         m_columnMap = new HashMap();
         m_uniqueColOrdinal = 0;

         try
         {
            m_backEndTable = new PSBackEndTable(tableName);
         }
         catch (IllegalArgumentException e)
         {
            throw new IllegalArgumentException(e.getLocalizedMessage());
         }
      }

      public String getName()
      {
         return m_tableName;
      }

      public void addColumn(ColumnDef col)
      {
         m_columns.add(col);
         col.setTable(this);
         col.setOrdinal(m_columns.size()); // 1-based
         m_columnMap.put(col.getName(), col);
      }

      /**
       * @author  chad loder
       *
       * @version 1.0 1999/6/3
       *
       * Inserts a unique ID column at ordinal 1, shifting
       * all other columns up there was no previous unique ID
       * column. If there was a previous unique ID column, it
       * will be discarded and replaced by this column.
       *
       * @param    col The unique ID column
       *
       */
      public void setUniqueIDColumn(ColumnDef col)
      {
         if (m_uniqueColOrdinal != 0)
         {
            m_columns.set(m_uniqueColOrdinal - 1, col);
         }
         else // no previous unique ID column
         {
            m_columns.add(0, col);
            m_columnMap.put(col.getName(), col);
            m_uniqueColOrdinal = 1;
         }

         col.setOrdinal(m_uniqueColOrdinal);

         // bump the other columns up by one
         for (int i = m_uniqueColOrdinal; i < m_columns.size(); i++)
         {
            ColumnDef bumpCol = (ColumnDef)m_columns.get(i);
            bumpCol.setOrdinal(bumpCol.getOrdinal() + 1);
         }

         col.setTable(this);
      }

      /**
       * @author   chad loder
       *
       * @version 1.0 1999/6/3
       *
       * Gets the unique ID column for this table.
       *
       * @return   ColumnDef The unique ID column, or null if none defined.
       */
      public ColumnDef getUniqueIDColumn()
      {
         if (m_uniqueColOrdinal != 0)
            return getColumn(m_uniqueColOrdinal);
         return null;
      }

      public ColumnDef getColumn(int ordinal /* 1-based */ )
      {
         return (ColumnDef)m_columns.get(ordinal - 1);
      }

      public ColumnDef getColumn(String colName)
      {
         return (ColumnDef)m_columnMap.get(colName);
      }

      public int getNumColumns()
      {
         return m_columns.size();
      }

      public PSBackEndColumn getBackEndColumn(String colName)
      {
         ColumnDef colDef = getColumn(colName);
         PSBackEndColumn backEndCol
            = new PSBackEndColumn(m_backEndTable, colDef.getName());
         return backEndCol;
      }

      public PSBackEndColumn getBackEndColumn(int ordinal /* 1-based */)
      {
         ColumnDef colDef = getColumn(ordinal);
         PSBackEndColumn backEndCol
            = new PSBackEndColumn(m_backEndTable, colDef.getName());
         return backEndCol;
      }

      /**
       * @author   chad loder
       *
       * @version 1.0 1999/6/4
       *
       * Initializes the given result set with the correct format
       * for this table. The result set will have the correct columns,
       * but no data.
       *
       * @param   rs   The result set to initialize.
       *
       */
      public void initResultSet(PSResultSet rs) throws SQLException
      {
         List[] rsCols = new List[m_columns.size()];
         for (int i = 0; i < rsCols.length; i++)
         {
            rsCols[i] = new ArrayList();
         }

         //--+trace("Setting result set with " + rsCols.length + " columns");

         rs.setResultData(rsCols, (HashMap)(m_columnMap.clone()));
         PSResultSetMetaData meta = new PSResultSetMetaData();
         initResultSetMetaData(meta);
         rs.setMetaData(meta);
      }

      /**
       * @author   chad loder
       *
       * @version 1.0 1999/6/10
       *
       * Returns true if this table depends on the given table in
       * some way. A table never depends on itself.
       *
       * @param   d
       *
       * @return   boolean
       */
      public boolean dependsOn(TableDef possDep)
      {
         // check the dependency tree for the left table and see
         for (int i = 1; i <= this.getNumColumns(); i++) // 1-based
         {
            ColumnDef col = this.getColumn(i);

            ColumnDef fKeyCol   = col.getForeignKey();
            if (fKeyCol == null)
               continue;

            TableDef parentTable = fKeyCol.getTable();
            if (parentTable.getName().equals(possDep.getName()))
               return true;

            // recurse if necessary
            boolean depends = this.dependsOn(parentTable);
            if (depends)
               return true;
         }

         return false;
      }

      public void initResultSetMetaData(PSResultSetMetaData meta)
         throws SQLException
      {
         for (int i = 1; i <= this.getNumColumns(); i++) // 1-based
         {
            ColumnDef col = getColumn(i);
            meta.addColumnMetaData(col.toColumnMetaData());
         }
      }

      private String m_tableName;
      private List m_columns;
      private int m_uniqueColOrdinal;
      private HashMap m_columnMap;
      private PSBackEndTable m_backEndTable;

   } // end inner class TableDef


     /**
      * @author chad loder
      *
      * @version     1.0 1999/6/13
      *
      * Private utility method to make a parent ID column name.
      * It is important that this column name will have no chance
      * of conflicting with an XML field or attribute.
      *
      * @param  baseColName
      *
      * @return String
      */
   private static String makeParentIdColName(String baseColName)
   {
      return "@@" + baseColName + "_parent_id";
   }

   /**
    * @author chad loder
    *
    * @version   1.0 1999/6/13
    *
    * Private utility method to make an ID column name for a column.
    * It is important that this column name will have no chance
    * of conflicting with an XML field or attribute.
    *
    * @param   baseColName
    *
    * @return String
    */
   private static String makeIdColName(String baseColName)
   {
      return "@@" + baseColName + "_id";
   }

   private PSDtdTree m_dtdTree;
   private Map m_tables;
   private List m_tablesInOrder;
   private boolean m_trace;
   private PrintStream m_traceOut;
   private String m_currentColumnPrefix;
   private Map m_parentNames;
}
