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

import com.percussion.util.PSSqlHelper;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Creates execution plans for schema and data modifications.
 */
public class PSJdbcPlanBuilder
{
   /**
    * Creates an execution plan for processing the table's schema changes.
    *
    * @param conn A valid connection to the table's database, never <code>null
    * </code>.  Connection is not closed by this method.
    * @param dbmsDef Used to connect to the database.  May not be <code>null
    * </code>.
    * @param tableSchema Describes the table for which plans will be created.
    * May not be <code>null</code>.
    * @param schemaPlan The plan to which execution steps for processing the
    * supplied schema will be added.  May not be <code>null</code>.
    *
    * @return The action that will be taken by the plan, one of the
    * SCHEMA_ACTION_xxx types.
    *
    * @throws IllegalArgumentException if conn, dbmsDef, tableSchema or
    * schemaPlan is <code>null</code>.
    * @throws PSJdbcTableFactoryException if the tableSchema is not valid or any
    * errors occur.
    */
   public static int createSchemaPlan(Connection conn,
      PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema tableSchema, PSJdbcExecutionPlan
         schemaPlan)
      throws PSJdbcTableFactoryException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      if (schemaPlan == null)
         throw new IllegalArgumentException("schemaPlan may not be null");

      tableSchema.validateSchema();

      // create empty plan
      PSJdbcTableFactory.logMessage("");
      PSJdbcTableFactory.logMessage(
         "Creating schema plan for table: " + tableSchema.getName());

      // create schema change event in case we need it
      List changeListeners = tableSchema.getSchemaChangeListeners();
      PSJdbcTableChangeEvent changeEvent = null;
      boolean addListeners = changeListeners != null;
      if (addListeners)
      {
         if (dbmsDef.getConnectionDetail() != null)
         {
            
            changeEvent = new PSJdbcTableChangeEvent(
               dbmsDef.getConnectionInfo(), tableSchema.getName(), 
               PSJdbcTableChangeEvent.ACTION_SCHEMA_CHANGED);
         }
         else
         {
         changeEvent = new PSJdbcTableChangeEvent(
            dbmsDef.getDriver(), dbmsDef.getServer(), dbmsDef.getDataBase(),
            dbmsDef.getSchema(), tableSchema.getName(),
            PSJdbcTableChangeEvent.ACTION_SCHEMA_CHANGED);
         }
      }

      // attempt to catalog the table, no data.
      PSJdbcTableSchema curSchema = PSJdbcTableFactory.catalogTable(conn,
         dbmsDef, tableSchema.getDataTypeMap(), tableSchema.getName(), false);

