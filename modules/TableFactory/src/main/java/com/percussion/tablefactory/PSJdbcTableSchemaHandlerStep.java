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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Step for processing the data handlers encapsulated by the specified
 * schema handler <code>schemaHandler</code> passes as parameter to the
 * constructor.
 */
public class  PSJdbcTableSchemaHandlerStep extends PSJdbcExecutionStep
{
   /**
    * Constructs a step for processing a schema handler. The value of
    * <code>srcTableSchema</code> and <code>destTableSchema</code> depend upon
    * the type of schema handler specified by <code>schemaHandler</code>.
    *
    * If <code>schemaHandler</code> is a schema handler of type
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_ON_CREATE</code> then
    * <code>srcTableSchema</code> is the schema of the table being created
    * and <code>destTableSchema</code> is <code>null</code>.
    *
    * If <code>schemaHandler</code> is a schema handler of one of the following
    * types:
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_NO_ALTER_TABLE_STMT</code> or
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_TO_BACKUP</code> or
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_FROM_BACKUP</code>
    * then <code>srcTableSchema</code> is the schema of the source table from
    * which the data is being obtained and <code>destTableSchema</code> is
    * the schema of the destination table into which data is being updated.
    *
    * @param dbmsDef provides the database/schema information for the table,
    * may not be <code>null</code>.
    * @param srcTableSchema schema of the table being created or schema of the
    * source table from which data is being obtained, may not be <code>null</code>
    * @param destTableSchema schema of the destination table into which data
    * is being updated, may be <code>null</code> if the enclosing schema
    * handler is of type <code>PSJdbcTableSchemaHandler.TYPE_INT_ON_CREATE</code>,
    * otherwise it may not be <code>null</code>
    * @param schemaHandler table schema handler, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>dbmsDef</code> or
    * <code>schemaHandler</code> or <code>srcTableSchema</code> is
    * <code>null</code>, or <code>destTableSchema</code> is <code>null</code>
    * <code>schemaHandler</code> is a schema handler is one of the following types:
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_NO_ALTER_TABLE_STMT</code> or
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_TO_BACKUP</code> or
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_FROM_BACKUP</code>
    */
   public  PSJdbcTableSchemaHandlerStep(PSJdbcDbmsDef dbmsDef,
      PSJdbcTableSchema srcTableSchema, PSJdbcTableSchema destTableSchema,
      PSJdbcTableSchemaHandler schemaHandler)
   {
      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");
      if (srcTableSchema == null)
         throw new IllegalArgumentException("srcTableSchema may not be null");
      if (schemaHandler == null)
         throw new IllegalArgumentException("schemaHandler may not be null");
      if ((schemaHandler.getType() == PSJdbcTableSchemaHandler.TYPE_INT_FROM_BACKUP) ||
         (schemaHandler.getType() == PSJdbcTableSchemaHandler.TYPE_INT_TO_BACKUP) ||
         (schemaHandler.getType() == PSJdbcTableSchemaHandler.TYPE_INT_NO_ALTER_TABLE_STMT))
      {
         if (destTableSchema == null)
            throw new IllegalArgumentException("destTableSchema may not be null");
      }

      m_dbmsDef = dbmsDef;
      m_srcTableSchema = srcTableSchema;
      m_destTableSchema = destTableSchema;
      m_schemaHandler = schemaHandler;
   }

   /**
    * @see com.percussion.tablefactory.PSJdbcExecutionStep
    */
   public int execute(Connection conn) throws SQLException
   {
      if (conn == null)
         throw new IllegalArgumentException("connection may not be null");

      String msg = "table schema handler step";
      PSJdbcTableFactory.logDebugMessage(msg);
      int updateCount = 0;
      PreparedStatement stmt = null;

      PSJdbcExecutionStepLog stepLogData = getStepLogData();
      stepLogData.setConnectionString(conn);
      stepLogData.setSqlQuery(msg);

      try
      {
         m_schemaHandler.initHandlers(m_dbmsDef, conn,
               m_srcTableSchema, m_destTableSchema);

         switch (m_schemaHandler.getType())
         {
            case PSJdbcTableSchemaHandler.TYPE_INT_NO_ALTER_TABLE_STMT:
            case PSJdbcTableSchemaHandler.TYPE_INT_TO_BACKUP:
            case PSJdbcTableSchemaHandler.TYPE_INT_FROM_BACKUP:
               updateCount = copyTableData(conn);
               break;

            case PSJdbcTableSchemaHandler.TYPE_INT_ON_CREATE:
               m_schemaHandler.execute(conn, null);
               break;
         }

         stepLogData.setSuccess(true);
         stepLogData.setUpdateCount(updateCount);
         if (updateCount == 0)
         {
            stepLogData.setErrorMessage(
               PSJdbcExecutionStepLog.NO_ROWS_UPDATED_MSG);
         }
         else
         {
            stepLogData.setErrorMessage(PSJdbcExecutionStepLog.SUCCESS_MSG);
         }

         return updateCount;
      }
      catch (SQLException e)
      {
         stepLogData.setSuccess(false);
         stepLogData.setUpdateCount(0);
         String details = " source schema " + m_srcTableSchema 
            + " dest schema" + m_destTableSchema;
         String errMsg = PSJdbcTableFactoryException
            .formatSqlException(details, e);
         stepLogData.setErrorMessage(errMsg);
         throw e;
      }
      catch (Exception e)
      {
         stepLogData.setSuccess(false);
         stepLogData.setUpdateCount(0);
         String errMsg = e.getLocalizedMessage();
         stepLogData.setErrorMessage(errMsg);
         throw new SQLException(errMsg);
      }
      finally
      {
         try
         {
            m_schemaHandler.closeHandlers(conn);
         }
         catch (Exception e)
         {
            // no-op
         }
      }
   }

