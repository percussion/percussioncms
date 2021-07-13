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
package com.percussion.tablefactory.tools;

import com.percussion.tablefactory.PSJdbcColumnDef;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcImportExportHelper;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Uses the table factory to export a given database table's definition (schema)
 * and data (content) as XML files.
 */ 
public class PSCatalogTableData
{

   private static final Logger log = LogManager.getLogger(PSCatalogTableData.class);

   /**
    * Creates a <code>PSJdbcDbmsDef</code> by loading the specified properties 
    * file.  Assumes that the password in the properties file is encrypted.
    * 
    * @param propsName name of a properties file that provides database
    * connection information, not <code>null</code>.
    * See {@link PSJdbcDbmsDef#PSJdbcDbmsDef(Properties)}
    * for a list of the required properties.
    * 
    * @throws IllegalArgumentException if the properties file is missing 
    * required properties.
    * @throws IOException if the file referenced by <code>propsName</code> does
    * not exist, or if errors occur reading the file
    * @throws PSJdbcTableFactoryException if an error occurs while decrypting
    * the database password specified in the properties file.
    */
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   public static PSJdbcDbmsDef loadProps(String propsName)
      throws PSJdbcTableFactoryException, IOException
   {
      if (propsName == null)
         throw new IllegalArgumentException("propsName may not be null");
      
      FileInputStream in = null;
      try
      {
         File propsFile = new File( propsName );
         Properties props = new Properties();
         in = new FileInputStream( propsFile );
         props.load( in );
         /* This utility class is designed to be used with the workflow 
            properties file, which should always have an encrypted password.  
            So force that setting if the flag isn't set. */
         if (props.getProperty("PWD_ENCRYPTED") == null)
         {
            props.setProperty( "PWD_ENCRYPTED", "Y" );
         }
         PSJdbcDbmsDef def = new PSJdbcDbmsDef( props );
         return def;
      }
      finally
      {
         if (in != null)
            try
            {
               in.close();
            } catch (IOException e)
            {
            }
      }
   }


   /**
    * Gets the schema and data for the specified table by connecting to the
    * specified database using the table factory.
    * 
    * @param def configuration for the table factory; specifies the database
    * to connect to; not <code>null</code>
    * @param tableName the name of the table to be cataloged, not 
    * <code>null</code> or empty.
    * 
    * @return the definition and data for the specified table.  Will be
    * <code>null</code> if the specified table does not exist.
    * 
    * @throws IOException propagated if the default data type map cannot be 
    * loaded
    * @throws SAXException propagated if an error occurs parsing the default
    * data type map
    * @throws SQLException propagated if a connection could not be established
    * to the database
    * @throws PSJdbcTableFactoryException if the table could not be cataloged
    */
   public static PSJdbcTableSchema catalogTable(PSJdbcDbmsDef def,
                                                 String tableName)
      throws PSJdbcTableFactoryException, IOException, SQLException,
      SAXException
   {
      if (def == null)
         throw new IllegalArgumentException( "PSJdbcDbmsDef may not be null" );

      if (tableName == null || tableName.trim().length() == 0)
         throw new IllegalArgumentException(
            "tableName may not be null or empty" );

      PSJdbcDataTypeMap map = new PSJdbcDataTypeMap( null, def.getDriver(),
         null );

      Connection conn = PSJdbcTableFactory.getConnection( def );
      return PSJdbcTableFactory.catalogTable(conn, def, map, tableName, true);
   }


   /**
    * Gets the name of the JDBC driver defined in the DB_DRIVER_NAME property
    * within the specified properties file.
    * 
    * @param propsName name of a properties file that provides database
    * connection information, not <code>null</code>.
    * See {@link PSJdbcDbmsDef#PSJdbcDbmsDef(Properties)}
    * for a list of the required properties.
    * 
    * @return the name of the JDBC driver defined in the properties file,
    * never <code>null</code> or empty.
    * 
    * @throws IOException if the file referenced by <code>propsName</code> does
    * not exist, or if errors occur reading the file
    * @throws PSJdbcTableFactoryException if an error occurs while decrypting
    * the database password specified in the properties file.
    */
   public static String getDriver(String propsName)
      throws IOException, PSJdbcTableFactoryException
   {
      if (propsName == null || propsName.trim().length() == 0)
         throw new IllegalArgumentException(
            "property file name must be not null and not empty" );

      PSJdbcDbmsDef def = loadProps( propsName );
      return def.getDriver();
   }


   /**
    * Gets the name of the database defined in the DB_NAME property
    * within the specified properties file.
    * 
    * @param propsName name of a properties file that provides database
    * connection information, not <code>null</code>. 
    * See {@link PSJdbcDbmsDef#PSJdbcDbmsDef(Properties)}
    * for a list of the required properties.
    * 
    * @return the name of the database defined in the properties file,
    * may be <code>null</code> or empty.
    * 
    * @throws IOException if the file referenced by <code>propsName</code> does
    * not exist, or if errors occur reading the file
    * @throws PSJdbcTableFactoryException if an error occurs while decrypting
    * the database password specified in the properties file.
    */
   public static String getDataBase(String propsName)
      throws IOException, PSJdbcTableFactoryException
   {
      if (propsName == null || propsName.trim().length() == 0)
         throw new IllegalArgumentException(
            "property file name must be not null and not empty" );

      PSJdbcDbmsDef def = loadProps( propsName );
      return def.getDataBase();
   }



