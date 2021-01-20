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


package com.percussion.deployer.server;

import com.percussion.conn.PSServerException;
import com.percussion.data.PSIdGenerator;
import com.percussion.deploy.server.PSServerJdbcDbmsDef;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDbmsInfo;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDeployComponentUtils;
import com.percussion.deployer.server.dependencies.PSDependencyUtils;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.design.objectstore.PSNotLockedException;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.security.PSAuthenticationRequiredException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcSelectFilter;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.tablefactory.PSJdbcTableSchemaCollection;
import com.percussion.tablefactory.PSJdbcUpdateKey;
import com.percussion.util.PSEntrySet;
import com.percussion.util.PSPreparedStatement;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.jdbc.IPSDatasourceResolver;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.jdbc.PSDatasourceConfig;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

import javax.naming.NamingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Singleton class to provide common database read/write functionality.
 */
public class PSDbmsHelper
{
   /**
    * Private ctor to enforce singleton pattern.
    */
   private PSDbmsHelper()
   {
   }

   /**
    * Gets the single instance of this class.
    *
    * @return The instance, never <code>null</code>.
    */
   public static PSDbmsHelper getInstance()
   {
      if (m_instance == null)
         m_instance = new PSDbmsHelper();
      
      return m_instance;
   }


   /**
    * Gets all entries from the specified table, returning the id and name
    * values for each row found.
    *
    * @param table The name of the table to return data for. May not be
    * <code>null</code> or empty.
    * @param idCol The name of the column to get the registration entry's id
    * from, may not be <code>null</code> or empty.
    * @param nameCol The name of the column to get the registration entry's name
    * from, may not be <code>null</code> or empty.
    * @param filter Optional filter to apply, may be <code>null</code>.
    *
    * @return A list of <code>Map.entry</code> objects, one for each row, each
    * containing the id value as the key, and the name as the value, both as
    * non-<code>null</code> <code>String</code> objects.  Never
    * <code>null</code>, may be empty, will not contain <code>null</code>
    * entries.
    *
    * @throws IllegalArgumentException If any param is invalid.
    * @throws PSDeployException if any errors occur.
    */
   public List getRegistrationEntries(String table, String idCol,
      String nameCol, PSJdbcSelectFilter filter) throws PSDeployException
   {
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");

      if (idCol == null || idCol.trim().length() == 0)
         throw new IllegalArgumentException("idCol may not be null or empty");

      if (nameCol == null || nameCol.trim().length() == 0)
         throw new IllegalArgumentException("nameCol may not be null or empty");

      List<PSEntrySet> result = new ArrayList<PSEntrySet>();

      PSJdbcTableData data = catalogTableData(table, new String[] {idCol,
            nameCol}, filter);
      if (data != null)
      {
         Iterator rows = data.getRows();
         while (rows.hasNext())
         {
            PSJdbcRowData row = (PSJdbcRowData)rows.next();
            PSJdbcColumnData id = row.getColumn(idCol);
            if (id == null || id.getValue() == null ||
               id.getValue().trim().length() == 0)
            {
               Object[] args = {table, idCol, id.getValue() == null ? "null" :
                     id.getValue()};
               throw new PSDeployException(
                  IPSDeploymentErrors.INVALID_REPOSITORY_COLUMN_VALUE, args);
            }

            PSJdbcColumnData name = row.getColumn(nameCol);
            if (name == null || name.getValue() == null ||
               name.getValue().trim().length() == 0)
            {
               Object[] args = {table, nameCol, name.getValue() == null ? "null" :
                     name.getValue()};
               throw new PSDeployException(
                  IPSDeploymentErrors.INVALID_REPOSITORY_COLUMN_VALUE, args);
            }

            result.add(new PSEntrySet(id.getValue(), name.getValue()));
         }
      }

      return result;
   }

