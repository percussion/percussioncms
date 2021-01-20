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

package com.percussion.data;

import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.log.PSLogServerWarning;
import com.percussion.server.PSServer;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.tools.PSPatternMatcher;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The PSTableMetaData class offers a more tractable interface to
 * table meta data than does java.sql.DatabaseMetaData. Results
 * are cached and put into a readily usable form.
 *
 * A PSDatabaseMetaData object may use JDBC resources during construction
 * or method invocation, but it does not keep any JDBC resources open
 * after the method returns, and caches meta data wherever possible.
 */
public class PSTableMetaData implements IPSConnectionInfo
{
   /**
    * Construct a new table meta data object from the given database meta data.
    * @param login The login to use to obtain connection details, may be 
    * <code>null</code> if using the default connection.
    * @param schema The schema in which the table resides, may be 
    * <code>null</code> or empty.
    * @param tableName The name of the table, may not be <code>null</code> or 
    * empty.
    * @param dmd The database meta data for the supplied login, may not be
    * <code>null</code>. 
    * 
    * @throws SQLException If there is an error obtaining connection details.
    * @throws NamingException If there is an error resolving the login to a
    * datasource.
    */
   public PSTableMetaData(PSBackEndLogin login,
      String schema, String tableName, DatabaseMetaData dmd)
      throws SQLException, NamingException
   {
      if (dmd == null)
         throw new IllegalArgumentException("dmd may not be null");
      
      m_login = login;
      m_connDetail = PSMetaDataCache.getConnectionDetail(login);
      m_driver = m_connDetail.getDriver();
      m_catalog = m_connDetail.getDatabase();
      m_dataSource = m_login == null ? null : m_login.getDataSource();
      m_schema = schema;
      m_tableName = tableName;
      m_columns = new ArrayList();
      m_patMat = new PSPatternMatcher('_', '%', "%");
      m_primaryKeyColumns = new ArrayList();
      m_foreignKeyColumns = new ArrayList();

      // now load the data (columns, pkeys, fkeys, indices)
      loadColumnInformation(dmd);
      loadKeyInformation(dmd);
      loadStatsAndIndexInformation(dmd);
   }

   /**
    * @author chadloder
    *
    * Converts this object to a PSBackEndTable
    *
    * @return   PSBackEndTable
    *
    * @since 1.1 1999/4/30
    *
    */
   PSBackEndTable toBackEndTable() throws SQLException
   {
      PSBackEndTable ret = null;
      try
      {
         ret = new PSBackEndTable(m_tableName);
         ret.setDataSource(m_dataSource);
         ret.setConnectionDetail(m_connDetail);
         ret.setTable(m_tableName);
      }
      catch (IllegalArgumentException e)
      {
         throw new SQLException(e.getLocalizedMessage());
      }
      return ret;
   }

   /**
    * @author chadloder
    *
    * Returns A list of the names of all columns defined in this table, sorted in
    * ascending column name order.
    *
    * @return String[] A list of column names
    *
    * @since 1.1 1999/4/30
    *
    */
   public String[] getColumns()
   {
      return getColumns("%");
   }

   /**
    * @author chadloder
    *
    * Gets a list of the names of all columns defined in this table whose name
    * matches the given pattern, sorted in ascending column name order.
    *
    * @param columnNamePattern The pattern to match
    *
    * @return String[] A list of column names
    *
    * @since 1.1 1999/4/30
    *
    */
   public String[] getColumns(String columnNamePattern)
   {
      List cols = new ArrayList(m_columns.size());
      for (Iterator i = m_columns.iterator(); i.hasNext(); )
      {
         ColumnInfo colInfo = (ColumnInfo)i.next();
         if (m_patMat.doesMatchPattern(columnNamePattern, colInfo.m_name))
         {
            cols.add(colInfo.m_name);
         }
      }

      String[] ret = new String[cols.size()];
      if (ret.length > 0)
         cols.toArray(ret);
      return ret;
   }

   /**
    * @author chadloder
    *
    * Gets a list of all the primary key columns for this table's primary key,
    * in column order.
    *
    * @return String[]
    *
    * @since 1.1 1999/4/30
    *
    */
   public String[] getPrimaryKeyColumns()
   {
      String[] ret = new String[m_primaryKeyColumns.size()];
      if (ret.length > 0)
         m_primaryKeyColumns.toArray(ret);
      return ret;
   }

