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
package com.percussion.install;

import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.util.PSProperties;
import com.percussion.util.PSSQLStatement;
import com.percussion.util.PSSqlHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
   This class is the Oracle8+ specific converter that converts all given oracle
   tables that have LONG or LONG RAW columns into appropriate LOB columns.
   All the existing data is first backed up and only then conversion is attempted.
   Backup tables (if the length permits - ORA limit 30 chars) are named using the
   following schema: "PSL2L_" + ORIGINAL_TABLE_NAME +"_BK"; if however the name
   is too long, then a timestamp based synthetic table name is generated instead,
   ie: "PSL2L_" + curTime +"_BK", if such name was genegated for a backup table
   and something goes wrong (ie: SQLException) one would then have to look at
   the log file in order to find an appriate backup table to restore the
   original table data from. Considering that this operation could be thought of
   as relatively risky, a full Oracle database backup shall be strongly advised.
*/
public class PSOraConvertLONG2LOBTool
{

   private static final Logger log = LogManager.getLogger(PSOraConvertLONG2LOBTool.class);

   /** opened database connection */
   private Connection    m_conn;

   /** database definition */
   private PSJdbcDbmsDef m_databaseDef;

   /** debug log helper */
   private static long ms_logTime = System.currentTimeMillis();

   /** debug log stream */
   private static PrintStream m_debugLogStream;

   /**
    * The only constuctor
    * @param conn opened database connection, never <code>null<code>
    * @param databaseDef never <code>null<code>
    * @param debugLogStream a stream for logging, never <code>null<code>
    * @throws SQLException
   */
   public PSOraConvertLONG2LOBTool(Connection conn,
                                   PSJdbcDbmsDef databaseDef,
                                   PrintStream debugLogStream)
      throws SQLException
   {
      if (conn==null || databaseDef==null || debugLogStream == null)
         throw new IllegalArgumentException("conn==null || databaseDef==null ||"
            + "debugLogStream==null");

      if (conn.isClosed())
         throw new IllegalArgumentException("conn.isClosed()");

      m_conn = conn;
      m_databaseDef = databaseDef;
      m_debugLogStream = debugLogStream;
   }

   /**
    * @return <code>true</code> if we are connected to Oracle
   */
   public boolean isOracle()
   {
      String driverName = m_databaseDef.getDriver();

      return driverName.trim().toLowerCase().indexOf("oracle") >= 0;

   }

   /**
    * This method is a filter, which if given a list of table names returns a
    * subset of those table names, which use ORA LONG or LONG RAW column types.
    * @param tableNamesToCheck an array of table names to check and filter for
    * a presense of the ORA LONG or LONG RAW column types.
    * Must not be <code>null</code>
    * @return an array of table names that represent a subset of the
    * tableNamesToCheck array that use ORA LONG or LONG RAW column type.
    * The <code>null</code> could be returned, which indicates that either there
    * are no tables in a given tableNamesToCheck array that use LONG columns or
    * in case if the {@link #isOracle()} method returns false.
    * @throws SQLException
    * @throws PSJdbcTableFactoryException
   */
   public String[] filterTablesToConvert(String[] tableNamesToCheck)
      throws SQLException, PSJdbcTableFactoryException
   {
      if (!isOracle())
         return null;

      if (tableNamesToCheck==null)
         throw new IllegalArgumentException("tableNamesToCheck==null");

      if (tableNamesToCheck.length <=0)
         return null;

      String inClauseTables = " (";
      //build the IN clause
      for (int i = 0; i < tableNamesToCheck.length; i++) {
         inClauseTables = inClauseTables + "'" + tableNamesToCheck[i] + "'";
         if (i + 1 < tableNamesToCheck.length)
            inClauseTables += ",";
      }

      inClauseTables += ") ";

      String filterLongTablesSql =
         "SELECT DISTINCT table_name " +
         "FROM  sys.user_tab_columns " +
         "WHERE table_name IN " + inClauseTables +
         "AND (data_type = 'LONG' OR data_type = 'LONG RAW') " +
         "ORDER BY table_name";

      ResultSet rs = performQuery(filterLongTablesSql);
      Vector vTables = new Vector();

      while(rs.next())
      {
         vTables.add(rs.getString("table_name"));
      }

      if (vTables.size() <=0)
         return null;

      String[] arrTableNames = new String[vTables.size()];
      for (int i = 0; i < arrTableNames.length; i++)
         arrTableNames[i] = (String)vTables.elementAt(i);

      return arrTableNames;
   }

