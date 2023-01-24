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

package com.percussion.ant.install;

import com.percussion.error.PSExceptionUtils;
import com.percussion.install.InstallUtil;
import com.percussion.install.PSLogger;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.PSJdbcUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * PSExecSQLStmt is an Installshield wizard bean which executes specified
 * sql statement during install.
 *
 * It has a general sql statement <code>sql</code> and a sql statement for
 * each database that is supported, <code>sqlSqlServer</code>,
 * <code>sqlOracle</code>, and
 * <code>sqlUDB</code>. Database specific sql statement takes preference over
 * the general sql statement. However if the database specific sql statement
 * is <code>null</code> or empty, then general sql statement is used.
 *
 * The <code>objectNames</code> contains the names of tables or views which
 * should be replaced by fully qualified table or view name before executing
 * the sql statement. For example, if the sql statement is CREATE VIEW
 * RXRELATEDCONTENT ... then "RXRELATEDCONTENT" string should be added to
 * <code>objectNames</code> so that it is replaced by fully qualified name
 * such as "rxmaster.dbo.RXRELATEDCONTENT" on MS SqlServer before executing the
 * sql statement.
 *
 *<br>
 * Example Usage:
 *<br>
 *<pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="execSQLStmt"
 *              class="com.percussion.ant.install.PSExecSQLStmt"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to execute the statement.
 *
 *  <code>
 *  &lt;execSQLStmt printExceptionStackTrace="true"
 *                  qualifyTableNames="TABLE1"
 *                  qualifyViewNames=""
 *                  sql="SELECT * FROM TABLE1 WHERE COLUMNID=0"
 *                  sqlOracle=""
 *                  sqlSqlServer=""
 *                  sqlUDB=""
 *                  sqlDerby=""
 *                  sqlMysql=""/&gt;
 *  </code>
 *
 * </pre>
 *
 */

public class PSExecSQLStmt extends PSAction
{
   private static final Logger log = LogManager.getLogger(PSExecSQLStmt.class);
   // see base class
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   @Override
   public void execute() {
      String propFile = getRootDir() + File.separator
              + "rxconfig/Installer/rxrepository.properties";

      File f = new File(propFile);
      if (!(f.exists() && f.isFile()))
         return;

      try (FileInputStream in = new FileInputStream(f)) {
         Properties props = new Properties();
         props.load(in);
         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
         if (getRootDir() != null && !"".equals(getRootDir())) {
            InstallUtil.setRootDir(getRootDir());
         }
         String pw = dbmsDef.getPassword();
         String driver = dbmsDef.getDriver();
         String server = dbmsDef.getServer();
         String database = dbmsDef.getDataBase();
         String uid = dbmsDef.getUserId();
         PSLogger.logInfo("Driver : " + driver + " Server : " + server + " Database : " + database + " uid : " + uid);
         try (Connection conn = InstallUtil.createConnection(driver,
                 server,
                 database,
                 uid,
                 pw
         )) {

            String strStmt = sql;
            String dbStrStmt = "";


            if (driver.equalsIgnoreCase(PSJdbcUtils.DB2))
               dbStrStmt = sqlUDB;
            else if (driver.equalsIgnoreCase(PSJdbcUtils.DERBY_DRIVER))
               dbStrStmt = sqlDerby;
            else if (driver.equalsIgnoreCase(PSJdbcUtils.MYSQL_DRIVER))
               dbStrStmt = sqlMysql;
            else if (driver.equalsIgnoreCase(PSJdbcUtils.JTDS_DRIVER) ||
                    driver.equalsIgnoreCase(PSJdbcUtils.MICROSOFT_DRIVER) ||
                    driver.equalsIgnoreCase(PSJdbcUtils.SPRINTA))
               dbStrStmt = sqlSqlServer;
            else if (driver.startsWith(PSJdbcUtils.ORACLE_PRIMARY))
               dbStrStmt = sqlOracle;

            if (dbStrStmt.trim().length() > 0)
               strStmt = dbStrStmt;

            if (strStmt.trim().length() < 1)
               return;

            // replace the table names with fully qualified names
            strStmt = qualifyTableNames(strStmt, dbmsDef);

            // replace the view names with fully qualified names
            strStmt = qualifyViewNames(strStmt, dbmsDef);

            PSLogger.logInfo("Executing statement : " + strStmt);
            try (Statement stmt = conn.createStatement()) {
               stmt.execute(strStmt);
            } catch (Exception e) {
               handleException(e);
            }

         } catch (Exception ex) {
           handleException(ex);
         } finally {
            if (driver.equalsIgnoreCase(PSJdbcUtils.DERBY_DRIVER))
               InstallUtil.shutDownDerby();
         }

      } catch (PSJdbcTableFactoryException | IOException e) {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }
   }

