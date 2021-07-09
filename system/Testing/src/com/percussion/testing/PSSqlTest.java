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
package com.percussion.testing;

import com.percussion.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * helper class to test JDBC functionality.
 */
public class PSSqlTest
{

   private static final Logger log = LogManager.getLogger(PSSqlTest.class);

   /**
    * Create a new connection. The connection information is hard coded in 
    * this method.
    * 
    * @return the connection created, never <code>null</code>. The caller is
    *    responsible to close it.
    * @throws SQLException for any errors creating a new connection.
    */
   private Connection getConnection() throws SQLException, ClassNotFoundException
   {
      String connStr = PSSqlHelper.getJdbcUrl(m_driver, m_host);
      Properties props = PSSqlHelper.makeConnectProperties(connStr, m_database,
         m_user, m_password);
      
      //use if source code is available
      //DriverManager.registerDriver(new net.sourceforge.jtds.jdbc.Driver());
      
      //use if loading driver from jar, jar must be first in classpath
      Class.forName("net.sourceforge.jtds.jdbc.Driver");
      
      Driver driver = DriverManager.getDriver(connStr);
      System.out.println("Driver version= " + driver.getMajorVersion() + "." +
         driver.getMinorVersion());
      
      Connection conn = DriverManager.getConnection(connStr, props);
      if (m_verbose)
      {
         System.out.println("\nConnection properties:");
         System.out.println("\tautocommit= " + 
            (conn.getAutoCommit() ? "true" : "false"));
         System.out.println("\ttransactionIsolation= " + 
            conn.getTransactionIsolation());
      }
      
      return conn;
   }
   
   /**
    * Get a list with all user table names for the supplied parameters.
    * 
    * @param conn the connection used to make to lookup, assumed not 
    *    <code>null</code>.
    * @param database the database name from which to make the lookup, assumed
    *    not <code>null</code> or empty.
    * @param schema the schema for which to make the lookup, assumed not 
    *    <code>null</code> or empty.
    * @return a list with all user table names as <code>String</code>, never 
    *    <code>null</code>, may be empty.
    * @throws SQLException for any error making  the lookup.
    */
   private List getUserTables(Connection conn, String database, String schema) 
      throws SQLException
   {
      ResultSet tables = null;
      try
      {
         List tableNames = new ArrayList();
         
         DatabaseMetaData metaData = conn.getMetaData();
         tables = metaData.getTables(database, schema, "%", 
            new String[] {"TABLE"});
         if (tables != null)
         {
            while (tables.next())
               tableNames.add(tables.getString(3));
         }
         
         return tableNames;
      }
      finally
      {
         if (tables != null)
            try { tables.close(); } catch (SQLException e) { /* noop */ }
      }
   }
   
