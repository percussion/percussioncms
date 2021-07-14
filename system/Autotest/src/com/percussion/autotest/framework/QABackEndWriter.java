/*[ QABackEndWriter.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.autotest.framework;

import com.percussion.autotest.framework.IQAWriter;
import com.percussion.autotest.framework.QAPerformanceStats;
import com.percussion.autotest.framework.QATestPageResults;
import com.percussion.autotest.framework.QATestResults;
import com.percussion.util.PSSqlHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * This class will write the test results to a backend database.  Note: this
 * class does not currently support Oracle as a back end.
 */
public class QABackEndWriter implements IQAWriter
{
   /**
    * Creates a new QA writer which writes the test results to the provided
    * Back End.
    *
    * @param serverProps The QAServer's properties, which must include the
    * following properties.  All properties must be defined, but their value
    * may be empty if they are not required.
    * <table>
    * <th>
    * <td>Property</td><td>Description</td><td>Required?</td>
    * </th>
    * <tr>
    * <td>DB_DRIVER_NAME</td><td>The name of the JDBC driver to use.</td>
    *    <td>Y</td>
    * </tr>
    * <tr>
    * <td>DB_DRIVER_CLASSNAME</td><td>The JDBC driver class to use.</td>
    *    <td>Y</td>
    * </tr>
    * <tr>
    * <td>DB_SERVER</td><td>The name of the database server or DSN.</td>
    *    <td>Y</td>
    * </tr>
    * <tr>
    * <td>DB_NAME</td><td>The name of the database.</td><td>N</td>
    * </tr>
    * <tr>
    * <tr>
    * <td>DB_SCHEMA</td><td>The schema to use.</td><td>N</td>
    * </tr>
    * <td>DB_UID</td><td>The userid to use if required to connect to the server.
    * </td><td>N</td>
    * </tr>
    * <tr>
    * <td>DB_PWD</td><td>The password to use, unencoded.</td><td>N</td>
    * </tr>
    * </table>
    *
    * @throws IllegalArgumentException if serverProps is <code>null</code>,
    * if any properties are missing, or if any required properties have
    * an empty value.
    */
   public QABackEndWriter(Properties serverProps)
   {
      // validate and retrieve properties
      if (serverProps == null)
         throw new IllegalArgumentException("serverProps may not be null");

      m_dbDriverName = serverProps.getProperty(DB_DRIVER_NAME);
      if (m_dbDriverName == null || m_dbDriverName.trim().length() == 0)
         throw new IllegalArgumentException(DB_DRIVER_NAME +
            " must be defined in the serverProperties and may not be empty");

      m_dbDriverClassName = serverProps.getProperty(DB_DRIVER_CLASSNAME);
      if (m_dbDriverClassName == null ||
         m_dbDriverClassName.trim().length() == 0)
      {
         throw new IllegalArgumentException(DB_DRIVER_CLASSNAME +
            " must be defined in the serverProperties and may not be empty");
      }
      
      m_dbServer = serverProps.getProperty(DB_SERVER);
      if (m_dbServer == null || m_dbServer.trim().length() == 0)
         throw new IllegalArgumentException(DB_SERVER +
            " must be defined in the serverProperties and may not be empty");

      m_dbName = serverProps.getProperty(DB_NAME);
      if (m_dbName == null)
         throw new IllegalArgumentException(DB_NAME +
            " must be defined in the serverProperties");

      m_dbSchema = serverProps.getProperty(DB_SCHEMA);
      if (m_dbSchema == null)
         throw new IllegalArgumentException(DB_SCHEMA +
            " must be defined in the serverProperties");

      m_dbUid = serverProps.getProperty(DB_UID);
      if (m_dbUid == null)
         throw new IllegalArgumentException(DB_UID +
            " must be defined in the serverProperties");

      m_dbPwd = serverProps.getProperty(DB_PWD);
      if (m_dbPwd == null)
         throw new IllegalArgumentException(DB_PWD +
            " must be defined in the serverProperties");
   }