   private void handleException(Exception ex){
      //ERROR Code for specified View Not Exist, ignore it
      if( ex.getMessage().contains("ORA-00942") || ex.getMessage().contains("does not exist")){
         PSLogger.logWarn(ex.getMessage());
         return;
      }

      if (!isFailonerror()) {
         if (!isSilenceErrors()) {
            PSLogger.logError(ex.getMessage());
            if (getPrintExceptionStackTrace()) {
               PSLogger.logError(ex);
            }
         }
         return;
      } else {
         if (!isSilenceErrors()) {
            PSLogger.logError(ex.getMessage());
            if (getPrintExceptionStackTrace()) {
               PSLogger.logError(ex);
            }
         }

         throw new BuildException(ex);
      }
   }

      /*******************************************************************
    * Private functions.
    *******************************************************************/

   /*******************************************************************
    * Property accessors and mutators.
    *******************************************************************/

   /**
    * Returns the name of table, which should be replaced by fully
    * qualified table name before executing the sql statement.
    *
    * @return names of tables or views which should be replaced by fully
    * qualified table or view name before executing the sql statement,
    * never <code>null</code>, may be empty
    */
   public String[] getQualifyTableNames()
   {
      return m_qualifyTableNames;
   }

   /**
    * Sets the name of table, which should be replaced by fully
    * qualified table name before executing the sql statement.
    *
    * @param objectNames names of tables, which should be replaced by
    * fully qualified table name before executing the sql statement,
    * may be <code>null</code> or empty, if <code>null</code> then set to empty
    * array.
    */
   public void setQualifyTableNames(String objectNames)
   {
      m_qualifyTableNames= convertToArray(objectNames);
   }

   /**
    * Returns the name of view, which should be replaced by fully
    * qualified view name before executing the sql statement.
    *
    * @return names of tables or views which should be replaced by fully
    * qualified view name before executing the sql statement,
    * never <code>null</code>, may be empty
    */
   public String[] getQualifyViewNames()
   {
      return m_qualifyViewNames;
   }

   /**
    * Sets the name of view, which should be replaced by fully
    * qualified view name before executing the sql statement.
    *
    * @param objectNames names of views, which should be replaced by
    * fully qualified view name before executing the sql statement,
    * may be <code>null</code> or empty, if <code>null</code> then set to empty
    * array.
    */
   public void setQualifyViewNames(String objectNames)
   {
      m_qualifyViewNames= convertToArray(objectNames);
   }


   /**
    * The sql statement to execute if database specific sql statment is not
    * specified.
    *
    * @return the sql statement to execute if database specific sql statment is
    * empty, never <code>null</code>, may be empty.
    */
   public String getSql()
   {
      return sql;
   }

   /**
    * Sets the sql statement to execute if database specific sql statment is not
    * specified.
    *
    * @param sql the sql statement to execute if database specific sql statment
    * is empty, may be <code>null</code> or empty, if <code>null</code> then
    * set to empty.
    */
   public void setSql(String sql)
   {
      if (sql == null)
         sql = "";
      this.sql = sql;
   }

   /**
    * The sql statement to execute for MS Sql Server database. If empty,
    * <code>sql</code> is executed if it is not empty.
    *
    * @return the sql statement to execute for MS Sql Server database,
    * never <code>null</code>, may be empty
    */
   public String getSqlSqlServer()
   {
      return sqlSqlServer;
   }

