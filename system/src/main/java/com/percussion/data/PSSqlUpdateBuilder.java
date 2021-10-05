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

import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.error.PSIllegalArgumentException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.percussion.util.PSSqlHelper.isMysql;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;




/**
 * The PSSqlUpdateBuilder class is used to build SQL SELECT statements.
 * It can be used to generate single table SELECTs or homogeneous
 * (same DBMS) joined SELECTs. The query optimizer is capable of building
 * heterogeneous (cross DBMS) SELECTs. It calls this class to build
 * each statement and also makes use of the PSQueryJoiner class to join
 * the returned data.
 *
 * @see         PSQueryOptimizer
 * @see         PSQueryJoiner
 */
public class PSSqlUpdateBuilder extends PSSqlBuilder
{
   /**
    * Construct a SQL builder to build an UPDATE statement.
    *
    * @param   table      the table to build the UPDATE for
    */
   PSSqlUpdateBuilder(PSBackEndTable table)
      throws PSIllegalArgumentException
   {
      super();

      m_Tables = new ArrayList(1);   // only allowing 1 entry at this time
      addTable(table);

      m_Mappings = new HashMap();
      m_Columns = new ArrayList();
      m_Keys = new ArrayList();
   }

   /**
    * This should never be called externally as we only allow one table
    * at this time, which is the one passed in to the constructor.
    *
    * @param   table      the table to add to the UPDATE
    */
   void addTable(PSBackEndTable table)
      throws PSIllegalArgumentException
   {
      if (m_Tables.size() == 0) {
         m_Tables.add(table);
      }
      else if (!m_Tables.contains(table)) {
         PSBackEndTable curTab = (PSBackEndTable)m_Tables.get(0);
         Object[] args = { curTab.getAlias(), table.getAlias() };
         throw new PSIllegalArgumentException(
            IPSBackEndErrors.SQL_BUILDER_MOD_SINGLE_TAB_ONLY, args);
      }
   }

   /**
    * Add the specified columns to the updateable column list
    * (used in the SET clause).
    *
    * @param   col      the column to add to the SET clause
    */
   void addUpdateColumn(PSBackEndColumn col)
      throws PSIllegalArgumentException
   {
      // this is really a test to see if they're trying to use a col
      // not in the appropriate table
      addTable(col.getTable());

      if (!m_Columns.contains(col))
         m_Columns.add(col);
   }

   /**
    * Add a key column (used in the WHERE clause). All keys in UPDATEs
    * take the form: key1 = ? AND key2 = ? AND ...
    *
    * @param   col      the column to add to the WHERE clause
    */
   void addKeyColumn(PSBackEndColumn col)
      throws PSIllegalArgumentException
   {
      // this is really a test to see if they're trying to use a col
      // not in the appropriate table
      addTable(col.getTable());

      if (!m_Keys.contains(col))
         m_Keys.add(col);
   }

   /**
    * Add the PSDataMapping object which can be used to map a back-end column
    * to the XML field it will be read from.
    *
    * @param   map      the XML field -> back-end column mapping to add
    */
   void addColumnToXmlMapping(PSDataMapping map)
      throws PSIllegalArgumentException
   {
      IPSBackEndMapping col = map.getBackEndMapping();
      if (!(col instanceof com.percussion.design.objectstore.PSBackEndColumn))
      {   // UDF's are not yet supported on the SQL side of an UPDATE
         PSExtensionCall call = (PSExtensionCall)col;
         Object[] args = { call.getExtensionRef() };
         throw new PSIllegalArgumentException(
            IPSBackEndErrors.SQL_BUILDER_UDF_NOT_SUPPORTED_IN_MOD, args);
      }

      String colName = ((PSBackEndColumn)col).getColumn();
      if (m_Mappings.get(colName) == null)
         m_Mappings.put(colName, map);
   }

   /**
    * Generate the statement using the specified connection keys.
    *
     * @param logins a list of logins, one per connection index in the values
     * contained within <code>connKeys</code>, must never be <code>null</code>
     * 
     * @param connKeys a ConcurrentHashMap that associates opaque keys representing
     * a specific database and server, and indecies into the <code>logins</code>
     * list passed to this method, must never be <code>null</code>
    */
   PSUpdateStatement generate(java.util.List logins, ConcurrentHashMap connKeys)
      throws PSIllegalArgumentException
   {
     HashMap dtHash = new HashMap();

      int iConnKey = validateBuilderConnection(dtHash, connKeys, logins);

      return generateUpdate(dtHash, iConnKey, 
         (PSBackEndLogin)logins.get(iConnKey));
   }

