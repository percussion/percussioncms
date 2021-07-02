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
package com.percussion.rxverify.modules;

import com.percussion.tablefactory.*;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 *  This class is used to check tables against sys_cmstableDef.xml via Jdbc for table existence, primary key,
 *  foreign key, and index.
 */
public class PSJdbcTableCheck extends PSVerifyDatabaseBase
{
    /**
     * Reads in sys_cmstableDef.xml definition file, iterates through
     * each table schema and compares with corresponding backend.  Logs
     * appropriate warnings.
     *
     * @param rxdir, the rhythmyx directory, may not be <code>null</code> or empty.
     * @param debug, debug flag, true for debugging
     *
     * @throws PSJdbcTableFactoryException if there are problems connecting to the database
     * @throws Exception for all other errors
     */
    public void checkTables( File rxdir, boolean debug )
            throws PSJdbcTableFactoryException, Exception
    {
        if ( rxdir == null && rxdir.length() == 0)
            throw new IllegalArgumentException("rxdir may not be null or empty");

        PSJdbcDbmsDef dbmsDef = getDbmsDef( rxdir );

        Logger l = LogManager.getLogger( getClass() );

        PSJdbcDataTypeMap dataMap = null;

        //Create datatype map
        try
        {
            dataMap = new PSJdbcDataTypeMap( null, dbmsDef.getDriver(), null );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw e;
        }

        //Read in sys_cmstableDef.xml
        File tableDef = new File(rxdir, "rxconfig/Server/sys_cmsTableDef.xml");
        Reader r = new FileReader(tableDef);

        Document def = PSXmlDocumentBuilder.createXmlDocument(
                r, false);

        if ( def == null )
            throw new IllegalArgumentException("def may not be null");

        PSJdbcTableSchemaCollection tableSchemaCollection =
                new PSJdbcTableSchemaCollection( def, dataMap );

        if (tableSchemaCollection == null)
            throw new IllegalArgumentException("tableSchemaCollection may not be null");

        Connection conn = null;

        //Try connecting to backend
        try
        {
            conn = PSJdbcTableFactory.getConnection( dbmsDef );

            l.info("#### Connection Established ####");
            l.info("Server: " + dbmsDef.getServer());
            l.info("Schema/Origin: " + dbmsDef.getSchema());
            l.info("Database: " + dbmsDef.getDataBase());
            l.info("User: " + dbmsDef.getUserId());
            l.info("################################");
        }
        catch (SQLException e)
        {
            Object[] args = {
                    PSJdbcTableFactoryException.formatSqlException(e)};
            throw new PSJdbcTableFactoryException(
                    IPSTableFactoryErrors.SQL_CONNECTION_FAILED, args, e);
        }

        Iterator itrSchemaColl = tableSchemaCollection.iterator();

        boolean hasErrors = false;
        boolean betbl_hasErrors;
        Vector<String> missingTables = new Vector<String>();

        l.info( "Check database tables" );

        //Begin processing tables
        while ( itrSchemaColl.hasNext() ){

            betbl_hasErrors = false;

            PSJdbcTableSchema tblSchema = (PSJdbcTableSchema) itrSchemaColl.next();
            PSJdbcTableSchema betblSchema = PSJdbcTableFactory.catalogTable( conn, dbmsDef, dataMap, tblSchema.getName(), false );

            String tblName = tblSchema.getName();
            String betblName = tblName;

            if ( debug )
                l.info( "Verifying Table " + betblName + "..." );

            if ( betblSchema != null )
            {
                betblName = betblSchema.getName();

                boolean pkErrors = checkPrimaryKeys( tblName, tblSchema, betblSchema, l );
                boolean idxErrors = checkIndexes( tblName, tblSchema, betblSchema, l );
                boolean fkErrors = checkForeignKeys( tblName, tblSchema, betblSchema, l );

                if ( pkErrors || idxErrors || fkErrors )
                    betbl_hasErrors = true;
            }
            else
            {
                //Backend table is missing
                //Log message betblSchema table NAME is missing
                l.error( "Table " + tblName + " is missing" );
                hasErrors = true;
                missingTables.addElement( betblName );
            }

            if ( debug )
                l.info( "Verification of Table " + betblName + " complete" );

            if ( betbl_hasErrors )
            {
                //Errors were found
                hasErrors = true;
            }

        }

        //Display overall results
        if ( !hasErrors )
            l.info( "SUCCESS: All backend tables match sys_cmstableDef.xml for table existence, primary key, index, and foreign key" );
        else
        {
            l.error( "FAILED: See error(s) above." );
            //Attempt to clean up bad tables
            //l.info( "Starting clean-up of bad tables..." );
            //PSJdbcTableFactory.processTables( conn, dbmsDef, tableSchemaCollection, new PrintStream( System.out ), true );
            //l.info( "Clean-up finished" );
        }

        try
        {
            if ( conn != null )
                conn.close();
        }
        catch ( SQLException sqle )
        {
            sqle.printStackTrace();
        }
    }

