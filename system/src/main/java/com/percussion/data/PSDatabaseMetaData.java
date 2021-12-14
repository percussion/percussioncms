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

import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.jdbc.PSConnectionInfo;
import com.percussion.utils.jdbc.PSJdbcUtils;
import com.percussion.utils.tools.PSPatternMatcher;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The PSDatabaseMetaData class offers a more tractable interface to
 * database meta data than does java.sql.DatabaseMetaData. Results
 * are cached and put into a readily useable form.
 *
 * A PSDatabaseMetaData object may use JDBC resources during construction
 * or method invocation, but it does not keep any JDBC resources open
 * after the method returns, and caches meta data wherever possible.
 */
public class PSDatabaseMetaData implements IPSConnectionInfo
{
   public PSDatabaseMetaData(PSBackEndLogin login)
      throws SQLException
   {
      this(login.getDataSource());
   }

   /**
    * @param dataSource
    */
   public PSDatabaseMetaData(String dataSource) 
   {
      m_dataSource = dataSource;
      m_patMat = new PSPatternMatcher('_', '%', "%");
      m_tables = new ArrayList();
      m_tableMetaData = new HashMap();
      m_dataTypeMap = null;   // flag to get data types on the first request
   }

   /**
    * @author chadloder
    *
    * Gets a list of the names of all the tables in all catalogs of this
    * database.
    *
    * @param   tableNamePattern
    *
    * @return String[]
    *
    * @since 1.1 1999/4/29
    *
    */
   public String[] getTables()
      throws SQLException
   {
      return getTables(null, null, "%", null);
   }


   /**
    * @author chadloder
    *
    * Gets a list of the names of all tables available in the given catalog
    * of this database.
    *
    * @param   catalog The name of a catalog. Null indicates that the catalog
    * name should be dropped from the selection criteria.
    *
    * @return String[]
    *
    * @since 1.1 1999/4/29
    *
    */
   public String[] getTables(String catalog)
      throws SQLException
   {
      return getTables(catalog, null, "%", null);
   }

   /**
    * @author chadloder
    *
    * Gets a list of the names of all tables available in the given catalog
    * whose schema matches the given schema pattern.
    *
    * @param   catalog The name of a catalog. "" retrieves tables without
    * a catalog. Null indicates that the catalog name should be dropped from
    * the selection criteria.
    *
    * @param   schemaPattern   A schema name pattern. "" retrieves tables
    * without a schema; null indicates that the schema name should be dropped
    * from the selection criterion.
    *
    * @return String[] A list of the names of all tables that match the
    * given criteria.
    *
    * @since 1.1 1999/4/29
    *
    */
   public String[] getTables(String catalog, String schemaPattern)
      throws SQLException
   {
      return getTables(catalog, schemaPattern, "%", null);
   }


   /**
    * @author chadloder
    *
    * Gets a list of the names of all tables available in the given catalog
    * whose schema matches the given schema pattern.
    *
    * @param   catalog The name of a catalog. "" retrieves tables without
    * a catalog. Null indicates that the catalog name should be dropped from
    * the selection criteria.
    *
    * @param   schemaPattern   A schema name pattern. "" retrieves tables
    * without a schema; null indicates that the schema name should be dropped
    * from the selection criterion.
    *
    * @param   tableNamePattern A table name pattern.
    *
    * @return String[] A list of the names of all tables that match the
    * given criteria.
    *
    * @since 1.1 1999/4/29
    *
    */
   public String[] getTables(String catalog, String schemaPattern,
      String tableNamePattern)
      throws SQLException
   {
      return getTables(catalog, schemaPattern, tableNamePattern, null);
   }

