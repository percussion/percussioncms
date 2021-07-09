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

import com.percussion.utils.collections.PSIteratorUtils;
import com.percussion.util.PSSqlHelper;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Retrieves and caches table info from {@link java.sql.DatabaseMetaData}.
 * Class is immutable after ctor.
 */
public class PSJdbcTableMetaData
{
   /**
    * Retrieve and cache table meta data for the specified table.
    *
    * @param dmd Provides the table metadata, may not be <code>null</code>.
    * @param dbmsDef Provides the database/schema to retrieve meta data from.
    * May not be <code>null</code>.
    * @param dataTypeMap Used to create column objects.  May not be <code>
    * null</code>.
    * @param tableName The table to retrieve meta data for.  May not be <code>
    * null</code> or empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public PSJdbcTableMetaData(DatabaseMetaData dmd, PSJdbcDbmsDef dbmsDef,
      PSJdbcDataTypeMap dataTypeMap, String tableName)
         throws PSJdbcTableFactoryException
   {
      if (dmd == null)
         throw new IllegalArgumentException("dmd may not be null");

      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (dataTypeMap == null)
         throw new IllegalArgumentException("dataTypeMap may not be null");

      if (tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName may not be null or empty");

      try
      {
         m_dbmsDef = dbmsDef;
         m_dataTypeMap = dataTypeMap;

         m_database = m_dbmsDef.getDataBase();

         if (m_database != null && m_database.trim().length() == 0)
            m_database = null;
         m_schema = m_dbmsDef.getSchema();
         if (m_schema != null && m_schema.trim().length() == 0)
            m_schema = null;

         if(m_schema == null){
            if(dbmsDef.getConnectionDetail()!= null && dbmsDef.getConnectionDetail().getOrigin()!=null ){
               m_schema = dbmsDef.getConnectionDetail().getOrigin();
            }
         }

         if(m_database == null){
            if(dbmsDef.getConnectionDetail()!=null && dbmsDef.getConnectionDetail().getDatabase()!=null){
               m_database = dbmsDef.getConnectionDetail().getDatabase();
            }
         }
         m_tableName = tableName;

         if (dmd.storesLowerCaseIdentifiers())
         {
            m_tableName = m_tableName.toLowerCase();
            if (m_schema != null)
               m_schema = m_schema.toLowerCase();
            if (m_database != null)
               m_database = m_database.toLowerCase();
         }
         else if (dmd.storesUpperCaseIdentifiers())
         {
            m_tableName = m_tableName.toUpperCase();
            if (m_schema != null)
               m_schema = m_schema.toUpperCase();
            if (m_database != null)
               m_database = m_database.toUpperCase();
         }

         checkTableExists(dmd);
         if (exists())
         {
            checkIsView(dmd);
            loadColumnInformation(dmd);
            if (!isView())
            {
               // there is no need to obtain primary key, foreign key and index
               // information for views as views do not support these keys and
               // indexes. Trying to get index information throws this
               // exception on Oracle :
               // ORA-01702: a view is not appropriate here
               loadKeyInformation(dmd);
               loadIndexInformation(dmd);
            }
         }
      }
      catch (SQLException e)
      {
         Object[] args = {tableName,
            PSJdbcTableFactoryException.formatSqlException(e)};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.SQL_TABLE_META_DATA, args, e);
      }
   }

   /**
    * @return <code>true</code> if the table exists, <code>false</code> if not.
    */
   public boolean exists()
   {
      return m_tableExists;
   }

   /**
    * Gets a list of all columns in this table.
    *
    * @return Iterator over zero or more PSJdbcColumnDef objects.  Never <code>
    * null</code>, may be empty.  The action of each column is set to {@link
    * PSJdbcTableComponent#ACTION_NONE}.
    */
   public Iterator getColumns()
   {
      return m_columns.iterator();
   }

   /**
    * Gets a list of all the primary key columns for this table's primary key.
    *
    * @return Iterator over zero or more column names, never <code>null</code>,
    * may be empty.
    */
   public Iterator getPrimaryKeyColumns()
   {
      return m_primaryKeyColumns.iterator();
   }