   /**
    * Validate the builder and its connection, returning the connection
    * key and filling in the datatypes for the table member on success.
    * 
    * @param   dtHash   The hashmap to fill in with the data types for
    *                   the table which this builder is associated with.
    *                   Never <code>null</code>
    *
    * @param logins a list of logins, one per connection index in the values
    * contained within <code>connKeys</code>, must never be <code>null</code>
    * 
    * @param connKeys a ConcurrentHashMap that associates opaque keys representing
    * a specific database and server, and indecies into the <code>logins</code>
    * list passed to this method, must never be <code>null</code>
    *
    * @return  The valid connection key for this builder.
    *
    * @throws PSIllegalArgumentException  If the builder does not have
    *          one table defined, any argument is invalid or the connection
    *          key is undefined.
    */
   int validateBuilderConnection(HashMap dtHash, ConcurrentHashMap connKeys, List logins)
      throws PSIllegalArgumentException
   {
      int size = m_Tables.size();
      
      // this is not multi-table ready!!!
      if (size == 0) {
         throw new PSIllegalArgumentException(
            IPSBackEndErrors.SQL_BUILDER_MOD_TABLE_REQD);
      }
      else if (size > 1) {
         throw new PSIllegalArgumentException(
            IPSBackEndErrors.SQL_BUILDER_MOD_SINGLE_TAB_ONLY);
      }

      PSBackEndTable table = (PSBackEndTable)m_Tables.get(0);
        Object serverKey = table.getServerKey();
        Integer iConnKey = (Integer)connKeys.get(serverKey);
        if (iConnKey == null) {
           Object[] args = { serverKey };
           throw new PSIllegalArgumentException(
              IPSBackEndErrors.SQL_BUILDER_NO_CONN_DEFINED, args);
        }

      PSBackEndLogin login = (PSBackEndLogin)logins.get(iConnKey.intValue());

      /* get the data types for this table */
      loadDataTypes(login, dtHash, table);

      return iConnKey.intValue();
   }

   /**
    * Generate the UPDATE statement using the specified connection keys.
    *
    * @param   dtHash      the hash table containing datatypes mapped to 
    *                      column names
    *
    * @param   iConnKey    the connection key 
    *
    * @param   login       the back-end database login
    *
    * @return  The appropriate update statement.
    *
    * @throws  PSIllegalArgumentException if any called method fails and
    *          throws this exception.
    */
   PSUpdateStatement generateUpdate(HashMap dtHash, int iConnKey, 
      PSBackEndLogin login)
      throws PSIllegalArgumentException
   {
      PSSqlBuilderContext context = new PSSqlBuilderContext();

      /* there's only one table permitted per statement */
      context.addText("UPDATE ");

      PSBackEndTable table = (PSBackEndTable)m_Tables.get(0);

      buildTableName(login, context, table);

      // build the SET clause for this table
      buildSetClause(context, table, dtHash);

      // build the WHERE clauses for this table
      buildWhereClauseFromKeys(context, table, dtHash);

      // we're done building, so close the last run
      context.closeTextRun();

      try {
         return new PSUpdateStatement(
            iConnKey, context.getBlocks(),
            PSUpdateStatement.TYPE_UPDATE);
      } catch (PSDataExtractionException e) {
         throw new PSIllegalArgumentException(
            e.getErrorCode(), e.getErrorArguments());
      }
   }

   /**
    * Get the columns which will be used to locate records when executing
    * the associated statement.
    *
    * @return      an array of columns (may be null)
    */
   public PSBackEndColumn[] getLookupColumns()
   {
      /* go through the where clauses to see which columns are the
       * lookup keys
       */
      int size;
      if ((m_Keys != null) && ((size = m_Keys.size()) != 0))
      {
         java.util.ArrayList colArray = new java.util.ArrayList(size);

         for (int i = 0; i < size; i++)
            colArray.add(m_Keys.get(i));

         size = colArray.size();
         if (size != 0) {
            PSBackEndColumn[] ret = new PSBackEndColumn[size];
            colArray.toArray(ret);
            return ret;
         }
      }

      return null;
   }

   /**
    * Does this builder contain any updatable columns? If not, an update
    * statement cannot be generated for it.
    */
   public boolean hasUpdateColumns()
   {
      return (m_Columns.size() > 0);
   }

