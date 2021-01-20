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
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableDataCollection;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.tablefactory.PSJdbcTableSchemaCollection;
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This is a general plugin class implements IPSUpgradePlugin.
 */
public class PSUpgradePluginGeneralTables implements IPSUpgradePlugin
{
   /**
    * Default Constructor.
    */
   public PSUpgradePluginGeneralTables()
   {
   }

   /**
    * Implements process method of IPSUpgradePlugin.
    * @param config IPSUpgradeModule object.
    *    may not be <code>null<code>.
    * @param elemData data element of plugin.
    * @return <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      config.getLogStream().println(
         "Running Rhythmyx repository upgrade process...");

      NodeList nl = elemData.getElementsByTagName("tableset");
      FileInputStream in = null;
      Connection conn = null;
      try
      {
         in = new FileInputStream(new File(RxUpgrade.getRxRoot() +
            "rxconfig/Installer/rxrepository.properties"));
         Properties props = new Properties();
         props.load(in);
         props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
         PSJdbcDataTypeMap dataTypeMap = new PSJdbcDataTypeMap(
            props.getProperty("DB_BACKEND"),
            props.getProperty("DB_DRIVER_NAME"), null);
         conn = RxLogTables.createConnection(props);
         ArrayList tableList =  null;
         ArrayList schemaXslList =  null;
         ArrayList dataXslList =  null;
         PSJdbcTableSchemaCollection schemaColl = null;
         PSJdbcTableDataCollection dataColl = null;
         Document schemaDoc = null;
         Document dataDoc = null;
         for(int j=0; nl!=null && j<nl.getLength(); j++)
         {
            /*Get tables names as arraylist.*/
            tableList =  InstallUtil.getValueList(
               InstallUtil.getElement(nl.item(j), "tables"),"table");
            /*Get schema xsl file names as arraylist.*/
            schemaXslList =  InstallUtil.getValueList(
               InstallUtil.getElement(nl.item(j), "transformschema"),"xsl");
            /*Get data xsl file names as arraylist.*/
            dataXslList =  InstallUtil.getValueList(
               InstallUtil.getElement(nl.item(j), "transformdata"),"xsl");
            try
            {
               schemaDoc = PSXmlDocumentBuilder.createXmlDocument();
               dataDoc = PSXmlDocumentBuilder.createXmlDocument();
               getTableSchemaAndDataDoc(config, conn, dbmsDef, dataTypeMap,
                  tableList, schemaDoc, dataDoc);

               for(int i=0; i<schemaXslList.size();i++)
               {
                   schemaDoc = RxUpgrade.transformXML(schemaDoc,
                     RxUpgrade.getUpgradeRoot() +
                     schemaXslList.get(i).toString());
               }

               for(int i=0; i<dataXslList.size();i++)
               {
                   dataDoc = RxUpgrade.transformXML(dataDoc,
                     RxUpgrade.getUpgradeRoot() +
                     dataXslList.get(i).toString());
               }

               schemaColl = new PSJdbcTableSchemaCollection(
                  schemaDoc, dataTypeMap);
               dataColl = new PSJdbcTableDataCollection(dataDoc);
               schemaColl.setTableData(dataColl);
               PSJdbcTableFactory.processTables(conn, dbmsDef, schemaColl,
                  config.getLogStream(), false);

            }
            catch(Exception e)
            {
               e.printStackTrace(config.getLogStream());
            }
         }
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
         config.getLogStream().println(
            "leaving the process() of the plugin...");
      }
      return null;
   }

   /**
    * Helper function to extract the schemaDoc and dataDoc from the database.
    * @param config module from configuration file.
    * @param conn a valid database connection.
    * @param dbmsDef Used to connect to the database and provides correct
    * schema/origin.
    * @param dataTypeMap The dataType map to use for this table's columns.
    * @param tablelist array of table names
    * @param schemaDoc table schema document
    * @param dataDoc table data document
    */
   static public void getTableSchemaAndDataDoc(IPSUpgradeModule config,
         Connection conn, PSJdbcDbmsDef dbmsDef, PSJdbcDataTypeMap dataTypeMap,
         ArrayList tableList, Document schemaDoc, Document dataDoc)
      throws PSJdbcTableFactoryException
   {
      PSJdbcTableDataCollection collData = new PSJdbcTableDataCollection();
      PSJdbcTableSchemaCollection collSchema =
         new PSJdbcTableSchemaCollection();
      PSJdbcTableSchema tableSchema = null;
      PSJdbcTableData tableData = null;
      ArrayList<PSJdbcTableSchema> schemasToSort = new ArrayList();
      for(int i=0;i<tableList.size();i++)
      {
         config.getLogStream().println("catalogging table: " +
            tableList.get(i).toString());
         tableSchema = PSJdbcTableFactory.catalogTable(conn, dbmsDef,
            dataTypeMap, tableList.get(i).toString(), true);
         if(tableSchema == null)
            continue;
         schemasToSort.add(tableSchema);
      }

       Collections.sort(schemasToSort);

       for (PSJdbcTableSchema sortedSchema : schemasToSort)
       {
           collSchema.add(sortedSchema);

           tableData = sortedSchema.getTableData();
           if(tableData == null)
               continue;
           collData.add(tableData);

       }

      schemaDoc.appendChild(collSchema.toXml(schemaDoc));
      dataDoc.appendChild(collData.toXml(dataDoc));
   }

   /**
    * Helper function prepares the dataDoc and returns.
    * @param conn a valid database connection.
    * @param dbmsDef Used to connect to the database and provides correct
    * schema/origin.
    * @param dataTypeMap The dataType map to use for this table's columns.
    * @param tableName name of the table for which the data is required.
    */
   static public Document getTableDataDoc(Connection conn,
      PSJdbcDbmsDef dbmsDef, PSJdbcDataTypeMap dataTypeMap, String tableName)
      throws PSJdbcTableFactoryException
   {
      PSJdbcTableSchema tableSchema = null;
      PSJdbcTableData tableData = null;
      tableSchema = PSJdbcTableFactory.catalogTable(conn, dbmsDef,
         dataTypeMap, tableName, true);
      if(tableSchema == null)
         return null;
      tableData = tableSchema.getTableData();
      if(tableData == null)
         return null;
      Document dataDoc = PSXmlDocumentBuilder.createXmlDocument();
      dataDoc.appendChild(tableData.toXml(dataDoc));

      return dataDoc;
   }
}
