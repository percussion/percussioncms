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

import com.percussion.util.PSCollection;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.PSJdbcUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


/**
 * This class is used to generate sql statements from PSJdbcTableComponent
 * objects.  It contains only static methods, and may not be instantiated.
 */
public class PSJdbcStatementFactory
{

   private static final Logger log = LogManager.getLogger(PSJdbcStatementFactory.class);

   /**
    * Private ctor to disallow instantiation.
    */
   private PSJdbcStatementFactory()
   {
   }

   /**
    * Returns an execution step that will create the specified table.
    *
    * @param dbmsDef Provides the database/schema information for the table.
    * May not be <code>null</code>.
    * @param tableSchema The table to create.  May not be <code>null</code>.
    *
    * @return The statement, never <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is <code>null</code>.
    */
   public static PSJdbcExecutionStep getCreateTableStatement(
      PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema tableSchema)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      StringBuffer buf = new StringBuffer();

      String fullName = PSSqlHelper.qualifyTableName(tableSchema.getName(),
         dbmsDef.getDataBase(), dbmsDef.getSchema(), dbmsDef.getDriver());

      buf.append("CREATE TABLE ");
      buf.append(fullName);
      buf.append(" (");

      Iterator columns = tableSchema.getColumns();
      boolean isFirst = true;
      while (columns.hasNext())
      {
         PSJdbcColumnDef col = (PSJdbcColumnDef)columns.next();
         if (!isFirst)
            buf.append(", ");
         else
            isFirst = false;

         buf.append(col.getSqlDef(dbmsDef));
      }

      String pKeyDef = getPrimaryKeyConstraint(dbmsDef, tableSchema);
      if (pKeyDef != null)
      {
         buf.append(", ");
         buf.append(pKeyDef);
      }

      String fKeyDef = getForeignKeyConstraint(dbmsDef, tableSchema);
      if (fKeyDef != null)
      {
         buf.append(", ");
         buf.append(fKeyDef);
      }

      String indexdefs = getUniqueConstraints(dbmsDef, tableSchema);
      if (indexdefs != null)
      {
         buf.append(", ");
         buf.append(indexdefs);
      }