   /**
    * Does this builder contain any key columns? If not, updates and
    * deletes may produce undesirable results.
    */
   public boolean hasKeyColumns()
   {
      return (m_Keys.size() > 0);
   }

   protected void buildTableName(
      PSBackEndLogin login, PSSqlBuilderContext context,
      PSBackEndTable table)
   {
      String tableName = getTableName(login, table, false);
      context.addText(tableName);
   }

   /**
    * Build the SET clause for the UPDATE statement.
    *
    * @param   context         the builder context
    *
    * @param   table            the table the statement is being built for
    *
    * @param   datatypes      the data type hash map containing the 
    *                           table.column to data type mapping
    */
   protected void buildSetClause(
      PSSqlBuilderContext context, PSBackEndTable table, HashMap datatypes)
      throws PSIllegalArgumentException
   {
      int size = m_Columns.size();
      /* if there are no columns, this is an error */
      if (size == 0) {
         Object[] args = { table.getAlias() };
         throw new PSIllegalArgumentException(
            IPSBackEndErrors.SQL_BUILDER_UPDATABLE_COL_REQD, args);
      }

      context.addText(" SET ");
      // go through the columns and build the appropriate SET clause
      if (!buildColumnAndPlaceholderList(
         context, table, datatypes, m_Columns, ", ", true))
      {
         Object[] args = { table.getAlias() };
         throw new PSIllegalArgumentException(
            IPSBackEndErrors.SQL_BUILDER_UPDATABLE_COL_REQD, args);
      }
   }

   /**
    * Build the WHERE clause for the UPDATE or DELETE statement.
    *
    * @param   context         the builder context
    *
    * @param   table            the table the statement is being built for
    *
    * @param   datatypes      the data type hash map containing the 
    *                           table.column to data type mapping
    */
   protected void buildWhereClauseFromKeys(
      PSSqlBuilderContext context, PSBackEndTable table, HashMap datatypes)
      throws PSIllegalArgumentException
   {
      int size;

      /* now go through the where clauses */
      if (m_Keys != null) {
         size = m_Keys.size();
         if (size == 0) {
            /* updates without WHERE clauses tend to be dangerous, so
             * we'll warn about this, but not give an error as it may
             * be intentional.
             */
            Object[] args = { table.getAlias() };
            com.percussion.log.PSLogManager.write(
               new com.percussion.log.PSLogServerWarning(
               IPSBackEndErrors.SQL_BUILDER_UPD_OR_DEL_NO_WHERE,
               args, false, "SQLUpdateBuilder"));
            return;
         }

         context.addText(" WHERE ");

         // go through the columns and build the appropriate WHERE clause
         if (!buildColumnAndPlaceholderList(
            context, table, datatypes, m_Keys, " AND ", false))
         {
            /* updates without WHERE clauses tend to be dangerous, so
             * we'll warn about this, but not give an error as it may
             * be intentional.
             */
            Object[] args = { table.getAlias() };
            com.percussion.log.PSLogManager.write(
               new com.percussion.log.PSLogServerWarning(
               IPSBackEndErrors.SQL_BUILDER_UPD_OR_DEL_NO_WHERE,
               args, false, "SQLUpdateBuilder"));
         }
      }
   }

   /**
    * Convenience function for builders which do not support lob columns,
    * will call buildColumnAndPlaceholderList with null for the lob column
    * initializer.
    *
    * @see #buildColumnAndPlaceholderList
    */
   protected boolean buildColumnAndPlaceholderList(
      PSSqlBuilderContext context, PSBackEndTable table, HashMap datatypes,
      ArrayList columnList, String delimiter, boolean ignoreAutoIncrements)
      throws PSIllegalArgumentException
   {
      return buildColumnAndPlaceholderList(context, table, datatypes,
         columnList, delimiter, ignoreAutoIncrements, null);      
   }