   /**
    * Gets the server's repository information
    *
    * @return The info, never <code>null</code>.
    *
    * @throws PSDeployException if the info cannot be obtained.
    */
   public PSDbmsInfo getServerRepositoryInfo() throws PSDeployException
   {
      if (m_repositoryInfo == null)
      {
         DataSourceInfo ds = getDataSourceInfo();
         if ( ds == null )
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
                  "Could not resolve datasource");
         
         IPSJndiDatasource dsSource   = ds.getDataSource();
         PSDatasourceConfig dsConfig = ds.getDataSourceConfig();
         
         m_repositoryInfo = new PSDbmsInfo(dsConfig.getName(), 
               dsSource.getDriverName(), dsSource.getServer(), 
               dsConfig.getDatabase(),  dsConfig.getOrigin(), 
               dsSource.getUserId(), dsSource.getPassword(), false);
      }
      return m_repositoryInfo;
   }
   
   
   /**
    * Util method to retrieve the datasource name
    * @return the datasource name
    */
   public String findADataSource()
   {
      return getDataSourceInfo().getDataSource().getName();
   }
   /**
    * A convenience routine to fetch the datasource
    * @return the datasource cannot be <code>null</code>
    */
   private DataSourceInfo getDataSourceInfo()
   {
      IPSJndiDatasource dsSource   = null;
      PSDatasourceConfig dsConfig = null;
      try
      {
         List<IPSJndiDatasource> jndiDSList = getJndiDataSourceList(); 
         
         IPSDatasourceResolver dsResolver   = getDataSourceConfigs();
         dsConfig = (PSDatasourceConfig) dsResolver
               .resolveDatasource((IPSConnectionInfo) null);
         
         String dataSourceName = dsConfig.getDataSource();
         for (Iterator iter = jndiDSList.iterator(); iter.hasNext();)
         {
            dsSource = (IPSJndiDatasource) iter.next();
            if ( dsSource.getName().equalsIgnoreCase(dataSourceName) )
               break;
         }
      }
      catch (PSDeployException e)
      {
         e.printStackTrace();
      }
      
      return new DataSourceInfo(dsSource, dsConfig);
   }
   /**
    * Private convenience method to fetch jndi data sources
    * @return the jndi datasource object
    * @throws PSDeployException, if there are any problems
    * 
    */
   private List<IPSJndiDatasource> getJndiDataSourceList()
         throws PSDeployException
   {
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      PSSecurityToken secTok    = PSRequest.getContextForRequest().getSecurityToken();
      List<IPSJndiDatasource> jndiSrcList = null;
      try
      {
         jndiSrcList = os.getJndiDatasources(null, secTok);
      }
      catch (PSAuthenticationRequiredException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.getLocalizedMessage());
      }
      catch (PSServerException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.getLocalizedMessage());      }
      catch (PSNotLockedException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.getLocalizedMessage());      }
      catch (PSLockedException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.getLocalizedMessage());      }
      catch (PSAuthorizationException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.getLocalizedMessage());
      }
      
      if (jndiSrcList == null || jndiSrcList.isEmpty())
         throw new IllegalStateException("Datasource cannot be null or empty");
      
      return jndiSrcList;
   }
   
   
   /**
    * A convenience method to fetch datasource resolver.
    * @return the datasource resolver
    * @throws PSDeployException, if any exceptions occur 
    */
   private IPSDatasourceResolver getDataSourceConfigs() throws PSDeployException
   {
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      PSSecurityToken secTok    = PSRequest.getContextForRequest().getSecurityToken();
      IPSDatasourceResolver dsResolver = null;
      try
      {
         dsResolver = os.getDatasourceConfigs(null, secTok);
      }
      catch (PSAuthenticationRequiredException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.getLocalizedMessage());      
      }
      catch (PSAuthorizationException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.getLocalizedMessage());      
      }
      catch (PSServerException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.getLocalizedMessage());      
      }
      catch (PSNotLockedException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.getLocalizedMessage());      
      }
      catch (PSLockedException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.getLocalizedMessage());      
      }
      return dsResolver;
   }

   /**
    * Gets a connection to the Rhythmyx repository.
    *
    * @return The connection, never <code>null</code>.  Caller is responsible
    * for calling <code>release()</code> on the connection when finished.
    *
    * @throws PSDeployException if there are any errors.
    */
   public Connection getRepositoryConnection()
      throws PSDeployException
   {
      Connection conn = null;

      try
      {
         conn = PSConnectionHelper.getDbConnection(null);
      }
      catch (SQLException e)
      {
         throw new PSDeployException(
            IPSDeploymentErrors.REPOSITORY_CONNECTION_ERROR,
               PSDeployException.formatSqlException(e));
      }
      catch (NamingException e)
      {
         throw new PSDeployException(
            IPSDeploymentErrors.REPOSITORY_CONNECTION_ERROR, 
            e.getLocalizedMessage());
      }

      return conn;
   }

   /**
    * Catalogs the specified table from the Rhythmyx server repository.
    *
    * @param tableName The name of the table, may not be <code>null</code> or
    * empty.
    * @param includeData If <code>true</code>, data will be included in the
    * catalog, otherwise only the schema will be cataloged.
    *
    * @return The table schema object, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>tableName</code> is invalid.
    * @throws PSDeployException if any errors occur.
    */
   public PSJdbcTableSchema catalogTable(String tableName,
      boolean includeData) throws PSDeployException
   {
      if (tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName may not be null or empty");

      PSJdbcTableSchema schema = getTableSchema(tableName);
      try
      {
         if (includeData)
            schema.setTableData(catalogTableData(schema, null, null));
      }
      catch (PSJdbcTableFactoryException e)
      {
         throw new PSDeployException(
            IPSDeploymentErrors.REPOSITORY_READ_WRITE_ERROR, 
            e.getLocalizedMessage());
      }
      
      return schema;
   }

   /**
    * Catalogs all rows from the specified table in the rx repository, applying
    * the optional filter if supplied.
    *
    * @param tableName The name of the table, may not be <code>null</code> or
    * empty.
    * @param columns an array of column names, may be <code>null</code>
    * or empty in which case all the columns are used in the select query.
    * The columns specified must belong to the specified <code>tableName</code>.
    * @param filter Optional filter to apply, may be <code>null</code>.
    *
    * @return The data, or <code>null</code> if no rows are returned by the
    * query.  All rows returned have their row action set to
    * {@link PSJdbcRowData#ACTION_INSERT}.
    *
    * @throws IllegalArgumentException if <code>tableName</code> is invalid.
    * @throws PSDeployException if any errors occur.
    */
   public PSJdbcTableData catalogTableData(String tableName, String[] columns,
      PSJdbcSelectFilter filter) throws PSDeployException
   {
      if (tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName may not be null or empty");

      return catalogTableDataHelper(tableName, null, columns, filter);
   }

   /**
    * Catalogs all rows from the table, specified by either
    * <code>PSJdbcTableSchema</code> or table-name (as <code>String</code>).
    *
    * <code>schema</code> is not <code>null</code>.
    * @param schema The schema for a database table. It may be
    * <code>null</code>. However, if it is <code>null</code>, then assume
    * <code>tableName</code> is not <code>null</code> or empty.
    * @param columns an array of column names, may be <code>null</code>
    * or empty in which case all the columns are used in the select query.
    * The columns specified must belong to the specified <code>tableName</code>.
    * @param filter Optional filter to apply, may be <code>null</code>.
    *
    * @return The data, or <code>null</code> if no rows are returned by the
    * query.  All rows returned have their row action set to
    * {@link PSJdbcRowData#ACTION_INSERT}.
    *
    * @throws IllegalArgumentException if <code>tableName</code> is invalid.
    * @throws PSDeployException if any errors occur.
    */
   public PSJdbcTableData catalogTableData(PSJdbcTableSchema schema,
      String[] columns, PSJdbcSelectFilter filter) throws PSDeployException
   {
      if (schema == null )
         throw new IllegalArgumentException("schema may not be null");

      return catalogTableDataHelper(null, schema, columns, filter);
   }

   /**
    * Catalogs all rows from the table, specified by either
    * <code>PSJdbcTableSchema</code> or table-name (as <code>String</code>).
    *
    * @param tableName The name of the table. It is may be <code>null</code>,
    * but assume never be empty. If it is <code>null</code>, then assume the
    * <code>schema</code> is not <code>null</code>.
    * @param schema The schema for a database table. It may be
    * <code>null</code>. However, if it is <code>null</code>, then assume
    * <code>tableName</code> is not <code>null</code> or empty.
    * @param columns an array of column names, may be <code>null</code>
    * or empty in which case all the columns are used in the select query.
    * The columns specified must belong to the specified <code>tableName</code>.
    * @param filter Optional filter to apply, may be <code>null</code>.
    *
    * @return The data, or <code>null</code> if no rows are returned by the
    * query.  All rows returned have their row action set to
    * {@link PSJdbcRowData#ACTION_INSERT}.
    *
    * @throws IllegalArgumentException if <code>tableName</code> is invalid.
    * @throws PSDeployException if any errors occur.
    */
   private PSJdbcTableData catalogTableDataHelper(String tableName,
      PSJdbcTableSchema schema, String[] columns, PSJdbcSelectFilter filter)
      throws PSDeployException
   {
      Connection conn = null;
      try
      {
         conn = getRepositoryConnection();
         PSJdbcDbmsDef dbmsDef = getDbmsDef();
         
         if ( schema == null )
            schema = getTableSchema(tableName);

         return PSJdbcTableFactory.catalogTableData(conn, dbmsDef, schema,
            columns, filter, PSJdbcRowData.ACTION_INSERT);
      }
      catch (PSJdbcTableFactoryException e)
      {
         throw new PSDeployException(
            IPSDeploymentErrors.REPOSITORY_READ_WRITE_ERROR,
               e.getLocalizedMessage());
      }
      finally
      {
         if (conn != null)
         {
            try {conn.close();} catch (SQLException e) {}
         }
      }

   }

   /**
    * Processing a database table according to the given
    * <code>PSJdbcTableSchema</code> object.
    *
    * @param schema The schema object to be processed, may not be
    * <code>null</code>
    *
    * @throws PSDeployException if there is an error.
    */
   public void processTable(PSJdbcTableSchema schema)
      throws PSDeployException
   {
      if (schema == null)
         throw new IllegalArgumentException("schema may not be null");

      Connection conn = null;
      try
      {
         conn = getRepositoryConnection();
         PSJdbcDbmsDef dbmsDef = getDbmsDef();
         PSJdbcTableFactory.processTable(conn, dbmsDef, schema, null, false);
      }
      catch (PSJdbcTableFactoryException e)
      {
         throw new PSDeployException(
            IPSDeploymentErrors.REPOSITORY_READ_WRITE_ERROR,
               e.getLocalizedMessage());
      }
      finally
      {
         if (conn != null)
         {
            try {conn.close();} catch (SQLException e) {}
         }
      }
   }

   /**
    * Pricessing a database table according to the given schema and table data.
    *
    * @param schema The scehma object to be processed, may not be
    * <code>null</code>.
    * @param tableData The table data to be processed, may not be
    * <code>null</code>.
    *
    * @throws PSDeployException if there is an error.
    */
   public void processTable(PSJdbcTableSchema schema, PSJdbcTableData tableData)
      throws PSDeployException
   {
      try {
         schema.setTableData(tableData);
      }
      catch (Exception e){
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      processTable(schema);
   }

   /**
    * Pricessing a database table from a given schema, table name and row data.
    *
    * @param schema The schema object, may not <code>null</code>.
    * @param tableName The table name, may not <code>null</code> or empty.
    * @param rowData The row data, may not <code>null</code>.
    *
    * @throws PSDeployException if an error occurs.
    */
   public void processTable(PSJdbcTableSchema schema, String tableName,
      PSJdbcRowData rowData) throws PSDeployException
   {
      List<PSJdbcRowData> rowList = new ArrayList<PSJdbcRowData>();
      rowList.add(rowData);
      PSJdbcTableData tblData;
      tblData = new PSJdbcTableData(tableName, rowList.iterator(), false);

      processTable(schema, tblData);
   }

   /**
    * Gets a table factory dbms def object for the Rhythmyx server repository.
    *
    * @return The dbms def object, never <code>null</code>.
    *
    * @throws PSDeployException If any errors occur.
    */
   public PSJdbcDbmsDef getDbmsDef() throws PSDeployException
   {
      if (m_dbmsDef == null)
      {
         try
         {
            IPSConnectionInfo info = null;
            m_dbmsDef = new PSServerJdbcDbmsDef(info);
         }
         catch (SQLException e)
         {
            throw new PSDeployException(
               IPSDeploymentErrors.REPOSITORY_CONNECTION_ERROR,
                  PSDeployException.formatSqlException(e));
         }
         catch (NamingException e)
         {
            throw new PSDeployException(
               IPSDeploymentErrors.REPOSITORY_CONNECTION_ERROR, 
               e.getLocalizedMessage());
         }
         
      }

      return m_dbmsDef;
   }

   /**
    * Gets a tablefactory datatype map for the Rx server repository's driver.
    *
    * @return The dbms def, never <code>null</code>.
    *
    * @throws PSDeployException If any errors occur.
    */
   public PSJdbcDataTypeMap getDataTypeMap() throws PSDeployException
   {
      if (m_dataTypeMap == null)
      {
         PSConnectionDetail conn=null;
         try
         {
            conn = PSConnectionHelper.getConnectionDetail();
         }
         catch (NamingException nex)
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
                  nex.toString());
         }
         catch (SQLException sqe)
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
                  sqe.toString());
         }
         
         try
         {
            m_dataTypeMap = new PSJdbcDataTypeMap(
               null, conn.getDriver(), null);
         }
         catch (Exception e)
         {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR,
                  e.getLocalizedMessage());
         }
      }

      return m_dataTypeMap;
   }

   /**
    * Reserves a new id for the specified table name
    *
    * @param tableName The name used as a key in the NEXTNUMBER table, may not
    * be <code>null</code> or empty.
    *
    * @return The id.
    *
    * @throws IllegalArgumentException if <code>name</code> is invalid.
    * @throws PSDeployException If there are any errors.
    */
   public int getNextId(String tableName) throws PSDeployException
   {
      if (tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName may not be null or empty");

      try
      {
         return PSIdGenerator.getNextId(tableName);
      }
      catch (SQLException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            PSDeployException.formatSqlException(e));
      }

   }

   /**
    * Reserves a block of new ids for the specified table name
    *
    * @param tableName The name used as a key in the NEXTNUMBER table, may not
    * be <code>null</code> or empty.
    * @param blockSize The number of ids need to be reserved.
    *
    * @return The id blocks in <code>int[]</code>.
    *
    * @throws IllegalArgumentException if <code>name</code> is invalid.
    * @throws PSDeployException If there are any errors.
    */
   static public int[] getNextIdBlock(String tableName, int blockSize)
      throws PSDeployException
   {
      if (tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName may not be null or empty");

      try
      {
         return PSIdGenerator.getNextIdBlock(tableName, blockSize);
      }
      catch (SQLException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            PSDeployException.formatSqlException(e));
      }
   }

   /**
    * Gets a column value from the given parameters.
    *
    * @param table The table name, may not <code>null</code> or empty.
    * @param column The column name, may not <code>null</code> or empty.
    * @param row The row data, which contains the value, may not
    * <code>null</code>.
    *
    * @return The value as <code>String</code>, never <code>null</code>, may
    * be empty.
    *
    * @throws PSDeployException if any error occurs.
    */
   public String getColumnString(String table, String column,
      PSJdbcRowData row) throws PSDeployException
   {
       if (table == null || table.trim().length() == 0)
          throw new IllegalArgumentException("table may not be null or empty");
       if (column == null || column.trim().length() == 0)
          throw new IllegalArgumentException("column may not be null or empty");
       if (row == null)
          throw new IllegalArgumentException("row may not be null or empty");

       PSJdbcColumnData cdata= row.getColumn(column);

       if (cdata == null || cdata.getValue() == null ||
          cdata.getValue().trim().length() == 0)
       {
          Object[] args = {table, column,
          cdata.getValue() == null ? "null" : cdata.getValue()};
          throw new PSDeployException(
              IPSDeploymentErrors.INVALID_REPOSITORY_COLUMN_VALUE, args);
      }

      return cdata.getValue();
   }

   /**
    * Gets a column value as an <code>Date</code> from the given parameters.
    *
    * @param table The table name, may not <code>null</code> or empty.
    * @param column The column name, may not <code>null</code> or empty.
    * @param row The row data, which contains the value, may not
    * <code>null</code>.
    *
    * @return The value as <code>int</code>, never <code>null</code>.
    *
    * @throws PSDeployException if any error occurs.
    */
   public Date getColumnDate(String table, String column,
      PSJdbcRowData row) throws PSDeployException
   {
       if (table == null || table.trim().length() == 0)
          throw new IllegalArgumentException("table may not be null or empty");
       if (column == null || column.trim().length() == 0)
          throw new IllegalArgumentException("column may not be null or empty");
       if (row == null)
          throw new IllegalArgumentException("row may not be null or empty");

      String sDate = getColumnString(table, column, row);
      Timestamp dateTime;
      try
      {
         dateTime = Timestamp.valueOf(sDate);
      }
      catch (IllegalArgumentException e)
      {
         Object[] args = {table, column, sDate};
         throw new PSDeployException(
            IPSDeploymentErrors.INVALID_REPOSITORY_COLUMN_VALUE, args);
      }
      return dateTime;
   }

   /**
    * Gets a column value as an <code>int</code> from the given parameters.
    *
    * @param table The table name, may not <code>null</code> or empty.
    * @param column The column name, may not <code>null</code> or empty.
    * @param row The row data, which contains the value, may not
    * <code>null</code>.
    *
    * @return The value as <code>int</code>.
    *
    * @throws PSDeployException if any error occurs.
    */
   public int getColumnInt(String table, String column,
      PSJdbcRowData row) throws PSDeployException
   {
       if (table == null || table.trim().length() == 0)
          throw new IllegalArgumentException("table may not be null or empty");
       if (column == null || column.trim().length() == 0)
          throw new IllegalArgumentException("column may not be null or empty");
       if (row == null)
          throw new IllegalArgumentException("row may not be null");

      String sNumber = getColumnString(table, column, row);
      int number;
      try
      {
         number = Integer.parseInt(sNumber);
      }
      catch (NumberFormatException e)
      {
         Object[] args = {table, column, sNumber};
         throw new PSDeployException(
            IPSDeploymentErrors.INVALID_REPOSITORY_COLUMN_VALUE, args);
      }
      return number;
   }

   /**
    * Get a application name from a given row, column of a table. The value of
    * the column must contain a URL string.
    * 
    * @param table The table name, may not <code>null</code> or empty.
    * @param column The column name, may not <code>null</code> or empty.
    * @param row The row data, which contains the value, may not
    * <code>null</code>.
    * 
    * @return The retrieved application name, it will never be <code>null</code>
    * or empty.
    * 
    * @throws IllegalArgumentException if a parameter is invalid.
    * @throws PSDeployException If unable to retrieve an application name from
    * the specified <code>column</code> and <code>row</code>, or any other 
    * error occurs.
    */
   public String getColumnAppName(String table, String column,
      PSJdbcRowData row) throws PSDeployException
   {
       if (table == null || table.trim().length() == 0)
          throw new IllegalArgumentException("table may not be null or empty");
       if (column == null || column.trim().length() == 0)
          throw new IllegalArgumentException("column may not be null or empty");
       if (row == null)
          throw new IllegalArgumentException("row may not be null or empty");

      String url;
      url = getColumnString(table, column, row);
          
      String appName = PSDeployComponentUtils.getAppName(url);
         
      if (appName == null || appName.trim().length() == 0)
      {
         Object[] args = {table, column, url};
         throw new PSDeployException(
            IPSDeploymentErrors.INVALID_REPOSITORY_COLUMN_VALUE, args);
      }
      
      return appName;
   }
      
   
   /**
    * Sets the update key from a given column name and a schema.
    *
    * @param colName The column name, may not be <code>null</code> or empty.
    * @param schema The schema object, may not be <code>null</code>.
    *
    * @throws PSDeployException if an error occurs.
    */
   public void setUpdateKeyForSchema(String colName, PSJdbcTableSchema schema)
      throws PSDeployException
   {
       if (colName == null || colName.trim().length() == 0)
          throw new IllegalArgumentException(
            "colName may not be null or empty");
       if (schema == null)
          throw new IllegalArgumentException("schema may not be null");

      List<String> cols = new ArrayList<String>();
      cols.add(colName);
      setUpdateKeyForSchema(cols.iterator(), schema);
   }

   /**
    * Sets the update key from a given columns and schema.
    *
    * @param columns A list of column names in <coce>String</code>, it may not
    * be <code>null</code> or empty.
    * @param schema The schema object, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if a parameter is invalid.
    * @throws PSDeployException if an error occurs.
    */
   public void setUpdateKeyForSchema(Iterator columns, PSJdbcTableSchema schema)
      throws PSDeployException
   {
       if (columns == null || columns.hasNext() == false)
          throw new IllegalArgumentException(
            "columns may not be null or empty");
       if (schema == null)
          throw new IllegalArgumentException("schema may not be null");

      try {
         PSJdbcUpdateKey updKey = new PSJdbcUpdateKey(columns);
         schema.setUpdateKey(updKey);
      }
      catch (Exception e){
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
   }

   /**
    * Creates a <code>PSJdbcRowData</code> for one column (name and value),
    * and an action.
    *
    * @param colName The column name, may not be <code>null</code> or empty.
    * @param colValue The column value, may not be <code>null</code> or empty.
    * @param action The action taken, assume is one of the
    * <code>PSJdbcRowData.ACTION_XXX</code> values.
    *
    * @return The created <code>PSJdbcRowData</code> object, never
    * <code>null</code>.
    */
   public PSJdbcRowData getRowDataForOneColumn(String colName,
      String colValue, int action)
   {
      if (colName == null || colName.trim().length() == 0)
         throw new IllegalArgumentException("colName may not be null or empty");
      if (colValue == null || colValue.trim().length() == 0)
         throw new IllegalArgumentException(
            "colValue may not be null or empty");

      List<PSJdbcColumnData> cols = new ArrayList<PSJdbcColumnData>();
      PSJdbcColumnData col = new PSJdbcColumnData(colName, colValue);
      cols.add(col);

      PSJdbcRowData rowData = new PSJdbcRowData(cols.iterator(), action);

      return rowData;
   }

   /**
    * Get an IN filter (in the format of: IN (XYZ, ABC, ...) from a list of ids
    * for a specified column.
    * 
    * @param ids The id list over one or more <code>String</code> items. It 
    * may not be <code>null</code> or empty.
    * @param idCol The column name for the filter, it may not be 
    * <code>null</code> or empty.
    * 
    * @return Generated IN filter, will never be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any parameter is invalid
    */
   public PSJdbcSelectFilter getFilterInFromIds(Iterator ids, String idCol)
   {
      if (ids == null || ids.hasNext() == false)
         throw new IllegalArgumentException("ids may not be null or empty");
      if (idCol == null || idCol.trim().length() == 0)
         throw new IllegalArgumentException("idCol may not be null or empty");
         
      StringBuffer sIds = new StringBuffer(0);
      
      while ( ids.hasNext() )
      {
         String id = (String) ids.next();
         if ( sIds.length() == 0 )
            sIds.append("(" + id);
         else
            sIds.append("," + id);
      }
      sIds.append(")");

      PSJdbcSelectFilter filter = null;

      filter = new PSJdbcSelectFilter(idCol,
            PSJdbcSelectFilter.IN, sIds.toString(), Types.INTEGER);

      return filter;      
   }   
   
   /**
    * Get a next id for a specified column and table from the database.
    * 
    * @param table The table name for the id, may not be <code>null</code> or
    * empty.
    * @param idCol The column name that the id is going to be used for, it may
    * not be <code>null</code> or empty.
    * @param filterCol The column name that may be filtered on when generating 
    * the next id, it may be <code>null</code> if no desire to use the filter.
    * @param filterColValue The value of the <code>filterCol</code>. 
    * 
    * @return A next id for the column and table.
    * 
    * @throws IllegalArgumentException if a parameter is invalid.
    * @throws PSDeployException if an error occurs.
    */
   public synchronized Integer getNextNumberFromDB(String table, String idCol, 
      String filterCol, int filterColValue)
      throws PSDeployException
   {
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
      if (idCol != null && idCol.trim().length() == 0)
         throw new IllegalArgumentException("idCol may not be null or empty");
      if (filterCol != null && filterCol.trim().length() == 0)
         throw new IllegalArgumentException(
            "filterCol may not be empty");

      Connection conn = null;

      Integer iResult;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      
      try
      {
         conn = getRepositoryConnection();

         String query = "SELECT MAX(" + idCol + ") FROM " + table;

         if ((filterCol != null) && (filterCol.trim().length() != 0) &&
             (!idCol.equalsIgnoreCase(filterCol)) )
         {
            query += " WHERE " + filterCol + " = ?";
            stmt = PSPreparedStatement.getPreparedStatement(conn, query);
            stmt.setInt(1, filterColValue);
         }
         else
         {
            stmt = PSPreparedStatement.getPreparedStatement(conn, query);
         }

         rs = stmt.executeQuery();
         if(false == rs.next())
            iResult = new Integer(1);
         else
            iResult = new Integer(rs.getInt(1)+1);
      }
      catch (SQLException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            PSDeployException.formatSqlException(e));
      }
      finally
      {
         if(null != rs)
            try {rs.close();} catch (Exception e){};
         if(null != stmt)
            try {stmt.close();} catch (Exception e) {};

         if (conn != null)
         {
            try {conn.close();} catch (SQLException e) {}
         }
      }
      return iResult;
   }   

   /**
    * Get a next id for a specified column and table from the next number 
    * repository in memory. It manages the next number repository in the way
    * that it always get its base number from the database (by calling
    * {@link #getNextNumberFromDB(String, String, String, int)}, then 
    * generating the subsequent number in memory until the repository is 
    * cleared by a call to {@link #clearNextIdInMemory()}. It will start the 
    * same above process again afterwords.
    * 
    * @param table The table name for the id, may not be <code>null</code> or
    * empty.
    * @param idCol The column name that the id is going to be used for, it may
    * not be <code>null</code> or empty.
    * @param filterCol The column name that may be filtered on when generating 
    * the next id, it may be <code>null</code> if no desire to use the filter.
    * @param filterColValue The value of the <code>filterCol</code>. It may not
    * be <code>null</code> or empty if <code>filterCol</code> is not 
    * <code>null</code> or empty. It must be an integer string.
    * 
    * @return A next id for the column and table.
    * 
    * @throws IllegalArgumentException if a parameter is invalid.
    * @throws PSDeployException if an error occurs.
    */
   public synchronized int getNextIdInMemory(String table, String idCol, 
      String filterCol, String filterColValue) throws PSDeployException
   {
      int iValue = 0;

      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
      if (idCol == null || idCol.trim().length() == 0)
         throw new IllegalArgumentException("idCol may not be null or empty");
      if (filterCol != null && filterCol.trim().length() > 0)
      {
         if (filterColValue == null || filterColValue.trim().length() == 0)
         {
            throw new IllegalArgumentException(
               "filterColValue may not be null or empty when filterCol is not");
         }
         else
         {
            try {
               iValue = Integer.parseInt(filterColValue);
            }
            catch (NumberFormatException e) {
               throw new IllegalArgumentException(
                  "filterColValue must be an integer string");
            }
         }
      }

      String key;
      if (filterCol == null)
         key = table;
      else
         key = table + ":" + filterCol + ":" + filterColValue;
         
      Integer nextNumber = (Integer) nextNumberMap.get(key); 

      if (nextNumber == null)
      {
         nextNumber = getNextNumberFromDB(table, idCol, filterCol, iValue);
      }
      else
      {
         nextNumber = new Integer(nextNumber.intValue() + 1);
      }

      nextNumberMap.put(key, nextNumber);

      return nextNumber.intValue();
   }

   /**
    * Get a list of next ids in a specified block from the next number 
    * repository in memory.
    * 
    * @param table The table name for the ids, may not be <code>null</code> or
    * empty.
    * @param idCol The column name that the ids is going to be used for, it may
    * not be <code>null</code> or empty.
    * @param filterCol The column name that may be filtered on when generating 
    * the next ids, it may be <code>null</code> if no desire to use the filter.
    * @param filterColValue The value of the <code>filterCol</code>. It may not
    * be <code>null</code> or empty if <code>filterCol</code> is not 
    * <code>null</code> or empty.
    * @param blockSize The number of ids need to be generated, must be greater
    * than <code>0</code>.
    * 
    * @return Array of ids with length of <code>blockSize</code>. It will
    * never be <code>null</code> or any other length.
    * 
    * @throws IllegalArgumentException if a parameter is invalid.
    * @throws PSDeployException if an error occurs.
    */
   public int[] getNextIdBlockInMemory(String table, String idCol, 
      String filterCol, String filterColValue, int blockSize) 
      throws PSDeployException
   {
      if (table == null || table.trim().length() == 0)
         throw new IllegalArgumentException("table may not be null or empty");
      if (idCol == null || idCol.trim().length() == 0)
         throw new IllegalArgumentException("idCol may not be null or empty");
      if (blockSize <= 0)
         throw new IllegalArgumentException("blockSize may not be <= 0");
      if (filterCol != null && filterCol.trim().length() == 0)
      {
         if (filterColValue == null || filterColValue.trim().length() == 0)
            throw new IllegalArgumentException(
               "filterColValue may not be null or empty when filterCol is not");
      }

      int[] ids = new int[blockSize];
            
      for (int i=0; i < blockSize; i++)
         ids[i] = getNextIdInMemory(table, idCol, filterCol, filterColValue);
      
      return ids;
   }
      
   /**
    * Clear the next number repository in memory.
    */
   public void clearNextIdInMemory()
   {
      nextNumberMap.clear();
   }
   
   /**
    * Determines if the given table name is a table used in of the shared group.
    * 
    * @param tableName the table name in question, assumed not <code>null</code>
    * or empty.
    * 
    * @return <code>true</code> if it is a shared table; otherwise 
    * <code>false</code>.
    * 
    * @throws PSDeployException if an error occurs.
    */
   private boolean isSharedTable(String tableName) throws PSDeployException
   {
      for (String table : PSDependencyUtils.getSharedGroupTables())
      {
         if (table.equalsIgnoreCase(tableName))
            return true;
      }
      return false;
   }
   /**
    * Gets the dependency type for the specified table.  First checks to see if 
    * the table's type was defined in the resource bundle, otherwise checks
    * {@link #isSystemTable(String)}.
    * 
    * @param tableName The name of the table, may not be <code>null</code> or 
    * empty.
    * 
    * @return The type, one of the <code>PSDependency.TYPE_XXX</code> values.
    * 
    * @throws IllegalArgumentException if <code>tablename</code> is 
    * <code>null</code> or empty.
    * @throws PSDeployException if any errors occur.
    */
   public int getDependencyType(String tableName) throws PSDeployException
   {
      int type;
      
      loadTableTypeDefs();
      Integer objType = (Integer)m_tableTypes.get(tableName);
      
      if (objType != null)
      {
         type = objType.intValue();
      }
      else if (isSystemTable(tableName))
      {
         type = PSDependency.TYPE_SYSTEM;
      }
      else if (isSharedTable(tableName))
      {
         type = PSDependency.TYPE_LOCAL;
      }
      else
      {
         type = PSDependency.TYPE_SHARED;
      }
      
      return type;
   }
   
   /**
    * Enables the table schema cache for non-system tables.  System table 
    * schemas are always cached.  Calling this method when the schema cache is 
    * already enabled does not result in an error, but will clear the cache.
    */
   void enableSchemaCache()
   {
      m_appSchemaMap = new HashMap<String, PSJdbcTableSchema>();
   }
   
   /**
    * Clears and diables the table schema cache for non-system tables.  Should 
    * be called at some point after enabling the schema cache with a call to 
    * <code>enableSchemaCache()</code>.  Calling this method when the schema
    * cache is not enabled does not result in an error.
    */
   void disableSchemaCache()
   {
      m_appSchemaMap = null;
   }
   
   /**
    * Loads the system table defintions if they have not yet been loaded.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private void loadSystemTableDefs() throws PSDeployException
   {
      if (m_systemTables == null)
      {
         m_systemTables = new HashSet<String>();
         File schemaFile = new File(PSServer.getRxDir().getAbsolutePath() + "/"
               + PSServer.SERVER_DIR, SYSTEM_TABLE_SCHEMA_FILE);
         Document schemaDoc = getXmlDocumentFromFile(schemaFile);
         try
         {
            PSJdbcTableSchemaCollection schemaColl = 
               new PSJdbcTableSchemaCollection(schemaDoc, getDataTypeMap());
            Iterator schemas = schemaColl.iterator();
            while (schemas.hasNext())
            {
               PSJdbcTableSchema schema = (PSJdbcTableSchema)schemas.next();
               m_systemTables.add(schema.getName());
            }
         }
         catch (PSJdbcTableFactoryException e)
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.toString());
         }
      }
   }
   
   /**
    * Loads the table types specified in the resource bundle if they have not
    * yet been loaded.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private void loadTableTypeDefs() throws PSDeployException
   {
      if (m_tableTypes == null)
      {
         m_tableTypes = new HashMap<String, Integer>();
         ResourceBundle bundle = getBundle();
         Enumeration tables = bundle.getKeys();
         try 
         {
            while (tables.hasMoreElements())
            {
               String key = (String)tables.nextElement();
               m_tableTypes.put(key, new Integer(bundle.getString(key)));
            }
         }
         catch (NumberFormatException e) 
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.toString());
         }
      }
   }

   /**
    * Determines if the specified table is a system table.
    * 
    * @param tableName The name of the table, assumed not <code>null</code> or 
    * empty.
    * 
    * @return <code>true</code> if it is a system table, <code>false</code>
    * otherwise.
    * 
    * @throws PSDeployException if any errors occur.
    */
   private boolean isSystemTable(String tableName) throws PSDeployException
   {
      loadSystemTableDefs();
      return m_systemTables.contains(tableName);
   }

   /**
    * This method is used to get the resource bundle.
    *
    * @return the bundle, never <code>null</code>.
    * 
    * @throws MissingResourceException if the bundle cannot be loaded.
    */
   private ResourceBundle getBundle()
   {
      if (m_bundle == null)
      {
         m_bundle = ResourceBundle.getBundle("com.percussion.deployer.server." + 
            "PSDbmsHelperResources");
      }      

      return m_bundle;
   }
   
   
   /**
    * Create an XML document from the contents of the supplied file.
    * 
    * @param file The file, may not be <code>null</code>.  
    * 
    * @return The document, never <code>null</code>.
    * 
    * @throws PSDeployException if the file does not exist, or if a valid XML 
    * document cannot be created from its contents.
    */
   private Document getXmlDocumentFromFile(File file) throws PSDeployException
   {
      if (file == null)
         throw new IllegalArgumentException("file may not be null");
      
      FileInputStream in = null;
      try 
      {
         in = new FileInputStream(file);
         return PSXmlDocumentBuilder.createXmlDocument(in, false);
      }
      catch (Exception e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      finally 
      {
         if (in != null)
            try {in.close();} catch (IOException e) {}
      }
   }
   
   /**
    * Gets the specified table's schema.
    * 
    * @param tableName The name of the table, may not be <code>null</code> or
    * empty.
    *
    * @return The table schema object, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>tableName</code> is invalid.
    * @throws PSDeployException if any errors occur.
    */
   private PSJdbcTableSchema getTableSchema(String tableName) 
      throws PSDeployException
   {
      PSJdbcTableSchema schema = findTable(tableName);
      
      if ( schema == null ) // table does not exist
      {
         Object[] args = {tableName};
         throw new PSDeployException(IPSDeploymentErrors.UNABLE_FIND_TABLE,
            args);
      }
         
      return schema;
   }
   

   /**
    * Finds the specified table's schema.
    * 
    * @param tableName The name of the table, may not be <code>null</code> or
    * empty.
    *
    * @return The table schema object, it may be <code>null</code> if the
    * table does not exist.
    *
    * @throws IllegalArgumentException if <code>tableName</code> is invalid.
    * @throws PSDeployException if any errors occur.
    */
   public PSJdbcTableSchema findTable(String tableName) 
      throws PSDeployException
   {
      if (tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName may not be null or empty");
      
      PSJdbcTableSchema schema = null;
      boolean canCache = false;
      boolean isSystemTable = (getDependencyType(tableName) == 
         PSDependency.TYPE_SYSTEM);
      
      // check cache if a system table
      if (isSystemTable)
      {
         canCache = true;
         schema = (PSJdbcTableSchema)m_sysSchemaMap.get(tableName);
      }
      else if (m_appSchemaMap != null)
      {
         // caching non-system schema
         canCache = true;
         schema = (PSJdbcTableSchema)m_appSchemaMap.get(tableName);
      }
      
      if (schema == null)
      {
         Connection conn = null;
         try
         {
            conn = getRepositoryConnection();
            schema = PSJdbcTableFactory.catalogTable(conn,
               getDbmsDef(), getDataTypeMap(), tableName, false);
         }
         catch (PSJdbcTableFactoryException e)
         {
            throw new PSDeployException(
               IPSDeploymentErrors.REPOSITORY_READ_WRITE_ERROR,
                  e.getLocalizedMessage());
         }
         finally
         {
            if (conn != null)
            {
               try {conn.close();} catch (SQLException e) {}
            }
         }
         
         if (canCache)
         {
            if (isSystemTable)
               m_sysSchemaMap.put(tableName, schema);
            else
               m_appSchemaMap.put(tableName, schema);
         }
      }
        
      return schema;
   }
   
   
   // a private class which acts as a container for datasource and datasource
   // config. Used by getServerRepositoryInfo() 
   private class DataSourceInfo 
   {
      IPSJndiDatasource m_dataSource;
      PSDatasourceConfig m_dsConfig;
      
      /** CTOR
       * @param ds the jndi datasource never <code>null</code>
       * @param cfg the datasource config never <code>null</code>
       */
      public DataSourceInfo(IPSJndiDatasource ds, PSDatasourceConfig cfg)
      {
         if ( ds == null )
            throw new IllegalArgumentException("datasource may not be null");         
         if ( cfg == null )
            throw new IllegalArgumentException("datasource config may not be null");
         
         m_dataSource = ds;
         m_dsConfig = cfg;
      }
      
      /**
       * getter for jndi datasource
       * @return the jndi datasource
       */
      public IPSJndiDatasource getDataSource()
      {
         return m_dataSource;
      }
      
      /**
       * getter for datasource config
       * @return datasource config
       */
      public PSDatasourceConfig getDataSourceConfig()
      {
         return m_dsConfig;
      }
   }

   
   /**
    * To manage next number in memory for the ids that do not managed by the
    * NEXTNUMBER table, such as WORKFLOWAPPS and those workflow related tables.
    * It will never to <code>null</code>, but may be empty. It uses table
    * and/or combination of table and filter (in <code>String</code> as key) 
    * map to its corresponding next number (in <code>Integer</code> as value of
    * the map).
    * 
    * It is modified by <code>getNextIdInMemory()</code>, but be cleared by
    * <code>clearNextIdInMemory()</code>
    */
   private Map<String, Integer> nextNumberMap = new HashMap<String, Integer>();
      
   /**
    * Singleton instance of this class, set by first call to
    * {@link #getInstance()}, never <code>null</code> or modified after that.
    */
   private static PSDbmsHelper m_instance = null;

   /**
    * The dbms def for the rx server repository.  <code>null</code> until the
    * first call to <code>getDbmsDef()</code>, never <code>null</code> or
    * modified after that.
    */
   private PSJdbcDbmsDef m_dbmsDef;

   /**
    * The data type map for the driver used to access the rx server repository.
    * <code>null</code> until the first call to <code>getDataTypeMap()</code>,
    * never <code>null</code> or modified after that.
    */
   private PSJdbcDataTypeMap m_dataTypeMap;

   /**
    * Name of file containing the system table schemas.
    */
   private static final String SYSTEM_TABLE_SCHEMA_FILE = "sys_cmsTableDef.xml";
   
   /**
    * Set of system table names as <code>String</code> objects, initialized 
    * during first call to <code>loadSystemTableDefs()</code>, never 
    * <code>null</code> or empty after that.
    */
   private Set<String> m_systemTables = null;
   
   /**
    * Map of tables and their types specified by the resource bundle, 
    * initialized during first call to <code>loadTableTypeDefs()</code>, never 
    * <code>null</code> or modified after that, may be empty.  Key is the table 
    * name as a <code>String</code>, and value is the dependency type as an 
    * <code>Integer</code>.
    */
   private Map<String, Integer> m_tableTypes = null;
   
   /**
    * String bundle used to define table dependency type overrides.  
    * <code>null</code> until loaded by a call to {@link #getBundle()}, never 
    * <code>null</code> or modified after that.
    */
   private ResourceBundle m_bundle = null;
   
   /**
    * Map of system table schemas, where key is tablename as a 
    * <code>String</code> and value is the corresponding 
    * <code>PSJdbcTableSchema</code> object.  Never <code>null</code>, may be 
    * empty.  Updated by calls to <code>getTableSchema()</code>.
    */
   private Map<String, PSJdbcTableSchema> m_sysSchemaMap = 
                                       new HashMap<String, PSJdbcTableSchema>();
   
   /**
    * Map of non-system table schemas, where key is tablename as a 
    * <code>String</code> and value is the corresponding 
    * <code>PSJdbcTableSchema</code> object.  <code>null</code> when the cache 
    * is disabled, is initialzed by calls to <code>enableSchemaCache()</code>, 
    * set back to <code>null</code> by calls to 
    * <code>disableSchemaCache()</code>, may be empty.  Entries may be updated 
    * by calls to <code>getTableSchema()</code>.
    */
   private Map<String, PSJdbcTableSchema> m_appSchemaMap = null;
   
   /**
    * Server's repository info, <code>null</code> until first call to
    * <code>getServerRepositoryInfo()</code>, never <code>null</code> after
    * that.
    */
   private PSDbmsInfo m_repositoryInfo = null;
   
}