   /**
    * Gets the identifier of the primary key constraint, if it was discovered
    * in the metadata.
    *
    * @return the name of the primary key constraint, or <code>null</code> if
    * the name is not known.
    */
   public String getPrimaryKeyName()
   {
      return m_primaryKeyName;
   }


   /**
    * Gets a list of all the foreign key columns for this table.
    *
    * @return A Map of Foreign Key Column names against a List of
    * a String[] with 3 entries, the column name, the external table name, and
    * the external column name respectively, all not <code>null</code> or empty.
    */
   public Map<String, List<String[]>> getForeignKeys()
   {
      return m_foreignKeyColumns;
   }


   /**
    * Convenience method that calls
    * {@link #getIndexes(int) getIndexes(PSJdbcIndex.TYPE_UNIQUE)}
    */
   public Iterator getIndexes()
   {
      return getIndexes(PSJdbcIndex.TYPE_UNIQUE);
   }

   /**
    * Returns an iterator containing the indexes of the specified type.
    *
    * @param type the type of indexes to include in the returned iterator,
    * should be one of <code>PSJdbcIndex.TYPE_XXX</code> or
    * multiple <code>PSJdbcIndex.TYPE_XXX</code> values OR'ed together.
    *
    * @return an iterator over zero or more <code>Map.Entry</code> objects
    * containing the index name (<code>String</code>) as key and a
    * <code>List</code> of column names (<code>String</code>) as value.
    * Never <code>null</code>, may be empty.
    */
   public Iterator getIndexes(int type)
   {
      Map indexes = new HashMap();
      Iterator it = m_indexes.iterator();
      while (it.hasNext())
      {
         PSJdbcIndex index = (PSJdbcIndex)it.next();
         if (index.isOfType(type))
            indexes.put(index.getName(),
               PSIteratorUtils.cloneList(index.getColumnNames()));
      }
      return indexes.entrySet().iterator();
   }

   /**
    * Returns an iterator over a list containing the indexes of the specified
    * type.
    *
    * @param type the type of indexes to include in the returned iterator,
    * should be one of <code>PSJdbcIndex.TYPE_XXX</code> or
    * multiple <code>PSJdbcIndex.TYPE_XXX</code> values OR'ed together.
    *
    * @return the Iterator over a list of <code>PSJdbcIndex</code> objects,
    * never <code>null</code>, may be empty.
    */
   public Iterator getIndexObjects(int type)
   {
      List list = new ArrayList();
      Iterator it = m_indexes.iterator();
      while (it.hasNext())
      {
         PSJdbcIndex index = (PSJdbcIndex)it.next();
         if (index.isOfType(type))
            list.add(index);
      }
      return list.iterator();
   }

   /**
    * @return <code>true</code> if the table represented by this meta data is a
    * "VIEW", <code>false</code> if not.
    */
   public boolean isView()
   {
      return m_isView;
   }

   /**
    * Private utility method to check if the table for which the metadata is
    * being created is a "VIEW".
    * @param md The meta data to use, assumed not <code>null</code>.
    * @throws SQLException if a database access error occurs
    */
   private void checkIsView(DatabaseMetaData md)
      throws SQLException
   {
      ResultSet rs = null;
      PSJdbcResultSetIteratorStep step = null;
      try
      {
         String driver = m_dbmsDef.getDriver();
         String sql = PSSqlHelper.getMetaDataQuery(driver, m_database, m_schema,
            m_tableName, true, false);
         if (sql != null)
         {
            Connection conn = md.getConnection();
            step = PSJdbcStatementFactory.getQueryStatement(m_dbmsDef, sql);
            step.execute(conn);
            if (step.hasNext())
               m_isView = true;
            else
               m_isView = false;
         }
         else
         {
            String[] types = new String[]{"VIEW"};
            rs = md.getTables(m_database, m_schema, m_tableName, types);

            // driver can return a null ResultSet even though the API doc implies
            // that they should not (for example, Informix)
            if (rs != null)
               m_isView = rs.next();
         }
      }
      finally
      {
         if (rs != null)
         {
            try
            {
               rs.close();
            }
            catch (SQLException e)
            {
               /* ignore */
            }
         }
         if (step != null)
            step.close();
      }
   }