   /**
    * Query all user tables, then get all foreign key meta data for each table
    * and measure the time used for the foreign key lookup.
    * 
    * @param conn the database connection to use, assumed not <code>null</code>.
    * @param tables a list of table names for which to perform the meta data
    *    test, assumed not <code>null</code>, may be empty.
    */
   private void metaDataTest(Connection conn, List tables)
   {
      PSStopwatch stopWatch = new PSStopwatch();
      
      ResultSet catalogs = null;
      ResultSet schemas = null;
      ResultSet keys = null;
      try
      {
         DatabaseMetaData metaData = conn.getMetaData();
         System.out.println("\n== metaDataTest ==============================");
         if (m_verbose)
         {
            System.out.println("All Procedures callable= " + 
               (metaData.allProceduresAreCallable() ? "yes" : "no"));
            System.out.println("All Tables selectable= " + 
               (metaData.allTablesAreSelectable() ? "yes" : "no"));
            
            System.out.println("\n");
            System.out.println("Available catalogs:");
            catalogs = metaData.getCatalogs();
            if (catalogs != null)
            {
               while (catalogs.next())
               {
                  System.out.println("\t" + catalogs.getString(1));
               }
            }
            
            System.out.println("\n");
            System.out.println("Available schemas:");
            schemas = metaData.getSchemas();
            if (schemas != null)
            {
               while (schemas.next())
               {
                  System.out.println("\t" + schemas.getString(1));
               }
            }
         }
         
         stopWatch.start();
         int margin = 0;
         Iterator walker = tables.iterator();
         while (walker.hasNext())
         {
            String tableName = (String) walker.next();
            if (m_verbose)
               System.out.println("Executing table: " + tableName);
            else
            {
               if (margin < MARGIN)
               {
                  System.out.print(".");
                  margin++;
               }
               else
               {
                  System.out.println(".");
                  margin = 0;
               }
            }
            
            keys = metaData.getImportedKeys(m_database, m_schema, tableName);
            boolean foundForeignKeys = false;
            if (keys != null)
            {
               while (keys.next())
               {
                  String s3 = keys.getString(3);
                  String s4 = keys.getString(4);
                  String s8 = keys.getString(8);
                  List colDef = new ArrayList();
                  colDef.add(s8);
                  colDef.add(s3);
                  colDef.add(s4);
                  
                  if (m_verbose)
                     System.out.println("\tForeign key= " + colDef.toString());
                  foundForeignKeys = true;
               }
            }
            
            if (m_verbose && !foundForeignKeys)
               System.out.println("\tForeign key= ");
            
            if (m_verbose)
               System.out.println("\t" + stopWatch.toString());
         }
         stopWatch.stop();
         System.out.println("\nTotal time to read the foreign keys from all " +
            "user tables:" + stopWatch.toString());
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      finally
      {
         if (keys != null)
            try { keys.close(); } catch (SQLException e) { /* noop */ }

         if (catalogs != null)
            try { catalogs.close(); } catch (SQLException e) { /* noop */ }

         if (schemas != null)
            try { schemas.close(); } catch (SQLException e) { /* noop */ }
      }
   }
   
   private void queryDataTest(Connection conn, List tables, String database, 
      String schema, String driver)
   {
      PSStopwatch stopWatch = new PSStopwatch();
      
      String queryString = "SELECT * FROM ";
      
      Statement statement = null;
      ResultSet rows = null;
      try
      {
         System.out.println("\n== queryDataTest =============================");
         statement = conn.createStatement();
         
         stopWatch.start();
         int margin = 0;
         int count = 0;
         while (count < m_queryCount)
         {
            Iterator walker = tables.iterator();
            while (walker.hasNext() && count < m_queryCount)
            {
               String tableName = PSSqlHelper.qualifyTableName(
                  (String) walker.next(), database, schema, driver);
               
               String query = queryString + tableName;

               if (m_verbose)
                  System.out.println("Executing query: " + query);
               else
               {
                  if (margin < MARGIN)
                  {
                     System.out.print(".");
                     margin++;
                  }
                  else
                  {
                     System.out.println(".");
                     margin = 0;
                  }
               }

               rows = statement.executeQuery(queryString + tableName);
               if (rows != null)
               {
                  while (rows.next() && count < m_queryCount)
                  {
                     count++;
                  }
               
                  rows.close();
               }
            }
         }

         stopWatch.stop();
         System.out.println("\nTotal time to read " + count + " rows:" + 
            stopWatch.toString());
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      finally
      {
         if (statement != null)
            try { statement.close(); } catch (SQLException e) { /* noop */ }
            
         if (rows != null)
            try { rows.close(); } catch (SQLException e) { /* noop */ }
      }
   }
   
   /**
    * Print usage to the system console.
    */
   private static void printUsage()
   {
      System.out.println("Usage:");
      System.out.println("\tjava com.percussion.testing.PSSqlTest driver " +
         "host database schema user password [queryCount] [verbose]");
      System.out.println("\te.g.: java com.percussion.testing.PSSqlTest " +
         "sybase Tds:winkelried:5001 rx50 dbo sa \"\"");
   }
   
   /**
    * Performs all defined tests.
    * 
    * @param args not used.
    */
   public static void main(String[] args)
   {
      Connection conn = null;
      try
      {
         PSSqlTest test = new PSSqlTest();
         
         if (args == null || args.length < 5)
         {
            printUsage();
            return;
         }
            
         for (int i=0; i<args.length; i++)
         {
            String arg = args[i];
            
            switch (i)
            {
               case 0:
                  m_driver = arg.trim();
                  break;
               case 1:
                  m_host = arg.trim();
                  break;
               case 2:
                  m_database = arg.trim();
                  break;
               case 3:
                  m_schema = arg.trim();
                  break;
               case 4:
                  m_user = arg.trim();
                  break;
               case 5:
                  m_password = arg.trim();
                  break;
               case 6:
                  try
                  {
                     m_queryCount = Integer.parseInt(arg.trim());
                  }
                  catch (NumberFormatException e)
                  {
                     System.out.println("Invalid query count, using default.");
                  }
                  break;
               case 7:
                  m_verbose = arg.trim().equalsIgnoreCase("verbose");
                  break;
            }
         }
         
         conn = test.getConnection();
         List userTables = test.getUserTables(conn, m_database, m_schema);
         
         test.metaDataTest(conn, userTables);
         test.queryDataTest(conn, userTables, m_database, m_schema, m_driver);
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         
         if (e instanceof SQLException)
         {
            SQLException sql = (SQLException) e;
            while (sql != null)
            {
               System.out.println("\t" + sql.getLocalizedMessage());
               sql = sql.getNextException();
            }
         }
      }
      finally
      {
         if (conn != null)
         {
            try
            {
               SQLWarning warning = conn.getWarnings();
               if (warning != null)
                  System.out.println("Warnings:");
               while (warning != null)
               {
                  System.out.println("\t" + warning.getLocalizedMessage());
                  warning = warning.getNextWarning();
               }
            }
            catch (SQLException e)
            {
               log.error(e.getMessage());
               log.debug(e.getMessage(), e);
            }
            
            try { conn.close(); } catch (SQLException e) { /* noop */ }
         }
      }
   }
   
   private static int MARGIN = 80;
   
   private static String m_driver = "";
   private static String m_host = "";
   private static String m_database = "";
   private static String m_schema = "";
   private static String m_user = "";
   private static String m_password = "";
   private static int m_queryCount = 1000;
   private static boolean m_verbose = false;
}