   /**
    * Command line interface for this tool.
    * For parameters see {@link PSCatalogTableData#usage()}
    * for a list of the required properties.
    * @param args
    */
   public static void main(String[] args)
   {
      if (args.length > 0 && (PSJdbcImportExportHelper.OPTION_DB_EXPORT.equals(args[0]) || 
            PSJdbcImportExportHelper.OPTION_DB_IMPPORT.equals(args[0]))) {
         mainExport(args);
         return;
      }
      if (args.length < 4)
         usage();

      String propsName = args[0];
      String tablesToExport = args[1];
      
      File tablesToExportFile = new File(tablesToExport);
      File schemaFile = new File( args[2] );
      File dataFile = new File( args[3] );
      BufferedWriter bo = null;
      
      FileOutputStream out = null;
      PSJdbcDbmsDef dbdef = null;
      try
      {
         dbdef = loadProps( propsName );
         
         if (!tablesToExportFile.exists())
         {
            Document tablesToExportDoc = generateTablesDocument(dbdef);
            
            bo = new BufferedWriter(
                  new OutputStreamWriter(new FileOutputStream( tablesToExportFile ),StandardCharsets.UTF_8));
            
            PSXmlDocumentBuilder.write( tablesToExportDoc, bo );
            bo.close();
            
            System.out.println( "Created tablesToExport file with a list of table names." );
            System.out.println( "Rerun this tool to catalog the actual table data." );
            
            return;
         }
         
         Document docTablesToExport = 
            PSXmlDocumentBuilder.createXmlDocument(
            new FileInputStream(new File(tablesToExport)), false);
            
         generateSchemaAndDataFiles(schemaFile, dataFile, dbdef, docTablesToExport, false);
         System.exit( 0 );
      } catch (Exception e)
      {
         System.out.println( "Unexpected error: " );
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         System.exit( 1 );
      } finally
      {
         if (dbdef!=null)
         {
            Connection conn;
            try
            {
               conn = dbdef.getConnection();
               if (conn!=null)
                  conn.close();
            }
            catch (Exception e)
            {
            }
         }
         
         if (out != null)
            try
            {
               out.close();
            } catch (IOException e)
            {
            }
      }
   }


   private static void generateSchemaAndDataFiles(File schemaFile, File dataFile, PSJdbcDbmsDef dbdef,
         Document docTablesToExport, boolean saveIndividual)
         throws PSJdbcTableFactoryException, IOException, SQLException, SAXException, FileNotFoundException
   {
      BufferedWriter out;
      PSXmlTreeWalker walker = new PSXmlTreeWalker(docTablesToExport);
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element table = walker.getNextElement(PSJdbcTableSchema.NODE_NAME,
         firstFlags);
      if (table == null)
      {
         usage();
         return; //must have at least one table            
      }

      Document outDefDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element outDefRoot = outDefDoc.createElement("tables");
      outDefDoc.appendChild(outDefRoot);
      
      Document outDataDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element outDataRoot = outDataDoc.createElement("tables");
      outDataDoc.appendChild(outDataRoot);
      
      int count = 0;
      List<PSJdbcTableSchema> schemas = new ArrayList<PSJdbcTableSchema>();
      while (table != null)
      {

         String tableName = table.getAttribute("name");

         //Skip table if it is listed to skip
          if(PSJdbcImportExportHelper.tablesToSkip.contains(tableName)) {
             System.out.println("Skipping table " + tableName);
             table = walker.getNextElement(PSJdbcTableSchema.NODE_NAME, nextFlags);
             continue;
          }

         PSJdbcTableSchema schema = catalogTable( dbdef, tableName );
         if (schema == null)
         {
            System.err.println("failed to catalog table: " + tableName);
         }
         else
         {
            count++;
            //if the limit size attribute value needs to be set to true.
            if (PSJdbcImportExportHelper.limitSizeForIndexMap.containsKey(tableName)){
               updateSchemaLimitSize(schema, tableName);
            }
            schemas.add(schema);

            System.out.println("Processing table " + tableName + "...");
            PSJdbcTableData data = schema.getTableData();
            if (data != null) {
               if (saveIndividual) {
                  Document dataDoc = PSXmlDocumentBuilder.createXmlDocument();
                  Element dataRoot = dataDoc.createElement("tables");
                  dataDoc.appendChild(dataRoot);
                  dataRoot.appendChild(dataDoc.importNode(
                        data.toXml(dataDoc), true));
                  File indDataFile = new File(dataFile.getParentFile(), tableName + "-data.xml");
                   out = new BufferedWriter
                        (new OutputStreamWriter(new FileOutputStream(indDataFile), StandardCharsets.UTF_8));
           
                  PSXmlDocumentBuilder.write( dataDoc, out );
                  out.close();
               }
               else {
                  outDataRoot.appendChild(outDataDoc.importNode(
                        data.toXml(outDataDoc), true));
               }
            }
         }
         table = walker.getNextElement(PSJdbcTableSchema.NODE_NAME, nextFlags);
      }
      Collections.sort(schemas);
      for (PSJdbcTableSchema schema : schemas) {
         outDefRoot.appendChild(outDefDoc.importNode(
               schema.toXml(outDefDoc), true));
      }
      
      out = new BufferedWriter
            (new OutputStreamWriter(new FileOutputStream( schemaFile ), StandardCharsets.UTF_8));
      PSXmlDocumentBuilder.write( outDefDoc, out );
      out.close();
      if (!saveIndividual) {
         out = new BufferedWriter
               (new OutputStreamWriter(new FileOutputStream( dataFile), StandardCharsets.UTF_8));
         PSXmlDocumentBuilder.write( outDataDoc, out );
         out.close();
      }
      System.out.println( "Completed data serialization from " + count + " tables." );
   }