   /**
    * Private utility method to check for table's existance.
    *
    * @param md The meta data to use, assumed not <code>null</code>.
    */
   private void checkTableExists(DatabaseMetaData md)
      throws SQLException
   {
      ResultSet rs = null;
      PSJdbcResultSetIteratorStep step = null;
      try
      {
         String driver = m_dbmsDef.getDriver();
         String sql = PSSqlHelper.getMetaDataQuery(driver, m_database, m_schema,
            m_tableName, false, false);
         if (sql != null)
         {
            Connection conn = md.getConnection();
            step = PSJdbcStatementFactory.getQueryStatement(m_dbmsDef, sql);
            step.execute(conn);
            if (step.hasNext())
               m_tableExists = true;
            else
               m_tableExists = false;
         }
         else
         {
            rs = md.getTables(m_database, m_schema, m_tableName, null);

            // driver can return a null ResultSet even though the API doc implies
            // that they should not (for example, Informix)
            if (rs != null)
               m_tableExists = rs.next();
         }
      }
      finally
      {
         if (rs != null)
         {
            try
            {
               rs.close();
            }
            catch (SQLException e)
            {
               /* ignore */
            }
         }
         if (step != null)
            step.close();
      }
   }

   /**
    * Private utility method to load the column information for this table from
    * the specified resultset meta data.
    *
    * @param meta The resultset meta data to use, assumed not <code>null</code>.
    *
    * @throws SQLException if there are any errors.
    */
   private void loadColumnInformation(ResultSetMetaData meta)
      throws SQLException
   {
      String driver = m_dbmsDef.getDriver();
      try
      {
         for (int i = 1; i <= meta.getColumnCount(); i++)
         {
            String colName = meta.getColumnName(i);

            int jdbcType = meta.getColumnType(i);
            String nativeType = meta.getColumnTypeName(i);

            jdbcType = PSSqlHelper.convertNativeDataType(
               new Short("" + jdbcType).shortValue(), nativeType, driver);
            String size = null;
            int sizeInt = meta.getColumnDisplaySize(i);
            if (m_dbmsDef.getDriver().equals("db2") && jdbcType == Types.BLOB)
            {
               size = String.valueOf(Integer.toUnsignedLong(sizeInt) / 2);
            }
            else
            {
               size = String.valueOf(sizeInt);
            }

            String scale = String.valueOf(meta.getScale(i));
            jdbcType = m_dataTypeMap.getJdbcType(nativeType, size, scale, 
               jdbcType);

            String defaultValue = null;
            boolean allowsNull = false;
            if (meta.isNullable(i) == ResultSetMetaData.columnNullable)
               allowsNull = true;

            size = convertSize(jdbcType, size, driver);
            scale = convertScale(jdbcType, scale);

            PSJdbcColumnDef col = new PSJdbcColumnDef(m_dataTypeMap, colName,
               PSJdbcTableComponent.ACTION_CREATE, jdbcType, size, scale,
               allowsNull, defaultValue);

            col.setNativeType(nativeType);
            m_columns.add(col);
         }
      }
      catch (PSJdbcTableFactoryException ex)
      {
         throw new SQLException(ex.getLocalizedMessage());
      }
   }