   /**
    * Implements IQAWriter to write the QA results to the specified back end.
    *
    * See {@link IQAWriter#write(QATestResults)}
    */
   public synchronized void write(QATestResults results) throws Exception
   {
      // enforce our contract
      if (results == null)
         throw new IllegalArgumentException("results may not be null");

      Connection conn = null;
      try
      {
         // list of test results written from pages
         List pageCases = new ArrayList();

         // connect to the database
         conn = makeConnection(m_dbDriverName, m_dbServer, m_dbName,
            m_dbUid, m_dbPwd);

         // write script info
         int scriptId = writeScriptInfo(conn, results);

         // write page info if we have it
         Iterator pages = results.getPages().iterator();
         while (pages.hasNext())
         {
            QATestPageResults page = (QATestPageResults)pages.next();
            int pageId = writePageInfo(conn, page, scriptId);

            List pageTests = page.getTests();
            pageCases.addAll(pageTests);

            for (int i = 0; i < pageTests.size(); i++)
               writeCaseInfo(conn, (QATestResult)pageTests.get(i), scriptId,
                  pageId);
         }

         // write test case info for cases not part of a page
         Iterator tests = results.getTests().iterator();
         while (tests.hasNext())
         {
            QATestResult test = (QATestResult)tests.next();
            if (!pageCases.contains(test))
               writeCaseInfo(conn, test, scriptId, -1);
         }
      }
      catch (Exception e)
      {
         String msg = null;
         if (e instanceof SQLException)
            msg = getFormattedExceptionText((SQLException)e);
         else
            msg = e.getLocalizedMessage();
         System.out.println("Error writing results to backend: " + msg);
         e.printStackTrace();
         throw e;
      }
      finally
      {
         if(null != conn)
            conn.close();
      }

   }

   /**
    * Writes the script level results info to the database.
    *
    * @param conn The connection to use, assumed not <code>null</code>. Does not
    * take ownership.
    * @param results The results to log, assumed not <code>null</code>.
    *
    * @return The script id assigned when inserting.
    *
    * @throws IOException if there are any errors sending the log to the
    * database.
    * @throws SQLException if there are any other errors.
    */
   private int writeScriptInfo(Connection conn, QATestResults results)
      throws SQLException, IOException
   {
      int scriptId = -1;
      PreparedStatement stmt = null;
      try
      {
         scriptId = getNextId(conn, "SCRIPT");
         stmt = conn.prepareStatement(getInsertStatement(SCRIPT_INSERT_TABLE,
            SCRIPT_INSERT_FIELDS));

         /* Note: the order of these bindings must match the order of the
          * fields in the SCRIPT_INSERT_FIELDS list
          */
         stmt.setInt(1, scriptId);
         stmt.setString(2, results.getScriptName());
         stmt.setString(3, results.getClientName());
         stmt.setTimestamp(4, new Timestamp(results.getStartDate().getTime()));
         stmt.setDouble(5, results.averageCaseTime());
         stmt.setDouble(6, results.medianCaseTime());
         stmt.setDouble(7, results.fastestCaseTime());
         stmt.setDouble(8, results.slowestCaseTime());
         stmt.setInt(9, results.getTestsCount());
         stmt.setInt(10, results.getFailuresCount());
         stmt.setInt(11, results.getErrorsCount());
         stmt.setInt(12, results.getSkipsCount());
         stmt.setInt(13, results.getPassesCount());

         // handle writing log with stream
         StringWriter writer = new StringWriter();
         results.writeLog(writer);
         String log = writer.toString();

         /* Use setAsciiStream to be as generic as possible
          * (won't work with Oracle)
          */
         ByteArrayInputStream in = new ByteArrayInputStream(
            log.getBytes());
         stmt.setAsciiStream(14, in, log.length());

         QAPerformanceStats cpuStats = results.getCpuUsage();
         if (cpuStats == null)
            cpuStats = new QAPerformanceStats();
         stmt.setInt(15, cpuStats.getAllCpuTicks().length);
         stmt.setInt(16, cpuStats.getMinCpu());
         stmt.setInt(17, cpuStats.getMaxCpu());
         stmt.setInt(18, cpuStats.getAvgCpu());

         stmt.executeUpdate();
         stmt.close();
         stmt = null;
         PSSqlHelper.commit(conn);
      }
      finally
      {
         if(null != stmt)
            try {stmt.close();} catch (Throwable T) {};
      }

      return scriptId;
   }

