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
import com.percussion.design.objectstore.PSBackEndCredential;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSTableLocator;
import com.percussion.design.objectstore.PSTableRef;
import com.percussion.design.objectstore.PSTableSet;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.jdbc.PSConnectionDetail;

import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility and caching class for database meta data. To use the cache you 
 * should reference the singleton instance for the entire life cycle of
 * your application.
 */
public class PSMetaDataCache
{
   /**
    * Get the singleton instance object for this class, will be created if
    * it does not exist yet.
    *
    * @return the singleton object for this class.
    */
   public static PSMetaDataCache getInstance()
   {
      if (ms_instance == null)
         ms_instance = new PSMetaDataCache();
      
      return ms_instance;
   }
   
   /**
    * Get the cached metadata for the provided table information. If a cache
    * entry for the provided table info exists it is returned. Otherwise the
    * metabase data is created , added to the cache and then returned.
    *
    * @param tableSet the table set to get the metabase data for, not
    *    <code>null</code>.
    * @return the database metabase data, never <code>null</code>.
    * @throws IllegalArgumentException if the provided tableSet is 
    *    <code>null</code>.
    */
   public static PSDatabaseMetaData getCachedDatabaseMetaData(
      PSTableSet tableSet)
   {
      if (tableSet == null)
         throw new IllegalArgumentException("tableSet cannot be null");

      PSTableLocator locator = tableSet.getTableLocation();
      PSBackEndCredential cred = locator.getCredentials();

      return getCachedDatabaseMetaData(cred.getDataSource());
   }

   /**
    * Get the cached metadata
    * @param dataSource the datasource, may be <code>null</code> or empty to
    * indicate the default datasource.
    * 
    * @return the cached data, never <code>null</code>.
    */
   static PSDatabaseMetaData getCachedDatabaseMetaData(String dataSource)
   {
      String key = getDbKey(dataSource);
      PSDatabaseMetaData meta = ms_cache.get(key);
      if (meta == null)
      {
         meta = new PSDatabaseMetaData(dataSource);
         ms_cache.put(key, meta);
      }      
      return meta;
   }

   /**
    * Get the table meta data for the provided table information.
    *
    * @param tableSet the table set to get the metabase data for, not
    *    <code>null</code>.
    * @param tableName the table name to create the metabase data for, not
    *    <code>null</code>.
    * @return the table metabase data, never <code>null</code>.
    * @throws IllegalArgumentException ifany passed parameter is 
    *    <code>null</code>.
    * @throws SQLException if any database processing goes wrong.
    */
   public static PSTableMetaData getTableMetaData(PSTableSet tableSet, 
      String tableName)
      throws SQLException
   {
      if (tableName == null)
         throw new IllegalArgumentException("tableName cannot be null");

      PSDatabaseMetaData metaData = getCachedDatabaseMetaData(tableSet);
      PSTableLocator locator = tableSet.getTableLocation();
      PSConnectionDetail detail = metaData.getConnectionDetail();
      return metaData.getTableMetaData(detail.getOrigin(), tableName);
   }

   /**
    * Get the table meta data for the provided table information.
    *
    * @param login the backend login to use, not
    *    <code>null</code>.
    * @param table the backend table to create the metabase data for, not
    *    <code>null</code>.  Connection detail will be loaded on the table
    *    after the method returns.
    * @return the table metabase data, never <code>null</code>.
    * @throws IllegalArgumentException if any passed parameter is
    *    <code>null</code>.
    * @throws SQLException if any database processing goes wrong.
    */
   public static PSTableMetaData getTableMetaData(PSBackEndLogin login,
      PSBackEndTable table)
      throws SQLException
   {
      if (login == null)
         throw new IllegalArgumentException("login cannot be null");

      if (table == null)
         throw new IllegalArgumentException("table cannot be null");
      
      PSDatabaseMetaData dmd = getCachedDatabaseMetaData(login);
      PSConnectionDetail detail = dmd.getConnectionDetail();
      table.setConnectionDetail(detail);
      PSTableMetaData tmd = dmd.getTableMetaData(detail.getOrigin(), 
         table.getTable());

      return tmd;
   }

