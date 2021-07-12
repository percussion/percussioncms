/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

import com.percussion.tablefactory.tools.PSCatalogTableData;
import com.percussion.util.PSBase64Encoder;
import com.percussion.util.PSLogger;
import com.percussion.util.PSProperties;
import com.percussion.util.PSSQLStatement;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.PSJdbcUtils;
import com.percussion.utils.xml.PSXmlNormalizingReader;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
*  This class is used to install and upgrade tables via Jdbc.
*/
public class PSJdbcTableFactory
{

   private static final Logger log = LogManager.getLogger(PSJdbcTableFactory.class);

   /**
    * Queries the backend and returns a table schema object with the info
    * about the table.  May also retrieve the current data if requested.
    * Binary data is not supported in general, although it may work for certain
    * drivers.
    *
    * @param conn A valid connection to use, must point to same database server
    * as dbmsDef.   May not be <code>null</code>.  Connection is not closed
    * by this method.
    * @param dbmsDef Used to connect to the database and provides correct
    * schema/origin.  May not be <code>null</code>.
    * @param dataTypeMap Required to create the tableSchema object.  May not
    * be <code>null</code>.
    * @param tableMetaMap Used to cache the table meta data. The meta data will
    * be created and added to the map if the processed table does not exist
    * in the map. The map key is the table name in <code>String</code>; the
    * map value is <code>PSJdbcTableMetaData</code> object of the related
    * table. It may be <code>null</code> if no need to cache the meta data.
    * @param tableName The unqualified name of the table to catalog.  May not be
    * <code>null</code> or empty.
    * @param includeData If <code>true</code>, a PSJdbcTableData object will
    * be created, populated with the current data from the table, and set on the
    * schema object.  Any existing table data information in the schema object
    * is first discarded.
    *
    * @return The tableSchema object filled out with columns, keys, and indexes.
    * If table does not exist, returns <code>null</code>.  If includeData is
    * <code>true</code>, then a PSJdbcTableData object with all existing rows
    * will be set on the tableSchema object as well.  Beware that this may be a
    * very large amount of data, and could require very large amounts of
    * memory!!  Action on all components will be set to {@link
    * PSJdbcTableComponent#ACTION_CREATE}
    *
    * @throws IllegalArgumentException if any parameter is <code>null</code>, or
    * if tableName is empty.
    * @throws PSJdbcTableFactoryException if there are any other errors.
    */
   public static PSJdbcTableSchema catalogTable(
      Connection conn,
      PSJdbcDbmsDef dbmsDef,
      Map tableMetaMap,
      PSJdbcDataTypeMap dataTypeMap,
      String tableName,
      boolean includeData)
      throws PSJdbcTableFactoryException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (dataTypeMap == null)
         throw new IllegalArgumentException("dataTypeMap may not be null");