   /**
    * Writes the page level results info to the database.
    *
    * @param conn The connection to use, assumed not <code>null</code>. Does not
    * take ownership.
    * @param results The results to log, assumed not <code>null</code>.
    * @param scriptId The script Id to use.
    *
    * @return The page id assigned when inserting.
    *
    * @throws SQLException if there are any errors.
    */
   private int writePageInfo(Connection conn, QATestPageResults results,
      int scriptId)
      throws SQLException
   {
      int pageId = -1;
      PreparedStatement stmt = null;
      try
      {
         pageId = getNextId(conn, "PAGE");
         stmt = conn.prepareStatement(getInsertStatement(PAGE_INSERT_TABLE,
            PAGE_INSERT_FIELDS));

         /* Note: the order of these bindings must match the order of the
          * fields in the PAGE_INSERT_FIELDS list
          */
         stmt.setInt(1, pageId);
         stmt.setInt(2, scriptId);
         stmt.setString(3, results.getPageName());
         stmt.setString(4, "?"); // TODO: "Authenticated?" not supported yet
         stmt.setDouble(5, (double)(results.getTimeToFirstByte()));
         stmt.setDouble(6, (double)(results.getTimeToLastByte()));
         stmt.setInt(7, results.getTests().size());
         stmt.setInt(8, results.getBytesSent());
         stmt.setInt(9, results.getBytesReceived());
         stmt.setInt(10, results.getMinCpu());
         stmt.setInt(11, results.getMaxCpu());
         stmt.setInt(12, results.getAvgCpu());
         stmt.setInt(13, results.getThreadPoolSize());

         stmt.executeUpdate();
         stmt.close();
         stmt = null;
         PSSqlHelper.commit(conn);
      }
      finally
      {
         if(null != stmt)
            try {stmt.close();} catch (Throwable T) {};
      }

      return pageId;
   }


