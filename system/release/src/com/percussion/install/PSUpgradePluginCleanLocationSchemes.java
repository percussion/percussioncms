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

import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.xml.PSXmlDocumentBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;


/**
 * This upgrade plug-in will clean out (delete) non-unique location
 * schemes from the RXLOCATIONSCHEME and RXLOCATIONSCHEMEPARAMS tables. It
 * will also writes to the log file showing any table rows that were deleted so that
 * a user can rebuild the scheme if needed.
 */
public class PSUpgradePluginCleanLocationSchemes implements IPSUpgradePlugin
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

         PSJdbcTableSchema lsSchema = null;
         lsSchema =
            PSJdbcTableFactory.catalogTable(
               conn, dbmsDef, dataTypeMap, LOCATION_SCHEME_TABLE, true);

         if (lsSchema == null)
         {
            log(
               "null value for tableSchema " + LOCATION_SCHEME_TABLE + " table");
            log("Table clean-up aborted");

            return null;
         }

         // Verify that required columns exist
         if (
            (lsSchema.getColumn("SCHEMEID") == null)
               || (lsSchema.getColumn("VARIANTID") == null)
               || (lsSchema.getColumn("CONTEXTID") == null))
         {
            log("Some required columns do not exist");
            log("Table clean-up aborted");

            return null;
         }

         PSJdbcTableSchema paramsSchema = null;
         paramsSchema =
            PSJdbcTableFactory.catalogTable(
               conn, dbmsDef, dataTypeMap, LOCATION_SCHEME_PARAMS_TABLE, true);

         if (paramsSchema == null)
         {
            log(
               "null value for tableSchema " + LOCATION_SCHEME_PARAMS_TABLE
               + " table");
            log("Table clean-up aborted");

            return null;
         }

         // Check for non-unique location schemes and
         // delete them if found
         if (!checkForDupsAndModify(conn, dbmsDef, lsSchema, paramsSchema))
         {
            log("No non-unique location schemes found.");
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
    * Looks for non-unique entries in the RXLOCATIONSCHEME table, a non-unique
    * entry is one that violates the unique compound key conatraint
    * (VARIANTID, CONTEXTID). If non-uniques are found it prepares the
    * appropriate table data entries to delete the non-unique entries from
    * the RXRXLOCATIONSCHEMEPARAM table. It then sets the table data entries into
    * the passed in schema so they can be processed by the caller.
    *
    * @param schemeDoc the table schema object for the RXLOCATIONSCHEME,
    * cannot be <code>null</code>.
    * @param paramsDoc the table schema object for the RXLOCATIONSCHEMEPARAMS,
    * cannot be <code>null</code>.
    * @return <code>true</code> if non-unique entries are found indicating
    * that processing needs to be done.
    */
   private boolean checkForDupsAndModify(
      final Connection conn, final PSJdbcDbmsDef dbmsDef,
      PSJdbcTableSchema lsSchema, PSJdbcTableSchema paramsSchema)
   {
      log("Checking for non-unique location schemes so they can be deleted.");

      List schemeIds = new ArrayList();
      List schemeKeys = new ArrayList();
      List lsDeleteData = new ArrayList();
      List paramsDeleteData = new ArrayList();
      StringBuffer lsDeletedRows = new StringBuffer();
      StringBuffer paramsDeletedRows = new StringBuffer();
      PSJdbcTableData lsData = lsSchema.getTableData();
      PSJdbcTableData paramsData = paramsSchema.getTableData();
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Iterator rows = paramsData.getRows();
      PSJdbcRowData tRow = null;
      Map paramsKeys = new HashMap();

      while (rows.hasNext())
      {
         tRow = (PSJdbcRowData)rows.next();
         paramsKeys.put(
            tRow.getColumn("SCHEMEPARAMID").getValue(),
            tRow);
      }

      rows = lsData.getRows();

      String schemeId = null;
      String variantId = null;
      String contextId = null;
      SchemeKey sk = null;

      while (rows.hasNext())
      {
         tRow = (PSJdbcRowData)rows.next();
         schemeId = tRow.getColumn("SCHEMEID").getValue();
         variantId = tRow.getColumn("VARIANTID").getValue();
         contextId = tRow.getColumn("CONTEXTID").getValue();

         sk = new SchemeKey(variantId, contextId);

         if (schemeKeys.contains(sk))
         {
            // Found dupe
            lsDeletedRows.append(
               PSXmlDocumentBuilder.toString(
                  tRow.toXml(doc),
                  PSXmlDocumentBuilder.FLAG_OMIT_XML_DECL));
            schemeIds.add(schemeId);

            List cols = new ArrayList();
            cols.add(new PSJdbcColumnData("SCHEMEID", schemeId));
            lsDeleteData.add(
               new PSJdbcRowData(
                  cols.iterator(),
                  PSJdbcRowData.ACTION_DELETE));

            // Setup to delete all LOCATIONSCHEMEPARAMS rows with
            // this SCHEMEID
            Iterator keys = paramsKeys.keySet().iterator();

            while (keys.hasNext())
            {
               String schemeparamid = (String)keys.next();
               PSJdbcRowData theRow =
                  (PSJdbcRowData)paramsKeys.get(schemeparamid);
               String value = theRow.getColumn("SCHEMEID").getValue();
               cols.clear();

               if ((value != null) && value.equals(schemeId))
               {
                  paramsDeletedRows.append(
                     PSXmlDocumentBuilder.toString(
                        theRow.toXml(doc),
                        PSXmlDocumentBuilder.FLAG_OMIT_XML_DECL));
                  cols.add(
                     new PSJdbcColumnData("SCHEMEPARAMID", schemeparamid));
                  paramsDeleteData.add(
                     new PSJdbcRowData(
                        cols.iterator(),
                        PSJdbcRowData.ACTION_DELETE));
               }
            }
         }
         else
         {
            schemeKeys.add(sk);
         }
      }

      if (schemeIds.isEmpty())
      {
         return false;
      }
      else
      {
         try
         {
            PSJdbcTableData tData = null;

            if (!paramsDeleteData.isEmpty())
            {
               tData =
                  new PSJdbcTableData(
                     LOCATION_SCHEME_PARAMS_TABLE,
                     paramsDeleteData.iterator());
               tData.setOnCreateOnly(false);
               paramsSchema.setTableData(tData);
               PSJdbcTableFactory.processTable(
                  conn, dbmsDef, paramsSchema, System.out, false);
            }

            log("Removing non-unique location schemes.");
            tData =
               new PSJdbcTableData(
                  LOCATION_SCHEME_TABLE,
                  lsDeleteData.iterator());
            tData.setOnCreateOnly(false);
            lsSchema.setTableData(tData);

            PSJdbcTableFactory.processTable(
               conn, dbmsDef, lsSchema, System.out, false);

            // Write a log of the rows removed so that they can
            // be recreated if necessary
            lsDeletedRows.insert(
               0, "<table name=\"" + LOCATION_SCHEME_TABLE + "\">\n");
            lsDeletedRows.insert(
               0,
               "\n\nThe following non-unique location scheme"
               + " rows were deleted:\n\n");
            lsDeletedRows.append("</table>\n\n");

            if (paramsDeletedRows.length() > 0)
            {
               paramsDeletedRows.insert(
                  0, "<table name=\"" + LOCATION_SCHEME_PARAMS_TABLE + "\">\n");
               paramsDeletedRows.append("</table>\n\n");
               lsDeletedRows.append(paramsDeletedRows.toString());
            }

            String path =
               RxUpgrade.getRxRoot() + File.separator + LOG_FILE_NAME;
            log(lsDeletedRows.toString());
         }
         catch (PSJdbcTableFactoryException e)
         {
            e.printStackTrace(m_config.getLogStream());
         }

         return true;
      }
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

   /**
    * Class that represents the location scheme's compound key
    *
    */
   private class SchemeKey
   {
      private SchemeKey(String variantId, String contextId)
      {
         mi_variantId = variantId;
         mi_contextId = contextId;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof SchemeKey)) return false;
         SchemeKey schemeKey = (SchemeKey) o;
         return Objects.equals(mi_variantId, schemeKey.mi_variantId) &&
                 Objects.equals(mi_contextId, schemeKey.mi_contextId);
      }

      @Override
      public int hashCode() {
         return Objects.hash(mi_variantId, mi_contextId);
      }

      public String mi_variantId;
      public String mi_contextId;
   }

   private IPSUpgradeModule m_config;
   private static final String LOCATION_SCHEME_TABLE = "RXLOCATIONSCHEME";
   private static final String LOCATION_SCHEME_PARAMS_TABLE =
      "RXLOCATIONSCHEMEPARAMS";
   private static final String LOG_FILE_NAME = "DeletedRxLocationSchemes.txt";
}