      // Does table exist?
      boolean tableExists = (curSchema != null);
      int result = SCHEMA_ACTION_NONE;
      if (!tableExists)
      {
         // if no table and alter, error
         if (tableSchema.isAlter())
         {
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.ALTER_NO_TABLE, tableSchema.getName());
         }
         else if (!tableSchema.isCreate())
         {
            // no table and don't create, return empty plan
            PSJdbcTableFactory.logMessage(
               "Table doesn't exist and create=n, no action taken");
            result = SCHEMA_ACTION_NONE;
         }
         else
         {
            // no table and allowed to create the table
            PSJdbcTableFactory.logMessage(
               "Table doesn't exist and create=y, creating table");
            result = SCHEMA_ACTION_CREATE;

            schemaPlan.addStep(PSJdbcStatementFactory.getCreateTableStatement(
               dbmsDef, tableSchema));

            // add step to create non-unique indexes
            schemaPlan.addStep(PSJdbcStatementFactory.getCreateIndexStatements(
               dbmsDef, tableSchema));

            // check if this table schema has a schema handler of type
            // "onCreate"
            PSJdbcTableSchemaHandler schemaHandler =
                  tableSchema.getTableSchemaHandler(
                     PSJdbcTableSchemaHandler.TYPE_INT_ON_CREATE);
            if (schemaHandler != null)
            {
               // add a step for processing the schema handler after this
               // table is created
               PSJdbcTableSchemaHandlerStep handlerStep =
                  new PSJdbcTableSchemaHandlerStep(
                     dbmsDef, tableSchema, null, schemaHandler);
               schemaPlan.addStep(handlerStep);
            }
         }
      }
      else
      {
         boolean hasRows = PSJdbcTableFactory.hasRows(conn, dbmsDef,
            tableSchema);

         // if not alter, get diffs, otherwise use alter schema
         StringBuilder schemaChangeBuffer = new StringBuilder();
         PSJdbcTableSchema tableChanges = tableSchema.isAlter() ?
            tableSchema :
            getTableChanges(curSchema, tableSchema, schemaChangeBuffer);

         // log the differences between the two schema objects
         if (tableChanges.hasChanges())
         {
            PSJdbcTableFactory.logMessage("Schema change : " + NEWLINE);
            PSJdbcTableFactory.logMessage(schemaChangeBuffer.toString());
         }
         if (!tableChanges.hasChanges())
         {
            // no changes, so just handle delolddata
            PSJdbcTableFactory.logMessage("Table exists, no changes");
            result = SCHEMA_ACTION_NONE;
            if (tableSchema.isDelOldData() && hasRows)
            {
               PSJdbcTableFactory.logMessage("Deleting all data");
               schemaPlan.addStep(PSJdbcStatementFactory.getClearTableStatement(
                  dbmsDef, tableSchema));
            }
         }
         else if ((tableChanges.canBeAltered()) &&
            (tableSchema.getTableSchemaHandler(
               PSJdbcTableSchemaHandler.TYPE_INT_NO_ALTER_TABLE_STMT) == null))
         {
            // only adding stuff, so delete data if required, alter table
            PSJdbcTableFactory.logMessage("Table exists, can alter");
            result = SCHEMA_ACTION_ALTER;

            if (tableSchema.isDelOldData() && hasRows)
            {
               PSJdbcTableFactory.logMessage("Deleting all data");
               schemaPlan.addStep(PSJdbcStatementFactory.getClearTableStatement(
                  dbmsDef, tableSchema));
            }

            // use the schema with the changes only to build the alter stmt
            PSJdbcExecutionStep step =
               PSJdbcStatementFactory.getAlterTableStatement(
               dbmsDef, tableChanges);

            // add change event to this step
            if (addListeners)
               step.addTableChangeEvent(changeEvent, changeListeners);

            schemaPlan.addStep(step);

            // add step to drop non-unique indexes
            schemaPlan.addStep(PSJdbcStatementFactory.getDropIndexStatements(
               dbmsDef, tableChanges));
            // add step to create non-unique indexes
            schemaPlan.addStep(PSJdbcStatementFactory.getCreateIndexStatements(
               dbmsDef, tableChanges));
         }
         else
         {
            PSJdbcTableFactory.logMessage("Table exists, can't alter");
            result = SCHEMA_ACTION_RECREATE;

            /* if only alter changes specified, need to overlay the changes onto
             * the existing schema so we have the full definition of what we
             * want to create
             */
            PSJdbcTableSchema newSchema = new PSJdbcTableSchema(tableSchema.isAlter() ?
               addTableChanges(curSchema, tableSchema) : tableSchema);
            // remove ForeignKey constraints to avoid double creation of the constraints (bug RHYT-426)
            newSchema.clearForeignKeys();

            boolean copyData = false;
            if (newSchema.isDelOldData() || !hasRows)
            {
               // no existing data or we're deleting it, so drop and create
               PSJdbcTableFactory.logMessage("No data or else deleting " +
                     "old data, backup, dropping and recreating");
            }
            else
            {
               // backup, drop, create, and restore
               PSJdbcTableFactory.logMessage(
                  "Table has data, backup, dropping, recreating, restore data");
               copyData = true;
            }
            
            try
            { 
               //find out if any other table has a foreign key that will
               //prevent us from dropping this table
               DatabaseMetaData dmd = conn.getMetaData();
               String[] names = {"TABLE"};
               ResultSet rsTableNames = dmd.getTables(null,
                  dbmsDef.getSchema(), "%", names); 
               
               while (rsTableNames.next())
               { 
                  String tableName = rsTableNames.getString("TABLE_NAME");
                  
                  //get FKs for a given table 
                  ResultSet  rs = dmd.getImportedKeys(dbmsDef.getDataBase(), 
                     dbmsDef.getSchema(), tableName);
                  if (rs != null)
                  {
                     while (rs.next())
                     {
                        //get table name
                        String fTableName = rs.getString(3);
                        if (fTableName.compareToIgnoreCase(
                           curSchema.getName())!=0) //is it our child?
                           continue;
                        
                        // attempt to catalog the table, no data.
                        PSJdbcTableSchema curFKSchema =
                           PSJdbcTableFactory.catalogTable(conn,
                              dbmsDef, tableSchema.getDataTypeMap(),
                              tableName, false);
                        
                        curSchema.addTableWithForeignKey(curFKSchema);
                     }
                     
                     rs.close();
                  }
               }
               
               if (rsTableNames==null)
                  rsTableNames.close();
               
               Iterator itTableSchemasFithFK =
                  curSchema.getTablesWithForeignKey();
               
               while (itTableSchemasFithFK.hasNext())
               {
                  PSJdbcTableSchema schemaFK = (PSJdbcTableSchema)
                  itTableSchemasFithFK.next();
                  
                  //drop FK constraint - to be restored later
                  List<PSJdbcForeignKey> fks = schemaFK.getForeignKeys();
                  for (PSJdbcForeignKey fk : fks) {
                  
                  String dropFKSQL =
                     PSJdbcStatementFactory.getDropFKContraint(dbmsDef,
                        schemaFK,fk);
                  
                  if (dropFKSQL==null)
                     continue;
                  
                     schemaPlan.addStep(new PSJdbcSqlStatement(dropFKSQL));
                  }
               }
               
               // does backup table exist?
               String backupTableName = getBackupTableName(conn,
                  newSchema.getName());
               boolean backupExists = PSJdbcTableFactory.catalogTable(
                  conn, dbmsDef, newSchema.getDataTypeMap(), backupTableName,
                  false) != null;
               
               // don't create backup with any constraints
               PSJdbcTableSchema backupSchema = new PSJdbcTableSchema(
                  curSchema);
               backupSchema.setPrimaryKey(null);
               backupSchema.clearIndexes();
               backupSchema.clearForeignKeys();
               // create backup - need to pass existing table schema
               // this also copies the data from original table to backup table
               PSJdbcTableSchemaHandler schemaHandler =
                  tableSchema.getTableSchemaHandler(
                     PSJdbcTableSchemaHandler.TYPE_INT_TO_BACKUP);
               schemaPlan.addStep(getCopyTableStatement(dbmsDef, backupSchema,
                  backupTableName, backupExists, true, schemaHandler));
               
               // drop it
               schemaPlan.addStep(PSJdbcStatementFactory.getDropTableStatement(
                  dbmsDef, newSchema.getName()));
               
               // recreate
               PSJdbcExecutionStep createStep =
                  PSJdbcStatementFactory.getCreateTableStatement(dbmsDef,
                     newSchema);
               
               // need error step to restore original table if create fails
               backupSchema = new PSJdbcTableSchema(curSchema);
               backupSchema.setName(backupTableName);
               PSJdbcExecutionStep errorStep = getCopyTableStatement(dbmsDef,
                  backupSchema, curSchema.getName(),
                  false, false, null);
               createStep.setErrorStep(errorStep);
               schemaPlan.addStep(createStep);
               
               // add step to create non-unique indexes
               PSJdbcExecutionStep createIndexStep = PSJdbcStatementFactory
                     .getCreateIndexStatements(dbmsDef, newSchema);
               schemaPlan.addStep(createIndexStep);
               
               PSJdbcExecutionStep listenerStep = createIndexStep; 
                             
               if (copyData)
               {
                  // restore data from backup
                  PSJdbcExecutionStep dataStep;
                  schemaHandler =
                     tableSchema.getTableSchemaHandler(
                        PSJdbcTableSchemaHandler.TYPE_INT_FROM_BACKUP);
                  if (schemaHandler != null)
                  {
                     dataStep = new PSJdbcTableSchemaHandlerStep(dbmsDef,
                        backupSchema, newSchema, schemaHandler);
                  }
                  else
                  {
                     dataStep = 
                        PSJdbcStatementFactory.getCopyTableDataStatement(
                           dbmsDef, backupSchema, newSchema);
                  }
                  
                  schemaPlan.addStep(dataStep);
                  listenerStep = dataStep;     
                  
                  // add error step to this step to restore if data move fails
                  errorStep = getCopyTableStatement(dbmsDef, backupSchema,
                     curSchema.getName(), true, false, null);
                  dataStep.setErrorStep(errorStep);
               }

               
               //restore previously removed FKs
               itTableSchemasFithFK =
                  curSchema.getTablesWithForeignKey();
               
               while (itTableSchemasFithFK.hasNext())
               {
                  PSJdbcTableSchema schema = 
                     (PSJdbcTableSchema) itTableSchemasFithFK.next();

                  String fullFKSQL = 
                     PSJdbcStatementFactory.getForeignKeyConstraint(dbmsDef,
                        schema,newSchema);
                  
                  if (fullFKSQL==null)
                     continue;
                  
                  String fullTableName = PSSqlHelper.qualifyTableName(
                     schema.getName(), dbmsDef.getDataBase(), 
                     dbmsDef.getSchema(), dbmsDef.getDriver());
                  
                  String restoreFKStep = "ALTER TABLE " + fullTableName +
                  " ADD " + fullFKSQL;
                  
                  listenerStep = new PSJdbcSqlStatement(restoreFKStep);
                  schemaPlan.addStep(listenerStep);
               }
               
               // add change event to this step
               if (addListeners && listenerStep != null)
               {
                  listenerStep.addTableChangeEvent(changeEvent, 
                     changeListeners);
               }
            }                     
            catch (SQLException e)
            {
               throw new PSJdbcTableFactoryException(0, e);
            }
            
         }
      }

      return result;
   }

   /**
    * Convenience method, calls {@link #getDataPlan(Connection,PSJdbcDbmsDef,
    * Map,PSJdbcTableSchema,PSJdbcTableData,PSJdbcExecutionPlan,
    * PSJdbcExecutionPlan,PSJdbcExecutionPlan,PSJdbcReplaceExecutionPlan,
    * boolean) getDataPlan(conn,dbmsDef,null,tableSchema,tableData,insertPlan,
    * deletePlan,updatePlan,replacePlan,isChildTable)}
    */
   static public void getDataPlan(Connection conn,
      PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema tableSchema,
      PSJdbcTableData tableData, PSJdbcExecutionPlan insertPlan,
      PSJdbcExecutionPlan deletePlan, PSJdbcExecutionPlan updatePlan,
      PSJdbcReplaceExecutionPlan replacePlan, boolean isChildTable)
      throws PSJdbcTableFactoryException
   {
      getDataPlan(
         conn,
         dbmsDef,
         null,
         tableSchema,
         tableData,
         insertPlan,
         deletePlan,
         updatePlan,
         replacePlan,
         isChildTable);
   }

   /**
    * Creates an execution plan for processing the table's data changes.
    * We need four separate plans to support child tables. The plans are
    * exectued in the following order : deletePlan then insertPlan then
    * replacePlan then updatePlan. The plans have steps for child tables
    * first and then for parent tables. For insertPlan and replacePlan
    * the steps are executed in reverse order because inserts need to
    * be done first for the parent table.
    *
    * @param conn A valid connection to the table's database, never <code>null
    * </code>.  Connection is not closed by this method.
    * @param dbmsDef Used to connect to the database.  May not be <code>null
    * </code>.
    * @param tableMetaMap Used to cache the table meta data. The meta data will
    * be created and added to the map if the processed table does not exist
    * in the map. The map key is the table name in <code>String</code>; the
    * map value is <code>PSJdbcTableMetaData</code> object of the related
    * table. It may be <code>null</code> if no need to cache the meta data.
    * @param tableSchema Describes the table for which plans will be created.
    * May not be <code>null</code>.
    * @param tableData rows of data which need to inserted, deleted, replaced
    * or updated depending upon the action attribute of the row. May not be
    * <code>null</code>.
    * @param insertPlan exectuion plan containing only the insert statements.
    * May not be <code>null</code>.
    * @param deletePlan exectuion plan containing only the delete statements.
    * May not be <code>null</code>.
    * @param updatePlan exectuion plan containing only the update statements.
    * May not be <code>null</code>.
    * @param replacePlan exectuion plan containing only the replace statements.
    * In replace statements we first do an update and if no rows are updated
    * then do an insert. May not be <code>null</code>.
    * @param isChildTable true if this table is a child table. For child
    * tables the insert action is converted to replace action. This is done
    * because for child tables we always do an update first and only if no
    * row is updated then we do an insert.
    *
    * @throws IllegalArgumentException if conn, dbmsDef, tableSchema,
    * tableData, insertPlan, deletePlan, updatePlan, replacePlan is
    * <code>null</code>, or if a row with an unrecognized action is supplied.
    *
    * @throws PSJdbcTableFactoryException if any errors occur.
    */
   static public void getDataPlan(
      Connection conn,
      PSJdbcDbmsDef dbmsDef,
      Map tableMetaMap,
      PSJdbcTableSchema tableSchema,
      PSJdbcTableData tableData,
      PSJdbcExecutionPlan insertPlan,
      PSJdbcExecutionPlan deletePlan,
      PSJdbcExecutionPlan updatePlan,
      PSJdbcReplaceExecutionPlan replacePlan,
      boolean isChildTable)
      throws PSJdbcTableFactoryException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      if (tableData == null)
         throw new IllegalArgumentException("tableData may not be null");

      if (insertPlan == null)
         throw new IllegalArgumentException("insertPlan may not be null");

      if (deletePlan == null)
         throw new IllegalArgumentException("deletePlan may not be null");

      if (updatePlan == null)
         throw new IllegalArgumentException("updatePlan may not be null");

      if (replacePlan == null)
         throw new IllegalArgumentException("replacePlan may not be null");

      

      // check in case schema has been modified to invalidate the data.
      tableSchema.validateTableData(tableData);

      Iterator rows = tableData.getRows();
      while (rows.hasNext())
      {
         PSJdbcRowData row = (PSJdbcRowData)rows.next();

         // for parent tables, catalog all the child tables rows for this parent
       

         int schemaAction = tableSchema.getSchemaAction();
         boolean bOnTableCreateOnly = row.onTableCreateOnly();
         if (bOnTableCreateOnly && schemaAction != SCHEMA_ACTION_CREATE)
            continue;

         int rowAction = row.getAction();
         if (rowAction == PSJdbcRowData.ACTION_INSERT)
         {
            if (isChildTable)
               rowAction = PSJdbcRowData.ACTION_REPLACE;
            else
            {
               Iterator parentTables = tableSchema.getParentTables();
               if (parentTables.hasNext())
                  rowAction = PSJdbcRowData.ACTION_REPLACE;
            }
         }

         PSJdbcExecutionStep insertStep = null;
         PSJdbcExecutionStep updateStep = null;
         PSJdbcExecutionStep deleteStep = null;

         // create child table plans here.
        
       
         switch (rowAction)
         {
            case PSJdbcRowData.ACTION_INSERT:
               insertStep = PSJdbcStatementFactory.getInsertStatement(
                  dbmsDef, tableSchema, row);
               insertPlan.addStep(insertStep);
               processChildTables(conn, dbmsDef, tableMetaMap, tableSchema, tableData, insertPlan, deletePlan,
                     updatePlan, replacePlan, isChildTable, row);
               break;

            case PSJdbcRowData.ACTION_INSERT_IF_NOT_EXIST:
               insertStep = PSJdbcStatementFactory.getInsertStatement(
                  dbmsDef, tableSchema, row);
               insertPlan.addStep(insertStep);
               insertStep.setIgnoreSQLExceptions(
                  PSSqlHelper.SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATIONS);
               processChildTables(conn, dbmsDef, tableMetaMap, tableSchema, tableData, insertPlan, deletePlan,
                     updatePlan, replacePlan, isChildTable, row);
               break;

            case PSJdbcRowData.ACTION_UPDATE:
               processChildTables(conn, dbmsDef, tableMetaMap, tableSchema, tableData, insertPlan, deletePlan,
                     updatePlan, replacePlan, isChildTable, row);
               updateStep = PSJdbcStatementFactory.getUpdateStatement(
                  dbmsDef, tableSchema, row);
               updatePlan.addStep(updateStep);
               break;

            case PSJdbcRowData.ACTION_REPLACE:
               updateStep = PSJdbcStatementFactory.getUpdateStatement(
                  dbmsDef, tableSchema, row);
               // ignore no rows updated exception
               updateStep.setIgnoreSQLExceptions(
                  PSSqlHelper.SQLSTATE_NO_UPDATE_ROWS);
               insertStep = PSJdbcStatementFactory.getInsertStatement(
                  dbmsDef, tableSchema, row);
               replacePlan.addStep(updateStep, insertStep);
               processChildTables(conn, dbmsDef, tableMetaMap, tableSchema, tableData, insertPlan, deletePlan,
                     updatePlan, replacePlan, isChildTable, row);
               break;

            case PSJdbcRowData.ACTION_DELETE:
               // The code below is an initial implementation of
               // programmatically doing a cascading delete. This option is not
               // supported presently and the database's cascade delete option
               // should be used for achieving this functionality.
             /* 
               Iterator childTablesList = tableSchema.getChildTables();
               while (childTablesList.hasNext())
               {
                  PSJdbcTableSchema childTableSchema =
                     (PSJdbcTableSchema)childTablesList.next();
                  addChildTablesDeletePlan(dbmsDef,
                     tableSchema, childTableSchema, row, deletePlan);
               }
               */
               processChildTables(conn, dbmsDef, tableMetaMap, tableSchema, tableData, insertPlan, deletePlan,
                     updatePlan, replacePlan, isChildTable, row);
               
               deleteStep = PSJdbcStatementFactory.getDeleteStatement(
                  dbmsDef, tableSchema, row);
               deletePlan.addStep(deleteStep);
               break;

            default:
               throw new IllegalArgumentException("unknown action (" +
                  rowAction + ") creating data plan for table: " +
               tableSchema.getName());
         }

         setLogData(insertStep, dbmsDef, tableSchema, row);
         setLogData(updateStep, dbmsDef, tableSchema, row);
         setLogData(deleteStep, dbmsDef, tableSchema, row);
         
        
      }
   }

   private static void processChildTables(
         Connection conn,
         PSJdbcDbmsDef dbmsDef,
         Map tableMetaMap,
         PSJdbcTableSchema tableSchema,
         PSJdbcTableData tableData,
         PSJdbcExecutionPlan insertPlan,
         PSJdbcExecutionPlan deletePlan,
         PSJdbcExecutionPlan updatePlan,
         PSJdbcReplaceExecutionPlan replacePlan,
         boolean isChildTable,
         PSJdbcRowData row) throws PSJdbcTableFactoryException
   {
      PSJdbcDataTypeMap dataTypeMap = null;
      try
      {
         dataTypeMap = new PSJdbcDataTypeMap(dbmsDef.getBackEndDB(),
            dbmsDef.getDriver(), null);
      }
      catch (Exception e)
      {
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.LOAD_DEFAULT_DATATYPE_MAP, e.toString(), e);
      }
      Iterator childTables = row.getChildTables();
      while (childTables.hasNext())
      {
         PSJdbcTableData childTableData =
            (PSJdbcTableData)childTables.next();
         PSJdbcTableSchema childTableSchema =
            PSJdbcTableFactory.getTableSchemaCollection().getTableSchema(
               childTableData.getName());
         PSJdbcPlanBuilder.getDataPlan(conn, dbmsDef, childTableSchema,
            childTableData, insertPlan, deletePlan, updatePlan, replacePlan,
            true);
      }
      
      // table row based on the foreign key relationship
      // we need to retrieve the value of primary key columns only for the
      // child table.
      // delete all the rows from the child table in the database which are
      // not in the tabledata
      // primary key can be composed of mulitple columns which forces us to
      // use this complicated mechanism instead of some simple SQL Statements
      if (!isChildTable)
      {
         Iterator childTblIt = tableSchema.getChildTables();
         while (childTblIt.hasNext())
         {
            PSJdbcTableSchema childTableSchema =
               PSJdbcTableFactory.getTableSchemaCollection().getTableSchema(
                  (String)childTblIt.next());

            if (!canUnpublishChildTables(tableSchema, childTableSchema, row))
            {
               // the update/primary/foreign keys may be such that it is not
               // possible to unpublish from the child table
               continue;
            }

            //
            // check if the child table exists in the database
            //
            // For database publisher, <code>dbmsDef</code> object has the
            // member variable <code>m_schema</code> set to <code>null</code>
            // "datapublisher" element defined in sys_DatabasePublisher.dtd
            // only defines two attributes "dbname" and "drivertype". A
            // new attribute "dbschema" is required to set the schema
            // correctly.
            // However for database publishing we make the assumption that
            // the tables already exist, so we need not check if the
            // child table exists in the database.
            if (dbmsDef.getSchema() != null)
            {
               PSJdbcTableSchema testChildTblSchema =
                  PSJdbcTableFactory.catalogTable(
                     conn,
                     dbmsDef,
                     tableMetaMap,
                     dataTypeMap,
                     childTableSchema.getName(),
                     false);
               if (testChildTblSchema == null)
                  continue;
            }

            String childTableName = PSSqlHelper.qualifyTableName(
               childTableSchema.getName(), dbmsDef.getDataBase(),
               dbmsDef.getSchema(), dbmsDef.getDriver());

            // construct the WHERE clause
            PSJdbcFilterContainer selFilter = new PSJdbcFilterContainer();
            List<PSJdbcForeignKey> fkeys= childTableSchema.getForeignKeys();
            for (PSJdbcForeignKey fkey : fkeys) {
               Iterator fkeyColIt = fkey.getColumns(tableSchema.getName());
               while (fkeyColIt.hasNext())
               {
                  String[] col = (String[])fkeyColIt.next();
                  String childTableColName = col[0];
                  String parentTableColName = col[2];
                  PSJdbcColumnDef colDef = childTableSchema.getColumn(
                     childTableColName);

                  PSJdbcColumnData colData = row.getColumn(parentTableColName);
                  if (colData == null)
                     continue;
                  if (colData.getValue() == null)
                     continue;

                  selFilter.doAND(new PSJdbcSelectFilter(
                     childTableName + "." + childTableColName,
                     PSJdbcSelectFilter.EQUALS,
                     colData.getValue(),
                     colDef.getType()));
               }
            }
            // get the key columns of the child table
            List keyCols = childTableSchema.getKeyColumns();
            int keyColsSize = keyCols.size();
            String [] pkColumns = new String[keyColsSize];
            for (int colIndex = 0; colIndex < keyColsSize; colIndex++)
               pkColumns[colIndex] = (String)keyCols.get(colIndex);

            // catalog the child table
            PSJdbcTableData childTblData =
               PSJdbcTableFactory.catalogTableData(conn,
                  dbmsDef, childTableSchema, pkColumns,
                  selFilter, PSJdbcRowData.ACTION_DELETE);

            // tabledata in memory for child table
            PSJdbcTableData memChildTblData = null;
            Iterator memChildTables = row.getChildTables();
            while (memChildTables.hasNext())
            {
               PSJdbcTableData tempMemChildTblData =
                  (PSJdbcTableData)memChildTables.next();
               if (childTableSchema.getName().equalsIgnoreCase(
                  tempMemChildTblData.getName()))
               {
                  memChildTblData = tempMemChildTblData;
                  break;
               }
            }

            // do a diff with the tabledata in memory
            PSJdbcTableData childTableData = doTableDataDiff(childTableSchema,
               childTblData, memChildTblData);

            // create a plan to remove the rows from the child tables
            if (childTableData != null)
            {
               PSJdbcPlanBuilder.getDataPlan(conn, dbmsDef, childTableSchema,
                  childTableData, insertPlan, deletePlan, updatePlan,
                  replacePlan, true);
            }
         }
      }
      return;
   }

   /**
    * Returns <code>true</code> if it is possible to unpublish from child table
    * based on the primary and foreign keys and a single row of data from the
    * parent table.
    * If childTableSchema is <code>null</code>, this method returns
    * <code>false</code>.
    *
    * @param parentTableSchema the schema of the parent table, assumed not
    * <code>null</code>
    * @param childTableSchema the schema of the child table, may be
    * <code>null</code>
    * @param parentRowData encapsulates the a single row of data from the parent
    * table, assumed not <code>null</code>
    *
    * @return <code>true</code> if it is possible to unpublish from child table,
    * <code>false</code> otherwise
    */
   @SuppressWarnings("unchecked")
   private static boolean canUnpublishChildTables(PSJdbcTableSchema parentTableSchema,
      PSJdbcTableSchema childTableSchema, PSJdbcRowData parentRowData)
   {
      if (childTableSchema == null)
         return false;

      // get the key columns of the child table
      // if no (update or primary) key columns are defined we cannot compare the
      // child table rows
      List keyCols = childTableSchema.getKeyColumns();
      if (keyCols.isEmpty())
         return false;

      // check if we can construct a valid WHERE clause of the SELECT query
      // for obtaining the child table rows corresponing to this parent table
      // row
      List<PSJdbcForeignKey> fkeys = childTableSchema.getForeignKeys();
      if (fkeys == null || fkeys.size()==0)
         return false;
      boolean bHasValidForeignKey = false;
      for (PSJdbcForeignKey fkey : fkeys) {
      Iterator fkeyColIt = fkey.getColumns(parentTableSchema.getName());
      List fkeyCols = new ArrayList();
  
      while (fkeyColIt.hasNext())
      {
         String[] col = (String[])fkeyColIt.next();
         String childTableColName = col[0];
         String parentTableColName = col[2];

         fkeyCols.add(childTableColName);

         // check if the parent row contains the value for the foreign key column
         // if not we cannot unpublish since the SELECT query for obtaining the
         // child table rows will be invalid
         PSJdbcColumnData colData = parentRowData.getColumn(parentTableColName);
         if ((colData == null) || (colData.getValue() == null))
         {
            // this situation will occur if the parent column is a
            // sequence/identity so that its value does not exist in the xml
            // file. we assume that the user will need to define another
            // foreign key for such cases
         }
         else
         {
            // if we can obtain the value of one foreign key column from
            // the parent row, we will allow it to proceed
            bHasValidForeignKey = true;
            break;
         }
      }
      if (bHasValidForeignKey == true) break;
      }
      return bHasValidForeignKey;
   }

   /**
    * Constructs a <code>PSJdbcTableData</code>object containing all the rows
    * that are in the dbChildTblData but not in the memChildTblData
    * @param tableSchema schema object for the table whose data is to be
    * compared, may not be <code>null</code>
    * @param dbChildTblData contains the rows from the database, may be
    * <code>null</code> if no rows exist in the database for this table
    * @param memChildTblData contains the rows obtained by parsing the xml file,
    * may be <code>null</code> if no data has been specified in the xml file
    * for this table
    *
    * @return a <code>PSJdbcTableData</code>object containing all the rows
    * that are in the dbChildTblData but not in the memChildTblData, may return
    * <code>null</code> if no rows exist in the database for this table
    *
    * @throws PSJdbcTableFactoryException if there are any errors.
    * @throws IllegalArgumentException if tableSchema is <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private static PSJdbcTableData doTableDataDiff(PSJdbcTableSchema tableSchema,
      PSJdbcTableData dbChildTblData, PSJdbcTableData memChildTblData)
      throws PSJdbcTableFactoryException
   {
      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      if (dbChildTblData == null)
      {
         // no rows exist in the database for this table
         return null;
      }
      Iterator itDb = dbChildTblData.getRows();
      if (!itDb.hasNext())
      {
         // no rows exist for this table in the database
         return null;
      }

      if (memChildTblData == null)
      {
         // no rows specified for this table in the xml file
         return dbChildTblData;
      }
      Iterator itMem = memChildTblData.getRows();
      if (!itMem.hasNext())
      {
         // no rows exist for this table in the xml file
         return dbChildTblData;
      }

      List rowList = new ArrayList();
      while (itDb.hasNext())
      {
         PSJdbcRowData dbRowData = (PSJdbcRowData)itDb.next();
         boolean addRowData = true;
         itMem = memChildTblData.getRows();
         while (itMem.hasNext())
         {
            PSJdbcRowData memRowData = (PSJdbcRowData)itMem.next();
            if (hasSameKeyValues(tableSchema, dbRowData, memRowData))
            {
               // match found for this database row data in the xml file
               addRowData = false;
               break;
            }
         }
         if (addRowData)
         {
            // no match found for this database row data in the xml file
            rowList.add(dbRowData);
         }
      }
      if (rowList.isEmpty())
         return null;
      return new PSJdbcTableData(tableSchema.getName(),
         rowList.iterator());
   }


   /**
    * Compares two rows based upon their primary key values.
    * 
    * @param tableSchema Table Schema object from which the primary keys are
    * obtained, may not be <code>null</code>.
    * @param rowData RowData object to be compared, may not be <code>null</code>.
    * @param rowDataToCompare RowData object to be compared, may not be
    * <code>null</code>.
    * 
    * @return <code>true</code> if both rows have the same values for primary
    * key, else return <code>false</code>.
    * 
    * @throws IllegalArgumentException if rowData or rowDataToCompare or schema
    * is <code>null</code> or table schema has no primary key defined.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public static boolean hasSameKeyValues(PSJdbcTableSchema tableSchema,
      PSJdbcRowData rowData, PSJdbcRowData rowDataToCompare)
      throws PSJdbcTableFactoryException
   {
      if (rowData == null)
         throw new IllegalArgumentException("row data may not be null");

      if (rowDataToCompare == null)
         throw new IllegalArgumentException("rowDataToCompare data may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      List pkColList = tableSchema.getKeyColumns();
      Iterator pkColNames = pkColList.iterator();
      if(!pkColNames.hasNext())
         throw new IllegalArgumentException(
            "Invalid primary or update key for table : " + tableSchema.getName());

      PSJdbcColumnData colFirst = null;
      PSJdbcColumnData colSecond = null;

      while (pkColNames.hasNext())
      {
         String colName = (String)pkColNames.next();
         colFirst =  rowData.getColumn(colName);
         if ((colFirst == null) || (colFirst.getValue() == null))
         {
            Object[] args = {tableSchema.getName(), colName};
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.UPDATE_DATA_NO_KEY_VALUE_IN_DB, args);
         }

         colSecond = rowDataToCompare.getColumn(colName);
         if ((colSecond == null) || (colSecond.getValue() == null))
         {
            Object[] args = {tableSchema.getName(), colName};
            throw new PSJdbcTableFactoryException(
               IPSTableFactoryErrors.UPDATE_DATA_NO_KEY_VALUE, args);
         }

         if(!colFirst.getValue().equals(colSecond.getValue()))
            return false;

      }
      return true;
   }

   /**
    * Sets the database, database server, table name and a single row containing
    * the primary key columns in the <code>PSJdbcLogData</code> object
    * associated with the execution of each step.
    *
    * @param step the step with whose <code>PSJdbcLogData</code>
    * object the database server and table name will be set, may be
    * <code>null</code>.
    * @param dbmsDef the database object from which the database server will
    * be obtained, never <code>null</code>.
    * @param tableSchema the table schema object from which the table name
    * will be obtained, never <code>null</code>.
    * @param row the row of data for which the step is to be executed, never
    * <code>null</code>
    *
    * @throws IllegalArgumentException if dbmsDef or tableSchema or row is
    * <code>null</code>
    */
   @SuppressWarnings("unchecked")
   static private void setLogData(PSJdbcExecutionStep step,
      PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema tableSchema,
      PSJdbcRowData row)
   {
      if(step == null) return;

      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      if (row == null)
         throw new IllegalArgumentException("row may not be null");

      PSJdbcExecutionStepLog stepLogData = step.getStepLogData();
      String dbServer = dbmsDef.getServer();
      stepLogData.setDBServer(dbServer);

      String dbName = dbmsDef.getDataBase();
      String dbType = dbmsDef.getBackEndDB();
      stepLogData.setDatabase(dbName, dbType);

      String tableName = tableSchema.getName();
      stepLogData.setTable(tableName);

      List columns = new ArrayList();
      Iterator pkColNames = tableSchema.getKeyColumns().iterator();
      if (pkColNames.hasNext())
      {
         while (pkColNames.hasNext())
         {
            String colName = (String)pkColNames.next();
            PSJdbcColumnData colData =  row.getColumn(colName);
            if(colData == null)
               continue;
            columns.add(colData);
         }
         if (columns.size() > 0)
         {
            PSJdbcRowData pkRow = new PSJdbcRowData(
               columns.iterator(), PSJdbcRowData.ACTION_INSERT);
            stepLogData.setPrimaryKey(pkRow);
         }
      }
   }

   /**
    * Adds delete steps for the child table based on the foreign key
    * relationship and the value of the foreign key columns in the parent table
    *
    * @param dbmsDef the object representing the database, may not be
    * <code>null</null>
    * @param parentTableSchema schema object representing the parent table,
    * may not be <code>null</code>
    * @param childTableSchema schema object representing the child table,
    * may not be <code>null</code>
    * @param parentRow row of data from the parent table containing the value
    * of foreign key columns which needs to be deleted from the child table,
    * may not be <code>null</code>
    * @param deletePlan the plan to which the delete steps will be added,
    * may not be <code>null</code>
    *
    * @throws IllegalArgumentException if any parameter is null.
    */
   @SuppressWarnings({"unchecked","unused"})
   static private void addChildTablesDeletePlan(PSJdbcDbmsDef dbmsDef,
      PSJdbcTableSchema parentTableSchema, PSJdbcTableSchema childTableSchema,
      PSJdbcRowData parentRow, PSJdbcExecutionPlan deletePlan)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (parentTableSchema == null)
         throw new IllegalArgumentException(
            "parentTableSchema may not be null");

      if (childTableSchema == null)
         throw new IllegalArgumentException(
            "childTableSchema may not be null");

      if (parentRow == null)
         throw new IllegalArgumentException("parentRow may not be null");

      if (deletePlan == null)
         throw new IllegalArgumentException("deletePlan may not be null");

      List<PSJdbcForeignKey> foreignKeys = childTableSchema.getForeignKeys();
      for (PSJdbcForeignKey foreignKey : foreignKeys) {
         List columns = new ArrayList();
         Iterator cols = foreignKey.getColumns(parentTableSchema.getName());
         while (cols.hasNext())
         {
            // col is a String[] with 3 entries, the child table column name,
            // the parent table name, and the parent table column name
            // respectively
            String[] col = (String[])cols.next();
            String childTableColName = col[0];
            String parentTableColName = col[2];
            PSJdbcColumnData parentColData =
               parentRow.getColumn(parentTableColName);
            if (parentColData != null)
            {
               PSJdbcColumnData childColData =
                  new PSJdbcColumnData(childTableColName,
                     parentColData.getValue(), parentColData.getEncoding());
               columns.add(childColData);
            }
         }
        
         if (columns.size() > 0)
         {
            PSJdbcRowData childRow = new PSJdbcRowData(columns.iterator(),
               PSJdbcRowData.ACTION_DELETE);
   
            Iterator grandChildTables = childTableSchema.getChildTables();
            while (grandChildTables.hasNext())
            {
               PSJdbcTableSchema grandChildTableSchema =
                  PSJdbcTableFactory.getTableSchemaCollection().getTableSchema(
                     (String)grandChildTables.next());
               Iterator childRows = getChildRows(dbmsDef, childTableSchema,
                  grandChildTableSchema, childRow);
               while (childRows.hasNext())
               {
                  addChildTablesDeletePlan(dbmsDef, childTableSchema,
                     grandChildTableSchema, (PSJdbcRowData)childRows.next(),
                     deletePlan);
               }
            }
            deletePlan.addStep(PSJdbcStatementFactory.getDeleteStatement(
               dbmsDef, childTableSchema, childRow));
         }
      }
   }

   /**
    * Fetches those child rows from the database whose foreign key column values
    * matches those of the parent table's row passed as parameter. This
    * function has not be implemented fully as we do not support doing a
    * cascading delete programmatically currently. This method needs
    * implementation of "SELECT" statement in PSJdbcStatementFactory.
    *
    * @param dbmsDef the object representing the database, may not be
    * <code>null</code>
    * @param parentTableSchema schema object representing the parent table,
    * may not be <code>null</code>
    * @param childTableSchema schema object representing the child table,
    * may not be <code>null</code>
    * @param parentRow row of data from the parent table containing the value
    * of foreign key columns which needs to be deleted from the child table,
    * may not be <code>null</code>
    *
    * @return an iterator over the list of child rows fetched from the database.
    *
    * @throws IllegalArgumentException if any parameter is null.
    */
   static private Iterator getChildRows(PSJdbcDbmsDef dbmsDef,
      PSJdbcTableSchema parentTableSchema, PSJdbcTableSchema childTableSchema,
      PSJdbcRowData parentRow)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (parentTableSchema == null)
         throw new IllegalArgumentException(
            "parentTableSchema may not be null");

      if (childTableSchema == null)
         throw new IllegalArgumentException(
            "childTableSchema may not be null");

      if (parentRow == null)
         throw new IllegalArgumentException("parentRow may not be null");

      List childRows = new ArrayList();
      // make a select query on the parent table and fetch the values of the
      // foreign key columns. Then create rows of child table and insert them
      // into the childRows list.
      //
      // ResultSet rs = conn.executeQuery(
      //      "select col1,col2 from parent table where col3 = '1');
      // while (rs.next)
      // {
      //    String col1val = rs.getString("col1");
      //    PSJdbcColumnData childColData1 = new PSJdbcColumnData(
      //       "childtablecolumn", col1val);
      //    columns.removeAll();
      //    columns.add(childColData1);
      //    PSJdbcRowData childRow = new PSJdbcRowData(columns.iterator(),
      //       PSJdbcRowData.ACTION_DELETE);
      //    childRows.add(childRow);
      // }
      return childRows.iterator();
   }


   /**
    * Creates a block of statements that creates a copy of the specified
    * table, and copying all current data to that table.  If a backup table
    * already exists, it is dropped.
    *
    * @param dbmsDef Provides the database/schema information for the table.
    * Assumed not <code>null</code>.
    * @param tableSchema The table to drop.  assumed not <code>null</code>.
    * @param targetTableName The name of the backup table to create.  Assumed
    *  not <code>null</code> or empty.
    * @param targetExists If <code>true</code>, a drop statement is first
    * added for the target table, if <code>false</code>, it is not.
    * @param failOnDataCopyError If <code>true</code>, if the copy of the data
    * fails, execution will stop.  If <code>false</code>, it will not.
    * @param schemaHandler table schema handler, may be
    * <code>null</code>
    *
    * @return The statement block, never <code>null</code> or empty.
    */
   private static PSJdbcExecutionStep getCopyTableStatement(
      PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema tableSchema,
      String targetTableName, boolean targetExists, boolean failOnDataCopyError,
      PSJdbcTableSchemaHandler schemaHandler)
   {
      PSJdbcExecutionBlock block = new PSJdbcExecutionBlock();

      // drop current target if it exists
      if (targetExists)
         block.addStep(PSJdbcStatementFactory.getDropTableStatement(dbmsDef,
            targetTableName));

      // create copy of source schema
      PSJdbcTableSchema targetSchema = new PSJdbcTableSchema(tableSchema);
      targetSchema.setName(targetTableName);

      // create the table
      block.addStep(PSJdbcStatementFactory.getCreateTableStatement(dbmsDef,
         targetSchema));

      // copy the data
      PSJdbcExecutionStep dataStep = null;
      if (schemaHandler != null)
      {
         dataStep = new PSJdbcTableSchemaHandlerStep(
            dbmsDef, tableSchema, targetSchema, schemaHandler);
      }
      else
      {
         dataStep = PSJdbcStatementFactory.getCopyTableDataStatement(
            dbmsDef, tableSchema, targetSchema);
      }
      dataStep.setStopOnError(failOnDataCopyError);
      block.addStep(dataStep);
      return block;
   }


   /**
    * Diff the two schema objects and returns table schema object containing
    * the changes.  Will contain all columns, and keys, with their actions set
    * appropriately, and the table action will be set to alter.  If a component
    * is not changed, it will still be included with its action set to {@link
    * PSJdbcTableComponent#ACTION_NONE}.  If an update key is defined in the
    * new schema, it will replace any defined in the old schema.
    *
    * @param oldSchema The schema representing the current table.  Assumed not
    * <code>null</code>.
    * @param newSchema The schema representing the desired table.  Assumed not
    * <code>null</code>.
    * @param buffer an empty string buffer to log schema changes, assumed
    * not <code>null</code> and empty
    *
    * @return The tableSchema object, or <code>null</code> if no changes are
    * required.
    *
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   @SuppressWarnings("unchecked")
   private static PSJdbcTableSchema getTableChanges(PSJdbcTableSchema oldSchema,
      PSJdbcTableSchema newSchema, StringBuilder buffer)
      throws PSJdbcTableFactoryException
   {
      // First build list of column changes
      List changedCols = new ArrayList();
      Iterator newCols = newSchema.getColumns();
      while (newCols.hasNext())
      {
         PSJdbcColumnDef newCol = (PSJdbcColumnDef)newCols.next();
         PSJdbcColumnDef oldCol = oldSchema.getColumn(newCol.getName());
         int colAction;
         if (oldCol == null)
         {
            // doesn't exist, so it's an add
            colAction = PSJdbcTableComponent.ACTION_CREATE;
            buffer.append("New column: " + NEWLINE);
            buffer.append(newCol);
         }
         else
         {
            // exists, so see if it's changed
            if (newCol.isChanged(oldCol))
            {
               colAction = PSJdbcTableComponent.ACTION_REPLACE;
               buffer.append("Modified column: " + NEWLINE);
               buffer.append("Old column: " + NEWLINE);
               buffer.append(oldCol);
               buffer.append("New column: " + NEWLINE);
               buffer.append(newCol);
            }
            else
            {
               colAction = PSJdbcTableComponent.ACTION_NONE;
            }
         }
         PSJdbcColumnDef difCol = new PSJdbcColumnDef(newCol);
         difCol.setAction(colAction);
         changedCols.add(difCol);

      }

      // Now go through old columns and see if any need to be removed
      Iterator oldCols = oldSchema.getColumns();
      while (oldCols.hasNext())
      {
         PSJdbcColumnDef oldCol = (PSJdbcColumnDef)oldCols.next();
         PSJdbcColumnDef newCol = newSchema.getColumn(oldCol.getName());
         if (newCol == null)
         {
            // not in new schema, so we need to delete it
            PSJdbcColumnDef difCol = new PSJdbcColumnDef(oldCol);
            difCol.setAction(PSJdbcTableComponent.ACTION_DELETE);
            changedCols.add(difCol);
            buffer.append("Deleted column: " + NEWLINE);
            buffer.append(oldCol);
         }
      }

      // now we can create the changed schema object using the column list
      PSJdbcTableSchema diffTableSchema = new PSJdbcTableSchema(
         newSchema.getName(), changedCols.iterator());
      diffTableSchema.setCreate(false);
      diffTableSchema.setAlter(true);

      // check primary key for changes
      int flags = PSJdbcTableComponent.COMPARE_IGNORE_NAME |
         PSJdbcTableComponent.COMPARE_IGNORE_ACTION;

      PSJdbcPrimaryKey newPKey = newSchema.getPrimaryKey();
      PSJdbcPrimaryKey oldPKey = oldSchema.getPrimaryKey();
      if (newPKey == null && oldPKey != null)
      {
         PSJdbcPrimaryKey difPKey = new PSJdbcPrimaryKey(
            oldPKey.getColumnNames(), PSJdbcTableComponent.ACTION_DELETE);
         diffTableSchema.setPrimaryKey(difPKey);
         buffer.append("Deleted primary key: " + NEWLINE);
         buffer.append(difPKey);
      }
      else if (newPKey != null)
      {
         int pkAction;
         if (oldPKey == null)
         {
            pkAction = PSJdbcTableComponent.ACTION_CREATE;
            buffer.append("New primary key: " + NEWLINE);
            buffer.append(newPKey);
         }
         else
         {
            if (newPKey.compare(oldPKey, flags) >=
               PSJdbcTableComponent.IS_EXACT_MATCH)
            {
               pkAction = PSJdbcTableComponent.ACTION_NONE;
            }
            else
            {
               pkAction = PSJdbcTableComponent.ACTION_REPLACE;
               buffer.append("Modified primary key: " + NEWLINE);
               buffer.append("Old primary key: " + NEWLINE);
               buffer.append(oldPKey);
               buffer.append("New primary key: " + NEWLINE);
               buffer.append(newPKey);
            }
         }
         PSJdbcPrimaryKey difPKey = new PSJdbcPrimaryKey(
            newPKey.getColumnNames(), pkAction);
         diffTableSchema.setPrimaryKey(difPKey);
      }

      // check foreign key for changes
      List<PSJdbcForeignKey> newFKeys = newSchema.getForeignKeys();
      List<PSJdbcForeignKey> oldFKeys = oldSchema.getForeignKeys();
      
      List<PSJdbcForeignKey> updateKeys = new ArrayList<PSJdbcForeignKey>();
      Map<PSJdbcForeignKey,PSJdbcForeignKey> modifiedKeys = new HashMap<PSJdbcForeignKey,PSJdbcForeignKey>();
      for (PSJdbcForeignKey fk : oldFKeys) 
      {
         int fkAction;
         if (!newFKeys.contains(fk)) 
         {
               PSJdbcForeignKey difFKey = new PSJdbcForeignKey(fk.getName(),fk.getColumns(),
                  PSJdbcTableComponent.ACTION_DELETE);
               updateKeys.add(difFKey);
               buffer.append("Deleted foreign key: " + NEWLINE);
               buffer.append(difFKey);
         } else {
            for (PSJdbcForeignKey newKey : newFKeys) {
               if (newKey==fk) {
                  if (newKey.isComponentEqual(fk))
                  {
                    fkAction = PSJdbcTableComponent.ACTION_NONE;
                  }
                  else
                  {
                    fkAction = PSJdbcTableComponent.ACTION_REPLACE;
                     buffer.append("Modified foreign key: " + NEWLINE);
                     buffer.append("Old foreign key: " + NEWLINE);
                     buffer.append(fk);
                     buffer.append("New foreign key: " + NEWLINE);
                     buffer.append(newKey);
                  }
                  PSJdbcForeignKey difFKey = new PSJdbcForeignKey(newKey.getName(),newKey.getColumns(),
                        fkAction);
                  updateKeys.add(difFKey);
                  break;
               }
            }
          
         }
           
      }
      
      for (PSJdbcForeignKey fk2 : newFKeys) 
      {
         if (!oldFKeys.contains(fk2)) 
         {
            buffer.append("New foreign key: " + NEWLINE);
            buffer.append(fk2);
            PSJdbcForeignKey difFKey = new PSJdbcForeignKey(fk2.getColumns(),
                  PSJdbcTableComponent.ACTION_CREATE);
            updateKeys.add(difFKey);
         }
      }
      
      diffTableSchema.setForeignKeys(updateKeys);
      
      
      
 

      // check indexes for changes
      Iterator newIndexes = null;
      Iterator oldIndexes = null;

      newIndexes = newSchema.getIndexes(
         PSJdbcIndex.TYPE_UNIQUE | PSJdbcIndex.TYPE_NON_UNIQUE);
      while (newIndexes.hasNext())
      {
         PSJdbcIndex newIndex = (PSJdbcIndex)newIndexes.next();
         oldIndexes = oldSchema.getIndexes(
            PSJdbcIndex.TYPE_UNIQUE | PSJdbcIndex.TYPE_NON_UNIQUE);

         int match = PSJdbcTableComponent.IS_GENERIC_MISMATCH;

         while (oldIndexes.hasNext())
         {
            PSJdbcIndex oldIndex = (PSJdbcIndex)oldIndexes.next();
            int compare = oldIndex.compare(newIndex, flags);
            if (compare >= PSJdbcTableComponent.IS_EXACT_MATCH)
            {
               match = compare;
               break;
            }
         }
         int indexAction = PSJdbcTableComponent.ACTION_NONE;
         if (match < PSJdbcTableComponent.IS_EXACT_MATCH)
         {
            // doesn't exist, so it's an add
            indexAction = PSJdbcTableComponent.ACTION_CREATE;
            buffer.append("New index: " + NEWLINE);
            buffer.append(newIndex);
         }

         PSJdbcIndex difIndex = new PSJdbcIndex(newIndex.getName(),
            newIndex.getColumnNames(), indexAction, newIndex.getType());
         diffTableSchema.setIndex(difIndex);
      }

      // Now go through old indexes and see if any need to be removed
      oldIndexes = oldSchema.getIndexes(
         PSJdbcIndex.TYPE_UNIQUE | PSJdbcIndex.TYPE_NON_UNIQUE);
      while (oldIndexes.hasNext())
      {
         PSJdbcIndex oldIndex = (PSJdbcIndex)oldIndexes.next();
         newIndexes = newSchema.getIndexes(
            PSJdbcIndex.TYPE_UNIQUE | PSJdbcIndex.TYPE_NON_UNIQUE);

         int match = PSJdbcTableComponent.IS_GENERIC_MISMATCH;

         while (newIndexes.hasNext())
         {
            PSJdbcIndex newIndex = (PSJdbcIndex)newIndexes.next();
            int compare = oldIndex.compare(newIndex, flags);
            if (compare >= PSJdbcTableComponent.IS_EXACT_MATCH)
            {
               match = compare;
               break;
            }
         }
         int indexAction = PSJdbcTableComponent.ACTION_NONE;
         if (match < PSJdbcTableComponent.IS_EXACT_MATCH)
         {
            // if this index is unique check if it is same as the primary
            // key
            boolean deletedIndex = true;
            if (oldIndex.getType() == PSJdbcIndex.TYPE_UNIQUE)
            {
               PSJdbcPrimaryKey newPrimKey = newSchema.getPrimaryKey();
               if (newPrimKey != null)
               {
                  PSJdbcIndex primKeyIndex = new PSJdbcIndex(
                     newPrimKey.getName(), newPrimKey.getColumnNames(),
                     oldIndex.getAction(), oldIndex.getType());
                  int comp = primKeyIndex.compare(oldIndex, flags);
                  if (comp >= PSJdbcTableComponent.IS_EXACT_MATCH)
                     deletedIndex = false;
               }
            }

            if (deletedIndex)
            {
               // not in new schema, so we need to delete it
               indexAction = PSJdbcTableComponent.ACTION_DELETE;
               buffer.append("Deleted index: " + NEWLINE);
               buffer.append(oldIndex);
            }
         }

         PSJdbcIndex difIndex = new PSJdbcIndex(oldIndex.getName(),
            oldIndex.getColumnNames(), indexAction, oldIndex.getType());
         diffTableSchema.setIndex(difIndex);
      }

      // check for an update key
      PSJdbcUpdateKey newUKey = newSchema.getUpdateKey();
      PSJdbcUpdateKey oldUKey = oldSchema.getUpdateKey();
      if (newUKey != null)
      {
         // defined in the new schema, use it
         diffTableSchema.setUpdateKey(new PSJdbcUpdateKey(
            newUKey.getColumnNames()));
      }
      else if (oldUKey != null)
      {
         // only defined in old schema, use it
         diffTableSchema.setUpdateKey(new PSJdbcUpdateKey(
            oldUKey.getColumnNames()));
      }

      return diffTableSchema;
   }


   /**
    * Takes a schema object with only changes specified and merges them with
    * a schema object fully defining the current table, so that the result is a
    * table schema fully defining the desired table.  If an updateKey is defined
    * in the object with changes, it is used in the resulting schema.
    *
    * @param oldSchema The schema representing the current table.  Assumed not
    * <code>null</code>.
    * @param newSchema The schema containing the changes to make to the current
    * table. Assumed not <code>null</code>.
    *
    * @return A new composite tableSchema object, never <code>null</code>.
    *
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   private static PSJdbcTableSchema addTableChanges(PSJdbcTableSchema oldSchema,
      PSJdbcTableSchema newSchema) throws PSJdbcTableFactoryException
   {
      // start with a copy of the old schema
      PSJdbcTableSchema resultSchema = new PSJdbcTableSchema(oldSchema);

      // process column changes
      Iterator newCols = newSchema.getColumns();
      while (newCols.hasNext())
      {
         PSJdbcColumnDef newCol = (PSJdbcColumnDef)newCols.next();
         int colAction = newCol.getAction();
         if (colAction == PSJdbcTableComponent.ACTION_NONE)
            continue;
         else if (colAction == PSJdbcTableComponent.ACTION_DELETE)
            resultSchema.removeColumn(newCol.getName());
         else
            resultSchema.setColumn(newCol);
      }

      // process primary key changes
      PSJdbcPrimaryKey newPKey = newSchema.getPrimaryKey();
      if (newPKey != null)
      {
         int colAction = newPKey.getAction();
         if (colAction == PSJdbcTableComponent.ACTION_DELETE)
            resultSchema.setPrimaryKey(null);
         else if (colAction != PSJdbcTableComponent.ACTION_NONE)
            resultSchema.setPrimaryKey(newPKey);
      }

      // process foreign key changes
      List<PSJdbcForeignKey> newFKeys = newSchema.getForeignKeys();
      List<PSJdbcForeignKey> changedFKeys = new ArrayList<PSJdbcForeignKey>();
      for (PSJdbcForeignKey newFKey : newFKeys) {
         int colAction = newFKey.getAction();
         if (colAction != PSJdbcTableComponent.ACTION_NONE && colAction != PSJdbcTableComponent.ACTION_DELETE)
            changedFKeys.add(newFKey);
      }
      resultSchema.setForeignKeys(changedFKeys);
      // process index changes
      Iterator indexes = newSchema.getIndexes(
         PSJdbcIndex.TYPE_UNIQUE | PSJdbcIndex.TYPE_NON_UNIQUE);
      while (indexes.hasNext())
      {
         PSJdbcIndex newIndex = (PSJdbcIndex)indexes.next();
         int action = newIndex.getAction();
         if (action == PSJdbcTableComponent.ACTION_DELETE)
            resultSchema.removeIndex(newIndex.getName());
         else if (action != PSJdbcTableComponent.ACTION_NONE)
            resultSchema.setIndex(newIndex);
      }

      // process update key - if defined in newSchema, use it
      PSJdbcUpdateKey newUKey = newSchema.getUpdateKey();
      if (newUKey != null)
         resultSchema.setUpdateKey(newUKey);

      return resultSchema;
   }

   /**
    * Creates a backup table name for the specified table.  Appends value of
    * {@link #BACKUP_SUFFIX} to the name, first truncating the name if the
    * resulting length would exceed the databases maxium table length.
    *
    * @param conn A connection to use to get the max table name length, assumed
    * to be a valid connection.
    * @param tablename The name of the table, assumed not <code>null</code> or
    * empty.
    *
    * @return The backup name, not <code>null</code> or empty.
    *
    * @throws PSJdbcTableFactoryException if an error occurs retrieving the max
    * length allowed from the database.
    */
   private static String getBackupTableName(Connection conn, String tablename)
      throws PSJdbcTableFactoryException
   {
      try
      {
         String bakName = tablename + BACKUP_SUFFIX;
         int max = conn.getMetaData().getMaxTableNameLength();
         if (bakName.length() > max)
         {
            int dif = bakName.length() - max;
            String base = tablename.substring(0, tablename.length() - dif);
            bakName = base + BACKUP_SUFFIX;
         }
         return bakName;
      }
      catch (SQLException e)
      {
         Object[] args = {tablename,
            PSJdbcTableFactoryException.formatSqlException(e)};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.SCHEMA_PROCESS_ERROR, args, e);
      }
   }

   /**
    * Serializes the table schema to Xml and writes it to the log.
    *
    * @param tableSchema the table schema which will be serialized to Xml
    * format and written to log, assumed not <code>null</code>
    */
   @SuppressWarnings("unused")
   private static void logTableSchema(PSJdbcTableSchema tableSchema)
   {
      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = tableSchema.toXml(doc);
         doc.appendChild(root);
         PSJdbcTableFactory.logMessage("Schema change : ");
         PSJdbcTableFactory.logMessage(PSXmlDocumentBuilder.toString(doc));
      }
      catch (Exception e)
      {
         //no-op
      }
   }

   /**
    * Constant to indicate no action will be taken when processing the schema.
    */
   public static int SCHEMA_ACTION_NONE = 0;

   /**
    * Constant to indicate the table will be created when processing the schema.
    */
   public static int SCHEMA_ACTION_CREATE = 1;

   /**
    * Constant to indicate the table will be altered when processing the schema.
    */
   public static int SCHEMA_ACTION_ALTER = 2;

   /**
    * Constant to indicate the table will be backed up, dropped and recreated
    * when processing the schema.
    */
   public static int SCHEMA_ACTION_RECREATE = 3;

   /**
    * Text to append to table name when creating a backup.
    */
   public static final String BACKUP_SUFFIX = "_BAK";

   /**
    * Constant for newline character.
    */
   public static final String NEWLINE =
      System.getProperty("line.separator", "\n");
 }


