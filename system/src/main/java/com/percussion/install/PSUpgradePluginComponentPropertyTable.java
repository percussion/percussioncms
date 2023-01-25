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
package com.percussion.install;

import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcPlanBuilder;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.tablefactory.install.RxLogTables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Plugin class to modify RXSYSCOMPONENTPROPERTY table.
 * Two new columns have been added to the above table, one of which is
 * non-nullable column. The regular installation process creates a backup table
 * but fails to add the data. This plugin takes the data from the backup table
 * and then generates a sequential number for the newly added PROPERTYID column
 */
public class PSUpgradePluginComponentPropertyTable implements IPSUpgradePlugin
{
   /**
    * Default constructor
    */
   public PSUpgradePluginComponentPropertyTable()
   {
   }

   /**
    * Implements process method of IPSUpgardePlugin.
    */
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      FileInputStream in = null;
      String bkptablename = "RXSYSCOMPONENTPROPERTY" +
         PSJdbcPlanBuilder.BACKUP_SUFFIX;
      Connection conn = null;
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

         Document propsDoc = PSUpgradePluginGeneralTables.getTableDataDoc(conn,
            dbmsDef, dataTypeMap, bkptablename);
         if(propsDoc == null)
         {
            config.getLogStream().println("Could not extract data out "
               + "of " + bkptablename + " table");
            config.getLogStream().println("Table upgrade aborted");
            return null;
         }

         NodeList propRows = propsDoc.getElementsByTagName("row");
         if(propRows == null || propRows.getLength() < 1)
         {
            config.getLogStream().println(
               "No data in " + bkptablename + " table");
            config.getLogStream().println("Table upgrade aborted");
            return null;
         }

         Element elem = null;
         Element temp = null;
         String value = null;
         Text text = null;
         for(int i=0; i<propRows.getLength(); i++)
         {
            elem = (Element)propRows.item(i);
            //Add column PROPERTYID
            temp = propsDoc.createElement("column");
            temp.setAttribute("name", "PROPERTYID");
            try
            {
               value = Integer.toString(i+1);
            }
            catch(Throwable t)
            {
            }
            text = propsDoc.createTextNode(value);
            temp.appendChild(text);
            elem.appendChild(temp);
         }
         PSJdbcTableSchema tableSchema = null;
         tableSchema = PSJdbcTableFactory.catalogTable(conn, dbmsDef,
            dataTypeMap, "RXSYSCOMPONENTPROPERTY", true);
         if(tableSchema == null)
         {
            config.getLogStream().println(
               "null value for tableSchema RXSYSCOMPONENTPROPERTY table");
            config.getLogStream().println("Table upgrade aborted");
            return null;
         }

         propsDoc.getDocumentElement().setAttribute("onCreateOnly", "no");
         PSJdbcTableData tableData =
            new PSJdbcTableData(propsDoc.getDocumentElement());
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
}