   /**
    * @author chadloder
    *
    * Gets a list of the names of all tables available in the given catalog
    * whose schema matches the given schema pattern.
    *
    * @param   catalog The name of a catalog. "" retrieves tables without
    * a catalog. Null indicates that the catalog name should be dropped from
    * the selection criteria.
    *
    * @param   schemaPattern   A schema name pattern. "" retrieves tables
    * without a schema; null indicates that the schema name should be dropped
    * from the selection criterion.
    *
    * @param   types   A list of table types to include. null indicates that
    * all table types should be retrieved.
    *
    * @return String[] A list of the names of all tables that match the
    * given criteria.
    *
    * @since 1.1 1999/4/29
    *
    */
   public String[] getTables(String catalog, String schemaPattern,
      String tableNamePattern, String[] types)
      throws SQLException
   {
      ArrayList matchingTables = new ArrayList();

      if (m_tables != null)
      {
         matchingTables.ensureCapacity(m_tables.size());

         String[] sortedTypes = null;
         if (null != types)
         {
            sortedTypes = new String[types.length];
            System.arraycopy(types, 0, sortedTypes, 0, types.length);
            Arrays.sort(sortedTypes);
         }
         for (Iterator i = m_tables.iterator(); i.hasNext(); )
         {
            ShortTableInfo info = (ShortTableInfo)i.next();
            if (catalog != null)
            {
               if (info.m_catalog == null || !info.m_catalog.equals(catalog))
                  continue;
            }
            if (schemaPattern != null && !schemaPattern.equals("%"))
            {
               if (info.m_schema == null
                  || !m_patMat.doesMatchPattern(schemaPattern, info.m_schema))
                  continue;
            }
            if (tableNamePattern != null && !tableNamePattern.equals("%"))
            {
               if (!m_patMat.doesMatchPattern(tableNamePattern, info.m_tableName))
                  continue;
            }
            if (sortedTypes != null)
            {
               if (Arrays.binarySearch(sortedTypes, info.m_type) < 0)
                  continue;
            }
            matchingTables.add(info.m_tableName);
         }
      }

      return (String[])matchingTables.toArray(new String [matchingTables.size()]);
   }

   /**
    * Gets the table meta data for a table in this database
    *
    * @param tableName
    *
    * @return PSTableMetaData The table meta data for the specified table
    * @throws SQLException if there are any errors.
    */
   public PSTableMetaData getTableMetaData(
      String schemaName, String tableName)
      throws SQLException
   {
      String tableKey;
      if (schemaName == null)
         tableKey = "." + tableName;
      else
         tableKey = schemaName + "." + tableName;

      PSTableMetaData tmd = (PSTableMetaData)m_tableMetaData.get(tableKey);
      if (null != tmd)
         return tmd;

      Connection conn = null;
      try {
         conn = PSConnectionHelper.getDbConnection(this);

         DatabaseMetaData dmd = conn.getMetaData();
         loadTableMetaData(schemaName, tableName, dmd);
      }
      catch (NamingException e)
      {
         throw new SQLException(e.getLocalizedMessage());
      }
      finally {
         if (conn != null)
            try { conn.close(); } catch (SQLException e) { /* ignore */ }
      }

      return (PSTableMetaData)m_tableMetaData.get(tableKey);
   }

   /**
    * Get the data type definitions for the data types supported
    * by this back-end as a map.
    *
    * @return a map containing the data type name as the key and the
    * data type definition as the value
    * @throws SQLException
    *
    * @see         PSDataTypeInfo
    */
   public java.util.Map getDataTypeDefinitionMap()
      throws SQLException
   {
      if (m_dataTypeMap == null)
      {
         /* we should probably synchronize, but it's really no big deal as
          * the worst case scenario is multiple people do this at the same
          * time. There's no real harm from that.
          */
         java.util.Map types = new java.util.HashMap();

         Connection conn = null;
         try {
            conn = PSConnectionHelper.getDbConnection(this);
            DatabaseMetaData dmd = conn.getMetaData();
            ResultSet rs = null;

            try
            {
               rs = dmd.getTypeInfo();
            }
            catch (java.sql.SQLException e)
            {
               // this may simply be unsupported, so let's not die over it
               try
               {
                  if (rs != null)
                     rs.close();
               }
               catch (SQLException ex)
               { /* ignore */
               }
               rs = null; // will cause the remaining logic to be ignored

               Object[] args =
               {m_dataSource, "", "", "getTypeInfo",
                     PSSqlException.toString(e)};
               com.percussion.log.PSLogManager
                     .write(new com.percussion.log.PSLogServerWarning(
                           IPSBackEndErrors.LOAD_META_DATA_EXCEPTION, args,
                           true, "DatabaseMetaData"));
            }

            try {
               if (rs != null) {
                  while (rs.next()) {
                     String typeName = rs.getString(1);
                     short sType = rs.getShort(2);
                     types.put(typeName, new PSDataTypeInfo(
                        typeName,      // type_name
                        sType,      // data_type
                        ((sType == Types.BLOB) || (sType == Types.CLOB)) ? 0 :
                        rs.getInt(3),         // precision
                        rs.getString(4),      // literal_prefix
                        rs.getString(5),      // literal_suffix
                        rs.getShort(7),      // nullable
                        rs.getShort(9),      // searchable
                        rs.getBoolean(12),   // auto_increment
                        rs.getShort(14),      // minimum_scale
                        rs.getShort(15)      // maximum_scale
                     ));
                  }
               }
            } finally {
               if (rs != null)
                  try { rs.close(); } catch (SQLException e) { /* ignore */ }
            }
         }
         catch (NamingException e)
         {
            throw new SQLException(e.getLocalizedMessage());
         } 
         finally 
         {
            if (conn != null)
               try { conn.close(); } catch (SQLException e) { /* ignore */ }
         }

         m_dataTypeMap = types;
      }

      return m_dataTypeMap;
   }