   /**
    * Gets a list of all the foreign key columns for this table. Foreign
    * keys refer to another table's column(s) (often the primary key).
    *
    * @return      the array of table.column names
    */
   public String[] getForeignKeyColumns()
   {
      String[] ret = new String[m_foreignKeyColumns.size()];
      if (ret.length > 0)
         m_foreignKeyColumns.toArray(ret);
      return ret;
   }

   /**
    * Gets a list of all the columns which are automatically inserted or
    * updated by the database server. These columns should not be included
    * in insert or update statements.
    *
    * @return the array of column names, never <code>null</code>, may be empty.
    */
   public String[] getAutoUpdateColumns()
   {
      // may need to aquire this info lazily
      if (m_autoUpdateColumns == null)
      {
         m_autoUpdateColumns = new ArrayList();

         if (PSSqlHelper.supportsIdentityColumns(m_driver))
         {
            ResultSet rs = null;
            Connection conn = null;
            try
            {
               conn = getConnection();

               DatabaseMetaData md = conn.getMetaData();

               rs = md.getVersionColumns(m_catalog, m_schema, m_tableName);
               // driver can return a null ResultSet even though the API doc
               // implies that they should not (for example, Informix)
               if (rs != null)
               {
                  while (rs.next()) {
                     String colName = rs.getString(1);
                     m_autoUpdateColumns.add(colName);
                  }
               }
            }
            catch (SQLException e)
            {
               // this may simply be unsupported, so let's not die over it
               // log this!
               Object[] args = { "SQL access",
                        PSSqlException.toString(e) };
               com.percussion.log.PSLogManager.write(
                     new PSLogServerWarning(
                     IPSBackEndErrors.DATABASE_ACCESS_ERROR, args,
                     false, "PSTableMetaData"));
            }
            finally
            {
               if (rs != null)
               {
                  try { rs.close(); } catch (SQLException e) { /* ignore */ }
               }

               if (conn != null)
               {
                  try { conn.close(); } catch (SQLException e) { /* ignore */ }
               }
            }

         }
      }

      // convert to array and return
      String[] ret = new String[m_autoUpdateColumns.size()];
      if (ret.length > 0)
         m_autoUpdateColumns.toArray(ret);
      return ret;
   }

   /**
    * @author chadloder
    *
    * Gets the name of the primary key. May be null.
    *
    * @return String The name of the primary key for this table.
    *
    * @since 1.1 1999/4/30
    *
    */
   public String primaryKeyName()
   {
      return m_primaryKeyName;
   }

   /**
    * @author chadloder
    *
    * Tells whether the given column allows null values or not
    *
    * @param   columnName   The column whose nullability is to be checked
    *
    * @return boolean true if this column allows nulls (or if we can't tell),
    * false if this column does not allow nulls.
    *
    * @since 1.1 1999/4/30
    *
    */
   public boolean isNullable(String columnName) throws SQLException
   {
      int colIdx = Collections.binarySearch(m_columns, columnName);
      if (colIdx < 0)
         throw new SQLException("no such column " + columnName);

      ColumnInfo info = (ColumnInfo)m_columns.get(colIdx);
      if (info.m_nullable == DatabaseMetaData.columnNoNulls)
         return false;
      return true;
   }

   /**
    * @author chadloder
    *
    * Gets the ordinal position (1-based) of the column with the given name, or -1
    * if this table has no column with that name.
    *
    * @param   columnName   The name of the column to return
    *
    * @return int The ordinal position of the column, or -1.
    *
    * @since 1.1 1999/4/30
    *
    */
   public int columnIndex(String columnName) throws SQLException
   {
      int colIdx = Collections.binarySearch(m_columns, columnName);
      if (colIdx < 0)
         return -1;
      ColumnInfo info = (ColumnInfo)m_columns.get(colIdx);
      return info.m_ordinalPosition;
   }



   /**
    * Gets the java.sql data type of the column with the given name. However,
    * it goes further than just returning what the driver reports. For example,
    * if the driver reported type <code>OTHER</code> (as Oracle does for LOB),
    * we would look in our local map and try to determine it's underlying type
    * (in the case of clob, return <code>CLOB</code> instead of <code>OTHER
    * </code>.
    *
    * @param columnName The database column name for which you want the data
    *    type. The columnName search is done case-insensitive. Throws exception
    *    if <code>null</code> or empty or doesn't match any existing column.
    *
    * @return The best JDBC type that we can determine.
    *
    * @throws SQLException If columnName is <code>null</code>, empty, or the
    *    name doesn't match any of the columns of this table.
    *
    * @since 1.1 1999/4/30
    */
   public short getColumnType(String columnName) throws SQLException
   {
      if ( null == columnName || columnName.trim().length() == 0 )
         throw new SQLException("no such column '" + columnName + "'");
      Integer jdbcType = (Integer) m_dataTypes.get(columnName.toLowerCase());
      if (null == jdbcType)
         throw new SQLException("no such column " + columnName);
      return (short) jdbcType.intValue();
   }


