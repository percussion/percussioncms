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
package com.percussion.install;

import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcPlanBuilder;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableSchema;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.percussion.tablefactory.install.RxLogTables;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
/**
 * Plugin class to modify RXLOCATIONSCHEMEPARAMS table.
 * Two new columns have been added to the above table, both are
 * non-nullable columns. The regular installation process creates a backup table
 * but fails to add the data. This plugin takes the data from the backup table
 * and then generates a sequential number for the newly added SCHEMEPARAMID
 * column and values for CONTEXTID column it picks it from LOCATIONSCHEME Table.
 */

public class PSUpgradePluginLocationSchemeTable implements IPSUpgradePlugin
{
   /**
    * Default constructor
    */
   public PSUpgradePluginLocationSchemeTable()
   {
   }

   /**
    * Implements process method of IPSUpgardePlugin.
    */
    public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      config.getLogStream().println("inside the process() of the plugin...");

      FileInputStream in = null;
      Connection conn = null;
      String bkptablename = "RXLOCATIONSCHEMEPARAMS" +
         PSJdbcPlanBuilder.BACKUP_SUFFIX;
      try
      {
         in = new FileInputStream(new File(RxUpgrade.getRxRoot() +
            File.separator + config.REPOSITORY_PROPFILEPATH));
         Properties props = new Properties();
         props.load(in);
         props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
         PSJdbcDataTypeMap dataTypeMap =
            new PSJdbcDataTypeMap(props.getProperty("DB_BACKEND"),
            props.getProperty("DB_DRIVER_NAME"), null);
         conn = RxLogTables.createConnection(props);

         Document paramsDoc = PSUpgradePluginGeneralTables.getTableDataDoc(
            conn, dbmsDef, dataTypeMap, bkptablename);
         if(paramsDoc == null)
         {
            config.getLogStream().println(
            "Could not extract data out of " + bkptablename + " table");
            config.getLogStream().println("Table upgrade aborted");
            return null;
         }

         Document schemesDoc = PSUpgradePluginGeneralTables.getTableDataDoc(
            conn, dbmsDef, dataTypeMap, "RXLOCATIONSCHEME");
         if(schemesDoc == null)
         {
            config.getLogStream().println(
               "Could not extract data out of RXLOCATIONSCHEME table");
            config.getLogStream().println("Table upgrade aborted");
            return null;
         }

         NodeList paramRows = paramsDoc.getElementsByTagName("row");
         if(paramRows == null || paramRows.getLength() < 1)
         {
            config.getLogStream().println(
               "No data in " + bkptablename + " table");
            config.getLogStream().println("Table upgrade aborted");
            return null;
         }

         NodeList schemeRows = schemesDoc.getElementsByTagName("row");
         if(schemeRows == null || schemeRows.getLength() < 1)
         {
            config.getLogStream().println("No data in RXLOCATIONSCHEME table");
            config.getLogStream().println("Table upgrade aborted");
            return null;
         }

         Element elem = null;
         Element temp = null;
         String value = null;
         Text text = null;
         for(int i=0; i<paramRows.getLength(); i++)
         {
            elem = (Element)paramRows.item(i);
            //Add column SCHEMEPARAMID
            temp = paramsDoc.createElement("column");
            temp.setAttribute("name", "SCHEMEPARAMID");
            try
            {
               value = Integer.toString(i+1);
            }
            catch(Throwable t)
            {
            }
            text = paramsDoc.createTextNode(value);
            temp.appendChild(text);
            elem.appendChild(temp);

            //Add column CONTEXTID
            temp = paramsDoc.createElement("column");
            temp.setAttribute("name", "CONTEXTID");
            value = getContextId(elem, schemeRows);
            text = paramsDoc.createTextNode(value);
            temp.appendChild(text);
            elem.appendChild(temp);
         }
         PSJdbcTableSchema tableSchema = null;
         tableSchema = PSJdbcTableFactory.catalogTable(conn, dbmsDef,
            dataTypeMap, "RXLOCATIONSCHEMEPARAMS", true);
         if(tableSchema == null)
         {
            config.getLogStream().println(
               "nulll value for tableSchema RXLOCATIONSCHEMEPARAMS table");
            config.getLogStream().println("Table upgrade aborted");
            return null;
         }

         paramsDoc.getDocumentElement().setAttribute("onCreateOnly", "no");
         PSJdbcTableData tableData =
            new PSJdbcTableData(paramsDoc.getDocumentElement());
         tableSchema.setTableData(tableData);

         PSJdbcTableFactory.processTable(
            conn, dbmsDef, tableSchema, config.getLogStream(), false);
      }
      catch(Exception e)
      {
         e.printStackTrace(config.getLogStream());
      }
      finally
      {
         try
         {
            if(in != null)
            {
               in.close();
               in =null;
            }
         }
         catch(Throwable t)
         {
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
            conn = null;
         }
      }
      config.getLogStream().println("leaving the process() of the plugin...");
      return null;
   }

   /**
    * Helper function returns contextid.
    * @param elemSchemeParam
    * @param nlSchemes rows with the following DTD
    * <row>
         <column name="SCHEMEID">1</column>
         <column name="SCHEMENAME">Article</column>
         <column name="DESCRIPTION">Article scheme</column>
         <column name="VARIANTID">1</column>
         <column name="CONTENTTYPEID">1</column>
         <column name="CONTEXTID">1</column>
         <column name="GENERATOR">Java/global/percussion</column>
      </row>

    */
   private String getContextId(Element elemSchemeParam, NodeList nlSchemes)
   {
      NodeList nl = elemSchemeParam.getElementsByTagName("column");
      Element column = null;
      String schemeid = null;
      Node node = null;
      for(int i=0; nl!=null && i<nl.getLength(); i++)
      {
         column = (Element)nl.item(i);
         if(column.getAttribute("name").equalsIgnoreCase("SCHEMEID"))
         {
            node = column.getFirstChild();
            if(node != null && node instanceof Text)
            {
               schemeid = ((Text)node).getData();
               break;
            }
         }
      }
      if(schemeid == null)
         return null;
      else
         schemeid = schemeid.trim();

      Element row = null;
      String contextid = null;
      String temp = null;
      boolean found = false;
      for(int j=0; nlSchemes!=null && j<nlSchemes.getLength(); j++)
      {
         row = (Element)nlSchemes.item(j);
         nl = row.getElementsByTagName("column");
         for(int i=0; nl!=null && i<nl.getLength(); i++)
         {
            column = (Element)nl.item(i);
            if(column.getAttribute("name").equalsIgnoreCase("CONTEXTID"))
            {
               node = column.getFirstChild();
               if(node != null && node instanceof Text)
               {
                  contextid = ((Text)node).getData();
               }
            }

            if(column.getAttribute("name").equalsIgnoreCase("SCHEMEID"))
            {
               node = column.getFirstChild();
               if(node != null && node instanceof Text)
               {
                  temp = ((Text)node).getData();
                  if(temp!= null && schemeid.equals(temp.trim()))
                  {
                     found = true;
                  }
               }
            }
         }
         if(found)
            break;
      }
      return contextid;
   }
}