   /**
    * Converts this object to a PSBackEndLogin to make further connections
    * to this database
    *
    * @return   PSBackEndLogin
    */
   PSBackEndLogin toBackEndLogin()
   {
      return new PSBackEndLogin(m_dataSource);      
   }

   /**
    * Get the data type definitions for the data types supported
    * by this back-end as an array.
    *
    * @return      an array of data type definitions
    * @throws SQLException
    * @throws NamingException
    */
   public PSDataTypeInfo[] getDataTypeDefinitions()
      throws NamingException, SQLException
   {
      java.util.Map types = getDataTypeDefinitionMap();
      int size = (types == null) ? 0 : types.size();
      PSDataTypeInfo[] dtArray = new PSDataTypeInfo[size];

      if (size > 0) {
         java.util.Iterator ite = types.values().iterator();
         for (int i = 0; ite.hasNext() && (i < size); i++)
            dtArray[i] = (PSDataTypeInfo)ite.next();
      }

      return dtArray;
   }


   /**
    * Is this server/driver on our list of tweaked drivers that needs
    * nulls to be untweaked?
    * This set contains drivers have had their data types tweaked by us in some
    * way to conform to jdbc types and don't like it when using setNull()
    *
    * @param   obj   The key for this server/driver.
    *
    * @return  <code>true</code> if we have to go back and get the native
    *          data type to set the value to null, <code>false</code>
    *          otherwise.
    */
   public static boolean unTweakDriverNulls(Object obj)
   {
      return ms_driversNeedingUntweakedNulls.contains((UNTWEAKED_DRIVER_PREFIX + 
         (obj == null ? "" : obj)).toLowerCase());
   }

   /**
    * Guess the corresponding native type's jdbc counterpart.
    *
    * @param nativeType the native type to convert to a JDBC type.
    *
    * @return The JDBC type if it can be determined, or else the native type
    * that was supplied.
    */
   public static short guessNativeDataTypeConversion(short nativeType)
   {
      short dt = nativeType;
      Short sJdbcType = (Short) ms_dataTypeConversionMap.get(
         new Short(nativeType));
      if (sJdbcType != null) {
         dt = sJdbcType.shortValue();
      }
      return dt;
   }