   /**
    * Sets the sql statement to execute for MS Sql Server database. If
    * <code>null</code> or empty, then <code>sql</code> is executed if it is
    * not empty.
    *
    * @param sqlSqlServer the sql statement to execute for MS Sql Server
    * database, may be <code>null</code> or empty, if <code>null</code> then
    * set to empty.
    */
   public void setSqlSqlServer(String sqlSqlServer)
   {
      if (sqlSqlServer == null)
         sqlSqlServer = "";
      this.sqlSqlServer = sqlSqlServer;
   }

   /**
    * The sql statement to execute for Oracle database. If empty,
    * <code>sql</code> is executed if it is not empty.
    *
    * @return the sql statement to execute for Oracle database,
    * never <code>null</code>, may be empty
    */
   public String getSqlOracle()
   {
      return sqlOracle;
   }

   /**
    * Sets the sql statement to execute for Oracle database. If
    * <code>null</code> or empty, then <code>sql</code> is executed if it is
    * not empty.
    *
    * @param sqlOracle the sql statement to execute for Oracle
    * database, may be <code>null</code> or empty, if <code>null</code> then
    * set to empty.
    */
   public void setSqlOracle(String sqlOracle)
   {
      if (sqlOracle == null)
         sqlOracle = "";
      this.sqlOracle = sqlOracle;
   }

   /**
    * The sql statement to execute for DB2 database. If empty,
    * <code>sql</code> is executed if it is not empty.
    *
    * @return the sql statement to execute for DB2 database,
    * never <code>null</code>, may be empty
    */
   public String getSqlUDB()
   {
      return sqlUDB;
   }

   /**
    * Sets the sql statement to execute for DB2 database. If
    * <code>null</code> or empty, then <code>sql</code> is executed if it is
    * not empty.
    *
    * @param sqlUDB the sql statement to execute for DB2
    * database, may be <code>null</code> or empty, if <code>null</code> then
    * set to empty.
    */
   public void setSqlUDB(String sqlUDB)
   {
      if (sqlUDB == null)
         sqlUDB = "";
      this.sqlUDB = sqlUDB;
   }

   /**
    * The sql statement to execute for Derby database. If empty,
    * <code>sql</code> is executed if it is not empty.
    *
    * @return the sql statement to execute for Derby database,
    * never <code>null</code>, may be empty
    */
   public String getSqlDerby()
   {
      return sqlDerby;
   }

   /**
    * Sets the sql statement to execute for Derby database. If
    * <code>null</code> or empty, then <code>sql</code> is executed if it is
    * not empty.
    *
    * @param sqlDerby the sql statement to execute for Derby
    * database, may be <code>null</code> or empty, if <code>null</code> then
    * set to empty.
    */
   public void setSqlDerby(String sqlDerby)
   {
      if (sqlDerby == null)
         sqlDerby = "";
      this.sqlDerby = sqlDerby;
   }


   /**
    * The sql statement to execute for Mysql database. If empty,
    * <code>sql</code> is executed if it is not empty.
    *
    * @return the sql statement to execute for Mysql database,
    * never <code>null</code>, may be empty
    */
   public String getSqlMysql()
   {
      return sqlMysql;
   }

   /**
    * Sets the sql statement to execute for Mysql database. If
    * <code>null</code> or empty, then <code>sql</code> is executed if it is
    * not empty.
    *
    * @param sqlMysql the sql statement to execute for Mysql
    * database, may be <code>null</code> or empty, if <code>null</code> then
    * set to empty.
    */
   public void setSqlMysql(String sqlMysql)
   {
      if (sqlMysql == null)
         sqlMysql = "";
      this.sqlMysql = sqlMysql;
   }

   /**
    * Indicates whether the stack trace of the exception generated when
    * executing the SQL statement should be printed to the log.
    *
    * @return <code>true</code> if the stack trace should be printed,
    * <code>false</code> otherwise.
    *
    */
   public boolean getPrintExceptionStackTrace()
   {
      return m_printExceptionStackTrace;
   }

   /**
    * Sets whether the stack trace of the exception generated when
    * executing the SQL statement should be printed to the log.
    *
    * @param print <code>true</code> if the stack trace should be printed,
    * <code>false</code> otherwise.
    */
   public void setPrintExceptionStackTrace(boolean print)
   {
      m_printExceptionStackTrace = print;
   }