   /**
    * Copies data from the source table to the destination table. Returns the
    * number of rows copied.
    *
    * @param conn the database connection to use, assumed not <code>null</code>
    *
    * @return the number of rows copied from the source table to the
    * destination table, always non-negative.
    *
    * @throws SQLException if any error occurs updating source or table data
    * @throws IOException if any error occurs reading or writing LOB data
    */
   private int copyTableData(Connection conn)
      throws SQLException, IOException, PSJdbcTableFactoryException
   {
      PSJdbcResultSetIteratorStep step =
         PSJdbcStatementFactory.getResultSetIteratorStatement(
            m_dbmsDef, m_srcTableSchema, null, null,
            PSJdbcRowData.ACTION_INSERT);

      int updateCount = 0;
      try
      {
         step.execute(conn);
         // walk result and for each row, build a row of column data
         List rowList = new ArrayList();
         PSJdbcRowData insertRow = step.next();
         while (insertRow != null)
         {
            insertRow = processRow(insertRow);

            // process the data handlers of the enclosing schema handler
            insertRow = m_schemaHandler.execute(conn, insertRow);

            if (insertRow != null)
            {
               // create the insert statement
               PSJdbcExecutionStep insertStep = PSJdbcStatementFactory.getInsertStatement(
                  m_dbmsDef, m_destTableSchema, insertRow);
               insertStep.execute(conn);
            }
            updateCount++;
            insertRow = step.next();
         }
      }
      finally
      {
         if (step != null)
            step.close();
      }
      return updateCount;
   }

   /**
    * Removes the columns that are in the source table but not in the
    * destination table. Adds columns that have default value
    * defined and which do not exist in the source table but exist in the
    * destination table.
    *
    * @param srcRow row of data from the source table, assumed not
    * <code>null</code>
    *
    * @return modified row data, never <code>null</code>
    */
   private PSJdbcRowData processRow(PSJdbcRowData srcRow)
   {
      Iterator srcColumns = m_srcTableSchema.getColumns();
      while (srcColumns.hasNext())
      {
         // first check to see if it's in the target
         PSJdbcColumnDef sCol = (PSJdbcColumnDef)srcColumns.next();
         PSJdbcColumnDef tCol = m_destTableSchema.getColumn(sCol.getName());
         boolean inTarget = (tCol != null);
         if (!inTarget)
            srcRow.removeColumn(sCol.getName());
      }

      Iterator targetColumns = m_destTableSchema.getColumns();
      while (targetColumns.hasNext())
      {
         // first check to see if it's in the source
         PSJdbcColumnDef tCol = (PSJdbcColumnDef)targetColumns.next();
         PSJdbcColumnDef sCol = m_srcTableSchema.getColumn(tCol.getName());
         boolean inSrc = (sCol != null);
         if (!inSrc)
         {
            // if not in source, only include it if the column  has a default
            // value defined
            String defaultValue = tCol.getDefaultValue();
            if (defaultValue != null)
               srcRow.addColumn(new PSJdbcColumnData(tCol.getName(), defaultValue));
         }
      }
      return srcRow;
   }

   /**
    * provides the database/schema information for the table, initialized in the
    * constructor, never <code>null</code> after initialization
    */
   private PSJdbcDbmsDef m_dbmsDef = null;

   /**
    * schema of the source table from which data needs to be copied, initialized
    * in the constructor, never <code>null</code> after initialization
    */
   private PSJdbcTableSchema m_srcTableSchema = null;

   /**
    * schema of the destination table into which data will be copied, initialized
    * in the constructor, may be <code>null</code>
    */
   private PSJdbcTableSchema m_destTableSchema = null;

   /**
    * schema handler, initialized in the constructor,
    * may not be <code>null</code>
    */
   private PSJdbcTableSchemaHandler m_schemaHandler = null;


}