   /**
    * Loads the metadata for the specified table and places it in the cache.  If
    * metadata for this table already exists in the cache, nothing is done.
    *
    * @param schemaName The name of the schema or origin, may be <code>null
    * </code> or empty.
    * @param tableName The name of the table, assumed not <code>null</code> or
    * empty.
    * @param dmd The database meta data for this database, assumed not <code>
    * null</code>.
    *
    *@throws NamingException if there are any JNDI errors collecting the data.
    * @throws SQLException if there are any database errors collecting the
    * data.
    */
   private void loadTableMetaData(
      String schemaName, String tableName, DatabaseMetaData dmd)
      throws SQLException, NamingException
   {
      String tableKey = getTableKey(tableName, schemaName);

      if (m_tableMetaData.get(tableKey) == null)
      {

         /* first check to see if this guy needs untweaking when using setNull,
            store as driver:server */
         if (ms_driversNeedingUntweakedNulls.contains(
            dmd.getDatabaseProductName()))
         {
            ms_driversNeedingUntweakedNulls.add((UNTWEAKED_DRIVER_PREFIX + 
               (m_dataSource == null ? "" : m_dataSource)).toLowerCase());
         }

         PSTableMetaData tmd = new PSTableMetaData(
            toBackEndLogin(), schemaName, tableName, dmd);
         m_tableMetaData.put(tableKey, tmd);
      }
   }

   /**
    * Creates two maps if they have not already been created.  Creates a
    * map of Native types to JDBC types, and also creates a map of SQL types
    * to JDBC types for this database.
    *
    * @param conn The connection, may not be <code>null</code>.
    * @param meta The database metadata, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if either param is <code>null</code>.
    */
   public static HashMap loadNativeDataTypeMap( Connection conn,
         DatabaseMetaData meta)
      throws SQLException
   {
      if (conn == null || meta == null)
         throw new IllegalArgumentException("One or more params is null");

      ResultSet rs = null;
      String dbmsType = "";

      String driver = PSJdbcUtils.getDriverFromUrl(meta.getURL());
      try
      {
         dbmsType = meta.getDatabaseProductName();
      }
      catch (SQLException e)
      {
         // this is not critical, just prevents the special case check
      }

      HashMap map = (HashMap)ms_fixedUpDataTypeMap.get(dbmsType);
      if (map != null)
         return map;
      map = new HashMap();

      if (dbmsType != null && dbmsType.trim().length() != 0)
      {
         try
         {
            rs = meta.getTypeInfo();
            if (rs != null)
            {
               while (rs.next())
               {
                  final String typeName = rs.getString(1);
                  short nativeType = rs.getShort(2);
                  short jdbcType = PSSqlHelper.convertNativeDataType(
                     nativeType, typeName, driver);
                  ms_dataTypeConversionMap.put(new Short(nativeType),
                        new Short(jdbcType));
                  map.put(typeName, new Short(jdbcType));
               }
            }
         }
         finally
         {
            try
            {
               if (rs != null)
                  rs.close();
            }
            catch (SQLException e)
            {
               /* in case we're catching an exception, this is not necessary */
            }
         }

      }

      if (map.size() != 0)
      {
         ms_fixedUpDataTypeMap.put(dbmsType, map);
      }
      return map;
   }

   /**
    * Get the connection detail for this datasource.
    * 
    * @return The detail, never <code>null</code>.
    * 
    * @throws SQLException if there are any errors.
    */
   public PSConnectionDetail getConnectionDetail() throws SQLException
   {
      if (m_connectionDetail == null)
      {
         try
         {
            m_connectionDetail = PSConnectionHelper.getConnectionDetail(
               new PSConnectionInfo(m_dataSource));
         }
         catch (NamingException e)
         {
            throw new SQLException(e.getLocalizedMessage());
         }
      }
      
      return m_connectionDetail;
   }
   
