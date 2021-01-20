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

package com.percussion.ant.install;

import com.percussion.install.IPSUpgradeModule;
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.install.PSLogger;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSProperties;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.PSJdbcUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * PSTrimRxPubDocs is a task which will trim the RXPUBDOCS table based on
 * the user property.  This class will trim to the date if one was specified by
 * the user.
 *
 * It will also write to the log file showing any table rows that were removed.
 *
 *<br>
 * Example Usage:
 *<br>
 *<pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="trimRxPubDocs"
 *              class="com.percussion.ant.install.PSTrimRxPubDocs"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to trim the table.
 *
 *  <code>
 *  &lt;trimRxPubDocs shouldTrim="true" trimDate="2005-11-01"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSTrimRxPubDocs extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      String strRootDir = null;
      Connection conn = null;
      PSProperties props = null;
      PSJdbcDbmsDef dbmsDef = null;

      try
      {
         strRootDir = getRootDir();

         if (!(strRootDir.endsWith(File.separator)))
            strRootDir += File.separator;

         props = new PSProperties(strRootDir + IPSUpgradeModule.REPOSITORY_PROPFILEPATH);
         props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
         dbmsDef = new PSJdbcDbmsDef(props);
         conn = RxLogTables.createConnection(props);

         if (conn == null)
         {
            PSLogger.logError(
            "PSTrimRxPubDocs#execute : Could not establish connection with database");
            PSLogger.logError(
            "PSTrimRxPubDocs#execute : Table modifications aborted");

            return;
         }

         // Trim table if specified
         if (m_bShouldTrim)
         {
            trimTable(conn, dbmsDef, m_strTrimDate);
         }
      }
      catch(Exception e)
      {
         PSLogger.logError("PSTrimRxPubDocs#execute : " + e.getMessage());
      }
      finally
      {
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
            }
            conn = null;
         }
      }
   }

   /**
    * Removes all rows with PUBDATE prior to the specified date
    *
    * @param conn the database connection object, cannot be <code>null</code>.
    * @param dbmsDef the database definition, cannot be <code>null</code>.
    * @param date the date to which publishing history will be trimmed,
    * assumed to be valid in the form mm/dd/yyyy, cannot be <code>null</code>.
    * @return <code>true</code> if modifications were made.
    */
   private void trimTable(
         final Connection conn, final PSJdbcDbmsDef dbmsDef, final String date)
   {

      PSLogger.logInfo("Trimming " + RXPUBDOCS_TABLE + " prior to " + date);

      String trimDate = date.replaceAll("/", "-");
      String qualTableName = PSSqlHelper.qualifyTableName(RXPUBDOCS_TABLE,
            dbmsDef.getDataBase(), dbmsDef.getSchema(),
            dbmsDef.getDriver());

      String trimStmtSql = "DELETE FROM " + qualTableName + " WHERE " +
      qualTableName + ".PUBDATE < '" + trimDate + "'";
      String trimStmtOracle = "DELETE FROM " + qualTableName + " WHERE " +
      qualTableName + ".PUBDATE < TO_DATE('" +
      trimDate + "', 'MM-DD-YYYY')";
      String trimStmt = "";
      String driver = dbmsDef.getDriver();
      if (PSSqlHelper.isOracle(driver))
         trimStmt = trimStmtOracle;
      else
         trimStmt = trimStmtSql;

      int rows = 0;
      try
      {

         if ( PSSqlHelper.isOracle(driver) ||
               driver.equals(PSJdbcUtils.DB2))
            conn.setAutoCommit(false);

         Statement stmt = conn.createStatement();
         rows = stmt.executeUpdate(trimStmt);

         if (PSSqlHelper.isOracle(driver) ||
               driver.equals(PSJdbcUtils.DB2))
         {
            conn.setAutoCommit(true);
            conn.commit();
         }
      }
      catch (SQLException e)
      {
         PSLogger.logError("PSTrimRxPubDocs#trimTable : " + e.getMessage());
      }

      // Write a log of the number of rows removed
      PSLogger.logInfo("Trimmed " + rows + " row(s) from " + RXPUBDOCS_TABLE);
   }

   /*************************************************************************
    * Property Accessors and Mutators
    *************************************************************************/

   /**
    * Sets the should trim flag.
    *
    * @param shouldTrim if <code>true</code> the table will be trimmed
    * according to the date specified by <code>m_strTrimDate</code>, otherwise
    * no modifications will be made.
    */
   public void setShouldTrim(boolean shouldTrim)
   {
      m_bShouldTrim = shouldTrim;
   }

   /**
    * Sets the trim date.
    *
    * @param trimDate all publishing history prior to this date will be
    * removed from the RXPUBDOCS table.
    */
   public void setTrimDate(String trimDate)
   {
      m_strTrimDate = trimDate;
   }

   /**************************************************************************
    * private function
    **************************************************************************/

   /**************************************************************************
    * Static Strings
    *************************************************************************/

   /**
    * RXPUBDOCS Table constant
    */
   private static String RXPUBDOCS_TABLE = "RXPUBDOCS";



   /**************************************************************************
    * Properties
    *************************************************************************/

   /**
    * Determines if the table should be trimmed.
    */
   private boolean m_bShouldTrim = false;

   /**
    * All history prior to this date will be removed from the table.
    */
   private String m_strTrimDate;




}

