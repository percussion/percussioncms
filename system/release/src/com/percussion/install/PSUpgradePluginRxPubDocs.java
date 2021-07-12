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
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.PSJdbcUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;


/**
 * This upgrade plug-in will populate the VARIANTID column of the
 * RXPUBDOCS table with the sys_variantid value found in the CONTENTURL column.
 * It will also write to the log file showing any table rows that were modified.
 */
public class PSUpgradePluginRxPubDocs implements IPSUpgradePlugin
{
   /**
    * Implements process method of IPSUpgardePlugin.
    */
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      m_config = config;
      log("Executing PSUpgradePluginRxPubDocs...");

      FileInputStream in = null;
      Connection conn = null;
      
      try
      {
         in = new FileInputStream(
               new File(
                  RxUpgrade.getRxRoot() + File.separator
                  + IPSUpgradeModule.REPOSITORY_PROPFILEPATH));

         Properties props = new Properties();
         props.load(in);
         props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");

         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
         
         conn = RxLogTables.createConnection(props);
         
         if (conn == null)
         {
            log(
               "Could not establish connection with database\n\n");
            log("Table modifications aborted");

            return null;
         }

         // Modify table
         if (!populateVariantId(conn, dbmsDef))
         {
            log("No modifications were made.");
         }
      }
      catch (Exception e)
      {
         e.printStackTrace(m_config.getLogStream());
      }
      finally
      {
         try
         {
            if (in != null)
            {
               in.close();
               in = null;
            }
         }
         catch (Throwable t) {}

         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e) {}

            conn = null;
         }
      }

      log("leaving the process() of the plugin...");
      return null;
   }

   /**
    * Populates the VARIANTID column with the sys_variantid value found in the
    * CONTENTURL column, if it exists, otherwise, it is unchanged.
    *
    * @param conn the database connection object, cannot be <code>null</code>.
    * @param connUpdate the database connection object for 
    * @param dbmsDef the database definition, cannot be <code>null</code>.
    * @return <code>true</code> if modifications were made.
    */
   private boolean populateVariantId(
      final Connection conn, final PSJdbcDbmsDef dbmsDef)
   {
      log("Attempting to modify VARIANTID column.");

      String qualTableName = PSSqlHelper.qualifyTableName(RXPUBDOCS_TABLE,
            dbmsDef.getDataBase(), dbmsDef.getSchema(), 
            dbmsDef.getDriver());
      
      String queryStmt = "SELECT " + qualTableName + ".VARIANTID," + 
                         qualTableName + ".CONTENTURL FROM " + qualTableName +
                         " WHERE " + qualTableName + ".VARIANTID=-1";
      
      boolean modifications = false;
      int rowsModified = 0;
      
      try
      {
         String driver = dbmsDef.getDriver();
         if ( PSSqlHelper.isOracle(driver) ||
               driver.equals(PSJdbcUtils.DB2))
         {
            conn.setAutoCommit(false);
            queryStmt = queryStmt + " FOR UPDATE";
         }
      
         Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                  ResultSet.CONCUR_UPDATABLE);
         
         ResultSet rs = stmt.executeQuery(queryStmt);      
                  
         while (rs.next())
         {
            String strVariantId = "";
            String contenturl = "";
            String[] parameters = null;
            boolean foundVariantId = false;
            int i;

            contenturl = rs.getString("CONTENTURL");
            
            // Split contenturl into parameters
            parameters = contenturl.split("&");
            
            for (i = 0; i < parameters.length; i++)
            {
               String parameter = parameters[i];
             
               if (parameter.startsWith("sys_variantid="))
               {
                  // Pull out the actual id
                  strVariantId = parameter.substring(parameter.indexOf("=") + 1);
                  foundVariantId = true;
                  break;
               }
            }
            
            if (foundVariantId && rs.getInt("VARIANTID") == -1 && 
                strVariantId.length() > 0)
            {
               // Found a variantid, now put it in VARIANTID column
               rs.updateInt("VARIANTID", (new Integer(strVariantId)).intValue());
               rs.updateRow();
               rowsModified++;
               modifications = true;
            }
         }
         
         if (PSSqlHelper.isOracle(driver) ||
               driver.equals(PSJdbcUtils.DB2))
         {
            conn.setAutoCommit(true);
            conn.commit();
         }
      }
      catch (SQLException e)
      {
            e.printStackTrace(m_config.getLogStream());
      }
      
      // Write a log of the number of rows modified
      String results = rowsModified + " rows"
            + " modified (VARIANTID was populated using CONTENTURL)"
            + " in table " + RXPUBDOCS_TABLE;
      
      if (rowsModified > 0)
         log(results);

      return modifications;
   }

   /**
    * Prints message to the log printstream if it exists
    * or just sends it to System.out
    *
    * @param msg the message to be logged, can be <code>null</code>.
    */
   private void log(String msg)
   {
      if (msg == null)
      {
         return;
      }

      if (m_config != null)
      {
         m_config.getLogStream().println(msg);
      }
      else
      {
         System.out.println(msg);
      }
   }

   private IPSUpgradeModule m_config;
   private static final String RXPUBDOCS_TABLE = "RXPUBDOCS";
}