   /**
    * Build the column and placeholder list based upon the specified
    * parameters. This can be used in an UPDATE statement's SET clause
    * or in a DELETE or UPDATE's WHERE clause.
    *
    * @param   context         the builder context
    *
    * @param   table            the table the statement is being built for
    *
    * @param   datatypes      the data type hash map containing the 
    *                           table.column to data type mapping
    *
    * @param   columnList      the columns to build into the statement
    *
    * @param   delimiter      the delimiter to use, such as ", " for SET
    *                           clauses or " AND " for WHERE clauses
    *
    * @param   ignoreAutoIncrements
    *                           <code>true</code> to omit columns which are
    *                           auto-incremented by the server
    *
    * @param   lci            the lob column initializer,
    *                         can be <code>null</code>.
    *
    * @return                  <code>true</code> if at least one column
    *                           was used; <code>false</code> otherwise
    */
   protected boolean buildColumnAndPlaceholderList(
      PSSqlBuilderContext context, PSBackEndTable table, HashMap datatypes,
      ArrayList columnList, String delimiter, boolean ignoreAutoIncrements,
      IPSLobColumnInitializer lci)
      throws PSIllegalArgumentException
   {
      int colCount = 0;   // keep track of the number of cols we set
      int size = columnList.size();
      for (int i = 0; i < size; i++) {
         IPSBackEndMapping beMap = (IPSBackEndMapping)columnList.get(i);
         if (!(beMap instanceof com.percussion.design.objectstore.PSBackEndColumn))
         {   // UDF's are not yet supported in UPDATEs
            PSExtensionCall call = (PSExtensionCall)beMap;
            Object[] args = { call.getExtensionRef() };
            throw new PSIllegalArgumentException(
               IPSBackEndErrors.SQL_BUILDER_UDF_NOT_SUPPORTED_IN_MOD, args);
         }

         PSBackEndColumn col = (PSBackEndColumn)beMap;
         if (!table.equals(col.getTable()))
            continue;

         String columnName = col.getColumn();
         PSDataMapping map = (PSDataMapping)m_Mappings.get(columnName);
         if (map == null) {
            Object[] args = { columnName };
            throw new PSIllegalArgumentException(
               IPSBackEndErrors.SQL_BUILDER_MOD_MAP_REQD, args);
         }

         /* if we're removing auto-increment columns, and this column
          * is one of them, skip it
          */
         if (ignoreAutoIncrements && isAutoIncrement(table, columnName))
            continue;


         colCount++;
         if (colCount > 1)
            context.addText(delimiter);

         context.addText(columnName);
         context.addText(" = ");

         IPSReplacementValue replValue
            = (IPSReplacementValue)map.getDocumentMapping();
         Integer jdbcType = (Integer)datatypes.get(columnName.toLowerCase());
         if (jdbcType != null)
            context.addReplacementField(replValue, jdbcType.intValue(), col, lci);
         else
            context.addReplacementField(replValue, PSStatementColumn.UNKNOWN_JDBC_TYPE, col);   // fix for bug id Rx-99-12-0001
      }

      return (colCount > 0);   // true if at least 1 col; false if 0
   }

   /**
    * Given a column name which may be in table.column or column format,
    * return just the column name.
    */
   protected String getStrippedColumnName(String columnName)
   {
      int pos = columnName.lastIndexOf('.');
      if (pos == -1)
         return columnName;

      return columnName.substring(pos+1);
   }

   /**
    * Get the table meta data object associated with this builder.
    * 
    * @param table the back-end table to get the meta data for, may not be
    * <code>null</code>.
    * 
    * @return the table meta data object, never <code>null</code>.
    * 
    * @throws SQLException if there are any errors.
    */
   protected PSTableMetaData getTableMetaData(PSBackEndTable table)
      throws SQLException
   {
      PSDatabaseMetaData dbMeta = PSOptimizer.getCachedDatabaseMetaData(table);
      return (dbMeta == null) ? null : dbMeta.getTableMetaData(
         table.getConnectionDetail().getOrigin(), table.getTable());
   }

   /**
    * Is this an auto increment column?
    *
    * @param   columnName      the name of the column to check
    *
    * @return                  <code>true</code> if it is
    */
   protected boolean isAutoIncrement(PSBackEndTable table, String columnName)
   {
      //Auto increment code does not work for MySQL
      String driver = table.getConnectionDetail().getDriver();
      if(isNotEmpty(driver) && isMysql(driver)){
          return false;
      }
      
      if (m_autoIncrCols == null) {
         try {
            PSTableMetaData meta = getTableMetaData(table);
            m_autoIncrCols = meta.getAutoUpdateColumns();
         } catch (java.sql.SQLException e) {
            // log this as we may generate bad INSERT/UPDATE statements
            Object[] args = { table.getDataSource(), "", "", 
               "getAutoUpdateColumns", PSSqlException.toString(e) };
            com.percussion.log.PSLogManager.write(
               new com.percussion.log.PSLogServerWarning(
               IPSBackEndErrors.LOAD_META_DATA_EXCEPTION,
               args, false, "SqlUpdateBuilder"));

            // chances are we'll fail again, so set it non-null which
            // prevents us from doing the potentially slow load of the
            // meta data (eg, when server is down we may wait a while)
            m_autoIncrCols = new String[0];
         }
      }

      boolean isAutoIncrement = false;
      for (int j = 0; j < m_autoIncrCols.length; j++) {
         if (columnName.equalsIgnoreCase(m_autoIncrCols[j])) {
            isAutoIncrement = true;
            break;
         }
      }
      return isAutoIncrement;
   }