   /**
    * Creates a temporary table with a LONG column, then inserts data into it
    * and makes an attempt to convert it. Any SQL exception from here indicates
    * that (depending on the ORA error) connection most likely desn't have enough
    * priveleges to do the conversion job.
    * @return <code>true</code> on success
    * @throws SQLException
    */
   public boolean testConversion()
      throws SQLException, PSJdbcTableFactoryException
   {
      boolean result = false;

      String owner = m_databaseDef.getSchema();

      /*
      CREATE TABLE "VITALY"."RXLONG" ("CONTENTID" NUMBER(10) NOT NULL, "REVISIONID"
      NUMBER(10) NOT NULL, "BODYCONTENT" LONG, "BODYML" NUMBER(10),
      "ABSTRACT" VARCHAR2(255))

      INSERT INTO "VITALY"."RXLONG"
      ("CONTENTID" ,"REVISIONID" , "BODYCONTENT" , "BODYML" ,"ABSTRACT")
      VALUES (1 , 1 , 'some text to insert' , 5 , ' abstract ')
      */

      //create test table
      String testTableName = "PSL2LTEST" + "_" + System.currentTimeMillis();

      String createTableSql = "CREATE TABLE \"" + owner + "\".\"" +
         testTableName + "\" (\"CONTENTID\" NUMBER(10) NOT NULL, \"REVISIONID\" " +
         "NUMBER(10) NOT NULL, \"BODYCONTENT\" LONG, \"BODYML\" NUMBER(10), " +
         "\"ABSTRACT\" VARCHAR2(255))";

      try
      {
         performUpdate(createTableSql);

         final String someTextToInsert = "some test text to insert into the long column";

         //insert test data
         String insertTestDataSql = "INSERT INTO \"" + owner + "\".\"" +
            testTableName + "\" (\"CONTENTID\" ,\"REVISIONID\" " +
            ", \"BODYCONTENT\" , \"BODYML\" , \"ABSTRACT\") " +
            "VALUES (1 , 1 , '" + someTextToInsert + "' , 5 , ' abstract ') ";

         performUpdate(insertTestDataSql);

         //attemt a conversion
         OraTable t = new OraTable(m_conn, owner, testTableName);
         t.queryTableInfo();
         t.convert();

         //if we come here it must a success
         String selectSql = "SELECT * FROM \"" + owner + "\".\"" + testTableName + "\"";

         ResultSet rs = performQuery(selectSql);

         while(rs.next())
         {
            Clob clobBody = rs.getClob("BODYCONTENT");

            String tmp = clobBody.getSubString((long)1, (int)clobBody.length());

            if (tmp.compareTo(someTextToInsert)==0) {
               result = true;
            }
         }
      }
      catch (SQLException ex)
      {
         log.error(ex.getMessage());
         log.debug(ex.getMessage(), ex);
         String msg = PSJdbcTableFactoryException.formatSqlException(ex);
         throw ex;
      }
      finally
      {
         //delete test table
         String deleteTestTable = "DROP TABLE \"" + owner + "\".\"" + testTableName + "\"";

         performUpdate(deleteTestTable);
      }

      return result;
   }

   /**
     * Performs a LONG to LOB conversion only of those tables that are filtered
     * out by the {@link #filterTablesToConvert(String[])} method.
     * @param tableNames an array of ORA tables names passed in for conversion
     * must never be <code>null</code>.
     * @return <code>true</code> on success
     * @throws SQLException on any ORA error
     * @throws PSJdbcTableFactoryException
   */
   public boolean executeConversion(String[] tableNames)
      throws SQLException, PSJdbcTableFactoryException
   {
      if (tableNames==null)
         throw new IllegalArgumentException("tableNames==null");

      String[] arrTablesToConvert = filterTablesToConvert(tableNames);

      if (arrTablesToConvert == null || arrTablesToConvert.length <=0) {
         logIt("NO LONG tables found in the given list.");
         return true;
      }

      Collection coll = Arrays.asList(arrTablesToConvert);

      return executeConversion2(coll);
   }

   /**
     * Performs a LONG to LOB conversion of all given tables. It is assumed that
     * this method is only given a list of tables that indeed use LOONG columns
     * and so no additional filtering is perfomed to insure that.
     * @param collLongTableNames a collection of ORA tables names that use LONGs
     * never <code>null</code>
     * @return <code>true</code> on success
     * @throws SQLException on any ORA error
     * @throws PSJdbcTableFactoryException
   */
   private boolean executeConversion2(Collection collLongTableNames)
      throws SQLException, PSJdbcTableFactoryException
   {
      if (collLongTableNames==null)
         throw new IllegalArgumentException("collLongTableNames==null");

      String schemaName = m_databaseDef.getSchema();

      //create a table instance for each LONG table and gather all the info we
      //need to convert this table, which includes detailed column info, etc.
      Iterator itTableNames = collLongTableNames.iterator();

      Collection collLongTables = new ArrayList();
      OraTable curTable = null;

      try
      {
         //first gather all the info needed to convert given tables
         logIt("Gathering table information..");
         
         while(itTableNames.hasNext())
         {
            String tableName = (String)itTableNames.next();
            
            curTable = new OraTable(m_conn, schemaName, tableName);

            curTable.queryTableInfo();

            collLongTables.add(curTable);
         }

         logIt("About to convert " + collLongTables.size() + " LONG tables.");

         Iterator itTables = collLongTables.iterator();

         while(itTables.hasNext())
         {
            curTable = (OraTable)itTables.next();

            logIt("About to convert table: " +
                  curTable.getOwner() + "." + curTable.getName());

            m_conn.setAutoCommit(false); //begins transaction

            //
            //execute the conversion
            //
            curTable.convert();

            PSSqlHelper.commit(m_conn);

            logIt("Finished Converting table: " +
               curTable.getOwner() + "." + curTable.getName());
         }

         logIt("Successfully Converted " + collLongTables.size() + " LONG tables.");

         return true;
      }
      catch (SQLException sqlEx)
      {
         //whatever happens we want to roll it back
         if (m_conn!=null && !m_conn.isClosed())
            PSSqlHelper.rollback(m_conn);

         String msg = PSJdbcTableFactoryException.formatSqlException(sqlEx);
         log.error(sqlEx.getMessage());
         log.debug(sqlEx.getMessage(), sqlEx);

         logIt("SQLException tableName: " + curTable.getName() +
               " Executing ROLLBACK TRANSACTION " + " msg=" + msg);

         throw sqlEx;
      }
      catch(Exception ex)
      {
         //whatever happens we want to roll it back
         if (m_conn!=null && !m_conn.isClosed())
            PSSqlHelper.rollback(m_conn);

         log.error(ex.getMessage());
         log.debug(ex.getMessage(), ex);

         logIt("Exception tableName: " + curTable.getName() +
               " Executing ROLLBACK TRANSACTION " + " msg=" + ex.getMessage());

         return false;
      }
 
   }

