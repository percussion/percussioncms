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
package com.percussion.tablefactory.install;

import com.percussion.install.RxInstallerProperties;
import com.percussion.log.PSLogDatabase;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.util.PSSQLStatement;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.PSJdbcUtils;

import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This class provides all functionality to create/maintain the logging tables
 * used in rhythmyx server.
 */
public class RxLogTables
{
   /**
    * Create all tables for the rhythmyx logging system. If the logging tables
    * already exist we skip that steps.
    *
    * @param driver the db driver type, not <code>null</code> or empty
    * @param server the database server, not <code>null</code> or empty
    * @param db the database to be used, may be <code>null</code> or empty
    * @param uid the user Id, may be <code>null</code> or empty
    * @param pw the user password, may be <code>null</code> or empty
    * @param schema the schema/owner/origin, may be <code>null</code> or empty
    * @throws IllegalArgumentException for any illegal argument
    * @throws SQLException if any database related error occurred
    */
   public static void createLogTables(String driver, String server,
                                      String db, String uid,
                                      String pw,  String schema) throws SQLException
   {
      if (driver == null || driver.length() == 0)
         throw new IllegalArgumentException("we need a valid driver type");

      if (server == null || server.trim().length() == 0)
         throw new IllegalArgumentException("we need a valid driver type");

      Connection conn = createConnection(driver, server, db, uid, pw);
      if (conn == null)
         throw new SQLException("Could not connect to server: " + server);

      DatabaseMetaData targetMeta = conn.getMetaData();


      String intTypeName = null;
      String tinyintTypeName = null;
      String varcharTypeName = null;
      String typeName = null;
      short jdbcType = 0;
      ResultSet rs = targetMeta.getTypeInfo();
      while (rs.next())
      {
         typeName = rs.getString(1);
         jdbcType = rs.getShort(2);

         if ((tinyintTypeName == null) && (jdbcType == Types.TINYINT))
         {
            if (!rs.getBoolean(12)) // only use non-autoincrement cols
               tinyintTypeName = typeName;
         }
         else if ((intTypeName == null) && (jdbcType == Types.INTEGER))
         {
            if (!rs.getBoolean(12)) // only use non-autoincrement cols
               intTypeName = typeName;
         }
         else if ((varcharTypeName == null) && (jdbcType == Types.VARCHAR))
            varcharTypeName = typeName;
      }
      rs.close();

      //oracle having a problem getting types, if it failed default to oracle types
      //todo: better error handling
      if(intTypeName == null)
          intTypeName = "INT";
      if(varcharTypeName == null)
          varcharTypeName = "VARCHAR2";

      if (intTypeName == null)
         throw new SQLException("INT type is not supported in target");
      else if (varcharTypeName == null)
         throw new SQLException("VARCHAR type is not supported in target");

      if (tinyintTypeName == null)
         tinyintTypeName = intTypeName;   // promote the type, don't error

      Statement stmt = PSSQLStatement.getStatement(conn);
      if (!(hasTable(targetMeta, schema, PSLogDatabase.TABLE_PSLOG)))
      {
         StringBuffer buf = new StringBuffer(512);
         buf.append("CREATE TABLE ");
         buf.append(PSSqlHelper.qualifyTableName(PSLogDatabase.TABLE_PSLOG,
                                                 db,
                                                 schema, driver));
         buf.append(" (");
         buf.append(" " + PSLogDatabase.COL_LOG_ID_HIGH + " ");
         buf.append(intTypeName);
         buf.append(" NOT NULL ");
         buf.append(", " + PSLogDatabase.COL_LOG_ID_LOW + " ");
         buf.append(intTypeName);
         buf.append(" NOT NULL ");
         buf.append(", " + PSLogDatabase.COL_LOG_TYPE + " ");
         buf.append(tinyintTypeName);
         buf.append(" NOT NULL ");
         buf.append(", " + PSLogDatabase.COL_LOG_APPL + " ");
         buf.append(intTypeName);
         buf.append(" NOT NULL ");
         
         buf.append(", constraint ");
         buf.append(PSSqlHelper.qualifyPrimaryKeyName(PSLogDatabase.COL_PSLOG_PKEY,
                                                      db,
                                                      schema, driver));
         buf.append(" primary key ");

         buf.append("(" + PSLogDatabase.COL_LOG_ID_HIGH + ", " +
            PSLogDatabase.COL_LOG_ID_LOW + "))");
         stmt.execute(buf.toString());
      }

      if (!(hasTable(targetMeta, schema, PSLogDatabase.TABLE_PSLOGDAT)))
      {
         StringBuffer buf = new StringBuffer(512);
         buf.append("CREATE TABLE ");
         buf.append(PSSqlHelper.qualifyTableName(PSLogDatabase.TABLE_PSLOGDAT,
                                                 db,
                                                 schema, driver));
         buf.append(" (");
         buf.append(" " + PSLogDatabase.COL_LOG_ID_HIGH + " ");
         buf.append(intTypeName);
         buf.append(" NOT NULL ");
         buf.append(", " + PSLogDatabase.COL_LOG_ID_LOW + " ");
         buf.append(intTypeName);
         buf.append(" NOT NULL ");
         buf.append(", " + PSLogDatabase.COL_LOG_SEQ + " ");
         buf.append(intTypeName);
         buf.append(" NOT NULL ");
         buf.append(", " + PSLogDatabase.COL_LOG_SUBT + " ");
         buf.append(intTypeName);
         buf.append(" NOT NULL ");
         buf.append(", " + PSLogDatabase.COL_LOG_SUBSEQ + " ");
         buf.append(intTypeName);
         buf.append(" NOT NULL ");
         buf.append(", " + PSLogDatabase.COL_LOG_DATA + " ");
         buf.append(varcharTypeName);
         buf.append("(255)");

         buf.append(", constraint ");
         buf.append(PSSqlHelper.qualifyPrimaryKeyName(PSLogDatabase.COL_PSLOGDAT_PKEY,
                                                      db,
                                                      schema, driver));
         buf.append(" primary key ");

         buf.append("(" + PSLogDatabase.COL_LOG_ID_HIGH + ", " +
            PSLogDatabase.COL_LOG_ID_LOW + ", " + PSLogDatabase.COL_LOG_SEQ +
            ", " + PSLogDatabase.COL_LOG_SUBSEQ + "))");
         stmt.execute(buf.toString());
      }

      if (conn != null)
         conn.close();
   }