   /**
    * Helper method that sets the limitSizeForIndex attribute value to true to handle MySql column index sizes.
    * This is based on the settings.
    * @param schema assumed not <code>null</code>
    * @param tableName assumed not <code>null</code>
    */
   private static void updateSchemaLimitSize(PSJdbcTableSchema schema, String tableName)
   {
      String temp = PSJdbcImportExportHelper.limitSizeForIndexMap.get(tableName);
      String[] columns = temp.split(":");
      for (String column : columns) {
         PSJdbcColumnDef colDef = schema.getColumn(column);
         colDef.setLimitSizeForIndex(true);
      }
   }


   private static Document generateTablesDocument(PSJdbcDbmsDef dbdef)
         throws SQLException, PSJdbcTableFactoryException
   {
      //catalog all tables and output a tablesToExport xml file
      
      //tablesToExport DTD is similar to table def / data, see example:
      /*
      <?xml version="1.0" encoding="UTF-8"?>
      <tables>
         <table name="T1"/>
         <table name="T2"/>
      <tables>
      */
      
      Document tablesToExportDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element tablesToExportRoot = tablesToExportDoc.createElement("tables");
      tablesToExportDoc.appendChild(tablesToExportRoot);
      
      Collection tables = PSJdbcTableFactory.catalogTables(dbdef, "%");
      Iterator iter = tables.iterator();
      while (iter.hasNext())
      {
         String tName = (String) iter.next();
         Element tn = tablesToExportDoc.createElement("table");
         tn.setAttribute("name", tName);
         tablesToExportRoot.appendChild(tn);
      }
      return tablesToExportDoc;
   }

   public static void mainExport(String[] args) {
      Map<String, String> optionsMap = PSJdbcImportExportHelper.getOptions(args);
      PSJdbcDbmsDef dbdef = null;
      try {
         //Props file
         dbdef = loadProps(optionsMap.get(PSJdbcImportExportHelper.OPTION_DB_PROPS) );
         
         //Storage location
         String storagePath = optionsMap.get(PSJdbcImportExportHelper.OPTION_STORAGE_PATH);
         File binaryStorageFolder = new File(storagePath   + File.separator + PSJdbcImportExportHelper.BINARY_DATA_FOLDER +
                  File.separator + PSJdbcImportExportHelper.BINARY_DATA_INITIAL_BUCKET);
         binaryStorageFolder.mkdirs();
         dbdef.setBinaryStorageLocation(binaryStorageFolder);
         
         File defDataFolder = new File(storagePath + File.separator + PSJdbcImportExportHelper.DEF_DATA_FOLDER);
         defDataFolder.mkdirs();
         
         Document docTablesToExport = generateTablesDocument(dbdef);
         
         File tableDef = new File(defDataFolder + File.separator + "tableDef.xml");
         File tableData = new File(defDataFolder + File.separator + "tableData.xml");
         
         generateSchemaAndDataFiles(tableDef, tableData, dbdef, docTablesToExport, true);
         
         System.exit(0);
      }
      catch (Exception e) {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }

   /**
    * Prints the parameters required by the <code>main</code> method to stdout, 
    * and terminates with a nonzero status code.
    */
   private static void usage()
   {
      System.out.println( "Usage:" );
      System.out.println( "java com.percussion.tablefactory.toos.PSCatalogTableData <props> <tablesToExport> <schemaFile> <dataFile>" );
      System.out.println( "where:" );
      System.out.println( "props - path to dbms props file" );
      System.out.println( "tablesToExport - path to xml file that contains table names to export; simple dtd similar to the table def;" );
      System.out.println( "i.e.: <tables><table name='tableName'/></tables> " );
      System.out.println( "note: if this file doesn't exist it creates it by cataloging all the table names in a given schema and exits." );
      System.out.println( "schemaFile - path to xml file to which schema results are written" );
      System.out.println( "dataFile - path to xml file to which data results are written" );
      System.exit( 1 );
   }
}