   /**
    * @return a collection of table names under a given schema that use LONGs
    * @throws SQLException
    */
   public Collection querySchemaLONGTables()
      throws SQLException, PSJdbcTableFactoryException
   {
      String schemaName = m_databaseDef.getSchema();

      ArrayList al = new ArrayList();
      
      /* get list of tables that use LONG or LONG RAW datatype
      input: schema name
      output: table names
      */
      String getLONGTablesSql =
          "SELECT DISTINCT table_name " +
          "FROM sys.user_tab_columns " +
          "WHERE (data_type = 'LONG' OR data_type = 'LONG RAW') " +
          "ORDER BY table_name";

      ResultSet rsLONGTables = performQuery(getLONGTablesSql);

      while (rsLONGTables.next())
      {
         String tablename = rsLONGTables.getString("table_name");

         al.add(tablename);
      }

      return al;
   }

   /** main method is provided for testing as well as an example */
   public static void main(String[] args)
   {
      if (args.length < 2)
      {
         System.out.println("Must supply at least two arguments!");
         System.out.println("Format: <command> <Conn Properties File Path> " +
            "[TableNameToConvert_1] [TableNameToConvert_2] [TableNameToConvert_N]");
         System.out.println("Valid commands: list | test | convert");
         System.out.println("");
         System.out.println("Sample Properties File Content:");
         System.out.println("DB_NAME=ORCL");
         System.out.println("DB_BACKEND=ORA");
         System.out.println("DB_SERVER=@serverHostName:1521:ORCL");
         System.out.println("DB_DRIVER_NAME=oracle:thin");
         System.out.println("DB_DRIVER_CLASS_NAME=oracle.jdbc.OracleDriver");
         System.out.println("DB_SCHEMA=scott");
         System.out.println("UID=scott");
         System.out.println("PWD=1234");
         System.out.println("");
         System.out.println("ie: java PSOraConvertLONG2LOBTool list conn.props");
         System.out.println("ie: java PSOraConvertLONG2LOBTool test conn.props");
         System.out.println("ie: java PSOraConvertLONG2LOBTool convert conn.props NOTIFICATIONS RXARTICLE");
         return;
      }
      
      final String CMD_CONVERT = "convert"; 
      final String CMD_LIST = "list";
      final String CMD_TEST = "test";
      
      String cmd = args[0].trim();
      
      if (!cmd.equalsIgnoreCase(CMD_CONVERT) && !cmd.equalsIgnoreCase(CMD_LIST) && 
         !cmd.equalsIgnoreCase(CMD_TEST))
      {
         System.out.println("Invalid command!");
         System.out.println("Valid commands: list | test | convert");
         return;
      } 
      
      PSProperties props;
      try
      {
         props = new PSProperties(args[1]);
      }
      catch (FileNotFoundException e1)
      {
         log.error(e1.getMessage());
         log.debug(e1.getMessage(), e1);
         return;
      }
      catch (IOException e1)
      {
         log.error(e1.getMessage());
         log.debug(e1.getMessage(), e1);
         return;
      }
      
      Connection conn = null;
      try
      {
         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
         PSOraConvertLONG2LOBTool convertTool =
            new PSOraConvertLONG2LOBTool(dbmsDef.getConnection(), dbmsDef, System.out);
       
         if (!convertTool.isOracle())
         {
            System.out.println("Not connected to ORA! - exiting");
            return;
         }

         if (cmd.equalsIgnoreCase(CMD_LIST))
         {
            //get a list of LONG tables found is user's schema
            Collection collLongTables = convertTool.querySchemaLONGTables();
            
            if (collLongTables.size()<=0)
               System.out.println("No tables in a given schema use LONG column type.");
            else
               System.out.println("List of table names that use LONG column type:");
            
            Iterator iter = collLongTables.iterator();
            while (iter.hasNext())
            {
               String tName = (String) iter.next();
               
               System.out.println(tName);
            }
         }
         else if(cmd.equalsIgnoreCase(CMD_TEST))
         {
            //create test table and execute a test conversion
            System.out.println("Attempting to execute test conversion on a temporary table.");
            convertTool.testConversion();
            System.out.println("Finished Test conversion of a temporary table.");
         }
         else if(cmd.equalsIgnoreCase(CMD_CONVERT))
         {
            int nTables = args.length - 2;
            if (nTables <= 0)
            {
               System.out.println("Must supply at least on table name! - exiting");
               return;
            }
            
            System.out.println("Requested to convert these tables:");
            
            String tableNames[] = new String[nTables];
                        
            for (int i = 0; i < tableNames.length; i++)
            {
               tableNames[i] = args[i + 2];
               System.out.println(tableNames[i]);
            }
            
            //make sure they need to be converted
            String[] arrTableNamesToConvert =
               convertTool.filterTablesToConvert(tableNames);
               
            if (arrTableNamesToConvert==null || arrTableNamesToConvert.length <=0)
            {
               System.out.println("No LONG columns found - no need to convert.");
               return;
            }
            
            System.out.println("About to execute Conversion of the following tables:");
            for (int i = 0; i < arrTableNamesToConvert.length; i++)
            {
               System.out.println(arrTableNamesToConvert[i]);
            }
            
            convertTool.executeConversion(arrTableNamesToConvert);
            
            System.out.println("Conversion completed.");
         }
      }
      catch(SQLException e)
      {
         String msg = PSJdbcTableFactoryException.formatSqlException(e);
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         logIt(msg);
      }
      catch(PSJdbcTableFactoryException jdbcEx)
      {
         log.error(jdbcEx.getMessage());
         log.debug(jdbcEx.getMessage(), jdbcEx);
         logIt(jdbcEx.getMessage());
      }
      catch(Exception ex)
      {
         String msg = ex.getMessage();
         logIt(msg);
         log.error(ex.getMessage());
         log.debug(ex.getMessage(), ex);
      }
   }