      if (tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName may not be null or empty");
            
      PSJdbcTableSchema tableSchema = null;
      try
      {
         PSJdbcTableMetaData tmd = null;
         if (tableMetaMap != null)
            tmd = (PSJdbcTableMetaData)tableMetaMap.get(tableName);

         if (tmd == null)
         {
            // need to see if table exists before starting
            tmd = PSJdbcTableFactory.getTableMetaData(conn, dbmsDef,
               dataTypeMap, tableName);

            if (tableMetaMap != null) // cache the meta data as needed.
               tableMetaMap.put(tableName, tmd);
         }
         if (tmd.exists())
         {
            /* if we couldn't get columns for some reason, next line will throw
             * exception for us
             */
            tableSchema = new PSJdbcTableSchema(tableName,
               tmd.getColumns());

            if (tmd.isView())
            {
               tableSchema.setIsView(true);
            }

            Iterator pkeys = tmd.getPrimaryKeyColumns();
            if (pkeys.hasNext())
               tableSchema.setPrimaryKey(new PSJdbcPrimaryKey(
                  tmd.getPrimaryKeyName(), pkeys,
                  PSJdbcTableComponent.ACTION_CREATE));

            List<PSJdbcForeignKey> fKeys = new ArrayList<PSJdbcForeignKey>();
            tableSchema.setForeignKeys(fKeys);
            
            for (Entry<String, List<String[]>> fKeysEntry : tmd.getForeignKeys().entrySet()) {
               String fkName = fKeysEntry.getKey();
               List<String[]> fkeysCols = fKeysEntry.getValue();
               PSJdbcForeignKey newFKey = new PSJdbcForeignKey(fkeysCols.iterator(),
                     PSJdbcTableComponent.ACTION_CREATE);
               newFKey.setName(fkName);
               fKeys.add(newFKey);
            }

            Iterator indexes = tmd.getIndexObjects(
               PSJdbcIndex.TYPE_UNIQUE | PSJdbcIndex.TYPE_NON_UNIQUE);
            while (indexes.hasNext())
            {
               PSJdbcIndex index = (PSJdbcIndex)indexes.next();
               tableSchema.setIndex(new PSJdbcIndex(index.getName(),
                  index.getColumnNames(), PSJdbcTableComponent.ACTION_CREATE,
                  index.getType()));
            }

            // get data if requested
            if (includeData)
            {
               if (hasRows(conn, dbmsDef, tableSchema))
               {
                  PSJdbcTableData tableData = catalogTableData(conn, dbmsDef,
                     tableSchema);
                  tableSchema.setTableData(tableData);
               }
            }
         }
      }
      catch (SQLException e)
      {
         Object[] args = {tableName,
            PSJdbcTableFactoryException.formatSqlException(e)};
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);

         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.SQL_CATALOG_TABLE_FAILED, args, e);
      }

      return tableSchema;
   }

   /**
    * Convenience method, calls {@link #catalogTable(Connection,PSJdbcDbmsDef,
    * Map,PSJdbcDataTypeMap,String,boolean) catalogTable(conn,dbmsDef,null,
    * dataTypeMap,tableName,includeData)}
    */
   public static PSJdbcTableSchema catalogTable(Connection conn,
      PSJdbcDbmsDef dbmsDef, PSJdbcDataTypeMap dataTypeMap, String tableName,
         boolean includeData)
      throws PSJdbcTableFactoryException
   {
      return catalogTable(
         conn,
         dbmsDef,
         null,
         dataTypeMap,
         tableName,
         includeData);
   }

   /**
    * Version of {@link #processTable(Connection, PSJdbcDbmsDef,
    * PSJdbcTableSchema, PrintStream, boolean)} that creates the Connection
    * first.
    */
   public static void processTable(PSJdbcDbmsDef dbmsDef,
      PSJdbcTableSchema tableSchema, PrintStream logOut, boolean logDebug)
         throws PSJdbcTableFactoryException
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      Connection conn = null;
      try
      {
         conn = getConnection(dbmsDef);
         processTable(conn, dbmsDef, tableSchema, logOut, logDebug);
      }
      catch (SQLException e)
      {
         Object[] args = {tableSchema.getName(),
            PSJdbcTableFactoryException.formatSqlException(e)};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.SQL_CONNECTION_FAILED, args, e);
      }
      finally
      {
         if (conn != null)
            try {conn.close();} catch (SQLException e){}
      }
   }

   /**
    * Creates or modifies the specified table, processing data definitions if
    * they have been provided.  Binary data is not supported in general,
    * although it may work for certain drivers.
    *
    * @param conn The connection to use, must connect to the database server
    * specified in dbmsDef.  May not be <code>null</code>.  Connection is not
    * closed by this method.
    *
    * @param dbmsDef Used to connect to the database and provides correct
    * schema/origin.  May not be <code>null</code>.
    *
    * @param tableMetaMap Used to cache the table meta data. The meta data will
    * be created and added to the map if the processed table does not exist
    * in the map. The map key is the table name in <code>String</code>; the
    * map value is <code>PSJdbcTableMetaData</code> object of the related
    * table. It may be <code>null</code> if no need to cache the meta data.
    *
    * @param tableSchema A valid tableSchema object containing at least one
    * PSJdbcColumnDef, and possibly a PSJdbcTableData object.
    *
    * @param logOut If not <code>null</code>, log messages will be written to
    * this stream.  If <code>null</code>, they will not. This method does not
    * take ownership of the stream and will not attempt to close it when
    * processing is completed.
    *
    * @param logDebug If <code>true</code> and logOut is not <code>null</code>,
    * debugging messages will also be written to the logging output stream.  If
    * <code>false</code>, they will not.  If logOut is <code>null</code>, this
    * parameter has no effect.
    *
    *
    * @throws IllegalArgumentException if conn, dbmsDef or tableSchema is
    * <code>null</code>.
    * @throws PSJdbcTableFactoryException if there are any errors.
    *
    * @see PSJdbcTableSchema for possible options on creating and modifying.
    * @see PSJdbcTableData for possible options on processing data.
    */
   public static void processTable(
      Connection conn,
      PSJdbcDbmsDef dbmsDef,
      Map tableMetaMap,
      PSJdbcTableSchema tableSchema,
      PrintStream logOut,
      boolean logDebug)
      throws PSJdbcTableFactoryException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      PSLogger logger = null;

      boolean processingData = false;
      try
      {
         logger = getLogger();
         if (logOut != null)
         {
            logger.enableLoggingOutput(true);
            logger.setOutputStream(logOut);
            logger.enableDebuggingOutput(logDebug);
         }
         else
            logger.enableLoggingOutput(false);

         PSJdbcTableMetaData tmd = null;
         String tableName = tableSchema.getName();
         PSJdbcTableData tableData = tableSchema.getTableData();
         boolean bAllowSchemaChanges = tableSchema.isAllowSchemaChanges();
         boolean bAllowTableCreation = tableSchema.isCreate();
         boolean bTableExists = false;
         boolean processData = true;
         if (tableData == null) processData = false;


         // Look for the table and its meta-data.
         // Fetch and cache the meta-data if it's missing.
         // (Settings in Table Schema define allowable operations on the table.
         //   refer to Table Definition File of the Publishing template.)
         if ( bAllowSchemaChanges ||
               (!bAllowSchemaChanges && bAllowTableCreation) )
         {           
            if (tableMetaMap != null)
               tmd = (PSJdbcTableMetaData)tableMetaMap.get(tableName);
            if (tmd == null)
            {
               tmd = getTableMetaData(conn,
                  dbmsDef, tableSchema.getDataTypeMap(), tableSchema.getName());
               if (tableMetaMap != null)
                  tableMetaMap.put(tableName, tmd);
            }
            bTableExists = tmd.exists();
         }
            
         // Process Schema and Data changes to table
         if ( bAllowSchemaChanges ||
            (!bTableExists && !bAllowSchemaChanges && bAllowTableCreation) )
         {
            // Check for Views (as opposed to table)
            // Schema changes for a view is not supported
            if (tableSchema.isView() || tmd.isView())
               throw new PSJdbcTableFactoryException(
                  IPSTableFactoryErrors.ALTER_VIEW_NOT_SUPPORTED,
                  tableSchema.getName());

            // Process schema changes
            PSJdbcExecutionPlan plan = new PSJdbcExecutionPlan();
            int schemaAction = PSJdbcPlanBuilder.createSchemaPlan(
                  conn, dbmsDef, tableSchema, plan);
            tableSchema.setSchemaAction(schemaAction);

            plan.execute(conn);

            // Process data changes if required
            if (processData)
            {
               processingData = true;
               // if onCreateOnly and didn't create table, don't process
               if (tableData.onCreateOnly() && 
                  (schemaAction != PSJdbcPlanBuilder.SCHEMA_ACTION_CREATE))
               {
                  logMessage(
                     "onCreateOnly=yes and table not created, skipping data plan");
                  processData = false;
               }
               // if no table and no action, don't process data (no table!)
               else if (!bTableExists && 
                     (schemaAction == PSJdbcPlanBuilder.SCHEMA_ACTION_NONE))
               {
                  logMessage("Table does not exist and not created, " +
                     "skipping data plan");
                  processData = false;
               }
               processingData = false;
            }
         }

         /*
          * We need four separate plans to support child tables. The plans are
          * executed in the following order : deletePlan then insertPlan then
          * replacePlan then updatePlan. The plans have steps for child tables
          * first and then for parent tables. For insertPlan and replacePlan
          * the steps are executed in reverse order because inserts need to
          * be done first for the parent table.
          */
         if (processData)
         {
            processingData = true;

            PSJdbcExecutionPlan insertPlan = new PSJdbcExecutionPlan();
            PSJdbcExecutionPlan deletePlan = new PSJdbcExecutionPlan();
            PSJdbcExecutionPlan updatePlan = new PSJdbcExecutionPlan();
            PSJdbcReplaceExecutionPlan replacePlan =
               new PSJdbcReplaceExecutionPlan();

            PSJdbcPlanBuilder.getDataPlan(
               conn,
               dbmsDef,
               tableMetaMap,
               tableSchema,
               tableData,
               insertPlan,
               deletePlan,
               updatePlan,
               replacePlan,
               false);

            deletePlan.execute(conn);
            ms_planLogsContainer.addPlanLogData(deletePlan.getPlanLogData());

            insertPlan.execute(conn);
            ms_planLogsContainer.addPlanLogData(insertPlan.getPlanLogData());

            replacePlan.execute(conn);
            ms_planLogsContainer.addPlanLogData(replacePlan.getPlanLogData());

            updatePlan.execute(conn);
            ms_planLogsContainer.addPlanLogData(updatePlan.getPlanLogData());

            processingData = false;
         }
      }
      catch (SQLException e)
      {
         int errorCode = processingData ?
            IPSTableFactoryErrors.DATA_PROCESS_ERROR :
               IPSTableFactoryErrors.SCHEMA_PROCESS_ERROR;

         Object[] args = {tableSchema.getName(),
            PSJdbcTableFactoryException.formatSqlException(e)};
         PSJdbcTableFactoryException ex = new PSJdbcTableFactoryException(
            errorCode, args, e);
         logger.logMessage(ex.getLocalizedMessage());
         throw ex;
      }
      finally
      {
         if (logger != null)
            logger.shutdown();
      }
   }

   /**
    * Convenient method, calls {@link processTable(Connection, PSJdbcDbmsDef,
    * null, PSJdbcTableSchema, PrintStream, boolean)}.
    */
   public static void processTable(Connection conn, PSJdbcDbmsDef dbmsDef,
      PSJdbcTableSchema tableSchema, PrintStream logOut, boolean logDebug)
         throws PSJdbcTableFactoryException
   {
      processTable(conn, dbmsDef, null, tableSchema, logOut, logDebug);
   }

   /**
    * Creates or modifies each table in the supplied collection, processing data
    * definitions if they have been provided.  See {@link #processTable(
    * Connection, PSJdbcDbmsDef, PSJdbcTableSchema, PrintStream, boolean)} for
    * more info.
    *
    * @param conn The connection to use, must connect to the database server
    * specified in dbmsDef.  May not be <code>null</code>.  Connection is not
    * closed by this method.
    *
    * @param dbmsDef Used to connect to the database and provides correct
    * schema/origin.  May not be <code>null</code>.
    *
    * @param tableMetaMap Used to cache the table meta data. The meta data will
    * be created and added to the map if the processed table does not exist
    * in the map. The map key is the table name in <code>String</code>; the
    * map value is <code>PSJdbcTableMetaData</code> object of the related
    * talbe. It may be <code>null</code> if no need to cache the meta data.
    *
    * @param tables A collection of table schema objects, each containing at
    * least one PSJdbcColumnDef, and possibly a PSJdbcTableData object.
    *
    * @param logOut If not <code>null</code>, log messages will be written to
    * this stream.  If <code>null</code>, they will not. This method does not
    * take ownership of the stream and will not attempt to close it when
    * processing is completed.
    *
    * @param logDebug If <code>true</code> and logOut is not <code>null</code>,
    * debugging messages will also be written to the logging output stream.  If
    * <code>false</code>, they will not.  If logOut is <code>null</code>, this
    * parameter has no effect.
    *
    *
    * @throws IllegalArgumentException if tables is <code>null</code> or does
    * not contain at least one table schema object, or if any PSJdbcTableSchema
    * object defined therein does not contain at least one PSJdbcColumnDef.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public static void processTables(
      Connection conn,
      PSJdbcDbmsDef dbmsDef,
      Map tableMetaMap,
      PSJdbcTableSchemaCollection tables,
      PrintStream logOut,
      boolean logDebug)
      throws PSJdbcTableFactoryException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tables == null)
         throw new IllegalArgumentException("tables may not be null");

      ms_planLogsContainer.clearPlanLogs();
      Iterator i = tables.iterator();
      while (i.hasNext())
      {
         PSJdbcTableSchema tableSchema = (PSJdbcTableSchema)i.next();
         processTable(
            conn,
            dbmsDef,
            tableMetaMap,
            tableSchema,
            logOut,
            logDebug);
      }
   }

   /**
    * Convenient method, calls {@link processTables(Connection,PSJdbcDbmsDef,
    * null,PSJdbcTableSchemaCollection,PrintStream,boolean)}.
    */
   public static void processTables(Connection conn, PSJdbcDbmsDef dbmsDef,
      PSJdbcTableSchemaCollection tables, PrintStream logOut, boolean logDebug)
         throws PSJdbcTableFactoryException
   {
      processTables(
         conn,
         dbmsDef,
         null,
         tables,
         logOut,
         logDebug);
   }

   /**
    * Convenience version of {@link #processTables(Connection, PSJdbcDbmsDef,
    *  PSJdbcTableSchemaCollection, PrintStream, boolean)}
    * that creates all required objects from the source files.
    *
    * @param serverProps A set of properties that provides connection and basic
    * table information.  May not be <code>null</code>.  See {@link
    * PSJdbcDbmsDef#PSJdbcDbmsDef(Properties) PSJdbcDbmsDef ctor} for a
    * description of the properties expected.
    *
    * @param dataTypeMap An Xml document conforming to the DataTypeMaps DTD
    * defined in datatypemap.dtd.
    * Must include a set of mappings referred to by the DB_BACKEND and
    * DB_DRIVER_NAME properties in the serverProps properties.  May be <code>
    * null</code>, in which case the default datatype map xml resource file is
    * used.  The first set of mappings matching the DB_BACKEND and
    * DB_DRIVER_NAME properties are used.  Only one of the two properties must
    * be supplied, and if both are supplied, an attempt will be made to match
    * on both properties.
    *
    * @param tableDef An Xml document conforming to the tables DTD defined in
    * tabledef.dtd.  May not be <code>null</code>.  Provides all table schema
    * information and any options for processing the table.
    *
    * @param tableData An Xml document conforming to the tables DTD defined in
    * the tabledata.dtd.  May be <code>null</code>.  If provided, will be used
    * to insert/modify data.  Each table defined in this document must conform
    * the schema of a table defined in the tableDef document with the same name.
    *
    * @param logOut If not <code>null</code>, log messages will be written to
    * this stream.  If <code>null</code>, they will not. This method does not
    * take ownership of the stream and will not attempt to close it when
    * processing is completed.
    *
    * @param logDebug If <code>true</code> and logOut is not <code>null</code>,
    * debugging messages will also be written to the logging output stream.  If
    * <code>false</code>, they will not.  If logOut is <code>null</code>, this
    * parameter has no effect.
    *
    * @throws IllegalArgumentException if serverProps, or tableDef
    * are <code>null</code> or if a required property is missing.
    * @throws PSJdbcTableFactoryException if any other errors occur.
    */

   public static void processTables(Properties serverProps,
      Document dataTypeMap, Document tableDef, Document tableData,
         PrintStream logOut, boolean logDebug)
            throws PSJdbcTableFactoryException
   {
      if (serverProps == null)
         throw new IllegalArgumentException("serverProps may not be null");

      PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(serverProps);
      processTables(dbmsDef, dataTypeMap, tableDef, tableData, logOut, logDebug);
   }

   /**
    * Convenience method, calls {@link #processTables(null, PSJdbcDbmsDef,
    * null, Document, Document, PrintStream, boolean, boolean)}.
    */
   public static void processTables(PSJdbcDbmsDef dbmsDef, Document dataTypeMap,
      Document doc, PrintStream logOut, boolean logDebug,
      boolean transactionSupport)
         throws PSJdbcTableFactoryException
   {
      processTables(
         null,
         dbmsDef,
         null,
         dataTypeMap,
         doc,
         logOut,
         logDebug,
         transactionSupport);
   }

   /**
    * Convenience version of {@link #processTables(Connection, PSJdbcDbmsDef,
    * Map, Document, Document, Document, PrintStream, boolean)} that creates the
    * tabledef and tabledata from a document conforming to the
    * sys_DatabasePublisher.dtd.
    *
    * @param conn The connection to the database. It may be <code>null</code>;
    *    otherwise it will be left open afterwards.
    *
    * @param dbmsDef Used to connect to the database and provides correct
    *    schema/origin. May not be <code>null</code>.
    *
    * @param tableMetaMap Used to cache the table meta data. The meta data will
    *    be created and added to the map if the processed table does not exist
    *    in the map. The map key is the table name in <code>String</code>; the
    *    map value is <code>PSJdbcTableMetaData</code> object of the related
    *    talbe. It may be <code>null</code> if no need to cache the meta data.
    *
    * @param dataTypeMap An Xml document conforming to the DataTypeMaps DTD
    *    defined in datatypemap.dtd, may be <code>null</code>.
    *
    * @param doc an Xml document conforming to the sys_DatabasePublisher.dtd,
    *    not <code>null</code>.
    *
    * @param logOut If not <code>null</code>, log messages will be written to
    *    this stream. If <code>null</code>, they will not. This method does not
    *    take ownership of the stream and will not attempt to close it when
    *    processing is completed.
    *
    * @param logDebug If <code>true</code> and logOut is not <code>null</code>,
    *    debugging messages will also be written to the logging output stream.
    *    If <code>false</code>, they will not. If logOut is <code>null</code>,
    *    this parameter has no effect.
    *
    * @param transactionSupport this flag is currently only supported if
    *    "allowSchemaChanges" in the provided tabledef is disabled. If
    *    <code>true</code> and "allowSchemaChanges" is disabled, tranasaction
    *    support for data changes will be used. If <code>true</code> and
    *    "allowSchemaChanges" is enabled, an UnsupportedOperationException will
    *    be thrown. If <code>false</code>, no transaction support is provided.
    *
    * @throws IllegalArgumentException if the supplied dbmsDef is
    *    <code>null</code> or the publisher document is <code>null</code>.
    * @throws PSJdbcTableFactoryException if any errors occur.
    */
   public static void processTables(Connection conn, PSJdbcDbmsDef dbmsDef,
      Map tableMetaMap, Document dataTypeMap,
      Document doc, PrintStream logOut, boolean logDebug,
      boolean transactionSupport)
         throws PSJdbcTableFactoryException
   {
      Document tableDef = transform(doc, TO_TABLEDEF);
      Document tableData = transform(doc, TO_TABLEDATA);

      // tableDef and tableData here are instances of com.icl.saxon.tinytree.TinyDocumentImpl
      // and not of org.apache.xerces.dom.DocumentImpl, so tablefactory
      // should avoid using any xerces specific classes or methods
      processTables(
         conn,
         dbmsDef,
         tableMetaMap,
         dataTypeMap,
         tableDef,
         tableData,
         logOut,
         logDebug,
         transactionSupport);
   }

   /**
    * Creates or modifies each table in the supplied collection, processing data
    * definitions if they have been provided.  See {@link #processTable(
    * Connection, PSJdbcDbmsDef, PSJdbcTableSchema, PrintStream, boolean)} for
    * more info.
    *
    * @param dbmsDef Used to connect to the database and provides correct
    * schema/origin.  May not be <code>null</code>.
    *
    * @param dataTypeMap An Xml document conforming to the DataTypeMaps DTD
    * defined in datatypemap.dtd, may be <code>null</code>
    *
    * @param tableDef An Xml document conforming to the tables DTD defined in
    * tabledef.dtd.  May not be <code>null</code>.  Provides all table schema
    * information and any options for processing the table.
    *
    * @param tableData An Xml document conforming to the tables DTD defined in
    * the tabledata.dtd.  May be <code>null</code>.  If provided, will be used
    * to insert/modify data.  Each table defined in this document must conform
    * the schema of a table defined in the tableDef document with the same name.
    *
    * @param logOut If not <code>null</code>, log messages will be written to
    * this stream.  If <code>null</code>, they will not. This method does not
    * take ownership of the stream and will not attempt to close it when
    * processing is completed.
    *
    * @param logDebug If <code>true</code> and logOut is not <code>null</code>,
    * debugging messages will also be written to the logging output stream.  If
    * <code>false</code>, they will not.  If logOut is <code>null</code>, this
    * parameter has no effect.
    *
    * @throws IllegalArgumentException if dbmsDef is <code>null</code> or if
    * tableDef is <code>null</code>
    *
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public static void processTables(PSJdbcDbmsDef dbmsDef,
      Document dataTypeMap, Document tableDef, Document tableData,
         PrintStream logOut, boolean logDebug)
            throws PSJdbcTableFactoryException
   {
      processTables(dbmsDef, dataTypeMap, tableDef, tableData, logOut,
         logDebug, false);
   }

   /**
    * Convenient method, calls {@link processTables(null,PSJdbcDbmsDef,null,
    * Document,Document,Document,PrintStream,boolean,boolean)}.
    */
   public static void processTables(PSJdbcDbmsDef dbmsDef,
      Document dataTypeMap, Document tableDef, Document tableData,
         PrintStream logOut, boolean logDebug, boolean transactionSupport)
            throws PSJdbcTableFactoryException
   {
      processTables(
         null,
         dbmsDef,
         null,
         dataTypeMap,
         tableDef,
         tableData,
         logOut,
         logDebug,
         transactionSupport);
   }

   /**
    * Creates or modifies each table in the supplied collection, processing data
    * definitions if they have been provided.  See {@link #processTable(
    * Connection, PSJdbcDbmsDef, PSJdbcTableSchema, PrintStream, boolean)} for
    * more info.
    *
    * @param conn The connection to the database. It may be <code>null</code>;
    *    otherwise it will be left open afterwards.
    *
    * @param dbmsDef Used to connect to the database and provides correct
    *    schema/origin. May not be <code>null</code>.
    *
    * @param tableMetaMap Used to cache the table meta data. The meta data will
    *    be created and added to the map if the processed table does not exist
    *    in the map. The map key is the table name in <code>String</code>; the
    *    map value is <code>PSJdbcTableMetaData</code> object of the related
    *    talbe. It may be <code>null</code> if no need to cache the meta data.
    *
    * @param dataTypeMap An Xml document conforming to the DataTypeMaps DTD
    * defined in datatypemap.dtd, may be <code>null</code>
    *
    * @param tableDef An Xml document conforming to the tables DTD defined in
    * tabledef.dtd.  May not be <code>null</code>.  Provides all table schema
    * information and any options for processing the table.
    *
    * @param tableData An Xml document conforming to the tables DTD defined in
    * the tabledata.dtd.  May be <code>null</code>.  If provided, will be used
    * to insert/modify data.  Each table defined in this document must conform
    * the schema of a table defined in the tableDef document with the same name.
    *
    * @param logOut If not <code>null</code>, log messages will be written to
    * this stream.  If <code>null</code>, they will not. This method does not
    * take ownership of the stream and will not attempt to close it when
    * processing is completed.
    *
    * @param logDebug If <code>true</code> and logOut is not <code>null</code>,
    * debugging messages will also be written to the logging output stream.  If
    * <code>false</code>, they will not.  If logOut is <code>null</code>, this
    * parameter has no effect.
    *
    * @param transactionSupport this flag is currently only supported if
    *    "allowSchemaChanges" in the provided tabledef is disabled. If
    *    <code>true</code> and "allowSchemaChanges" is disabled, tranasaction
    *    support for data changes will be used. If <code>true</code> and
    *    "allowSchemaChanges" is enabled, an UnsupportedOperationException will
    *    be thrown. If <code>false</code>, no transaction support is provided.
    *
    * @throws IllegalArgumentException if dbmsDef is <code>null</code> or if
    * tableDef is <code>null</code>
    *
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public static synchronized void processTables(
      Connection conn,
      PSJdbcDbmsDef dbmsDef,
      Map tableMetaMap,
      Document dataTypeMap,
      Document tableDef,
      Document tableData,
      PrintStream logOut,
      boolean logDebug,
      boolean transactionSupport)
      throws PSJdbcTableFactoryException
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableDef == null)
         throw new IllegalArgumentException("tableDef may not be null");

      PSJdbcDataTypeMap dataTypeMapObj = null;
      if (dataTypeMap == null)
      {
         try
         {
            dataTypeMapObj = new PSJdbcDataTypeMap(dbmsDef.getBackEndDB(),
               dbmsDef.getDriver(), null);
         }
         catch (Exception e)
         {
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.LOAD_DEFAULT_DATATYPE_MAP, e.toString(), e);
         }
      }
      else
      {
         dataTypeMapObj = new PSJdbcDataTypeMap(dataTypeMap,
            dbmsDef.getBackEndDB(), dbmsDef.getDriver(), null);
      }
      ms_tableSchemaColl.fromXml(tableDef.getDocumentElement(), dataTypeMapObj);
      if (tableData != null)
      {
         PSJdbcTableDataCollection tableDataColl =
            new PSJdbcTableDataCollection(tableData);
         ms_tableSchemaColl.setTableData(tableDataColl);
      }

      /*
       * Test if schema changes are allowed and transaction support was
       * requested. If so we throw an UnsupportedOperationException telling the
       * user that this is not supported.
       */

      boolean makeConntion = false;
      try
      {
         if (conn == null)
         {
            conn = getConnection(dbmsDef);
            conn.setAutoCommit(!transactionSupport);
            makeConntion = true;
         }
         processTables(
            conn,
            dbmsDef,
            tableMetaMap,
            ms_tableSchemaColl,
            logOut,
            logDebug);

         if (transactionSupport)
            conn.commit();
      }
      catch (SQLException e)
      {
         if (conn != null)
         {
            try
            {
               if (transactionSupport)
                  conn.rollback();
            }
            catch (SQLException e1)
            {
               // ignore
            }
         }
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.SCHEMA_COLL_PROCESS_ERROR,
               PSJdbcTableFactoryException.formatSqlException(e), e);
      }
      catch (PSJdbcTableFactoryException e1)
      {
         try
         {
            if (transactionSupport)
               conn.rollback();
         }
         catch (SQLException e2)
         {
            // ignore
         }
         throw e1;
      }
      finally
      {
         if (makeConntion && conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e2)
            {
               // ignore
            }
         }
      }
   }

   /**
    * Returns a valid Connection to the specified server.  Caller owns the
    * connection after that and is responsible for closing it.
    *
    * @param dbmsDef A valid PSJdbcDbmsDef object.  May not be <code>null
    * </code>.
    *
    * @throws IllegalArgumentException if dbmsDef is <code>null</code>.
    * @throws SQLException if there is an error getting the connection.
    * @throws PSJdbcTableFactoryException if there are any other errors.
    */
   public static Connection getConnection(PSJdbcDbmsDef dbmsDef) throws
      SQLException, PSJdbcTableFactoryException
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      return dbmsDef.getConnection();
   }

   /**
    * Calls {@link #processTables(Properties, Document, Document, Document,
    * PrintStream, boolean) processTables()} using the supplied arguments:
    *
    * <ol>
    * <li>properties_file_name - path to the properties file defining the
    *    backend database server.  Required.</li>
    * <li>data_type_map_file - path to xml file containing the datatype map.
    *    Required (but may be dummy value if the 'm' switch is set).</li>
    * <li>table-desc-file - path to xml file containing the table schema
    *    defintions.  Required.</li>
    * <li>table-data-file - path to xml file containing the table data
    *    defitions.  Optional.</li>
    * <li>switches - A hyphen followed by one or more switches. Optional.
    * <ul>
    * <li>l: standard messages are logged
    * <li>d: debug messages are logged as well.
    * <li>m: use default datatype map instead of supplied xml file.
    * <li>p: specifies database publishing. table-desc-file in this case
    *        must conform to sys_DatabasePublisher.dtd, table-data-file is
    *        not required.
    * </ul>
    * <li>outFile - path to the file where log messages should be written.
    *    Optional.  May only be specified if logging switches are also specified.
    *    If not specified and logging is on, message are written to the console.
    *    </li>
    * </ol>
    * Example
    *     com.percussion.tablefactory.PSJdbcTableFactory
    *     serverProps.properties rxdatatypemaps.xml cmstabledef.xml
    *     cmstabledata.xml -ld log.txt
    *
    */
   public static void main(String[] args)
   {
      if (args.length > 0 && PSJdbcImportExportHelper.OPTION_DB_IMPPORT.equals(args[0])) {
         mainImport(args);
         return;
      }
      
      if (args.length < 3)
      {
         showUsage();
         System.exit(1);
      }

      if (args[0].equalsIgnoreCase("-dbprops"))
      {
         //execute in a new mode
         main2(args);
         return;
      }

      PrintStream out = null;
      try
      {
         // process the switches first (so we can ignore map if needed)
         Document data = null;
         String switches = null;
         boolean logMessages = false;
         boolean logDebug = false;
         boolean useDefaultMap = false;
         boolean dbPublishing = false;
         File outFile = null;
         if (args.length >= 4)
         {
            for (int i = 3; i < args.length; i++)
            {
               if (args[i].startsWith("-"))
               {
                  // it's the switches
                  switches = args[i].toLowerCase();
                  logMessages = (switches.indexOf('l') > -1);
                  logDebug = (logMessages && (switches.indexOf('d') > -1));
                  useDefaultMap = (switches.indexOf('m') > -1);
                  dbPublishing = (switches.indexOf('p') > -1);
               }
               else if (switches == null && data == null)
               {
                  // haven't gotten switches yet, so must be data
                  data = PSXmlDocumentBuilder.createXmlDocument(
                     new FileInputStream(args[i]), false);
               }
               else if (switches != null && outFile == null)
               {
                  // have switches, must be outfile
                  outFile = new File(args[i]);
               }
               else
               {
                  System.out.println("Invalid argument: " + args[i]);
                  showUsage();
                  System.exit(1);
               }
            }
         }

         Properties props = new Properties();
         props.load(new FileInputStream(args[0]));

         Document map = null;
         if (!useDefaultMap)
            map = PSXmlDocumentBuilder.createXmlDocument(
               new FileInputStream(args[1]), false);

         File tableDefXml = new File(args[2]);
         String tableDefXmlPath = tableDefXml.getAbsolutePath();
         if (!tableDefXml.isFile())
         {
            System.out.println("invalid table definition xml : " + tableDefXmlPath);
            return;
         }

         System.out.println("parsing table definition xml : " + tableDefXmlPath);
         Document def = PSXmlDocumentBuilder.createXmlDocument(
            new FileInputStream(args[2]), false);

         if (logMessages)
         {
            if (outFile != null)
               out = new PrintStream(new FileOutputStream(outFile), true);
            else
               out = System.out;
         }

         if (dbPublishing)
         {
            PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
            processTables(dbmsDef, map, def, out, logDebug, false);
         }
         else
         {
            processTables( props, map, def, data, out, logDebug );
         }
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      finally
      {
         if (out != null)
            try {out.close();} catch (Exception e){
               log.error(e.getMessage());
               log.debug(e.getMessage(), e);}
      }
   }

   /**
    * Main method to handle the import of data that has been exported using PSCatalogTables#mainExport method.
    * @param args
    */
   private static void mainImport(String[] args)
   {
      //Validates and gets the options as a map, if the args are not valid then shows usage and exits.
      Map<String, String> optionsMap = PSJdbcImportExportHelper.getOptions(args);
      BufferedReader in=null;
      PSJdbcDbmsDef dbmsDef = null;
      try {
         //Props file
         dbmsDef = PSCatalogTableData.loadProps(optionsMap.get(PSJdbcImportExportHelper.OPTION_DB_PROPS) );
         
         //root storage path
         String storagePath = optionsMap.get(PSJdbcImportExportHelper.OPTION_STORAGE_PATH);
         File binaryStorageFolder = new File(storagePath   + File.separator + PSJdbcImportExportHelper.BINARY_DATA_FOLDER);
         dbmsDef.setBinaryStorageLocation(binaryStorageFolder);
         
         File defDataFolder = new File(storagePath + File.separator + PSJdbcImportExportHelper.DEF_DATA_FOLDER);
         
         //Load the table definition file
         File tableDefFile = new File(defDataFolder + File.separator + "tableDef.xml");
         in = new BufferedReader(
               new InputStreamReader(
                     new FileInputStream(tableDefFile), StandardCharsets.UTF_8));
         
         Document allTablesDef = PSXmlDocumentBuilder.createXmlDocument(in, false);
         in.close();
        
         NodeList nodeList = allTablesDef.getElementsByTagName("table");
         //Loop through the tables and load the individual data file and process
         for (int i = 0; i < nodeList.getLength(); i++) {
            
           
             Document tableDef = PSXmlDocumentBuilder.createXmlDocument();
             Element root = tableDef.createElement("tables");
             tableDef.appendChild(root);
             
             Element tableElem = (Element)nodeList.item(i);
             root.appendChild(tableDef.importNode(tableElem, true));
             String tableName = tableElem.getAttribute("name");

             File tableDataFile = new File(defDataFolder + File.separator + tableName +"-data.xml");
             //If the table data file exits, process it
             if (tableDataFile.exists()) {
                FileInputStream inputXml=null;
                try {
                  inputXml = new FileInputStream(tableDataFile);
                   
                   in = new BufferedReader(new PSXmlNormalizingReader(inputXml));
                   Document tableData = PSXmlDocumentBuilder.createXmlDocument(in, false);
                   in.close();
                   System.out.println("Started processing: " + tableName);
                   processTables(dbmsDef, null, tableDef, tableData, null, false);
                   System.out.println("Finished processing: " + tableName);
                }
                catch (Exception e) {
                   log.error("Error occurred while importing table data : {}", tableName);
                   log.error(e.getMessage());
                   log.debug(e.getMessage(), e);
                }finally{
                   if(inputXml!=null)
                      inputXml.close();
                      
                   if(in != null)
                      in.close();
                }
             }
             else {
                //Just create the empty table by passing null for data file.
                System.out.println("Started processing empty table: " + tableName);
                processTables(dbmsDef, null, tableDef, null, null, false);
                System.out.println("Finished processing empty table: " + tableName);
             }
             
         }
         System.exit(0);
      }
      catch (Exception e) {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }finally{
         if(in != null)
            try
            {
               in.close();
            }
            catch (IOException e)
            {
            }
      }
      
   }

   /**
     * A more flexible main method that takes multiple def and data files,
     * see example.
     * Example:
     *     com.percussion.tablefactory.PSJdbcTableFactory
     *     -dbprops serverProps.properties -typemap rxdatatypemaps.xml
     *     -def cmstabledef.xml -data cmstabledata.xml -options ld -log log.txt
     */
   public static void main2(String[] args)
   {
      if (args.length < 8)
      {
         showUsage2();
         System.exit(1);
      }

      PrintStream out = null;
      String propsFile = null;
      String typemapFile = null;
      String options = null;
      String logFile = null;

      Collection defs = new ArrayList();
      Collection datas = new ArrayList();

      boolean nextDbProps = false;
      boolean nextDbTypeMap = false;
      boolean nextDef = false;
      boolean nextData = false;
      boolean nextOptions = false;
      boolean nextLog = false;
      for (int i = 0; i < args.length; i++)
      {
         String tmp = args[i];

         if (tmp.equalsIgnoreCase("-dbprops"))
            nextDbProps = true;
         else if (tmp.equalsIgnoreCase("-typemap"))
            nextDbTypeMap = true;
         else if (tmp.equalsIgnoreCase("-def"))
            nextDef = true;
         else if (tmp.equalsIgnoreCase("-data"))
            nextData = true;
         else if (tmp.equalsIgnoreCase("-options"))
            nextOptions = true;
         else if (tmp.equalsIgnoreCase("-log"))
            nextLog = true;
         else
         {
            if (tmp==null || tmp.trim().length()==0)
            {
               System.out.println("missing value after option " + args[i-1]);
               showUsage2();
               return;
            }

            if (nextDbProps)
            {
               propsFile = tmp;
               File f = new File(tmp);
               if (!f.exists())
               {
                  System.out.println("dbprops file missing");
                  showUsage2();
                  return;
               }
            }
            else if (nextDbTypeMap)
            {
               typemapFile = tmp;
               File f = new File(tmp);
               if (!f.exists())
               {
                  System.out.println("typemap file missing");
                  showUsage2();
                  return;
               }
            }
            else if (nextDef)
            {
               defs.add(tmp);
               File f = new File(tmp);
               if (!f.exists())
               {
                  System.out.println("def file missing");
                  showUsage2();
                  return;
               }
            }
            else if (nextData)
            {
               datas.add(tmp);
               File f = new File(tmp);
               if (!f.exists())
               {
                  System.out.println("data file missing");
                  showUsage2();
                  return;
               }
            }
            else if (nextOptions)
            {
               options = tmp;
            }
            else if (nextLog)
            {
               logFile = tmp;
            }

            nextDbProps = false;
            nextDbTypeMap = false;
            nextDef = false;
            nextData = false;
            nextOptions = false;
            nextLog = false;

         }
      }

      PSJdbcTableSchemaCollection schemaColl = null;
      PSJdbcTableDataCollection dataColl = null;
      PSJdbcTableSchema schema = null;
      PSJdbcTableData data = null;
      PrintStream ps = System.out;
      Connection conn = null;
      PSProperties props = null;
      PSJdbcDbmsDef dbmsDef = null;
      PSJdbcDataTypeMap dataTypeMap = null;

      try
      {
         // process the options first (so we can ignore map if needed)
         boolean logMessages = false;
         boolean logDebug = false;
         boolean useDefaultMap = false;
         boolean dbPublishing = false;
         boolean encrypted = false;
         File outFile = null;

         if (logFile!=null)
            outFile = new File(logFile);

         if (options != null)
         {
            logMessages = (options.indexOf('l') > -1);
            logDebug = (logMessages && (options.indexOf('d') > -1));
            useDefaultMap = (options.indexOf('m') > -1);
            dbPublishing = (options.indexOf('p') > -1);
            encrypted = (options.indexOf('e') > -1);
         }
         
         props = new PSProperties(propsFile);
         
         props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY,
            encrypted ? "Y" : "N");

         dbmsDef = new PSJdbcDbmsDef(props);
         dataTypeMap = new PSJdbcDataTypeMap(
                  props.getProperty("DB_BACKEND"),
                  props.getProperty("DB_DRIVER_NAME"), null);

         conn = PSJdbcTableFactory.getConnection(dbmsDef);

         //get table def files
         Iterator itdefs = defs.iterator();

         while (itdefs.hasNext())
         {
            String filePath = (String) itdefs.next();

            Document doc = PSXmlDocumentBuilder.createXmlDocument(
                     new FileInputStream(new File(filePath)),
                     false);

               if (schemaColl==null)
                  schemaColl = new PSJdbcTableSchemaCollection(doc,
                     dataTypeMap);
               else
                  schemaColl.addAll(new PSJdbcTableSchemaCollection(doc,
                                      dataTypeMap));

         }

         //get table data files
         Iterator itdatas = datas.iterator();

         while (itdatas.hasNext())
         {
            String filePath = (String) itdatas.next();

            File f = new File(filePath);

            //set system property so that table factory can find external
            //resources if any.
            String fName = f.getName();
            //get table factory file name with no extension
            //ie: {cmstableData.external.root}
            //FB: RV_RETURN_VALUE_IGNORED NC 1-17-16
            fName = fName.substring(0, fName.length()-4);
            System.setProperty("{" + fName + ".external.root}", f.getParent());

            Document doc = PSXmlDocumentBuilder.createXmlDocument(
                     new FileInputStream(f),
                     false);

               if (dataColl==null)
                  dataColl = new PSJdbcTableDataCollection(doc);
               else
                  dataColl.addAll(new PSJdbcTableDataCollection(doc));
         }

         Document map = null;
         if (!useDefaultMap) 
         {
            if (typemapFile==null || !(new File(typemapFile)).exists())
            {
               System.out.println("typemap file missing");
               showUsage2();
               return;
            }
            
            map = PSXmlDocumentBuilder.createXmlDocument(
               new FileInputStream(typemapFile), false);
         }

         Document def = PSXmlDocumentBuilder.createXmlDocument();
         Element tables  = schemaColl.toXml(def);
         def.appendChild(tables);

         if (logMessages)
         {
            if (outFile != null)
               out = new PrintStream(new FileOutputStream(outFile), true);
            else
               out = System.out;
         }

         if (dbPublishing)
         {
            processTables(dbmsDef, map, def, out, logDebug, false);
         }
         else
         {
            Document dataDoc = PSXmlDocumentBuilder.createXmlDocument();
            Element dataTables  = dataColl.toXml(dataDoc);
            dataDoc.appendChild(dataTables);

            processTables( props, map, def, dataDoc, out, logDebug );
         }

      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      finally
      {
         if (out != null)
            try {out.close();} catch (Exception e){
               log.error(e.getMessage());
               log.debug(e.getMessage(), e);}
      }
   }

   /**
    * Determines if a table is empty or not.
    *
    * @param conn A valid, non-<code>null</code> connection.
    * @param dbmsDef The database where the table is located.  May not be <code>
    * null</code>.
    * @param tableSchema The table to check, may not be <code>null</code>.
    *
    * @return <code>true</code> if the table has rows, <code>false</code> if
    * not.
    *
    * @throws IllegalArgumentExcpetion if conn, dbmsDef, or tableSchema is
    * <code>null</code>.
    * @throws PSJdbcTableFactoryException if the table does not exist or any
    * other errors occur.
    */
   static boolean hasRows(Connection conn, PSJdbcDbmsDef dbmsDef,
      PSJdbcTableSchema tableSchema) throws PSJdbcTableFactoryException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      String sqlStmt = "SELECT COUNT(*) FROM " + PSSqlHelper.qualifyTableName(
            tableSchema.getName(), dbmsDef.getDataBase(), dbmsDef.getSchema(),
            dbmsDef.getDriver()) ;

      Statement stmt = null;
      ResultSet rsCount = null;

      try
      {
         int count = 0;
         stmt = PSSQLStatement.getStatement(conn);
         rsCount = stmt.executeQuery(sqlStmt);

         if (rsCount.next())
            count = rsCount.getInt(1);

         return count > 0;
      }
      catch (SQLException e)
      {
         Object args[] = {tableSchema.getName(),
            PSJdbcTableFactoryException.formatSqlException(e)};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.CHECK_EXISTING_DATA, args, e);
      }
      finally
      {
         if (rsCount != null)
            try {rsCount.close();} catch (SQLException e){}
         if (stmt != null)
            try {stmt.close();} catch (SQLException e){}
      }
   }

   /**
    * Queries the specified table for all rows and returns them as a
    * PSJdbcTableData object.
    *
    * @param conn A valid, non-<code>null</code> connection.
    * @param dbmsDef The database where the table is located.  May not be <code>
    * null</code>.
    * @param tableSchema The table to query, may not be <code>null</code>.
    *
    * @return The tableData object, never <code>null</code>, may be empty (no
    * rows).  All rows have an action of {@link PSJdbcRowData#ACTION_INSERT}.
    * Columns with <code>null</code> values are not included in the results.
    *
    * @throws PSJdbcTableFactoryException if the table does not exist or any
    * other errors occur.
    */
   private static PSJdbcTableData catalogTableData(Connection conn,
      PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema tableSchema)
         throws PSJdbcTableFactoryException
   {
      return catalogTableData(conn, dbmsDef, tableSchema,
         null, null, PSJdbcRowData.ACTION_INSERT);
   }

   /**
   * Queries the specified table for all rows and returns them as a
   * PSJdbcTableData object.
   *
   * @param conn A valid, non-<code>null</code> connection.
   * @param dbmsDef The database where the table is located,
   * may not be <code> null</code>.
   * @param tableSchema The table to query, may not be <code>null</code>.
   * @param columns an array of column names, may be <code>null</code>
   * or empty array in which case all the columns are used in the select query.
   * The columns specified must belong to the table specified
   * by the tableSchema parameter.
   * @param filter encapsulates the where clause of the select query,
   * may be <code>null</code> in which
   * case the select query does not have a where clause.
   * @param rowAction the action to be set for the rows cataloged
   *
   * @return the tableData object encapsulating the rows retrieved from the
   * database, may return <code>null</code> if no rows are
   * retrieved from the database
   *
   * @throws PSJdbcTableFactoryException if the table does not exist or
   * any other errors occur.
   * @throws IllegalArgumentException if conn or dbmsDef or tableSchema is
   * <code>null</code>
   */
   public static PSJdbcTableData catalogTableData(Connection conn,
      PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema tableSchema,
      String[] columns, PSJdbcSelectFilter filter, int rowAction)
      throws PSJdbcTableFactoryException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      PSJdbcResultSetIteratorStep step =
         PSJdbcStatementFactory.getResultSetIteratorStatement(
            dbmsDef, tableSchema, columns, filter, rowAction);

      try
      {
         step.execute(conn);
         // walk result and for each row, build a row of column data
         List rowList = new ArrayList();
         PSJdbcRowData row = step.next();
         long count=0;
         while (row != null)
         {
             count++;
             System.out.println("Processing row " + count);
            rowList.add(row);
            row = step.next();
         }

         if (rowList.isEmpty())
            return null;
         return new PSJdbcTableData(tableSchema.getName(), rowList.iterator());
      }
      catch (SQLException e)
      {
         Object args[] = {tableSchema.getName(),
            PSJdbcTableFactoryException.formatSqlException(e)};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.SQL_CATALOG_DATA, args, e);
      }
      finally
      {
         if (step != null)
            step.close();
      }
   }

   /**
    * Catalogs all table names in a given dbmsDef.
    *
    * @param dbmsDef def for a given database, never <code>null</code>.
    * @param tableNamePattern table name filter, ie: "%" returns all the tables
    * in a given schema, may be <code>null</code> or <code>empty</code> in which
    * case all table names returned.
    *
    * @return collection of table names as String objects,
    * never <code>null</code>, may be <code>empty</code>.
    * @throws SQLException if a database access error occurs
    * @throws PSJdbcTableFactoryException if database connection using the
    * datasource object fails.
    */
   public static Collection catalogTables(PSJdbcDbmsDef dbmsDef,
      String tableNamePattern) throws SQLException, PSJdbcTableFactoryException
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      final String filterAll = "%";

      if (tableNamePattern==null || tableNamePattern.trim().length()==0)
         tableNamePattern = filterAll;

      // catalog the table names
      List tableList = new ArrayList();

      String db = dbmsDef.getDataBase();

      if (db.trim().length() == 0)
         db = filterAll;

      String schema = dbmsDef.getSchema();
      if (schema.trim().length() == 0)
         schema = filterAll;

      Connection conn = dbmsDef.getConnection();

      ResultSet rs = null;
      try
      {
         DatabaseMetaData meta = conn.getMetaData();
         rs = meta.getTables(db, schema,
            tableNamePattern, new String[] {"TABLE"});

         if (rs != null)
         {
            while (rs.next())
            {
               tableList.add(rs.getString(COLNO_TABLE_NAME));
            }
         }
      }
      finally
      {
         if (rs != null)
            try { rs.close();} catch (SQLException e){}
      }

      return tableList;
   }


   /**
    * Returns the value of a column for the current row from a resultset.
    * This function does not call the <code>next</code> method of the ResultSet,
    * nor does it <code>close</code> the ResultSet.
    *
    * To DO:
    * If the column is of any of the following jdbc types, then it should do a
    * Base64 encoding of the column value and set this encoding type on the
    * returned column data object:
    * Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB,
    * Types.LONGVARCHAR, Types.CLOB
    *
    * @param dbmsDef The database where the table is located,
    * may not be <code> null</code>.
    * @param tableSchema The table on whose data the resultset
    * <code>rs</code>is based and contains the column <code>columnName</code>,
    * may not be <code>null</code>.
    * @param rs contains row data from which the value corresponding to the
    * column <code>columnName</code> is to be obtained, may not
    * <code>null</code>.
    * @param columnName the name of the column whose data is to be obtained,
    * may not be <code>null</code> or empty.
    *
    * @return the value of a column for the current row from a resultset, never
    * <code>null</code>
    *
    * @throws IllegalArgumentException if dbmsDef or tableSchema or rs or
    * columnName is <code>null</code> or if columnName is empty
    */
   public static PSJdbcColumnData getColumnData(PSJdbcDbmsDef dbmsDef,
      PSJdbcTableSchema tableSchema,  ResultSet rs, String columnName)
      throws SQLException, IOException
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");
      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");
      if (rs == null)
         throw new IllegalArgumentException("rs may not be null");
      if ((columnName == null) || (columnName.trim().length() < 1))
         throw new IllegalArgumentException("columnName may not be null or empty");

      String columnValue = null;
      PSJdbcColumnDef columnDef = tableSchema.getColumn(columnName);
      int jdbcDataType = columnDef.getType();
      int encoding = PSJdbcColumnData.ENC_TEXT;
      switch (jdbcDataType)
      {
         case Types.BLOB:            
            InputStream is = null;
            ByteArrayOutputStream baos = null;
            
            try
            {
               is = rs.getBinaryStream(columnName);
               if (is != null)
               {
                  //If binary storage location not set, process the old way
                  if (dbmsDef.getBinaryStorageLocation() == null) {
                  baos = new ByteArrayOutputStream();               
                  // encode it
                  PSBase64Encoder.encode(is, baos);
                     baos.flush();
   
                  columnValue = baos.toString();                  
                  } else {
                   
                     
                     //Store the binary data and save the hash part of the column
                     columnValue = storeBinaryData(dbmsDef, is);
                  }
                  encoding = PSJdbcColumnData.ENC_BASE64;
               }
            }
            catch(IOException e)
            {
               throw new RuntimeException(e.getLocalizedMessage());
            }
            finally
            {
               if (is != null)
               {
                  try
                  {
                     is.close();
                  }
                  catch (Exception e)
                  {
                  }
                  is = null;
               }
               if (baos != null)
               {
                  try
                  {
                     baos.close();
                  }
                  catch (Exception e)
                  {
                  }
                  baos = null;
               }
            }   
            break;
         case Types.CLOB:
         
            String driver = dbmsDef.getDriver();
            if ((driver.equalsIgnoreCase(PSJdbcUtils.DB2)) ||
               (driver.startsWith(PSJdbcUtils.ORACLE_PRIMARY)))
            {
               columnValue = getClobColumnData(rs,columnName);
            }
            else
            {
               columnValue = rs.getString(columnName);
            }
            break;

         default:
            columnValue = rs.getString(columnName);
            break;
      }
      return new PSJdbcColumnData(columnName, columnValue, encoding);
   }

   /**
    * Stores the binary file, if not exists.
    * @param dbmsDef assumed not <code>null</code>
    * @param baos assumed not <code>null</code>
    * @return String md5Hex hash value of the binary data
    * @throws IOException
    */
   private static String storeBinaryData(PSJdbcDbmsDef dbmsDef, InputStream is) throws IOException
   {
   
      File temp = File.createTempFile("rxtf", null,getBinaryDataFolder(dbmsDef));
      String hash = null;
     
    
      MessageDigest md=null;
      try
      {
         md = MessageDigest.getInstance("SHA-256");
         }
         catch (NoSuchAlgorithmException e)
         {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
         }

         try(FileOutputStream outputStream = new FileOutputStream(temp))
         {

            byte[] dataBytes = new byte[4096];
  
            int nread = 0;
            while ((nread = is.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
                outputStream.write(dataBytes, 0, nread);
                
            }
            byte[] mdbytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte mdbyte : mdbytes) {
               sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16).substring(1));
            }
            hash = sb.toString();
            outputStream.flush();
         }
  
       File newName = new File(getBinaryDataFolder(dbmsDef) +"/" + hash);
       if(newName.exists())
          newName.delete();
       
       temp.renameTo(newName);
       
      return hash;
   }
   
   /**
    * Gets the binary data folder, if the number of files in the folder are more than the max files then creates the next bucket.
    * @param dbmsDef assumed not <code>null</code>
    * @return location for binary storage.
    */
   private static File getBinaryDataFolder(PSJdbcDbmsDef dbmsDef) 
   {
      if (dbmsDef.getBinaryStorageLocation().list().length >= PSJdbcImportExportHelper.MAX_FILES_IN_FILDER){
         File nextBucket = PSJdbcImportExportHelper.getNextBucket(dbmsDef.getBinaryStorageLocation());
         dbmsDef.setBinaryStorageLocation(nextBucket);
      }
      return dbmsDef.getBinaryStorageLocation();
   }
   
   /**
    * Returns the value of a clob column for the current row from a resultset.
    * This function does not call the <code>next</code> method of the ResultSet,
    * nor does it <code>close</code> the ResultSet.
    *
    * @param rs contains row data from which the value corresponding to the
    * column <code>columnName</code> is to be obtained, may not
    * <code>null</code>.
    * @param columnName the name of the column whose data is to be obtained,
    * may not be <code>null</code> or empty.
    *
    * @return the value of a column for the current row from a resultset, may
    * be <code>null</code> if the CLOB column value is <code>null</code>, may
    * be empty
    *
    * @throws IllegalArgumentException if rs or
    * columnName is <code>null</code> or if columnName is empty
    */
   public static String getClobColumnData(ResultSet rs, String columnName)
      throws SQLException, IOException
   {
      if (rs == null)
         throw new IllegalArgumentException("rs may not be null");
      if ((columnName == null) || (columnName.trim().length() < 1))
         throw new IllegalArgumentException("columnName may not be null or empty");

      return PSSqlHelper.getClobColumnData(rs.getCharacterStream(columnName));
   }

   /**
    * Returns a tableMetaData object with column, and primary and foreign keys
    * for the specified table.
    *
    * @param conn A valid connection, not <code>null</code>.  This class does
    * not close the connection.
    * @param dbmsDef Provides the database/schema to retrieve meta data from.
    * May not be <code>null</code>.
    * @param dataTypeMap Used to create column objects.  May not be <code>
    * null</code>.
    * @param tableName The unqualified table name to retrieve meta data for.
    * May not be <code> null</code> or empty.
    *
    * @returns The table meta data, never <code>null</code>, may not contain
    * any info if the table does not exist (check {@link
    * PSJdbcTableMetaData#exists()})
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   static PSJdbcTableMetaData getTableMetaData(Connection conn,
      PSJdbcDbmsDef dbmsDef, PSJdbcDataTypeMap dataTypeMap, String tableName)
         throws PSJdbcTableFactoryException, SQLException
   {

      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (dataTypeMap == null)
         throw new IllegalArgumentException("dataTypeMap may not be null");

      if (tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName may not be null or empty");

      DatabaseMetaData dmd = conn.getMetaData();
      return new PSJdbcTableMetaData(dmd, dbmsDef, dataTypeMap, tableName);
   }

   /**
    * Returns the singleton instance of the logger.  If logger has not been
    * instantiated yet, it will be created.  Otherwise returns the logger with
    * the logging settings intialized by one of the {@link #processTables} or
    * {@link #processTable} methods.
    *
    * @return the logger, never <code>null</code>.
    */
   static PSLogger getLogger()
   {
      if (ms_logger == null)
         ms_logger = PSLogger.getLogger();

      return ms_logger;
   }

   /**
    * Returns the singleton table schema collection object.
    *
    * @return the table schema collection object, never <code>null</code>.
    */
   public static PSJdbcTableSchemaCollection getTableSchemaCollection()
   {
      return ms_tableSchemaColl;
   }

   /**
    * Convenience method that calls <code>getLogger().logMessage()</code>.
    *
    * @param message The message to log, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if message is <code>null</code>.
    *
    * @see #getLogger()
    */
   static void logMessage(String message)
   {
      if (message == null)
         throw new IllegalArgumentException("message may not be null");
      getLogger().logMessage(message);
   }

   /**
    * Convenience method that calls <code>getLogger().logDebugMessage()</code>.
    *
    * @param message The message to log, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if message is <code>null</code>.
    *
    * @see #getLogger()
    */
   static void logDebugMessage(String message)
   {
      if (message == null)
         throw new IllegalArgumentException("message may not be null");
      getLogger().logDebugMessage(message);
   }

   /**
    * Transforms the provided document using the supplied stylesheet and
    * returns it.
    *
    * @param doc the XML file to be transformed, assumed not <code>null</code>.
    * @param xslFileName the stylesheet name to use to transfrom the supplied
    *    document, assumed not <code>null</code> or empty.
    * @return the transformed document, never <code>null</code>.
    * @throws PSJdbcTableFactoryException if the provided stylesheet cannot be
    *    found or for any transfromation error.
    */
   private static Document transform(Document doc, String xslFileName)
      throws PSJdbcTableFactoryException
   {
      // locate and load the XSL File
      URL xslUrl = PSJdbcTableFactory.class.getResource(xslFileName);
      if (xslUrl == null)
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.STYLESHEET_NOT_FOUND, xslFileName);

      DOMResult result = new DOMResult();
      try
      {
         TransformerFactory factory = TransformerFactory.newInstance();
         Transformer transformer = factory.newTransformer(
            new StreamSource(xslUrl.toString()));

         transformer.transform(new DOMSource(doc), result);
      }
      catch (Throwable t)
      {
         Object[] args =
         {
            xslFileName,
            t.getMessage()
         };
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.TRANSFORMATION_ERROR, args, t);
      }

      return (Document) result.getNode();
   }

   /**
    * Displays the command line usages instructions.
    */
   private static void showUsage()
   {
      System.out.println(
         "Usage: java com.percussion.tablefactory.PSJdbcTableFactory " +
         "<properties_file_name> <data_type_map_file> <table-desc-file> " +
         "[<table-data-file>] [-<options>[ <outFile>]]");

      out("Where:");
      out("properties_file_name - path to the properties file defining the");
      out("    backend database server.  Required.");
      out("data_type_map_file - path to XML file containing the datatype map.");
      out("    Required (but may be dummy value if the 'm' switch is set).");
      out("table-desc-file - path to XML file containing the table schema");
      out("    definitions.  Required.");
      out("table-data-file - path to XML file containing the table data");
      out("    definitions.  Optional.");
      out("options - one or more options i.e.:");
      out("    l: standard messages are logged ");
      out("    d: debug messages are logged (requires 'l' switch) ");
      out("    m: use default datatype map instead of supplied xml file.");
      out("    p: use for database publishing. If this flag is defined then");
      out("       table-desc-file should contain the path of the xml file to publish");
      out("outFile - path to the file where log messages should be written.");
      out("    Optional.  May only be specified if logging switches are also");
      out("    specified.  If not specified and logging is on, message are");
      out("    written to the console.");
      out("");
      out("Example:");
      out("com.percussion.tablefactory.PSJdbcTableFactory " +
      "serverProps.properties rxdatatypemaps.xml cmstabledef.xml " +
      "cmstabledata.xml -ld log.txt");

   }

   /**
    * Displays the command line usages instructions.
    */
   private static void showUsage2()
   {
      System.out.println(
         "Usage: java com.percussion.tablefactory.PSJdbcTableFactory " +
         "-dbprops <properties_file_name> -typemap <data_type_map_file>" +         
         "-def <table-desc-file> -data <table-data-file> " +         
         "[-options [ <outFile>]] [-log <logFile>]");
      out("Where:");
      out("properties_file_name - path to the properties file defining the");
      out("    backend database server.  Required.");
      out("data_type_map_file - path to XML file containing the datatype map.");
      out("    Required (but may be dummy value if the 'm' switch is set).");
      out("table-desc-file - path to XML file containing the table schema");
      out("    definitions.  Required.");
      out("table-data-file - path to XML file containing the table data");
      out("    definitions.  Optional.");
      out("options - one or more switches:");
      out("    l: standard messages are logged ");
      out("    d: debug messages are logged (requires 'l' switch) ");
      out("    m: use default datatype map instead of supplied xml file.");
      out("    p: use for database publishing. If this flag is defined then");
      out("       table-desc-file should contain the path of the xml file to publish");
      out("    e: if provided then password is expected to be encrypted");      
      out("logFile - path to the file where log messages should be written.");
      out("    Optional.  May only be specified if logging switches are also");
      out("    specified.  If not specified and logging is on, message are");
      out("    written to the console.");
      out("");
      out("Example:");
      out("com.percussion.tablefactory.PSJdbcTableFactory " +
      "-dbprops serverProps.properties -typemap rxdatatypemaps.xml" +      
      "-def cmstabledef.xml -data cmstabledata.xml -options ld -log log.txt");

   }


   /**
    * Prints the supplied string as a line to the system output stream.
    * (This method exists to save fifteen characters' width per line in the
    * <code>showUsage</code> method.)
    * @param s line to print, assumed not <code>null</code>.
    */
   private static void out(String s)
   {
      System.out.println( s );
   }

   /**
    * Returns the <code>PSJdbcExecutionPlanLogsContainer</code> object which
    * stores the list of <code>PSJdbcExecutionPlanLog</code> objects associated
    * with the execution of each plan.
    * @return the <code>PSJdbcExecutionPlanLogsContainer</code> object
    * containing the list of <code>PSJdbcExecutionPlanLog</code> objects
    * associated with the execution of each plan, never <code>null</code>
    */
   static public PSJdbcExecutionPlanLogsContainer getPlanLogsContainer()
   {
      return ms_planLogsContainer;
   }

   /**
    * Stylesheet to transform documents conforming to the
    * sys_DatabasePublisher.dtd or sys_Tabledef.dtd into documents conforming
    * to the tabledef.dtd.
    */
   private static final String TO_TABLEDEF = "PSJdbcTableDef.xsl";

   /**
    * Stylesheet to transform documents conforming to the
    * sys_DatabasePublisher.dtd or sys_Tabledata.dtd into documents conforming
    * to the tabledata.dtd.
    */
   private static final String TO_TABLEDATA = "PSJdbcTableData.xsl";

   /**
    * Holds that static reference to the singleton logger object.  <code>null
    * </code> until the first call to {@link #getLogger()}, then not modified
    * after that.
    */
   private volatile static PSLogger ms_logger = null;

   /**
    * Holds that static reference to the singleton table schema
    * collection object. Never <code>null</code>.
    */
   private static PSJdbcTableSchemaCollection ms_tableSchemaColl =
         new PSJdbcTableSchemaCollection();

   /**
    * Stores the <code>PSJdbcExecutionPlanLog</code> objects associated with
    * the execution of each plan, never <code>null</code>.
    * This container is reset each time before processing the tables. Therefore
    * the user has to read the results before calling {@link #processTables(
    * PSJdbcDbmsDef, Document, Document, Document, PrintStream, boolean)}
    * method the next time.
    */
   private static PSJdbcExecutionPlanLogsContainer ms_planLogsContainer =
      new PSJdbcExecutionPlanLogsContainer();

   /**
    * Column number in result set for table name
    */
   private static final int COLNO_TABLE_NAME    = 3;
}