   /**
    * Writes the test case level results info to the database.
    *
    * @param conn The connection to use, assumed not <code>null</code>. Does not
    * take ownership.
    * @param results The test results to log, assumed not <code>null</code>.
    * @param scriptId The script Id to use.
    * @param pageId The page Id to use.  <code>-1</code> if test is not part
    * of a page.
    *
    * @throws SQLException if there are any errors.
    */
   private void writeCaseInfo(Connection conn, QATestResult test, int scriptId,
      int pageId) throws SQLException
   {
      int caseId = -1;
      PreparedStatement stmt = null;
      try
      {
         // first insert the case
         caseId = getNextId(conn, "CASE");
         stmt = conn.prepareStatement(getInsertStatement(CASE_INSERT_TABLE,
            CASE_INSERT_FIELDS));

         /* Note: the order of these bindings must match the order of the
          * fields in the CASE_INSERT_FIELDS list
          */
         stmt.setInt(1, caseId);
         stmt.setTimestamp(2, new Timestamp(test.endTime().getTime()));
         stmt.setTimestamp(3, new Timestamp(test.startTime().getTime()));
         stmt.setString(4, test.getStatus() + "");
         stmt.setString(5, test.getRequestType());

         // handle writing detail with stream
         String detail = test.getDetail() == null ? "" :
            test.getDetail().toString();
         /* Use setAsciiStream to be as generic as possible
          * (won't work with Oracle)
          */
         ByteArrayInputStream in = new ByteArrayInputStream(
            detail.getBytes());
         stmt.setAsciiStream(6, in, detail.length());

         stmt.setString(7, test.getMessage());
         stmt.setString(8, "?"); // TODO: "Authenticated?" not supported yet

         QAPerformanceStats stats = test.getPerformanceStats();
         stmt.setInt(9, stats.getBytesSent());
         stmt.setInt(10, stats.getBytesReceived());
         stmt.setInt(11, stats.getMinCpu());
         stmt.setInt(12, stats.getMaxCpu());
         stmt.setInt(13, stats.getAvgCpu());
         stmt.setDouble(14, (double)(stats.getTimeOfFirstByte() -
            stats.getTimeOfRequest()));
         stmt.setDouble(15, (double)(stats.getTimeOfLastByte() -
            stats.getTimeOfRequest()));
         stmt.setDouble(16, (double)(stats.getConnectTime() -
            stats.getTimeOfRequest()));

         stmt.executeUpdate();
         stmt.close();
         stmt = null;

         if (pageId == -1)
         {
            stmt = conn.prepareStatement(getInsertStatement(
               SCRIPT_CASE_INSERT_TABLE, SCRIPT_CASE_INSERT_FIELDS));

            /* Note: the order of these bindings must match the order of the
             * fields in the SCRIPT_CASE_INSERT_FIELDS list
             */
            stmt.setInt(1, scriptId);
            stmt.setInt(2, caseId);

            stmt.executeUpdate();
            stmt.close();
            stmt = null;
         }
         else
         {
            stmt = conn.prepareStatement(getInsertStatement(
               PAGE_CASE_INSERT_TABLE, PAGE_CASE_INSERT_FIELDS));

            /* Note: the order of these bindings must match the order of the
             * fields in the PAGE_CASE_INSERT_FIELDS list
             */
            stmt.setInt(1, pageId);
            stmt.setInt(2, caseId);

            stmt.executeUpdate();
            stmt.close();
            stmt = null;
         }

         // write cpu info if we have it
         Object[][] ticks = stats.getAllCpuTicks();
         for (int i=0; i < ticks.length; i++)
         {
            Date timestamp = (Date)ticks[i][0];
            int utilization = ((Integer)ticks[i][1]).intValue();

            stmt = conn.prepareStatement(getInsertStatement(
               CPU_INSERT_TABLE, CPU_INSERT_FIELDS));

            /* Note: the order of these bindings must match the order of the
             * fields in the CPU__INSERT_FIELDS list
             */
            stmt.setInt(1, caseId);
            stmt.setTimestamp(2, new Timestamp(timestamp.getTime()));
            stmt.setInt(3, utilization);

            stmt.executeUpdate();
            stmt.close();
            stmt = null;
         }

         PSSqlHelper.commit(conn);
      }
      finally
      {
         if(null != stmt)
            try {stmt.close();} catch (Throwable T) {};
      }
   }