   /**
    * This class is a simplified abstraction of the Oracle table
   */
   private class OraTable
   {
      /** ORA table name length limit */
      private static final int TABLE_NAME_LIMIT = 30;

      /** backup table name prefix */
      private static final String BACKUP_NAME_PREFIX = "PSL2L";
      /** backup table name suffix */
      private static final String BACKUP_NAME_SUFFIX = "BK";

      private boolean isTableInfoLoaded = false;

      private Connection m_conn;
      private String m_owner; //schema name
      private String m_name;

      //all column definitions
      private Collection m_collColumns = new ArrayList();

      private OraTable m_backupTable = null;

      OraTable(Connection conn, String owner, String name)
      {
         m_conn = conn;
         m_owner = owner;
         m_name = name;
      }

      String getOwner()
      {
         return m_owner;
      }

      String getName()
      {
         return m_name;
      }

      void queryTableInfo() throws SQLException
      {
         //gather all the column information
         queryColumnInfo();

         isTableInfoLoaded = true;
      }

      private String generateBackupTableName() throws SQLException
      {
         String backupName = "";
         //ORA restricts table name length to 30 chars

         //come up with a BAK name under 30 char and verify
         //that such a name hasn't been already taken
         String owner = m_owner;
         String tableName = m_name;

         boolean nameExists = false;

         do
         {
            backupName = "";

            if (nameExists) {
               //we already failed ORA unique name check once
               backupName = BACKUP_NAME_PREFIX + "_" + System.currentTimeMillis() +
               "_" + BACKUP_NAME_SUFFIX;
            }
            else
            {
               backupName = BACKUP_NAME_PREFIX + "_" + tableName +
                  "_" + BACKUP_NAME_SUFFIX;
            }

            if (backupName.length() > TABLE_NAME_LIMIT) {
               nameExists = true;
               //cycle once more to generate a time stamp based name
               continue;
            }

            nameExists = doesTableExists(owner, backupName);

         } while (nameExists);

         return backupName;
      }

      private boolean doesTableExists(String owner, String name)
      {
         boolean exists = false;

         try
         {
            String sql =
               "select * from sys.user_catalog where table_name = '" + name + "'";

            ResultSet rs = performQuery(sql);

            exists = rs.next();
         }
         catch(SQLException slqEx)
         {
            exists = false;
         }
         catch(Exception ex)
         {
            exists = false;
         }

         return exists;
      }

      private void disableAllTriggers()  throws SQLException
      {
         String owner = getOwner();
         String tableName = getName();

         String sql = "ALTER TABLE \"" +
             owner + "\".\"" + tableName + "\" " +
             "DISABLE ALL TRIGGERS";

         int res = performUpdate(sql);
      }

      private void enableAllTriggers()  throws SQLException
      {
         String owner = getOwner();
         String tableName = getName();

         String sql = "ALTER TABLE \"" +
             owner + "\".\"" + tableName + "\" " +
             "ENABLE ALL TRIGGERS";

         int res = performUpdate(sql);
      }

      private OraColumn getLongColumn()
      {
         String colName = null;
         Iterator it = m_collColumns.iterator();

         while(it.hasNext())
         {
             OraColumn col = (OraColumn)it.next();

             if (col.isLongType()) {
               return col;
             }
         }

         return null;
      }

      private boolean dropLongColumn() throws SQLException
      {
         //drop LONG column
         OraColumn longCol = getLongColumn();
         if (longCol==null) {
            //must have already dropped it
            return true;
         }

         String owner = getOwner();
         String tableName = getName();

         String sql = "ALTER TABLE \"" +
             owner + "\".\"" + tableName + "\" " +
             "DROP " + longCol.formatForDrop();

         int res = performUpdate(sql);

         return true;
      }

      private boolean addLobColumn() throws SQLException
      {
         // add replacement LONG replacing LOB column
         OraColumn longCol = getLongColumn();
         if (longCol==null) {
            //must have already dropped it
            return true;
         }

         String owner = getOwner();
         String tableName = getName();

         String sql = "ALTER TABLE \"" +
             owner + "\".\"" + tableName + "\" " +
             "ADD " + longCol.formatForAdd(true);

         int res = performUpdate(sql);

         return true;
      }

      private void deleteAllData() throws SQLException
      {
         String owner = getOwner();
         String tableName = getName();

         String sql = "DELETE FROM \"" +
             owner + "\".\"" + tableName + "\" ";

         int res = performUpdate(sql);
      }