    /**
     * checkPrimaryKeys performs exhaustive comparison of primary keys
     * of two PSJdbcTableSchema objects and logs diagnostic information
     * @param tblSchema base table, it may be <code>null</code>.
     * @param betblSchema test table, it may be <code>null</code>.
     * @param l logger, assume never <code>null</code>.
     *
     * @returns boolean true if keys are not equal, false otherwise
     *
     */
    private boolean checkPrimaryKeys( String tableName, PSJdbcTableSchema tblSchema, PSJdbcTableSchema betblSchema, Logger l )
    {
        boolean hasErrors = false;

        PSJdbcPrimaryKey pk1 = tblSchema.getPrimaryKey();
        PSJdbcPrimaryKey pk2 = betblSchema.getPrimaryKey();

        if ( pk1 != null && pk2 != null )
        {
            Vector<String> tblpkColumnsVec = new Vector<String>();
            Vector<String> betblpkColumnsVec = new Vector<String>();

            Iterator itrtblpkCols = pk1.getColumnNames();
            Iterator itrbetblpkCols = pk2.getColumnNames();

            //Build vector of pk1 columns
            while ( itrtblpkCols.hasNext() ){
                tblpkColumnsVec.add( (String) itrtblpkCols.next() );
            }

            //Build vector of pk2 columns
            while ( itrbetblpkCols.hasNext() ){
                betblpkColumnsVec.add( (String) itrbetblpkCols.next() );
            }

            //Try to match each table primary key defined for table in sys_cmstableDef.xml
            //with a corresponding primary key in backend table using checkForErrors

            boolean pktableMatch = true;

            pktableMatch = checkForErrors( tableName, tblpkColumnsVec, betblpkColumnsVec, "primary key", l );

            if ( !pktableMatch )
                hasErrors = true;


        }
        else if ( ( pk1 == null && pk2 != null ) ||
                ( pk1 != null && pk2 == null ) )
        {
            //Either the backend table has a primary key and the
            //definition does not or the other way around
            if ( pk1 == null && pk2 != null )
            {
                Iterator itrbetblPK = pk2.getColumnNames();

                while ( itrbetblPK.hasNext() ){
                    l.error( "Table " + tableName + " has unknown primary key " + (String) itrbetblPK.next() );
                }
            }
            else
            {
                Iterator itrtblPK = pk1.getColumnNames();

                while ( itrtblPK.hasNext() ){
                    l.error( "Table " + tableName + " is missing primary key " + (String) itrtblPK.next() );
                }
            }

            hasErrors = true;
        }

        return hasErrors;
    }

    /**
     * checkIndexes performs exhaustive comparison of indexes
     * of two PSJdbcTableSchema objects and logs diagnostic information
     * @param tblSchema base table, it may be <code>null</code>.
     * @param betblSchema test table, it may be <code>null</code>.
     * @param l logger, assume never <code>null</code>.
     *
     * @returns boolean true if indexes are not equal, false otherwise
     *
     */
    private boolean checkIndexes( String tableName, PSJdbcTableSchema tblSchema, PSJdbcTableSchema betblSchema, Logger l )
    {
        boolean hasErrors = false;

        Iterator itr1 = tblSchema.getIndexes( PSJdbcIndex.TYPE_UNIQUE | PSJdbcIndex.TYPE_NON_UNIQUE );
        Iterator itr2 = betblSchema.getIndexes( PSJdbcIndex.TYPE_UNIQUE | PSJdbcIndex.TYPE_NON_UNIQUE );

        Vector<String> tblIdxColNms = new Vector<String>();
        Vector<String> betblIdxColNms = new Vector<String>();

        //Build vector of itr1 column names
        while ( itr1.hasNext() ){

            PSJdbcIndex tblIdx = (PSJdbcIndex) itr1.next();
            Iterator itrtblIdxCol = tblIdx.getColumnNames();

            while ( itrtblIdxCol.hasNext() ){
                tblIdxColNms.add( (String) itrtblIdxCol.next() );
            }

        }

        //Build vector of itr2 column names
        while ( itr2.hasNext() ){

            PSJdbcIndex betblIdx = (PSJdbcIndex) itr2.next();
            Iterator itrbetblIdxCol = betblIdx.getColumnNames();

            while ( itrbetblIdxCol.hasNext() ){
                betblIdxColNms.add( (String) itrbetblIdxCol.next() );
            }

        }

        //Try to match each table index defined for table in sys_cmstableDef.xml
        //with a corresponding index in backend table using checkForErrors

        boolean idxtableMatch = true;

        idxtableMatch = checkForErrors( tableName, tblIdxColNms, betblIdxColNms, "index", l );

        if ( !idxtableMatch )
            hasErrors = true;

        return hasErrors;
    }