   /**
    * Gets the next id to use for a particular table. Performs a query against
    * the NEXTNUMBER table, looking for an entry that matches  the supplied
    * keyId. If  one is found, the value associated with that entry +1 is
    * returned, otherwise, a new entry is created in the table and 1 is
    * returned.
    *
    * @param conn The connectin to use, assumed not <code>null</code>. Does not
    * take ownership.
    * @param keyId The key to use, assumed not <code>null</code> or empty.
    *
    * @return the next available id.
    *
    * @throws SQLException if there are any errors.
    */
   private int getNextId(Connection conn, String keyId) throws SQLException
   {
      int iResult;
      PreparedStatement stmt = null;
      ResultSet rs = null;

      try
      {
         int rowcount = 0;

         // query the current value
         stmt = conn.prepareStatement(ID_QUERY);
         stmt.setString(1, keyId);

         rs = stmt.executeQuery();

         // if we got one, increment it, otherwise start at 1
         boolean isInsert = false;
         if(false == rs.next())
         {
            iResult = 1;
            isInsert = true;
         }
         else
            iResult = rs.getInt(1) + 1;
         rs.close();
         rs = null;
         stmt.close();
         stmt = null;

         // update the row or insert a new one
         if (!isInsert)
         {
            stmt = conn.prepareStatement(ID_UPDATE);
            stmt.setInt(1, iResult);
            stmt.setString(2, keyId);
            rowcount = stmt.executeUpdate();
         }
         else
         {
            stmt = conn.prepareStatement(ID_INSERT);
            stmt.setString(1, keyId);
            stmt.setInt(2, iResult);
            rowcount = stmt.executeUpdate();
         }

        PSSqlHelper.commit(conn);

         if(0 == rowcount)
         {
            // this would be a bug or some bizarre sql problem
            String action = isInsert ? "insert" : "update";
            throw new SQLException("Unable to " + action +
               " next id value for key " + keyId);
         }
      }
      finally
      {
         if(null != rs)
            try {rs.close();} catch (Throwable T) {};
         if(null != stmt)
            try {stmt.close();} catch (Throwable T) {};
      }

      return iResult;

   }

   /**
    * Makes a connection to the back end via JDBC.
    *
    * @param driver The JDBC driver, assumed not <code>null</code> or empty.
    * @param server The server, assumed not <code>null</code> or empty.
    * @param database The database, may be <code>null</code> or empty.
    * @param userid The user id for login, may be <code>null</code> or empty.
    * @param password The un-encrypted password for the user id, may be <code>
    * null</code> or empty.
    * 
    * @return The Connection, never <code>null</code>
    *
    * @throws SQLException If any errors are encountered.
    * @throws ClassNotFoundException if the jdbc driver class cannot be located.
    */
   private Connection makeConnection(String driver, String server,
      String database, String userid, String password) throws SQLException,
         ClassNotFoundException
   {
      Properties props = new Properties();
      props.setProperty("user", (userid == null ? "" : userid));

      // get the plaintext version of the password
      props.setProperty("password", (password == null ? "" : password));

      if (database != null)
         props.setProperty("catalog", database);

      /* drivers like IBM's require this to avoid popping up a dialog box */
      props.setProperty("prompt", "false");

      // Must first get Driver, then connect to avoid deadlocks
      Class.forName(m_dbDriverClassName);
      StringBuilder rval = new StringBuilder(40);
      rval.append("jdbc:");
      if (driver.equalsIgnoreCase("asa"))
      {
         rval.append("odbc");
      }
      else
      {
         rval.append(driver);
      }
      rval.append(':');
      rval.append(server);
      String connStr = rval.toString();
      Driver drv = DriverManager.getDriver(connStr);
      Connection conn = drv.connect(connStr, props);
      if ( null == conn )
      {
         // We get here if no drivers recognize the URL
         throw new SQLException(
            "No loaded JDBC driver can handle this connect string: " + connStr);
      }

      /* Connecting may not have put us in the specified database, so if we
       * specified one, make sure we are in it, and switch if we are not.
       */
      if (database != null && database.length() != 0)
      {
         if (!database.equalsIgnoreCase(conn.getCatalog()))
         {
            try
            {
               conn.setCatalog(database);
            }
            catch (java.sql.SQLWarning e)
            {
               /* ignore SQL warnings as is usually drivers like MSSQL notifying
                * success.
                */
            }

            // if it's still not set, warn about it
            if (!database.equalsIgnoreCase(conn.getCatalog()))
            {
               throw new SQLException("Unable to switch database to " +
                  database);
            }
         }
      }
      return conn;
   }