   /**
    * Helper function to qualify any specified table names in a given sql
    * statement.
    *
    * @param strStmt the unqualified sql statement, assumed not <code>null</code>
    * @param dbmsDef the PSJdbcDbmsDef object with current repository properties,
    * assumed not <code>null</code>
    * @return the qualified sql statement, never <code>null</code>, may be empty.
    */
   private String qualifyTableNames(String strStmt, PSJdbcDbmsDef dbmsDef)
   {
      StringBuilder strBuffer = new StringBuilder();
      StringTokenizer stok = new StringTokenizer(strStmt);

      while (stok.hasMoreTokens())
      {
         String token = stok.nextToken();

         for (int i = 0; i < m_qualifyTableNames.length; i++)
         {
            String name = m_qualifyTableNames[i].trim();

            if (token.compareTo(name) == 0)
            {
               token = PSSqlHelper.qualifyTableName(name, dbmsDef.getDataBase(),
                       dbmsDef.getSchema(), dbmsDef.getDriver());
               break;
            }
         }

         strBuffer.append(token);

         if (stok.hasMoreTokens())
            strBuffer.append(" ");
      }

      return strBuffer.toString();
   }

   /**
    * Helper function to qualify any specified view names in a given sql
    * statement.
    *
    * @param strStmt the unqualified sql statement, assumed not <code>null</code>
    * @param dbmsDef the PSJdbcDbmsDef object with current repository properties,
    * assumed not <code>null</code>
    * @return the qualified sql statement, never <code>null</code>, may be empty.
    */
   private String qualifyViewNames(String strStmt, PSJdbcDbmsDef dbmsDef)
   {
      StringBuilder strBuffer = new StringBuilder();
      StringTokenizer stok = new StringTokenizer(strStmt);

      while (stok.hasMoreTokens())
      {
         String token = stok.nextToken();

         for (int i = 0; i < m_qualifyViewNames.length; i++)
         {
            String name = m_qualifyViewNames[i].trim();

            if (token.compareTo(name) == 0)
            {
               token = PSSqlHelper.qualifyViewName(name, dbmsDef.getDataBase(),
                       dbmsDef.getSchema(), dbmsDef.getDriver());
               break;
            }
         }

         strBuffer.append(token);

         if (stok.hasMoreTokens())
            strBuffer.append(" ");
      }

      return strBuffer.toString();
   }

   /*******************************************************************
    * Properties
    *******************************************************************/

   /**
    * names of tables, which should be replaced by fully qualified table
    * name before executing the sql statement, never <code>null</code>,
    * may be empty.
    */
   private String[] m_qualifyTableNames = new String[0];

   /**
    * names of views, which should be replaced by fully qualified view
    * name before executing the sql statement, never <code>null</code>,
    * may be empty.
    */
   private String[] m_qualifyViewNames = new String[0];


   /**
    * The sql statement to execute if database specific sql statment is
    * empty, never <code>null</code>, may be empty
    */
   private String sql = "";

   /**
    * sql statement to use for MS Sql Server database, never <code>null</code>,
    * may be empty. If empty, <code>sql</code> is executed if it is not empty
    */
   private String sqlSqlServer = "";

   /**
    * sql statement to use for Oracle database, never <code>null</code>,
    * may be empty. If empty, <code>sql</code> is executed if it is not empty
    */
   private String sqlOracle = "";

   /**
    * sql statement to use for UDB (DB2) database, never <code>null</code>,
    * may be empty. If empty, <code>sql</code> is executed if it is not empty
    */
   private String sqlUDB = "";

   /**
    * sql statement to use for Apache Derby database, never <code>null</code>,
    * may be empty. If empty, <code>sql</code> is executed if it is not empty
    */
   private String sqlDerby = "";

   /**
    * sql statement to use for Mysql database, never <code>null</code>,
    * may be empty. If empty, <code>sql</code> is executed if it is not empty
    */
   private String sqlMysql = "";

   /**
    * Indicates whether the stack trace of the exception generated when
    * executing the SQL statement should be printed to the log,
    * defaults to <code>true</code>, modified using
    * <code>setPrintExceptionStackTrace()</code> method.
    */
   private boolean m_printExceptionStackTrace = true;

   /*******************************************************************
    * Member variables
    *******************************************************************/


}