   /**
    * Create a new database connection for the provided driver type. Use the
    * given server configuration to get the driver class. The caller of this
    * function is responsible for closing the connection.
    *
    * @param driver the db driver type
    * @param server the database server
    * @param db the database to be used
    * @param uid the user Id
    * @param pw the user password
    * return the created connection, may be <code>null</code>
    * @throws SQLException if any database related error occurred
    */
   public static Connection createConnection(String driver, String server,
      String db, String uid, String pw) throws SQLException
   {
      String className = null;
      
      String strJTdsSqlServerDesc =
         RxInstallerProperties.getResources().getString("jtdssqlserverdesc");

      if(driver.equals(PSJdbcUtils.ORACLE))
          className = RxInstallerProperties.getResources().getString("oracle");
      else if(driver.equals(strJTdsSqlServerDesc))
         className = RxInstallerProperties.getResources().getString("jtds");

      else
          className = RxInstallerProperties.getResources().getString(driver);

      if (className == null)
         throw new SQLException("Cannot load driver of " + driver);
      try
      {
         Class.forName(className);
      }
      catch (ClassNotFoundException cls)
      {
         System.out.println("Could not find the driver class : " + className);
         System.out.println("Exception : " + cls.getMessage());
         throw new SQLException("JDBC driver class not found. " + cls.toString());
      }
      catch (LinkageError link)
      {
         System.out.println("linkage error");
         System.out.println("Exception : " + link.getMessage());
         throw new SQLException("JDBC driver could not be loaded. " + link.toString());
      }
      catch (Exception e)
      {
         System.out.println("Exception : " + e.getMessage());
         throw new SQLException("Exception. " + e.toString());
      }

      String dbUrl = PSSqlHelper.getJdbcUrl(driver, server);
      Properties props = PSSqlHelper.makeConnectProperties(dbUrl, 
         db, uid, pw);

      System.out.println("Connecting to : " + dbUrl);

      Connection conn = null;
      try
      {
         conn = DriverManager.getConnection(dbUrl, props);

         if (conn != null)
         {
            if (db != null)
               conn.setCatalog(db);
         }
      }
      catch (SQLException e)
      {
         System.out.println("Connection failed : " + e.getMessage());
         conn = null;
      }
      return conn;
   }

   /**
    * Returns a newly created database connection. The caller of this function
    * is responsible for closing the connection.
    * @param props contains the required database connection parameters,
    * never <code>null</code>
    * @return a newly created database connection, never <code>null</code>
    * @throws SQLException if database connection using the parameters
    * specified in the property file fails.
    * @throws PSJdbcTableFactoryException if database connection using the
    * datasource object fails.
    * @throws IllegalArgumentException if props is <code>null</code>
    */
   public static Connection createConnection(Properties props)
      throws SQLException, PSJdbcTableFactoryException
   {
      Connection conn = null;
      if (props == null)
         throw new IllegalArgumentException("props may not be null");
            
      PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
      conn = PSJdbcTableFactory.getConnection(dbmsDef);
      
      return conn;
   }

   /**
    * Tests if a table with the given name exists in the current database. Three
    * attempts are made: 1st we try the name as is, 2nd we try the name all
    * uppercase and last we try it all lower case.
    *
    * @param md the database meta data to use
    * @param table the table name we are looking for
    * @return <code>true</code> if the table exists, <code>false</code> otherwise
    * @throws IllegalArgumentException for any illegal argument
    */
   private static boolean hasTable(DatabaseMetaData md, String schema,
                                   String table) throws SQLException
   {
      if (md == null)
         throw new IllegalArgumentException("need a valid database meta data object");

      ResultSet rs = null;
      try
      {
         rs = md.getTables(null, schema, table, null);
         if (rs.next())
            return true;

         rs.close();
         rs = md.getTables(null, schema, table.toUpperCase(), null);
         if (rs.next())
            return true;

         rs.close();
         rs = md.getTables(null, schema, table.toLowerCase(), null);
         if (rs.next())
            return true;

         return false;
      }
      finally
      {
         if (rs != null)
            rs.close();
      }
   }


   /**
    * Adds the jar file URL to the internal list of jdbc driver URLs.
    * @param jarUrl the jar file URL to be added to the internal list of
    * jdbc driver URLs, may not be <code>null</code>
    * @throws IllegalArgumentException if jarUrl is <code>null</code>
    */
   public static void addJarFileUrl(URL jarUrl)
   {
      if(jarUrl == null)
         throw new IllegalArgumentException("jarUrl may not be null");
      if (m_jarUrls == null)
         m_jarUrls = new ArrayList();
      m_jarUrls.add(jarUrl);
   }

   /**
    * the list of jar file URLs for jdbc drivers, initially <code>null</code>
    */
   private static List m_jarUrls = null;

   
}