      private void restoreAllData(OraTable backupTable) throws SQLException
      {
         if (backupTable==null)
            throw new IllegalArgumentException("backupTable==null");

         /*
         INSERT INTO "VITALY"."RXLONG"
         SELECT "CONTENTID", "REVISIONID", "BODYML" ,"ABSTRACT", "BODYCONTENT"
         FROM "VITALY"."RXLONG_BAK"
         */

         StringBuffer buf = new StringBuffer(512);
         buf.append("INSERT INTO \"");
         buf.append(m_owner);
         buf.append("\".\"");
         buf.append(m_name);
         buf.append("\" SELECT ");

         //since we added a replacement LOB column to the original table
         //it is now appears as the last column, so we need to put it last.

         OraColumn lobCol = null;

         //append all columns
         Iterator itcol = m_collColumns.iterator();

         for (;itcol.hasNext();)
         {
            OraColumn col = (OraColumn)itcol.next();

            if (col.isLongType()) {
               lobCol = col;
               //we still remember it as LONG, the actual column type is already LOB
               //we want to add this column last
               continue;
            }

            buf.append(col.formatForInsertFromBackup());

            if (itcol.hasNext() || lobCol!=null)
                buf.append(",");
         }

         //add last LOB column
         buf.append(lobCol.formatForInsertFromBackup());

         buf.append(" FROM \"");
         buf.append(backupTable.getOwner());
         buf.append("\".\"");
         buf.append(backupTable.getName());
         buf.append("\" ");

         String sql = buf.toString();

         int res = performUpdate(sql);
      }

      private OraTable createBackupTable() throws SQLException
      {
         OraTable t = null;

         String backupTableName = generateBackupTableName();

         String createTableSql =
            formatForCreate(m_owner, backupTableName, true);

         int res = performUpdate(createTableSql);

         //remember all about the backup table
         t = new OraTable(m_conn, m_owner, backupTableName);
         t.queryTableInfo();

         return t;
      }

      private boolean backupData(OraTable backupTable) throws SQLException
      {
         if (backupTable==null)
            throw new IllegalArgumentException("backupTable==null");

         logIt("About to backup data from table: " + m_name +
               ", to backupTableName: " + backupTable.getName());

         /*
         INSERT INTO "VITALY"."RXLONG_BAK"
         SELECT "CONTENTID" ,"REVISIONID", TO_LOB("BODYCONTENT") , "BODYML" ,"ABSTRACT"
         FROM "VITALY"."RXLONG"
         */

         StringBuffer buf = new StringBuffer(512);
         buf.append("INSERT INTO \"");
         buf.append(backupTable.getOwner());
         buf.append("\".\"");
         buf.append(backupTable.getName());
         buf.append("\" SELECT ");

         //append all columns
         Iterator itcol = m_collColumns.iterator();

         for (;itcol.hasNext();)
         {
            OraColumn col = (OraColumn)itcol.next();

            buf.append(col.formatForInsertIntoBackup(true));

            if (itcol.hasNext())
                buf.append(",");
         }

         buf.append(" FROM \"");
         buf.append(m_owner);
         buf.append("\".\"");
         buf.append(m_name);
         buf.append("\" ");

         String sql = buf.toString();

         int res = performUpdate(sql);

         return true;
      }

      private int dropTable(OraTable tableToDrop) throws SQLException
      {
         if (tableToDrop==null)
            throw new IllegalArgumentException("tableToDrop==null");

         String owner = tableToDrop.getOwner();
         String tableName = tableToDrop.getName();

         String dropTableSql = "DROP TABLE \"" + owner + "\".\"" + tableName + "\"";

         int res = performUpdate(dropTableSql);

         return res;
      }

      /**
       * performs a number of steps in order to convert this table
       * @return <code>true</code> on success
       * @throws SQLException
      */
      boolean convert() throws SQLException
      {
         if (!isTableInfoLoaded)
            throw new IllegalStateException("isTableInfoLoaded==false");

         if (m_backupTable != null) {
            //must have already conveted
            throw new IllegalStateException("m_backupTable!=null");
         }

         //
         //here we need to complete a number of steps in this exact sequence:
         //

         /*1. first must disable all triggers
          for example: ALTER TABLE "VITALY"."RXLONG" DISABLE ALL TRIGGERS
         */
         disableAllTriggers();

         /*2. create a backup table with LONG column replaced by appropriate LOB
         for example:
          CREATE TABLE "VITALY"."RXLONG_BAK" ("CONTENTID" NUMBER(10) NOT NULL,
          "REVISIONID" NUMBER(10) NOT NULL, "BODYCONTENT" CLOB, "BODYML" NUMBER(10),
          "ABSTRACT" VARCHAR2(255))
         */
         m_backupTable = createBackupTable();

         /*3. copy data from this table into a backup table on the fly converting
              LONG columns to a respective LOB columns, for example:
         INSERT INTO "VITALY"."RXLONG_BAK"
         SELECT "CONTENTID" ,"REVISIONID", TO_LOB("BODYCONTENT") , "BODYML" , "ABSTRACT"
         FROM "VITALY"."RXLONG"
         */
         backupData(m_backupTable);

         /*4. get rid of the LONG column, for example:
              ALTER TABLE "VITALY"."RXLONG" DROP ("BODYCONTENT")
         */
         dropLongColumn();

         /*5. The data is now all backed up, drop all the rows from LONG table
              for example: DELETE FROM "VITALY"."RXLONG"
         */
         deleteAllData();

         /*6. add a new LOB column with the same name as LONG column once was
              for example: ALTER TABLE "VITALY"."RXLONG" ADD ("BODYCONTENT" CLOB)
         */
         addLobColumn();

         /*7. copy data from the backup table into a modified original table
              taking into account that the newly added LOB column was added
              to the table schema and so it now appears as the last column.
         */
         restoreAllData(m_backupTable);

         //8. time to drop a backup table
         dropTable(m_backupTable);

         //9. enable all triggers, which were disabled at the first step
         enableAllTriggers();

         //
         // ALL DONE
         //
         return true;
      }