    /**
     * checkForeignKeys performs exhaustive comparison of foreign keys
     * of two PSJdbcTableSchema objects and logs diagnostic information
     * @param tblSchema base table, it may be <code>null</code>.
     * @param betblSchema test table, it may be <code>null</code>.
     * @param l logger, assume never <code>null</code>.
     *
     * @returns boolean true if keys are not equal, false otherwise
     *
     */
    private boolean checkForeignKeys( String tableName, PSJdbcTableSchema tblSchema, PSJdbcTableSchema betblSchema, Logger l )
    {
        boolean hasErrors = false;

        List<PSJdbcForeignKey> fks1 = tblSchema.getForeignKeys();
        List<PSJdbcForeignKey> fks2 = betblSchema.getForeignKeys();
        if ( fks1 != null && fks2 != null && (fks1.size()>0 || fks2.size()>0))
        {
            Vector<String> tblFKColumns = new Vector<String>();
            Vector<String> betblFKColumns = new Vector<String>();
            String col[];
            //Build vector of itrfk1 columns
            for (PSJdbcForeignKey fk : fks1) {
                Iterator itrfk1 = fk.getColumns();
                col = (String[]) itrfk1.next();
                tblFKColumns.add( col[0] + " " + col[1] + " " + col[2] );
            }
            //Build vector of itrfk2 columns
            for (PSJdbcForeignKey fk : fks2) {
                Iterator itrfk2 = fk.getColumns();
                col = (String[]) itrfk2.next();
                betblFKColumns.add( col[0] + " " + col[1] + " " + col[2] );
            }
            //Try to match each table foreign key defined for table in sys_cmstableDef.xml
            //with a corresponding foreign key in backend table using checkForErrors

            boolean fktableMatch = true;

            fktableMatch = checkForErrors( tableName, tblFKColumns, betblFKColumns, "foreign key", l );

            if ( !fktableMatch )
                hasErrors = true;
        }

        return hasErrors;

    }