      buf.append(")");
      return new PSJdbcSqlStatement(buf.toString());
   }
   
   /**
    * Returns an execution step that will drop FK constraint.
    * @param dbmsDef Provides the database/schema information for the table.
    * May not be <code>null</code>.
    * @param tableSchema Provides the table components that require changes,
    * but may contain columns with action set to {@link
    * PSJdbcTableComponent#ACTION_NONE}, which are used in primary or foreign
    * keys.  May not be <code>null</code>.
    * @return complete ALTER table SQL statement, returns <code>null</code>
    * if there is no FK in this table.
    */
   public static String getDropFKContraint(PSJdbcDbmsDef dbmsDef,
      PSJdbcTableSchema tableSchema, PSJdbcForeignKey fk)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");
         
      String fullName = PSSqlHelper.qualifyTableName(tableSchema.getName(),
         dbmsDef.getDataBase(), dbmsDef.getSchema(), dbmsDef.getDriver());
      
      String buffer = "ALTER TABLE "  + fullName +
      " DROP CONSTRAINT " + fk.getName();
      
      if(PSSqlHelper.isMysql(dbmsDef.getDriver()))
      {
          buffer = "ALTER TABLE "  + fullName +
          " DROP FOREIGN KEY " + fk.getName();
      }
         
      return buffer;
   }

   /**
    * Returns an execution step that will drop Primary Key constraint.
    * @param dbmsDef Provides the database/schema information for the table.
    * May not be <code>null</code>.
    * @param tableSchema Provides the table components that require changes,
    * but may contain columns with action set to {@link
    * PSJdbcTableComponent#ACTION_NONE}, which are used in primary or foreign
    * keys.  May not be <code>null</code>.
    * @return complete ALTER table SQL statement, returns <code>null</code>
    * if there is no FK in this table.
    */
   public static String getDropPrimaryContraint(PSJdbcDbmsDef dbmsDef,
      PSJdbcTableSchema tableSchema, PSJdbcPrimaryKey pk)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      String fullName = PSSqlHelper.qualifyTableName(tableSchema.getName(),
         dbmsDef.getDataBase(), dbmsDef.getSchema(), dbmsDef.getDriver());

      String buffer = "ALTER TABLE "  + fullName +
      " DROP CONSTRAINT " + pk.getName();

      if(PSSqlHelper.isMysql(dbmsDef.getDriver()))
      {
          buffer = "ALTER TABLE "  + fullName +
          " DROP PRIMARY KEY " + pk.getName();
      }

      return buffer;
   }
   /**
    * Returns an execution step that will alter the specified table.  Can only
    * process components that are being added.
    *
    * @param dbmsDef Provides the database/schema information for the table.
    * May not be <code>null</code>.
    * @param tableSchema Provides the table components that require changes,
    * but may contain columns with action set to {@link
    * PSJdbcTableComponent#ACTION_NONE}, which are used in primary or foreign
    * keys.  May not be <code>null</code>.
    *
    * @return The statement, never <code>null</code>. Is actually a block of
    * alter table statements.
    *
    * @throws IllegalArgumentException if tableSchema contains any components
    * with an action set to {@link PSJdbcTableComponent#ACTION_DELETE} or {@link
    * PSJdbcTableComponent#ACTION_REPLACE} or any param is <code>null</code>.
    */
   public static PSJdbcExecutionStep getAlterTableStatement(PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema tableSchema)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      String fullTableName = PSSqlHelper.qualifyTableName(tableSchema.getName(), dbmsDef.getDataBase(),
            dbmsDef.getSchema(), dbmsDef.getDriver());

      PSJdbcExecutionBlock block = new PSJdbcExecutionBlock();

      // add each column create
      Iterator columns = tableSchema.getColumns();
      while (columns.hasNext())
      {
         PSJdbcColumnDef column = (PSJdbcColumnDef)columns.next();
         if (!column.canAlter())
            throw new IllegalArgumentException("invalid alter action on column " + column.getName());
         if (column.getAction() == PSJdbcTableComponent.ACTION_CREATE)
         {
            block.addStep(getAddComponentStatement(fullTableName, column.getSqlDef(dbmsDef)));
         }
         else if (column.getAction() == PSJdbcTableComponent.ACTION_DELETE)
         {
            block.addStep(getDropComponentStatement(fullTableName, column.getName()));
         }
      }

      // add pk create
      PSJdbcPrimaryKey pKey = tableSchema.getPrimaryKey();
      if (pKey != null )
      {
         if (pKey.getAction() == PSJdbcTableComponent.ACTION_CREATE) {
            block.addStep(getAddComponentStatement(fullTableName, getPrimaryKeyConstraint(dbmsDef, tableSchema)));
      }
         else if (pKey.getAction() == PSJdbcTableComponent.ACTION_DELETE)
         {
            block.addStep(new PSJdbcSqlStatement(getDropPrimaryContraint(dbmsDef, tableSchema, pKey)));
         }

      }

      // add each fk create
      for (PSJdbcForeignKey fKey : tableSchema.getForeignKeys())
      {

         if (fKey != null)
         {
            String fkName = getQualifiedFkName(tableSchema.getName(), fKey.getName());

         // add a separate alter statement for each external table constraint
         Iterator tables = fKey.getTables();
         int i = 1;
         while (tables.hasNext())
         {
            String tableName = (String)tables.next();
            Iterator cols = fKey.getColumns(tableName);
               if (fKey.getAction() == PSJdbcTableComponent.ACTION_CREATE)
               {
            block.addStep(getAddComponentStatement(fullTableName,
                        getForeignKeyConstraintInt(dbmsDef, fkName + "_" + i++, cols)));
         }
               else if (fKey.getAction() == PSJdbcTableComponent.ACTION_DELETE)
               {
                  block.addStep(new PSJdbcSqlStatement(getDropFKContraint(dbmsDef, tableSchema, fKey)));
      }
            }

         }
      }
      //add indexes as unique constraints
      Iterator indexes = tableSchema.getIndexes(PSJdbcIndex.TYPE_UNIQUE);
      while (indexes.hasNext())
      {
         PSJdbcIndex index = (PSJdbcIndex)indexes.next();
         if (index.getAction() == PSJdbcTableComponent.ACTION_CREATE)
            block.addStep(getAddComponentStatement(fullTableName, getUniqueConstraint(dbmsDef, tableSchema, index)));
         else if (index.getAction() == PSJdbcTableComponent.ACTION_DELETE) {
            block.addStep(new PSJdbcSqlStatement(getDropUniqueContraint(dbmsDef, tableSchema, index)));
         }
      }

      return block;
   }

   /**
    * Create statement to drop an individual unique constraint;
    *
    * @param dbmsDef
    * @param tableSchema
    * @param index
    * @return
    */
   private static String getDropUniqueContraint(PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema tableSchema, PSJdbcIndex index)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      String dropSql="";

         if (index.getAction() == PSJdbcTableComponent.ACTION_DELETE)
         {
            String indexName = index.getName();
            if (indexName == null || indexName.trim().length() == 0)
               throw new IllegalArgumentException("Index must have a valid "
                  + "name for creating DROP INDEX statement");

            dropSql = PSSqlHelper.getDropIndexStatement(
               dbmsDef.getDriver(), tableSchema.getName(),
               dbmsDef.getDataBase(), dbmsDef.getSchema(), indexName);


         }
      return dropSql;
   }

   /**
    * Returns an execution step that will drop the specified table.
    *
    * @param dbmsDef Provides the database/schema information for the table.
    * May not be <code>null</code>.
    * @param tableName The table to drop.  May not be <code>null
    * </code> or empty.
    *
    * @return The statement, never <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public static PSJdbcExecutionStep getDropTableStatement(
      PSJdbcDbmsDef dbmsDef, String tableName)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName may not be null or empty");

      String qualifiedName = PSSqlHelper.qualifyTableName(tableName,
         dbmsDef.getDataBase(), dbmsDef.getSchema(), dbmsDef.getDriver());

       return new PSJdbcSqlStatement("DROP TABLE " + qualifiedName);
   }

   /**
    * Copies data from the source table to the target table, excluding source
    * columns that do not exist in the target.  If target columns are missing
    * from the source, then a default value must be provided in the column if
    * it does not allow nulls.
    *
    * @param dbmsDef Provides the database/schema information for the tables.
    * May not be <code>null</code>.
    * @param sourceTableSchema The source table.  May not be <code>null</code>.
    * @param targetTableSchema The target table.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public static PSJdbcExecutionStep getCopyTableDataStatement(
      PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema sourceTableSchema,
      PSJdbcTableSchema targetTableSchema)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (sourceTableSchema == null)
         throw new IllegalArgumentException(
            "sourceTableSchema may not be null");

      if (targetTableSchema == null)
         throw new IllegalArgumentException(
            "targetTableSchema may not be null");

      String strFullSource = PSSqlHelper.qualifyTableName(
         sourceTableSchema.getName(), dbmsDef.getDataBase(),
         dbmsDef.getSchema(), dbmsDef.getDriver());

      String strFullTarget = PSSqlHelper.qualifyTableName(
         targetTableSchema.getName(), dbmsDef.getDataBase(),
         dbmsDef.getSchema(), dbmsDef.getDriver());

      StringBuffer buf = new StringBuffer();
      buf.append("INSERT INTO ");
      buf.append(strFullTarget);
      buf.append(" (");

      // add columns from target, saving values for binding as we go
      StringBuffer valueBuf = new StringBuffer();
      boolean firstTime = true;
      PSCollection values = new PSCollection(PSJdbcStatementColumn.class);
      Iterator targetColumns = targetTableSchema.getColumns();
      while (targetColumns.hasNext())
      {
         // first check to see if it's in the source
         PSJdbcColumnDef tCol = (PSJdbcColumnDef)targetColumns.next();
         PSJdbcColumnDef sCol = sourceTableSchema.getColumn(tCol.getName());
         boolean inSrc = sCol != null;

         // if not in source, only include it if we have a default value
         boolean includeColumn = true;
         String defaultValue = tCol.getDefaultValue();
         if (!inSrc && defaultValue == null)
            includeColumn = false;

         if (includeColumn)
         {
            // add the column to the statement
            if (firstTime)
               firstTime = false;
            else
            {
               buf.append(", ");
               valueBuf.append(", ");
            }
            buf.append(tCol.getName());

            // see if we need a parameter for the default value
            if (!inSrc)
            {
               // can't get column from source, add parameter for default value
               valueBuf.append(
                  PSSqlHelper.getParameterMarker(
                     dbmsDef.getDriver(), tCol.getNativeType(), tCol.getSize()
                  )
               );
               values.add(new PSJdbcStatementColumn(defaultValue,
                  tCol.getType()));
            }
            else
            {
               // just add the column name
               valueBuf.append(sCol.getName());
            }
         }

      }

      buf.append(") ");
      buf.append("SELECT ");
      buf.append(valueBuf.toString());

      buf.append(" FROM ");
      buf.append(strFullSource);

      PSJdbcSqlStatement stmt;
      if (values.isEmpty())
         stmt = new PSJdbcSqlStatement(buf.toString());
      else
         stmt = new PSJdbcPreparedSqlStatement(buf.toString(), values);

      return stmt;
   }

   /**
   * Returns a step which queries the table specified by <code>tableSchema</code>
   * in its <code>execute</code> method. Then the result set can be iterated
   * by using the <code>next()</code> method of
   * <code>PSJdbcResultSetIteratorStep</code>.
   *
   * @param dbmsDef Provides the database/schema information for the tables,
   * may not be <code>null</code>.
   * @param tableSchema The table to query, may not be <code>null</code>.
   * @param columns an array of column names, may be <code>null</code>
   * or empty array in which case all the columns are used in the select query.
   * The columns specified must belong to the table specified by the
   * <code>tableSchema</code> parameter.
   * @param filter encapsulates the where clause of the select query,
   * may be <code>null</code> in which case the select query does not have a
   * where clause.
   * @param rowAction the action to be set for the rows cataloged, should be
   * one of the following values:
   * <code>PSJdbcRowData.ACTION_INSERT</code> or
   * <code>PSJdbcRowData.ACTION_UPDATE</code> or
   * <code>PSJdbcRowData.ACTION_REPLACE</code> or
   * <code>PSJdbcRowData.ACTION_DELETE</code> or
   * <code>PSJdbcRowData.ACTION_INSERT_IF_NOT_EXIST</code>
   *
   * @return the step which queries the table specified by <code>tableSchema</code>
   * in its <code>execute</code> method, never <code>null</code>
   *
   * @throws PSJdbcTableFactoryException if any error occurs.
   * @throws IllegalArgumentException if <code>dbmsDef</code> or
   * <code>tableSchema</code> is <code>null</code>
   */
   public static PSJdbcResultSetIteratorStep getResultSetIteratorStatement(
      PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema tableSchema,
      String[] columns, PSJdbcSelectFilter filter, int rowAction)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      // build sql statement
      StringBuffer buf = new StringBuffer();
      String fullName = PSSqlHelper.qualifyTableName(tableSchema.getName(),
         dbmsDef.getDataBase(), dbmsDef.getSchema(), dbmsDef.getDriver());

      buf.append("SELECT ");

      // add columns
      List colList = new ArrayList();
      if (columns == null || columns.length == 0)
      {
         // if no columns specified, fetch all the columns
         Iterator colSchemas = tableSchema.getColumns();
         while (colSchemas.hasNext())
         {
            PSJdbcColumnDef colDef = (PSJdbcColumnDef)colSchemas.next();
            colList.add(colDef.getName());
         }
      }
      else
      {
         // fetch only the specified columns
         int colSize = columns.length;
         for (int k = 0; k < colSize; k++)
            colList.add(columns[k]);
      }

      Iterator colIt = colList.iterator();
      boolean isFirst = true;
      while (colIt.hasNext())
      {
         String colName = (String)colIt.next();
         if (isFirst)
            isFirst = false;
         else
            buf.append(", ");
         buf.append(colName);
      }

      buf.append(" FROM ");
      buf.append(fullName);

      if (filter != null)
      {
         // add the WHERE clause
         String strFilter = filter.toString();
         if (!(strFilter == null || strFilter.trim().length() == 0))
         {
            buf.append(PSJdbcSelectFilter.WHERE);
            buf.append(strFilter);
         }
      }
      return new PSJdbcResultSetIteratorStep(buf.toString(),
         dbmsDef, tableSchema, rowAction);
   }

   /**
    * Returns an execution step that will delete all rows from the table.
    *
    * @param dbmsDef Provides the database/schema information for the table.
    * May not be <code>null</code>.
    * @param tableSchema The table to clear.  May not be <code>null</code>.
    *
    * @return The statement, never <code>null</code>.
    *
    * @throws IllegalArgumentException if dbmsDef or tableSchema is <code>null
    * </code>.
    */
   public static PSJdbcExecutionStep getClearTableStatement(
      PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema tableSchema)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      String fullName = PSSqlHelper.qualifyTableName(tableSchema.getName(),
         dbmsDef.getDataBase(), dbmsDef.getSchema(), dbmsDef.getDriver());

      return new PSJdbcSqlStatement("DELETE FROM " + fullName);
   }

   /**
    * Returns the string to be used in INSERT INTO...SELECT... statement
    * for child tables whose foreign key references the parent tables
    * identity/sequence column.
    *
    * @param dbmsDef Provides the database/schema information for the table,
    * assumed not <code>null</code>.
    * @param tableSchema The table to insert into. Assumed not <code>null</code>
    * @param row The row to insert. Assumed not <code>null</code>.
    * @param valueBuf the string containing the value of columns of the child
    * table, assumed not <code>null</code>
    * @return the string to be used in INSERT INTO...SELECT... statement
    * for child tables whose foreign key references the parent tables
    * identity/sequence column, never <code>null</code> or empty.
    */
   private static String getInsertSelectString(PSJdbcDbmsDef dbmsDef,
      PSJdbcTableSchema tableSchema, PSJdbcRowData row,
      String valueBuf)
   {
      String selString = "";

      PSJdbcRowData parentRowData = row.getParentRowData();
      String parentTableName = PSSqlHelper.qualifyTableName(
            parentRowData.getTableName(), dbmsDef.getDataBase(), dbmsDef.getSchema(),
         dbmsDef.getDriver());
      String colNames = "";
      String colValues="";
      boolean hasRefCols = false;
      for (PSJdbcForeignKey fkey : tableSchema.getForeignKeys() )
      {
      Iterator cols = fkey.getColumns(parentRowData.getTableName());
      while (cols.hasNext())
      {
         String[] col = (String[])cols.next();
         String childTableColName = col[0];
         String parentTableColName = col[2];


         PSJdbcColumnData parentColData =
            parentRowData.getColumn(parentTableColName);

         if (!(parentColData == null || parentColData.getValue() == null))
            continue;

         colNames += "," + childTableColName;
         colValues += "," + parentTableName + "." + parentTableColName;
         hasRefCols = true;
      }
      }
      if (hasRefCols) {
         PSJdbcTableSchema parentTableSchema =
            PSJdbcTableFactory.getTableSchemaCollection().getTableSchema(parentTableName);

         boolean bUseUpdateKey = false;
         String str = "";
         PSJdbcPrimaryKey pkey = parentTableSchema.getPrimaryKey();
         if (pkey != null)
         {
            Iterator it = pkey.getColumnNames();
            int i = 0;
            while (it.hasNext())
            {
               String pkColName = (String)it.next();
               if (i != 0)
                  str += " AND ";
               else
                  i++;
               str += parentTableName + ".";
               str += pkColName;
               str += " = ";
               PSJdbcColumnData pkColData = parentRowData.getColumn(pkColName);
               if (pkColData == null)
               {
                  bUseUpdateKey = true;
                  break;
               }
               str += pkColData.getValue();
            }
         }
         if (bUseUpdateKey)
         {
            str = "";
            PSJdbcUpdateKey ukey = parentTableSchema.getUpdateKey();
            if (ukey != null)
            {
               Iterator it = ukey.getColumnNames();
               int i = 0;
               while (it.hasNext())
               {
                  String upColName = (String)it.next();
                  if (i != 0)
                     str += " AND ";
                  else
                     i++;
                  str += parentTableName + ".";
                  str += upColName;
                  str += " = ";
                  PSJdbcColumnData upColData = parentRowData.getColumn(upColName);
                  if (!(upColData == null ||
                     upColData.getValue() == null))
                     str += upColData.getValue();
               }
            }
         }

          selString = colNames + " ) SELECT " + valueBuf +
              ", " + colValues +
                 " FROM " + parentTableName +
              " WHERE " + str;
      }
      return selString;
   }

   /**
    * Returns an execution step that will insert the specified row in the table.
    *
    * @param dbmsDef Provides the database/schema information for the table.
    * May not be <code>null</code>.
    * @param tableSchema The table to insert into.  May not be <code>null
    * </code>.
    * @param row The row to insert.  May not be <code>null</code>.
    *
    * @return The statement, never <code>null</code>.
    *
    * @throws IllegalArgumentException if dbmsDef, tableSchema, or row is
    * <code>null</code>.
    */
   public static PSJdbcPreparedSqlStatement getInsertStatement(
      PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema tableSchema, PSJdbcRowData row)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      if (row == null)
         throw new IllegalArgumentException("row may not be null");

      String fullName = PSSqlHelper.qualifyTableName(tableSchema.getName(),
         dbmsDef.getDataBase(), dbmsDef.getSchema(), dbmsDef.getDriver());

      boolean isDBOracle = isDBOracle(dbmsDef);
      
      StringBuffer selLobString = new StringBuffer("SELECT ");
      List lobTypes = new ArrayList();
      List lobValues = new ArrayList();
      List lobValuesEncoding = new ArrayList();

      StringBuffer buf = new StringBuffer();
      StringBuffer valueBuf = new StringBuffer();
      PSCollection values = new PSCollection(PSJdbcStatementColumn.class);

      buf.append("INSERT INTO ");
      buf.append(fullName);
      buf.append(" (");

      boolean firstTime = true;
      Iterator columns = row.getColumns();
      while (columns.hasNext())
      {
         PSJdbcColumnData dataCol = (PSJdbcColumnData)columns.next();
         PSJdbcColumnDef schemaCol = tableSchema.getColumn(dataCol.getName());

         if(schemaCol == null)
         {
            throw new RuntimeException("Undefined Table.Column: " +
               tableSchema.getName() + "." + dataCol.getName());
         }
         int colType = schemaCol.getType();

         //For non-Oracle databases ignore sequence columns on insert
         if(schemaCol.isSequence() && !isDBOracle)
            continue;

         if (firstTime)
            firstTime = false;
         else
         {
            buf.append(", ");
            valueBuf.append(", ");
         }
         // add the column to the statement, building values clause as we go
         buf.append(dataCol.getName());

         if (schemaCol.isSequence())
         {
            //sequence columns handled differently in oracle
            //insert statements should have "SequenceName.NextVal" for the
            //value of the column
            String seq = schemaCol.getSequence();
            if (seq == null || seq.trim().length() == 0)
               throw new RuntimeException(
                  "Sequence name is invalid for table " + fullName);

            valueBuf.append(seq);
            valueBuf.append(".NEXTVAL");
         }
         else if ((colType == Types.CLOB || colType == Types.BLOB)
               && isDBOracle)
         {
            selLobString.append(dataCol.getName());
            selLobString.append(",");
            lobTypes.add(new Integer(colType));
            lobValues.add(dataCol.getValue());
            lobValuesEncoding.add(new Integer(dataCol.getEncoding()));

            if (colType == Types.CLOB)
            {
               // for oracle clob data should be inserted using
               // EMPTY_CLOB function
               valueBuf.append("EMPTY_CLOB()");
            }
            else
            {
               // for oracle blob data should be inserted using
               // EMPTY_BLOB function
               valueBuf.append("EMPTY_BLOB()");
            }
         }
         else
         {
            valueBuf.append("?");
            String value = dataCol.getValue();
            PSJdbcBinaryColumnValue binaryValue=null;
            
            //if it is import option process the value
            if (StringUtils.isNotBlank(value) && dbmsDef.getBinaryStorageLocation() != null && colType == Types.BLOB) {
               try {
                  binaryValue = getBinaryValue(dbmsDef, value);
               }
               catch (Exception e) {

                  log.error(e.getMessage());
                  log.debug(e.getMessage(), e);
                  System.out.println("Failed to get the binary value for hash " + value + " for table " + fullName);
               }
            }
            
            if(binaryValue!=null){
               values.add(new PSJdbcStatementColumn(binaryValue,
                     schemaCol.getType(), dataCol.getEncoding()));
            }else{
               values.add(new PSJdbcStatementColumn(value,
               schemaCol.getType(), dataCol.getEncoding()));
         }
           
      }
      }

      boolean bUseInsertIntoSelectStmt = row.getUseInsertIntoSelectStmt();
      if (bUseInsertIntoSelectStmt)
      {
         buf.append(getInsertSelectString(dbmsDef, tableSchema, row,
            valueBuf.toString()));
      }
      else
      {
         buf.append(") values (");
         buf.append(valueBuf.toString());
         buf.append(")");
      }

      if (isDBOracle && lobValues.size() > 0)
      {
         selLobString.setCharAt(selLobString.length()-1, ' ');
         selLobString.append(" FROM ");
         selLobString.append(fullName);
         selLobString.append(" WHERE ROWID = ? FOR UPDATE");

         return new PSJdbcOracleSqlStatement(
            PSJdbcOracleSqlStatement.ORACLE_INSERT, buf.toString(), values,
            selLobString.toString(), null, lobTypes, lobValues,
            lobValuesEncoding);
      }
      return new PSJdbcPreparedSqlStatement(buf.toString(), values);
   }

   /**
    * Gets the binary value by loading the file using the hash.
    * @param dbmsDef assumed not <code>null</code>
    * @param hash assumed not <code>null</code>
    * @return String binary data.
    * @throws IOException
    */
   private static PSJdbcBinaryColumnValue getBinaryValue(PSJdbcDbmsDef dbmsDef, String hash) throws IOException
   {   
      
      int count = 1;
      File bucket = new File(dbmsDef.getBinaryStorageLocation(), PSJdbcImportExportHelper.BINARY_DATA_INITIAL_BUCKET);
      do {
         File binaryFile = new File(bucket, hash);
         if (binaryFile.exists()) {
            try(FileInputStream is = new FileInputStream(binaryFile)) {
               return new PSJdbcBinaryColumnValue(is, binaryFile.length());
            }
         }

         bucket = new File(dbmsDef.getBinaryStorageLocation(), PSJdbcImportExportHelper.BINARY_DATA_BUCKET + "_" + count++);
      } while (bucket.exists());
      
      System.out.println("Could not locate binary file for " + hash + " , is the binary data in the " + dbmsDef.getBinaryStorageLocation() + " folder?");
      return null;
   }

   
   /**
    * Returns an execution step that will update the specified row in the table.
    * If the table being updated has update keys defined, they will be used,
    * otherwise the primary key will be used (one or the other must have been
    * provided).
    *
    * @param dbmsDef Provides the database/schema information for the table.
    * May not be <code>null</code>.
    * @param tableSchema The table to insert into.  May not be <code>null
    * </code>.
    * @param row The row to insert.  May not be <code>null</code>.
    *
    * @return The statement, never <code>null</code>.
    *
    * @throws IllegalArgumentException if dbmsDef or tableSchema is <code>null
    * </code>, or if no keys have been provided.
    */
   public static PSJdbcExecutionStep getUpdateStatement(
      PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema tableSchema, PSJdbcRowData row)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      if (row == null)
         throw new IllegalArgumentException("row may not be null");

      String fullName = PSSqlHelper.qualifyTableName(tableSchema.getName(),
         dbmsDef.getDataBase(), dbmsDef.getSchema(), dbmsDef.getDriver());

      boolean isDBOracle = isDBOracle(dbmsDef);
      StringBuffer selLobString = new StringBuffer("SELECT ");
      List lobTypes = new ArrayList();
      List lobValues = new ArrayList();
      List lobValuesEncoding = new ArrayList();
      boolean isDummyUpdate = true;

      StringBuffer buf = new StringBuffer();
      PSCollection values = new PSCollection(PSJdbcStatementColumn.class);
      StringBuffer keyBuf = new StringBuffer();
      StringBuffer keyColBuf = new StringBuffer();
      PSCollection keyValues = new PSCollection(PSJdbcStatementColumn.class);

      buf.append("UPDATE ");
      buf.append(fullName);
      buf.append(" SET ");

      boolean firstCol = true;
      boolean firstKey = true;
      List keyCols = tableSchema.getKeyColumns();
      if (keyCols.isEmpty())
         throw new IllegalArgumentException(
            "primary or update keys must be defined for update statements");

      Iterator columns = row.getColumns();
      while (columns.hasNext())
      {
         PSJdbcColumnData dataCol = (PSJdbcColumnData)columns.next();
         String colName = dataCol.getName();
         PSJdbcColumnDef schemaCol = tableSchema.getColumn(colName);
         if(schemaCol == null)
         {
            throw new RuntimeException("Undefined Table.Column: " +
               tableSchema.getName() + "." + colName);
         }
         int colType = schemaCol.getType();
         // sequence columns may not have any data
         if(schemaCol.isSequence())
         {
            if (dataCol == null)
               continue;
            if (dataCol.getValue() == null)
               continue;
         }

         boolean isKey = keyCols.contains(colName);

         // add the column to the statement, building WHERE clause as we go
         if (isKey)
         {
            if (!firstKey)
            {
               keyBuf.append(" AND ");
               keyColBuf.append(",");
               keyColBuf.append(colName);
               keyColBuf.append("=?");
            }
            else
            {
               firstKey = false;
               keyColBuf.append(colName);
               keyColBuf.append("=?");
            }
            keyBuf.append(colName);
            keyBuf.append("=?");
            keyValues.add(new PSJdbcStatementColumn(dataCol.getValue(),
               schemaCol.getType()));
         }
         else
         {
            if ((colType == Types.CLOB || colType == Types.BLOB)
               && isDBOracle)
            {
               selLobString.append(colName);
               selLobString.append(",");
               lobTypes.add(new Integer(colType));
               lobValues.add(dataCol.getValue());
               lobValuesEncoding.add(new Integer(dataCol.getEncoding()));
            }
            else
            {
               if (!firstCol)
                  buf.append(", ");
               else
                  firstCol = false;

               isDummyUpdate = false;
               buf.append(colName);
               buf.append("=?");
               values.add(new PSJdbcStatementColumn(dataCol.getValue(),
                  schemaCol.getType(), dataCol.getEncoding()));
            }
         }
      }

      if (isDummyUpdate)
      {
         // this line fails if key contains more than 1 column
         // UPDATE animesh.CCML_VCON SET VCON=? AND DOC_ID=? WHERE VCON=? AND DOC_ID=?
         //buf.append(keyBuf.toString());
         buf.append(keyColBuf.toString());
         buf.append(" WHERE ");
         buf.append(keyBuf.toString());
         values.addAll(keyValues);
         values.addAll(keyValues);
      }
      else
      {
         buf.append(" WHERE ");
         buf.append(keyBuf.toString());
         values.addAll(keyValues);
      }

      if (isDBOracle && lobValues.size() > 0)
      {
         selLobString.setCharAt(selLobString.length()-1, ' ');
         selLobString.append(" FROM ");
         selLobString.append(fullName);
         selLobString.append(" WHERE ");
         selLobString.append(keyBuf.toString());
         selLobString.append(" FOR UPDATE");

         int stmtType = PSJdbcOracleSqlStatement.ORACLE_UPDATE;
         if (isDummyUpdate) stmtType =
            PSJdbcOracleSqlStatement.ORACLE_DUMMY_UPDATE;
         return new PSJdbcOracleSqlStatement(
            stmtType, buf.toString(), values,
            selLobString.toString(), keyValues, lobTypes, lobValues,
            lobValuesEncoding);
      }
      return new PSJdbcPreparedSqlStatement(buf.toString(), values);
   }

   /**
    * Helper method for figuring if the db is oracle. This code segment is 
    * used in multiple locations
    * @param dbmsDef Provides the database/schema information for the table.
    * May not be <code>null</code>.
    * @return <code>true</code> if the driver is an oracle driver
    */
   private static boolean isDBOracle(PSJdbcDbmsDef dbmsDef)
   {
      String driver = dbmsDef.getDriver();
      if (driver != null && driver.startsWith(PSJdbcUtils.ORACLE_PRIMARY))
         return true;
      return false;
   }

   /**
    * Returns an execution step that will delete the specified row from the
    * table.  If the table being updated has update keys defined, they will be
    * used, otherwise the primary key will be used (one of the two must be
    * provided).
    *
    * @param dbmsDef Provides the database/schema information for the table.
    * May not be <code>null</code>.
    * @param tableSchema The table to delete from.  May not be <code>null
    * </code>.
    * @param row The row to delete.  May not be <code>null</code>.
    *
    * @return The statement, never <code>null</code>.
    *
    * @throws IllegalArgumentException if dbmsDef, tableSchema or row is
    * <code>null</code>, or if no keys have been provided..
    */
   public static PSJdbcExecutionStep getDeleteStatement(
      PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema tableSchema, PSJdbcRowData row)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      if (row == null)
         throw new IllegalArgumentException("row may not be null");

      String fullName = PSSqlHelper.qualifyTableName(tableSchema.getName(),
         dbmsDef.getDataBase(), dbmsDef.getSchema(), dbmsDef.getDriver());

      StringBuffer keyBuf = new StringBuffer();
      PSCollection keyValues = new PSCollection(PSJdbcStatementColumn.class);

      keyBuf.append("DELETE FROM ");
      keyBuf.append(fullName);
      keyBuf.append(" WHERE ");

      boolean firstKey = true;
      List keyCols = tableSchema.getKeyColumns();
      if (keyCols.isEmpty())
         throw new IllegalArgumentException(
            "primary or update keys must be defined for delete statements");

      Iterator columns = row.getColumns();
      while (columns.hasNext())
      {
         PSJdbcColumnData dataCol = (PSJdbcColumnData)columns.next();
         PSJdbcColumnDef schemaCol = tableSchema.getColumn(dataCol.getName());
         boolean isKey = keyCols.contains(dataCol.getName());

         // skip if not a key value
         if (!isKey)
            continue;

         // add the column to the statement, building where clause as we go
         if (!firstKey)
            keyBuf.append(" AND ");
         else
            firstKey = false;
         keyBuf.append(dataCol.getName());
         keyBuf.append("=?");
         keyValues.add(new PSJdbcStatementColumn(dataCol.getValue(),
            schemaCol.getType()));
      }

      return new PSJdbcPreparedSqlStatement(keyBuf.toString(), keyValues);

   }

   /**
   * Returns a step which executes the specified query.
   *
   * @param dbmsDef Provides the database/schema information,
   * may not be <code>null</code>.
   *
   * @param sqlQuery The sql query encapsulated by the returned step, this
   * query will be executed when the returned step is executed,
   * may not be <code>null</code> or empty
   *
   * @return the step which executes the specified query, never <code>null</code>
   *
   * @throws IllegalArgumentException if <code>dbmsDef</code> or
   * <code>sqlQuery</code> is invalid.
   */
   public static PSJdbcResultSetIteratorStep getQueryStatement(
      PSJdbcDbmsDef dbmsDef, String sqlQuery)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (sqlQuery == null || sqlQuery.trim().length() < 0)
         throw new IllegalArgumentException(
            "sqlQuery may not be null or empty");

      return new PSJdbcResultSetIteratorStep(sqlQuery, dbmsDef);
   }

   /**
    * Creates SQL definition for primary key constraint.  The constraint will
    * be named using the primary key name (if assigned) or "pk_"+table name, if
    * the name does not exceed the maximum length for a constraint identifier.
    * If the constraint name is too long, the database will assign a unique
    * identifier.
    *
    * @param dbmsDef Used to qualify the primary key name, may not be
    *  <code>null</code>.
    * @param tableSchema The table possibly containing the primary key.
    * May not be <code>null</code>.
    *
    * @return The primary key constraint definition, or <code>null</code> if the
    * tableSchema does not contain one.
    *
    * @throws IllegalArgumentException if dbmsDef, tableSchema is <code>null
    * </code>.
    */
   private static String getPrimaryKeyConstraint(PSJdbcDbmsDef dbmsDef,
      PSJdbcTableSchema tableSchema)
   {
      StringBuffer buf = null;

      PSJdbcPrimaryKey pKey = tableSchema.getPrimaryKey();

      if (pKey != null)
      {
         buf = new StringBuffer();
         String pkName = getQualifiedPkName(tableSchema.getName(),
            pKey.getName(), dbmsDef);

         // omit constraint name if too long for backend
         if (pkName.length() <=
            PSSqlHelper.getMaxConstraintNameLength(dbmsDef.getDriver()))
         {
            buf.append("CONSTRAINT ");
            buf.append(pkName).append(" ");
         }
         buf.append("PRIMARY KEY (");

         Iterator pCols = pKey.getColumnNames();

         boolean isFirst = true;
         while (pCols.hasNext())
         {
            if (!isFirst)
               buf.append(", ");
            else
               isFirst = false;

            buf.append((String)pCols.next());
         }
         buf.append(")");
      }
      return buf == null ? null : buf.toString();
   }
   public static String getForeignKeyConstraint(PSJdbcDbmsDef dbmsDef,
                                                PSJdbcTableSchema schema)
   {
      return getForeignKeyConstraint(dbmsDef,schema,null);
   }
   /**
    * Creates sql definition for all external tables in a foreign key
    * definition.
    *
    * @param dbmsDef The database server info for the tables.
    * Assumed not <code>null</code>.
    *
    * @param schema
    * @param tableSchema The table possibly containing the foreign key.
    * Assumed not <code>null</code>.
    *
    * @return The foreign key definition, or <code>null</code> if the
    * tableSchema does not contain any.
    */
   public static String getForeignKeyConstraint(PSJdbcDbmsDef dbmsDef,
                                                PSJdbcTableSchema schema, PSJdbcTableSchema newSchema)
   {
      StringBuffer buf = null;

      List<PSJdbcForeignKey> fKeys = schema.getForeignKeys();
      String newTable = newSchema==null? null : newSchema.getName();
      for (PSJdbcForeignKey fKey : fKeys) {
      if (fKey != null)
      {
         buf = new StringBuffer();

         Iterator tables = fKey.getTables();
         int i = 1;

         while (tables.hasNext()) {
            String tableName = (String) tables.next();
            if (newTable != null && !tableName.equals(newTable))
               continue;
            String fkName = StringUtils.isBlank(fKey.getName()) ? "fk_" + tableName + "_" + i : fKey.getName();


            Iterator<String[]> cols = fKey.getColumns(tableName);
            HashSet<String> fkCols = new HashSet<>();
            while (cols.hasNext())
               fkCols.add(cols.next()[2]);


            if (newTable!=null)
            {
               HashSet<String> pkCols = new HashSet<>();
               PSJdbcPrimaryKey pk = newSchema.getPrimaryKey();
               Iterator pkIt = pk.getColumnNames();
               while (pkIt.hasNext())
                  pkCols.add((String)pkIt.next());

               if (!pkCols.equals(fkCols))
               {
                  // removing invalid fk no longer matches table,  must be reset in
                  // dependent table definition update
                  break;
               }
            }
            if (i++ > 1)
               buf.append(", ");
               cols = fKey.getColumns(tableName);
               while (cols.hasNext()) {
                  buf.append(getForeignKeyConstraintInt(dbmsDef, fkName, cols));
         }
      }
         }
      }
      return buf == null || buf.length()==0 ? null : buf.toString();
   }


   /**
    * Creates foreign key constraint sql definition for a single external table.
    *
    * @param dbmsDef The database server info for the tables.
    * Assumed not <code>null</code>.
    * @param fkName The qualified foreign key constraint name, assumed not
    * <code>null</code>.
    * @param cols Iterator over a list of foreign key columns for a single
    * external table.  See {@link PSJdbcForeignKey#getColumns(String)} for more
    * info.
    *
    * @return The foreign key constaint, never <code>null</code>.
    */
   private static String getForeignKeyConstraintInt(PSJdbcDbmsDef dbmsDef,
      String fkName, Iterator cols)
   {
      // first build list of internal and external columns
      StringBuffer intBuf = new StringBuffer();
      StringBuffer extBuf = new StringBuffer();
      String tableName = null;

      boolean firstTime = true;
      while (cols.hasNext())
      {
         String[] fkeyInfo = (String[])cols.next();
         if (firstTime)
         {
            tableName = fkeyInfo[1];
            firstTime = false;
         }
         else
         {
            intBuf.append(", ");
            extBuf.append(", ");
         }

         intBuf.append(fkeyInfo[0]);
         extBuf.append(fkeyInfo[2]);
      }

      String driver = dbmsDef.getDriver();
      StringBuffer buf = new StringBuffer();
      // omit constraint name if too long for backend
      if (fkName.length() <=
         PSSqlHelper.getMaxConstraintNameLength(driver))
      {
         buf.append("CONSTRAINT ");
         buf.append(fkName).append(" ");
      }
      buf.append("FOREIGN KEY (");
      buf.append(intBuf.toString());
      buf.append(") REFERENCES ");
      
      String qualifiedName = PSSqlHelper.qualifyTableName(tableName,
            dbmsDef.getDataBase(), dbmsDef.getSchema(), driver);
      
      buf.append(qualifiedName);
      buf.append(" (");
      buf.append(extBuf.toString());
      buf.append(")");

      return buf.toString();
   }

   /**
    * Creates unique constraint sql definitions for all unique indexes defined
    * in a table
    *
    * @param dbmsDef The database server info for the tables.
    * Assumed not <code>null</code>.
    *
    * @param tableSchema The table possibly containing the index definitions.
    * Assumed not <code>null</code>.
    *
    * @return The index definitions, or <code>null</code> if the
    * tableSchema does not contain any.
    */
   private static String getUniqueConstraints(PSJdbcDbmsDef dbmsDef,
      PSJdbcTableSchema tableSchema)
   {
      StringBuffer buf = null;

      Iterator indexes = tableSchema.getIndexes(PSJdbcIndex.TYPE_UNIQUE);
      if (indexes.hasNext())
         buf = new StringBuffer();

      boolean firstTime = true;
      while (indexes.hasNext())
      {
         PSJdbcIndex index = (PSJdbcIndex)indexes.next();
         if (firstTime)
            firstTime = false;
         else
            buf.append(", ");

         buf.append(getUniqueConstraint(dbmsDef, tableSchema, index));
      }

      return buf == null ? null : buf.toString();
   }

   /**
    * Creates unique constraint sql definition for a set of columns.
    *
    * @param dbmsDef The database server info for the tables.
    * Assumed not <code>null</code>.
    * @param tableSchema The table containing the index definition.
    * Assumed not <code>null</code>.
    * @param index The index to create the constraint from, assumed not
    * <code>null</code>.
    *
    * @return The unique constaint definition, never <code>null</code>.
    */
   private static String getUniqueConstraint(PSJdbcDbmsDef dbmsDef,
      PSJdbcTableSchema tableSchema, PSJdbcIndex index)
   {
      StringBuffer buf = new StringBuffer();
      String constraintName = getQualifiedIndexName(tableSchema.getName(), index.getName(), dbmsDef);

         buf.append("CONSTRAINT ");
         buf.append(constraintName).append(" ");

      buf.append("UNIQUE (");

      boolean isFirst = true;
      Iterator cols = index.getColumnNames();
      while (cols.hasNext())
      {
         if (!isFirst)
            buf.append(", ");
         else
            isFirst = false;

         buf.append((String)cols.next());
      }
      buf.append(")");

      return buf.toString();
   }

   /**
    * Returns a constraint name for this primary key.
    *
    * @param tableName The name of the table, assumed not <code>null</code>
    * or empty.
    * @param pkName The name of the primary key, may be <code>null</code>
    * or empty, in which case a name is created by prepending "pk_" to the
    * tableName.
    * @param dbmsDef Used to qualify the name, assumed not <code>null</code>.
    *
    * @return The name, never <code>null</code> or empty.
    */
   private static String getQualifiedPkName(String tableName, String pkName,
      PSJdbcDbmsDef dbmsDef)
   {
      if (pkName == null || pkName.trim().length() == 0)
         pkName = "pk_" + tableName;

      String qualName = PSSqlHelper.qualifyPrimaryKeyName(pkName,
         dbmsDef.getDataBase(), dbmsDef.getSchema(), dbmsDef.getDriver());

      return qualName;
   }

   /**
    * Returns a constraint name for this foreign key.
    *
    * @param tableName The name of the table, assumed not <code>null</code>
    * or empty.
    * @param fkName The name of the foreign key, may be <code>null</code>
    * or empty, in which case a name is created by prepending "fk_" to the
    * tableName.
    *
    * @return The name, never <code>null</code> or empty.
    */
   private static String getQualifiedFkName(String tableName, String fkName)
   {
      if (fkName == null || fkName.trim().length() == 0)
         fkName = "fk_" + tableName;

      return fkName;
   }

   /**
    * Returns a name for the index. The maximum length of returned index name
    * is 17 since DB2 only allows index name upto 18 characters.
    *
    * If <code>indexName</code> is not
    * <code>null</code> and non-empty, then it is returned else an index name
    * is generated by concatenating :
    * "IX_" + tableName[0, 4] + "_" + 2 digit random number +
    * "_" + 2 digit random number + "_" + 2 digit random number
    *
    * @param tableName the name of the table, assumed not <code>null</code>
    * or empty.
    * @param indexName The name of the index, may be <code>null</code>
    * or empty
    * @param dbmsDef Provides the database/schema information for the table.
    * May not be <code>null</code>.
    * 
    * @return the index name , never <code>null</code> or empty.
    */
   private static String getQualifiedIndexName(String tableName,
      String indexName, PSJdbcDbmsDef dbmsDef)
   {
      StringBuffer buf = new StringBuffer();
      if ( isDBOracle(dbmsDef) )
      {
         buf.append(dbmsDef.getSchema());
         buf.append(".");
      }
      
      if (indexName == null || indexName.trim().length() == 0 ||
            indexName.trim().length() > PSSqlHelper.getMaxConstraintNameLength(
                  dbmsDef.getDriver()))
      {
         SecureRandom rand = new SecureRandom();
         buf.append("IX_");
         int len = tableName.trim().length();
         int endIndex = len > 5 ? 4 : len;
         buf.append(tableName.substring(0, endIndex).toUpperCase());
         buf.append("_");
         buf.append(rand.nextInt(99));
         buf.append("_");
         buf.append(rand.nextInt(99));
         buf.append("_");
         buf.append(rand.nextInt(99));
      }
      else
      {
         buf.append(indexName);
      }
      return buf.toString();
   }

   /**
    * Generates an alter table statement to add the specified component.
    *
    * @param tableName The fully qualified table name.  Assumed not <code>null
    * </code>.
    * @param componentDef The SQL string defining the component. Assumed not
    * <code>null </code>.
    *
    * @return The add column statement, never <code>null</code>.
    */
   private static PSJdbcSqlStatement getAddComponentStatement(String tableName,
      String componentDef)
   {
      StringBuffer buf = new StringBuffer();

      buf.append("ALTER TABLE ");
      buf.append(tableName);

      buf.append(" ADD ");
      buf.append(componentDef);

      return new PSJdbcSqlStatement(buf.toString());
   }

   /**
    * Generates an alter table statement to drop the specified component.
    *
    * @param tableName The fully qualified table name.  Assumed not <code>null
    * </code>.
    * @param columnName The column to drop. Assumed not
    * <code>null </code>.
    *
    * @return The add column statement, never <code>null</code>.
    */
   private static PSJdbcSqlStatement getDropComponentStatement(String tableName,
      String columnName)
   {
      StringBuffer buf = new StringBuffer();

      buf.append("ALTER TABLE ");
      buf.append(tableName);

      buf.append(" DROP COLUMN ");
      buf.append(columnName);

      return new PSJdbcSqlStatement(buf.toString());
   }


   /**
    * Returns an execution block that contains one or more steps for dropping
    * non-unique indexes for the specified table. This block contains one
    * step for each non-unique index to be deleted. If no index is to be
    * deleted, then the execution block does not contain any step.
    *
    * @param dbmsDef provides the database/schema information for the table,
    * may not be <code>null</code>.
    *
    * @param tableSchema schema of the table for which non-unique indexes are
    * to be deleted, may not be <code>null</code>.
    *
    * @return the execution block containing a step for each non-unique
    * index to be deleted, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>dbmsDef</code> or
    * <code>tableSchema</code> is <code>null</code> or if any index which is
    * to be deleted does not have a non-<code>null</code> and non-empty name
    */
   public static PSJdbcExecutionStep getDropIndexStatements(
      PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema tableSchema)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      PSJdbcExecutionBlock block = new PSJdbcExecutionBlock();
      Iterator it = tableSchema.getIndexes(PSJdbcIndex.TYPE_NON_UNIQUE);
      while (it.hasNext())
      {
         PSJdbcIndex index = (PSJdbcIndex)it.next();
         if (index.getAction() == PSJdbcTableComponent.ACTION_DELETE)
         {
            String indexName = index.getName();
            if (indexName == null || indexName.trim().length() == 0)
               throw new IllegalArgumentException("Index must have a valid "
                  + "name for creating DROP INDEX statement");

            String dropSql = PSSqlHelper.getDropIndexStatement(
               dbmsDef.getDriver(), tableSchema.getName(),
               dbmsDef.getDataBase(), dbmsDef.getSchema(), indexName);

            block.addStep(new PSJdbcSqlStatement(dropSql));
         }
      }
      return block;
   }

   /**
    * Returns an execution block that contains one or more steps for creating
    * non-unique indexes for the specified table. This block contains one
    * step for each non-unique index to be created. If no index is to be
    * created, then the execution block does not contain any step.
    *
    * @param dbmsDef provides the database/schema information for the table,
    * may not be <code>null</code>.
    *
    * @param tableSchema schema of the table for which non-unique indexes are
    * to be created, may not be <code>null</code>.
    *
    * @return the execution block containing a step for each non-unique
    * index to be created, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>dbmsDef</code> or
    * <code>tableSchema</code> is <code>null</code>
    */
   public static PSJdbcExecutionStep getCreateIndexStatements(
      PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema tableSchema)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      PSJdbcExecutionBlock block = new PSJdbcExecutionBlock();
      Iterator it = tableSchema.getIndexes(PSJdbcIndex.TYPE_NON_UNIQUE);
      while (it.hasNext())
      {
         PSJdbcIndex index = (PSJdbcIndex)it.next();
         if (index.getAction() == PSJdbcTableComponent.ACTION_CREATE)
         {
            PSJdbcExecutionStep step = getCreateIndexStatement(
               dbmsDef, tableSchema, index);
            block.addStep(step);
         }
      }
      return block;
   }

   /**
    * Returns an execution step for creating a non-unique index for the
    * specified table.
    *
    * @param dbmsDef provides the database/schema information for the table,
    * may not be <code>null</code>.
    *
    * @param tableSchema schema of the table for which non-unique indexes are
    * to be created, may not be <code>null</code>.
    *
    * @param index the non-unique index to be created, may not be
    * <code>null</code>, should be of type
    * <code>PSJdbcIndex.TYPE_NON_UNIQUE</code> and its action should be
    * <code>PSJdbcTableComponent.ACTION_CREATE</code>
    *
    * @return the execution step for creating the specified non-unique
    * index, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>dbmsDef</code> or
    * <code>tableSchema</code> or <code>index</code> is <code>null</code>
    * or <code>index</code> is not of type
    * <code>PSJdbcIndex.TYPE_NON_UNIQUE)</code> or its action is not equal
    * <code>PSJdbcTableComponent.ACTION_CREATE</code>
    */
   public static PSJdbcExecutionStep getCreateIndexStatement(
      PSJdbcDbmsDef dbmsDef, PSJdbcTableSchema tableSchema, PSJdbcIndex index)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (tableSchema == null)
         throw new IllegalArgumentException("tableSchema may not be null");

      if (index == null)
         throw new IllegalArgumentException("index may not be null");

      if (!index.isOfType(PSJdbcIndex.TYPE_NON_UNIQUE))
         throw new IllegalArgumentException("Invalid index type: "
            + index.getType());

      if (index.getAction() != PSJdbcTableComponent.ACTION_CREATE)
         throw new IllegalArgumentException("Invalid index action: "
            + index.getAction());

      String fullTableName = PSSqlHelper.qualifyTableName(tableSchema.getName(),
         dbmsDef.getDataBase(), dbmsDef.getSchema(), dbmsDef.getDriver());

      String constraintName = getQualifiedIndexName(tableSchema.getName(), 
         index.getName(), dbmsDef);

      StringBuffer buf = new StringBuffer();
      buf.append("CREATE INDEX ");
      buf.append(constraintName);
      buf.append(" ON ");
      buf.append(fullTableName);
      buf.append(" (");

      boolean isFirst = true;
      Iterator cols = index.getColumnNames();
      while (cols.hasNext())
      {
         if (!isFirst)
            buf.append(", ");
         else
            isFirst = false;

         buf.append((String)cols.next());
      }
      buf.append(")");
      return new PSJdbcSqlStatement(buf.toString());
   }

}


