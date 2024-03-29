/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.tablefactory.tools;

import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcExecutionStep;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcStatementFactory;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.tablefactory.PSJdbcTableSchema;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;

public class DbUtils {

    /**
     * Creates backup of existing table. First we will attempt to copy the
     * data using copy table statement (INSERT INTO ... SELECT .. FROM ..)
     * If this fails, (for example, if the table has a LONG column on Oracle)
     * then we will catalog data from the source table and insert into the
     * destination table.
     * This method does not close the database connection.
     *
     * @param conn Database connection, may not be <code>null</code>
     * @param dbmsDef Used to connect to the database and provides correct
     * schema/origin. May not be <code>null</code>.
     * @param dataTypeMap Required to create the tableSchema object. May not be
     * <code>null</code>.
     * @param srcTableName Name of the table whose backup is to be created,
     * may not be <code>null</code> or empty.
     * @param destTableName Name of the backup table, may not be <code>null</code>
     * or empty.
     * @param logOut If not <code>null</code>, log messages will be written to
     * this stream.  If <code>null</code>, they will not. This method does not
     * take ownership of the stream and will not attempt to close it when
     * processing is completed.
     * @param logDebug If <code>true</code> and logOut is not <code>null</code>,
     * debugging messages will also be written to the logging output stream.  If
     * <code>false</code>, they will not.  If logOut is <code>null</code>, this
     * parameter has no effect.
     *
     * @return <code>true</code> if the backup of the table was sucessful,
     * <code>false</code> otherwise.
     * @throws IllegalArgumentException if any param is invalid
     */
    public static boolean backupTable(
            Connection conn, PSJdbcDbmsDef dbmsDef,
            PSJdbcDataTypeMap dataTypeMap, String srcTableName, String destTableName,
            PrintStream logOut, boolean logDebug)
            throws SQLException, PSJdbcTableFactoryException
    {
        if (conn == null)
            throw new IllegalArgumentException("conn may not be null");

        if (dbmsDef == null)
            throw new IllegalArgumentException("dbmsDef may not be null");

        if (dataTypeMap == null)
            throw new IllegalArgumentException("dataTypeMap may not be null");

        if ((srcTableName == null) || (srcTableName.trim().length() < 1))
            throw new IllegalArgumentException("srcTableName may not be null or empty");

        if ((destTableName == null) || (destTableName.trim().length() < 1))
            throw new IllegalArgumentException("destTableName may not be null or empty");

        // first catalog the schema only
        PSJdbcTableSchema srcTableSchema =
                PSJdbcTableFactory.catalogTable(conn, dbmsDef, dataTypeMap,
                        srcTableName, false);

        if (srcTableSchema == null)
        {
            // source table does not exist
            return false;
        }

        // create the destination table schema from the source schema
        // the destination table should not have any primary key, foreign key,
        // unique key or or any other index
        PSJdbcTableSchema destTableSchema = new PSJdbcTableSchema(srcTableSchema);
        destTableSchema.setName(destTableName);
        destTableSchema.setPrimaryKey(null);
        destTableSchema.setForeignKeys(null,false);
        destTableSchema.clearIndexes();

        // drop the table
        PSJdbcExecutionStep step =
                PSJdbcStatementFactory.getDropTableStatement(dbmsDef,
                        destTableSchema.getName());
        try
        {
            step.execute(conn);
        }
        catch (SQLException sqle)
        {
            // Drop Table throws a SQLException if the table does not
            // does not exist in the system catalog. We will only catch the
            // SQLException.
        }

        // create the table
        step = PSJdbcStatementFactory.getCreateTableStatement(dbmsDef,
                destTableSchema);
        step.execute(conn);

        // table successfully created, now copy data
        step = PSJdbcStatementFactory.getCopyTableDataStatement(
                dbmsDef, srcTableSchema, destTableSchema);

        boolean copyDataSuccess = true;
        try
        {
            step.execute(conn);
        }
        catch (Throwable t)
        {
            logOut.println(t.getMessage());
            copyDataSuccess = false;
        }

        if (copyDataSuccess)
            return true;

        // catalog data from the source table
        PSJdbcTableData tableData = PSJdbcTableFactory.catalogTableData(
                conn, dbmsDef, srcTableSchema,
                null, null, PSJdbcRowData.ACTION_INSERT);

        // insert the catalogged data into the destination table
        if (tableData != null)
        {
            destTableSchema.setAllowSchemaChanges(false);
            destTableSchema.setTableData(tableData);
            PSJdbcTableFactory.processTable(
                    conn, dbmsDef, destTableSchema, logOut, logDebug);
        }
        return true;
    }



}