    /**
     * dumpSchema writes a summary of the table schemas to the
     * "rxtableschemas.log" file in the rhythmyx root directory.
     *
     * @param itrSchemaColl iterator of the collection of table schemas,
     * assumed not <code>null</code>.
     * @param rxdir file representation of the rhythmyx root directory,
     * assumed not <code>null</code>.
     *
     * @returns boolean true if keys are not equal, false otherwise
     *
     */
    private void dumpSchema( Iterator itrSchemaColl, File rxdir )
    {
        FileWriter fwriter = null;
        try
        {
            File logFile = new File(rxdir, "rxtableschemas.log");
            fwriter = new FileWriter(logFile);

            while ( itrSchemaColl.hasNext() )
            {
                PSJdbcTableSchema tblSchema = (PSJdbcTableSchema) itrSchemaColl.next();
                String tblName = tblSchema.getName();

                fwriter.write("#### Table " + tblName + "####\n");

                PSJdbcPrimaryKey pkey = tblSchema.getPrimaryKey();
                if (pkey != null)
                {
                    fwriter.write("Primary Key(s):\n");
                    Iterator iterPK = pkey.getColumnNames();
                    while (iterPK.hasNext())
                        fwriter.write((String) iterPK.next() + "\n");
                    fwriter.write("\n");
                }

                for (PSJdbcForeignKey fkey : tblSchema.getForeignKeys())
                {
                    fwriter.write("Foreign Key(s):\n");
                    Iterator iterFK = fkey.getColumns();
                    while (iterFK.hasNext())
                    {
                        String[] columns = (String[]) iterFK.next();
                        fwriter.write("Key Name - " + fkey.getName() + ", "
                                + "Column - " + columns[0]  + ", "
                                + "External Table - " + columns[1] + ", "
                                + "External Column - " + columns[2] + "\n");
                    }
                    fwriter.write("\n");
                }

                Iterator itr1 = tblSchema.getIndexes(
                        PSJdbcIndex.TYPE_UNIQUE | PSJdbcIndex.TYPE_NON_UNIQUE );

                if (itr1.hasNext())
                {
                    fwriter.write("Index definitions:\n");
                    while (itr1.hasNext())
                    {
                        PSJdbcIndex indexDef = (PSJdbcIndex) itr1.next();
                        String yes = "yes";
                        String no = "no";
                        String isUnique = "";

                        int type = indexDef.getType();
                        if (type == PSJdbcIndex.TYPE_UNIQUE)
                            isUnique = yes;
                        else
                            isUnique = no;

                        fwriter.write(indexDef.getName() + " isUnique=" + isUnique + " ");
                        Iterator iterColumns = indexDef.getColumnNames();

                        fwriter.write('[');
                        while (iterColumns.hasNext())
                        {
                            fwriter.write((String) iterColumns.next());
                            if (iterColumns.hasNext())
                                fwriter.write(',');
                        }
                        fwriter.write("]\n");
                    }
                }

                fwriter.write("\n");
            }
        }
        catch (IOException io)
        {
            System.err.println("Exception occurred: " + io.getMessage());
        }
        finally
        {
            if (fwriter != null)
                try
                {
                    fwriter.close();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
    }

    /**
     * checkForErrors checks if v1 elements are contained in v2
     * and also if v2 has unrecognized elements
     * logs appropriate error messages
     * @param v1 base vector
     * @param v2 test vector
     * @param type determines if it is primary key, index, or foreign key check
     * @param l is logger
     *
     * @returns boolean true if errors, false otherwise
     */
    private boolean checkForErrors( String tableName, Vector v1, Vector v2, String type, Logger l )
    {
        boolean match;
        boolean tableMatch = true;
        String columnName;

        int j;

        for ( j=0; j < v1.size(); j++ )
        {
            columnName = (String) v1.elementAt(j);
            match = contains( v2, columnName );

            if ( !match )
            {
                //Log message
                l.error( "Table " + tableName + " is missing " + type + " " + columnName );

                if ( type.compareTo( "index" ) == 0 )
                {
                    //String sqlStmt = "ALTER TABLE " + tableName + " ADD INDEX (" + columnName + ")";

                    //Update table
                    //alterTable( conn, sqlStmt, l );
                }

                tableMatch = false;
            }

        }

        for ( j=0; j < v2.size(); j++ )
        {
            columnName = (String) v2.elementAt(j);
            match = contains( v1, columnName );

            if ( !match )
            {
                //Log message
                l.error( "Table " + tableName + " has unknown " + type + " " + columnName );

                if ( type.compareTo( "index" ) == 0 )
                {
                    //String sqlStmt = "ALTER TABLE " + tableName + " DROP INDEX (" + columnName + ");";

                    //Update table
                    //alterTable( conn, sqlStmt, l );
                }

                tableMatch = false;
            }

        }

        return tableMatch;

    }

    /**
     * contains checks if a given vector contains a given string,
     * regardless of case
     *
     * @param v vector
     * @param s string
     *
     * @returns boolean true if the string is contained in the vector,
     * false otherwise
     */
    private boolean contains( Vector v, String s )
    {
        int i;
        String str;
        boolean iscontained = false;

        for ( i = 0; i < v.size(); i++ )
        {
            str = (String) v.elementAt(i);

            if ( str.equalsIgnoreCase(s) )
            {
                iscontained = true;
                break;
            }
        }

        return iscontained;
    }

    /**
     * alterTable updates table accordingly
     * and logs diagnostic information
     *
     * @param conn Connection with database
     * @param tableName name of table to update
     * @param sqlStmt SQL statement to execute
     * @param l logger
     *
     */
      /*private void alterTable( Connection conn, String sqlStmt, Logger l )
      {
         l.info( "Executing SQL statemet " + sqlStmt );

         Statement stmt = null;
         ResultSet rsCount = null;

         try
         {
            stmt = PSSQLStatement.getStatement(conn);
            rsCount = stmt.executeQuery(sqlStmt);
         }
         catch (SQLException e)
         {
            e.printStackTrace();
         }
         finally
         {
            if (rsCount != null)
               try {rsCount.close();} catch (SQLException e){}
               if (stmt != null)
                  try {stmt.close();} catch (SQLException e){}
         }

      }*/

}