   /**
    * The meaning of size depends on the data type. For numeric types, it is
    * the precision. For character and date types, it is the max number of
    * chars that can be stored in the column.
    *
    * @return A non-negative value.
    *
    * @throws SQLException If this table does not have a column by the supplied
    *    name.
    */
   public int getSize( String columnName )
      throws SQLException
   {
      int colIdx = Collections.binarySearch(m_columns, columnName);
      if (colIdx < 0)
         throw new SQLException("no such column " + columnName);
      ColumnInfo info = (ColumnInfo)m_columns.get(colIdx);
      return info.m_size;
   }

   /**
    * This property is only defined for numeric types. It is the number of
    * digits to the right of the decimal point.
    *
    * @return For numeric types, a non-negative value. Undefined for other
    *    types.
    *
    * @throws SQLException If this table does not have a column by the supplied
    *    name.
    */
   public int getScale( String columnName )
      throws SQLException
   {
      int colIdx = Collections.binarySearch(m_columns, columnName);
      if (colIdx < 0)
         throw new SQLException("no such column " + columnName);
      ColumnInfo info = (ColumnInfo)m_columns.get(colIdx);
      return info.m_fractionalDigits;
   }

   /**
    * The database dependent type name for this column.
    *
    * @return Never <code>null</code>, may be empty.
    *
    * @throws SQLException If this table does not have a column by the supplied
    *    name.
    */
   public String getTypeName( String columnName )
      throws SQLException
   {
      int colIdx = Collections.binarySearch(m_columns, columnName);
      if (colIdx < 0)
         throw new SQLException("no such column " + columnName);
      ColumnInfo info = (ColumnInfo)m_columns.get(colIdx);
      return null == info.m_typeName ? "" : info.m_typeName;
   }

   /**
    * @author chadloder
    *
    * Gets the table statistics for this table.
    *
    * @return PSTableStatistics The table statistics.
    *
    * @since 1.1 1999/4/30
    *
    */
   public PSTableStatistics getStatistics() throws SQLException
   {
      return m_tableStats;
   }


   /**
    * @author chadloder
    *
    * Gets a list of index statistics for this table, one PSIndexStatistics
    * object for each index defined.
    *
    * @return PSIndexStatistics[]
    *
    * @since 1.1 1999/4/30
    *
    */
   public PSIndexStatistics[] getIndexStatistics() throws SQLException
   {
      return m_indexStats;
   }

   /**
    * Create a map of the data types for each column in this table.
    *
    * @param alias The table alias to use in the map.
    *
    * @return the hash map in which both alias.column and column are
    * used as the key and its java.sql.Types.xxx data type is stored as
    * the value.
    *
    * @throws SQLException if there are any database errors.
    */
   public Map loadDataTypes(String alias) throws SQLException
   {
      Map dtMap = null;

      // see if this has already been built
      dtMap = (Map)m_dataTypeMap.get(alias.toLowerCase());
      if (dtMap != null)
         return dtMap;

      // see if we've already built our base datatype map
      if (m_dataTypes == null)
      {
         m_dataTypes = new HashMap();
         java.sql.Connection conn = null;
         ResultSet rs = null;
         try {
            conn = getConnection();
            
            DatabaseMetaData meta = conn.getMetaData();

            /* while processing the columns, we may need to lookup types
             * which are not JDBC compliant to see if there is a native type
             * which matches. Drivers like SQL Server do not allow meta data
             * calls while processing meta data calls on the same connection.
             * as such, we are now pre-loading the native data type info.
             * (addresses Bug Id: Rx-99-10-0185)
             */
            PSDatabaseMetaData.loadNativeDataTypeMap(conn, meta);

            rs = meta.getColumns(conn.getCatalog(), m_schema, m_tableName, "%");

            String colName, strColType;
            while (rs.next()) {
               /* THESE MUST BE READ IN THE CORRECT SEQUENCE !!! */
               colName   = rs.getString(4 /* column name */);
               short jdbcType   = rs.getShort(5 /* java.sql.Types data type */);
               strColType  = rs.getString(6 /* column data type name */);

               jdbcType = PSSqlHelper.convertNativeDataType(
                  jdbcType, strColType, m_driver);

               /* store it as just the column */
               m_dataTypes.put(colName.toLowerCase(), new Integer(jdbcType));
            }

         }
         finally 
         {
            try {
               if (rs != null)
                  rs.close();
            } catch (Exception e) { /* done anyway, ignore it */ }

            try {
               if (conn != null)
                  conn.close();
            } catch (Exception e) { /* done anyway, ignore it */ }
         }
      }

      // need to add in entries with the table alias as part of the key
      dtMap = new HashMap();
      m_dataTypeMap.put(alias.toLowerCase(), dtMap);

      Iterator entries = m_dataTypes.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry entry = (Map.Entry)entries.next();
         String key = (String)entry.getKey();
         Integer val = (Integer)entry.getValue();
         dtMap.put(new String(key),
            new Integer(val.intValue()));
         dtMap.put(alias.toLowerCase() + "." + key,
            new Integer(val.intValue()));
      }

