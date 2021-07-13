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

import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.util.PSSQLStatement;
import com.percussion.xml.PSXmlDocumentBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


/**
 * This upgrade plug-in will clean the TITLE column of the
 * CONTENTSTATUS table by replacing <code>null</code> or non-text entries with
 * a dummy value of "title_CONTENTID". It will also write to the log file showing
 * any table rows that were modified.
 */
public class PSUpgradePluginCleanContentStatus implements IPSUpgradePlugin
{
   /**
    * Implements process method of IPSUpgardePlugin.
    */
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      m_config = config;
      log("inside the process() of the plugin...");

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
         PSJdbcDataTypeMap dataTypeMap =
            new PSJdbcDataTypeMap(
               props.getProperty("DB_BACKEND"),
               props.getProperty("DB_DRIVER_NAME"),
               null);
         conn = RxLogTables.createConnection(props);

         PSJdbcTableSchema csSchema = null;
         csSchema =
            PSJdbcTableFactory.catalogTable(
               conn, dbmsDef, dataTypeMap, CONTENT_STATUS_TABLE, true);

         if (csSchema == null)
         {
            log(
               "null value for tableSchema " + CONTENT_STATUS_TABLE + " table");
            log("Table clean-up aborted");

            return null;
         }

         // Verify that required columns exist
         if (csSchema.getColumn("TITLE") == null ||
               csSchema.getColumn("CONTENTID") == null)
         {
            log("Some required columns do not exist");
            log("Table clean-up aborted");

            return null;
         }

         // Check for <code>null</code> and non-text titles
         // modify them if found
         if (!checkForNullsNonTextAndModify(conn, dbmsDef, csSchema))
         {
            log("No null or non-text titles found.");
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
    * Looks for <code>null</code> and non-text titles in the CONTENTSTATUS table.
    * If <code>null</code> or non-text title is found it is modified to be
    * "title_CONTENTID".
    *
    * @param conn the database connection object, cannot be <code>null</code>.
    * @param dbmsDef the database definition, cannot be <code>null</code>.
    * @param csSchema the table schema object for the CONTENTSTATUS table,
    * cannot be <code>null</code>.
    * @return <code>true</code> if <code>null</code> or non-text titles are found
    * indicating that processing needs to be done.
    */
   private boolean checkForNullsNonTextAndModify(
      final Connection conn, final PSJdbcDbmsDef dbmsDef,
      PSJdbcTableSchema csSchema)
   {
      log("Checking for null and non-text titles so they can be modified.");

      StringBuffer csModifiedRows = new StringBuffer();
      PSJdbcTableData csData = csSchema.getTableData();
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Iterator rows = csData.getRows();
      PSJdbcRowData tRow = null;
      Map nullntRows = new HashMap();
      String title = "";
      String newTitle = "title_";
      String contentid = "0";
      boolean nullntFound = false;
      Statement stmt = null;

      // Scan each row
      while (rows.hasNext())
      {
         tRow = (PSJdbcRowData)rows.next();
         title = tRow.getColumn("TITLE").getValue();
         contentid = tRow.getColumn("CONTENTID").getValue();

         try {

            if ((title == null) || (title.trim().length() == 0))
            {
               // Found <code>null</code> or non-text title
               nullntFound = true;
               stmt = PSSQLStatement.getStatement(conn);
               stmt.executeUpdate("UPDATE " + CONTENT_STATUS_TABLE +
                       " SET TITLE='" + newTitle + contentid + "'" +
                       " WHERE CONTENTID=" + contentid);
               csModifiedRows.append(
                     PSXmlDocumentBuilder.toString(
                        tRow.toXml(doc),
                        PSXmlDocumentBuilder.FLAG_OMIT_XML_DECL));
            }
         }
         catch (SQLException e)
         {
            e.printStackTrace(m_config.getLogStream());
         }
      }

      // Write a log of the rows removed so that the user can
      // find out what was modified
      csModifiedRows.insert(
            0, "<table name=\"" + CONTENT_STATUS_TABLE + "\">\n");
      csModifiedRows.insert(
            0,
            "\n\nThe following rows with null or non-text TITLE column values"
            + " were modified:\n\n");
      csModifiedRows.append("</table>\n\n");

      log(csModifiedRows.toString());

      return nullntFound;
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
   private static final String CONTENT_STATUS_TABLE = "CONTENTSTATUS";
}
