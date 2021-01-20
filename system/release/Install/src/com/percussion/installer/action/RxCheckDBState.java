/******************************************************************************
 *
 * [ RxCheckForUnicode.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.InstallUtil;
import com.percussion.install.RxFileManager;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.utils.jdbc.PSJdbcUtils;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * This action checks initial state of database, this will eventually include
 * checking roles are correct. If it does not, an appropriate warning message
 * will be created.
 */
public class RxCheckDBState extends RxIAAction
{
   @Override
   public void execute()
   {
      FileInputStream in = null;
      Connection conn = null;

      try
      {
         String strRootDir = getInstallValue(RxVariables.INSTALL_DIR);
         RxFileManager rxfm = new RxFileManager(strRootDir);
         File propFile = new File(rxfm.getRepositoryFile());
         if (propFile.exists())
         {
            in = new FileInputStream(propFile);
            Properties props = new Properties();
            props.load(in);
            props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");

            PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
            String database = dbmsDef.getDataBase();
            String schema = dbmsDef.getSchema();
            String driver = dbmsDef.getDriver();

            // Currently only need to connect to mssql
            // do not connect to derby, it is not set up yet
            if (driver.equals(PSJdbcUtils.JTDS_DRIVER))
               conn = RxLogTables.createConnection(props);

            if (conn != null)
            {

               if (!checkMSSqlForSnapshotIsolation(conn, driver, database))
               {
                  try
                  {
                     autoSetMSSqlSnapshotIsolation(conn, database);
                  }
                  catch (SQLException e)
                  {
                     setInstallValue(RxVariables.CHECK_DB_ERROR, "true");
                     setInstallValue(RxVariables.CHECK_DB_MESSAGE, "sqlServerNotSnapshotIsolation");
                  }
               }

               if (!m_dbStatusError)
               {
                  RxLogger.logInfo("DB Status OK");
               }
            }
         }
      }
      catch (Throwable e)
      {
         String msg = "RxCheckDBState#execute: The following error " + "occurred: " + e.getMessage();
         setInstallValue(RxVariables.CHECK_DB_ERROR, "true");
         setInstallValue(RxVariables.CHECK_DB_MESSAGE, "checkDBError");
         RxLogger.logError(msg);
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (Exception e)
            {
            }
         }
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
            }
         }
      }
   }

   /**
    * Check if the read_committed_snapshot isolation level is turned on for
    * MSSQL
    * 
    * @param conn
    * @param driver
    * @param database
    * @return true if not MSSQL or read_committed_snapshot is on for the
    *         database
    * @throws SQLException
    */
   public static boolean checkMSSqlForSnapshotIsolation(Connection conn, String driver, String database)
         throws SQLException
   {
      if (driver.equals(PSJdbcUtils.JTDS_DRIVER))
      {
         PreparedStatement stmnt = null;
         ResultSet resultSet = null;
         boolean snapshot = false;
         try
         {
            stmnt = conn
                  .prepareStatement("SELECT snapshot_isolation_state,is_read_committed_snapshot_on FROM sys.databases WHERE name= ?");
            stmnt.setString(1, database);
            resultSet = stmnt.executeQuery();

            if (resultSet != null && resultSet.next())
            {
               if (resultSet.getInt(1) == 1 || resultSet.getInt(1) == 3)
               {
                  RxLogger.logInfo("snapshot_isolation_state is on");
                  snapshot = true;
               }
               else
               {
                  RxLogger.logInfo("snapshot_isolation_state is off");
               }
               if (resultSet.getInt(2) == 1)
               {
                  RxLogger.logInfo("is_read_committed_snapshot_on is on");
               }
               else
               {
                  RxLogger.logInfo("is_read_committed_snapshot_on is off");
                  snapshot &= false;
               }
            }
         }
         finally
         {
            dbCloser(null, resultSet, stmnt);
         }

         return snapshot;

      }
      return true;
   }

   /**
    * 
    * Attempt to set snapshot isolation level for MSSQL throws a SQL Exception
    * if user does not have permission for this action.
    * 
    * @param conn
    * @param database
    * @throws SQLException
    */
   public static void autoSetMSSqlSnapshotIsolation(Connection conn, String database) throws SQLException
   {

      String statement = "ALTER DATABASE " + database + " SET SINGLE_USER WITH ROLLBACK IMMEDIATE";
      InstallUtil.executeStatement(conn, statement);
      statement = "ALTER DATABASE " + database + " SET ALLOW_SNAPSHOT_ISOLATION ON";
      InstallUtil.executeStatement(conn, statement);
      statement = "ALTER DATABASE " + database + " SET READ_COMMITTED_SNAPSHOT ON";
      InstallUtil.executeStatement(conn, statement);
      statement = "ALTER DATABASE " + database + " SET MULTI_USER";
      InstallUtil.executeStatement(conn, statement);

   }

   /**
    * Cleanly close the db resources
    * 
    * @param conn
    * @param resultSet
    * @param stmnt
    */
   private static void dbCloser(Connection conn, ResultSet resultSet, PreparedStatement stmnt)
   {
      if (conn != null)
         try
         {
            conn.close();
         }
         catch (Exception e)
         {
         }
      if (resultSet != null)
         try
         {
            resultSet.close();
         }
         catch (Exception e)
         {
         }
      if (stmnt != null)
         try
         {
            stmnt.close();
         }
         catch (Exception e)
         {
         }
   }

   /**************************************************************************
    * Bean property Accessors and Mutators
    **************************************************************************/

   /**
    * Returns the warning message for non-unicode databases. This will include
    * the current repository server, name, and schema/owner.
    * 
    * @return the non-unicode warning message, never <code>null</code>, may be
    *         empty.
    */
   public String getDbStatusErrorMsg()
   {
      return m_dbStatusErrorMsg;
   }

   /**************************************************************************
    * private function
    **************************************************************************/

   /**************************************************************************
    * Bean properties
    **************************************************************************/

   /**
    * Non-unicode database warning message, never <code>null</code>, may be
    * empty.
    */
   private String m_dbStatusErrorMsg = "";

   /**************************************************************************
    * Static variables
    **************************************************************************/

   /**
    * Flag for unicode support.
    */
   public static boolean m_dbStatusError = false;
}