   /**
    * Get metadata from the cache
    * 
    * @param info The connection info, may be <code>null</code> to specify the
    * default connection.
    * @return the metadata, never <code>null</code>.
    */
   public static PSDatabaseMetaData getCachedDatabaseMetaData(
         IPSConnectionInfo info)
   {
      PSDatabaseMetaData dmd = null;
      String datasource = info == null ? null : info.getDataSource();
      
      dmd = getCachedDatabaseMetaData(datasource);

      return dmd;
   }

   /**
    * Get metadata from the cache
    * 
    * @param info The connection info, may be <code>null</code> to specify the
    * default connection.
    * @return the detail, never <code>null</code>.
    * @throws SQLException if there are any errors
    */
   public static PSConnectionDetail getConnectionDetail(
         IPSConnectionInfo info) throws SQLException 
   {
      PSDatabaseMetaData dmd = null;
      String datasource = info == null ? null : info.getDataSource();
      
      dmd = getCachedDatabaseMetaData(datasource);

      return dmd.getConnectionDetail();
   }   
   
   /**
    * Checks if the provided backend column is of type binary.
    * 
    * @param tableSet the table set to get the metabase data for, not
    *           <code>null</code>.
    * @param column the backend column to test, not <code>null</code>.
    * @throws IllegalArgumentException if any provided parameter is
    *            <code>null</code>.
    * @throws SQLException if any database processing goes wrong.
    */
   public static boolean isBinaryBackendColumn(PSTableSet tableSet,
      PSBackEndColumn column)
      throws SQLException
   {
      String tableName = column.getTable().getTable();
      // try to get the table name from the tableset
      if ( null == tableName || tableName.trim().length() == 0 )
      {
         String alias = column.getTable().getAlias();
         Iterator refs = tableSet.getTableRefs();
         while ( refs.hasNext())
         {
            PSTableRef ref = (PSTableRef) refs.next();
            if ( ref.getAlias().equals( alias ))
            {
               tableName = ref.getName();
               break;
            }
         }
      }
      PSTableMetaData metaData = getTableMetaData(tableSet, tableName);
      switch (metaData.getColumnType(column.getColumn()))
      {
         case Types.BINARY:
         case Types.BLOB:
         case Types.LONGVARBINARY:
         case Types.VARBINARY:
            return true;
      }
      
      return false;
   }

   /**
    * Flushes any cached metadata for the specified datasource.  Data will be
    * refreshed the next time it is requested.  
    *
    * @param dataSource the datasource to flush info on.  May be 
    * <code>null</code> or empty to use the default datasource.
    *
    * @return <code>true</code> if metadata was found and flushed, <code>false
    * </code> if no metadata was located for the specified datasource.
    */
   public static boolean flushDatabaseMetaData(String dataSource)
   {
      String key = getDbKey(dataSource);
      PSDatabaseMetaData meta = ms_cache.remove(key);      
      
      return (meta != null);
   }

   /**
    * Private since we only allow a singleton object.
    */
   private PSMetaDataCache()
   {
   }
   
   /**
    * Construct a key for the given data source
    * @param dataSource the data source, may be <code>null</code> or empty
    * @return the key
    */
   private static String getDbKey(String dataSource)
   {
      return ("ds:" + (dataSource == null ? "" : dataSource)).toLowerCase();
   }

   /**
    * Load the connection detail and set it on the table.  If table already has
    * connection detail, method simply returns.
    * 
    * @param table The table for which the datasource is used to load connection
    * detail, may not be <code>null</code>. 
    * @throws SQLException if there are any errors.
    */
   public static void loadConnectionDetail(PSBackEndTable table) 
      throws SQLException
   {
      if (table == null)
         throw new IllegalArgumentException("table may not be null");
      
      if (table.getConnectionDetail() != null)
         return;
      
      PSDatabaseMetaData dbmd = getCachedDatabaseMetaData(
         table.getDataSource());
      table.setConnectionDetail(dbmd.getConnectionDetail());
   }

   /**
    * Contains the single instance of this class. <code>null</code> until the
    * first time <code>getCachedDatabaseMetaData</code> is called. Never 
    * <code>null</code> after that.
    */
   private static PSMetaDataCache ms_instance = null;

   /**
    * A hash map containing cached metabase data. As key we use a combined
    * string of ds:<datasource>, while the values are
    * PSDatabaseMetaData objects.
    */
   private static Map<String, PSDatabaseMetaData> ms_cache = 
      new HashMap<>();
}