   /**
    * Creates insert statement to prepare.
    *
    * @param table The table name, assumed not <code>null</code> or empty.
    * @param fields Array of field names, assumed not <code>null</code>.
    *
    * @return The insert statement, never <code>null</code> or empty.
    */
   private String getInsertStatement(String table, String[] fields)
   {
      StringBuilder stmt = new StringBuilder();
      stmt.append("INSERT INTO ");
      stmt.append(table);
      stmt.append("(");

      for (int i = 0; i < fields.length; i++)
      {
         if (i > 0)
            stmt.append(", ");
         stmt.append(fields[i]);
      }

      stmt.append(") VALUES (");
      for (int i = 0; i < fields.length; i++)
      {
         if (i > 0)
            stmt.append(", ");
         stmt.append("?");
      }
      stmt.append(")");

      return stmt.toString();
   }

   /**
    * Returns a formatted string containing the test of all of the exceptions
    * contained in the supplied SQLException.
    * <p>There seems to be a bug in the Sprinta driver. We get an exception
    * for Primary key constraint violation, which has a sql warning as the
    * next exception (warning). But this next warning has a circular
    * reference to itself in the next link. So we check for this problem and
    * limit the max errors we will process.
    *
    * @param e The exception to process. If <code>null</code>, an empty
    *    string is returned.
    *
    * @return The string, never <code>null</code>, may be empty.
    */
   public static String getFormattedExceptionText(SQLException e)
   {
      if ( null == e )
         return "";
         
      StringBuilder errorText   = new StringBuilder();

      int errNo = 1;
      final int maxErrors = 20;
      for ( ; e != null && errNo <= maxErrors; )
      {
         errorText.append( "[" );
         errorText.append( errNo );
         errorText.append( "] " );
         errorText.append( e.getSQLState());
         errorText.append( ": " );
         errorText.append( e.getMessage());
         errorText.append( " " );
         SQLException tmp = e.getNextException();
         if ( e == tmp )
            break;
         else
            e = tmp;
         errNo++;
      }
      if ( errNo == maxErrors + 1 )
      {
         errorText.append( "[Maximum # of error messages (" );
         errorText.append( maxErrors );
         errorText.append(  ") exceeded. Rest truncated]" );
      }

      return errorText.toString();
   }

   /**
    * Driver class name used to connect to the database.  Initialized in the
    * ctor, never <code>null</code> or modified after that.
    */
   private String m_dbDriverClassName = null;

   /**
    * Driver name used to connect to the database.  Initialized in the ctor,
    * never <code>null</code> or modified after that.
    */
   private String m_dbDriverName = null;

   /**
    * Server name used to connect to the database.  Initialized in the ctor,
    * never <code>null</code> or modified after that.
    */
   private String m_dbServer = null;

   /**
    * Database name used to connect to the database.  Initialized in the ctor,
    * may be <code>null</code>, never modified after that.
    */
   private String m_dbName = null;

   /**
    * Schema name used to connect to the database.  Initialized in the ctor,
    * may be <code>null</code>, never modified after that.
    */
   private String m_dbSchema = null;

   /**
    * User id used to connect to the database.  Initialized in the ctor,
    * may be <code>null</code>, never modified after that.
    */
   private String m_dbUid = null;

   /**
    * Password used to connect to the database.  Initialized in the ctor,
    * may be <code>null</code>, never modified after that.
    */
   private String m_dbPwd = null;

   /**
    * Constant for the property containing the driver class name.
    */
   private static final String DB_DRIVER_CLASSNAME = "DB_DRIVER_CLASSNAME";

   /**
    * Constant for the property containing the driver name.
    */
   private static final String DB_DRIVER_NAME = "DB_DRIVER_NAME";

   /**
    * Constant for the property containing the server name.
    */
   private static final String DB_SERVER = "DB_SERVER";

   /**
    * Constant for the property containing the database name.
    */
   private static final String DB_NAME = "DB_NAME";

   /**
    * Constant for the property containing the schema name.
    */
   private static final String DB_SCHEMA = "DB_SCHEMA";

   /**
    * Constant for the property containing the user id.
    */
   private static final String DB_UID = "DB_UID";

   /**
    * Constant for the property containing the password.
    */
   private static final String DB_PWD = "DB_PWD";