   /**
    * Private utility method to load the column information for this table
    *
    * @param md The meta data to use, assumed not <code>null</code>.
    *
    * @throws SQLException if there are any errors.
    */
   private void loadColumnInformation(DatabaseMetaData md)
      throws SQLException
   {
      ResultSet rs = null;
      PSJdbcResultSetIteratorStep step = null;

      try
      {
         String driver = m_dbmsDef.getDriver();
         String sql = PSSqlHelper.getMetaDataQuery(driver, m_database, m_schema,
            m_tableName, isView(), true);

         if (sql != null)
         {
            Connection conn = md.getConnection();
            step = PSJdbcStatementFactory.getQueryStatement(m_dbmsDef, sql);
            step.execute(conn);
            ResultSetMetaData meta = step.getMetaData();
            if (meta != null)
               loadColumnInformation(meta);
         }
         else
         {
            rs = md.getColumns(m_database, m_schema, m_tableName, "%");
            // driver can return a null ResultSet even though the API doc implies
            // that they should not (for example, Informix)
            if (rs != null)
            {
               while (rs.next())
               {
                  Short driverType = new Short(rs.getShort(5));
                  String typeName = rs.getString(6);
                  Short convertedType = new Short(
                     PSSqlHelper.convertNativeDataType(
                        driverType.shortValue(), typeName, driver));
                  int type = convertedType.intValue();

                  String nativeType = rs.getString(6);
                  String size = String.valueOf(rs.getInt(7));
                  String scale = String.valueOf(rs.getInt(9));                  
                  type = m_dataTypeMap.getJdbcType(nativeType, size, scale, 
                     type);

                  size = convertSize(type, size, driver);
                  scale = convertScale(type, scale);

                  String strNulls = rs.getString(18);
                  boolean allowNulls = !strNulls.trim().equalsIgnoreCase("NO");

                  PSJdbcColumnDef col = new PSJdbcColumnDef(m_dataTypeMap,
                     rs.getString(4), PSJdbcTableComponent.ACTION_CREATE,
                     type, size, scale, allowNulls, rs.getString(12));
                  col.setNativeType(nativeType);

                  m_columns.add(col);
               }
            }
         }
      }
      catch (PSJdbcTableFactoryException ex)
      {
         throw new SQLException(ex.getLocalizedMessage());
      }
      finally
      {
         if (rs != null)
         {
            try
            {
               rs.close();
            }
            catch (SQLException e)
            {
               /* ignore */
            }
         }
         if (step != null)
            step.close();
      }
   }

   /**
    * Converts <code>size</code> to an appropriate value to store as a
    * column definition's size.
    *
    * @param type The jdbc datatype returned by the metadata
    * @param size The size returned by the metadata
    * @param driver the driver name never <code>null</code>
    * @return The value to store for size, either a string representation of
    * <code>size</code>, or <code>null</code> if size is not supported for this
    * type, if the supplied size equals the default size, or if no mapping was 
    * found for the specified <code>type</code>. Never empty.
    * 
    * Note: Conversion occurs only if the default size is greater than 1
    */
   private String convertSize(int type, String size, String driver)
   {
      // see if the data type map has defined a default size for this type
      PSJdbcDataTypeMapping dataType = m_dataTypeMap.getMapping( type );
      if (dataType == null || dataType.getDefaultSize() == null)
         return null;
      return size;
   }


   /**
    * Converts <code>scale</code> to an appropriate value to store as a
    * column definition's scale.
    *
    * @param type The jdbc datatype returned by the metadata
    * @param scale The scale returned by the metadata
    *
    * @return The value to store for scale, either a string representation of
    * <code>scale</code>, or <code>null</code> if scale is not supported for
    * this type, if the supplied scale equals the default scale, or if no 
    * mapping was found for the supplied <code>type</code>. Never empty.
    */
   private String convertScale(int type, String scale)
   {
      // see if the data type map has defined a default scale for this type
      PSJdbcDataTypeMapping dataType = m_dataTypeMap.getMapping( type );
      if (dataType == null || dataType.getDefaultScale() == null)
      {
         scale = null;
      }  
      
      return scale;
   }