      /**
       * formats a "Create Like" SQL that would be used to create a backup table
       * @param owner ORA schema name, never <code>null</code>
       * @param new_table_name backup table name, never <code>null</code>
       * @param convertLong2Lob <code>true</code> makes it to substitute LONG
       * type by an approriate LOB type.
       * @return
      */
      String formatForCreate(String owner, String new_table_name,
                             boolean convertLong2Lob)
      {
         if (owner==null || new_table_name==null)
            throw new IllegalArgumentException("owner==null || new_table_name==null");

         StringBuffer buf = new StringBuffer(512);
         buf.append("CREATE TABLE \"");
         buf.append(owner);
         buf.append("\".\"");
         buf.append(new_table_name);
         buf.append("\" (");

         Iterator itcol = m_collColumns.iterator();

         for (;itcol.hasNext();)
         {
            OraColumn col = (OraColumn)itcol.next();

            buf.append(col.formatForCreate(convertLong2Lob));

            if (itcol.hasNext())
                buf.append(",");
         }

         buf.append(" )");

         return buf.toString();
      }

      /**
       * queries all the information about this table's columns, which is needed
       * to later create a backup table and copy the data.
       * @throws SQLException
      */
      private void queryColumnInfo() throws SQLException
      {
         m_collColumns.clear();
         
         logIt("Query tableName: " + m_name);

         String getColumnInfoSql =
            "SELECT c.column_name, c.data_type, c.data_length, c.data_precision, " +
            "c.data_scale, c.nullable, c.data_default, m.comments, c.column_id, " +
            "c.data_type_owner, c.data_type_mod " +
            "FROM sys.user_tab_columns c, sys.user_col_comments m " +
            "WHERE c.table_name = '" + m_name + "' " +
            "AND c.table_name = m.table_name " +
            "AND c.column_name = m.column_name " +
            "ORDER BY column_id";

         ResultSet rs = performQuery(getColumnInfoSql);

         while (rs.next())
         {
            String colName = rs.getString("column_name");
            String colId = rs.getString("column_id");

            OraColumn tc = new OraColumn(m_conn, m_owner, m_name, rs);

            m_collColumns.add(tc);
         }
      }
   }

   /**
    *  This class is a simplified abstraction of the Oracle column
   */
   private class OraColumn
   {
      private Connection m_conn;
      private String m_owner;
      private String m_table_name;

      private String m_column_name;
      private String m_data_type;
      private String m_data_length;
      private String m_data_precision;
      private String m_data_scale;
      private String m_nullable;
      private String m_data_default;
      private String m_comments;
      private String m_column_id;
      private String m_data_type_owner;
      private String m_data_type_mod;

      OraColumn(Connection conn, String owner, String table_name, ResultSet rs)
         throws SQLException
      {
         if (conn==null || table_name == null || rs == null)
            throw new IllegalArgumentException("conn==null||table_name==null " +
                                               " || rs == null");

         m_conn = conn;
         m_owner = owner;
         m_table_name = table_name;

         m_column_name = rs.getString("column_name");
         m_data_type = rs.getString("data_type");
         m_data_length = rs.getString("data_length");

         m_data_precision = rs.getString("data_precision");
         m_data_scale = rs.getString("data_scale");
         m_nullable = rs.getString("nullable");
         m_data_length = rs.getString("data_length");
         m_data_default = rs.getString("data_default");

         m_comments = rs.getString("comments");
         m_column_id = rs.getString("column_id");

         m_data_type_owner = rs.getString("data_type_owner");
         m_data_type_mod = rs.getString("data_type_mod");
      }

      String getOwner() {
         return m_owner;
      }

      String getTableName() {
         return m_table_name;
      }

      String getName() {
         return m_column_name;
      }

      String getDataType() {
         return m_data_type;
      }

      /**
       * @return <code>true</code> if this column type is either LONG or LONG RAW
      */
      boolean isLongType() {
         if (m_data_type==null)
            throw new IllegalStateException("m_data_type==null");

         return m_data_type.trim().toLowerCase().indexOf("long") >=0 ;
      }

      /**
       * formats an update SQL that is used to create a backup table
       * @param convertLongsToLobs - if <code>true</code> replaces LONGs with LOBs
       * @return result update SQL, never <code>null</code>
       */
      public String formatForCreate(boolean convertLongsToLobs)
      {
         String str = "\"" + m_column_name + "\" ";

         String dataType = m_data_type.trim().toLowerCase();

         if (convertLongsToLobs && dataType.compareTo("long")==0)
         {
            str = str + " CLOB ";
         }
         else if (convertLongsToLobs && dataType.compareTo("long raw")==0)
         {
            str = str + " BLOB ";
         }
         else if (dataType.indexOf("char") >=0 ) {
            //use length field
            str = str + m_data_type + "(" + m_data_length + ") ";
         }
         else if ((dataType.indexOf("number") >=0) ||
                  (dataType.indexOf("integer") >=0 ) ||
                  (dataType.indexOf("smallint") >=0) ) {

            //use precision
            str = str + m_data_type + "(" + m_data_precision;

            if (m_data_scale!=null) {
               str = str + "," + m_data_scale;
            }

            str = str + ") ";
         }
         else {
            //output only a name
            str = str + m_data_type + " ";
         }

         //add constraint if any
         if (m_nullable != null &&
             m_nullable.trim().toLowerCase().compareTo("n")==0) {

            str = str + " NOT NULL ";
         }

         return str;
      }