      return dtMap;
   }

   /**
    * Get a connection
    * 
    * @return a connection, never <code>null</code>.
    * 
    * @throws SQLException if there are any errors.
    */
   private Connection getConnection() throws SQLException
   {
      Connection conn;

      try
      {
         conn = PSConnectionHelper.getDbConnection(this);
      }
      catch (NamingException e)
      {
         throw new SQLException(e.getLocalizedMessage());
      }

      return conn;
   }

   /**
    * Determines if the specified type is a known JDBC datatype.
    *
    * @param type The type to check.
    *
    * @return <code>true</code> if it is a JDBC datatype, <code>false</code>
    * if not.
    */
   public static boolean isJdbcDataType(int type)
   {
      switch (type)
      {
         case Types.BIT:
         case Types.TINYINT:
         case Types.SMALLINT:
         case Types.INTEGER:
         case Types.BIGINT:
         case Types.FLOAT:
         case Types.REAL:
         case Types.DOUBLE:
         case Types.NUMERIC:
         case Types.DECIMAL:
         case Types.CHAR:
         case Types.VARCHAR:
         case Types.LONGVARCHAR:
         case Types.DATE:
         case Types.TIME:
         case Types.TIMESTAMP:
         case Types.BINARY:
         case Types.VARBINARY:
         case Types.LONGVARBINARY:
         case Types.NULL:
         case Types.OTHER:
         case Types.JAVA_OBJECT:
         case Types.DISTINCT:
         case Types.STRUCT:
         case Types.ARRAY:
         case Types.BLOB:
         case Types.CLOB:
         case Types.REF:
            return true;

         default:
            return false;
      }
   }

   /**
    * Sets the estimate statistics.
    * 
    * @param estimateStats a set of estimated (table) statistics. It maps table 
    *    name (map key) to its estimated number of rows (map value). 
    *    Never <code>null</code>, but may be empty.
    */
   public static void setEstimateStatistics(Map<String,Integer> estimateStats)
   {
      if (estimateStats == null)
         throw new IllegalArgumentException("estimateStats may not be null.");
      
      ms_estimateStatistics = estimateStats;
   }
   
   /**
    * This contains a set of estimated (table) statistics. It maps table name
    * (map key) to its estimated number of rows (map value). Defaults to an 
    * empty map.
    */
   private static Map<String, Integer> ms_estimateStatistics = new HashMap<String, Integer>();

   /**
    * @author chadloder
    *
    * Private utility method to load the key information for this table
    *
    * @param   md   The meta data to use
    *
    * @since 1.1 1999/4/30
    *
    */
   private void loadKeyInformation(DatabaseMetaData md) throws SQLException
   {
      /* bug id Rx-99-11-0029 has been fixed by wrappering the
       * DatabaseMetaData object with one capable of setting up the
       * catalog request as required for the given driver.
       */
      ResultSet rs = null;
      try
      {
         rs = md.getPrimaryKeys(m_catalog, m_schema, m_tableName);
         // driver can return a null ResultSet even though the API doc implies
         // that they should not (for example, Informix)
         if (rs != null)
         {
            String keyName = null;
            while (rs.next())
            {
               m_primaryKeyColumns.add(rs.getString(4));
               keyName = rs.getString(6);
               if (keyName != null)
                  m_primaryKeyName = keyName;
            }
         }
      } catch (SQLException e) {
         /* this may not be supported by the specified driver.
          * we'll log this, hoping it's a lack of support, rather than
          * error entirely.
          *
          * LOG THIS!
          */
         Object[] args = { m_dataSource, m_schema, m_tableName, 
            "getPrimaryKeys", PSSqlException.toString(e) };
         com.percussion.log.PSLogManager.write(
            new PSLogServerWarning(
            IPSBackEndErrors.LOAD_META_DATA_EXCEPTION,
            args, false, "PSTableMetaData"));
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
         rs = md.getImportedKeys(m_catalog, m_schema, m_tableName);
         // driver can return a null ResultSet even though the API doc implies
         // that they should not (for example, Informix)
         if (rs != null)
         {
            while (rs.next())
            {
               m_foreignKeyColumns.add(rs.getString(3) + "." + rs.getString(4));
            }
         }
      } catch (SQLException e) {
         /* this may not be supported by the specified driver.
          * we'll log this, hoping it's a lack of support, rather than
          * error entirely.
          *
          * LOG THIS!
          */
         Object[] args = { m_dataSource, m_schema, m_tableName, 
            "getImportedKeys", PSSqlException.toString(e) };
         com.percussion.log.PSLogManager.write(
            new PSLogServerWarning(
            IPSBackEndErrors.LOAD_META_DATA_EXCEPTION,
            args, false, "PSTableMetaData"));
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
    * Private utility method to load the index information for this table.
    * An estimated statistics will be created instead of load the index and 
    * statistics from the repository if this table exists in the collection
    * of the estimate statistics.
    *
    * @param md The meta data to use; assumed not <code>null</code>.
    *
    * @since 1.1 1999/4/30
    *
    */
   
   private static Logger ms_logger = Logger.getLogger(PSTableMetaData.class);
   
   private void loadStatsAndIndexInformation(DatabaseMetaData md) 
      throws SQLException
   {
      PSBackEndTable table = toBackEndTable();
      if (isEstimateSatisticsEnabled()) 
      {
         m_tableStats = new PSTableStatistics(m_login, table, 1);
         ms_logger.debug("estimate table: " + table.getTable() + " value = 1");
      } 
      else 
      {
         Integer estimateRow = ms_estimateStatistics.get(table.getTable());
         if (estimateRow != null)
         {
            m_tableStats = new PSTableStatistics(m_login, table, estimateRow
                   .intValue());
            ms_logger.debug("estimate table: " + table.getTable()
                   + " value = " + estimateRow.intValue());
          }
          else
          {
             m_tableStats = new PSTableStatistics(m_login, table, md);
          }
      
          m_indexStats = m_tableStats.getIndexStatistics();
      }
   }
    
   /**
   * Determines if estimate statistics is enabled for all tables 
   * and views. This behavior is determined by "enableEstimateStatistics"
   * property defined in sever.properties. 
   * 
   * @return <code>false<code> if above property equals <code>false</code>;
   * otherwise return <code>true</code>. Defaults to <code>true</code>.
   */
   boolean isEstimateSatisticsEnabled()
   {
      String enableEstimate = PSServer.getServerProps().getProperty(
         "enableEstimateStatistics", "true");

      if ((StringUtils.equals(enableEstimate, "false")))  
         return false;

      return true;
    }

   /**
     * @author chadloder
     * 
     * Private utility method to load the column information for this table
     * 
     * @param md
     *            The meta data to use
     * 
     * @since 1.1 1999/4/30
     * 
     */
   private void loadColumnInformation(DatabaseMetaData md)
      throws SQLException
   {
      /* Note, this method no longer makes decisions about what columns
       * are "auto-update" columns. This is done in "getAutoUpdateColumns()"
       * method instead.
       */
      // we depend upon this already existing (which it should)
      PSDatabaseMetaData psDbMeta = PSOptimizer.getCachedDatabaseMetaData(
         m_dataSource);
      
      Map typeMap = psDbMeta.getDataTypeDefinitionMap();

      ResultSet rs = null;
      try
      {
         rs = md.getColumns(m_catalog, m_schema, m_tableName, "%");
         // driver can return a null ResultSet even though the API doc implies
         // that they should not (for example, Informix)
         if (rs != null)
         {
            while (rs.next())
            {
               ColumnInfo colInfo = new ColumnInfo(rs.getString(4),
                  rs.getShort(5), rs.getString(6), rs.getInt(7),
                  rs.getInt(9), rs.getInt(10), rs.getInt(11), rs.getString(12),
                  rs.getString(13), rs.getInt(16), rs.getInt(17));
               m_columns.add(colInfo);
            }
            Collections.sort(m_columns);
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
    * Private class to keep track of column information
    *
    */
   private class ColumnInfo implements Comparable
   {
      public ColumnInfo(String name, short type, String typeName,
         int columnSize, int fractionalDigits, int radix, int nullable,
         String remarks, String defaultValue, int charLength, int ordinalPosition)
      {
// System.out.println(name + " " + typeName + " (" + type + ")");
         m_name = name;
         m_type = type;
         m_typeName = typeName;
         m_size = columnSize;
         m_fractionalDigits = fractionalDigits;
         m_radix = radix;
         m_nullable = nullable;
         m_remarks = remarks;
         m_defaultValue = defaultValue;
         m_charLength = charLength;
         m_ordinalPosition = ordinalPosition;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof ColumnInfo)) return false;
         ColumnInfo that = (ColumnInfo) o;
         return m_type == that.m_type &&
                 m_size == that.m_size &&
                 m_fractionalDigits == that.m_fractionalDigits &&
                 m_radix == that.m_radix &&
                 m_nullable == that.m_nullable &&
                 m_charLength == that.m_charLength &&
                 m_ordinalPosition == that.m_ordinalPosition &&
                 Objects.equals(m_name, that.m_name) &&
                 Objects.equals(m_typeName, that.m_typeName) &&
                 Objects.equals(m_remarks, that.m_remarks) &&
                 Objects.equals(m_defaultValue, that.m_defaultValue);
      }

      @Override
      public int hashCode() {
         return Objects.hash(m_name, m_typeName, m_type, m_size, m_fractionalDigits, m_radix, m_nullable, m_remarks, m_defaultValue, m_charLength, m_ordinalPosition);
      }

      public int compareTo(Object o)
      {
         if (o instanceof String)
            return m_name.compareTo((String)o);

         ColumnInfo other = (ColumnInfo)o;
         return m_name.compareTo(other.m_name);
      }

      public String m_name;
      public String m_typeName;
      public short m_type;
      public int m_size;
      public int m_fractionalDigits;
      public int m_radix;
      public int m_nullable;
      public String m_remarks;
      public String m_defaultValue;
      public int m_charLength;
      public int m_ordinalPosition;
   }

   private PSBackEndLogin m_login;
   private String m_catalog;
   private String m_schema;
   private String m_tableName;
   private String m_driver;
   private String m_dataSource;

   // a list (in key order) of the primary key columns
   private List m_primaryKeyColumns;

   // the name of this table's primary key
   private String m_primaryKeyName;

   // a list of the foreign key columns
   private List m_foreignKeyColumns;

   /**
    * a list of the columns which are automatically inserted/updated.  <code>
    * null</code> until a call to {@link #getAutoUpdateColumns()} or {@link
    * #loadColumnInformation(DatabaseMetaData)} is made.
    */
   private List m_autoUpdateColumns = null;

   // the table statistics for this table
   private PSTableStatistics m_tableStats;

   // an array of index statistics for this table, one for each index
   private PSIndexStatistics[] m_indexStats;

   // a list (in column name order) of ColumnInfo objects
   private List m_columns;

   // a SQL style pattern matcher
   private PSPatternMatcher m_patMat;

   /**
    * Map of datatypes with column name as the key and the JDBC datatype as the
    * value.  Initialized by the first call to {@link #loadDataTypes(String)},
    * never <code>null</code> or modified after that.
    */
   private Map m_dataTypes = null;

   /**
    * Map of datatype maps based on table alias as the key and the datatype
    * map as the value.  Never <code>null</code>, entries are added by calls to
    * {@link #loadDataTypes(String)}.
    */
   private Map m_dataTypeMap = new HashMap();

   /* (non-Javadoc)
    * @see com.percussion.util.jdbc.IPSConnectionInfo#getDataSource()
    */
   public String getDataSource()
   {
      return m_dataSource;
   }
   
   /**
    * Connection detail for this table's datasource, never <code>null</code>
    * after construction.
    */
   private PSConnectionDetail m_connDetail;
}
