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

package com.percussion.tablefactory.tools;

import com.percussion.tablefactory.*;

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
        destTableSchema.setForeignKeys(null);
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