   /**
    * Sql statement to select current id.
    */
   private static final String ID_QUERY =
      "SELECT NEXTNR FROM NEXTNUMBER WHERE KEYNAME = ?";

   /**
    * Sql statement to update current id.
    */
   private static final String ID_UPDATE =
      "UPDATE NEXTNUMBER SET NEXTNR = ? WHERE KEYNAME = ?";

   /**
    * Sql statement to insert current id.
    */
   private static final String ID_INSERT =
      "INSERT INTO NEXTNUMBER (KEYNAME, NEXTNR) VALUES (?, ?)";

   /**
    * Constant for script table name.
    */
   private static final String SCRIPT_INSERT_TABLE = "TEST_SCRIPT_RESULTS";

   /**
    * Array of field names for script table.
    */
   private static final String[] SCRIPT_INSERT_FIELDS =
   {
      "SCRIPT_ID",
      "SCRIPT_NAME",
      "CLIENT_NAME",
      "START_TIME",
      "AVG_CASE_TIME",
      "MEDIAN_CASE_TIME",
      "FASTEST_CASE_TIME",
      "SLOWEST_CASE_TIME",
      "NUM_CASES",
      "NUM_FAILURES",
      "NUM_ERRORS",
      "NUM_SKIPS",
      "NUM_PASSES",
      "LOG",
      "CPU_STAT_COUNT",
      "CPU_MIN",
      "CPU_MAX",
      "CPU_AVG"
   };

   /**
    * Constant for test case table name.
    */
   private static final String CASE_INSERT_TABLE = "TEST_CASE_RESULTS";

   /**
    * Array of field names for test case table.
    */
   private static final String[] CASE_INSERT_FIELDS =
   {
      "CASE_ID",
      "END_TIME",
      "START_TIME",
      "STATUS",
      "TYPE",
      "DETAIL",
      "MESSAGE",
      "AUTHENTICATED",
      "BYTES_SENT",
      "BYTES_RECEIVED",
      "CPU_MIN",
      "CPU_MAX",
      "CPU_AVG",
      "TTFB",
      "TTLB",
      "CONNECT_TIME"
   };

   /**
    * Constant for test page table name.
    */
   private static final String PAGE_INSERT_TABLE = "TEST_PAGE_RESULTS";

   /**
    * Array of field names for test page table.
    */
   private static final String[] PAGE_INSERT_FIELDS =
   {
      "PAGE_ID",
      "SCRIPT_ID",
      "NAME",
      "AUTHENTICATED",
      "TTFB",
      "TTLB",
      "SOCKET_CONNECTS",
      "BYTES_SENT",
      "BYTES_RECEIVED",
      "CPU_MIN",
      "CPU_MAX",
      "CPU_AVG",
      "THREAD_POOL_SIZE"
   };

   /**
    * Constant for cpu table name.
    */
   private static final String CPU_INSERT_TABLE = "TEST_CASE_CPU";

   /**
    * Array of field names for cpu table.
    */
   private static final String[] CPU_INSERT_FIELDS =
   {
      "CASE_ID",
      "TIMESTAMP",
      "CPU_UTILIZATION"
   };

   /**
    * Constant for script-case cross table name.
    */
   private static final String SCRIPT_CASE_INSERT_TABLE = "SCRIPT_CASE_RESULTS";

   /**
    * Array of field names for script-case cross table.
    */
   private static final String[] SCRIPT_CASE_INSERT_FIELDS =
   {
      "SCRIPT_ID",
      "CASE_ID"
   };

   /**
    * Constant for page-case cross table name.
    */
   private static final String PAGE_CASE_INSERT_TABLE = "PAGE_CASE_RESULTS";

   /**
    * Array of field names for page-case cross table.
    */
   private static final String[] PAGE_CASE_INSERT_FIELDS =
   {
      "PAGE_ID",
      "CASE_ID"
   };

}