   /**
    * Flushes any cached metadata for the specified table.  Data will be
    * refreshed the next time it is requested.
    *
    * @param tableName The name of the table to flush.  May not be <code>null
    * </code> or empty.
    *
    * @param origin The origin or schema.  May be <code>null</code> or empty.
    *
    * @return <code>true</code> if the metadata was located and flushed, <code>
    * false</code> if no metadata for the specified table was located.
    *
    * @throws IllegalArgumentException if tableName is <code>null</code> or
    * empty.
    */
   public boolean flushTableMetaData(String tableName, String origin)
   {
      if (tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName may not be null or empty");

      String tableKey = getTableKey(tableName, origin);
      Object o = m_tableMetaData.remove(tableKey);

      return (o != null);
   }

   /**
    * Constructs hash key for the specified table.
    *
    * @param tableName The name of the table.  May not be <code>null</code> or
    * empty.
    * @param origin The origin or schema.  May be <code>null</code> or empty.
    *
    * @return The key, never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if tableName is <code>null</code> or
    * empty.
    */
   private String getTableKey(String tableName, String origin)
   {
      if (tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName may not be null or empty");

      String tableKey = null;

      if (origin == null || origin.trim().length() == 0)
         tableKey = "." + tableName;
      else
         tableKey = origin + "." + tableName;

      return tableKey;
   }


   // the list of data types supported by the back-end
   private Map m_dataTypeMap;

   // a list of ShortTableInfo objects, one for each table contained herein
   private List m_tables;

   // a map from table names to PSTableMetaData objects
   private Map m_tableMetaData;

   // a SQL style pattern matcher
   private PSPatternMatcher m_patMat;

   // the datasource to which this metadata applies
   private String m_dataSource;

   /**
    * The connection detail for this datasource, never <code>null</code>.
    */
   private PSConnectionDetail m_connectionDetail;
   
   /**
    * Stores a mapping of the driver:server combos and their associated
    * data types. This allows us to recover from mismapped data types more
    * quickly by asking the server for the data type info only once.  Never
    * <code>null</code>, modified by a call to {@link #loadNativeDataTypeMap(
    * Connection, DatabaseMetaData) loadNativeDataTypeMap}.
    */
   protected static HashMap ms_fixedUpDataTypeMap = new HashMap();

   /**
    * Stores a mapping of some native datatypes and
    * their related jdbc data types. Never <code>null</code>, modified by a call
    * to {@link #loadNativeDataTypeMap(Connection, DatabaseMetaData)
    * loadNativeDataTypeMap}.
    */
   protected static HashMap ms_dataTypeConversionMap = new HashMap();


   /**
    *  The hash set containing datasources which require that the
    *  modified datatypes be returned to their native types when
    *  dealing with setting null values.  This set will be
    *  created and populated in the static() method and will therefore never
    *  be <code>null</code> or empty.  The names are lowercased as datasource
    *  names must be treated case-insensitively.
    */
   private static HashSet ms_driversNeedingUntweakedNulls = new HashSet();
   private static final String UNTWEAKED_DRIVER_PREFIX = "ds:";

   /**
    * An internal class we use to keep track of table info.
    *
    */
   private class ShortTableInfo implements Comparable
   {
      public ShortTableInfo(String catalog, String schema, String tableName,
         String type, String remarks)
      {
         m_catalog = catalog;
         m_schema = schema;
         m_tableName = tableName;
         m_type = type;
         m_remarks = remarks;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof ShortTableInfo)) return false;
         ShortTableInfo that = (ShortTableInfo) o;
         return Objects.equals(m_catalog, that.m_catalog) &&
                 Objects.equals(m_schema, that.m_schema) &&
                 Objects.equals(m_type, that.m_type) &&
                 Objects.equals(m_tableName, that.m_tableName) &&
                 Objects.equals(m_remarks, that.m_remarks);
      }

      @Override
      public int hashCode() {
         return Objects.hash(m_catalog, m_schema, m_type, m_tableName, m_remarks);
      }

      /**
       * Reflects the ordering used in DatabaseMetaData.getTables:
       * ordered by type, schema, and name.
       *
       * Catalog and remarks are not used in the comparison.
       */
      public int compareTo(Object o)
      {
         ShortTableInfo other = (ShortTableInfo)o;

         int i = m_type.compareTo(other.m_type);;
         if (i != 0)
            return i;

         if (m_schema != null || other.m_schema != null)
         {
            if (m_schema == null)
               return -1;
            else if (other.m_schema == null)
               return 1;
         }

         i = m_schema.compareTo(other.m_schema);
         if (i != 0)
            return i;

         return    m_tableName.compareTo(other.m_tableName);
      }

      public String m_catalog; // may be null
      public String m_schema; // may be null
      public String m_type;
      public String m_tableName; // may NOT be null
      public String m_remarks; // may be null
   } // end inner class ShortTableInfo


   /* (non-Javadoc)
    * @see com.percussion.util.jdbc.IPSConnectionInfo#getDataSource()
    */
   public String getDataSource()
   {
      return m_dataSource;
   }
}