   /**
    * Private utility method to load the key information for this table
    *
    * @param md The meta data to use, assumed not <code>null</code>.
    *
    * @throws SQLException if there are any errors.
    */
   private void loadKeyInformation(DatabaseMetaData md) throws SQLException
   {
      ResultSet rs = null;
      try
      {
         rs = md.getPrimaryKeys(m_database, m_schema, m_tableName);
         // driver can return a null ResultSet even though the API doc implies
         // that they should not (for example, Informix)
         if (rs != null)
         {
            // build a tree map so we can sort by sequence
            Map keys = new TreeMap();  // use compareTo() of the key object
            String keyName = null;
            while (rs.next())
            {
               String colName = rs.getString(4);
               Short index = new Short(rs.getShort(5));
               keys.put(index, colName);

               // see if we have the name of the primary key
               keyName = rs.getString(6);
               if (keyName != null)
                  m_primaryKeyName = keyName;
            }

            m_primaryKeyColumns.addAll(keys.values());
         }
      }
      finally
      {
         if (rs != null)
         {
            try { rs.close(); } catch (SQLException e) { /* ignore */ }
         }
      }

      rs = null;
      try {
         rs = md.getImportedKeys(m_database, m_schema, m_tableName);
         // driver can return a null ResultSet even though the API doc implies
         // that they should not (for example, Informix)
         if (rs != null)
         {
            while (rs.next())
            {
               String[] colDef = new String[3];
               colDef[0] = rs.getString(8);
               colDef[1] = rs.getString(3);
               colDef[2] = rs.getString(4);
               
               
               
               String fkName = rs.getString(12);
               if (fkName!=null && fkName.trim().length() > 0) {
               List<String[]> keyCols = m_foreignKeyColumns.get(fkName);
               if (keyCols==null) {
                  keyCols = new ArrayList<>();
                  m_foreignKeyColumns.put(fkName, keyCols);
               }
               keyCols.add(colDef);
               }
            }
         }
      }
      finally
      {
         if (rs != null)
         {
            try { rs.close(); } catch (SQLException e) { /* ignore */ }
         }
      }
   }

   /**
    * Returns FK name. 
    * @return FK name, may be <code>null</code>, never empty.
    */
   public String getFKName()
   {
      return m_fkName;
   }
   
   /**
    * Private utility method to load the unique index information for this
    * table.  Must be called after {@link #loadKeyInformation(DatabaseMetaData)
    * loadKeyInformation} to avoid listing the primary key as an index as well.
    *
    * @param md The meta data to use, assumed not <code>null</code>.
    *
    * @throws SQLException if there are any errors.
    */
   private void loadIndexInformation(DatabaseMetaData md) throws SQLException
   {
      ResultSet rs = null;
      m_indexes.clear();

      // Map containing the unique index name as key and the List of columns
      // as value
      Map uniqueIndexMap = new HashMap();

      // Map containing the non-unique index name as key and the List of
      // columns as value
      Map nonUniqueIndexMap = new HashMap();

      try
      {
         // ask for all (unique and non-unique) indexes
         rs = md.getIndexInfo(m_database, m_schema, m_tableName, false, true);
         if (rs != null)
         {
            while (rs.next())
            {
               boolean unique = !rs.getBoolean(4); // NON_UNIQUE column
               String name = rs.getString(6);
               String colName = rs.getString(9);

               // jdbc may return a bogus entry, so skip it.
               if (name == null || colName == null)
                  continue;

               // will get the primary key columns too, so skip them
               if (m_primaryKeyName != null && m_primaryKeyName.equals(name))
                  continue;

               // check if this is a "backing index".
               // -skip them if they are for Primary or Foreign keys.
               // -include them if they are Unique keys, but change their
               //  name to match what the was given in the SQL statement
               //  which created them.
               StringBuffer nameBuffer = new StringBuffer(name);
               if (PSSqlHelper.handleBackingIndex(nameBuffer, md))
                  continue;
               name = nameBuffer.toString(); // update name of index
               
               Map whichMap = unique ? uniqueIndexMap : nonUniqueIndexMap;
               List colList = (List)whichMap.get(name);
               if (colList == null)
               {
                  colList = new ArrayList();
                  whichMap.put(name, colList);
               }
               colList.add(colName);
            }
         }
         m_indexes.addAll(getIndexes(uniqueIndexMap, PSJdbcIndex.TYPE_UNIQUE));
         m_indexes.addAll(getIndexes(nonUniqueIndexMap,
            PSJdbcIndex.TYPE_NON_UNIQUE));
      }
      catch(PSJdbcTableFactoryException ex)
      {
         throw new SQLException(ex.getLocalizedMessage());
      }
      finally
      {
         if (rs != null)
         {
            try { rs.close(); } catch (SQLException e) { /* ignore */ }
         }
      }
   }