   /**
    * Build a column list of the form col1, col2, ..., coln. Any columns
    * which are not updatable (eg, MS SQL identity columns) are not included
    * in the list.
    */
   protected void buildColumnList(
      PSSqlBuilderContext context, PSBackEndTable table, HashMap datatypes,
      boolean usePlaceHolder)
      throws PSIllegalArgumentException
   {
      buildColumnList(context, table, datatypes, usePlaceHolder, m_Columns);
   }

   /**
    * Build a column list of the form col1, col2, ..., coln. Any columns
    * which are not updatable (eg, MS SQL identity columns) are not included
    * in the list.  This method calls buildColumnList with a null lob 
    * column initializer, for builders who are not concerned with supporting
    * lob columns.
    */
   protected void buildColumnList(
      PSSqlBuilderContext context, PSBackEndTable table, HashMap datatypes,
      boolean usePlaceHolder, List columns)
      throws PSIllegalArgumentException
   {
      buildColumnList(context, table, datatypes, usePlaceHolder, columns, null);
   }

   /**
    * Build a column list of the form col1, col2, ..., coln. Any columns
    * which are not updatable (eg, MS SQL identity columns) are not included
    * in the list.
    */
   protected void buildColumnList(
      PSSqlBuilderContext context, PSBackEndTable table, HashMap datatypes,
      boolean usePlaceHolder, List columns, IPSLobColumnInitializer lci)
      throws PSIllegalArgumentException
   {
      int size = columns.size();
      int colCount = 0;
      for (int i = 0; i < size; i++) {
         IPSBackEndMapping beMap = (IPSBackEndMapping)columns.get(i);
         if (!(beMap instanceof com.percussion.design.objectstore.PSBackEndColumn))
         {   // UDF's are not yet supported in UPDATEs
            PSExtensionCall call = (PSExtensionCall)beMap;
            Object[] args = { call.getExtensionRef() };
            throw new PSIllegalArgumentException(
               IPSBackEndErrors.SQL_BUILDER_UDF_NOT_SUPPORTED_IN_MOD, args);
         }

         PSBackEndColumn col = (PSBackEndColumn)beMap;
         if (table.equals(col.getTable())) {
            String columnName = col.getColumn();
            PSDataMapping map = (PSDataMapping)m_Mappings.get(columnName);
            if (map == null) {
               Object[] args = { columnName };
               throw new PSIllegalArgumentException(
                  IPSBackEndErrors.SQL_BUILDER_MOD_MAP_REQD, args);
            }

            /* we're always removing auto-increment columns on insert.
             * if this is an auto-increment column, skip it
             */
            if (isAutoIncrement(table, columnName))
               continue;

            colCount++;
            if (colCount > 1)
               context.addText(", ");

            if (usePlaceHolder) {
               IPSReplacementValue replValue
                  = (IPSReplacementValue)map.getDocumentMapping();
               Integer jdbcType = (Integer)datatypes.get(columnName.toLowerCase());
               if (jdbcType != null)
                  context.addReplacementField(replValue, jdbcType.intValue(), col, lci);
               else
                  context.addReplacementField(replValue, PSStatementColumn.UNKNOWN_JDBC_TYPE, col);   // fix for bug id Rx-99-12-0001
            }
            else {
               context.addText(columnName);
            }
         }
      }
      // this can't happen as buildColumnList would have caught it, but...
      //Following code does not work with MySql
      String driver = table.getConnectionDetail().getDriver();
      if(isEmpty(driver) || !isMysql(driver)){
          if (colCount == 0) {
             Object[] args = { table.getAlias() };
             throw new PSIllegalArgumentException(
                IPSBackEndErrors.SQL_BUILDER_UPDATABLE_COL_REQD, args);
          }
      }
   }