      /**
       * @return SQL that is used to drop this table, never <code>null</code>
      */
      public String formatForDrop()
      {
         //ALTER TABLE "VITALY"."RXLONG" DROP ("BODYCONTENT")

         String str = " (\"" + m_column_name + "\") ";

         return str;
      }

      /**
       * @param convertLongsToLobs
       * @return SQL that is used to add a LOB column, never <code>null</code>
      */
      public String formatForAdd(boolean convertLongsToLobs)
      {
         //ALTER TABLE "VITALY"."RXLONG" ADD ("BODYCONTENT" CLOB)

         String str = "(\"" + m_column_name + "\" ";

         String dataType = m_data_type.trim().toLowerCase();

         if (convertLongsToLobs && dataType.compareTo("long")==0)
         {
            str = str + " CLOB ";
         }
         else if (convertLongsToLobs && dataType.compareTo("long raw")==0)
         {
            str = str + " BLOB ";
         }
         else {
            //we only want to drop LONGs, so we must never come here
            throw new IllegalStateException("must only add replacement for dropped LONGs");
         }

         str = str + ") ";

         return str;
      }

      /**
       * @param convertLongsToLobs if <code>true</code> then wraps a LONG column
       * into the TO_LOB ORA statement that does the conversion on the insert
       * @return
      */
      String formatForInsertIntoBackup(boolean convertLongsToLobs)
      {
         String str = " ";

         String dataType = m_data_type.trim().toLowerCase();

         if (convertLongsToLobs && dataType.indexOf("long")>=0)
         {
            str = str + " TO_LOB(" + m_column_name + ")";
         }
         else {
            str = str + m_column_name + " ";
         }

         return str;
      }

      String formatForInsertFromBackup()
      {
         String str = " " + m_column_name + " ";

         return str;
      }
   }

   /**
    * performs database udate and does logging
    * @param sql SQL to pass to the DBMS, never <code>null</code>
    * @return
    * @throws SQLException
   */
   private int performUpdate(String sql) throws SQLException
   {
      if (m_conn == null || sql == null)
         throw new IllegalArgumentException("conn == null || sql == null");

      Connection conn = m_conn;
      boolean logResults = true;

      logIt(sql);

      Statement stmt = null;
      ResultSet rs = null;

      try
      {

         stmt = PSSQLStatement.getStatement(m_conn);
         int res = stmt.executeUpdate(sql);

         logIt("executeUpdate returned: res=" + res);

         return res;
      }
      catch (SQLException ex)
      {
         throw ex;
      }
   }

   /**
    * performs database query and does logging
    * @param sql SQL to pass to the DBMS, never <code>null</code>
    * @return
    * @throws SQLException
   */
   private ResultSet performQuery(String sql) throws SQLException
   {
      if (sql == null)
         throw new IllegalArgumentException("sql == null");

      if(m_conn == null)
         throw new IllegalStateException("m_conn == null");

      Connection conn = m_conn;
      boolean logResults = true;

      logIt(sql);

      Statement stmt = null;
      ResultSet rs = null;

      try
      {
         if (logResults) {
            stmt = PSSQLStatement
                  .getStatement(m_conn, ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);

         }
         else {
            stmt = PSSQLStatement.getStatement(m_conn);
         }

         rs = stmt.executeQuery(sql);

         if (logResults) {
            logQueryResults(rs);
         }

         return rs;
      }
      catch (SQLException ex)
      {
         throw ex;
      }
   }

   /**
    * logs database query results
    * @param rs result set to log, can be <code>null<code>
    * @throws SQLException
    */
   private static void logQueryResults(ResultSet rs) throws SQLException
   {
      String msg = "Query Returned:\n";

      if (rs==null) {
         msg = msg + "ResultSet == null";
      }
      else
      {
         if (rs.next())
         {
            //get result set meta data
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            String columnNames = "";

            for (int i = 1; i <= colCount; i++) {
               String name = meta.getColumnName(i);
               columnNames = columnNames + name;
               if (i < colCount) {
                  columnNames += ",";
               }
            }

            columnNames += "\n";

            String colValues = "";

            do
            {
               for (int i = 1; i <= colCount; i++) {
                  String typeName = meta.getColumnTypeName(i);

                  int type = meta.getColumnType(i);

                  switch(type)
                  {
                     case Types.VARCHAR:
                     case Types.CHAR:
                     case Types.INTEGER:
                     case Types.NUMERIC:
                     case Types.DATE:
                     case Types.TIME:
                     case Types.TIMESTAMP:
                        String val = rs.getString(meta.getColumnName(i));
                        colValues = colValues + val;
                        break;
                  default:
                     colValues = colValues + typeName;
                  }

                  if (i < colCount) {
                     colValues += ",";
                  }
               }

               colValues += "\n";
            }
            while(rs.next());

            msg = msg + columnNames + colValues;

            rs.beforeFirst();
         }
         else {
            msg = msg + "Empty ResultSet";
         }

         logIt(msg, false);
      }
   }

   /**
    * debug log helper
    * @param msg message to log
    * @param wordWrap <code>true</code> derects it to do a word wrap,
    * <code>false</code> logs it as is.
    */
   private static void logIt(String msg, boolean wordWrap)
   {
      if (msg==null)
         msg = "null";

      if (wordWrap)
         msg = bidirectionalWordWrap(msg, 120, -25, false);

      long curTime =  System.currentTimeMillis();

      long timeSpan = curTime - ms_logTime;

      ms_logTime = curTime;

      String msg2 = "\n<PSOraConvertLONG2LOBTool> time: " +
                    curTime + " timeSpan: " + timeSpan + "\n " + msg;

      if (m_debugLogStream==null)
         m_debugLogStream = System.out;
         
      m_debugLogStream.println(msg2);
         
   }