   /**
    * Returns a list of index definitions created from the specified map
    * containing the index name and columns.
    *
    * @param indexMap the map containing the index name (<code>String</code>)
    * as key and a list (<code>List</code>) of column names
    * (<code>String</code>) as value, assumed not-<code>null</code>, may be
    * empty
    *
    * @param type indicates the type of index, should be one of
    * <code>PSJdbcIndex.TYPE_XXX</code> value.
    *
    * @return the list of index definitions, never <code>null</code>, may be
    * empty
    *
    * @throws PSJdbcTableFactoryException if any value in the specified map
    * contains any <code>null</code>, empty or duplicate column names, or if
    * there are any other errors.
    */
   private List getIndexes(Map indexMap, int type)
      throws PSJdbcTableFactoryException
   {
      List list = new ArrayList();
      Iterator it = indexMap.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry item = (Map.Entry)it.next();
         String name = (String)item.getKey();
         List columns = (List)item.getValue();
         PSJdbcIndex index = new PSJdbcIndex(name, columns.iterator(),
            PSJdbcTableComponent.ACTION_NONE, type);
         list.add(index);
      }
      return list;
   }

   /**
    * A list (in key order) of the primary key columns, initialized during
    * ctor, never <code>null</code> or modified after that.  May be empty.
    * Column name is stored as a String.
    */
   private List m_primaryKeyColumns = new ArrayList();

   /**
    * The name of this table's primary key if available during ctor, may be
    * <code>null</code>.
    */
   private String m_primaryKeyName;

   /**
    * A list of the foreignkey columns, initialized during
    * ctor, never <code>null</code> or modified after that.  May be empty.
    * <br>
    * Each entry is a String[] with 3 entries,
    * the column name, the external table name, and the external column name
    * respectively.  Each entry in the list is not <code>null</code>.
    */
   private Map<String,List<String[]>> m_foreignKeyColumns = new HashMap<String,List<String[]>>();

   /**
    * A List of PSJdbcColumnDef objects, initialized during ctor, never
    * <code>null</code>, may be empty.
    */
   private List m_columns = new ArrayList();

   /**
    * A list of index definitions (<code>PSJdbcIndex</code> objects).
    * These index definitions are obtained from the database for this table and
    * so always have a valid and unique name.
    * Never <code>null</code>, may be empty.
    */
   private List m_indexes = new ArrayList();

   /**
    * The table to retrieve data for, initialized in the ctor, never <code>null
    * </code>, empty or modified after that.  Normalized for the specified
    * backend database.
    */
   private String m_tableName;

   /**
    * The database to retrieve data from, initialized in the ctor, may be
    * <code>null</code>, never empty or modified after that. Normalized for the
    * specified backend database.
    */
   private String m_database;

   /**
    * The schema to retrieve data from, initialized in the ctor, may be
    * <code>null</code>, never empty or modified after that. Normalized for the
    * specified backend database.
    */
   private String m_schema;

   /**
    * The server to retrieve data from, initialized in the ctor, never <code>
    * null</code> or modified after that.
    */
   private PSJdbcDbmsDef m_dbmsDef;

   /**
    * Used to create column objects, initialized in the ctor, never <code>
    * null</code> or modified after that.
    */
   private PSJdbcDataTypeMap m_dataTypeMap;

   /**
    * <code>true</code> if the table exists, <code>false</code> if not.  Set
    * in the ctor, never modified after that.
    */
   private boolean m_tableExists = false;

   /**
    * <code>true</code> if the table represented by this meta data is a
    * "VIEW", <code>false</code> if not.  Set in the ctor, never modified
    * after that.
    */
   private boolean m_isView = false;
   
   /**
    * FK name if any. may be <code>null</code>.
    */
   private String m_fkName = null;

}