   /**
    * Generate an INSERT statement using the specified connection keys.
    *
    * @param   dtHash      the hash table containing datatypes mapped to 
    *                      column names
    *
    * @param   iConnKey    the connection key 
    *
    * @param   login       the back-end database login
    *
    * @return  The appropriate insert statement.
    *
    * @throws  PSIllegalArgumentException if any called method fails and
    *          throws this exception.
    */
   PSUpdateStatement generateInsert(HashMap dtHash, int iConnKey, 
      PSBackEndLogin login)
      throws PSIllegalArgumentException
   {
      PSSqlBuilderContext context = new PSSqlBuilderContext();
      PSBackEndTable table = (PSBackEndTable)m_Tables.get(0);

      /* there's only one table here */
      context.addText("INSERT INTO ");

      buildTableName(login, context, table);

      /* build: (column list) VALUES (place holders) */
      context.addText(" (");

      // go through the columns and keys
      buildColumnList(context, table, dtHash, false /* use colname */);

      context.addText(") VALUES (");

      buildColumnList(context, table, dtHash, true /* use placeholder */);

      context.addText(")");

      // we're done building, so close the last run
      context.closeTextRun();

      try {
         return new PSUpdateStatement(
            iConnKey, context.getBlocks(),
            PSUpdateStatement.TYPE_INSERT);
      } catch (PSDataExtractionException e) {
         throw new PSIllegalArgumentException(
            e.getErrorCode(), e.getErrorArguments());
      }
   }

   /**
    * Generate the statement for UPDATE with INSERT using the specified
    * connection keys.
    *
    * @param   dtHash      the hash table containing datatypes mapped to 
    *                      column names
    *
    * @param   iConnKey    the connection key 
    *
    * @param   login       the back-end database login
    *
    * @return  The appropriate update with insert statement.
    *
    * @throws  PSIllegalArgumentException if any called method fails and
    *          throws this exception.
    */
   PSUpdateStatement generateUpdateInsert(HashMap dtHash, int iConnKey, 
      PSBackEndLogin login)
      throws PSIllegalArgumentException
   {
      PSSqlBuilderContext context = new PSSqlBuilderContext();
      PSSqlBuilderContext insertContext = new PSSqlBuilderContext();
      PSBackEndTable table = (PSBackEndTable)m_Tables.get(0);
         
      /* there's only one table permitted per statement */
      context.addText("UPDATE ");
      buildTableName(login, context, table);

      // build the SET clause for this table
      buildSetClause(context, table, dtHash);

      // build the WHERE clauses for this table
      buildWhereClauseFromKeys(context, table, dtHash);

      // we're done building, so close the last run
      context.closeTextRun();


      // also build the INSERT statement

      // for the INSERT statement, we need all key and update columns
      // in the column list
      ArrayList columnList = new ArrayList();
      columnList.addAll(m_Keys);
      columnList.addAll(m_Columns);

      insertContext.addText("INSERT INTO ");
      buildTableName(login, insertContext, table);
      insertContext.addText(" (");   // (column list) VALUES (place holders)
      buildColumnList(
         insertContext, table, dtHash, false, /* use colname */
         columnList);
      insertContext.addText(") VALUES (");
      buildColumnList(
         insertContext, table, dtHash, true, /* use placeholder */
         columnList);
      insertContext.addText(")");
      insertContext.closeTextRun();

      try {
         return new PSUpdateInsertStatement( iConnKey,
            context.getBlocks(), insertContext.getBlocks());
      } catch (PSDataExtractionException e) {
         throw new PSIllegalArgumentException(
            e.getErrorCode(), e.getErrorArguments());
      }
   }

   /**
    * the list of columns automatically updated by the back-end server.
    */
   protected String[]         m_autoIncrCols;

   /**
    * The back-end tables to build the statement(s) for.
    * Some day, we may use this to build statements across tables.
    * For now, we're using the array list, but allowing only one entry.
    */
   protected ArrayList         m_Tables;

   /**
    * The XML field - back-end column mappings (PSDataMapping).
    */
   protected HashMap            m_Mappings;

   /**
    * The PSBackEndColumn for each updatable column
    * (PSUpdateColumn.isUpdateable == true).
    */
   protected ArrayList         m_Columns;

   /**
    * the PSBackEndColumn for each key column to use in the WHERE
    * (PSUpdateColumn.isKey == true).
    */
   protected ArrayList         m_Keys;
}