   /**
    * debug log helper
    * @param msg message to log
   */
   private static void logIt(String msg)
   {
      if (msg==null)
         msg = "null";

      msg = bidirectionalWordWrap(msg, 120, -25, false);

      long curTime =  System.currentTimeMillis();

      long timeSpan = curTime - ms_logTime;

      ms_logTime = curTime;

      String msg2 = "\n<PSOraConvertLONG2LOBTool> time: " +
                    curTime + " timeSpan:" + timeSpan + "\n " + msg;

      if (m_debugLogStream==null)
         m_debugLogStream = System.out;
      
      m_debugLogStream.println(msg2);
   }


   /**
    * Wraps a debug log message using a word wrap algorythm
    * @param message message to word wrap, never <code>null</code>
    * @param wrapColumn column or char index to wrap at
    * @param thresholdColumns a positive or negative threshold in columns, which
    * allows to wrap niceley at the next or prior space in a given threshold range.
    * must never be <code>0</code>.
    * @param isBidirectional <code>true</code> indicates that the wrapping shall
    * be attempted using a positive and negative thresholds with a given wrap
    * threshold derection attempted first following by an alternate direction.
    * <code>false</code> - only goes in a direction given by the thresholdColumns.
    * @return result log message with new lines already inserted
   */
   private static String bidirectionalWordWrap(String message, int wrapColumn,
                                               int thresholdColumns,
                                               boolean isBidirectional)
   {
      if (message==null)
         throw new IllegalArgumentException("message must not be null");
      if (wrapColumn <= 0)
         throw new IllegalArgumentException("wrapColumn must be > 0");
      if (thresholdColumns == 0)
         throw new IllegalArgumentException("thresholdColumns must be != 0");

      if (wrapColumn >= message.length())
         return message; //no need to wrap it

      StringBuffer buf = new StringBuffer(message.length());
      int col = 0;

      for (int i = 0; i < message.length(); i++)
      {
         char c = message.charAt(i);

         if (col >= wrapColumn)
         {
            //wrap it on the next or prior space, if none of the next or prior
            //thresholdColumns char is found to be a space, then wrap it at the wrapColumn
            int  j = i;
            int  absThreshold = thresholdColumns > 0 ? thresholdColumns : thresholdColumns * -1;
            char testBuf[] = new char[absThreshold];
            int  testStep = thresholdColumns > 0 ? 1 : - 1;
            int  testInd = thresholdColumns > 0 ? 0 : absThreshold - 1;
            char testCh;
            int  testCount = 0;

            while (true)
            {
               if (absThreshold <=0 || (thresholdColumns > 0 && j >= message.length()))
                  //reach the limit going from left to right
                  break;
               else if (absThreshold <=0 || (thresholdColumns < 0 && j < 0))
                  //reach the limit going from right to left
                  break;

               testCh = testBuf[testInd] = message.charAt(j);
               testCount++;

               if (testCh == ' ')
               {
                  //found a space, wrap it right after the space
                  if (thresholdColumns > 0) {
                     //going from left to right
                     //append what was accumulated in the test buffer
                     buf.append(testBuf, thresholdColumns > 0 ? 0 : testInd, testCount);
                  }
                  else if (thresholdColumns < 0 && testCount > 1) {
                     //going from left to right
                     //remove what already added to the main buffer
                     buf = buf.delete(buf.length() - testCount + 1, buf.length());
                  }

                  buf.append('\n');
                  i = j;
                  col = 0;
                  break;
               }
               if (thresholdColumns > 0 ? (j + 1 >= message.length()) : (j - 1 <= 0))
               {
                  //reached left of right end of the message - done with wrapping
                  if (thresholdColumns > 0)
                     buf.append(testBuf, thresholdColumns > 0 ? 0 : testInd, testCount);

                  //return whatever we have accumulated in the buffer
                  return buf.toString();
               }
               else if(testCh == '\n' || testCh == '\f' || testCh == '\r')
               {
                  //found a new line char before a single space, there is then
                  //no need to wrap it here, reset the column and keep going
                  if (thresholdColumns > 0)
                     buf.append(testBuf, thresholdColumns > 0 ? 0 : testInd, testCount);
                  else if (thresholdColumns < 0 && testCount > 1) {
                     //remove ones that were already added
                     buf = buf.delete(buf.length() - testCount + 1, buf.length());
                  }

                  i = j;
                  col = 0;
                  break;
               }

               //move indexes
               absThreshold--;
               j = j + testStep;
               testInd = testInd + testStep;
            }

            //see if we already wrapped it on the space or a new line
            if (col != 0) {
               if (isBidirectional && thresholdColumns > 0) {
                  //now try searching from right to left

                  //recurse
                  return bidirectionalWordWrap(message, wrapColumn, thresholdColumns * -1, isBidirectional);

               }
               else {
                  //neither on the right nor on the left we could find a space
                  //or a new line char in a given threshold
                  //- give up and wrap it at the wrapColumn
                  buf.append(c);
                  buf.append('\n');
                  col = 0;
               }
            }
         }
         else if (c == '\n' || c == '\f' || c == '\r')
         {
            buf.append(c);
            col = 0;
         }
         else {
            buf.append(c);
         }

         col++;
      }

      return buf.toString();
   }
}
